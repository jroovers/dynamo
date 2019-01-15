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
package com.vaadin.data.util.filter;

import java.util.Arrays;
import java.util.Collection;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.shared.util.SharedUtil;

/**
 * Extra Vaadin filter to support the in collection comparision
 * 
 * @author patrickdeenen
 *
 */
public class In implements Filter {

	private final Object propertyId;
	private final Collection<Comparable<?>> values;
	private final boolean emptyValuesIsPass;

	public In(Object propertyId, Collection<Comparable<?>> values, boolean emptyValuesIsPass) {
		super();
		this.propertyId = propertyId;
		this.values = values;
		this.emptyValuesIsPass = emptyValuesIsPass;
	}

	public In(Object propertyId, Collection<Comparable<?>> values) {
		super();
		this.propertyId = propertyId;
		this.values = values;
		this.emptyValuesIsPass = true;
	}

	public Object getPropertyId() {
		return propertyId;
	}

	public Collection<Comparable<?>> getValues() {
		return values;
	}

	public boolean isEmptyValuesIsPass() {
		return emptyValuesIsPass;
	}

	@Override
	public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
		Object value = item.getItemProperty(getPropertyId()).getValue();
		if (value instanceof Comparable) {
			if (values != null && !values.isEmpty()) {
				Comparable comparable = (Comparable) value;
				for (Comparable<?> v : values) {
					if (value.equals(v)) {
						return true;
					}
				}
			} else if (emptyValuesIsPass) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean appliesToProperty(Object propertyId) {
		return getPropertyId() != null && getPropertyId().equals(propertyId);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] { getPropertyId(), getValues(), isEmptyValuesIsPass() });
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		// Only objects of the same class can be equal
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		final In o = (In) obj;

		// Checks the properties one by one
		boolean propertyIdEqual = SharedUtil.equals(getPropertyId(), o.getPropertyId());
		boolean valuesEqual = SharedUtil.equals(getValues(), o.getValues());
		boolean emptyValuesIsPassEqual = SharedUtil.equals(isEmptyValuesIsPass(), o.isEmptyValuesIsPass());
		return propertyIdEqual && valuesEqual && emptyValuesIsPassEqual;

	}
}
