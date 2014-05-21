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

package net.dsys.commons.api.exception;

/**
 * This class is thrown when there is a condition in the code that can only be
 * the result of a coding bug.
 * 
 * @author Ricardo Padilha
 */
public class Bug extends Error {

	private static final long serialVersionUID = 1L;

	/**
	 * @see Error#Error(String)
	 */
	public Bug(final String message) {
		super(message);
	}

	/**
	 * @see Error#Error(Throwable)
	 */
	public Bug(final Throwable cause) {
		super(cause);
	}

	/**
	 * @see Error#Error(String, Throwable)
	 */
	public Bug(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see Error#Error(String, Throwable, boolean, boolean)
	 */
	protected Bug(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
