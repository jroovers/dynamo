package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.ui.VerticalLayout;

public class CollapsiblePanelTest {

	@Test
	public void testOpenClose() {
		CollapsiblePanel panel = new CollapsiblePanel("Caption", new VerticalLayout());
		panel.setOpen(true);
		Assert.assertTrue(panel.isOpen());

		panel.setOpen(false);
		Assert.assertFalse(panel.isOpen());
	}

	@Test
	public void testReplaceContent() {
		CollapsiblePanel panel = new CollapsiblePanel("Caption", new VerticalLayout());

		ResponsiveRow v2 = new ResponsiveRow();
		panel.setContent(v2);

		Assert.assertEquals(ResponsiveRow.class, panel.getContentWrapper().iterator().next().getClass());
	}
}
