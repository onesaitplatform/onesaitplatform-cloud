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
package com.minsait.onesait.platform.controlpanel.controller.adminHome;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdminGraphUtil {

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
    private String urlUsers;
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

    private static final String CREATE_STR = "create";

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
        urlUsers = url + "/users/";
    }

    public List<AdminGraphDTO> constructGraphWithOntologies(List<Ontology> ontologies, User user) {

        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.ontologies", "ONTOLOGIES");
        final String description = utils.getMessage("tooltip_ontologies", null);

        if (ontologies == null) {
            if (utils.isAdministrator())
                ontologies = ontologyRepository.findAll();
            else
                ontologies = ontologyRepository
                    .findByUserAndOntologyUserAccessAndAllPermissions(userService.getUser(utils.getUserId()));
        }

        arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlOntology + "list", GENERIC_USER_NAME,
            "ONTOLOGIES", utils.getUserId(), name, "suit", description, urlOntology + CREATE_STR, ontologies.size()));
        
        return arrayLinks;
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

    public List<AdminGraphDTO> constructGraphWithUsers(List<User> users) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.users", "USERS");
        final String description = utils.getMessage("tooltip_users", null);

        if (users == null) {
            users = userService.getAllUsers();
        }
        try {
            arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlUsers + "list", GENERIC_USER_NAME, "users",
                utils.getUserId(), name, "suit", description, urlUsers + CREATE_STR, users.size()));
        } catch (final Exception e) {
            log.error("An error has ocurred loading graph with apis", e);
        }
        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithAPIs(List<Api> apis, User user) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.apis", "APIS");
        final String description = utils.getMessage("tooltip_apis", null);

        if (apis == null) {
            apis = apiManagerService.loadAPISByFilter(null, null, null, utils.getUserId());
        }
        
        try {
            arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlApis + "list", GENERIC_USER_NAME, "apis",
                utils.getUserId(), name, "suit", description, urlApis + CREATE_STR, apis.size()));
        } catch (final Exception e) {
            log.error("An error has ocurred loading graph with apis", e);
        }

        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithFlows(List<FlowDomain> domains, User user) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.flows", "FLOWS");
        final String description = utils.getMessage("tooltip_flows", null);

        if (domains == null) {
            domains = flowService.getFlowDomainByUser(userService.getUser(utils.getUserId()));
        }
        try {
            arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlFlows + "list", GENERIC_USER_NAME, "flows",
                utils.getUserId(), name, "suit", description, urlFlows + CREATE_STR, domains.size()));
        } catch (final Exception e) {
            log.error("An error has ocurred loading graph with flows", e);
        }

        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithWebProjects(List<WebProjectDTO> projects, User user) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.webprojects", "WEB PROJECTS");
        final String description = utils.getMessage("tooltip_webprojects", null);

        if (projects != null) {
            
            try {
                arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlWebProjects + "list", GENERIC_USER_NAME,
                    "webprojects", utils.getUserId(), name, "suit", description, urlWebProjects + CREATE_STR,
                    projects.size()));
            } catch (final Exception e) {
                log.error("An error has ocurred loading graph with web projects", e);
            }
        }
        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithDigitalTwins(List<DigitalTwinDevice> twins, User user) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.digitaltwin", "DIGITAL TWINS");
        final String description = utils.getMessage("tooltip_digitaltwin", null);

        if (twins == null) {
            twins = digitalTwinDeviceService.getAllByUserId(utils.getUserId());
        }
        
        try {
            arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlDigitalTwin + "list", GENERIC_USER_NAME,
                "digitaltwins", utils.getUserId(), name, "suit", description, urlDigitalTwin + CREATE_STR,
                twins.size()));
        } catch (final Exception e) {
            log.error("An error has ocurred loading graph with digital twins", e);
        }

        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithClientPlatforms(List<ClientPlatform> clientPlatforms, User user) {

        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.clients", "PLATFORM CLIENTS");
        final String description = utils.getMessage("tooltip_clients", null);

        if (clientPlatforms == null) {
            if (utils.isAdministrator())
                clientPlatforms = clientPlatformRepository.findAll();
            else
                clientPlatforms = clientPlatformRepository.findByUser(userService.getUser(utils.getUserId()));
        }

        try {
            // carga de nodo clientPlatform
            arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlClientPlatform + "list", GENERIC_USER_NAME,
                "deviceandsystems", utils.getUserId(), name, "suit", description, urlClientPlatform + CREATE_STR,
                clientPlatforms.size()));
        } catch (final Exception e) {
            log.error("An error has ocurred loading graph with client platforms", e);
        }

        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithVisualization(List<Gadget> gadgets, List<Dashboard> dashboards,
        User user) {

        if (gadgets == null) {
            if(utils.isAdministrator())
                gadgets = gadgetRepository.findAll();
            else
                gadgets = gadgetRepository.findByUser(userService.getUser(utils.getUserId()));
        }

        if (dashboards == null) {
            if(utils.isAdministrator())
                dashboards = dashboardRepository.findAll();
            else
                dashboards = dashboardRepository.findByUser(userService.getUser(utils.getUserId()));
        }

        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        final String name = utils.getMessage("name.visualization", "VISUALIZATIONS");
        final String description = utils.getMessage("tooltip_visualization", null);
        // carga de nodo gadget
        arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, null, GENERIC_USER_NAME, "VISUALIZATIONS",
            utils.getUserId(), name, "suit", description, null, gadgets.size() + dashboards.size()));

        final String gadgetName = utils.getMessage("name.gadgets", "GADGETS");
        final String gadgetDescription = utils.getMessage("tooltip_gadgets",
            "Gadgets are visual elements that can be plugged into dashboards");
        if (gadgets != null) {
            try {
                // carga de nodo gadget dependiente de visualizacion
                arrayLinks.add(new AdminGraphDTO(name, gadgetName, urlGadget + "list", name, gadgetName,
                    name, gadgetName, "suit", gadgetDescription, urlGadget + "selectWizard", gadgets.size()));
            } catch (final Exception e) {
                log.error("An error has ocurred loading graph with gadgets", e);
            }
        }

        final String dashName = utils.getMessage("name.dashboards", "DASHBOARDS");
        final String dashDescription = utils.getMessage("tooltip_dashboards",
            "Dashboards are used to represent information from different sources");
        try {
            arrayLinks.add(new AdminGraphDTO(name, dashName, urlDashboard + "list", name, dashName,
                name, dashName, "suit", dashDescription, urlDashboard + CREATE_STR, dashboards.size()));
        } catch (final Exception e) {
            log.error("An error has ocurred loading graph with dashboards", e);
        }

        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithNotebooks(List<Notebook> notebooks, User user) {
        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        if (utils.getRole().equals("ROLE_DATASCIENTIST") || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
            final String name = utils.getMessage("name.notebook", "NOTEBOOKS");
            final String description = utils.getMessage("tooltip_notebooks", null);

            if (notebooks == null) {
                notebooks = notebookService.getNotebooks(utils.getUserId());
            }
            
            try {
                arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlNotebook + "list", GENERIC_USER_NAME,
                    "notebooks", utils.getUserId(), name, "suit", description, urlNotebook + CREATE_STR,
                    notebooks.size()));
            } catch (final Exception e) {
                log.error("An error has ocurred loading graph with notebooks", e);
            }
        }
        return arrayLinks;
    }

    public List<AdminGraphDTO> constructGraphWithDataFlows(List<Pipeline> dataflows, User user) {

        final List<AdminGraphDTO> arrayLinks = new LinkedList<>();
        if (utils.getRole().equals("ROLE_DATASCIENTIST") || utils.getRole().equals("ROLE_ADMINISTRATOR")) {
            final String name = utils.getMessage("name.dataflow", "DATAFLOWS");
            final String description = utils.getMessage("tooltip_dataflows", null);

            if (dataflows == null) {
                dataflows = dataflowService.getPipelines(utils.getUserId());
            }
            
            try {
                arrayLinks.add(new AdminGraphDTO(GENERIC_USER_NAME, name, urlDataflow + "list", GENERIC_USER_NAME,
                    "pipelines", utils.getUserId(), name, "suit", description, urlDataflow + CREATE_STR,
                    dataflows.size()));
            } catch (final Exception e) {
                log.error("An error has ocurred loading graph with dataflows", e);
            }
        }
        return arrayLinks;
    }
}
