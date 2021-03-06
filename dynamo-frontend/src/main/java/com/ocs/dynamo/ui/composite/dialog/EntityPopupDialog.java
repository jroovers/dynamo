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
package com.ocs.dynamo.ui.composite.dialog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A pop-up dialog for adding a new entity or viewing the details of an existing
 * entry
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 */
public class EntityPopupDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

    private static final long serialVersionUID = -2012972894321597214L;

    /**
     * The entity to add/modify
     */
    private T entity;

    /**
     * The entity model
     */
    private EntityModel<T> entityModel;

    private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

    /**
     * The form options
     */
    private FormOptions formOptions;

    /**
     * The layout used to create/modify the entity
     */
    private SimpleEditLayout<ID, T> layout;

    private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

    /**
     * The OK button used to close the dialog in read-only mode
     */
    private Button okButton;

    /**
     * The service used to query the database
     */
    private BaseService<ID, T> service;

    /**
     * Constructor
     * 
     * @param service
     * @param entity
     * @param entityModel
     * @param formOptions
     */
    public EntityPopupDialog(BaseService<ID, T> service, T entity, EntityModel<T> entityModel,
            Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions) {
        this.service = service;
        this.entityModel = entityModel;
        this.formOptions = formOptions;
        this.entity = entity;
        this.fieldFilters = fieldFilters;
    }

    /**
     * Callback method that is called after the user is done editing the entry
     * 
     * @param cancel    whether the edit action was cancelled
     * @param newEntity whether the user was adding a new entity
     * @param entity    the entity that was being edited
     */
    public void afterEditDone(boolean cancel, boolean newEntity, T entity) {
        // override in subclasses
    }

    /**
     * Creates a new entity
     * 
     * @return
     */
    protected T createEntity() {
        return service.createNewEntity();
    }

    @Override
    protected void doBuild(VerticalLayout parent) {

        // cancel button makes no sense in a popup
        formOptions.setHideCancelButton(false);

        layout = new SimpleEditLayout<ID, T>(entity, service, entityModel, formOptions) {

            private static final long serialVersionUID = -2965981316297118264L;

            @Override
            protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
                super.afterEditDone(cancel, newEntity, entity);
                EntityPopupDialog.this.close();
                EntityPopupDialog.this.afterEditDone(cancel, newEntity, entity);
            }

            @Override
            protected T createEntity() {
                return EntityPopupDialog.this.createEntity();
            }

            @Override
            protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
                EntityPopupDialog.this.postProcessButtonBar(buttonBar, viewMode);
            }

            @Override
            protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
                EntityPopupDialog.this.postProcessEditFields(editForm);
            }

        };
        layout.setFieldFilters(fieldFilters);
        parent.add(layout);
    }

    @Override
    protected void doBuildButtonBar(HorizontalLayout buttonBar) {
        // in read-only mode, display only an "OK" button that closes the dialog
        buttonBar.setVisible(formOptions.isReadOnly());
        if (formOptions.isReadOnly()) {
            okButton = new Button(messageService.getMessage("ocs.ok", VaadinUtils.getLocale()));
            okButton.addClickListener(event -> close());
            buttonBar.add(okButton);
        }
    }

    public T getEntity() {
        return layout.getEntity();
    }

    public SimpleEditLayout<ID, T> getLayout() {
        return layout;
    }

    public Button getOkButton() {
        return okButton;
    }

    public List<Button> getSaveButtons() {
        return layout.getEditForm().getSaveButtons();
    }

    @Override
    protected String getTitle() {
        return entityModel.getDisplayName(VaadinUtils.getLocale());
    }

    protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
        // overwrite in subclasses when needed
    }

    protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
        // overwrite in subclasses when needed
    }

}
