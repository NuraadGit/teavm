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
import java.util.ArrayList;
import java.util.List;
import org.teavm.classlib.java.nio.TByteBuffer;
import org.teavm.classlib.java.nio.channels.TClosedChannelException;
import org.teavm.classlib.java.nio.channels.TFileChannel;
import org.teavm.classlib.java.nio.channels.TFileLock;
import org.teavm.classlib.java.nio.channels.TNonReadableChannelException;
import org.teavm.classlib.java.nio.channels.TNonWritableChannelException;
import org.teavm.classlib.java.nio.channels.TOverlappingFileLockException;
import org.teavm.classlib.java.nio.channels.TReadableByteChannel;
import org.teavm.classlib.java.nio.channels.TWritableByteChannel;
import org.teavm.runtime.fs.VirtualFileAccessor;

class TVirtualFileChannel extends TFileChannel {
    private final VirtualFileAccessor accessor;
    private final boolean readable;
    private final boolean writable;
    private final boolean append;
    private byte[] buffer = new byte[256];
    private final List<VirtualFileLock> locks = new ArrayList<>();

    TVirtualFileChannel(VirtualFileAccessor accessor, boolean readable, boolean writable, boolean append) {
        this.accessor = accessor;
        this.readable = readable;
        this.writable = writable;
        this.append = append;
    }

    private void ensureOpen() throws IOException {
        if (!isOpen()) {
            throw new TClosedChannelException();
        }
    }

    private void ensureReadable() throws IOException {
        ensureOpen();
        if (!readable) {
            throw new TNonReadableChannelException();
        }
    }

    private void ensureWritable() throws IOException {
        ensureOpen();
        if (!writable) {
            throw new TNonWritableChannelException();
        }
    }

    private byte[] buffer(int size) {
        if (buffer.length < size) {
            buffer = new byte[Math.max(size, buffer.length * 2)];
        }
        return buffer;
    }

    private int readAt(TByteBuffer dst) throws IOException {
        int len = dst.remaining();
        if (len == 0) {
            return 0;
        }
        int remainingInFile = accessor.size() - accessor.tell();
        if (remainingInFile <= 0) {
            return -1;
        }
        len = Math.min(len, remainingInFile);
        if (dst.hasArray()) {
            int n = accessor.read(dst.array(), dst.arrayOffset() + dst.position(), len);
            if (n > 0) {
                dst.position(dst.position() + n);
            }
            return n;
        }
        byte[] tmp = buffer(len);
        int n = accessor.read(tmp, 0, len);
        if (n > 0) {
            dst.put(tmp, 0, n);
        }
        return n;
    }

    private int writeAt(TByteBuffer src) throws IOException {
        int len = src.remaining();
        if (len == 0) {
            return 0;
        }
        if (src.hasArray()) {
            accessor.write(src.array(), src.arrayOffset() + src.position(), len);
            src.position(src.position() + len);
        } else {
            byte[] tmp = buffer(len);
            src.get(tmp, 0, len);
            accessor.write(tmp, 0, len);
        }
        return len;
    }

    @Override
    public int read(TByteBuffer dst) throws IOException {
        ensureReadable();
        return readAt(dst);
    }

    @Override
    public long read(TByteBuffer[] dsts, int offset, int length) throws IOException {
        ensureReadable();
        long total = 0;
        for (int i = offset; i < offset + length; ++i) {
            int n = readAt(dsts[i]);
            if (n < 0) {
                return total == 0 ? -1 : total;
            }
            total += n;
            if (dsts[i].hasRemaining()) {
                break;
            }
        }
        return total;
    }

    @Override
    public int write(TByteBuffer src) throws IOException {
        ensureWritable();
        if (append) {
            accessor.seek(accessor.size());
        }
        return writeAt(src);
    }

    @Override
    public long write(TByteBuffer[] srcs, int offset, int length) throws IOException {
        ensureWritable();
        if (append) {
            accessor.seek(accessor.size());
        }
        long total = 0;
        for (int i = offset; i < offset + length; ++i) {
            total += writeAt(srcs[i]);
        }
        return total;
    }

    @Override
    public long position() throws IOException {
        ensureOpen();
        return accessor.tell();
    }

    @Override
    public TFileChannel position(long newPosition) throws IOException {
        ensureOpen();
        if (newPosition < 0) {
            throw new IllegalArgumentException();
        }
        accessor.seek((int) newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        ensureOpen();
        return accessor.size();
    }

    @Override
    public TFileChannel truncate(long size) throws IOException {
        ensureWritable();
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        int pos = accessor.tell();
        if (size < accessor.size()) {
            accessor.resize((int) size);
        }
        if (pos > size) {
            accessor.seek((int) size);
        }
        return this;
    }

    @Override
    public void force(boolean metaData) throws IOException {
        ensureOpen();
        accessor.flush();
    }

    @Override
    public long transferTo(long position, long count, TWritableByteChannel target) throws IOException {
        ensureReadable();
        int pos = accessor.tell();
        try {
            accessor.seek((int) position);
            long total = 0;
            TByteBuffer tmp = TByteBuffer.allocate((int) Math.min(count, 8192));
            while (total < count) {
                tmp.clear();
                tmp.limit((int) Math.min(tmp.capacity(), count - total));
                int n = readAt(tmp);
                if (n <= 0) {
                    break;
                }
                tmp.flip();
                while (tmp.hasRemaining()) {
                    target.write(tmp);
                }
                total += n;
            }
            return total;
        } finally {
            accessor.seek(pos);
        }
    }

    @Override
    public long transferFrom(TReadableByteChannel src, long position, long count) throws IOException {
        ensureWritable();
        int pos = accessor.tell();
        try {
            accessor.seek((int) position);
            long total = 0;
            TByteBuffer tmp = TByteBuffer.allocate((int) Math.min(count, 8192));
            while (total < count) {
                tmp.clear();
                tmp.limit((int) Math.min(tmp.capacity(), count - total));
                int n = src.read(tmp);
                if (n <= 0) {
                    break;
                }
                tmp.flip();
                writeAt(tmp);
                total += n;
            }
            return total;
        } finally {
            accessor.seek(pos);
        }
    }

    @Override
    public int read(TByteBuffer dst, long position) throws IOException {
        ensureReadable();
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        int pos = accessor.tell();
        try {
            accessor.seek((int) position);
            return readAt(dst);
        } finally {
            accessor.seek(pos);
        }
    }

    @Override
    public int write(TByteBuffer src, long position) throws IOException {
        ensureWritable();
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        int pos = accessor.tell();
        try {
            if (position > accessor.size()) {
                accessor.resize((int) position);
            }
            accessor.seek((int) position);
            return writeAt(src);
        } finally {
            accessor.seek(pos);
        }
    }

    @Override
    public TFileLock lock(long position, long size, boolean shared) throws IOException {
        TFileLock result = tryLock(position, size, shared);
        if (result == null) {
            throw new TOverlappingFileLockException();
        }
        return result;
    }

    @Override
    public TFileLock tryLock(long position, long size, boolean shared) throws IOException {
        ensureOpen();
        if (shared && !readable) {
            throw new TNonReadableChannelException();
        }
        if (!shared && !writable) {
            throw new TNonWritableChannelException();
        }
        for (int i = 0, l = locks.size(); i < l; ++i) {
            if (locks.get(i).overlaps(position, size)) {
                throw new TOverlappingFileLockException();
            }
        }
        VirtualFileLock lock = new VirtualFileLock(this, position, size, shared);
        locks.add(lock);
        return lock;
    }

    @Override
    protected void implCloseChannel() throws IOException {
        for (int i = 0, l = locks.size(); i < l; ++i) {
            locks.get(i).valid = false;
        }
        locks.clear();
        accessor.close();
    }

    private static final class VirtualFileLock extends TFileLock {
        boolean valid = true;

        VirtualFileLock(TVirtualFileChannel channel, long position, long size, boolean shared) {
            super(channel, position, size, shared);
        }

        @Override
        public boolean isValid() {
            return valid && channel().isOpen();
        }

        @Override
        public void release() throws IOException {
            if (valid) {
                valid = false;
                ((TVirtualFileChannel) channel()).locks.remove(this);
            }
        }
    }
}
