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
package com.minsait.onesait.platform.persistence.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.DBResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("NoPersistenceBasicOpsDBRepository")
@Slf4j
public class NoPersistenceBasicOpsDBRepository implements BasicOpsDBRepository {
	private static final String NO_OP_CONTROL_ONTOLOGY = "NO-OP control ontology";

	@Override
	public String insert(String ontology, String instance) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "1";
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final DBResult result = new DBResult();
		result.setOk(true);
		result.setId("1");
		final List<DBResult> results = new ArrayList<>();
		return ComplexWriteResult.builder().type(ComplexWriteResultType.BULK).data(results).build();
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		result.setStrIds("");
		return result;
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		result.setStrIds("");
		return result;
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		result.setStrIds("");
		return result;
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String findById(String ontology, String objectId) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "";
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "";
	}

	@Override
	public String findAllAsJson(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public List<String> findAll(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public long count(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return 0;
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		result.setStrIds("");
		return result;
	}

	@Override
	public long countNative(String collectionName, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return 0;
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		result.setStrIds("");
		return result;
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(0);
		result.setIds(new ArrayList<>());
		result.setStrIds("");
		return result;
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final DBResult result = new DBResult();
		result.setOk(true);
		result.setId("1");
		final List<DBResult> results = new ArrayList<>();
		return ComplexWriteResult.builder().type(ComplexWriteResultType.BULK).data(results).build();
	}
}