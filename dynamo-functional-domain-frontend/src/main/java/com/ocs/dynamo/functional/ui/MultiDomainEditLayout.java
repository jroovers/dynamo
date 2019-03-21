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
package com.ocs.dynamo.functional.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.OrPredicate;
import com.ocs.dynamo.filter.SimpleStringPredicate;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.ResponsiveUtil;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout that allows the user to easily manage multiple domains. The list of
 * domain classes can be passed to the constructor. Please note that for every
 * domain class, you must define a (default) service
 *
 * @author bas.rutten
 */
public class MultiDomainEditLayout extends BaseCustomComponent {

    private static final long serialVersionUID = 4410282343830892631L;

    /**
     * The classes of the domains that are managed by this screen
     */
    private final List<Class<? extends Domain>> domainClasses;

    /**
     * Entity model overrides
     */
    private Map<Class<?>, String> entityModelOverrides = new HashMap<>();

    /**
     * The form options (these are passed directly to the split layout)
     */
    private FormOptions formOptions;

    /**
     * The main layout
     */
    private VerticalLayout mainLayout;

    /**
     * The selected domain class
     */
    private Class<? extends Domain> selectedDomain;

    /**
     * The layout that contains the controls for editing the selected domain
     */
    private VerticalLayout selectedDomainLayout;

    /**
     * The split layout that displays the currently selected domain
     */
    private ServiceBasedSplitLayout<?, ?> splitLayout;

    /**
     * Constructor
     *
     * @param formOptions   the form options
     * @param domainClasses the classes of the domains
     */
    public MultiDomainEditLayout(FormOptions formOptions, List<Class<? extends Domain>> domainClasses) {
        this.formOptions = formOptions;
        this.domainClasses = domainClasses;
    }

    /**
     * Callback method that is used after the user changes the selected domain
     */
    public void afterSelectedDomainChanged() {
        // overwrite in subclasses
    }

    /**
     * Adds an entity model override
     *
     * @param clazz     the entity class
     * @param reference the reference to use for the overridden model
     */
    public void addEntityModelOverride(Class<?> clazz, String reference) {
        entityModelOverrides.put(clazz, reference);
    }

    @Override
    public void attach() {
        super.attach();
        build();
    }

    @Override
    public void build() {
        if (mainLayout == null) {

            mainLayout = new DefaultVerticalLayout(true, true);

            // form that contains the combo box
            ResponsiveLayout form = ResponsiveUtil.createPaddedLayout();
            mainLayout.addComponent(form);

            // combo box for selecting domain
            ComboBox<Class<? extends Domain>> domainCombo = new ComboBox<>(message("ocs.select.domain"), getDomainClasses());
            domainCombo.setItemCaptionGenerator(item -> getEntityModel(item).getDisplayName(VaadinUtils.getLocale()));
            domainCombo.setSizeFull();

            // respond to a change by displaying the correct domain
            domainCombo.addValueChangeListener(event -> selectDomain((Class<? extends Domain>) event.getValue()));

            form.addRow().withComponents(domainCombo);

            selectedDomainLayout = new DefaultVerticalLayout();
            mainLayout.addComponent(selectedDomainLayout);

            // select the first domain (if there is any)
            if (!getDomainClasses().isEmpty()) {
                domainCombo.setValue(getDomainClasses().get(0));
            }
            setCompositionRoot(mainLayout);
        }
    }

    /**
     * Constructs a custom field
     * 
     * @param entityModel    the entity model
     * @param attributeModel the attribute mode
     * @param viewMode       whether the screen is in view mode
     * @return
     */
    protected <R extends AbstractEntity<?>> AbstractComponent constructCustomField(EntityModel<R> entityModel,
            AttributeModel attributeModel, boolean viewMode) {
        // overwrite in subclasses
        return null;
    }

    /**
     * Constructs the header layout that is placed above the results grid
     * 
     * @return
     */
    protected Component constructHeaderLayout() {
        // overwrite in subclasses
        return null;
    }

    /**
     * Construct a split layout for a certain domain
     *
     * @param domainClass the class of the domain
     * @param formOptions the form options
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends Domain> ServiceBasedSplitLayout<Integer, T> constructSplitLayout(Class<T> domainClass, FormOptions formOptions) {

        BaseService<Integer, T> baseService = (BaseService<Integer, T>) ServiceLocatorFactory.getServiceLocator()
                .getServiceForEntity(domainClass);
        if (baseService != null) {
            ServiceBasedSplitLayout<Integer, T> layout = new ServiceBasedSplitLayout<Integer, T>(baseService,
                    getEntityModelFactory().getModel(domainClass), QueryType.ID_BASED, formOptions,
                    new SortOrder<String>(Domain.ATTRIBUTE_NAME, SortDirection.ASCENDING)) {

                private static final long serialVersionUID = -6504072714662771230L;

                @Override
                protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
                        boolean viewMode, boolean searchMode) {
                    return MultiDomainEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode);
                }

                @Override
                protected Component constructHeaderLayout() {
                    return MultiDomainEditLayout.this.constructHeaderLayout();
                }

                @Override
                protected boolean isEditAllowed() {
                    return MultiDomainEditLayout.this.isEditAllowed();
                }

                @Override
                protected boolean mustEnableComponent(AbstractComponent component, T selectedItem) {
                    if (getRemoveButton() == component) {
                        return isDeleteAllowed(getSelectedDomain());
                    }
                    return true;
                }

                @Override
                protected void postProcessButtonBar(ResponsiveRow buttonBar) {
                    MultiDomainEditLayout.this.postProcessButtonBar(buttonBar);
                }

                @Override
                protected void postProcessLayout(Layout main) {
                    MultiDomainEditLayout.this.postProcessSplitLayout(main);
                }
            };
            layout.setQuickSearchFilterSupplier(
                    value -> new OrPredicate<>(new SimpleStringPredicate<>(Domain.ATTRIBUTE_NAME, value, true, false),
                            new SimpleStringPredicate<>(Domain.ATTRIBUTE_CODE, value, true, false)));
            return layout;
        } else {
            throw new OCSRuntimeException(message("ocs.no.service.class.found", domainClass));
        }
    }

    public List<Class<? extends Domain>> getDomainClasses() {
        return domainClasses;
    }

    /**
     * Returns the entity model to use for a certain domain class
     *
     * @param domainClass the domain class
     * @return
     */
    private <T> EntityModel<T> getEntityModel(Class<T> domainClass) {
        String override = entityModelOverrides.get(domainClass);
        return override != null ? getEntityModelFactory().getModel(override, domainClass) : getEntityModelFactory().getModel(domainClass);
    }

    /**
     * @return the currently selected domain class
     */
    public Class<? extends Domain> getSelectedDomain() {
        return selectedDomain;
    }

    /**
     * @return the currently selected item
     */
    public Domain getSelectedItem() {
        return (Domain) splitLayout.getSelectedItem();
    }

    /**
     * @return the currently selected split layout
     */
    public ServiceBasedSplitLayout<?, ?> getSplitLayout() {
        return splitLayout;
    }

    /**
     * Check if the deletion of domain values for a certain class is allowed
     *
     * @param clazz the class
     * @return
     */
    protected boolean isDeleteAllowed(Class<?> clazz) {
        return true;
    }

    /**
     * Indicates whether editing is allowed
     */
    protected boolean isEditAllowed() {
        return true;
    }

    /**
     * @param buttonBar
     */
    protected void postProcessButtonBar(ResponsiveRow buttonBar) {
        // overwrite in subclasses
    }

    /**
     * Post processes the split layout after it has been created
     *
     * @param main
     */
    protected void postProcessSplitLayout(Layout main) {
        // overwrite in subclasses
    }

    /**
     * Registers a component. The component will be disabled or enabled depending on
     * whether an item is selected
     *
     * @param button the button to register
     */
    public void registerComponent(AbstractComponent comp) {
        if (splitLayout != null) {
            splitLayout.registerComponent(comp);
        }
    }

    /**
     * Reloads the screen
     */
    public void reload() {
        if (splitLayout != null) {
            splitLayout.reload();
        }
    }

    /**
     * Constructs a layout for editing a certain domain
     *
     * @param clazz the domain class
     */
    public void selectDomain(Class<? extends Domain> clazz) {
        selectedDomain = clazz;
        ServiceBasedSplitLayout<?, ?> layout = constructSplitLayout(clazz, formOptions);
        selectedDomainLayout.replaceComponent(splitLayout, layout);
        splitLayout = layout;
        afterSelectedDomainChanged();
    }
}
