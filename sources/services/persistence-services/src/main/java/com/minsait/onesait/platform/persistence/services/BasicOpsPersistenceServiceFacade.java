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

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.factory.BasicOpsDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BasicOpsPersistenceServiceFacade implements BasicOpsDBRepository, NativeBasicOpsRepository {

	@Autowired
	private BasicOpsDBRepositoryFactory basicOpsDBRepositoryFactory;

	@Autowired
	private OntologyRepository ontologyRepository;

	public RtdbDatasource getOntologyDataSource(String ontologyId) {
		final Ontology ds = ontologyRepository.findByIdentification(ontologyId);
		return ds.getRtdbDatasource();
	}

	public Ontology getOntology(String ontologyId) {
		return ontologyRepository.findByIdentification(ontologyId);
	}

	@Override
	public MultiDocumentOperationResult updateNative(RtdbDatasource dataSource, String collection, String query,
			String data, boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(dataSource).updateNative(collection, query, data, includeIds);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(RtdbDatasource dataSource, String collection, String query,
			boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(dataSource).deleteNative(collection, query, includeIds);
	}

	@Override
	public long countNative(RtdbDatasource dataSource, String collectionName, String query) {
		return basicOpsDBRepositoryFactory.getInstance(dataSource).countNative(collectionName, query);
	}

	@Override
	public String insert(String ontology, String instance) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).insert(ontology, instance);
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).insertBulk(ontology, instances, order, includeIds);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).updateNative(ontology, updateStmt, includeIds);
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).queryNative(ontology, query);
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).queryNative(ontology, query, offset, limit);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).queryNativeAsJson(ontology, query);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).queryNativeAsJson(ontology, query, offset, limit);
	}

	@Override
	public String findById(String ontology, String objectId) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).findById(ontology, objectId);
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).querySQLAsJson(ontology, query);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).querySQLAsTable(ontology, query);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).querySQLAsJson(ontology, query, offset);
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).querySQLAsTable(ontology, query, offset);
	}

	@Override
	public String findAllAsJson(String ontology) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).findAllAsJson(ontology);
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).findAllAsJson(ontology, limit);
	}

	@Override
	public List<String> findAll(String ontology) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).findAll(ontology);
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).findAll(ontology, limit);
	}

	@Override
	public long count(String ontology) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).count(ontology);
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeId) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).delete(ontology, includeId);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		return basicOpsDBRepositoryFactory.getInstance(ontologyName).deleteNativeById(ontologyName, objectId);
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		return basicOpsDBRepositoryFactory.getInstance(ontologyName).updateNativeByObjectIdAndBodyData(ontologyName,
				objectId, body);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(collection).updateNative(collection, query, data, includeIds);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(collection).deleteNative(collection, query, includeIds);
	}

	@Override
	public long countNative(String collectionName, String query) {
		return basicOpsDBRepositoryFactory.getInstance(collectionName).countNative(collectionName, query);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collectionName, String updateStmt)
			throws DBPersistenceException {
		return basicOpsDBRepositoryFactory.getInstance(collectionName)
				.queryUpdateTransactionCompensationNative(collectionName, updateStmt);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collectionName, String query, String data)
			throws DBPersistenceException {
		return basicOpsDBRepositoryFactory.getInstance(collectionName).queryUpdateTransactionCompensationNative(query,
				data);
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		return basicOpsDBRepositoryFactory.getInstance(ontologyName)
				.queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(ontologyName, objectId);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		return basicOpsDBRepositoryFactory.getInstance(collection).queryDeleteTransactionCompensationNative(collection,
				query);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		return basicOpsDBRepositoryFactory.getInstance(collection).queryDeleteTransactionCompensationNative(collection);
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		return basicOpsDBRepositoryFactory.getInstance(collection)
				.queryDeleteTransactionCompensationNativeById(collection, objectId);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		return basicOpsDBRepositoryFactory.getInstance(ontology).querySQLAsJson(ontology, query, offset, limit);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		return basicOpsDBRepositoryFactory.getInstance(collection).updateBulk(collection, queries, includeIds);
	}
}
