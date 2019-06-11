package com.ocs.dynamo.ui.composite.form;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.layout.CompositionLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.vaadin.ui.Layout;

public class CompositionLayoutTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService testEntityService;

	private SimpleEditLayout<Integer, TestEntity> nested1;

	@Before
	public void setUp() {
		Mockito.when(testEntityService.getEntityClass()).thenReturn(TestEntity.class);

	}

	@Test
	public void testAssignEntity() {
		TestEntity t1 = new TestEntity();
		t1.setId(43);
		CompositionLayout<Integer, TestEntity> layout = new CompositionLayout<Integer, TestEntity>(t1) {

			private static final long serialVersionUID = 5637048893987681686L;

			@Override
			protected void doBuildLayout(Layout main) {
				nested1 = new SimpleEditLayout<Integer, TestEntity>(getEntity(), testEntityService,
						factory.getModel(TestEntity.class), new FormOptions());
				nested1.build();
				addNestedComponent(nested1);
			}

		};
		layout.build();

		TestEntity t2 = new TestEntity();
		t2.setId(44);
		Mockito.when(testEntityService.fetchById(44)).thenReturn(t2);

		layout.assignEntity(t2);
		layout.reload();

		Assert.assertEquals(t2, nested1.getEntity());
	}
}
