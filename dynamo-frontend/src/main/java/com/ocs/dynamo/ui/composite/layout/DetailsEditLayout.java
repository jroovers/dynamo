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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.component.ResponsiveUtil;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Layout;

/**
 * A layout for displaying various nested forms below each other
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID
 * @param <T> the type of the entity that is managed in the form
 */
public class DetailsEditLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends CustomField<Collection<T>>
        implements NestedComponent, UseInViewMode {

    /**
     * A container that holds the edit form for a single entity along with a button
     * bar
     * 
     * @author Bas Rutten
     *
     */
    class FormContainer extends ResponsiveLayout {

        private static final long serialVersionUID = 3507638736422806589L;

        /**
         * The actual edit form
         */
        private ModelBasedEditForm<ID, T> form;

        /**
         * Button for deleting the form
         */
        private Button removeButton;

        /**
         * Button bar
         */
        private ResponsiveRow buttonBar;

        /**
         * Constructor
         * 
         * @param form the model based edit form
         */
        FormContainer(ModelBasedEditForm<ID, T> form) {
            setSizeFull();
            this.form = form;
            ResponsiveUtil.addFullWidthRow(this, form);

            buttonBar = ResponsiveUtil.createButtonBar();
            ResponsiveUtil.addFullWidthRow(this, buttonBar);

            // remove button
            if (!viewMode && getFormOptions().isShowRemoveButton()) {
                removeButton = new Button(messageService.getMessage("ocs.remove", VaadinUtils.getLocale()));
                removeButton.setIcon(VaadinIcons.TRASH);
                removeButton.addClickListener(event -> {
                    removeEntityConsumer.accept(this.form.getEntity());
                    items.remove(this.form.getEntity());

                    ResponsiveRow parentRow = VaadinUtils.getParentOfClass(this, ResponsiveRow.class);
                    mainFormContainer.removeComponent(parentRow);
                    forms.remove(this);
                });
                buttonBar.addComponent(removeButton);
            }
            postProcessButtonBar(buttonBar);
        }

        public Button getDeleteButton() {
            return removeButton;
        }

        public T getEntity() {
            return form.getEntity();
        }

        public void postProcessButtonBar(ResponsiveRow buttonBar) {
            // overwrite in subclasses
        }

        public void setDeleteAllowed(boolean enabled) {
            if (removeButton != null) {
                removeButton.setEnabled(enabled);
            }
        }

        public void setDeleteVisible(boolean visible) {
            if (removeButton != null) {
                removeButton.setVisible(visible);
            }
        }

        public void setEntity(T t) {
            form.setEntity(t);
        }

        public void setFieldEnabled(String path, boolean enabled) {
            form.getFieldOptional(path).ifPresent(f -> f.setEnabled(enabled));
        }

        public void setFieldVisible(String path, boolean visible) {
            form.getFieldOptional(path).ifPresent(f -> f.setVisible(visible));
        }

        public boolean validateAllFields() {
            return form.validateAllFields();
        }

    }

    private static final long serialVersionUID = -1203245694503350276L;

    /**
     * The button that can be used to add forms
     */
    private Button addButton;

    /**
     * The entity model of the entity to display
     */
    private final EntityModel<T> entityModel;

    /**
     * The entity models used for rendering the individual fields (mostly useful for
     * lookup components)
     */
    private Map<String, String> attributeEntityModels = new HashMap<>();

    /**
     * The attribute model of the attribute to display
     */
    private final AttributeModel attributeModel;

    /**
     * The comparator (will be used to sort the items)
     */
    private Comparator<T> comparator;

    /**
     * Optional field filters for restricting the contents of combo boxes
     */
    private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

    /**
     * Form options that determine which buttons and functionalities are available
     */
    private FormOptions formOptions;

    /**
     * The list of items to display
     */
    private List<T> items;

    /**
     * The message service
     */
    private final MessageService messageService;

    /**
     * Whether the component is in view mode. If this is the case, editing is not
     * allowed and no buttons will be displayed
     */
    private boolean viewMode;

    /**
     * Service for interacting with the database
     */
    private BaseService<ID, T> service;

    /**
     * The individual edit forms
     */
    private List<FormContainer> forms = new ArrayList<>();

    /**
     * Container that holds all the sub forms
     */
    private ResponsiveLayout mainFormContainer;

    /**
     * Supplier for creating a new entity
     */
    private Supplier<T> createEntitySupplier;

    /**
     * Consumer for removing an entity
     */
    private Consumer<T> removeEntityConsumer;

    /**
     * Constructor
     * 
     * @param service        the service
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param viewMode       whether the form is in view mode
     * @param formOptions    the form options
     * @param comparator     the comparator for sorting the items
     */
    public DetailsEditLayout(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode,
            FormOptions formOptions, Comparator<T> comparator) {
        this.service = service;
        this.entityModel = entityModel;
        this.attributeModel = attributeModel;
        this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
        this.comparator = comparator;
        this.items = new ArrayList<>();
        this.viewMode = viewMode;
        this.formOptions = formOptions;
    }

    /**
     * Adds a detail edit form
     * 
     * @param t the entity to display/edit
     */
    private void addDetailEditForm(T t) {

        ModelBasedEditForm<ID, T> editForm = new ModelBasedEditForm<ID, T>(t, service, entityModel, formOptions, fieldFilters) {

            private static final long serialVersionUID = -7229109969816505927L;

            @Override
            protected void afterLayoutBuilt(Layout layout, boolean viewMode) {
                DetailsEditLayout.this.afterLayoutBuilt(this, viewMode);
            }

            @Override
            protected void afterModeChanged(boolean viewMode) {
                DetailsEditLayout.this.afterModeChanged(this, viewMode);
            }

            @Override
            protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode) {
                return DetailsEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode);
            }

            @Override
            protected void postProcessEditFields() {
                super.postProcessEditFields();
                DetailsEditLayout.this.postProcessEditFields(this);
            }
        };
        editForm.setFieldEntityModels(getFieldEntityModels());

        // use all available columns in nested form
        editForm.setColumnWidth(DynamoConstants.MAX_COLUMNS);
        editForm.setFieldFilters(fieldFilters);
        editForm.setNestedMode(true);
        editForm.setViewMode(viewMode);
        editForm.build();

        FormContainer fc = new FormContainer(editForm) {

            private static final long serialVersionUID = 6186428121967857827L;

            @Override
            public void postProcessButtonBar(ResponsiveRow buttonBar) {
                DetailsEditLayout.this.postProcessDetailButtonBar(forms.size(), buttonBar, viewMode);
            }
        };
        forms.add(fc);
        ResponsiveUtil.addFullWidthRow(mainFormContainer, fc);
    }

    /**
     * Adds an attribute entity model - this can be used to overwrite the default
     * entity model that is used for rendering complex selection components (lookup
     * dialogs)
     * 
     * @param path      the path to the field
     * @param reference the unique ID of the entity model
     */
    public final void addAttributeEntityModel(String path, String reference) {
        attributeEntityModels.put(path, reference);
    }

    protected void afterLayoutBuilt(ModelBasedEditForm<ID, T> editForm, boolean viewMode) {
        // override in subclasses
    }

    protected void afterModeChanged(ModelBasedEditForm<ID, T> editForm, boolean viewMode) {
        // override in subclasses
    }

    /**
     * Constructs the button that is used for adding new items
     * 
     * @param buttonBar the button bar
     */
    protected void constructAddButton(ResponsiveRow buttonBar) {
        addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
        addButton.setIcon(VaadinIcons.PLUS);
        addButton.addClickListener(event -> {
            T t = createEntitySupplier.get();
            items.add(t);
            addDetailEditForm(t);
        });

        addButton.setVisible(!viewMode && !formOptions.isHideAddButton());
        buttonBar.addComponent(addButton);

    }

    /**
     * Constructs the button bar
     * 
     * @param parent the layout to which to add the button bar
     */
    protected void constructButtonBar(ResponsiveLayout parent) {
        ResponsiveRow buttonBar = ResponsiveUtil.createButtonBar();

        buttonBar.setVisible(!viewMode);
        parent.addRow(buttonBar);

        constructAddButton(buttonBar);
        postProcessButtonBar(buttonBar);
    }

    /**
     * Method that is called to create a custom field. Override in subclasses if
     * needed
     * 
     * @param entityModel    the entity model of the entity that is displayed in the
     *                       component
     * @param attributeModel the attribute model of the attribute for which we are
     *                       constructing a field
     * @param viewMode       whether the form is in view mode
     * @return
     */
    protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode) {
        return null;
    }

    @Override
    protected void doSetValue(Collection<T> value) {
        setItems(value);
    }

    public Button getAddButton() {
        return addButton;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public Supplier<T> getCreateEntitySupplier() {
        return createEntitySupplier;
    }

    public T getEntity(int index) {
        if (index < this.forms.size()) {
            return this.forms.get(index).getEntity();
        }
        return null;
    }

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public Map<String, String> getFieldEntityModels() {
        return attributeEntityModels;
    }

    public Map<String, SerializablePredicate<?>> getFieldFilters() {
        return fieldFilters;
    }

    /**
     * Returns the FormContainer specified by the index
     * 
     * @param index the zero-based index of the form container
     * @return
     */
    public FormContainer getFormContainer(int index) {
        if (index < this.forms.size()) {
            return forms.get(index);
        }
        return null;
    }

    /**
     * Returns the current number of forms
     * 
     * @return
     */
    public Integer getFormCount() {
        return forms.size();
    }

    public FormOptions getFormOptions() {
        return formOptions;
    }

    public Consumer<T> getRemoveEntityConsumer() {
        return removeEntityConsumer;
    }

    @Override
    public Collection<T> getValue() {
        // does not actually have to return anything
        return null;
    }

    /**
     * Constructs the actual component
     */
    @Override
    protected Component initContent() {

        ResponsiveLayout layout = new ResponsiveLayout().withFullSize();

        setCaption(attributeModel.getDisplayName(VaadinUtils.getLocale()));

        mainFormContainer = new ResponsiveLayout().withFullSize();
        ResponsiveUtil.addFullWidthRow(layout, mainFormContainer);

        // add the buttons
        constructButtonBar(layout);

        // initial filling
        setItems(items);

        return layout;
    }

    public boolean isViewMode() {
        return viewMode;
    }

    /**
     * Callback method that is used to modify the main button bar that appears below
     * the sub-forms. Override in subclasses if needed
     * 
     * @param buttonBar the button bar
     */
    protected void postProcessButtonBar(ResponsiveRow buttonBar) {
        // overwrite in subclass if needed
    }

    /**
     * Callback method that is used to modify the detail button bar that is rendered
     * for every sub-form
     * 
     * @param index     the zero-based index of the sub-form
     * @param buttonBar the button bar
     * @param viewMode  whether the component is in view mode
     */
    protected void postProcessDetailButtonBar(int index, ResponsiveRow buttonBar, boolean viewMode) {
        // overwrite in subclass if needed
    }

    /**
     * Callback method that is used to modify the fields after creation. This method
     * is called just once during component construction
     * 
     * @param editForm the edit form that contains the fields
     */
    protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
        // override in subclasses
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * Sets the supplier used for creating a new entity
     * 
     * @param createEntitySupplier the supplier
     */
    public void setCreateEntitySupplier(Supplier<T> createEntitySupplier) {
        this.createEntitySupplier = createEntitySupplier;
    }

    /**
     * Enables or disables the delete button for a sub-form
     * 
     * @param index   the zero-based index of the sub-form
     * @param allowed whether deleting is allowed
     */
    public void setDeleteEnabled(int index, boolean allowed) {
        if (index < this.forms.size()) {
            this.forms.get(index).setDeleteAllowed(allowed);
        }
    }

    /**
     * Sets the visibility of the delete button for a form
     * 
     * @param index   the zero-based index of the form
     * @param visible the desired visibility
     */
    public void setDeleteVisible(int index, boolean visible) {
        if (index < this.forms.size()) {
            this.forms.get(index).setDeleteVisible(visible);
        }
    }

    /**
     * Sets the entity for a certain form to the provided entity
     * 
     * @param index  the zero-based index of the form
     * @param entity the entity to set
     */
    public void setEntity(int index, T entity) {
        if (index < this.forms.size()) {
            this.forms.get(index).setEntity(entity);
        }
    }

    /**
     * Enables or disables a field inside a form
     * 
     * @param index   the zero-based index of the form
     * @param path    the path to the attribute
     * @param enabled whether to enable the field
     */
    public void setFieldEnabled(int index, String path, boolean enabled) {
        if (index < this.forms.size()) {
            this.forms.get(index).setFieldEnabled(path, enabled);
        }
    }

    public void setFieldEntityModels(Map<String, String> fieldEntityModels) {
        this.attributeEntityModels = fieldEntityModels;
    }

    public void setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
        this.fieldFilters = fieldFilters;
    }

    /**
     * Sets the visibility of the specified field in a specified sub-form
     * 
     * @param index   the zero-based index of the sub-form
     * @param path    the path to the field
     * @param visible the desired visibility
     */
    public void setFieldVisible(int index, String path, boolean visible) {
        if (index < this.forms.size()) {
            this.forms.get(index).setFieldVisible(path, visible);
        }
    }

    public void setFormOptions(FormOptions formOptions) {
        this.formOptions = formOptions;
    }

    /**
     * Sets the items that are specified in the layout
     * 
     * @param items the new set of items to be displayed
     */
    public void setItems(Collection<T> items) {

        List<T> list = new ArrayList<>();
        list.addAll(items);
        if (comparator != null) {
            list.sort(comparator);
        }

        this.items = list;

        if (mainFormContainer != null) {
            mainFormContainer.removeAllComponents();
            forms.clear();
            for (T t : items) {
                addDetailEditForm(t);
            }
        }
    }

    /**
     * Sets the Consumer to be carried out for decoupling/removing an entity
     * 
     * @param removeEntityConsumer
     */
    public void setRemoveEntityConsumer(Consumer<T> removeEntityConsumer) {
        this.removeEntityConsumer = removeEntityConsumer;
    }

    public void setService(BaseService<ID, T> service) {
        this.service = service;
    }

    /**
     * Validates all underlying forms
     */
    public boolean validateAllFields() {
        boolean error = false;
        for (FormContainer f : forms) {
            error |= f.validateAllFields();
        }
        return error;
    }
}
