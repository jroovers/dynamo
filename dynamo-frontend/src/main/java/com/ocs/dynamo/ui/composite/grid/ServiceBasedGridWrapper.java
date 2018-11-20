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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;

/**
 * A wrapper for a grid that retrieves its data directly from the database
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key of the entity
 * @param <T> type of the entity
 */
public class ServiceBasedGridWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseGridWrapper<ID, T> implements Searchable<T> {

	private static final long serialVersionUID = -4691108261565306844L;

	/**
	 * The search filter that is applied to the table
	 */
	private SerializablePredicate<T> filter;

	/**
	 * The maximum number of results
	 */
	private Integer maxResults;

	/**
	 * @param service     the service object
	 * @param entityModel the entity model
	 * @param queryType   the query type to use
	 * @param order       the default sort order
	 * @param joins       options list of fetch joins to include in the query
	 */
	public ServiceBasedGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			SerializablePredicate<T> filter, List<SortOrder<?>> sortOrders, boolean allowExport, boolean editable,
			FetchJoinInformation... joins) {
		super(service, entityModel, queryType, sortOrders, allowExport, editable, joins);
		this.filter = filter;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected DataProvider<T, SerializablePredicate<T>> constructDataProvider() {
		DataProvider<T, SerializablePredicate<T>> provider;
		if (QueryType.PAGING.equals(getQueryType())) {
			provider = new PagingDataProvider<>(getService(), getEntityModel(), getJoins());
		} else {
			provider = new IdBasedDataProvider<>(getService(), getEntityModel(), getJoins());
		}
		((BaseDataProvider<ID, T>) provider).setMaxResults(maxResults);
		doConstructDataProvider(provider);
		return provider;
	}

	protected SerializablePredicate<T> getFilter() {
		return filter;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	@Override
	protected void initSortingAndFiltering() {
		super.initSortingAndFiltering();
		// sets the initial filter
		getGrid().getDataCommunicator().setDataProvider(getDataProvider(), filter);
	}

	@Override
	public void reloadDataProvider() {
		search(getFilter());
	}

	@Override
	public void search(SerializablePredicate<T> filter) {
		SerializablePredicate<T> temp = beforeSearchPerformed(filter);
		setDataProvider(constructDataProvider());
		getGrid().getDataCommunicator().setDataProvider(getDataProvider(), temp != null ? temp : filter);
	}

	/**
	 * Sets the provided filter as the component filter and then refreshes the
	 * container
	 * 
	 * @param filter
	 */
	public void setFilter(SerializablePredicate<T> filter) {
		this.filter = filter;
		search(filter);
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

}