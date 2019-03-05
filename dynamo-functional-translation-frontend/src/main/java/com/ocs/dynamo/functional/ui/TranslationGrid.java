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

import java.util.Collection;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.component.DetailsEditGrid;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;

/**
 * Grid component for managing the translations for a single field
 * 
 * @author Bas Rutten
 *
 * @param <ID> the ID of the translation entity
 * @param <E> the type of the translation entity
 */
public class TranslationGrid<ID, E extends AbstractEntityTranslated<ID, Translation<E>>>
		extends DetailsEditGrid<Integer, Translation<E>> implements CanAssignEntity<ID, E> {

	private static final long serialVersionUID = 4974840467576193534L;

	/**
	 * The parent entity
	 */
	private E entity;

	/**
	 * The field name for which the translations are added
	 */
	private String fieldName;

	/**
	 * Whether the addition of new values is disabled
	 */
	private boolean localesRestricted;

	@SuppressWarnings("unchecked")
	private BaseService<Integer, Translation<E>> translationService = (BaseService<Integer, Translation<E>>) ServiceLocatorFactory
			.getServiceLocator().getServiceForEntity(Translation.class);

	public TranslationGrid(E entity, String fieldName, EntityModel<Translation<E>> entityModel,
			AttributeModel attributeModel, boolean viewMode, boolean localesRestricted) {
		super(entityModel, attributeModel, viewMode,
				new FormOptions().setHideAddButton(localesRestricted).setShowRemoveButton(!localesRestricted));
		this.entity = entity;
		this.fieldName = fieldName;
		this.localesRestricted = localesRestricted;
		setCreateEntitySupplier(() -> {
			Translation<E> translation;
			try {
				translation = getEntityModel().getEntityClass().newInstance();
				translation.setField(fieldName);
				entity.addTranslation(translation);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new OCSRuntimeException("Could not create translation", e);
			}
			return translation;
		});
		setRemoveEntityConsumer(toRemove -> {
			// No need to remove the entity from the database if it has not been saved yet
			// (no id)
			if (toRemove.getId() != null) {
				translationService.delete(toRemove);
			}
			entity.removeTranslation(toRemove);
		});
	}

	@Override
	protected Component initContent() {
		Component result = super.initContent();
		getGrid().setUpdateCaption(false);
		getGrid().setCaption(null);
		return result;
	}

	@Override
	public void assignEntity(E entity) {
		this.entity = entity;
	}

	@Override
	protected void postProcessComponent(AttributeModel am, AbstractComponent comp) {
		if (am.getPath().equals("locale") && localesRestricted) {
			comp.setEnabled(false);
		}
	}

	@Override
	protected AbstractComponent constructCustomField(EntityModel<Translation<E>> entityModel,
			AttributeModel attributeModel, boolean viewMode) {
		final Collection<String> textAreaFields = entity.getTextAreaFields();
		if (textAreaFields.contains(fieldName) && attributeModel.getName().equals("translation")) {
			return new TextArea();
		}
		return null;
	}
}
