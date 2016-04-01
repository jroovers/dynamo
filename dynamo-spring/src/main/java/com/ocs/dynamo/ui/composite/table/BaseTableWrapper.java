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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * A base class for wrapper objects for tables that are based on the entity
 * model
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class BaseTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCustomComponent {

	private static final long serialVersionUID = -4691108261565306844L;

	private Container container;

	private EntityModel<T> entityModel;

	private FetchJoinInformation[] joins;

	private final QueryType queryType;

	private final BaseService<ID, T> service;

	private SortOrder sortOrder;

	protected Table table;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param queryType
	 * @param sortOrder
	 * @param joins
	 */
	public BaseTableWrapper(BaseService<ID, T> service, EntityModel<T> entityModel,
			QueryType queryType, SortOrder sortOrder, FetchJoinInformation[] joins) {
		this.service = service;
		this.entityModel = entityModel;
		this.queryType = queryType;
		this.sortOrder = sortOrder;
		this.joins = joins;
	}

	/**
	 * Builds the component.
	 */
	@Override
	public void build() {
		VerticalLayout main = new VerticalLayout();

		this.container = constructContainer();

		table = getTable();
		table.setPageLength(DynamoConstants.PAGE_SIZE);

		initSortingAndFiltering();

		main.addComponent(table);

		// add a change listener that responds to the selection of an item
		table.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				onSelect(table.getValue());
			}
		});

		setCompositionRoot(main);
	}

	/**
	 * Creates the container that holds the relevant data
	 * 
	 * @return
	 */
	protected abstract Container constructContainer();

	public Container getContainer() {
		return container;
	}

	/**
	 * @return the entityModel
	 */
	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public BaseService<ID, T> getService() {
		return service;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public Table getTable() {
		if (table == null) {
			table = new ModelBasedTable<>(this.container, entityModel, getEntityModelFactory(),
					getMessageService());
		}
		return table;
	}

	/**
	 * Initializes the sorting and filtering for the table
	 */
	protected void initSortingAndFiltering() {
		if (getSortOrder() != null) {
			table.sort(new Object[] { getSortOrder().getPropertyId() },
					new boolean[] { SortDirection.ASCENDING == getSortOrder().getDirection() });
		}
	}

	/**
	 * Respond to a selection of an item in the table
	 */
	protected void onSelect(Object selected) {
		// override in subclass if needed
	}

	/**
	 * Reloads the data in the container
	 */
	public abstract void reloadContainer();

	public void setJoins(FetchJoinInformation[] joins) {
		this.joins = joins;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

}
