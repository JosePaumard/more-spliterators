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
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class GatingSpliteratorTest {

    @Test
    public void should_gate_empty_streams_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Predicate<String> validator = String::isEmpty;

        // When
        GatingSpliterator<String> gatingSpliterator = GatingSpliterator.of(strings.spliterator(), validator);
        long count = StreamSupport.stream(gatingSpliterator, false).count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_gate_a_stream_correctly() {
        // Given
        Stream<String> strings = Stream.of("", "", "", "", "one", "two", "three");

        // When
        GatingSpliterator<String> gatingSpliterator = GatingSpliterator.of(strings.spliterator(), s -> !s.isEmpty());
        List<String> list = StreamSupport.stream(gatingSpliterator, false).collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).asList().containsExactly("one", "two", "three");
    }
}