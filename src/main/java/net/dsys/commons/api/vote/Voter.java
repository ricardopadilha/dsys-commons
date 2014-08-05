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

package net.dsys.commons.api.vote;

import javax.annotation.Nonnull;
import javax.annotation.meta.When;

/**
 * This is an helper interface to {@link Election}. Implementors have to provide an
 * integer that represents the vote of the given object.
 * 
 * @author Ricardo Padilha
 */
public interface Voter<V> {

	/**
	 * @return an integer that represents the vote of a given object
	 */
	int getVote(@Nonnull(when = When.MAYBE) V value);

}
