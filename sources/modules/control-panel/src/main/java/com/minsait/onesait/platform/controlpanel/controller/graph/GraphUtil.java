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
package com.minsait.onesait.platform.controlpanel.controller.graph;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.Microservice;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.microservice.MicroserviceService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GraphUtil {

	private String urlClientPlatform;
	private String urlDashboard;
	private String urlGadget;
	private String urlOntology;
	private String urlApis;
	private String urlFlows;
	private String urlDigitalTwin;
	private String urlWebProjects;
	private String urlNotebook;
	private String urlDataflow;
	private String urlMicroservice;
	private static final String GENERIC_USER_NAME = "USER";
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private IntegrationResourcesService intregationResourcesService;
	@Autowired
	private UserService userService;
	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private FlowDomainService flowService;
	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;
	@Autowired
	private NotebookService notebookService;
	@Autowired
	private DataflowService dataflowService;
	@Autowired
	private MicroserviceService microserviceService;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepo;
	@Autowired
	private ApiOperationRepository apiOperationRepository;

	private static final String CREATE_STR = "create";
	private static final String SHOW_STR = "show/";
	private static final String ONTOLOGY_STR = "entity";
	private static final String LICENSING_STR = "licensing";
	private static final String COLUMNS_STR = "columns";

	@PostConstruct
	public void init() {
		// initialize URLS

		final String url = intregationResourcesService.getUrl(Module.CONTROLPANEL, ServiceUrl.BASE);
		urlClientPlatform = url + "/devices/show/";
		urlGadget = url + "/gadgets/";
		urlDashboard = url + "/dashboards/";
		urlOntology = url + "/ontologies/";
		urlApis = url + "/apimanager/";
		urlFlows = url + "/flows/";
		urlDigitalTwin = url + "/digitaltwindevices/";
		urlWebProjects = url + "/webprojects/";
		urlNotebook = url + "/notebooks/";
		urlDataflow = url + "/dataflow/";
		urlMicroservice = url + "/microservices/";

	}

	public List<GraphDTO> constructGraphWithOntologies(List<Ontology> ontologies, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.ontologies", "ONTOLOGIES");
		final String description = utils.getMessage("tooltip_ontologies", null);

		arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlOntology + "list", GENERIC_USER_NAME, "ENTITIES",
				utils.getUserId(), name, "suit", description, urlOntology + CREATE_STR));

		if (ontologies == null) {
			if (utils.isAdministrator())
				ontologies = ontologyRepository.findAll();
			else
				ontologies = ontologyRepository
						.findByUserAndOntologyUserAccessAndAllPermissions(userService.getUser(utils.getUserId()));

		}
		if (null != user)
			ontologies = ontologies.stream().filter(o -> o.getUser().equals(user)).collect(Collectors.toList());
		for (final Ontology ont : ontologies) {
			final Set<OntologyRelation> relations = new TreeSet<>();
			try {
				addOntologyReferenceLinks(ont, arrayLinks, relations);
			} catch (final IOException e) {
				log.error("Not adding ontology {} references, cause: ", ont.getIdentification(), e.getMessage());

			}
			arrayLinks.add(new GraphDTO(name, ont.getId(), urlOntology + "list", urlOntology + SHOW_STR + ont.getId(),
					name, ONTOLOGY_STR, name, ont.getIdentification(), LICENSING_STR, relations,
					ont.getIdentification()));
		}
		return arrayLinks;
	}

	public void addOntologyReferenceLinks(Ontology ontology, List<GraphDTO> arrayLinks, Set<OntologyRelation> relations)
			throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode schemaOrigin = mapper.readTree(ontology.getJsonSchema());
		if (!schemaOrigin.path("_references").isMissingNode()) {
			schemaOrigin.path("_references").forEach(r -> {
				String originAtt = getOriginAtt(r.get("self").asText());
				String targetAtt = r.get("target").asText().split("#")[1].replaceAll("properties.", "");
				final String targetOntology = getTargetOntology(r.get("target").asText());
				final Ontology target = ontologyRepository.findByIdentification(targetOntology);
				final String refOrigin = refJsonSchema(schemaOrigin);
				if (!"".equals(refOrigin))
					originAtt = originAtt.replaceAll(refOrigin.replace("/", ""),
							schemaOrigin.at("/required/0").asText());
				if (target == null)
					throw new GenericRuntimeOPException(
							"Target ontology of " + ontology.getIdentification() + " not found on platform");
				try {
					final JsonNode schemaTarget = mapper.readTree(target.getJsonSchema());
					final String refTarget = refJsonSchema(schemaTarget);
					if (!"".equals(refTarget))
						targetAtt = targetAtt.replaceAll(refTarget.replace("/", ""),
								schemaTarget.at("/required/0").asText());
				} catch (final IOException e) {
					log.debug("No $ref");
				}
				relations.add(new OntologyRelation(ontology.getIdentification(), target.getIdentification(), originAtt,
						targetAtt));
				arrayLinks.add(new GraphDTO(ontology.getId(), target.getId(), urlOntology + SHOW_STR + ontology.getId(),
						urlOntology + SHOW_STR + target.getId(), ONTOLOGY_STR, ONTOLOGY_STR,
						ontology.getIdentification(), target.getIdentification(), LICENSING_STR, "Linked"));

			});
		}
	}

	public String refJsonSchema(JsonNode schema) {
		final Iterator<Entry<String, JsonNode>> elements = schema.path("properties").fields();
		String reference = "";
		while (elements.hasNext()) {
			final Entry<String, JsonNode> entry = elements.next();
			if (!entry.getValue().path("$ref").isMissingNode()) {
				final String ref = entry.getValue().path("$ref").asText();
				reference = ref.substring(ref.lastIndexOf("#/")).substring(1);
			}
		}
		return reference;
	}

	public String getOriginAtt(String self) {
		return self.replaceAll("properties.", "");
	}

	public String getTargetOntology(String target) {
		return target.replaceFirst("ontologies/schema/", "").split("#")[0];
	}

	public List<GraphDTO> constructGraphWithAPIs(List<Api> apis, User user) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.apis", "APIS");
		final String description = utils.getMessage("tooltip_apis", null);

		if (apis == null) {
			apis = apiManagerService.loadAPISByFilter(null, null, null, utils.getUserId());
		}
		if (null != user)
			apis = apis.stream().filter(a -> a.getUser().equals(user)).collect(Collectors.toList());
		try {
			arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlApis + "list", GENERIC_USER_NAME, "apis",
					utils.getUserId(), name, "suit", description, urlApis + CREATE_STR));
			apis.forEach(a -> {
				arrayLinks.add(new GraphDTO(name, a.getId(), urlApis + "list", urlApis + SHOW_STR + a.getId(), name,
						"api", name, a.getIdentification(), LICENSING_STR, a.getIdentification()));
				if (a.getApiType().equals(ApiType.INTERNAL_ONTOLOGY))
					arrayLinks.add(new GraphDTO(a.getId(), a.getOntology().getId(), urlApis + SHOW_STR + a.getId(),
							urlOntology + SHOW_STR + a.getOntology().getId(), "api", ONTOLOGY_STR,
							a.getIdentification(), a.getOntology().getIdentification(), LICENSING_STR,
							a.getIdentification(), a.getOntology().getIdentification()));
			});
		} catch (final Exception e) {
			log.error("An error has ocurred loading graph with apis", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithFlows(List<FlowDomain> domains, User user) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.flows", "FLOWS");
		final String description = utils.getMessage("tooltip_flows", null);

		if (domains == null) {
			domains = flowService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
		}
		if (null != user)
			domains = domains.stream().filter(d -> d.getUser().equals(user)).collect(Collectors.toList());
		try {
			arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlFlows + "list", GENERIC_USER_NAME, "flows",
					utils.getUserId(), name, "suit", description, urlFlows + CREATE_STR));
			domains.forEach(d -> {
				arrayLinks.add(
						new GraphDTO(name, d.getId(), urlFlows + "list", urlFlows + SHOW_STR + d.getIdentification(),
								name, "flow", name, d.getIdentification(), LICENSING_STR, d.getIdentification()));
			});
		} catch (final Exception e) {
			log.error("An error has ocurred loading graph with flows", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithWebProjects(List<WebProjectDTO> projects, User user) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.webprojects", "WEB PROJECTS");
		final String description = utils.getMessage("tooltip_webprojects", null);

		arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlWebProjects + "list", GENERIC_USER_NAME,
				"webprojects", utils.getUserId(), name, "suite", description, urlWebProjects + CREATE_STR));
		if (projects != null) {
			if (null != user)
				projects = projects.stream().filter(p -> p.getUserId().equals(user.getUserId()))
						.collect(Collectors.toList());
			try {
				projects.forEach(p -> {
					arrayLinks.add(new GraphDTO(name, p.getIdentification(), urlWebProjects + "list",
							intregationResourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE) + "web/"
									+ p.getIdentification() + "/" + p.getMainFile(),
							name, "webproject", name, p.getIdentification(), LICENSING_STR, p.getIdentification()));
				});
			} catch (final Exception e) {
				log.error("An error has ocurred loading graph with web projects", e);
			}
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithDigitalTwins(List<DigitalTwinDevice> twins, User user) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.digitaltwin", "DIGITAL TWINS");
		final String description = utils.getMessage("tooltip_digitaltwin", null);

		if (twins == null) {
			twins = digitalTwinDeviceService.getAllByUserId(utils.getUserId());
		}
		if (null != user)
			twins = twins.stream().filter(t -> t.getUser().equals(user)).collect(Collectors.toList());
		try {
			arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlDigitalTwin + "list", GENERIC_USER_NAME,
					"digitaltwins", utils.getUserId(), name, "suit", description, urlDigitalTwin + CREATE_STR));
			twins.forEach(dt -> {
				arrayLinks.add(new GraphDTO(name, dt.getId(), urlDigitalTwin + "list",
						urlDigitalTwin + SHOW_STR + dt.getId(), name, "digitaltwin", name, dt.getIdentification(),
						LICENSING_STR, dt.getIdentification()));
			});
		} catch (final Exception e) {
			log.error("An error has ocurred loading graph with digital twins", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithClientPlatforms(List<ClientPlatform> clientPlatforms, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.clients", "PLATFORM CLIENTS");
		final String description = utils.getMessage("tooltip_clients", null);

		// carga de nodo clientPlatform
		arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlClientPlatform + "list", GENERIC_USER_NAME,
				"deviceandsystems", utils.getUserId(), name, "suit", description, urlClientPlatform + CREATE_STR));

		if (clientPlatforms == null) {
			clientPlatforms = clientPlatformRepository.findByUser(userService.getUser(utils.getUserId()));
		}
		if (null != user)
			clientPlatforms = clientPlatforms.stream().filter(c -> c.getUser().equals(user))
					.collect(Collectors.toList());
		try {
			for (final ClientPlatform clientPlatform : clientPlatforms) {
				// Creación de enlaces
				arrayLinks.add(new GraphDTO(name, clientPlatform.getId(), urlClientPlatform + "list",
						urlClientPlatform + clientPlatform.getIdentification(), name, "clientplatform", name,
						clientPlatform.getIdentification(), LICENSING_STR, clientPlatform.getIdentification()));

				if (clientPlatform.getClientPlatformOntologies() != null) {
					final List<ClientPlatformOntology> clientPlatformOntologies = new LinkedList<>(
							clientPlatform.getClientPlatformOntologies());
					for (final ClientPlatformOntology clientPlatformOntology : clientPlatformOntologies) {
						final Ontology ontology = clientPlatformOntology.getOntology();
						// Crea link entre ontologia y clientPlatform

						arrayLinks.add(new GraphDTO(ontology.getId(), clientPlatform.getId(),
								urlOntology + ontology.getId(), urlClientPlatform + clientPlatform.getIdentification(),
								ONTOLOGY_STR, "clientplatform", ontology.getIdentification(),
								clientPlatform.getIdentification(), LICENSING_STR, ontology.getIdentification()));
					}
				}
			}
		} catch (final Exception e) {
			log.error("An error has ocurred loading graph with client platforms", e);
		}

		return arrayLinks;
	}

	private List<GraphDTO> constructGraphWithGadgets(String visualizationId, String visualizationName,
			List<Gadget> gadgets, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.gadgets", "GADGETS");
		final String description = utils.getMessage("tooltip_gadgets",
				"Gadgets are visual elements that can be plugged into dashboards");

		// carga de nodo gadget dependiente de visualizacion
		arrayLinks.add(new GraphDTO(visualizationId, name, null, urlGadget + "list", visualizationId, name,
				visualizationName, name, "suit", description, urlGadget + "selectWizard"));

		if (gadgets == null) {
			gadgets = gadgetRepository.findByUser(userService.getUser(utils.getUserId()));
		}
		if (null != user)
			gadgets = gadgets.stream().filter(o -> o.getUser().equals(user)).collect(Collectors.toList());
		if (gadgets != null) {
			try {
				for (final Gadget gadget : gadgets) {
					// Creación de enlaces
					arrayLinks.add(new GraphDTO(name, gadget.getId(), urlGadget + "list",
							urlGadget + "update/" + gadget.getId(), name, "gadget", name, gadget.getIdentification(),
							LICENSING_STR, gadget.getIdentification()));

				}
				gadgets.clear();
			} catch (final Exception e) {
				log.error("An error has ocurred loading graph with gadgets", e);
			}
		}

		return arrayLinks;
	}

	private List<GraphDTO> constructGraphWithDashboard(String visualizationId, String visualizationName,
			List<Dashboard> dashboards, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.dashboards", "DASHBOARDS");
		final String description = utils.getMessage("tooltip_dashboards",
				"Dashboards are used to represent information from different sources");
		arrayLinks.add(new GraphDTO(visualizationId, name, null, urlDashboard + "list", visualizationId, name,
				visualizationName, name, "suit", description, urlDashboard + CREATE_STR));

		if (dashboards == null) {
			dashboards = dashboardRepository.findByUser(userService.getUser(utils.getUserId()));
		}
		if (null != user)
			dashboards = dashboards.stream().filter(o -> o.getUser().equals(user)).collect(Collectors.toList());
		try {
			for (final Dashboard dashboard : dashboards) {
				try {
					arrayLinks.add(new GraphDTO(name, dashboard.getId(), urlDashboard + "list",
							urlDashboard + "view/" + dashboard.getId(), name, "dashboard", name,
							dashboard.getIdentification(), LICENSING_STR, dashboard.getIdentification()));
					final List<String> gadgetIds = getGadgetIdsFromModel(dashboard.getModel());
					for (final String gadget : gadgetIds) {
						arrayLinks.add(new GraphDTO(gadget, dashboard.getId(), urlGadget + "update/" + gadget,
								urlDashboard + dashboard.getId(), "gadget", "dashboard", null,
								dashboard.getIdentification(), LICENSING_STR, gadget));
					}
				} catch (final Exception e) {
					log.error("" + e);
				}
			}
		} catch (final Exception e) {
			log.error("An error has ocurred loading graph with dashboards", e);
		}

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithVisualization(List<Gadget> gadgets, List<Dashboard> dashboards, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.visualization", "VISUALIZATIONS");
		final String description = utils.getMessage("tooltip_visualization", null);
		// carga de nodo gadget
		arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, null, GENERIC_USER_NAME, "VISUALIZATIONS",
				utils.getUserId(), name, "suit", description, null));

		arrayLinks.addAll(constructGraphWithGadgets(name, name, gadgets, user));

		arrayLinks.addAll(constructGraphWithDashboard(name, name, dashboards, user));

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithNotebooks(List<Notebook> notebooks, User user) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		if (utils.getRole().equals("ROLE_DATASCIENTIST") || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			final String name = utils.getMessage("name.notebook", "NOTEBOOKS");
			final String description = utils.getMessage("tooltip_notebooks", null);

			if (notebooks == null) {
				notebooks = notebookService.getNotebooks(utils.getUserId());
			}
			if (null != user)
				notebooks = notebooks.stream().filter(n -> n.getUser().equals(user)).collect(Collectors.toList());
			try {
				arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlNotebook + "list", GENERIC_USER_NAME,
						"notebooks", utils.getUserId(), name, "suit", description, urlNotebook + CREATE_STR));
				notebooks.forEach(dt -> {
					arrayLinks.add(new GraphDTO(name, dt.getId(), urlNotebook + "list",
							urlNotebook + SHOW_STR + dt.getId(), name, "notebook", name, dt.getIdentification(),
							LICENSING_STR, dt.getIdentification()));
				});
			} catch (final Exception e) {
				log.error("An error has ocurred loading graph with notebooks", e);
			}
		}
		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithDataFlows(List<Pipeline> dataflows, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		if (utils.getRole().equals("ROLE_DATASCIENTIST") || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
			final String name = utils.getMessage("name.dataflow", "DATAFLOWS");
			final String description = utils.getMessage("tooltip_dataflows", null);

			if (dataflows == null) {
				dataflows = dataflowService.getPipelines(utils.getUserId());
			}
			if (null != user)
				dataflows = dataflows.stream().filter(o -> o.getUser().equals(user)).collect(Collectors.toList());
			try {
				arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlDataflow + "list", GENERIC_USER_NAME,
						"pipelines", utils.getUserId(), name, "suit", description, urlDataflow + CREATE_STR));
				dataflows.forEach(dt -> {
					arrayLinks.add(new GraphDTO(name, dt.getId(), urlDataflow + "list",
							urlDataflow + SHOW_STR + dt.getId(), name, "pipeline", name, dt.getIdentification(),
							LICENSING_STR, dt.getIdentification()));
				});
			} catch (final Exception e) {
				log.error("An error has ocurred loading graph with dataflows", e);
			}
		}
		return arrayLinks;

	}

	public List<GraphDTO> constructGraphWithMicroservices(List<Microservice> microservices, User user) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.microservice", "MICROSERVICES");
		final String description = utils.getMessage("tooltip_microservices", null);

		if (microservices == null) {
			User userUtils= userService.getUser(utils.getUserId());
			microservices = microserviceService.getMicroservices(userUtils);
		}
		if (null != user)
			microservices = microservices.stream().filter(o -> o.getUser().equals(user)).collect(Collectors.toList());
		try {
			arrayLinks.add(new GraphDTO(GENERIC_USER_NAME, name, null, urlMicroservice + "list", GENERIC_USER_NAME,
					"microservices", utils.getUserId(), name, "suit", description, urlMicroservice + CREATE_STR));
			microservices.forEach(dt -> {
				arrayLinks.add(new GraphDTO(name, dt.getId(), urlMicroservice + "list",
						urlMicroservice + "update/" + dt.getId(), name, "microservice", name, dt.getIdentification(),
						LICENSING_STR, dt.getIdentification()));
			});
		} catch (final Exception e) {
			log.error("An error has ocurred loading graph with dataflows", e);
		}
		return arrayLinks;
	}
	
	public List<String> getGadgetIdsFromModel(String modelJson) throws IOException {
		final List<String> gadgetIds = new LinkedList<>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode jsonNode = objectMapper.readTree(modelJson);
		final int rows = jsonNode.path("rows").size();
		for (int i = 0; i < rows; i++) {
			final int columns = jsonNode.path("rows").path(i).path(COLUMNS_STR).size();
			for (int j = 0; j < columns; j++) {
				final int widgets = jsonNode.path("rows").path(i).path(COLUMNS_STR).path(j).path("widgets").size();
				for (int k = 0; k < widgets; k++) {
					String gadgetId = jsonNode.path("rows").path(i).path(COLUMNS_STR).path(j).path("widgets").path(k)
							.path("config").get("gadgetId").asText();
					gadgetId = gadgetId.split("_")[0];
					gadgetIds.add(gadgetId);
				}
			}

		}
		return gadgetIds;

	}

}