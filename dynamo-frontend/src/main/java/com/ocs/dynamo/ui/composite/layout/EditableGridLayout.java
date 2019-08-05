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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.dialog.EntityPopupDialog;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.HasValue;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.Editor;

/**
 * A layout for editing entities directly inside a grid. This layout supports
 * both a "row by row" and an "all rows at once" setting which can be specified
 * on the FormOptions by setting the GridEditMode.
 * 
 * For creating new entities a popup is used
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 */
@SuppressWarnings("serial")
public class EditableGridLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCollectionLayout<ID, T> {

    private static final long serialVersionUID = 4606800218149558500L;

    /**
     * The default page length (number of rows in the grid)
     */
    private static final int PAGE_LENGTH = 12;

    /**
     * The add button
     */
    private Button addButton;

    /**
     * Button for canceling edit mode
     */
    private Button cancelButton;

    /**
     * The button for changing to edit mode
     */
    private Button editButton;

    /**
     * Button for saving changes (in "edit all at once" mode)
     */
    private Button saveButton;

    /**
     * The filter that is applied to limit the search results
     */
    protected SerializablePredicate<T> filter;

    /**
     * The main layout
     */
    private VerticalLayout mainLayout;

    /**
     * The page length (number of visible rows)
     */
    private int pageLength = PAGE_LENGTH;

    /**
     * The icon to use inside the remove button
     */
    private Resource removeIcon = VaadinIcons.TRASH;

    /**
     * The message to display inside the "remove" button
     */
    private String removeMessage;

    /**
     * Whether the screen is in view mode
     */
    private boolean viewmode;

    /**
     * Mapping from entity to associated binder
     */
    private Map<T, Binder<T>> binders = new HashMap<>();

    /**
     * Supplier for creating the filter
     */
    private Supplier<SerializablePredicate<T>> filterSupplier;

    /**
     * The currently used wrapper component
     */
    private BaseGridWrapper<ID, T> currentWrapper;

    /**
     * Constructor
     * 
     * @param service     the service
     * @param entityModel the entity model to base the grid on
     * @param formOptions
     * @param sortOrder
     * @param joins
     */
    public EditableGridLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions, SortOrder<?> sortOrder,
            FetchJoinInformation... joins) {
        super(service, entityModel, formOptions, sortOrder, joins);
    }

    @Override
    public void attach() {
        super.attach();
        build();
    }

    @Override
    public void build() {
        buildFilter();
        if (mainLayout == null) {
            setViewmode(!isEditAllowed() || getFormOptions().isOpenInViewMode());
            mainLayout = new DefaultVerticalLayout(true, true);

            constructGrid();

            mainLayout.addComponent(getButtonBar());

            addButton = new Button(message("ocs.add"));
            addButton.setIcon(VaadinIcons.PLUS);
            addButton.addClickListener(event -> {
                // create new entry by means of pop-up dialog
                EntityPopupDialog<ID, T> dialog = new EntityPopupDialog<ID, T>(getService(), null, getEntityModel(), getFieldFilters(),
                        new FormOptions()) {

                    @Override
                    public void afterEditDone(boolean cancel, boolean newEntity, T entity) {
                        // reload so that the newly created entity shows up
                        reload();
                    }

                    @Override
                    protected T createEntity() {
                        return EditableGridLayout.this.createEntity();
                    }

                };
                dialog.build();
                UI.getCurrent().addWindow(dialog);
            });
            getButtonBar().addComponent(addButton);
            addButton.setVisible(!getFormOptions().isHideAddButton() && isEditAllowed() && !isViewmode());

            // button for switching to edit mode
            editButton = new Button(message("ocs.edit"));
            editButton.setIcon(VaadinIcons.EDIT);
            editButton.addClickListener(event -> toggleViewMode(false));
            editButton.setVisible(getFormOptions().isEditAllowed() && isEditAllowed() && isViewmode());
            getButtonBar().addComponent(editButton);

            // button for canceling edit mode
            cancelButton = new Button(message("ocs.cancel"));
            cancelButton.setIcon(VaadinIcons.BAN);
            cancelButton.addClickListener(event -> {
                // cancel the editor if it is open
                if (getGridWrapper().getGrid().getEditor().isOpen()) {
                    getGridWrapper().getGrid().getEditor().cancel();
                }
                toggleViewMode(true);
            });
            cancelButton.setVisible(isEditAllowed() && !isViewmode() && getFormOptions().isOpenInViewMode());
            getButtonBar().addComponent(cancelButton);

            // button for saving changes
            saveButton = new Button(message("ocs.save"));
            saveButton.addClickListener(event -> {

                // perform validation
                List<T> toSave = Lists.newArrayList(binders.keySet());
                boolean valid = binders.values().stream().map(b -> b.validate()).allMatch(s -> s.isOk());
                if (valid) {
                    if (getFormOptions().isConfirmSave()) {
                        // ask for confirmation before saving
                        VaadinUtils.showConfirmDialog(getMessageService(), getMessageService().getMessage("ocs.confirm.save.all",
                                VaadinUtils.getLocale(), getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale())), () -> {
                                    try {
                                        getService().save(toSave);
                                        // save and recreate grid to avoid optimistic locks
                                        binders.clear();
                                        clearGridWrapper();
                                        constructGrid();
                                    } catch (RuntimeException ex) {
                                        handleSaveException(ex);
                                    }
                                });
                    } else {
                        // do not ask for confirmation before saving
                        try {
                            getService().save(toSave);
                            // save and reassign to avoid optimistic locks
                            binders.clear();
                            clearGridWrapper();
                            constructGrid();
                        } catch (RuntimeException ex) {
                            handleSaveException(ex);
                        }
                    }
                }
            });
            saveButton.setVisible(isEditAllowed() && !isViewmode() && GridEditMode.SIMULTANEOUS.equals(getFormOptions().getGridEditMode()));
            getButtonBar().addComponent(saveButton);

            postProcessButtonBar(getButtonBar());
            constructGridDividers();
            postProcessLayout(mainLayout);
        }
        setCompositionRoot(mainLayout);
    }

    /**
     * Constructs the filter that limits which records show up
     */
    protected void buildFilter() {
        this.filter = filterSupplier == null ? null : filterSupplier.get();
    }

    /**
     * Initializes the grid
     */
    protected void constructGrid() {
        BaseGridWrapper<ID, T> wrapper = getGridWrapper();
        // make sure the grid can be edited
        Editor<T> editor = wrapper.getGrid().getEditor();
        editor.setEnabled(!isViewmode());
        editor.addSaveListener(event -> {
            try {
                T t = getService().save((T) event.getBean());
                // reassign to avoid optimistic lock
                wrapper.getGrid().getEditor().getBinder().setBean(t);
                wrapper.getGrid().getDataProvider().refreshAll();
            } catch (OCSValidationException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });
        // make sure changes are not persisted right away
        editor.setBuffered(true);
        wrapper.getGrid().setSelectionMode(Grid.SelectionMode.SINGLE);
        wrapper.getGrid().setHeightByRows(getPageLength());
        wrapper.getGrid().addSelectionListener(event -> setSelectedItems(event.getAllSelectedItems()));

        if (currentWrapper == null) {
            mainLayout.addComponent(wrapper);
        } else {
            mainLayout.replaceComponent(currentWrapper, wrapper);
        }

        // remove button at the end of the row
        if (getFormOptions().isShowRemoveButton() && isEditAllowed() && !isViewmode()) {
            String defaultMsg = message("ocs.remove");
            Column<T, Component> removeColumn = getGridWrapper().getGrid().addComponentColumn(
                    (ValueProvider<T, Component>) t -> isViewmode() ? null : new RemoveButton(removeMessage, removeIcon) {
                        @Override
                        protected void doDelete() {
                            binders.remove(t);
                            doRemove(t);
                        }

                        @Override
                        protected String getItemToDelete() {
                            return FormatUtils.formatEntity(getEntityModel(), t);
                        }
                    });
            removeColumn.setCaption(defaultMsg).setId("remove");
        }
        currentWrapper = wrapper;
    }

    @Override
    protected BaseGridWrapper<ID, T> constructGridWrapper() {
        ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<ID, T>(getService(), getEntityModel(), QueryType.ID_BASED,
                getFormOptions(), filter, getFieldFilters(), getSortOrders(), !viewmode, getJoins()) {

            @Override
            protected BindingBuilder<T, ?> doBind(T t, AbstractComponent field) {
                if (!binders.containsKey(t)) {
                    binders.put(t, new BeanValidationBinder<>(getEntityModel().getEntityClass()));
                    binders.get(t).setBean(t);
                }
                Binder<T> binder = binders.get(t);
                return binder.forField((HasValue<?>) field);
            }

            @Override
            protected void onSelect(Object selected) {
                setSelectedItems(selected);
                checkButtonState(getSelectedItem());
            }

            @Override
            protected void postProcessDataProvider(final DataProvider<T, SerializablePredicate<T>> provider) {
                EditableGridLayout.this.postProcessDataProvider(provider);
            }

        };
        postConfigureGridWrapper(wrapper);
        wrapper.setMaxResults(getMaxResults());
        wrapper.build();
        return wrapper;
    }

    @Override
    protected void detailsMode(T entity) {
        // not needed
    }

    /**
     * Method that is called to remove an item
     */
    protected void doRemove(T t) {
        getService().delete(t);
        getGridWrapper().reloadDataProvider();
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
        return getGridWrapper().getDataProvider();
    }

    public Button getEditButton() {
        return editButton;
    }

    public Supplier<SerializablePredicate<T>> getFilterSupplier() {
        return filterSupplier;
    }

    @Override
    public ServiceBasedGridWrapper<ID, T> getGridWrapper() {
        return (ServiceBasedGridWrapper<ID, T>) super.getGridWrapper();
    }

    @Override
    public int getPageLength() {
        return pageLength;
    }

    public Resource getRemoveIcon() {
        return removeIcon;
    }

    public String getRemoveMessage() {
        return removeMessage;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public boolean isViewmode() {
        return viewmode;
    }

    @Override
    public void refresh() {
        // override in subclasses
    }

    @Override
    public void reload() {
        buildFilter();
        getGridWrapper().setFilter(filter);
    }

    public void setFilterSupplier(Supplier<SerializablePredicate<T>> filterSupplier) {
        this.filterSupplier = filterSupplier;
    }

    @Override
    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
    }

    public void setRemoveIcon(Resource removeIcon) {
        this.removeIcon = removeIcon;
    }

    public void setRemoveMessage(String removeMessage) {
        this.removeMessage = removeMessage;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedItems(Object selection) {
        if (selection != null) {
            if (selection instanceof Collection<?>) {
                // the lazy query container returns an array of IDs of the
                // selected items
                Collection<?> col = (Collection<?>) selection;
                if (!col.isEmpty()) {
                    T t = (T) col.iterator().next();
                    setSelectedItem(t);
                } else {
                    setSelectedItem(null);
                }
            } else {
                T t = (T) selection;
                setSelectedItem(t);
            }
        } else {
            setSelectedItem(null);
        }
    }

    protected void setViewmode(boolean viewmode) {
        this.viewmode = viewmode;
    }

    /**
     * Sets the view mode of the screen, and adapts the grid and all buttons
     * accordingly
     *
     * @param viewMode the new desired value for the view mode
     */
    protected void toggleViewMode(boolean viewMode) {
        setViewmode(viewMode);

        // replace the current grid
        clearGridWrapper();
        binders.clear();
        constructGrid();

        // check the button statuses
        getGridWrapper().getGrid().getEditor().setEnabled(!isViewmode() && isEditAllowed());
        saveButton.setVisible(GridEditMode.SIMULTANEOUS.equals(getFormOptions().getGridEditMode()) && !isViewmode() && isEditAllowed());
        editButton.setVisible(isViewmode() && getFormOptions().isEditAllowed() && isEditAllowed());
        cancelButton.setVisible(!isViewmode() && isEditAllowed() && getFormOptions().isOpenInViewMode());
        addButton.setVisible(!isViewmode() && !getFormOptions().isHideAddButton() && isEditAllowed());
    }
}
