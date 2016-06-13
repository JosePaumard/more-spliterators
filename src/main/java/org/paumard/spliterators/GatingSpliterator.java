/*
 * Copyright (C) 2015 José Paumard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.paumard.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * See the documentation and patterns to be used in this class in the {@link MoreSpliterators} factory class.
 *
 * @author José
 */
public class GatingSpliterator<E> implements Spliterator<E> {

    private final Spliterator<E> spliterator;
    private final Predicate<E> gate;
    private final AtomicBoolean hasMore = new AtomicBoolean(false);

    public static <E> GatingSpliterator<E> of(Spliterator<E> spliterator, Predicate<E> gate) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(gate);

        return new GatingSpliterator<>(spliterator, gate);
    }

    private GatingSpliterator(Spliterator<E> spliterator, Predicate<E> gate) {
        this.spliterator = spliterator;
        this.gate = gate;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {

        return spliterator.tryAdvance(e -> {
            if (!hasMore.get() && gate.test(e)) {
                hasMore.set(true);
            }
            if (hasMore.get()) {
                action.accept(e);
            }
        });
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> split = this.spliterator.trySplit();
        return split == null ? null : new GatingSpliterator<>(split, gate);
    }

    @Override
    public long estimateSize() {
        if (hasMore.get()) {
            return this.spliterator.estimateSize();
        } else {
            return 0;
        }
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics() & ~Spliterator.SIZED & ~Spliterator.SUBSIZED;
    }
}