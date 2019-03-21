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
package com.ocs.dynamo.ui.composite.form.process;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.ocs.dynamo.ui.component.ResponsiveUtil;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

/**
 * A form that contains a file upload component and a progress bar
 * 
 * @author bas.rutten
 */
public abstract class UploadForm extends ProgressForm<byte[]> {

    /**
     * Callback object for handling a file upload
     * 
     * @author bas.rutten
     */
    private class UploadReceiver implements SucceededListener, Receiver {

        private static final long serialVersionUID = -8672072143565385035L;

        private ByteArrayOutputStream stream;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            stream = new ByteArrayOutputStream();
            return stream;
        }

        @Override
        public void uploadSucceeded(final SucceededEvent event) {
            final byte[] bytes = stream.toByteArray();
            if (bytes != null && bytes.length > 0) {
                UploadForm.this.fileName = event.getFilename();
                startWork(bytes);
            } else {
                showNotification(message("ocs.no.file.selected"), Notification.Type.ERROR_MESSAGE);
            }
        }
    }

    private static final long serialVersionUID = -4717815709838453902L;

    private String fileName;

    private boolean showCancelButton;

    private Upload upload;

    /**
     * Constructor
     * 
     * @param progressMode     the desired progress mode
     * @param screenMode       the desired screen mode
     * @param showCancelButton whether to include a cancel button
     */
    public UploadForm(UI ui, ProgressMode progressMode, boolean showCancelButton) {
        super(ui, progressMode);
        this.showCancelButton = showCancelButton;
    }

    /**
     * The method that is executed after the cancel button is clicked
     */
    protected void cancel() {
        // override in subclass if needed
    }

    /**
     * Constructs the screen-specific form content
     * 
     * @param layout
     */
    protected void doBuildForm(ResponsiveLayout layout) {
        // override in subclass
    }

    @Override
    protected void doBuildLayout(ResponsiveLayout main) {

        ResponsiveLayout form = ResponsiveUtil.createPaddedLayout();
        main.addComponent(form);

        // add custom components
        doBuildForm(form);

        // add file upload field
        UploadReceiver receiver = new UploadReceiver();

        upload = new Upload(message("ocs.uploadform.title"), receiver);

        upload.addSucceededListener(receiver);
        ResponsiveRow row = ResponsiveUtil.createRowWithSpacing();
        form.addRow(row).withComponents(upload);

        if (showCancelButton) {
            Button cancelButton = new Button(message("ocs.cancel"));
            cancelButton.addClickListener((Button.ClickListener) event -> cancel());
            ResponsiveRow row2 = ResponsiveUtil.createRowWithSpacing();
            form.addRow(row2).withComponents(cancelButton);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public Upload getUpload() {
        return upload;
    }

}
