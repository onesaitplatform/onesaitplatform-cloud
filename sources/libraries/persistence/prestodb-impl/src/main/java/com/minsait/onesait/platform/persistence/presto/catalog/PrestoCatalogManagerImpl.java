/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.persistence.presto.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("PrestoCatalogManagerImpl")
public class PrestoCatalogManagerImpl implements PrestoCatalogManager {

	final private static String FILE_EXTENSION_STR = ".properties";
	final private static String CATALOG_PATH = "prestodb-catalog-path";

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	private String catalogPath;

	@PostConstruct
	public void init() {
		catalogPath = Optional.ofNullable(
				(String) integrationResourcesService.getGlobalConfiguration().getEnv().getDatabase().get(CATALOG_PATH))
				.orElse("/catalog/path/");
	}

	@Override
	public void writeCatalogFile(String catalogName, Properties properties) throws IOException {
		final String filename = catalogName + FILE_EXTENSION_STR;
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(catalogPath + filename);
			properties.store(outputStream, null);
		} catch (IOException e) {
			throw e;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

	}

	@Override
	public Properties readCatalogFile(String catalogName) throws IOException {
		final String filename = catalogName + FILE_EXTENSION_STR;
		FileInputStream inputStream = null;
		Properties properties = new Properties();
		try {
			inputStream = new FileInputStream(catalogPath + filename);
			properties.load(inputStream);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return properties;
	}

	@Override
	public void deleteCatalogFile(String catalogName) {
		final String filename = catalogName + FILE_EXTENSION_STR;
		final File file = new File(catalogPath + filename);
		if (!file.delete()) {
			log.warn("Unable to delete file properties: {}", file.getName());
		}
	}
}
