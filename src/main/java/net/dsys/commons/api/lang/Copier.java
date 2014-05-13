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

/**
 * Interface used to provide generic copiers, i.e., implementations that copy
 * the content of one object into another.
 * 
 * @author Ricardo Padilha
 */
public interface Copier<T> {

	/**
	 * Copy the content of the input into the output, clearing the output if
	 * needed. The state of the input must remain unchanged.
	 */
	void copy(T in, T out);

}
