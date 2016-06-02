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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class RepeatingSpliteratorTest {

    @Test
    public void should_return_an_empty_stream_when_called_with_an_empty_stream() {
        // Given
        Stream<String> stream = Stream.empty();
        int repeating = 2;

        // When
        RepeatingSpliterator<String> spliterator = RepeatingSpliterator.of(stream.spliterator(), repeating);

        // Then
        Stream<String> resultStream = StreamSupport.stream(spliterator, false);
        assertThat(resultStream.count()).isEqualTo(0L);
    }

    @Test
    public void should_return_a_repeating_stream_when_called_on_a_non_empty_stream() {
        // Given
        Stream<String> stream = Stream.of("a", "b", "c");
        int repeating = 2;

        // When
        RepeatingSpliterator<String> spliterator = RepeatingSpliterator.of(stream.spliterator(), repeating);

        // Then
        List<String> result =
        StreamSupport.stream(spliterator, false)
                .collect(Collectors.toList());

        assertThat(result).asList().containsExactly("a", "a", "b", "b", "c", "c");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_repeating_spliterator_on_a_null_spliterator() {

        RepeatingSpliterator<String> spliterator = RepeatingSpliterator.of(null, 3);
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_repeating_spliterator_with_a_repeating_factor_of_1() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2");

        // When
        RepeatingSpliterator<String> spliterator = RepeatingSpliterator.of(streamA.spliterator(), 1);
    }
}