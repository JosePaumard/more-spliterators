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

import org.paumard.spliterators.exception.WhyWouldYouDoThatException;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by José
 */
public class TraversingSpliterator<E> implements Spliterator<Stream<E>> {

    private Spliterator<E>[] spliterators;
    private AtomicBoolean firstGroup = new AtomicBoolean(true);

    public static <E> TraversingSpliterator<E> of(Spliterator<E>... spliterators) {
        Objects.requireNonNull(spliterators);
        if (spliterators.length < 2)
            throw new WhyWouldYouDoThatException("Why would you want to traverse less than two streams?");

        return new TraversingSpliterator<>(spliterators);
    }

    @SafeVarargs
    private TraversingSpliterator(Spliterator<E>... spliterators) {
        this.spliterators = spliterators;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Stream<E>> action) {
        Stream.Builder<E> builder = Stream.builder();
        boolean hasMore = true;
        for (Spliterator<E> spliterator : spliterators) {
            hasMore &= spliterator.tryAdvance(builder::add);
        }
        if (hasMore) {
            action.accept(builder.build());
            firstGroup.getAndSet(false);
        }
        if (!hasMore && firstGroup.getAndSet(false))
            action.accept(Stream.<E>empty());
        return hasMore;
    }


    @Override
    public Spliterator<Stream<E>> trySplit() {
        TraversingSpliterator<E>[] spliterators =
                Stream.of(this.spliterators).map(Spliterator::trySplit).toArray(TraversingSpliterator[]::new);
        return TraversingSpliterator.of((Spliterator<E>[]) spliterators);
    }

    @Override
    public long estimateSize() {
        return spliterators[0].estimateSize();
    }

    @Override
    public int characteristics() {
        return spliterators[0].characteristics();
    }

}