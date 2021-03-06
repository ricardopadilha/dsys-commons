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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import javax.annotation.Nonnull;

/**
 * @author Ricardo Padilha
 */
public final class SettableFuture<V> implements Future<V> {

	private final Object sync;
	private boolean done;
	private boolean cancelled;
	private V value;
	private Throwable exception;

	public SettableFuture() {
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
			sync.notifyAll();
		}
	}

	/**
	 * Define the outcome of this future, notify threads waiting on
	 * {@link #get()}.
	 */
	public void fail(@Nonnull final Throwable exception) {
		synchronized (sync) {
			if (done) {
				return;
			}
			this.exception = exception;
			this.done = true;
			sync.notifyAll();
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
			sync.notifyAll();
			return true;
		}
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
				if (exception != null) {
					throw new ExecutionException(exception);
				}
				return value;
			}
			while (!done) {
				sync.wait();
			}
			if (cancelled) {
				throw new CancellationException();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
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
				if (exception != null) {
					throw new ExecutionException(exception);
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
}
