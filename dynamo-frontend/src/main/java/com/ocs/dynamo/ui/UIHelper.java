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
package com.ocs.dynamo.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;

/**
 * Helper class for the UI, mainly concerned with handling navigation and
 * storing state
 * 
 * @author Bas Rutten
 *
 */
public class UIHelper {

    private static final String SELECTED_ENTITY = "selectedEntity";
    
    private static final String SCREEN_MODE = "screenMode";
    
    private static final String SELECTED_TAB = "selectedTab";
    
    /**
     * Mapping for navigating to pages after clicking on a link
     */
    private Map<Class<?>, Consumer<?>> entityOnViewMapping = new HashMap<>();

    /**
     * Adds a mapping for carrying out navigation within the application
     * 
     * @param entityClass    the type of the entity
     * @param navigateAction the action to carry out
     */
    public void addEntityNavigationMapping(Class<?> entityClass, Consumer<?> navigateAction) {
        entityOnViewMapping.put(entityClass, navigateAction);
    }

    public String getScreenMode() {
        return (String) VaadinUtils.getFromSession(SCREEN_MODE);
    }

    public Object getSelectedEntity() {
        return VaadinUtils.getFromSession(SELECTED_ENTITY);
    }

    public Integer getSelectedTab() {
        return (Integer) VaadinUtils.getFromSession(SELECTED_TAB);
    }

    /**
     * Navigates to a view
     * 
     * @param viewName the name of the view to navigate to
     */
    public void navigate(String viewName) {
        UI.getCurrent().navigate(viewName);
    }

    /**
     * Navigate to a screen based on the actual type of parameter o. During
     * initialization of the UI of your project a mapping from type to consumer must
     * have been provided by adding it through the method addEntityOnViewMapping.
     * 
     * @param o The selected object to be displayed on the target screen.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void navigateToEntityScreen(Object o) {
        if (o != null) {
            Consumer navigateToView = entityOnViewMapping.getOrDefault(o.getClass(),
                    err -> Notification.show("No view mapping registered for class: " + o.getClass()));
            if (navigateToView != null) {
                try {
                    navigateToView.accept(o);
                } catch (Exception e) {
                    Notification.show("An exception occurred while executing the mapped action for class: " + o.getClass()
                            + " with message: " + e.getMessage());
                    throw e;
                }
            }
        }
    }

    public void setScreenMode(String screenMode) {
        VaadinUtils.storeInSession(SCREEN_MODE, screenMode);
    }

    public void setSelectedEntity(Object selectedEntity) {
        VaadinUtils.storeInSession(SELECTED_ENTITY, selectedEntity);
    }

    public void setSelectedTab(Integer selectedTab) {
        VaadinUtils.storeInSession(SELECTED_TAB, selectedTab);
    }

}