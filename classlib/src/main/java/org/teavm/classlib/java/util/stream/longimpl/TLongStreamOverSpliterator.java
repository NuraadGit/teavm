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
package org.teavm.classlib.java.util.stream.longimpl;

import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import org.teavm.classlib.java.util.TSpliterator;

public class TLongStreamOverSpliterator extends TSimpleLongStreamImpl {
    private TSpliterator.OfLong spliterator;

    public TLongStreamOverSpliterator(TSpliterator.OfLong spliterator) {
        this.spliterator = spliterator;
    }

    @Override
    public boolean next(LongPredicate consumer) {
        AdapterAction action = new AdapterAction(consumer);
        while (spliterator.tryAdvance(action)) {
            if (!action.wantsMore) {
                return true;
            }
        }
        return false;
    }

    static class AdapterAction implements LongConsumer {
        private LongPredicate consumer;
        boolean wantsMore;

        AdapterAction(LongPredicate consumer) {
            this.consumer = consumer;
        }

        @Override
        public void accept(long t) {
            wantsMore = consumer.test(t);
        }
    }

    @Override
    protected int estimateSize() {
        return (int) spliterator.estimateSize();
    }

    @Override
    public long count() {
        return spliterator.hasCharacteristics(Spliterator.SIZED) ? spliterator.estimateSize() : super.count();
    }
}
