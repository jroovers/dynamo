package com.ocs.dynamo.ui.composite.export;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

public class FixedExportDialogTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private UI ui;

    private TestEntity e1;

    private TestEntity e2;

    @Mock
    private TestEntityService service;

    @Mock
    private ExportService exportService;

    private FixedExportDialog<Integer, TestEntity> dialog;

    private EntityModel<TestEntity> model;

    @Override
    public void setUp() {
        super.setUp();
        e1 = new TestEntity(1, "Kevin", 12L);
        e2 = new TestEntity(2, "Bob", 14L);

        Supplier<List<TestEntity>> supplier = () -> Lists.newArrayList(e1, e2);
        model = factory.getModel(TestEntity.class);
        dialog = new FixedExportDialog<>(exportService, model, ExportMode.FULL, null, supplier);
    }

    @Test
    public void testBuild() {

        dialog.build();
        Panel panel = (Panel) dialog.iterator().next();

        ResponsiveLayout layout = (ResponsiveLayout) panel.getContent();
    }
}
