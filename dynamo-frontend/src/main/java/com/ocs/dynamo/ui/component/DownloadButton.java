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
package com.ocs.dynamo.ui.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.ui.Button;

/**
 * A button that starts a file download process when clicked
 * 
 * @author bas.rutten
 */
public class DownloadButton extends Button {

	private static final long serialVersionUID = -7163648327567831406L;

	private StreamResource resource;

	private FileDownloader downloader;

	private Supplier<InputStream> createContent;

	private Supplier<String> createFileName;

	/**
	 * Constructor
	 * 
	 * @param caption the caption of the button
	 */
	public DownloadButton(String caption, Supplier<InputStream> createContent, Supplier<String> createFileName) {
		super(caption);
		this.createContent = createContent;
		this.createFileName = createFileName;

		resource = new StreamResource(new StreamSource() {

			private static final long serialVersionUID = -4870779918745663459L;

			@Override
			public InputStream getStream() {
				return createContent.get();
			}

		}, createFileName.get());

		downloader = new FileDownloader(resource) {

			private static final long serialVersionUID = -5072481083052841701L;

			public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path)
					throws IOException {
				((StreamResource) getFileDownloadResource()).setFilename(createFileName.get());
				return super.handleConnectorRequest(request, response, path);
			}
		};
		downloader.extend(this);
	}

	/**
	 * 
	 */
	public void update() {

		downloader.setFileDownloadResource(new StreamResource(new StreamSource() {

			private static final long serialVersionUID = -4870779918745663459L;

			@Override
			public InputStream getStream() {
				return createContent.get();
			}

		}, createFileName.get()));
	}

}
