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
package com.ocs.dynamo.ui.composite.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.domain.model.util.EntityModelUtil;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultEmbedded;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

/**
 * An edit form that is constructed based on an entity model
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
@SuppressWarnings("serial")
public class ModelBasedEditForm<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseCustomComponent {

    /**
     * A custom field that can be used to upload a file
     * 
     * @author bas.rutten
     *
     */
    private final class UploadComponent extends CustomField<byte[]> {

        private AttributeModel attributeModel;

        private UploadComponent(AttributeModel attributeModel) {
            this.attributeModel = attributeModel;
        }

        @Override
        protected Component initContent() {
            final byte[] bytes = ClassUtils.getBytes(entity, attributeModel.getName());
            final Embedded image = new DefaultEmbedded(null, bytes);

            VerticalLayout main = new DefaultVerticalLayout(false, true);

            // for a LOB field, create an upload and an image
            // retrieve the current value
            if (attributeModel.isImage()) {
                image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
                image.setVisible(bytes != null);
                main.addComponent(image);
            } else {
                Label label = new Label(message("ocs.no.preview.available"));
                main.addComponent(label);
            }

            // callback object to handle successful upload
            UploadReceiver receiver = new UploadReceiver(image, attributeModel.getName(),
                    attributeModel.getAllowedExtensions().toArray(new String[0]));

            HorizontalLayout buttons = new DefaultHorizontalLayout(false, true);
            main.addComponent(buttons);

            Upload upload = new Upload(null, receiver);
            upload.addSucceededListener(receiver);
            buttons.addComponent(upload);

            // a button used to clear the image
            Button clearButton = new Button(message("ocs.clear"));
            clearButton.addClickListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    ClassUtils.clearFieldValue(getEntity(), attributeModel.getName(), byte[].class);
                    image.setVisible(false);
                }
            });
            buttons.addComponent(clearButton);

            setCaption(attributeModel.getDisplayName());

            return main;
        }

        @Override
        public Class<? extends byte[]> getType() {
            return byte[].class;
        }

    }

    /**
     * Callback object for handling a file upload
     * 
     * @author bas.rutten
     */
    private final class UploadReceiver implements SucceededListener, Receiver {

        // the name of the field that must be updated
        private String fieldName;

        private ByteArrayOutputStream stream;

        private String[] supportedExtensions;

        // the target component that must be updated after an upload
        private Embedded target;

        private UploadReceiver(Embedded target, String fieldName, String... supportedExtensions) {
            this.target = target;
            this.fieldName = fieldName;
            this.supportedExtensions = supportedExtensions;
        }

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            stream = new ByteArrayOutputStream();
            return stream;
        }

        @Override
        public void uploadSucceeded(SucceededEvent event) {
            if (stream != null && stream.toByteArray().length > 0) {
                String extension = FilenameUtils.getExtension(event.getFilename());

                if (supportedExtensions == null
                        || supportedExtensions.length == 0
                        || (extension != null && Arrays.asList(supportedExtensions).contains(
                                extension))) {

                    // set the image source
                    if (target != null) {
                        target.setVisible(true);

                        StreamResource.StreamSource ss = new StreamResource.StreamSource() {

                            @Override
                            public InputStream getStream() {
                                return new ByteArrayInputStream(stream.toByteArray());
                            }
                        };

                        target.setSource(new StreamResource(ss, System.nanoTime() + ".png"));
                    }

                    // copy the bytes to the entity
                    ClassUtils.setBytes(stream.toByteArray(), entity, fieldName);
                } else {
                    Notification.show(message("ocs.modelbasededitform.upload.format.invalid"),
                            Notification.Type.ERROR_MESSAGE);
                }
            }
        }
    }

    private static final long serialVersionUID = 2201140375797069148L;

    /**
     * Indicates whether the fields have been post processed
     */
    private boolean fieldsProcessed;

    /**
     * A map containing the edit tables per view mode
     */
    private Map<Boolean, List<DetailsEditTable<?, ?>>> detailTables = new HashMap<>();

    /**
     * A map containing the collection tables per view mode
     */
    private Map<Boolean, List<CollectionTable<?>>> collectionTables = new HashMap<>();

    /**
     * Indicates whether all details tables for editing complex fields are valid
     */
    private Map<SignalsParent, Boolean> detailTablesValid = new HashMap<>();

    /**
     * 
     */
    private T entity;

    /**
     * 
     */
    private EntityModel<T> entityModel;

    /**
     * The field factory
     */
    private ModelBasedFieldFactory<T> fieldFactory;

    /**
     * Field filters for easily building filtered combo boxes
     */
    private Map<String, Filter> fieldFilters = new HashMap<>();

    /**
     * The field that must receive focus
     */
    private Field<?> firstField;

    private FormOptions formOptions;

    /**
     * A map containing all the labels that were added - used to replace the label values as the
     * selected entity changes
     */
    private Map<Boolean, Map<AttributeModel, Component>> labels = new HashMap<>();

    private VerticalLayout mainEditLayout;

    private VerticalLayout mainViewLayout;

    private Map<Boolean, BeanFieldGroup<T>> groups = new HashMap<>();

    private List<Button> saveButtons = new ArrayList<>();

    private BaseService<ID, T> service;

    private Map<Boolean, HorizontalLayout> titleBars = new HashMap<>();

    private Map<Boolean, Map<AttributeModel, Component>> uploads = new HashMap<>();

    private Map<Boolean, Label> titleLabels = new HashMap<>();

    /**
     * Whether to display the component in view mode
     */
    private boolean viewMode;

    /**
     * Constructor
     * 
     * @param entity
     * @param service
     * @param entityModel
     * @param formOptions
     */
    public ModelBasedEditForm(T entity, BaseService<ID, T> service, EntityModel<T> entityModel,
            FormOptions formOptions, Map<String, Filter> fieldFilters) {
        this.service = service;
        this.entity = entity;
        this.formOptions = formOptions;
        this.entityModel = entityModel;
        this.fieldFilters = fieldFilters;

        Class<T> clazz = service.getEntityClass();

        // set the custom field factory
        this.fieldFactory = ModelBasedFieldFactory.getInstance(entityModel, getMessageService());

        // open in view mode when this is requested, and it is not a new object
        this.viewMode = !isEditAllowed()
                || (formOptions.isOpenInViewMode() && entity.getId() != null);

        // set up a bean field group for automatic binding and validation
        BeanItem<T> beanItem = new BeanItem<T>(entity);
        BeanFieldGroup<T> group = new BeanFieldGroup<T>(clazz);
        group.setItemDataSource(beanItem);
        group.setBuffered(false);
        groups.put(Boolean.FALSE, group);

        beanItem = new BeanItem<T>(entity);
        group = new BeanFieldGroup<T>(clazz);
        group.setItemDataSource(beanItem);
        group.setBuffered(false);
        groups.put(Boolean.TRUE, group);

    }

    /**
     * Adds a field for a certain attribute
     * 
     * @param parent
     * @param entityModel
     * @param attributeModel
     */
    private void addField(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel,
            int count) {
        AttributeType type = attributeModel.getAttributeType();
        if (attributeModel.isVisible()
                && (AttributeType.BASIC.equals(type) || AttributeType.LOB.equals(type) || attributeModel
                        .isComplexEditable())) {
            if (attributeModel.isReadOnly() || isViewMode()) {
                if (entity.getId() != null) {
                    // read-only attribute are hidden for new entities
                    if (attributeModel.isUrl()
                            || (AttributeType.DETAIL.equals(type) || AttributeType.ELEMENT_COLLECTION
                                    .equals(type)) && attributeModel.isComplexEditable()) {
                        // display a complex component in read-only mode
                        constructField(parent, entityModel, attributeModel, true, count);
                    } else {
                        // display a label
                        if (attributeModel.isUrl()) {
                            URLField urlField = (URLField) fieldFactory.constructField(
                                    attributeModel, fieldFilters);
                            groups.get(isViewMode()).bind(urlField, attributeModel.getName());
                            parent.addComponent(urlField);
                        } else {
                            Component label = constructLabel(entity, attributeModel);
                            labels.get(isViewMode()).put(attributeModel, label);
                            parent.addComponent(label);
                        }
                    }
                } else {
                    // new entity - create the label but do not display it
                    if (attributeModel.isUrl()) {
                        URLField urlField = (URLField) fieldFactory.constructField(attributeModel,
                                fieldFilters);
                        groups.get(isViewMode()).bind(urlField, attributeModel.getName());
                        parent.addComponent(urlField);
                    } else {
                        Component label = constructLabel(entity, attributeModel);
                        label.setVisible(false);
                        labels.get(isViewMode()).put(attributeModel, label);
                        parent.addComponent(label);
                    }
                }
            } else {
                // display an editable field
                if (AttributeType.BASIC.equals(type) || AttributeType.MASTER.equals(type)
                        || AttributeType.DETAIL.equals(type)
                        || AttributeType.ELEMENT_COLLECTION.equals(type)) {
                    constructField(parent, entityModel, attributeModel, false, count);
                } else if (AttributeType.LOB.equals(type)) {
                    // for a LOB field we need to construct a rather
                    // elaborate upload component
                    UploadComponent uploadForm = constructUploadField(attributeModel);
                    parent.addComponent(uploadForm);
                    uploads.get(isViewMode()).put(attributeModel, uploadForm);
                }
            }
        }
    }

    /**
     * Method that is called after save action has been successfully carried out
     */
    protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
        // do nothing
    }

    /**
     * Main build method - lazily constructs the layout for either edit or view mode
     */
    @Override
    public void build() {
        if (isViewMode()) {
            if (mainViewLayout == null) {
                Map<AttributeModel, Component> map = new HashMap<>();
                labels.put(Boolean.TRUE, map);

                Map<AttributeModel, Component> uploadMap = new HashMap<>();
                uploads.put(Boolean.TRUE, uploadMap);

                detailTables.put(Boolean.TRUE, new ArrayList<DetailsEditTable<?, ?>>());
                collectionTables.put(Boolean.TRUE, new ArrayList<CollectionTable<?>>());
                mainViewLayout = buildMainLayout(entityModel);
            }
            setCompositionRoot(mainViewLayout);
        } else {
            if (mainEditLayout == null) {
                Map<AttributeModel, Component> map = new HashMap<>();
                labels.put(Boolean.FALSE, map);

                Map<AttributeModel, Component> uploadMap = new HashMap<>();
                uploads.put(Boolean.FALSE, uploadMap);

                detailTables.put(Boolean.FALSE, new ArrayList<DetailsEditTable<?, ?>>());
                collectionTables.put(Boolean.FALSE, new ArrayList<CollectionTable<?>>());
                mainEditLayout = buildMainLayout(entityModel);

                postProcessEditFields();
            }
            setCompositionRoot(mainEditLayout);
        }

    }

    /**
     * Constructs the main layout of the screen
     * 
     * @param entityModel
     * @return
     */
    protected VerticalLayout buildMainLayout(EntityModel<T> entityModel) {
        VerticalLayout layout = new VerticalLayout();

        titleLabels.put(isViewMode(), constructTitleLabel());

        // horizontal layout that contains title label and buttons

        titleBars.put(isViewMode(), new DefaultHorizontalLayout(false, true));
        titleBars.get(isViewMode()).addComponent(titleLabels.get(isViewMode()));

        HorizontalLayout buttonBar = constructButtonBar();
        buttonBar.setSizeUndefined();
        titleBars.get(isViewMode()).addComponent(buttonBar);
        layout.addComponent(titleBars.get(isViewMode()));

        // extra label (just for spacing)
        layout.addComponent(new Label(""));

        Layout form = null;
        if (entityModel.usesDefaultGroupOnly()) {
            form = new FormLayout();
        } else {
            form = new DefaultVerticalLayout(false, true);
        }

        // in case of vertical layout (the default), don't use the entire screen
        if (ScreenMode.VERTICAL.equals(formOptions.getScreenMode())) {
            form.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
        }

        int count = 0;
        if (!entityModel.usesDefaultGroupOnly()) {
            // display the attributes in groups

            TabSheet tabSheet = null;
            boolean tabs = AttributeGroupMode.TABSHEET.equals(formOptions.getAttributeGroupMode());
            if (tabs) {
                tabSheet = new TabSheet();
                form.addComponent(tabSheet);
            }

            if (getParentGroupHeaders() != null && getParentGroupHeaders().length > 0) {
                // extra layer of grouping
                for (String parentGroupHeader : getParentGroupHeaders()) {
                    Layout innerForm = constructAttributeGroupLayout(form, tabs, tabSheet,
                            parentGroupHeader, false);

                    // check if there is a tabsheet needed on the inner level
                    TabSheet innerTabSheet = null;
                    boolean innerTabs = !tabs;
                    if (innerTabs) {
                        innerTabSheet = new TabSheet();
                        innerForm.addComponent(innerTabSheet);
                    }

                    int tempCount = processParentHeaderGroup(parentGroupHeader, innerForm,
                            innerTabs, innerTabSheet, count);
                    count += tempCount;
                }
            } else {
                for (String attributeGroup : entityModel.getAttributeGroups()) {
                    if (entityModel.isAttributeGroupVisible(attributeGroup, viewMode)) {
                        Layout innerForm = constructAttributeGroupLayout(form, tabs, tabSheet,
                                getAttributeGroupCaption(attributeGroup), true);

                        for (AttributeModel attributeModel : entityModel
                                .getAttributeModelsForGroup(attributeGroup)) {
                            addField(innerForm, entityModel, attributeModel, count);
                            count++;
                        }
                    }
                }
            }
        } else {
            // iterate over the attributes and add them to the form (without any
            // grouping)
            for (AttributeModel attributeModel : entityModel.getAttributeModels()) {
                addField(form, entityModel, attributeModel, count);
                count++;
            }
        }

        layout.addComponent(form);

        if (firstField != null) {
            firstField.focus();
        }

        layout.addComponent(constructButtonBar());
        checkSaveButtonState();

        return layout;
    }

    private int processParentHeaderGroup(String parentGroupHeader, Layout innerForm,
            boolean innerTabs, TabSheet innerTabSheet, int startCount) {
        int count = 0;

        for (String attributeGroup : entityModel.getAttributeGroups()) {
            if (entityModel.isAttributeGroupVisible(attributeGroup, viewMode)) {
                if (getParentGroup(attributeGroup).equals(parentGroupHeader)) {
                    Layout innerLayout2 = constructAttributeGroupLayout(innerForm, innerTabs,
                            innerTabSheet, getAttributeGroupCaption(attributeGroup), true);
                    for (AttributeModel attributeModel : entityModel
                            .getAttributeModelsForGroup(attributeGroup)) {
                        addField(innerLayout2, entityModel, attributeModel, startCount + count);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Sets the state (enabled/disabled) of the save button. The button is enabled if the from is
     * valid and all of its detail tables are as well
     */
    private void checkSaveButtonState() {
        for (Button saveButton : saveButtons) {
            boolean valid = groups.get(isViewMode()).isValid();

            for (Boolean b : detailTablesValid.values()) {
                valid &= b;
            }
            saveButton.setEnabled(valid);
        }
    }

    /**
     * Construct the layout (form and panel) for an attribute group
     * 
     * @param parent
     *            the parent component
     * @param tabs
     *            whether to include the component in a tab sheet
     * @param tabSheet
     *            the parent tab sheet (only used if the "tabs" parameter is true)
     * @param caption
     *            caption of the panel or tab sheet
     * @param lowest
     *            indicates whether this is the lowest level
     * @return
     */
    private Layout constructAttributeGroupLayout(Layout parent, boolean tabs, TabSheet tabSheet,
            String caption, boolean lowest) {
        Layout innerLayout = null;
        if (lowest) {
            innerLayout = new FormLayout();
            ((FormLayout) innerLayout).setMargin(true);

            if (!tabs) {
                ((FormLayout) innerLayout).setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
            }
        } else {
            innerLayout = new DefaultVerticalLayout(true, true);
        }

        if (tabs) {
            tabSheet.addTab(innerLayout, caption);
        } else {
            Panel panel = new Panel();
            panel.setStyleName("attributePanel");
            panel.setCaption(caption);
            panel.setContent(innerLayout);
            parent.addComponent(panel);
        }
        return innerLayout;
    }

    /**
     * Creates a custom field
     * 
     * @param entityModel
     *            the entity model to base the field on
     * @param attributeModel
     *            the attribute model to base the field on
     * @return
     */
    protected Field<?> constructCustomField(EntityModel<T> entityModel,
            AttributeModel attributeModel, boolean viewMode) {
        // by default, return null. override in subclasses in order to create
        // specific fields
        return null;
    }

    /**
     * Constructs the save button
     */
    private Button constructSaveButton() {
        Button saveButton = new Button(message("ocs.save"));
        saveButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    boolean isNew = entity.getId() == null;

                    entity = service.save(entity);
                    setEntity(service.fetchById(entity.getId()));
                    Notification.show(message("ocs.changes.saved"),
                            Notification.Type.TRAY_NOTIFICATION);

                    // set to viewmode, load the view mode screen, and fill the
                    // details
                    if (formOptions.isOpenInViewMode()) {
                        viewMode = true;
                        build();

                    }

                    afterEditDone(false, isNew, getEntity());
                } catch (RuntimeException ex) {
                    handleSaveException(ex);
                }
            }
        });

        // enable/disable save button based on form validity
        saveButton.setEnabled(groups.get(isViewMode()).isValid());
        for (Field<?> f : groups.get(isViewMode()).getFields()) {
            f.addValueChangeListener(new Property.ValueChangeListener() {

                @Override
                public void valueChange(ValueChangeEvent event) {
                    checkSaveButtonState();
                }
            });
        }
        return saveButton;
    }

    /**
     * Constructs a field for a certain attribute
     * 
     * @param form
     * @param entityModel
     * @param attributeModel
     */
    @SuppressWarnings("unchecked")
    private void constructField(Layout form, EntityModel<T> entityModel,
            AttributeModel attributeModel, boolean viewMode, int count) {

        // allow the user to override the construction of a field
        Field<?> field = constructCustomField(entityModel, attributeModel, viewMode);
        if (field == null) {
            // if no custom field is defined, then use the default
            field = fieldFactory.constructField(attributeModel, fieldFilters);
        }

        if (field instanceof URLField) {
            ((URLField) field).setEditable(!isViewMode());
        }

        if (field instanceof DetailsEditTable) {
            detailTables.get(isViewMode()).add((DetailsEditTable<?, ?>) field);
        }

        if (field instanceof CollectionTable) {
            ((CollectionTable<?>) field).setViewMode(isViewMode());
            collectionTables.get(isViewMode()).add((CollectionTable<?>) field);
        }

        if (field != null) {
            if (!(field instanceof DetailsEditTable)) {
                groups.get(viewMode).bind(field, attributeModel.getName());
            }
            field.setSizeFull();
            form.addComponent(field);
        }

        // set the default value for new objects
        if (entity.getId() == null && attributeModel.getDefaultValue() != null) {
            field.getPropertyDataSource().setValue(attributeModel.getDefaultValue());
        }

        // store a reference to the first field so we can give it focus
        if (count == 0) {
            firstField = field;
        }
    }

    private Label constructTitleLabel() {
        Label label = null;

        // add title label
        String mainValue = EntityModelUtil.getMainAttributeValue(entity, entityModel);
        if (isViewMode()) {
            label = new Label(message("ocs.modelbasededitform.title.view",
                    entityModel.getDisplayName(), mainValue), ContentMode.HTML);
        } else {
            if (entity.getId() == null) {
                // create a new entity
                label = new Label(message("ocs.modelbasededitform.title.create",
                        entityModel.getDisplayName()), ContentMode.HTML);
            } else {
                // update an existing entity
                label = new Label(message("ocs.modelbasededitform.title.update",
                        entityModel.getDisplayName(), mainValue), ContentMode.HTML);
            }
        }
        return label;
    }

    /**
     * Constructs an upload field
     * 
     * @param entityModel
     * @param attributeModel
     */
    private UploadComponent constructUploadField(AttributeModel attributeModel) {
        return new UploadComponent(attributeModel);
    }

    private HorizontalLayout constructButtonBar() {
        HorizontalLayout buttonBar = new DefaultHorizontalLayout();

        // button to go back to the main screen when in view mode
        if (isViewMode() && formOptions.isShowBackButton()) {
            Button backButton = new Button(message("ocs.back"));
            backButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    afterEditDone(true, entity.getId() == null, entity);
                }
            });

            buttonBar.addComponent(backButton);
        }

        // in edit mode, display a cancel button
        if (!isViewMode() && !formOptions.isHideCancelButton()) {

            Button cancelButton = new Button(message("ocs.cancel"));
            cancelButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (entity.getId() != null) {
                        entity = service.fetchById(entity.getId());
                    }
                    afterEditDone(true, entity.getId() == null, entity);
                }
            });

            buttonBar.addComponent(cancelButton);
        }

        // create the save button
        if (!isViewMode()) {
            Button saveButton = constructSaveButton();
            buttonBar.addComponent(saveButton);
            saveButtons.add(saveButton);
        }

        // create the edit button
        if (isViewMode() && formOptions.isShowEditButton() && isEditAllowed()) {
            Button editButton = new Button(message("ocs.edit"));
            editButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    setViewMode(false);
                }
            });

            buttonBar.addComponent(editButton);
        }

        return buttonBar;
    }

    /**
     * @param attributeGroup
     * @return
     */
    private String getAttributeGroupCaption(String attributeGroup) {
        return EntityModel.DEFAULT_GROUP.equals(attributeGroup) ? message("ocs.default.group.caption")
                : attributeGroup;
    }

    public List<DetailsEditTable<?, ?>> getDetailTables() {
        return detailTables.get(isViewMode());
    }

    public T getEntity() {
        return entity;
    }

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public Field<?> getField(String propertyName) {
        return groups.get(isViewMode()).getField(propertyName);
    }

    /**
     * Indicates which parent group a certain child group belongs to. The parent group must be
     * mentioned in the result of the <code>getParentGroupHeaders</code> method. The childGroup must
     * be the name of an attribute group from the entity model
     * 
     * @param childGroup
     * @return
     */
    protected String getParentGroup(String childGroup) {
        return null;
    }

    /**
     * Returns the group headers of any additional parent groups that must be included in the form.
     * These can be used to add an extra layer of nesting of the attribute groups
     * 
     * @return
     */
    protected String[] getParentGroupHeaders() {
        return null;
    }

    public boolean isViewMode() {
        return viewMode;
    }

    public void setEntity(T entity) {
        this.entity = entity;

        setViewMode(formOptions.isOpenInViewMode() && entity.getId() != null);

        // recreate the group
        BeanItem<T> beanItem = new BeanItem<T>(entity);
        groups.get(isViewMode()).setItemDataSource(beanItem);

        // "rebuild" so that the correct layout is displayed
        build();

        // reconstruct all labels (since they cannot be bound automatically)
        for (Entry<AttributeModel, Component> e : labels.get(isViewMode()).entrySet()) {
            Component newLabel = constructLabel(entity, e.getKey());

            // label is displayed in view mode or when its an existing entity
            newLabel.setVisible(entity.getId() != null || isViewMode());

            // replace all existing labels with new labels
            HasComponents hc = e.getValue().getParent();
            if (hc instanceof Layout) {
                ((Layout) hc).replaceComponent(e.getValue(), newLabel);
                labels.get(isViewMode()).put(e.getKey(), newLabel);
            }
        }

        // refresh the upload components
        for (Entry<AttributeModel, Component> e : uploads.get(isViewMode()).entrySet()) {
            Component newLabel = constructUploadField(e.getKey());

            HasComponents hc = e.getValue().getParent();
            if (hc instanceof Layout) {
                ((Layout) hc).replaceComponent(e.getValue(), newLabel);
                uploads.get(isViewMode()).put(e.getKey(), newLabel);
            }
        }

        //
        for (DetailsEditTable<?, ?> table : getDetailTables()) {
            table.fillDetails();
        }

        // update the title label
        Label newTitleLabel = constructTitleLabel();
        titleBars.get(isViewMode()).replaceComponent(titleLabels.get(isViewMode()), newTitleLabel);
        titleLabels.put(isViewMode(), newTitleLabel);

    }

    public void setViewMode(boolean viewMode) {
        boolean oldMode = this.viewMode;

        // check what the new view mode must become and adapt the screen
        this.viewMode = !isEditAllowed() || viewMode;

        BeanItem<T> beanItem = new BeanItem<T>(entity);
        groups.get(isViewMode()).setItemDataSource(beanItem);

        constructTitleLabel();
        build();

        for (DetailsEditTable<?, ?> table : getDetailTables()) {
            table.fillDetails();
        }

        // if this is the first time in edit mode, post process the editable
        // fields
        if (!isViewMode() && !fieldsProcessed) {
            postProcessEditFields();
            fieldsProcessed = true;
        }

        if (oldMode != this.viewMode) {
            afterModeChanged(isViewMode());
        }
    }

    public void signalDetailsTableValid(SignalsParent component, boolean valid) {
        detailTablesValid.put(component, valid);
        checkSaveButtonState();
    }

    /**
     * Indicates whether it is allowed to edit this component
     * 
     * @return
     */
    protected boolean isEditAllowed() {
        return true;
    }

    /**
     * Post-processes any edit fields- this method does nothing by default but must be used to call
     * the postProcessEditFields callback method on an enclosing component
     */
    protected void postProcessEditFields() {
        // overwrite in subclasses
    }

    /**
     * Callback method that is called after the mode has changed from or to view mode
     */
    protected void afterModeChanged(boolean viewMode) {
        // overwrite in subclasses
    }

}