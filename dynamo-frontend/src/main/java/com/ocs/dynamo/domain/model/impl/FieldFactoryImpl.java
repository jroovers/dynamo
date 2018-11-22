package com.ocs.dynamo.domain.model.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.vaadin.teemu.switchui.Switch;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CheckboxMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.FancyListSelect;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSelect;
import com.ocs.dynamo.ui.component.QuickAddListSingleSelect;
import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
import com.ocs.dynamo.ui.component.TimeField;
import com.ocs.dynamo.ui.component.TokenFieldSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.form.ElementCollectionGrid;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.IntToDoubleConverter;
import com.ocs.dynamo.ui.converter.LocalDateWeekCodeConverter;
import com.ocs.dynamo.ui.converter.LongToDoubleConverter;
import com.ocs.dynamo.ui.converter.ZonedDateTimeToLocalDateTimeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.ui.validator.EmailValidator;
import com.ocs.dynamo.ui.validator.URLValidator;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FieldFactoryImpl<T> implements FieldFactory {

	private static final long serialVersionUID = -5684112523268959448L;

	private static final ConcurrentMap<String, FieldFactoryImpl<?>> nonValidatingInstances = new ConcurrentHashMap<>();

	private static final ConcurrentMap<String, FieldFactoryImpl<?>> searchInstances = new ConcurrentHashMap<>();

	private static final ConcurrentMap<String, FieldFactoryImpl<?>> validatingInstances = new ConcurrentHashMap<>();

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 *
	 * @param model          the entity model
	 * @param messageService the message service
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> FieldFactoryImpl<T> getInstance(final EntityModel<T> model, final MessageService messageService) {
		if (!nonValidatingInstances.containsKey(model.getReference())) {
			nonValidatingInstances.put(model.getReference(),
					new FieldFactoryImpl<>(model, messageService, false, false));
		}
		return (FieldFactoryImpl<T>) nonValidatingInstances.get(model.getReference());
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 *
	 * @param model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> FieldFactoryImpl<T> getSearchInstance(final EntityModel<T> model,
			final MessageService messageService) {
		if (!searchInstances.containsKey(model.getReference())) {
			searchInstances.put(model.getReference(), new FieldFactoryImpl<>(model, messageService, false, true));
		}
		return (FieldFactoryImpl<T>) searchInstances.get(model.getReference());
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 *
	 * @param model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> FieldFactoryImpl<T> getValidatingInstance(final EntityModel<T> model,
			final MessageService messageService) {
		if (!validatingInstances.containsKey(model.getReference())) {
			validatingInstances.put(model.getReference(), new FieldFactoryImpl<>(model, messageService, true, false));
		}
		return (FieldFactoryImpl<T>) validatingInstances.get(model.getReference());
	}

	private final MessageService messageService;

	private final EntityModel<T> model;

	private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	private final Collection<FieldFactory> fieldFactories;

	// indicates whether the system is in search mode. In search mode,
	// components for
	// some attributes are constructed differently (e.g. we render two search
	// fields to be able to
	// search for a range of integers)
	private final boolean search;

	// indicates whether extra validators must be added. This is the case when
	// using the field factory in an
	// editable table
	private final boolean validate;

	/**
	 * Constructor
	 *
	 * @param model          the entity model
	 * @param messageService the message service
	 * @param validate       whether to add extra validators (this is the case when
	 *                       the field is displayed inside a table)
	 * @param search         whether the fields are displayed inside a search form
	 *                       (this has an effect on the construction of some fields)
	 */
	public FieldFactoryImpl(final EntityModel<T> model, final MessageService messageService, final boolean validate,
			final boolean search) {
		this.model = model;
		this.messageService = messageService;
		this.validate = validate;
		this.search = search;
		this.fieldFactories = serviceLocator.getServices(FieldFactory.class);
	}

	/**
	 * Constructs a field for selecting multiple values from a collection
	 * 
	 * @param am               the attribute model
	 * @param fieldEntityModel the entity model
	 * @param fieldFilter      the field filter to apply
	 * @param search           whether the field is in search mode
	 * @param multipleSelect   whether multiple select is allowed
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> AbstractField<?> constructCollectionSelect(
			AttributeModel am, EntityModel<?> fieldEntityModel, SerializablePredicate<?> fieldFilter, boolean search,
			boolean multipleSelect) {
		final EntityModel<?> em = resolveEntityModel(fieldEntityModel, am, search);

		final BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator.getServiceForEntity(em.getEntityClass());
		final SortOrder<?>[] sos = constructSortOrder(em);

		// mode depends on whether we are searching
		final AttributeSelectMode mode = search ? am.getSearchSelectMode() : am.getSelectMode();

		if (AttributeSelectMode.LOOKUP.equals(mode)) {
			// lookup field
			return constructLookupField(am, fieldEntityModel, fieldFilter, search, true);
		} else if (AttributeSelectMode.FANCY_LIST.equals(mode)) {
			// fancy list select
			FancyListSelect<ID, S> listSelect = new FancyListSelect<ID, S>(service, (EntityModel<S>) em, am,
					(SerializablePredicate<S>) fieldFilter, search, sos);
			listSelect.setRows(SystemPropertyUtils.getDefaultListSelectRows());
			return listSelect;
		} else if (AttributeSelectMode.LIST.equals(mode)) {
			// simple list select if everything else fails or is not applicable
			if (multipleSelect || Collection.class.isAssignableFrom(am.getType())) {
				return new QuickAddListSelect<ID, S>((EntityModel<S>) em, am, service,
						(SerializablePredicate<S>) fieldFilter, search, SystemPropertyUtils.getDefaultListSelectRows(),
						sos);
			} else {
				return new QuickAddListSingleSelect<>((EntityModel<S>) em, am, service,
						(SerializablePredicate<S>) fieldFilter, search, SystemPropertyUtils.getDefaultListSelectRows(),
						sos);
			}
		} else {
			// by default, use a token field
			return new TokenFieldSelect<ID, S>((EntityModel<S>) em, am, service, (SerializablePredicate<S>) fieldFilter,
					search, sos);
		}
	}

	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> QuickAddEntityComboBox<ID, S> constructComboBox(
			AttributeModel am, EntityModel<?> entityModel, SerializablePredicate<?> fieldFilter, boolean search) {
		entityModel = resolveEntityModel(entityModel, am, search);
		final BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());
		final SortOrder<?>[] sos = constructSortOrder(entityModel);
		return new QuickAddEntityComboBox<ID, S>((EntityModel<S>) entityModel, am, service, SelectMode.FILTERED,
				(SerializablePredicate<S>) fieldFilter, search, null, sos);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends Enum> ComboBox constructEnumComboBox(final Class<E> enumClass) {
		ComboBox cb = new ComboBox<>();

		// sort on the description
		final List<E> list = Arrays.asList(enumClass.getEnumConstants());
		list.sort((a, b) -> {
			final String msg1 = messageService.getEnumMessage(enumClass, a, VaadinUtils.getLocale());
			final String msg2 = messageService.getEnumMessage(enumClass, b, VaadinUtils.getLocale());
			return msg1.compareToIgnoreCase(msg2);
		});

		// set data provider and caption generator
		cb.setDataProvider(new ListDataProvider<E>(list));
		cb.setItemCaptionGenerator(e -> messageService.getEnumMessage(enumClass, (E) e, VaadinUtils.getLocale()));
		return cb;
	}

	/**
	 * Constructs a field - this is the main method that must be called to construct
	 * a field
	 * 
	 * @param am               the attribute model
	 * @param fieldEntityModel the entity model used for the field
	 * @param fieldFilters     the set of field filters
	 * @return
	 */
	public AbstractComponent constructField(AttributeModel am, EntityModel<?> fieldEntityModel,
			Map<String, SerializablePredicate<?>> fieldFilters, boolean viewMode) {
		AbstractComponent field = null;

		// in certain cases, never render a field
		if (EditableType.READ_ONLY.equals(am.getEditableType())
				&& (!am.isUrl() && !am.isNavigable() && !AttributeType.DETAIL.equals(am.getAttributeType()))
				&& !search) {
			return null;
		}

		SerializablePredicate<?> fieldFilter = fieldFilters == null ? null : fieldFilters.get(am.getPath());

		if (am.isNavigable() && viewMode) {
			// navigable link (note: place this BEFORE checking for an entity or entity
			// collections, the link
			// (in view mode) beats any selection components
			field = constructInternalLinkField(am, fieldEntityModel);
		} else if (AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())) {
			if (!search) {
				// use a "collection table" for an element collection
				final FormOptions fo = new FormOptions().setShowRemoveButton(true);
				if (String.class.equals(am.getMemberType())) {
					ElementCollectionGrid<?, ?, String> grid = new ElementCollectionGrid<>(am, fo);
					field = grid;
				} else if (NumberUtils.isInteger(am.getMemberType())) {
					ElementCollectionGrid<?, ?, Integer> grid = new ElementCollectionGrid<>(am, fo);
					field = grid;
				} else if (NumberUtils.isLong(am.getMemberType())) {
					ElementCollectionGrid<?, ?, Long> grid = new ElementCollectionGrid<>(am, fo);
					field = grid;
				} else {
					// other types not supported for now
					throw new OCSRuntimeException("Element collections of this type are currently not supported");
				}
			} else {
				// TODO: search field
				field = constructSimpleTokenField(fieldEntityModel != null ? fieldEntityModel : am.getEntityModel(), am,
						am.getPath().substring(am.getPath().lastIndexOf('.') + 1), true, null);
			}
		} else if (AbstractEntity.class.isAssignableFrom(am.getType()))

		{
			// lookup or combo field for an entity
			field = constructSelect(am, fieldEntityModel, fieldFilter);
		} else if (Collection.class.isAssignableFrom(am.getType())) {
			// render a multiple select component for a collection
			field = constructCollectionSelect(am, fieldEntityModel, fieldFilter, search, true);
		} else if (AttributeTextFieldMode.TEXTAREA.equals(am.getTextFieldMode()) && !search) {
			field = new TextArea();
		} else if (Enum.class.isAssignableFrom(am.getType())) {
			field = constructEnumComboBox(am.getType().asSubclass(Enum.class));
		} else if (search && (am.getType().equals(Boolean.class) || am.getType().equals(boolean.class))) {
			// in a search screen, we need to offer the true, false, and
			// undefined options
			field = constructSearchBooleanComboBox(am);
		} else if (Boolean.class.equals(am.getType()) || boolean.class.equals(am.getType())) {
			// regular boolean (not search mode)
			if (CheckboxMode.SWITCH.equals(am.getCheckboxMode())) {
				field = new Switch();
			} else {
				field = new CheckBox();
			}
		} else if (am.isWeek()) {
			// special case - week field in a table
			field = new TextField();
		} else if (search && AttributeSelectMode.TOKEN.equals(am.getSearchSelectMode())
				&& AttributeType.BASIC.equals(am.getAttributeType())) {
			// token field for searching distinct values
			field = constructSimpleTokenField(fieldEntityModel != null ? fieldEntityModel : am.getEntityModel(), am,
					am.getPath().substring(am.getPath().lastIndexOf('.') + 1), false, null);
		} else if ((NumberUtils.isLong(am.getType()) || NumberUtils.isInteger(am.getType())
				|| BigDecimal.class.equals(am.getType())) && NumberSelectMode.SLIDER.equals(am.getNumberSelectMode())) {
			final Slider slider = new Slider(am.getDisplayName());

			if (am.getMinValue() != null) {
				slider.setMin(am.getMinValue());
			}
			if (am.getMaxValue() != null) {
				slider.setMax(am.getMaxValue());
			}
			field = slider;
		} else if (LocalDateTime.class.equals(am.getType()) || ZonedDateTime.class.equals(am.getType())) {
			DateTimeField df = new DateTimeField();
			df.setDateFormat(am.getDisplayFormat());
			field = df;
		} else if (LocalDate.class.equals(am.getType())) {
			// date field
			DateField df = new DateField();
			df.setDateFormat(am.getDisplayFormat());
			field = df;
		} else if (LocalTime.class.equals(am.getType())) {
			TimeField tf = new TimeField(am);
			field = tf;
		} else if (String.class.equals(am.getType()) || NumberUtils.isNumeric(am.getType())) {
			if (am.isUrl()) {
				final TextField textField = new TextField();
				textField.setSizeFull();
				field = new URLField(textField, am, false);
			} else {
				field = new TextField();
			}
		}

		if (field != null) {
			postProcessField(field, am);
		}

		return field;
	}

	@Override
	public AbstractField<?> constructField(Context<?> context) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Constructs an internal link field
	 * 
	 * @param am
	 * @param entityModel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> AbstractField<?> constructInternalLinkField(
			AttributeModel am, EntityModel<?> entityModel) {
		EntityModel<?> em = resolveEntityModel(entityModel, am, true);
		return new InternalLinkField<>(am, (EntityModel<S>) em, null);
	}

	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> EntityLookupField<ID, S> constructLookupField(
			AttributeModel am, EntityModel<?> overruled, SerializablePredicate<?> fieldFilter, boolean search,
			boolean multiSelect) {

		// for a lookup field, don't use the nested model but the base model -
		// this is
		// because the search in the pop-up screen is conducted on a "clean",
		// unnested entity list so
		// using a path from the parent entity makes no sense here
		final EntityModel<?> entityModel = overruled != null ? overruled
				: serviceLocator.getEntityModelFactory().getModel(am.getNormalizedType());
		final BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(am.getMemberType() != null ? am.getMemberType() : entityModel.getEntityClass());
		final SortOrder<?>[] sos = constructSortOrder(entityModel);
		return new EntityLookupField<ID, S>(service, (EntityModel<S>) entityModel, am,
				(SerializablePredicate<S>) fieldFilter, search, multiSelect,
				sos.length == 0 ? null : Lists.newArrayList(sos));
	}

	/**
	 * Constructs a combo box for filtering on a boolean
	 * 
	 * @param am the attribute model
	 * @return
	 */
	private ComboBox<Boolean> constructSearchBooleanComboBox(final AttributeModel am) {
		final ComboBox<Boolean> cb = new ComboBox<Boolean>();

		ListDataProvider<Boolean> provider = new ListDataProvider<>(Lists.newArrayList(Boolean.TRUE, Boolean.FALSE));
		cb.setDataProvider(provider);
		cb.setItemCaptionGenerator(
				b -> Boolean.TRUE.equals(b) ? am.getTrueRepresentation() : am.getFalseRepresentation());
		return cb;
	}

	private AbstractComponent constructSelect(final AttributeModel am, final EntityModel<?> fieldEntityModel,
			final SerializablePredicate<?> fieldFilter) {
		AbstractComponent field = null;
		AttributeSelectMode selectMode = search ? am.getSearchSelectMode() : am.getSelectMode();

		if (search && am.isMultipleSearch()) {
			// in case of multiple search, defer to the
			// "constructCollectionSelect" method
			field = this.constructCollectionSelect(am, fieldEntityModel, fieldFilter, search, true);
		} else if (AttributeSelectMode.COMBO.equals(selectMode)) {
			// combo box
			field = constructComboBox(am, fieldEntityModel, fieldFilter, search);
		} else if (AttributeSelectMode.LOOKUP.equals(selectMode)) {
			// single select lookup field
			field = constructLookupField(am, fieldEntityModel, fieldFilter, search, false);
		} else {
			// list select (single select)
			field = this.constructCollectionSelect(am, fieldEntityModel, fieldFilter, search, false);
		}
		return field;
	}

	/**
	 * Constructs a field for looking up simple values (Strings, ints) from a table
	 * field or a collection table
	 * 
	 * @param entityModel       the entity model
	 * @param am                the attribute model
	 * @param distinctField
	 * @param elementCollection
	 * @param fieldFilter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>, O extends Comparable<O>> SimpleTokenFieldSelect<ID, S, O> constructSimpleTokenField(
			EntityModel<?> entityModel, AttributeModel am, String distinctField, boolean elementCollection,
			SerializablePredicate<S> fieldFilter) {
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());
		return new SimpleTokenFieldSelect<ID, S, O>(service, (EntityModel<S>) entityModel, am, fieldFilter,
				distinctField, (Class<O>) am.getNormalizedType(), elementCollection);
	}

	@SuppressWarnings("unchecked")
	private SortOrder<String>[] constructSortOrder(final EntityModel<?> entityModel) {
		final SortOrder<String>[] sos = new SortOrder[entityModel.getSortOrder().size()];
		int i = 0;
		for (final AttributeModel am : entityModel.getSortOrder().keySet()) {
			sos[i++] = new SortOrder<>(am.getName(),
					entityModel.getSortOrder().get(am) ? SortDirection.ASCENDING : SortDirection.DESCENDING);
		}
		return sos;
	}

	private void postProcessField(final AbstractComponent field, final AttributeModel am) {
		field.setCaption(am.getDisplayName());
		field.setDescription(am.getDescription());

		if (field instanceof AbstractTextField) {
			final AbstractTextField textField = (AbstractTextField) field;
			textField.setDescription(am.getDescription());
		} else if (field instanceof DateField) {
			// set a separate format for a date field
			DateField dateField = (DateField) field;
			if (am.getDisplayFormat() != null) {
				dateField.setDateFormat(am.getDisplayFormat());
			}
		}

		if (field instanceof AbstractField) {
			AbstractField<?> af = (AbstractField<?>) field;
			af.setRequiredIndicatorVisible(search ? am.isRequiredForSearching() : am.isRequired());
		}

	}

	/**
	 * 
	 * @param entityModel
	 * @param am
	 * @param search
	 * @return
	 */
	private EntityModel<?> resolveEntityModel(EntityModel<?> entityModel, final AttributeModel am, Boolean search) {
		if (entityModel == null) {
			if (!Boolean.TRUE.equals(search) && am.getNestedEntityModel() != null) {
				entityModel = am.getNestedEntityModel();
			} else {
				final Class<?> type = am.getNormalizedType();
				entityModel = serviceLocator.getEntityModelFactory().getModel(type.asSubclass(AbstractEntity.class));
			}
		}
		return entityModel;
	}

	@SuppressWarnings("unchecked")
	public static <U> void addConvertsAndValidators(BindingBuilder<U, ?> builder, AttributeModel am) {
		MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

		if (am.isEmail()) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			sBuilder.withNullRepresentation("").withValidator(
					new EmailValidator(messageService.getMessage("ocs.no.valid.email", VaadinUtils.getLocale())));
		} else if (am.isWeek()) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			sBuilder.withConverter(new LocalDateWeekCodeConverter());
		} else if (builder.getField() instanceof AbstractTextField) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			sBuilder.withNullRepresentation("");
			if (am.getType().equals(BigDecimal.class)) {
				sBuilder.withConverter(ConverterFactory.createBigDecimalConverter(am.isCurrency(), am.isPercentage(),
						SystemPropertyUtils.useThousandsGroupingInEditMode(), am.getPrecision(),
						VaadinUtils.getCurrencySymbol()));
			} else if (NumberUtils.isInteger(am.getType())) {
				sBuilder.withConverter(ConverterFactory.createIntegerConverter(
						SystemPropertyUtils.useThousandsGroupingInEditMode(), am.isPercentage()));
			} else if (NumberUtils.isLong(am.getType())) {
				sBuilder.withConverter(ConverterFactory
						.createLongConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), am.isPercentage()));
			}
		} else if (builder.getField() instanceof Slider) {
			BindingBuilder<U, Double> sBuilder = (BindingBuilder<U, Double>) builder;
			sBuilder.withNullRepresentation(0.0);
			if (am.getType().equals(Integer.class)) {
				sBuilder.withConverter(new IntToDoubleConverter());
			} else if (am.getType().equals(Long.class)) {
				sBuilder.withConverter(new LongToDoubleConverter());
			}
		} else if (builder.getField() instanceof URLField) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			sBuilder.withNullRepresentation("").withValidator(
					new URLValidator(messageService.getMessage("ocs.no.valid.url", VaadinUtils.getLocale())));
		} else if (builder.getField() instanceof DateTimeField && ZonedDateTime.class.equals(am.getType())) {
			BindingBuilder<U, LocalDateTime> sBuilder = (BindingBuilder<U, LocalDateTime>) builder;
			sBuilder.withConverter(new ZonedDateTimeToLocalDateTimeConverter(ZoneId.systemDefault()));
		}
	}
}