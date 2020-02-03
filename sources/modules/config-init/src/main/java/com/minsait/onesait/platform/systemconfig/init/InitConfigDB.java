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
package com.minsait.onesait.platform.systemconfig.init;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.commons.OSDetector;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.BaseLayer;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.ConsoleMenu;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccessType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyCategory;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyDataType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.AggregationFunction;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.WindowType;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.PipelineUserAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Rollback;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.BaseLayerRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.ClientConnectionRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceSimulationRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.repository.ConsoleMenuRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyCategoryRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesPropertyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesWindowRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessRepository;
import com.minsait.onesait.platform.config.repository.OntologyUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyVirtualDatasourceRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.RollbackRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;
import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.configdb")
@RunWith(SpringRunner.class)
@Order(1)
@SpringBootTest
public class InitConfigDB {

	private static final String MICROSERVICE_STR = "microservice";
	private static final String TIMESERIE_STR = "TimeSerie";
	private static final String REST_STR = "Restaurants";
	private static final String ROUTES_STR = "routes";
	private static final String ROUTESEXT_STR = "routesexten";
	private static final String AIRPORT_STR = "airportsdata";
	private static final String QA_STR = "QA_OVERVIEW";
	private static final String PRODUCERERROR_STR = "Producer_ErrorCat";
	private static final String PRODUCERERRORCAT_STR = "producer_errorCat";
	private static final String PRODUCERERRORTYPE_STR = "producer_errorType";
	private static final String TRENDERROR_STR = "trend_errorCat";
	private static final String TRENDERRORTYPE_STR = "trend_errorType";
	private static final String ERRORSONDATE_STR = "errorsOnDate";
	private static final String ERRORBYSITE_STR = "errorsBySite";
	private static final String QADETAIL_STR = "QA_DETAIL";
	private static final String ERRORONDATE_STR = "errorsTypeOnDate";
	private static final String QADETAIL_EXT_STR = "QA_DETAIL_EXTENDED";
	private static final String METRICS_STR = "metrics";
	private static final String RESULT_STR = "result";
	private static final String OPERATIONTYPE_STR = "operationType";
	private static final String VALUE_STR = "value";
	private static final String SHA_STR = "SHA256(LoOY0z1pq+O2/h05ysBSS28kcFc8rSr7veWmyEi7uLs=)";
	private static final String RESTSCHEMA_STR = "examples/Restaurants-schema.json";
	private static final String GTKPEXAMPLE_STR = "GTKP-Example";
	private static final String GENERALIOT_STR = "General,IoT";
	private static final String GENERALIOTSMART_STR = "General,IoT,Smart Cities";
	private static final String GENERALIOTSMARTGSM_STR = "General,IoT,GSMA,Smart Cities";
	private static final String DESTINATIONMAP_STR = "countriesAsDestinationMap";
	private static final String ROUTESDESTTOP_STR = "routesDestTop";
	private static final String ROUTESORIGINTOP_STR = "routesOriginTop";
	private static final String DESCNOTEBOOKEXMP_STR = "Ontology for notebook-dashboard example";
	private static final String SEPARATOR = "  \r\n";
	private static final String SEPARATOR_BIS = "  }\r\n";

	private boolean started = false;
	private User userDeveloper = null;
	private User userAdministrator = null;
	private User user = null;
	private User userAnalytics = null;
	private GadgetDatasource gadgetDatasourceDeveloper = null;

	@Value("${onesaitplatform.webproject.rootfolder.path:/usr/local/webprojects/}")
	private String rootFolder;

	private static final String ISO3166_2 = "ISO3166_2";

	@Autowired
	private InitConfigDBDigitalTwin initDigitalTwin;

	@Autowired
	private WebProjectRepository webProjectRepository;

	@Autowired
	ClientConnectionRepository clientConnectionRepository;
	@Autowired
	ClientPlatformRepository clientPlatformRepository;
	@Autowired
	ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	ConsoleMenuRepository consoleMenuRepository;
	@Autowired
	DataModelRepository dataModelRepository;
	@Autowired
	DashboardRepository dashboardRepository;
	@Autowired
	GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	GadgetRepository gadgetRepository;
	@Autowired
	OntologyRepository ontologyRepository;
	@Autowired
	OntologyCategoryRepository ontologyCategoryRepository;

	@Autowired
	OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	OntologyUserAccessTypeRepository ontologyUserAccessTypeRepository;
	@Autowired
	DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	TokenRepository tokenRepository;
	@Autowired
	UserRepository userCDBRepository;
	@Autowired
	ConfigurationRepository configurationRepository;

	@Autowired
	FlowDomainRepository domainRepository;

	@Autowired
	RollbackRepository rollbackRepository;

	@Autowired
	DigitalTwinTypeRepository digitalTwinTypeRepository;

	@Autowired
	DigitalTwinDeviceRepository digitalTwinDeviceRepository;

	@Autowired
	UserTokenRepository userTokenRepository;

	@Autowired
	MarketAssetRepository marketAssetRepository;

	@Autowired
	NotebookRepository notebookRepository;

	@Autowired
	PipelineRepository pipelineRepository;

	@Autowired
	ClientPlatformInstanceSimulationRepository simulationRepository;

	@Autowired
	OntologyVirtualDatasourceRepository ontologyVirtualDataSourceRepository;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private AppRepository appRepository;

	@Autowired
	private BaseLayerRepository baseLayerRepository;

	@Autowired
	private NotebookUserAccessTypeRepository notebookUserAccessTypeRepository;

	@Autowired
	private PipelineUserAccessTypeRepository pipelineUserAccessTypeRepository;

	@Autowired
	private DashboardConfRepository dashboardConfRepository;

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private LayerRepository layerRepository;

	@Autowired
	private ViewerRepository viewerRepository;

	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;

	@Autowired
	private OntologyTimeSeriesPropertyRepository ontologyTimeSeriesPropertyRepository;

	@Autowired
	private OntologyTimeSeriesWindowRepository ontologyTimeSeriesWindowRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private SubcategoryRepository subcategoryRepository;

	@Value("${onesaitplatform.server.name:localhost}")
	private String serverName;

	@Value("${onesaitplatform.init.mailconfig}")
	private boolean loadMailConfig;

	@Value("${onesaitplatform.init.database.mongodb.servers:realtimedb:27017}")
	private String rtdbServers;

	private static final String DATAMODEL_EMPTY_BASE = "EmptyBase";

	private static final String MASTER_GADGET_1 = "MASTER-Gadget-1";
	private static final String MASTER_GADGET_2 = "MASTER-Gadget-2";
	private static final String MASTER_GADGET_3 = "MASTER-Gadget-3";
	private static final String MASTER_GADGET_4 = "MASTER-Gadget-4";
	private static final String MASTER_GADGET_5 = "MASTER-Gadget-5";
	private static final String MASTER_GADGET_6 = "MASTER-Gadget-6";
	private static final String MASTER_GADGET_7 = "MASTER-Gadget-7";
	private static final String MASTER_GADGET_8 = "MASTER-Gadget-8";
	private static final String MASTER_GADGET_9 = "MASTER-Gadget-9";
	private static final String MASTER_GADGET_10 = "MASTER-Gadget-10";
	private static final String MASTER_GADGET_11 = "MASTER-Gadget-11";
	private static final String MASTER_GADGET_12 = "MASTER-Gadget-12";
	private static final String MASTER_GADGET_13 = "MASTER-Gadget-13";
	private static final String MASTER_GADGET_14 = "MASTER-Gadget-14";
	private static final String MASTER_GADGET_15 = "MASTER-Gadget-15";

	private static final String MASTER_GADGET_DATASOURCE_1 = "MASTER-GadgetDatasource-1";
	private static final String MASTER_GADGET_DATASOURCE_2 = "MASTER-GadgetDatasource-2";
	private static final String MASTER_GADGET_DATASOURCE_3 = "MASTER-GadgetDatasource-3";
	private static final String MASTER_GADGET_DATASOURCE_4 = "MASTER-GadgetDatasource-4";
	private static final String MASTER_GADGET_DATASOURCE_5 = "MASTER-GadgetDatasource-5";
	private static final String MASTER_GADGET_DATASOURCE_6 = "MASTER-GadgetDatasource-6";
	private static final String MASTER_GADGET_DATASOURCE_7 = "MASTER-GadgetDatasource-7";
	private static final String MASTER_GADGET_DATASOURCE_8 = "MASTER-GadgetDatasource-8";
	private static final String MASTER_GADGET_DATASOURCE_9 = "MASTER-GadgetDatasource-9";
	private static final String MASTER_GADGET_DATASOURCE_10 = "MASTER-GadgetDatasource-10";
	private static final String MASTER_GADGET_DATASOURCE_11 = "MASTER-GadgetDatasource-11";
	private static final String MASTER_GADGET_DATASOURCE_12 = "MASTER-GadgetDatasource-12";
	private static final String MASTER_GADGET_DATASOURCE_13 = "MASTER-GadgetDatasource-13";
	private static final String MASTER_GADGET_DATASOURCE_14 = "MASTER-GadgetDatasource-14";
	private static final String MASTER_GADGET_DATASOURCE_15 = "MASTER-GadgetDatasource-15";

	private static final String ONTOLOGY_HELSINKIPOPULATION = "HelsinkiPopulation";
	private static final String TICKET = "Ticket";
	private static final String SUPERMARKETS = "Supermarkets";
	private static final String INUNDACIONES = "Inundaciones500";

	private static final String ISO3166_1 = "ISO3166_1";
	private static final String TICKETING_APP = "TicketingApp";
	private static final String DEFAULT = "default";
	private static final String DEVELOPER = "developer";
	private static final String ADMINISTRATOR = "administrator";
	private static final String DOCKER = "docker";
	private static final String QUERY = "query";
	private static final String CESIUM = "cesium";
	private static final String TABLE = "table";

	private static final String GADGET1CONFIG = "{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true}}]}}";
	private static final String GADGET2CONFIG = "{\"tablePagination\":{\"limit\":\"5\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\",\"trHeightFooter\":\"40\",\"textColorTHead\":\"#141414\",\"textColorBody\":\"#000000\",\"textColorFooter\":\"#000000\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}";
	private static final String GADGET5CONFIG = "{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}";

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;

			log.info("Start initConfigDB...");
			// first we need to create users
			initRoleUser();
			log.info("OK init_RoleUser");
			initUser();
			log.info("OK init_User");
			//
			initDataModel();
			log.info("OK init_DataModel");
			initOntologyCategory();
			log.info("OK init_OntologyCategory");
			initOntology();
			log.info("OK init_Ontology");
			initOntologyUserAccess();
			log.info("OK init_OntologyUserAccess");
			initOntologyUserAccessType();
			log.info("OK init_OntologyUserAccessType");

			initOntologyCategory();
			log.info("OK init_OntologyCategory");

			initClientPlatform();
			log.info("OK init_ClientPlatform");
			initClientPlatformOntology();
			log.info("OK init_ClientPlatformOntology");

			initToken();
			log.info("OK init_Token");

			initUserToken();
			log.info("OK USER_Token");

			initOntologyRestaurants();
			log.info("OK init_OntologyRestaurants");

			initGadgetDatasource();
			log.info("OK init_GadgetDatasource");

			initGadgetTemplate();
			log.info("OK init_GadgetTemplate");

			initGadget();
			log.info("OK init_Gadget");
			initGadgetMeasure();
			log.info("OK init_GadgetMeasure");

			initDashboard();
			log.info("OK init_Dashboard");
			initDashboardConf();
			log.info("OK init_DashboardConf");
			initDashboardUserAccessType();
			log.info("OK init_DashboardUserAccessType");

			initMenuControlPanel();
			log.info("OK init_ConsoleMenu");
			initConsoleMenuRollBack();
			log.info("OK initConsoleMenuRollBack");
			initConfiguration();
			log.info("OK init_Configuration");

			initFlowDomain();
			log.info("OK init_FlowDomain");

			initDigitalTwin.initDigitalTwinType();
			log.info("OK init_DigitalTwinType");

			initDigitalTwin.initDigitalTwinDevice();
			log.info("OK init_DigitalTwinDevice");

			initMarketPlace();
			log.info("OK init_Market");

			initNotebook();
			log.info("OK init_Notebook");

			initDataflow();
			log.info("OK init_dataflow");

			initNotebookUserAccessType();
			log.info("OK init_notebook_user_access_type");

			initDataflowUserAccessType();
			log.info("OK init_dataflow_user_access_type");

			initSimulations();
			log.info("OK init_simulations");

			initOpenFlightSample();
			log.info("OK init_openflight");

			initBaseLayers();
			log.info("OK init_BaseLayers");

			initQAWindTurbinesSample();
			log.info("OK init_QA_WindTurbines");

			initLayers();
			log.info("OK init_Layers");

			initViewers();
			log.info("OK init_Viewers");

			initRealms();
			log.info("OK initRealms");

			initCategories();
			log.info("OK Categories");

			// init_OntologyVirtualDatasource();
			// log.info(" OK init_OntologyVirtualDatasource");
			// init_realms();

			// initWebProject();
			// log.info("OK initWebProject");

			initWebProject();
			log.info("OK initWebProject");
		}

	}

	private void initBaseLayers() {
		final long count = baseLayerRepository.count();
		if (count == 0) {
			BaseLayer baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-1");
			baseLayer.setIdentification("osm.Mapnik.Labels");
			baseLayer.setName("Open Street Maps");
			baseLayer.setTechnology(CESIUM);
			baseLayer.setUrl("https://a.tile.openstreetmap.org/");

			baseLayerRepository.save(baseLayer);

			baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-2");
			baseLayer.setIdentification("esri.Topo.Labels");
			baseLayer.setName("ESRI World Topo Map");
			baseLayer.setTechnology(CESIUM);
			baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer");

			baseLayerRepository.save(baseLayer);

			baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-3");
			baseLayer.setIdentification("esri.Streets.Labels");
			baseLayer.setName("ESRI World Street Map");
			baseLayer.setTechnology(CESIUM);
			baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Street_Map/MapServer");

			baseLayerRepository.save(baseLayer);

			baseLayer = new BaseLayer();
			baseLayer.setId("MASTER-BaseLayer-4");
			baseLayer.setIdentification("esri.Imagery.NoLabels");
			baseLayer.setName("ESRI Imagery");
			baseLayer.setTechnology(CESIUM);
			baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer");

			baseLayerRepository.save(baseLayer);
		}
	}

	private void initRealms() {

		if (appRepository.findOne("MASTER-Realm-1") == null) {
			final App app = new App();
			app.setId("MASTER-Realm-1");
			app.setIdentification("GovConsole");
			app.setDescription("This is a realm provided for the governance console");
			app.setUser(getUserDeveloper());
			AppRole role = new AppRole();
			role.setApp(app);
			role.setDescription("Front-end developer");
			role.setName("FRONT");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Back-end developer");
			role.setName("BACK");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Product owner");
			role.setName("P.O.");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("UX designer");
			role.setName("UX-UI");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Devops CI/CD");
			role.setName("DEVOPS");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Administrator of the console");
			role.setName("ADMIN");

			final AppUser admin = new AppUser();
			admin.setRole(role);
			admin.setUser(getUserAdministrator());

			role = new AppRole();
			role.setApp(app);
			role.setDescription("User of the console");
			role.setName("USER");

			final AppUser userApp = new AppUser();
			userApp.setRole(role);
			userApp.setUser(getUser());

			app.setUserExtraFields(
					"{\"firstName\":\"string\",\"lastName\":\"string\",\"telephone\":\"string\",\"location\":{\"color\":\"string\",\"floor\":\"string\",\"place\":\"string\"}}");
			appRepository.save(app);
		}
	}

	private void initSimulations() {
		ClientPlatformInstanceSimulation simulation = simulationRepository.findById("MASTER-DeviceSimulation-1");
		if (simulation == null) {
			simulation = new ClientPlatformInstanceSimulation();
			simulation.setId("MASTER-DeviceSimulation-1");
			simulation.setActive(false);
			simulation.setCron("0/5 * * ? * * *");
			simulation.setIdentification("Issue generator");
			simulation.setInterval(5);
			simulation.setJson(loadFromResources("simulations/DeviceSimulation_example1.json"));
			simulation.setClientPlatform(clientPlatformRepository.findByIdentification(TICKETING_APP));
			simulation.setOntology(ontologyRepository.findByIdentification(TICKET));
			if (simulation.getClientPlatform() != null) {
				final List<Token> tokens = tokenRepository.findByClientPlatform(simulation.getClientPlatform());
				if (tokens != null && !tokens.isEmpty()) {
					simulation.setToken(tokens.get(0));
					simulation.setUser(getUserDeveloper());
					simulationRepository.save(simulation);
				}
			}
		}

	}

	public void initOpenFlightSample() {
		initOntologyOpenFlight();
		initDashboardOpenFlight();
		initGadgetOpenFlight();
		initGadgetDatasourceOpenFlight();
		initGadgetMeasureOpenFlight();
	}

	public void initQAWindTurbinesSample() {
		initOntologyQAWindTurbines();
		initDashboardQAWindTurbines();
		initGadgetQAWindTurbines();
		initGadgetDatasourceQAWindTurbines();
		initGadgetMeasureQAWindTurbines();
	}

	private void initFlowDomain() {
		log.info("init_FlowDomain");
		// Domain for administrator
		if (domainRepository.count() == 0) {
			FlowDomain domain = new FlowDomain();
			domain.setId("MASTER-FlowDomain-1");
			domain.setActive(true);
			domain.setIdentification("adminDomain");
			domain.setUser(userCDBRepository.findByUserId(ADMINISTRATOR));
			domain.setHome("/tmp/administrator");
			domain.setState("START");
			domain.setPort(8000);
			domain.setServicePort(7000);
			domainRepository.save(domain);
			// Domain for developer
			domain = new FlowDomain();
			domain.setId("MASTER-FlowDomain-2");
			domain.setActive(true);
			domain.setIdentification("devDomain");
			domain.setUser(userCDBRepository.findByUserId(DEVELOPER));
			domain.setHome("/tmp/developer");
			domain.setState("START");
			domain.setPort(8001);
			domain.setServicePort(7001);
			domainRepository.save(domain);
		}
	}

	private void initConfiguration() {
		log.info("init_Configuration");
		if (configurationRepository.count() == 0) {

			Configuration config = new Configuration();
			config.setId("MASTER-Configuration-1");
			config.setType(Configuration.Type.TWITTER);
			config.setUser(getUserAdministrator());
			config.setEnvironment("dev");
			config.setYmlConfig(loadFromResources("configurations/TwitterConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-2");
			config.setType(Configuration.Type.TWITTER);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setSuffix("lmgracia");
			config.setDescription("Twitter");
			config.setYmlConfig(loadFromResources("configurations/TwitterConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-3");
			config.setType(Configuration.Type.SCHEDULING);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("RtdbMaintainer config");
			config.setYmlConfig(loadFromResources("configurations/SchedulingConfiguration_default.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-4");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Endpoints default profile");
			config.setYmlConfig(loadFromResources("configurations/EndpointModulesConfigurationDefault.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-5");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DOCKER);
			config.setDescription("Endpoints docker profile");

			config.setYmlConfig(
					replaceEnvironment(loadFromResources("configurations/EndpointModulesConfigurationDocker.yml")));

			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-6");
			config.setType(Configuration.Type.MAIL);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			if (loadMailConfig)
				config.setYmlConfig(loadFromResources("configurations/MailConfiguration.yml"));
			else
				config.setYmlConfig(loadFromResources("configurations/MailConfigurationDefault.yml"));
			configurationRepository.save(config);

			//
			config = new Configuration();
			config.setId("MASTER-Configuration-8");
			config.setType(Configuration.Type.MONITORING);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setYmlConfig(loadFromResources("configurations/MonitoringConfiguration.yml"));
			configurationRepository.save(config);

			//
			config = new Configuration();
			config.setId("MASTER-Configuration-10");
			config.setType(Configuration.Type.OPENSHIFT);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Openshift configuration");
			config.setYmlConfig(loadFromResources("configurations/OpenshiftConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-11");
			config.setType(Configuration.Type.DOCKER);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Rancher docker compose configuration");
			config.setSuffix("Rancher");
			config.setYmlConfig(loadFromResources("configurations/DockerCompose_Rancher.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-12");
			config.setType(Configuration.Type.NGINX);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Nginx conf template");
			config.setSuffix("Nginx");
			config.setYmlConfig(loadFromResources("configurations/nginx-template.conf"));
			configurationRepository.save(config);
		}
		Configuration config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, DEFAULT);
		if (config == null) {
			config = new Configuration();
			config.setDescription("onesait Platform global configuration");
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-13");
			config.setType(Type.OPEN_PLATFORM);
			config.setUser(getUserAdministrator());
			config.setYmlConfig(loadFromResources("configurations/OpenPlatformConfiguration_default.yml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, DOCKER);
		if (config == null) {
			config = new Configuration();
			config.setDescription("onesait Platform global configuration");
			config.setEnvironment(DOCKER);
			config.setId("MASTER-Configuration-14");
			config.setType(Type.OPEN_PLATFORM);
			config.setUser(getUserAdministrator());
			config.setYmlConfig(
					replaceRTDBServers(loadFromResources("configurations/OpenPlatformConfiguration_docker.yml")));

			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndSuffix(Type.DOCKER, "default", "microservice");
		if (config == null) {
			config = new Configuration();
			config.setDescription(MICROSERVICE_STR);
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-16");
			config.setType(Type.DOCKER);
			config.setUser(getUserAdministrator());
			config.setSuffix(MICROSERVICE_STR);
			config.setYmlConfig(loadFromResources("configurations/Microservice-compose.yml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironmentAndSuffix(Type.JENKINS, DEFAULT,
				"IOT_CLIENT_ARCHETYPE");
		if (config == null) {
			config = new Configuration();
			config.setDescription("Pipeline XML for microservice generation");
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-17");
			config.setType(Type.JENKINS);
			config.setUser(getUserAdministrator());
			config.setSuffix("IOT_CLIENT_ARCHETYPE");
			config.setYmlConfig(loadFromResources("configurations/JenkinsXMLTemplateIoT.xml"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndSuffix(Type.JENKINS, DEFAULT, "ML_MODEL_ARCHETYPE");
		if (config == null) {
			config = new Configuration();
			config.setDescription("Pipeline XML for microservice generation");
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-18");
			config.setType(Type.JENKINS);
			config.setUser(getUserAdministrator());
			config.setSuffix("ML_MODEL_ARCHETYPE");
			config.setYmlConfig(loadFromResources("configurations/JenkinsXMLTemplateML.xml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironment(Type.GOOGLE_ANALYTICS, DEFAULT);
		if (config == null) {
			config = new Configuration();
			config.setId("MASTER-Configuration-19");
			config.setType(Configuration.Type.GOOGLE_ANALYTICS);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setYmlConfig(loadFromResources("configurations/GoogleAnalyticsConfiguration.yml"));
			configurationRepository.save(config);
		}

	}

	public void initClientPlatformOntology() {

		log.info("init ClientPlatformOntology");
		final List<ClientPlatformOntology> cpos = clientPlatformOntologyRepository.findAll();
		if (cpos.isEmpty()) {
			if (clientPlatformRepository.findAll().isEmpty())
				throw new GenericRuntimeOPException("There must be at least a ClientPlatform with id=1 created");
			if (ontologyRepository.findAll().isEmpty())
				throw new GenericRuntimeOPException("There must be at least a Ontology with id=1 created");
			log.info("No Client Platform Ontologies");

			ClientPlatformOntology cpo = new ClientPlatformOntology();
			cpo.setId("MASTER-ClientPlatformOntology-1");
			cpo.setClientPlatform(clientPlatformRepository.findByIdentification(TICKETING_APP));
			cpo.setOntology(ontologyRepository.findByIdentification(TICKET));
			cpo.setAccess(Ontology.AccessType.ALL);
			clientPlatformOntologyRepository.save(cpo);
			//
			cpo = new ClientPlatformOntology();
			cpo.setId("MASTER-ClientPlatformOntology-2");
			cpo.setClientPlatform(clientPlatformRepository.findByIdentification(GTKPEXAMPLE_STR));
			cpo.setOntology(ontologyRepository.findByIdentification(ONTOLOGY_HELSINKIPOPULATION));
			cpo.setAccess(Ontology.AccessType.ALL);
			clientPlatformOntologyRepository.save(cpo);
		}
	}

	public void initClientPlatform() {
		log.info("init ClientPlatform");
		final List<ClientPlatform> clients = clientPlatformRepository.findAll();
		if (clients.isEmpty()) {
			log.info("No clients ...");
			ClientPlatform client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-1");
			client.setUser(getUserDeveloper());
			client.setIdentification("Client-MasterData");
			client.setEncryptionKey("b37bf11c-631e-4bc4-ae44-910e58525952");
			client.setDescription("ClientPatform created as MasterData");
			clientPlatformRepository.save(client);

			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-2");
			client.setUser(getUserDeveloper());
			client.setIdentification(GTKPEXAMPLE_STR);
			client.setEncryptionKey("f9dfe72e-7082-4fe8-ba37-3f569b30a691");
			client.setDescription("ClientPatform created as Example");
			clientPlatformRepository.save(client);

			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-3");
			client.setUser(getUserDeveloper());
			client.setIdentification(TICKETING_APP);
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Platform client for issues and ticketing");
			clientPlatformRepository.save(client);

			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-4");
			client.setUser(getUserDeveloper());
			client.setIdentification("DeviceMaster");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Device template for testing");
			clientPlatformRepository.save(client);
		}

	}

	public void initMenuControlPanel() {
		log.info("init ConsoleMenu");
		final List<ConsoleMenu> menus = consoleMenuRepository.findAll();

		if (!menus.isEmpty()) {
			consoleMenuRepository.deleteAll();
		}

		log.info("No menu elents found...adding");
		try {
			log.info("Adding menu for role ADMIN");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-1");
			menu.setJson(loadFromResources("menu/menu_admin.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role ADMIN");
		}
		try {
			log.info("Adding menu for role DEVELOPER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-2");
			menu.setJson(loadFromResources("menu/menu_developer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DEVELOPER");
		}
		try {
			log.info("Adding menu for role USER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-3");

			menu.setJson(loadFromResources("menu/menu_user.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_USER.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role USER");
		}
		try {
			log.info("Adding menu for role ANALYTIC");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-4");
			menu.setJson(loadFromResources("menu/menu_analytic.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role ANALYTIC");
		}
		try {
			log.info("Adding menu for role DATAVIEWER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-5");
			menu.setJson(loadFromResources("menu/menu_dataviewer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DATAVIEWER");
		}
	}

	public void initConsoleMenuRollBack() {
		log.info("init ConsoleMenuRollBack");
		try {
			final ConsoleMenu menu = consoleMenuRepository.findById("MASTER-ConsoleMenu-1");

			Rollback rollback = rollbackRepository.findByEntityId(menu.getId());
			if (rollback == null) {
				rollback = new Rollback();

				rollback.setEntityId(menu.getId());
				rollback.setType(Rollback.EntityType.MENU);
			}
			final String result = toString(menu);
			rollback.setSerialization(result);

			rollbackRepository.save(rollback);
		} catch (final Exception e) {
			log.error("Error creating console menu rollback for MASTER-ConsoleMenu-1: " + e);
		}
		try {
			final ConsoleMenu menu = consoleMenuRepository.findById("MASTER-ConsoleMenu-2");

			Rollback rollback = rollbackRepository.findByEntityId(menu.getId());
			if (rollback == null) {
				rollback = new Rollback();

				rollback.setEntityId(menu.getId());
				rollback.setType(Rollback.EntityType.MENU);
			}
			final String result = toString(menu);
			rollback.setSerialization(result);

			rollbackRepository.save(rollback);
		} catch (final Exception e) {
			log.error("Error creating console menu rollback for MASTER-ConsoleMenu-2: " + e);
		}
		try {
			final ConsoleMenu menu = consoleMenuRepository.findById("MASTER-ConsoleMenu-3");

			Rollback rollback = rollbackRepository.findByEntityId(menu.getId());
			if (rollback == null) {
				rollback = new Rollback();

				rollback.setEntityId(menu.getId());
				rollback.setType(Rollback.EntityType.MENU);
			}
			final String result = toString(menu);
			rollback.setSerialization(result);

			rollbackRepository.save(rollback);
		} catch (final Exception e) {
			log.error("Error creating console menu rollback for MASTER-ConsoleMenu-3: " + e);
		}
		try {
			final ConsoleMenu menu = consoleMenuRepository.findById("MASTER-ConsoleMenu-4");

			Rollback rollback = rollbackRepository.findByEntityId(menu.getId());
			if (rollback == null) {
				rollback = new Rollback();

				rollback.setEntityId(menu.getId());
				rollback.setType(Rollback.EntityType.MENU);
			}
			final String result = toString(menu);
			rollback.setSerialization(result);

			rollbackRepository.save(rollback);
		} catch (final Exception e) {
			log.error("Error creating console menu rollback for MASTER-ConsoleMenu-4: " + e);
		}
		try {
			final ConsoleMenu menu = consoleMenuRepository.findById("MASTER-ConsoleMenu-5");

			Rollback rollback = rollbackRepository.findByEntityId(menu.getId());
			if (rollback == null) {
				rollback = new Rollback();

				rollback.setEntityId(menu.getId());
				rollback.setType(Rollback.EntityType.MENU);
			}
			final String result = toString(menu);
			rollback.setSerialization(result);

			rollbackRepository.save(rollback);
		} catch (final Exception e) {
			log.error("Error creating console menu rollback for MASTER-ConsoleMenu-5: " + e);
		}
	}

	private static String toString(Serializable o) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();

		return Base64.getEncoder().encodeToString(baos.toByteArray());

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

	private byte[] loadFileFromResources(String name) {
		try {
			final Resource resource = resourceLoader.getResource("classpath:" + name);
			final InputStream is = resource.getInputStream();
			return IOUtils.toByteArray(is);

		} catch (final Exception e) {
			log.error("Error loading resource: " + name + ".Please check if this error affect your database");
			log.error(e.getMessage());
			throw new GenericRuntimeOPException(e);
		}
	}

	private String replaceEnvironment(String yamlSt) {
		try {
			if (yamlSt == null)
				throw new GenericRuntimeOPException("YAML is null");
			return yamlSt.replace("${SERVER_NAME}", serverName);
		} catch (final Exception e) {
			log.error("Error replacing environment: " + serverName + ".On endpoint configuration file");
			log.error(e.getMessage());
			throw new GenericRuntimeOPException(e);
		}
	}

	private String replaceRTDBServers(String yamlSt) {
		try {
			return yamlSt.replace("${REALTIMEDBSERVERS}", rtdbServers);
		} catch (final Exception e) {
			log.error("Error replacing RTDB servers: " + rtdbServers + ".On endpoint configuration file");
			log.error(e.getMessage());
			return null;
		}
	}

	public void initDashboardConf() {
		log.info("init DashboardConf");
		final List<DashboardConf> dashboardsConf = dashboardConfRepository.findAll();
		if (dashboardsConf.isEmpty()) {
			log.info("No dashboardsConf...adding");
			// Default
			final DashboardConf dashboardConfDefault = new DashboardConf();
			final String defaultSchema = "{\"header\":{\"title\":\"My Dashboard\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfDefault.setId("MASTER-DashboardConf-1");
			dashboardConfDefault.setIdentification(DEFAULT);
			dashboardConfDefault.setModel(defaultSchema);
			dashboardConfDefault.setDescription("Default style");
			dashboardConfRepository.save(dashboardConfDefault);
			// Iframe
			final DashboardConf dashboardConfNoTitle = new DashboardConf();
			final String notitleSchema = "{\"header\":{\"title\":\"\",\"enable\":false,\"height\":0,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfNoTitle.setId("MASTER-DashboardConf-2");
			dashboardConfNoTitle.setIdentification("notitle");
			dashboardConfNoTitle.setModel(notitleSchema);
			dashboardConfNoTitle.setDescription("No title style");
			dashboardConfRepository.save(dashboardConfNoTitle);
			// notitle echarts
			final DashboardConf dashboardConfNoTitleEcharts = new DashboardConf();
			final String notitleechartsSchema = "{\"header\":{\"title\":\"\",\"enable\":false,\"height\":0,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfNoTitleEcharts.setId("MASTER-DashboardConf-3");
			dashboardConfNoTitleEcharts.setIdentification("notitleecharts");
			dashboardConfNoTitleEcharts.setModel(notitleechartsSchema);
			dashboardConfNoTitleEcharts.setHeaderlibs(" <!-- ECHARTS -->\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.common.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/bmap.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/dataTool.min.js\"></script>\r\n"
					+ " 	<!-- ECHARTS -->\r\n" + "    <!-- DATA TABLE -->\r\n"
					+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css\"/>\r\n"
					+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/responsive/2.2.3/css/responsive.dataTables.min.css\"/>\r\n"
					+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/buttons/1.5.6/css/buttons.dataTables.min.css\"/>\r\n"
					+ " 	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/jquery.dataTables.min.js\"></script>\r\n"
					+ "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/dataTables.bootstrap4.min.js\"></script>\r\n"
					+ "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/responsive/2.2.3/js/dataTables.responsive.min.js\"></script>\r\n"
					+ "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/dataTables.buttons.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/buttons.colVis.min.js\"></script>\r\n"
					+ "     <!-- DATA TABLE -->");
			dashboardConfNoTitleEcharts
					.setDescription("No title style with ECharts libraries, and Datatable libraries");
			dashboardConfRepository.save(dashboardConfNoTitleEcharts);

		}
		if (!dashboardConfRepository.exists("MASTER-DashboardConf-4")) {
			final DashboardConf dashboardConfSynoptic = new DashboardConf();
			final String synopticSchema = "{\"header\":{\"title\":\" \",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":false},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fixed\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":20,\"fixedRowHeight\":20,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfSynoptic.setId("MASTER-DashboardConf-4");
			dashboardConfSynoptic.setIdentification("fixed");
			dashboardConfSynoptic.setModel(synopticSchema);
			dashboardConfSynoptic.setDescription("Fixed style");
			dashboardConfRepository.save(dashboardConfSynoptic);
		}
		if (!dashboardConfRepository.exists("MASTER-DashboardConf-5")) {
			final DashboardConf dashboardConfNoTitleEcharts = new DashboardConf();
			final String notitleechartsSchema = "{\"header\":{\"title\":\"\",\"enable\":false,\"height\":0,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fixed\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfNoTitleEcharts.setId("MASTER-DashboardConf-5");
			dashboardConfNoTitleEcharts.setIdentification("notitleechartsfixed");
			dashboardConfNoTitleEcharts.setModel(notitleechartsSchema);
			dashboardConfNoTitleEcharts.setHeaderlibs(" <!-- ECHARTS -->\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.common.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/bmap.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/dataTool.min.js\"></script>\r\n"
					+ " 	<!-- ECHARTS -->\r\n" + "    <!-- DATA TABLE -->\r\n"
					+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css\"/>\r\n"
					+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/responsive/2.2.3/css/responsive.dataTables.min.css\"/>\r\n"
					+ "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/buttons/1.5.6/css/buttons.dataTables.min.css\"/>\r\n"
					+ " 	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/jquery.dataTables.min.js\"></script>\r\n"
					+ "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/dataTables.bootstrap4.min.js\"></script>\r\n"
					+ "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/responsive/2.2.3/js/dataTables.responsive.min.js\"></script>\r\n"
					+ "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/dataTables.buttons.min.js\"></script>\r\n"
					+ "    <script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/buttons.colVis.min.js\"></script>\r\n"
					+ "     <!-- DATA TABLE -->");
			dashboardConfNoTitleEcharts
					.setDescription("No title style with ECharts libraries, and Datatable libraries");
			dashboardConfRepository.save(dashboardConfNoTitleEcharts);
		}

	}

	public void initDashboard() {
		log.info("init Dashboard");
		final List<Dashboard> dashboards = dashboardRepository.findAll();
		if (dashboards.isEmpty()) {
			log.info("No dashboards...adding");

			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-1");
			dashboard.setIdentification("TempDeveloperDashboard");
			dashboard.setDescription("Dashboard analytics restaurants");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(
					"{\"header\":{\"title\":\"My new osp Dashboard\",\"enable\":true,\"height\":56,\"logo\":{\"height\":48},\"backgroundColor\":\"hsl(220, 23%, 20%)\",\"textColor\":\"hsl(0, 0%, 100%)\",\"iconColor\":\"hsl(0, 0%, 100%)\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{\"$$hashKey\":\"object:64\"},{\"x\":0,\"y\":0,\"cols\":20,\"rows\":7,\"id\":\""
							+ getGadget().getId()
							+ "\",\"content\":\"bar\",\"type\":\"bar\",\"header\":{\"enable\":true,\"title\":{\"icon\":\"\",\"iconColor\":\"hsl(220, 23%, 20%)\",\"text\":\"My Gadget\",\"textColor\":\"hsl(220, 23%, 20%)\"},\"backgroundColor\":\"hsl(0, 0%, 100%)\",\"height\":\"25\"},\"backgroundColor\":\"white\",\"padding\":0,\"border\":{\"color\":\"#c7c7c7de\",\"width\":1,\"radius\":5},\"$$hashKey\":\"object:107\"}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[],\"livehtml_1526292431685\":[],\"b163b6e4-a8d2-4c3b-b964-5efecf0dd3a0\":[]}}");
			dashboard.setPublic(true);
			dashboard.setUser(getUserDeveloper());

			dashboardRepository.save(dashboard);
		}
	}

	public void initDashboardOpenFlight() {
		if (dashboardRepository.findById("MASTER-Dashboard-2") == null) {
			log.info("init Dashboard OpenFlight");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-2");
			dashboard.setIdentification("Visualize OpenFlights Data");
			dashboard.setDescription("Visualize OpenFlights Data example from notebook data");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/OpenFlight.json"));
			dashboard.setPublic(true);
			dashboard.setUser(getUserAnalytics());

			dashboardRepository.save(dashboard);
		}
	}

	public void initDashboardQAWindTurbines() {
		if (dashboardRepository.findByIdentification("QA_WindTurbines_dashboard") == null) {
			log.info("init Dashboard QA_WindTurbines");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-3");
			dashboard.setIdentification("QA_WindTurbines_dashboard");
			dashboard.setDescription("Dashboard to visualize data from QA_DETAIL");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/QA_WindTurbines.json"));
			dashboard.setPublic(true);
			dashboard.setUser(getUserAnalytics());

			dashboardRepository.save(dashboard);
		}
	}

	private Gadget getGadget() {
		final List<Gadget> gadgets = gadgetRepository.findAll();
		return gadgets.get(0);
	}

	private User getUserDeveloper() {
		if (userDeveloper == null)
			userDeveloper = userCDBRepository.findByUserId(DEVELOPER);
		return userDeveloper;
	}

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = userCDBRepository.findByUserId(ADMINISTRATOR);
		return userAdministrator;
	}

	private User getUser() {
		if (user == null)
			user = userCDBRepository.findByUserId("user");
		return user;
	}

	private User getUserAnalytics() {
		if (userAnalytics == null)
			userAnalytics = userCDBRepository.findByUserId("analytics");
		return userAnalytics;
	}

	private GadgetDatasource getGadgetDatasourceDeveloper() {
		if (gadgetDatasourceDeveloper == null)
			gadgetDatasourceDeveloper = gadgetDatasourceRepository.findAll().get(0);
		return gadgetDatasourceDeveloper;
	}

	public void initDataModel() {

		log.info("init DataModel");
		final List<DataModel> dataModels = dataModelRepository.findAll();

		if (dataModels.isEmpty()) {
			log.info("No DataModels ...");
			DataModel dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-1");
			dataModel.setIdentification("Alarm");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Alarm.json"));
			dataModel.setDescription("Base Alarm: assetId, timestamp, severity, source, details and status..");
			dataModel.setLabels("Alarm,General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-2");
			dataModel.setIdentification("Audit");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Audit.json"));
			dataModel.setDescription("Base Audit");
			dataModel.setLabels("Audit,General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-3");
			dataModel.setIdentification("DeviceLog");
			dataModel.setTypeEnum(DataModel.MainType.SYSTEM_ONTOLOGY);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_DeviceLog.json"));
			dataModel.setDescription("Data model for device logging");
			dataModel.setLabels("General,IoT,Log");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-4");
			dataModel.setIdentification("Device");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Device.json"));
			dataModel.setDescription("Base Device");
			dataModel.setLabels("Audit,General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-5");
			dataModel.setIdentification(DATAMODEL_EMPTY_BASE);
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_EmptyBase.json"));
			dataModel.setDescription("Base DataModel");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-6");
			dataModel.setIdentification("Feed");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Feed.json"));
			dataModel.setDescription("Base Feed");
			dataModel.setLabels("Audit,General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-7");
			dataModel.setIdentification("Twitter");
			dataModel.setTypeEnum(DataModel.MainType.SOCIAL_MEDIA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Twitter.json"));
			dataModel.setDescription("Twitter DataModel");
			dataModel.setLabels("Twitter,Social Media");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-8");
			dataModel.setIdentification("BasicSensor");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_BasicSensor.json"));
			dataModel.setDescription("DataModel for sensor sending measures for an assetId");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-9");
			dataModel.setIdentification("GSMA-AirQualityObserved");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityObserved.json"));
			dataModel.setDescription("An observation of air quality conditions at a certain place and time");
			dataModel.setLabels(GENERALIOTSMARTGSM_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-10");
			dataModel.setIdentification("GSMA-AirQualityStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityStation.json"));
			dataModel.setDescription("Air Quality Station observing quality conditions at a certain place and time");
			dataModel.setLabels(GENERALIOTSMARTGSM_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-11");
			dataModel.setIdentification("GSMA-AirQualityThreshold");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityThreshold.json"));
			dataModel.setDescription(
					"Provides the air quality thresholds in Europe. Air quality thresholds allow to calculate an air quality index (AQI).");
			dataModel.setLabels(GENERALIOTSMARTGSM_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-12");
			dataModel.setIdentification("GSMA-Device");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Device.json"));
			dataModel.setDescription(
					"A Device is a tangible object which contains some logic and is producer and/or consumer of data. A Device is always assumed to be capable of communicating electronically via a network.");
			dataModel.setLabels(GENERALIOTSMARTGSM_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-13");
			dataModel.setIdentification("GSMA-KPI");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-KPI.json"));
			dataModel.setDescription(
					"Key Performance Indicator (KPI) is a type of performance measurement. KPIs evaluate the success of an organization or of a particular activity in which it engages.");
			dataModel.setLabels(GENERALIOTSMARTGSM_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-14");
			dataModel.setIdentification("GSMA-OffstreetParking");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-OffstreetParking.json"));
			dataModel.setDescription(
					"A site, off street, intended to park vehicles, managed independently and with suitable and clearly marked access points (entrances and exits).");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-15");
			dataModel.setIdentification("GSMA-Road");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Road.json"));
			dataModel.setDescription("Contains a harmonised geographic and contextual description of a road.");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-16");
			dataModel.setIdentification("GSMA-StreetLight");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-StreetLight.json"));
			dataModel.setDescription("GSMA Model that represents an urban streetlight");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-17");
			dataModel.setIdentification("GSMA-Vehicle");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Vehicle.json"));
			dataModel.setDescription("A harmonised description of a Vehicle");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-18");
			dataModel.setIdentification("GSMA-WasteContainer");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WasteContainer.json"));
			dataModel.setDescription("GSMA WasteContainer");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-19");
			dataModel.setIdentification("GSMA-WeatherObserved");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WeatherObserved.json"));
			dataModel.setDescription("An observation of weather conditions at a certain place and time.");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-20");
			dataModel.setIdentification("GSMA-WeatherStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WeatherStation.json"));
			dataModel.setDescription("GSMA Weather Station Model");
			dataModel.setLabels(GENERALIOTSMART_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-21");
			dataModel.setIdentification("Request");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Request.json"));
			dataModel.setDescription("Request for something.");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-22");
			dataModel.setIdentification("Response");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Response.json"));
			dataModel.setDescription("Response for a request.");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-23");
			dataModel.setIdentification("MobileElement");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_MobileElement.json"));
			dataModel.setDescription("Generic Mobile Element representation.");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-24");
			dataModel.setIdentification("Log");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Log.json"));
			dataModel.setDescription("Log representation.");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-25");
			dataModel.setIdentification("Issue");
			dataModel.setTypeEnum(DataModel.MainType.GENERAL);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Issue.json"));
			dataModel.setDescription("Issue representation.");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-26");
			dataModel.setIdentification("AuditPlatform");
			dataModel.setTypeEnum(DataModel.MainType.SYSTEM_ONTOLOGY);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_AuditPlatform.json"));
			dataModel.setDescription("System Ontology. Auditory of operations between user and Platform.");
			dataModel.setLabels(GENERALIOT_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}

		if (dataModelRepository.findById("MASTER-DataModel-27") == null) {
			final DataModel dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-27");
			dataModel.setIdentification("VideoResult");
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_VideoResult.json"));
			dataModel.setDescription("Ontology for Video Broker Processor results.");
			dataModel.setLabels("video,processing,iot,ocr,yolo,analytics");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}

		if (dataModelRepository.findById("MASTER-DataModel-28") == null) {
			final DataModel dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-28");
			dataModel.setIdentification("AssetType");
			dataModel.setTypeEnum(DataModel.MainType.SMART_CITIES);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_AssetType.json"));
			dataModel.setDescription("Ontology for Asset inventory");
			dataModel.setLabels("smartcities,iot");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}
		if (dataModelRepository.findById("MASTER-DataModel-29") == null) {
			final DataModel dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-29");
			dataModel.setIdentification("GSMA-BikeStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-BikeStation.json"));
			dataModel.setDescription("GSMA-BikeStation");
			dataModel.setLabels(GENERALIOTSMARTGSM_STR);
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}

		if (dataModelRepository.findById("MASTER-DataModel-30") == null) {
			final DataModel dataModel = new DataModel();
			dataModel.setId("MASTER-DataModel-30");
			dataModel.setIdentification(TIMESERIE_STR);
			dataModel.setTypeEnum(DataModel.MainType.IOT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_TimeSerie.json"));
			dataModel.setDescription("Ontology for TimeSerie");
			dataModel.setLabels("iot");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}

	}

	public void initGadget() {
		log.info("init Gadget");
		final List<Gadget> gadgets = gadgetRepository.findAll();
		if (gadgets.isEmpty()) {
			log.info("No gadgets ...");

			Gadget gadget = new Gadget();
			gadget.setId(MASTER_GADGET_1);
			gadget.setIdentification("My Gadget");
			gadget.setPublic(false);
			gadget.setDescription("gadget cousin score");
			gadget.setType("bar");
			gadget.setConfig(GADGET1CONFIG);
			gadget.setUser(getUserDeveloper());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_2);
			gadget.setIdentification("airportsByCountry");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(TABLE);
			gadget.setConfig(GADGET2CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_3);
			gadget.setIdentification("airportsByCountryTop10");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Airports\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_4);
			gadget.setIdentification(DESTINATIONMAP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("map");
			gadget.setConfig(
					"{\"center\":{\"lat\":44.08758502824516,\"lng\":18.6328125,\"zoom\":1},\"markersFilter\":\"count\",\"jsonMarkers\":\"[\\n{\\\"markerColor\\\": \\\"#0066ff\\\", \\\"iconColor\\\":\\\"black\\\" ,\\\"icon\\\":\\\"plane\\\",\\\"min\\\":2001},\\n{\\\"markerColor\\\": \\\"#4d94ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":2000,\\\"min\\\":501},\\n{\\\"markerColor\\\": \\\"#80b3ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":500,\\\"min\\\":51},\\n{\\\"markerColor\\\": \\\"#b3d1ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":50,\\\"min\\\":6},\\n{\\\"markerColor\\\": \\\"#e6f0ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":5}\\n]\"}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_5);
			gadget.setIdentification("destinationCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(GADGET5CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_6);
			gadget.setIdentification("originCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_7);
			gadget.setIdentification(ROUTESDESTTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(GADGET5CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_8);
			gadget.setIdentification(ROUTESORIGINTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(GADGET5CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void initGadgetOpenFlight() {

		Gadget gadget = null;

		if (gadgetRepository.findById(MASTER_GADGET_2) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_2);
			gadget.setIdentification("airportsByCountry");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(TABLE);
			gadget.setConfig(GADGET2CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById(MASTER_GADGET_3) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_3);
			gadget.setIdentification("airportsByCountryTop10");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Airports\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById(MASTER_GADGET_4) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_4);
			gadget.setIdentification(DESTINATIONMAP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("map");
			gadget.setConfig(
					"{\"center\":{\"lat\":44.08758502824516,\"lng\":18.6328125,\"zoom\":1},\"markersFilter\":\"count\",\"jsonMarkers\":\"[\\n{\\\"markerColor\\\": \\\"#0066ff\\\", \\\"iconColor\\\":\\\"black\\\" ,\\\"icon\\\":\\\"plane\\\",\\\"min\\\":2001},\\n{\\\"markerColor\\\": \\\"#4d94ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":2000,\\\"min\\\":501},\\n{\\\"markerColor\\\": \\\"#80b3ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":500,\\\"min\\\":51},\\n{\\\"markerColor\\\": \\\"#b3d1ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":50,\\\"min\\\":6},\\n{\\\"markerColor\\\": \\\"#e6f0ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":5}\\n]\"}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById(MASTER_GADGET_5) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_5);
			gadget.setIdentification("destinationCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById(MASTER_GADGET_6) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_6);
			gadget.setIdentification("originCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById(MASTER_GADGET_7) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_7);
			gadget.setIdentification(ROUTESDESTTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(GADGET1CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findById(MASTER_GADGET_8) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_8);
			gadget.setIdentification(ROUTESORIGINTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(GADGET1CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void initGadgetQAWindTurbines() {

		Gadget gadget = null;

		if (gadgetRepository.findByIdentification("producertbl") == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_9);
			gadget.setIdentification("producertbl");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(TABLE);
			gadget.setConfig(
					"{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"30\",\"trHeightBody\":\"30\",\"trHeightFooter\":\"30\",\"textColorTHead\":\"#555555\",\"textColorBody\":\"#555555\",\"textColorFooter\":\"#555555\"},\"options\":{\"rowSelection\":true,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findByIdentification(PRODUCERERRORCAT_STR) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_10);
			gadget.setIdentification(PRODUCERERRORCAT_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("mixed");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":true,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Error category\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findByIdentification(TRENDERROR_STR) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_11);
			gadget.setIdentification(TRENDERROR_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("line");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Date\"},\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findByIdentification(ERRORBYSITE_STR) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_12);
			gadget.setIdentification(ERRORBYSITE_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":true,\"ticks\":{\"suggestedMin\":\"\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":true,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Site\"},\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findByIdentification(PRODUCERERRORTYPE_STR) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_13);
			gadget.setIdentification(PRODUCERERRORTYPE_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("mixed");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":true,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Error code\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findByIdentification(TRENDERRORTYPE_STR) == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_14);
			gadget.setIdentification(TRENDERRORTYPE_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType("line");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of errors\",\"display\":true},\"stacked\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"Date\"},\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (gadgetRepository.findByIdentification("tableerrordetail") == null) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_15);
			gadget.setIdentification("tableerrordetail");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(TABLE);
			gadget.setConfig(
					"{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"30\",\"trHeightBody\":\"30\",\"trHeightFooter\":\"30\",\"textColorTHead\":\"#000000\",\"textColorBody\":\"#000000\",\"textColorFooter\":\"#000000\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void initGadgetDatasource() {

		log.info("init GadgetDatasource");
		final List<GadgetDatasource> gadgetDatasource = gadgetDatasourceRepository.findAll();
		if (gadgetDatasource.isEmpty()) {
			log.info("No gadget querys ...");

			final GadgetDatasource gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_1);
			gadgetDatasources.setIdentification("DsRawRestaurants");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery("select * from Restaurants");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(REST_STR));
			gadgetDatasources.setMaxvalues(150);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);

		}

	}

	public void initGadgetDatasourceOpenFlight() {
		GadgetDatasource gadgetDatasources = new GadgetDatasource();

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_2) == null) {
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_2);
			gadgetDatasources.setIdentification(ROUTESORIGINTOP_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select r.routes.src as src,count(r) as count from routes as r group by r.routes.src order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTES_STR));
			gadgetDatasources.setMaxvalues(20);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_3) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_3);
			gadgetDatasources.setIdentification(ROUTESDESTTOP_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select r.routes.dest as dest,count(r) as count from routes as r group by r.routes.dest order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTES_STR));
			gadgetDatasources.setMaxvalues(20);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_4) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_4);
			gadgetDatasources.setIdentification("countriesAsDestination");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select re.routesexten.countrysrc,re.routesexten.countrydest,count(re) as count from routesexten As re group by re.routesexten.countrysrc,re.routesexten.countrydest order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTESEXT_STR));
			gadgetDatasources.setMaxvalues(10);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_5) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_5);
			gadgetDatasources.setIdentification(DESTINATIONMAP_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select re.countrysrc,re.countrydest,re.count, iso.ISO3166.latitude , iso.ISO3166.longitude from ( select rx.routesexten.countrysrc As countrysrc, rx.routesexten.countrydest As countrydest, count(re.routesexten.countrysrc) As count from routesexten as rx group by rx.routesexten.countrysrc, rx.routesexten.countrydest order by count desc) As re inner join ISO3166_1 As iso on re.countrydest = iso.ISO3166.name order by re.count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTESEXT_STR));
			gadgetDatasources.setMaxvalues(500);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_6) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_6);
			gadgetDatasources.setIdentification("airportsCountByCountryTop10");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select airp.airportsdata.country, count(airp.airportsdata.country) AS count from airportsdata AS airp group by airp.airportsdata.country order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(AIRPORT_STR));
			gadgetDatasources.setMaxvalues(10);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_7) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_7);
			gadgetDatasources.setIdentification("airportsCountByCountry");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select airp.airportsdata.country as acountry, count(*) AS count from airportsdata AS airp group by airp.airportsdata.country");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(AIRPORT_STR));
			gadgetDatasources.setMaxvalues(300);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_8) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_8);
			gadgetDatasources.setIdentification("distinctCountries");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select distinct routesexten.routesexten.countrysrc as country from routesexten order by country");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTESEXT_STR));
			gadgetDatasources.setMaxvalues(500);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}
	}

	public void initGadgetDatasourceQAWindTurbines() {
		GadgetDatasource gadgetDatasources = new GadgetDatasource();
		final String configData = "[['mainField_cat1','secondaryField_cat1'],['mainField_cat2,'secondaryField_cat2','tertiaryField_cat2']]";

		if (gadgetDatasourceRepository.findByIdentification("QA_overview") == null) {
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_9);
			gadgetDatasources.setIdentification("QA_overview");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery("select * from QA_OVERVIEW");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(QA_STR));
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findByIdentification(PRODUCERERRORCAT_STR) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_10);
			gadgetDatasources.setIdentification(PRODUCERERRORCAT_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select nameCat1,nameCat2,nameCat3, process_date,idAdaptador, totalLoaded, structural, integrity, business,\" Ok\" as refCat from Producer_ErrorCat");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(PRODUCERERROR_STR));
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findByIdentification(TRENDERROR_STR) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_11);
			gadgetDatasources.setIdentification(TRENDERROR_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery("select * from errorsOnDate");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ERRORSONDATE_STR));
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findByIdentification(ERRORBYSITE_STR) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_12);
			gadgetDatasources.setIdentification(ERRORBYSITE_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select qa.process_date, qa.idAdaptador, qa.errors, (qa.errors*0.15 - qa.errors*0.15%1) as meteor, (qa.errors*0.16 - qa.errors*0.16%1) as forecast, site.name from ((select process_date,idAdaptador,siteCode,count(*) as errors from QA_DETAIL group by process_date,idAdaptador,siteCode) as qa inner join SITES as site on site.siteCode = qa.siteCode)");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(QADETAIL_STR));
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findByIdentification(PRODUCERERRORTYPE_STR) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_13);
			gadgetDatasources.setIdentification(PRODUCERERRORTYPE_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select '100' as nameType0, '101' as nameType1, '102' as nameType2,  '103' as nameType3, '104' as nameType4, '105' as nameType5,  '106' as nameType6, '107' as nameType7, '108' as nameType8,  '109' as nameType9, '110' as nameType10,  process_date,idAdaptador,sum(CASE errorCode WHEN 100 THEN 1 ELSE 0 END) as e100,sum(CASE errorCode WHEN 101 THEN 1 ELSE 0 END) as e101,sum(CASE errorCode WHEN 102 THEN 1 ELSE 0 END) as e102,sum(CASE errorCode WHEN 103 THEN 1 ELSE 0 END) as e103,sum(CASE errorCode WHEN 104 THEN 1 ELSE 0 END) as e104,sum(CASE errorCode WHEN 105 THEN 1 ELSE 0 END) as e105,sum(CASE errorCode WHEN 106 THEN 1 ELSE 0 END) as e106,sum(CASE errorCode WHEN 107 THEN 1 ELSE 0 END) as e107,sum(CASE errorCode WHEN 108 THEN 1 ELSE 0 END) as e108,sum(CASE errorCode WHEN 109 THEN 1 ELSE 0 END) as e109,sum(CASE errorCode WHEN 110 THEN 1 ELSE 0 END) as e110 from QA_DETAIL group by process_date,idAdaptador");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(QADETAIL_STR));
			gadgetDatasources.setMaxvalues(1000);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findByIdentification(TRENDERRORTYPE_STR) == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_14);
			gadgetDatasources.setIdentification(TRENDERRORTYPE_STR);
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery("select * from errorsTypeOnDate");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ERRORONDATE_STR));
			gadgetDatasources.setMaxvalues(100);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (gadgetDatasourceRepository.findByIdentification("listerroraxpo") == null) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_15);
			gadgetDatasources.setIdentification("listerroraxpo");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery("select * from QA_DETAIL_EXTENDED");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(QADETAIL_EXT_STR));
			gadgetDatasources.setMaxvalues(2000);
			gadgetDatasources.setConfig(configData);
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}
	}

	public void initGadgetMeasure() {
		log.info("init GadgetMeasure");
		final List<GadgetMeasure> gadgetMeasures = gadgetMeasureRepository.findAll();
		if (gadgetMeasures.isEmpty()) {
			log.info("No gadget measures ...");

			final GadgetMeasure gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-1");
			gadgetMeasure.setDatasource(getGadgetDatasourceDeveloper());
			gadgetMeasure.setConfig(
					"{\"fields\":[\"Restaurant.cuisine\",\"Restaurant.grades[0].score\"],\"name\":\"score\",\"config\":{\"backgroundColor\":\"#000000\",\"borderColor\":\"#000000\",\"pointBackgroundColor\":\"#000000\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(getGadget());
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void initGadgetMeasureOpenFlight() {
		GadgetMeasure gadgetMeasure;

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-2").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-2");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_7));
			gadgetMeasure.setConfig("{\"fields\":[\"count\"],\"name\":\"Country\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_2));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-3").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-3");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_7));
			gadgetMeasure.setConfig("{\"fields\":[\"acountry\"],\"name\":\"Number of Airports\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_2));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-4").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-4");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_6));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"country\",\"count\"],\"name\":\"Top 10 Countries By Airports\",\"config\":{\"backgroundColor\":\"#2d60b5\",\"borderColor\":\"#2d60b5\",\"pointBackgroundColor\":\"#2d60b5\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_3));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-5").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-5");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_5));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"latitude\",\"longitude\",\"countrydest\",\"countrydest\",\"count\"],\"name\":\"\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_4));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-6").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-6");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_4));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"countrydest\",\"count\"],\"name\":\"Top Country Destinations\",\"config\":{\"backgroundColor\":\"#e8cb6a\",\"borderColor\":\"#e8cb6a\",\"pointBackgroundColor\":\"#e8cb6a\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_5));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-7").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-7");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_4));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"countrysrc\",\"count\"],\"name\":\"Top Country Origins\",\"config\":{\"backgroundColor\":\"#879dda\",\"borderColor\":\"#879dda\",\"pointBackgroundColor\":\"#879dda\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_6));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-8").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-8");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_3));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"dest\",\"count\"],\"name\":\"Top Destination Airports\",\"config\":{\"backgroundColor\":\"#4e851b\",\"borderColor\":\"#4e851b\",\"pointBackgroundColor\":\"#4e851b\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_7));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-9").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-9");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_2));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"src\",\"count\"],\"name\":\"Top Origin Airports\",\"config\":{\"backgroundColor\":\"#b02828\",\"borderColor\":\"#b02828\",\"pointBackgroundColor\":\"#b02828\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_8));
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void initGadgetMeasureQAWindTurbines() {
		GadgetMeasure gadgetMeasure;

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-10").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-10");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9));
			gadgetMeasure
					.setConfig("{\"fields\":[\"idAdaptador\"],\"name\":\"Producer\",\"config\":{\"position\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-11").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-11");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"dataLost\"],\"name\":\"Missed Data (%)\",\"config\":{\"position\":\"2\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-12").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-12");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9));
			gadgetMeasure
					.setConfig("{\"fields\":[\"bad\"],\"name\":\"Wrong Records\",\"config\":{\"position\":\"4\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-13").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-13");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9));
			gadgetMeasure
					.setConfig("{\"fields\":[\"good\"],\"name\":\"Right Records\",\"config\":{\"position\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-14").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-14");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"totalLoaded\"],\"name\":\"Data Loaded\",\"config\":{\"position\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-15").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-15");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat1\",\"structural\"],\"name\":\"Structural errors\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-16").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-16");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat2\",\"integrity\"],\"name\":\"Integrity errors\",\"config\":{\"backgroundColor\":\"rgba(0,168,57,0.44)\",\"borderColor\":\"rgba(0,168,57,0.44)\",\"pointBackgroundColor\":\"rgba(0,168,57,0.44)\",\"pointHoverBackgroundColor\":\"rgba(0,168,57,0.44)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-17").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-17");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat3\",\"business\"],\"name\":\"Business errors\",\"config\":{\"backgroundColor\":\"rgba(201,58,58,0.88)\",\"borderColor\":\"rgba(201,58,58,0.88)\",\"pointBackgroundColor\":\"rgba(201,58,58,0.88)\",\"pointHoverBackgroundColor\":\"rgba(201,58,58,0.88)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-18").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-18");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"refCat\",\"totalLoaded\"],\"name\":\" Ok \",\"config\":{\"backgroundColor\":\"#e39d34\",\"borderColor\":\"#e39d34\",\"pointBackgroundColor\":\"#e39d34\",\"pointHoverBackgroundColor\":\"#e39d34\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-19").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-19");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"structural\"],\"name\":\"Structural errors\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-20").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-20");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"totalLoaded\"],\"name\":\"Ok\",\"config\":{\"backgroundColor\":\"#e39d34\",\"borderColor\":\"#e39d34\",\"pointBackgroundColor\":\"#e39d34\",\"pointHoverBackgroundColor\":\"#e39d34\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-21").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-21");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"integrity\"],\"name\":\"Integrity errors\",\"config\":{\"backgroundColor\":\"rgba(0,168,57,0.44)\",\"borderColor\":\"rgba(0,168,57,0.44)\",\"pointBackgroundColor\":\"rgba(0,168,57,0.44)\",\"pointHoverBackgroundColor\":\"rgba(0,168,57,0.44)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-22").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-22");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"business\"],\"name\":\"Business errors\",\"config\":{\"backgroundColor\":\"rgba(201,58,58,0.88)\",\"borderColor\":\"rgba(201,58,58,0.88)\",\"pointBackgroundColor\":\"rgba(201,58,58,0.88)\",\"pointHoverBackgroundColor\":\"rgba(201,58,58,0.88)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-23").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-23");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_12));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"forecast\"],\"name\":\"Production Forecast\",\"config\":{\"backgroundColor\":\"rgba(223,94,255,0.62)\",\"borderColor\":\"rgba(223,94,255,0.62)\",\"pointBackgroundColor\":\"rgba(223,94,255,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_12));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-24").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-24");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_12));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"errors\"],\"name\":\"WTG\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_12));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-25").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-25");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_12));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"meteor\"],\"name\":\"Meter\",\"config\":{\"backgroundColor\":\"rgba(17,245,149,0.62)\",\"borderColor\":\"rgba(17,245,149,0.62)\",\"pointBackgroundColor\":\"rgba(17,245,149,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_12));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-26").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-26");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType2\",\"e102\"],\"name\":\"102 The raw has no enough fields\",\"config\":{\"backgroundColor\":\"rgba(114,181,62,0.62)\",\"borderColor\":\"rgba(114,181,62,0.62)\",\"pointBackgroundColor\":\"rgba(114,181,62,0.62)\",\"pointHoverBackgroundColor\":\"rgba(114,181,62,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-27").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-27");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType5\",\"e105\"],\"name\":\"105 Invalid numeric format \",\"config\":{\"backgroundColor\":\"#eda437\",\"borderColor\":\"#eda437\",\"pointBackgroundColor\":\"#eda437\",\"pointHoverBackgroundColor\":\"#eda437\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-28").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-28");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType9\",\"e109\"],\"name\":\"109 Duplicated record\",\"config\":{\"backgroundColor\":\"rgba(84,0,168,0.26)\",\"borderColor\":\"rgba(84,0,168,0.26)\",\"pointBackgroundColor\":\"rgba(84,0,168,0.26)\",\"pointHoverBackgroundColor\":\"rgba(84,0,168,0.26)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-29").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-29");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType4\",\"e104\"],\"name\":\"104 Invalid date format\",\"config\":{\"backgroundColor\":\"rgba(41,196,230,0.67)\",\"borderColor\":\"rgba(41,196,230,0.67)\",\"pointBackgroundColor\":\"rgba(41,196,230,0.67)\",\"pointHoverBackgroundColor\":\"rgba(41,196,230,0.67)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-30").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-30");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType3\",\"e103\"],\"name\":\"103 Mandatory fields \",\"config\":{\"backgroundColor\":\"rgba(24,0,168,0.62)\",\"borderColor\":\"rgba(24,0,168,0.62)\",\"pointBackgroundColor\":\"rgba(24,0,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(24,0,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-31").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-31");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType6\",\"e106\"],\"name\":\"106 Integrity error\",\"config\":{\"backgroundColor\":\"rgba(70,131,224,0.57)\",\"borderColor\":\"rgba(70,131,224,0.57)\",\"pointBackgroundColor\":\"rgba(70,131,224,0.57)\",\"pointHoverBackgroundColor\":\"rgba(70,131,224,0.57)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-32").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-32");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType0\",\"e100\"],\"name\":\"100 Frozen data\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-33").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-33");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType10\",\"e110\"],\"name\":\"110 Decimal precision \",\"config\":{\"backgroundColor\":\"rgba(0,148,168,0.62)\",\"borderColor\":\"rgba(0,148,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,148,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,148,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-34").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-34");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType8\",\"e108\"],\"name\":\"108 Out of bounds sup \",\"config\":{\"backgroundColor\":\"rgba(0,168,67,0.21)\",\"borderColor\":\"rgba(0,168,67,0.21)\",\"pointBackgroundColor\":\"rgba(0,168,67,0.21)\",\"pointHoverBackgroundColor\":\"rgba(0,168,67,0.21)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-35").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-35");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType7\",\"e107\"],\"name\":\"107 Out of bounds inf \",\"config\":{\"backgroundColor\":\"rgba(168,30,0,0.49)\",\"borderColor\":\"rgba(168,30,0,0.49)\",\"pointBackgroundColor\":\"rgba(168,30,0,0.49)\",\"pointHoverBackgroundColor\":\"rgba(168,30,0,0.49)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-36").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-36");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType1\",\"e101\"],\"name\":\"101 Max null values per hour\",\"config\":{\"backgroundColor\":\"rgba(122,89,5,0.98)\",\"borderColor\":\"rgba(122,89,5,0.98)\",\"pointBackgroundColor\":\"rgba(122,89,5,0.98)\",\"pointHoverBackgroundColor\":\"rgba(122,89,5,0.98)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-37").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-37");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"duplicated\"],\"name\":\"109 Duplicated record\",\"config\":{\"backgroundColor\":\"rgba(84,0,168,0.26)\",\"borderColor\":\"rgba(84,0,168,0.26)\",\"pointBackgroundColor\":\"rgba(84,0,168,0.26)\",\"pointHoverBackgroundColor\":\"rgba(84,0,168,0.26)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-38").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-38");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"decimalPrecision\"],\"name\":\"110 Decimal precision\",\"config\":{\"backgroundColor\":\"rgba(0,148,168,0.62)\",\"borderColor\":\"rgba(0,148,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,148,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,148,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-39").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-39");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"numericFormat\"],\"name\":\"105 Invalid numeric format\",\"config\":{\"backgroundColor\":\"#eda437\",\"borderColor\":\"#eda437\",\"pointBackgroundColor\":\"#eda437\",\"pointHoverBackgroundColor\":\"#eda437\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-40").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-40");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"nullValues\"],\"name\":\"101 Null values\",\"config\":{\"backgroundColor\":\"rgba(122,89,5,0.98)\",\"borderColor\":\"rgba(122,89,5,0.98)\",\"pointBackgroundColor\":\"rgba(122,89,5,0.98)\",\"pointHoverBackgroundColor\":\"rgba(122,89,5,0.98)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-41").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-41");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"dateFormat\"],\"name\":\"104 Invalid date format\",\"config\":{\"backgroundColor\":\"rgba(41,196,230,0.67)\",\"borderColor\":\"rgba(41,196,230,0.67)\",\"pointBackgroundColor\":\"rgba(41,196,230,0.67)\",\"pointHoverBackgroundColor\":\"rgba(41,196,230,0.67)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-42").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-42");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"integrity\"],\"name\":\"106 Integrity error\",\"config\":{\"backgroundColor\":\"rgba(70,131,224,0.57)\",\"borderColor\":\"rgba(70,131,224,0.57)\",\"pointBackgroundColor\":\"rgba(70,131,224,0.57)\",\"pointHoverBackgroundColor\":\"rgba(70,131,224,0.57)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-43").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-43");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"mandatoryFields\"],\"name\":\"103 Mandatory fields\",\"config\":{\"backgroundColor\":\"rgba(24,0,168,0.62)\",\"borderColor\":\"rgba(24,0,168,0.62)\",\"pointBackgroundColor\":\"rgba(24,0,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(24,0,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-44").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-44");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"raw\"],\"name\":\"102 The raw has no enough fields\",\"config\":{\"backgroundColor\":\"rgba(114,181,62,0.62)\",\"borderColor\":\"rgba(114,181,62,0.62)\",\"pointBackgroundColor\":\"rgba(114,181,62,0.62)\",\"pointHoverBackgroundColor\":\"rgba(114,181,62,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-45").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-45");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"boundsInf\"],\"name\":\"107 Out of bounds inf\",\"config\":{\"backgroundColor\":\"rgba(168,30,0,0.49)\",\"borderColor\":\"rgba(168,30,0,0.49)\",\"pointBackgroundColor\":\"rgba(168,30,0,0.49)\",\"pointHoverBackgroundColor\":\"rgba(168,30,0,0.49)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-46").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-46");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"frozenData\"],\"name\":\"100 Frozen data\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.72)\",\"borderColor\":\"rgba(0,108,168,0.72)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.72)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.72)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-47").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-47");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"boundsSup\"],\"name\":\"108 Out of bounds sup\",\"config\":{\"backgroundColor\":\"rgba(0,168,67,0.21)\",\"borderColor\":\"rgba(0,168,67,0.21)\",\"pointBackgroundColor\":\"rgba(0,168,67,0.21)\",\"pointHoverBackgroundColor\":\"rgba(0,168,67,0.21)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-48").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-48");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"errorCategory\"],\"name\":\"Error Category\",\"config\":{\"position\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-49").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-49");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"raw\"],\"name\":\"Original raw content\",\"config\":{\"position\":\"5\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-50").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-50");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15));
			gadgetMeasure
					.setConfig("{\"fields\":[\"assetName\"],\"name\":\"WTG Name\",\"config\":{\"position\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-51").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-51");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15));
			gadgetMeasure
					.setConfig("{\"fields\":[\"siteName\"],\"name\":\"Site Name\",\"config\":{\"position\":\"2\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-52").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-52");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"errorDescription\"],\"name\":\"Error Description\",\"config\":{\"position\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (gadgetMeasureRepository.findById("MASTER-GadgetMeasure-53").isEmpty()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-53");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15));
			gadgetMeasure
					.setConfig("{\"fields\":[\"timestamp\"],\"name\":\"Timestamp\",\"config\":{\"position\":\"4\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15));
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void initOntologyCategory() {

		log.info("init OntologyCategory");
		final List<OntologyCategory> categories = ontologyCategoryRepository.findAll();
		if (categories.isEmpty()) {
			log.info("No ontology categories found..adding");
			final OntologyCategory category = new OntologyCategory();
			category.setId("MASTER-Ontology-Categorty-1");
			category.setIdentification("ontologias_categoria_cultura");
			category.setDescription("ontologias_categoria_cultura_desc");
			ontologyCategoryRepository.save(category);
		}

	}

	public void initOntology() {

		final String ONTOLOGY_MASTER = "OntologyMaster";
		final String ONTOLOGY_AIRCOMETER = "AirCOMeter";
		final String ONTOLOGY_AIRQUALITY = "AirQuality";
		final String ONTOLOGY_CONTPERF = "ContPerf";
		final String ONTOLOGY_TWEETSENTIMENT = "TweetSentiment";
		final String ONTOLOGY_GEOAIRQUALITY = "GeoAirQuality";
		final String ONTOLOGY_CITYPOPULATION = "CityPopulation";

		final String ONTOLOGY_AIRQUALITYGR2 = "AirQuality_gr2";
		final String ONTOLOGY_TWINPROPERTIESTURBINE = "TwinPropertiesTurbine";
		final String ONTOLOGY_TWINPROPERTIESSENSEHAT = "TwinPropertiesSensehat";
		final String ONTOLOGY_NATIVENOTIFKEYS = "NativeNotifKeys";
		final String ONTOLOGY_NOTIFICATIONMESSAGE = "notificationMessage";

		log.info("init Ontology");
		List<DataModel> dataModels;

		log.info("No ontologies..adding");
		Ontology ontology = new Ontology();

		if (ontologyRepository.findByIdentification(ONTOLOGY_MASTER) == null) {
			ontology.setId("MASTER-Ontology-1");
			ontology.setDataModel(dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0));
			ontology.setJsonSchema(
					dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0).getJsonSchema());
			ontology.setIdentification(ONTOLOGY_MASTER);
			ontology.setDescription("Ontology created as Master Data");
			ontology.setMetainf(ONTOLOGY_MASTER);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification(TICKET) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-2");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Ticket.json"));
			ontology.setDescription("Ontology created for Ticketing");
			ontology.setIdentification(TICKET);
			ontology.setMetainf(TICKET);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setDataModel(dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0));
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_CONTPERF) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-3");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ContPerf.json"));
			ontology.setDescription("Ontology created for performance testing");
			ontology.setIdentification(ONTOLOGY_CONTPERF);
			ontology.setMetainf(ONTOLOGY_CONTPERF);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setDataModel(dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0));
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_HELSINKIPOPULATION) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-4");
			ontology.setJsonSchema(loadFromResources("examples/HelsinkiPopulation-schema.json"));
			ontology.setDescription("Ontology HelsinkiPopulation for testing");
			ontology.setIdentification(ONTOLOGY_HELSINKIPOPULATION);
			ontology.setMetainf(ONTOLOGY_HELSINKIPOPULATION);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_TWEETSENTIMENT) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-5");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_TweetSentiment.json"));
			ontology.setDescription(ONTOLOGY_TWEETSENTIMENT);
			ontology.setIdentification(ONTOLOGY_TWEETSENTIMENT);
			ontology.setMetainf(ONTOLOGY_TWEETSENTIMENT);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_GEOAIRQUALITY) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-6");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_GeoAirQuality.json"));
			ontology.setDescription("Air quality retrieved from https://api.waqi.info/search");
			ontology.setIdentification(ONTOLOGY_GEOAIRQUALITY);
			ontology.setMetainf(ONTOLOGY_GEOAIRQUALITY);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_CITYPOPULATION) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-7");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_CityPopulation.json"));
			ontology.setDescription(
					"Population of Urban Agglomerations with 300,000 Inhabitants or More in 2014, by Country, 1950-2030 (thousands)");
			ontology.setIdentification(ONTOLOGY_CITYPOPULATION);
			ontology.setMetainf(ONTOLOGY_CITYPOPULATION);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_AIRQUALITYGR2) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-8");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirQuality_gr2.json"));
			ontology.setDescription(ONTOLOGY_AIRQUALITYGR2);
			ontology.setIdentification(ONTOLOGY_AIRQUALITYGR2);
			ontology.setMetainf(ONTOLOGY_AIRQUALITYGR2);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_AIRQUALITY) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-9");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirQuality.json"));
			ontology.setDescription(ONTOLOGY_AIRQUALITY);
			ontology.setIdentification(ONTOLOGY_AIRQUALITY);
			ontology.setMetainf(ONTOLOGY_AIRQUALITY);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_AIRCOMETER) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-10");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirCOMeter.json"));
			ontology.setDescription(ONTOLOGY_AIRCOMETER);
			ontology.setIdentification(ONTOLOGY_AIRCOMETER);
			ontology.setMetainf(ONTOLOGY_AIRCOMETER);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ONTOLOGY_TWINPROPERTIESTURBINE) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-11");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Turbine.json"));
			ontology.setDescription("Digital Twin Shadow");
			ontology.setIdentification(ONTOLOGY_TWINPROPERTIESTURBINE);
			ontology.setMetainf(ONTOLOGY_TWINPROPERTIESTURBINE);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(RtdbDatasource.DIGITAL_TWIN);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ONTOLOGY_TWINPROPERTIESSENSEHAT) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-16");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_SenseHat.json"));
			ontology.setDescription("Digital Twin Shadow");
			ontology.setIdentification(ONTOLOGY_TWINPROPERTIESSENSEHAT);
			ontology.setMetainf(ONTOLOGY_TWINPROPERTIESSENSEHAT);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(RtdbDatasource.DIGITAL_TWIN);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_NATIVENOTIFKEYS) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-25");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_NativeNotifKeys.json"));
			ontology.setDescription("Contains user tokens from end-devices connected to the notification system");
			ontology.setIdentification(ONTOLOGY_NATIVENOTIFKEYS);
			ontology.setMetainf("notifications");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_NOTIFICATIONMESSAGE) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-26");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_notificationMessage.json"));
			ontology.setDescription("Ontology to store outbound notification messages");
			ontology.setIdentification(ONTOLOGY_NOTIFICATIONMESSAGE);
			ontology.setMetainf("notifications");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(SUPERMARKETS) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-27");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_supermarkets.json"));
			ontology.setDescription("Ontology to store georeferenced data about supermarkets in Las Palmas");
			ontology.setIdentification(SUPERMARKETS);
			ontology.setMetainf("gis");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("MetricsApi") == null) {

			// Crea la ontologia
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-30");
			ontology.setJsonSchema(loadFromResources("metrics/OntologySchema_metricsApi.json"));
			ontology.setDescription("Ontology to store Metrics for API Manager");
			ontology.setIdentification("MetricsApi");
			ontology.setMetainf(METRICS_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			OntologyTimeSeriesProperty resultProperty = this.createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			OntologyTimeSeriesProperty userProperty = this.createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			OntologyTimeSeriesProperty apiProperty = this.createTimeSeriesProperty(oTS, "api", PropertyDataType.STRING,
					PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			OntologyTimeSeriesProperty operationTypeProperty = this.createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(operationTypeProperty);

			OntologyTimeSeriesProperty valueProperty = this.createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			OntologyTimeSeriesWindow hourlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			OntologyTimeSeriesWindow dailyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			OntologyTimeSeriesWindow monthlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.DAYS, WindowType.MONTHS);
			ontologyTimeSeriesWindowRepository.save(monthlyWindow);

		}

		if (ontologyRepository.findByIdentification("MetricsControlPanel") == null) {

			// Crea la ontologia
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-31");
			ontology.setJsonSchema(loadFromResources("metrics/OntologySchema_metricsControlPanel.json"));
			ontology.setDescription("Ontology to store Metrics for ControlPanel");
			ontology.setIdentification("MetricsControlPanel");
			ontology.setMetainf(METRICS_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			OntologyTimeSeriesProperty resultProperty = this.createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			OntologyTimeSeriesProperty userProperty = this.createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			OntologyTimeSeriesProperty apiProperty = this.createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			OntologyTimeSeriesProperty valueProperty = this.createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			OntologyTimeSeriesWindow hourlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			OntologyTimeSeriesWindow dailyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			OntologyTimeSeriesWindow monthlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.DAYS, WindowType.MONTHS);
			ontologyTimeSeriesWindowRepository.save(monthlyWindow);

		}

		if (ontologyRepository.findByIdentification("MetricsOntology") == null) {

			// Crea la ontologia
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-32");
			ontology.setJsonSchema(loadFromResources("metrics/OntologySchema_metricsOntology.json"));
			ontology.setDescription("Ontology to store Metrics for stats of ontologies usage");
			ontology.setIdentification("MetricsOntology");
			ontology.setMetainf(METRICS_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			OntologyTimeSeriesProperty resultProperty = this.createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			OntologyTimeSeriesProperty userProperty = this.createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			OntologyTimeSeriesProperty apiProperty = this.createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			OntologyTimeSeriesProperty apiSource = this.createTimeSeriesProperty(oTS, "source", PropertyDataType.STRING,
					PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiSource);

			OntologyTimeSeriesProperty apiOntology = this.createTimeSeriesProperty(oTS, "ontology",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiOntology);

			OntologyTimeSeriesProperty valueProperty = this.createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			OntologyTimeSeriesWindow hourlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			OntologyTimeSeriesWindow dailyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			OntologyTimeSeriesWindow monthlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.DAYS, WindowType.MONTHS);
			ontologyTimeSeriesWindowRepository.save(monthlyWindow);

		}

		if (ontologyRepository.findByIdentification("MetricsOperation") == null) {

			// Crea la ontologia
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-33");
			ontology.setJsonSchema(loadFromResources("metrics/OntologySchema_metricsOperation.json"));
			ontology.setDescription("Ontology to store Metrics for stats of operations in platform brokers");
			ontology.setIdentification("MetricsOperation");
			ontology.setMetainf(METRICS_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			OntologyTimeSeriesProperty resultProperty = this.createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			OntologyTimeSeriesProperty userProperty = this.createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			OntologyTimeSeriesProperty apiProperty = this.createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			OntologyTimeSeriesProperty apiSource = this.createTimeSeriesProperty(oTS, "source", PropertyDataType.STRING,
					PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiSource);

			OntologyTimeSeriesProperty valueProperty = this.createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			OntologyTimeSeriesWindow hourlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			OntologyTimeSeriesWindow dailyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			OntologyTimeSeriesWindow monthlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.DAYS, WindowType.MONTHS);
			ontologyTimeSeriesWindowRepository.save(monthlyWindow);

		}

		if (ontologyRepository.findByIdentification("MetricsQueriesControlPanel") == null) {

			// Crea la ontologia
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-34");
			ontology.setJsonSchema(loadFromResources("metrics/OntologySchema_metricsQueriesControlPanel.json"));
			ontology.setDescription("Ontology to store Metrics for stats of queries in query tool of Control Panel");
			ontology.setIdentification("MetricsQueriesControlPanel");
			ontology.setMetainf(METRICS_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			OntologyTimeSeriesProperty resultProperty = this.createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			OntologyTimeSeriesProperty userProperty = this.createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			OntologyTimeSeriesProperty ontologyProperty = this.createTimeSeriesProperty(oTS, "ontology",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(ontologyProperty);

			OntologyTimeSeriesProperty valueProperty = this.createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			OntologyTimeSeriesWindow hourlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			OntologyTimeSeriesWindow dailyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			OntologyTimeSeriesWindow monthlyWindow = this.createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.DAYS, WindowType.MONTHS);
			ontologyTimeSeriesWindowRepository.save(monthlyWindow);

		}

	}

	private OntologyTimeSeriesProperty createTimeSeriesProperty(OntologyTimeSeries oTS, String propertyName,
			PropertyDataType dataType, PropertyType type) {

		OntologyTimeSeriesProperty resultProperty = new OntologyTimeSeriesProperty();
		resultProperty.setOntologyTimeSeries(oTS);
		resultProperty.setPropertyDataType(dataType);
		resultProperty.setPropertyName(propertyName);
		resultProperty.setPropertyType(type);

		return resultProperty;
	}

	private OntologyTimeSeriesWindow createTimeSeriesWindow(OntologyTimeSeries oTS,
			AggregationFunction aggregationFuncion, FrecuencyUnit frecuencyUnit, WindowType windowType) {
		OntologyTimeSeriesWindow hourlyWindow = new OntologyTimeSeriesWindow();
		hourlyWindow.setOntologyTimeSeries(oTS);
		hourlyWindow.setAggregationFunction(aggregationFuncion);
		hourlyWindow.setBdh(false);
		hourlyWindow.setFrecuency(1);
		hourlyWindow.setFrecuencyUnit(frecuencyUnit);
		hourlyWindow.setWindowType(windowType);

		return hourlyWindow;
	}

	private void initLayers() {
		Layer layer;

		if (layerRepository.findByIdentification(SUPERMARKETS).isEmpty()) {

			layer = new Layer();
			layer.setId("MASTER-Layer-01");
			layer.setIdentification(SUPERMARKETS);
			layer.setDescription("supermarkets in Las Palmas");
			layer.setPublic(true);
			layer.setGeometryField("geometry");
			layer.setGeometryType("Point");
			layer.setHeatMap(false);
			layer.setQuery("select c from Supermarkets as c where c.Supermarkets.company={$company}");
			layer.setRefreshTime(30);
			layer.setQueryParams("[{\"param\":\"company\",\"type\":\"STRING\",\"default\":\"mercadona\"}]");
			layer.setOntology(ontologyRepository.findByIdentification(SUPERMARKETS));
			layer.setOuterColor("#000000");
			layer.setInnerColor("#1410f5");
			layer.setOuterThin("1");
			layer.setSize("20");
			layer.setFilter(true);
			layer.setFilters(
					"[{\"operation\":\"status===open\",\"color\":\"#14e842\"},{\"operation\":\"status===close\",\"color\":\"#eb0d0d\"}]");
			layer.setUser(getUserDeveloper());
			layerRepository.save(layer);
		}
		if (layerRepository.findByIdentification(INUNDACIONES).isEmpty()) {

			layer = new Layer();
			layer.setId("MASTER-Layer-02");
			layer.setIdentification(INUNDACIONES);
			layer.setDescription("Riesgo de inundaciones, retorno a 500 años");
			layer.setPublic(true);
			layer.setUrl("https://servicios.idee.es/wms-inspire/riesgos-naturales/inundaciones");
			layer.setExternalType("wms");
			layer.setLayerTypeWms("NZ.Flood.MarinaT500");
			layer.setUser(getUserDeveloper());
			layerRepository.save(layer);
		}

	}

	private void initViewers() {
		final Viewer viewer = new Viewer();

		if (viewerRepository.findByIdentification("MASTER-Viewer-01").isEmpty()) {
			viewer.setId("MASTER-Viewer-01");
			viewer.setIdentification("LasPalmasViewer");
			viewer.setDescription("Viewer tutorial example of Las Palmas");
			viewer.setPublic(true);
			viewer.setBaseLayer(baseLayerRepository.findByIdentification("osm.Mapnik.Labels").get(0));

			final Layer layer01 = layerRepository.findByIdentification(SUPERMARKETS).get(0);
			final Layer layer02 = layerRepository.findByIdentification(INUNDACIONES).get(0);

			viewer.getLayers().add(layer01);
			viewer.getLayers().add(layer02);

			viewer.setUser(getUserDeveloper());
			viewer.setJs(loadFromResources("examples/viewer.html"));
			viewer.setLatitude("28.134");
			viewer.setLongitude("-15.434");
			viewer.setHeight("4500");

			layer01.getViewers().add(viewer);
			layer02.getViewers().add(viewer);

			viewerRepository.save(viewer);

		}
	}

	public void initOntologyOpenFlight() {
		Ontology ontology;
		List<DataModel> dataModels;

		if (ontologyRepository.findByIdentification(ROUTES_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-12");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_routes.json"));
			ontology.setDescription(DESCNOTEBOOKEXMP_STR);
			ontology.setIdentification(ROUTES_STR);
			ontology.setMetainf(ROUTES_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ROUTESEXT_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-13");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_routesexten.json"));
			ontology.setDescription(DESCNOTEBOOKEXMP_STR);
			ontology.setIdentification(ROUTESEXT_STR);
			ontology.setMetainf(ROUTESEXT_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ISO3166_1) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-14");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ISO3166_1.json"));
			ontology.setDescription("Ontology defining the standard alpha codes and number for all the countries");
			ontology.setIdentification(ISO3166_1);
			ontology.setMetainf(ISO3166_1);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ISO3166_2) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-17");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ISO3166_2.json"));
			ontology.setDescription("Ontology defining the standard alpha codes for provincess");
			ontology.setIdentification(ISO3166_2);
			ontology.setMetainf(ISO3166_2);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(AIRPORT_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-15");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_airportsdata.json"));
			ontology.setDescription(DESCNOTEBOOKEXMP_STR);
			ontology.setIdentification(AIRPORT_STR);
			ontology.setMetainf(AIRPORT_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
	}

	public void initOntologyQAWindTurbines() {
		Ontology ontology;
		List<DataModel> dataModels;

		if (ontologyRepository.findByIdentification(QA_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-18");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_QA_OVERVIEW.json"));
			ontology.setDescription("QA_OVERVIEW DM");
			ontology.setIdentification(QA_STR);
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(PRODUCERERROR_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-19");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Producer_ErrorCat.json"));
			ontology.setDescription("Producer_ErrorCat desc");
			ontology.setIdentification(PRODUCERERROR_STR);
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ERRORSONDATE_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-20");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_errorsOnDate.json"));
			ontology.setDescription("Different errors clasified by category and date");
			ontology.setIdentification(ERRORSONDATE_STR);
			ontology.setMetainf("error,category");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(QADETAIL_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-21");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_QA_DETAIL.json"));
			ontology.setDescription("Detail about quality");
			ontology.setIdentification(QADETAIL_STR);
			ontology.setMetainf("QA,detail");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ERRORONDATE_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-22");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_errorsTypeOnDate.json"));
			ontology.setDescription("Different errors clasified by error type and date");
			ontology.setIdentification(ERRORONDATE_STR);
			ontology.setMetainf("error,type");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(QADETAIL_EXT_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-23");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_QA_DETAIL_EXTENDED.json"));
			ontology.setDescription("A version of QA_DETAIL with assets and sites names");
			ontology.setIdentification(QADETAIL_EXT_STR);
			ontology.setMetainf("qa,detail,extended");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification("SITES") == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-24");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_SITES.json"));
			ontology.setDescription("Info about a wind park");
			ontology.setIdentification("SITES");
			ontology.setMetainf("site,wind,park");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
	}

	public void initOntologyUserAccess() {
		log.info("init OntologyUserAccess");
	}

	public void initOntologyUserAccessType() {
		final String allPermissions = "Todos los permisos";
		log.info("init OntologyUserAccessType");
		final List<OntologyUserAccessType> types = ontologyUserAccessTypeRepository.findAll();
		if (types.isEmpty()) {
			log.info("No user access types found...adding");
			OntologyUserAccessType type = new OntologyUserAccessType();
			type.setId("MASTER-Ontology-User-Access-Type-1");
			type.setName("ALL");
			type.setDescription(allPermissions);
			ontologyUserAccessTypeRepository.save(type);
			type = new OntologyUserAccessType();
			type.setId("MASTER-Ontology-User-Access-Type-2");
			type.setName("QUERY");
			type.setDescription(allPermissions);
			ontologyUserAccessTypeRepository.save(type);
			type = new OntologyUserAccessType();
			type.setId("MASTER-Ontology-User-Access-Type-3");
			type.setName("INSERT");
			type.setDescription(allPermissions);
			ontologyUserAccessTypeRepository.save(type);
		}

	}

	public void initDashboardUserAccessType() {

		log.info("init DashboardUserAccessType");
		final List<DashboardUserAccessType> types = dashboardUserAccessTypeRepository.findAll();
		if (types.isEmpty()) {
			log.info("No user access types found...adding");
			DashboardUserAccessType type = new DashboardUserAccessType();
			type.setId("MASTER-Dashboard-User-Access-Type-1");
			type.setName("EDIT");
			type.setDescription("view and edit access");
			dashboardUserAccessTypeRepository.save(type);
			type = new DashboardUserAccessType();
			type.setId("MASTER-Dashboard-User-Access-Type-2");
			type.setName("VIEW");
			type.setDescription("view access");
			dashboardUserAccessTypeRepository.save(type);

		}

	}

	public void initRoleUser() {
		log.info("init init_RoleUser");
		final List<Role> types = roleRepository.findAll();
		if (types.isEmpty()) {
			try {

				log.info("No roles en tabla.Adding...");
				Role type = new Role();
				type.setIdEnum(Role.Type.ROLE_ADMINISTRATOR);
				type.setName("Administrator");
				type.setDescription("Administrator of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DEVELOPER);
				type.setName("Developer");
				type.setDescription("Advanced User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_USER);
				type.setName("User");
				type.setDescription("Basic User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DATASCIENTIST);
				type.setName("Analytics");
				type.setDescription("Analytics User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_PARTNER);
				type.setName("Partner");
				type.setDescription("Partner in the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_SYS_ADMIN);
				type.setName("SysAdmin");
				type.setDescription("System Administradot of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_OPERATIONS);
				type.setName("Operations");
				type.setDescription("Operations for the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DEVOPS);
				type.setName("DevOps");
				type.setDescription("DevOps for the Platform");
				roleRepository.save(type);
				//
				// UPDATE of the ROLE_ANALYTICS
				final Role typeSon = roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString());
				final Role typeParent = roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString());
				typeSon.setRoleParent(typeParent);
				roleRepository.save(typeSon);

				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DATAVIEWER);
				type.setName("DataViewer");
				type.setDescription("DataViewer User of the Platform");
				roleRepository.save(type);

			} catch (final Exception e) {
				log.error("Error initRoleType:" + e.getMessage());
				roleRepository.deleteAll();
				throw new GenericRuntimeOPException("Error creating Roles...Stopping");
			}

		}
	}

	public void initToken() {
		log.info("init token");
		final List<Token> tokens = tokenRepository.findAll();
		if (tokens.isEmpty()) {
			log.info("No Tokens, adding ...");
			if (clientPlatformRepository.findAll().isEmpty())
				throw new GenericRuntimeOPException("You need to create ClientPlatform before Token");

			ClientPlatform client = clientPlatformRepository.findByIdentification(TICKETING_APP);
			final Set<Token> hashSetTokens = new HashSet<>();

			Token token = new Token();
			token.setId("MASTER-Token-1");
			token.setClientPlatform(client);
			token.setTokenName("e7ef0742d09d4de5a3687f0cfdf7f626");
			token.setActive(true);
			hashSetTokens.add(token);
			client.setTokens(hashSetTokens);
			tokenRepository.save(token);

			client = clientPlatformRepository.findByIdentification("DeviceMaster");
			token = new Token();
			token.setId("MASTER-Token-2");
			token.setClientPlatform(client);
			token.setTokenName("a16b9e7367734f04bc720e981fcf483f");
			tokenRepository.save(token);

			client = clientPlatformRepository.findByIdentification(GTKPEXAMPLE_STR);
			token = new Token();
			token.setId("MASTER-Token-3");
			token.setClientPlatform(client);
			token.setTokenName("690662b750274c8ba8748d7d55e9db5b");
			tokenRepository.save(token);
		}

	}

	public void initUserToken() {
		log.info("init user token");
		final List<UserToken> tokens = userTokenRepository.findAll();
		if (tokens.isEmpty()) {
			final List<User> userList = userCDBRepository.findAll();
			int i = 1;
			for (final Iterator<User> iterator = userList.iterator(); iterator.hasNext(); i++) {
				final User userCDB = iterator.next();
				final UserToken userToken = new UserToken();
				userToken.setId("MASTER-UserToken-" + i);
				userToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
				userToken.setUser(userCDB);
				userToken.setCreatedAt(Calendar.getInstance().getTime());
				try {
					userTokenRepository.save(userToken);
				} catch (final Exception e) {
					log.info("Could not create user token for user " + user.getUserId());
				}
			}
		}
	}

	public void initUser() {
		log.info("init UserCDB");
		final List<User> types = userCDBRepository.findAll();
		User type = null;
		if (types.isEmpty()) {
			try {
				log.info("No types en tabla.Adding...");
				type = new User();
				type.setUserId(ADMINISTRATOR);
				type.setPassword("SHA256(LoOY0z1pq+O2/h05ysBSS28kcFc8rSr7veWmyEi7uLs=)");
				type.setFullName("A Administrator of the Platform");
				type.setEmail("administrator@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId(DEVELOPER);
				type.setPassword("SHA256(yskOwp3Zjjuvf4UxUGcC7Ybq5w9S1iXJS2whDw4sE1A=)");
				type.setFullName("A Developer of the Platform.");
				type.setEmail("developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_developer");
				type.setPassword(SHA_STR);
				type.setFullName("Demo Developer of the Platform");
				type.setEmail("demo_developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("user");
				type.setPassword(SHA_STR);
				type.setFullName("Generic User of the Platform");
				type.setEmail("user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_user");
				type.setPassword(SHA_STR);
				type.setFullName("Demo User of the Platform");
				type.setEmail("demo_user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("analytics");
				type.setPassword(SHA_STR);
				type.setFullName("Generic Analytics User of the Platform");
				type.setEmail("analytics@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("partner");
				type.setPassword(SHA_STR);
				type.setFullName("Generic Partner of the Platform");
				type.setEmail("partner@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_PARTNER.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("sysadmin");
				type.setPassword(SHA_STR);
				type.setFullName("Generic SysAdmin of the Platform");
				type.setEmail("sysadmin@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_SYS_ADMIN.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("operations");
				type.setPassword(SHA_STR);
				type.setFullName("Operations of the Platform");
				type.setEmail("operations@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_OPERATIONS.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("dataviewer");
				type.setPassword(SHA_STR);
				type.setFullName("DataViewer User of the Platform");
				type.setEmail("dataviewer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()));
				userCDBRepository.save(type);
				//
			} catch (final Exception e) {
				log.error("Error UserCDB:" + e.getMessage());
				userCDBRepository.deleteAll();
				throw new GenericRuntimeOPException("Error creating users...ignoring creation rest of Tables");
			}
		}
	}

	public void initMarketPlace() {
		log.info("init MarketPlace");

		final String MARKET_IMG_NODERED_PNG = "market/img/NODERED.png";
		final String MARKET_IMG_NOTEBOOK_PNG = "market/img/NOTEBOOK.png";

		final List<MarketAsset> marketAssets = marketAssetRepository.findAll();
		if (marketAssets.isEmpty()) {
			log.info("No market Assets...adding");
			MarketAsset marketAsset = new MarketAsset();
			// Getting Started Guide
			marketAsset.setId("MASTER-MarketAsset-1");
			marketAsset.setIdentification("GettingStartedGuide");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/GettingStartedGuide.json"));
			marketAsset.setImage(loadFileFromResources("market/img/asset.jpg"));
			marketAsset.setImageType("jpg");
			marketAssetRepository.save(marketAsset);

			// Architecture
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-2");
			marketAsset.setIdentification("onesaitPlatformArchitecture");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/onesaitPlatformArchitecture.json"));
			marketAsset.setImage(loadFileFromResources("market/img/asset.jpg"));
			marketAsset.setImageType("jpg");
			marketAssetRepository.save(marketAsset);

			// onesaitPlatform WITH DOCKER
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-3");
			marketAsset.setIdentification("onesaitPlatformWithDocker");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/onesaitPlatformWithDocker.json"));
			marketAsset.setImage(loadFileFromResources("market/img/docker.png"));
			marketAsset.setImageType("png");
			marketAssetRepository.save(marketAsset);

			// DIGITAL TWIN
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-4");
			marketAsset.setIdentification("DIGITAL TWIN EXAMPLE");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/DigitalTwin.json"));
			marketAsset.setImage(loadFileFromResources("market/img/gears.png"));
			marketAsset.setImageType("png");
			marketAsset.setContent(loadFileFromResources("market/docs/TurbineHelsinki.zip"));
			marketAsset.setContentId("TurbineHelsinki.zip");
			marketAssetRepository.save(marketAsset);

			// OAUTH2 Authentication
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-5");
			marketAsset.setIdentification("OAuth2AndJWT");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/Oauth2Authentication.json"));
			marketAsset.setContent(loadFileFromResources("market/docs/oauth2-authentication.zip"));
			marketAsset.setContentId("oauth2-authentication.zip");
			marketAssetRepository.save(marketAsset);

			// JSON document example for Data import tool
			marketAsset = new MarketAsset();
			marketAsset.setId("MASTER-MarketAsset-6");
			marketAsset.setIdentification("Countries JSON");
			marketAsset.setUser(getUserAdministrator());
			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);
			marketAsset.setJsonDesc(loadFromResources("market/details/Countries.json"));
			marketAsset.setImage(loadFileFromResources("market/img/json.png"));
			marketAsset.setImageType("png");
			marketAsset.setContent(loadFileFromResources("market/docs/countries.json"));
			marketAsset.setContentId("countries.json");
			marketAssetRepository.save(marketAsset);

			// Digital Twin Web
			createMarketAsset("MASTER-MarketAsset-7", "SenseHatDemo", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.WEBPROJECT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/SenseHatDemo.json", null, null, null, null);

			// Digital Twin Sense Hat
			createMarketAsset("MASTER-MarketAsset-8", "DigitalTwinSenseHat", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.APPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/DigitalTwinSenseHat.json", "market/img/jar-file.jpg", "jpg",
					"market/docs/SensehatHelsinki.zip", "SensehatHelsinki.zip");

			// Videos
			createMarketAsset("MASTER-MarketAsset-9", "Tutorials", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/Tutorials.json", null, null, null, null);

			// Quickview Video
			createMarketAsset("MASTER-MarketAsset-10", "QuickviewPlatform", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/QuickviewPlatform.json", null, null, null, null);

			// Health Check Android Application
			createMarketAsset("MASTER-MarketAsset-11", "HealthCheckAndroidApplication",
					MarketAsset.MarketAssetState.APPROVED, MarketAsset.MarketAssetType.APPLICATION,
					MarketAsset.MarketAssetPaymentMode.FREE, true, "market/details/HealthCheckApplication.json", null,
					null, "market/docs/HealthCheckApp.zip", "HealthCheckApp.zip");

			// Airports Dashboard
			createMarketAsset("MASTER-MarketAsset-12", "AirportsDashboard", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.URLAPPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/Airports.json", null, null, null, null);

			// Notebook - Regression
			createMarketAsset("MASTER-MarketAsset-13", "Notebook", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/Notebookregression.json", MARKET_IMG_NOTEBOOK_PNG, "png",
					"market/docs/LinearRegressionBostonAttributesDemo.json",
					"LinearRegressionBostonAttributesDemo.json");

			// Notebook - Phyton Examples
			createMarketAsset("MASTER-MarketAsset-14", "NotebookPython", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NotebookPythonClient.json", MARKET_IMG_NOTEBOOK_PNG, "png",
					"market/docs/OnesaitPlatformPythonClient.json", "OnesaitPlatformPythonClient.json");

			// Notebook - Spark Examples
			createMarketAsset("MASTER-MarketAsset-15", "NotebookSpark", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NotebookSparkClient.json", MARKET_IMG_NOTEBOOK_PNG, "png",
					"market/docs/OnesaitPlatformSparkClient.json", "OnesaitPlatformSparkClient.json");

			// Notebook - Examples Int.
			createMarketAsset("MASTER-MarketAsset-16", "NotebookZeppelin", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NotebookZeppelinInterpretes.json", MARKET_IMG_NOTEBOOK_PNG, "png",
					"market/docs/OnesaitPlatformZeppelinInterpretes.json", "OnesaitPlatformZeppelinInterpretes.json");

			// Node-Red Flow - Text Azure Cognitive Services
			createMarketAsset("MASTER-MarketAsset-17", "NoderedACSLanguage", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NoderedAzureCognitiveServiceLanguage.json", MARKET_IMG_NODERED_PNG, "png",
					"market/docs/AzureCognitiveServiceLanguage.json", "AzureCognitiveServiceLanguage.json");

			// Node-Red Flow - Speech Azure Cognitive Services
			createMarketAsset("MASTER-MarketAsset-18", "NoderedACSSpeech", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NoderedAzureCognitiveServiceSpeech.json", MARKET_IMG_NODERED_PNG, "png",
					"market/docs/AzureCognitiveServiceSpeech.json", "AzureCognitiveServiceSpeech.json");

			// Node-Red Flow - Translation Azure Cognitive Services
			createMarketAsset("MASTER-MarketAsset-19", "NoderedACSTranslation", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NoderedAzureCognitiveServiceTranslation.json", MARKET_IMG_NODERED_PNG, "png",
					"market/docs/AzureCognitiveServiceTranslation.json", "AzureCognitiveServiceTranslation.json");

			// Node-Red Flow - Vision Azure Cognitive Services
			createMarketAsset("MASTER-MarketAsset-20", "NoderedACSVision", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/NoderedAzureCognitiveServiceVision.json", MARKET_IMG_NODERED_PNG, "png",
					"market/docs/AzureCognitiveServiceVision.json", "AzureCognitiveServiceVision.json");

			// Node-Red Flow - External API REST Invoke
			createMarketAsset("MASTER-MarketAsset-21", "OpenNotify", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/OpenNotify.json", MARKET_IMG_NODERED_PNG, "png", "market/docs/OpenNotify.json",
					"AzureCognitiveServiceVision.json");
		}
	}

	private void createMarketAsset(String id, String identification, MarketAsset.MarketAssetState state,
			MarketAsset.MarketAssetType assetType, MarketAsset.MarketAssetPaymentMode paymentMode, boolean isPublic,
			String jsonDesc, String image, String imageType, String content, String contentId) {

		final MarketAsset marketAsset = new MarketAsset();
		marketAsset.setId(id);
		marketAsset.setIdentification(identification);
		marketAsset.setUser(getUserAdministrator());
		marketAsset.setPublic(isPublic);
		marketAsset.setState(state);
		marketAsset.setMarketAssetType(assetType);
		marketAsset.setPaymentMode(paymentMode);
		marketAsset.setJsonDesc(loadFromResources(jsonDesc));
		if (image != null) {
			marketAsset.setImage(loadFileFromResources(image));
			marketAsset.setImageType(imageType);
		}
		if (content != null) {
			marketAsset.setContent(loadFileFromResources(content));
			marketAsset.setContentId(contentId);
		}
		marketAssetRepository.save(marketAsset);
	}

	public void initNotebook() {
		log.info("init notebook");
		final List<Notebook> notebook = notebookRepository.findAll();
		if (notebook.isEmpty()) {
			try {
				final User userNotebookAnalytics = getUserAnalytics();
				final Notebook n = new Notebook();
				n.setId("MASTER-Notebook-1");
				n.setUser(userNotebookAnalytics);
				n.setIdentification("Analytics osp notebook tutorial");
				// Default zeppelin notebook tutorial ID
				n.setIdzep("2A94M5J1Z");
				notebookRepository.save(n);
			} catch (final Exception e) {
				log.info("Could not create notebook by:" + e.getMessage());
			}

		}
	}

	public void initDataflow() {
		log.info("init dataflow");
	}

	public void initNotebookUserAccessType() {
		log.info("init notebook access type");
		final List<NotebookUserAccessType> notebookUat = notebookUserAccessTypeRepository.findAll();
		if (notebookUat.isEmpty()) {
			try {
				final NotebookUserAccessType p = new NotebookUserAccessType();
				p.setId("ACCESS-TYPE-1");
				p.setDescription("Edit Access");
				p.setName("EDIT");
				notebookUserAccessTypeRepository.save(p);
			} catch (final Exception e) {
				log.info("Could not create notebook access type by:" + e.getMessage());
			}

		}
	}

	public void initDataflowUserAccessType() {
		log.info("init dataflow access type");
		final List<PipelineUserAccessType> pipelineUat = pipelineUserAccessTypeRepository.findAll();
		if (pipelineUat.isEmpty()) {
			try {
				final PipelineUserAccessType p = new PipelineUserAccessType();
				p.setId("ACCESS-TYPE-1");
				p.setDescription("Edit Access");
				p.setName("EDIT");
				pipelineUserAccessTypeRepository.save(p);
			} catch (final Exception e) {
				log.info("Could not create dataflow access type by:" + e.getMessage());
			}

		}
	}

	public void initGadgetTemplate() {
		log.info("init GadgetTemplate");
		final String templateJS = "//Write your controller (JS code) code here\r\n" + "\r\n"
				+ "//Focus here and F11 to full screen editor\r\n" + "\r\n"
				+ "//This function will be call once to init components\r\n" + "vm.initLiveComponent = function(){\r\n"
				+ "\r\n" + "};\r\n" + "\r\n"
				+ "//This function will be call when data change. On first execution oldData will be null\r\n"
				+ "vm.drawLiveComponent = function(newData, oldData){\r\n" + "\r\n" + "};\r\n" + "\r\n"
				+ "//This function will be call on element resize\r\n" + "vm.resizeEvent = function(){\r\n" + "\r\n"
				+ "}\r\n" + "\r\n" + "//This function will be call when element is destroyed\r\n"
				+ "vm.destroyLiveComponent = function(){\r\n" + "\r\n" + "};";
		final List<GadgetTemplate> gadgets = gadgetTemplateRepository.findAll();
		if (gadgets.isEmpty()) {
			log.info("No gadgetsTemplate ...");

			GadgetTemplate gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-1");
			gadgetTemplate.setIdentification("Select");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setDescription("this template creates a drop-down list");
			gadgetTemplate.setTemplate("<!-- \r\n"
					+ "to use the template we can create a datasource with a query like this:\r\n"
					+ "select distinct (Restaurant.borough) as borough from Restaurants as c\r\n"
					+ "When we use the template, the parameters can be filled, for example, as follows:\r\n"
					+ "SIGNALNAME, name of the signal that the gadget emits, we write borough\r\n"
					+ "PARAMETER-FROMDATASOURCE-VALUE, we select the datasource borough parameter that would be the value that is sent.\r\n"
					+ "PARAMETER-FROMDATASOURCE-LABEL, we select the datasource borough parameter that would be the label that is shown.-->\r\n"
					+ "\r\n" + "\r\n" + "\r\n" + "\r\n"
					+ "<md-input-container style=\"margin:0;padding:0;width:100%\">\r\n"
					+ "  <md-select ng-change=\"sendFilter('<!--label-osp  name=\"signalName\" type=\"text\"-->',c)\" ng-model=\"c\">\r\n"
					+ "    <md-option ng-value=\"inst.<!--label-osp  name=\"parameter-fromDataSource-value\" type=\"ds_parameter\"--> \" ng-repeat='inst in ds' >\r\n"
					+ "    {{inst.<!--label-osp  name=\"parameter-fromDataSource-label\" type=\"ds_parameter\"-->}}\r\n"
					+ "    </md-option>\r\n" + "  </md-select>\r\n" + "</md-input-container> ");

			gadgetTemplate.setTemplateJS(templateJS);
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);

			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-2");
			gadgetTemplate.setIdentification("Simple Value");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setDescription("this template shows a value with its title and an icon");
			gadgetTemplate.setTemplate("<style>\r\n" + "  .card-count{\r\n" + "   color: #2e43ab;\r\n"
					+ "    font-weight: bold;\r\n" + "    font-size: -webkit-xxx-large; \r\n"
					+ "   padding-left: 20px;\r\n" + SEPARATOR_BIS + "  .card-title{\r\n" + "   color: #000000;\r\n"
					+ "    font-weight: bold;\r\n" + "    font-size: x-large;   	\r\n" + SEPARATOR_BIS + SEPARATOR
					+ ".card-green{\r\n" + "  color:green;\r\n" + SEPARATOR + SEPARATOR_BIS + "  .my-card{\r\n"
					+ "      padding: 15px;\r\n" + SEPARATOR_BIS + "   .card-icon{     \r\n"
					+ "      padding-top: 0px;\r\n" + "      padding-left: 0px;\r\n" + "      padding-bottom: 25px;\r\n"
					+ "      padding-right: 25px;\r\n" + SEPARATOR_BIS + SEPARATOR + "</style>\r\n" + "\r\n" + "\r\n"
					+ "<div class=\"my-card\">\r\n" + "\r\n" + "\r\n"
					+ " <md-icon class=\"card-icon\" style=\"font-size:35px\">assessment</md-icon> \r\n"
					+ " <label class=\"card-title\"><!--label-osp  name=\"title\" type=\"text\"--></label><br>\r\n"
					+ " <label class=\"card-count\">{{ds[0].<!--label-osp  name=\"ontology field\" type=\"ds_parameter\"-->}}</label>\r\n"
					+ "      \r\n" + "\r\n" + "</div>\r\n" + "\r\n");

			gadgetTemplate.setTemplateJS(templateJS);
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);

			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-3");
			gadgetTemplate.setIdentification("Chart Bubble");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setDescription("this template creates a chart bubble");
			gadgetTemplate.setTemplate("<span ng-init=\"\r\n" + "    cdata=[];\r\n"
					+ "    cdatasetOverride={label: '<!--label-osp  name=\"series label\" type=\"text\"-->'};\r\n"
					+ "    options={\r\n" + "      legend: {display: true}, \r\n"
					+ "      maintainAspectRatio: false, \r\n" + "      responsive: true,\r\n" + "      scales: {\r\n"
					+ "        xAxes: [{\r\n" + "          display: true,\r\n" + "          scaleLabel: {\r\n"
					+ "              labelString: '<!--label-osp  name=\"xAxes label\" type=\"text\"-->',\r\n"
					+ "              display: true\r\n" + "          }\r\n" + "        }],\r\n"
					+ "        yAxes: [{\r\n" + "          display: true,\r\n" + "          scaleLabel: {\r\n"
					+ "              labelString: '<!--label-osp  name=\"yAxes label\" type=\"text\"-->',\r\n"
					+ "              display: true\r\n" + "          }\r\n" + "        }]\r\n" + "      }\r\n"
					+ "    };\"/>\r\n" + "\r\n" + "    \r\n" + "<span ng-repeat=\"inst in ds\">\r\n"
					+ "  <span ng-init=\"cdata.push({x:inst.<!--label-osp  name=\"xAxes Data\" type=\"ds_parameter\"-->, y:inst.<!--label-osp  name=\"yAxes Data\" type=\"ds_parameter\"-->, r:inst.<!--label-osp  name=\"radio Data\" type=\"ds_parameter\"-->})\"></span>\r\n"
					+ "</span>\r\n" + "\r\n" + "<div style=\"height:calc(100% - 50px)\">\r\n" + "\r\n"
					+ "<canvas class=\"chart chart-bubble\" chart-data=\"cdata\"\r\n"
					+ "                  chart-colors=\"colors\" chart-options=\"options\" chart-series=\"cseries\" chart-labels=\"clabels\" chart-dataset-override=\"cdatasetOverride\"></canvas>\r\n"
					+ "\r\n");

			gadgetTemplate.setTemplateJS(templateJS);
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
	}

	private void initOntologyRestaurants() {

		log.info("init OntologyRestaurants");
		final String schema = RESTSCHEMA_STR;
		if (ontologyRepository.findByIdentification(REST_STR) == null) {
			final Ontology ontology = new Ontology();
			if (OSDetector.isWindows()) {
				ontology.setJsonSchema(loadFromResources(schema));
			} else {
				ontology.setJsonSchema("/tmp/" + schema);
			}
			ontology.setId("MASTER-Ontology-Restaurant-1");
			ontology.setIdentification(REST_STR);
			ontology.setDescription("Ontology Restaurants for testing");
			ontology.setMetainf(REST_STR);
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setDataModel(dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0));
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setJsonSchema(loadFromResources(RESTSCHEMA_STR));
			ontologyRepository.save(ontology);
		}
	}

	private void initWebProject() {

		log.info("init WebProject");

		if (webProjectRepository.findByIdentification(CESIUM) == null) {

			loadWebProyect();

			final WebProject webProject = new WebProject();

			webProject.setDescription("Stand Alone Library of Cesium 1.60 nad CesiumHeatMap");
			webProject.setIdentification(CESIUM);
			webProject.setUser(getUserDeveloper());
			webProject.setMainFile("Cesium1.60/Cesium.js");

			webProjectRepository.save(webProject);

		}

	}

	private void initCategories() {
		log.info("init Categories");

		if (categoryRepository.findAll().isEmpty()) {
			final Category category = new Category();
			category.setIdentification("GeneralCategory");
			category.setDescription("General Category Description");
			categoryRepository.save(category);

			final Subcategory subcategory = new Subcategory();
			subcategory.setIdentification("GeneralSubcategory");
			subcategory.setDescription("General Subcategory Description");
			subcategory.setCategory(category);
			subcategoryRepository.save(subcategory);
		}
	}

	private void loadWebProyect() {
		try {

			final MultipartFile file = new MockMultipartFile("file", "cesium.zip",
					MediaType.APPLICATION_OCTET_STREAM_VALUE,
					Model.class.getClassLoader().getResourceAsStream("cesium/cesium.zip"));

			uploadFileToFolder(file, rootFolder + "cesium/");
			unzipFile(rootFolder + "cesium/", file.getOriginalFilename());
		} catch (final FileNotFoundException e) {
			log.error("File not found 'cesium/cesium.zip'. {}", e);
		} catch (final IOException e) {
			log.error("Error reading file 'cesium/cesium.zip' {}", e);
		}

	}

	private void uploadFileToFolder(MultipartFile file, String path) {

		final String fileName = file.getOriginalFilename();
		byte[] bytes;
		try {
			bytes = file.getBytes();

			final InputStream is = new ByteArrayInputStream(bytes);

			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + fileName;
			final OutputStream os = new FileOutputStream(new File(fullPath));

			IOUtils.copy(is, os);

			is.close();
			os.close();
		} catch (final IOException e) {
			throw new WebProjectServiceException("Error uploading files " + e);
		}

		log.debug("File: " + path + fileName + " uploaded");
	}

	private void unzipFile(String path, String fileName) {

		final File folder = new File(path + fileName);
		log.debug("Unzipping zip file: " + folder);

		DataInputStream is = null;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(folder))) {
			final byte[] buffer = new byte[4];
			final byte[] zipbf = new byte[] { 0x50, 0x4B, 0x03, 0x04 };

			is = new DataInputStream(new FileInputStream(folder));
			is.readFully(buffer);
			is.close();
			if (!Arrays.equals(buffer, zipbf)) {
				throw new WebProjectServiceException("Error: Invalid file");
			}

			ZipEntry ze;

			while (null != (ze = zis.getNextEntry())) {
				if (ze.isDirectory()) {
					final File f = new File(path + ze.getName());
					f.mkdirs();
				} else {
					log.debug("Unzipping file: " + ze.getName());
					final FileOutputStream fos = new FileOutputStream(path + ze.getName());
					IOUtils.copy(zis, fos);
					fos.close();
					zis.closeEntry();
				}
			}

		} catch (final IOException e) {
			throw new WebProjectServiceException("Error unzipping files " + e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (final IOException e) {
					log.debug("Error: " + e);
				}
		}

		if (folder.exists()) {
			deleteFile(folder);
		}

	}

	private void deleteFile(File file) {
		try {
			Files.delete(file.toPath());
		} catch (final IOException e) {
			log.debug("Error deleting folder: {}", file.getPath());
		}
	}

}