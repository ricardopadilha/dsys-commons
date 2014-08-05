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
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import net.dsys.commons.api.future.CallbackFuture;

/**
 * @author Ricardo Padilha
 */
public final class SettableCallbackFuture<V> implements CallbackFuture<V> {

	private final Object sync;
	private boolean done;
	private boolean cancelled;
	private V value;
	private Throwable throwable;
	private Queue<Task> tasks;
	private boolean notified;

	public SettableCallbackFuture() {
		this.sync = new Object();
	}

	/**
	 * Define the outcome of this future, notify threads waiting on
	 * {@link #get()}.
	 */
	public void success(final V value) {
		synchronized (sync) {
			if (done) {
				return;
			}
			this.value = value;
			this.done = true;
			notifyCompletion();
		}
	}

	/**
	 * Define the outcome of this future, notify threads waiting on
	 * {@link #get()}.
	 */
	public void fail(@Nonnull final Throwable throwable) {
		synchronized (sync) {
			if (done) {
				return;
			}
			this.throwable = throwable;
			this.done = true;
			notifyCompletion();
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
			notifyCompletion();
			return true;
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
	public V get() throws InterruptedException, ExecutionException {
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
			throws InterruptedException, ExecutionException, TimeoutException {
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
			final long millis = unit.toMillis(timeout);
			final int nanos = (int) (unit.toNanos(timeout) - TimeUnit.MILLISECONDS.toNanos(millis));
			sync.wait(millis, nanos);
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
}
