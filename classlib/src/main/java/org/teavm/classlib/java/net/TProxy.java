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
package org.teavm.classlib.java.net;

public class TProxy {
    public enum Type {
        DIRECT,
        HTTP,
        SOCKS
    }

    public static final TProxy NO_PROXY = new TProxy();

    private final Type type;
    private final TSocketAddress sa;

    private TProxy() {
        type = Type.DIRECT;
        sa = null;
    }

    public TProxy(Type type, TSocketAddress sa) {
        if (type == Type.DIRECT || sa == null) {
            throw new IllegalArgumentException("type " + type + " is not compatible with address " + sa);
        }
        this.type = type;
        this.sa = sa;
    }

    public Type type() {
        return type;
    }

    public TSocketAddress address() {
        return sa;
    }

    @Override
    public String toString() {
        return type == Type.DIRECT ? "DIRECT" : type + " @ " + sa;
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof TProxy)) {
            return false;
        }
        TProxy p = (TProxy) obj;
        return p.type == type && (sa == null ? p.sa == null : sa.equals(p.sa));
    }

    @Override
    public final int hashCode() {
        return sa == null ? type.hashCode() : type.hashCode() + sa.hashCode();
    }
}
