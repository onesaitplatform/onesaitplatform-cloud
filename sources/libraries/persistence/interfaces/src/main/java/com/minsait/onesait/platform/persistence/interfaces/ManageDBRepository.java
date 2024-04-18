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
package com.minsait.onesait.platform.persistence.interfaces;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;

public interface ManageDBRepository {

	public static final String BDTR_MONGO_SCHEMA_KEY = "BDTR_MONGO_SCHEMA_KEY";
	public static final String BDTR_RELATIONAL_TABLE_FIELDS = "BDTR_RELATIONAL_TABLE_FIELDS";

	public Map<String, Boolean> getStatusDatabase();

	public String createTable4Ontology(String ontology, String schema, Map<String, String> config);

	public List<String> getListOfTables();

	public List<String> getListOfTables4Ontology(String ontology);

	public void removeTable4Ontology(String ontology);

	public void createIndex(String ontology, String attribute);

	public void createIndex(String ontology, String nameIndex, String attribute);

	public void createIndex(String sentence);

	public void createTTLIndex(String ontology, String attribute, Long seconds);

	public void dropIndex(String ontology, String indexName);

	public List<String> getListIndexes(String ontology);

	public String getIndexes(String ontology);

	public void validateIndexes(String ontology, String schema);

	public ExportData exportToJson(String ontology, long startDateMillis, String path);

	public long deleteAfterExport(String ontology, String query);

	List<DescribeColumnData> describeTable(String name);

	public Map<String, String> getAdditionalDBConfig(String ontology);

	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config);
}
