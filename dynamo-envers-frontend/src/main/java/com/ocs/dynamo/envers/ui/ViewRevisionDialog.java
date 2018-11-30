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
package com.ocs.dynamo.envers.ui;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.envers.domain.RevisionKey;
import com.ocs.dynamo.envers.domain.VersionedEntity;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.dialog.BaseModalDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.ServiceBasedSplitLayout;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * A dialog for viewing the revisions of an entity
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 * @param <U> the type of the revision entity
 */
public class ViewRevisionDialog<ID, T extends AbstractEntity<ID>, U extends VersionedEntity<ID, T>>
		extends BaseModalDialog {

	private static final long serialVersionUID = -8950374678949377884L;

	private static final int PAGE_SIZE = 5;

	private ServiceBasedSplitLayout<RevisionKey<ID>, U> layout;

	private BaseService<RevisionKey<ID>, U> service;

	private EntityModel<U> entityModel;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * The ID of the entity
	 */
	private ID id;

	/**
	 * Constructor
	 * 
	 * @param service     the service used to access the database
	 * @param entityModel the entity model
	 * @param id          the ID of the entity
	 */
	public ViewRevisionDialog(BaseService<RevisionKey<ID>, U> service, EntityModel<U> entityModel, ID id) {
		this.service = service;
		this.entityModel = entityModel;
		this.id = id;
		setStyleName("revisionDialog");
	}

	/**
	 * Constructs a custom field for use in the details form
	 * 
	 * @param entityModel
	 * @param attributeModel
	 * @return
	 */
	protected AbstractComponent constructCustomField(EntityModel<U> entityModel, AttributeModel attributeModel) {
		// override in subclasses
		return null;
	}

	@Override
	protected void doBuild(Layout parent) {
		FormOptions fo = new FormOptions().setReadOnly(true).setScreenMode(ScreenMode.VERTICAL)
				.setAttributeGroupMode(AttributeGroupMode.TABSHEET).setExportAllowed(true);
		layout = new ServiceBasedSplitLayout<RevisionKey<ID>, U>(service, entityModel, QueryType.PAGING, fo, null) {

			private static final long serialVersionUID = -5302678717934028964L;

			@Override
			protected AbstractComponent constructCustomField(EntityModel<U> entityModel, AttributeModel attributeModel,
					boolean viewMode, boolean searchMode) {
				return ViewRevisionDialog.this.constructCustomField(entityModel, attributeModel);
			}

			@Override
			protected SerializablePredicate<U> constructFilter() {
				// always filter on ID
				return new EqualsPredicate<U>(DynamoConstants.ID, id);
			}
		};
		layout.setPageLength(PAGE_SIZE);
		parent.addComponent(layout);
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		Button closeButton = new Button(messageService.getMessage("ocs.close", VaadinUtils.getLocale()));
		closeButton.addClickListener(e -> close());
		buttonBar.addComponent(closeButton);
	}

	@Override
	protected String getTitle() {
		return messageService.getMessage("ocs.revision.history", VaadinUtils.getLocale());
	}

}