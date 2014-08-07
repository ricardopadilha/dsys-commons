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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.dsys.commons.api.exception.Bug;
import net.dsys.commons.api.future.CallbackFuture;
import net.dsys.commons.api.lang.Merger;
import net.dsys.commons.impl.builder.Mandatory;
import net.dsys.commons.impl.builder.Optional;
import net.dsys.commons.impl.lang.FixedMerger;

/**
 * @author Ricardo Padilha
 */
public final class MergingCallbackFuture<V> implements CallbackFuture<V> {

	private final Object sync;
	private final Merger<V> merger;
	private final List<CallbackFuture<V>> futures;
	private final int size;
	private final BitSet bitset;
	private final List<V> values;

	private boolean done;
	private boolean cancelled;
	private V value;
	private Throwable throwable;

	private Queue<Task> tasks;
	private boolean notified;

	public MergingCallbackFuture(@Nonnull final Merger<V> merger,
			@Nonnull final Collection<CallbackFuture<V>> futures) {
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

		this.sync = new Object();
		this.merger = merger;
		this.futures = new ArrayList<>(futures);
		this.size = futures.size();
		this.bitset = new BitSet(size);
		this.values = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			final int index = i;
			final CallbackFuture<V> future = this.futures.get(i);
			future.onCompletion(new Runnable() {
				@Override
				public void run() {
					done(index, future);
				}
			});
		}
	}

	void done(@Nonnegative final int index, @Nonnull final Future<V> future) {
		if (!future.isDone()) {
			throw new Bug("!future.isDone()");
		}
		if (future.isCancelled()) {
			return;
		}
		synchronized (sync) {
			if (done) {
				return;
			}
			try {
				values.add(future.get());
				bitset.set(index);
				if (bitset.cardinality() == size) {
					this.value = merger.merge(values);
					this.done = true;
					notifyCompletion();
				}
			} catch (final Throwable throwable) {
				if (throwable instanceof ExecutionException) {
					this.throwable = throwable.getCause();
				} else {
					this.throwable = throwable;
				}
				this.done = true;
				notifyCompletion();
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		synchronized (sync) {
			if (done) {
				return false;
			}
			this.cancelled = true;
			this.done = true;
			boolean cancel = true;
			for (final Future<V> future : futures) {
				cancel &= future.cancel(mayInterruptIfRunning);
			}
			notifyCompletion();
			return cancel;
		}
	}

	/**
	 * Only call from synchronized block
	 */
	private void notifyCompletion() {
		if (notified) {
			return;
		}
		if (tasks != null) {
			while (!tasks.isEmpty()) {
				tasks.poll().execute();
			}
		}
		notified = true;
		sync.notifyAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		synchronized (sync) {
			return cancelled;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		synchronized (sync) {
			return done;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get() throws ExecutionException, InterruptedException {
		synchronized (sync) {
			if (done) {
				if (cancelled) {
					throw new CancellationException();
				}
				if (throwable != null) {
					throw new ExecutionException(throwable);
				}
				return value;
			}
			while (!done) {
				sync.wait();
			}
			if (cancelled) {
				throw new CancellationException();
			}
			if (throwable != null) {
				throw new ExecutionException(throwable);
			}
			return value;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(final long timeout, final TimeUnit unit)
			throws ExecutionException, TimeoutException, InterruptedException {
		synchronized (sync) {
			if (done) {
				if (cancelled) {
					throw new CancellationException();
				}
				if (throwable != null) {
					throw new ExecutionException(throwable);
				}
				return value;
			}
			LockSupport.parkNanos(sync, unit.toNanos(timeout));
			if (!done) {
				throw new TimeoutException();
			}
			if (cancelled) {
				throw new CancellationException();
			}
			return value;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCompletion(final Runnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable == null");
		}

		if (notified) {
			runnable.run();
			return;
		}

		synchronized (sync) {
			// re-check for safety
			if (notified) {
				runnable.run();
				return;
			}
			if (tasks == null) {
				tasks = new ArrayDeque<>();
			}
			tasks.add(new Task(runnable));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCompletion(final Runnable runnable, final Executor executor) {
		if (runnable == null) {
			throw new NullPointerException("runnable == null");
		}
		if (executor == null) {
			throw new NullPointerException("executor == null");
		}

		if (notified) {
			executor.execute(runnable);
			return;
		}

		synchronized (sync) {
			// re-check for safety
			if (notified) {
				executor.execute(runnable);
				return;
			}
			if (tasks == null) {
				tasks = new ArrayDeque<>();
			}
			tasks.add(new Task(runnable, executor));
		}
	}

	/**
	 * @return a {@link Builder} for {@link MergingCallbackFuture}, which uses a
	 *         {@link Merger} created using {@link #createNullMerger()}
	 */
	@Nonnull
	public static <E> Builder<E> builder() {
		return new Builder<>();
	}

	/**
	 * @author Ricardo Padilha
	 */
	private static final class Task {

		private final Runnable runnable;
		private final Executor executor;

		Task(@Nonnull final Runnable runnable) {
			this.runnable = runnable;
			this.executor = null;
		}

		Task(@Nonnull final Runnable runnable, @Nonnull final Executor executor) {
			this.runnable = runnable;
			this.executor = executor;
		}

		void execute() {
			if (executor != null) {
				executor.execute(runnable);
				return;
			}
			runnable.run();
		}
	}

	/**
	 * If no Merger is provided, a {@link FixedMerger} returning
	 * <code>null</code> is used.
	 * 
	 * @author Ricardo Padilha
	 */
	public static final class Builder<E> {

		private final List<CallbackFuture<E>> list;
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
		public Builder<E> mergeWith(@Nonnull final Merger<E> merger) {
			if (merger == null) {
				throw new NullPointerException("merger == null");
			}
			this.merger = merger;
			return this;
		}

		@Optional(defaultValue = "empty", restrictions = "future != null")
		public Builder<E> add(@Nonnull final CallbackFuture<E> future) {
			if (future == null) {
				throw new NullPointerException("future == null");
			}
			list.add(future);
			return this;
		}

		@Nonnull
		public MergingCallbackFuture<E> build() {
			return new MergingCallbackFuture<>(merger, list);
		}

	}
}
