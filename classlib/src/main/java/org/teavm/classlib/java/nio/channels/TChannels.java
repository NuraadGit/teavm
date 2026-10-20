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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;
import org.teavm.classlib.java.nio.TByteBuffer;

public final class TChannels {
    private TChannels() {
    }

    public static InputStream newInputStream(TReadableByteChannel ch) {
        Objects.requireNonNull(ch, "ch");
        return new InputStream() {
            private final byte[] single = new byte[1];

            @Override
            public int read() throws IOException {
                int n = read(single, 0, 1);
                return n <= 0 ? -1 : single[0] & 0xFF;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                Objects.checkFromIndexSize(off, len, b.length);
                if (len == 0) {
                    return 0;
                }
                int n;
                do {
                    n = ch.read(TByteBuffer.wrap(b, off, len));
                } while (n == 0);
                return n;
            }

            @Override
            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static OutputStream newOutputStream(TWritableByteChannel ch) {
        Objects.requireNonNull(ch, "ch");
        return new OutputStream() {
            private final byte[] single = new byte[1];

            @Override
            public void write(int b) throws IOException {
                single[0] = (byte) b;
                write(single, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                Objects.checkFromIndexSize(off, len, b.length);
                TByteBuffer buffer = TByteBuffer.wrap(b, off, len);
                while (buffer.hasRemaining()) {
                    ch.write(buffer);
                }
            }

            @Override
            public void close() throws IOException {
                ch.close();
            }
        };
    }

    public static TReadableByteChannel newChannel(InputStream in) {
        Objects.requireNonNull(in, "in");
        return new TReadableByteChannel() {
            private boolean open = true;

            @Override
            public int read(TByteBuffer dst) throws IOException {
                int len = dst.remaining();
                if (len == 0) {
                    return 0;
                }
                byte[] tmp = new byte[Math.min(len, 8192)];
                int n = in.read(tmp, 0, tmp.length);
                if (n > 0) {
                    dst.put(tmp, 0, n);
                }
                return n;
            }

            @Override
            public boolean isOpen() {
                return open;
            }

            @Override
            public void close() throws IOException {
                open = false;
                in.close();
            }
        };
    }

    public static TWritableByteChannel newChannel(OutputStream out) {
        Objects.requireNonNull(out, "out");
        return new TWritableByteChannel() {
            private boolean open = true;

            @Override
            public int write(TByteBuffer src) throws IOException {
                int len = src.remaining();
                byte[] tmp = new byte[len];
                src.get(tmp);
                out.write(tmp);
                return len;
            }

            @Override
            public boolean isOpen() {
                return open;
            }

            @Override
            public void close() throws IOException {
                open = false;
                out.close();
            }
        };
    }

    public static Reader newReader(TReadableByteChannel ch, Charset charset) {
        return new InputStreamReader(newInputStream(ch), charset);
    }

    public static Reader newReader(TReadableByteChannel ch, String csName) {
        return newReader(ch, Charset.forName(csName));
    }

    public static Writer newWriter(TWritableByteChannel ch, Charset charset) {
        return new OutputStreamWriter(newOutputStream(ch), charset);
    }

    public static Writer newWriter(TWritableByteChannel ch, String csName) {
        return newWriter(ch, Charset.forName(csName));
    }
}
