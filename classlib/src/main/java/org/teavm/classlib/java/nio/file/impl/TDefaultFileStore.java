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
import org.teavm.classlib.java.nio.file.TFileStore;
import org.teavm.classlib.java.nio.file.attribute.TBasicFileAttributeView;
import org.teavm.classlib.java.nio.file.attribute.TFileAttributeView;
import org.teavm.classlib.java.nio.file.attribute.TFileStoreAttributeView;

final class TDefaultFileStore extends TFileStore {
    static final TDefaultFileStore INSTANCE = new TDefaultFileStore();

    private TDefaultFileStore() {
    }

    @Override
    public String name() {
        return "vfs";
    }

    @Override
    public String type() {
        return "teavm";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends TFileAttributeView> type) {
        return type == TBasicFileAttributeView.class;
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return name.equals("basic");
    }

    @Override
    public <V extends TFileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null;
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        throw new UnsupportedOperationException("'" + attribute + "' not recognized");
    }

    @Override
    public String toString() {
        return "/ (vfs)";
    }
}
