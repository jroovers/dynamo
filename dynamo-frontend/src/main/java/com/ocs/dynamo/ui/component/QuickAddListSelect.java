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
package com.ocs.dynamo.ui.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Sets;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;

/**
 * 
 * A ListSelect field that allows the quick addition of simple entities.
 * Supports multiple select use cases
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity that is being displayed
 * @param <T> the type of the entity that is being displayed
 */
public class QuickAddListSelect<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Collection<T>> implements Refreshable {

	private static final long serialVersionUID = 4246187881499965296L;

	/**
	 * The list select component
	 */
	private EntityListSelect<ID, T> listSelect;

	/**
	 * Whether quick adding is allowed
	 */
	private boolean quickAddAllowed;

	/**
	 * Constructor
	 * 
	 * @param entityModel
	 * @param attributeModel
	 * @param service
	 * @param filter
	 * @param multiSelect
	 * @param rows
	 * @param sortOrder
	 */
	@SafeVarargs
	public QuickAddListSelect(EntityModel<T> entityModel, AttributeModel attributeModel, BaseService<ID, T> service,
			SerializablePredicate<T> filter, boolean search, int rows, SortOrder<?>... sortOrder) {
		super(service, entityModel, attributeModel, filter);
		listSelect = new EntityListSelect<>(entityModel, attributeModel, service, filter, sortOrder);
		listSelect.setRows(rows);
		this.quickAddAllowed = !search && attributeModel != null && attributeModel.isQuickAddAllowed();
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<Collection<T>> listener) {
		return listSelect.addValueChangeListener(
				event -> listener.valueChange(new ValueChangeEvent<>(this, event.getOldValue(), false)));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		// add to the container
		ListDataProvider<T> provider = (ListDataProvider<T>) listSelect.getDataProvider();
		provider.getItems().add(entity);
		listSelect.select(entity);
	}

	@Override
	public void clearAdditionalFilter() {
		super.clearAdditionalFilter();
		if (listSelect != null) {
			listSelect.refresh(getFilter());
		}
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		if (listSelect != null) {
			if (value == null) {
				value = Collections.emptyList();
			}
			listSelect.setValue(Sets.newHashSet(value));
		}
	}

	@Override
	public void focus() {
		if (listSelect != null) {
			listSelect.focus();
		}
	}

	public EntityListSelect<ID, T> getListSelect() {
		return listSelect;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<T> getValue() {
		if (listSelect != null) {
			return (Collection<T>) convertToCorrectCollection(listSelect.getValue());
		}
		return null;
	}

	@Override
	protected Component initContent() {
		ResponsiveLayout layout = new ResponsiveLayout().withFullSize();
		ResponsiveRow bar = ResponsiveUtil.createRowWithSpacing();
		layout.addRow(bar);

		if (this.getAttributeModel() != null) {
			this.setCaption(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
		}

		// no caption needed (the wrapping component has the caption)
		listSelect.setCaption(null);
		listSelect.addValueChangeListener(event -> setValue(event.getValue()));
		listSelect.setSizeFull();

		int factor = DynamoConstants.MAX_COLUMNS - (quickAddAllowed ? BUTTON_COLS : 0);
		bar.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, factor, factor, factor).withComponent(listSelect);

		if (quickAddAllowed) {
			addQuickAddButton(bar, false);
		}

		return layout;
	}

	/**
	 * Refreshes the data in the list
	 */
	@Override
	public void refresh() {
		if (listSelect != null) {
			listSelect.refresh(getFilter());
		}
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		setFilter(filter);
		if (listSelect != null) {
			listSelect.refresh(filter);
		}
	}

	@Override
	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
		super.setAdditionalFilter(additionalFilter);
		if (listSelect != null) {
			listSelect.refresh(getFilter() == null ? additionalFilter : getFilter().and(additionalFilter));
		}
	}

	/**
	 * Delegate the component error message to the list select
	 */
	@Override
	public void setComponentError(ErrorMessage componentError) {
		super.setComponentError(componentError);
		if (listSelect != null) {
			listSelect.setComponentError(componentError);
		}
	}
}
