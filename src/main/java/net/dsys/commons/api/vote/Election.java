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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.meta.When;

/**
 * An Election implements a voting algorithm to select the majority element
 * in a given collection of votes.
 * 
 * @author Ricardo Padilha
 */
public interface Election {

	/**
	 * Select the majority out of an {@link Iterable}, based on {@link Vote#value()}.
	 * @param threshold
	 *            minimum vote count threshold
	 * @param converter the conversion function for the votes
	 * @param votes
	 *            possible candidates for election
	 * 
	 * @return the winner or <code>null</code> if none found
	 * @throws IllegalArgumentException
	 *             if there are more different candidates than the threshold
	 */
	@Nonnull(when = When.MAYBE)
	<V> V elect(@Nonnegative int threshold, @Nonnull Voter<V> converter, @Nonnull Iterable<V> votes);

	/**
	 * Select the majority out of an array, based on value.
	 * @param threshold
	 *            minimum vote count threshold
	 * @param nullValue
	 *            value that represents <code>null</code>, and should be ignored
	 * @param votes
	 *            possible candidates for majority selection
	 * 
	 * @return the index of the winner or <code>-1</code> if none found
	 * @throws IllegalArgumentException
	 *             if there are more different candidates than the threshold
	 */
	@Nonnegative(when = When.MAYBE)
	int elect(@Nonnegative int threshold, int nullValue, @Nonnull int... votes);

	/**
	 * Select the majority out of an array, based on value.
	 * @param threshold
	 *            minimum vote count threshold
	 * @param nullValue
	 *            value that represents <code>null</code>, and should be ignored
	 * @param votes
	 *            possible candidates for majority selection
	 * 
	 * @return the index of the winner or <code>-1</code> if none found
	 * @throws IllegalArgumentException
	 *             if there are more different candidates than the threshold
	 */
	@Nonnegative(when = When.MAYBE)
	int elect(@Nonnegative int threshold, long nullValue, @Nonnull long... votes);

}
