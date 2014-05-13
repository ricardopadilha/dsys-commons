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

package net.dsys.commons.impl.lang;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ricardo Padilha
 */
public final class DaemonThreadFactory implements ThreadFactory {

	private final String name;
	private final ThreadGroup group;
	private final AtomicInteger counter;

	public DaemonThreadFactory(final String name) {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		this.name = name;
		this.counter = new AtomicInteger(1);
		final SecurityManager s = System.getSecurityManager();
		if (s != null) {
			this.group = s.getThreadGroup();
		} else {
			this.group = Thread.currentThread().getThreadGroup();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Thread newThread(final Runnable runnable) {
		final String threadName = name + "-" + counter.getAndIncrement();
		final Thread thread = new Thread(group, runnable, threadName);
		thread.setDaemon(true);
		thread.setPriority(Thread.NORM_PRIORITY);
		return thread;
	}

}
