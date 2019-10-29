/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui;

import com.vaadin.flow.function.SerializablePredicate;

/**
 * An interface for objects that can receive search requests
 * 
 * @param <T>
 * @author bas.rutten
 */
@FunctionalInterface
public interface Searchable<T> {

	/**
	 * Perform the search
	 * 
	 * @param filter
	 *            the filter to apply to the search
	 */
	void search(SerializablePredicate<T> filter);

}
