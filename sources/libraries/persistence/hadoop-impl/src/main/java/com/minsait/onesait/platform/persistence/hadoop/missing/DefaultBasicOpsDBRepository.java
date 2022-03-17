/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.hadoop.missing;

import static com.minsait.onesait.platform.persistence.hadoop.common.HadoopMessages.NOT_SUPPORTED;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DefaultBasicOpsDBRepository implements BasicOpsDBRepository {

	@Override
	public String insert(String ontology, String instance) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String findById(String ontology, String objectId) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String findAllAsJson(String ontology) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> findAll(String ontology) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public long count(String ontology) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public long countNative(String collectionName, String query) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		log.error(NOT_SUPPORTED);
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_SUPPORTED);
	}

}
