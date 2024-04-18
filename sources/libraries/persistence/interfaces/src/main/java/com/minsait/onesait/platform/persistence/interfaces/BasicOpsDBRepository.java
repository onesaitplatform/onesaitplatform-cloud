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
package com.minsait.onesait.platform.persistence.interfaces;

import java.util.List;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;

public interface BasicOpsDBRepository {

	public String insert(String ontology, String schema, String instance);

	public ComplexWriteResult insertBulk(String ontology, String schema, List<String> instances, boolean order,
			boolean includeIds);

	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds);

	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds);

	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds);

	public List<String> queryNative(String ontology, String query);

	public List<String> queryNative(String ontology, String query, int offset, int limit);

	public String queryNativeAsJson(String ontology, String query);

	public String queryNativeAsJson(String ontology, String query, int offset, int limit);

	public String findById(String ontology, String objectId);

	public String querySQLAsJson(String ontology, String query);

	public String querySQLAsTable(String ontology, String query);

	public String querySQLAsJson(String ontology, String query, int offset);

	public String querySQLAsTable(String ontology, String query, int offset);

	public String findAllAsJson(String ontology);

	public String findAllAsJson(String ontology, int limit);

	public List<String> findAll(String ontology);

	public List<String> findAll(String ontology, int limit);

	public long count(String ontology);

	public MultiDocumentOperationResult delete(String ontology, boolean includeIds);

	public long countNative(String collectionName, String query);

	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId);

	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body);

}
