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
package com.ocs.dynamo.ui.composite.export.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.XlsStyleGenerator;
import com.ocs.dynamo.ui.composite.layout.ExportMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.MathUtils;

/**
 * Template for exporting a data set to Excel based on the Entity model
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public class ModelBasedExcelExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseExportTemplate<ID, T> {

	private XlsStyleGenerator<ID, T> generator;

	private CustomXlsStyleGenerator<ID, T> customGenerator;

	private Workbook workbook;

	/**
	 * Constructor
	 *
	 * @param service         the service used to retrieve the data
	 * @param entityModel     the entity model
	 * @param sortOrders      any sort orders to apply to the data
	 * @param filter          the filter that is used to retrieve the appropriate
	 *                        data
	 * @param title           the title of the sheet
	 * @param customGenerator custom style generator
	 * @param joins
	 */
	public ModelBasedExcelExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode mode,
			SortOrder[] sortOrders, Filter filter, String title, CustomXlsStyleGenerator<ID, T> customGenerator,
			FetchJoinInformation... joins) {
		super(service, entityModel, mode, sortOrders, filter, title, joins);
		this.customGenerator = customGenerator;
	}

	/**
	 * Indicates whether it is possible to resize the columns
	 *
	 * @return
	 */
	protected boolean canResize() {
		return !(getWorkbook() instanceof SXSSFWorkbook);
	}

	/**
	 * Creates the style generator
	 *
	 * @param workbook the work book that is being created
	 * @return
	 */
	protected XlsStyleGenerator<ID, T> createGenerator(Workbook workbook) {
		return new BaseXlsStyleGenerator<>(workbook);
	}

	protected Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	@Override
	protected byte[] generate(DataSetIterator<ID, T> iterator) throws IOException {
		setWorkbook(createWorkbook(iterator.size()));
		Sheet sheet = getWorkbook().createSheet(getTitle());
		setGenerator(createGenerator(getWorkbook()));

		boolean resize = canResize();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		// add header row
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(TITLE_ROW_HEIGHT);

		int i = 0;
		for (AttributeModel am : getEntityModel().getAttributeModels()) {
			if (show(am)) {
				if (!resize) {
					sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
				}
				Cell cell = titleRow.createCell(i);
				cell.setCellStyle(getGenerator().getHeaderStyle(i));
				cell.setCellValue(am.getDisplayName());
				i++;
			}
		}

		// iterate over the rows
		int rowIndex = 1;
		T entity = iterator.next();
		while (entity != null) {
			Row row = sheet.createRow(rowIndex);
			int colIndex = 0;
			for (AttributeModel am : getEntityModel().getAttributeModels()) {
				if (am != null && show(am)) {
					Object value = ClassUtils.getFieldValue(entity, am.getPath());
					Cell cell = createCell(row, colIndex, entity, value, am);
					writeCellValue(cell, value, getEntityModel(), am);
					colIndex++;
				}
			}
			rowIndex++;
			entity = iterator.next();
		}
		resizeColumns(sheet);

		getWorkbook().write(stream);
		return stream.toByteArray();
	}

	/**
	 * Resizes all columns on a sheet if possible
	 *
	 * @param sheet the sheet
	 */
	protected void resizeColumns(Sheet sheet) {
		if (canResize()) {
			for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}
		}
	}

	protected void writeCellValue(Cell cell, Object value, EntityModel<T> em, AttributeModel am) {
		if (value instanceof Integer || value instanceof Long) {
			// integer or long numbers
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Date && (am == null || !am.isWeek())) {
			cell.setCellValue((Date) value);
		} else if (value instanceof LocalDate) {
			cell.setCellValue(DateUtils.toLegacyDate((LocalDate) value));
		} else if (value instanceof LocalDateTime) {
			cell.setCellValue(DateUtils.toLegacyDate((LocalDateTime) value));
		} else if (value instanceof BigDecimal) {
			boolean isPercentage = am != null && am.isPercentage();
			int defaultPrecision = SystemPropertyUtils.getDefaultDecimalPrecision();
			if (isPercentage) {
				// percentages in the application are just numbers,
				// but in Excel they are fractions that
				// are displayed as percentages -> so, divide by 100
				double temp = ((BigDecimal) value)
						.divide(MathUtils.HUNDRED, DynamoConstants.INTERMEDIATE_PRECISION, RoundingMode.HALF_UP)
						.setScale(am.getPrecision() + defaultPrecision, RoundingMode.HALF_UP).doubleValue();
				cell.setCellValue(temp);
			} else {
				cell.setCellValue(((BigDecimal) value)
						.setScale(am == null ? defaultPrecision : am.getPrecision(), RoundingMode.HALF_UP)
						.doubleValue());
			}
		} else if (am != null) {
			// use the attribute model
			String str = FormatUtils.formatPropertyValue(getEntityModelFactory(), am, value, ", ");
			cell.setCellValue(str);
		}
	}

	/**
	 * Creates an Excel cell and applies the correct style. The style to use depends
	 * on the attribute model and the value to display in the cell
	 *
	 * @param row            the row to which to add the cell
	 * @param colIndex       the column index of the cell
	 * @param entity         the entity that is represented in the row
	 * @param value          the cell value
	 * @param attributeModel the attribute model used to determine the style
	 * @return
	 */
	protected Cell createCell(Row row, int colIndex, T entity, Object value, AttributeModel attributeModel) {
		Cell cell = row.createCell(colIndex);
		cell.setCellStyle(getGenerator().getCellStyle(colIndex, entity, value, attributeModel));
		if (customGenerator != null) {
			CellStyle custom = customGenerator.getCustomCellStyle(workbook, entity, value, attributeModel);
			if (custom != null) {
				cell.setCellStyle(custom);
			}
		}
		return cell;
	}

	/**
	 * Creates an appropriate work book - if the size is below the threshold then a
	 * normal workbook is created. Otherwise a streaming workbook is created. This
	 * is much faster and more efficient, but you cannot auto resize the columns
	 *
	 * @param size the number of rows
	 * @return
	 */
	protected Workbook createWorkbook(int size) {
		if (size > MAX_SIZE_BEFORE_STREAMING) {
			return new SXSSFWorkbook();
		}
		return new XSSFWorkbook();
	}

	public XlsStyleGenerator<ID, T> getGenerator() {
		return generator;
	}

	public void setGenerator(XlsStyleGenerator<ID, T> generator) {
		this.generator = generator;
	}

}
