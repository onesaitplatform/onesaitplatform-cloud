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
package com.minsait.onesait.platform.persistence.hadoop.hive;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;

@Component("HiveBasicOpsDBRepository")
@Scope("prototype")
@Lazy
@ConditionalOnBean(name = NameBeanConst.HIVE_TEMPLATE_JDBC_BEAN_NAME)
public class HiveBasicOpsDBRepository implements BasicOpsDBRepository {

	private static final String NOT_IMPLEMENTED = "Method not implemented";

	@Override
	public String insert(String ontology, String instance) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String findById(String ontology, String objectId) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String findAllAsJson(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> findAll(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public long count(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public long countNative(String collectionName, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset, int limit) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public ComplexWriteResult updateBulk(String collection, String queries, boolean includeIds) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

}
