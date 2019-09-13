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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.DataModel.MainType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRest.SecurityType;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperation.DefaultOperationType;
import com.minsait.onesait.platform.config.model.OntologyRestOperation.OperationType;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam.ParamOperationType;
import com.minsait.onesait.platform.config.model.OntologyRestSecurity;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestHeadersRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestOperationParamRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestOperationRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestSecurityRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.datamodel.dto.DataModelDTO;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.templates.PlatformQuery;
import com.minsait.onesait.platform.config.services.templates.QueryTemplateService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyServiceImpl implements OntologyService {

	@Autowired
	EntityDeletionService deletionService;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	private OntologyUserAccessTypeRepository ontologyUserAccessTypeRepository;
	@Autowired
	private DataModelRepository dataModelRepository;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyRestSecurityRepository ontologyRestSecurityRepo;
	@Autowired
	private OntologyRestOperationParamRepository ontologyRestOperationParamRepo;
	@Autowired
	private OntologyRestOperationRepository ontologyRestOperationRepo;
	@Autowired
	private OntologyRestRepository ontologyRestRepo;
	@Autowired
	private OntologyRestHeadersRepository ontologyRestHeadersRepo;
	@Autowired
	private OntologyVirtualDatasourceRepository ontologyVirtualDatasourceRepository;
	@Autowired
	private OntologyVirtualRepository ontologyvirtualRepository;
	@Autowired
	private OntologyDataService ontologyDataService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private OntologyKPIRepository ontologyKpiRepository;
	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;
	@Autowired
	private QueryTemplateService queryTemplateService;
	@Value("${onesaitplatform.ontologies.schema.ignore-case-properties:false}")
	private boolean ignoreTitleCaseCheck;

	@Autowired(required = false)
	@Qualifier("routerServiceImpl")
	private RouterService routerService;

	private static final String USER_UNAUTH_STR = "The user is not authorized";
	private static final String DATOS_STR = "datos";
	private static final String PROP_STR = "properties";
	private static final String OBJ_STR = "object";
	private static final String ARRAY_STR = "array";
	private static final String FORMAT_STR = "format";
	private static final String ITEMS_STR = "items";
	private static final String TYPE_STR = "type";

	public static final String KPI_TYPE = "kpi";
	public static final String TIMESERIES_TYPE = "timeseries";

	public static final String QUERY_SQL = "SQL";
	public static final String QUERY_NATIVE = "NATIVE";
	public static final String DATAMODEL_DEFAULT_NAME = "EmptyBase";
	public static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<Ontology> getAllOntologies(String sessionUserId) {

		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return ontologyRepository.findByUserAndOntologyUserAccessAndAllPermissions(sessionUser);
		}
	}

	@Override
	public List<Ontology> getOntologiesByUserId(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return ontologyRepository.findByUserAndAccess(sessionUser);
		}

	}

	@Override
	public List<Ontology> getOntologiesByUserAndAccess(String sessionUserId, String identification,
			String description) {
		List<Ontology> ontologies;
		final User sessionUser = userService.getUser(sessionUserId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			ontologies = ontologyRepository.findByIdentificationLikeAndDescriptionLike(identification, description);
		} else {
			ontologies = ontologyRepository.findByUserAndPermissionsANDIdentificationAndDescription(sessionUser,
					identification, description);
		}
		return ontologies;

	}

	@Override
	public List<Ontology> getOntologiesWithDescriptionAndIdentification(String sessionUserId, String identification,
			String description) {
		List<Ontology> ontologies;
		final User sessionUser = userService.getUser(sessionUserId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			ontologies = ontologyRepository
					.findByIdentificationContainingAndDescriptionContainingAndActiveTrue(identification, description);
		} else {
			ontologies = ontologyRepository.findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(
					sessionUser, identification, description);
		}
		return ontologies;
	}

	@Override
	public List<Ontology> getOntologiesByUserIdAndDataModel(String sessionUserId, String datamodel) {
		final User sessionUser = userService.getUser(sessionUserId);
		final List<DataModel> lDataModels = dataModelRepository.findByName(datamodel);
		if (lDataModels == null || lDataModels.isEmpty()) {
			log.warn("DataModel {} not found, no ontologies will be returned", datamodel);
			return new ArrayList<>();
		} else if (lDataModels.size() > 1) {
			log.warn("Several DataModels were found for name {}, using first one", datamodel);
		}

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {

			return ontologyRepository.findAllByDataModelOrderByIdentificationAsc(lDataModels.get(0));
		} else {
			return ontologyRepository.findAllByUserOwnerAndDataModel(sessionUser, lDataModels.get(0));
		}
	}

	@Override
	public List<Ontology> getOntologiesByUserIdAndType(String sessionUserId, String type) {
		final User sessionUser = userService.getUser(sessionUserId);
		final List<Ontology> ontologiesList = new ArrayList<>();

		switch (type) {

		case KPI_TYPE:
			final List<OntologyKPI> lOntologyKp = sessionUser.getRole().getId()
					.equals(Role.Type.ROLE_ADMINISTRATOR.toString()) ? ontologyKpiRepository.findAll()
							: ontologyKpiRepository.findByUser(sessionUser);

			lOntologyKp.forEach(o -> ontologiesList.add(o.getOntology()));
			break;

		case TIMESERIES_TYPE:
			final List<OntologyTimeSeries> lOntologyTs = sessionUser.getRole().getId()
					.equals(Role.Type.ROLE_ADMINISTRATOR.toString()) ? ontologyTimeSeriesRepository.findAll()
							: ontologyTimeSeriesRepository.findByUser(sessionUser);

			lOntologyTs.forEach(o -> ontologiesList.add(o.getOntology()));
			break;

		default:
			log.warn("type {} not allowed, no ontologies will be returned", type);
			break;
		}

		return ontologiesList;

	}

	@Override
	public List<String> getAllIdentificationsByUser(String userId) {
		List<Ontology> ontologies;
		final User user = userService.getUser(userId);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			ontologies = ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			ontologies = ontologyRepository.findByUserOrderByIdentificationAsc(user);
		}

		final List<String> identifications = new ArrayList<>();
		for (final Ontology ontology : ontologies) {
			identifications.add(ontology.getIdentification());

		}
		return identifications;
	}

	@Override
	public Ontology getOntologyById(String ontologyId, String sessionUserId) {
		final Ontology ontology = ontologyRepository.findById(ontologyId);
		final User sessionUser = userService.getUser(sessionUserId);
		if (ontology != null) {
			if (hasUserPermissionForQuery(sessionUser, ontology)) {
				return ontology;
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			return null;
		}

	}

	@Override
	public OntologyRest getOntologyRestByOntologyId(Ontology ontologyId) {
		return ontologyRestRepo.findByOntologyId(ontologyId);
	}

	@Override
	public OntologyRestSecurity getOntologyRestSecurityByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestSecurityRepo.findById(ontologyRest.getSecurityId().getId());
	}

	@Override
	public OntologyRestHeaders getOntologyRestHeadersByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestHeadersRepo.findById(ontologyRest.getHeaderId().getId());
	}

	@Override
	public List<OntologyRestOperation> getOperationsByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestOperationRepo.findByOntologyRestId(ontologyRest);
	}

	@Override
	public List<OntologyRestOperationParam> getOperationsParamsByOperation(OntologyRestOperation operation) {
		return ontologyRestOperationParamRepo.findByOperationId(operation);
	}

	@Override
	public Ontology getOntologyByIdentification(String identification, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final Ontology ontology = ontologyRepository.findByIdentification(identification);

		if (ontology != null) {
			if (hasUserPermissionForQuery(sessionUser, ontology)) {
				return ontology;
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			return null;
		}
	}

	@Override
	public List<DataModel> getAllDataModels() {
		return dataModelRepository.findAll();
	}

	@Override
	public List<DataModelDTO> getEmptyBaseDataModel() {
		return DataModelDTO.fromDataModels(dataModelRepository.findByName(DATAMODEL_DEFAULT_NAME));
	}

	@Override
	public List<String> getAllDataModelTypes() {
		final List<MainType> types = Arrays.asList(DataModel.MainType.values());
		final List<String> typesString = new ArrayList<>();
		for (final MainType type : types) {
			typesString.add(type.toString());
		}
		return typesString;
	}

	@Override
	public boolean hasUserPermissionForQuery(User user, Ontology ontology) {
		if (ontology == null || user == null)
			return false;
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else if (ontology.isPublic()) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);
			if (userAuthorization != null) {
				switch (OntologyUserAccessType.Type.valueOf(userAuthorization.getOntologyUserAccessType().getName())) {
				case ALL:
				case INSERT:
				case QUERY:
					return true;
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(user.getUserId(), ontology.getId(), ResourceAccessType.VIEW);
			}
		}
	}

	@Override
	public boolean hasUserPermissionForQuery(String userId, Ontology ontology) {
		final User user = userService.getUser(userId);
		return hasUserPermissionForQuery(user, ontology);
	}

	@Override
	public boolean hasUserPermissionForQuery(String userId, String ontologyIdentificator) {
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentificator);
		return hasUserPermissionForQuery(userId, ontology);
	}

	@Override
	public boolean hasUserPermissionForInsert(User user, Ontology ontology) {
		if (ontology == null || user == null)
			return false;
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);
			if (userAuthorization != null) {
				switch (OntologyUserAccessType.Type.valueOf(userAuthorization.getOntologyUserAccessType().getName())) {
				case ALL:
				case INSERT:
					return true;
				default:
					return false;
				}
			} else {
				return resourceService.hasAccess(user.getUserId(), ontology.getId(), ResourceAccessType.MANAGE);
			}
		}
	}

	@Override
	public boolean hasUserPermissionForInsert(String userId, String ontologyIdentificator) {
		final User user = userService.getUser(userId);
		final Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentificator);
		return hasUserPermissionForInsert(user, ontology);
	}

	@Override
	public boolean hasUserPermissionForInsert(String userId, Ontology ontology) {
		final User user = userService.getUser(userId);
		return hasUserPermissionForInsert(user, ontology);
	}

	@Override
	public Map<String, String> getOntologyFields(String identification, String sessionUserId) throws IOException {

		Map<String, String> fields = new TreeMap<>();
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		JsonNode jsonNode = mapper.readTree(ontology.getJsonSchema());

		// Predefine Path to data properties
		if (jsonNode != null) {
			if (!jsonNode.path(DATOS_STR).path(PROP_STR).isMissingNode())
				jsonNode = jsonNode.path(DATOS_STR).path(PROP_STR);
			else
				jsonNode = jsonNode.path(PROP_STR);

			fields = extractFieldsFromJsonNode(jsonNode);

		}

		return fields;
	}

	private Map<String, String> extractFieldsFromJsonNode(JsonNode jsonNode) {
		final Map<String, String> fields = new TreeMap<>();
		final Iterator<String> iterator = jsonNode.fieldNames();
		String property;
		while (iterator.hasNext()) {
			property = iterator.next();

			if (jsonNode.path(property).get(TYPE_STR).asText().equals(OBJ_STR)) {
				extractSubFieldsFromJson(fields, jsonNode, property, property, false, false);
			} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsFromJson(fields, jsonNode, property, property, true, false);
			} else {
				if (jsonNode.path(property).get(FORMAT_STR) != null)
					fields.put(property, "date");
				else
					fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
			}

		}
		return fields;
	}

	@Override
	public Map<String, String> getOntologyFieldsQueryTool(String identification, String sessionUserId)
			throws IOException {
		Map<String, String> fields = new TreeMap<>();
		String context = "";
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		if (ontology != null) {

			JsonNode jsonNode = null;
			try {

				jsonNode = mapper.readTree(ontology.getJsonSchema());

			} catch (final Exception e) {
				if (ontology.getJsonSchema().contains("'"))
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
			}

			// Predefine Path to data properties
			if (jsonNode != null) {
				if (!jsonNode.path(DATOS_STR).path(PROP_STR).isMissingNode()) {
					context = jsonNode.path(PROP_STR).fields().next().getKey();
					jsonNode = jsonNode.path(DATOS_STR).path(PROP_STR);
				} else {
					jsonNode = jsonNode.path(PROP_STR);
				}
				
				fields = extractFieldsQueryToolFromJsonNode(jsonNode);
			}
		}
		// add Context to fields for query
		if (!context.equals("")) {
			final Map<String, String> fieldsForQuery = new TreeMap<>();
			for (final Map.Entry<String, String> field : fields.entrySet()) {
				final String key = field.getKey();
				final String value = field.getValue();
				fieldsForQuery.put(context + "." + key, value);
			}
			fields = fieldsForQuery;
		}
		return fields;
	}

	private Map<String, String> extractFieldsQueryToolFromJsonNode(JsonNode jsonNode) {
		Map<String, String> fields = new TreeMap<>();
		final Iterator<String> iterator = jsonNode.fieldNames();
		String property;
		while (iterator.hasNext()) {
			property = iterator.next();
			if (jsonNode.path(property).toString().equals("{}")) {
				fields.put(property, OBJ_STR);
			} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(OBJ_STR)) {
				fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
				extractSubFieldsFromJson(fields, jsonNode, property, property, false, true);
			} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsFromJson(fields, jsonNode, property, property, true, true);
			} else {
				if (jsonNode.path(property).get(FORMAT_STR) != null)
					fields.put(property, "date");
				else
					fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
			}

		}
		return fields;
	}

	@Override
	public void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config,
			boolean hasDocuments) {
		if (hasDocuments) {
			if (!ontology.getRtdbDatasource().equals(RtdbDatasource.KUDU)) {
				ontologyDataService.checkRequiredFields(ontologyRepository.findById(ontology.getId()).getJsonSchema(),
						ontology.getJsonSchema());
			} else {
				ontologyDataService.checkSameSchema(ontologyRepository.findById(ontology.getId()).getJsonSchema(),
						ontology.getJsonSchema());
			}
		}
		updateOntology(ontology, sessionUserId, config);

	}

	@Override
	public void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config) {
		final Ontology ontologyDb = ontologyRepository.findById(ontology.getId());
		final User sessionUser = userService.getUser(sessionUserId);
		String objectId = null;
		if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
			objectId = config.getObjectId();
		}

		if (ontologyDb != null) {
			if (hasUserPermisionForChangeOntology(sessionUser, ontologyDb)) {
				checkOntologySchema(ontology.getJsonSchema());
				if (!ignoreTitleCaseCheck)
					ontologyDataService.checkTitleCaseSchema(ontology.getJsonSchema());

				ontology.setUser(ontologyDb.getUser());

				ontology.setOntologyUserAccesses(ontologyDb.getOntologyUserAccesses());

				if (ontology.isRtdbToHdb())
					ontology.setRtdbClean(true);
				else
					ontology.setRtdbToHdbStorage(null);

				if (ontology.isRtdbClean() && ontology.getRtdbCleanLapse().equals(RtdbCleanLapse.NEVER)) {
					ontology.setRtdbCleanLapse(RtdbCleanLapse.ONE_MONTH);
				}

				ontology.setIdentification(ontologyDb.getIdentification());
				ontologyRepository.save(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					createRestOntology(ontologyDb, config);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					final OntologyVirtual ontologyVirtual = ontologyvirtualRepository.findByOntologyId(ontologyDb);
					ontologyVirtual.setOntologyId(ontology);
					ontologyVirtual.setObjectId(objectId);
					ontologyvirtualRepository.save(ontologyVirtual);
				}
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			throw new OntologyServiceException("Ontology does not exist");
		}
	}

	@Override
	@Transactional
	public void createOntology(Ontology ontology, OntologyConfiguration config) {

		if (ontologyRepository.findByIdentification(ontology.getIdentification()) == null) {
			if (ontology.isRtdbClean()
					&& (ontology.getRtdbCleanLapse() == null || ontology.getRtdbCleanLapse().getMilliseconds() == 0)) {
				ontology.setRtdbClean(false);
				ontology.setRtdbCleanLapse(RtdbCleanLapse.NEVER);
			} else if (!ontology.isRtdbClean()) {
				ontology.setRtdbCleanLapse(RtdbCleanLapse.NEVER);
			}
			if (ontology.getDataModel() != null) {
				final DataModel dataModel = dataModelRepository.findById(ontology.getDataModel().getId());
				ontology.setDataModel(dataModel);
			} else {
				final DataModel dataModel = dataModelRepository.findByName(DATAMODEL_DEFAULT_NAME).get(0);
				ontology.setDataModel(dataModel);
			}

			if (!ignoreTitleCaseCheck && !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL))
				ontologyDataService.checkTitleCaseSchema(ontology.getJsonSchema());

			final User user = userService.getUser(ontology.getUser().getUserId());
			if (user != null) {
				ontology.setUser(user);
				ontologyRepository.saveAndFlush(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					createRestOntology(ontology, config);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					createVirtualOntology(ontology, config.getDatasource(), config.getObjectId());
				}
			} else {
				throw new OntologyServiceException("Invalid user");
			}
		} else {
			throw new OntologyServiceException(
					"Ontology with identification: " + ontology.getIdentification() + " exists");
		}

	}

	private void createVirtualOntology(Ontology ontology, String datasourceName, String objectId) {
		if (objectId == null || objectId.equals("")) {
			objectId = null;
		}
		final OntologyVirtualDatasource datasource = ontologyVirtualDatasourceRepository
				.findByDatasourceName(datasourceName);

		if (datasource != null) {
			final OntologyVirtual ontologyVirtual = new OntologyVirtual();
			ontologyVirtual.setDatasourceId(datasource);
			ontologyVirtual.setOntologyId(ontology);
			ontologyVirtual.setObjectId(objectId);
			ontologyvirtualRepository.save(ontologyVirtual);
		} else {
			throw new OntologyServiceException("Datasource " + datasourceName + " not found.");
		}
	}

	private void createRestOntology(Ontology ontology, OntologyConfiguration config) {
		try {
			final OntologyRest ontologyRestUpdate = ontologyRestRepo.findByOntologyId(ontology);
			OntologyRest ontologyRest = new OntologyRest();

			Boolean isUpdate = false;
			if (ontologyRestUpdate != null) {
				isUpdate = true;
				ontologyRest = ontologyRestUpdate;
			}
			final JSONObject authJson = new JSONObject();

			Boolean isAuth = false;
			if (config.getAuthCheck() != null && config.getAuthCheck().equals("on")) {
				isAuth = true;
			}
			if (isAuth) {

				if (config.getAuthMethod() != null) {
					if (config.getAuthMethod().equalsIgnoreCase("apiKey")) {
						authJson.put("header", config.getHeader());
						authJson.put("token", config.getToken());
						ontologyRest.setSecurityType(SecurityType.API_KEY);
					} else if (config.getAuthMethod().equalsIgnoreCase("oauth")) {
						authJson.put("user", config.getOauthUser());
						authJson.put("password", config.getOauthPass());
						ontologyRest.setSecurityType(SecurityType.OAUTH);
					} else if (config.getAuthMethod().equalsIgnoreCase("basic")) {
						authJson.put("user", config.getBasicUser());
						authJson.put("password", config.getBasicPass());
						ontologyRest.setSecurityType(SecurityType.BASIC);
					}
				} else {
					ontologyRest.setSecurityType(SecurityType.NONE);
				}

			} else {
				ontologyRest.setSecurityType(SecurityType.NONE);
			}

			OntologyRestSecurity security = new OntologyRestSecurity();
			if (isUpdate) {
				security = ontologyRestUpdate.getSecurityId();
			}

			security.setConfig(authJson.toString());
			security = ontologyRestSecurityRepo.save(security);

			OntologyRestHeaders ontologyHeaders = new OntologyRestHeaders();
			if (isUpdate) {
				ontologyHeaders = ontologyRestUpdate.getHeaderId();
			}

			String headersConfig = "[]";
			if (config.getHeaders() != null && config.getHeaders().length > 0) {
				final JSONArray jsonHeader = new JSONArray(config.getHeaders()[0]);
				headersConfig = jsonHeader.toString();
			}
			ontologyHeaders.setConfig(headersConfig);
			ontologyHeaders = ontologyRestHeadersRepo.save(ontologyHeaders);

			final Set<OntologyRestOperation> operationsList = new HashSet<>();
			final Set<OntologyRestOperationParam> paramsRestOperations = new HashSet<>();

			List<OntologyRestOperation> operationsOld = new ArrayList<>();

			if (config.getOperations() != null) {

				final JSONArray jsonArray = new JSONArray(config.getOperations()[0]);

				for (int i = 0; i < jsonArray.length(); i++) {

					final JSONObject json = jsonArray.getJSONObject(i);
					final String name = json.getString("name");
					final String path = json.getString("path");
					final String type = json.getString("type");
					final String defaultOperationType = json.getString("defaultOperationType");
					final String description = json.getString("description");
					final String origin = json.getString("origin");

					OntologyRestOperation operation = new OntologyRestOperation();
					if (isUpdate) {
						operationsOld = ontologyRestOperationRepo.findByOntologyRestId(ontologyRest);
						operation = ontologyRestOperationRepo.findByOntologyRestIdAndName(ontologyRest, name);
						if (operation == null) {
							operation = new OntologyRestOperation();
						}
					}

					final JSONArray pathParams = json.getJSONArray("pathParams");
					final JSONArray queryParams = json.getJSONArray("queryParams");

					for (int x = 0; x < pathParams.length(); x++) {

						final JSONObject pathObj = pathParams.getJSONObject(x);
						final Integer index = pathObj.getInt("indexes");
						final String namePath = pathObj.getString("namesPaths");
						final String field = pathObj.getString("fieldsPaths");

						OntologyRestOperationParam operationParam = new OntologyRestOperationParam();

						if (isUpdate && operation.getId() != null) {
							operationParam = ontologyRestOperationParamRepo.findByOperationIdAndNameAndType(operation,
									namePath, ParamOperationType.PATH);
							if (operationParam == null) {
								operationParam = new OntologyRestOperationParam();
							}
						}
						operationParam.setIndexParam(index);
						operationParam.setName(namePath);
						operationParam.setType(ParamOperationType.PATH);
						operationParam.setOperationId(operation);
						operationParam.setField(field);

						paramsRestOperations.add(operationParam);
					}

					for (int x = 0; x < queryParams.length(); x++) {

						final JSONObject queryObj = queryParams.getJSONObject(x);
						final String nameQuery = queryObj.getString("namesQueries");
						final String field = queryObj.getString("fieldsQueries");

						OntologyRestOperationParam operationParam = new OntologyRestOperationParam();

						if (isUpdate && operation.getId() != null) {
							operationParam = ontologyRestOperationParamRepo.findByOperationIdAndNameAndType(operation,
									nameQuery, ParamOperationType.QUERY);
							if (operationParam == null) {
								operationParam = new OntologyRestOperationParam();
							}
						}
						operationParam.setName(nameQuery);
						operationParam.setType(ParamOperationType.QUERY);
						operationParam.setOperationId(operation);
						operationParam.setField(field);

						paramsRestOperations.add(operationParam);
					}

					operation.setName(name);
					operation.setPath(path);
					operation.setType(OperationType.valueOf(type.toUpperCase()));
					operation.setDefaultOperationType(DefaultOperationType.valueOf(defaultOperationType.toUpperCase()));
					operation.setDescription(description);
					operation.setOntologyRestId(ontologyRest);
					operation.setOrigin(origin);

					operationsList.add(operation);

				}
			}

			ontologyRest.setBaseUrl(config.getBaseUrl());
			ontologyRest.setHeaderId(ontologyHeaders);
			ontologyRest.setOntologyId(ontology);
			ontologyRest.setSwaggerUrl(config.getSwagger());
			ontologyRest.setSecurityId(security);
			if (config.getSchema() != null) {
				ontologyRest.setJsonSchema(config.getSchema());
				ontology.setJsonSchema(ontologyRest.getJsonSchema());
				ontologyRepository.save(ontology);
			}
			ontologyRestRepo.save(ontologyRest);

			if (isUpdate) {

				for (final OntologyRestOperation op : operationsOld) {
					if (!operationsList.contains(op)) {
						ontologyRestOperationRepo.delete(op);
					}
				}
			}

			ontologyRestOperationRepo.save(operationsList);

			ontologyRestOperationParamRepo.save(paramsRestOperations);

		} catch (final Exception e) {
			throw new OntologyServiceException("Problems creating the external rest ontology", e);

		}

	}

	private Map<String, String> extractSubFieldsFromJson(Map<String, String> fields, JsonNode jsonNode, String property,
			String parentField, boolean isPropertyArray, boolean addTypeObject) {
		if (isPropertyArray) {
			if (!jsonNode.path(property).path(ITEMS_STR).path(PROP_STR).isMissingNode())
				jsonNode = jsonNode.path(property).path(ITEMS_STR).path(PROP_STR);
			else if (!jsonNode.path(property).path(PROP_STR).isMissingNode()) {
				jsonNode = jsonNode.path(property).path(PROP_STR);
			} else {
				jsonNode = jsonNode.path(property).path(ITEMS_STR);
				final int size = jsonNode.size();
				try {
					for (int i = 0; i < size; i++) {
						fields.put(parentField + "." + i, jsonNode.path(i).get(TYPE_STR).asText());
					}
				} catch (final Exception e) {
					fields.put(parentField + "." + 0, jsonNode.get(TYPE_STR).asText());
				}
				return fields;

			}
		} else {
			jsonNode = jsonNode.path(property).path(PROP_STR);
		}
		final Iterator<String> iterator = jsonNode.fieldNames();
		String subProperty;
		while (iterator.hasNext()) {
			subProperty = iterator.next();

			if (jsonNode.path(subProperty).get(TYPE_STR).asText().equals(OBJ_STR)) {
				if (addTypeObject)
					fields.put(parentField + "." + subProperty, jsonNode.path(subProperty).get(TYPE_STR).asText());
				extractSubFieldsFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty, false,
						addTypeObject);
			} else if (jsonNode.path(subProperty).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty, true,
						addTypeObject);

			} else {
				if (subProperty.equals("$date"))
					fields.put(parentField, "date");
				else {
					if (jsonNode.path(subProperty).get(FORMAT_STR) != null)
						fields.put(parentField + "." + subProperty, "date");
					else
						fields.put(parentField + "." + subProperty, jsonNode.path(subProperty).get(TYPE_STR).asText());

				}
			}
		}

		return fields;

	}

	@Override
	public List<Ontology> getOntologiesByClientPlatform(ClientPlatform clientPlatform) {
		final List<Ontology> ontologies = new ArrayList<>();
		for (final ClientPlatformOntology relation : clientPlatformOntologyRepository
				.findByClientPlatform(clientPlatform)) {
			ontologies.add(relation.getOntology());
		}
		return ontologies;
	}

	@Override
	public boolean hasOntologyUsersAuthorized(String ontologyId) {
		final Ontology ontology = ontologyRepository.findById(ontologyId);
		final List<OntologyUserAccess> authorizations = ontologyUserAccessRepository.findByOntology(ontology);
		return authorizations != null && !authorizations.isEmpty();
	}

	@Override
	public List<OntologyUserAccess> getOntologyUserAccesses(String ontologyId, String sessionUserId) {
		final Ontology ontology = getOntologyById(ontologyId, sessionUserId);
		return ontologyUserAccessRepository.findByOntology(ontology);
	}

	@Override
	@Modifying
	public OntologyUserAccess createUserAccess(String ontologyId, String userId, String typeName,
			String sessionUserId) {

		final Ontology ontology = ontologyRepository.findById(ontologyId);
		final User sessionUser = userService.getUser(sessionUserId);

		if (hasUserPermisionForChangeOntology(sessionUser, ontology)) {
			final List<OntologyUserAccessType> managedTypes = ontologyUserAccessTypeRepository.findByName(typeName);
			final OntologyUserAccessType managedType = managedTypes != null && !managedTypes.isEmpty()
					? managedTypes.get(0)
					: null;
			final User userToBeAutorized = userService.getUser(userId);
			if (ontology != null && managedType != null && userToBeAutorized != null) {
				final OntologyUserAccess ontologyUserAccess = new OntologyUserAccess();
				ontologyUserAccess.setOntology(ontology);
				ontologyUserAccess.setUser(userToBeAutorized);
				ontologyUserAccess.setOntologyUserAccessType(managedType);

				ontology.getOntologyUserAccesses().add(ontologyUserAccess);
				return ontologyRepository.save(ontology).getOntologyUserAccesses().stream()
						.filter(oua -> oua.getOntology().equals(ontology) && oua.getUser().equals(userToBeAutorized)
								&& oua.getOntologyUserAccessType().equals(managedType))
						.findFirst().orElse(ontologyUserAccess);
			} else {
				throw new OntologyServiceException("Problem creating the authorization");
			}
		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	public OntologyUserAccess getOntologyUserAccessByOntologyIdAndUserId(String ontologyId, String userId,
			String sessionUserId) {
		final Ontology ontology = getOntologyById(ontologyId, sessionUserId);
		final User user = userService.getUser(userId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findByOntologyAndUser(ontology, user);
		if (userAccess == null) {
			throw new OntologyServiceException("Problem obtaining user data");
		} else {
			return userAccess;
		}
	}

	@Override
	public OntologyUserAccess getOntologyUserAccessById(String userAccessId, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findById(userAccessId);
		if (hasUserPermissionForQuery(sessionUser, userAccess.getOntology())) {
			return userAccess;
		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	@Modifying
	public void deleteOntologyUserAccess(String userAccessId, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findById(userAccessId);

		if (hasUserPermisionForChangeOntology(sessionUser, userAccess.getOntology())) {
			final Set<OntologyUserAccess> accesses = userAccess.getOntology().getOntologyUserAccesses().stream()
					.filter(a -> !a.getId().equals(userAccess.getId())).collect(Collectors.toSet());
			final Ontology ontology = ontologyRepository.findById(userAccess.getOntology().getId());
			ontology.getOntologyUserAccesses().clear();
			ontology.getOntologyUserAccesses().addAll(accesses);
			ontologyRepository.save(ontology);

		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	@Modifying
	public void updateOntologyUserAccess(String userAccessId, String typeName, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final OntologyUserAccess userAccess = ontologyUserAccessRepository.findById(userAccessId);
		final List<OntologyUserAccessType> types = ontologyUserAccessTypeRepository.findByName(typeName);
		if (!CollectionUtils.isEmpty(types)) {
			if (hasUserPermisionForChangeOntology(sessionUser, userAccess.getOntology())) {

				final OntologyUserAccessType typeDB = types.get(0);
				final Set<OntologyUserAccess> accesses = userAccess.getOntology().getOntologyUserAccesses();
				accesses.remove(userAccess);
				userAccess.setOntologyUserAccessType(typeDB);
				accesses.add(userAccess);
				userAccess.getOntology().getOntologyUserAccesses().clear();
				userAccess.getOntology().getOntologyUserAccesses().addAll(accesses);
				ontologyRepository.save(userAccess.getOntology());
			} else {
				throw new OntologyServiceException(USER_UNAUTH_STR);
			}
		} else {
			throw new IllegalStateException("Incorrect type of access");
		}

	}

	@Override
	public boolean hasUserPermisionForChangeOntology(User user, Ontology ontology) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);
			if (userAuthorization != null && OntologyUserAccessType.Type.valueOf(
					userAuthorization.getOntologyUserAccessType().getName()) == OntologyUserAccessType.Type.ALL)
				return true;

		}
		return false;

	}

	@Override
	public boolean hasClientPlatformPermisionForInsert(String clientPlatformId, String ontologyId) {
		final ClientPlatformOntology clientPlatformOntology = clientPlatformOntologyRepository
				.findByOntologyAndClientPlatform(ontologyId, clientPlatformId);

		if (clientPlatformOntology != null) {

			switch (clientPlatformOntology.getAccess()) {
			case ALL:
			case INSERT:
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean hasClientPlatformPermisionForQuery(String clientPlatformId, String ontologyId) {

		final ClientPlatformOntology clientPlatformOntology = clientPlatformOntologyRepository
				.findByOntologyAndClientPlatform(ontologyId, clientPlatformId);

		if (clientPlatformOntology != null) {

			switch (clientPlatformOntology.getAccess()) {
			case ALL:
			case INSERT:
			case QUERY:
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean isIdValid(String ontologyId) {

		final String regExp = "^[^\\d].*";
		return (ontologyId.matches(regExp));
	}

	@Override
	public List<RtdbDatasource> getDatasources() {
		return Arrays.asList(Ontology.RtdbDatasource.values());
	}

	@Override
	public List<Ontology> getCleanableOntologies() {
		return ontologyRepository.findByRtdbCleanTrueAndRtdbCleanLapseNotNull();
	}

	@Transactional
	@Override
	public void delete(Ontology ontology) {
		ontologyRepository.deleteById(ontology.getId());
	}

	@Override
	public String getRtdbFromOntology(String ontologyIdentification) {
		final Ontology ont = ontologyRepository.findByIdentification(ontologyIdentification);
		if (ont.getRtdbDatasource().equals(Ontology.RtdbDatasource.VIRTUAL))

			return ontologyvirtualRepository
					.findOntologyVirtualDatasourceByOntologyIdentification(ont.getIdentification()).getSgdb()
					.toString();
		else
			return ontologyRepository.findByIdentification(ontologyIdentification).getRtdbDatasource().name();

	}

	@Override
	public List<String> getDatasourcesRelationals() {
		return ontologyVirtualDatasourceRepository.findAllByOrderByDatasourceNameAsc().stream()
				.map(OntologyVirtualDatasource::getDatasourceName).collect(Collectors.toList());
	}

	@Override
	public List<String> getPublicDatasourcesRelationals() {
		return ontologyVirtualDatasourceRepository.findByIsPublicTrue().stream()
				.map(OntologyVirtualDatasource::getDatasourceName).collect(Collectors.toList());
	}

	@Override
	public OntologyVirtual getOntologyVirtualByOntologyId(Ontology ontology) {
		return ontologyvirtualRepository.findByOntologyId(ontology);
	}

	@Override
	public void checkOntologySchema(String schema) {
		ProcessingReport report;
		try {
			report = ontologyDataService.reportJsonSchemaValid(schema);
		} catch (final IOException e) {
			log.error("Could not parse json schema {}", e.getMessage());
			throw new OntologyDataJsonProblemException("Could not parse json schema");
		}
		if (!report.isSuccess())
			throw new OntologyDataJsonProblemException("Json schema is not valid: " + report.toString());

	}

	@Override
	public List<Ontology> getAllAuditOntologies() {
		return ontologyRepository.findByIdentificationStartingWith(ServiceUtils.AUDIT_COLLECTION_NAME);
	}

	@Override
	public boolean hasUserPermission(User user, AccessType access, Ontology ontology) {
		switch (access) {
		case ALL:
			return hasUserPermisionForChangeOntology(user, ontology);
		case INSERT:
			return hasUserPermissionForInsert(user, ontology);
		case QUERY:
			return hasUserPermissionForQuery(user, ontology);
		default:
			return false;
		}
	}

	@Override
	public void executeKPI(String user, String query, String ontology, String postProcessScript)
			throws Exception {

		final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
		
		// send query
		String queryTranslated = query;
		if (newQuery != null) {
			queryTranslated = newQuery.getQuery();
		}
		log.debug("Send query for ontology: " + ontology + " query:" + query + " for user:" + user);
		final String result = processQuery(query, getOntologyFromQuery(queryTranslated), ApiOperation.Type.GET.name(),
				"", "", user);

		if (result != null && !result.equals("error")) {
			// insert data
			log.debug("Insert result query for ontology: " + ontology + "  query:" + query + " for user:" + user);
			String output = result;
			if (postProcessScript != null && !"".equals(postProcessScript)) {
				final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
				try {
					final String scriptPostprocessFunction = "function postprocess(data){ " + postProcessScript + " }";
					engine.eval(scriptPostprocessFunction);
					final Invocable inv = (Invocable) engine;
					output = (String) inv.invokeFunction("postprocess", result);
				} catch (final ScriptException e) {
					log.error("ERROR from Scripting Post Process, Exception detected", e.getCause().getMessage());
				}
			}
			processQuery("", ontology, ApiOperation.Type.POST.name(), output, "", user);
		}
	}

	public String processQuery(String query, String ontologyID, String method, String body, String objectId,
			String user) {

		com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType operationType = null;
		if (method.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
			body = query;
			operationType = com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.QUERY;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
			operationType = com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.INSERT;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
			operationType = com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.UPDATE;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
			operationType = com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.DELETE;
		} else {
			operationType = com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.QUERY;
		}

		final com.minsait.onesait.platform.router.service.app.model.OperationModel model = com.minsait.onesait.platform.router.service.app.model.OperationModel
				.builder(ontologyID,
						com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType
								.valueOf(operationType.name()),
						user,
						com.minsait.onesait.platform.router.service.app.model.OperationModel.Source.INTERNAL_ROUTER)
				.body(body).queryType(QueryType.SQL).objectId(objectId).deviceTemplate("").build();
		final NotificationModel modelNotification = new NotificationModel();

		modelNotification.setOperationModel(model);

		final OperationResultModel result = routerService.query(modelNotification);

		if (result != null) {
			if ("ERROR".equals(result.getResult())) {
				return "error";
			}

			String ret = result.getResult();

			if (operationType == com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType.INSERT) {
				final JSONObject obj = new JSONObject(ret);
				if (obj.has(InsertResult.DATA_PROPERTY)) {
					ret = obj.get(InsertResult.DATA_PROPERTY).toString();
				}
			}

			return ret;
		} else {
			return null;
		}

	}

	@Override
	public String getOntologyFromQuery(String query) {
		query = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ").trim().replaceAll(" +", " ").replace(" FROM ",
				" from ");

		final String[] list = query.split("from ");

		if (list.length > 1) {
			for (int i = 1; i < list.length; i++) {
				if (!list[i].startsWith("(")) {
					int indexOf = list[i].toLowerCase().indexOf(' ', 0);
					final int indexOfCloseBracket = list[i].toLowerCase().indexOf(')', 0);
					indexOf = (indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf) ? indexOfCloseBracket
							: indexOf;
					if (indexOf == -1) {
						indexOf = list[i].length();
					}
					return list[i].substring(0, indexOf).trim();
				}
			}
		}
		
		if (query.contains("db.")) {
			String result = query.substring(query.indexOf("db.") + 3);
			result = result.substring(0, result.indexOf('.'));
			return result;
		}

		return "";
	}

	@Override
	public boolean existsOntology(String identificacion) {
		try {
			return ontologyRepository.findByIdentification(identificacion) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	@Override
	public Ontology getOntologyByIdentification(String identification) {
		return ontologyRepository.findByIdentification(identification);
	}

}
