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

import java.nio.ByteBuffer;

import javax.annotation.Nonnegative;

import net.dsys.commons.api.lang.Factory;

/**
 * @author Ricardo Padilha
 */
public final class ByteBufferFactory implements Factory<ByteBuffer> {

	private final int length;

	public ByteBufferFactory(@Nonnegative final int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length < 1");
		}
		this.length = length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer newInstance() {
		return ByteBuffer.allocate(length);
	}

}
