package com.ocs.dynamo.ui.composite.export;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ExportDialogTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private UI ui;

	@Mock
	private TestEntityService service;

	@Mock
	private ExportService exportService;

	private ExportDialog<Integer, TestEntity> dialog;

	private EntityModel<TestEntity> model;

	@Before
	public void setUp() {
		model = factory.getModel(TestEntity.class);
		dialog = new ExportDialog<>(exportService, model, ExportMode.FULL,
				new EqualsPredicate<TestEntity>("name", "Bob"), null, null);
	}

	@Test
	public void testBuild() {

		dialog.build();
		Panel panel = (Panel) dialog.iterator().next();

		VerticalLayout layout = (VerticalLayout) panel.getContent();
		Iterator<Component> it = layout.iterator();

		Component button1 = it.next();
		Assert.assertTrue(button1 instanceof DownloadButton);

		Component button2 = it.next();
		Assert.assertTrue(button1 instanceof DownloadButton);
		((DownloadButton) button2).click();
	}
}