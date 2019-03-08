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
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.Converter;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;

/**
 * This factory can be used to generate TranslationTable objects for attributes
 * in fields which have to be dynamically localized (translated). It expects a
 * generic database table based on entity Translation and attributes to be
 * translated mapped to the translation collection in this entity.
 *
 * This class can be used in 2 ways: [1] by hand [2] as a factory delegate as
 * part of the editform.
 *
 * @author patrick.deenen@opencircle.solutions
 *
 * @param <ID> The type for the id of the entity which has translated attributes
 * @param <T> The type which implements the translation for the entity
 */
public class TranslationFieldFactory implements FieldFactory {

	private HashMap<String, AbstractComponent> fields = new HashMap<>();

	private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	/**
	 * Default constructor
	 */
	public TranslationFieldFactory() {
	}

	public void clearFields() {
		fields.clear();
	}

	@Override
	public AbstractComponent constructField(AttributeModel am) {
		return constructField(FieldFactoryContext.createDefault(am));
	}

	@Override
	public AbstractComponent constructField(FieldFactoryContext context) {
		AttributeModel am = context.getAttributeModel();
		if (am.isVisible()) {

			Map<String, SerializablePredicate<?>> fieldFilters = context.getFieldFilters();
			SerializablePredicate<?> fieldFilter = fieldFilters == null ? null : fieldFilters.get(am.getPath());
			if (fieldFilter != null && (AbstractEntityTranslated.class.isAssignableFrom(am.getType())
					|| AbstractEntityTranslated.class.isAssignableFrom(am.getNestedEntityModel().getEntityClass()))) {

				// construct combo box
				EntityModel<?> entityModel = (EntityModel<?>) resolveEntityModel(context.getFieldEntityModel(), am,
						context.isSearch());
				BaseService<?, ?> service = (BaseService<?, ?>) serviceLocator
						.getServiceForEntity(entityModel.getEntityClass());
				// return new TranslatedComboBox<ID, T>(service, entityModel, am,
				// (SerializablePredicate<T>) fieldFilter);
				return null;
			} else if (am.getNestedEntityModel() != null
					&& Translation.class.isAssignableFrom(am.getNestedEntityModel().getEntityClass())
					&& context.getParentEntity() != null) {
				return constructGrid(context, am);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <ID, E extends AbstractEntityTranslated<ID, Translation<E>>> TranslationGrid<ID, E> constructGrid(
			FieldFactoryContext context, AttributeModel am) {
		final EntityModel<Translation<E>> nem = (EntityModel<Translation<E>>) serviceLocator.getEntityModelFactory()
				.getModel(am.getNestedEntityModel().getEntityClass());
		TranslationGrid<ID, E> tt = new TranslationGrid<ID, E>((E) context.getParentEntity(), am.getName(), nem, am,
				context.getViewMode(), am.isLocalesRestricted());
		tt.setRequiredIndicatorVisible(am.isRequired());
		tt.setCaption(am.getDisplayName(VaadinUtils.getLocale()));
		return tt;
	}

	/**
	 * Resolves an entity model by falling back first to the nested attribute model
	 * and then to the default model for the normalized type of the property
	 *
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param search
	 * @return
	 */
	private EntityModel<?> resolveEntityModel(EntityModel<?> entityModel, AttributeModel attributeModel,
			Boolean search) {
		if (entityModel == null) {
			if (!Boolean.TRUE.equals(search) && attributeModel.getNestedEntityModel() != null) {
				entityModel = attributeModel.getNestedEntityModel();
			} else {
				Class<?> type = attributeModel.getNormalizedType();
				entityModel = serviceLocator.getEntityModelFactory().getModel(type.asSubclass(AbstractEntity.class));
			}
		}
		return entityModel;
	}

	@Override
	public <U> void addConvertersAndValidators(BindingBuilder<U, ?> builder, AttributeModel am,
			Converter<String, ?> customConverter) {
		// do nothing
	}
}
