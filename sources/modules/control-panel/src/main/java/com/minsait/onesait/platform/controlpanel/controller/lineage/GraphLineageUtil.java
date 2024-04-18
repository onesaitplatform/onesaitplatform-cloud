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
package com.minsait.onesait.platform.controlpanel.controller.lineage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.LineageRelations;
import com.minsait.onesait.platform.config.model.LineageRelations.Group;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.lineage.LineageService;
import com.minsait.onesait.platform.controlpanel.rest.management.dataflow.DataFlowStorageManagementController;
import com.minsait.onesait.platform.controlpanel.rest.management.flowengine.FlowengineManagementController;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GraphLineageUtil {

	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepo;
	@Autowired
	private ApiOperationRepository apiOperationRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private DataFlowStorageManagementController dataflowController;
	@Autowired
	private LineageService lineageService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private FlowengineManagementController flowengineManagementController;
	@Autowired
	private FlowDomainRepository flowdomainRepository;

	private static final String ONTOLOGY_STR = "ontology";
	private static final String API_JSON = "Api";
	private static final String API_VERSION_JSON = "Version";
	private static final String API_STR = "api";
	private static final String DIGITALCLIENT_STR = "digitalclient";
	private static final String DIGITALCLIENT_JSON = "Digital Client";
	private static final String ONTOLOGY_JSON = "Ontology";
	private static final String DASHBOARD_STR = "dashboard";
	private static final String DATASOURCE_JSON = "Datasource";
	private static final String DATASOURCE_STR = "datasource";
	private static final String GADGET_JSON = "Gadget";
	private static final String GADGET_STR = "gadget";
	private static final String DASHBOARD_JSON = "Dashboard";
	private static final String DATAFLOW_JSON = "Dataflow";
	private static final String DATAFLOW_STR = "dataflow";
	private static final String NOTEBOOK_JSON = "Notebook";
	private static final String MICROSERVICE_JSON = "Microservice";
	private static final String FLOWENGINE_STR = "flow";
	private static final String FLOW_JSON = "Flow";
	private static final String FLOW_TAB_JSON = "Flow Tab";

	private ObjectMapper mapper = new ObjectMapper();

	public List<GraphLineageDTO> constructGraphWithAPIsForOntology(String ontologyIdentification, User user) {
		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);

		List<ApiOperation> apiPostOperations = apiOperationRepository.findByOntologyAndPostMethod(ontology);
		List<ApiOperation> apiGetOperations = apiOperationRepository.findByOntologyAndGetMethod(ontology);

		if (!user.isAdmin()) {
			apiPostOperations = apiPostOperations.stream().filter(c -> c.getApi().getUser().equals(user))
					.collect(Collectors.toList());
			apiGetOperations = apiGetOperations.stream().filter(c -> c.getApi().getUser().equals(user))
					.collect(Collectors.toList());
		}

		try {
			for (final ApiOperation apiPost : apiPostOperations) {

				arrayLinks.add(GraphLineageDTO.constructSingleNode(apiPost.getApi().getId(), API_STR,
						apiPost.getApi().getIdentification(), apiPost.getApi().getIdentification(),
						buildProperties(apiPost.getApi(), Group.API, null)));

				// Creación de enlaces origen
				arrayLinks.add(new GraphLineageDTO(apiPost.getApi().getId(), ontology.getId(), API_STR, ONTOLOGY_STR,
						apiPost.getApi().getIdentification(), ontology.getIdentification(),
						ontology.getIdentification(), buildProperties(ontology, Group.ONTOLOGY, null), false));

				arrayLinks.addAll(getExternalRelations(apiPost.getApi(), user));
			}

			for (final ApiOperation apiGet : apiGetOperations) {

				arrayLinks.add(GraphLineageDTO.constructSingleNode(apiGet.getApi().getId(), API_STR,
						apiGet.getApi().getIdentification(), apiGet.getApi().getIdentification(),
						buildProperties(apiGet.getApi(), Group.API, null)));

				// Creación de enlaces destino
				arrayLinks.add(new GraphLineageDTO(ontology.getId(), apiGet.getApi().getId(), ONTOLOGY_STR, API_STR,
						ontology.getIdentification(), apiGet.getApi().getIdentification(),
						apiGet.getApi().getIdentification(), buildProperties(apiGet.getApi(), Group.API, null), false));

				arrayLinks.addAll(getExternalRelations(apiGet.getApi(), user));
			}

			arrayLinks.addAll(getExternalRelations(ontology, user));
		} catch (final Exception e) {
			log.error("An error has ocurred lineage graph for Ontology: {} - Api", ontologyIdentification, e);
		}

		return arrayLinks;

	}

	public List<GraphLineageDTO> constructGraphWithClientPlatformsForOntology(String ontologyIdentification,
			User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		try {
			arrayLinks.add(
					GraphLineageDTO.constructSingleNode(ontology.getId(), ONTOLOGY_STR, ontology.getIdentification(),
							ontology.getIdentification(), buildProperties(ontology, Group.ONTOLOGY, null)));

			List<ClientPlatformOntology> cpOntologyInsertList = clientPlatformOntologyRepo
					.findByOntologyAndInsertAccess(ontology);
			List<ClientPlatformOntology> cpOntologyQueryList = clientPlatformOntologyRepo
					.findByOntologyAndQueryAccess(ontology);

			if (!user.isAdmin()) {
				cpOntologyInsertList = cpOntologyInsertList.stream()
						.filter(c -> c.getClientPlatform().getUser().equals(user)).collect(Collectors.toList());
				cpOntologyQueryList = cpOntologyQueryList.stream()
						.filter(c -> c.getClientPlatform().getUser().equals(user)).collect(Collectors.toList());
			}

			for (final ClientPlatformOntology clientPlatformOnt : cpOntologyInsertList) {

				arrayLinks.add(GraphLineageDTO.constructSingleNode(clientPlatformOnt.getClientPlatform().getId(),
						DIGITALCLIENT_STR, clientPlatformOnt.getClientPlatform().getIdentification(),
						clientPlatformOnt.getClientPlatform().getIdentification(),
						buildProperties(clientPlatformOnt.getClientPlatform(), Group.DIGITALCLIENT, null)));
				// Creación de enlaces origen
				arrayLinks.add(new GraphLineageDTO(clientPlatformOnt.getClientPlatform().getId(), ontology.getId(),
						DIGITALCLIENT_STR, ONTOLOGY_STR, clientPlatformOnt.getClientPlatform().getIdentification(),
						ontology.getIdentification(), ontology.getIdentification(),
						buildProperties(ontology, Group.ONTOLOGY, null), false));
				arrayLinks.addAll(getExternalRelations(clientPlatformOnt.getClientPlatform(), user));
			}

			for (final ClientPlatformOntology clientPlatformOnt : cpOntologyQueryList) {

				arrayLinks.add(GraphLineageDTO.constructSingleNode(clientPlatformOnt.getClientPlatform().getId(),
						DIGITALCLIENT_STR, clientPlatformOnt.getClientPlatform().getIdentification(),
						clientPlatformOnt.getClientPlatform().getIdentification(),
						buildProperties(clientPlatformOnt.getClientPlatform(), Group.DIGITALCLIENT, null)));
				// Creación de enlaces destino
				arrayLinks.add(new GraphLineageDTO(ontology.getId(), clientPlatformOnt.getClientPlatform().getId(),
						ONTOLOGY_STR, DIGITALCLIENT_STR, ontology.getIdentification(),
						clientPlatformOnt.getClientPlatform().getIdentification(),
						clientPlatformOnt.getClientPlatform().getIdentification(),
						buildProperties(ontology, Group.ONTOLOGY, null), false));
				arrayLinks.addAll(getExternalRelations(clientPlatformOnt.getClientPlatform(), user));
			}
		} catch (final Exception e) {
			log.error("An error has ocurred lineage graph for Ontology: {} - client platforms", ontologyIdentification,
					e);
		}

		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphForDashboard(String dashboardIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();

		Dashboard dashboard = dashboardRepository.findByIdentification(dashboardIdentification).get(0);
		try {
			arrayLinks.add(
					GraphLineageDTO.constructSingleNode(dashboard.getId(), DASHBOARD_STR, dashboard.getIdentification(),
							dashboard.getIdentification(), buildProperties(dashboard, Group.DASHBOARD, null)));

			JsonNode interaction = mapper.readTree(dashboard.getModel()).get("interactionHash");
			Iterator<String> it = interaction.fieldNames();
			while (it.hasNext()) {
				Optional<Gadget> gadget = gadgetRepository.findById(it.next());
				if (gadget.isPresent()) {
					List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository.findByGadget(gadget.get());
					for (GadgetMeasure gadgetMeasure : gadgetMeasures) {

						arrayLinks.add(GraphLineageDTO.constructSingleNode(
								gadgetMeasure.getDatasource().getOntology().getId(), ONTOLOGY_STR,
								gadgetMeasure.getDatasource().getOntology().getIdentification(),
								gadgetMeasure.getDatasource().getOntology().getIdentification(),
								buildProperties(gadgetMeasure.getDatasource().getOntology(), Group.ONTOLOGY, null)));

						arrayLinks.addAll(constructGraphWithClientPlatformsForOntology(
								gadgetMeasure.getDatasource().getOntology().getIdentification(), user));
						arrayLinks.addAll(constructGraphWithAPIsForOntology(
								gadgetMeasure.getDatasource().getOntology().getIdentification(), user));
						arrayLinks.addAll(constructGraphWithDashboardsForOntology(
								gadgetMeasure.getDatasource().getOntology().getIdentification(), user));
						arrayLinks.addAll(constructGraphWithDataflowForOntology(
								gadgetMeasure.getDatasource().getOntology().getIdentification(), user));
					}
				}
			}
			arrayLinks.addAll(getExternalRelations(dashboard, user));
		} catch (IOException e) {
			log.error("An error has ocurred lineage graph for Dashboard: {}", dashboardIdentification, e);
		}
		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphForApi(String apiIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		String apiId = apiIdentification.split("-")[0].trim();
		String apiVersion = apiIdentification.split("-")[1].trim().substring(1);
		Api api = apiRepository.findByIdentificationAndNumversion(apiId, Integer.parseInt(apiVersion));

		try {
			arrayLinks.add(GraphLineageDTO.constructSingleNode(api.getId(), API_STR, api.getIdentification(),
					api.getIdentification(), buildProperties(api, Group.API, null)));

			List<ApiOperation> apiPostOperations = apiOperationRepository.findByApiAndOperation(api,
					ApiOperation.Type.POST);
			List<ApiOperation> apiGetOperations = apiOperationRepository.findByApiAndOperation(api,
					ApiOperation.Type.GET);

			if (!user.isAdmin()) {
				apiPostOperations = apiPostOperations.stream().filter(c -> c.getApi().getUser().equals(user))
						.collect(Collectors.toList());
				apiGetOperations = apiGetOperations.stream().filter(c -> c.getApi().getUser().equals(user))
						.collect(Collectors.toList());
			}

			for (final ApiOperation apiPost : apiPostOperations) {
				// Creación de enlaces origen
				arrayLinks.add(new GraphLineageDTO(api.getId(), api.getOntology().getId(), API_STR, ONTOLOGY_STR,
						api.getIdentification(), api.getOntology().getIdentification(),
						api.getOntology().getIdentification(), buildProperties(api, Group.API, null), false));
			}
			for (final ApiOperation apiGet : apiGetOperations) {
				// Creación de enlaces destino
				arrayLinks.add(new GraphLineageDTO(api.getOntology().getId(), api.getId(), ONTOLOGY_STR, API_STR,
						api.getOntology().getIdentification(), api.getIdentification(), api.getIdentification(),
						buildProperties(api.getOntology(), Group.ONTOLOGY, null), false));
			}

			arrayLinks
					.addAll(constructGraphWithClientPlatformsForOntology(api.getOntology().getIdentification(), user));
			arrayLinks.addAll(constructGraphWithDashboardsForOntology(api.getOntology().getIdentification(), user));
			arrayLinks.addAll(constructGraphWithDataflowForOntology(api.getOntology().getIdentification(), user));

			arrayLinks.addAll(getExternalRelations(api, user));

		} catch (Exception e) {
			log.error("An error has ocurred lineage graph for Api: {}", apiIdentification, e);
		}
		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphForDatasource(String datasourceIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();

		GadgetDatasource datasource = gadgetDatasourceRepository.findByIdentification(datasourceIdentification);
		try {
			arrayLinks.add(GraphLineageDTO.constructSingleNode(datasource.getId(), DATASOURCE_STR,
					datasource.getIdentification(), datasource.getIdentification(),
					buildProperties(datasource, Group.DATASOURCE, null)));

			arrayLinks.add(new GraphLineageDTO(datasource.getOntology().getId(), datasource.getId(), ONTOLOGY_STR,
					DATASOURCE_STR, datasource.getOntology().getIdentification(), datasource.getIdentification(),
					datasource.getIdentification(), buildProperties(datasource, Group.DATASOURCE, null), false));

			arrayLinks.addAll(constructGraphWithAPIsForOntology(datasource.getOntology().getIdentification(), user));
			arrayLinks.addAll(
					constructGraphWithClientPlatformsForOntology(datasource.getOntology().getIdentification(), user));

			// Creación de enlaces con gadget
			List<GadgetMeasure> gadgets = gadgetMeasureRepository.findByDatasource(datasource.getId());
			for (final GadgetMeasure gadget : gadgets) {
				arrayLinks.add(new GraphLineageDTO(datasource.getId(), gadget.getGadget().getId(), DATASOURCE_STR,
						GADGET_STR, datasource.getIdentification(), gadget.getGadget().getIdentification(),
						gadget.getGadget().getIdentification(), buildProperties(gadget.getGadget(), Group.GADGET, null),
						false));

				// Creación de enlaces con dashboard
				List<Dashboard> dashboards = new ArrayList<>();
				if (!user.isAdmin()) {
					dashboards = dashboardRepository.findByUserPermission(user);
				} else {
					dashboards = dashboardRepository.findAll();
				}
				dashboards = dashboards.stream().filter(c -> c.getModel().contains(gadget.getGadget().getId()))
						.collect(Collectors.toList());
				for (final Dashboard dashboard : dashboards) {
					arrayLinks.add(new GraphLineageDTO(gadget.getGadget().getId(), dashboard.getId(), GADGET_STR,
							DASHBOARD_STR, gadget.getGadget().getIdentification(), dashboard.getIdentification(),
							dashboard.getIdentification(), buildProperties(dashboard, Group.DASHBOARD, null), false));
				}

			}
			arrayLinks
					.addAll(constructGraphWithDataflowForOntology(datasource.getOntology().getIdentification(), user));
			arrayLinks.addAll(getExternalRelations(datasource, user));
		} catch (Exception e) {
			log.error("Error constructing lineage graph for datasource.", e);
		}

		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphForGadget(String gadgetIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();

		Gadget g = gadgetRepository.findByIdentification(gadgetIdentification);
		try {
			arrayLinks.add(GraphLineageDTO.constructSingleNode(g.getId(), GADGET_STR, g.getIdentification(),
					g.getIdentification(), buildProperties(g, Group.GADGET, null)));

			// Creación de enlaces con gadget
			List<GadgetMeasure> gadgets = gadgetMeasureRepository.findByGadget(g);
			for (final GadgetMeasure gadget : gadgets) {

				arrayLinks.addAll(constructGraphWithAPIsForOntology(
						gadget.getDatasource().getOntology().getIdentification(), user));
				arrayLinks.addAll(constructGraphWithClientPlatformsForOntology(
						gadget.getDatasource().getOntology().getIdentification(), user));
				arrayLinks.addAll(constructGraphWithDataflowForOntology(
						gadget.getDatasource().getOntology().getIdentification(), user));

				arrayLinks.add(new GraphLineageDTO(gadget.getDatasource().getOntology().getId(),
						gadget.getDatasource().getId(), ONTOLOGY_STR, DATASOURCE_STR,
						gadget.getDatasource().getOntology().getIdentification(),
						gadget.getDatasource().getIdentification(), gadget.getDatasource().getIdentification(),
						buildProperties(gadget.getDatasource(), Group.GADGET, null), false));

				arrayLinks.add(new GraphLineageDTO(gadget.getDatasource().getId(), gadget.getGadget().getId(),
						DATASOURCE_STR, GADGET_STR, gadget.getDatasource().getIdentification(),
						gadget.getGadget().getIdentification(), gadget.getGadget().getIdentification(),
						buildProperties(gadget.getGadget(), Group.GADGET, null), false));

				// Creación de enlaces con dashboard
				List<Dashboard> dashboards = dashboardRepository.findByUserPermission(user);
				dashboards = dashboards.stream().filter(c -> c.getModel().contains(gadget.getGadget().getId()))
						.collect(Collectors.toList());
				for (final Dashboard dashboard : dashboards) {
					arrayLinks.add(new GraphLineageDTO(gadget.getGadget().getId(), dashboard.getId(), GADGET_STR,
							DASHBOARD_STR, gadget.getGadget().getIdentification(), dashboard.getIdentification(),
							dashboard.getIdentification(), buildProperties(dashboard, Group.DASHBOARD, null), false));
				}

			}
			arrayLinks.addAll(getExternalRelations(g, user));
		} catch (Exception e) {
			log.error("Error constructinf graph lineage for dashboard.", e);
		}
		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphWithDashboardsForOntology(String ontologyIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		List<GadgetDatasource> gadgetDatasources = new ArrayList<>();

		if (!user.isAdmin()) {
			gadgetDatasources = gadgetDatasourceRepository.findByUserAndOntology(user, ontology);
		} else {
			gadgetDatasources = gadgetDatasourceRepository.findByOntology(ontology);
		}

		try {
			for (final GadgetDatasource datasource : gadgetDatasources) {

				// Creación de enlaces del datasource
				arrayLinks.add(new GraphLineageDTO(ontology.getId(), datasource.getId(), ONTOLOGY_STR, DATASOURCE_STR,
						ontology.getIdentification(), datasource.getIdentification(), datasource.getIdentification(),
						buildProperties(datasource, Group.DATASOURCE, null), false));

				arrayLinks.addAll(getExternalRelations(datasource, user));

				// Creación de enlaces con gadget
				List<GadgetMeasure> gadgets = gadgetMeasureRepository.findByDatasource(datasource.getId());
				for (final GadgetMeasure gadget : gadgets) {
					arrayLinks.add(new GraphLineageDTO(datasource.getId(), gadget.getGadget().getId(), DATASOURCE_STR,
							GADGET_STR, datasource.getIdentification(), gadget.getGadget().getIdentification(),
							gadget.getGadget().getIdentification(),
							buildProperties(gadget.getGadget(), Group.GADGET, null), false));

					arrayLinks.addAll(getExternalRelations(gadget.getGadget(), user));

					// Creación de enlaces con dashboard
					List<Dashboard> dashboards = new ArrayList<>();
					if (!user.isAdmin()) {
						dashboards = dashboardRepository.findByUserPermission(user);
					} else {
						dashboards = dashboardRepository.findAll();
					}
					dashboards = dashboards.stream().filter(c -> c.getModel().contains(gadget.getGadget().getId()))
							.collect(Collectors.toList());
					for (final Dashboard dashboard : dashboards) {
						arrayLinks.add(new GraphLineageDTO(gadget.getGadget().getId(), dashboard.getId(), GADGET_STR,
								DASHBOARD_STR, gadget.getGadget().getIdentification(), dashboard.getIdentification(),
								dashboard.getIdentification(), buildProperties(dashboard, Group.DASHBOARD, null),
								false));

						arrayLinks.addAll(getExternalRelations(dashboard, user));
					}

				}

			}

		} catch (final Exception e) {
			log.error("An error has ocurred lineage graph for Ontology: {} - Gadgets", ontologyIdentification, e);
		}

		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphForDataflow(String dataflowIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		ArrayNode opNodesDestiny = mapper.createArrayNode();
		ArrayNode opNodesOrigin = mapper.createArrayNode();
		Pipeline pipeline = dataflowService.getPipelineByIdentification(dataflowIdentification);

		try {
			ResponseEntity<String> response = dataflowController.exportPipeline(dataflowIdentification);

			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.error("An error has ocurred lineage graph for Ontology: {} - Dataflow", dataflowIdentification,
						response.getBody());
				return arrayLinks;
			}
			JsonNode stages = mapper.readTree(response.getBody()).get("pipelineConfig").get("stages");

			if (stages.isArray()) {
				for (JsonNode stage : stages) {
					if (stage.get("library").asText().equals("onesaitplatform-streamsets")) {
						if (stage.get("instanceName").asText().contains("OnesaitPlatformDestination")) {
							opNodesDestiny.add(stage);
						} else if (stage.get("instanceName").asText().contains("OnesaitPlatformOrigin")
								|| stage.get("instanceName").asText().contains("OnesaitPlatformLookup")) {
							opNodesOrigin.add(stage);
						}
					}
				}
				populateArrayListForDataflowNodes(opNodesDestiny, opNodesOrigin, arrayLinks, pipeline, user, null,
						stages);
			}

			arrayLinks.addAll(getExternalRelations(pipeline, user));

		} catch (final Exception e) {
			log.error("An error has ocurred lineage graph for Ontology: {} - Dataflow", dataflowIdentification, e);
		}

		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphWithDataflowForOntology(String ontologyIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		ArrayNode opNodesDestiny = mapper.createArrayNode();
		ArrayNode opNodesOrigin = mapper.createArrayNode();
		List<Pipeline> pipelines = dataflowService.getPipelines(user.getUserId());

		try {
			for (Pipeline pipeline : pipelines) {
				ResponseEntity<String> response = dataflowController.exportPipeline(pipeline.getIdentification());

				if (!response.getStatusCode().equals(HttpStatus.OK)) {
					log.error("An error has ocurred lineage graph for Ontology: {} - Dataflow",
							pipeline.getIdentification(), response.getBody());
					return arrayLinks;
				}
				JsonNode stages = mapper.readTree(response.getBody()).get("pipelineConfig").get("stages");
				if (stages.isArray()) {
					for (JsonNode stage : stages) {
						if (stage.get("library").asText().equals("onesaitplatform-streamsets")) {
							if (stage.get("instanceName").asText().contains("OnesaitPlatformDestination")) {
								opNodesDestiny.add(stage);
							} else if (stage.get("instanceName").asText().contains("OnesaitPlatformOrigin")
									|| stage.get("instanceName").asText().contains("OnesaitPlatformLookup")) {
								opNodesOrigin.add(stage);
							}
						}
					}
					populateArrayListForDataflowNodes(opNodesDestiny, opNodesOrigin, arrayLinks, pipeline, user,
							ontologyIdentification, stages);
				}
			}

		} catch (final Exception e) {
			log.error("An error has ocurred lineage graph for Ontology: {} - Dataflow", ontologyIdentification, e);
		}

		return arrayLinks;
	}

	public List<GraphLineageDTO> constructGraphWithFlowengineForOntology(String ontologyIdentification, User user) {

		final List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		Ontology ontology = ontologyRepository.findByIdentification(ontologyIdentification);
		FlowDomain flowDomain = new FlowDomain();
		Map<String, String> flowTabs = new HashMap<>();
		if (!user.isAdmin()) {
			flowDomain = flowdomainRepository.findByUserUserId(user.getUserId());
		}

		try {
			ResponseEntity<String> response = flowengineManagementController.exportFlowDomainByUser();

			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				log.error("An error has ocurred lineage graph for Ontology: {} - Flowengine",
						flowDomain.getIdentification(), response.getBody());
				return arrayLinks;
			}
			JsonNode data = mapper.readTree(response.getBody());
			if (data.isArray()) {
				for (JsonNode d : data) {
					if (d.get("type").asText().equals("tab")) {
						flowTabs.put(d.get("id").asText(), d.get("label").asText());
					} else if ((d.get("type").asText().equals("onesaitplatform-query-static")
							|| d.get("type").asText().equals("onesaitplatform-notification-endpoint"))
							&& d.get("ontology").asText().equals(ontologyIdentification)) {
						arrayLinks.add(new GraphLineageDTO(ontology.getId(), flowDomain.getId(), ONTOLOGY_STR,
								FLOWENGINE_STR, ontology.getIdentification(), flowDomain.getIdentification(),
								flowDomain.getIdentification(),
								buildProperties(flowDomain, Group.FLOW, flowTabs.get(d.get("z").asText())), false));
					} else if (d.get("type").asText().equals("onesaitplatform-insert")
							&& d.get("ontology").asText().equals(ontologyIdentification)) {

						arrayLinks.add(GraphLineageDTO.constructSingleNode(flowDomain.getId(), FLOWENGINE_STR,
								flowDomain.getIdentification(), flowDomain.getIdentification(),
								buildProperties(flowDomain, Group.FLOW, flowTabs.get(d.get("z").asText()))));

						arrayLinks.add(new GraphLineageDTO(flowDomain.getId(), ontology.getId(), FLOWENGINE_STR,
								ONTOLOGY_STR, flowDomain.getIdentification(), ontology.getIdentification(),
								ontology.getIdentification(),
								buildProperties(ontology, Group.ONTOLOGY, flowTabs.get(d.get("z").asText())), false));
					}
				}
			}

		} catch (final Exception e) {
			log.error("An error has ocurred lineage graph for Ontology: {} - Dataflow", ontologyIdentification, e);
		}

		return arrayLinks;
	}

	private List<GraphLineageDTO> processPipelineForOntology(Ontology ontology, ClientPlatform digitalClient,
			Pipeline pipeline, Boolean isDestiny, String sourceOntology, JsonNode properties, User user)
			throws IOException {
		List<GraphLineageDTO> arrayLinks = new LinkedList<>();
		if (digitalClient != null && ontology != null && ontology.getIdentification().equals(sourceOntology)) {
			arrayLinks.add(GraphLineageDTO.constructSingleNode(pipeline.getId(), DATAFLOW_STR,
					pipeline.getIdentification(), pipeline.getIdentification(), properties));

			arrayLinks.addAll(getExternalRelations(pipeline, user));

			if (isDestiny) {
				arrayLinks.add(new GraphLineageDTO(pipeline.getId(), digitalClient.getId(), DATAFLOW_STR,
						DIGITALCLIENT_STR, pipeline.getIdentification(), digitalClient.getIdentification(),
						digitalClient.getIdentification(), buildProperties(digitalClient, Group.DIGITALCLIENT, null),
						false));
			} else {
				arrayLinks.add(new GraphLineageDTO(digitalClient.getId(), pipeline.getId(), DIGITALCLIENT_STR,
						DATAFLOW_STR, digitalClient.getIdentification(), pipeline.getIdentification(),
						pipeline.getIdentification(), buildProperties(pipeline, Group.DATAFLOW, null), false));
			}
		}
		return arrayLinks;
	}

	private Ontology getOntologyFromStage(JsonNode stage) {
		JsonNode configurations = stage.get("configuration");
		if (configurations.isArray()) {
			for (JsonNode config : configurations) {
				if (config.get("name").asText().equals("stageDConfig.ontology")) {
					return ontologyRepository.findByIdentification(config.get("value").asText());
				}
			}
		}
		return null;
	}

	private ClientPlatform getClientPlatformFromStage(JsonNode stage) {
		JsonNode configurations = stage.get("configuration");
		if (configurations.isArray()) {
			for (JsonNode config : configurations) {
				if (config.get("name").asText().equals("stageDConfig.device")) {
					return clientPlatformRepository.findByIdentification(config.get("value").asText());
				}
			}
		}
		return null;
	}

	private JsonNode getOriginNodeFromStage(JsonNode stages, String laneId) {
		for (JsonNode stage : stages) {
			JsonNode outputLanes = stage.get("outputLanes");
			if (outputLanes.isArray() && outputLanes.size() > 0) {
				for (JsonNode lane : outputLanes) {
					if (lane.asText().equals(laneId)) {
						JsonNode inputLanes = stage.get("inputLanes");
						if (inputLanes.isArray() && inputLanes.size() > 0) {
							for (JsonNode input : inputLanes) {
								return getOriginNodeFromStage(stages, input.asText());
							}
						} else {
							return stage;
						}
					}
				}
			}
		}
		return null;
	}

	private JsonNode getPropertiesFromDataflowNode(JsonNode stage, String pipelineId) {
		String stageName = stage.get("stageName").asText();
		Configuration configuration = configurationService.getConfiguration(Type.LINEAGE, "Lineage");
		ObjectNode result = mapper.createObjectNode();
		JsonNode configurations = stage.get("configuration");
		try {
			if (stageName != null) {
				JsonNode json = mapper.readTree(configuration.getYmlConfig());
				if (json.isArray()) {
					json.forEach(node -> {
						if (node.get("id").asText().equals(stageName)) {
							JsonNode properties = node.get(node.get("defaultLanguage").asText());
							result.put("Dataflow", pipelineId);
							result.put("Origin", properties.get("label").asText());
							for (JsonNode conf : configurations) {
								if (properties.get("properties").has(conf.get("name").asText())) {
									result.put(properties.get("properties").get(conf.get("name").asText()).asText(),
											conf.get("value").asText());
								}
							}
						}
					});
				}
			}
			return result;
		} catch (IOException e) {
			log.error("Error getting centralized configuration for lineage.", e);
			return result;
		}
	}

	private List<GraphLineageDTO> processPipelineForDataflow(Ontology ontology, ClientPlatform digitalClient, User user,
			Pipeline pipeline, Boolean isDestiny) throws IOException {
		List<GraphLineageDTO> arrayLinks = new LinkedList<>();

		if (digitalClient != null && ontology != null) {
			arrayLinks.add(
					GraphLineageDTO.constructSingleNode(pipeline.getId(), DATAFLOW_STR, pipeline.getIdentification(),
							pipeline.getIdentification(), buildProperties(pipeline, Group.DATAFLOW, null)));

			arrayLinks.addAll(getExternalRelations(pipeline, user));

			if (isDestiny) {
				arrayLinks.add(new GraphLineageDTO(pipeline.getId(), digitalClient.getId(), DATAFLOW_STR,
						DIGITALCLIENT_STR, pipeline.getIdentification(), digitalClient.getIdentification(),
						digitalClient.getIdentification(), buildProperties(digitalClient, Group.DIGITALCLIENT, null),
						false));
			} else {
				arrayLinks.add(new GraphLineageDTO(ontology.getId(), pipeline.getId(), ONTOLOGY_STR, DATAFLOW_STR,
						ontology.getIdentification(), pipeline.getIdentification(), pipeline.getIdentification(),
						buildProperties(pipeline, Group.DATAFLOW, null), false));
			}
			arrayLinks.addAll(constructGraphWithClientPlatformsForOntology(ontology.getIdentification(), user));
			arrayLinks.addAll(constructGraphWithAPIsForOntology(ontology.getIdentification(), user));
			arrayLinks.addAll(constructGraphWithDashboardsForOntology(ontology.getIdentification(), user));
		}
		return arrayLinks;
	}

	private List<GraphLineageDTO> populateArrayListForDataflowNodes(ArrayNode opNodesDestiny, ArrayNode opNodesOrigin,
			List<GraphLineageDTO> arrayLinks, Pipeline pipeline, User user, String sourceOntology, JsonNode stages)
			throws IOException {
		Ontology ontology = new Ontology();
		ClientPlatform clientPlatform = new ClientPlatform();
		if (opNodesOrigin.size() == 0 && opNodesDestiny.size() > 0) {
			for (JsonNode stage : opNodesDestiny) {
				ontology = getOntologyFromStage(stage);
				clientPlatform = getClientPlatformFromStage(stage);
				JsonNode properties = mapper.createObjectNode();
				JsonNode inputLanes = stage.get("inputLanes");
				if (inputLanes.isArray() && inputLanes.size() > 0) {
					for (JsonNode input : inputLanes) {
						JsonNode origin = getOriginNodeFromStage(stages, input.asText());
						properties = getPropertiesFromDataflowNode(origin, pipeline.getIdentification());
					}
				}

				arrayLinks.addAll(sourceOntology == null
						? processPipelineForDataflow(ontology, clientPlatform, user, pipeline, true)
						: processPipelineForOntology(ontology, clientPlatform, pipeline, true, sourceOntology,
								properties, user));
			}
		} else if (opNodesOrigin.size() > 0 && opNodesDestiny.size() == 0) {
			for (JsonNode stage : opNodesOrigin) {
				ontology = getOntologyFromStage(stage);
				clientPlatform = getClientPlatformFromStage(stage);
				arrayLinks.addAll(sourceOntology == null
						? processPipelineForDataflow(ontology, clientPlatform, user, pipeline, false)
						: processPipelineForOntology(ontology, clientPlatform, pipeline, false, sourceOntology, null,
								user));
			}
		} else if (opNodesOrigin.size() > 0 && opNodesDestiny.size() > 0) {
			for (JsonNode stage : opNodesOrigin) {
				ontology = getOntologyFromStage(stage);
				clientPlatform = getClientPlatformFromStage(stage);
				arrayLinks.addAll(sourceOntology == null
						? processPipelineForDataflow(ontology, clientPlatform, user, pipeline, false)
						: processPipelineForOntology(ontology, clientPlatform, pipeline, false, sourceOntology, null,
								user));
			}
			for (JsonNode stage : opNodesDestiny) {
				ontology = getOntologyFromStage(stage);
				clientPlatform = getClientPlatformFromStage(stage);
				arrayLinks.addAll(sourceOntology == null
						? processPipelineForDataflow(ontology, clientPlatform, user, pipeline, true)
						: processPipelineForOntology(ontology, clientPlatform, pipeline, true, sourceOntology, null,
								user));
			}
		}
		return arrayLinks;
	}

	private List<GraphLineageDTO> getExternalRelations(OPResource resource, User user) throws IOException {
		List<GraphLineageDTO> arrayLinks = new LinkedList<>();

		List<LineageRelations> relations = lineageService.findByTargetOrSource(resource, user);

		for (LineageRelations relation : relations) {

			GraphLineageDTO sourceDto = GraphLineageDTO.constructSingleNode(relation.getSource().getId(),
					relation.getSourceGroup().name().toLowerCase(), relation.getSource().getIdentification(),
					relation.getSource().getIdentification(),
					buildProperties(relation.getSource(), relation.getSourceGroup(), null));
			sourceDto.setIsExternal(true);

			GraphLineageDTO targetDto = GraphLineageDTO.constructSingleNode(relation.getTarget().getId(),
					relation.getTargetGroup().name().toLowerCase(), relation.getTarget().getIdentification(),
					relation.getTarget().getIdentification(),
					buildProperties(relation.getTarget(), relation.getTargetGroup(), null));
			targetDto.setIsExternal(true);

			arrayLinks.add(sourceDto);
			arrayLinks.add(targetDto);
			arrayLinks.add(new GraphLineageDTO(relation.getSource().getId(), relation.getTarget().getId(),
					relation.getSourceGroup().name().toLowerCase(), relation.getTargetGroup().name().toLowerCase(),
					relation.getSource().getIdentification(), relation.getTarget().getIdentification(),
					relation.getTarget().getIdentification(),
					buildProperties(relation.getTarget(), relation.getTargetGroup(), null), true));
		}
		return arrayLinks;
	}

	private String getTypeStr(Group group) {
		if (group.equals(Group.API)) {
			return API_JSON;
		} else if (group.equals(Group.DASHBOARD)) {
			return DASHBOARD_JSON;
		} else if (group.equals(Group.DATAFLOW)) {
			return DATAFLOW_JSON;
		} else if (group.equals(Group.DATASOURCE)) {
			return DATASOURCE_JSON;
		} else if (group.equals(Group.DIGITALCLIENT)) {
			return DIGITALCLIENT_JSON;
		} else if (group.equals(Group.GADGET)) {
			return GADGET_JSON;
		} else if (group.equals(Group.ONTOLOGY)) {
			return ONTOLOGY_JSON;
		} else if (group.equals(Group.NOTEBOOK)) {
			return NOTEBOOK_JSON;
		} else if (group.equals(Group.MICROSERVICE)) {
			return MICROSERVICE_JSON;
		} else if (group.equals(Group.FLOW)) {
			return FLOW_JSON;
		}
		return null;
	}

	private JsonNode buildProperties(OPResource resource, Group group, String flowTab) {
		ObjectNode properties = mapper.createObjectNode();
		properties.put(getTypeStr(group), resource.getIdentification());
		if (group.equals(Group.API)) {
			Api api = (Api) resource;
			properties.put(API_VERSION_JSON, api.getNumversion());
		} else if (group.equals(Group.FLOW)) {
			properties.put(FLOW_TAB_JSON, flowTab);
		}
		return properties;

	}

}