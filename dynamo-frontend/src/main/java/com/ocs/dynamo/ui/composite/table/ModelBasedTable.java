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
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.BaseUI;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.table.export.TableExportActionHandler;
import com.ocs.dynamo.ui.composite.table.export.TableExportMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * A Table that bases its columns on the meta model of an entity
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class ModelBasedTable<ID extends Serializable, T extends AbstractEntity<ID>> extends Table {

	private static final long serialVersionUID = 6946260934644731038L;

	/**
	 * The container that hold the data for the table
	 */
	private Container container;

	/**
	 * Custom currency symbol to be used for this table
	 */
	private String currencySymbol;

	/**
	 * The entity model of the entities to display in the table
	 */
	private EntityModel<T> entityModel;

	/**
	 * The entity model factory
	 */
	private EntityModelFactory entityModelFactory;

	/**
	 * Indicated whether table export is allowed
	 */
	private boolean exportAllowed;

	/***
	 * Indicate whether to update the caption with the number of items in the table
	 */
	private boolean updateTableCaption = true;

	/**
	 * The message service
	 */
	private MessageService messageService;

	/**
	 * Constructor
	 *
	 * @param container
	 *            the data container
	 * @param model
	 *            the entity model that determines what to display
	 * @param exportAllowed
	 *            whether export of the table is allowed
	 */
	public ModelBasedTable(Container container, EntityModel<T> model, boolean exportAllowed) {
		super("", container);
		this.container = container;
		this.entityModel = model;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();
		this.exportAllowed = exportAllowed;

		TableUtils.defaultInitialization(this);

		// add a custom field factory that takes care of special cases and
		// validation
		this.setTableFieldFactory(ModelBasedFieldFactory.getInstance(entityModel, messageService));
		generateColumns(model);

		// add export functionality
		if (isExportAllowed()) {
			List<EntityModel<?>> list = new ArrayList<>();
			list.add(model);
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), list, model.getDisplayNamePlural(), null,
					false, TableExportMode.EXCEL, null));
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), list, model.getDisplayNamePlural(), null,
					false, TableExportMode.EXCEL_SIMPLIFIED, null));
			addActionHandler(new TableExportActionHandler(UI.getCurrent(), list, model.getDisplayNamePlural(), null,
					false, TableExportMode.CSV, null));
		}

		// update the table caption to reflect the number of items
		if (isUpdateTableCaption()) {
			addItemSetChangeListener(e -> updateTableCaption());
		}
	}

	/**
	 * Adds a column to the table
	 *
	 * @param attributeModel
	 *            the (possibly nested) attribute model for which to add a column
	 * @param propertyNames
	 *            the properties to be added
	 * @param headerNames
	 *            the headers to be added
	 */
	private void addColumn(final AttributeModel attributeModel, List<Object> propertyNames, List<String> headerNames) {
		if (attributeModel.isVisibleInTable()) {
			propertyNames.add(attributeModel.getPath());
			headerNames.add(attributeModel.getDisplayName());

			// for the lazy query container we explicitly have to add the
			// properties - for the standard Bean container this is not
			// needed
			if (container instanceof LazyQueryContainer) {
				LazyQueryContainer lazyContainer = (LazyQueryContainer) container;
				if (!lazyContainer.getContainerPropertyIds().contains(attributeModel.getPath())) {
					lazyContainer.addContainerProperty(attributeModel.getPath(), attributeModel.getType(),
							attributeModel.getDefaultValue(),
							EditableType.READ_ONLY.equals(attributeModel.getEditableType()),
							attributeModel.isSortable());
				}
			}

			// generated column with clickable URL (only in view mode)
			addUrlField(attributeModel);
			addInternalLinkField(attributeModel);

			if (attributeModel.isNumerical()) {
				this.setColumnAlignment(attributeModel.getPath(), Table.Align.RIGHT);
			}
		}
	}

	/**
	 * Adds a generated column
	 *
	 * @param attributeModel
	 *            the attribute model for which to add the column
	 */
	private void addGeneratedColumn(final AttributeModel attributeModel) {
		if (attributeModel.isVisibleInTable() && attributeModel.isUrl()) {
			addUrlField(attributeModel);
		}
	}

	/**
	 * Adds any generated columns (URL fields) in response to a change to view mode
	 */
	public void addGeneratedColumns() {
		for (AttributeModel attributeModel : entityModel.getAttributeModels()) {
			addGeneratedColumn(attributeModel);
			if (attributeModel.getNestedEntityModel() != null) {
				for (AttributeModel nestedAttributeModel : attributeModel.getNestedEntityModel().getAttributeModels()) {
					addGeneratedColumn(nestedAttributeModel);
				}
			}
		}
	}

	/**
	 * Adds an URL field for a certain attribute
	 *
	 * @param attributeModel
	 *            the attribute model
	 */
	private void addUrlField(final AttributeModel attributeModel) {
		if (attributeModel.isUrl() && !isEditable()) {
			this.addGeneratedColumn(attributeModel.getPath(), new ColumnGenerator() {

				private static final long serialVersionUID = -3191235289754428914L;

				@Override
				public Object generateCell(Table source, final Object itemId, Object columnId) {
					URLField field = (URLField) ((ModelBasedFieldFactory<?>) getTableFieldFactory())
							.createField(attributeModel.getPath(), null);
					if (field != null) {
						String val = (String) getItem(itemId).getItemProperty(columnId).getValue();
						field.setValue(val);
					}
					return field;
				}
			});
		}
	}

	/**
	 * Adds a button/link for navigation within the application
	 *
	 * @param attributeModel.
	 *            For this to work you must register a navigation rule in the BaseUI
	 *            at the base of your application
	 */
	private void addInternalLinkField(final AttributeModel attributeModel) {
		if (attributeModel.isNavigable() && !isEditable()
				&& AttributeType.MASTER.equals(attributeModel.getAttributeType())) {
			this.addGeneratedColumn(attributeModel.getPath(), new ColumnGenerator() {

				private static final long serialVersionUID = -3191235289754428914L;

				@Override
				public Object generateCell(Table source, final Object itemId, Object columnId) {
					Object val = getItem(itemId).getItemProperty(columnId).getValue();
					if (val != null) {

						String str = FormatUtils.formatEntity(attributeModel.getNestedEntityModel(), val);
						Button button = new Button(str);
						button.setStyleName(ValoTheme.BUTTON_LINK);
						button.addClickListener(event -> {
							BaseUI ui = (BaseUI) UI.getCurrent();
							ui.navigateToEntityScreenDirectly(val);
						});

						return button;
					}
					return null;
				}
			});
		}
	}

	/**
	 * Overridden to deal with custom formatting
	 */
	@Override
	protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
		String result = FormatUtils.formatPropertyValue(this, entityModelFactory, entityModel, rowId, colId, property,
				", ");
		if (result != null) {
			return result;
		}
		return super.formatPropertyValue(rowId, colId, property);
	}

	/**
	 * Generates the columns of the table based on the entity model
	 *
	 * @param container
	 *            the container
	 * @param model
	 *            the entity model
	 */
	protected void generateColumns(EntityModel<T> model) {
		generateColumns(model.getAttributeModels());
		this.setCaption(model.getDisplayNamePlural());
		this.setDescription(model.getDescription());
	}

	/**
	 * Generates the columns of the table based on a select number of attribute
	 * models
	 *
	 * @param attributeModels
	 *            the attribute models for which to generate columns
	 */
	protected void generateColumns(List<AttributeModel> attributeModels) {
		List<Object> propertyNames = new ArrayList<>();
		List<String> headerNames = new ArrayList<>();
		generateColumnsRecursive(attributeModels, propertyNames, headerNames);
		this.setVisibleColumns(propertyNames.toArray());
		this.setColumnHeaders(headerNames.toArray(new String[0]));
	}

	private void generateColumnsRecursive(List<AttributeModel> attributeModels, List<Object> propertyNames,
			List<String> headerNames) {
		for (AttributeModel attributeModel : attributeModels) {
			addColumn(attributeModel, propertyNames, headerNames);
			if (attributeModel.getNestedEntityModel() != null) {
				generateColumnsRecursive(attributeModel.getNestedEntityModel().getAttributeModels(), propertyNames,
						headerNames);
			}
		}
	}

	public Container getContainer() {
		return container;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public boolean isExportAllowed() {
		return exportAllowed;
	}

	/**
	 * Removes a generated column
	 *
	 * @param attributeModel
	 *            the attribute model for which to remove the column
	 */
	private void removeGeneratedColumn(final AttributeModel attributeModel) {
		if (attributeModel.isVisibleInTable() && attributeModel.isUrl()) {
			removeGeneratedColumn(attributeModel.getPath());
		}
	}

	/**
	 * Remove any generated columns - this is used when switching between modes in
	 * order to remove any generated columns containing URL fields
	 */
	public void removeGeneratedColumns() {
		removeGeneratedColumnsRecursive(entityModel.getAttributeModels());
	}

	private void removeGeneratedColumnsRecursive(List<AttributeModel> attributeModels) {
		for (AttributeModel attributeModel : attributeModels) {
			removeGeneratedColumn(attributeModel);
			if (attributeModel.getNestedEntityModel() != null) {
				removeGeneratedColumnsRecursive(attributeModel.getNestedEntityModel().getAttributeModels());
			}
		}
	}

	/**
	 * Sets the visibility of a column. This can only be used to show/hide columns
	 * that would show up in the table based on the entity model
	 *
	 * @param propertyId
	 *            the ID of the column.
	 * @param visible
	 *            whether the column must be visible
	 */
	public void setColumnVisible(Object propertyId, boolean visible) {
		Object[] visibleCols = getVisibleColumns();
		List<Object> temp = Arrays.stream(visibleCols).filter(c -> !c.equals(propertyId)).collect(Collectors.toList());
		boolean alreadyVisible = Arrays.stream(visibleCols).anyMatch(c -> c.equals(propertyId));

		// add column if not already visible
		if (!alreadyVisible || visible) {
			temp.add(propertyId);
		}
		setVisibleColumns(temp.toArray(new Object[0]));
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	/**
	 * Updates the table caption in response to a change of the data set
	 */
	public void updateTableCaption() {
		setCaption(entityModel.getDisplayNamePlural() + " " + messageService.getMessage("ocs.showing.results",
				VaadinUtils.getLocale(), getContainerDataSource().size()));
	}

	public boolean isUpdateTableCaption() {
		return updateTableCaption;
	}

	public void setUpdateTableCaption(boolean updateTableCaption) {
		this.updateTableCaption = updateTableCaption;
	}

}
