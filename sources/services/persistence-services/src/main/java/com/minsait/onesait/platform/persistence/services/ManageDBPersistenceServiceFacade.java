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
package com.minsait.onesait.platform.persistence.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ManageDBPersistenceServiceFacade implements ManageDBRepository, NativeManageDBRepository {

	private static final String METHOD_NOT_EXECUTABLE = "Method not executable, please use same definition with RtdbDatasource parameter";

	@Autowired
	private ManageDBRepositoryFactory manageDBRepositoryFactory;

	@Override
	public Map<String, Boolean> getStatusDatabase(RtdbDatasource dataSource) {
		return manageDBRepositoryFactory.getInstance(dataSource).getStatusDatabase();
	}

	@Override
	public List<String> getListOfTables(RtdbDatasource dataSource) {
		return manageDBRepositoryFactory.getInstance(dataSource).getListOfTables();
	}

	@Override
	public void createIndex(RtdbDatasource dataSource, String sentence) {
		manageDBRepositoryFactory.getInstance(dataSource).createIndex(sentence);
	}

	public List<DescribeColumnData> describeTable(RtdbDatasource dataSource, String name) {
		return manageDBRepositoryFactory.getInstance(dataSource).describeTable(name);
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		return manageDBRepositoryFactory.getInstance(ontology).createTable4Ontology(ontology, schema, config);
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		return manageDBRepositoryFactory.getInstance(ontology).getListOfTables4Ontology(ontology);
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		manageDBRepositoryFactory.getInstance(ontology).removeTable4Ontology(ontology);

	}

	@Override
	public void createIndex(String ontology, String attribute) {
		manageDBRepositoryFactory.getInstance(ontology).createIndex(ontology, attribute);

	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		manageDBRepositoryFactory.getInstance(ontology).createIndex(ontology, nameIndex, attribute);

	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		manageDBRepositoryFactory.getInstance(ontology).dropIndex(ontology, indexName);

	}

	@Override
	public List<String> getListIndexes(String ontology) {
		return manageDBRepositoryFactory.getInstance(ontology).getListIndexes(ontology);
	}

	@Override
	public String getIndexes(String ontology) {
		return manageDBRepositoryFactory.getInstance(ontology).getIndexes(ontology);
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		manageDBRepositoryFactory.getInstance(ontology).validateIndexes(ontology, schema);

	}

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		throw new DBPersistenceException(METHOD_NOT_EXECUTABLE);
	}

	@Override
	public List<String> getListOfTables() {
		throw new DBPersistenceException(METHOD_NOT_EXECUTABLE);
	}

	@Override
	public void createIndex(String sentence) {
		throw new DBPersistenceException(METHOD_NOT_EXECUTABLE);
	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String pathToFile) {
		throw new DBPersistenceException(METHOD_NOT_EXECUTABLE);

	}

	public ExportData exportToJson(RtdbDatasource rtdbDatasource, String ontology, long startDateMillis,
			String pathToFile) {
		return manageDBRepositoryFactory.getInstance(rtdbDatasource).exportToJson(ontology, startDateMillis,
				pathToFile);

	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		throw new DBPersistenceException(METHOD_NOT_EXECUTABLE);
	}

	public long deleteAfterExport(RtdbDatasource rtdbDatasource, String ontology, String query) {
		return manageDBRepositoryFactory.getInstance(rtdbDatasource).deleteAfterExport(ontology, query);
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {

		throw new DBPersistenceException(METHOD_NOT_EXECUTABLE);

	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		return manageDBRepositoryFactory.getInstance(ontology).getAdditionalDBConfig(ontology);
	}

	@Override
	public String updateTable4Ontology(String ontology, String jsonSchema, Map<String, String> config) {
		return manageDBRepositoryFactory.getInstance(ontology).updateTable4Ontology(ontology, jsonSchema, config);
	}

}
