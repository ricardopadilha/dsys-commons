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

package net.dsys.commons.impl.builder;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This method is part of a group of options. All of the methods annotated with
 * the same group name affect the same property of the builder. If also
 * annotated with {@link Mandatory}, then one of the methods in the group needs
 * to be called. The {@link #seeAlso()} parameter indicates the other methods in
 * the group.
 * 
 * @author Ricardo Padilha
 */
@Documented
@Target({ ElementType.METHOD })
public @interface OptionGroup {

	String name();

	String seeAlso();

}
