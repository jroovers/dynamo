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
package com.ocs.dynamo.functional.ui;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.ocs.dynamo.functional.domain.Translation;
import com.vaadin.data.util.converter.Converter;

/**
 * @author patrickdeenen
 *
 */
public class TranslationConverter implements Converter<Translation<?>, Set<Translation<?>>> {

	private static final long serialVersionUID = -4917352378661719483L;

	public TranslationConverter() {
		// default constructor
	}

	@Override
	public Set<Translation<?>> convertToModel(Translation<?> value, Class<? extends Set<Translation<?>>> targetType,
			Locale locale) throws ConversionException {
		if (value == null) {
			return null;
		}
		Set<Translation<?>> r = new HashSet<>();
		r.add(value);
		return r;
	}

	@Override
	public Translation<?> convertToPresentation(Set<Translation<?>> value, Class<? extends Translation<?>> targetType,
			Locale locale) throws ConversionException {
		if (value == null) {
			return null;
		}
		Translation<?> result = null;
		for (Translation<?> t : value) {
			if (t.getLocale().getCode().equals(locale.toString())) {
				result = t;
				break;
			}
		}
		if (result == null && !value.isEmpty()) {
			result = value.iterator().next();
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<Set<Translation<?>>> getModelType() {
		return (Class<Set<Translation<?>>>) (Object) Set.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<Translation<?>> getPresentationType() {
		return (Class<Translation<?>>) (Object) Translation.class;
	}

}
