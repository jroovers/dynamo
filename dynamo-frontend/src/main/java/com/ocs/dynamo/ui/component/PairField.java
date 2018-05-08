package com.ocs.dynamo.ui.component;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;

public class PairField<L, R> extends CustomField<Pair<L, R>> {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(PairField.class);

	private Field<L> left;
	private Field<R> right;
	private Component middle;

	private boolean maskChanges = false;

	public PairField(Field<L> left, Field<R> right) {
		this(left, right, null);
	}

	public PairField(Field<L> left, Field<R> right, Component middle) {
		if (left == null || right == null) {
			throw new IllegalArgumentException();
		}
		this.left = left;
		this.right = right;
		this.middle = middle;
		
		ValueChangeListener listener = e -> {
			synchronized (PairField.this) {
				if (!maskChanges) {
					maskChanges = true;
					setValue(Pair.of(left.getValue(), right.getValue()));
					maskChanges = false;
				}
			}
		};
		left.addValueChangeListener(listener);
		right.addValueChangeListener(listener);
		
		addValueChangeListener(e -> {
			synchronized (PairField.this) {
				if (!maskChanges) {
					maskChanges = true;
					left.setValue(getValue().getLeft());
					right.setValue(getValue().getRight());
					maskChanges = false;
				}
			}
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8008364520978553938L;

	@Override
	protected Component initContent() {
		HorizontalLayout root = new HorizontalLayout();
		root.setHeight(null);
		root.setWidth(null);
		root.addComponent(left);
		if (middle != null) {
			root.addComponent(middle);
		}
		root.addComponent(right);

		return root;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Pair<L, R>> getType() {
		return (Class<Pair<L, R>>) (Class<?>) Pair.class;
	}
}
