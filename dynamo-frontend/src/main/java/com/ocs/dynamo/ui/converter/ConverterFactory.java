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

import java.math.BigDecimal;
import java.time.ZoneId;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.data.Converter;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.converter.StringToLongConverter;

public final class ConverterFactory {

	private ConverterFactory() {
		// hidden constructor
	}

	/**
	 * Creates a BigDecimalConverter
	 * 
	 * @param currency       whether the field is a currency field
	 * @param percentage     whether to include a percentage sign
	 * @param useGrouping    whether to uses a thousands grouping
	 * @param precision      the desired decimal precision
	 * @param currencySymbol the currency symbol to include
	 * @return
	 */
	public static BigDecimalConverter createBigDecimalConverter(boolean currency, boolean percentage,
			boolean useGrouping, int precision, String currencySymbol) {
		if (currency) {
			return new CurrencyBigDecimalConverter(precision, useGrouping, currencySymbol);
		} else if (percentage) {
			return new PercentageBigDecimalConverter(precision, useGrouping);
		}
		return new BigDecimalConverter(precision, useGrouping);
	}

	/**
	 * Create a converter for a certain type
	 * 
	 * @param clazz          the type
	 * @param attributeModel the attribute model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Converter<String, T> createConverterFor(Class<T> clazz, AttributeModel attributeModel,
			boolean grouping) {
		if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
			return (Converter<String, T>) createIntegerConverter(grouping, attributeModel.isPercentage());
		} else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
			return (Converter<String, T>) createLongConverter(grouping, attributeModel.isPercentage());
		} else if (clazz.equals(BigDecimal.class)) {
			return (Converter<String, T>) createBigDecimalConverter(attributeModel.isCurrency(),
					attributeModel.isPercentage(), grouping, attributeModel.getPrecision(),
					SystemPropertyUtils.getDefaultCurrencySymbol());
		}
		return null;
	}

	/**
	 * Creates a converter for converting between integer and String
	 * 
	 * @param useGrouping whether to use the thousands grouping separator
	 * @return
	 */
	public static StringToIntegerConverter createIntegerConverter(boolean useGrouping, boolean percentage) {
		return percentage ? new PercentageIntegerConverter(useGrouping)
				: new GroupingStringToIntegerConverter(useGrouping);
	}

	/**
	 * Creates a converter for a LocalDate
	 * 
	 * @return
	 */
	public static LocalDateToDateConverter createLocalDateConverter() {
		return new LocalDateToDateConverter();
	}

	/**
	 * Creates a converter for a LocalDateTimeConverter
	 * 
	 * @return
	 */
	public static LocalDateTimeToDateConverter createLocalDateTimeConverter() {
		return new LocalDateTimeToDateConverter();
	}

	/**
	 * Creates a converter for a LocalTime
	 * 
	 * @return
	 */
	public static LocalTimeToDateConverter createLocalTimeConverter() {
		return new LocalTimeToDateConverter();
	}

	/**
	 * Creates a converter for converting between long and String
	 * 
	 * @param useGrouping whether to use a grouping
	 * @param percentage  whether to include a percentage sign
	 * @return
	 */
	public static StringToLongConverter createLongConverter(boolean useGrouping, boolean percentage) {
		return percentage ? new PercentageLongConverter(useGrouping) : new GroupingStringToLongConverter(useGrouping);
	}

	/**
	 * Creates a converter for a ZonedDateTime
	 * 
	 * @return
	 */
	public static ZonedDateTimeToLocalDateTimeConverter createZonedDateTimeConverter(ZoneId zoneId) {
		return new ZonedDateTimeToLocalDateTimeConverter(zoneId);
	}
}
