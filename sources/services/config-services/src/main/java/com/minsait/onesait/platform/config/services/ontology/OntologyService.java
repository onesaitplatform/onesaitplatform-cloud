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
package com.minsait.onesait.platform.config.services.ontology;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.dto.OntologyForList;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.OntologyDataAccess;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyPresto;
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
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyFieldDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyListIndexMongoConfDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyPropertiesIndexConfDTO;
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

	List<OntologyVirtual> getOntologyVirtualByTableName(String tableName);

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

	List<OntologyDTO> getOntologiesForList(String userId, String identification, String description, Boolean showOwned,
			Boolean showAudit, Boolean showLog);

	List<OntologyDTO> getAllOntologiesForList(String sessionUserId, String identification, String description,
			String filterAudit, String filterLog);

	List<OntologyDTO> getOntologiesForListByUserPropietary(String sessionUserId, String identification,
			String description, String filterAudit, String filterLog);

	List<Ontology> getOntologiesByOwner(String sessionUserId);

	OntologyDataAccess createOrUpdateDataAccess(String ontId, String realm, String role, String user, String rule,
			String sessionuser);

	List<OntologyDataAccess> getOntologyUserDataAccesses(String id, String userId);

	void deleteDataAccess(String id, String userId);

	Map<String, String> getUserDataAccess(String user);

	Map<String, List<String>> getResourcesFromOntology(Ontology ontology);

	List<Ontology> getAllOntologiesByUser(String userId);

	List<String> getIdentificationsByUserAndPermissions(String userId);

	List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description);

	OntologyElastic getOntologyElasticByOntologyId(Ontology ontology);

	String getElementsAssociated(String ontologyId);

	List<OntologyPropertiesIndexConfDTO> getPropertiesOntology(Ontology ontology, List<String> indexList);

	List<OntologyPropertiesIndexConfDTO> getPropertiesOntologyVirtual(Ontology ontology,
			Map<String, List<String>> indexList);

	List<OntologyListIndexMongoConfDTO> getIndexTrue(String getindexMongoDB);

	OntologyPresto getOntologyPrestoByOntologyId(Ontology ontology);

	Ontology getOntologyByIdForDelete(String ontologyId, String sessionUserId) throws JsonProcessingException;

	Map<String, OntologyFieldDTO> getOntologyFieldsAndDesc(String identification, String sessionUserId)
			throws IOException;

	List<Ontology> getOntologiesByUserIdOnly(String sessionUserId);

	Ontology getOntologyByIdInsert(String ontologyId, String sessionUserId);

	Ontology getOntologyByIdentificationInsert(String ontologyId, String sessionUserId);

	Map<String, OntologyFieldDTO> getOntologyFieldsAndDescForms(String identification, String sessionUserId)
			throws IOException;

	boolean isTimescaleVirtualOntology(Ontology o);

	Ontology getOntologyByIdOrIdentification(String ontologyId, String sessionUserId);
}
