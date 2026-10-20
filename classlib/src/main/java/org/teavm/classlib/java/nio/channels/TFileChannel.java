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
package org.teavm.classlib.java.nio.channels;

import java.io.IOException;
import java.util.Set;
import org.teavm.classlib.java.nio.TByteBuffer;
import org.teavm.classlib.java.nio.channels.spi.TAbstractInterruptibleChannel;
import org.teavm.classlib.java.nio.file.TOpenOption;
import org.teavm.classlib.java.nio.file.TPath;
import org.teavm.classlib.java.nio.file.attribute.TFileAttribute;
import org.teavm.classlib.java.nio.file.spi.TFileSystemProvider;

public abstract class TFileChannel extends TAbstractInterruptibleChannel
        implements TSeekableByteChannel, TGatheringByteChannel, TScatteringByteChannel {
    protected TFileChannel() {
    }

    public static TFileChannel open(TPath path, Set<? extends TOpenOption> options, TFileAttribute<?>... attrs)
            throws IOException {
        TFileSystemProvider provider = path.getFileSystem().provider();
        return provider.newFileChannel(path, options, attrs);
    }

    public static TFileChannel open(TPath path, TOpenOption... options) throws IOException {
        return open(path, Set.of(options));
    }

    @Override
    public abstract int read(TByteBuffer dst) throws IOException;

    @Override
    public abstract long read(TByteBuffer[] dsts, int offset, int length) throws IOException;

    @Override
    public final long read(TByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    @Override
    public abstract int write(TByteBuffer src) throws IOException;

    @Override
    public abstract long write(TByteBuffer[] srcs, int offset, int length) throws IOException;

    @Override
    public final long write(TByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    @Override
    public abstract long position() throws IOException;

    @Override
    public abstract TFileChannel position(long newPosition) throws IOException;

    @Override
    public abstract long size() throws IOException;

    @Override
    public abstract TFileChannel truncate(long size) throws IOException;

    public abstract void force(boolean metaData) throws IOException;

    public abstract long transferTo(long position, long count, TWritableByteChannel target) throws IOException;

    public abstract long transferFrom(TReadableByteChannel src, long position, long count) throws IOException;

    public abstract TFileLock lock(long position, long size, boolean shared) throws IOException;

    public final TFileLock lock() throws IOException {
        return lock(0L, Long.MAX_VALUE, false);
    }

    public abstract TFileLock tryLock(long position, long size, boolean shared) throws IOException;

    public final TFileLock tryLock() throws IOException {
        return tryLock(0L, Long.MAX_VALUE, false);
    }

    public abstract int read(TByteBuffer dst, long position) throws IOException;

    public abstract int write(TByteBuffer src, long position) throws IOException;

    public static class MapMode {
        public static final MapMode READ_ONLY = new MapMode("READ_ONLY");
        public static final MapMode READ_WRITE = new MapMode("READ_WRITE");
        public static final MapMode PRIVATE = new MapMode("PRIVATE");

        private final String name;

        private MapMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
