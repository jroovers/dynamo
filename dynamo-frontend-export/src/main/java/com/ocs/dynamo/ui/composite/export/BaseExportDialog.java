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

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.component.ResponsiveUtil;
import com.ocs.dynamo.ui.composite.dialog.BaseModalDialog;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;

/**
 * Base class for export dialogs
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public abstract class BaseExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

    private static final long serialVersionUID = 2066899457738401866L;

    protected static final String EXTENSION_CSV = ".csv";

    protected static final String EXTENSION_XLS = ".xlsx";

    private final ExportService exportService;

    private final ExportMode exportMode;

    private final EntityModel<T> entityModel;

    public BaseExportDialog(ExportService exportService, EntityModel<T> entityModel, ExportMode exportMode) {
        this.entityModel = entityModel;
        this.exportService = exportService;
        this.exportMode = exportMode;
    }

    protected abstract Button createDownloadCSVButton();

    protected abstract Button createDownloadExcelButton();

    @Override
    protected void doBuild(ResponsiveLayout parent) {

        ResponsiveRow rr = ResponsiveUtil.createRowWithSpacing();
        parent.addRow(rr);

        Button exportExcelButton = createDownloadExcelButton();
        rr.addColumn().withComponent(exportExcelButton);
        Button exportCsvButton = createDownloadCSVButton();
        rr.addColumn().withComponent(exportCsvButton);
    }

    @Override
    protected void doBuildButtonBar(ResponsiveRow buttonBar) {
        Button cancelButton = new Button(message("ocs.cancel"));
        cancelButton.addClickListener(event -> close());
        cancelButton.setIcon(VaadinIcons.BAN);
        buttonBar.addComponent(cancelButton);
    }

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public ExportMode getExportMode() {
        return exportMode;
    }

    public ExportService getExportService() {
        return exportService;
    }

    @Override
    protected String getTitle() {
        return message("ocs.export");
    }

}
