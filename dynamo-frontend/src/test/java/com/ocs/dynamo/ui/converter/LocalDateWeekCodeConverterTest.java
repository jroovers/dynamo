package com.ocs.dynamo.ui.converter;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

public class LocalDateWeekCodeConverterTest {

	private LocalDateWeekCodeConverter converter = new LocalDateWeekCodeConverter();

	@Test
	public void testToModel() {
		Result<LocalDate> date = converter.convertToModel(null, new ValueContext());
		Assert.assertNull(date);

		date = converter.convertToModel("2014-52", new ValueContext());
		Assert.assertEquals(DateUtils.createLocalDate("22122014"), date.getOrThrow(r -> new OCSRuntimeException()));

		date = converter.convertToModel("2015-01", new ValueContext());
		Assert.assertEquals(DateUtils.createLocalDate("29122014"), date.getOrThrow(r -> new OCSRuntimeException()));

		date = converter.convertToModel("2015-02", new ValueContext());
		Assert.assertEquals(DateUtils.createLocalDate("05012015"), date.getOrThrow(r -> new OCSRuntimeException()));

		date = converter.convertToModel("2015-52", new ValueContext());
		Assert.assertEquals(DateUtils.createLocalDate("21122015"), date.getOrThrow(r -> new OCSRuntimeException()));

		date = converter.convertToModel("2015-53", new ValueContext());
		Assert.assertEquals(DateUtils.createLocalDate("28122015"), date.getOrThrow(r -> new OCSRuntimeException()));

		date = converter.convertToModel("2016-01", new ValueContext());
		Assert.assertEquals(DateUtils.createLocalDate("04012016"), date.getOrThrow(r -> new OCSRuntimeException()));
	}

	@Test
	public void testToPresentation() {
		String str = converter.convertToPresentation(null, new ValueContext());
		Assert.assertNull(str);

		str = converter.convertToPresentation(DateUtils.createLocalDate("22122014"), new ValueContext());
		Assert.assertEquals("2014-52", str);

		str = converter.convertToPresentation(DateUtils.createLocalDate("29122014"), new ValueContext());
		Assert.assertEquals("2015-01", str);

		str = converter.convertToPresentation(DateUtils.createLocalDate("05012015"), new ValueContext());
		Assert.assertEquals("2015-02", str);
	}
}
