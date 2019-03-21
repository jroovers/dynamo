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
package com.ocs.dynamo.ui.view;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.ui.component.ResponsiveUtil;
/**
 * A base class for a "lazy" view that is only constructed once per UI - data will not be
 * reloaded once the view is opened again
 */
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

/**
 * A view that is only built once per scope
 * 
 * @author Bas Rutten
 *
 */
public abstract class LazyBaseView extends BaseView {

    private static final long serialVersionUID = -2500168085668166838L;

    private ResponsiveLayout lazy = null;

    /**
     * Method that is called when the view is entered - lazily constructs the layout
     */
    @Override
    public final void enter(ViewChangeEvent event) {
        if (lazy == null) {
            lazy = initLayout();
            lazy.setStyleName(DynamoConstants.CSS_LAZY_CONTAINER);
            ResponsiveUtil.addFullWidthRow(lazy, build());
            setCompositionRoot(lazy);
            afterBuild();
            addResizeListener();
        } else {
            refresh();
        }
    }

    /**
     * Constructs the view
     * 
     * @return the parent component of the constructed view
     */
    protected abstract Component build();

    /**
     * Refreshes the screen after it is re-opened
     */
    protected void refresh() {
        // override in subclasses
    }

    /**
     * One-time method to be carried out after the component has been built
     */
    protected void afterBuild() {
        // override in subclasses
    }

}
