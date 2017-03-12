package com.ocs.jasperreports.export;

import com.ocs.jasperreports.ReportGenerator;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimplePptxExporterConfiguration;
import net.sf.jasperreports.export.SimplePptxReportConfiguration;
import net.sf.jasperreports.export.SimpleRtfExporterConfiguration;
import net.sf.jasperreports.export.SimpleRtfReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import java.io.OutputStream;

public class ExporterFactory {

	@SuppressWarnings("unchecked")
	public static Exporter getExporter(ReportGenerator.Format format, JasperPrint jasperPrint,
			OutputStream outputStream) {
		final Exporter exporter;
		switch (format) {
		case EXCEL:
			exporter = ExporterFactory.getExcelExporter();
			break;
		case POWERPOINT:
			exporter = ExporterFactory.getPptxExporter();
			break;
		case DOC:
			exporter = ExporterFactory.getRtfExporter();
			break;
		default:
			// PDF
			exporter = ExporterFactory.getPdfExporter();
			break;
		}
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new WriterOutputStreamExporterOutput(outputStream));

		return exporter;
	}

	private static JRPptxExporter getPptxExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRPptxExporter exporter = new JRPptxExporter(jasperReportsContext);

		final SimplePptxExporterConfiguration exporterConfiguration = new SimplePptxExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimplePptxReportConfiguration reportConfiguration = new SimplePptxReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}

	private static JRRtfExporter getRtfExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRRtfExporter exporter = new JRRtfExporter(jasperReportsContext);

		final SimpleRtfExporterConfiguration exporterConfiguration = new SimpleRtfExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimpleRtfReportConfiguration reportConfiguration = new SimpleRtfReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}

	private static JRPdfExporter getPdfExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRPdfExporter exporter = new JRPdfExporter(jasperReportsContext);

		final SimplePdfExporterConfiguration exporterConfiguration = new SimplePdfExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimplePdfReportConfiguration reportConfiguration = new SimplePdfReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}

	private static JRXlsExporter getExcelExporter() {
		final JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		final JRXlsExporter exporter = new JRXlsExporter(jasperReportsContext);

		final SimpleXlsExporterConfiguration exporterConfiguration = new SimpleXlsExporterConfiguration();
		exporter.setConfiguration(exporterConfiguration);

		final SimpleXlsReportConfiguration reportConfiguration = new SimpleXlsReportConfiguration();
		exporter.setConfiguration(reportConfiguration);

		return exporter;
	}
}
