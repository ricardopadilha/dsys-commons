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

import javax.annotation.Nonnull;
import javax.annotation.meta.When;

/**
 * Generic interface for merging data.
 * 
 * @author Ricardo Padilha
 */
public interface Merger<T> {

	/**
	 * @param values
	 *            the values to be merged
	 * @return the merged result
	 */
	@Nonnull(when = When.MAYBE)
	T merge(@Nonnull Collection<T> values);

}
