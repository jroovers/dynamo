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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.table.GridUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Property;

/**
 * TODO: do we need all these additional formatting methods?
 * 
 * Utilities for formatting property values
 * 
 * @author bas.rutten
 *
 */
public final class FormatUtils {

	private static MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	private static EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator()
			.getEntityModelFactory();

	private FormatUtils() {
		// private constructor
	}

	/**
	 * Extracts a field value from an object and formats it
	 * 
	 * @param am  the attribute model
	 * @param obj the object from which to extract the value
	 * @return
	 */
	public static <T> String extractAndFormat(Grid<T> grid, AttributeModel am, Object obj) {
		Object value = ClassUtils.getFieldValue(obj, am.getPath());
		return formatPropertyValue(grid, entityModelFactory, am, value, VaadinUtils.getLocale(), ",");
	}

	/**
	 * Formats and entity
	 * 
	 * @param entityModel the entity model for the entity
	 * @param value       the entity
	 * @return
	 */
	public static String formatEntity(EntityModel<?> entityModel, Object value) {
		if (value instanceof AbstractEntity) {
			AbstractEntity<?> entity = (AbstractEntity<?>) value;
			if (entityModel.getDisplayProperty() != null) {
				return ClassUtils.getFieldValueAsString(entity, entityModel.getDisplayProperty());
			} else {
				return entity.toString();
			}
		}
		return null;
	}

	/**
	 * Formats a collection of entities (turns it into a comma-separated string
	 * based on the value of the "displayProperty")
	 *
	 * @param entityModelFactory the entity model factory
	 * @param collection         the collection of entities to format
	 * @return
	 */
	public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel,
			Object collection, String separator) {
		List<String> result = new ArrayList<>();
		Iterable<?> col = (Iterable<?>) collection;
		for (Object next : col) {
			if (next instanceof AbstractEntity) {
				EntityModel<?> entityModel = entityModelFactory.getModel(next.getClass());
				String displayProperty = entityModel.getDisplayProperty();
				if (displayProperty != null) {
					result.add(ClassUtils.getFieldValueAsString(next, displayProperty));
				} else {
					result.add(next.toString());
				}
			} else if (next instanceof Number) {
				result.add(VaadinUtils.numberToString(attributeModel, attributeModel.getNormalizedType(), next, true,
						VaadinUtils.getLocale()));
			} else {
				result.add(next.toString());
			}
		}
		return result.stream().collect(Collectors.joining(separator));
	}

	/**
	 * Formats a property value
	 * 
	 * @param entityModelFactory the entity model factory
	 * @param model              the attribute model for the property
	 * @param value              the value of the property
	 * @return
	 */
	public static String formatPropertyValue(EntityModelFactory entityModelFactory, AttributeModel model, Object value,
			String separator) {
		return formatPropertyValue(null, entityModelFactory, model, value, VaadinUtils.getLocale(), separator);
	}

	/**
	 * Formats a property value
	 *
	 * @param table              the table in which the property occurs
	 * @param entityModelFactory the entity model factor
	 * @param entityModel        the entity model
	 * @param rowId              the row ID of the property
	 * @param colId              the column ID/property
	 * @param property           the property
	 * @return
	 */
	public static <T> String formatPropertyValue(Grid<T> table, EntityModelFactory entityModelFactory,
			EntityModel<T> entityModel, Object rowId, Object colId, Property<?> property, String separator) {
		return formatPropertyValue(table, entityModelFactory, entityModel, rowId, colId, property,
				VaadinUtils.getLocale(), separator);
	}

	/**
	 * Formats a property value - for use with a hierarchical grid
	 * 
	 * @param table
	 * @param entityModelFactory
	 * @param entityModel
	 * @param messageService
	 * @param rowId
	 * @param colId
	 * @param property
	 * @param locale
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> String formatPropertyValue(Grid<T> grid, EntityModelFactory entityModelFactory,
			EntityModel<T> entityModel, Object rowId, Object colId, Property<?> property, Locale locale,
			String separator) {
		// if (table.getContainerDataSource() instanceof
		// ModelBasedHierarchicalContainer) {
		// ModelBasedHierarchicalContainer<?> c = (ModelBasedHierarchicalContainer<?>)
		// table.getContainerDataSource();
		// ModelBasedHierarchicalDefinition def =
		// c.getHierarchicalDefinitionByItemId(rowId);
		// Object path = c.unmapProperty(def, colId);
		// return formatPropertyValue(table, entityModelFactory,
		// path == null ? null :
		// def.getEntityModel().getAttributeModel(path.toString()), property.getValue(),
		// locale, separator);
		// }
		return formatPropertyValue(grid, entityModelFactory, entityModel.getAttributeModel(colId.toString()),
				property.getValue(), locale, separator);
	}

	/**
	 * Formats a property value
	 *
	 * @param entityModelFactory the entity model factory
	 * @param entityModel        the entity model
	 * @param model              the attribute model
	 * @param value              the property value
	 * @param locale             the locale to use
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> String formatPropertyValue(Grid<T> grid, EntityModelFactory entityModelFactory,
			AttributeModel model, Object value, Locale locale, String separator) {
		if (model != null && value != null) {
			if (model.isWeek()) {
				if (value instanceof LocalDate) {
					return DateUtils.toWeekCode((LocalDate) value);
				}
			} else if (Boolean.class.equals(model.getType()) || boolean.class.equals(model.getType())) {
				if (!StringUtils.isEmpty(model.getTrueRepresentation()) && Boolean.TRUE.equals(value)) {
					return model.getTrueRepresentation();
				} else if (!StringUtils.isEmpty(model.getFalseRepresentation()) && Boolean.FALSE.equals(value)) {
					return model.getFalseRepresentation();
				}
				return Boolean.toString(Boolean.TRUE.equals(value));
			} else if (LocalDate.class.equals(model.getType())) {
				// in case of a date field, use the entered display format
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(model.getDisplayFormat());

				// set time zone for a time stamp field
				if (AttributeDateType.TIMESTAMP.equals(model.getDateType())) {
					dateTimeFormatter = dateTimeFormatter.withZone(VaadinUtils.getTimeZone(UI.getCurrent()));
				}
				return dateTimeFormatter.format((LocalDate)value);
			} else if (DateUtils.isJava8DateType(model.getType())) {
				return DateUtils.formatJava8Date(model.getType(), value, model.getDisplayFormat());
			} else if (BigDecimal.class.equals(model.getType())) {
				String cs = GridUtils.getCurrencySymbol(grid);
				return VaadinUtils.bigDecimalToString(model.isCurrency(), model.isPercentage(),
						model.isUseThousandsGrouping(), model.getPrecision(), (BigDecimal) value, locale, cs);
			} else if (NumberUtils.isNumeric(model.getType())) {
				// generic functionality for all other numbers
				return VaadinUtils.numberToString(model, model.getType(), value, model.isUseThousandsGrouping(),
						locale);
			} else if (model.getType().isEnum()) {
				// in case of an enumeration, look it up in the message
				// bundle
				String msg = messageService.getEnumMessage((Class<Enum<?>>) model.getType(), (Enum<?>) value,
						VaadinUtils.getLocale());
				if (msg != null) {
					return msg;
				}
			} else if (value instanceof Iterable) {
				String result = formatEntityCollection(entityModelFactory, model, value, separator);
				return grid == null ? result : restrictToMaxLength(result, model);
			} else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
				EntityModel<?> detailEntityModel = model.getNestedEntityModel();
				if (detailEntityModel == null) {
					detailEntityModel = entityModelFactory.getModel(model.getType());
				}
				String displayProperty = detailEntityModel.getDisplayProperty();
				if (displayProperty == null) {
					throw new OCSRuntimeException(
							"No displayProperty set for entity " + detailEntityModel.getEntityClass());
				}
				AttributeModel detailModel = detailEntityModel.getAttributeModel(displayProperty);
				return formatPropertyValue(grid, entityModelFactory, detailModel,
						ClassUtils.getFieldValue(value, displayProperty), locale, separator);
			} else if (value instanceof AbstractEntity) {
				// single entity
				Object result = ClassUtils.getFieldValue(value, model.getPath());
				if (result == null) {
					return null;
				}
				return grid == null ? result.toString() : restrictToMaxLength(result.toString(), model);
			} else {
				// just use the String value
				return grid == null ? value.toString() : restrictToMaxLength(value.toString(), model);
			}
		}
		return null;
	}

	/**
	 * Restricts a value to its maximum length defined in the attribute model
	 *
	 * @param input the input value
	 * @param am    the attribute model
	 * @return
	 */
	private static String restrictToMaxLength(String input, AttributeModel am) {
		if (am.getMaxLengthInTable() != null && input != null && input.length() > am.getMaxLengthInTable()) {
			return input.substring(0, am.getMaxLengthInTable()) + "...";
		}
		return input;
	}
}
