/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.help.migration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.services.utils.ZipUtil;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MigrationHelper {

	private Template exportTemplate;
	private Template importTemplate;
	private Template exportFilesTemplate;
	private Template importFilesTemplate;

	@Autowired
	private ZipUtil zipUtil;

	@Value("${digitaltwin.temp.dir}")
	private String tempDir;

	@PostConstruct
	public void init() {
		Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		try {
			TemplateLoader templateLoader = new ClassTemplateLoader(getClass(), "/migration/templates");
			cfg.setTemplateLoader(templateLoader);

			exportTemplate = cfg.getTemplate("exportTemplate.ftl");
			importTemplate = cfg.getTemplate("importTemplate.ftl");
			exportFilesTemplate = cfg.getTemplate("exportFilesTemplate.ftl");
			importFilesTemplate = cfg.getTemplate("importFilesTemplate.ftl");
		} catch (IOException e) {
			log.error("Error configuring the template loader.", e);
		}
	}

	public File generateFiles(List<String> ontologies, String userMongo, String passwordMongo) {

		String directory = tempDir + File.separator + UUID.randomUUID();

		File src = createFile(directory + File.separator + "migration" + File.separator);

		if (src != null) {
			Map<String, Object> dataMap = new HashMap<>();
			StringBuilder builder = new StringBuilder();
			for (String o : ontologies) {
				builder.append(o).append(" ");
			}
			dataMap.put("ontologies", builder.toString());
			dataMap.put("user", userMongo);
			dataMap.put("password", passwordMongo);

			File zipFile = null;

			try (Writer writerExport = new FileWriter(src + File.separator + "export.sh");
					Writer writerImport = new FileWriter(src + File.separator + "import.sh");) {
				zipFile = File.createTempFile("scripts", ".zip");

				// create exportFile
				exportTemplate.process(dataMap, writerExport);
				writerExport.flush();

				// create importFile
				importTemplate.process(dataMap, writerImport);
				writerImport.flush();

			} catch (final Exception e) {
				log.error("Error generating Script to Mongo export/import", e);
			}

			File fileDirectory = new File(directory);
			try {
				zipUtil.zipDirectory(fileDirectory, zipFile);
			} catch (IOException e) {
				log.error("Zip file scripts failed", e);
			}

			// Removes the directory
			this.deleteDirectory(fileDirectory);

			return zipFile;
		}
		return null;

	}

	public File generateBinaryFilesExport(List<String> files, String userMongo, String passwordMongo) {

		String directory = tempDir + File.separator + UUID.randomUUID();

		File src = createFile(directory + File.separator + "migration" + File.separator);

		if (src != null) {
			Map<String, Object> dataMap = new HashMap<>();
			StringBuilder builder = new StringBuilder();
			for (String o : files) {
				builder.append("\"" + o + "\"").append(" ");
			}
			dataMap.put("files", builder.toString());
			dataMap.put("user", userMongo);
			dataMap.put("password", passwordMongo);

			File zipFile = null;

			try (Writer writerExport = new FileWriter(src + File.separator + "export.sh");
					Writer writerImport = new FileWriter(src + File.separator + "import.sh");) {
				zipFile = File.createTempFile("scripts", ".zip");

				// create exportFile
				exportFilesTemplate.process(dataMap, writerExport);
				writerExport.flush();

				// create importFile
				importFilesTemplate.process(dataMap, writerImport);
				writerImport.flush();

			} catch (final Exception e) {
				log.error("Error generating Script to BinaryFiles export/import", e);
			}

			File fileDirectory = new File(directory);
			try {
				zipUtil.zipDirectory(fileDirectory, zipFile);
			} catch (IOException e) {
				log.error("Zip file scripts failed", e);
			}

			// Removes the directory
			this.deleteDirectory(fileDirectory);

			return zipFile;
		}
		return null;

	}

	private File createFile(String path) {

		log.info("New file is going to be generate on: " + path);

		final File file = new File(path);

		if (!file.exists()) {
			Boolean success = file.mkdirs();
			if (!success) {
				log.error("Creating Scripts for Mongo import/export failed.");
				return null;
			}
		} else {
			log.error("Creating scripts for Mongo import/export failed, the temporary directory don't exist: "
					+ file.getAbsolutePath());
			return null;
		}

		return file;

	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

}
