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
package com.ocs.dynamo.ui.composite.export;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.UI;

/**
 * Delegate for handling exports. This service is responsibly for displaying an
 * export dialog and performing the actual export as well
 * 
 * @author Bas Rutten
 *
 */
public interface ExportDelegate {

    /**
     * Exports a non-fixed set of data (e.g. the data in a grid)
     * 
     * @param ui          the Vaadin UI
     * @param entityModel the entity model of the entity that is being exported
     * @param mode        the export mode
     * @param predicate   filter predicate to limit the results
     * @param sortOrders  sort orders to used to order the results
     * @param joins the fetch joins to apply when fetching data
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> void export(UI ui, EntityModel<T> entityModel, ExportMode mode,
            SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders, FetchJoinInformation... joins);

    /**
     * Exports a fixed set of data
     * 
     * @param ui          the Vaadin UI
     * @param entityModel the entity model
     * @param mode        the export mode
     * @param items       the entities to export
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> void exportFixed(UI ui, EntityModel<T> entityModel, ExportMode mode,
            Collection<T> items);

}
