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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

/**
 * A converter for a BigDecimal field that includes a currency symbol.
 * 
 * @author bas.rutten
 */
public class CurrencyBigDecimalConverter extends BigDecimalConverter {

	private static final long serialVersionUID = -8785156070280947096L;

	private String currencySymbol;

	/**
	 * 
	 * Constructor for CurrencyBigDecimalConverter.
	 * 
	 * @param precision
	 * @param useGrouping
	 * @param currencySymbol
	 */
	public CurrencyBigDecimalConverter(int precision, boolean useGrouping, String currencySymbol) {
		super(precision, useGrouping);
		this.currencySymbol = currencySymbol;
	}

	@Override
	public Result<BigDecimal> convertToModel(String value, ValueContext context) {
		if (value == null) {
			return null;
		}

		if (!StringUtils.isEmpty(value) && !value.startsWith(currencySymbol)) {
			String oldValue = value.trim();
			value = currencySymbol;
			value += this.getDecimalFormat(context.getLocale().orElse(VaadinUtils.getLocale())).getPositivePrefix()
					.length() > 1 ? " " : "";
			value += oldValue;
		}
		return super.convertToModel(value, context);
	}

	@Override
	protected DecimalFormat constructFormat(Locale locale) {
		// ignore the locale that is passed as a parameter, and use the default
		// locale instead so
		// that the number formatting is always the same
		DecimalFormat nf = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
		DecimalFormatSymbols s = nf.getDecimalFormatSymbols();
		s.setCurrencySymbol(currencySymbol);
		nf.setDecimalFormatSymbols(s);
		return nf;
	}
}
