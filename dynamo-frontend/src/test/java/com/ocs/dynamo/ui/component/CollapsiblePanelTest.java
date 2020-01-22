package com.ocs.dynamo.ui.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CollapsiblePanelTest {

    @Test
    public void testOpenClose() {
        CollapsiblePanel panel = new CollapsiblePanel("Caption", new VerticalLayout());
        panel.setOpen(true);
        assertTrue(panel.isOpen());

        panel.setOpen(false);
        assertFalse(panel.isOpen());
    }

    @Test
    public void testReplaceContent() {
        CollapsiblePanel panel = new CollapsiblePanel("Caption", new VerticalLayout());

        VerticalLayout v2 = new VerticalLayout();
        panel.setContent(v2);

        assertEquals(v2, panel.getContentWrapper().getChildren().iterator().next());
    }
}
