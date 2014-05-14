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

package net.dsys.commons.api.lang;

import java.util.Collection;

/**
 * A conditional merger will only merge if enough values are given. Callers can
 * check if there are enough values using {@link #canMerge(Collection)}.
 * 
 * @author Ricardo Padilha
 */
public interface ConditionalMerger<T> extends Merger<T> {

	/**
	 * @param values
	 *            the values to be merged
	 * @return <code>true</code> if there are enough values to be merged
	 */
	boolean canMerge(Collection<T> values);

	/**
	 * @param values
	 *            the values to be merged
	 * @return the merged result
	 * @throws IllegalArgumentException
	 *             if there are not enough values to merge
	 */
	@Override
	T merge(Collection<T> values);

}
