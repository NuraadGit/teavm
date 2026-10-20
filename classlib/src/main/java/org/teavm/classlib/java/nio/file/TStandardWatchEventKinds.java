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
package org.teavm.classlib.java.nio.file;

public final class TStandardWatchEventKinds {
    public static final TWatchEvent.Kind<Object> OVERFLOW = new StdKind<>("OVERFLOW", Object.class);
    public static final TWatchEvent.Kind<TPath> ENTRY_CREATE = new StdKind<>("ENTRY_CREATE", TPath.class);
    public static final TWatchEvent.Kind<TPath> ENTRY_DELETE = new StdKind<>("ENTRY_DELETE", TPath.class);
    public static final TWatchEvent.Kind<TPath> ENTRY_MODIFY = new StdKind<>("ENTRY_MODIFY", TPath.class);

    private TStandardWatchEventKinds() {
    }

    private static class StdKind<T> implements TWatchEvent.Kind<T> {
        private final String name;
        private final Class<T> type;

        StdKind(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
