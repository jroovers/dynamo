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
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.vaadin.teemu.switchui.Switch;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.jarektoro.responsivelayout.ResponsiveRow.SpacingSize;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.component.BaseDetailsEditGrid;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.CollapsiblePanel;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.component.DefaultEmbedded;
import com.ocs.dynamo.ui.component.ResponsiveUtil;
import com.ocs.dynamo.ui.component.ServiceBasedDetailsEditGrid;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

/**
 * An edit form that is constructed based on an entity model s
 * 
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 * @author bas.rutten
 */
@SuppressWarnings("serial")
public class ModelBasedEditForm<ID extends Serializable, T extends AbstractEntity<ID>> extends AbstractModelBasedForm<ID, T>
        implements NestedComponent {

    /**
     * A custom field that can be used to upload a file
     *
     * @author bas.rutten
     */
    private final class UploadComponent extends CustomField<byte[]> {

        private AttributeModel attributeModel;

        private UploadComponent(AttributeModel attributeModel) {
            this.attributeModel = attributeModel;
        }

        @Override
        protected void doSetValue(byte[] value) {
            // not needed
        }

        @Override
        public byte[] getValue() {
            // not needed
            return null;
        }

        @Override
        protected Component initContent() {
            addStyleNames("uploadField");

            byte[] bytes = ClassUtils.getBytes(getEntity(), attributeModel.getName());
            Embedded image = new DefaultEmbedded(null, bytes);

            ResponsiveLayout main = new ResponsiveLayout().withFullSize();

            // for a LOB field, create an upload and an image
            // retrieve the current value
            if (attributeModel.isImage()) {
                image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
                image.setVisible(bytes != null);
                main.addRow().withSpacing(SpacingSize.SMALL, true).withComponents(image);
            } else {
                Label label = new Label(message("ocs.no.preview.available"));
                main.addRow().withSpacing(SpacingSize.SMALL, true).withComponents(label);
            }

            // callback object to handle successful upload
            UploadReceiver receiver = new UploadReceiver(image, attributeModel.getName(), attributeModel.getFileNameProperty(),
                    attributeModel.getAllowedExtensions().toArray(new String[0]));

            ResponsiveRow buttonBar = ResponsiveUtil.createButtonBar();
            main.addComponent(buttonBar);

            Upload upload = new Upload(null, receiver);
            upload.addSucceededListener(receiver);
            buttonBar.addComponent(upload);

            // a button used to clear the image
            Button clearButton = new Button(message("ocs.clear"));
            clearButton.addClickListener(event -> {
                ClassUtils.clearFieldValue(getEntity(), attributeModel.getName(), byte[].class);
                image.setVisible(false);
                if (attributeModel.getFileNameProperty() != null) {
                    ClassUtils.clearFieldValue(getEntity(), attributeModel.getFileNameProperty(), String.class);
                    refreshLabel(attributeModel.getFileNameProperty());
                }
            });
            buttonBar.addComponent(clearButton);
            setCaption(attributeModel.getDisplayName(VaadinUtils.getLocale()));

            return main;
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

        // the name of the file that is uploaded
        private String fileNameFieldName;

        private ByteArrayOutputStream stream;

        private String[] supportedExtensions;

        // the target component that must be updated after an upload
        private Embedded target;

        /**
         * Constructor
         *
         * @param target              the target component that must be updated after an
         *                            upload
         * @param fieldName           the name of the field
         * @param supportedExtensions the supported file extensions
         */
        private UploadReceiver(Embedded target, String fieldName, String fileNameFieldName, String... supportedExtensions) {
            this.target = target;
            this.fieldName = fieldName;
            this.fileNameFieldName = fileNameFieldName;
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

                if (supportedExtensions == null || supportedExtensions.length == 0
                        || (extension != null && Arrays.asList(supportedExtensions).contains(extension.toLowerCase()))) {

                    // set the image source
                    if (target != null) {
                        target.setVisible(true);
                        StreamResource.StreamSource ss = (StreamResource.StreamSource) () -> new ByteArrayInputStream(stream.toByteArray());
                        target.setSource(new StreamResource(ss, System.nanoTime() + ".png"));
                    }

                    // copy the bytes to the entity
                    ClassUtils.setBytes(stream.toByteArray(), getEntity(), fieldName);

                    // also set the file name if needed
                    if (fileNameFieldName != null) {
                        ClassUtils.setFieldValue(getEntity(), fileNameFieldName, event.getFilename());
                        refreshLabel(fileNameFieldName);
                    }

                    if (afterUploadConsumer != null) {
                        afterUploadConsumer.accept(event.getFilename(), stream.toByteArray());
                    }
                } else {
                    showNotifification(message("ocs.modelbasededitform.upload.format.invalid"), Notification.Type.ERROR_MESSAGE);
                }
            }
        }
    }

    private static final long serialVersionUID = 2201140375797069148L;

    private static final String EDIT_BUTTON_DATA = "editButton";

    private static final String SAVE_BUTTON_DATA = "saveButton";

    private static final String BACK_BUTTON_DATA = "backButton";

    private static final String CANCEL_BUTTON_DATA = "cancelButton";

    private static final String NEXT_BUTTON_DATA = "nextButton";

    private static final String PREV_BUTTON_DATA = "prevButton";

    /**
     * For keeping track of attribute groups per view mode
     */
    private Map<Boolean, Map<String, Object>> attributeGroups = new HashMap<>();

    /**
     * The relations to fetch when selecting a single detail relation
     */
    private FetchJoinInformation[] detailJoins;

    /**
     * Indicates whether all details tables for editing complex fields are valid
     */
    private Map<NestedComponent, Boolean> detailComponentsValid = new HashMap<>();

    /**
     * The selected entity
     */
    private T entity;

    /**
     * The field factory
     */
    private FieldFactory fieldFactory = FieldFactory.getInstance();

    /**
     * Indicates whether the fields have been post processed
     */
    private boolean fieldsProcessed;

    /**
     * Groups for data binding (one for each view mode)
     */
    private Map<Boolean, Binder<T>> groups = new HashMap<>();

    /**
     * A map containing all the labels that were added - used to replace the label
     * values as the selected entity changes
     */
    private Map<Boolean, Map<AttributeModel, Component>> labels = new HashMap<>();

    private ResponsiveLayout mainEditLayout;

    private ResponsiveLayout mainViewLayout;

    private Map<Boolean, List<Button>> buttons = new HashMap<>();

    private BaseService<ID, T> service;

    private Map<Boolean, TabSheet> tabSheets = new HashMap<>();

    private Map<Boolean, ResponsiveRow> titleBars = new HashMap<>();

    private Map<Boolean, Label> titleLabels = new HashMap<>();

    private Map<Boolean, Map<AttributeModel, Component>> uploads = new HashMap<>();

    private Map<Boolean, Map<AttributeModel, Component>> previews = new HashMap<>();

    private Map<Boolean, Set<String>> alreadyBound = new HashMap<>();

    /**
     * Map from tab index to the first field on each tab
     */
    private Map<Integer, Focusable> firstFields = new HashMap<>();

    /**
     * Whether the component supports iteration over the records
     */
    private boolean supportsIteration;

    /**
     * Whether to display the component in view mode
     */
    private boolean viewMode;

    /**
     * The fields to which to assign the currently selected entity after the
     * selected entity changes
     */
    private List<CanAssignEntity<ID, T>> assignEntityToFields = new ArrayList<>();

    /**
     * Whether the form is in nested mode
     */
    private boolean nestedMode;

    /**
     * Custom consumer that is to be called instead of the regular save behaviour
     */
    private Consumer<T> customSaveConsumer;

    /**
     * Optional code to execute after file upload
     */
    private BiConsumer<String, byte[]> afterUploadConsumer;

    /**
     * The column width (out of 12) of the edit form
     */
    private int columnWidth = SystemPropertyUtils.getDefaultFormColumnWidth();

    /**
     * Label column with (out of 12) of the edit form
     */
    private int labelWidth = SystemPropertyUtils.getDefaultLabelColumnWidth();

    /**
     * Constructor
     *
     * @param entity       the entity
     * @param service      the service
     * @param entityModel  the entity model
     * @param formOptions  the form options
     * @param fieldFilters the field filters
     */
    public ModelBasedEditForm(T entity, BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
            Map<String, SerializablePredicate<?>> fieldFilters) {
        super(formOptions, fieldFilters, entityModel);
        this.service = service;
        this.entity = entity;
        afterEntitySet(entity);
        Class<T> clazz = service.getEntityClass();

        // open in view mode when this is requested, and it is not a new object
        this.viewMode = !isEditAllowed() || (formOptions.isOpenInViewMode() && entity.getId() != null);

        // set up a bean field group for automatic binding and validation
        Binder<T> binder = new BeanValidationBinder<>(clazz);
        binder.setBean(entity);
        groups.put(Boolean.FALSE, binder);

        binder = new BeanValidationBinder<>(clazz);
        binder.setBean(entity);
        groups.put(Boolean.TRUE, binder);

        // init panel maps
        attributeGroups.put(Boolean.TRUE, new HashMap<>());
        attributeGroups.put(Boolean.FALSE, new HashMap<>());

        alreadyBound.put(Boolean.TRUE, new HashSet<>());
        alreadyBound.put(Boolean.FALSE, new HashSet<>());

        buttons.put(Boolean.TRUE, new ArrayList<>());
        buttons.put(Boolean.FALSE, new ArrayList<>());
    }

    /**
     * Adds a field for a certain attribute
     *
     * @param parent         the layout to which to add the field
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     */
    private void addField(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel, int tabIndex, boolean sameRow,
            int fieldWidth) {
        AttributeType type = attributeModel.getAttributeType();
        if (!alreadyBound.get(isViewMode()).contains(attributeModel.getPath()) && attributeModel.isVisible()
                && (AttributeType.BASIC.equals(type) || AttributeType.LOB.equals(type) || attributeModel.isComplexEditable())) {
            if (EditableType.READ_ONLY.equals(attributeModel.getEditableType()) || isViewMode()) {
                if (attributeModel.isUrl() || attributeModel.isNavigable()) {
                    // display a complex component even in read-only mode
                    constructField(parent, entityModel, attributeModel, true, tabIndex, sameRow, fieldWidth);
                } else if (AttributeType.LOB.equals(type) && attributeModel.isImage()) {
                    // image preview
                    Component c = constructImagePreview(attributeModel);
                    parent.addComponent(c);
                    previews.get(isViewMode()).put(attributeModel, c);
                } else {
                    AbstractComponent f = constructCustomField(entityModel, attributeModel, viewMode);
                    if (f instanceof UseInViewMode) {
                        constructField(parent, entityModel, attributeModel, true, tabIndex, sameRow, fieldWidth);
                    } else {// otherwise display a label
                        constructLabel(parent, entityModel, attributeModel, tabIndex, sameRow, fieldWidth);
                    }
                }
            } else {
                // display an editable field
                if (AttributeType.BASIC.equals(type) || AttributeType.MASTER.equals(type) || AttributeType.DETAIL.equals(type)
                        || AttributeType.ELEMENT_COLLECTION.equals(type)) {
                    constructField(parent, entityModel, attributeModel, false, tabIndex, sameRow, fieldWidth);
                } else if (AttributeType.LOB.equals(type)) {
                    // for a LOB field we need to construct a rather
                    // elaborate upload component
                    UploadComponent uploadForm = constructUploadField(attributeModel);
                    addFormRow(parent, uploadForm, attributeModel, labelWidth);
                    uploads.get(isViewMode()).put(attributeModel, uploadForm);
                }
            }
            alreadyBound.get(isViewMode()).add(attributeModel.getPath());
        }
    }

    /**
     * Adds a form row containing the specified component
     * 
     * @param parent
     * @param component
     * @param attributeModel
     * @param labelWidth
     */
    private void addFormRow(Layout parent, AbstractComponent component, AttributeModel attributeModel, int labelWidth) {
        ResponsiveRow rr = ResponsiveUtil.createFormRow();
        parent.addComponent(rr);

        Label label = createExtraLabel(component, attributeModel);
        rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, labelWidth, labelWidth, labelWidth).withComponent(label);
        component.setCaption("");
        int fieldWidth = DynamoConstants.MAX_COLUMNS - labelWidth;
        rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(component);
    }

    /**
     * Add a listener to respond to a tab change and focus the first available field
     *
     * @param tabSheet
     */
    private void addTabChangeListener(TabSheet tabSheet) {
        tabSheet.addSelectedTabChangeListener(event -> {
            Component c = event.getTabSheet().getSelectedTab();
            if (tabSheets.get(isViewMode()) != null && tabSheets.get(isViewMode()).getTab(c) != null) {
                int index = VaadinUtils.getTabIndex(tabSheets.get(isViewMode()), tabSheets.get(isViewMode()).getTab(c).getCaption());
                afterTabSelected(index);
                if (firstFields.get(index) != null) {
                    firstFields.get(index).focus();
                }
            }
        });
    }

    /**
     * Method that is called after the user is done editing an entity
     *
     * @param cancel    whether the user cancelled the editing
     * @param newObject whether the object is a new object
     * @param entity    the entity
     */
    protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
        // override in subclass
    }

    /**
     * Respond to the setting of a new entity as the selected entity. This can be
     * used to fetch any additionally required data
     *
     * @param entity the entity
     */
    protected void afterEntitySet(T entity) {
        // override in subclass
    }

    /**
     * Method that is called after a layout is built for the first time
     * 
     * @param layout   the layout that has just been built
     * @param viewMode whether the form is currently in view mode
     */
    protected void afterLayoutBuilt(Layout layout, boolean viewMode) {
        // after the layout
    }

    /**
     * Callback method that is called after the mode has changed from or to view
     * mode
     */
    protected void afterModeChanged(boolean viewMode) {
        // overwrite in subclasses
    }

    /**
     * Callback method that is called after a tab has been selected
     *
     * @param tabIndex the zero-based index of the selected tab
     */
    protected void afterTabSelected(int tabIndex) {
        // overwrite in subclasses
    }

    /**
     * Called after the user navigates back to a search screen using the back button
     *
     * @return
     */
    protected void back() {
        // overwrite in subclasses
    }

    /**
     * Main build method - lazily constructs the layout for either edit or view mode
     */
    @Override
    public void build() {
        ResponsiveLayout rl = new ResponsiveLayout().withFullSize();
        setCompositionRoot(rl);
        rl.removeAllComponents();

        if (isViewMode()) {
            if (mainViewLayout == null) {
                Map<AttributeModel, Component> map = new HashMap<>();
                labels.put(Boolean.TRUE, map);

                Map<AttributeModel, Component> uploadMap = new HashMap<>();
                uploads.put(Boolean.TRUE, uploadMap);

                Map<AttributeModel, Component> previewMap = new HashMap<>();
                previews.put(Boolean.TRUE, previewMap);

                mainViewLayout = buildMainLayout(getEntityModel());
            }
            rl.addRow().withDefaultRules(DynamoConstants.MAX_COLUMNS, DynamoConstants.MAX_COLUMNS, columnWidth, columnWidth).addColumn()
                    .withComponent(mainViewLayout);
        } else {
            if (mainEditLayout == null) {
                Map<AttributeModel, Component> map = new HashMap<>();
                labels.put(Boolean.FALSE, map);

                Map<AttributeModel, Component> uploadMap = new HashMap<>();
                uploads.put(Boolean.FALSE, uploadMap);

                Map<AttributeModel, Component> previewMap = new HashMap<>();
                previews.put(Boolean.FALSE, previewMap);

                mainEditLayout = buildMainLayout(getEntityModel());
                for (CanAssignEntity<ID, T> field : assignEntityToFields) {
                    field.assignEntity(entity);
                }

                if (!fieldsProcessed) {
                    postProcessEditFields();
                    fieldsProcessed = true;
                }
            }
            rl.addRow().withDefaultRules(DynamoConstants.MAX_COLUMNS, DynamoConstants.MAX_COLUMNS, columnWidth, columnWidth).addColumn()
                    .withComponent(mainEditLayout);
        }
    }

    /**
     * Constructs the main layout of the form
     *
     * @param entityModel the entity model to base the form on
     * @return
     */
    protected ResponsiveLayout buildMainLayout(EntityModel<T> entityModel) {
        ResponsiveLayout layout = new ResponsiveLayout().withFullSize();
        layout.setStyleName(DynamoConstants.CSS_MAIN_FORM);
        titleLabels.put(isViewMode(), constructTitleLabel());

        titleBars.put(isViewMode(), ResponsiveUtil.createRowWithSpacing().withStyleName("titleBar"));
        titleBars.get(isViewMode()).addComponent(titleLabels.get(isViewMode()));

        ResponsiveRow buttonBar = null;
        if (!nestedMode) {
            buttonBar = constructButtonBar(false);
            if (getFormOptions().isPlaceButtonBarAtTop()) {
                layout.addRow(buttonBar);
            } else {
                titleBars.get(isViewMode()).addColumn().withComponent(buttonBar);
            }
        }
        layout.addComponent(titleBars.get(isViewMode()));
        titleBars.get(isViewMode()).setVisible(!nestedMode);

        if (!entityModel.usesDefaultGroupOnly()) {
            // display the attributes in groups

            boolean tabs = AttributeGroupMode.TABSHEET.equals(getFormOptions().getAttributeGroupMode());
            if (tabs) {
                TabSheet tabSheet = new TabSheet();
                tabSheets.put(isViewMode(), tabSheet);

                ResponsiveUtil.addFullWidthRow(layout, tabSheet);

                // focus first field after tab change
                addTabChangeListener(tabSheet);
            }

            if (getParentGroupHeaders() != null && getParentGroupHeaders().length > 0) {
                // extra layer of grouping (always tabs)
                int tabIndex = 0;
                for (String parentGroupHeader : getParentGroupHeaders()) {
                    ResponsiveLayout innerLayout = constructAttributeGroupLayout(layout, tabs, tabSheets.get(isViewMode()),
                            parentGroupHeader, false);

                    // add a tab sheet on the inner level if needed
                    TabSheet innerTabSheet = null;
                    boolean innerTabs = !tabs;
                    if (innerTabs) {
                        innerTabSheet = new TabSheet();
                        ResponsiveRow row = innerLayout.addRow();
                        row.addColumn().withComponent(innerTabSheet);
                    }

                    // add all appropriate inner groups
                    processParentHeaderGroup(parentGroupHeader, innerLayout, innerTabs, innerTabSheet, tabIndex);
                    tabIndex++;
                }
            } else {
                // just one layer of attribute groups
                int tabIndex = 0;
                for (String attributeGroup : entityModel.getAttributeGroups()) {

                    if (entityModel.isAttributeGroupVisible(attributeGroup, isViewMode())) {
                        Layout innerForm = constructAttributeGroupLayout(layout, tabs, tabSheets.get(isViewMode()), attributeGroup, true);

                        for (AttributeModel attributeModel : entityModel.getAttributeModelsForGroup(attributeGroup)) {
                            addField(innerForm, entityModel, attributeModel, tabIndex, false, 0);
                        }
                        if (AttributeGroupMode.TABSHEET.equals(getFormOptions().getAttributeGroupMode())) {
                            tabIndex++;
                        }
                    }
                }
            }
        } else {
            // iterate over the attributes and add them to the form (without any
            // grouping)
            for (AttributeModel attributeModel : entityModel.getAttributeModels()) {
                addField(layout, entityModel, attributeModel, 0, false, 0);
            }
        }

        constructCascadeListeners();

        if (firstFields.get(0) != null) {
            firstFields.get(0).focus();
        }

        if (!nestedMode) {
            buttonBar = constructButtonBar(true);
            buttonBar.setSizeUndefined();
            layout.addComponent(buttonBar);
        }
        disableCreateOnlyFields();
        afterLayoutBuilt(layout, isViewMode());

        return layout;
    }

    /**
     * Checks the state of the iteration (prev/next) buttons. These will be shown in
     * view mode or if the form only supports an edit mode
     *
     * @param checkEnabled whether to check if the buttons should be enabled
     */
    private void checkIterationButtonState(boolean checkEnabled) {
        for (Button b : filterButtons(NEXT_BUTTON_DATA)) {
            b.setVisible(isSupportsIteration() && getFormOptions().isShowNextButton() && entity.getId() != null);
            if (checkEnabled && b.isVisible() && (isViewMode() || !getFormOptions().isOpenInViewMode())) {
                b.setEnabled(true);
            } else {
                b.setEnabled(false);
            }
        }
        for (Button b : filterButtons(PREV_BUTTON_DATA)) {
            b.setVisible(isSupportsIteration() && getFormOptions().isShowPrevButton() && entity.getId() != null);
            if (checkEnabled && b.isVisible() && (isViewMode() || !getFormOptions().isOpenInViewMode())) {
                b.setEnabled(true);
            } else {
                b.setEnabled(false);
            }
        }
    }

    /**
     * Construct the layout (form and panel) for an attribute group
     *
     * @param parent     the parent component
     * @param tabs       whether to include the component in a tab sheet
     * @param tabSheet   the parent tab sheet (only used if the "tabs" parameter is
     *                   true)
     * @param messageKey caption of the panel or tab sheet
     * @param lowest     indicates whether this is the lowest level
     * @return
     */
    private ResponsiveLayout constructAttributeGroupLayout(Layout parent, boolean tabs, TabSheet tabSheet, String messageKey,
            boolean lowest) {

        ResponsiveLayout innerLayout = new ResponsiveLayout().withFullSize().withSpacing();
        innerLayout.setStyleName("innerLayout");

        if (tabs) {
            Tab added = tabSheet.addTab(innerLayout, message(messageKey));
            attributeGroups.get(isViewMode()).put(messageKey, added);
        } else {
            // add a panel
            CollapsiblePanel panel = new CollapsiblePanel();
            panel.setStyleName("attributePanel");
            panel.setCaption(message(messageKey));
            panel.setContent(innerLayout);
            parent.addComponent(panel);
            attributeGroups.get(isViewMode()).put(messageKey, panel);
        }
        return innerLayout;
    }

    /**
     * Constructs the button bar
     * 
     * @param bottom indicates whether the button bar appears at the bottom of the
     *               screen
     * @return
     */
    private ResponsiveRow constructButtonBar(boolean bottom) {
        ResponsiveRow buttonBar = ResponsiveUtil.createButtonBar();

        // button to go back to the main screen when in view mode
        if (isViewMode() && getFormOptions().isShowBackButton()) {
            Button backButton = new Button(message("ocs.back"));
            backButton.setIcon(VaadinIcons.BACKWARDS);
            backButton.setStyleName(DynamoConstants.CSS_BACK_BUTTON);
            backButton.addClickListener(event -> back());
            backButton.setData(BACK_BUTTON_DATA);
            buttonBar.addComponent(backButton);
            buttons.get(isViewMode()).add(backButton);
        }

        constructCancelButton(buttonBar);
        constructSaveButton(buttonBar, bottom);

        // create the edit button
        if (isViewMode() && getFormOptions().isEditAllowed() && isEditAllowed()) {
            Button editButton = new Button(message("ocs.edit"));
            editButton.setIcon(VaadinIcons.PENCIL);
            editButton.addClickListener(event -> setViewMode(false));
            buttonBar.addComponent(editButton);
            buttons.get(isViewMode()).add(editButton);
            editButton.setData(EDIT_BUTTON_DATA);
        }

        Button prevButton = null;
        Button nextButton = null;

        // button for moving to the previous record
        if (isSupportsIteration() && getFormOptions().isShowPrevButton()) {
            prevButton = new Button(message("ocs.previous"));
            prevButton.setIcon(VaadinIcons.ARROW_LEFT);
            prevButton.addClickListener(e -> {
                T prev = getPreviousEntity();
                if (prev != null) {
                    setEntity(prev, true);
                }
                getPreviousButtons().stream().forEach(b -> b.setEnabled(hasPrevEntity()));
            });
            prevButton.setData(PREV_BUTTON_DATA);
            buttons.get(isViewMode()).add(prevButton);
            buttonBar.addComponent(prevButton);
            prevButton.setEnabled(hasPrevEntity());
        }

        // button for moving to the next record
        if (isSupportsIteration() && getFormOptions().isShowNextButton()) {
            nextButton = new Button(message("ocs.next"));
            nextButton.setIcon(VaadinIcons.ARROW_RIGHT);
            nextButton.addClickListener(e -> {
                T next = getNextEntity();
                if (next != null) {
                    setEntity(next, true);
                }
                getNextButtons().stream().forEach(b -> b.setEnabled(hasNextEntity()));

            });
            nextButton.setEnabled(hasNextEntity());
            nextButton.setData(NEXT_BUTTON_DATA);
            buttons.get(isViewMode()).add(nextButton);
            buttonBar.addComponent(nextButton);
        }

        if (prevButton != null) {
            prevButton.setVisible(isSupportsIteration() && getFormOptions().isShowPrevButton() && entity.getId() != null);
        }
        if (nextButton != null) {
            nextButton.setVisible(isSupportsIteration() && getFormOptions().isShowNextButton() && entity.getId() != null);
        }

        postProcessButtonBar(buttonBar, isViewMode());

        return buttonBar;
    }

    private void constructCancelButton(ResponsiveRow buttonBar) {
        if (!isViewMode() && !getFormOptions().isHideCancelButton()) {
            Button cancelButton = new Button(message("ocs.cancel"));
            cancelButton.setStyleName(DynamoConstants.CSS_CANCEL_BUTTON);
            cancelButton.setData(CANCEL_BUTTON_DATA);
            cancelButton.addClickListener(event -> {
                if (entity.getId() != null) {
                    entity = service.fetchById(entity.getId(), getDetailJoins());
                }
                afterEditDone(true, entity.getId() == null, entity);
            });
            cancelButton.setIcon(VaadinIcons.BAN);
            buttonBar.addComponent(cancelButton);
            buttons.get(isViewMode()).add(cancelButton);
        }
    }

    /**
     * Adds any value change listeners for taking care of cascading search
     */
    @SuppressWarnings("unchecked")
    private <S> void constructCascadeListeners() {
        for (final AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
            HasValue<S> field = (HasValue<S>) getField(isViewMode(), am.getPath());
            if (field != null) {
                field.addValueChangeListener(event -> {
                    for (String cascadePath : am.getCascadeAttributes()) {
                        CascadeMode cm = am.getCascadeMode(cascadePath);
                        if (CascadeMode.BOTH.equals(cm) || CascadeMode.EDIT.equals(cm)) {
                            AbstractComponent cascadeField = getField(isViewMode(), cascadePath);
                            if (cascadeField instanceof Cascadable) {
                                Cascadable<S> ca = (Cascadable<S>) cascadeField;
                                if (event.getValue() == null) {
                                    ca.clearAdditionalFilter();
                                } else {
                                    ca.setAdditionalFilter(new EqualsPredicate<S>(am.getCascadeFilterPath(cascadePath), event.getValue()));
                                }
                            } else {
                                // field not found or does not support cascading
                                throw new OCSRuntimeException("Cannot setup cascading from " + am.getPath() + " to " + cascadePath);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Callback method for inserting custom converter
     * 
     * @param am
     * @return
     */
    protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
        return null;
    }

    /**
     * Creates a custom field
     *
     * @param entityModel    the entity model to base the field on
     * @param attributeModel the attribute model to base the field on
     * @return
     */
    protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode) {
        // by default, return null. override in subclasses in order to create
        // specific fields
        return null;
    }

    /**
     * Constructs a field or label for a certain attribute
     *
     * @param parent   the parent layout to which to add the field
     * @param em       the entity model
     * @param am       the attribute model
     * @param viewMode whether the screen is in view mode
     */
    @SuppressWarnings({ "unchecked" })
    private void constructField(Layout parent, EntityModel<T> em, AttributeModel am, boolean viewMode, int tabIndex, boolean sameRow,
            int fieldWidth) {

        EntityModel<?> fieldEntityModel = getFieldEntityModel(am);
        // allow the user to override the construction of a field
        AbstractComponent field = constructCustomField(em, am, viewMode);
        if (field == null) {
            FieldFactoryContext ctx = FieldFactoryContext.create().setAttributeModel(am).setFieldEntityModel(fieldEntityModel)
                    .setFieldFilters(getFieldFilters()).setViewMode(viewMode);
            field = fieldFactory.constructField(ctx);
        }

        if (field instanceof URLField) {
            ((URLField) field).setEditable(!isViewMode() && !EditableType.CREATE_ONLY.equals(am.getEditableType()));
        }

        if (field != null) {
            field.setId(ResponsiveUtil.getId(am));

            // apply styling to every field except for the switch (the switch has a fixed
            // width)
            if (!(field instanceof Switch)) {
                field.addStyleName(DynamoConstants.CSS_DYNAMO_FIELD);
                field.setResponsive(true);
                field.setSizeFull();
            }

            if (field instanceof BaseDetailsEditGrid) {
                field.addStyleName(DynamoConstants.CSS_DETAILS_EDIT_GRID);
            }

            // add converters and validators
            if (!(field instanceof ServiceBasedDetailsEditGrid)) {
                BindingBuilder<T, ?> builder = groups.get(viewMode).forField((HasValue<?>) field);
                fieldFactory.addConvertersAndValidators(builder, am, constructCustomConverter(am));
                builder.bind(am.getPath());
            }

            if (!am.getGroupTogetherWith().isEmpty()) {
                // group multiple fields together on the same line
                int extraFields = am.getGroupTogetherWith().size();

                switch (extraFields) {
                case 1:
                    fieldWidth = 3;
                    break;
                case 2:
                    fieldWidth = 2;
                    break;
                default:
                    fieldWidth = 1;
                }

                // multiple fields on one line
                ResponsiveRow rr = new ResponsiveRow().withSpacing(SpacingSize.SMALL, true).withStyleName(DynamoConstants.CSS_DYNAMO_FORM);
                parent.addComponent(rr);

                Label label = createExtraLabel(field, am);

                rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, labelWidth, labelWidth, labelWidth).withComponent(label);
                rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(field);

                for (String path : am.getGroupTogetherWith()) {
                    AttributeModel nestedAm = getEntityModel().getAttributeModel(path);
                    if (nestedAm != null) {
                        addField(rr, em, nestedAm, tabIndex, true, fieldWidth);
                    }
                }
            } else {
                if (!sameRow) {
                    addFormRow(parent, field, am, labelWidth);
                } else {
                    // put the component on the same row as an earlier component
                    Label label = createExtraLabel(field, am);
                    ResponsiveRow rr = (ResponsiveRow) parent;
                    rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(label);
                    field.setCaption("");
                    rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(field);
                }
            }
        }

        // set the default value for new objects
        if (entity.getId() == null && am.getDefaultValue() != null) {
            setDefaultValue((HasValue<Object>) field, am.getDefaultValue());
        }

        // store a reference to the first field so we can give it focus
        // store a reference to the first field so we can give it focus
        if (!isViewMode() && firstFields.get(tabIndex) == null && field.isEnabled() && !(field instanceof CheckBox)
                && (field instanceof Focusable)) {
            firstFields.put(tabIndex, (Focusable) field);
        }

        if (field instanceof CanAssignEntity) {
            ((CanAssignEntity<ID, T>) field).assignEntity(entity);
            assignEntityToFields.add((CanAssignEntity<ID, T>) field);
        }
    }

    /**
     * Constructs a preview component for displaying an image
     * 
     * @param attributeModel the attribute model for the image property
     * @return
     */
    private Component constructImagePreview(AttributeModel attributeModel) {
        byte[] bytes = ClassUtils.getBytes(getEntity(), attributeModel.getName());
        Embedded image = new DefaultEmbedded(attributeModel.getDisplayName(VaadinUtils.getLocale()), bytes);
        image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
        return image;
    }

    /**
     * Constructs a label
     *
     * @param parent         the parent component to which the label must be added
     * @param entityModel    the entity model
     * @param attributeModel the attribute model for the attribute for which to
     *                       create a* label
     * @param tabIndex       the number of components added so far
     */
    private void constructLabel(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel, int tabIndex, boolean sameRow,
            int fieldWidth) {
        AbstractComponent label = constructLabel(entity, attributeModel);
        labels.get(isViewMode()).put(attributeModel, label);

        if (!attributeModel.getGroupTogetherWith().isEmpty()) {

            int extraFields = attributeModel.getGroupTogetherWith().size();

            switch (extraFields) {
            case 1:
                fieldWidth = 3;
                break;
            case 2:
                fieldWidth = 2;
                break;
            default:
                fieldWidth = 1;
            }

            // multiple fields on one line
            ResponsiveRow rr = new ResponsiveRow().withSpacing(SpacingSize.SMALL, true).withStyleName(DynamoConstants.CSS_DYNAMO_FORM);
            parent.addComponent(rr);

            Label extraLabel = createExtraLabel(label, attributeModel);

            rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, labelWidth, labelWidth, labelWidth).withComponent(extraLabel);
            rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(label);

            // add components to the same row
            for (String path : attributeModel.getGroupTogetherWith()) {
                AttributeModel am = entityModel.getAttributeModel(path);
                if (am != null) {
                    addField(rr, getEntityModel(), am, tabIndex, true, fieldWidth);
                }
            }
        } else {
            if (!sameRow) {
                // simply put the field on a row of its own
                ResponsiveRow rr = new ResponsiveRow().withSpacing(SpacingSize.SMALL, true).withStyleName(DynamoConstants.CSS_DYNAMO_FORM);
                parent.addComponent(rr);

                Label extraLabel = createExtraLabel(label, attributeModel);
                rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, labelWidth, labelWidth, labelWidth).withComponent(extraLabel);

                fieldWidth = DynamoConstants.MAX_COLUMNS - labelWidth;
                rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(label);
            } else {
                // add another label on the same row
                Label extraLabel = createExtraLabel(label, attributeModel);
                ResponsiveRow rr = (ResponsiveRow) parent;
                rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(extraLabel);
                rr.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, fieldWidth, fieldWidth, fieldWidth).withComponent(label);

            }
        }
    }

    /**
     * Constructs the save button
     *
     * @param bottom indicates whether this is the button at the bottom of the
     *               screen
     */
    private void constructSaveButton(ResponsiveRow buttonBar, boolean bottom) {
        if (!isViewMode()) {
            Button saveButton = new Button(
                    (entity != null && entity.getId() != null) ? message("ocs.save.existing") : message("ocs.save.new"));
            saveButton.setIcon(VaadinIcons.SAFE);
            saveButton.setStyleName(DynamoConstants.CSS_SAVE_BUTTON);
            saveButton.addClickListener(event -> {
                try {
                    // validate all fields
                    boolean error = validateAllFields();
                    if (!error) {
                        if (getFormOptions().isConfirmSave()) {
                            // ask for confirmation before saving
                            service.validate(entity);
                            VaadinUtils.showConfirmDialog(getMessageService(), getMessageService().getMessage("ocs.confirm.save",
                                    VaadinUtils.getLocale(), getEntityModel().getDisplayName(VaadinUtils.getLocale())), () -> {
                                        try {
                                            if (customSaveConsumer != null) {
                                                customSaveConsumer.accept(entity);
                                            } else {
                                                doSave();
                                            }
                                        } catch (RuntimeException ex) {
                                            if (!handleCustomException(ex)) {
                                                handleSaveException(ex);
                                            }
                                        }
                                    });
                        } else {
                            if (customSaveConsumer != null) {
                                customSaveConsumer.accept(entity);
                            } else {
                                doSave();
                            }
                        }
                    }
                } catch (RuntimeException ex) {
                    if (!handleCustomException(ex)) {
                        handleSaveException(ex);
                    }
                }
            });

            // enable/disable save button based on form validity
            saveButton.setData(SAVE_BUTTON_DATA);
            if (bottom) {
                groups.get(isViewMode()).getFields()
                        .forEach(f -> f.addValueChangeListener(event -> ((AbstractComponent) f).setComponentError(null)));
            }
            buttons.get(isViewMode()).add(saveButton);
            buttonBar.addComponent(saveButton);
        }
    }

    private Label constructTitleLabel() {
        Label label = null;

        // add title label
        String mainValue = EntityModelUtils.getDisplayPropertyValue(entity, getEntityModel());
        if (isViewMode()) {
            label = new Label(
                    message("ocs.modelbasededitform.title.view", getEntityModel().getDisplayName(VaadinUtils.getLocale()), mainValue));
        } else {
            if (entity.getId() == null) {
                // create a new entity
                label = new Label(message("ocs.modelbasededitform.title.create", getEntityModel().getDisplayName(VaadinUtils.getLocale())));
            } else {
                // update an existing entity
                label = new Label(message("ocs.modelbasededitform.title.update", getEntityModel().getDisplayName(VaadinUtils.getLocale()),
                        mainValue));
            }
        }
        label.setContentMode(ContentMode.HTML);

        return label;
    }

    /**
     * Constructs an upload field
     *
     * @param attributeModel
     */
    private UploadComponent constructUploadField(AttributeModel attributeModel) {
        return new UploadComponent(attributeModel);
    }

    /**
     * Creates an extra label to replace the Vaadin label that we are hiding
     * 
     * @param field          the field
     * @param attributeModel the attribute model
     * @return
     */
    private Label createExtraLabel(AbstractComponent field, AttributeModel attributeModel) {
        Label label = new Label(attributeModel.getDisplayName(VaadinUtils.getLocale()));
        if (attributeModel.isRequired()) {
            label.addStyleName("required");
        }
        label.addStyleName("caption");

        return label;
    }

    /**
     * Disables any fields that are only editable when creating a new entity
     */
    private void disableCreateOnlyFields() {
        if (!isViewMode()) {
            for (AttributeModel am : getEntityModel().getAttributeModels()) {
                AbstractComponent field = getField(isViewMode(), am.getPath());
                if (field != null && EditableType.CREATE_ONLY.equals(am.getEditableType())) {
                    field.setEnabled(entity.getId() == null);
                }
            }
        }
    }

    /**
     * Perform the actual save action
     */
    public void doSave() {
        boolean isNew = entity.getId() == null;
        entity = service.save(entity);
        setEntity(service.fetchById(entity.getId(), getDetailJoins()), !getFormOptions().isOpenInViewMode());
        showNotifification(message("ocs.changes.saved"), Notification.Type.TRAY_NOTIFICATION);

        // set to view mode, load the view mode screen, and fill the
        // details
        if (getFormOptions().isOpenInViewMode()) {
            viewMode = true;
            build();
        }
        afterEditDone(false, isNew, getEntity());
    }

    private List<Button> filterButtons(String data) {
        return Collections
                .unmodifiableList(buttons.get(isViewMode()).stream().filter(b -> data.equals(b.getData())).collect(Collectors.toList()));
    }

    public BiConsumer<String, byte[]> getAfterUploadConsumer() {
        return afterUploadConsumer;
    }

    public CollapsiblePanel getAttributeGroupPanel(String key) {
        Object c = attributeGroups.get(isViewMode()).get(key);
        if (c instanceof CollapsiblePanel) {
            return (CollapsiblePanel) c;
        }
        return null;
    }

    public List<Button> getBackButtons() {
        return filterButtons(BACK_BUTTON_DATA);
    }

    /**
     * Returns the binding for a field
     * 
     * @param fieldName
     * @return
     */
    public Binding<T, ?> getBinding(String fieldName) {
        Optional<Binding<T, ?>> binding = groups.get(viewMode).getBinding(fieldName);
        if (binding.isPresent()) {
            return binding.get();
        }
        return null;
    }

    public List<Button> getCancelButtons() {
        return filterButtons(CANCEL_BUTTON_DATA);
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public Consumer<T> getCustomSaveConsumer() {
        return customSaveConsumer;
    }

    public FetchJoinInformation[] getDetailJoins() {
        return detailJoins;
    }

    public List<Button> getEditButtons() {
        return filterButtons(EDIT_BUTTON_DATA);
    }

    public T getEntity() {
        return entity;
    }

    /**
     * Retrieves a field for a certain property
     * 
     * @param viewMode  whether the screen is in view mode
     * @param fieldName the name of the field/property
     * @return
     */
    private AbstractComponent getField(boolean viewMode, String fieldName) {
        Optional<Binding<T, ?>> binding = groups.get(viewMode).getBinding(fieldName);
        if (binding.isPresent()) {
            return (AbstractComponent) binding.get().getField();
        }
        return null;
    }

    /**
     * Returns the field with the given name, if it exists
     * 
     * @param fieldName the name of the field
     * @return
     */
    public AbstractComponent getField(String fieldName) {
        return getField(isViewMode(), fieldName);
    }

    /**
     * Returns an the field with the given name, if it exists, cast to a HasValue
     * 
     * @param fieldName the name of the field
     * @return
     */
    @SuppressWarnings("unchecked")
    public <U> HasValue<U> getFieldAsHasValue(String fieldName) {
        return (HasValue<U>) getField(isViewMode(), fieldName);
    }

    /**
     * Returns an Optional that contains the field with the given name, if it exists
     * 
     * @param fieldName the name of the field
     * @return
     */
    public Optional<AbstractComponent> getFieldOptional(String fieldName) {
        return Optional.ofNullable(getField(fieldName));
    }

    /**
     * Returns the collection of all input fields for the specified mode
     * 
     * @param viewMode the view mode
     * @return
     */
    public Collection<HasValue<?>> getFields(boolean viewMode) {
        return groups.get(viewMode).getFields().collect(Collectors.toList());
    }

    public Label getLabel(String propertyName) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            return (Label) labels.get(isViewMode()).get(am);
        }
        return null;
    }

    public List<Button> getNextButtons() {
        return filterButtons(NEXT_BUTTON_DATA);
    }

    /**
     * Returns the next entity from the encapsulating layout
     * 
     * @return
     */
    protected T getNextEntity() {
        // overwrite in subclass
        return null;
    }

    /**
     * Indicates which parent group a certain child group belongs to. The parent
     * group must be mentioned in the result of the
     * <code>getParentGroupHeaders</code> method. The childGroup must be the name of
     * an attribute group from the entity model
     *
     * @param childGroup
     * @return
     */
    protected String getParentGroup(String childGroup) {
        return null;
    }

    /**
     * Returns the group headers of any additional parent groups that must be
     * included in the form. These can be used to add an extra layer of nesting of
     * the attribute groups
     *
     * @return
     */
    protected String[] getParentGroupHeaders() {
        return null;
    }

    public List<Button> getPreviousButtons() {
        return filterButtons(PREV_BUTTON_DATA);
    }

    /**
     * Returns the previous entity from the encapsulating layout
     * 
     * @return
     */
    protected T getPreviousEntity() {
        // overwrite in subclass
        return null;
    }

    public List<Button> getSaveButtons() {
        return filterButtons(SAVE_BUTTON_DATA);
    }

    public int getSelectedTabIndex() {
        if (tabSheets.get(isViewMode()) != null) {
            Component c = tabSheets.get(isViewMode()).getSelectedTab();
            return VaadinUtils.getTabIndex(tabSheets.get(isViewMode()), tabSheets.get(isViewMode()).getTab(c).getCaption());
        }
        return 0;
    }

    protected boolean handleCustomException(RuntimeException ex) {
        return false;
    }

    protected boolean hasNextEntity() {
        return false;
    }

    protected boolean hasPrevEntity() {
        return false;
    }

    /**
     * Check whether a certain attribute group is visible
     *
     * @param key the message key by which the group is identifier
     * @return
     */
    public boolean isAttributeGroupVisible(String key) {
        Object c = attributeGroups.get(false).get(key);
        return c == null ? false : isGroupVisible(c);
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
     * Check if a certain attribute group is visible
     *
     * @param c the component representing the attribute group
     * @return
     */
    private boolean isGroupVisible(Object c) {
        if (c != null) {
            if (c instanceof Component) {
                return ((Component) c).isVisible();
            } else if (c instanceof Tab) {
                return ((Tab) c).isVisible();
            }
        }
        return false;
    }

    public boolean isNestedMode() {
        return nestedMode;
    }

    public boolean isSupportsIteration() {
        return supportsIteration;
    }

    /**
     * Check if the form is valid
     *
     * @return
     */
    public boolean isValid() {
        boolean valid = groups.get(isViewMode()).isValid();
        valid &= detailComponentsValid.values().stream().allMatch(x -> x);
        return valid;
    }

    public boolean isViewMode() {
        return viewMode;
    }

    /**
     * Post-processes the button bar that is displayed above/below the edit form
     *
     * @param buttonBar
     * @param viewMode
     */
    protected void postProcessButtonBar(ResponsiveRow buttonBar, boolean viewMode) {
        // overwrite in subclasses
    }

    /**
     * Post-processes any edit fields- this method does nothing by default but must
     * be used to call the postProcessEditFields callback method on an enclosing
     * component
     */
    protected void postProcessEditFields() {
        // overwrite in subclasses
    }

    /**
     * Processes all fields that are part of a property group
     *
     * @param parentGroupHeader the group header
     * @param innerForm         the form layout to which to add the fields
     * @param innerTabs         whether we are displaying tabs
     * @param innerTabSheet     the tab sheet to which to add the fields
     * @param tabIndex
     */
    private void processParentHeaderGroup(String parentGroupHeader, Layout innerForm, boolean innerTabs, TabSheet innerTabSheet,
            int tabIndex) {

        // display a group if it is not the default group
        for (String attributeGroup : getEntityModel().getAttributeGroups()) {
            if ((!EntityModel.DEFAULT_GROUP.equals(attributeGroup) || getEntityModel().isAttributeGroupVisible(attributeGroup, viewMode))
                    && getParentGroup(attributeGroup).equals(parentGroupHeader)) {
                Layout innerLayout2 = constructAttributeGroupLayout(innerForm, innerTabs, innerTabSheet, attributeGroup, true);
                for (AttributeModel attributeModel : getEntityModel().getAttributeModelsForGroup(attributeGroup)) {
                    addField(innerLayout2, getEntityModel(), attributeModel, tabIndex, false, 0);
                }
            }
        }
    }

    public void putAttributeGroupPanel(String key, Component c) {
        attributeGroups.get(isViewMode()).put(key, c);
    }

    /**
     * Reconstructs all labels after a change of the view mode or the selected
     * entity
     */
    private void reconstructLabels() {
        // reconstruct all labels (since they cannot be bound automatically)
        if (labels.get(isViewMode()) != null) {
            for (Entry<AttributeModel, Component> e : labels.get(isViewMode()).entrySet()) {
                Object value = ClassUtils.getFieldValue(entity, e.getKey().getPath());
                String formatted = FormatUtils.formatPropertyValue(getEntityModelFactory(), e.getKey(), value, "<br/>");
                ((Label) e.getValue()).setValue(formatted);
            }
        }

        // also replace the title label
        Label titleLabel = titleLabels.get(isViewMode());
        if (titleLabel != null) {
            Label newLabel = constructTitleLabel();
            titleLabel.setValue(newLabel.getValue());
        }
    }

    /**
     * Refreshes the binding for the currently selected entity. This can be used to
     * force a fresh after you make changes to e.g. the converters after the entity
     * has already been set
     */
    public void refreshBinding() {
        groups.get(isViewMode()).setBean(entity);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void refreshFieldFilters() {

        // if there is a field filter, make sure it is used
        for (String propertyName : getFieldFilters().keySet()) {
            Optional<Binding<T, ?>> binding = groups.get(isViewMode()).getBinding(propertyName);
            if (binding.isPresent()) {
                HasValue<?> field = binding.get().getField();
                if (field instanceof CustomEntityField) {
                    SerializablePredicate<?> fieldFilter = getFieldFilters().get(propertyName);
                    ((CustomEntityField) field).refresh(fieldFilter);
                }
            }
        }

        // otherwise refresh
        groups.get(isViewMode()).getFields().forEach(field -> {
            if (field instanceof Refreshable && !(field instanceof CustomEntityField)) {
                ((Refreshable) field).refresh();
            }
        });
    }

    /**
     * Replaces a label (in response to a change)
     * 
     * TODO: does not seem to work properly
     *
     * @param propertyName
     */
    public void refreshLabel(String propertyName) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            Component replacement = constructLabel(getEntity(), am);
            Component oldLabel = labels.get(isViewMode()).get(am);

            // label is displayed in view mode or when its an existing entity
            replacement.setVisible(true);

            // replace all existing labels with new labels
            HasComponents hc = labels.get(isViewMode()).get(am).getParent();
            if (hc instanceof Layout) {
                ((Layout) hc).replaceComponent(oldLabel, replacement);
                labels.get(isViewMode()).put(am, replacement);
            }
        }
    }

    /**
     * Replaces an existing label by a label with the provided value
     *
     * @param propertyName the name of the property for which to replace the label
     * @param value        the name value
     */
    public void refreshLabel(String propertyName, String value) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            Component replacement = new Label(value);
            replacement.setCaption(am.getDisplayName(VaadinUtils.getLocale()));
            Component oldLabel = labels.get(isViewMode()).get(am);

            // label is displayed in view mode or when its an existing entity
            replacement.setVisible(true);

            // replace all existing labels with new labels
            Component label = labels.get(isViewMode()).get(am);
            HasComponents hc = label == null ? null : label.getParent();
            if (hc instanceof Layout) {
                ((Layout) hc).replaceComponent(oldLabel, replacement);
                labels.get(isViewMode()).put(am, replacement);
            }
        }
    }

    /**
     * Removes any error messages from the individual form components
     */
    private void resetComponentErrors() {
        groups.get(isViewMode()).getFields().forEach(f -> ((AbstractComponent) f).setComponentError(null));
    }

    /**
     * Resets the selected tab index
     */
    public void resetTab() {
        if (tabSheets.get(isViewMode()) != null && !getFormOptions().isPreserveSelectedTab()) {
            tabSheets.get(isViewMode()).setSelectedTab(0);
        }
    }

    /**
     * Selects the tab specified by the provided index
     *
     * @param index
     */
    public void selectTab(int index) {
        if (tabSheets.get(isViewMode()) != null) {
            tabSheets.get(isViewMode()).setSelectedTab(index);
        }
    }

    public void setAfterUploadConsumer(BiConsumer<String, byte[]> afterUploadConsumer) {
        this.afterUploadConsumer = afterUploadConsumer;
    }

    /**
     * Shows/hides an attribute group
     *
     * @param key     the message key by which the group is identified
     * @param visible whether to show/hide the group
     */
    public void setAttributeGroupVisible(String key, boolean visible) {
        Object c = attributeGroups.get(false).get(key);
        setGroupVisible(c, visible);
        c = attributeGroups.get(true).get(key);
        setGroupVisible(c, visible);
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }

    /**
     * Shows or hides the component for a certain property - this will work*
     * regardless of the view
     *
     * @param propertyName the name of the property for which to show/hide the
     *                     property
     * @param visible
     */
    public void setComponentVisible(String propertyName, boolean visible) {
        setLabelVisible(propertyName, visible);
        AbstractComponent field = getField(isViewMode(), propertyName);
        if (field != null) {
            field.setVisible(visible);
        }
    }

    /**
     * Sets the custom code to be carried out after the Save button is clicked
     * 
     * @param customSaveConsumer
     */
    public void setCustomSaveConsumer(Consumer<T> customSaveConsumer) {
        this.customSaveConsumer = customSaveConsumer;
    }

    /**
     * Sets the default value for a field
     * 
     * @param field the field
     * @param value the default value
     */
    private <R> void setDefaultValue(HasValue<R> field, R value) {
        field.setValue(value);
    }

    public void setDetailJoins(FetchJoinInformation[] detailJoins) {
        this.detailJoins = detailJoins;
    }

    public void setEntity(T entity) {
        setEntity(entity, true);
    }

    public void setEntity(T entity, boolean rebuild) {
        setEntity(entity, entity.getId() != null, rebuild);
    }

    /**
     * Sets the currently selected entity to the provided entity
     * 
     * @param entity                the entity
     * @param checkIterationButtons
     * @param rebuild               whether to rebuild the form directly
     */
    private void setEntity(T entity, boolean checkIterationButtons, boolean rebuild) {
        this.entity = entity;

        if (rebuild) {
            refreshFieldFilters();

            // inform all children
            for (CanAssignEntity<ID, T> field : assignEntityToFields) {
                field.assignEntity(entity);
            }
            afterEntitySet(this.entity);
            setViewMode(getFormOptions().isOpenInViewMode() && entity.getId() != null, checkIterationButtons);

            // recreate the group
            groups.get(isViewMode()).setBean(entity);

            build();
            reconstructLabels();

            // refresh the upload components
            for (Entry<AttributeModel, Component> e : uploads.get(isViewMode()).entrySet()) {
                HasComponents hc = e.getValue().getParent();
                if (hc instanceof Layout) {
                    Component uc = constructUploadField(e.getKey());
                    ((Layout) hc).replaceComponent(e.getValue(), uc);
                    uploads.get(isViewMode()).put(e.getKey(), uc);
                }
            }

            // refresh preview components
            for (Entry<AttributeModel, Component> e : previews.get(isViewMode()).entrySet()) {
                HasComponents hc = e.getValue().getParent();
                if (hc instanceof Layout) {
                    Component pv = constructImagePreview(e.getKey());
                    ((Layout) hc).replaceComponent(e.getValue(), pv);
                    previews.get(isViewMode()).put(e.getKey(), pv);
                }
            }

            // enable/disable fields for create only mode
            disableCreateOnlyFields();

            // change caption depending on entity state
            updateSaveButtonCaptions();

            for (Button b : getCancelButtons()) {
                b.setVisible((!isViewMode() && !getFormOptions().isHideCancelButton())
                        || (getFormOptions().isFormNested() && entity.getId() == null));
            }
        }
    }

    /**
     * Sets the "required" status for a field. Convenience method that also
     * correctly handles the situation in which there are multiple fields behind
     * each other on the same row
     *
     * @param propertyName the name of the property
     * @param required     whether the property is required
     */
    public void setFieldRequired(String propertyName, boolean required) {
        AbstractComponent field = getField(isViewMode(), propertyName);
        if (field != null) {

            if (!required) {
                ((AbstractComponent) field).setComponentError(null);
            }

            // if there are multiple fields in a row, we need to some additional trickery
            // since
            // to make sure the "required" asterisk is properly displayed
            AttributeModel am = getEntityModel().getAttributeModel(propertyName);
            if (am != null && !am.getGroupTogetherWith().isEmpty()) {
                Layout parentLayout = (Layout) field.getParent().getParent();
                if (required) {
                    parentLayout.addStyleName(DynamoConstants.CSS_REQUIRED);
                } else {
                    parentLayout.removeStyleName(DynamoConstants.CSS_REQUIRED);
                }
            }
        }
    }

    /**
     * Hides/shows a group of components
     *
     * @param c       the parent component of the group
     * @param visible whether to set the component to visible
     */
    private void setGroupVisible(Object c, boolean visible) {
        if (c != null) {
            if (c instanceof Component) {
                ((Component) c).setVisible(visible);
            } else if (c instanceof Tab) {
                ((Tab) c).setVisible(visible);
            }
        }
    }

    /**
     * Shows or hides a label
     *
     * @param propertyName the name of the property for which to show/hide the label
     * @param visible      whether to show the label
     */
    public void setLabelVisible(String propertyName, boolean visible) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            Component label = labels.get(isViewMode()).get(am);
            if (label != null) {
                VaadinUtils.getParentOfClass(label, ResponsiveRow.class).setVisible(visible);
            }
        }
    }

    public void setNestedMode(boolean nestedMode) {
        this.nestedMode = nestedMode;
    }

    public void setSupportsIteration(boolean supportsIteration) {
        this.supportsIteration = supportsIteration;
    }

    /**
     * Overwrite the value of the title label
     *
     * @param value the desired value
     */
    public void setTitleLabel(String value) {
        titleLabels.get(isViewMode()).setValue(value);
    }

    /**
     * Sets the view mode of the component
     * 
     * @param viewMode the desired view mode
     */
    public void setViewMode(boolean viewMode) {
        setViewMode(viewMode, true);
    }

    /**
     * Switches the form from or to view mode
     *
     * @param viewMode the new view mode
     */
    private void setViewMode(boolean viewMode, boolean checkIterationButtons) {
        boolean oldMode = this.viewMode;

        // check what the new view mode must become and adapt the screen
        this.viewMode = !isEditAllowed() || viewMode;

        groups.get(isViewMode()).setBean(entity);

        build();
        reconstructLabels();

        checkIterationButtonState(checkIterationButtons);

        // if this is the first time in edit mode, post process the editable
        // fields
        if (!isViewMode() && !fieldsProcessed) {
            postProcessEditFields();
            fieldsProcessed = true;
        }

        // update button captions
        updateSaveButtonCaptions();
        disableCreateOnlyFields();

        // preserve tab index when switching
        if (tabSheets.get(oldMode) != null) {
            Component c = tabSheets.get(oldMode).getSelectedTab();
            int index = VaadinUtils.getTabIndex(tabSheets.get(oldMode), tabSheets.get(oldMode).getTab(c).getCaption());
            tabSheets.get(this.viewMode).setSelectedTab(index);

            // focus first field
            if (!isViewMode() && firstFields.get(index) != null) {
                firstFields.get(index).focus();
            }
        } else if (firstFields.get(0) != null) {
            firstFields.get(0).focus();
        }

        resetComponentErrors();
        if (oldMode != this.viewMode) {
            afterModeChanged(isViewMode());
        }
    }

    /**
     * Apply styling to a label
     *
     * @param propertyName the name of the property
     * @param className    the name of the CSS class to add
     */
    public void styleLabel(String propertyName, String className) {
        AttributeModel am = getEntityModel().getAttributeModel(propertyName);
        if (am != null) {
            Component editLabel = labels.get(false) == null ? null : labels.get(false).get(am);
            Component viewLabel = labels.get(true) == null ? null : labels.get(true).get(am);

            if (editLabel != null) {
                editLabel.addStyleName(className);
            }
            if (viewLabel != null) {
                viewLabel.addStyleName(className);
            }
        }
    }

    /**
     * Sets the caption of the save button depending on whether we are creating or
     * updating an entity
     */
    private void updateSaveButtonCaptions() {
        for (Button b : getSaveButtons()) {
            if (entity.getId() != null) {
                b.setCaption(message("ocs.save.existing"));
            } else {
                b.setCaption(message("ocs.save.new"));
            }
        }
    }

    /**
     * Validates all fields and returns true if an error occurs
     *
     * @return
     */
    @Override
    public boolean validateAllFields() {
        boolean error = false;

        BinderValidationStatus<T> status = groups.get(isViewMode()).validate();
        error = !status.isOk();

        // validate nested form and components
        error |= groups.get(isViewMode()).getFields().anyMatch(f -> {
            if (f instanceof NestedComponent) {
                return ((NestedComponent) f).validateAllFields();
            }
            return false;
        });
        return error;
    }

    public int getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(int labelWidth) {
        this.labelWidth = labelWidth;
    }

}
