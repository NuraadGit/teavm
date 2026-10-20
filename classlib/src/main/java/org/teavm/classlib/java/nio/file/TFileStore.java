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

import java.io.IOException;
import org.teavm.classlib.java.nio.file.attribute.TFileAttributeView;
import org.teavm.classlib.java.nio.file.attribute.TFileStoreAttributeView;

public abstract class TFileStore {
    protected TFileStore() {
    }

    public abstract String name();

    public abstract String type();

    public abstract boolean isReadOnly();

    public abstract long getTotalSpace() throws IOException;

    public abstract long getUsableSpace() throws IOException;

    public abstract long getUnallocatedSpace() throws IOException;

    public long getBlockSize() throws IOException {
        throw new UnsupportedOperationException();
    }

    public abstract boolean supportsFileAttributeView(Class<? extends TFileAttributeView> type);

    public abstract boolean supportsFileAttributeView(String name);

    public abstract <V extends TFileStoreAttributeView> V getFileStoreAttributeView(Class<V> type);

    public abstract Object getAttribute(String attribute) throws IOException;
}
