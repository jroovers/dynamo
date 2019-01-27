package com.ocs.dynamo.ui;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.menu.MenuService;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Panel;

public class BaseUITest extends BaseMockitoTest {

	private BaseUI ui = new BaseUI() {

		private static final long serialVersionUID = -741712405722333410L;

		@Override
		protected void init(VaadinRequest request) {

		}
	};

	@Mock
	private MenuService menuService;

	@Mock
	private ViewProvider viewProvider;

	@Mock
	private View view;

	private boolean navigated = false;

	@Override
	public void setUp() {
		super.setUp();
		wireTestSubject(ui);
	}

	/**
	 * Try navigating while there is no view defined
	 */
	@Test(expected = NullPointerException.class)
	public void testNavigateToEntityScreenNoViewDefined() {
		ui.navigateToEntityScreen(new TestEntity());
	}

	/**
	 * Try correct navigation
	 */
	@Test
	public void testNavigateToEntityScreen() {
		ui.addEntityNavigationMapping(TestEntity.class, a -> {
			navigated = true;
		});

		ui.navigateToEntityScreen(null);
		ui.navigateToEntityScreen(new TestEntity());
		Assert.assertTrue(navigated);
	}

	@Test(expected = NullPointerException.class)
	public void testNavigateToEntityScreenException() {
		ui.addEntityNavigationMapping(TestEntity.class, a -> {
			throw new OCSRuntimeException();
		});

		ui.navigateToEntityScreen(null);
		ui.navigateToEntityScreen(new TestEntity());
		Assert.assertTrue(navigated);
	}

	@Test(expected = NullPointerException.class)
	public void testInitNavigation() {
		Mockito.when(viewProvider.getView(Mockito.anyString())).thenReturn(view);
		Mockito.when(viewProvider.getViewName(Mockito.anyString())).thenReturn("StartView");
		ui.initNavigation(viewProvider, new Panel(), "StartView", true);
	}
}
