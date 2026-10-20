/*
 *  Copyright 2023 ihromant.
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
package org.teavm.classlib.java.util;

import org.teavm.classlib.impl.Base64Impl;

public final class TBase64 {
    private TBase64() {
    }

    public static Encoder getEncoder() {
        return new Encoder(Base64Impl.alphabet, true);
    }

    public static Encoder getUrlEncoder() {
        return new Encoder(Base64Impl.urlAlphabet, false);
    }

    public static Encoder getMimeEncoder() {
        return new Encoder(Base64Impl.alphabet, true, 76, new byte[] { '\r', '\n' });
    }

    public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
        for (byte b : lineSeparator) {
            if (Base64Impl.reverse[b & 0xFF] != -1 || b == '=') {
                throw new IllegalArgumentException("Illegal base64 line separator character 0x"
                        + Integer.toString(b, 16));
            }
        }
        lineLength &= ~3;
        if (lineLength <= 0) {
            return new Encoder(Base64Impl.alphabet, true);
        }
        return new Encoder(Base64Impl.alphabet, true, lineLength, lineSeparator.clone());
    }

    public static class Encoder {
        private final byte[] mapping;
        private final boolean padding;
        private final int lineLength;
        private final byte[] lineSeparator;

        private Encoder(byte[] mapping, boolean padding) {
            this(mapping, padding, 0, null);
        }

        private Encoder(byte[] mapping, boolean padding, int lineLength, byte[] lineSeparator) {
            this.mapping = mapping;
            this.padding = padding;
            this.lineLength = lineLength;
            this.lineSeparator = lineSeparator;
        }

        public byte[] encode(byte[] src) {
            byte[] encoded = Base64Impl.encode(src, mapping, padding);
            if (lineSeparator == null || encoded.length <= lineLength) {
                return encoded;
            }
            int lines = (encoded.length - 1) / lineLength;
            byte[] result = new byte[encoded.length + lines * lineSeparator.length];
            int j = 0;
            for (int i = 0; i < encoded.length; i += lineLength) {
                if (i > 0) {
                    System.arraycopy(lineSeparator, 0, result, j, lineSeparator.length);
                    j += lineSeparator.length;
                }
                int len = Math.min(lineLength, encoded.length - i);
                System.arraycopy(encoded, i, result, j, len);
                j += len;
            }
            return result;
        }

        public String encodeToString(byte[] src) {
            var bytes = encode(src);
            var chars = new char[bytes.length];
            for (var i = 0; i < bytes.length; ++i) {
                chars[i] = (char) (bytes[i] & 0xFF);
            }
            return new String(chars);
        }

        public Encoder withoutPadding() {
            return new Encoder(mapping, false, lineLength, lineSeparator);
        }
    }

    public static Decoder getDecoder() {
        return new Decoder(Base64Impl.reverse);
    }

    public static Decoder getUrlDecoder() {
        return new Decoder(Base64Impl.urlReverse);
    }

    public static Decoder getMimeDecoder() {
        return new Decoder(Base64Impl.reverse, true);
    }

    public static class Decoder {
        private final int[] mapping;
        private final boolean mime;

        private Decoder(int[] mapping) {
            this(mapping, false);
        }

        private Decoder(int[] mapping, boolean mime) {
            this.mapping = mapping;
            this.mime = mime;
        }

        public byte[] decode(byte[] src) {
            if (mime) {
                int count = 0;
                for (int i = 0; i < src.length; ++i) {
                    int b = src[i] & 0xFF;
                    if (mapping[b] != -1 || b == '=') {
                        ++count;
                    }
                }
                if (count != src.length) {
                    byte[] filtered = new byte[count];
                    int j = 0;
                    for (int i = 0; i < src.length; ++i) {
                        int b = src[i] & 0xFF;
                        if (mapping[b] != -1 || b == '=') {
                            filtered[j++] = src[i];
                        }
                    }
                    src = filtered;
                }
            }
            return Base64Impl.decode(src, mapping);
        }

        public byte[] decode(String src) {
            var bytes = new byte[src.length()];
            for (var i = 0; i < bytes.length; ++i) {
                bytes[i] = (byte) src.charAt(i);
            }
            return decode(bytes);
        }
    }
}
