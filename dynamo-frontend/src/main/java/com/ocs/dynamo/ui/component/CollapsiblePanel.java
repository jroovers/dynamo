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

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontIcon;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;

/**
 * A collapsible panel for grouping attributes
 * 
 * @author Bas Rutten
 *
 */
public class CollapsiblePanel extends ResponsiveLayout {

	private static final long serialVersionUID = -7979238391035057707L;

	private FontIcon closedIcon = VaadinIcons.PLUS_CIRCLE;

	private FontIcon openIcon = VaadinIcons.MINUS_CIRCLE;

	private Button toggle = new Button(openIcon);

	private ResponsiveLayout contentWrapper = new ResponsiveLayout();

	/**
	 * Constructor
	 */
	public CollapsiblePanel() {
		this.setSizeFull();
		toggle.setStyleName(ValoTheme.BUTTON_BORDERLESS + " " + "leftAlign");
		toggle.setSizeFull();

		contentWrapper.setVisible(true);

		ResponsiveUtil.addFullWidthRow(this, toggle);
		ResponsiveUtil.addFullWidthRow(this, contentWrapper);

		toggle.addClickListener(event -> setOpen(!isOpen()));
	}

	public CollapsiblePanel(String caption, Component content) {
		this();
		setCaption(caption);
		contentWrapper.addComponent(content);
	}

	public boolean isOpen() {
		return toggle.getIcon() == openIcon;
	}

	public CollapsiblePanel setOpen(boolean open) {
		contentWrapper.setVisible(open);
		toggle.setIcon(open ? getOpenIcon() : getClosedIcon());
		return this;
	}

	public CollapsiblePanel setContent(Component content) {
		this.contentWrapper.removeAllComponents();
		this.contentWrapper.addRow().withDefaultRules(12, 12, 12, 12).withComponents(content);
		return this;
	}

	@Override
	public void setCaption(String caption) {
		toggle.setCaption(caption);
	}

	public ResponsiveLayout getContentWrapper() {
		return contentWrapper;
	}

	public FontIcon getClosedIcon() {
		return closedIcon;
	}

	public void setClosedIcon(FontIcon closedIcon) {
		this.closedIcon = closedIcon;
	}

	public FontIcon getOpenIcon() {
		return openIcon;
	}

	public void setOpenIcon(FontIcon openIcon) {
		this.openIcon = openIcon;
	}

}
