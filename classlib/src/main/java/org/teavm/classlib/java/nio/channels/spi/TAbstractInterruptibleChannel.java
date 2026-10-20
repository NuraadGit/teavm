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
package org.teavm.classlib.java.nio.channels.spi;

import java.io.IOException;
import org.teavm.classlib.java.nio.channels.TInterruptibleChannel;

public abstract class TAbstractInterruptibleChannel implements TInterruptibleChannel {
    private boolean closed;

    protected TAbstractInterruptibleChannel() {
    }

    @Override
    public final void close() throws IOException {
        if (!closed) {
            closed = true;
            implCloseChannel();
        }
    }

    protected abstract void implCloseChannel() throws IOException;

    @Override
    public final boolean isOpen() {
        return !closed;
    }

    protected final void begin() {
    }

    protected final void end(boolean completed) {
    }
}
