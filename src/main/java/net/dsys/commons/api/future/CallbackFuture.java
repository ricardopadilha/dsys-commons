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

package net.dsys.commons.api.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

/**
 * Based on Guava's ListenableFuture.
 * 
 * @author Ricardo Padilha
 */
public interface CallbackFuture<V> extends Future<V> {

    /**
     * Overridden to include annotations.
     * 
     * {@inheritDoc}
     */
    @Override
    
	V get() throws InterruptedException, ExecutionException;

    @Override
	V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException;

	/**
	 * If this future is not done, the runnable will run in the same thread that
	 * completes the future. Otherwise it will run in the caller's thread.
	 */
	void onCompletion(@Nonnull Runnable runnable);

	/**
	 * The runnable will be executed by the executor.
	 */
	void onCompletion(@Nonnull Runnable runnable, @Nonnull Executor executor);

}
