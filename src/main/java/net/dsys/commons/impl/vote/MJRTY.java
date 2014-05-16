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

import net.dsys.commons.api.vote.Election;
import net.dsys.commons.api.vote.Voter;

/**
 * Vote selection based on threshold majority. This is a simple majority vote.
 * We can have up to threshold+1 different replies.
 * 
 * @see <a href="http://www.cs.utexas.edu/ftp/techreports/tr81-32.pdf">MJRTY - A
        Fast Majority Vote Algorithm, with R.S. Boyer. In R.S. Boyer (ed.),
        Automated Reasoning: Essays in Honor of Woody Bledsoe, Automated
        Reasoning Series, Kluwer Academic Publishers, Dordrecht, The
        Netherlands, 1991, pp. 105-117.</a>
 * @author Ricardo Padilha
 */
public final class MJRTY implements Election {

	public MJRTY() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <V> V elect(final int threshold, final Voter<V> converter, final Iterable<V> votes) {
		if (votes == null) {
			return null;
		}

		V winner = null;
		int winnerValue = 0;
		int count = 0;
		// first pass: pairing
		for (final V v : votes) {
			if (v == null) {
				continue;
			} else if (winner == null) {
				winner = v;
				winnerValue = converter.getVote(v);
				count = 1;
			} else if (winnerValue == converter.getVote(v)) {
				count++;
			} else if (--count == 0) {
				winner = null;
			}
		}
		// second pass: counting
		if (winner != null && count < threshold) {
			int recount = 0;
			int nulls = 0;
			for (final V v : votes) {
				if (v == null) {
					nulls++;
				} else if (winner.equals(v) && ++recount >= threshold) {
					return winner;
				}
			}
			if (nulls == 0) {
				// ERROR: Too many different replies!
				throw new IllegalArgumentException("No majority found above threshold");
			}
		}
		return winner;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int elect(final int threshold, final int nullValue, final int... votes) {
		final int k = votes.length;
		int nulls = 0;
		int index = -1;
		int winner = nullValue;
		int count = 0;
		// first pass: pairing
		for (int i = 0; i < k; i++) {
			final int value = votes[i];
			if (value == nullValue) {
				nulls++;
			} else if (winner == nullValue) {
				index = i;
				winner = value;
				count = 1;
			} else if (winner == value) {
				count++;
			} else if (--count == 0) {
				index = -1;
				winner = nullValue;
			}
		}
		// second pass: counting
		if (winner != nullValue && count < threshold) {
			int recount = 0;
			for (int i = 0; i < k; i++) {
				if (winner == votes[i] && ++recount >= threshold) {
					return index;
				}
			}
			if (nulls == 0) {
				// ERROR: Too many different replies!
				throw new IllegalStateException("No majority found above threshold");
			}
		}
		return index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int elect(final int threshold, final long nullValue, final long... votes) {
		final int k = votes.length;
		int nulls = 0;
		int index = -1;
		long winner = nullValue;
		int count = 0;
		// first pass: pairing
		for (int i = 0; i < k; i++) {
			final long value = votes[i];
			if (value == nullValue) {
				nulls++;
			} else if (winner == nullValue) {
				index = i;
				winner = value;
				count = 1;
			} else if (winner == value) {
				count++;
			} else if (--count == 0) {
				index = -1;
				winner = nullValue;
			}
		}
		// second pass: counting
		if (winner != nullValue && count < threshold) {
			int recount = 0;
			for (int i = 0; i < k; i++) {
				if (winner == votes[i] && ++recount >= threshold) {
					return index;
				}
			}
			if (nulls == 0) {
				// ERROR: Too many different replies!
				throw new IllegalStateException("No majority found above threshold");
			}
		}
		return index;
	}
}
