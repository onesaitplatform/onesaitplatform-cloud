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
package com.minsait.onesait.platform.config.services.ontology;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.config.dto.OntologyForList;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestSecurity;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.datamodel.dto.DataModelDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceDTO;

public interface OntologyService {

	static final String QUERY_SQL = "SQL";
	static final String QUERY_NATIVE = "NATIVE";
	static final String DATAMODEL_DEFAULT_NAME = "EmptyBase";
	static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";

	List<Ontology> getAllOntologies(String sessionUserId);

	List<OntologyForList> getOntologiesForListByUserId(String sessionUserId);

	List<Ontology> getOntologiesByUserId(String sessionUserId);

	List<Ontology> getOntologiesByUserAndAccess(String sessionUserId, String identification, String description);

	List<Ontology> getOntologiesWithDescriptionAndIdentification(String sessionUserId, String identification,
			String description);

	List<OntologyForList> getOntologiesForListWithDescriptionAndIdentification(String sessionUserId,
			String identification, String description);

	List<Ontology> getOntologiesByUserIdAndDataModel(String sessionUserId, String datamodel);

	List<Ontology> getOntologiesByUserIdAndType(String sessionUserId, String type);

	List<String> getAllIdentificationsByUser(String userId);

	Ontology getOntologyById(String ontologyId, String sessionUserId);

	Ontology getOntologyByIdentification(String identification, String sessionUserId);

	List<DataModel> getAllDataModels();

	List<String> getAllDataModelTypes();

	boolean hasUserPermissionForQuery(User user, Ontology ontology);

	boolean hasUserPermissionForQuery(String userId, Ontology ontology);

	boolean hasUserPermissionForQuery(String userId, String ontologyId);

	boolean hasUserPermission(User user, AccessType access, Ontology ontology);

	boolean hasUserPermissionForInsert(User user, Ontology ontology);

	boolean hasUserPermissionForInsert(String userId, Ontology ontology);

	boolean hasUserPermissionForInsert(String userId, String ontologyIdentificator);

	boolean hasUserPermisionForChangeOntology(User user, Ontology ontology);

	boolean hasClientPlatformPermisionForInsert(String clientPlatformId, String ontologyId);

	boolean hasClientPlatformPermisionForQuery(String clientPlatformId, String ontologyId);

	void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config);

	void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config, boolean hasDocuments);

	boolean isIdValid(String ontologyId);

	void createOntology(Ontology ontology, OntologyConfiguration config);

	List<Ontology> getOntologiesByClientPlatform(ClientPlatform clientPlatform);

	List<Ontology> getAllAuditOntologies();

	/**
	 * This method checks if an ontology has authorizations for other users
	 * different from its owner.
	 *
	 * @param ontologyId the id of the ontology.
	 * @return true if any other user has authorization over the ontology.
	 */
	boolean hasOntologyUsersAuthorized(String ontologyId);

	List<OntologyUserAccess> getOntologyUserAccesses(String ontologyId, String sessionUserId);

	OntologyUserAccess createUserAccess(String ontologyId, String userId, String typeName, String sessionUserId);

	OntologyUserAccess getOntologyUserAccessByOntologyIdAndUserId(String ontologyId, String userId,
			String sessionUserId);

	OntologyUserAccess getOntologyUserAccessById(String userAccessId, String sessionUserId);

	void deleteOntologyUserAccess(String userAccessId, String sessionUserId);

	void updateOntologyUserAccess(String userAccessId, String typeName, String sessionUserId);

	Map<String, String> getOntologyFieldsQueryTool(String identification, String sessionUserId) throws IOException;

	Map<String, String> getOntologyFields(String identification, String sessionUserId) throws IOException;

	List<Ontology.RtdbDatasource> getDatasources();

	List<Ontology> getCleanableOntologies();

	void delete(Ontology ontology);

	OntologyRest getOntologyRestByOntologyId(Ontology ontologyId);

	OntologyRestSecurity getOntologyRestSecurityByOntologyRest(OntologyRest ontologyRest);

	List<OntologyRestOperation> getOperationsByOntologyRest(OntologyRest ontologyRest);

	OntologyRestHeaders getOntologyRestHeadersByOntologyRest(OntologyRest ontologyRest);

	List<OntologyRestOperationParam> getOperationsParamsByOperation(OntologyRestOperation operation);

	List<DataModelDTO> getEmptyBaseDataModel();

	List<VirtualDatasourceDTO> getDatasourcesRelationals();

	OntologyVirtual getOntologyVirtualByOntologyId(Ontology ontology);

	String getRtdbFromOntology(String ontologyIdentification);

	void checkOntologySchema(String schema);

	List<VirtualDatasourceDTO> getPublicOrOwnedDatasourcesRelationals(String sessionUserId);

	OntologyVirtualDatasource getOntologyVirtualDatasourceByName(String datasourceName);

	Map<String, String> executeKPI(String user, String query, String ontology, String context) throws Exception;

	public boolean existsOntology(String identificacion);

	Ontology getOntologyByIdentification(String identification);

	public String getOntologyFromQuery(String query);

	boolean hasEncryptionEnabled(String ontology);

	List<OntologyKPI> getOntologyKpisByOntology(Ontology ontology);

	List<OntologyDTO> getAllOntologiesForListWithProjectsAccess(String sessionUserId);

	List<OntologyDTO> getAllOntologiesForList(String sessionUserId, String identification, String description);

	List<OntologyDTO> getOntologiesForListByUser(String sessionUserId, String identification, String description);

	List<OntologyDTO> getOntologiesForListByUserPropietary(String sessionUserId, String identification,
			String description);

	List<Ontology> getOntologiesByOwner(String sessionUserId);

}
