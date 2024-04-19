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
package com.minsait.onesait.platform.config.services.ontology;

import java.io.IOException;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.dto.OntologyForList;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.DataModel.MainType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyDataAccess;
import com.minsait.onesait.platform.config.model.OntologyElastic;
import com.minsait.onesait.platform.config.model.OntologyElastic.PatternFunctionType;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyPresto;
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
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.DatasetResourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.OntologyDataAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyElasticRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyPrestoRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestHeadersRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestOperationParamRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestOperationRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestRepository;
import com.minsait.onesait.platform.config.repository.OntologyRestSecurityRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeseriesTimescaleAggregatesRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.repository.SubscriptionRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.datamodel.dto.DataModelDTO;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyFieldDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceDTO;
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
	@Lazy
	EntityDeletionService deletionService;
	@Autowired
	private AppRepository appRepository;
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
	private DatasetResourceRepository datasetResourceRepository;
	@Autowired
	@Lazy
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
	private OntologyDataAccessRepository ontologyDataAccessRepository;
	@Autowired
	@Lazy
	private OPResourceService resourceService;
	@Autowired
	private OntologyKPIRepository ontologyKpiRepository;
	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;
	@Autowired
	private QueryTemplateService queryTemplateService;
	@Value("${onesaitplatform.ontologies.schema.ignore-case-properties:false}")
	private boolean ignoreTitleCaseCheck;
	@Autowired
	@Lazy
	SecurityService securityService;
	@Autowired
	private OntologyElasticRepository elasticOntologyRepository;
	@Autowired
	private OntologyTimeseriesTimescaleAggregatesRepository OntologyTimeSeriesTimescaleAggregatesRepository;

	@Autowired
	ApiRepository apiRepository;

	@Autowired
	GadgetDatasourceRepository gadgetDatasourceRepository;

	@Autowired
	LayerRepository layerRepository;

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@Autowired
	OntologyPrestoRepository ontologyPrestoRepository;

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
	private static final String DESCRIPTION_STR = "description";

	public static final String KPI_TYPE = "kpi";
	public static final String TIMESERIES_TYPE = "timeseries";

	public static final String QUERY_SQL = "SQL";
	public static final String QUERY_NATIVE = "NATIVE";
	public static final String DATAMODEL_DEFAULT_NAME = "EmptyBase";
	public static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";

	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profileDetector;

	final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	private int defaultReplicas = 0;
	private int defaultShards = 5;

	@PostConstruct
	void initializeIt() {
		try {
			final String profile = profileDetector.getActiveProfile();
			final GlobalConfiguration globalConfiguration = configurationService.getGlobalConfiguration(profile);
			final Map<String, Object> database = globalConfiguration.getEnv().getDatabase();

			@SuppressWarnings("unchecked")
			final Map<String, Object> elasticsearch = (Map<String, Object>) database.get("elasticsearch");

			@SuppressWarnings("unchecked")
			final Map<String, Object> defaults = (Map<String, Object>) elasticsearch.get("defaults");

			defaultReplicas = (int) defaults.get("replicas");
			defaultShards = (int) defaults.get("shards");
		} catch (final Exception e) {
			log.warn("Error loading configuration values for elasticSearch indexes. Using defauts.");
		}
	}

	@Override
	public List<Ontology> getAllOntologies(String sessionUserId) {

		final User sessionUser = userService.getUser(sessionUserId);
		if (userService.isUserAdministrator(sessionUser)) {
			return ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return ontologyRepository.findByUserAndOntologyUserAccessAndAllPermissions(sessionUser);
		}
	}

	@Override
	public List<OntologyDTO> getAllOntologiesForListWithProjectsAccess(String sessionUserId) {

		final User sessionUser = userService.getUser(sessionUserId);
		final List<OntologyForList> ontologiesForList = ontologyRepository
				.findOntologyForListOrderByIdentificationAsc();
		if (!sessionUser.isAdmin()) {
			securityService.setSecurityToInputList(ontologiesForList, sessionUser, "Ontology");
		}
		final List<OntologyKPI> kpis = ontologyKpiRepository.findByUser(sessionUser);
		final Map<String, OntologyKPI> mapKpis = new HashMap<>();
		for (final OntologyKPI kpi : kpis) {
			mapKpis.put(kpi.getOntology().getId(), kpi);
		}
		final List<OntologyUserAccess> access = ontologyUserAccessRepository.findAll();

		final Map<String, List<OntologyUserAccess>> mapAccess = new HashMap<>();
		for (final OntologyUserAccess a : access) {
			if (mapAccess.containsKey(a.getOntology().getId())) {
				mapAccess.get(a.getOntology().getId()).add(a);
			} else {
				final List<OntologyUserAccess> accessList = new ArrayList<>();
				accessList.add(a);
				mapAccess.put(a.getOntology().getId(), accessList);
			}
		}

		final List<OntologyDTO> dtos = new ArrayList<>();
		for (final OntologyForList temp : ontologiesForList) {
			if (temp.getAccessType() != null) {

				final OntologyDTO obj = new OntologyDTO();
				obj.setActive(temp.isActive());
				obj.setCreatedAt(temp.getCreated_at());
				obj.setDescription(temp.getDescription());
				obj.setId(temp.getId());
				obj.setIdentification(temp.getIdentification());
				obj.setPublic(temp.isPublic());
				obj.setUpdatedAt(temp.getUpdated_at());
				obj.setAuthorizations(mapAccess.containsKey(obj.getId()));
				obj.setUser(temp.getUser());
				obj.setDataModel(temp.getDataModel());
				obj.setRtdbDatasource(temp.getRtdbDatasource());
				obj.setOntologyKPI(mapKpis.get(obj.getId()));
				obj.setOntologyUserAccesses(mapAccess.containsKey(obj.getId()) ? mapAccess.get(obj.getId())
						: new ArrayList<OntologyUserAccess>());
				dtos.add(obj);
			}
		}

		return dtos;
	}

	@Override
	public List<OntologyDTO> getAllOntologiesForList(String sessionUserId, String identification, String description) {

		final User sessionUser = userService.getUser(sessionUserId);
		List<OntologyForList> ontologiesForList = new ArrayList<>();
		List<OntologyUserAccess> access = new ArrayList<>();
		final Map<String, OntologyKPI> mapKpis = new HashMap<>();
		List<OntologyKPI> kpis = new ArrayList<>();
		final Map<String, List<OntologyUserAccess>> mapAccess = new HashMap<>();

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.isAdmin()) {
			ontologiesForList = ontologyRepository
					.findOntologiesForListByIdentificationLikeAndDescriptionLike(identification, description);
			kpis = ontologyKpiRepository.findAll();
		} else {
			ontologiesForList = ontologyRepository
					.findOntologiesForListByUserAndPermissionsANDIdentificationAndDescription(sessionUser,
							identification, description);
			securityService.setSecurityToInputList(ontologiesForList, sessionUser, "Ontology");
			kpis = ontologyKpiRepository.findByUser(sessionUser);
		}

		for (final OntologyKPI kpi : kpis) {
			mapKpis.put(kpi.getOntology().getId(), kpi);
		}

		access = ontologyUserAccessRepository.findAll();
		for (final OntologyUserAccess a : access) {
			if (mapAccess.containsKey(a.getOntology().getId())) {
				mapAccess.get(a.getOntology().getId()).add(a);
			} else {
				final List<OntologyUserAccess> accessList = new ArrayList<>();
				accessList.add(a);
				mapAccess.put(a.getOntology().getId(), accessList);
			}
		}

		final List<OntologyVirtual> virtualOntologies = ontologyvirtualRepository.findAll();
		final Map<String, OntologyVirtual> mapVirtual = new HashMap<>();
		for (final OntologyVirtual ov : virtualOntologies) {
			mapVirtual.put(ov.getOntologyId().getId(), ov);
		}
		final List<OntologyPresto> prestoOntologies = ontologyPrestoRepository.findAll();
		final Map<String, OntologyPresto> mapPresto = new HashMap<>();
		for (final OntologyPresto op : prestoOntologies) {
			mapPresto.put(op.getOntologyId().getId(), op);
		}

		final List<OntologyDTO> dtos = new ArrayList<>();
		for (final OntologyForList temp : ontologiesForList) {
			if (temp.getAccessType() != null) {
				final OntologyDTO obj = new OntologyDTO();
				obj.setActive(temp.isActive());
				obj.setCreatedAt(temp.getCreated_at());
				obj.setDescription(temp.getDescription());
				obj.setId(temp.getId());
				obj.setIdentification(temp.getIdentification());
				obj.setPublic(temp.isPublic());
				obj.setUpdatedAt(temp.getUpdated_at());
				obj.setAuthorizations(mapAccess.containsKey(obj.getId()));
				
				if(userService.isUserAdministrator(sessionUser) || sessionUser.getId().equals(temp.getUser().getId())) {
					obj.setIsAuthorizationsPermissions("ALL");		
				} else {
					
					if (temp.isPublic() == true ){
						obj.setIsAuthorizationsPermissions("QUERY");	
						}
				
					for (OntologyUserAccess permission : access) {
						if (permission.getUser().getId().equals(sessionUser.getId()) && permission.getOntology().getId().equalsIgnoreCase(temp.getId())) {
							obj.setIsAuthorizationsPermissions(permission.getOntologyUserAccessType().getName().toUpperCase());
						}
					}
				}
				
				obj.setUser(temp.getUser());
				obj.setDataModel(temp.getDataModel());
				obj.setRtdbDatasource(temp.getRtdbDatasource());
				obj.setOntologyKPI(mapKpis.get(obj.getId()));
				obj.setOntologyUserAccesses(mapAccess.containsKey(obj.getId()) ? mapAccess.get(obj.getId())
						: new ArrayList<OntologyUserAccess>());
				if (obj.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL) && mapVirtual.get(obj.getId()) != null) {
					obj.setRtdbDatasourceType(mapVirtual.get(obj.getId()).getDatasourceId().getSgdb().toString());
				} else if (obj.getRtdbDatasource().equals(RtdbDatasource.PRESTO)
						&& mapPresto.get(obj.getId()) != null) {
					obj.setRtdbDatasourceType(mapPresto.get(obj.getId()).getDatasourceCatalog().toUpperCase());
				}
				dtos.add(obj);
			}
		}

		return dtos;
	}

	@Override
	public List<OntologyDTO> getOntologiesForListByUser(String sessionUserId, String identification,
			String description) {

		final User sessionUser = userService.getUser(sessionUserId);
		List<OntologyForList> ontologiesForList = new ArrayList<>();
		List<OntologyUserAccess> access = new ArrayList<>();
		final Map<String, List<OntologyUserAccess>> mapAccess = new HashMap<>();
		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		ontologiesForList = ontologyRepository
				.findOntologiesForListByUserAndPermissionsANDIdentificationAndDescriptionNoPublic(sessionUser,
						identification, description);
		securityService.setSecurityToInputList(ontologiesForList, sessionUser, "Ontology");
		final List<OntologyKPI> kpis = ontologyKpiRepository.findByUser(sessionUser);
		final Map<String, OntologyKPI> ids = new HashMap<>();
		for (final OntologyKPI kpi : kpis) {
			ids.put(kpi.getOntology().getId(), kpi);
		}
		access = ontologyUserAccessRepository.findAll();
		for (final OntologyUserAccess a : access) {
			if (mapAccess.containsKey(a.getOntology().getId())) {
				mapAccess.get(a.getOntology().getId()).add(a);
			} else {
				final List<OntologyUserAccess> accessList = new ArrayList<>();
				accessList.add(a);
				mapAccess.put(a.getOntology().getId(), accessList);
			}
		}
		final List<OntologyDTO> dtos = new ArrayList<>();
		for (final OntologyForList temp : ontologiesForList) {
			if (temp.getAccessType() != null) {

				final OntologyDTO obj = new OntologyDTO();
				obj.setActive(temp.isActive());
				obj.setCreatedAt(temp.getCreated_at());
				obj.setDescription(temp.getDescription());
				obj.setId(temp.getId());
				obj.setIdentification(temp.getIdentification());
				obj.setPublic(temp.isPublic());
				obj.setUpdatedAt(temp.getUpdated_at());
				obj.setAuthorizations(mapAccess.containsKey(temp.getId()));
				obj.setUser(temp.getUser());
				obj.setDataModel(temp.getDataModel());
				obj.setRtdbDatasource(temp.getRtdbDatasource());
				obj.setOntologyKPI(ids.get(obj.getId()));
				obj.setOntologyUserAccesses(mapAccess.containsKey(obj.getId()) ? mapAccess.get(obj.getId())
						: new ArrayList<OntologyUserAccess>());

				dtos.add(obj);
			}
		}

		return dtos;
	}

	@Override
	public List<OntologyDTO> getOntologiesForListByUserPropietary(String sessionUserId, String identification,
			String description) {

		final User sessionUser = userService.getUser(sessionUserId);
		List<OntologyForList> ontologiesForList = new ArrayList<>();
		List<OntologyUserAccess> access = new ArrayList<>();
		final Map<String, List<OntologyUserAccess>> mapAccess = new HashMap<>();
		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (description.equals("") && identification.equals("")) {
			ontologiesForList = ontologyRepository.findOntologiesForListByUser(sessionUser);
		}

		if (!description.equals("") && !identification.equals("")) {
			ontologiesForList = ontologyRepository.findOntologiesForListByUserAndIdentificationLikeAndDescriptionLike(
					sessionUser, identification, description);
		}

		if (description.equals("") && !identification.equals("")) {
			ontologiesForList = ontologyRepository.findOntologiesForListByUserAndIdentificationLike(sessionUser,
					identification);
		}

		if (!description.equals("") && identification.equals("")) {
			ontologiesForList = ontologyRepository.findOntologiesForListByUserAndDescriptionLike(sessionUser,
					description);
		}

		securityService.setSecurityToInputList(ontologiesForList, sessionUser, "Ontology");
		final List<OntologyKPI> kpis = ontologyKpiRepository.findByUser(sessionUser);
		final Map<String, OntologyKPI> ids = new HashMap<>();
		for (final OntologyKPI kpi : kpis) {
			ids.put(kpi.getOntology().getId(), kpi);
		}

		final List<OntologyVirtual> virtualOntologies = ontologyvirtualRepository.findByUser(sessionUser);
		final Map<String, OntologyVirtual> mapVirtual = new HashMap<>();
		for (final OntologyVirtual ov : virtualOntologies) {
			mapVirtual.put(ov.getOntologyId().getId(), ov);
		}
		final List<OntologyPresto> prestoOntologies = ontologyPrestoRepository.findByUser(sessionUser);
		final Map<String, OntologyPresto> mapPresto = new HashMap<>();
		for (final OntologyPresto op : prestoOntologies) {
			mapPresto.put(op.getOntologyId().getId(), op);
		}

		access = ontologyUserAccessRepository.findAll();
		for (final OntologyUserAccess a : access) {
			if (mapAccess.containsKey(a.getOntology().getId())) {
				mapAccess.get(a.getOntology().getId()).add(a);
			} else {
				final List<OntologyUserAccess> accessList = new ArrayList<>();
				accessList.add(a);
				mapAccess.put(a.getOntology().getId(), accessList);
			}
		}
		final List<OntologyDTO> dtos = new ArrayList<>();
		for (final OntologyForList temp : ontologiesForList) {
			if (temp.getAccessType() != null) {

				final OntologyDTO obj = new OntologyDTO();
				obj.setActive(temp.isActive());
				obj.setCreatedAt(temp.getCreated_at());
				obj.setDescription(temp.getDescription());
				obj.setId(temp.getId());
				obj.setIdentification(temp.getIdentification());
				obj.setPublic(temp.isPublic());
				obj.setUpdatedAt(temp.getUpdated_at());
				obj.setAuthorizations(mapAccess.containsKey(temp.getId()));

				obj.setIsAuthorizationsPermissions("ALL");		

				obj.setUser(temp.getUser());
				obj.setDataModel(temp.getDataModel());
				obj.setRtdbDatasource(temp.getRtdbDatasource());
				obj.setOntologyKPI(ids.get(obj.getId()));
				obj.setOntologyUserAccesses(mapAccess.containsKey(obj.getId()) ? mapAccess.get(obj.getId())
						: new ArrayList<OntologyUserAccess>());
				if (obj.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL) && mapVirtual.get(obj.getId()) != null) {
					obj.setRtdbDatasourceType(mapVirtual.get(obj.getId()).getDatasourceId().getSgdb().toString());
				} else if (obj.getRtdbDatasource().equals(RtdbDatasource.PRESTO)
						&& mapPresto.get(obj.getId()) != null) {
					obj.setRtdbDatasourceType(mapPresto.get(obj.getId()).getDatasourceCatalog().toUpperCase());
				}
				dtos.add(obj);
			}
		}

		return dtos;
	}

	@Override
	public List<OntologyForList> getOntologiesForListByUserId(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.isAdmin()) {
			return ontologyRepository.findOntologyForListOrderByIdentificationAsc();
		} else {
			return ontologyRepository
					.findOntologiesForListByUserAndPermissionsANDIdentificationAndDescription(sessionUser, "", "");
		}

	}

	@Override
	public List<Ontology> getOntologiesByUserId(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (userService.isUserAdministrator(sessionUser)) {
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

		if (userService.isUserAdministrator(sessionUser)) {
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

		if (userService.isUserAdministrator(sessionUser)) {
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
		final List<DataModel> lDataModels = dataModelRepository.findByIdentification(datamodel);
		if (lDataModels == null || lDataModels.isEmpty()) {
			log.warn("DataModel {} not found, no ontologies will be returned", datamodel);
			return new ArrayList<>();
		} else if (lDataModels.size() > 1) {
			log.warn("Several DataModels were found for name {}, using first one", datamodel);
		}

		if (userService.isUserAdministrator(sessionUser)) {

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
			final List<OntologyKPI> lOntologyKp = sessionUser.isAdmin() ? ontologyKpiRepository.findAll()
					: ontologyKpiRepository.findByUser(sessionUser);

			lOntologyKp.forEach(o -> ontologiesList.add(o.getOntology()));
			break;

		case TIMESERIES_TYPE:
			final List<OntologyTimeSeries> lOntologyTs = sessionUser.isAdmin() ? ontologyTimeSeriesRepository.findAll()
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
		if (userService.isUserAdministrator(user)) {
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
	public List<Ontology> getAllOntologiesByUser(String userId) {

		List<Ontology> ontologies;
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			ontologies = ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			ontologies = ontologyRepository.findByUserOrderByIdentificationAsc(user);
		}

		return ontologies;
	}

	@Override
	public List<String> getIdentificationsByUserAndPermissions(String userId) {
		List<String> ontologies;
		final User user = userService.getUser(userId);
		if (userService.isUserAdministrator(user)) {
			ontologies = ontologyRepository.findAllIdentifications();
		} else {
			ontologies = ontologyRepository.findIdentificationsByUserAndPermissions(user);
		}

		return ontologies;
	}

	@Override
	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description) {
		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		List<OPResourceDTO> ontologies;
		final User user = userService.getUser(userId);
		if (user.isAdmin()) {
			ontologies = ontologyRepository.findAllDto(identification, description);
		} else {
			ontologies = ontologyRepository.findDtoByUserAndPermissions(user, identification, description);
		}
		return ontologies;
	}

	@Override
	public Ontology getOntologyById(String ontologyId, String sessionUserId) {
		final Ontology ontology = ontologyRepository.findById(ontologyId).orElse(null);
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
	public List<OntologyKPI> getOntologyKpisByOntology(Ontology ontology) {
		return ontologyKpiRepository.findByOntology(ontology);
	}

	@Override
	public OntologyRest getOntologyRestByOntologyId(Ontology ontologyId) {
		return ontologyRestRepo.findByOntologyId(ontologyId);
	}

	@Override
	public OntologyRestSecurity getOntologyRestSecurityByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestSecurityRepo.findById(ontologyRest.getSecurityId().getId()).orElse(null);
	}

	@Override
	public OntologyRestHeaders getOntologyRestHeadersByOntologyRest(OntologyRest ontologyRest) {
		return ontologyRestHeadersRepo.findById(ontologyRest.getHeaderId().getId()).orElse(null);
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
		return DataModelDTO.fromDataModels(dataModelRepository.findByIdentification(DATAMODEL_DEFAULT_NAME));
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
	public OntologyVirtualDatasource getOntologyVirtualDatasourceByName(String datasourceName) {
		return ontologyVirtualDatasourceRepository.findByIdentification(datasourceName);
	}

	@Override
	public boolean hasUserPermissionForQuery(User user, Ontology ontology) {
		if (ontology == null || user == null) {
			return false;
		}
		if (userService.isUserAdministrator(user)) {
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
	public String getElementsAssociated(String ontologyId) {
		final JSONArray elements = new JSONArray();

		final Ontology ontology = ontologyRepository.findById(ontologyId).get();
		if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
			final OntologyVirtual ontologyVirtual = ontologyvirtualRepository.findByOntologyId(ontology);

			final JSONObject e = new JSONObject();
			e.put("id", ontologyVirtual.getDatasourceId().getId());
			e.put("identification", ontologyVirtual.getDatasourceId().getIdentification());
			e.put("type", ontologyVirtual.getDatasourceId().getClass().getSimpleName());
			elements.put(e);
			return elements.toString();
		} else {
			return elements.toString();
		}

	}

	@Override
	public boolean hasUserPermissionForQuery(String userId, Ontology ontology) {
		final User user = userService.getUser(userId);
		return hasUserPermissionForQuery(user, ontology);
	}

	@Override
	public boolean hasUserPermissionForQuery(String userId, String ontologyIdentificator) {
		final Ontology ontology = getOntologyByIdentification(ontologyIdentificator);
		return hasUserPermissionForQuery(userId, ontology);
	}

	@Override
	public boolean hasUserPermissionForInsert(User user, Ontology ontology) {
		if (ontology == null || user == null) {
			return false;
		}
		if (userService.isUserAdministrator(user)) {
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
	public boolean hasEncryptionEnabled(String ontology) {
		final Ontology ont = getOntologyByIdentification(ontology);
		if (ont == null) {
			return false;
		} else {
			return ont.isAllowsCypherFields();
		}
	}

	@Override
	public Map<String, String> getOntologyFields(String identification, String sessionUserId) throws IOException {

		Map<String, String> fields = new TreeMap<>();
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		JsonNode jsonNode = mapper.readTree(ontology.getJsonSchema());

		// Predefine Path to data properties
		if (jsonNode != null) {
			if (!jsonNode.path(DATOS_STR).path(PROP_STR).isMissingNode()) {
				jsonNode = jsonNode.path(DATOS_STR).path(PROP_STR);
			} else {
				jsonNode = jsonNode.path(PROP_STR);
			}
			if (!jsonNode.path("type").isMissingNode() && !jsonNode.path("type").path("enum").isMissingNode()
					&& jsonNode.path("type").path("enum").get(0).asText().equalsIgnoreCase("FeatureCollection")) {
				fields.put("FeatureCollection", "FeatureCollection");
			} else {
				fields = extractFieldsFromJsonNode(jsonNode);
			}
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
				if (jsonNode.path(property).get(FORMAT_STR) != null) {
					fields.put(property, "date");
				} else {
					fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
				}
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
				if (ontology.getJsonSchema().contains("'")) {
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
				}
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

	@Override
	public Map<String, OntologyFieldDTO> getOntologyFieldsAndDesc(String identification, String sessionUserId)
			throws IOException {
		Map<String, OntologyFieldDTO> fields = new TreeMap<>();
		String context = "";
		final Ontology ontology = getOntologyByIdentification(identification, sessionUserId);
		if (ontology != null) {

			JsonNode jsonNode = null;
			try {

				jsonNode = mapper.readTree(ontology.getJsonSchema());

			} catch (final Exception e) {
				if (ontology.getJsonSchema().contains("'")) {
					jsonNode = mapper.readTree(ontology.getJsonSchema().replaceAll("'", "\""));
				}
			}

			// Predefine Path to data properties
			if (jsonNode != null) {
				if (!jsonNode.path(DATOS_STR).path(PROP_STR).isMissingNode()) {
					context = jsonNode.path(PROP_STR).fields().next().getKey();
					jsonNode = jsonNode.path(DATOS_STR).path(PROP_STR);
				} else {
					jsonNode = jsonNode.path(PROP_STR);
				}

				fields = extractFieldsWithDescriptionQueryToolFromJsonNode(jsonNode);
			}
		}
		// add Context to fields for query
		if (!context.equals("")) {

			final Map<String, OntologyFieldDTO> fieldsForQuery = new TreeMap<>();
			for (final Map.Entry<String, OntologyFieldDTO> field : fields.entrySet()) {
				final String key = field.getKey();
				final OntologyFieldDTO value = field.getValue();
				value.setPath(context + "." + value.getPath());
				fieldsForQuery.put(context + "." + key, value);
			}
			fields = fieldsForQuery;
		}
		return fields;
	}

	private Map<String, OntologyFieldDTO> extractFieldsWithDescriptionQueryToolFromJsonNode(JsonNode jsonNode) {
		final Map<String, OntologyFieldDTO> fields = new TreeMap<>();
		final Iterator<String> iterator = jsonNode.fieldNames();
		String property;
		while (iterator.hasNext()) {
			property = iterator.next();
			if (jsonNode.path(property).toString().equals("{}")) {
				final OntologyFieldDTO dto = new OntologyFieldDTO();
				dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
				dto.setPath(property);
				dto.setType(OBJ_STR);
				fields.put(property, dto);

			} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(OBJ_STR)) {
				final OntologyFieldDTO dto = new OntologyFieldDTO();
				dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
				dto.setPath(property);
				dto.setType(jsonNode.path(property).get(TYPE_STR).asText());
				fields.put(property, dto);

				extractSubFieldsAndDescriptionFromJson(fields, jsonNode, property, property, false, true);
			} else if (jsonNode.path(property).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsAndDescriptionFromJson(fields, jsonNode, property, property, true, true);
			} else if (jsonNode.path(property).get(TYPE_STR) instanceof com.fasterxml.jackson.databind.node.ArrayNode) {
				final com.fasterxml.jackson.databind.node.ArrayNode types = (com.fasterxml.jackson.databind.node.ArrayNode) jsonNode
						.path(property).get(TYPE_STR);
				String type = "";
				for (int i = 0; i < types.size(); i++) {
					if (!types.get(i).asText().equals("null")) {
						type = types.get(i).asText();
					}
				}
				final OntologyFieldDTO dto = new OntologyFieldDTO();
				dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
				dto.setPath(property);
				dto.setType(type);
				fields.put(property, dto);
			} else {
				if (jsonNode.path(property).get(FORMAT_STR) != null) {
					final OntologyFieldDTO dto = new OntologyFieldDTO();
					dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
					dto.setPath(property);
					dto.setType("date");
					fields.put(property, dto);
				} else {

					final OntologyFieldDTO dto = new OntologyFieldDTO();
					dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
					dto.setPath(property);
					dto.setType(jsonNode.path(property).get(TYPE_STR).asText());
					fields.put(property, dto);
				}
			}

		}
		return fields;
	}

	private String getDescription(JsonNode jsonNode) {
		try {
			return jsonNode.asText();
		} catch (final Exception e) {
			return null;
		}
	}

	private Map<String, OntologyFieldDTO> extractSubFieldsAndDescriptionFromJson(Map<String, OntologyFieldDTO> fields,
			JsonNode jsonNode, String property, String parentField, boolean isPropertyArray, boolean addTypeObject) {
		if (isPropertyArray) {
			if (!jsonNode.path(property).path(ITEMS_STR).path(PROP_STR).isMissingNode()) {
				jsonNode = jsonNode.path(property).path(ITEMS_STR).path(PROP_STR);
			} else if (!jsonNode.path(property).path(PROP_STR).isMissingNode()) {
				jsonNode = jsonNode.path(property).path(PROP_STR);
			} else {
				jsonNode = jsonNode.path(property).path(ITEMS_STR);
				final int size = jsonNode.size();
				try {
					for (int i = 0; i < size; i++) {
						final OntologyFieldDTO dto = new OntologyFieldDTO();
						dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
						dto.setPath(parentField + "." + i);
						dto.setType(jsonNode.path(i).get(TYPE_STR).asText());
						fields.put(dto.getPath(), dto);

					}
				} catch (final Exception e) {
					final OntologyFieldDTO dto = new OntologyFieldDTO();
					dto.setDescription(getDescription(jsonNode.path(property).get(DESCRIPTION_STR)));
					dto.setPath(parentField + "." + 0);
					dto.setType(jsonNode.get(TYPE_STR).asText());
					fields.put(dto.getPath(), dto);

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
				if (addTypeObject) {
					final OntologyFieldDTO dto = new OntologyFieldDTO();
					dto.setDescription(getDescription(jsonNode.path(subProperty).get(DESCRIPTION_STR)));
					dto.setPath(parentField + "." + subProperty);
					dto.setType(jsonNode.path(subProperty).get(TYPE_STR).asText());
					fields.put(dto.getPath(), dto);
				}
				extractSubFieldsAndDescriptionFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty,
						false, addTypeObject);
			} else if (jsonNode.path(subProperty).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsAndDescriptionFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty,
						true, addTypeObject);

			} else {
				if (subProperty.equals("$date")) {
					final OntologyFieldDTO dto = new OntologyFieldDTO();
					dto.setDescription(getDescription(jsonNode.path(subProperty).get(DESCRIPTION_STR)));
					dto.setPath(parentField);
					dto.setType("date");
					fields.put(dto.getPath(), dto);

				} else {
					if (jsonNode.path(subProperty).get(FORMAT_STR) != null) {

						final OntologyFieldDTO dto = new OntologyFieldDTO();
						dto.setDescription(getDescription(jsonNode.path(subProperty).get(DESCRIPTION_STR)));
						dto.setPath(parentField + "." + subProperty);
						dto.setType("date");
						fields.put(dto.getPath(), dto);
					} else {
						final OntologyFieldDTO dto = new OntologyFieldDTO();
						dto.setDescription(getDescription(jsonNode.path(subProperty).get(DESCRIPTION_STR)));
						dto.setPath(parentField + "." + subProperty);
						dto.setType(jsonNode.path(subProperty).get(TYPE_STR).asText());
						fields.put(dto.getPath(), dto);
					}
				}
			}
		}
		return fields;

	}

	private Map<String, String> extractFieldsQueryToolFromJsonNode(JsonNode jsonNode) {
		final Map<String, String> fields = new TreeMap<>();
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
				if (jsonNode.path(property).get(FORMAT_STR) != null) {
					fields.put(property, "date");
				} else {
					fields.put(property, jsonNode.path(property).get(TYPE_STR).asText());
				}
			}

		}
		return fields;
	}

	@Override
	public void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config,
			boolean hasDocuments) {
		ontologyRepository.findById(ontology.getId()).ifPresent(o -> {
			if (hasDocuments) {
				if (!ontology.getRtdbDatasource().equals(RtdbDatasource.KUDU)) {
					ontologyDataService.checkRequiredFields(o.getJsonSchema(), ontology.getJsonSchema());
				} else {
					ontologyDataService.checkSameSchema(o.getJsonSchema(), ontology.getJsonSchema());
				}
			}
			ontology.setCreatedAt(o.getCreatedAt());
			ontology.setPartitionKey(o.getPartitionKey());
			updateOntology(ontology, sessionUserId, config);
		});

	}

	@Override
	public void updateOntology(Ontology ontology, String sessionUserId, OntologyConfiguration config) {
		ontologyRepository.findById(ontology.getId()).ifPresent(ontologyDb -> {
			final User sessionUser = userService.getUser(sessionUserId);
			String objectId = null;
			if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				objectId = config.getObjectId();
			}

			if (ontologyDb != null) {
				if (hasUserPermisionForChangeOntology(sessionUser, ontologyDb)) {
					checkOntologySchema(ontology.getJsonSchema());
					if (!ignoreTitleCaseCheck) {
						ontologyDataService.checkTitleCaseSchema(ontology.getJsonSchema());
					}

					ontology.setUser(ontologyDb.getUser());

					ontology.setOntologyUserAccesses(ontologyDb.getOntologyUserAccesses());

					if (ontology.isRtdbToHdb()) {
						ontology.setRtdbClean(true);
					} else {
						ontology.setRtdbToHdbStorage(null);
					}

					if (ontology.isRtdbClean() && ontology.getRtdbCleanLapse().equals(RtdbCleanLapse.NEVER)) {
						ontology.setRtdbCleanLapse(RtdbCleanLapse.ONE_MONTH);
					}

					ontology.setIdentification(ontologyDb.getIdentification());
					ontologyRepository.saveAndFlush(ontology);
					if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
						createRestOntology(ontologyDb, config);
					} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
						final OntologyVirtual ontologyVirtual = ontologyvirtualRepository.findByOntologyId(ontologyDb);
						ontologyVirtual.setOntologyId(ontology);
						ontologyVirtual.setObjectId(objectId);
						ontologyVirtual.setObjectGeometry(config.getObjectGeometry());
						ontologyvirtualRepository.save(ontologyVirtual);
					}
				} else {
					throw new OntologyServiceException(USER_UNAUTH_STR);
				}
			} else {
				throw new OntologyServiceException("Ontology does not exist");
			}
		});

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
				final DataModel dataModel = dataModelRepository.findById(ontology.getDataModel().getId()).orElse(null);
				ontology.setDataModel(dataModel);
			} else {
				final DataModel dataModel = dataModelRepository.findByIdentification(DATAMODEL_DEFAULT_NAME).get(0);
				ontology.setDataModel(dataModel);
			}

			if (!ignoreTitleCaseCheck && !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				ontologyDataService.checkTitleCaseSchema(ontology.getJsonSchema());
			}

			ontology.setPartitionKey(config == null ? null : config.getPartitionKey());
			final User user = userService.getUser(ontology.getUser().getUserId());
			if (user != null) {
				ontology.setUser(user);
				ontology = ontologyRepository.saveAndFlush(ontology);
				if (ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)) {
					createRestOntology(ontology, config);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					createVirtualOntology(ontology, config.getDatasource(), config.getDatasourceTableName(),
							config.getDatasourceDatabase(), config.getDatasourceSchema(), config.getObjectId(),
							config.getObjectGeometry());
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.ELASTIC_SEARCH)) {
					createElasticOntology(ontology, config);
				} else if (ontology.getRtdbDatasource().equals(RtdbDatasource.PRESTO)) {
					createPrestoOntology(ontology, VirtualDatasourceType.PRESTO.toString(),
							config.getDatasourceTableName(), config.getDatasourceCatalog(),
							config.getDatasourceSchema());
				}
			} else {
				throw new OntologyServiceException("Invalid user");
			}
		} else {
			throw new OntologyServiceException(
					"Ontology with identification: " + ontology.getIdentification() + " exists");
		}

	}

	private void createElasticOntology(Ontology ontology, OntologyConfiguration config) {
		// add default values if config does not have them set: shards & replicas
		int shards = defaultShards;
		int replicas = defaultReplicas;
		int substringStart = 0;
		int substringEnd = -1;
		PatternFunctionType patternFunctionType = PatternFunctionType.NONE;
		if (config == null) {
			config = new OntologyConfiguration();
		}
		if (config.getShards() == null || config.getShards().isEmpty() || !config.isAllowsCustomElasticConfig()) {
			config.setShards(String.valueOf(defaultShards));
		} else {
			try {
				shards = Integer.parseInt(config.getShards());
			} catch (final Exception e) {
				log.error("Invalid shards value '{}' for ElasticSearch Ontology {}", config.getShards(),
						ontology.getIdentification());
				throw new OntologyServiceException("Shards value '" + config.getShards() + "' is not valid.");
			}
		}
		if (config.getReplicas() == null || config.getReplicas().isEmpty() || !config.isAllowsCustomElasticConfig()) {
			config.setReplicas(String.valueOf(defaultReplicas));
		} else {
			try {
				replicas = Integer.parseInt(config.getReplicas());
			} catch (final Exception e) {
				log.error("Invalid replicas value '{}' for ElasticSearch Ontology {}", config.getReplicas(),
						ontology.getIdentification());
				throw new OntologyServiceException("Replicas value '" + config.getReplicas() + "' is not valid.");
			}
		}
		if (config.getSubstringStart() != null) {
			try {
				substringStart = Integer.parseInt(config.getSubstringStart());
			} catch (final Exception e) {
				log.error("Invalid substring start value '{}' for ElasticSearch Ontology {}",
						config.getSubstringStart(), ontology.getIdentification());
				throw new OntologyServiceException(
						"Substring start value '" + config.getSubstringStart() + "' is not valid.");
			}
		}
		if (config.getSubstringEnd() != null) {
			try {
				substringEnd = Integer.parseInt(config.getSubstringEnd());
			} catch (final Exception e) {
				log.error("Invalid substring end value '{}' for ElasticSearch Ontology {}", config.getSubstringEnd(),
						ontology.getIdentification());
				throw new OntologyServiceException(
						"Substring end value '" + config.getSubstringEnd() + "' is not valid.");
			}
		}
		if (substringEnd != -1 && substringEnd <= substringStart) {
			log.error(
					"Invalid substring end value '{}' for ElasticSearch Ontology {}. End value must be greater than start value.",
					config.getSubstringEnd(), ontology.getIdentification());
			throw new OntologyServiceException("Substring end value '" + config.getSubstringEnd()
					+ "' is not valid. End value must be greater than start value.");
		}

		if (config.getPatternFunction() != null && !config.getPatternFunction().isEmpty()) {
			patternFunctionType = PatternFunctionType.valueOf(config.getPatternFunction());
		}

		final OntologyElastic elasticOntology = new OntologyElastic();
		elasticOntology.setShards(shards);
		elasticOntology.setReplicas(replicas);
		elasticOntology.setOntologyId(ontology);
		elasticOntology.setCustomConfig(config.isAllowsCustomElasticConfig());
		elasticOntology.setTemplateConfig(config.isAllowsTemplateConfig());
		elasticOntology.setPatternField(config.getPatternField());
		elasticOntology.setPatternFunction(patternFunctionType);
		elasticOntology.setSubstringStart(substringStart);
		elasticOntology.setSubstringEnd(substringEnd);
		elasticOntology.setCustomIdConfig(config.isAllowsCustomIdConfig());
		elasticOntology.setIdField(config.getCustomIdField());
		elasticOntology.setAllowsUpsertById(config.isAllowsUpsertById());
		elasticOntologyRepository.save(elasticOntology);
	}

	private void createVirtualOntology(Ontology ontology, String datasourceName, String datasourceTableName,
			String datasourceDatabase, String datasourceSchema, String objectId, String objectGeometry) {
		if (objectId == null || objectId.equals("")) {
			objectId = null;
		}
		final OntologyVirtualDatasource datasource = ontologyVirtualDatasourceRepository
				.findByIdentification(datasourceName);

		if (datasource != null) {
			final OntologyVirtual ontologyVirtual = new OntologyVirtual();
			ontologyVirtual.setDatasourceId(datasource);
			ontologyVirtual.setDatasourceTableName(datasourceTableName);
			ontologyVirtual.setDatasourceDatabase(datasourceDatabase);
			ontologyVirtual.setDatasourceSchema(datasourceSchema);
			ontologyVirtual.setOntologyId(ontology);
			ontologyVirtual.setObjectId(objectId);
			ontologyVirtual.setObjectGeometry(objectGeometry);
			ontologyvirtualRepository.save(ontologyVirtual);
		} else {
			throw new OntologyServiceException("Datasource " + datasourceName + " not found.");
		}
	}

	private void createPrestoOntology(Ontology ontology, String datasourceName, String datasourceTableName,
			String datasourceCatalog, String datasourceSchema) {
		final OntologyPresto ontologyPresto = new OntologyPresto();
		ontologyPresto.setDatasourceTableName(datasourceTableName);
		ontologyPresto.setDatasourceCatalog(datasourceCatalog);
		ontologyPresto.setDatasourceSchema(datasourceSchema);
		ontologyPresto.setOntologyId(ontology);
		ontologyPrestoRepository.save(ontologyPresto);
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
					if (config.getAuthMethod().equalsIgnoreCase("apiKey")
							|| config.getAuthMethod().equalsIgnoreCase("api_key")) {
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
				headersConfig = new JSONArray(config.getHeaders()[0]).toString();
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

			ontologyRestOperationRepo.saveAll(operationsList);

			ontologyRestOperationParamRepo.saveAll(paramsRestOperations);

		} catch (final Exception e) {
			throw new OntologyServiceException("Problems creating the external rest ontology", e);

		}

	}

	private Map<String, String> extractSubFieldsFromJson(Map<String, String> fields, JsonNode jsonNode, String property,
			String parentField, boolean isPropertyArray, boolean addTypeObject) {
		if (isPropertyArray) {
			if (!jsonNode.path(property).path(ITEMS_STR).path(PROP_STR).isMissingNode()) {
				jsonNode = jsonNode.path(property).path(ITEMS_STR).path(PROP_STR);
			} else if (!jsonNode.path(property).path(PROP_STR).isMissingNode()) {
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
				if (addTypeObject) {
					fields.put(parentField + "." + subProperty, jsonNode.path(subProperty).get(TYPE_STR).asText());
				}
				extractSubFieldsFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty, false,
						addTypeObject);
			} else if (jsonNode.path(subProperty).get(TYPE_STR).asText().equals(ARRAY_STR)) {
				extractSubFieldsFromJson(fields, jsonNode, subProperty, parentField + "." + subProperty, true,
						addTypeObject);

			} else {
				if (subProperty.equals("$date")) {
					fields.put(parentField, "date");
				} else {
					if (jsonNode.path(subProperty).get(FORMAT_STR) != null) {
						fields.put(parentField + "." + subProperty, "date");
					} else {
						fields.put(parentField + "." + subProperty, jsonNode.path(subProperty).get(TYPE_STR).asText());
					}

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
		final Optional<Ontology> opt = ontologyRepository.findById(ontologyId);
		if (!opt.isPresent()) {
			return false;
		}
		final Ontology ontology = opt.get();
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

		final Optional<Ontology> opt = ontologyRepository.findById(ontologyId);
		if (!opt.isPresent()) {
			throw new OntologyServiceException("Ontology does not exist");
		}
		final Ontology ontology = opt.get();
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
		final Optional<OntologyUserAccess> opt = ontologyUserAccessRepository.findById(userAccessId);
		if (!opt.isPresent()) {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
		final OntologyUserAccess userAccess = opt.get();
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
		final Optional<OntologyUserAccess> opt = ontologyUserAccessRepository.findById(userAccessId);
		if (!opt.isPresent()) {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
		final OntologyUserAccess userAccess = opt.get();

		if (hasUserPermisionForChangeOntology(sessionUser, userAccess.getOntology())) {
			final Set<OntologyUserAccess> accesses = userAccess.getOntology().getOntologyUserAccesses().stream()
					.filter(a -> !a.getId().equals(userAccess.getId())).collect(Collectors.toSet());
			ontologyRepository.findById(userAccess.getOntology().getId()).ifPresent(ontology -> {
				ontology.getOntologyUserAccesses().clear();
				ontology.getOntologyUserAccesses().addAll(accesses);
				ontologyRepository.save(ontology);
			});

		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	@Modifying
	public void updateOntologyUserAccess(String userAccessId, String typeName, String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		final Optional<OntologyUserAccess> opt = ontologyUserAccessRepository.findById(userAccessId);
		if (!opt.isPresent()) {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
		final OntologyUserAccess userAccess = opt.get();
		final List<OntologyUserAccessType> types = ontologyUserAccessTypeRepository.findByName(typeName);
		if (!CollectionUtils.isEmpty(types)) {
			if (hasUserPermisionForChangeOntology(sessionUser, userAccess.getOntology())) {
				final OntologyUserAccessType typeDB = types.get(0);
				userAccess.setOntologyUserAccessType(typeDB);
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
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (ontology.getUser().getUserId().equals(user.getUserId())) {
			return true;
		} else {
			final OntologyUserAccess userAuthorization = ontologyUserAccessRepository.findByOntologyAndUser(ontology,
					user);
			if (userAuthorization != null && OntologyUserAccessType.Type.valueOf(
					userAuthorization.getOntologyUserAccessType().getName()) == OntologyUserAccessType.Type.ALL) {
				return true;
			}

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
		return ontologyId.matches(regExp);
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
		if (ont.getRtdbDatasource().equals(Ontology.RtdbDatasource.VIRTUAL)) {
			return ontologyvirtualRepository
					.findOntologyVirtualDatasourceByOntologyIdentification(ont.getIdentification()).getSgdb()
					.toString();
		} else {
			return ontologyRepository.findByIdentification(ontologyIdentification).getRtdbDatasource().name();
		}

	}

	@Override
	public List<VirtualDatasourceDTO> getDatasourcesRelationals() {
		final List<VirtualDatasourceDTO> virtualDatasetDTOList = new ArrayList<>();
		final List<OntologyVirtualDatasource> virtualDatasources = ontologyVirtualDatasourceRepository
				.findAllByOrderByIdentificationAsc();

		for (final OntologyVirtualDatasource ontologyVirtualDatasource : virtualDatasources) {
			final VirtualDatasourceDTO virtualDatasetDTO = new VirtualDatasourceDTO(ontologyVirtualDatasource);
			virtualDatasetDTOList.add(virtualDatasetDTO);
		}

		return virtualDatasetDTOList;
	}

	@Override
	public List<VirtualDatasourceDTO> getPublicOrOwnedDatasourcesRelationals(String sessionUserId) {
		final List<VirtualDatasourceDTO> virtualDatasetDTOList = new ArrayList<>();
		final User sessionUser = userService.getUser(sessionUserId);
		final List<OntologyVirtualDatasource> virtualDatasources = ontologyVirtualDatasourceRepository
				.findByUserOrIsPublicTrue(sessionUser);

		for (final OntologyVirtualDatasource ontologyVirtualDatasource : virtualDatasources) {
			final VirtualDatasourceDTO virtualDatasetDTO = new VirtualDatasourceDTO(ontologyVirtualDatasource);
			virtualDatasetDTOList.add(virtualDatasetDTO);
		}

		return virtualDatasetDTOList;
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
		if (!report.isSuccess()) {
			String result = "";
			for (final Object element : report) {
				final ProcessingMessage processingMessage = (ProcessingMessage) element;
				result = result + processingMessage.getMessage() + " ,\n";
			}
			result = result.substring(0, result.length() - 3);
			throw new OntologyDataJsonProblemException("Json schema is not valid:\n " + result);
		}

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
	public Map<String, String> executeKPI(String user, String query, String ontology, String postProcessScript)
			throws Exception {

		final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
		final Map<String, String> mapResponse = new HashMap<>();
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
				try {
					final String scriptPostprocessFunction = "function postprocess(data){ " + postProcessScript + " }";
					engine.eval(scriptPostprocessFunction);
					final Invocable inv = (Invocable) engine;
					output = (String) inv.invokeFunction("postprocess", result);
				} catch (final ScriptException e) {
					mapResponse.put("status", "error");
					mapResponse.put("message", "ontology.error.create.kpi.message.postprocess");
					log.error("ERROR from Scripting Post Process, Exception detected", e.getCause().getMessage());
					return mapResponse;
				}
			}
			if (output == null || output.trim().equals("[ ]")) {
				mapResponse.put("status", "OK");
				mapResponse.put("message", "ontology.error.create.kpi.message.insert.no.data");
				return mapResponse;
			}
			final String resultInsert = processQuery("", ontology, ApiOperation.Type.POST.name(), output, "", user);
			if (resultInsert.equals("error")) {
				mapResponse.put("status", "error");
				mapResponse.put("message", "ontology.error.create.kpi.message.insert");
				return mapResponse;
			} else {
				mapResponse.put("status", "OK");
				mapResponse.put("message", "");
				return mapResponse;
			}

		} else {
			mapResponse.put("status", "error");
			mapResponse.put("message", "ontology.error.create.kpi.message.query");
			return mapResponse;
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
			if (!result.isStatus()) {
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
		query = query.replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ").trim().replaceAll(" +", " ")
				.replace(" FROM ", " from ").replaceAll(",", " ");

		final String[] list = query.split("from ");

		if (list.length > 1) {
			for (int i = 1; i < list.length; i++) {
				if (!list[i].startsWith("(")) {
					int indexOf = list[i].toLowerCase().indexOf(' ', 0);
					final int indexOfCloseBracket = list[i].toLowerCase().indexOf(')', 0);
					indexOf = indexOfCloseBracket != -1 && indexOfCloseBracket < indexOf ? indexOfCloseBracket
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
		Ontology ontology = ontologyRepository.findByIdentification(identification);
		// If ontology is not found, try searching TimescaDB aggregate with that
		// identification
		if (ontology == null) {
			final OntologyTimeseriesTimescaleAggregates aggr = OntologyTimeSeriesTimescaleAggregatesRepository
					.findByIdentification(identification);
			if (aggr != null) {
				ontology = aggr.getOntologyTimeSeries().getOntology();
			}
		}
		return ontology;
	}

	@Override
	public List<OntologyForList> getOntologiesForListWithDescriptionAndIdentification(String sessionUserId,
			String identification, String description) {
		List<OntologyForList> ontologies;
		final User sessionUser = userService.getUser(sessionUserId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (sessionUser.isAdmin()) {
			ontologies = ontologyRepository
					.findOntologyForListByIdentificationContainingAndDescriptionContaining(identification, description);
		} else {
			ontologies = ontologyRepository
					.findOntologyForListByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(
							sessionUser, identification, description);
		}
		return ontologies;
	}

	@Override
	public List<Ontology> getOntologiesByOwner(String sessionUserId) {
		final User sessionUser = userService.getUser(sessionUserId);
		if (sessionUser.isAdmin()) {
			return ontologyRepository.findAllByOrderByIdentificationAsc();
		} else {
			return ontologyRepository.findByUser(sessionUser);
		}
	}

	@Override
	public OntologyDataAccess createOrUpdateDataAccess(String ontId, String realm, String role, String user,
			String rule, String sessionUserId) {

		final Optional<Ontology> opt = ontologyRepository.findById(ontId);
		if (!opt.isPresent()) {
			throw new OntologyServiceException("Ontology does not exist");
		}
		final Ontology ontology = opt.get();
		final User sessionUser = userService.getUser(sessionUserId);

		if (hasUserPermisionForChangeOntology(sessionUser, ontology)) {

			OntologyDataAccess ontologyDataAccess = null;

			if (user != null && !user.equals("")) {
				final User userRule = userService.getUser(user);

				ontologyDataAccess = ontologyDataAccessRepository.findByOntologyAndUser(ontology, userRule);

				if (ontologyDataAccess == null) {
					ontologyDataAccess = new OntologyDataAccess();
				}

				ontologyDataAccess.setUser(userRule);
			} else if (realm != null && !realm.equals("") && role != null && !role.equals("")) {

				final App app = appRepository.findByIdentification(realm);

				if (app == null) {
					throw new OntologyServiceException("Realm does not exist");

				}

				final Optional<AppRole> optAppRole = app.getAppRoles().stream()
						.filter(appRole -> appRole.getName().equals(role)).findFirst();

				if (!optAppRole.isPresent()) {
					throw new OntologyServiceException("Role does not exist");
				}

				ontologyDataAccess = ontologyDataAccessRepository.findByOntologyAndRole(ontology, optAppRole.get());

				if (ontologyDataAccess == null) {
					ontologyDataAccess = new OntologyDataAccess();
				}

				ontologyDataAccess.setAppRole(optAppRole.get());
			}

			ontologyDataAccess.setRule(rule);
			ontologyDataAccess.setOntology(ontology);

			return ontologyDataAccessRepository.save(ontologyDataAccess);

		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
	}

	@Override
	public List<OntologyDataAccess> getOntologyUserDataAccesses(String ontologyId, String sessionUserId) {
		final Ontology ontology = getOntologyById(ontologyId, sessionUserId);
		return ontologyDataAccessRepository.findByOntology(ontology);
	}

	@Override
	public void deleteDataAccess(String id, String userId) {
		final User sessionUser = userService.getUser(userId);
		final Optional<OntologyDataAccess> opt = ontologyDataAccessRepository.findById(id);
		if (!opt.isPresent()) {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}
		final OntologyDataAccess userDataAccess = opt.get();

		if (hasUserPermisionForChangeOntology(sessionUser, userDataAccess.getOntology())) {
			ontologyDataAccessRepository.delete(userDataAccess);
		} else {
			throw new OntologyServiceException(USER_UNAUTH_STR);
		}

	}

	@Override
	public Map<String, String> getUserDataAccess(String user) {
		final List<OntologyDataAccess> dataAccessList = ontologyDataAccessRepository.findUserAccessByUser(user);

		final Map<String, String> dataAccessQueriesMap = new HashMap<>();

		for (final OntologyDataAccess ontologyDataAccess : dataAccessList) {
			dataAccessQueriesMap.put(ontologyDataAccess.getOntology().getIdentification(),
					getUserOntologyDataAccess(ontologyDataAccess.getOntology().getIdentification(), dataAccessList));
		}
		return dataAccessQueriesMap;
	}

	private String getUserOntologyDataAccess(String ontology, List<OntologyDataAccess> dataAccessList) {
		String accessQuery = "";
		String roleAccessQuery = "";
		for (final OntologyDataAccess ontologyDataAccess : dataAccessList) {
			if (ontologyDataAccess.getOntology().getIdentification().equals(ontology)) {
				if (ontologyDataAccess.getUser() != null) {
					accessQuery = ontologyDataAccess.getRule();
				} else if (roleAccessQuery.equals("")) {
					roleAccessQuery = roleAccessQuery + ontologyDataAccess.getRule();
				} else {
					roleAccessQuery = roleAccessQuery + " OR " + ontologyDataAccess.getRule();
				}
			}
		}

		if (accessQuery.equals("")) {
			accessQuery = roleAccessQuery;
		} else if (!roleAccessQuery.equals("")) {
			accessQuery = accessQuery + " AND (" + roleAccessQuery + ")";
		}
		return accessQuery;
	}

	@Override
	public Map<String, List<String>> getResourcesFromOntology(Ontology ontology) {
		final Map<String, List<String>> mapResources = new HashMap<>();
		final List<String> clients = new ArrayList<>();
		for (final ClientPlatformOntology clientPlatformOntology : clientPlatformOntologyRepository
				.findByOntology(ontology)) {
			clients.add(clientPlatformOntology.getClientPlatform().getIdentification());
		}

		mapResources.put("apis", apiRepository.findIdentificationByOntology(ontology.getIdentification()));
		mapResources.put("datasources",
				gadgetDatasourceRepository.findIdentificationByOntology(ontology.getIdentification()));
		mapResources.put("layers", layerRepository.findIdentificationByOntology(ontology.getIdentification()));
		mapResources.put("subscriptions",
				subscriptionRepository.findIdentificationByOntology(ontology.getIdentification()));
		mapResources.put("clients", clients);
		mapResources.put("resources",
				datasetResourceRepository.findIdentificationByOntology(ontology.getIdentification()));
		final List<String> rtdbDatasource = new ArrayList<>();
		rtdbDatasource.add(ontology.getRtdbDatasource().toString());
		mapResources.put("rtdbDatasource", rtdbDatasource);

		return mapResources;
	}

	@Override
	public OntologyElastic getOntologyElasticByOntologyId(Ontology ontology) {
		return elasticOntologyRepository.findByOntologyId(ontology);
	}

	@Override
	public OntologyPresto getOntologyPrestoByOntologyId(Ontology ontology) {
		return ontologyPrestoRepository.findByOntologyId(ontology);
	}

	@Override
	public Ontology getOntologyByIdForDelete(String ontologyId, String sessionUserId) {
		final Ontology ontology = ontologyRepository.findById(ontologyId).orElse(null);
		final User sessionUser = userService.getUser(sessionUserId);
		if (resourceService.isResourceSharedInAnyProject(ontology)) {
			throw new OPResourceServiceException(
					" This Ontology is shared within a Project, revoke access from project prior to deleting");
		}
		if (hasUserPermisionForChangeOntology(sessionUser, ontology)) {
			return ontology;
		} else {
			throw new OntologyServiceException(" User does not have rights to delete ontology");
		}
	}
}
