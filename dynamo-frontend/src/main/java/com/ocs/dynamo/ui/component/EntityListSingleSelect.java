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
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.SortUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Custom ListSelect component for selecting a single item from a list
 *
 * @param <ID> type of the primary key of the entity
 * @param <T> type of the entity
 */
public class EntityListSingleSelect<ID extends Serializable, T extends AbstractEntity<ID>> extends ListBox<T>
        implements Refreshable, Cascadable<T> {

    public enum SelectMode {
        ALL, FILTERED, FIXED;
    }

    private static final long serialVersionUID = 3041574615271340579L;

    /**
     * The attribute model that governs how to build the component
     */
    private final AttributeModel attributeModel;

    /**
     * The select mode (filtered, all, or fixed)
     */
    private SelectMode selectMode = SelectMode.FILTERED;

    /**
     * The sort orders
     */
    private final SortOrder<?>[] sortOrders;

    /**
     * The service for querying the database
     */
    private BaseService<ID, T> service;

    /**
     * The search filter to use in filtered mode
     */
    private SerializablePredicate<T> filter;

    /**
     * The original search filter
     */
    private SerializablePredicate<T> originalFilter;

    /**
     * The addition search filter for cascading
     */
    private SerializablePredicate<T> additionalFilter;

    /**
     * The entity model of the entities being displayed
     */
    private EntityModel<T> targetEntityModel;

    /**
     * Constructor
     *
     * @param targetEntityModel
     * @param attributeModel
     * @param service
     * @param mode
     * @param filter
     * @param items
     * @param itemCaptionPropertyId
     * @param sortOrder
     */
    @SafeVarargs
    public EntityListSingleSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
            SelectMode mode, SerializablePredicate<T> filter, ListDataProvider<T> sharedProvider, List<T> items,
            SortOrder<?>... sortOrders) {
        this.targetEntityModel = targetEntityModel;
        this.service = service;
        this.selectMode = mode;
        this.sortOrders = sortOrders;
        this.attributeModel = attributeModel;
        this.filter = filter;
        setHeight("100px");

        ListDataProvider<T> provider = sharedProvider;
        if (provider == null) {
            if (SelectMode.ALL.equals(mode)) {
                // add all items (but sorted)
                provider = new ListDataProvider<>(service.findAll(SortUtils.translateSortOrders(sortOrders)));
            } else if (SelectMode.FILTERED.equals(mode)) {
                // add a filtered selection of items
                items = service.find(new FilterConverter<T>(targetEntityModel).convert(filter), SortUtils.translateSortOrders(sortOrders));
                provider = new ListDataProvider<>(items);
            } else if (SelectMode.FIXED.equals(mode)) {
                provider = new ListDataProvider<>(items);
            }
        }
        setDataProvider(provider);

        // non-standard way of setting captions
        setRenderer(new ComponentRenderer<Text, T>(t -> {
            String label = EntityModelUtils.getDisplayPropertyValue(t, targetEntityModel);
            return new Text(label == null ? "" : label);
        }));
    }

    /**
     * Constructor - for the "FILTERED" mode
     *
     * @param targetEntityModel the entity model of the entities that are to be
     *                          displayed
     * @param attributeModel    the attribute model for the property that is bound
     *                          to this component
     * @param service           the service used to retrieve the entities
     * @param filter            the filter used to filter the entities
     * @param sortOrder         the sort order used to sort the entities
     */
    @SafeVarargs
    public EntityListSingleSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
            SerializablePredicate<T> filter, ListDataProvider<T> sharedProvider, SortOrder<?>... sortOrder) {
        this(targetEntityModel, attributeModel, service, SelectMode.FILTERED, filter, sharedProvider, null, sortOrder);
    }

    /**
     * Constructor - for the "ALL" mode
     *
     * @param targetEntityModel the entity model of the entities that are to be
     *                          displayed
     * @param attributeModel    the attribute model for the property that is bound
     *                          to this component
     * @param service           the service used to retrieve entities
     */
    @SafeVarargs
    public EntityListSingleSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel, BaseService<ID, T> service,
            SortOrder<?>... sortOrder) {
        this(targetEntityModel, attributeModel, service, SelectMode.ALL, null, null, null, sortOrder);
    }

    /**
     * Constructor - for the "FIXED" mode
     *
     * @param targetEntityModel the entity model of the entities that are to be
     *                          displayed
     * @param attributeModel    the attribute model for the property that is bound
     *                          to this component
     * @param items             the list of entities to display
     */
    public EntityListSingleSelect(EntityModel<T> targetEntityModel, AttributeModel attributeModel, List<T> items) {
        this(targetEntityModel, attributeModel, null, SelectMode.FIXED, null, null, items);
    }

    @Override
    public void clearAdditionalFilter() {
        this.additionalFilter = filter;
        this.filter = originalFilter;
        refresh();
    }

    @Override
    public SerializablePredicate<T> getAdditionalFilter() {
        return additionalFilter;
    }

    public AttributeModel getAttributeModel() {
        return attributeModel;
    }

    @SuppressWarnings("unchecked")
    public int getDataProviderSize() {
        ListDataProvider<T> bic = (ListDataProvider<T>) this.getDataProvider();
        return bic.getItems().size();
    }

    public SerializablePredicate<T> getFilter() {
        return filter;
    }

    public SelectMode getSelectMode() {
        return selectMode;
    }

    public SortOrder<?>[] getSortOrders() {
        return sortOrders;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void refresh() {
        ListDataProvider<T> provider = (ListDataProvider<T>) getDataProvider();
        if (SelectMode.ALL.equals(selectMode)) {
            // add all items (but sorted)
            provider.getItems().clear();
            provider.getItems().addAll(service.findAll(SortUtils.translateSortOrders(sortOrders)));
        } else if (SelectMode.FILTERED.equals(selectMode)) {
            // add a filtered selection of items
            provider.getItems().clear();
            com.ocs.dynamo.dao.SortOrder[] orders = SortUtils.translateSortOrders(sortOrders);
            if (orders != null) {
                List<T> list = service.find(new FilterConverter<T>(targetEntityModel).convert(filter), orders);
                provider.getItems().addAll(list);
            } else {
                List<T> list = service.find(new FilterConverter<T>(targetEntityModel).convert(filter));
                provider.getItems().addAll(list);
            }

        }
    }

    public void refresh(SerializablePredicate<T> filter) {
        this.originalFilter = filter;
        this.filter = filter;
        refresh();
    }

    @Override
    public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
        setValue(null);
        this.additionalFilter = additionalFilter;
        this.filter = originalFilter == null ? additionalFilter : new AndPredicate<>(originalFilter, additionalFilter);
        refresh();
    }

}
