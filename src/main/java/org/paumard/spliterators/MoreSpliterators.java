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

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by José
 */
public class MoreSpliterators {

    public static <E> Stream<Stream<E>> group(Stream<E> stream, int groupingFactor) {
        GroupingSpliterator<E> spliterator = GroupingSpliterator.of(stream.spliterator(), groupingFactor);
        return StreamSupport.stream(spliterator, false);
    }

    public static <E> Stream<E> repeat(Stream<E> stream, int repeatingFactor) {
        RepeatingSpliterator<E> spliterator = RepeatingSpliterator.of(stream.spliterator(), repeatingFactor);
        return StreamSupport.stream(spliterator, false);
    }

    public static <E> Stream<Stream<E>> roll(Stream<E> stream, int rollingFactor) {
        RollingSpliterator<E> spliterator = RollingSpliterator.of(stream.spliterator(), rollingFactor);
        return StreamSupport.stream(spliterator, false);
    }

    public static <E> Stream<Stream<E>> traverse(Stream<E>... streams) {
        Spliterator[] spliterators = Stream.of(streams).map(Stream::spliterator).toArray(Spliterator[]::new);
        TraversingSpliterator<E> spliterator = TraversingSpliterator.of(spliterators);
        return StreamSupport.stream(spliterator, false);
    }

    public static <E> Stream<E> weave(Stream<E>... streams) {
        Spliterator[] spliterators = Stream.of(streams).map(Stream::spliterator).toArray(Spliterator[]::new);
        WeavingSpliterator<E> spliterator = WeavingSpliterator.of(spliterators);
        return StreamSupport.stream(spliterator, false);
    }

    public static <E1, E2, R> Stream<R> zip(Stream<E1> stream1, Stream<E2> stream2, BiFunction<E1, E2, R> function) {
        ZippingSpliterator.Builder builder = new ZippingSpliterator.Builder();
        ZippingSpliterator<E1, E2, R> spliterator =
        builder.with(stream1.spliterator())
                .and(stream2.spliterator())
                .mergedBy(function)
                .build();
        return StreamSupport.stream(spliterator, false);
    }
}
