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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class WeavingSpliteratorTest {

    @Test
    public void should_weave_empty_streams_into_a_stream_of_an_empty_stream() {
        // Given
        Stream<String> strings1 = Stream.empty();
        Stream<String> strings2 = Stream.empty();

        // When
        WeavingSpliterator<String> weavingSpliterator = WeavingSpliterator.of(strings1.spliterator(), strings2.spliterator());
        Stream<String> weavedSteam = StreamSupport.stream(weavingSpliterator, false);

        // Then
        assertThat(weavedSteam.count()).isEqualTo(0);
    }

    @Test
    public void should_weave_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings1 = Stream.of( "1",  "2",  "3",  "4");
        Stream<String> strings2 = Stream.of("11", "12", "13", "14");

        // When
        WeavingSpliterator<String> weavingSpliterator = WeavingSpliterator.of(strings1.spliterator(), strings2.spliterator());
        Stream<String> stream = StreamSupport.stream(weavingSpliterator, false);
        List<String> collect = stream.collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(8);
        assertThat(collect).isEqualTo(Arrays.asList("1", "11", "2", "12", "3", "13", "4", "14"));
    }

    @Test
    public void should_weave_a_non_empty_stream_with_correct_substreams_content_of_different_sizes() {
        // Given
        Stream<String> strings1 = Stream.of( "1",  "2",  "3",  "4");
        Stream<String> strings2 = Stream.of("11", "12", "13", "14", "15");

        // When
        WeavingSpliterator<String> weavingSpliterator = WeavingSpliterator.of(strings1.spliterator(), strings2.spliterator());
        Stream<String> stream = StreamSupport.stream(weavingSpliterator, false);
        List<String> collect = stream.collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(8);
        assertThat(collect).isEqualTo(Arrays.asList("1", "11", "2", "12", "3", "13", "4", "14"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_weaving_spliterator_on_null() {

        WeavingSpliterator<String> groupingSpliterator = WeavingSpliterator.of(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_not_build_a_weaving_spliterator_on_less_than_two_spliterators() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        WeavingSpliterator<String> weavingSpliterator = WeavingSpliterator.of(strings.spliterator());
    }
}