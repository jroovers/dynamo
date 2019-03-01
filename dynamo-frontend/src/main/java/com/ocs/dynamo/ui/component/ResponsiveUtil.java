package com.ocs.dynamo.ui.component;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.jarektoro.responsivelayout.ResponsiveRow.SpacingSize;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
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

	public static ResponsiveRow createButtonBar() {
		return createRowWithSpacing().withStyleName(DynamoConstants.CSS_DYNAMO_BUTTON_BAR);
	}

	/**
	 * Creates a responsive row with spacing enabled
	 * 
	 * @return
	 */
	public static ResponsiveRow createRowWithSpacing() {
		return new ResponsiveRow().withSpacing(SpacingSize.SMALL, true);
	}

	/**
	 * Creates a row with spacing and with the provided style name
	 * 
	 * @param styleName the style name
	 * @return
	 */
	public static ResponsiveRow createRowWithStyle(String styleName) {
		return createRowWithSpacing().withStyleName(styleName);
	}

	/**
	 * Creates the CSS ID for an auxiliary search field
	 * 
	 * @param am
	 * @return
	 */
	public static String getAuxId(AttributeModel am) {
		return getId(am) + "_aux";
	}

	/**
	 * Creates the CSS ID for a field
	 * 
	 * @param am
	 * @return
	 */
	public static String getId(AttributeModel am) {
		return am.getPath().replace('.', '_');
	}

}
