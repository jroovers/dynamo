package com.ocs.dynamo.ui.validator;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

public class URLValidatorTest {

	@Test
	public void testFalse1() {
		URLValidator validator = new URLValidator("Not a valid URL");
		ValidationResult result = validator.apply("test", new ValueContext());
		Assert.assertTrue(result.isError());
	}

	@Test()
	public void testFalse2() {
		URLValidator validator = new URLValidator("Not a valid URL");
		ValidationResult result = validator.apply("44", new ValueContext());
		Assert.assertTrue(result.isError());
	}

	@Test
	public void testCorrect1() {
		URLValidator validator = new URLValidator("Not a valid URL");
		ValidationResult result = validator.apply("http://www.google.nl", new ValueContext());
		Assert.assertFalse(result.isError());
	}

	@Test
	public void testCorrect2() {
		URLValidator validator = new URLValidator("Not a valid URL");
		ValidationResult result = validator.apply("www.google.nl", new ValueContext());
		Assert.assertFalse(result.isError());
	}

	@Test
	public void testCorrect3() {
		URLValidator validator = new URLValidator("Not a valid URL");
		ValidationResult result = validator.apply("mijn.site.nl", new ValueContext());
		Assert.assertFalse(result.isError());
	}
}
