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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ricardo Padilha
 */
public final class CountDownFuture<V> implements Future<V> {

	private final CountDownLatch latch;
	private final V value;
	private volatile Throwable exception;

	public CountDownFuture(final CountDownLatch latch, final V value) {
		if (latch == null) {
			throw new NullPointerException("latch == null");
		}
		this.latch = latch;
		this.value = value;
	}

	public void fail(final Throwable exception) {
		assert latch.getCount() > 0;
		this.exception = exception;
		while (latch.getCount() > 0) {
			latch.countDown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get() throws InterruptedException, ExecutionException {
		latch.await();
		final Throwable t = exception;
		if (t != null) {
			throw new ExecutionException(t);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
			TimeoutException {
		if (!latch.await(timeout, unit)) {
			throw new TimeoutException();
		}
		final Throwable t = exception;
		if (t != null) {
			throw new ExecutionException(t);
		}
		return value;
	}

}
