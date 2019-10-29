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
package com.ocs.dynamo.ui.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.server.VaadinSession;

/**
 * Utility class for Vaadin-related functionality
 * 
 * @author bas.rutten
 */
public final class VaadinUtils {

    private static String appendPercentage(String s, boolean percentage) {
        if (s == null) {
            return null;
        }
        return percentage ? s + "%" : s;
    }

    /**
     * Converts a BigDecimal value to a String
     *
     * @param percentage  whether the value represents a percentage
     * @param useGrouping whether to use a thousand grouping
     * @param value       the value
     * @param locale      the locale to use
     * @return
     */
    public static String bigDecimalToString(boolean percentage, boolean useGrouping, BigDecimal value) {
        return bigDecimalToString(false, percentage, useGrouping, SystemPropertyUtils.getDefaultDecimalPrecision(), value, getLocale());
    }

    /**
     *
     * Converts a BigDecimal value to a String (shortcut for values that are not
     * currency and not percentage)
     *
     * @param percentage  whether the value represents a percentage
     * @param useGrouping whether to use a thousand grouping
     * @param value       the value
     * @return
     */
    public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, BigDecimal value) {
        return bigDecimalToString(currency, percentage, useGrouping, SystemPropertyUtils.getDefaultDecimalPrecision(), value, getLocale());
    }

    /**
     * * Converts a BigDecimal value to a String
     *
     * @param currency    whether the value represents a currency
     * @param percentage  whether the value represents a percentage
     * @param useGrouping whether to use a thousand grouping
     * @param value       the value
     * @param locale      the locale to use
     * @return
     */
    public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision, BigDecimal value,
            Locale locale) {
        return bigDecimalToString(currency, percentage, useGrouping, precision, value, locale, getCurrencySymbol());
    }

    /**
     * Converts a BigDecimal to a String
     * 
     * @param currency       whether to include a currency symbol
     * @param percentage     whether to include a percentage sign
     * @param useGrouping    whether to use a thousands grouping separator
     * @param precision      the desired precision
     * @param value          the value to convert
     * @param locale         the locale to use
     * @param currencySymbol the currency symbol to use
     * @return
     */
    public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision, BigDecimal value,
            Locale locale, String currencySymbol) {
        return fractionalToString(currency, percentage, useGrouping, precision, value, locale, currencySymbol);
    }

    /**
     * Converts a double to a String
     * 
     * @param currency    whether to include a currency symbol
     * @param percentage  whether to include a percentage sign
     * @param useGrouping whether to use a thousands grouping separator
     * @param precision   the desired precision
     * @param value       the value to convert
     * @param locale      the locale to use
     * @return
     */
    public static String doubleToString(boolean currency, boolean percentage, boolean useGrouping, int precision, Double value,
            Locale locale) {
        return doubleToString(currency, percentage, useGrouping, precision, value, locale, getCurrencySymbol());
    }

    /**
     * Converts a double to a String
     * 
     * @param currency       whether to include a currency symbol
     * @param percentage     whether to include a percentage sign
     * @param useGrouping    whether to use a thousands grouping separator
     * @param precision      the desired precision
     * @param value          the value to convert
     * @param locale         the locale to use
     * @param currencySymbol the currency symbol to use
     * @return
     */
    public static String doubleToString(boolean currency, boolean percentage, boolean useGrouping, int precision, Double value,
            Locale locale, String currencySymbol) {
        return fractionalToString(currency, percentage, useGrouping, precision, value, locale, currencySymbol);
    }

    /**
     * Converts a fractional value to a String
     * 
     * @param currency       whether to include a currency symbol
     * @param percentage     whether to include a percentage sign
     * @param useGrouping    whether to use a thousands grouping separator
     * @param precision      the desired precision
     * @param value          the value to convert
     * @param locale         the locale to use
     * @param currencySymbol the currency symbol to use
     * @return
     */
    private static String fractionalToString(boolean currency, boolean percentage, boolean useGrouping, int precision, Number value,
            Locale locale, String currencySymbol) {
        if (value == null) {
            return null;
        }

        DecimalFormat df = null;
        if (currency) {
            df = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
            DecimalFormatSymbols s = df.getDecimalFormatSymbols();
            s.setCurrencySymbol(currencySymbol);
            df.setDecimalFormatSymbols(s);
        } else {
            df = (DecimalFormat) DecimalFormat.getInstance(locale);
        }
        df.setGroupingUsed(useGrouping);
        df.setMaximumFractionDigits(precision);
        df.setMinimumFractionDigits(precision);

        String s = df.format(value);
        return appendPercentage(s, percentage);
    }

    /**
     * Returns the currency symbol to be used - by default this is looked up from
     * the session, with a fallback to the system property "default.currency.symbol"
     *
     * @return
     */
    public static String getCurrencySymbol() {
        String cs = SystemPropertyUtils.getDefaultCurrencySymbol();

        VaadinSession vs = VaadinSession.getCurrent();
        if (vs != null && vs.getAttribute(DynamoConstants.CURRENCY_SYMBOL) != null) {
            cs = (String) vs.getAttribute(DynamoConstants.CURRENCY_SYMBOL);
        }
        return cs;
    }

    /**
     * Returns the locale to be used inside date picker components. This checks for
     * the presence of the DynamoConstants.DATE_LOCALE setting on the session. If
     * this is not set, it falls back to the normal locale mechanism
     * 
     * @return
     */
    public static Locale getDateLocale() {
        if (VaadinSession.getCurrent() != null && VaadinSession.getCurrent().getAttribute(DynamoConstants.DATE_LOCALE) != null) {
            return (Locale) VaadinSession.getCurrent().getAttribute(DynamoConstants.DATE_LOCALE);
        }
        return new Locale(SystemPropertyUtils.getDefaultDateLocale());
    }

    /**
     * Extracts the first value from a map entry that contains a collection
     *
     * @param map the map
     * @param key the map key
     * @return
     */
    public static String getFirstValueFromCollection(Map<String, Object> map, String key) {
        if (map != null) {
            Collection<?> col = (Collection<?>) map.get(key);
            if (col != null) {
                return col.iterator().next().toString();
            }
        }
        return null;
    }

    /**
     * Returns the locale associated with the current Vaadin session
     *
     * @return
     */
    public static Locale getLocale() {
        if (VaadinSession.getCurrent() != null && VaadinSession.getCurrent().getLocale() != null) {
            return VaadinSession.getCurrent().getLocale();
        }
        return new Locale(SystemPropertyUtils.getDefaultLocale());
    }

    /**
     * Returns the first parent component of the specified component that is a
     * subclass of the specified class
     *
     * @param component the component
     * @param clazz     the class
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getParentOfClass(Component component, Class<T> clazz) {
        while (component.getParent().isPresent()) {
            component = component.getParent().orElse(null);
            if (clazz.isAssignableFrom(component.getClass())) {
                return (T) component;
            }
        }
        return null;
    }

    /**
     * Returns the first value from a session attribute that contains a map
     *
     * @param attributeName the name of the attribute that holds the map
     * @param key           the map key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getSessionAttributeValueFromMap(String attributeName, String key) {
        Map<String, Object> map = (Map<String, Object>) VaadinSession.getCurrent().getSession().getAttribute(attributeName);
        return getFirstValueFromCollection(map, key);
    }

    /**
     * Converts an Integer to a String, using the Vaadin converters
     *
     * @param grouping indicates whether grouping separators must be used
     * @param value    the value to convert
     * @return
     */
    public static String integerToString(boolean grouping, boolean percentage, Integer value) {
        return integerToString(grouping, percentage, value, getLocale());
    }

    /**
     * Converts an Integer to a String, using the Vaadin converters
     *
     * @param grouping indicates whether grouping separators must be used
     * @param value    the value to convert
     * @param locale   the locale
     * @return
     */
    public static String integerToString(boolean grouping, boolean percentage, Integer value, Locale locale) {
        if (value == null) {
            return null;
        }
        NumberFormat format = NumberFormat.getInstance(locale);
        format.setGroupingUsed(grouping);
        String s = format.format(value);
        return appendPercentage(s, percentage);
    }

    /**
     * Converts an Long to a String, using the Vaadin converters
     *
     * @param grouping indicates whether grouping separators must be used
     * @param value    the value to convert
     * @return
     */
    public static String longToString(boolean grouping, boolean percentage, Long value) {
        return longToString(grouping, percentage, value, getLocale());
    }

    /**
     * Converts an Long to a String, using the Vaadin converters
     *
     * @param grouping indicates whether grouping separators must be used
     * @param value    the value to convert
     * @param locale   the locale
     * @return
     */
    public static String longToString(boolean grouping, boolean percentage, Long value, Locale locale) {
        if (value == null) {
            return null;
        }

        NumberFormat format = NumberFormat.getInstance(locale);
        format.setGroupingUsed(grouping);
        String s = format.format(value);
        return appendPercentage(s, percentage);
    }

    /**
     * Converts a number to a String
     * 
     * @param am
     * @param type
     * @param value
     * @param grouping
     * @param locale
     * @param currencySymbol
     * @return
     */
    public static <T> String numberToString(AttributeModel am, T value, boolean grouping, Locale locale, String currencySymbol) {
        if (NumberUtils.isInteger(am.getNormalizedType())) {
            return integerToString(grouping, am.isPercentage(), (Integer) value);
        } else if (NumberUtils.isLong(am.getNormalizedType())) {
            return longToString(grouping, am.isPercentage(), (Long) value);
        } else if (NumberUtils.isDouble(am.getNormalizedType()) || BigDecimal.class.equals(am.getNormalizedType())) {
            return fractionalToString(am.isCurrency(), am.isPercentage(), grouping, am.getPrecision(), (Number) value, locale,
                    currencySymbol);
        }
        return null;
    }

    /**
     * Sets the label on the provided field
     * 
     * @param field the field
     * @param label the text of the label
     */
    public static void setLabel(Component field, String label) {
        if (field instanceof TextField) {
            ((TextField) field).setLabel(label);
        } else if (field instanceof Checkbox) {
            ((Checkbox) field).setLabel(label);
        } else if (field instanceof CustomField) {
            ((CustomField<?>) field).setLabel(label);
        } else if (field instanceof ComboBox) {
            ((ComboBox<?>) field).setLabel(label);
        } else if (field instanceof TextArea) {
            ((TextArea) field).setLabel(label);
        } else if (field instanceof DatePicker) {
            ((DatePicker) field).setLabel(label);
        } else if (field instanceof TimePicker) {
            ((TimePicker) field).setLabel(label);
        }
    }

    /**
     * Displays a confirmation dialog that runs code when confirmed
     *
     * @param messageService
     * @param question       the question to be displayed in the dialog
     * @param whenConfirmed  the code to execute when the user confirms the dialog
     */
    public static void showConfirmDialog(MessageService messageService, String question, final Runnable whenConfirmed) {
        if (UI.getCurrent() != null) {
            String caption = messageService.getMessage("ocs.confirm", getLocale());
            String yes = messageService.getMessage("ocs.yes", getLocale());
            String no = messageService.getMessage("ocs.no", getLocale());

            ConfirmDialog.createQuestion().withCaption(caption).withMessage(question).withOkButton(whenConfirmed, ButtonOption.caption(yes))
                    .withCancelButton(ButtonOption.caption(no)).open();
        } else {
            whenConfirmed.run();
        }
    }

    /**
     * Displays a confirmation dialog that runs code both when confirmed and
     * canceled
     *
     * @param messageService
     * @param question       the question to be displayed in the dialog
     * @param whenConfirmed  the code to execute when the user confirms the dialog
     * @param whenCanceled   the code to execute when the user cancels the dialog
     */
    public static void showConfirmDialog(MessageService messageService, String question, final Runnable whenConfirmed,
            final Runnable whenCanceled) {
        if (UI.getCurrent() != null) {
            String caption = messageService.getMessage("ocs.confirm", getLocale());
            String yes = messageService.getMessage("ocs.yes", getLocale());
            String no = messageService.getMessage("ocs.no", getLocale());
            ConfirmDialog.createQuestion().withCaption(caption).withMessage(question).withOkButton(whenConfirmed, ButtonOption.caption(yes))
                    .withCancelButton(whenCanceled, ButtonOption.caption(no)).open();
        } else {
            whenConfirmed.run();
        }
    }

    /**
     * Stores the desired date locale in the session
     * 
     * @param locale the locale
     */
    public static void storeDateLocale(Locale locale) {
        VaadinSession.getCurrent().setAttribute(DynamoConstants.DATE_LOCALE, locale);
    }

    /**
     * Stores the default locale configured in the system properties in the Vaadin
     * session
     */
    public static void storeLocale(Locale locale) {
        VaadinSession.getCurrent().setLocale(locale);
    }

    public static void storeInSession(String name, Object value) {
        VaadinSession current = VaadinSession.getCurrent();
        if (current != null) {
            current.setAttribute(name, value);
        }
    }

    public static Object getFromSession(String name) {
        VaadinSession current = VaadinSession.getCurrent();
        if (current != null) {
            return current.getAttribute(name);
        }
        return null;
    }

    /**
     * Converts a String to a BigDecimal
     * 
     * @param percentage  whether a percentage sign might be included
     * @param useGrouping whether a thousands grouping separator might be included
     * @param currency    whether a currency symbol might be included
     * @param precision   the precision
     * @param value       the String value to convert
     * @param locale      the locale to use
     * @return
     */
    public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping, boolean currency, int precision, String value,
            Locale locale) {
        BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(currency, percentage, useGrouping, precision,
                VaadinUtils.getCurrencySymbol());
        return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
    }

    /**
     * Converts a String to a BigDecimal
     * 
     * @param percentage  whether a percentage sign might be included
     * @param useGrouping whether a thousands grouping separator might be included
     * @param currency    whether a currency symbol might be included
     * @param value       the value to include
     * @return
     */
    public static BigDecimal stringToBigDecimal(boolean percentage, boolean useGrouping, boolean currency, String value) {
        return stringToBigDecimal(percentage, useGrouping, currency, SystemPropertyUtils.getDefaultDecimalPrecision(), value, getLocale());
    }

    /**
     * Converts a String to a Double
     * 
     * @param percentage  whether a percentage sign might be included
     * @param useGrouping whether a thousands grouping separator might be included
     * @param currency    whether a currency symbol might be included
     * @param precision   the precision
     * @param value       the String value to convert
     * @param locale      the locale to use
     * @return
     */
    public static Double stringToDouble(boolean percentage, boolean useGrouping, boolean currency, int precision, String value,
            Locale locale) {
        StringToDoubleConverter converter = ConverterFactory.createDoubleConverter(currency, percentage, useGrouping, precision,
                VaadinUtils.getCurrencySymbol());
        return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
    }

    /**
     * Converts a String to an Integer
     *
     * @param grouping whether to include a thousands grouping separator
     * @param value    the String to convert
     * @return
     */
    public static Integer stringToInteger(boolean grouping, String value) {
        return stringToInteger(grouping, value, getLocale());
    }

    /**
     * Converts a String to an Integer
     *
     * @param grouping indicates whether the string could contain grouping
     *                 separators
     * @param value    the String to convert
     * @param locale   the locale to use for the conversion
     * @return
     */
    public static Integer stringToInteger(boolean grouping, String value, Locale locale) {
        StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping, false);
        return converter.convertToModel(value, new ValueContext(getLocale())).getOrThrow(r -> new OCSRuntimeException());
    }

    /**
     * Converts a String to a Long
     *
     * @param grouping indicates if a thousands separator is used
     * @param value    the String to convert
     * @return
     */
    public static Long stringToLong(boolean grouping, String value) {
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
        return converter.convertToModel(value, new ValueContext(getLocale())).getOrThrow(r -> new OCSRuntimeException());
    }

    /**
     * Converts a String to a Long
     *
     * @param grouping indicates if a thousands separator is used
     * @param value    the String to convert to convert
     * @param locale   the locale to use
     * @return
     */
    public static Long stringToLong(boolean grouping, String value, Locale locale) {
        StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
        return converter.convertToModel(value, new ValueContext(locale)).getOrThrow(r -> new OCSRuntimeException());
    }

    private VaadinUtils() {
        // hidden constructor
    }

}
