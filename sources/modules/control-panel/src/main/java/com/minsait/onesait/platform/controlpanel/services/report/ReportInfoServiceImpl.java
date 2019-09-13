/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.controlpanel.services.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportField;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportInfoDto;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameter;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportParameterType;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportType;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;

@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {

	public static final String JSON_DATA_SOURCE_ATT_NAME = "net.sf.jasperreports.json.source";
	public static final String JSON_DATA_SOURCE_ATT_TYPE = "STRING";

	@Override
	public ReportInfoDto extract(InputStream is, ReportExtension reportExtension) {
		ReportInfoDto reportInfo = null;

		switch (reportExtension) {
		case JRXML:
			reportInfo = extractFromJrxml(is, reportExtension);
			break;

		case JASPER:
			reportInfo = extractFromJasper(is, reportExtension);
			break;

		default:
			throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}

		return reportInfo;
	}

	private ReportInfoDto extractFromJrxml(InputStream is, ReportExtension reportExtension) {
		try {

			final JasperReport report = JasperCompileManager.compileReport(is);

			return extractFromReport(report);

		} catch (final JRException e) {
			throw new ReportInfoException(e);
		}
	}

	private ReportInfoDto extractFromJasper(InputStream is, ReportExtension reportExtension) {
		try {

			final JasperReport report = (JasperReport) JRLoader.loadObject(is);

			return extractFromReport(report);

		} catch (final JRException e) {
			throw new ReportInfoException(e);
		}
	}

	private ReportInfoDto extractFromReport(JasperReport report) {
		log.debug("INI. Extract data from report: {}", report.getName());
		List<ReportParameter> parameters = new ArrayList<>();
		List<ReportField<?>> fields = new ArrayList<>();
		String dataSource = "";
		if (report.getParameters() != null) {
			parameters = Arrays.stream(report.getParameters()).filter(p -> p.isForPrompting() && !p.isSystemDefined())
					.map(p -> ReportParameter.builder().name(p.getName()).description(p.getDescription())
							.type(ReportParameterType.fromJavaType(p.getValueClass().getName()))
							.value(p.getDefaultValueExpression() != null
									? p.getDefaultValueExpression().getText().replaceAll("\"", "")
									: null)
							.build())
					.collect(Collectors.toList());
			dataSource = Arrays.stream(report.getParameters())
					.filter(p -> !p.isForPrompting() && p.getName().equalsIgnoreCase(JSON_DATA_SOURCE_ATT_NAME))
					.map(parameter -> {
						return parameter.getDefaultValueExpression() != null
								? parameter.getDefaultValueExpression().getText()
								: "";
					}).findFirst().orElse("");
		}

		if (report.getFields() != null) {
			fields = Arrays
					.stream(report.getFields()).map(f -> ReportField.builder().name(f.getName())
							.description(f.getDescription()).type(f.getValueClass()).build())
					.collect(Collectors.toList());
		}

		return ReportInfoDto.builder().parameters(parameters).fields(fields).dataSource(dataSource).build();
	}

	@Override
	public byte[] generate(Report entity, ReportType type, Map<String, Object> parameters) {

		return generate(entity.getFile(), type, parameters, entity.getExtension());

	}

	private byte[] generate(byte[] source, ReportType type, Map<String, Object> params, ReportExtension extension) {
		final InputStream is = new ByteArrayInputStream(source);

		return generate(is, params, extension, type);
	}

	private byte[] generate(InputStream is, Map<String, Object> params, ReportExtension extension, ReportType type) {

		byte[] bytes = null;

		switch (extension) {
		case JRXML:
			bytes = generateFromJrXml(is, params, type);
			break;

		case JASPER:
			bytes = generateFromJasper(is, params, type);
			break;

		default:
			throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}

		return bytes;
	}

	private byte[] generateFromJrXml(InputStream is, Map<String, Object> params, ReportType type) {

		try {
			final JasperReport jasperReport = JasperCompileManager.compileReport(is);

			return generateFromReport(jasperReport, params, type);
		} catch (final JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromJasper(InputStream is, Map<String, Object> params, ReportType type) {

		try {
			final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);

			return generateFromReport(jasperReport, params, type);
		} catch (final JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromReport(JasperReport jasperReport, Map<String, Object> params, ReportType type)
			throws JRException {
		JasperPrint jasperPrint = null;
		if (hasDatasource(jasperReport))
			jasperPrint = JasperFillManager.fillReport(jasperReport, params);
		else
			jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
		return export(jasperPrint, type);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private byte[] export(JasperPrint jasperPrint, ReportType type) throws JRException {
		final Exporter exporter;
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean exportIsSet = false;

		switch (type) {
		case HTML:
			exporter = new HtmlExporter();
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
			exportIsSet = true;
			break;

		case CSV:
			exporter = new JRCsvExporter();
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
			exportIsSet = true;
			break;

		case XML:
			exporter = new JRXmlExporter();
			exporter.setExporterOutput(new SimpleXmlExporterOutput(out));
			exportIsSet = true;
			break;

		case XLSX:
			exporter = new JRXlsxExporter();
			break;

		case PDF:
		default:
			exporter = new JRPdfExporter();
			break;

		}
		if (!exportIsSet) {
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
		}

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

		exporter.exportReport();

		return out.toByteArray();
	}

	private boolean hasDatasource(JasperReport report) {
		return Arrays.stream(report.getParameters())
				.filter(p -> p.getName().equalsIgnoreCase(JSON_DATA_SOURCE_ATT_NAME)).map(p -> true).findFirst()
				.orElse(false);

	}
}