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
package com.minsait.onesait.platform.report.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryRepositoryLogicService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.dto.report.ReportField;
import com.minsait.onesait.platform.config.dto.report.ReportInfoDto;
import com.minsait.onesait.platform.config.dto.report.ReportParameter;
import com.minsait.onesait.platform.config.dto.report.ReportParameterType;
import com.minsait.onesait.platform.config.dto.report.ReportType;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.reports.ReportService;
import com.minsait.onesait.platform.report.exception.GenerateReportException;
import com.minsait.onesait.platform.report.exception.ReportInfoException;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import net.sf.jasperreports.repo.FileRepositoryPersistenceServiceFactory;
import net.sf.jasperreports.repo.FileRepositoryService;
import net.sf.jasperreports.repo.PersistenceServiceFactory;
import net.sf.jasperreports.repo.RepositoryService;

@Service
@Slf4j
public class ReportInfoServiceImpl implements ReportInfoService {
	private static List<String> datasources;

	public static final String JSON_DATA_SOURCE_ATT_NAME = "net.sf.jasperreports.json.source";
	public static final String DATA_ADAPTER_ATT_NAME = "net.sf.jasperreports.data.adapter";
	public static final String DATASOURCE_ATT_NAME = "net.sf.jasperreports.engine.JRDataSource";
	public static final String DATASOURCE_JSS_NAME = "com.jaspersoft.studio.data.defaultdataadapter";
	public static final String JSON_DATA_SOURCE_ATT_TYPE = "STRING";

	@Autowired
	private BinaryRepositoryLogicService binaryRepositoryLogicService;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private ReportService reportService;

	static {
		datasources = new ArrayList<>();
		datasources.add(JSON_DATA_SOURCE_ATT_NAME);
		datasources.add(DATA_ADAPTER_ATT_NAME);
		datasources.add(DATASOURCE_ATT_NAME);

	}

	@Override
	public void updateResource(Report report, String fileId, MultipartFile file) throws Exception {
		report.getResources().removeIf(r -> r.getId().equals(fileId));
		binaryRepositoryLogicService.updateBinary(fileId, file, null);
		report.getResources().add(binaryFileService.getFile(fileId));
		reportService.saveOrUpdate(report);
	}

	@Override
	public ReportInfoDto extract(InputStream is, ReportExtension reportExtension) {
		ReportInfoDto reportInfo = null;

		switch (reportExtension) {
		case JRXML:
			reportInfo = extractFromJrxml(is);
			break;

		case JASPER:
			reportInfo = extractFromJasper(is);
			break;

		default:
			throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}

		return reportInfo;
	}

	private ReportInfoDto extractFromJrxml(InputStream is) {
		try {

			final JasperReport report = JasperCompileManager.compileReport(is);

			return extractFromReport(report);

		} catch (final JRException e) {
			throw new ReportInfoException(e);
		}
	}

	private ReportInfoDto extractFromJasper(InputStream is) {
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
		// TO-DO Create dir with files
		final Set<BinaryFile> resources = entity.getResources();
		if (resources.isEmpty()) {
			return generate(entity.getFile(), type, parameters, entity.getExtension(), null);
		}
		final String path = "/tmp/reports/" + entity.getIdentification() + File.separator;
		try {
			createDirectoryAndFiles(path, entity, resources);
		} catch (final Exception e) {
			log.error("Error creating dirs for report {}", e);
			throw new OPResourceServiceException("Error creating dirs for report {}", e);
		}
		final byte[] generated = generate(entity.getFile(), type, parameters, entity.getExtension(), path);
		deleteAllFiles(path);
		return generated;

	}

	private File createDirectoryAndFiles(String path, Report report, Set<BinaryFile> resources) {
		final File directory = new File(path);
		directory.mkdirs();
		resources.forEach(r -> {
			writeFileToPath(path + r.getFileName(), r.getId());
		});
		return new File(path + report.getIdentification());
	}

	private void deleteAllFiles(String path) {
		final File f = new File(path);
		for (final File file : f.listFiles()) {
			try {
				Files.delete(file.toPath());
			} catch (final IOException e) {
				log.error("Could not delete file {}", f.getName());
			}
		}
	}

	private void writeFileToPath(String path, byte[] content) {
		try (FileOutputStream fileOuputStream = new FileOutputStream(path)) {
			fileOuputStream.write(content);
		} catch (final Exception e) {
			log.error("Error while trying to create file", e);
		}
		if (path.endsWith(".jar")) {
			try {
				loadLibrary(new File(path));
			} catch (final Exception e) {
				log.error("Couldn't load jar", e);
			}
		}
	}

	private void writeFileToPath(String path, String binaryFileId) {
		BinaryFileData data;
		try {
			data = binaryRepositoryLogicService.getBinaryFileWOPermission(binaryFileId);
			writeFileToPath(path, ((ByteArrayOutputStream) data.getData()).toByteArray());
		} catch (IOException | BinaryRepositoryException e) {
			log.error("Error while trying to create file", e);
		}

	}

	private byte[] generate(byte[] source, ReportType type, Map<String, Object> params, ReportExtension extension,
			String path) {
		final InputStream is = new ByteArrayInputStream(source);

		return generate(is, params, extension, type, path);
	}

	private byte[] generate(InputStream is, Map<String, Object> params, ReportExtension extension, ReportType type,
			String path) {

		byte[] bytes = null;

		switch (extension) {
		case JRXML:
			bytes = generateFromJrXml(is, params, type, path);
			break;

		case JASPER:
			bytes = generateFromJasper(is, params, type, path);
			break;

		default:
			throw new GenerateReportException("Unknown extension, must be jrxml or jasper");
		}

		return bytes;
	}

	private byte[] generateFromJrXml(InputStream is, Map<String, Object> params, ReportType type, String path) {

		try {
			final JasperReport jasperReport = JasperCompileManager.compileReport(is);

			return generateFromReport(jasperReport, params, type, path);
		} catch (final JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromJasper(InputStream is, Map<String, Object> params, ReportType type, String path) {

		try {
			final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);

			return generateFromReport(jasperReport, params, type, path);
		} catch (final JRException e) {
			log.error("Handle unchecked exception", e);
			throw new GenerateReportException(e);
		}
	}

	private byte[] generateFromReport(JasperReport jasperReport, Map<String, Object> params, ReportType type,
			String path) throws JRException {
		JasperPrint jasperPrint = null;
		SimpleJasperReportsContext ctx = null;
		if (!StringUtils.isEmpty(path)) {
			Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
			ctx = new SimpleJasperReportsContext();
			final FileRepositoryService fileRepository = new FileRepositoryService(ctx, path, false);
			ctx.setExtensions(RepositoryService.class, Collections.singletonList(fileRepository));
			ctx.setExtensions(PersistenceServiceFactory.class,
					Collections.singletonList(FileRepositoryPersistenceServiceFactory.getInstance()));
		}
		if (hasDatasource(jasperReport)) {
			if (ctx != null) {
				jasperPrint = JasperFillManager.getInstance(ctx).fill(jasperReport, params);
			} else {
				jasperPrint = JasperFillManager.fillReport(jasperReport, params);
			}
		} else {
			if (ctx != null) {
				jasperPrint = JasperFillManager.getInstance(ctx).fill(jasperReport, params, new JREmptyDataSource());
			} else {
				jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
			}
		}
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

		case DOCX:
			exporter = new JRDocxExporter();
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
		// first condition for adapters, second for json datasources
		replaceJSSDatasourceClass(report);
		report.getPropertiesMap().removeProperty(DATASOURCE_JSS_NAME);
		return Arrays.stream(report.getPropertiesMap().getPropertyNames()).anyMatch(s -> datasources.contains(s))
				|| Arrays.stream(report.getParameters()).anyMatch(p -> datasources.contains(p.getName()));

	}

	private void replaceJSSDatasourceClass(JasperReport report) {
		try {
			final String p = report.getPropertiesMap().getProperty(DATASOURCE_JSS_NAME);
			if (!StringUtils.isEmpty(p)) {
				report.getPropertiesMap().removeProperty(DATASOURCE_JSS_NAME);
				report.getPropertiesMap().setProperty(DATA_ADAPTER_ATT_NAME, p);
			}
		} catch (final Exception e) {
			log.warn("Could not replace jss data adapter class");
		}
	}

	/*
	 * Add jars from Report resources if needed (fonts, connectors, etc)
	 *
	 */
	public static synchronized void loadLibrary(java.io.File jar) throws GenericOPException {
		try {
			final java.net.URLClassLoader loader = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
			final java.net.URL url = jar.toURI().toURL();
			/* Disallow if already loaded */
			for (final java.net.URL it : java.util.Arrays.asList(loader.getURLs())) {
				if (it.equals(url)) {
					return;
				}
			}
			final java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL",
					new Class[] { java.net.URL.class });
			method.setAccessible(true); /* promote the method to public access */
			method.invoke(loader, new Object[] { url });
		} catch (final java.lang.NoSuchMethodException | java.lang.IllegalAccessException
				| java.net.MalformedURLException | java.lang.reflect.InvocationTargetException e) {
			throw new GenericOPException(e);
		}
	}
}