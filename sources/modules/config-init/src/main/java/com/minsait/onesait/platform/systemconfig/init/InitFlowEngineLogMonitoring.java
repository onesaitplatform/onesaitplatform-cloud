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
package com.minsait.onesait.platform.systemconfig.init;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ws.rs.NotFoundException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.dataflow.DataflowService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.flowEngineMonitor")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitFlowEngineLogMonitoring {

	private static final String RESOURCE_ENTITY_STR = "flowEngineResourceConsumption";
	private static final String LOG_ENTITY_STR = "flowEngineDomainLogs";
	private static final String ADMINISTRATOR = "administrator";
	private static final String DATAMODEL_EMPTY_BASE = "EmptyBase";
	private static final String DC_TOKEN = "e7ef0742d09d4de9c3412b0abdf7f826";
	private static final String QUERY = "query";
	private static final String DASHBOARD_FLOW_ENGINE_ID = "MASTER-Dashboard-FE-1";
	private static final String DASHBOARD_FLOW_ENGINE_IDENTIFICATION = "Dashboard Flow Engine Monitoring";
	private static final String[] DASHBOARD_FLOW_ENGINE_DATASOURCES_ID = new String[] { "MASTER-GadgetDatasource-FE-1",
			"MASTER-GadgetDatasource-FE-2", "MASTER-GadgetDatasource-FE-3", "MASTER-GadgetDatasource-FE-4",
			"MASTER-GadgetDatasource-FE-5" };
	private static final String[] DASHBOARD_FLOW_ENGINE_DATASOURCES_IDENTIFICATION = new String[] {
			"FlowEngineMonitor_cpu_usage", "FlowEngineMonitor_ram_usage", "FlowEngineMonitor_sockets_count",
			"FlowEngineMonitor_logs", "FlowEngineDomains" };
	private static final String[] DASHBOARD_FLOW_ENGINE_DATASOURCES_QUERY = new String[] {
			"SELECT cpu,domain,timestampISO FROM flowEngineResourceConsumption order by timestampISO asc",
			"SELECT memory/1024/1024 as memory,domain,timestampISO FROM flowEngineResourceConsumption order by timestampISO asc",
			"SELECT socketCount,domain,timestampISO FROM flowEngineResourceConsumption order by timestampISO asc",
			"SELECT * FROM flowEngineDomainLogs  order by timestamp desc",
			"SELECT distinct domain as domain FROM flowEngineResourceConsumption " };
	private static final int[] DASHBOARD_FLOW_ENGINE_DATASOURCES_LIMIT = new int[] { 2000, 2000, 2000, 2000, 2000 };
	private static final String[] DASHBOARD_FLOW_ENGINE_DATASOURCES_ONTOLOGY = new String[] {
			"flowEngineResourceConsumption", "flowEngineResourceConsumption", "flowEngineResourceConsumption",
			"flowEngineDomainLogs", "flowEngineResourceConsumption" };

	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_ID = new String[] { "MASTER-Gadget-FE-1",
			"MASTER-Gadget-FE-2", "MASTER-Gadget-FE-3", "MASTER-Gadget-FE-4" };
	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_IDENTIFICATION = new String[] {
			"FlowEngineMonitor_gadget_cpu_usage", "FlowEngineMonitor_gadget_memory_usage",
			"FlowEngineMonitor_gadget_socket_count", "FlowEngineMonitor_gadget_Domain_Logs" };
	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_TYPE = new String[] { "line", "line", "line", "table" };
	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_CONFIG = new String[] { "FlowEngineGadget_1_config.json",
			"FlowEngineGadget_2_config.json", "FlowEngineGadget_3_config.json", "FlowEngineGadget_4_config.json" };

	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_ID = new String[] { "MASTER-GadgetMeasure-1",
			"MASTER-GadgetMeasure-2", "MASTER-GadgetMeasure-3", "MASTER-GadgetMeasure-4", "MASTER-GadgetMeasure-5",
			"MASTER-GadgetMeasure-6", "MASTER-GadgetMeasure-7", "MASTER-GadgetMeasure-8" };
	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_DATASOURCE_ID = new String[] {
			"MASTER-GadgetDatasource-FE-1", "MASTER-GadgetDatasource-FE-4", "MASTER-GadgetDatasource-FE-4",
			"MASTER-GadgetDatasource-FE-4", "MASTER-GadgetDatasource-FE-2", "MASTER-GadgetDatasource-FE-4",
			"MASTER-GadgetDatasource-FE-4", "MASTER-GadgetDatasource-FE-3" };
	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_GADGET_ID = new String[] { "MASTER-Gadget-FE-1",
			"MASTER-Gadget-FE-4", "MASTER-Gadget-FE-4", "MASTER-Gadget-FE-4", "MASTER-Gadget-FE-2",
			"MASTER-Gadget-FE-4", "MASTER-Gadget-FE-4", "MASTER-Gadget-FE-3" };
	private static final String[] DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_CONFIG_ID = new String[] {
			"{\"fields\":[\"timestampISO\",\"cpu\"],\"name\":\"CPU usage\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}",
			"{\"fields\":[\"msg.msgid\"],\"name\":\"msg ID\",\"config\":{\"position\":\"3\"}}",
			"{\"fields\":[\"type\"],\"name\":\"Type\",\"config\":{\"position\":\"2\"}}",
			"{\"fields\":[\"level_name\"],\"name\":\"Level\",\"config\":{\"position\":\"1\"}}",
			"{\"fields\":[\"timestampISO\",\"memory\"],\"name\":\"\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}",
			"{\"fields\":[\"msg.message\"],\"name\":\"Message\",\"config\":{\"position\":\"4\"}}",
			"{\"fields\":[\"timestampISO\"],\"name\":\"Timestamp\",\"config\":{\"position\":\"0\"}}",
			"{\"fields\":[\"timestampISO\",\"socketCount\"],\"name\":\"Sockets\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}" };

	@Autowired
	@Lazy
	private OntologyService ontologyService;

	@Autowired
	ClientPlatformRepository clientPlatformRepository;
	@Autowired
	OntologyRepository ontologyRepository;
	@Autowired
	UserRepository userCDBRepository;
	@Autowired
	DataModelRepository dataModelRepository;
	@Autowired
	private DataflowService dataflowService;
	@Autowired
	TokenRepository tokenRepository;
	@Autowired
	GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	GadgetRepository gadgetRepository;
	@Autowired
	DashboardRepository dashboardRepository;
	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	private User userAdministrator = null;

	@PostConstruct
	@Test
	public void init() throws GenericOPException {

		// ----------------------------
		// Creation of ontologies
		// ----------------------------
		log.info("Creating FlowEngine resources entity...");
		List<DataModel> dataModels;
		Ontology ontology;

		// Create ontology for resources consumption
		if (ontologyRepository.findByIdentification(RESOURCE_ENTITY_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-fe-resource-log");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_fe_resources_consumtion.json"));
			ontology.setDescription("It contains the history of resource consuption for every FlowEngine domain.");
			ontology.setIdentification(RESOURCE_ENTITY_STR);
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
				// createUserAccess("MASTER-Ontology-OpenData-1-UserAccess", "QUERY", ontology,
				// getUser());
			}

			log.info("FlowEngine resources entity successfully created");
		} else {
			log.info("FlowEngine resources entity already exists. Skipping step...");
		}

		// Create ontology for ops log
		log.info("Creating FlowEngine Domain detail ops entity...");
		Ontology ontology2;
		if (ontologyRepository.findByIdentification(LOG_ENTITY_STR) == null) {
			ontology2 = new Ontology();
			ontology2.setId("MASTER-Ontology-fe-ops");
			ontology2.setJsonSchema(loadFromResources("examples/OntologySchema_fe_logs.json"));
			ontology2.setDescription("It contains the history of resource consuption for every FlowEngine domain.");
			ontology2.setIdentification(LOG_ENTITY_STR);
			ontology2.setMetainf("imported,json");
			ontology2.setActive(true);
			ontology2.setRtdbClean(false);
			ontology2.setRtdbToHdb(false);
			ontology2.setPublic(false);
			ontology2.setUser(getUserAdministrator());
			ontology2.setAllowsCypherFields(false);
			ontology2.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology2.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology2, null);
				// createUserAccess("MASTER-Ontology-OpenData-1-UserAccess", "QUERY", ontology,
				// getUser());
			}
			log.info("FlowEngine Domain detail ops entity successfully created");
		} else {
			log.info("FlowEngine Domain detail ops entity already exists. Skipping step...");
		}
		log.info("Creating Digital client...");

		// ----------------------------
		// Creation of Digital Client
		// ----------------------------

		ClientPlatform client = new ClientPlatform();
		if (!clientPlatformRepository.findById("MASTER-FlowEngine-internal-logging").isPresent()) {

			client.setId("MASTER-FlowEngine-internal-logging");
			client.setUser(getUserAdministrator());
			client.setIdentification("FlowEngine-logging-client");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("ClientPatform created for FlowEngine logging.");
			clientPlatformRepository.save(client);
			Set<ClientPlatformOntology> ontologies = new HashSet<>();
			ClientPlatformOntology clientPlatform1 = new ClientPlatformOntology();
			clientPlatform1.setOntology(ontologyRepository.findByIdentification(RESOURCE_ENTITY_STR));
			clientPlatform1.setAccess(AccessType.ALL);
			clientPlatform1.setClientPlatform(client);
			ontologies.add(clientPlatform1);
			ClientPlatformOntology clientPlatform2 = new ClientPlatformOntology();
			clientPlatform2.setOntology(ontologyRepository.findByIdentification(LOG_ENTITY_STR));
			clientPlatform2.setAccess(AccessType.ALL);
			clientPlatform2.setClientPlatform(client);
			ontologies.add(clientPlatform2);
			client.setClientPlatformOntologies(ontologies);
			clientPlatformRepository.save(client);

			final Set<Token> hashSetTokens = new HashSet<>();

			Token token = new Token();
			token.setId("MASTER-Token-fe-client");
			token.setClientPlatform(client);
			token.setTokenName(DC_TOKEN);
			token.setActive(true);
			hashSetTokens.add(token);
			client.setTokens(hashSetTokens);
			tokenRepository.save(token);

			log.info("Digital client successfully created");
		} else {
			log.info("Digital client already exists. Skipping step...");
		}

		// ----------------------------
		// Creation of Dataflow
		// ----------------------------

		log.info("Creating log reader Dataflow...");
		Pipeline pipeline = null;
		try {
			pipeline = dataflowService.getPipelineByIdentification("FlowEngineLogProcessor");
		} catch (NotFoundException e) {
			log.info("Creating log reader Dataflow... Dataflow does not Exist.");
		}

		if (pipeline == null) {
			String pipelineConfig = loadFromResources("dataflows/FlowEngineLogProcessor.json");
			// change config pipeline parameters for token
			pipelineConfig = pipelineConfig.replaceFirst("DC_TOKEN_PLACEHOLDER", DC_TOKEN);
			dataflowService.importPipeline(ADMINISTRATOR, "FlowEngineLogProcessor", pipelineConfig, false);
			log.info("Log reader Dataflow successfully created");
		} else {
			log.info("Log reader Dataflow already exists. Skipping step...");
		}

		// ----------------------------
		// Creation of Datasources
		// ----------------------------
		initGadgetDatasourcesQueriesFlowEngine();

		// ----------------------------
		// Creation of Gadgets
		// ----------------------------

		initGadgetFlowEngine();

		// ----------------------------
		// Creation of Gadgets Measures
		// ----------------------------

		initGadgetMeasuresFlowEngine();

		// ----------------------------
		// Creation of Dashboard
		// ----------------------------
		initDashboardFlowEngine();
	}

	// UTILS

	private void initGadgetDatasourcesQueriesFlowEngine() {

		for (int i = 0; i < DASHBOARD_FLOW_ENGINE_DATASOURCES_ID.length; i++) {
			if (!gadgetDatasourceRepository.findById(DASHBOARD_FLOW_ENGINE_DATASOURCES_ID[i]).isPresent()) {
				GadgetDatasource gadgetDatasources = new GadgetDatasource();
				log.info("Creating " + DASHBOARD_FLOW_ENGINE_DATASOURCES_ID[i] + " Datasource ...");
				gadgetDatasources.setId(DASHBOARD_FLOW_ENGINE_DATASOURCES_ID[i]);
				gadgetDatasources.setIdentification(DASHBOARD_FLOW_ENGINE_DATASOURCES_IDENTIFICATION[i]);
				gadgetDatasources.setDescription(DASHBOARD_FLOW_ENGINE_DATASOURCES_IDENTIFICATION[i]);
				gadgetDatasources.setMode(QUERY);
				gadgetDatasources.setQuery(DASHBOARD_FLOW_ENGINE_DATASOURCES_QUERY[i]);
				gadgetDatasources.setDbtype("RTDB");
				gadgetDatasources.setRefresh(0);
				gadgetDatasources.setOntology(
						ontologyRepository.findByIdentification(DASHBOARD_FLOW_ENGINE_DATASOURCES_ONTOLOGY[i]));
				gadgetDatasources.setMaxvalues(DASHBOARD_FLOW_ENGINE_DATASOURCES_LIMIT[i]);
				gadgetDatasources.setConfig("{\"simpleMode\":true}");
				gadgetDatasources.setUser(getUserAdministrator());
				gadgetDatasourceRepository.save(gadgetDatasources);

				log.info(DASHBOARD_FLOW_ENGINE_DATASOURCES_ID[i] + " Datasource successfully created");
			}
		}
	}

	private void initGadgetFlowEngine() {
		for (int i = 0; i < DASHBOARD_FLOW_ENGINE_GADGET_ID.length; i++) {
			if (gadgetRepository.findById(DASHBOARD_FLOW_ENGINE_GADGET_ID[i]).isEmpty()) {
				log.info("Creating " + DASHBOARD_FLOW_ENGINE_GADGET_IDENTIFICATION[i] + " Gadget ...");
				Gadget gadget = new Gadget();
				gadget.setId(DASHBOARD_FLOW_ENGINE_GADGET_ID[i]);
				gadget.setIdentification(DASHBOARD_FLOW_ENGINE_GADGET_IDENTIFICATION[i]);
				gadget.setPublic(false);
				gadget.setDescription("");
				gadget.setType(gadgetTemplateRepository.findById(DASHBOARD_FLOW_ENGINE_GADGET_TYPE[i]).orElse(null));
				gadget.setConfig(loadFromResources("dashboardmodel/" + DASHBOARD_FLOW_ENGINE_GADGET_CONFIG[i]));
				gadget.setUser(getUserAdministrator());
				gadgetRepository.save(gadget);
				log.info(DASHBOARD_FLOW_ENGINE_GADGET_IDENTIFICATION[i] + " Gadget successfully created");
			}
		}
	}

	private void initGadgetMeasuresFlowEngine() {
		for (int i = 0; i < DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_ID.length; i++) {
			if (!gadgetMeasureRepository.findById(DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_ID[i]).isEmpty()) {
				log.info("Creating " + DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_ID[i] + " Gadget Measures...");
				final GadgetMeasure gadgetMeasure = new GadgetMeasure();
				gadgetMeasure.setId(DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_ID[i]);
				gadgetMeasure.setDatasource(gadgetDatasourceRepository
						.findById(DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_DATASOURCE_ID[i]).get());
				gadgetMeasure.setConfig(DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_CONFIG_ID[i]);
				gadgetMeasure.setGadget(
						gadgetRepository.getReferenceById(DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_GADGET_ID[i]));
				gadgetMeasureRepository.save(gadgetMeasure);
				log.info(DASHBOARD_FLOW_ENGINE_GADGET_MEASURE_ID[i] + " Gadget Measures successfully created");
			}
		}
	}

	private void initDashboardFlowEngine() {
		if (!dashboardRepository.findById(DASHBOARD_FLOW_ENGINE_ID).isPresent()) {
			log.info("init DashboardQueriesProfilerUI");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId(DASHBOARD_FLOW_ENGINE_ID);
			dashboard.setIdentification(DASHBOARD_FLOW_ENGINE_IDENTIFICATION);
			dashboard.setDescription(DASHBOARD_FLOW_ENGINE_IDENTIFICATION);
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/FlowengineMonitorModel.json"));
			dashboard.setPublic(false);
			dashboard.setHeaderlibs(loadFromResources("dashboardmodel/FlowEngineMonitorHeaderLibs.txt"));
			dashboard.setUser(getUserAdministrator());

			dashboardRepository.save(dashboard);
		}
	}

	private User getUserAdministrator() {
		if (userAdministrator == null) {
			userAdministrator = userCDBRepository.findByUserId(ADMINISTRATOR);
		}
		return userAdministrator;
	}

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					StandardCharsets.UTF_8);

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						StandardCharsets.UTF_8);
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}
}
