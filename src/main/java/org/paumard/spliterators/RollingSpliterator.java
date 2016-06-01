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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author José
 */
public class RollingSpliterator<E> implements Spliterator<Stream<E>> {

	private final int grouping ;
	private final Spliterator<E> spliterator ;
	private Object [] buffer ; // we cant create arrays of E
	private AtomicInteger bufferWriteIndex = new AtomicInteger(0) ;
	private AtomicInteger bufferReadIndex = new AtomicInteger(0) ;

	public static <E> RollingSpliterator<E> of(Spliterator<E> spliterator, int grouping) {
		Objects.requireNonNull(spliterator);
		if (grouping < 2)
			throw new IllegalArgumentException("Grouping factor should be greater than 2");

		return new RollingSpliterator<>(spliterator, grouping);
	}

	private RollingSpliterator(Spliterator<E> spliterator, int grouping) {
		this.spliterator = spliterator ;
		this.grouping = grouping ;
		this.buffer = new Object[grouping + 1] ;
	}

	@Override
	public boolean tryAdvance(Consumer<? super Stream<E>> action) {
		boolean finished = false ;

		if (bufferWriteIndex.get() == bufferReadIndex.get()) {
			for (int i = 0 ; i < grouping ; i++) {
				if (!advanceSpliterator()) {
					finished = true ;
				}
			}
		}
		if (!advanceSpliterator()) {
			finished = true ;
		}
		
		Stream<E> subStream = buildSubstream() ;
		action.accept(subStream) ;
		return !finished ;
	}

	private boolean advanceSpliterator() {
		return spliterator.tryAdvance(
					element -> { 
						buffer[bufferWriteIndex.get() % buffer.length] = element ; 
						bufferWriteIndex.incrementAndGet() ;
				});
	}

	@Override
	public Spliterator<Stream<E>> trySplit() {
		return new RollingSpliterator<>(spliterator.trySplit(), grouping) ;
	}
	
	@SuppressWarnings("unchecked")
	private Stream<E> buildSubstream() {
		
		Stream.Builder<E> subBuilder = Stream.builder() ;
		for (int i = 0 ; i < grouping ; i++) {			
			subBuilder.add((E)buffer[(i + bufferReadIndex.get()) % buffer.length]) ;
		}
		bufferReadIndex.incrementAndGet() ;
		return subBuilder.build() ;
	}

	@Override
	public long estimateSize() {
		return spliterator.estimateSize() - grouping ;
	}

	@Override
	public int characteristics() {
		return spliterator.characteristics() ;
	}
}