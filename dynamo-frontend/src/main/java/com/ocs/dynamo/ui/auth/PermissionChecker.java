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
package com.ocs.dynamo.ui.auth;

import java.util.List;

/**
 * Permission checker for determining which views and menu items are available for which user
 * 
 * @author bas.rutten
 *
 */
public interface PermissionChecker {

    /**
     * Checks if the user is allowed to access a certain view
     * 
     * @param viewName
     * @return
     */
    boolean isAccessAllowed(String viewName);

    /**
     * Returns a list of all view names
     * 
     * @return
     */
    List<String> getViewNames();

    /**
     * @param viewName
     * @return
     */
    boolean isEditOnly(String viewName);
}