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
package com.minsait.onesait.platform.business.services.virtual.datasources;

import java.util.List;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;

public interface VirtualDatasourceService {

	List<String> getAllIdentifications();

	List<OntologyVirtualDatasource> getAllDatasources();

	void createDatasource(OntologyVirtualDatasource datasource) throws GenericOPException;

	OntologyVirtualDatasource getDatasourceById(String id);

	void updateOntology(OntologyVirtualDatasource datasource, Boolean maintainCredentials, String oldCredentials);

	void deleteDatasource(OntologyVirtualDatasource datasource);

	Boolean checkConnection(String datasource, String user, String credentials, String sgdb, String url,
			String queryLimit) throws GenericOPException;

	Boolean checkConnectionExtern(String datasource) throws GenericOPException;

	Boolean changePublic(String datasource);

	String getUniqueColumn(final String ontology);

	List<OntologyVirtualDatasource> getAllByDatasourceNameAndUser(String identification, String sessionUserId);

	List<OntologyVirtualDatasource> getAllDatasourcesByUser(User user);

	OntologyVirtualDatasource getDatasourceByIdAndUserId(String id, String sessionUserId);

	OntologyVirtualDatasource getDatasourceByIdAndUserIdOrIsPublic(String id, String sessionUserId,
			ResourceAccessType type);

}