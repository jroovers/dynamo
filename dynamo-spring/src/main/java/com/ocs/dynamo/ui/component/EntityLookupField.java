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

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.util.EntityModelUtil;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * A composite component that displays a selected entity and offers a search dialog to search for
 * another one
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class EntityLookupField<ID extends Serializable, T extends AbstractEntity<ID>> extends
        QuickAddEntityField<ID, T, T> {

    private static final long serialVersionUID = 5377765863515463622L;

    /**
     * The button used to clear the current selection
     */
    private Button clearButton;

    /**
     * The filters to apply to the search dialog
     */
    private List<Filter> filters;

    /**
     * The joins to apply to the search in the search dialog
     */
    private final FetchJoinInformation[] joins;

    /**
     * The label that displays the currently selected item
     */
    private Label label;

    /**
     * The page length of the table in the search dialog
     */
    private Integer pageLength;

    /**
     * The button that brings up the search dialog
     */
    private Button selectButton;

    /**
     * The sort order to apply to the search dialog
     */
    private SortOrder sortOrder;

    /**
     * Indicates whether it is allowed to add items
     */
    private boolean addAllowed;

    /**
     * Constructor
     * 
     * @param service
     *            the service used to query the database
     * @param entityModel
     *            the entity model
     * @param attributeModel
     *            the attribute mode
     * @param filters
     *            the filter to apply when searching
     * @param search
     *            whether the component is used in a search screen
     * @param sortOrder
     *            the sort order
     * @param joins
     *            the joins to use when fetching data when filling the popop dialog
     */
    public EntityLookupField(BaseService<ID, T> service, EntityModel<T> entityModel,
            AttributeModel attributeModel, List<Filter> filters, boolean search,
            SortOrder sortOrder, FetchJoinInformation... joins) {
        super(service, entityModel, attributeModel);
        this.sortOrder = sortOrder;
        this.filters = filters;
        this.joins = joins;
        this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
    }

    public Button getClearButton() {
        return clearButton;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public Integer getPageLength() {
        return pageLength;
    }

    public Button getSelectButton() {
        return selectButton;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public Class<? extends T> getType() {
        return getEntityModel().getEntityClass();
    }

    @Override
    protected Component initContent() {
        HorizontalLayout bar = new DefaultHorizontalLayout(false, true, true);

        if (this.getAttributeModel() != null) {
            this.setCaption(getAttributeModel().getDisplayName());
        }

        label = new Label();
        updateLabel(getValue());
        bar.addComponent(label);

        // button for selecting an entity - brings up the search dialog
        selectButton = new Button(getMessageService().getMessage("ocs.select"));
        selectButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 8377632639548698729L;

            @Override
            public void buttonClick(ClickEvent event) {
                ModelBasedSearchDialog<ID, T> dialog = new ModelBasedSearchDialog<ID, T>(
                        getService(), getEntityModel(), filters, sortOrder, false, joins) {

                    private static final long serialVersionUID = -3432107069929941520L;

                    @Override
                    protected boolean doClose() {
                        setValue(getSelectedItem());
                        return true;
                    }
                };
                dialog.setPageLength(pageLength);
                dialog.build();
                UI.getCurrent().addWindow(dialog);
            }
        });
        bar.addComponent(selectButton);

        // button for clearing the current selection
        clearButton = new Button(getMessageService().getMessage("ocs.clear"));
        clearButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 8377632639548698729L;

            @Override
            public void buttonClick(ClickEvent event) {
                setValue(null);
            }
        });
        bar.addComponent(clearButton);

        // quick add button
        if (addAllowed) {
            Button addButton = constructAddButton();
            bar.addComponent(addButton);
        }

        return bar;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (selectButton != null) {
            selectButton.setEnabled(enabled);
            clearButton.setEnabled(enabled);
        }
    }

    @Override
    protected void setInternalValue(T newValue) {
        super.setInternalValue(newValue);
        updateLabel(newValue);
    }

    public void setPageLength(Integer pageLength) {
        this.pageLength = pageLength;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public void setValue(T newFieldValue) {
        super.setValue(newFieldValue);
        updateLabel(newFieldValue);
    }

    /**
     * Updates the value that is displayed in the label
     * 
     * @param newValue
     *            the new value
     */
    private void updateLabel(T newValue) {
        if (label != null) {
            label.setValue(newValue == null ? getMessageService()
                    .getMessage("ocs.no.item.selected") : EntityModelUtil.getDisplayPropertyValue(
                    newValue, getEntityModel()));
        }
    }

    @Override
    protected void afterNewEntityAdded(T entity) {
        setValue(entity);
    }

}
