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
package com.ocs.dynamo.ui.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * Converter for converting between integer and string - adds percentage sign
 * when needed
 * 
 * @author Bas Rutten
 *
 */
public class PercentageIntegerConverter extends GroupingStringToIntegerConverter {

	private static final long serialVersionUID = -3063510923788897054L;

	public PercentageIntegerConverter(String message, boolean useGrouping) {
		super(message, useGrouping);
	}

	@Override
	public String convertToPresentation(Integer value, ValueContext context) {
		String result = super.convertToPresentation(value, context);
		return result == null ? null : result + "%";
	}

	@Override
	public Result<Integer> convertToModel(String value, ValueContext context) {
		value = value == null ? null : value.replace("%", "");
		return super.convertToModel(value, context);
	}
}
