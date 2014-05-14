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

package net.dsys.commons.impl.vote;

import net.dsys.commons.api.vote.Voter;

/**
 * A voter based on {@link Object#hashCode()}.
 * 
 * @author Ricardo Padilha
 */
public final class HashcodeVoter<V> implements Voter<V> {

	private static final int NULL_VOTE = -1;

	public HashcodeVoter() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getVote(final V value) {
		if (value == null) {
			return NULL_VOTE;
		}
		return value.hashCode();
	}
}
