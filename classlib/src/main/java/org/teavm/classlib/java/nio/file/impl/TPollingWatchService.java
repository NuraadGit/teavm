/*
 *  Copyright 2026 N_uraad.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.nio.file.impl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.teavm.classlib.java.nio.file.TClosedWatchServiceException;
import org.teavm.classlib.java.nio.file.TStandardWatchEventKinds;
import org.teavm.classlib.java.nio.file.TWatchEvent;
import org.teavm.classlib.java.nio.file.TWatchKey;
import org.teavm.classlib.java.nio.file.TWatchService;
import org.teavm.classlib.java.nio.file.TWatchable;
import org.teavm.runtime.fs.VirtualFile;

final class TPollingWatchService implements TWatchService {
    private static final long SCAN_INTERVAL_MILLIS = 1000;

    private final List<PollingKey> keys = new ArrayList<>();
    private final ArrayDeque<PollingKey> signalled = new ArrayDeque<>();
    private boolean closed;
    private long lastScan;

    PollingKey register(TDefaultPath dir, TWatchEvent.Kind<?>[] events) throws IOException {
        if (closed) {
            throw new TClosedWatchServiceException();
        }
        for (int i = 0, l = keys.size(); i < l; ++i) {
            PollingKey key = keys.get(i);
            if (key.dir.pathString.equals(dir.pathString)) {
                key.setEvents(events);
                return key;
            }
        }
        PollingKey key = new PollingKey(this, dir, events);
        keys.add(key);
        return key;
    }

    private void scanIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastScan < SCAN_INTERVAL_MILLIS && now >= lastScan) {
            return;
        }
        lastScan = now;
        for (int i = 0; i < keys.size(); ++i) {
            PollingKey key = keys.get(i);
            if (key.valid) {
                key.scan();
            }
        }
    }

    void signal(PollingKey key) {
        if (!signalled.contains(key)) {
            signalled.add(key);
        }
    }

    private void checkOpen() {
        if (closed) {
            throw new TClosedWatchServiceException();
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            for (int i = 0, l = keys.size(); i < l; ++i) {
                keys.get(i).valid = false;
            }
            keys.clear();
            signalled.clear();
        }
    }

    @Override
    public TWatchKey poll() {
        checkOpen();
        scanIfNeeded();
        return signalled.poll();
    }

    @Override
    public TWatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        while (true) {
            TWatchKey key = poll();
            if (key != null) {
                return key;
            }
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return null;
            }
            Thread.sleep(Math.min(remaining, SCAN_INTERVAL_MILLIS));
        }
    }

    @Override
    public TWatchKey take() throws InterruptedException {
        while (true) {
            TWatchKey key = poll();
            if (key != null) {
                return key;
            }
            Thread.sleep(SCAN_INTERVAL_MILLIS);
        }
    }

    static final class PollingKey implements TWatchKey {
        private final TPollingWatchService service;
        final TDefaultPath dir;
        private boolean watchCreate;
        private boolean watchDelete;
        private boolean watchModify;
        private Map<String, Long> snapshot;
        private final List<TWatchEvent<?>> events = new ArrayList<>();
        private boolean ready = true;
        boolean valid = true;

        PollingKey(TPollingWatchService service, TDefaultPath dir, TWatchEvent.Kind<?>[] kinds) {
            this.service = service;
            this.dir = dir;
            setEvents(kinds);
            this.snapshot = list();
        }

        void setEvents(TWatchEvent.Kind<?>[] kinds) {
            watchCreate = false;
            watchDelete = false;
            watchModify = false;
            for (TWatchEvent.Kind<?> kind : kinds) {
                if (kind == TStandardWatchEventKinds.ENTRY_CREATE) {
                    watchCreate = true;
                } else if (kind == TStandardWatchEventKinds.ENTRY_DELETE) {
                    watchDelete = true;
                } else if (kind == TStandardWatchEventKinds.ENTRY_MODIFY) {
                    watchModify = true;
                }
            }
        }

        private Map<String, Long> list() {
            Map<String, Long> result = new HashMap<>();
            VirtualFile dirFile = dir.getFileSystem().vfs.getFile(dir.pathString);
            if (dirFile == null || !dirFile.isDirectory()) {
                return null;
            }
            String[] names = dirFile.listFiles();
            if (names != null) {
                for (String name : names) {
                    VirtualFile child = dir.getFileSystem().vfs.getFile(dir.resolve(name).toString());
                    result.put(name, child != null ? child.lastModified() : 0L);
                }
            }
            return result;
        }

        void scan() {
            Map<String, Long> current = list();
            if (current == null) {
                valid = false;
                signalEvent(TStandardWatchEventKinds.OVERFLOW, null);
                return;
            }
            Map<String, Long> previous = snapshot;
            snapshot = current;
            if (previous == null) {
                return;
            }
            for (Map.Entry<String, Long> entry : current.entrySet()) {
                Long old = previous.get(entry.getKey());
                if (old == null) {
                    if (watchCreate) {
                        signalEvent(TStandardWatchEventKinds.ENTRY_CREATE, entry.getKey());
                    }
                } else if (watchModify && !old.equals(entry.getValue())) {
                    signalEvent(TStandardWatchEventKinds.ENTRY_MODIFY, entry.getKey());
                }
            }
            if (watchDelete) {
                for (String name : previous.keySet()) {
                    if (!current.containsKey(name)) {
                        signalEvent(TStandardWatchEventKinds.ENTRY_DELETE, name);
                    }
                }
            }
        }

        private void signalEvent(TWatchEvent.Kind<?> kind, String name) {
            Object context = name != null ? new TDefaultPath(dir.getFileSystem(), name) : null;
            events.add(new PollingEvent<>(kind, context));
            if (ready) {
                ready = false;
                service.signal(this);
            }
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public List<TWatchEvent<?>> pollEvents() {
            List<TWatchEvent<?>> result = new ArrayList<>(events);
            events.clear();
            return result;
        }

        @Override
        public boolean reset() {
            if (!valid) {
                return false;
            }
            if (!ready) {
                if (events.isEmpty()) {
                    ready = true;
                } else {
                    service.signal(this);
                }
            }
            return true;
        }

        @Override
        public void cancel() {
            valid = false;
            service.keys.remove(this);
        }

        @Override
        public TWatchable watchable() {
            return dir;
        }
    }

    static final class PollingEvent<T> implements TWatchEvent<T> {
        private final Kind<T> kind;
        private final T context;

        @SuppressWarnings("unchecked")
        PollingEvent(Kind<?> kind, Object context) {
            this.kind = (Kind<T>) kind;
            this.context = (T) context;
        }

        @Override
        public Kind<T> kind() {
            return kind;
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public T context() {
            return context;
        }
    }
}
