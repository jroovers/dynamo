package com.ocs.dynamo.ui.component;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.jarektoro.responsivelayout.ResponsiveRow.SpacingSize;
import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.ui.Component;

/**
 * 
 * @author Bas Rutten
 *
 */
public final class ResponsiveUtil {

	/**
	 * Adds a component over the full width of the provided row
	 * 
	 * @param row       the row to add the component to
	 * @param component the component
	 */
	public static void addFullWidthComponent(ResponsiveRow row, Component component) {
		component.setSizeFull();
		row.addColumn().withDisplayRules(DynamoConstants.MAX_COLUMNS, DynamoConstants.MAX_COLUMNS,
				DynamoConstants.MAX_COLUMNS, DynamoConstants.MAX_COLUMNS).withComponent(component);
	}

	/**
	 * Adds a full width row to the provided row, then adds the provided component
	 * to it
	 * 
	 * @param layout    the layout
	 * @param component the component
	 */
	public static void addFullWidthRow(ResponsiveLayout layout, Component component) {
		layout.addRow().withDefaultRules(DynamoConstants.MAX_COLUMNS, DynamoConstants.MAX_COLUMNS,
				DynamoConstants.MAX_COLUMNS, DynamoConstants.MAX_COLUMNS).withComponents(component);
	}

	/**
	 * Creates a responsive row with spacing enabled
	 * 
	 * @return
	 */
	public static ResponsiveRow createRowWithSpacing() {
		return new ResponsiveRow().withSpacing(SpacingSize.SMALL, true);
	}

}
