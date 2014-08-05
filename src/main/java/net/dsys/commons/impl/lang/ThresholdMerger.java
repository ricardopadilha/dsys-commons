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

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.dsys.commons.api.lang.ConditionalMerger;
import net.dsys.commons.api.vote.Election;
import net.dsys.commons.api.vote.Voter;
import net.dsys.commons.impl.vote.HashcodeVoter;
import net.dsys.commons.impl.vote.MJRTY;

/**
 * Merges input using a voting algorithm to select the majority vote according
 * to a threshold.
 * 
 * @author Ricardo Padilha
 * @see MJRTY
 */
public final class ThresholdMerger<T> implements ConditionalMerger<T> {

	private final int threshold;
	private final Election election;
	private final Voter<T> voter;

	/**
	 * Vote is performed using {@link Object#hashCode()}.
	 * 
	 * @param threshold
	 *            threshold for valid merge
	 */
	public ThresholdMerger(@Nonnegative final int threshold) {
		this(threshold, new HashcodeVoter<T>());
	}

	/**
	 * @param threshold
	 *            threshold for valid merge
	 * @param voter
	 *            voting function
	 */
	public ThresholdMerger(@Nonnegative final int threshold, @Nonnull final Voter<T> voter) {
		if (threshold < 1) {
			throw new IllegalArgumentException("threshold < 1");
		}
		if (voter == null) {
			throw new NullPointerException("voter == null");
		}
		this.threshold = threshold;
		this.election = new MJRTY();
		this.voter = voter;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return values.size() >= threshold
	 */
	@Override
	public boolean canMerge(final Collection<T> values) {
		return values.size() >= threshold;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T merge(final Collection<T> values) {
		return election.elect(threshold, voter, values);
	}
}
