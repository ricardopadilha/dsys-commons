/**
 * Copyright 2014 Ricardo Padilha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dsys.commons.impl.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.dsys.commons.api.lang.Merger;
import net.dsys.commons.impl.builder.Mandatory;
import net.dsys.commons.impl.builder.Optional;
import net.dsys.commons.impl.lang.FixedMerger;

/**
 * @author Ricardo Padilha
 */
public final class MergingFuture<V> implements Future<V> {

	private final Merger<V> merger;
	private final Collection<Future<V>> futures;

	public MergingFuture(final Merger<V> merger, final Collection<Future<V>> futures) {
		if (merger == null) {
			throw new NullPointerException("merger == null");
		}
		if (futures == null) {
			throw new NullPointerException("futures == null");
		}
		for (final Future<V> future : futures) {
			if (future == null) {
				throw new NullPointerException("future == null");
			}
		}
		this.merger = merger;
		this.futures = new ArrayList<>(futures);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		boolean cancel = true;
		for (final Future<V> future : futures) {
			cancel &= future.cancel(mayInterruptIfRunning);
		}
		return cancel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		boolean cancel = true;
		for (final Future<V> future : futures) {
			cancel &= future.isCancelled();
		}
		return cancel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		boolean done = true;
		for (final Future<V> future : futures) {
			done &= future.isDone();
		}
		return done;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException {
		final List<V> list = new ArrayList<>(futures.size());
		for (final Future<V> future : futures) {
			list.add(future.get());
		}
		return merger.merge(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
			TimeoutException {
		final List<V> list = new ArrayList<>(futures.size());
		for (final Future<V> future : futures) {
			list.add(future.get(timeout, unit));
		}
		return merger.merge(list);
	}

	/**
	 * @return a {@link Builder} for {@link MergingFuture}, which uses a
	 *         {@link Merger} created using {@link #createNullMerger()}
	 */
	public static <E> Builder<E> builder() {
		return new Builder<>();
	}

	/**
	 * If no Merger is provided, a {@link FixedMerger} returning <code>null</code> is
	 * used.
	 * 
	 * @author Ricardo Padilha
	 */
	public static final class Builder<E> {

		private final List<Future<E>> list;
		private Merger<E> merger;

		Builder() {
			this.list = new ArrayList<>();
			this.merger = new FixedMerger<>(null);
		}

		/**
		 * Use a specific Merger to aggregate results from the merged futures.
		 * The default merger always returns <code>null</code>.
		 */
		@Mandatory(restrictions = "merger != null")
		public Builder<E> mergeWith(final Merger<E> merger) {
			if (merger == null) {
				throw new NullPointerException("merger == null");
			}
			this.merger = merger;
			return this;
		}

		@Optional(defaultValue = "empty", restrictions = "future != null")
		public Builder<E> add(final Future<E> future) {
			if (future == null) {
				throw new NullPointerException("future == null");
			}
			list.add(future);
			return this;
		}

		public MergingFuture<E> build() {
			return new MergingFuture<>(merger, list);
		}

	}
}
