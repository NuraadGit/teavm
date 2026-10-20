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

public abstract class TFileLock implements AutoCloseable {
    private final TChannel channel;
    private final long position;
    private final long size;
    private final boolean shared;

    protected TFileLock(TFileChannel channel, long position, long size, boolean shared) {
        if (position < 0 || size < 0 || size + position < 0) {
            throw new IllegalArgumentException();
        }
        this.channel = channel;
        this.position = position;
        this.size = size;
        this.shared = shared;
    }

    public final TFileChannel channel() {
        return (TFileChannel) channel;
    }

    public TChannel acquiredBy() {
        return channel;
    }

    public final long position() {
        return position;
    }

    public final long size() {
        return size;
    }

    public final boolean isShared() {
        return shared;
    }

    public final boolean overlaps(long position, long size) {
        if (position + size <= this.position) {
            return false;
        }
        return this.position + this.size > position;
    }

    public abstract boolean isValid();

    public abstract void release() throws IOException;

    @Override
    public final void close() throws IOException {
        release();
    }

    @Override
    public final String toString() {
        return getClass().getName() + "[" + position + ":" + size + " " + (shared ? "shared" : "exclusive") + " "
                + (isValid() ? "valid" : "invalid") + "]";
    }
}
