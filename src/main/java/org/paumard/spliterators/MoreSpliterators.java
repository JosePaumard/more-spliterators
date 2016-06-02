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

import static java.util.function.Function.identity;

/**
 * <p>A factory class used to create streams from other streams. There are currently seven ways of rearranging streams
 * using more-spliterators.
 * </p>
 *
 * <p>Here is a first example of what can be done:</p>
 * <pre>{@code
 *     // Create an example Stream
 *     Stream<String> stream = Stream.of("a0", "a1", "a2", "a3");
 *     Stream<Stream<String>> groupingStream = MoreSpliterators.group(stream, 2);
 *     List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());
 *     // The collect list is [["a0", "a1"]["a2", "a3"]]
 * }</pre>
 *
 * <p>See the documentation of each factory method for more information. </p>
 *
 * @author José Paumard
 * @since 0.1
 */
public class MoreSpliterators {

    /**
     * <p>Generates a stream by repeating the elements of the provided stream forever. This stream is not bounded. </p>
     * <pre>{@code
     *     Stream<String> stream = Stream.of("tick", "tock");
     *     Stream<String> cyclingStream = MoreSpliterator.cyce(stream);
     *     List<String> collect = cyclingStream.limit(9).collect(Collectors.toList());
     *     // The collect list is ["tick", "tock", "tick", "tock", "tick", "tock", "tick", "tock", "tick"]
     * }</pre>
     * <p>The returned spliterator is <code>ORDERED</code>.</p>
     * @param stream The stream to cycle on. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @return
     */
    public static <E> Stream<E> cycle(Stream<E> stream) {
        CyclingSpliterator<E> spliterator = CyclingSpliterator.of(stream.spliterator());
        return StreamSupport.stream(spliterator, false).flatMap(identity());
    }

    /**
     * <p>Generates a stream by regrouping the elements of the provided stream and putting them in a substream. The number
     * of elements regrouped is the <code>groupingFactor</code>.</p>
     * <p>Example:</p>
     * <pre>{@code
     *     Stream<String> stream = Stream.of("a0", "a1", "a2", "a3");
     *     Stream<Stream<String>> groupingStream = MoreSpliterators.group(stream, 2);
     *     List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());
     *     // The collect list is [["a0", "a1"]["a2", "a3"]]
     * }</pre>
     * <p>If the provided stream is empty, then the returned stream contains an empty stream.</p>
     * <p>The <code>groupingFactor</code> should be greater of equals than 2. A grouping factor of 0 does not make
     * sense. A grouping factor of 1 is in fact a mapping with a <code>Stream::of</code>. An
     * <code>IllegalArgumentException</code> will be thrown if a non valid <code>groupingFactor</code> is provided.</p>
     * <p>An <code>IllegalArgumentException</code> will also be thrown if the provided stream is not <code>ORDERED</code></p>
     * <p>The returned stream has the same characteristics as the provided stream, and is thus <code>ORDERED</code>.</p>
     * <p>All the returned substreams are guaranteed to produce <code>groupingFactor</code> elements. So there might be
     * elements from the provided stream that will not be consumed in the grouped stream. </p>
     * @param stream The stream to be grouped. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @param groupingFactor The grouping factor, should be greater of equal than 2.
     * @return
     */
    public static <E> Stream<Stream<E>> group(Stream<E> stream, int groupingFactor) {
        GroupingSpliterator<E> spliterator = GroupingSpliterator.of(stream.spliterator(), groupingFactor);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * <p>Generates a stream by repeating the elements of the provided stream. The number of times an element is
     * repeated is given by the repeating factor.
     * <p>Example:</p>
     * <pre>{@code
     *     Stream<String> stream = Stream.of("a0", "a1", "a2", "a3");
     *     Stream<String> repeatingStream = MoreSpliterators.repeat(stream, 3);
     *     List<String> collect = repeatingStream.collect(Collectors.toList());
     *     // The collect list is ["a0", "a0", "a0", "a1", "a1", "a1", "a2", "a2", "a2", "a3", "a3", "a3"]
     * }</pre>
     * <p>If the provided stream is empty, then the returned stream is also empty.</p>
     * <p>The <code>repeatingFactor</code> should be greater of equals than 2. A repeating factor of 0 does not make
     * sense. A repeating factor of 1 is in fact the identity operation. An
     * <code>IllegalArgumentException</code> will be thrown if a non valid <code>repeatingFactor</code> is provided.</p>
     * <p>An <code>IllegalArgumentException</code> will be thrown if a non <code>SIZED</code> stream is provided.
     * Believe me, trying to repeat an infinite stream is not a good idea.</p>
     * <p>The repeating of the provided stream should no lead to the producing of more than <code>Long.MAX_VALUE</code>.
     * Weird effects will occur in that case. </p>
     * <p>The returned stream is <code>ORDERED</code>.</p>
     * @param stream The stream to be repeated. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @param repeatingFactor The repeating factor, should be greater of equal than 2.
     * @return
     */
    public static <E> Stream<E> repeat(Stream<E> stream, int repeatingFactor) {
        RepeatingSpliterator<E> spliterator = RepeatingSpliterator.of(stream.spliterator(), repeatingFactor);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * <p>Generates a stream by grouping the elements of the provided stream, and by advancing one by one the first
     * element of the next substream. The numnber of elements of the substreams is the rolling factor.
     * <p>Example:</p>
     * <pre>{@code
     *     Stream<String> stream = Stream.of("a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7");
     *     Stream<Stream<String>> rollingStream = MoreSpliterators.roll(stream, 3);
     *     List<List<String>> collect = rollingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());
     *     // The collect list is [["a0", "a1", "a2"],
     *                             ["a1", "a2", "a3"],
     *                             ["a2", "a3", "a4"],
     *                             ["a3", "a4", "a5"],
     *                             ["a4", "a5", "a6"],
     *                             ["a5", "a6", "a7"]]
     * }</pre>
     * <p>If the provided stream is empty, then the returned stream contains an empty stream.</p>
     * <p>The <code>rollingFactor</code> should be greater of equals than 2. A rolling factor of 0 does not make
     * sense. A rolling factor of 1 is in fact a mapping with a <code>Stream::of</code>. An
     * <code>IllegalArgumentException</code> will be thrown if a non valid <code>rollingFactor</code> is provided.</p>
     * <p>An <code>IllegalArgumentException</code> will also be thrown is a non <code>ORDERED</code> stream is
     * provided.</p>
     * <p>The returned stream has the same characteristics as the provided stream, and is thus <code>ORDERED</code>.</p>
     * <p>All the returned substreams are guaranteed to produce <code>rollingFactor</code> elements. So there might be
     * elements from the provided stream that will not be consumed in the grouped stream. </p>
     * @param stream The stream to be rolled. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @param rollingFactor The rolling factor, should be greater of equal than 2.
     * @return
     */
    public static <E> Stream<Stream<E>> roll(Stream<E> stream, int rollingFactor) {
        RollingSpliterator<E> spliterator = RollingSpliterator.of(stream.spliterator(), rollingFactor);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * <p>Generates a stream by taking one element of the provided streams at a time, and putting them in a substream.
     * <p>Example:</p>
     * <pre>{@code
     *     Stream<String> stream0 = Stream.of("a00", "a01", "a02", "a03");
     *     Stream<String> stream1 = Stream.of("a10", "a11", "a12", "a13");
     *     Stream<String> stream2 = Stream.of("a20", "a21", "a22", "a23");
     *     Stream<String> stream3 = Stream.of("a30", "a31", "a32", "a33");
     *     Stream<Stream<String>> traversingStream = MoreSpliterators.traverse(stream0, stream1, stream2, stream3);
     *     List<List<String>> collect = traversingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());
     *     // The collect list is [["a00", "a10", "a20", "a30"],
     *                             ["a01", "a11", "a21", "a31"],
     *                             ["a02", "a12", "a22", "a32"],
     *                             ["a03", "a13", "a23", "a33"]]
     * }</pre>
     * <p>An <code>IllegalArgumentException</code> is thrown if there is only one stream provided in the varargs. In
     * that casse, the traversing would be a mapping with <code>Stream::of</code>.</p>
     * <p>An <code>IllegalArgumentException</code> is also thrown if one of the provided streams is not <code>ORDERED</code>. </p>
     * <p>The characteristics of the returned stream is the bitwise <code>AND</code> of all the characteristics of
     * the provided streams. In most of the cases, all these streams will share the same characteristics, so in this
     * case it will be the same as well. The returned stream is thus <code>ORDERED</code>.</p>
     * @param streams The streams to be traversed. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @return
     */
    public static <E> Stream<Stream<E>> traverse(Stream<E>... streams) {
        Spliterator[] spliterators = Stream.of(streams).map(Stream::spliterator).toArray(Spliterator[]::new);
        TraversingSpliterator<E> spliterator = TraversingSpliterator.of(spliterators);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * <p>Generates a stream by taking one element of the provided streams at a time, and putting them in the
     * resulting stream.
     * <p>Example:</p>
     * <pre>{@code
     *     Stream<String> stream0 = Stream.of("a00", "a01", "a02");
     *     Stream<String> stream1 = Stream.of("a10", "a11", "a12");
     *     Stream<String> stream2 = Stream.of("a20", "a21", "a22");
     *     Stream<Stream<String>> weavingStream = MoreSpliterators.traverse(stream0, stream1, stream2);
     *     List<String> collect = weavingStream.map(st -> st.collect(Collectors.toList()).collect(Collectors.toList());
     *     // The collect list is ["a00", "a10", "a20", "a01", "a11", "a21", "a02", "a12", "a22"]
     * }</pre>
     * <p>An <code>IllegalArgumentException</code> is thrown if there is only one stream provided in the varargs. In
     * that casse, the traversing would be a mapping with <code>Stream::of</code>.</p>
     * <p>An <code>IllegalArgumentException</code> is also thrown if one of the provided streams is not <code>ORDERED</code>. </p>
     * <p>The characteristics of the returned stream is the bitwise <code>AND</code> of all the characteristics of
     * the provided streams. In most of the cases, all these streams will share the same characteristics, so in this
     * case it will be the same as well. The returned stream is thus <code>ORDERED</code>.</p>
     * <p>The returned stream will stop producing elements as soon as one of the provided stream stops to do so.
     * So some of the elements of the provided streams might not be consumed. </p>
     * @param streams The streams to be weaved. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @return
     */
    public static <E> Stream<E> weave(Stream<E>... streams) {
        Spliterator[] spliterators = Stream.of(streams).map(Stream::spliterator).toArray(Spliterator[]::new);
        WeavingSpliterator<E> spliterator = WeavingSpliterator.of(spliterators);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * <p>Generates a stream by taking one element at a time from each of the provided streams, and transforming them
     * using the provided bifunction.
     * <p>Example:</p>
     * <pre>{@code
     *     Stream<String>  stream0 = Stream.of("a", "b", "c", "d");
     *     Stream<Integer> stream1 = Stream.of(0, 1, 2, 3);
     *     Bifunction<String, Integer, String> zipper = (s, i) -> s + "-" + i;
     *     Stream<String> zippingStream = MoreSpliterators.zip(stream0, stream1, zipper);
     *     List<String> collect = zippingStream.collect(Collectors.toList());
     *     // The collect list is ["a-0", "b-1", "c-2", "d-3"]
     * }</pre>
     * <p>The characteristics of the returned spliterator is the bitwise <code>AND</code> of the characteristics of
     * the provided streams. Those streams should have the same characteristics, so there will be no change on
     * this point. </p>
     * <p>The returned stream will stop producing elements as soon as one of the provided stream stops to do so.
     * So some of the elements of the provided streams might not be consumed. </p>
     * <p>A <code>NullPointerException</code> will be thrown if the <code>zipper</code> generates a null value. So
     * the returned stream is guaranteed not to have null values.</p>
     * <p>In case you cannot be sure that your zipper returns <code>null</code>, then you can provide a
     * <code>zipper</code> than wraps its result in an <code>Optional</code> (using the
     * <code>Optional.ofNullable()</code> factory method), and flat map the returned stream. Your nulls will then
     * be silently removed from the stream.</p>
     * @param stream1 The first stream to be zipped. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @param stream2 The second stream to be zipped. Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @param zipper The bifunction used to transform the elements of the two streams.
     *               Will throw a <code>NullPointerException</code> if <code>null</code>.
     * @return
     */
    public static <E1, E2, R> Stream<R> zip(Stream<E1> stream1, Stream<E2> stream2, BiFunction<E1, E2, R> zipper) {
        ZippingSpliterator.Builder builder = new ZippingSpliterator.Builder();
        ZippingSpliterator<E1, E2, R> spliterator =
        builder.with(stream1.spliterator())
                .and(stream2.spliterator())
                .mergedBy(zipper)
                .build();
        return StreamSupport.stream(spliterator, false);
    }
}
