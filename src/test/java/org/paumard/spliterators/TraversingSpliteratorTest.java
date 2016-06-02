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

import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class TraversingSpliteratorTest {


    @Test
    public void should_a_return_stream_of_empty_stream_if_provided_streams_are_empty() {
        // Given
        Stream<String> streamA = Stream.empty();
        Stream<String> streamB = Stream.empty();

        // When
        TraversingSpliterator<String> spliterator = TraversingSpliterator.of(streamA.spliterator(), streamB.spliterator());

        // Then
        Stream<Stream<String>> stream = StreamSupport.stream(spliterator, false);
        List<List<String>> collect =
        stream.map(str -> str.collect(Collectors.toList()))
                .collect(Collectors.toList());

        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0).isEmpty()).isTrue();
    }

    @Test
    public void should_traverse_two_streams_into_a_traversed_stream() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2", "a3");
        Stream<String> streamB = Stream.of("b1", "b2", "b3");

        // When
        TraversingSpliterator<String> spliterator = TraversingSpliterator.of(streamA.spliterator(), streamB.spliterator());

        // Then
        Stream<Stream<String>> stream = StreamSupport.stream(spliterator, false);
        List<List<String>> strings =
                stream.map(str -> str.collect(Collectors.toList()))
                        .collect(Collectors.toList());

        System.out.println(strings);

        assertThat(strings.size()).isEqualTo(3);
        assertThat(strings.get(0)).asList().containsSequence("a1", "b1");
        assertThat(strings.get(1)).asList().containsSequence("a2", "b2");
        assertThat(strings.get(2)).asList().containsSequence("a3", "b3");
    }

    @Test
    public void should_traverse_two_streams_and_skip_elements_if_a_stream_is_longer_than_the_other() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2");
        Stream<String> streamB = Stream.of("b1", "b2", "b3");

        // When
        TraversingSpliterator<String> spliterator = TraversingSpliterator.of(streamA.spliterator(), streamB.spliterator());

        // Then
        Stream<Stream<String>> stream = StreamSupport.stream(spliterator, false);
        List<List<String>> strings =
                stream.map(str -> str.collect(Collectors.toList()))
                        .collect(Collectors.toList());

        System.out.println(strings);

        assertThat(strings.size()).isEqualTo(2);
        assertThat(strings.get(0)).asList().containsExactly("a1", "b1");
        assertThat(strings.get(1)).asList().containsExactly("a2", "b2");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_transversal_spliterator_on_a_null_spliterator() {

        TraversingSpliterator<String> spliterator = TraversingSpliterator.of(null);
    }

    @Test(expectedExceptions = WhyWouldYouDoThatException.class)
    public void should_not_build_a_transversal_spliterator_on_only_one_spliterator() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2");

        // When
        TraversingSpliterator<String> spliterator = TraversingSpliterator.of(streamA.spliterator());
    }
}