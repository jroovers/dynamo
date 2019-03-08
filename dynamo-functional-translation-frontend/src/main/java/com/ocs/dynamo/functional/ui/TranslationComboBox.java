package com.ocs.dynamo.functional.ui;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.EntityComboBox;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;

/**
 * A combo box for entities that support multiple translations
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 * @param <E> the type of the translation
 */
public class TranslationComboBox<ID extends Serializable, T extends AbstractEntityTranslated<ID, E>, E extends Translation<T>>
		extends EntityComboBox<ID, T> {

	private static final long serialVersionUID = 4403245109020319295L;

	public TranslationComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
			BaseService<ID, T> service, SelectMode mode, SerializablePredicate<T> filter,
			ListDataProvider<T> sharedProvider, List<T> items, SortOrder<?>... sortOrders) {
		super(targetEntityModel, attributeModel, service, mode, filter, sharedProvider, items, sortOrders);
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		Locale locale = VaadinUtils.getLocale();
		com.ocs.dynamo.functional.domain.Locale loc = new com.ocs.dynamo.functional.domain.Locale();
		loc.setCode(locale.toString());
		this.setItemCaptionGenerator(t -> {
			E e = t.getTranslations(getAttributeModel().getPath(), loc);
			return e.getTranslation();
		});

		super.refresh(filter);
	}

	@Override
	public void refresh() {
		Locale locale = VaadinUtils.getLocale();
		com.ocs.dynamo.functional.domain.Locale loc = new com.ocs.dynamo.functional.domain.Locale();
		loc.setCode(locale.toString());
		this.setItemCaptionGenerator(t -> {
			E e = t.getTranslations(getAttributeModel().getPath(), loc);
			return e == null ? null : e.getTranslation();
		});

		super.refresh();
	}

}
