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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.commons.OSDetector;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.ConfigDBTenantConfig;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiCategories;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.DataType;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.HeaderType;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppRole;
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
import com.minsait.onesait.platform.config.model.DataflowInstance;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.I18nResources;
import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.LineageRelations;
import com.minsait.onesait.platform.config.model.LineageRelations.Group;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccessType;
import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.ODTypologyDataset;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.OntologyCategory;
import com.minsait.onesait.platform.config.model.OntologyPrestoDatasource;
import com.minsait.onesait.platform.config.model.OntologyPrestoDatasource.PrestoDatasourceType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyDataType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.AggregationFunction;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.WindowType;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyUserAccessType;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Pipeline.PipelineStatus;
import com.minsait.onesait.platform.config.model.PipelineUserAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Rollback;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.ApiRepository;
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
import com.minsait.onesait.platform.config.repository.DataflowInstanceRepository;
import com.minsait.onesait.platform.config.repository.DatasetResourceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateTypeRepository;
import com.minsait.onesait.platform.config.repository.I18nResourcesRepository;
import com.minsait.onesait.platform.config.repository.InternationalizationRepository;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.LineageRelationsRepository;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.NotebookUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.ODBinaryFilesDatasetRepository;
import com.minsait.onesait.platform.config.repository.ODTypologyDatasetRepository;
import com.minsait.onesait.platform.config.repository.ODTypologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyCategoryRepository;
import com.minsait.onesait.platform.config.repository.OntologyPrestoDatasourceRepository;
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
import com.minsait.onesait.platform.config.repository.SubscriptionRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;
import com.minsait.onesait.platform.config.services.dataflow.beans.DataflowCredential;
import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.MasterConfiguration;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.MasterConfigurationRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.repository.TenantRepository;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.configdb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitConfigDB {

	private static final String QUERY_METRICS = "QueryMetrics";
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
	private static final String SHA_EDGE_STR = "SHA256(LoOY0z1pq+O2/h05ysBSS28kcFc8rSr7veWmyEi7uLs=)";
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
	private static final String AIRLINE_SAFETY_STR = "airline_safety";
	private static final String INDONESIAN_CITIES_STR = "indonesian_cities";
	private static final String METEORITE_LANDINGS_STR = "meteorite_landings";

	private boolean started = false;
	private User userDeveloper = null;
	private User userAdministrator = null;
	private User user = null;
	private User userAnalytics = null;
	private GadgetDatasource gadgetDatasourceDeveloper = null;

	@Value("${onesaitplatform.webproject.rootfolder.path:/usr/local/webprojects/}")
	private String rootFolder;

	@Value("${onesaitplatform.webproject.baseurl:http://localhost:18000/web}")
	private String webProjectPath;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	@Value("${opendata.load-ontologies:false}")
	private boolean openDataPortal;

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
	InternationalizationRepository internationalizationRepository;
	@Autowired
	I18nResourcesRepository i18nResourcesRepository;
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
	ODTypologyRepository typologyRepository;
	@Autowired
	ODTypologyDatasetRepository typologyDatasetRepository;
	@Autowired
	ODBinaryFilesDatasetRepository binaryFilesDatasetRepository;

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
	DataflowInstanceRepository dataflowInstanceRepository;

	@Autowired
	ClientPlatformInstanceSimulationRepository simulationRepository;

	@Autowired
	OntologyVirtualDatasourceRepository ontologyVirtualDataSourceRepository;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	@Lazy
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
	private GadgetTemplateTypeRepository gadgetTemplateTypeRepository;

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private LayerRepository layerRepository;

	@Autowired
	private ViewerRepository viewerRepository;

	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;

	@Autowired
	private PipelineRepository pipelineRepository;

	@Autowired
	private OntologyTimeSeriesPropertyRepository ontologyTimeSeriesPropertyRepository;

	@Autowired
	private OntologyTimeSeriesWindowRepository ontologyTimeSeriesWindowRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private LineageRelationsRepository lineageRelationsRepository;

	@Autowired
	private SubcategoryRepository subcategoryRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private DatasetResourceRepository resourceRepository;

	@Value("${onesaitplatform.server.name:localhost}")
	private String serverName;

	@Value("${onesaitplatform.init.mailconfig}")
	private boolean loadMailConfig;

	@Value("${onesaitplatform.init.samples:false}")
	private boolean initSamples;

	@Value("${onesaitplatform.database.mongodb.servers:realtimedb:27017}")
	private String rtdbServers;

	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String rtdbDatabase;

	@Value("${onesaitplatform.database.mongodb.execution-timeout:10000}")
	private String rtdbExecutionTimeout;

	@Value("${onesaitplatform.database.mongodb.queries-limit:2000}")
	private String rtdbQueriesLimit;

	@Value("${onesaitplatform.database.mongodb.socket-timeout:5000}")
	private String rtdbSocketTimeout;

	@Value("${onesaitplatform.database.mongodb.connection-timeout:30000}")
	private String rtdbConnectionTimeout;

	@Value("${onesaitplatform.database.mongodb.wait-time:5000}")
	private String rtdbWaitTime;

	@Value("${onesaitplatform.database.mongodb.pool-size:100}")
	private String rtdbPoolSize;

	@Value("${onesaitplatform.database.mongodb.writeConcern:UNACKNOWLEDGED}")
	private String rtdbWriteConcern;

	@Value("${onesaitplatform.database.mongodb.sslEnabled:false}")
	private String rtdbSslEnabled;

	@Value("${onesaitplatform.database.mongodb.useQuasar:false}")
	private String rtdbUseQuasar;

	@Value("${onesaitplatform.server.minio.cookiedomain:localhost}")
	private String minioCookiedomain;

	@Value("${onesaitplatform.init.multitenant.adminToken:}")
	private String adminVerticalToken;

	@Autowired
	private MasterConfigurationRepository masterConfigurationRepository;

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
	private static final String MASTER_GADGET_16 = "MASTER-Gadget-16";
	private static final String MASTER_GADGET_17 = "MASTER-Gadget-17";

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
	private static final String IONASSETSCESIUM = "IonAssets";

	private static final String ISO3166_1 = "ISO3166_1";
	private static final String TICKETING_APP = "TicketingApp";
	private static final String DEFAULT = "default";
	private static final String DEVELOPER = "developer";
	private static final String ADMINISTRATOR = "administrator";
	private static final String PLATFORM_ADMINISTRATOR = "platform_admin";
	private static final String DOCKER = "docker";
	private static final String QUERY = "query";
	private static final String CESIUM = "cesium";
	private static final String CESIUM2 = "cesium2";
	private static final String TABLE = "table";

	private static final String GADGET1CONFIG = "{\"legend\":{\"display\":false,\"fullWidth\":false,\"position\":\"top\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,\"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":\"10\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\"labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"1\",\"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",\"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}";
	private static final String GADGET2CONFIG = "{\"tablePagination\":{\"limit\":\"5\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\",\"trHeightFooter\":\"40\",\"textColorTHead\":\"#141414\",\"textColorBody\":\"#000000\",\"textColorFooter\":\"#000000\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}";
	private static final String GADGET5CONFIG = "{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}";
	private static final String MASTER_INTERNATIONALIZATION_ONE = "MASTER-Internationalization-1";
	private static final String MASTER_INTERNATIONALIZATION_TWO = "MASTER-Internationalization-2";
	private static final String ACCESS_TYPE_ONE = "ACCESS-TYPE-1";
	private static final String MAIN_PS_WD = "SHA256(LoOY0z1pq+O2/h05ysBSS28kcFc8rSr7veWmyEi7uLs=)";
	private static final String CONSTANT_STR = "CONSTANT";
	private static final String MASTER_DASHBOARD_FRTH = "MASTER-Dashboard-4";
	private static final String MASTER_DASHBOARD_FIFTH = "MASTER-Dashboard-5";
	private static final String PATH_ID = "/{id}";
	private static final String MENU_NOT_FOUND = "Menu not found";
	private static final String PIPELINE_DESCRIPTION = "Pipeline XML for microservice generation";
	private static final String TWITTER = "Twitter";

	private static final String[] DASHBOARD_QUERYMETRICS_DATASOURCES_ID = new String[] { "MASTER-GadgetDatasource-16",
			"MASTER-GadgetDatasource-17", "MASTER-GadgetDatasource-18", "MASTER-GadgetDatasource-19",
			"MASTER-GadgetDatasource-20", "MASTER-GadgetDatasource-21", "MASTER-GadgetDatasource-22",
			"MASTER-GadgetDatasource-23", "MASTER-GadgetDatasource-24", "MASTER-GadgetDatasource-25",
			"MASTER-GadgetDatasource-26", "MASTER-GadgetDatasource-27", "MASTER-GadgetDatasource-28",
			"MASTER-GadgetDatasource-29", "MASTER-GadgetDatasource-30", "MASTER-GadgetDatasource-31",
			"MASTER-GadgetDatasource-32" };
	private static final String[] DASHBOARD_QUERYMETRICS_DATASOURCES_IDENTIFICATION = new String[] {
			"QueryMetrics_count_all", "QueryMetrics_group_source", "QueryMetrics_group_source_errors",
			"QueryMetrics_avg_queryTime_source", "QueryMetrics_mostactiveusers", "QueryMetrics_total_users_errors",
			"QueryMetrics_all", "QueryMetrics_total_error", "QueryMetrics_10_slowerqueries", "QueryMetrics_total",
			"QueryMetrics_max_totalMs", "QueryMetrics_all_disorderly", "QueryMetrics_distinct_entity",
			"QueryMetrics_distinct_queryType", "QueryMetrics_distinct_source", "QueryMetrics_distinct_status",
			"QueryMetrics_distinct_users" };
	private static final String[] DASHBOARD_QUERYMETRICS_DATASOURCES_QUERY = new String[] {
			"select count(*) as total, avg(totalMs) as media from QueryMetrics",
			"select count(*) as total, source from QueryMetrics group by source",
			"select count(*) as total, source from QueryMetrics  where status='ERROR' group by source",
			"select avg(queryTime) as average , source from (select (endTime - startTime) as queryTime , user, queryType, source, query, startTime, endTime, status, entity, datasource  from QueryMetrics  ) group by source",
			"select count(*) as total, user from QueryMetrics  group by user order by total desc ",
			"select count(*) as total, user from QueryMetrics where status='ERROR' group by user order by total desc ",
			"select * from QueryMetrics order by startTime desc",
			"select count(*) as total from QueryMetrics where status = 'ERROR'",
			"select * from QueryMetrics order by totalMs desc ", "select count(*) as total from QueryMetrics",
			"select max(totalMs) as totalMs from QueryMetrics ", "select * from QueryMetrics  ",
			"SELECT entity FROM QueryMetrics AS c group by entity order by entity",
			"SELECT queryType FROM QueryMetrics AS c group by queryType order by queryType",
			"SELECT source FROM QueryMetrics AS c group by source order by source",
			"select status from QueryMetrics group by status",
			"SELECT user FROM QueryMetrics AS c group by user order by user" };
	private static final int[] DASHBOARD_QUERYMETRICS_DATASOURCES_LIMIT = new int[] { 2000, 2000, 2000, 2000, 2000,
			2000, 2000, 2000, 10, 2000, 2000, 2000, 2000, 2000, 2000, 2000, 2000 };

	private static final String[] DASHBOARD_DATACLASS_DATASOURCES_ID = new String[] { "MASTER-GadgetDatasource-33",
			"MASTER-GadgetDatasource-34", "MASTER-GadgetDatasource-35", "MASTER-GadgetDatasource-36",
			"MASTER-GadgetDatasource-37" };
	private static final String[] DASHBOARD_DATACLASS_DATASOURCES_IDENTIFICATION = new String[] { "dataClassErrors",
			"DataClassError", "DataClassWarning", "DataClassGroupByTypes", "DataClassGroupByModule" };
	private static final String[] DASHBOARD_DATACLASS_DATASOURCES_QUERY = new String[] {
			"SELECT * FROM Audit_developer where methodName='dataClassError'",
			"SELECT count(*) as value FROM Audit_developer where methodName='dataClassError' AND type='ERROR'",
			"SELECT count(*) as value FROM Audit_developer where methodName='dataClassError' AND type='WARNING'",
			"SELECT * FROM Audit_developer group by type", "SELECT * FROM Audit_developer group by module" };
	private static final int[] DASHBOARD_DATACLASS_DATASOURCES_LIMIT = new int[] { 2000, 2000, 2000, 2000, 2000 };

	@Before
	public void setDBTenant() {
		Optional.ofNullable(System.getenv().get(ConfigDBTenantConfig.CONFIGDB_TENANT_ENVVAR)).ifPresent(s -> {
			MultitenancyContextHolder.setVerticalSchema(s);
			MultitenancyContextHolder.setTenantName(
					Tenant2SchemaMapper.defaultTenantName(Tenant2SchemaMapper.extractVerticalNameFromSchema(s)));
		});
	}

	@After
	public void unsetDBTenant() {
		MultitenancyContextHolder.clear();
	}

	@PostConstruct
	@Test
	public void init() throws GenericOPException {
		setDBTenant();
		if (!started) {
			started = true;

			log.info("Start initConfigDB...");
			// first we need to create users
			log.info("creating Default Vertical for multitenancy");
			initDefaultVertical();

			// initMasterConfiguration();

			initRoleUser();
			log.info("OK init_RoleUser");
			initUser();
			log.info("OK init_User");
			//

			initDataModel();
			log.info("OK init_DataModel");
			if (initSamples) {
				initOntologyCategory();
				log.info("OK init_OntologyCategory");
			}

			initOntology();
			log.info("OK init_Ontology");

			initOntologyUserAccess();
			log.info("OK init_OntologyUserAccess");
			initOntologyUserAccessType();
			log.info("OK init_OntologyUserAccessType");

			if (initSamples) {
				initClientPlatform();
				log.info("OK init_ClientPlatform");

				initClientPlatformOntology();
				log.info("OK init_ClientPlatformOntology");

				initToken();
				log.info("OK init_Token");

			}

			initUserToken();
			log.info("OK USER_Token");

			if (initSamples) {
				initOntologyRestaurants();
				log.info("OK init_OntologyRestaurants");

				initGadgetDatasource();
				log.info("OK init_GadgetDatasource");
			}

			initGadgetTemplateType();
			log.info("OK init_GadgetTemplate_Type");
			initGadgetTemplateInstances();
			log.info("OK init_GadgetTemplate_Instances");
			initGadgetsCrudAndImportTool();
			log.info("OK init_GadgetTemplate_CrudAndImportTool");

			if (initSamples) {

				initGadgetTemplate();
				log.info("OK init_GadgetTemplate");

				initGadget();
				log.info("OK init_Gadget");
				initGadgetMeasure();
				log.info("OK init_GadgetMeasure");
			}

			if (initSamples) {
				initDashboard();
				log.info("OK init_Dashboard");
			}

			initDashboardConf();
			log.info("OK init_DashboardConf");
			initDashboardUserAccessType();
			log.info("OK init_DashboardUserAccessType");

			initInternationalizationGadgets();
			log.info("OK init_Internationalization_Gadgets");

			if (initSamples) {
				initInternationalization();
				log.info("OK init_Internationalization");
			}

			initMenuControlPanel();
			log.info("OK init_ConsoleMenu");
			initConsoleMenuRollBack();
			log.info("OK initConsoleMenuRollBack");
			initConfiguration();
			log.info("OK init_Configuration");
			initFlowDomain();
			log.info("OK init_FlowDomain");

			if (initSamples) {

				initDigitalTwin.initDigitalTwinType();
				log.info("OK init_DigitalTwinType");

				initDigitalTwin.initDigitalTwinDevice();
				log.info("OK init_DigitalTwinDevice");
			}

			initMarketPlace();
			log.info("OK init_Market");

			if (initSamples) {
				initNotebook();
				log.info("OK init_Notebook");
			}

			initDataflowInstances();
			log.info("OK init_dataflow");

			if (initSamples) {
				initPipeline();
				log.info("OK init_Pipeline");
			}
			initNotebookUserAccessType();
			log.info("OK init_notebook_user_access_type");

			initDataflowUserAccessType();
			log.info("OK init_dataflow_user_access_type");

			if (initSamples) {
				initSimulations();
				log.info("OK init_simulations");
			}

			if (initSamples) {
				initOpenFlightSample();
				log.info("OK init_openflight");
			}
			// dashboard queries profiler ui
			initQueriesProfilerUI();
			log.info("OK init_Queries_Profiler_ui");

			// dashboard queries profiler ui
			initBaseLayers();
			log.info("OK init_BaseLayers");

			if (initSamples) {
				initQAWindTurbinesSample();
				log.info("OK init_QA_WindTurbines");

				initLayers();
				log.info("OK init_Layers");

				initViewers();
				log.info("OK init_Viewers");

				initRealms();
				log.info("OK initRealms");

			}

			initCategories();
			log.info("OK Categories");

			if (initSamples) {
				initTypology();
				log.info("OK Typologies");
				initTypologyDataset();
				log.info("OK Typologies-Dataset");

				// Dataclass_Dashboard
				initDataclassDashboard();
				log.info("OK init_Dataclass_Dashboard");

				initInternationalizationSample();
				log.info("OK initInternationalizationSample");

				initCrudAndImportSample();
				log.info("OK initCrudAndImportSample");

				// init_OntologyVirtualDatasource();
				// log.info(" OK init_OntologyVirtualDatasource");
				// init_realms();

				initWebProject();
				log.info("OK initWebProject");

				initApis();
				log.info("Init API");
			}
			if (initSamples) {
				initLineageRelations();
				log.info("OK initLineageRelations");
			}

			if (openDataPortal) {
				initSubscription();
				log.info("Init Subscription");
			}

			if (openDataPortal) {
				initOpenDataPortal();
				log.info("Init Open Data Portal ontologies and resources");
			}

			initPrestoConnections();
		}

	}

	private void initCrudAndImportSample() {
		initCrudAndImportGadgets();
		initCrudAndImportDashboard();
		initI18nResourcesCrudAndImport();

	}

	private void initCrudAndImportGadgets() {
		if (gadgetRepository.findByIdentification("gadget-crud-example") == null) {
			final Gadget gadget = new Gadget();
			gadget.setId(MASTER_GADGET_16);
			gadget.setIdentification("gadget-crud-example");
			gadget.setPublic(false);
			gadget.setInstance(true);
			gadget.setDescription("gadget-crud-example");
			gadget.setType(gadgetTemplateRepository.findById("MASTER-GadgetTemplate-7").orElse(null));
			gadget.setConfig(
					"{\"parameters\":{\"initialEntity\":\"\",\"typeGadget\":\"withWizard\",\"hideIdColumn\":false}}");
			gadget.setUser(getUserAdministrator());
			gadgetRepository.save(gadget);
		}
		if (gadgetRepository.findByIdentification("gadget-import-example") == null) {
			final Gadget gadget = new Gadget();
			gadget.setId(MASTER_GADGET_17);
			gadget.setIdentification("gadget-import-example");
			gadget.setPublic(false);
			gadget.setInstance(true);
			gadget.setDescription("gadget-import-example");
			gadget.setType(gadgetTemplateRepository.findById("MASTER-GadgetTemplate-8").orElse(null));
			gadget.setConfig("{\"parameters\":{\"initialEntity\":\"\"}}");
			gadget.setUser(getUserAdministrator());
			gadgetRepository.save(gadget);
		}
	}

	@Autowired
	private VerticalRepository verticalRepository;

	private void initDefaultVertical() {
		if (verticalRepository.findAll().isEmpty()) {
			final Vertical onesait = new Vertical();
			onesait.setName(Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME);
			onesait.setSchema(Tenant2SchemaMapper.DEFAULT_SCHEMA);
			Tenant development = new Tenant();
			development.setName(Tenant2SchemaMapper.defaultTenantName(
					Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())));
			development.setId("MASTER-Tenant-1");
			development = tenantRepository.save(development);
			development.getVerticals().add(onesait);
			onesait.getTenants().add(development);
			onesait.setId("MASTER-Vertical-1");
			verticalRepository.save(onesait);
		}
	}

	private void initMasterConfiguration() {
		if (masterConfigurationRepository.findByType(MasterConfiguration.Type.RTDB) == null) {
			final MasterConfiguration config = MasterConfiguration.builder()
					.description("RTDB Master configuration for Multitenancy").type(MasterConfiguration.Type.RTDB)
					.ymlConfig(loadFromResources("configurations/MultitenantRTDBConfiguration.yml")).build();
			masterConfigurationRepository.save(config);
		}
	}

	private @Autowired ApiRepository apiRepository;

	private void initOpenDataPortal() {
		initOpenDataPortalOntologies();
		log.info("Open Data Portal ontologies created");
		initOpenDataPortalResources();
		log.info("Open Data resources created");
	}

	private void initOpenDataPortalOntologies() {
		List<DataModel> dataModels;
		Ontology ontology;

		if (ontologyRepository.findByIdentification(AIRLINE_SAFETY_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-OpenData-1");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_airline_safety.json"));
			ontology.setDescription(
					"It contains the data behind the story: Should Travelers Avoid Flying Airlines That Have Had Crashes in the Past?");
			ontology.setIdentification(AIRLINE_SAFETY_STR);
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
				createUserAccess("MASTER-Ontology-OpenData-1-UserAccess", "QUERY", ontology, getUser());
			}
		}
		if (ontologyRepository.findByIdentification(INDONESIAN_CITIES_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-OpenData-2");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_indonesian_cities.json"));
			ontology.setDescription(
					"This is a exhaustive list of coordinates of Indonesian Cities which is hard to find. Acquired using google’s geocoding API.");
			ontology.setIdentification(INDONESIAN_CITIES_STR);
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
				createUserAccess("MASTER-Ontology-OpenData-2-UserAccess", "QUERY", ontology, getUser());
			}
		}
		if (ontologyRepository.findByIdentification(METEORITE_LANDINGS_STR) == null) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-OpenData-3");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_meteorite_landings.json"));
			ontology.setDescription(
					"This comprehensive data set from The Meteoritical Society contains information on all of the known meteorite landings.");
			ontology.setIdentification(METEORITE_LANDINGS_STR);
			ontology.setMetainf("imported,json");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
				createUserAccess("MASTER-Ontology-OpenData-3-UserAccess", "QUERY", ontology, getUser());
			}
		}
	}

	private void createUserAccess(String userAccessId, String type, Ontology ontology, User user) {
		final OntologyUserAccess userAccess = new OntologyUserAccess();
		userAccess.setId(userAccessId);
		userAccess.setOntology(ontology);
		final List<OntologyUserAccessType> queryAccess = ontologyUserAccessTypeRepository.findByName(type);
		if (!queryAccess.isEmpty()) {
			userAccess.setOntologyUserAccessType(queryAccess.get(0));
		}
		userAccess.setUser(user);
		ontologyUserAccessRepository.save(userAccess);
	}

	private void initOpenDataPortalResources() {
		DatasetResource newResource;
		if (resourceRepository.findResourceById("21ebc28f-b967-46e5-a8f6-0e977dee72fb") == null) {
			newResource = new DatasetResource();
			newResource.setId("21ebc28f-b967-46e5-a8f6-0e977dee72fb");
			newResource.setQuery("select * from airline_safety");
			newResource.setOntology(ontologyRepository.findByIdentification(AIRLINE_SAFETY_STR));
			newResource.setIdentification("Airline Safety");
			newResource.setUser(getUserDeveloper());
			resourceRepository.save(newResource);
		}
		if (resourceRepository.findResourceById("fd061918-4315-48e2-b716-18d84219a092") == null) {
			newResource = new DatasetResource();
			newResource.setId("fd061918-4315-48e2-b716-18d84219a092");
			newResource.setQuery("select * from indonesian_cities");
			newResource.setOntology(ontologyRepository.findByIdentification(INDONESIAN_CITIES_STR));
			newResource.setIdentification("Indonesian Cities");
			newResource.setUser(getUserDeveloper());
			resourceRepository.save(newResource);
		}
		if (resourceRepository.findResourceById("afcdc2e8-edb6-42f2-a6f6-8b2e630b4b34") == null) {
			newResource = new DatasetResource();
			newResource.setId("afcdc2e8-edb6-42f2-a6f6-8b2e630b4b34");
			newResource.setQuery("select * from meteorite_landings");
			newResource.setOntology(ontologyRepository.findByIdentification(METEORITE_LANDINGS_STR));
			newResource.setIdentification("Meteorite Landings");
			newResource.setUser(getUserDeveloper());
			resourceRepository.save(newResource);
		}

	}

	private void initSubscription() {
		if (subscriptionRepository.findByIdentification("ticketStatus").isEmpty()) {
			final Ontology ontology = ontologyRepository.findByIdentification(TICKET);
			final Subscription subscription = new Subscription();
			subscription.setIdentification("ticketStatus");
			subscription.setOntology(ontology);
			subscription.setProjection("$.Ticket.file");
			subscription.setQueryField("$.Ticket.status");
			subscription.setId("MASTER-Subscription-1");
			subscription.setQueryOperator("igual");
			subscription.setDescription("Subscription to Ticket status");
			subscription.setUser(userDeveloper);

			subscriptionRepository.save(subscription);
		}
	}

	private void initApis() {
		final String vueProjectApi = "project";
		final String projectOntology = "Project";
		if (apiRepository.findByIdentificationAndNumversion(vueProjectApi, 1) == null) {
			if (ontologyRepository.findByIdentification(projectOntology) == null) {
				final Ontology o = new Ontology();
				o.setIdentification(projectOntology);
				o.setDataModel(dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0));
				o.setDescription("Project ontology for Vue JS + SB 2 example");
				o.setJsonSchema(loadFromResources("datamodels/DataModel_Project.json"));
				o.setUser(userDeveloper);
				o.setId("MASTER-Ontology-35");
				o.setMetainf("project,example,vue,springboot");
				o.setPublic(true);
				ontologyRepository.save(o);
			}

			Api api = new Api();
			api.setIdentification(vueProjectApi);
			api.setApiType(ApiType.INTERNAL_ONTOLOGY);
			api.setCategory(ApiCategories.ALL);
			api.setDescription("Vue JS + SB 2 API");
			api.setNumversion(1);
			api.setPublic(true);
			api.setState(ApiStates.DEVELOPMENT);
			api.setUser(userDeveloper);
			api.setOntology(ontologyRepository.findByIdentification(projectOntology));
			api.setId("MASTER-Api-1");
			api = apiRepository.save(api);

			ApiOperation operation = new ApiOperation();
			operation.setIdentification("project_PUT");
			operation.setPath(PATH_ID);
			operation.setDescription("edit");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.PUT);
			operation.setApi(api);
			ApiQueryParameter body = new ApiQueryParameter();
			body.setName("body");
			body.setApiOperation(operation);
			body.setDataType(DataType.STRING);
			body.setHeaderType(HeaderType.BODY);
			body.setDescription("");
			body.setValue("");
			ApiQueryParameter id = new ApiQueryParameter();
			id.setName("id");
			id.setApiOperation(operation);
			id.setDataType(DataType.STRING);
			id.setHeaderType(HeaderType.PATH);
			id.setDescription("");
			id.setValue("");
			operation.getApiqueryparameters().addAll(Arrays.asList(id, body));
			api.getApiOperations().add(operation);

			operation = new ApiOperation();
			operation.setIdentification("project_POST");
			operation.setPath("/");
			operation.setDescription("post");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.POST);
			operation.setApi(api);
			body = new ApiQueryParameter();
			body.setName("body");
			body.setApiOperation(operation);
			body.setDataType(DataType.STRING);
			body.setHeaderType(HeaderType.BODY);
			body.setDescription("");
			body.setValue("");
			operation.getApiqueryparameters().add(body);
			api.getApiOperations().add(operation);

			operation = new ApiOperation();
			operation.setIdentification("project_GET");
			operation.setPath(PATH_ID);
			operation.setDescription("getbyid");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.GET);
			operation.setApi(api);
			id = new ApiQueryParameter();
			id.setName("id");
			id.setApiOperation(operation);
			id.setDataType(DataType.STRING);
			id.setHeaderType(HeaderType.PATH);
			id.setDescription("");
			id.setValue("");
			operation.getApiqueryparameters().add(id);
			api.getApiOperations().add(operation);

			operation = new ApiOperation();
			operation.setIdentification("project_GETAll");
			operation.setPath("");
			operation.setDescription("all");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.GET);
			operation.setApi(api);
			api.getApiOperations().add(operation);

			operation = new ApiOperation();
			operation.setIdentification("project_DELETEID");
			operation.setPath(PATH_ID);
			operation.setDescription("delete");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.DELETE);
			operation.setApi(api);
			id = new ApiQueryParameter();
			id.setName("id");
			id.setApiOperation(operation);
			id.setDataType(DataType.STRING);
			id.setHeaderType(HeaderType.PATH);
			id.setDescription("");
			id.setValue("");
			operation.getApiqueryparameters().add(id);
			api.getApiOperations().add(operation);

			operation = new ApiOperation();
			operation.setIdentification("name");
			operation.setPath("name/{pname}");
			operation.setDescription("filter by name");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.GET);
			operation.setApi(api);
			operation.setPostProcess("// data is a string variable containing the query output \n"
					+ "var dataArray = JSON.parse(data);\n" + "try{\n" + "  var instance = dataArray[0];\n"
					+ "  instance.id = instance._id;\n" + "  delete instance._id;\n"
					+ "  return JSON.stringify(instance);\n" + "}catch(error){ return data;}\n"
					+ "// A string result must be returned\n" + "return (JSON.stringify(dataArray));");
			ApiQueryParameter targetDb = new ApiQueryParameter();
			targetDb.setApiOperation(operation);
			targetDb.setCondition(CONSTANT_STR);
			targetDb.setDataType(DataType.STRING);
			targetDb.setHeaderType(HeaderType.QUERY);
			targetDb.setValue("rtdb");
			targetDb.setName("targetdb");
			targetDb.setDescription("");
			ApiQueryParameter queryType = new ApiQueryParameter();
			queryType.setApiOperation(operation);
			queryType.setCondition(CONSTANT_STR);
			queryType.setDataType(DataType.STRING);
			queryType.setHeaderType(HeaderType.QUERY);
			queryType.setValue("sql");
			queryType.setName("queryType");
			queryType.setDescription("");
			ApiQueryParameter query = new ApiQueryParameter();
			query.setApiOperation(operation);
			query.setCondition(CONSTANT_STR);
			query.setDataType(DataType.STRING);
			query.setHeaderType(HeaderType.QUERY);
			query.setValue("select p from Project as p where p.features.name={$pname}");
			query.setName(QUERY);
			query.setDescription("");
			ApiQueryParameter parameter = new ApiQueryParameter();
			parameter.setApiOperation(operation);
			parameter.setCondition("REQUIRED");
			parameter.setDataType(DataType.STRING);
			parameter.setHeaderType(HeaderType.PATH);
			parameter.setValue(null);
			parameter.setName("pname");
			parameter.setDescription("");
			operation.getApiqueryparameters().addAll(Arrays.asList(parameter, query, queryType, targetDb));
			api.getApiOperations().add(operation);

			operation = new ApiOperation();
			operation.setIdentification("id");
			operation.setPath("id/{name}");
			operation.setDescription("filter by name");
			operation.setOperation(com.minsait.onesait.platform.config.model.ApiOperation.Type.GET);
			operation.setPostProcess("// data is a string variable containing the query output \n"
					+ "var dataArray = JSON.parse(data);\n" + "try{\n" + "  var instance = dataArray[0];\n"
					+ "  instance.id = instance._id;\n" + "  delete instance._id;\n"
					+ "  return JSON.stringify(instance);\n" + "}catch(error){ return data;}\n"
					+ "// A string result must be returned\n" + "return (JSON.stringify(dataArray));");
			operation.setApi(api);
			query = new ApiQueryParameter();
			query.setApiOperation(operation);
			query.setCondition(CONSTANT_STR);
			query.setDataType(DataType.STRING);
			query.setHeaderType(HeaderType.QUERY);
			query.setValue("select p from Project as p where p.features.name={$name}");
			query.setName(QUERY);
			query.setDescription("");
			parameter = new ApiQueryParameter();
			parameter.setApiOperation(operation);
			parameter.setCondition("REQUIRED");
			parameter.setDataType(DataType.STRING);
			parameter.setHeaderType(HeaderType.PATH);
			parameter.setValue(null);
			parameter.setName("name");
			parameter.setDescription("");
			targetDb = new ApiQueryParameter();
			targetDb.setApiOperation(operation);
			targetDb.setCondition(CONSTANT_STR);
			targetDb.setDataType(DataType.STRING);
			targetDb.setHeaderType(HeaderType.QUERY);
			targetDb.setValue("rtdb");
			targetDb.setName("targetdb");
			targetDb.setDescription("");
			queryType = new ApiQueryParameter();
			queryType.setApiOperation(operation);
			queryType.setCondition(CONSTANT_STR);
			queryType.setDataType(DataType.STRING);
			queryType.setHeaderType(HeaderType.QUERY);
			queryType.setValue("sql");
			queryType.setName("queryType");
			queryType.setDescription("");
			operation.getApiqueryparameters().addAll(Arrays.asList(parameter, query, queryType, targetDb));
			api.getApiOperations().add(operation);

			apiRepository.save(api);

		}
	}

	private void initBaseLayers() {

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

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-5");
		baseLayer.setIdentification("osm.Mapnik.Labels.cs2");
		baseLayer.setName("Open Street Maps");
		baseLayer.setTechnology(CESIUM2);
		baseLayer.setUrl("https://a.tile.openstreetmap.org/");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-6");
		baseLayer.setIdentification("esri.Topo.Labels.cs2");
		baseLayer.setName("ESRI World Topo Map");
		baseLayer.setTechnology(CESIUM2);
		baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Topo_Map/MapServer");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-7");
		baseLayer.setIdentification("esri.Streets.Labels.cs2");
		baseLayer.setName("ESRI World Street Map");
		baseLayer.setTechnology(CESIUM2);
		baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Street_Map/MapServer");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-8");
		baseLayer.setIdentification("esri.Imagery.NoLabels.cs2");
		baseLayer.setName("ESRI Imagery");
		baseLayer.setTechnology(CESIUM2);
		baseLayer.setUrl("https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-9");
		baseLayer.setIdentification("esri.Gray.NoLabels");
		baseLayer.setName("ESRI Light Gray");
		baseLayer.setTechnology(CESIUM);
		baseLayer.setUrl("https://server.arcgisonline.com/arcgis/rest/services/Canvas/World_Light_Gray_Base/MapServer");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-10");
		baseLayer.setIdentification("esri.lightGray.cs2");
		baseLayer.setName("ESRI Light Gray");
		baseLayer.setTechnology(CESIUM2);
		baseLayer.setUrl("https://server.arcgisonline.com/arcgis/rest/services/Canvas/World_Light_Gray_Base/MapServer");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-11");
		baseLayer.setIdentification("esri.DarkGray.NoLabels");
		baseLayer.setName("ESRI Dark Gray");
		baseLayer.setTechnology(CESIUM);
		baseLayer.setUrl("https://server.arcgisonline.com/arcgis/rest/services/Canvas/World_Dark_Gray_Base/MapServer");

		baseLayerRepository.save(baseLayer);

		baseLayer = new BaseLayer();
		baseLayer.setId("MASTER-BaseLayer-12");
		baseLayer.setIdentification("esri.darkGray.cs2");
		baseLayer.setName("ESRI Dark Gray");
		baseLayer.setTechnology(CESIUM2);
		baseLayer.setUrl("https://server.arcgisonline.com/arcgis/rest/services/Canvas/World_Dark_Gray_Base/MapServer");

		baseLayerRepository.save(baseLayer);

	}

	private void initRealms() {

		if (appRepository.findById("MASTER-Realm-1").orElse(null) == null) {
			final App app = new App();
			app.setId("MASTER-Realm-2");
			app.setIdentification("platformCenter");
			app.setDescription("This is a realm provided for the Platform Center Console");
			app.setUser(getUserDeveloper());
			app.setSecret("changeIt2020");
			AppRole role = new AppRole();
			role.setApp(app);
			role.setDescription("Administrator");
			role.setName("ADMINISTRATOR");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Consultant");
			role.setName("CONSULTANT");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Developer");
			role.setName("DEVELOPER");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Product Owner");
			role.setName("PRODUCT_OWNER");
			app.getAppRoles().add(role);
			role = new AppRole();
			role.setApp(app);
			role.setDescription("Project ManagerProject Manager");
			role.setName("PROJECT_MANAGER");
			app.getAppRoles().add(role);

			appRepository.save(app);
		}
	}

	private void initSimulations() {
		ClientPlatformInstanceSimulation simulation = simulationRepository.findById("MASTER-DeviceSimulation-1")
				.orElse(null);
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

	public void initQueriesProfilerUI() {
		initGadgetDatasourcesQueriesProfilerUI();
		initDashboardQueriesProfilerUI();

	}

	public void initQAWindTurbinesSample() {
		initOntologyQAWindTurbines();
		initDashboardQAWindTurbines();
		initGadgetQAWindTurbines();
		initGadgetDatasourceQAWindTurbines();
		initGadgetMeasureQAWindTurbines();
	}

	public void initDataclassDashboard() {
		initGadgetDatasourcesDataclass();
		initDashboardDataclass();
	}

	public void initInternationalizationSample() {
		initDashboardInternationalizations();
		log.info("OK init_DashboardInternationalizations");
		initI18nResources();
		log.info("OK init_I18nResources");
	}

	private void initFlowDomain() {
		log.info("init_FlowDomain");
		// Domain for administrator
		if (MultitenancyContextHolder.getVerticalSchema().equals(Tenant2SchemaMapper.DEFAULT_SCHEMA)
				&& domainRepository.count() == 0) {
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

	private void initConfiguration() throws GenericOPException {
		log.info("init_Configuration");
		if (configurationRepository.count() == 0) {

			Configuration config = new Configuration();
			config.setIdentification("Twitter");
			config.setId("MASTER-Configuration-1");
			config.setType(Configuration.Type.TWITTER);
			config.setUser(getUserAdministrator());
			config.setDescription(TWITTER);
			config.setEnvironment("dev");
			config.setYmlConfig(loadFromResources("configurations/TwitterConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setIdentification("Twitter");
			config.setId("MASTER-Configuration-2");
			config.setType(Configuration.Type.TWITTER);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setIdentification("lmgracia");
			config.setDescription(TWITTER);
			config.setYmlConfig(loadFromResources("configurations/TwitterConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setIdentification("Scheduler");
			config.setId("MASTER-Configuration-3");
			config.setType(Configuration.Type.SCHEDULING);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("RtdbMaintainer config");
			config.setYmlConfig(loadFromResources("configurations/SchedulingConfiguration_default.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setIdentification("PlatformModules");
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
			config.setIdentification("PlatformModules");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DOCKER);
			config.setDescription("Endpoints docker profile");

			config.setYmlConfig(
					replaceEnvironment(loadFromResources("configurations/EndpointModulesConfigurationDocker.yml")));

			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setIdentification("Email");
			config.setId("MASTER-Configuration-6");
			config.setType(Configuration.Type.MAIL);
			config.setUser(getUserAdministrator());
			config.setDescription("Mail Config");
			config.setEnvironment(DEFAULT);
			if (loadMailConfig) {
				config.setYmlConfig(loadFromResources("configurations/MailConfiguration.yml"));
			} else {
				config.setYmlConfig(loadFromResources("configurations/MailConfigurationDefault.yml"));
			}
			configurationRepository.save(config);

			//
			config = new Configuration();
			config.setId("MASTER-Configuration-8");
			config.setIdentification("Monitoring");
			config.setType(Configuration.Type.MONITORING);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Spring Boot Admin Config");
			config.setYmlConfig(loadFromResources("configurations/MonitoringConfiguration.yml"));
			configurationRepository.save(config);

			//
			config = new Configuration();
			config.setId("MASTER-Configuration-10");
			config.setIdentification("Openshift");
			config.setType(Configuration.Type.OPENSHIFT);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Openshift configuration");
			config.setYmlConfig(loadFromResources("configurations/OpenshiftConfiguration.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setIdentification("Rancher");
			config.setId("MASTER-Configuration-11");
			config.setType(Configuration.Type.DOCKER);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Rancher docker compose configuration");
			config.setIdentification("Rancher");
			config.setYmlConfig(loadFromResources("configurations/DockerCompose_Rancher.yml"));
			configurationRepository.save(config);
			//
			config = new Configuration();
			config.setId("MASTER-Configuration-12");
			config.setType(Configuration.Type.NGINX);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Nginx conf template");
			config.setIdentification("Nginx");
			config.setYmlConfig(loadFromResources("configurations/nginx-template.conf"));
			configurationRepository.save(config);

			config = configurationRepository.findByTypeAndEnvironment(Type.ENDPOINT_MODULES, DEFAULT);
			config = new Configuration();
			config.setId("MASTER-Configuration-27");
			config.setIdentification("BillableModules");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("BillableModules for local environment");
			config.setYmlConfig(loadFromResources("configurations/BillableModulesDefault.json"));
			configurationRepository.save(config);

			config = configurationRepository.findByTypeAndEnvironment(Type.ENDPOINT_MODULES, DOCKER);
			config = new Configuration();
			config.setId("MASTER-Configuration-28");
			config.setIdentification("BillableModules");
			config.setType(Configuration.Type.ENDPOINT_MODULES);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DOCKER);
			config.setDescription("BillableModules for docker environment");
			config.setYmlConfig(loadFromResources("configurations/BillableModulesDocker.json"));
			configurationRepository.save(config);
		}

		Configuration config = configurationRepository.findByTypeAndEnvironment(Type.LINEAGE, DEFAULT);
		if (config == null) {
			config = new Configuration();
			config.setId("MASTER-Configuration-24");
			config.setIdentification("Lineage");
			config.setType(Configuration.Type.LINEAGE);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Lineage Config");
			config.setYmlConfig(loadFromResources("configurations/LineageConfiguration.json"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.CUSTOM, DEFAULT, "ADMIN_PANEL");
		if (config == null) {
			config = new Configuration();
			config.setIdentification("ADMIN_PANEL");
			config.setDescription("Administrator Panel Configuration");
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-25");
			config.setType(Type.CUSTOM);
			config.setUser(getUserAdministrator());
			config.setYmlConfig(loadFromResources("configurations/AdminPanelConfiguration_default.yml"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, DEFAULT);
		if (config == null) {
			config = new Configuration();
			config.setIdentification("Platform");
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
			config.setIdentification("Platform");
			config.setDescription("onesait Platform global configuration");
			config.setEnvironment(DOCKER);
			config.setId("MASTER-Configuration-14");
			config.setType(Type.OPEN_PLATFORM);
			config.setUser(getUserAdministrator());
			final String yml = loadFromResources("configurations/OpenPlatformConfiguration_docker.yml");
			if (yml == null) {
				throw new GenericOPException("Null yaml from OpenPlatformConfiguration.yml");
			}
			config.setYmlConfig(applyCustomConfig(yml));

			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.DOCKER, DEFAULT,
				MICROSERVICE_STR);
		if (config == null) {
			config = new Configuration();
			config.setDescription(MICROSERVICE_STR);
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-16");
			config.setType(Type.DOCKER);
			config.setUser(getUserAdministrator());
			config.setIdentification(MICROSERVICE_STR);
			config.setYmlConfig(loadFromResources("configurations/Microservice-compose.yml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.JENKINS, DEFAULT,
				"IOT_CLIENT_ARCHETYPE");
		if (config == null) {
			config = new Configuration();
			config.setDescription(PIPELINE_DESCRIPTION);
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-17");
			config.setType(Type.JENKINS);
			config.setUser(getUserAdministrator());
			config.setIdentification("IOT_CLIENT_ARCHETYPE");
			config.setYmlConfig(loadFromResources("configurations/JenkinsXMLTemplateIoT.xml"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.JENKINS, DEFAULT,
				"ML_MODEL_ARCHETYPE");
		if (config == null) {
			config = new Configuration();
			config.setDescription(PIPELINE_DESCRIPTION);
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-18");
			config.setType(Type.JENKINS);
			config.setUser(getUserAdministrator());
			config.setIdentification("ML_MODEL_ARCHETYPE");
			config.setYmlConfig(loadFromResources("configurations/JenkinsXMLTemplateML.xml"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.JENKINS, DEFAULT,
				"NOTEBOOK_ARCHETYPE");
		if (config == null) {
			config = new Configuration();
			config.setDescription(PIPELINE_DESCRIPTION);
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-20");
			config.setType(Type.JENKINS);
			config.setUser(getUserAdministrator());
			config.setIdentification("NOTEBOOK_ARCHETYPE");
			config.setYmlConfig(loadFromResources("configurations/JenkinsXMLTemplateNaaS.xml"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironment(Type.GOOGLE_ANALYTICS, DEFAULT);
		if (config == null) {
			config = new Configuration();
			config.setIdentification("Google");
			config.setId("MASTER-Configuration-19");
			config.setType(Configuration.Type.GOOGLE_ANALYTICS);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Google Analytics Configuration");
			config.setYmlConfig(loadFromResources("configurations/GoogleAnalyticsConfiguration.yml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironment(Type.EXPIRATIONUSERS, DEFAULT);
		if (config == null) {
			config = new Configuration();
			config.setIdentification("ExpirationUserPass");
			config.setId("MASTER-Configuration-21");
			config.setType(Configuration.Type.EXPIRATIONUSERS);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Expiration Users config");
			config.setYmlConfig(loadFromResources("configurations/ExpirationUsersPass_default.yml"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findById("MASTER-Configuration-22").orElse(null);
		if (config == null) {
			config = new Configuration();
			config.setIdentification("JsonSqlEngine");
			config.setId("MASTER-Configuration-22");
			config.setType(Type.SQLENGINE);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Json dictionary config file for SQL Engine");
			config.setYmlConfig(loadFromResources("configurations/JSONSqlEngine_default.json"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findById("MASTER-Configuration-23").orElse(null);
		if (config == null) {
			config = new Configuration();
			config.setIdentification("JavaSqlEngine");
			config.setId("MASTER-Configuration-23");
			config.setType(Type.SQLENGINE);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Java class util file for SQL Engine");
			config.setYmlConfig(loadFromResources("configurations/JavaUtilSqlEngine_default.java"));
			configurationRepository.save(config);
		}
		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.KAFKA_PROPERTIES, DEFAULT,
				"Kafka Client Properties");
		if (config == null) {
			config = new Configuration();
			config.setDescription("Kafka connection and topic properties for clients.");
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-29");
			config.setType(Type.KAFKA_PROPERTIES);
			config.setUser(getUserAdministrator());
			config.setIdentification("Kafka Client Properties");
			config.setYmlConfig(loadFromResources("configurations/kafkaProperties.json"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findById("MASTER-Configuration-30").orElse(null);
		if (config == null) {
			config = new Configuration();
			config.setId("MASTER-Configuration-30");
			config.setIdentification("GeneralDataClass");
			config.setType(Configuration.Type.DATACLASS);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Data class with general rules");
			config.setYmlConfig(loadFromResources("configurations/GeneralDataClass.yml"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.PRESTO_PROPERTIES, DEFAULT,
				"Presto Connection Properties");
		if (config == null) {
			config = new Configuration();
			config.setDescription("Presto connection properties.");
			config.setEnvironment(DEFAULT);
			config.setId("MASTER-Configuration-31");
			config.setType(Type.PRESTO_PROPERTIES);
			config.setUser(getUserAdministrator());
			config.setIdentification("Presto Connection Properties");
			config.setYmlConfig(loadFromResources("configurations/PrestoProperties.json"));
			configurationRepository.save(config);
		}

		config = configurationRepository.findById("MASTER-Configuration-32").orElse(null);
		if (config == null) {
			config = new Configuration();
			config.setId("MASTER-Configuration-32");
			config.setIdentification("MapsProjectConfiguration");
			config.setType(Configuration.Type.MAPS_PROJECT);
			config.setUser(getUserAdministrator());
			config.setEnvironment(DEFAULT);
			config.setDescription("Urls and descriptions for viewers");
			config.setYmlConfig(loadFromResources("configurations/MapsProjectConfiguration.yml"));
			configurationRepository.save(config);
		}

	}

	public void initClientPlatformOntology() {

		log.info("init ClientPlatformOntology");
		final List<ClientPlatformOntology> cpos = clientPlatformOntologyRepository.findAll();
		if (cpos.isEmpty()) {
			if (clientPlatformRepository.findAll().isEmpty()) {
				throw new GenericRuntimeOPException("There must be at least a ClientPlatform with id=1 created");
			}
			if (ontologyRepository.findAll().isEmpty()) {
				throw new GenericRuntimeOPException("There must be at least a Ontology with id=1 created");
			}
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

		ClientPlatform client = new ClientPlatform();
		if (!clientPlatformRepository.findById("MASTER-ClientPlatform-1").isPresent()) {

			client.setId("MASTER-ClientPlatform-1");
			client.setUser(getUserDeveloper());
			client.setIdentification("Client-MasterData");
			client.setEncryptionKey("b37bf11c-631e-4bc4-ae44-910e58525952");
			client.setDescription("ClientPatform created as MasterData");
			clientPlatformRepository.save(client);
		}

		if (!clientPlatformRepository.findById("MASTER-ClientPlatform-2").isPresent()) {
			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-2");
			client.setUser(getUserDeveloper());
			client.setIdentification(GTKPEXAMPLE_STR);
			client.setEncryptionKey("f9dfe72e-7082-4fe8-ba37-3f569b30a691");
			client.setDescription("ClientPatform created as Example");
			clientPlatformRepository.save(client);
		}

		if (!clientPlatformRepository.findById("MASTER-ClientPlatform-3").isPresent()) {
			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-3");
			client.setUser(getUserDeveloper());
			client.setIdentification(TICKETING_APP);
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Platform client for issues and ticketing");
			clientPlatformRepository.save(client);
		}
		if (!clientPlatformRepository.findById("MASTER-ClientPlatform-4").isPresent()) {
			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-4");
			client.setUser(getUserDeveloper());
			client.setIdentification("DeviceMaster");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Device template for testing");
			clientPlatformRepository.save(client);
		}
		if (!clientPlatformRepository.findById("MASTER-ClientPlatform-5").isPresent()) {
			client = new ClientPlatform();
			client.setId("MASTER-ClientPlatform-5");
			client.setUser(getUserAnalytics());
			client.setIdentification("DefaultClient");
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
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role ADMIN");
		}
		try {
			log.info("Adding menu for role DEVELOPER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-2");
			menu.setJson(loadFromResources("menu/menu_developer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DEVELOPER");
		}
		try {
			log.info("Adding menu for role USER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-3");

			menu.setJson(loadFromResources("menu/menu_user.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_USER.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role USER");
		}
		try {
			log.info("Adding menu for role ANALYTIC");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-4");
			menu.setJson(loadFromResources("menu/menu_analytic.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role ANALYTIC");
		}
		try {
			log.info("Adding menu for role DATAVIEWER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-5");
			menu.setJson(loadFromResources("menu/menu_dataviewer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DATAVIEWER");
		}
		try {
			log.info("Adding menu for role PLATFORM_ADMINISTRATOR");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-6");
			menu.setJson(loadFromResources("menu/menu_platform_admin.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_PLATFORM_ADMIN.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DATAVIEWER");
		}
		try {
			log.info("Adding menu for role DEVOPS");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-7");

			menu.setJson(loadFromResources("menu/menu_devops.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DEVOPS.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role DEVOPS");
		}
		try {
			log.info("Adding menu for role PARTNER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-8");

			menu.setJson(loadFromResources("menu/menu_partner.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_PARTNER.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role PARTNER");
		}
		try {
			log.info("Adding menu for role OPERATIONS");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-9");

			menu.setJson(loadFromResources("menu/menu_operations.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_OPERATIONS.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role OPERATIONS");
		}
		try {
			log.info("Adding menu for role SYS_ADMIN");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-10");

			menu.setJson(loadFromResources("menu/menu_sys_admin.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_SYS_ADMIN.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role SYS_ADMIN");
		}
		try {
			log.info("Adding menu for role EDGE_USER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-11");

			menu.setJson(loadFromResources("menu/menu_edge_user.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_EDGE_USER.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role SYS_ADMIN");
		}
		try {
			log.info("Adding menu for role EDGE_USER");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-12");

			menu.setJson(loadFromResources("menu/menu_edge_developer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_EDGE_DEVELOPER.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role EDGE_DEVELOPER");
		}
		try {
			log.info("Adding menu for role EDGE_ADMINISTRATOR");

			final ConsoleMenu menu = new ConsoleMenu();
			menu.setId("MASTER-ConsoleMenu-13");

			menu.setJson(loadFromResources("menu/menu_edge_admin.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_EDGE_ADMINISTRATOR.toString()).orElse(null));
			consoleMenuRepository.save(menu);
		} catch (final Exception e) {
			log.error("Error adding menu for role EDGE_ADMINISTRATOR");
		}
	}

	public void initConsoleMenuRollBack() {
		log.info("init ConsoleMenuRollBack");
		try {
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById("MASTER-ConsoleMenu-1");
			if (!opt.isPresent()) {
				throw new GenericOPException(MENU_NOT_FOUND);
			}
			final ConsoleMenu menu = opt.get();

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
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById("MASTER-ConsoleMenu-2");
			if (!opt.isPresent()) {
				throw new GenericOPException(MENU_NOT_FOUND);
			}
			final ConsoleMenu menu = opt.get();

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
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById("MASTER-ConsoleMenu-3");
			if (!opt.isPresent()) {
				throw new GenericOPException(MENU_NOT_FOUND);
			}
			final ConsoleMenu menu = opt.get();

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
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById("MASTER-ConsoleMenu-4");
			if (!opt.isPresent()) {
				throw new GenericOPException(MENU_NOT_FOUND);
			}
			final ConsoleMenu menu = opt.get();
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
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById("MASTER-ConsoleMenu-5");
			if (!opt.isPresent()) {
				throw new GenericOPException(MENU_NOT_FOUND);
			}
			final ConsoleMenu menu = opt.get();

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
		try {
			final Optional<ConsoleMenu> opt = consoleMenuRepository.findById("MASTER-ConsoleMenu-6");
			if (!opt.isPresent()) {
				throw new GenericOPException(MENU_NOT_FOUND);
			}
			final ConsoleMenu menu = opt.get();

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
			if (yamlSt == null) {
				throw new GenericRuntimeOPException("YAML is null");
			}
			return yamlSt.replace("${SERVER_NAME}", serverName).replace("${SERVER_BASE_DOMAIN_MINIO_AUTH}",
					minioCookiedomain);
		} catch (final Exception e) {
			log.error("Error replacing environment: " + serverName + ".On endpoint configuration file");
			log.error(e.getMessage());
			throw new GenericRuntimeOPException(e);
		}
	}

	private String applyCustomConfig(String yamlSt) {
		try {
			return yamlSt.replace("${REALTIMEDBSERVERS}", rtdbServers)
					.replace("${RTDB_EXECUTION_TIMEOUT}", rtdbExecutionTimeout)
					.replace("${RTDB_QUERIES_LIMIT}", rtdbQueriesLimit)
					.replace("${MONGO_SOCKET_TIMEOUT}", rtdbSocketTimeout)
					.replace("${MONGO_CONNECT_TIMEOUT}", rtdbConnectionTimeout)
					.replace("${MONGO_WAIT_TIME}", rtdbWaitTime).replace("${MONGO_POOL_SIZE}", rtdbPoolSize)
					.replace("${REALTIMEDBNAME}", rtdbDatabase).replace("${REALTIMEDBWRITECONCERN}", rtdbWriteConcern)
					.replace("${MONGO_SSL_ENABLED}", rtdbSslEnabled).replace("${MONGO_USE_QUASAR}", rtdbUseQuasar);
		} catch (final Exception e) {
			log.error("Error replacing RTDB servers: " + rtdbServers + ".On endpoint configuration file");
			log.error(e.getMessage());
			return yamlSt;
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
			/*
			 * final DashboardConf dashboardConfNoTitleEcharts = new DashboardConf(); final
			 * String notitleechartsSchema =
			 * "{\"header\":{\"title\":\"\",\"enable\":false,\"height\":0,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			 * dashboardConfNoTitleEcharts.setId("MASTER-DashboardConf-3");
			 * dashboardConfNoTitleEcharts.setIdentification("notitleecharts");
			 * dashboardConfNoTitleEcharts.setModel(notitleechartsSchema);
			 * dashboardConfNoTitleEcharts.setHeaderlibs(" <!-- ECHARTS -->\r\n" +
			 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.common.min.js\"></script>\r\n"
			 * +
			 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.min.js\"></script>\r\n"
			 * +
			 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/bmap.min.js\"></script>\r\n"
			 * +
			 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/dataTool.min.js\"></script>\r\n"
			 * + " 	<!-- ECHARTS -->\r\n" + "    <!-- DATA TABLE -->\r\n" +
			 * "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css\"/>\r\n"
			 * +
			 * "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/responsive/2.2.3/css/responsive.dataTables.min.css\"/>\r\n"
			 * +
			 * "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/buttons/1.5.6/css/buttons.dataTables.min.css\"/>\r\n"
			 * +
			 * " 	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/jquery.dataTables.min.js\"></script>\r\n"
			 * +
			 * "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/dataTables.bootstrap4.min.js\"></script>\r\n"
			 * +
			 * "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/responsive/2.2.3/js/dataTables.responsive.min.js\"></script>\r\n"
			 * +
			 * "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/dataTables.buttons.min.js\"></script>\r\n"
			 * +
			 * "    <script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/buttons.colVis.min.js\"></script>\r\n"
			 * + "     <!-- DATA TABLE -->"); dashboardConfNoTitleEcharts
			 * .setDescription("No title style with ECharts libraries, and Datatable libraries"
			 * ); dashboardConfRepository.save(dashboardConfNoTitleEcharts);
			 */

		}
		if (!dashboardConfRepository.existsById("MASTER-DashboardConf-4")) {
			final DashboardConf dashboardConfSynoptic = new DashboardConf();
			final String synopticSchema = "{\"header\":{\"title\":\" \",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":false},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":false,\"mobileBreakpoint\":640,\"minCols\":299,\"maxCols\":301,\"minRows\":299,\"maxRows\":301,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":40,\"defaultItemRows\":40,\"fixedColWidth\":2,\"fixedRowHeight\":20,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConfSynoptic.setId("MASTER-DashboardConf-4");
			dashboardConfSynoptic.setIdentification("fixed");
			dashboardConfSynoptic.setModel(synopticSchema);
			dashboardConfSynoptic.setDescription("Fixed style");
			dashboardConfRepository.save(dashboardConfSynoptic);
		}
		/*
		 * if (!dashboardConfRepository.existsById("MASTER-DashboardConf-5")) { final
		 * DashboardConf dashboardConfNoTitleEcharts = new DashboardConf(); final String
		 * notitleechartsSchema =
		 * "{\"header\":{\"title\":\"\",\"enable\":false,\"height\":0,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"navigation\":{\"showBreadcrumbIcon\":false,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":299,\"maxCols\":301,\"minRows\":299,\"maxRows\":301,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":40,\"defaultItemRows\":40,\"fixedColWidth\":2,\"fixedRowHeight\":20,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[]}}";
		 * dashboardConfNoTitleEcharts.setId("MASTER-DashboardConf-5");
		 * dashboardConfNoTitleEcharts.setIdentification("notitleechartsfixed");
		 * dashboardConfNoTitleEcharts.setModel(notitleechartsSchema);
		 * dashboardConfNoTitleEcharts.setHeaderlibs(" <!-- ECHARTS -->\r\n" +
		 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.common.min.js\"></script>\r\n"
		 * +
		 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/echarts.min.js\"></script>\r\n"
		 * +
		 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/bmap.min.js\"></script>\r\n"
		 * +
		 * "    <script type=\"text/javascript\" src=\"https://cdnjs.cloudflare.com/ajax/libs/echarts/4.2.1/extension/dataTool.min.js\"></script>\r\n"
		 * + " 	<!-- ECHARTS -->\r\n" + "    <!-- DATA TABLE -->\r\n" +
		 * "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css\"/>\r\n"
		 * +
		 * "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/responsive/2.2.3/css/responsive.dataTables.min.css\"/>\r\n"
		 * +
		 * "    <link rel=\"stylesheet\" href=\"https://cdn.datatables.net/buttons/1.5.6/css/buttons.dataTables.min.css\"/>\r\n"
		 * +
		 * " 	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/jquery.dataTables.min.js\"></script>\r\n"
		 * +
		 * "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/1.10.18/js/dataTables.bootstrap4.min.js\"></script>\r\n"
		 * +
		 * "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/responsive/2.2.3/js/dataTables.responsive.min.js\"></script>\r\n"
		 * +
		 * "	<script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/dataTables.buttons.min.js\"></script>\r\n"
		 * +
		 * "    <script type=\"text/javascript\" src=\"https://cdn.datatables.net/buttons/1.5.6/js/buttons.colVis.min.js\"></script>\r\n"
		 * + "     <!-- DATA TABLE -->"); dashboardConfNoTitleEcharts
		 * .setDescription("No title style with ECharts libraries, and Datatable libraries"
		 * ); dashboardConfRepository.save(dashboardConfNoTitleEcharts); }
		 */
		if (!dashboardConfRepository.existsById("MASTER-DashboardConf-0")) {
			final DashboardConf dashboardConf = new DashboardConf();
			final String defaultNewSchema = "{\"header\":{\"title\":\"My Dashboard\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"showfavoritesg\":true,\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":20,\"minRows\":20,\"maxRows\":20,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":1,\"defaultItemRows\":1,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":true,\"pushItems\":false,\"disablePushOnDrag\":true,\"disablePushOnResize\":true,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"onDrag&Resize\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":false,\"enableEmptyCellAlign\":true,\"disableLiveResize\":true,\"disableLiveMove\":true,\"dragGadgetType\":\"livehtml\",\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConf.setId("MASTER-DashboardConf-0");
			dashboardConf.setIdentification("Default Style from 2.2");
			dashboardConf.setModel(defaultNewSchema);
			dashboardConf.setDescription("Style with swap, shadow, drawdrag and element align");
			dashboardConfRepository.save(dashboardConf);
		}
		if (!dashboardConfRepository.existsById("MASTER-DashboardConf-6")) {
			final DashboardConf dashboardConf = new DashboardConf();
			final String defaultNewSchema = "{\"header\":{\"title\":\"My Dashboard\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"showfavoritesg\":true,\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":20,\"minRows\":20,\"maxRows\":20,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":1,\"defaultItemRows\":1,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":true,\"pushItems\":false,\"disablePushOnDrag\":true,\"disablePushOnResize\":true,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"onDrag&Resize\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":false,\"enableEmptyCellAlign\":true,\"disableLiveResize\":true,\"disableLiveMove\":true,\"dragGadgetType\":\"livehtml\",\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConf.setId("MASTER-DashboardConf-6");
			dashboardConf.setIdentification("gadgets crud and importTool");
			dashboardConf.setModel(defaultNewSchema);
			dashboardConf.setHeaderlibs(
					"<script src=\"/controlpanel/static/vendor/element-ui/index.js\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/element-ui/locale/en.min.js\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
							+ "\n"
							+ "<script src=\"/controlpanel/static/vendor/el-search-table-pagination/index.min.js\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/jsoneditor/jsoneditor.js\"></script>\n" + "\n"
							+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n"
							+ "<link rel=\"stylesheet\" href=\"/controlpanel/static/vendor/element-ui/theme-chalk/index.css\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />\n"
							+ "\n" + "<script>\n" + "ELEMENT.locale(ELEMENT.lang.en)\n" + "var __env = __env || {};\n"
							+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
							+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
							+ "			\"form.entity\": \"Entidad\",\n"
							+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
							+ "			\"form.select\": \"Seleccionar\",\n"
							+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
							+ "			\"form.operator\": \"Operador\",\n"
							+ "			\"form.condition\": \"Condición\",\n"
							+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
							+ "			\"form.write.here\": \"Escriba aquí\",\n"
							+ "			\"form.select.field\": \"Seleccionar campo\",\n"
							+ "			\"form.orderby\": \"Ordenar por\",\n"
							+ "			\"form.order.type\": \"Tipo de pedido\",\n"
							+ "			\"form.where\": \"Where\",\n"
							+ "			\"form.max.value\": \"Valor máximo\",\n"
							+ "			\"form.offset\": \"Desplazamiento\",\n"
							+ "			\"form.reset\": \"Restablecer\",\n" + "			\"form.search\": \"Buscar\",\n"
							+ "			\"form.records\": \"Registros\",\n"
							+ "			\"form.columns\": \"Columnas\",\n"
							+ "			\"column.options\": \"Opciones\",\n"
							+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
							+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
							+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
							+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
							+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
							+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
							+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
							+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
							+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
							+ "			\"form.edit.record\": \"Editar registro\",\n"
							+ "			\"form.detail.record\": \"Registro detallado\",\n"
							+ "			\"button.cancel\": \"Cancelar\",\n"
							+ "			\"button.delete\": \"Eliminar\",\n" + "			\"button.save\": \"Guardar\",\n"
							+ "			\"button.close\": \"Cerrar\",\n" + "			\"button.new\": \"Nuevo\",\n"
							+ "			\"button.apply\": \"Aplicar\",\n"
							+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
							+ "		    \"form.title.import\": \"Importar datos\",\n"
							+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
							+ "			\"form.download.csv\":\"Descargar CSV\",\n"
							+ "    		\"form.download.json\":\"Descargar JSON\",\n"
							+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
							+ "		    \"button.click\": \"haga click aquí\",\n"
							+ "		    \"button.click.upload\": \"para subirlo\",\n"
							+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n"
							+ "		    \"button.import\": \"Importar\",\n"
							+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
							+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
							+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
							+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
							+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
							+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
							+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
							+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
							+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
							+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
							+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
							+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
							+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
							+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
							+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
							+ "			\"button.all.records\": \"Todos los registros\",\n"
							+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
							+ "			\"error.message.download\": \"Error descargando datos\",\n"
							+ "			\"error.message.empty\": \"Error no existen registros\",\n"
							+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
							+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
							+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
							+ "			\"form.show.wizard\": \"Show search wizard\",\n"
							+ "			\"form.select\": \"Select\",\n"
							+ "			\"form.select.fields\": \"Select Fields\",\n"
							+ "			\"form.operator\": \"Operator\",\n"
							+ "			\"form.condition\": \"Condition\",\n"
							+ "			\"form.select.operator\": \"Select Operator\",\n"
							+ "			\"form.write.here\": \"Write here\",\n"
							+ "			\"form.select.field\": \"Select Field\",\n"
							+ "			\"form.orderby\": \"Order by\",\n"
							+ "			\"form.order.type\": \"Order Type\",\n"
							+ "			\"form.where\": \"Where\",\n"
							+ "			\"form.max.value\": \"Max Value\",\n"
							+ "			\"form.offset\": \"Offset\",\n" + "			\"form.reset\": \"Reset\",\n"
							+ "			\"form.search\": \"Search\",\n" + "			\"form.records\": \"Records\",\n"
							+ "			\"form.columns\": \"Columns\",\n"
							+ "			\"column.options\": \"Options\",\n"
							+ "			\"form.new.record.title\": \"New record\",\n"
							+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
							+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
							+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
							+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
							+ "			\"message.created.successfully\": \"Record created successfully\",\n"
							+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
							+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
							+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
							+ "			\"form.edit.record\": \"Edit record \",\n"
							+ "			\"form.detail.record\": \"Detail record \",\n"
							+ "			\"button.cancel\": \"Cancel\",\n"
							+ "			\"button.delete\": \"Delete\",\n" + "			\"button.save\": \"Save\",\n"
							+ "			\"button.close\": \"Close\",\n" + "			\"button.new\": \"New\",\n"
							+ "			\"button.apply\": \"Apply\",\n"
							+ "		    \"form.select.entity\": \"Select Entity\",\n"
							+ "		    \"form.title.import\": \"Import records\",\n"
							+ "		    \"form.download.template\": \"Download Template\",\n"
							+ "			\"form.download.csv\":\"Download CSV\",\n"
							+ "    		\"form.download.json\":\"Download JSON\",\n"
							+ "		    \"button.drop\": \"Drop file or\",\n"
							+ "		    \"button.click\": \"click here\",\n"
							+ "		    \"button.click.upload\": \"to upload\",\n"
							+ "		    \"form.info.max\": \"Max. 2mb csv\",\n"
							+ "		    \"button.import\": \"Import\",\n"
							+ "		    \"button.showmore\": \"Show More Details\",\n"
							+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
							+ "		    \"message.success.loaded.1\": \"The\",\n"
							+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
							+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
							+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
							+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
							+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
							+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
							+ "		    \"error.message.processing\": \"Error processing data\",\n"
							+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
							+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
							+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
							+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
							+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
							+ "			\"button.all.records\": \"All the records\",\n"
							+ "			\"button.only.selection.records\": \"Only the selection\",\n"
							+ "			\"error.message.download\": \"Error downloading data\",\n"
							+ "			\"error.message.empty\": \"Error there are no records\",\n"
							+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
							+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
							+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n"
							+ "	var localLocale ='EN';\n" + "	try{\n"
							+ "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
							+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
							+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
							+ " // link messages with internacionalization json on controlpanel\n"
							+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");
			dashboardConf.setDescription("styling with headers for crud gadgets and importTool");
			dashboardConfRepository.save(dashboardConf);
		}
		if (!dashboardConfRepository.existsById("MASTER-DashboardConf-7")) {
			final DashboardConf dashboardConf = new DashboardConf();
			final String defaultNewSchema = "{\"header\":{\"title\":\"My Dashboard\",\"enable\":true,\"height\":72,\"logo\":{\"height\":48},\"backgroundColor\":\"#FFFFFF\",\"textColor\":\"#060E14\",\"iconColor\":\"#060E14\",\"pageColor\":\"#2e6c99\"},\"showfavoritesg\":true,\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":20,\"minRows\":20,\"maxRows\":20,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":1,\"defaultItemRows\":1,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":true,\"pushItems\":false,\"disablePushOnDrag\":true,\"disablePushOnResize\":true,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"onDrag&Resize\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":false,\"enableEmptyCellAlign\":true,\"disableLiveResize\":true,\"disableLiveMove\":true,\"dragGadgetType\":\"livehtml\",\"api\":{}},\"interactionHash\":{\"1\":[]}}";
			dashboardConf.setId("MASTER-DashboardConf-7");
			dashboardConf.setIdentification("ODS gadgets crud and importTool");
			dashboardConf.setModel(defaultNewSchema);
			dashboardConf
					.setHeaderlibs("<script src=\"/controlpanel/static/vendor/jsoneditor/jsoneditor.js\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n" + "\n"
							+ "\n" + "<script>\n" + "\n" + "var __env = __env || {};\n"
							+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
							+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
							+ "			\"form.entity\": \"Entidad\",\n"
							+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
							+ "			\"form.select\": \"Seleccionar\",\n"
							+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
							+ "			\"form.operator\": \"Operador\",\n"
							+ "			\"form.condition\": \"Condición\",\n"
							+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
							+ "			\"form.write.here\": \"Escriba aquí\",\n"
							+ "			\"form.select.field\": \"Seleccionar campo\",\n"
							+ "			\"form.orderby\": \"Ordenar por\",\n"
							+ "			\"form.order.type\": \"Tipo de pedido\",\n"
							+ "			\"form.where\": \"Where\",\n"
							+ "			\"form.max.value\": \"Valor máximo\",\n"
							+ "			\"form.offset\": \"Desplazamiento\",\n"
							+ "			\"form.reset\": \"Restablecer\",\n" + "			\"form.search\": \"Buscar\",\n"
							+ "			\"form.records\": \"Registros\",\n"
							+ "			\"form.columns\": \"Columnas\",\n"
							+ "			\"column.options\": \"Opciones\",\n"
							+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
							+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
							+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
							+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
							+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
							+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
							+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
							+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
							+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
							+ "			\"form.edit.record\": \"Editar registro\",\n"
							+ "			\"form.detail.record\": \"Registro detallado\",\n"
							+ "			\"button.cancel\": \"Cancelar\",\n"
							+ "			\"button.delete\": \"Eliminar\",\n" + "			\"button.save\": \"Guardar\",\n"
							+ "			\"button.close\": \"Cerrar\",\n" + "			\"button.new\": \"Nuevo\",\n"
							+ "			\"button.apply\": \"Aplicar\",\n"
							+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
							+ "		    \"form.title.import\": \"Importar datos\",\n"
							+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
							+ "			\"form.download.csv\":\"Descargar CSV\",\n"
							+ "    		\"form.download.json\":\"Descargar JSON\",\n"
							+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
							+ "		    \"button.click\": \"haga click aquí\",\n"
							+ "		    \"button.click.upload\": \"para subirlo\",\n"
							+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n"
							+ "		    \"button.import\": \"Importar\",\n"
							+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
							+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
							+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
							+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
							+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
							+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
							+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
							+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
							+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
							+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
							+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
							+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
							+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
							+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
							+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
							+ "			\"button.all.records\": \"Todos los registros\",\n"
							+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
							+ "			\"error.message.download\": \"Error descargando datos\",\n"
							+ "			\"error.message.empty\": \"Error no existen registros\",\n"
							+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
							+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
							+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
							+ "			\"form.show.wizard\": \"Show search wizard\",\n"
							+ "			\"form.select\": \"Select\",\n"
							+ "			\"form.select.fields\": \"Select Fields\",\n"
							+ "			\"form.operator\": \"Operator\",\n"
							+ "			\"form.condition\": \"Condition\",\n"
							+ "			\"form.select.operator\": \"Select Operator\",\n"
							+ "			\"form.write.here\": \"Write here\",\n"
							+ "			\"form.select.field\": \"Select Field\",\n"
							+ "			\"form.orderby\": \"Order by\",\n"
							+ "			\"form.order.type\": \"Order Type\",\n"
							+ "			\"form.where\": \"Where\",\n"
							+ "			\"form.max.value\": \"Max Value\",\n"
							+ "			\"form.offset\": \"Offset\",\n" + "			\"form.reset\": \"Reset\",\n"
							+ "			\"form.search\": \"Search\",\n" + "			\"form.records\": \"Records\",\n"
							+ "			\"form.columns\": \"Columns\",\n"
							+ "			\"column.options\": \"Options\",\n"
							+ "			\"form.new.record.title\": \"New record\",\n"
							+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
							+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
							+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
							+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
							+ "			\"message.created.successfully\": \"Record created successfully\",\n"
							+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
							+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
							+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
							+ "			\"form.edit.record\": \"Edit record \",\n"
							+ "			\"form.detail.record\": \"Detail record \",\n"
							+ "			\"button.cancel\": \"Cancel\",\n"
							+ "			\"button.delete\": \"Delete\",\n" + "			\"button.save\": \"Save\",\n"
							+ "			\"button.close\": \"Close\",\n" + "			\"button.new\": \"New\",\n"
							+ "			\"button.apply\": \"Apply\",\n"
							+ "		    \"form.select.entity\": \"Select Entity\",\n"
							+ "		    \"form.title.import\": \"Import records\",\n"
							+ "		    \"form.download.template\": \"Download Template\",\n"
							+ "			\"form.download.csv\":\"Download CSV\",\n"
							+ "    		\"form.download.json\":\"Download JSON\",\n"
							+ "		    \"button.drop\": \"Drop file or\",\n"
							+ "		    \"button.click\": \"click here\",\n"
							+ "		    \"button.click.upload\": \"to upload\",\n"
							+ "		    \"form.info.max\": \"Max. 2mb csv\",\n"
							+ "		    \"button.import\": \"Import\",\n"
							+ "		    \"button.showmore\": \"Show More Details\",\n"
							+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
							+ "		    \"message.success.loaded.1\": \"The\",\n"
							+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
							+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
							+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
							+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
							+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
							+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
							+ "		    \"error.message.processing\": \"Error processing data\",\n"
							+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
							+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
							+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
							+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
							+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
							+ "			\"button.all.records\": \"All the records\",\n"
							+ "			\"button.only.selection.records\": \"Only the selection\",\n"
							+ "			\"error.message.download\": \"Error downloading data\",\n"
							+ "			\"error.message.empty\": \"Error there are no records\",\n"
							+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
							+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
							+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n"
							+ "	var localLocale ='EN';\n" + "	try{\n"
							+ "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
							+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
							+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
							+ " // link messages with internacionalization json on controlpanel\n"
							+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");
			dashboardConf.setDescription("styling ODS with headers for crud gadgets and importTool");
			dashboardConfRepository.save(dashboardConf);
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
		if (!dashboardRepository.findById("MASTER-Dashboard-2").isPresent()) {
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

	public void initDashboardQueriesProfilerUI() {
		if (!dashboardRepository.findById("MASTER-Dashboard-6").isPresent()) {
			log.info("init DashboardQueriesProfilerUI");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-6");
			dashboard.setIdentification("QueriesProfilerUI");
			dashboard.setDescription("Dashboard Queries Profiler UI");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/QueriesProfilerUI.json"));
			dashboard.setPublic(false);
			dashboard.setHeaderlibs(" \n" + "\n"
					+ "<script src=\"/controlpanel/static/vendor/echarts/echarts.min.js\"></script>\n"
					+ "<!-- import JavaScript ELEMENTS-->\n"
					+ "<script src=\"/controlpanel/static/vendor/element-ui/index.js\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
					+ "<script src=\"/controlpanel/static/vendor/element-ui/locale/en.min.js\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
					+ "<!-- import style -->\n" + "<!-- import CSS ELEMENTS-->\n"
					+ "<link rel=\"stylesheet\" href=\"/controlpanel/static/vendor/element-ui/theme-chalk/index.css\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />\n"
					+ "\n" + "<style>\n" + "    md-toolbar {\n" + "    margin-bottom: -10px;\n"
					+ "    height: 40px;  \n" + "    margin-left: 20px;\n" + "    margin-top: 10px\n" + "    }\n" + "\n"
					+ "    * {\n" + "      box-sizing: border-box;\n" + "    }\n" + "\n" + "    .loading-container {\n"
					+ "      width: 100%;\n" + "      max-width: 40%;\n" + "      text-align: center;\n"
					+ "      color: #fff;\n" + "      position: relative;\n" + "      margin: 0 32px;\n" + "    }\n"
					+ "\n" + "    .component {\n" + "       width: 100%;\n" + "      height: 100%;\n"
					+ "      display: block;      \n" + "      padding: 12px;\n" + "    }\n" + "    \n" + "\n"
					+ "    .loading-container:before {\n" + "        content: '';\n" + "        position: absolute;\n"
					+ "        width: 400px;\n" + "        height: 3px;\n" + "        background-color: #fff;\n"
					+ "        bottom: 0;\n" + "        left: 0;\n" + "        border-radius: 10px;\n"
					+ "        animation: movingLine 2s infinite ease-in-out;\n" + "      }\n" + "\n"
					+ "    @keyframes movingLine {\n" + "      0% {\n" + "        opacity: 0;\n" + "        width: 0;\n"
					+ "      }\n" + "\n" + "      33.3%, 66% {\n" + "        opacity: 0.8;\n" + "        width: 100%;\n"
					+ "      }\n" + "      \n" + "      85% {\n" + "        width: 0;\n" + "        left: initial;\n"
					+ "        right: 0;\n" + "        opacity: 1;\n" + "      }\n" + "\n" + "      100% {\n"
					+ "      opacity: 0;\n" + "      width: 0;\n" + "      }\n" + "    }\n" + "\n"
					+ "    .loading-text {\n" + "      font-size: 1vw;\n" + "      line-height: 40px;\n"
					+ "      letter-spacing: 10px;\n" + "      margin-bottom: 30px;\n" + "      display: flex;\n"
					+ "      justify-content: space-evenly;     \n" + "    }\n" + "\n" + "    .loading-text span {\n"
					+ "        animation: moveLetters 2s infinite ease-in-out;\n"
					+ "        transform: translatex(0);\n" + "        position: relative;\n"
					+ "        display: inline-block;\n" + "        opacity: 0;\n"
					+ "        text-shadow: 0px 2px 10px rgba(46, 74, 81, 0.3); \n" + "      }\n" + "\n" + "    \n"
					+ "    .loading-text span:nth-child(1) {\n" + "        animation-delay: 0.1s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(2) {\n" + "        animation-delay: 0.2s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(3) {\n" + "        animation-delay: 0.3s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(4) {\n" + "        animation-delay: 0.4s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(5) {\n" + "        animation-delay: 0.5s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(6) {\n" + "        animation-delay: 0.6s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(7) {\n" + "        animation-delay: 0.7s;\n" + "    }\n" + "\n"
					+ "    .loading-text span:nth-child(8) {\n" + "        animation-delay: 0.8s;\n" + "    }\n" + "\n"
					+ "   .title-text {\n" + "        width: 100%;\n" + "        font-size: 4rem;\n"
					+ "        line-height: 75px;\n" + "        letter-spacing: 0px;\n"
					+ "        margin-bottom: 30px;\n" + "        display: flex;\n"
					+ "        justify-content: center;\n" + "        position: relative;\n" + "        color: white;\n"
					+ "        top: -10px;\n" + "        font-weight: 600;\n" + "        z-index: 200;\n" + "    }\n"
					+ "\n" + "    .icon-title {\n" + "        margin-right: 12px;\n" + "        top: -10%;\n"
					+ "        position: absolute;\n" + "        left: -65%;\n" + "        opacity: .3;\n"
					+ "        font-size: 2rem;\n" + "        padding: 24px;\n"
					+ "        background: rgb(0 0 0 / 50%);\n" + "        color: white;\n"
					+ "        border-radius: 12px;\n" + "        z-index: 1;\n" + "    }\n" + "\n"
					+ "    @keyframes moveLetters {\n" + "      0% {\n" + "        transform: translateX(-15vw);\n"
					+ "        opacity: 0;\n" + "      }\n" + "      \n" + "      33.3%, 66% {\n"
					+ "        transform: translateX(0);\n" + "        opacity: 1;\n" + "      }\n" + "      \n"
					+ "      100% {\n" + "        transform: translateX(15vw);\n" + "        opacity: 0;\n"
					+ "      }\n" + "    }\n" + "\n" + "    .component-card {\n"
					+ "      background-color: transparent;\n" + "      color: #666;\n" + "      padding: 0rem;\n"
					+ "      height: 100%;\n" + "    }\n" + "\n" + "    .component-cards {\n"
					+ "      max-width: 100%;\n" + "      height:100%;\n" + "      margin: 0 auto;\n"
					+ "      display: grid;\n" + "      gap: .5rem;  \n" + "      grid-template-columns: 1fr 2fr;\n"
					+ "    }\n" + "\n" + "    .card-only {\n" + "      grid-template-columns: 1fr!important;\n"
					+ "    }\n" + "\n" + "    .component-title {\n" + "      padding: 0 0 1rem 0;\n"
					+ "        font-size: 1.1rem;\n" + "        font-weight: 500;\n" + "        font-style: normal;\n"
					+ "        line-height: 1.5rem;\n" + "    }\n" + "\n" + "    @media (max-width: 600px) {\n"
					+ "      .component-cards { grid-template-columns: 0 1fr }\n" + "    }\n" + "\n"
					+ "    @media (max-width: 900px) {\n"
					+ "      .component-cards { grid-template-columns: 1fr 3fr }\n" + "    }\n"
					+ "    @media (min-width: 1200px) {\n"
					+ "      .component-cards { grid-template-columns: 1fr 2fr }\n" + "    }\n" + "\n"
					+ "  .ods-dataviz__title {\n" + "      font-size: .85rem!important;\n" + "      font-weight: 500;\n"
					+ "      color: #333;\n" + "      text-align:center;\n" + "  }  \n" + "\n"
					+ "  @keyframes fadeOut{\n" + "    0%{opacity: 0;}\n" + "    30%{opacity: 1;}\n"
					+ "    80%{opacity: .9;}\n" + "    100%{opacity: 0;}\n" + "  }\n" + "  @keyframes fadeIn{\n"
					+ "    from{opacity: 0;}\n" + "    to{opacity: 1;}\n" + "  }\n"
					+ "  .gridster { background-color: #f5f5f5!important}\n" + "\n"
					+ ".gadget-app { font-family: 'soho' !important;font-size:0.85rem !important;}\n"
					+ "el-table td { font-family: 'soho' !important;font-size:0.85rem !important;}\n"
					+ "gridster-item { box-shadow: 0 0.75rem 1.5rem rgb(18 38 63 / 3%); }\n" + "</style>\n"
					+ "<script>\n" + "  ELEMENT.locale(ELEMENT.lang.en)\n" + "</script>");
			dashboard.setUser(getUserAdministrator());

			dashboardRepository.save(dashboard);
		}
	}

	public void initDashboardInternationalizations() {
		if (!dashboardRepository.findById(MASTER_DASHBOARD_FRTH).isPresent()) {
			log.info("init Dashboard Internationalization");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId(MASTER_DASHBOARD_FRTH);
			dashboard.setIdentification("Internationalization Dashboard Example");
			dashboard.setDescription("Internationalization Dashboard Example");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/InternationalizationDashExample.json"));
			dashboard.setPublic(true);
			dashboard.setUser(getUserAdministrator());

			dashboardRepository.save(dashboard);
		}
	}

	public void initDashboardDataclass() {
		if (!dashboardRepository.findById("MASTER-Dashboard-7").isPresent()) {
			log.info("init DashboardDataclassController");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId("MASTER-Dashboard-7");
			dashboard.setIdentification("DataclassController");
			dashboard.setDescription("Dashboard Gestión Errores Dataclass");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(loadFromResources("dashboardmodel/DataClassController.json"));
			dashboard.setPublic(false);
			dashboard.setHeaderlibs("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n"
					+ "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n"
					+ "<link href=\"https://fonts.googleapis.com/css2?family=Poppins:wght@100;400;600&display=swap\" rel=\"stylesheet\">\n"
					+ " <script src=\"https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js\" integrity=\"sha512-r22gChDnGvBylk90+2e/ycr3RVrDi8DIOkIGNhJlKfuyQM4tIRAI062MaV8sfjQKYVGjOBaZBOA87z+IhZE9DA==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
					+ " \n"
					+ "<link href=\"https://cdn.jsdelivr.net/npm/vue-good-table@2.19.1/dist/vue-good-table.css\" rel=\"stylesheet\">\n"
					+ "<script src=\"https://cdn.jsdelivr.net/npm/vue-good-table@2.19.1/dist/vue-good-table.min.js\"></script>\n"
					+ "<script src=\"https://cdn.jsdelivr.net/npm/echarts@5.4.0/dist/echarts.min.js\"></script>\n"
					+ "\n" + "<!-- import style -->\n" + "\n" + "<style>\n" + "    md-toolbar {\n"
					+ "    margin-bottom: -10px;\n" + "    height: 40px;  \n" + "    margin-left: 20px;\n"
					+ "    margin-top: 10px\n" + "    }\n" + "    * {\n" + "      box-sizing: border-box;\n" + "    }\n"
					+ "    .loading-container {\n" + "      width: 100%;\n" + "      max-width: 40%;\n"
					+ "      text-align: center;\n" + "      color: #fff;\n" + "      position: relative;\n"
					+ "      margin: 0 32px;\n" + "    }\n" + "    .component {\n" + "       width: 100%;\n"
					+ "      height: 100%;\n" + "      display: block;      \n" + "      padding: 12px;\n" + "    }\n"
					+ "    .loading-container:before {\n" + "        content: '';\n" + "        position: absolute;\n"
					+ "        width: 400px;\n" + "        height: 3px;\n" + "        background-color: #fff;\n"
					+ "        bottom: 0;\n" + "        left: 0;\n" + "        border-radius: 10px;\n"
					+ "        animation: movingLine 2s infinite ease-in-out;\n" + "      }\n"
					+ "    @keyframes movingLine {\n" + "      0% {\n" + "        opacity: 0;\n" + "        width: 0;\n"
					+ "      }\n" + "      33.3%, 66% {\n" + "        opacity: 0.8;\n" + "        width: 100%;\n"
					+ "      }\n" + "      \n" + "      85% {\n" + "        width: 0;\n" + "        left: initial;\n"
					+ "        right: 0;\n" + "        opacity: 1;\n" + "      }\n" + "      100% {\n"
					+ "      opacity: 0;\n" + "      width: 0;\n" + "      }\n" + "    }\n" + "    .loading-text {\n"
					+ "      font-size: 1vw;\n" + "      line-height: 40px;\n" + "      letter-spacing: 10px;\n"
					+ "      margin-bottom: 30px;\n" + "      display: flex;\n"
					+ "      justify-content: space-evenly;     \n" + "    }\n" + "    .loading-text span {\n"
					+ "        animation: moveLetters 2s infinite ease-in-out;\n"
					+ "        transform: translatex(0);\n" + "        position: relative;\n"
					+ "        display: inline-block;\n" + "        opacity: 0;\n"
					+ "        text-shadow: 0px 2px 10px rgba(46, 74, 81, 0.3); \n" + "      }\n"
					+ "    .loading-text span:nth-child(1) {\n" + "        animation-delay: 0.1s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(2) {\n" + "        animation-delay: 0.2s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(3) {\n" + "        animation-delay: 0.3s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(4) {\n" + "        animation-delay: 0.4s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(5) {\n" + "        animation-delay: 0.5s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(6) {\n" + "        animation-delay: 0.6s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(7) {\n" + "        animation-delay: 0.7s;\n" + "    }\n"
					+ "    .loading-text span:nth-child(8) {\n" + "        animation-delay: 0.8s;\n" + "    }\n"
					+ "   .title-text {\n" + "        width: 100%;\n" + "        font-size: 4rem;\n"
					+ "        line-height: 75px;\n" + "        letter-spacing: 0px;\n"
					+ "        margin-bottom: 30px;\n" + "        display: flex;\n"
					+ "        justify-content: center;\n" + "        position: relative;\n" + "        color: white;\n"
					+ "        top: -10px;\n" + "        font-weight: 600;\n" + "        z-index: 200;\n" + "    }\n"
					+ "    .icon-title {\n" + "        margin-right: 12px;\n" + "        top: -10%;\n"
					+ "        position: absolute;\n" + "        left: -65%;\n" + "        opacity: .3;\n"
					+ "        font-size: 2rem;\n" + "        padding: 24px;\n"
					+ "        background: rgb(0 0 0 / 50%);\n" + "        color: white;\n"
					+ "        border-radius: 12px;\n" + "        z-index: 1;\n" + "    }\n"
					+ "    @keyframes moveLetters {\n" + "      0% {\n" + "        transform: translateX(-15vw);\n"
					+ "        opacity: 0;\n" + "      }\n" + "      \n" + "      33.3%, 66% {\n"
					+ "        transform: translateX(0);\n" + "        opacity: 1;\n" + "      }\n" + "      \n"
					+ "      100% {\n" + "        transform: translateX(15vw);\n" + "        opacity: 0;\n"
					+ "      }\n" + "    }\n" + "    .component-card {\n" + "      background-color: transparent;\n"
					+ "      color: #666;\n" + "      padding: 0rem;\n" + "      height: 100%;\n" + "    }\n"
					+ "    .component-cards {\n" + "      max-width: 100%;\n" + "      height:100%;\n"
					+ "      margin: 0 auto;\n" + "      display: grid;\n" + "      gap: .5rem;  \n"
					+ "      grid-template-columns: 1fr 2fr;\n" + "    }\n" + "    .card-only {\n"
					+ "      grid-template-columns: 1fr!important;\n" + "    }\n" + "    .component-title {\n"
					+ "      padding: 0 0 1rem 0;\n" + "        font-size: 1.1rem;\n" + "        font-weight: 500;\n"
					+ "        font-style: normal;\n" + "        line-height: 1.5rem;\n" + "    }\n"
					+ "    @media (max-width: 600px) {\n" + "      .component-cards { grid-template-columns: 0 1fr }\n"
					+ "    }\n" + "    @media (max-width: 900px) {\n"
					+ "      .component-cards { grid-template-columns: 1fr 3fr }\n" + "    }\n"
					+ "    @media (min-width: 1200px) {\n"
					+ "      .component-cards { grid-template-columns: 1fr 2fr }\n" + "    }\n"
					+ "  .ods-dataviz__title {\n" + "      font-size: .85rem!important;\n" + "      font-weight: 500;\n"
					+ "      color: #333;\n" + "      text-align:center;\n" + "  }  \n" + "  @keyframes fadeOut{\n"
					+ "    0%{opacity: 0;}\n" + "    30%{opacity: 1;}\n" + "    80%{opacity: .9;}\n"
					+ "    100%{opacity: 0;}\n" + "  }\n" + "  @keyframes fadeIn{\n" + "    from{opacity: 0;}\n"
					+ "    to{opacity: 1;}\n" + "  }\n" + "</style>");
			dashboard.setUser(getUserAdministrator());
			dashboardRepository.save(dashboard);
		}
	}

	public void initCrudAndImportDashboard() {
		if (!dashboardRepository.findById(MASTER_DASHBOARD_FIFTH).isPresent()) {
			log.info("init Dashboard Crud and Import Example");
			final Dashboard dashboard = new Dashboard();
			dashboard.setId(MASTER_DASHBOARD_FIFTH);
			dashboard.setIdentification("Dashboard Crud and Import Example");
			dashboard.setDescription("Dashboard Crud and Import Example");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setHeaderlibs(
					"<script src=\"/controlpanel/static/vendor/element-ui/index.js\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/element-ui/locale/en.min.js\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
							+ "\n"
							+ "<script src=\"/controlpanel/static/vendor/el-search-table-pagination/index.min.js\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/jsoneditor/jsoneditor.js\"></script>\n" + "\n"
							+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n"
							+ "<link rel=\"stylesheet\" href=\"/controlpanel/static/vendor/element-ui/theme-chalk/index.css\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />\n"
							+ "\n" + "<script>\n" + "ELEMENT.locale(ELEMENT.lang.en)\n" + "var __env = __env || {};\n"
							+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
							+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
							+ "			\"form.entity\": \"Entidad\",\n"
							+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
							+ "			\"form.select\": \"Seleccionar\",\n"
							+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
							+ "			\"form.operator\": \"Operador\",\n"
							+ "			\"form.condition\": \"Condición\",\n"
							+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
							+ "			\"form.write.here\": \"Escriba aquí\",\n"
							+ "			\"form.select.field\": \"Seleccionar campo\",\n"
							+ "			\"form.orderby\": \"Ordenar por\",\n"
							+ "			\"form.order.type\": \"Tipo de pedido\",\n"
							+ "			\"form.where\": \"Where\",\n"
							+ "			\"form.max.value\": \"Valor máximo\",\n"
							+ "			\"form.offset\": \"Desplazamiento\",\n"
							+ "			\"form.reset\": \"Restablecer\",\n" + "			\"form.search\": \"Buscar\",\n"
							+ "			\"form.records\": \"Registros\",\n"
							+ "			\"form.columns\": \"Columnas\",\n"
							+ "			\"column.options\": \"Opciones\",\n"
							+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
							+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
							+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
							+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
							+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
							+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
							+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
							+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
							+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
							+ "			\"form.edit.record\": \"Editar registro\",\n"
							+ "			\"form.detail.record\": \"Registro detallado\",\n"
							+ "			\"button.cancel\": \"Cancelar\",\n"
							+ "			\"button.delete\": \"Eliminar\",\n" + "			\"button.save\": \"Guardar\",\n"
							+ "			\"button.close\": \"Cerrar\",\n" + "			\"button.new\": \"Nuevo\",\n"
							+ "			\"button.apply\": \"Aplicar\",\n"
							+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
							+ "		    \"form.title.import\": \"Importar datos\",\n"
							+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
							+ "			\"form.download.csv\":\"Descargar CSV\",\n"
							+ "    		\"form.download.json\":\"Descargar JSON\",\n"
							+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
							+ "		    \"button.click\": \"haga click aquí\",\n"
							+ "		    \"button.click.upload\": \"para subirlo\",\n"
							+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n"
							+ "		    \"button.import\": \"Importar\",\n"
							+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
							+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
							+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
							+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
							+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
							+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
							+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
							+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
							+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
							+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
							+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
							+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
							+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
							+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
							+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
							+ "			\"button.all.records\": \"Todos los registros\",\n"
							+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
							+ "			\"error.message.download\": \"Error descargando datos\",\n"
							+ "			\"error.message.empty\": \"Error no existen registros\",\n"
							+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
							+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
							+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
							+ "			\"form.show.wizard\": \"Show search wizard\",\n"
							+ "			\"form.select\": \"Select\",\n"
							+ "			\"form.select.fields\": \"Select Fields\",\n"
							+ "			\"form.operator\": \"Operator\",\n"
							+ "			\"form.condition\": \"Condition\",\n"
							+ "			\"form.select.operator\": \"Select Operator\",\n"
							+ "			\"form.write.here\": \"Write here\",\n"
							+ "			\"form.select.field\": \"Select Field\",\n"
							+ "			\"form.orderby\": \"Order by\",\n"
							+ "			\"form.order.type\": \"Order Type\",\n"
							+ "			\"form.where\": \"Where\",\n"
							+ "			\"form.max.value\": \"Max Value\",\n"
							+ "			\"form.offset\": \"Offset\",\n" + "			\"form.reset\": \"Reset\",\n"
							+ "			\"form.search\": \"Search\",\n" + "			\"form.records\": \"Records\",\n"
							+ "			\"form.columns\": \"Columns\",\n"
							+ "			\"column.options\": \"Options\",\n"
							+ "			\"form.new.record.title\": \"New record\",\n"
							+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
							+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
							+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
							+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
							+ "			\"message.created.successfully\": \"Record created successfully\",\n"
							+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
							+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
							+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
							+ "			\"form.edit.record\": \"Edit record \",\n"
							+ "			\"form.detail.record\": \"Detail record \",\n"
							+ "			\"button.cancel\": \"Cancel\",\n"
							+ "			\"button.delete\": \"Delete\",\n" + "			\"button.save\": \"Save\",\n"
							+ "			\"button.close\": \"Close\",\n" + "			\"button.new\": \"New\",\n"
							+ "			\"button.apply\": \"Apply\",\n"
							+ "		    \"form.select.entity\": \"Select Entity\",\n"
							+ "		    \"form.title.import\": \"Import records\",\n"
							+ "		    \"form.download.template\": \"Download Template\",\n"
							+ "			\"form.download.csv\":\"Download CSV\",\n"
							+ "    		\"form.download.json\":\"Download JSON\",\n"
							+ "		    \"button.drop\": \"Drop file or\",\n"
							+ "		    \"button.click\": \"click here\",\n"
							+ "		    \"button.click.upload\": \"to upload\",\n"
							+ "		    \"form.info.max\": \"Max. 2mb csv\",\n"
							+ "		    \"button.import\": \"Import\",\n"
							+ "		    \"button.showmore\": \"Show More Details\",\n"
							+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
							+ "		    \"message.success.loaded.1\": \"The\",\n"
							+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
							+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
							+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
							+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
							+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
							+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
							+ "		    \"error.message.processing\": \"Error processing data\",\n"
							+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
							+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
							+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
							+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
							+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
							+ "			\"button.all.records\": \"All the records\",\n"
							+ "			\"button.only.selection.records\": \"Only the selection\",\n"
							+ "			\"error.message.download\": \"Error downloading data\",\n"
							+ "			\"error.message.empty\": \"Error there are no records\",\n"
							+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
							+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
							+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n"
							+ "	var localLocale ='EN';\n" + "	try{\n"
							+ "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
							+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
							+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
							+ " // link messages with internacionalization json on controlpanel\n"
							+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");
			dashboard.setModel(loadFromResources("dashboardmodel/CrudImportDashExample.json"));
			dashboard.setPublic(true);
			dashboard.setUser(getUserAdministrator());
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
		if (userDeveloper == null) {
			userDeveloper = userCDBRepository.findByUserId(DEVELOPER);
		}
		return userDeveloper;
	}

	private User getUserAdministrator() {
		if (userAdministrator == null) {
			userAdministrator = userCDBRepository.findByUserId(ADMINISTRATOR);
		}
		return userAdministrator;
	}

	private User getUser() {
		if (user == null) {
			user = userCDBRepository.findByUserId("user");
		}
		return user;
	}

	private User getUserAnalytics() {
		if (userAnalytics == null) {
			userAnalytics = userCDBRepository.findByUserId("analytics");
		}
		return userAnalytics;
	}

	private GadgetDatasource getGadgetDatasourceDeveloper() {
		if (gadgetDatasourceDeveloper == null) {
			gadgetDatasourceDeveloper = gadgetDatasourceRepository.findAll().get(0);
		}
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
			dataModel.setIdentification(TWITTER);
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

		if (!dataModelRepository.findById("MASTER-DataModel-27").isPresent()) {
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

		if (!dataModelRepository.findById("MASTER-DataModel-28").isPresent()) {
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
		if (!dataModelRepository.findById("MASTER-DataModel-29").isPresent()) {
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

		if (!dataModelRepository.findById("MASTER-DataModel-30").isPresent()) {
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
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(GADGET1CONFIG);
			gadget.setUser(getUserDeveloper());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_2);
			gadget.setIdentification("airportsByCountry");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById(TABLE).orElse(null));
			gadget.setConfig(GADGET2CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_3);
			gadget.setIdentification("airportsByCountryTop10");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Airports\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_4);
			gadget.setIdentification(DESTINATIONMAP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("map").orElse(null));
			gadget.setConfig(
					"{\"center\":{\"lat\":44.08758502824516,\"lng\":18.6328125,\"zoom\":1},\"markersFilter\":\"count\",\"jsonMarkers\":\"[\\n{\\\"markerColor\\\": \\\"#0066ff\\\", \\\"iconColor\\\":\\\"black\\\" ,\\\"icon\\\":\\\"plane\\\",\\\"min\\\":2001},\\n{\\\"markerColor\\\": \\\"#4d94ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":2000,\\\"min\\\":501},\\n{\\\"markerColor\\\": \\\"#80b3ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":500,\\\"min\\\":51},\\n{\\\"markerColor\\\": \\\"#b3d1ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":50,\\\"min\\\":6},\\n{\\\"markerColor\\\": \\\"#e6f0ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":5}\\n]\"}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_5);
			gadget.setIdentification("destinationCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(GADGET5CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_6);
			gadget.setIdentification("originCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true},\"stacked\":false,\"sort\":false,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":false,\"ticks\":{},\"scaleLabel\":{\"display\":true,\"labelString\":\"\"},\"hideLabel\":\"1\",\"gridLines\":{\"display\":false}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_7);
			gadget.setIdentification(ROUTESDESTTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(GADGET5CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);

			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_8);
			gadget.setIdentification(ROUTESORIGINTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(GADGET5CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}
	}

	public void initGadgetOpenFlight() {

		Gadget gadget = null;

		if (!gadgetRepository.findById(MASTER_GADGET_2).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_2);
			gadget.setIdentification("airportsByCountry");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById(TABLE).orElse(null));
			gadget.setConfig(GADGET2CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (!gadgetRepository.findById(MASTER_GADGET_3).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_3);
			gadget.setIdentification("airportsByCountryTop10");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Airports\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (!gadgetRepository.findById(MASTER_GADGET_4).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_4);
			gadget.setIdentification(DESTINATIONMAP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("map").orElse(null));
			gadget.setConfig(
					"{\"center\":{\"lat\":44.08758502824516,\"lng\":18.6328125,\"zoom\":1},\"markersFilter\":\"count\",\"jsonMarkers\":\"[\\n{\\\"markerColor\\\": \\\"#0066ff\\\", \\\"iconColor\\\":\\\"black\\\" ,\\\"icon\\\":\\\"plane\\\",\\\"min\\\":2001},\\n{\\\"markerColor\\\": \\\"#4d94ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":2000,\\\"min\\\":501},\\n{\\\"markerColor\\\": \\\"#80b3ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":500,\\\"min\\\":51},\\n{\\\"markerColor\\\": \\\"#b3d1ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":50,\\\"min\\\":6},\\n{\\\"markerColor\\\": \\\"#e6f0ff\\\", \\\"iconColor\\\":\\\"black\\\",\\\"icon\\\":\\\"plane\\\",\\\"max\\\":5}\\n]\"}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (!gadgetRepository.findById(MASTER_GADGET_5).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_5);
			gadget.setIdentification("destinationCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (!gadgetRepository.findById(MASTER_GADGET_6).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_6);
			gadget.setIdentification("originCountries");
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"Number of routes\",\"display\":true}}]}}");
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (!gadgetRepository.findById(MASTER_GADGET_7).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_7);
			gadget.setIdentification(ROUTESDESTTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
			gadget.setConfig(GADGET1CONFIG);
			gadget.setUser(getUserAnalytics());
			gadgetRepository.save(gadget);
		}

		if (!gadgetRepository.findById(MASTER_GADGET_8).isPresent()) {
			gadget = new Gadget();
			gadget.setId(MASTER_GADGET_8);
			gadget.setIdentification(ROUTESORIGINTOP_STR);
			gadget.setPublic(false);
			gadget.setDescription("");
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById(TABLE).orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById("mixed").orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById("line").orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById("bar").orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById("mixed").orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById("line").orElse(null));
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
			gadget.setType(gadgetTemplateRepository.findById(TABLE).orElse(null));
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
			gadgetDatasources.setDescription("Restaurants sample Datasource.");
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

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_2).isPresent()) {
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_2);
			gadgetDatasources.setIdentification(ROUTESORIGINTOP_STR);
			gadgetDatasources.setDescription("Routes group by src sample Datasource.");
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

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_3).isPresent()) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_3);
			gadgetDatasources.setIdentification(ROUTESDESTTOP_STR);
			gadgetDatasources.setDescription("Routes group by dest sample Datasource.");
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

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_4).isPresent()) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_4);
			gadgetDatasources.setIdentification("countriesAsDestination");
			gadgetDatasources.setDescription("Routesexten group by countrysrc sample Datasource.");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select re.routesexten.countrysrc as countrysrc ,re.routesexten.countrydest as countrydest ,count(re) as count from routesexten As re group by re.routesexten.countrysrc,re.routesexten.countrydest order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTESEXT_STR));
			gadgetDatasources.setMaxvalues(10);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_5).isPresent()) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_5);
			gadgetDatasources.setIdentification(DESTINATIONMAP_STR);
			gadgetDatasources.setDescription("Routesexten sample Datasource.");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select re.countrysrc as countrysrc,re.countrydest as countrydest,re.count, iso.ISO3166.latitude as latitude, iso.ISO3166.longitude as longitude from ( select rx.routesexten.countrysrc As countrysrc, rx.routesexten.countrydest As countrydest, count(re.routesexten.countrysrc) As count from routesexten as rx group by rx.routesexten.countrysrc, rx.routesexten.countrydest order by count desc) As re inner join ISO3166_1 As iso on re.countrydest = iso.ISO3166.name");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(ROUTESEXT_STR));
			gadgetDatasources.setMaxvalues(500);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_6).isPresent()) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_6);
			gadgetDatasources.setIdentification("airportsCountByCountryTop10");
			gadgetDatasources.setDescription("Airports group by country top 10 sample Datasource.");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"select airp.airportsdata.country as country, count(airp.airportsdata.country) AS count from airportsdata AS airp group by airp.airportsdata.country order by count desc");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(ontologyRepository.findByIdentification(AIRPORT_STR));
			gadgetDatasources.setMaxvalues(10);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserAnalytics());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_7).isPresent()) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_7);
			gadgetDatasources.setIdentification("airportsCountByCountry");
			gadgetDatasources.setDescription("Restaurants sample Datasource.");
			gadgetDatasources.setDescription("Airports group by country sample Datasource.");
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

		if (!gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_8).isPresent()) {
			gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setId(MASTER_GADGET_DATASOURCE_8);
			gadgetDatasources.setIdentification("distinctCountries");
			gadgetDatasources.setDescription("Routesexten group by countrysrc sample Datasource.");
			gadgetDatasources.setMode(QUERY);
			gadgetDatasources.setQuery(
					"SELECT r.routesexten.countrysrc AS country FROM routesexten as r group by r.routesexten.countrysrc ORDER BY country");
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
			gadgetDatasources.setDescription("QA_OVERVIEW sample Datasource.");
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
			gadgetDatasources.setDescription("Producer_ErrorCat sample Datasource.");
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
			gadgetDatasources.setDescription("ErrorsOnDate sample Datasource.");
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
			gadgetDatasources.setDescription("QA_DETAILS sample Datasource.");
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
			gadgetDatasources.setDescription("Producer error type sample Datasource.");
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
			gadgetDatasources.setDescription("Errors type on date sample Datasource.");
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
			gadgetDatasources.setDescription("QA_DETAIL_EXTENDED sample Datasource.");
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

	public void initGadgetDatasourcesQueriesProfilerUI() {
		final GadgetDatasource gadgetDatasources = new GadgetDatasource();
		for (int i = 0; i < DASHBOARD_QUERYMETRICS_DATASOURCES_ID.length; i++) {
			if (!gadgetDatasourceRepository.findById(DASHBOARD_QUERYMETRICS_DATASOURCES_ID[i]).isPresent()) {
				gadgetDatasources.setId(DASHBOARD_QUERYMETRICS_DATASOURCES_ID[i]);
				gadgetDatasources.setIdentification(DASHBOARD_QUERYMETRICS_DATASOURCES_IDENTIFICATION[i]);
				gadgetDatasources.setDescription(DASHBOARD_QUERYMETRICS_DATASOURCES_IDENTIFICATION[i]);
				gadgetDatasources.setMode(QUERY);
				gadgetDatasources.setQuery(DASHBOARD_QUERYMETRICS_DATASOURCES_QUERY[i]);
				gadgetDatasources.setDbtype("RTDB");
				gadgetDatasources.setRefresh(0);
				gadgetDatasources.setOntology(ontologyRepository.findByIdentification("QueryMetrics"));
				gadgetDatasources.setMaxvalues(DASHBOARD_QUERYMETRICS_DATASOURCES_LIMIT[i]);
				gadgetDatasources.setConfig("{\"simpleMode\":true}");
				gadgetDatasources.setUser(getUserAdministrator());
				gadgetDatasourceRepository.save(gadgetDatasources);
			}
		}
	}

	public void initGadgetDatasourcesDataclass() {
		if (ontologyRepository.findByIdentification("Audit_developer") == null && initSamples) {
			final DataModel dataModel = dataModelRepository.findDatamodelsByIdentification("AuditPlatform");
			final Ontology ontology = new Ontology();
			ontology.setId("MASTER-Ontology-38");
			ontology.setDataModel(dataModel);
			ontology.setJsonSchema(dataModel.getJsonSchema());
			ontology.setIdentification("Audit_developer");
			ontology.setDescription("System Ontology. Auditory of operations between user and Platform for user: "
					+ getUserDeveloper().getUserId());
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(false);
			ontology.setUser(getUserDeveloper());
			ontology.setRtdbDatasource(RtdbDatasource.ELASTIC_SEARCH);
			ontologyService.createOntology(ontology, null);
		}
		final GadgetDatasource gadgetDatasources = new GadgetDatasource();
		for (int i = 0; i < DASHBOARD_DATACLASS_DATASOURCES_ID.length; i++) {
			if (!gadgetDatasourceRepository.findById(DASHBOARD_DATACLASS_DATASOURCES_ID[i]).isPresent()) {
				gadgetDatasources.setId(DASHBOARD_DATACLASS_DATASOURCES_ID[i]);
				gadgetDatasources.setIdentification(DASHBOARD_DATACLASS_DATASOURCES_IDENTIFICATION[i]);
				gadgetDatasources.setDescription(DASHBOARD_DATACLASS_DATASOURCES_IDENTIFICATION[i]);
				gadgetDatasources.setMode(QUERY);
				gadgetDatasources.setQuery(DASHBOARD_DATACLASS_DATASOURCES_QUERY[i]);
				gadgetDatasources.setDbtype("RTDB");
				gadgetDatasources.setRefresh(0);
				gadgetDatasources.setOntology(ontologyRepository.findByIdentification("Audit_developer"));
				gadgetDatasources.setMaxvalues(DASHBOARD_DATACLASS_DATASOURCES_LIMIT[i]);
				gadgetDatasources.setConfig("{\"simpleMode\":true}");
				gadgetDatasources.setUser(getUserAdministrator());
				gadgetDatasourceRepository.save(gadgetDatasources);
			}
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

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-2").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-2");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_7).orElse(null));
			gadgetMeasure.setConfig("{\"fields\":[\"count\"],\"name\":\"Country\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_2).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-3").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-3");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_7).orElse(null));
			gadgetMeasure.setConfig("{\"fields\":[\"acountry\"],\"name\":\"Number of Airports\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_2).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-4").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-4");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_6).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"country\",\"count\"],\"name\":\"Top 10 Countries By Airports\",\"config\":{\"backgroundColor\":\"#2d60b5\",\"borderColor\":\"#2d60b5\",\"pointBackgroundColor\":\"#2d60b5\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_3).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-5").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-5");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_5).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"latitude\",\"longitude\",\"countrydest\",\"countrydest\",\"count\"],\"name\":\"\",\"config\":{}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_4).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-6").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-6");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_4).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"countrydest\",\"count\"],\"name\":\"Top Country Destinations\",\"config\":{\"backgroundColor\":\"#e8cb6a\",\"borderColor\":\"#e8cb6a\",\"pointBackgroundColor\":\"#e8cb6a\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_5).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-7").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-7");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_4).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"countrysrc\",\"count\"],\"name\":\"Top Country Origins\",\"config\":{\"backgroundColor\":\"#879dda\",\"borderColor\":\"#879dda\",\"pointBackgroundColor\":\"#879dda\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_6).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-8").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-8");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_3).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"dest\",\"count\"],\"name\":\"Top Destination Airports\",\"config\":{\"backgroundColor\":\"#4e851b\",\"borderColor\":\"#4e851b\",\"pointBackgroundColor\":\"#4e851b\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_7).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-9").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-9");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_2).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"src\",\"count\"],\"name\":\"Top Origin Airports\",\"config\":{\"backgroundColor\":\"#b02828\",\"borderColor\":\"#b02828\",\"pointBackgroundColor\":\"#b02828\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_8).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void initGadgetMeasureQAWindTurbines() {
		GadgetMeasure gadgetMeasure;

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-10").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-10");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9).orElse(null));
			gadgetMeasure
					.setConfig("{\"fields\":[\"idAdaptador\"],\"name\":\"Producer\",\"config\":{\"position\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-11").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-11");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"dataLost\"],\"name\":\"Missed Data (%)\",\"config\":{\"position\":\"2\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-12").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-12");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9).orElse(null));
			gadgetMeasure
					.setConfig("{\"fields\":[\"bad\"],\"name\":\"Wrong Records\",\"config\":{\"position\":\"4\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-13").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-13");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9).orElse(null));
			gadgetMeasure
					.setConfig("{\"fields\":[\"good\"],\"name\":\"Right Records\",\"config\":{\"position\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-14").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-14");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_9).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"totalLoaded\"],\"name\":\"Data Loaded\",\"config\":{\"position\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_9).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-15").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-15");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat1\",\"structural\"],\"name\":\"Structural errors\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-16").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-16");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat2\",\"integrity\"],\"name\":\"Integrity errors\",\"config\":{\"backgroundColor\":\"rgba(0,168,57,0.44)\",\"borderColor\":\"rgba(0,168,57,0.44)\",\"pointBackgroundColor\":\"rgba(0,168,57,0.44)\",\"pointHoverBackgroundColor\":\"rgba(0,168,57,0.44)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-17").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-17");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameCat3\",\"business\"],\"name\":\"Business errors\",\"config\":{\"backgroundColor\":\"rgba(201,58,58,0.88)\",\"borderColor\":\"rgba(201,58,58,0.88)\",\"pointBackgroundColor\":\"rgba(201,58,58,0.88)\",\"pointHoverBackgroundColor\":\"rgba(201,58,58,0.88)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-18").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-18");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_10).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"refCat\",\"totalLoaded\"],\"name\":\" Ok \",\"config\":{\"backgroundColor\":\"#e39d34\",\"borderColor\":\"#e39d34\",\"pointBackgroundColor\":\"#e39d34\",\"pointHoverBackgroundColor\":\"#e39d34\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_10).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-19").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-19");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"structural\"],\"name\":\"Structural errors\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-20").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-20");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"totalLoaded\"],\"name\":\"Ok\",\"config\":{\"backgroundColor\":\"#e39d34\",\"borderColor\":\"#e39d34\",\"pointBackgroundColor\":\"#e39d34\",\"pointHoverBackgroundColor\":\"#e39d34\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-21").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-21");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"integrity\"],\"name\":\"Integrity errors\",\"config\":{\"backgroundColor\":\"rgba(0,168,57,0.44)\",\"borderColor\":\"rgba(0,168,57,0.44)\",\"pointBackgroundColor\":\"rgba(0,168,57,0.44)\",\"pointHoverBackgroundColor\":\"rgba(0,168,57,0.44)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-22").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-22");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_11).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"business\"],\"name\":\"Business errors\",\"config\":{\"backgroundColor\":\"rgba(201,58,58,0.88)\",\"borderColor\":\"rgba(201,58,58,0.88)\",\"pointBackgroundColor\":\"rgba(201,58,58,0.88)\",\"pointHoverBackgroundColor\":\"rgba(201,58,58,0.88)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_11).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-23").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-23");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_12).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"forecast\"],\"name\":\"Production Forecast\",\"config\":{\"backgroundColor\":\"rgba(223,94,255,0.62)\",\"borderColor\":\"rgba(223,94,255,0.62)\",\"pointBackgroundColor\":\"rgba(223,94,255,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_12).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-24").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-24");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_12).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"errors\"],\"name\":\"WTG\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_12).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-25").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-25");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_12).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"name\",\"meteor\"],\"name\":\"Meter\",\"config\":{\"backgroundColor\":\"rgba(17,245,149,0.62)\",\"borderColor\":\"rgba(17,245,149,0.62)\",\"pointBackgroundColor\":\"rgba(17,245,149,0.62)\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_12).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-26").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-26");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType2\",\"e102\"],\"name\":\"102 The raw has no enough fields\",\"config\":{\"backgroundColor\":\"rgba(114,181,62,0.62)\",\"borderColor\":\"rgba(114,181,62,0.62)\",\"pointBackgroundColor\":\"rgba(114,181,62,0.62)\",\"pointHoverBackgroundColor\":\"rgba(114,181,62,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-27").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-27");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType5\",\"e105\"],\"name\":\"105 Invalid numeric format \",\"config\":{\"backgroundColor\":\"#eda437\",\"borderColor\":\"#eda437\",\"pointBackgroundColor\":\"#eda437\",\"pointHoverBackgroundColor\":\"#eda437\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-28").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-28");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType9\",\"e109\"],\"name\":\"109 Duplicated record\",\"config\":{\"backgroundColor\":\"rgba(84,0,168,0.26)\",\"borderColor\":\"rgba(84,0,168,0.26)\",\"pointBackgroundColor\":\"rgba(84,0,168,0.26)\",\"pointHoverBackgroundColor\":\"rgba(84,0,168,0.26)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-29").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-29");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType4\",\"e104\"],\"name\":\"104 Invalid date format\",\"config\":{\"backgroundColor\":\"rgba(41,196,230,0.67)\",\"borderColor\":\"rgba(41,196,230,0.67)\",\"pointBackgroundColor\":\"rgba(41,196,230,0.67)\",\"pointHoverBackgroundColor\":\"rgba(41,196,230,0.67)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-30").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-30");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType3\",\"e103\"],\"name\":\"103 Mandatory fields \",\"config\":{\"backgroundColor\":\"rgba(24,0,168,0.62)\",\"borderColor\":\"rgba(24,0,168,0.62)\",\"pointBackgroundColor\":\"rgba(24,0,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(24,0,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-31").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-31");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType6\",\"e106\"],\"name\":\"106 Integrity error\",\"config\":{\"backgroundColor\":\"rgba(70,131,224,0.57)\",\"borderColor\":\"rgba(70,131,224,0.57)\",\"pointBackgroundColor\":\"rgba(70,131,224,0.57)\",\"pointHoverBackgroundColor\":\"rgba(70,131,224,0.57)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-32").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-32");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType0\",\"e100\"],\"name\":\"100 Frozen data\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.62)\",\"borderColor\":\"rgba(0,108,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-33").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-33");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType10\",\"e110\"],\"name\":\"110 Decimal precision \",\"config\":{\"backgroundColor\":\"rgba(0,148,168,0.62)\",\"borderColor\":\"rgba(0,148,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,148,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,148,168,0.62)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-34").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-34");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType8\",\"e108\"],\"name\":\"108 Out of bounds sup \",\"config\":{\"backgroundColor\":\"rgba(0,168,67,0.21)\",\"borderColor\":\"rgba(0,168,67,0.21)\",\"pointBackgroundColor\":\"rgba(0,168,67,0.21)\",\"pointHoverBackgroundColor\":\"rgba(0,168,67,0.21)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-35").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-35");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType7\",\"e107\"],\"name\":\"107 Out of bounds inf \",\"config\":{\"backgroundColor\":\"rgba(168,30,0,0.49)\",\"borderColor\":\"rgba(168,30,0,0.49)\",\"pointBackgroundColor\":\"rgba(168,30,0,0.49)\",\"pointHoverBackgroundColor\":\"rgba(168,30,0,0.49)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-36").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-36");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_13).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"nameType1\",\"e101\"],\"name\":\"101 Max null values per hour\",\"config\":{\"backgroundColor\":\"rgba(122,89,5,0.98)\",\"borderColor\":\"rgba(122,89,5,0.98)\",\"pointBackgroundColor\":\"rgba(122,89,5,0.98)\",\"pointHoverBackgroundColor\":\"rgba(122,89,5,0.98)\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_13).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-37").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-37");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"duplicated\"],\"name\":\"109 Duplicated record\",\"config\":{\"backgroundColor\":\"rgba(84,0,168,0.26)\",\"borderColor\":\"rgba(84,0,168,0.26)\",\"pointBackgroundColor\":\"rgba(84,0,168,0.26)\",\"pointHoverBackgroundColor\":\"rgba(84,0,168,0.26)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-38").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-38");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"decimalPrecision\"],\"name\":\"110 Decimal precision\",\"config\":{\"backgroundColor\":\"rgba(0,148,168,0.62)\",\"borderColor\":\"rgba(0,148,168,0.62)\",\"pointBackgroundColor\":\"rgba(0,148,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(0,148,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-39").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-39");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"numericFormat\"],\"name\":\"105 Invalid numeric format\",\"config\":{\"backgroundColor\":\"#eda437\",\"borderColor\":\"#eda437\",\"pointBackgroundColor\":\"#eda437\",\"pointHoverBackgroundColor\":\"#eda437\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-40").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-40");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"nullValues\"],\"name\":\"101 Null values\",\"config\":{\"backgroundColor\":\"rgba(122,89,5,0.98)\",\"borderColor\":\"rgba(122,89,5,0.98)\",\"pointBackgroundColor\":\"rgba(122,89,5,0.98)\",\"pointHoverBackgroundColor\":\"rgba(122,89,5,0.98)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-41").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-41");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"dateFormat\"],\"name\":\"104 Invalid date format\",\"config\":{\"backgroundColor\":\"rgba(41,196,230,0.67)\",\"borderColor\":\"rgba(41,196,230,0.67)\",\"pointBackgroundColor\":\"rgba(41,196,230,0.67)\",\"pointHoverBackgroundColor\":\"rgba(41,196,230,0.67)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-42").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-42");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"integrity\"],\"name\":\"106 Integrity error\",\"config\":{\"backgroundColor\":\"rgba(70,131,224,0.57)\",\"borderColor\":\"rgba(70,131,224,0.57)\",\"pointBackgroundColor\":\"rgba(70,131,224,0.57)\",\"pointHoverBackgroundColor\":\"rgba(70,131,224,0.57)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-43").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-43");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"mandatoryFields\"],\"name\":\"103 Mandatory fields\",\"config\":{\"backgroundColor\":\"rgba(24,0,168,0.62)\",\"borderColor\":\"rgba(24,0,168,0.62)\",\"pointBackgroundColor\":\"rgba(24,0,168,0.62)\",\"pointHoverBackgroundColor\":\"rgba(24,0,168,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-44").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-44");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"raw\"],\"name\":\"102 The raw has no enough fields\",\"config\":{\"backgroundColor\":\"rgba(114,181,62,0.62)\",\"borderColor\":\"rgba(114,181,62,0.62)\",\"pointBackgroundColor\":\"rgba(114,181,62,0.62)\",\"pointHoverBackgroundColor\":\"rgba(114,181,62,0.62)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-45").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-45");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"boundsInf\"],\"name\":\"107 Out of bounds inf\",\"config\":{\"backgroundColor\":\"rgba(168,30,0,0.49)\",\"borderColor\":\"rgba(168,30,0,0.49)\",\"pointBackgroundColor\":\"rgba(168,30,0,0.49)\",\"pointHoverBackgroundColor\":\"rgba(168,30,0,0.49)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-46").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-46");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"frozenData\"],\"name\":\"100 Frozen data\",\"config\":{\"backgroundColor\":\"rgba(0,108,168,0.72)\",\"borderColor\":\"rgba(0,108,168,0.72)\",\"pointBackgroundColor\":\"rgba(0,108,168,0.72)\",\"pointHoverBackgroundColor\":\"rgba(0,108,168,0.72)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-47").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-47");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_14).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"process_date\",\"boundsSup\"],\"name\":\"108 Out of bounds sup\",\"config\":{\"backgroundColor\":\"rgba(0,168,67,0.21)\",\"borderColor\":\"rgba(0,168,67,0.21)\",\"pointBackgroundColor\":\"rgba(0,168,67,0.21)\",\"pointHoverBackgroundColor\":\"rgba(0,168,67,0.21)\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_14).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-48").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-48");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"errorCategory\"],\"name\":\"Error Category\",\"config\":{\"position\":\"0\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-49").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-49");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"raw\"],\"name\":\"Original raw content\",\"config\":{\"position\":\"5\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-50").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-50");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15).orElse(null));
			gadgetMeasure
					.setConfig("{\"fields\":[\"assetName\"],\"name\":\"WTG Name\",\"config\":{\"position\":\"3\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-51").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-51");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15).orElse(null));
			gadgetMeasure
					.setConfig("{\"fields\":[\"siteName\"],\"name\":\"Site Name\",\"config\":{\"position\":\"2\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-52").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-52");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15).orElse(null));
			gadgetMeasure.setConfig(
					"{\"fields\":[\"errorDescription\"],\"name\":\"Error Description\",\"config\":{\"position\":\"1\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}

		if (!gadgetMeasureRepository.findById("MASTER-GadgetMeasure-53").isPresent()) {
			gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setId("MASTER-GadgetMeasure-53");
			gadgetMeasure.setDatasource(gadgetDatasourceRepository.findById(MASTER_GADGET_DATASOURCE_15).orElse(null));
			gadgetMeasure
					.setConfig("{\"fields\":[\"timestamp\"],\"name\":\"Timestamp\",\"config\":{\"position\":\"4\"}}");
			gadgetMeasure.setGadget(gadgetRepository.findById(MASTER_GADGET_15).orElse(null));
			gadgetMeasureRepository.save(gadgetMeasure);
		}
	}

	public void initInternationalization() {
		log.info("init Internationalization");
		final Optional<Internationalization> internationalizations = internationalizationRepository
				.findById(MASTER_INTERNATIONALIZATION_ONE);
		if (!internationalizations.isPresent()) {
			log.info("No internationalizations...adding");

			final Internationalization internationalization = new Internationalization();
			internationalization.setId(MASTER_INTERNATIONALIZATION_ONE);
			internationalization.setIdentification("InternationalizationExample");
			internationalization.setDescription("Internationalization example");
			internationalization
					.setJsoni18n(loadFromResources("internationalizations/InternationalizationExample.json"));
			internationalization.setPublic(true);
			internationalization.setUser(getUserAdministrator());

			internationalizationRepository.save(internationalization);
		}

	}

	public void initInternationalizationGadgets() {
		log.info("init Internationalization_gadgets");
		final Optional<Internationalization> internationalizations = internationalizationRepository
				.findById(MASTER_INTERNATIONALIZATION_TWO);
		if (!internationalizations.isPresent()) {
			log.info("No internationalizations gadgetInternationalization...adding");

			final Internationalization internationalization = new Internationalization();
			internationalization.setId(MASTER_INTERNATIONALIZATION_TWO);
			internationalization.setIdentification("gadgetInternationalization");
			internationalization.setDescription("Internationalization for gadgets crud and importTool");
			internationalization
					.setJsoni18n(loadFromResources("internationalizations/InternationalizationCrudImporTool.json"));
			internationalization.setPublic(true);
			internationalization.setUser(getUserAdministrator());
			internationalizationRepository.save(internationalization);
		}
	}

	public void initI18nResources() {
		log.info("init i18nResources");
		final List<I18nResources> i18nResources = i18nResourcesRepository.findAll();
		if (i18nResources.isEmpty()) {
			log.info("No i18n resources...adding");
			if (internationalizationRepository.findById(MASTER_INTERNATIONALIZATION_ONE).isPresent()
					&& dashboardRepository.findById(MASTER_DASHBOARD_FRTH).isPresent()) {
				final I18nResources i18nresource = new I18nResources();
				i18nresource.setId("MASTER-I18nResources-1");
				i18nresource
						.setI18n(internationalizationRepository.findById(MASTER_INTERNATIONALIZATION_ONE).orElse(null));
				i18nresource.setOpResource(dashboardRepository.findById(MASTER_DASHBOARD_FRTH).orElse(null));

				i18nResourcesRepository.save(i18nresource);
			}
		}
	}

	public void initI18nResourcesCrudAndImport() {
		log.info("init i18nResources");
		final Optional<I18nResources> i18nResources = i18nResourcesRepository.findById("MASTER-I18nResources-2");
		if (!i18nResources.isPresent()) {
			log.info("No i18n resources for crud and import...adding");
			if (internationalizationRepository.findById(MASTER_INTERNATIONALIZATION_TWO).isPresent()
					&& dashboardRepository.findById(MASTER_DASHBOARD_FRTH).isPresent()) {
				final I18nResources i18nresource = new I18nResources();
				i18nresource.setId("MASTER-I18nResources-2");
				i18nresource
						.setI18n(internationalizationRepository.findById(MASTER_INTERNATIONALIZATION_TWO).orElse(null));
				i18nresource.setOpResource(dashboardRepository.findById(MASTER_DASHBOARD_FIFTH).orElse(null));
				i18nResourcesRepository.save(i18nresource);
			}
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
		final String ONTOLOGY_DATACLASS = "DataClassExample";

		final String ONTOLOGY_AIRQUALITYGR2 = "AirQuality_gr2";
		final String ONTOLOGY_TWINPROPERTIESTURBINE = "TwinPropertiesTurbine";
		final String ONTOLOGY_TWINPROPERTIESSENSEHAT = "TwinPropertiesSensehat";
		final String ONTOLOGY_NATIVENOTIFKEYS = "NativeNotifKeys";
		final String ONTOLOGY_NOTIFICATIONMESSAGE = "notificationMessage";

		log.info("init Ontology");
		List<DataModel> dataModels;

		log.info("No ontologies..adding");
		Ontology ontology = new Ontology();

		if (ontologyRepository.findByIdentification(ONTOLOGY_MASTER) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification(TICKET) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
			ontologyService.createOntology(ontology, null);
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_HELSINKIPOPULATION) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_TWEETSENTIMENT) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_GEOAIRQUALITY) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_CITYPOPULATION) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_AIRQUALITYGR2) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_AIRQUALITY) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_AIRCOMETER) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ONTOLOGY_TWINPROPERTIESTURBINE) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}

		if (ontologyRepository.findByIdentification(ONTOLOGY_TWINPROPERTIESSENSEHAT) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_NATIVENOTIFKEYS) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(ONTOLOGY_NOTIFICATIONMESSAGE) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (ontologyRepository.findByIdentification(SUPERMARKETS) == null && initSamples) {
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			final OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			final OntologyTimeSeriesProperty resultProperty = createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			final OntologyTimeSeriesProperty userProperty = createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			final OntologyTimeSeriesProperty apiProperty = createTimeSeriesProperty(oTS, "api", PropertyDataType.STRING,
					PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			final OntologyTimeSeriesProperty operationTypeProperty = createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(operationTypeProperty);

			final OntologyTimeSeriesProperty valueProperty = createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			final OntologyTimeSeriesWindow hourlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			final OntologyTimeSeriesWindow dailyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			final OntologyTimeSeriesWindow monthlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
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
			ontology.setContextDataEnabled(true);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			final OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			final OntologyTimeSeriesProperty resultProperty = createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			final OntologyTimeSeriesProperty userProperty = createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			final OntologyTimeSeriesProperty apiProperty = createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			final OntologyTimeSeriesProperty valueProperty = createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			final OntologyTimeSeriesWindow hourlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			final OntologyTimeSeriesWindow dailyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			final OntologyTimeSeriesWindow monthlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
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
			ontology.setContextDataEnabled(true);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			final OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			final OntologyTimeSeriesProperty resultProperty = createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			final OntologyTimeSeriesProperty userProperty = createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			final OntologyTimeSeriesProperty apiProperty = createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			final OntologyTimeSeriesProperty apiSource = createTimeSeriesProperty(oTS, "source",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiSource);

			final OntologyTimeSeriesProperty apiOntology = createTimeSeriesProperty(oTS, "ontology",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiOntology);

			final OntologyTimeSeriesProperty valueProperty = createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			final OntologyTimeSeriesWindow hourlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			final OntologyTimeSeriesWindow dailyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			final OntologyTimeSeriesWindow monthlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
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
			ontology.setContextDataEnabled(true);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			final OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			final OntologyTimeSeriesProperty resultProperty = createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			final OntologyTimeSeriesProperty userProperty = createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			final OntologyTimeSeriesProperty apiProperty = createTimeSeriesProperty(oTS, OPERATIONTYPE_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiProperty);

			final OntologyTimeSeriesProperty apiSource = createTimeSeriesProperty(oTS, "source",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(apiSource);

			final OntologyTimeSeriesProperty valueProperty = createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			final OntologyTimeSeriesWindow hourlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			final OntologyTimeSeriesWindow dailyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			final OntologyTimeSeriesWindow monthlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
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
			ontology.setContextDataEnabled(true);
			ontology.setRtdbDatasource(Ontology.RtdbDatasource.MONGO);
			dataModels = dataModelRepository.findByIdentification(TIMESERIE_STR);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}

			// La especializa como TimeSeries
			final OntologyTimeSeries oTS = new OntologyTimeSeries();
			oTS.setOntology(ontology);
			oTS.setId(ontology.getId());
			ontologyTimeSeriesRepository.save(oTS);

			// Crea las propiedades
			final OntologyTimeSeriesProperty resultProperty = createTimeSeriesProperty(oTS, RESULT_STR,
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(resultProperty);

			final OntologyTimeSeriesProperty userProperty = createTimeSeriesProperty(oTS, "user",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(userProperty);

			final OntologyTimeSeriesProperty ontologyProperty = createTimeSeriesProperty(oTS, "ontology",
					PropertyDataType.STRING, PropertyType.TAG);
			ontologyTimeSeriesPropertyRepository.save(ontologyProperty);

			final OntologyTimeSeriesProperty valueProperty = createTimeSeriesProperty(oTS, VALUE_STR,
					PropertyDataType.INTEGER, PropertyType.SERIE_FIELD);
			ontologyTimeSeriesPropertyRepository.save(valueProperty);

			// Crea las ventanas
			final OntologyTimeSeriesWindow hourlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.MINUTES, WindowType.HOURS);
			ontologyTimeSeriesWindowRepository.save(hourlyWindow);

			final OntologyTimeSeriesWindow dailyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.HOURS, WindowType.DAYS);
			ontologyTimeSeriesWindowRepository.save(dailyWindow);

			final OntologyTimeSeriesWindow monthlyWindow = createTimeSeriesWindow(oTS, AggregationFunction.SUM,
					FrecuencyUnit.DAYS, WindowType.MONTHS);
			ontologyTimeSeriesWindowRepository.save(monthlyWindow);

		}
		if (ontologyRepository.findByIdentification(QUERY_METRICS) == null) {
			ontology.setId("MASTER-Ontology-35");
			ontology.setDataModel(dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0));
			ontology.setJsonSchema(loadFromResources("examples/QueryMetrics.json"));
			ontology.setIdentification(QUERY_METRICS);
			ontology.setDescription("Query Metrics Entity");
			ontology.setMetainf(QUERY_METRICS);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(false);
			ontology.setUser(getUserAnalytics());
			ontology.setAllowsCypherFields(false);
			ontology.setContextDataEnabled(false);
			ontologyService.createOntology(ontology, null);
		}

		if (ontologyRepository.findByIdentification(ONTOLOGY_DATACLASS) == null && initSamples) {
			ontology = new Ontology();
			ontology.setId("MASTER-Ontology-36");
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_DataClass.json"));
			ontology.setDescription(
					"Ontology to test the preprocessing of data when inserting them, with the data class");
			ontology.setIdentification(ONTOLOGY_DATACLASS);
			ontology.setMetainf(ONTOLOGY_DATACLASS);
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontology.setEnableDataClass(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);
			ontology.setContextDataEnabled(true);
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
	}

	private OntologyTimeSeriesProperty createTimeSeriesProperty(OntologyTimeSeries oTS, String propertyName,
			PropertyDataType dataType, PropertyType type) {

		final OntologyTimeSeriesProperty resultProperty = new OntologyTimeSeriesProperty();
		resultProperty.setOntologyTimeSeries(oTS);
		resultProperty.setPropertyDataType(dataType);
		resultProperty.setPropertyName(propertyName);
		resultProperty.setPropertyType(type);

		return resultProperty;
	}

	private OntologyTimeSeriesWindow createTimeSeriesWindow(OntologyTimeSeries oTS,
			AggregationFunction aggregationFuncion, FrecuencyUnit frecuencyUnit, WindowType windowType) {
		final OntologyTimeSeriesWindow hourlyWindow = new OntologyTimeSeriesWindow();
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

		if (layerRepository.findByIdentification(IONASSETSCESIUM).isEmpty()) {

			layer = new Layer();
			layer.setId("MASTER-Layer-03");
			layer.setIdentification(IONASSETSCESIUM);
			layer.setDescription("Edificios modelados en 3D");
			layer.setPublic(true);
			layer.setExternalType("cesium_ion_asset");
			layer.setLayerTypeWms("96188");
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
			viewer.setJs(buildJSCode());
			viewer.setLatitude("28.134");
			viewer.setLongitude("-15.434");
			viewer.setHeight("4500");

			layer01.getViewers().add(viewer);
			layer02.getViewers().add(viewer);

			viewerRepository.save(viewer);

		}

		if (viewerRepository.findByIdentification("MASTER-Viewer-02").isEmpty()) {
			viewer.setId("MASTER-Viewer-02");
			viewer.setIdentification("Edificios3Dviewer");
			viewer.setDescription("Visor con modelado de edificios 3D");
			viewer.setPublic(true);
			viewer.setBaseLayer(baseLayerRepository.findByIdentification("esri.Streets.Labels.cs2").get(0));

			final Layer layer01 = layerRepository.findByIdentification(IONASSETSCESIUM).get(0);

			viewer.getLayers().add(layer01);

			viewer.setUser(getUserDeveloper());
			viewer.setJs(buildJSCodeCesium2());
			viewer.setLatitude("40.41");
			viewer.setLongitude("-3.69");
			viewer.setHeight("6500");

			layer01.getViewers().add(viewer);

			viewerRepository.save(viewer);

		}
	}

	private String buildJSCode() {
		final freemarker.template.Configuration cfg = new freemarker.template.Configuration(
				freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		final Map<String, Object> dataMap = new HashMap<>();

		try {
			final TemplateLoader templateLoader = new ClassTemplateLoader(getClass(), "/examples");

			cfg.setTemplateLoader(templateLoader);
			final Template indexViewerTemplate = cfg.getTemplate("viewer.ftl");

			dataMap.put("cesiumPath", webProjectPath + "cesium/Cesium1.60/Cesium.js");
			dataMap.put("widgetcss", webProjectPath + "cesium/Cesium1.60/Widgets/widgets.css");
			dataMap.put("basePath", basePath);

			// write the freemarker output to a StringWriter
			final StringWriter stringWriter = new StringWriter();
			indexViewerTemplate.process(dataMap, stringWriter);

			// get the String from the StringWriter
			return stringWriter.toString();
		} catch (final IOException e) {
			log.error("Error configuring the template loader. {}", e.getMessage());
		} catch (final TemplateException e) {
			log.error("Error processing the template loades. {}", e.getMessage());
		}
		return null;
	}

	private String buildJSCodeCesium2() {
		final freemarker.template.Configuration cfg = new freemarker.template.Configuration(
				freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		final Map<String, Object> dataMap = new HashMap<>();

		try {
			final TemplateLoader templateLoader = new ClassTemplateLoader(getClass(), "/examples");

			cfg.setTemplateLoader(templateLoader);
			final Template indexViewerTemplate = cfg.getTemplate("viewerCesium2.ftl");

			dataMap.put("cesiumPath", webProjectPath + "cesium/Cesium1.92/Cesium.js");
			dataMap.put("widgetcss", webProjectPath + "cesium/Cesium1.92/Widgets/widgets.css");
			dataMap.put("heatmap", webProjectPath + "cesium/CesiumHeatmap/CesiumHeatmap.js");
			dataMap.put("onesaitCesiumPath", webProjectPath + "onesaitCesium/v2");
			dataMap.put("basePath", basePath);

			// write the freemarker output to a StringWriter
			final StringWriter stringWriter = new StringWriter();
			indexViewerTemplate.process(dataMap, stringWriter);

			// get the String from the StringWriter
			return stringWriter.toString();
		} catch (final IOException e) {
			log.error("Error configuring the template loader. {}", e.getMessage());
		} catch (final TemplateException e) {
			log.error("Error processing the template loades. {}", e.getMessage());
		}
		return null;
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
			dataModels = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE);

			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyService.createOntology(ontology, null);
			}
		}
		if (clientPlatformOntologyRepository
				.findByClientPlatform(clientPlatformRepository.findByIdentification("DefaultClient")) != null) {
			final ClientPlatformOntology cpo = new ClientPlatformOntology();
			cpo.setId("MASTER-ClientPlatformOntology-3");
			cpo.setClientPlatform(clientPlatformRepository.findByIdentification("DefaultClient"));
			cpo.setOntology(ontologyRepository.findByIdentification(AIRPORT_STR));
			cpo.setAccess(Ontology.AccessType.INSERT);
			clientPlatformOntologyRepository.save(cpo);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
			ontology.setContextDataEnabled(true);
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
		Role type = new Role();
		Role typeSon = null;
		Role typeParent = new Role();
		final List<Role> types = roleRepository.findAll();
		if (types.isEmpty()) {
			try {

				log.info("No roles en tabla.Adding...");

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
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_EDGE_USER);
				type.setName("Edge User");
				type.setDescription("User of the Platform for Edge");
				type = roleRepository.save(type);
				//
				// UPDATE of the ROLE_EDGE_USER

				typeSon = type;
				if (roleRepository.findById(Role.Type.ROLE_USER.toString()).isPresent()) {
					typeParent = roleRepository.findById(Role.Type.ROLE_USER.toString()).orElse(null);
				}
				typeSon.setRoleParent(typeParent);
				roleRepository.save(typeSon);

				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_EDGE_DEVELOPER);
				type.setName("Edge Developer");
				type.setDescription("Developer of the Platform for Edge");
				type = roleRepository.save(type);
				//
				// UPDATE of the ROLE_EDGE_USER
				typeSon = type;
				if (roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).isPresent()) {
					typeParent = roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).orElse(null);
					typeSon.setRoleParent(typeParent);
					roleRepository.save(typeSon);
				}
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_EDGE_ADMINISTRATOR);
				type.setName("Edge Administrator");
				type.setDescription("Administrator of the Platform for Edge");
				type = roleRepository.save(type);
				//
				// UPDATE of the ROLE_EDGE_USER
				typeSon = type;
				if (roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()).isPresent()) {
					typeParent = roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()).orElse(null);
					typeSon.setRoleParent(typeParent);
					roleRepository.save(typeSon);
				}
				//
				// UPDATE of the ROLE_ANALYTICS
				if (roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()).isPresent()
						&& roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).isPresent()) {
					typeSon = roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()).orElse(null);
					typeParent = roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).orElse(null);
					if (typeSon == null) {
						throw new GenericOPException("ROLE_DATASCIENTIST NOT FOUND");
					}
					typeSon.setRoleParent(typeParent);
					roleRepository.save(typeSon);
				}

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
		if (!roleRepository.findById(Role.Type.ROLE_PLATFORM_ADMIN.name()).isPresent()) {
			type = new Role();
			type.setIdEnum(Role.Type.ROLE_PLATFORM_ADMIN);
			type.setName("Multitenant admin");
			type.setDescription("Administration of multitenant platform");
			roleRepository.save(type);
		}
	}

	public void initToken() {
		log.info("init token");
		final List<Token> tokens = tokenRepository.findAll();
		if (tokens.isEmpty()) {
			log.info("No Tokens, adding ...");
			if (clientPlatformRepository.findAll().isEmpty()) {
				throw new GenericRuntimeOPException("You need to create ClientPlatform before Token");
			}

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

			client = clientPlatformRepository.findByIdentification("DefaultClient");
			token = new Token();
			token.setId("MASTER-Token-4");
			token.setClientPlatform(client);
			token.setTokenName("690662b750274c8ba8748d7d55e9db5m");
			tokenRepository.save(token);

		}

	}

	public void initUserToken() {
		// to-do parameterize user token
		log.info("init user token");
		final String vertical = Tenant2SchemaMapper
				.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema());
		final List<UserToken> tokens = userTokenRepository.findAll();
		if (tokens.isEmpty()) {
			final List<User> userList = userCDBRepository.findAll();
			int i = 1;
			for (final Iterator<User> iterator = userList.iterator(); iterator.hasNext(); i++) {
				final User userCDB = iterator.next();
				final UserToken userToken = new UserToken();
				userToken.setId("MASTER-UserToken-" + i);
				userToken.setUser(userCDB);
				userToken.setCreatedAt(Calendar.getInstance().getTime());
				if (!MultitenancyContextHolder.getVerticalSchema().equals(Tenant2SchemaMapper.DEFAULT_SCHEMA)
						&& userCDB.getUserId().equals(ADMINISTRATOR + "_" + vertical)
						&& StringUtils.hasText(adminVerticalToken)) {
					userToken.setToken(adminVerticalToken);

				} else {
					userToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
				}
				try {
					userTokenRepository.save(userToken);
				} catch (final Exception e) {
					log.info("Could not create user token for user " + user.getUserId());
				}
			}
		}
	}

	@Autowired
	private TenantRepository tenantRepository;

	@Transactional
	public void initUser() {
		log.info("init UserCDB");
		final List<User> types = userCDBRepository.findAll();
		User type = null;
		if (types.isEmpty()) {
			try {
				// only one platform administrator per system config init (if ran multiple times
				// for different tenants)

				// if new tenant then create admin tenant user
				if (!MultitenancyContextHolder.getVerticalSchema().equals(Tenant2SchemaMapper.DEFAULT_SCHEMA)) {
					final String vertical = Tenant2SchemaMapper
							.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema());
					type = new User();
					type.setUserId(ADMINISTRATOR + "_" + vertical);
					type.setPassword(MAIN_PS_WD);
					type.setFullName("Administrator of vertical " + vertical);
					type.setEmail(vertical.toLowerCase() + "@onesaitplatform.com");
					type.setActive(true);
					type.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()).orElse(null));
					userCDBRepository.save(type);
				}

				log.info("No types en tabla.Adding...");
				type = new User();
				type.setUserId(ADMINISTRATOR);
				type.setPassword(MAIN_PS_WD);
				type.setFullName("A Administrator of the Platform");
				type.setEmail("administrator@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId(DEVELOPER);
				type.setPassword("SHA256(LoOY0z1pq+O2/h05ysBSS28kcFc8rSr7veWmyEi7uLs=)");
				type.setFullName("A Developer of the Platform.");
				type.setEmail("developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_developer");
				type.setPassword(SHA_STR);
				type.setFullName("Demo Developer of the Platform");
				type.setEmail("demo_developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("user");
				type.setPassword(SHA_STR);
				type.setFullName("Generic User of the Platform");
				type.setEmail("user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_user");
				type.setPassword(SHA_STR);
				type.setFullName("Demo User of the Platform");
				type.setEmail("demo_user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("analytics");
				type.setPassword(SHA_STR);
				type.setFullName("Generic Analytics User of the Platform");
				type.setEmail("analytics@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("partner");
				type.setPassword(SHA_STR);
				type.setFullName("Generic Partner of the Platform");
				type.setEmail("partner@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_PARTNER.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("sysadmin");
				type.setPassword(SHA_STR);
				type.setFullName("Generic SysAdmin of the Platform");
				type.setEmail("sysadmin@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_SYS_ADMIN.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("operations");
				type.setPassword("SHA256(DCxLLN6X4qrlIoI0vvuyEApc5xZWZgXgTfYkqyuhUTQ=)");
				type.setFullName("Operations of the Platform");
				type.setEmail("operations@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_OPERATIONS.toString()).orElse(null));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("dataviewer");
				type.setPassword(SHA_STR);
				type.setFullName("DataViewer User of the Platform");
				type.setEmail("dataviewer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()).orElse(null));
				userCDBRepository.save(type);

				type = new User();
				type.setUserId("anonymous");
				type.setPassword(SHA_STR);
				type.setFullName("Anonymous User of the Platform");
				type.setEmail("anonymous@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_USER.toString()).orElse(null));
				userCDBRepository.save(type);

				type = new User();
				type.setUserId("edge_administrator");
				type.setPassword(SHA_EDGE_STR);
				type.setFullName("EDGE Administrator User of the Platform");
				type.setEmail("edge_admin@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_EDGE_ADMINISTRATOR.toString()).orElse(null));
				userCDBRepository.save(type);

				type = new User();
				type.setUserId("edge_developer");
				type.setPassword(SHA_EDGE_STR);
				type.setFullName("EDGE Developer User of the Platform");
				type.setEmail("edge_developer@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_EDGE_DEVELOPER.toString()).orElse(null));
				userCDBRepository.save(type);

				type = new User();
				type.setUserId("edge_user");
				type.setPassword(SHA_EDGE_STR);
				type.setFullName("EDGE User User of the Platform");
				type.setEmail("edge_user@onesaitplatform.com");
				type.setActive(true);
				type.setRole(roleRepository.findById(Role.Type.ROLE_EDGE_USER.toString()).orElse(null));
				userCDBRepository.save(type);

			} catch (final Exception e) {
				log.error("Error UserCDB:" + e.getMessage());
				userCDBRepository.deleteAll();
				throw new GenericRuntimeOPException("Error creating users...ignoring creation rest of Tables");
			}
		}
		if (masterUserRepository.findByUserId(PLATFORM_ADMINISTRATOR) == null) {
			final User master = new User();
			master.setUserId(PLATFORM_ADMINISTRATOR);
			master.setRole(roleRepository.findById(Role.Type.ROLE_PLATFORM_ADMIN.name()).orElse(null));
			master.setPassword(MAIN_PS_WD);
			master.setFullName("Platform administrator");
			master.setEmail("platformadmin@onesaitplatform.com");
			userCDBRepository.save(master);
		}
	}

	private void initLineageRelations() {
		final ClientPlatform client = clientPlatformRepository.findByIdentification("DefaultClient");
		if (client != null && notebookRepository.findById("MASTER-Notebook-1").isPresent()
				&& pipelineRepository.findById("MASTER-Pipeline-1").isPresent()) {
			LineageRelations relation = new LineageRelations();
			relation.setTarget(client);
			relation.setSource(notebookRepository.findById("MASTER-Notebook-1").get());
			relation.setUser(getUserAnalytics());
			relation.setTargetGroup(Group.DIGITALCLIENT);
			relation.setSourceGroup(Group.NOTEBOOK);

			lineageRelationsRepository.save(relation);

			relation = new LineageRelations();
			relation.setTarget(client);
			relation.setSource(pipelineRepository.findById("MASTER-Pipeline-1").get());
			relation.setUser(getUserAnalytics());
			relation.setTargetGroup(Group.DIGITALCLIENT);
			relation.setSourceGroup(Group.DATAFLOW);

			lineageRelationsRepository.save(relation);
		}
	}

	@Autowired
	private MasterUserRepository masterUserRepository;

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
		// SSO-OAuth2-Plugin
		if (!marketAssetRepository.findById("MASTER-MarketAsset-22").isPresent()) {
			createMarketAsset("MASTER-MarketAsset-22", "sso-oauth-plugin", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.APPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/sso-oauth-plugin.json", null, null, "market/docs/sso-oauth-plugin.zip", null);
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

	public void initDataflowInstances() {
		log.info("init dataflow instances");
		final boolean hasDefault = dataflowInstanceRepository.findByDefaultInstance(true) != null;
		if (!hasDefault) {
			final DataflowInstance instance = new DataflowInstance();
			instance.setId("MASTER-DataflowInstance-1");
			instance.setIdentification("Default");
			instance.setId("MASTER-DataflowInstance-1");
			instance.setUrl("http://streamsets:18630");
			instance.setDefaultInstance(true);

			final DataflowCredential adminCredential = new DataflowCredential();
			adminCredential.setUser("admin");
			adminCredential.setPassword("admin");
			adminCredential.setType(DataflowCredential.Type.ADMINISTRATOR);

			instance.setAdminCredentials(adminCredential.getEncryptedCredentials());

			final DataflowCredential userCredential = new DataflowCredential();
			userCredential.setUser("user1");
			userCredential.setPassword("user1");
			userCredential.setType(DataflowCredential.Type.MANAGER);

			instance.setUserCredentials(userCredential.getEncryptedCredentials());

			final DataflowCredential guestCredential = new DataflowCredential();
			guestCredential.setUser("guest");
			guestCredential.setPassword("guest");
			guestCredential.setType(DataflowCredential.Type.GUEST);

			instance.setGuestCredentials(guestCredential.getEncryptedCredentials());

			dataflowInstanceRepository.save(instance);
		}
	}

	private void initPipeline() {
		if (pipelineRepository.findAll().isEmpty()) {
			final Pipeline pipe = new Pipeline();
			pipe.setIdentification("PipelineDefault");
			pipe.setInstance(dataflowInstanceRepository.findByDefaultInstance(true));
			pipe.setUser(getUserAnalytics());
			pipe.setId("MASTER-Pipeline-1");
			pipe.setPublic(false);
			pipe.setStatus(PipelineStatus.STOPPED);
			pipe.setIdstreamsets("PipelineDefault2e4803d3-d5e1-4e21-ae7d-26ea0e747d9d");
			pipelineRepository.save(pipe);

		}
	}

	public void initNotebookUserAccessType() {
		log.info("init notebook access type");
		final List<NotebookUserAccessType> notebookUat = notebookUserAccessTypeRepository.findAll();
		if (notebookUat.isEmpty()) {
			try {
				final NotebookUserAccessType p = new NotebookUserAccessType();
				p.setId(ACCESS_TYPE_ONE);
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
		final List<String> uatIds = pipelineUserAccessTypeRepository.findAll().stream()
				.map(PipelineUserAccessType::getId).collect(Collectors.toList());

		if (!uatIds.contains(ACCESS_TYPE_ONE)) {
			try {
				final PipelineUserAccessType p = new PipelineUserAccessType();
				p.setId(ACCESS_TYPE_ONE);
				p.setDescription("Edit Access");
				p.setName("EDIT");
				pipelineUserAccessTypeRepository.save(p);
			} catch (final Exception e) {
				log.info("Could not create dataflow access type by:" + e.getMessage());
			}
		}

		if (!uatIds.contains("ACCESS-TYPE-2")) {
			try {
				final PipelineUserAccessType p = new PipelineUserAccessType();
				p.setId("ACCESS-TYPE-2");
				p.setDescription("View Access");
				p.setName("VIEW");
				pipelineUserAccessTypeRepository.save(p);
			} catch (final Exception e) {
				log.info("Could not create dataflow access type by:" + e.getMessage());
			}
		}
	}

	public void initGadgetTemplateType() {
		log.info("init GadgetTemplateType");
		final String angularTemplateJS = "//Write your controller (JS code) code here\n\n//Focus here and F11 or F10 to full screen editor\n\n//This function will be call once to init components\nvm.initLiveComponent = function(){\n\n};\n\n//This function will be call when data change. On first execution oldData will be null\nvm.drawLiveComponent = function(newData, oldData){\n\n};\n\n//This function will be call on element resize\nvm.resizeEvent = function(){\n\n}\n\n//This function will be call when element is destroyed\nvm.destroyLiveComponent = function(){\n\n};\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};";
		final String angularTemplate = "<!-- Write your HTML <div></div> and CSS <style></style> here -->\n<!--Focus here and F11 or F10 to full screen editor-->";
		final String angularHeaders = "";
		final String vueTemplateJS = "//Write your Vue JSON controller code here\n\n//Focus here and F11 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\t\t\tvm.vueapp.$destroy();\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t},\n\t\tsendValue: vm.sendValue,\n\t\tsendFilter: vm.sendFilter\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n";
		final String vueTemplate = "<!--Focus here and F11 or F10 to full screen editor-->\n<!-- Write your CSS <style></style> here -->\n<div class=\"gadget-app\">\n<!-- Write your HTML <div></div> here -->\n</div>";
		final String vueHeaders = "";
		final String vueODSTemplateJS = "//Write your Vue with ODS JSON controller code here\n\n//Focus here and F11 or F10 to full screen editor\n\n//This function will be call once to init components\n\nvm.vueconfig = {\n\tel: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),\n\tdata:{\n\t\tds:[]\n\t},\n\tmethods:{\n\t\tdrawVueComponent: function(newData,oldData){\n\t\t\t//This will be call on new data\n\t\t},\n\t\tresizeEvent: function(){\n\t\t\t//Resize event\n\t\t},\n\t\tdestroyVueComponent: function(){\n\t\t\tvm.vueapp.$destroy();\n\t\t},\n\t\treceiveValue: function(data){\n\t\t\t//data received from datalink\n\t\t},\n\t\tsendValue: vm.sendValue,\n\t\tsendFilter: vm.sendFilter\n\t}\n}\n\n//Init Vue app\nvm.vueapp = new Vue(vm.vueconfig);\n";
		final String vueODSTemplate = "<!--Focus here and F11 or F10 to full screen editor-->\n<!-- Write your CSS <style></style> here -->\n<div class=\"gadget-app\">\n<!-- Write your HTML <div></div> here -->\n</div>";

		final String vueODSHeaders = "";
		final String reactTemplateJS = "//Write your controller (JS code) code here\n\n//Focus here and F11 or F10 to full screen editor\n\n//This function will be call for render the React Gadget\nfunction GadgetComponent(props) {\n    const ds = props.ds;\n    return React.createElement(\"div\", null, null);\n}\n\n//This function will be call on init event and when data arrives to the React Gadget\nvm.renderReactGadget = function(ds, old_ds){\n  ReactDOM.render(\n      React.createElement(GadgetComponent, { ds: ds || [] }), document.querySelector('#' + vm.id + ' reacttemplate' + ' .rootapp')\n  );\n}\n\n//This function will be call in destroy event of React Gadget\nvm.destroyReactGadget = function(){\n\n}\n\n//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\nvm.receiveValue = function(data){\n\n};";
		final String reactTemplate = "<!-- Write your React CSS Style Here \n<!--Focus here and F11 or F10 to full screen editor-->";
		final String reactHeaders = "<script src=\"/controlpanel/static/vendor/react/react.production.min.js\" crossorigin></script>\n<script src=\"/controlpanel/static/vendor/react/react-dom.production.min.js\" crossorigin></script>";
		final List<GadgetTemplateType> gadgetsTemplatesType = gadgetTemplateTypeRepository.findAll();
		if (gadgetsTemplatesType.isEmpty()) {
			log.info("No gadgetsTemplateType ...");

			GadgetTemplateType gadgetTemplateType = new GadgetTemplateType();
			gadgetTemplateType.setId("angularJS");
			gadgetTemplateType.setIdentification("AngularJS");
			gadgetTemplateType.setTemplate(angularTemplate);
			gadgetTemplateType.setTemplateJS(angularTemplateJS);
			gadgetTemplateType.setHeaderlibs(angularHeaders);
			gadgetTemplateTypeRepository.save(gadgetTemplateType);

			gadgetTemplateType = new GadgetTemplateType();
			gadgetTemplateType.setId("vueJS");
			gadgetTemplateType.setIdentification("VueJS");
			gadgetTemplateType.setTemplate(vueTemplate);
			gadgetTemplateType.setTemplateJS(vueTemplateJS);
			gadgetTemplateType.setHeaderlibs(vueHeaders);
			gadgetTemplateTypeRepository.save(gadgetTemplateType);

			gadgetTemplateType = new GadgetTemplateType();
			gadgetTemplateType.setId("vueJSODS");
			gadgetTemplateType.setIdentification("VueJS+ODS");
			gadgetTemplateType.setTemplate(vueODSTemplate);
			gadgetTemplateType.setTemplateJS(vueODSTemplateJS);
			gadgetTemplateType.setHeaderlibs(vueODSHeaders);
			gadgetTemplateTypeRepository.save(gadgetTemplateType);

			gadgetTemplateType = new GadgetTemplateType();
			gadgetTemplateType.setId("reactJS");
			gadgetTemplateType.setIdentification("ReactJS");
			gadgetTemplateType.setTemplate(reactTemplate);
			gadgetTemplateType.setTemplateJS(reactTemplateJS);
			gadgetTemplateType.setHeaderlibs(reactHeaders);
			gadgetTemplateTypeRepository.save(gadgetTemplateType);
		}
	}

	public void initGadgetTemplate() {
		log.info("init GadgetTemplate");
		final String templateJS = "//Write your controller (JS code) code here\r\n" + "\r\n"
				+ "//Focus here and F11 or F10 to full screen editor\r\n" + "\r\n"
				+ "//This function will be call once to init components\r\n" + "vm.initLiveComponent = function(){\r\n"
				+ "\r\n" + "};\r\n" + "\r\n"
				+ "//This function will be call when data change. On first execution oldData will be null\r\n"
				+ "vm.drawLiveComponent = function(newData, oldData){\r\n" + "\r\n" + "};\r\n" + "\r\n"
				+ "//This function will be call on element resize\r\n" + "vm.resizeEvent = function(){\r\n" + "\r\n"
				+ "}\r\n" + "\r\n" + "//This function will be call when element is destroyed\r\n"
				+ "vm.destroyLiveComponent = function(){\r\n" + "\r\n" + "};";
		final List<GadgetTemplate> gadgets = gadgetTemplateRepository.findAll();
		GadgetTemplate gadgetTemplate;
		if (gadgets.isEmpty()) {
			log.info("No gadgetsTemplate ...");

			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-1");
			gadgetTemplate.setIdentification("Select");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("angularJS");
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
			gadgetTemplate.setIdentification("SimpleValue");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("angularJS");
			gadgetTemplate.setDescription("template shows a value with its title and an icon");
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
					+ " <label class=\"card-count\">{{ds[0].<!--label-osp  name=\"ontologyfield\" type=\"ds_parameter\"-->}}</label>\r\n"
					+ "      \r\n" + "\r\n" + "</div>\r\n" + "\r\n");

			gadgetTemplate.setTemplateJS(templateJS);
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);

			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-3");
			gadgetTemplate.setIdentification("ChartBubble");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("angularJS");
			gadgetTemplate.setDescription("this template creates a chart bubble");
			gadgetTemplate.setTemplate("<span ng-init=\"\r\n" + "    cdata=[];\r\n"
					+ "    cdatasetOverride={label: '<!--label-osp  name=\"seriesLabel\" type=\"text\"-->'};\r\n"
					+ "    options={\r\n" + "      legend: {display: true}, \r\n"
					+ "      maintainAspectRatio: false, \r\n" + "      responsive: true,\r\n" + "      scales: {\r\n"
					+ "        xAxes: [{\r\n" + "          display: true,\r\n" + "          scaleLabel: {\r\n"
					+ "              labelString: '<!--label-osp  name=\"xAxeslabel\" type=\"text\"-->',\r\n"
					+ "              display: true\r\n" + "          }\r\n" + "        }],\r\n"
					+ "        yAxes: [{\r\n" + "          display: true,\r\n" + "          scaleLabel: {\r\n"
					+ "              labelString: '<!--label-osp  name=\"yAxeslabel\" type=\"text\"-->',\r\n"
					+ "              display: true\r\n" + "          }\r\n" + "        }]\r\n" + "      }\r\n"
					+ "    };\"/>\r\n" + "\r\n" + "    \r\n" + "<span ng-repeat=\"inst in ds\">\r\n"
					+ "  <span ng-init=\"cdata.push({x:inst.<!--label-osp  name=\"xAxesData\" type=\"ds_parameter\"-->, y:inst.<!--label-osp  name=\"yAxesData\" type=\"ds_parameter\"-->, r:inst.<!--label-osp  name=\"radioData\" type=\"ds_parameter\"-->})\"></span>\r\n"
					+ "</span>\r\n" + "\r\n" + "<div style=\"height:calc(100% - 50px)\">\r\n" + "\r\n"
					+ "<canvas class=\"chart chart-bubble\" chart-data=\"cdata\"\r\n"
					+ "                  chart-colors=\"colors\" chart-options=\"options\" chart-series=\"cseries\" chart-labels=\"clabels\" chart-dataset-override=\"cdatasetOverride\"></canvas>\r\n"
					+ "\r\n");

			gadgetTemplate.setTemplateJS(templateJS);
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-4").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-4");
			gadgetTemplate.setIdentification("ReactMaterialList");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("reactJS");
			gadgetTemplate.setHeaderlibs(
					"<script src=\"/controlpanel/static/vendor/react/react.production.min.js\" crossorigin></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/react/react-dom.production.min.js\" crossorigin></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/material-ui/material-ui.production.min.js\" crossorigin></script>\n"
							+ " ");
			gadgetTemplate.setDescription("react js material list need to import dependencies");
			gadgetTemplate.setTemplate("<style>\n" + "    .MuiListItemText-root{\n" + "        background: #d8eaff\n"
					+ "    }\n" + "</style>");

			gadgetTemplate.setTemplateJS("\n" + "const {\n" + "  List,\n" + "  ListItem,\n" + "  ListItemText\n"
					+ "} = MaterialUI;\n" + "\n"
					+ "var key = /*label-osp  name=\"key\" type=\"ds_parameter\"*/\"dummyk\"\n"
					+ "var value = /*label-osp  name=\"value\" type=\"ds_parameter\"*/\"dummyv\"\n" + "\n" + "\n"
					+ "function GadgetComponent(props) {\n" + "    const ds = props.ds;\n"
					+ "    const listItems = ds.map(inst => React.createElement(ListItem, {button: true}, React.createElement(ListItemText, {\n"
					+ "        primary: inst[key]\n" + "    }), inst[value]));\n"
					+ "    return React.createElement(List, null, listItems);\n" + "}\n" + "\n"
					+ "vm.renderReactGadget = function(ds, old_ds){\n" + "  ReactDOM.render(\n"
					+ "      React.createElement(GadgetComponent, { ds: ds || [{dummyk:1,dummyv:2}] }), document.querySelector('#' + vm.id + ' reacttemplate' + ' .rootapp')\n"
					+ "  );\n" + "}\n" + "\n" + "vm.destroyReactGadget = function(){\n" + "\n" + "}");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-5").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-5");
			gadgetTemplate.setIdentification("VueEchartLineorBar");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("vueJS");
			gadgetTemplate.setHeaderlibs(
					"<!--Write here your html code to load required libs and init scripts for your component\n"
							+ "    When you use it into some Dashboard you'll need to include it in header libs section -->\n"
							+ "    <script src=\"/controlpanel/static/vendor/echarts-410/echarts.min.js\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/vue-echarts-402/index.js\"></script>");
			gadgetTemplate.setDescription("vue echart component from datasource bar or line");
			gadgetTemplate.setTemplate("<!-- Write your HTML <div></div> and CSS <style></style> here -->\n"
					+ "<!--Focus here and F11 or F10 to full screen editor-->\n"
					+ "<v-chart :options=\"chartConfig\"></v-chart>");

			gadgetTemplate.setTemplateJS("//Write your Vue with JSON controller code here\n" + "\n"
					+ "//Focus here and F11 or F10 to full screen editor\n" + "\n"
					+ "var color = /*select-osp  name=\"ColorSerie\" type=\"ds\" options=\"red,blue,green,orange,yellow,black,purple,pink\"*/ 'red'\n"
					+ "var typechart = /*select-osp  name=\"ChartType\" type=\"ds\" options=\"bar,line\"*/ 'line'\n"
					+ "var key = /*label-osp  name=\"Key\" type=\"ds_parameter\"*/ \"key\"\n"
					+ "var value = /*label-osp  name=\"Value\" type=\"ds_parameter\"*/ \"value\"\n" + "\n"
					+ "//This function will be call once to init components\n" + "vm.vueconfig = {\n"
					+ "    el: document.getElementById(vm.id).querySelector('vuetemplate'),\n" + "    data: {\n"
					+ "        ds: [{\"key\":\"A\",\"value\":123},{\"key\":\"B\",\"value\":143}]\n" + "    },\n"
					+ "    computed: {\n" + "        chartConfig() {\n" + "            return {\n"
					+ "                xAxis: {\n" + "                    type: 'category',\n"
					+ "                    data: this.ds.map(inst => inst[key])\n" + "                },\n"
					+ "                yAxis: {\n" + "                    type: 'value'\n" + "                },\n"
					+ "                series: [{\n" + "                    data: this.ds.map(inst => inst[value]),\n"
					+ "                    type: typechart,\n" + "                    color: color\n"
					+ "                }]\n" + "            };\n" + "        }\n" + "    },\n" + "    methods: {\n"
					+ "        drawVueComponent: function (newData, oldData) {\n"
					+ "            //This will be call on new data\n" + "        },\n"
					+ "        resizeEvent: function () {\n" + "            //Resize event\n" + "        },\n"
					+ "        destroyVueComponent: function () {\n" + "            vm.vueapp.$destroy();\n"
					+ "        },\n" + "        receiveValue: function (data) {\n"
					+ "            //data received from datalink\n" + "        },\n"
					+ "        sendValue: vm.sendValue,\n" + "        sendFilter: vm.sendFilter\n" + "    },\n"
					+ "\tcomponents: {\n" + "\t\t'v-chart':VueECharts\n" + "\t}\n" + "}\n" + "\n" + "//Init Vue app\n"
					+ "vm.vueapp = new Vue(vm.vueconfig);\n");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-6").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-6");
			gadgetTemplate.setIdentification("Vue ODS Select");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("vueJSODS");
			gadgetTemplate.setHeaderlibs(
					"<!--Write here your html code to load required libs and init scripts for your component\n"
							+ "    When you use it into some Dashboard you'll need to include it in header libs section -->\n"
							+ "    <script src=\"/controlpanel/static/vendor/echarts-410/echarts.min.js\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/vue-echarts-402/index.js\"></script>");
			gadgetTemplate.setDescription("vue ods select component from datasource");
			gadgetTemplate.setTemplate("<!-- Write your HTML <div></div> and CSS <style></style> here -->\n"
					+ "<!--Focus here and F11 or F10 to full screen editor-->\n" + "<ods-select\n"
					+ "  v-model=\"value\"\n" + "  :placeholder=\"select\"\n" + "  @change=\"sendFilter(key,value)\"\n"
					+ "  >\n" + "  <ods-option\n" + "    v-for=\"item in ds\"\n" + "    :key=\"item[key]\"\n"
					+ "    :label=\"item[label]\"\n" + "    :value=\"item[key]\">\n" + "  </ods-option>\n"
					+ "</ods-select>");

			gadgetTemplate.setTemplateJS("//Write your Vue ODS JSON controller code here\n" + "\n"
					+ "//Focus here and F11 or F10 to full screen editor\n" + "\n"
					+ "//This function will be call once to init components\n" + "\n"
					+ "var key = /*label-osp  name=\"KeySelect\" type=\"ds_parameter\"*/ \"value\"\n"
					+ "var label =  /*label-osp  name=\"ValueSelect\" type=\"ds_parameter\"*/ \"label\"\n"
					+ "var select = /*label-osp  name=\"PlaceHolder\" type=\"text\"*/ \"Select\"\n" + "\n"
					+ "vm.vueconfig = {\n" + "    el: document.getElementById(vm.id).querySelector(\"vuetemplate\"),\n"
					+ "    data: {\n" + "        ds: [{\n" + "            value: \"Option1\",\n"
					+ "            label: \"Option1\"\n" + "        }, {\n" + "            value: \"Option2\",\n"
					+ "            label: \"Option2\"\n" + "        }, {\n" + "            value: \"Option3\",\n"
					+ "            label: \"Option3\"\n" + "        }, {\n" + "            value: \"Option4\",\n"
					+ "            label: \"Option4\"\n" + "        }, {\n" + "            value: \"Option5\",\n"
					+ "            label: \"Option5\"\n" + "        }],\n" + "        value: \"\",\n"
					+ "        key: key,\n" + "        label: label,\n" + "        select: select\n" + "    },\n"
					+ "    methods: {\n" + "        drawVueComponent: function (newData, oldData) {\n"
					+ "            //This will be call on new data\n" + "        },\n"
					+ "        resizeEvent: function () {\n" + "            //Resize event\n" + "        },\n"
					+ "        destroyVueComponent: function () {\n" + "\n" + "        },\n"
					+ "        receiveValue: function (data) {\n" + "            //data received from datalink\n"
					+ "        },\n" + "        sendValue: vm.sendValue,\n" + "        sendFilter: vm.sendFilter\n"
					+ "    }\n" + "}\n" + "\n" + "//Init Vue app\n" + "vm.vueapp = new Vue(vm.vueconfig);\n");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-9").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-9");
			gadgetTemplate.setIdentification("VueEchartMixed");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("vueJS");
			gadgetTemplate.setHeaderlibs(
					"<!--Write here your html code to load required libs and init scripts for your component\n"
							+ "    When you use it into some Dashboard you'll need to include it in header libs section -->\n"
							+ "    <script src=\"/controlpanel/static/vendor/echarts-410/echarts.min.js\"></script>\n"
							+ "<script src=\"/controlpanel/static/vendor/vue-echarts-402/index.js\"></script>");
			gadgetTemplate.setDescription("vue echart mixed chart with parameters");
			gadgetTemplate.setTemplate("<!--Focus here and F11 or F10 to full screen editor-->\n"
					+ "<!-- Write your CSS <style></style> here -->\n" + "<style>\n" + "    .fullsize {\n"
					+ "        height: 100%;\n" + "        width: 100%;\n" + "    }\n" + "</style>\n"
					+ "<div class=\"gadget-app\">\n" + "    <!-- Write your HTML <div></div> here -->\n"
					+ "    <v-chart class=\"fullsize\" :options=\"chartConfig\" autoresize loading></v-chart>\n"
					+ "</div>");

			gadgetTemplate.setTemplateJS("//Write your Vue with JSON controller code here\n" + "\n"
					+ "//Focus here and F11 or F10 to full screen editor\n" + "\n"
					+ "function findValues(jsonData, path) {\n"
					+ "    if (!(jsonData instanceof Object) || typeof (path) === \"undefined\") {\n"
					+ "        throw \"Not valid argument:jsonData:\" + jsonData + \", path:\" + path;\n" + "    }\n"
					+ "    path = path.replace(/\\[(\\w+)\\]/g, '.$1'); // convert indexes to properties\n"
					+ "    path = path.replace(/^\\./, ''); // strip a leading dot\n"
					+ "    var pathArray = path.split('.');\n"
					+ "    for (var i = 0, n = pathArray.length; i < n; ++i) {\n" + "        var key = pathArray[i];\n"
					+ "        if (key in jsonData) {\n" + "            if (jsonData[key] !== null) {\n"
					+ "                jsonData = jsonData[key];\n" + "            } else {\n"
					+ "                return null;\n" + "            }\n" + "        } else {\n"
					+ "            return key;\n" + "        }\n" + "    }\n" + "    return jsonData;\n" + "}\n" + "\n"
					+ "function calculateSeries(data) {\n"
					+ "    return vm.tparams.parameters.series.map(function (s) {\n" + "        var that = this\n"
					+ "        var ds = ds;\n" + "        var s = {\n" + "            type: s.type,\n"
					+ "            name: s.label,\n" + "            yAxisIndex: s.yAxis,\n"
					+ "            data: data.map(inst => findValues(inst, s.field)),\n"
					+ "            color: s.color\n" + "        }\n" + "        if (s.type == 'point') {\n"
					+ "            s.type = 'line'\n" + "            s.lineStyle = {\n" + "                width: 0\n"
					+ "            }\n" + "        }\n" + "        return s;\n" + "    })\n" + "}\n" + "\n"
					+ "//This function will be call once to init components\n" + "vm.vueconfig = {\n"
					+ "    el: document.querySelector('#' + vm.id + ' .gadget-app'),\n" + "    data: {\n"
					+ "        ds: []\n" + "    },\n" + "    computed: {\n" + "        chartConfig() {\n"
					+ "            return {\n" + "                legend: {\n"
					+ "                    show: vm.tparams.parameters.general.showLegend\n" + "                },\n"
					+ "                grid: {\n"
					+ "                    left: Math.max(0, ...vm.tparams.parameters.axes.yAxis.filter(axis => axis.position === 'left').map(axis => parseInt(axis.offset?axis.offset:0))) + 60,\n"
					+ "                    right: Math.max(0, ...vm.tparams.parameters.axes.yAxis.filter(axis => axis.position === 'right').map(axis => parseInt(axis.offset?axis.offset:0))) + 60\n"
					+ "                },\n" + "                xAxis: {\n" + "                    type: 'category',\n"
					+ "                    data: this.ds.map(inst => findValues(inst, vm.tparams.parameters.axes.xAxis.field)),\n"
					+ "                    name: vm.tparams.parameters.axes.xAxis.label\n" + "                },\n"
					+ "                yAxis: vm.tparams.parameters.axes.yAxis.map(function (yAxis) {\n"
					+ "                    var yAxis = {\n" + "                        id: yAxis.id,\n"
					+ "                        position: yAxis.position,\n"
					+ "                        type: yAxis.type,\n" + "                        name: yAxis.label,\n"
					+ "                        offset: parseInt(yAxis.offset?yAxis.offset:0)\n"
					+ "                    }\n" + "                    if (yAxis.min) {\n"
					+ "                        yAxis['min'] = yAxis.min\n" + "                    }\n"
					+ "                    if (yAxis.max) {\n" + "                        yAxis['max'] = yAxis.max\n"
					+ "                    }\n" + "                    return yAxis;\n" + "                }),\n"
					+ "                series: calculateSeries(this.ds),\n" + "                tooltip: {\n"
					+ "                    show: vm.tparams.parameters.general.showTooltip,\n"
					+ "                    axisPointer: {\n" + "                        type: 'cross'\n"
					+ "                    },\n" + "                    trigger: 'axis'\n" + "                },\n"
					+ "                dataZoom: [\n" + "                    {\n"
					+ "                        show: vm.tparams.parameters.axes.xAxis.showZoom,\n"
					+ "                        realtime: true\n" + "                    }\n" + "                ]\n"
					+ "            }\n" + "        }\n" + "    },\n" + "    methods: {\n"
					+ "        drawVueComponent: function (newData, oldData) {\n"
					+ "            //This will be call on new data\n" + "        },\n"
					+ "        resizeEvent: function () {\n" + "            //Resize event\n" + "        },\n"
					+ "        destroyVueComponent: function () {\n" + "            vm.vueapp.$destroy();\n"
					+ "        },\n" + "        receiveValue: function (data) {\n"
					+ "            //data received from datalink\n" + "        },\n"
					+ "        sendValue: vm.sendValue,\n" + "        sendFilter: vm.sendFilter\n" + "    },\n"
					+ "    components: {\n" + "        'v-chart': VueECharts\n" + "    }\n" + "}\n" + "\n"
					+ "vm.drawLiveComponent = function () { }\n" + "\n" + "//Init Vue app\n"
					+ "vm.vueapp = new Vue(vm.vueconfig);\n" + "");
			gadgetTemplate.setConfig(
					"{\"gform\":[{\"id\":8,\"type\":\"section\",\"elements\":[{\"id\":4,\"type\":\"checkbox\",\"name\":\"showLegend\",\"default\":true,\"title\":\"Show Legend\"},{\"id\":4,\"type\":\"checkbox\",\"name\":\"showTooltip\",\"default\":true,\"title\":\"Show Tooltip\"}],\"name\":\"general\",\"title\":\"General\"},{\"id\":8,\"type\":\"section\",\"elements\":[{\"id\":8,\"type\":\"section\",\"elements\":[{\"id\":1,\"type\":\"input-text\",\"name\":\"label\",\"default\":\"\",\"title\":\"\"},{\"id\":6,\"type\":\"ds-field\",\"name\":\"field\"},{\"id\":4,\"type\":\"checkbox\",\"name\":\"showZoom\",\"default\":false,\"title\":\"Show Zoom\"}],\"name\":\"xAxis\",\"title\":\"X Axis \"},{\"id\":9,\"type\":\"section-array\",\"elements\":[{\"id\":10,\"type\":\"autogenerate-id\",\"name\":\"id\",\"prefix\":\"\"},{\"id\":1,\"type\":\"input-text\",\"name\":\"label\",\"default\":\"\"},{\"id\":3,\"type\":\"selector\",\"name\":\"position\",\"options\":[{\"value\":\"left\",\"text\":\"\"},{\"value\":\"right\",\"text\":\"\"}],\"default\":\"left\"},{\"id\":3,\"type\":\"selector\",\"name\":\"type\",\"options\":[{\"value\":\"value\",\"text\":\"Linear\"},{\"value\":\"log\",\"text\":\"Logarithmic\"}],\"default\":\"value\"},{\"id\":2,\"type\":\"input-number\",\"name\":\"min\",\"default\":\"0\"},{\"id\":2,\"type\":\"input-number\",\"name\":\"max\",\"default\":\"\"},{\"id\":2,\"type\":\"input-number\",\"name\":\"offset\",\"default\":\"0\"}],\"name\":\"yAxis\",\"title\":\"Y Axes\"}],\"name\":\"axes\",\"title\":\"Axes Config\"},{\"id\":9,\"type\":\"section-array\",\"elements\":[{\"id\":6,\"type\":\"ds-field\",\"name\":\"field\"},{\"id\":1,\"type\":\"input-text\",\"name\":\"label\",\"default\":\"\"},{\"id\":5,\"type\":\"color-picker\",\"name\":\"color\",\"default\":\"rgba(30, 144, 255, 1)\"},{\"id\":3,\"type\":\"selector\",\"name\":\"type\",\"options\":[{\"value\":\"bar\",\"text\":\"\"},{\"value\":\"line\",\"text\":\"\"},{\"value\":\"point\",\"text\":\"\"}],\"default\":\"line\"},{\"id\":11,\"type\":\"model-selector\",\"name\":\"yAxis\",\"path\":\"axes.yAxis.*.id\"}],\"name\":\"series\",\"title\":\"Data Series\"}]}");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-10").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-10");
			gadgetTemplate.setIdentification("ScatterMap");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("angularJS");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("Leaflet Scatter map with variable size");
			gadgetTemplate.setTemplate("<!-- Write your HTML <div></div> and CSS <style></style> here -->\n"
					+ "<!--Focus here and F11 to full screen editor-->\n" + "<style>	\n" + "    .gadget-app {\n"
					+ "			width: 100%;\n" + "			height: 100%;\n" + "	}\n" + "</style>\n"
					+ "<div class='gadget-app'></div>");

			gadgetTemplate.setTemplateJS("//Write your controller (JS code) code here\n"
					+ "//Focus here and F11 to full screen editor\n" + "\n" + "function getMinMax(data, field) {\n"
					+ "    var minmax;\n" + "    if (!data.length) {\n" + "        console.warn(\"no data\")\n"
					+ "        var minmax = {\n" + "            min: 0,\n" + "            max: 0\n" + "        }\n"
					+ "    } else {\n" + "        var minmax = {\n" + "            min: data[0][field],\n"
					+ "            max: data[0][field]\n" + "        }\n"
					+ "        for (var i=1;i < data.length; i++) {\n"
					+ "            minmax.min = Math.min(minmax.min, data[i][field]);\n"
					+ "            minmax.max = Math.max(minmax.max, data[i][field]);\n" + "        }\n" + "    }\n"
					+ "    return minmax;\n" + "}\n" + "\n" + "function valueToSize (min,max,value,minsize,maxsize) {\n"
					+ "    var range = max-min;\n" + "    var rangesize = maxsize-minsize;\n"
					+ "    return (value/range)*rangesize + parseFloat(minsize)\n" + "}\n" + "\n"
					+ "//This function will be call once to init components\n"
					+ "vm.initLiveComponent = function () {\n"
					+ "    var mapElem = document.querySelector('#' + vm.id + ' .gadget-app')\n"
					+ "    vm.map = L.map(mapElem, {\n"
					+ "        center: [parseFloat(vm.tparams.parameters.center.latitude), parseFloat(vm.tparams.parameters.center.longitude)],\n"
					+ "        zoom: parseInt(vm.tparams.parameters.center.zoom)\n" + "    });\n"
					+ "    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n"
					+ "        attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n"
					+ "    }).addTo(vm.map);\n" + "    vm.map.createPane('markers');\n" + "};\n" + "\n"
					+ "//This function will be call when data change. On first execution oldData will be null\n"
					+ "vm.drawLiveComponent = function (newData, oldData) {\n"
					+ "    var minmax = getMinMax(newData, vm.tparams.parameters.point.value);\n"
					+ "    newData.map(function (d) {\n"
					+ "        var cmarker = new L.CircleMarker([d[vm.tparams.parameters.point.latField], d[vm.tparams.parameters.point.lonField]], {\n"
					+ "            pane: \"markers\",\n" + "            color: vm.tparams.parameters.point.color,\n"
					+ "            fillColor: vm.tparams.parameters.point.color,\n" + "            fillOpacity: 0.5,\n"
					+ "            stroke: false,\n"
					+ "            radius: parseFloat(valueToSize(minmax.min,minmax.max,d[vm.tparams.parameters.point.value],vm.tparams.parameters.point.size.min,vm.tparams.parameters.point.size.max))\n"
					+ "        });\n" + "        cmarker.addTo(vm.map).bindPopup(\n"
					+ "            d[vm.tparams.parameters.point.title] + \"\\n\" + d[vm.tparams.parameters.point.value]\n"
					+ "        );\n" + "    })\n" + "};\n" + "\n" + "//This function will be call on element resize\n"
					+ "vm.resizeEvent = function () {\n" + "\n" + "}\n" + "\n"
					+ "//This function will be call when element is destroyed\n"
					+ "vm.destroyLiveComponent = function () {\n" + "\n" + "};\n" + "\n"
					+ "//This function will be call when receiving a value from vm.sendValue(idGadgetTarget,data)\n"
					+ "vm.receiveValue = function (data) {\n" + "\n" + "};");
			gadgetTemplate.setConfig(
					"{\"gform\":[{\"id\":8,\"type\":\"section\",\"elements\":[{\"id\":2,\"type\":\"input-number\",\"name\":\"latitude\",\"default\":\"0\",\"min\":\"-90\",\"max\":\"90\"},{\"id\":2,\"type\":\"input-number\",\"name\":\"longitude\",\"default\":\"0\",\"min\":\"-180\",\"max\":\"180\"},{\"id\":2,\"type\":\"input-number\",\"name\":\"zoom\",\"default\":\"0\",\"min\":\"0\"}],\"name\":\"center\",\"title\":\"Center\"},{\"id\":8,\"type\":\"section\",\"elements\":[{\"id\":5,\"type\":\"color-picker\",\"name\":\"color\",\"default\":\"rgba(30, 144, 255, 1)\"},{\"id\":6,\"type\":\"ds-field\",\"name\":\"latField\",\"title\":\"Latitude Field\"},{\"id\":6,\"type\":\"ds-field\",\"name\":\"lonField\",\"title\":\"Longitude Field\"},{\"id\":6,\"type\":\"ds-field\",\"name\":\"title\",\"title\":\"Title\"},{\"id\":6,\"type\":\"ds-field\",\"name\":\"value\",\"title\":\"Value\"},{\"id\":8,\"type\":\"section\",\"elements\":[{\"id\":2,\"type\":\"input-number\",\"name\":\"min\",\"default\":\"20\",\"min\":\"1\"},{\"id\":2,\"type\":\"input-number\",\"name\":\"max\",\"default\":\"35\",\"min\":\"1\"}],\"name\":\"size\"}],\"name\":\"point\"}]}");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-11").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("MASTER-GadgetTemplate-11");
			gadgetTemplate.setIdentification("Simple Value (Vue)");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("vueJS");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("Vue simple value with increment from previous value os datasource");
			gadgetTemplate.setTemplate("<!-- Write your HTML <div></div> and CSS <style></style> here -->\n"
					+ "<!--Focus here and F11 to full screen editor-->\n" + "<style>\n" + "	.card-count {\n"
					+ "		font-weight: bold;\n" + "		font-size: -webkit-xxx-large;\n"
					+ "		padding-left: 20px;\n" + "	}\n" + "\n" + "	.card-count-perc {\n"
					+ "		font-size: -webkit-large;\n" + "		padding-left: 10px;\n" + "	}\n" + "\n"
					+ "	.card-title {\n" + "		color: #000000;\n" + "		font-size: x-large;\n" + "	}\n" + "\n"
					+ "	.card-green {\n" + "		color: green;\n" + "	}\n" + "\n" + "	.my-card {\n"
					+ "		padding: 15px;\n" + "	}\n" + "\n" + "	.card-icon {\n" + "		padding-top: 0px;\n"
					+ "		padding-left: 0px;\n" + "		padding-bottom: 25px;\n" + "		padding-right: 25px;\n"
					+ "	}\n" + "\n" + "	.fullsize {\n" + "		height: 100%;\n" + "		width: 100%;\n" + "	}\n"
					+ "</style>\n" + "<div class=\"gadget-app\">\n" + "	<div class=\"my-card\">\n"
					+ "		<!--<md-icon class=\"card-icon\" style=\"font-size:35px\"></md-icon>-->\n"
					+ "		<label class=\"card-title\">{{title}}</label><br>\n"
					+ "		<label v-bind:style=\"'color:' + colorField\" class=\"card-count\">{{ds[0][field]}}</label>\n"
					+ "		<label v-bind:style=\"'color:' + increase.color\" class=\"card-count-perc\">{{' (' + increase.value + '%)'}}</label>\n"
					+ "	</div>\n" + "</div>");

			gadgetTemplate.setTemplateJS("//Write your Vue JSON controller code here\n" + "\n"
					+ "//Focus here and F11 to full screen editor\n" + "\n"
					+ "//This function will be call once to init components\n" + "\n"
					+ "var title = vm.tparams.parameters.title;\n" + "var field = vm.tparams.parameters.field;\n"
					+ "var colorField = vm.tparams.parameters.colorField;\n" + "\n" + "vm.vueconfig = {\n"
					+ "	el: document.querySelector('#' + vm.id + ' .gadget-app'),\n" + "	data:{\n"
					+ "		ds:[],\n" + "        title: title,\n" + "        field: field,\n"
					+ "		colorField: colorField\n" + "	},\n" + "	computed: {\n" + "        increase() {\n"
					+ "			if (this.ds.length<2) {\n" + "				return {\n"
					+ "					value: \"+\" + 0,\n" + "					color: 'orange'\n"
					+ "				}\n" + "			} else {\n"
					+ "				if (!(isNaN(this.ds[0][this.field]) || isNaN(this.ds[1][this.field]))) {\n"
					+ "					var value = 100-((this.ds[0][this.field]/this.ds[1][this.field])*100)\n"
					+ "					return {\n" + "						value: value.toFixed(2),\n"
					+ "						color: (value>0?'green':(value==0?'orange':'red'))\n"
					+ "					}\n" + "				} else {\n" + "					return {\n"
					+ "						value: \"?\",\n" + "						color: 'orange'\n"
					+ "					}\n" + "				}\n" + "			}\n" + "		}\n"
					+ "	},		\n" + "	methods:{\n" + "		drawVueComponent: function(newData,oldData){\n"
					+ "			//This will be call on new data\n" + "		},\n" + "		resizeEvent: function(){\n"
					+ "			//Resize event\n" + "		},\n" + "		destroyVueComponent: function(){\n"
					+ "			vm.vueapp.$destroy();\n" + "		},\n" + "		receiveValue: function(data){\n"
					+ "			//data received from datalink\n" + "		},\n" + "		sendValue: vm.sendValue,\n"
					+ "		sendFilter: vm.sendFilter\n" + "	}\n" + "}\n" + "\n" + "//Init Vue app\n"
					+ "vm.vueapp = new Vue(vm.vueconfig);\n" + "");
			gadgetTemplate.setConfig(
					"{\"gform\":[{\"name\":\"title\",\"type\":\"input-text\",\"title\":\"Title of KPI\"},{\"name\":\"field\",\"type\":\"ds-field\",\"title\":\"Field to show\"},{\"id\":5,\"type\":\"color-picker\",\"name\":\"colorField\",\"default\":\"rgba(0, 0, 0, 1)\",\"title\":\"Field Color\"}]}");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}

	}

	public void initGadgetTemplateInstances() {
		log.info("init GadgetTemplate instances");

		GadgetTemplate gadgetTemplate;

		// add gadget templates dummy for gadgets
		if (gadgetTemplateRepository.findById("line").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("line");
			gadgetTemplate.setIdentification("line");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("bar").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("bar");
			gadgetTemplate.setIdentification("bar");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("mixed").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("mixed");
			gadgetTemplate.setIdentification("mixed");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("pie").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("pie");
			gadgetTemplate.setIdentification("pie");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("wordcloud").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("wordcloud");
			gadgetTemplate.setIdentification("wordcloud");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("map").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("map");
			gadgetTemplate.setIdentification("map");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("radar").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("radar");
			gadgetTemplate.setIdentification("radar");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("table").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("table");
			gadgetTemplate.setIdentification("table");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		if (gadgetTemplateRepository.findById("datadiscovery").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
			gadgetTemplate.setId("datadiscovery");
			gadgetTemplate.setIdentification("datadiscovery");
			gadgetTemplate.setPublic(true);
			gadgetTemplate.setType("base");
			gadgetTemplate.setHeaderlibs("");
			gadgetTemplate.setDescription("dummy template that serves as a type for gadgets");
			gadgetTemplate.setTemplate("");
			gadgetTemplate.setTemplateJS("");
			gadgetTemplate.setUser(getUserAdministrator());
			gadgetTemplateRepository.save(gadgetTemplate);
		}
		// end add gadget templates dummy for gadgets
	}

	private void initGadgetsCrudAndImportTool() {
		log.info("init GadgetTemplate_CrudAndImportTool");
		GadgetTemplate gadgetTemplate;
		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-7").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
		} else {
			gadgetTemplate = gadgetTemplateRepository.findById("MASTER-GadgetTemplate-7").get();
		}
		gadgetTemplate.setId("MASTER-GadgetTemplate-7");
		gadgetTemplate.setIdentification("gadget-crud");
		gadgetTemplate.setPublic(true);
		gadgetTemplate.setType("vueJS");
		gadgetTemplate.setHeaderlibs(
				"<script src=\"/controlpanel/static/vendor/element-ui/index.js\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
						+ "<script src=\"/controlpanel/static/vendor/element-ui/locale/en.min.js\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
						+ "\n"
						+ "<script src=\"/controlpanel/static/vendor/el-search-table-pagination/index.min.js\"></script>\n"
						+ "<script src=\"/controlpanel/static/vendor/jsoneditor/jsoneditor.js\"></script>\n" + "\n"
						+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n"
						+ "<link rel=\"stylesheet\" href=\"/controlpanel/static/vendor/element-ui/theme-chalk/index.css\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />\n"
						+ "\n" + "<script>\n" + "ELEMENT.locale(ELEMENT.lang.en)\n" + "var __env = __env || {};\n"
						+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
						+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
						+ "			\"form.entity\": \"Entidad\",\n"
						+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
						+ "			\"form.select\": \"Seleccionar\",\n"
						+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
						+ "			\"form.operator\": \"Operador\",\n"
						+ "			\"form.condition\": \"Condición\",\n"
						+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
						+ "			\"form.write.here\": \"Escriba aquí\",\n"
						+ "			\"form.select.field\": \"Seleccionar campo\",\n"
						+ "			\"form.orderby\": \"Ordenar por\",\n"
						+ "			\"form.order.type\": \"Tipo de pedido\",\n"
						+ "			\"form.where\": \"Where\",\n" + "			\"form.max.value\": \"Valor máximo\",\n"
						+ "			\"form.offset\": \"Desplazamiento\",\n"
						+ "			\"form.reset\": \"Restablecer\",\n" + "			\"form.search\": \"Buscar\",\n"
						+ "			\"form.records\": \"Registros\",\n" + "			\"form.columns\": \"Columnas\",\n"
						+ "			\"column.options\": \"Opciones\",\n"
						+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
						+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
						+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
						+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
						+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
						+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
						+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
						+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
						+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
						+ "			\"form.edit.record\": \"Editar registro\",\n"
						+ "			\"form.detail.record\": \"Registro detallado\",\n"
						+ "			\"button.cancel\": \"Cancelar\",\n" + "			\"button.delete\": \"Eliminar\",\n"
						+ "			\"button.save\": \"Guardar\",\n" + "			\"button.close\": \"Cerrar\",\n"
						+ "			\"button.new\": \"Nuevo\",\n" + "			\"button.apply\": \"Aplicar\",\n"
						+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
						+ "		    \"form.title.import\": \"Importar datos\",\n"
						+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
						+ "			\"form.download.csv\":\"Descargar CSV\",\n"
						+ "    		\"form.download.json\":\"Descargar JSON\",\n"
						+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
						+ "		    \"button.click\": \"haga click aquí\",\n"
						+ "		    \"button.click.upload\": \"para subirlo\",\n"
						+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n"
						+ "		    \"button.import\": \"Importar\",\n"
						+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
						+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
						+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
						+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
						+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
						+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
						+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
						+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
						+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
						+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
						+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
						+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
						+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
						+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
						+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
						+ "			\"button.all.records\": \"Todos los registros\",\n"
						+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
						+ "			\"error.message.download\": \"Error descargando datos\",\n"
						+ "			\"error.message.empty\": \"Error no existen registros\",\n"
						+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
						+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
						+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
						+ "			\"form.show.wizard\": \"Show search wizard\",\n"
						+ "			\"form.select\": \"Select\",\n"
						+ "			\"form.select.fields\": \"Select Fields\",\n"
						+ "			\"form.operator\": \"Operator\",\n"
						+ "			\"form.condition\": \"Condition\",\n"
						+ "			\"form.select.operator\": \"Select Operator\",\n"
						+ "			\"form.write.here\": \"Write here\",\n"
						+ "			\"form.select.field\": \"Select Field\",\n"
						+ "			\"form.orderby\": \"Order by\",\n"
						+ "			\"form.order.type\": \"Order Type\",\n" + "			\"form.where\": \"Where\",\n"
						+ "			\"form.max.value\": \"Max Value\",\n" + "			\"form.offset\": \"Offset\",\n"
						+ "			\"form.reset\": \"Reset\",\n" + "			\"form.search\": \"Search\",\n"
						+ "			\"form.records\": \"Records\",\n" + "			\"form.columns\": \"Columns\",\n"
						+ "			\"column.options\": \"Options\",\n"
						+ "			\"form.new.record.title\": \"New record\",\n"
						+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
						+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
						+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
						+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
						+ "			\"message.created.successfully\": \"Record created successfully\",\n"
						+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
						+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
						+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
						+ "			\"form.edit.record\": \"Edit record \",\n"
						+ "			\"form.detail.record\": \"Detail record \",\n"
						+ "			\"button.cancel\": \"Cancel\",\n" + "			\"button.delete\": \"Delete\",\n"
						+ "			\"button.save\": \"Save\",\n" + "			\"button.close\": \"Close\",\n"
						+ "			\"button.new\": \"New\",\n" + "			\"button.apply\": \"Apply\",\n"
						+ "		    \"form.select.entity\": \"Select Entity\",\n"
						+ "		    \"form.title.import\": \"Import records\",\n"
						+ "		    \"form.download.template\": \"Download Template\",\n"
						+ "			\"form.download.csv\":\"Download CSV\",\n"
						+ "    		\"form.download.json\":\"Download JSON\",\n"
						+ "		    \"button.drop\": \"Drop file or\",\n"
						+ "		    \"button.click\": \"click here\",\n"
						+ "		    \"button.click.upload\": \"to upload\",\n"
						+ "		    \"form.info.max\": \"Max. 2mb csv\",\n"
						+ "		    \"button.import\": \"Import\",\n"
						+ "		    \"button.showmore\": \"Show More Details\",\n"
						+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
						+ "		    \"message.success.loaded.1\": \"The\",\n"
						+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
						+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
						+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
						+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
						+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
						+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
						+ "		    \"error.message.processing\": \"Error processing data\",\n"
						+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
						+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
						+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
						+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
						+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
						+ "			\"button.all.records\": \"All the records\",\n"
						+ "			\"button.only.selection.records\": \"Only the selection\",\n"
						+ "			\"error.message.download\": \"Error downloading data\",\n"
						+ "			\"error.message.empty\": \"Error there are no records\",\n"
						+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
						+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
						+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n"
						+ "	var localLocale ='EN';\n" + "	try{\n"
						+ "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
						+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
						+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
						+ " // link messages with internacionalization json on controlpanel\n"
						+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");

		gadgetTemplate.setDescription("CRUD gadget template");
		gadgetTemplate.setTemplate("<!-- Write your HTML <div></div> and CSS <style></style> here -->\n"
				+ "<!--Focus here and F11 to full screen editor-->\n" + "<style>\n" + "    div.el-dialog__body h3 {\n"
				+ "        font-size: 14px !important;\n" + "        display: none !important;\n" + "    }\n" + "\n"
				+ "    .control-label {\n" + "        margin-top: 1px!important;\n"
				+ "        color: #505D66 !important;\n" + "        font-weight: normal !important;\n"
				+ "        width: fit-content !important;\n" + "        font-size: small !important;\n" + "    }\n"
				+ "\n" + "    .control-label .required,\n" + "    .form-group .required {\n"
				+ "        color: #A73535 !important;\n" + "        font-size: 12px !important;\n"
				+ "        padding-left: 2px !important;\n" + "    }\n" + "\n" + "    .el-select {\n"
				+ "        display: block !important;\n" + "\n" + "\n" + "    }\n" + "\n" + "    .wizard-style {\n"
				+ "        top: 28px !important;\n" + "        margin-left: 15px !important;\n" + "    }\n" + "\n"
				+ "    .reset-button {\n" + "        color: #A7AEB2!important;\n"
				+ "        background: #ffffff!important;\n" + "        border: none!important;\n"
				+ "        text-align: center!important;\n" + "        margin-top: 21px!important;\n" + "    }\n" + "\n"
				+ "    .search-button {\n" + "        margin-left: 10px!important;\n"
				+ "        background: #1168A6!important;\n" + "        border-radius: 2px!important;\n"
				+ "        text-align: center!important;\n" + "        margin-top: 21px!important;\n"
				+ "        color: #ffffff!important;\n" + "    }\n" + "\n" + "    .el-cancel-button {\n"
				+ "        color: #1168A6!important;\n" + "        border: none!important;\n"
				+ "        text-align: center!important;\n" + "        float:right;\n" + "    }\n" + "\n"
				+ "    .el-apply-button {\n" + "        margin-left: 10px!important;\n"
				+ "        margin-right: 20px!important;\n" + "        background: #1168A6!important;\n"
				+ "        border-radius: 2px!important;\n" + "        text-align: center!important;\n"
				+ "        color: #ffffff!important;\n" + "        float:right;\n" + "    }\n" + "\n"
				+ "    .el-input__inner {\n" + "        background: #F7F8F8 !important;\n" + "    }\n" + "\n"
				+ "    .button-plus {\n" + "        background: #1168A6!important;\n"
				+ "        border-radius: 2px!important;\n" + "        height: 32px!important;\n"
				+ "        width: 32px!important;\n" + "        margin-top: 28px!important;\n"
				+ "        padding-left: 9px!important;\n" + "    }\n" + "\n" + "    .button-plus:hover {\n"
				+ "        background: #1168A6!important;\n" + "        border-radius: 2px!important;\n"
				+ "        height: 32px!important;\n" + "        width: 32px!important;\n"
				+ "        margin-top: 28px!important;\n" + "        padding-left: 9px!important;\n" + "    }\n" + "\n"
				+ "    .button-plus-create {\n" + "        background: #1168A6!important;\n"
				+ "        border-radius: 2px!important;\n" + "        height: 32px!important;\n"
				+ "        width: 32px!important;\n" + "        margin-top: 24px!important;\n"
				+ "        padding-left: 9px!important;\n" + "    }\n" + "\n" + "    .button-plus-create:hover {\n"
				+ "        background: #1168A6!important;\n" + "        border-radius: 2px!important;\n"
				+ "        height: 32px!important;\n" + "        width: 32px;\n"
				+ "        margin-top: 24px!important;\n" + "        padding-left: 9px!important;\n" + "    }\n" + "\n"
				+ "    .records-title {\n" + "        margin-top: 30px !important;\n"
				+ "        font-size: 17px !important;\n" + "        line-height: 24px !important;\n"
				+ "        color: #051724 !important;\n" + "    }\n" + "\n" + "    .el-dialog-title {\n"
				+ "        font-size: 17px !important;\n" + "        line-height: 24px !important;\n"
				+ "        color: #051724 !important;\n" + "    }\n" + "    .el-dialog__header {\n"
				+ "        padding: 37px 20px 10px !important;\n" + "    }\n" + "    .search-menu-title {\n"
				+ "        margin-top: 26px !important;\n" + "        margin-left: 5px !important;\n" + "\n" + "    }\n"
				+ "\n" + "    .search-menu-title-magnifying-glass {\n" + "        margin-top: 26px!important;\n"
				+ "        margin-left: 10px!important;\n" + "        margin-bottom: -4px!important;\n" + "    }\n"
				+ "\n" + "    .el-row-modal-grey {\n" + "        margin-bottom: -30px!important;\n"
				+ "        margin-left: -20!important;\n" + "        margin-right: -20!important;\n"
				+ "        padding-bottom: 24px!important;\n" + "        margin-top: 24px!important;\n" + "    }\n"
				+ "\n" + "    .el-table .cell {\n" + "        font-size: 12px !important;\n" + "    }\n" + "\n"
				+ "    .el-table .el-table__cell {\n" + "        padding: 5px 0 !important;\n" + "    }\n" + "\n"
				+ "    .trash-icon-red {\n"
				+ "        filter: invert(23%) sepia(100%) saturate(3793%) hue-rotate(352deg) brightness(91%) contrast(65%);\n"
				+ "    }\n" + "\n" + "    .edit-icon-blue {\n"
				+ "        filter: invert(31%) sepia(41%) saturate(2221%) hue-rotate(180deg) brightness(92%) contrast(90%);\n"
				+ "    }\n" + "    .download-icons-grey {\n"
				+ "          filter: invert(0%) sepia(0%) saturate(0%) hue-rotate(162deg) brightness(93%) contrast(88%);\n"
				+ "    }\n" + "    .el-form-item__content {\n" + "        display: none;\n" + "    }\n" + "\n"
				+ "    .el-table th.el-table__cell {\n" + "        background-color: #f9f9f9 !important;\n"
				+ "        color: #505D66 !important;\n" + "    }\n" + "\n" + "\n" + "    ::-webkit-scrollbar {\n"
				+ "        right: 2px;\n" + "        width: 7px;\n" + "        height: 7px;\n" + "    }\n" + "\n"
				+ "    ::-webkit-scrollbar-thumb {\n" + "        background: #959595ad;\n"
				+ "        border-radius: 10px;\n" + "    }\n" + "    ::-webkit-scrollbar-track {\n"
				+ "        box-shadow: inset 0 0 5px transparent;\n" + "        border-radius: 10px;\n" + "    }\n"
				+ "\n" + "    .row {\n" + "        display: -ms-flexbox !important;\n"
				+ "        display: flex !important;\n" + "        -ms-flex-wrap: wrap !important;\n"
				+ "        flex-wrap: wrap !important;\n" + "        width:100% !important;\n"
				+ "        margin-right: -15px !important;\n" + "        margin-left: -15px !important;\n" + "    }\n"
				+ "    .col-md-12 {\n" + "        -ms-flex: 0 0 100% !important;\n"
				+ "        flex: 0 0 100% !important;\n" + "        max-width: 100% !important;\n" + "    }\n"
				+ "    .col, .col-1, .col-10, .col-11, .col-12, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-auto, .col-lg, .col-lg-1, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-auto, .col-md, .col-md-1, .col-md-10, .col-md-11, .col-md-12, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-auto, .col-sm, .col-sm-1, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-auto, .col-xl, .col-xl-1, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-auto {\n"
				+ "        position: relative !important;\n" + "        width: 100% !important;\n"
				+ "        padding-right: 15px !important;\n" + "        padding-left: 15px !important;\n" + "    }\n"
				+ "    label {\n" + "        display: inline-block !important;\n"
				+ "        margin-bottom: 0.5rem !important;\n" + "    }\n" + "    .form-group {\n"
				+ "        margin-bottom: 1rem !important;\n" + "    }\n" + "\n" + "    .form-control {\n"
				+ "        display: block !important;\n" + "        width: 100%!important;\n"
				+ "        height: calc(1.5em + 0.75rem + 2px)!important;\n"
				+ "        padding: 0.375rem 0.75rem!important;\n" + "        font-size: 1rem!important;\n"
				+ "        font-weight: 400!important;\n" + "        line-height: 1.5!important;\n"
				+ "        color: #495057!important;\n" + "        background-color: #fff!important;\n"
				+ "        background-clip: padding-box!important;\n" + "        border: 1px solid #ced4da!important;\n"
				+ "        border-radius: 0.25rem!important;\n"
				+ "        transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out!important;\n"
				+ "    }\n" + "    button, input, optgroup, select, textarea {\n" + "        margin: 0 !important;\n"
				+ "        font-family: inherit !important;\n" + "        font-size: inherit !important;\n"
				+ "        line-height: inherit !important;\n" + "    }\n" + "    .form-control:focus {\n"
				+ "        color: #495057 !important;\n" + "        background-color: #fff !important;\n"
				+ "        border-color: #80bdff !important;\n" + "        outline: 0 !important;\n"
				+ "        box-shadow: 0 0 0 0.2rem rgb(0 123 255 / 25%) !important;\n" + "    }\n" + "\n" + "\n"
				+ "</style>\n" + "<div class=\"appgadget\">\n" + "    <!-- entity selector -->\n"
				+ "    <el-row style=\"box-shadow: 0px 1px 0px #D7DADC;padding-top:24px;padding-bottom:24px\">\n"
				+ "        <el-col :span=\"8\">\n"
				+ "            <label class=\"control-label\">{{ $t(\"form.entity\") }}<span class=\"required\" aria-required=\"true\">\n"
				+ "                    *</span></label></br>\n"
				+ "            <el-select :disabled=\"showSelectOntology\" size=\"small\"  v-model=\"selectedOntology\"\n"
				+ "                @change=\"onChangeEntity($event)\" filterable :placeholder=\"$t('form.select')\">\n"
				+ "                <el-option v-for=\"onto in ontologies\" :key=\"onto.identification\" :label=\"onto.identification\"\n"
				+ "                    :value=\"onto.identification\">\n" + "                </el-option>\n"
				+ "            </el-select>\n" + "        </el-col>\n" + "        <!-- wizard switch -->\n"
				+ "        <el-col v-if=\"typeGadget=='withWizard'||typeGadget=='searchOnly'\" :span=\"8\">\n"
				+ "            <el-switch class=\"wizard-style\" v-model=\"showWizard\" @change=\"calculeTableheight\" :disabled=\"disabledWizard\"\n"
				+ "                :active-text=\"$t('form.show.wizard')\"></el-switch>\n" + "        </el-col>\n"
				+ "    </el-row>\n" + "    <!-- wizard  -->\n" + "    <div class=\"crudWizard\" v-if=\"showWizard\">\n"
				+ "        <el-row justify=\"center\" type=\"flex\"\n"
				+ "            style=\"box-shadow: 0px 1px 0px #D7DADC;padding-top:24px;padding-bottom:24px\" :gutter=\"10\">\n"
				+ "\n" + "            <el-col :xs=\"7\" :sm=\"7\" :md=\"7\" :lg=\"7\" :xl=\"7\">\n"
				+ "                <label class=\"control-label\">{{ $t(\"form.where\") }}</label></br>\n"
				+ "                <el-select size=\"small\" v-model=\"selectWizard\" multiple collapse-tags :placeholder=\"$t('form.select')\">\n"
				+ "                    <el-option v-for=\"item in selectWizardOptions\" :key=\"item.value\" :label=\"item.label\"\n"
				+ "                        :value=\"item.value\">\n" + "                    </el-option>\n"
				+ "                </el-select>\n" + "            </el-col>\n"
				+ "            <el-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\">\n"
				+ "                <el-button size=\"small\" class=\"button-plus\" @click=\"dialogAddSelectVisibleFunction\"><img\n"
				+ "                        v-bind:src=\"platformhost + '/static/images/dashboards/icon_button_plus.svg'\"></el-button>\n"
				+ "            </el-col>\n" + "            <el-col :xs=\"7\" :sm=\"7\" :md=\"7\" :lg=\"7\" :xl=\"7\">\n"
				+ "                <label v-if=\"typeGadget!='searchOnly'\" class=\"control-label\">{{ $t(\"form.orderby\") }}</label></br>\n"
				+ "                <el-select v-if=\"typeGadget!='searchOnly'\" size=\"small\" v-model=\"orderByWizard\" multiple collapse-tags\n"
				+ "                    :placeholder=\"$t('form.select')\">\n"
				+ "                    <el-option v-for=\"itemo in orderByWizardOptions\" :key=\"itemo.value\" :label=\"itemo.label\"\n"
				+ "                        :value=\"itemo.value\">\n" + "                    </el-option>\n"
				+ "                </el-select>\n" + "            </el-col>\n"
				+ "            <el-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\">\n"
				+ "                <el-button v-if=\"typeGadget!='searchOnly'\" size=\"small\" class=\"button-plus\"\n"
				+ "                    @click=\"dialogAddOrderByVisibleFunction\"><img\n"
				+ "                        v-bind:src=\"platformhost + '/static/images/dashboards/icon_button_plus.svg'\"></el-button>\n"
				+ "            </el-col>\n"
				+ "            <el-col :xs=\"3\" :sm=\"3\" :md=\"3\" :lg=\"3\" :xl=\"3\" style=\"min-width:100px\">\n"
				+ "                <label v-if=\"typeGadget!='searchOnly'\" class=\"control-label\">{{ $t(\"form.max.value\") }}</label> </br>\n"
				+ "                <el-input v-if=\"typeGadget!='searchOnly'\" type=\"number\" size=\"small\" v-model=\"limitWizard\"\n"
				+ "                    controls-position=\"right\" :min=\"0\">\n" + "                </el-input>\n"
				+ "            </el-col>\n"
				+ "            <el-col :xs=\"3\" :sm=\"3\" :md=\"3\" :lg=\"3\" :xl=\"3\" style=\"min-width:100px\">\n"
				+ "                <label v-if=\"typeGadget!='searchOnly'\" class=\"control-label\"> {{ $t(\"form.offset\") }} </label></br>\n"
				+ "                <el-input v-if=\"typeGadget!='searchOnly'\" type=\"number\" size=\"small\" v-model=\"offsetWizard\"\n"
				+ "                    controls-position=\"right\" :min=\"0\">\n" + "                </el-input>\n"
				+ "            </el-col>\n" + "\n"
				+ "            <el-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\" style=\"min-width:100px\">\n"
				+ "\n"
				+ "                <el-button size=\"small\" class=\"reset-button float-right\" @click=\"resetWizard()\">{{ $t(\"form.reset\") }}\n"
				+ "                </el-button>\n" + "            </el-col>\n"
				+ "            <el-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\" style=\"min-width:100px\">\n"
				+ "                <el-button size=\"small\" class=\"search-button float-right\" @click=\"searchWizard()\">\n"
				+ "                    {{ $t(\"form.search\") }}</el-button>\n" + "\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </div>\n" + "    <!-- div table -->\n" + "    <div v-if=\"showTable\">\n"
				+ "        <el-row justify=\"center\" type=\"flex\" :gutter=\"10\">\n"
				+ "            <el-col :xs=\"3\" :sm=\"3\" :md=\"3\" :lg=\"2\" :xl=\"2\" >\n"
				+ "                <label class=\"control-label records-title\">{{ $t(\"form.records\") }}</label>\n"
				+ "            </el-col>\n" + "            <el-col :xs=\"5\" :sm=\"5\" :md=\"5\" :lg=\"6\" :xl=\"6\">\n"
				+ "                <el-button size=\"small\" v-if=\"showMagnifyingGlass\" type=\"text\"\n"
				+ "                    class=\"button-options-columns search-menu-title-magnifying-glass\"\n"
				+ "                    @click=\"showMagnifyingGlass = false\"><img\n"
				+ "                        v-bind:src=\"platformhost + '/static/images/dashboards/icon_magnifying_glass.svg'\"></el-button>\n"
				+ "                <el-input type=\"string\" v-if=\"!showMagnifyingGlass\" class=\"search-menu-title\" size=\"small\"\n"
				+ "                    v-model=\"searchString\">\n"
				+ "                    <el-button size=\"small\" slot=\"prefix\" type=\"text\" class=\"button-options-columns\"\n"
				+ "                        @click=\"showMagnifyingGlass = true\"><img\n"
				+ "                            v-bind:src=\"platformhost + '/static/images/dashboards/icon_magnifying_glass.svg'\"></el-button>\n"
				+ "                </el-input>\n" + "            </el-col>\n"
				+ "            <el-col :offset=\"4\" :xs=\"12\" :sm=\"12\" :md=\"12\" :lg=\"12\" :xl=\"12\" style=\"text-align: right;\">\n"
				+ "                <el-dropdown style=\"margin-right: 10px;\" @command=\"downloadData\">\n"
				+ "                <el-button size=\"small\" type=\"text\" class=\"button-options-columns\" >\n"
				+ "                    <img v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\"></el-button>\n"
				+ "\n" + "                <el-dropdown-menu slot=\"dropdown\">\n"
				+ "                    <el-dropdown-item v-if=\"executeSearch\" command=\"csv\" ><img style=\"margin-top: 8px;\" class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.csv\") }}</el-dropdown-item>\n"
				+ "                    <el-dropdown-item v-if=\"executeSearch\" command=\"json\"  ><img style=\"margin-top: 8px;\" class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.json\") }}</el-dropdown-item>\n"
				+ "                    <el-dropdown-item v-if=\"!executeSearch\" command=\"allcsv\" ><img style=\"margin-top: 8px;\" class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.csv\") }}</el-dropdown-item>\n"
				+ "                    <el-dropdown-item v-if=\"!executeSearch\" command=\"alljson\"  ><img style=\"margin-top: 8px;\" class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.json\") }}</el-dropdown-item>\n"
				+ "                </el-dropdown-menu>\n" + "                </el-dropdown>\n" + "\n" + "\n" + "\n"
				+ "\n" + "\n"
				+ "                <el-button size=\"small\" type=\"text\" class=\"button-options-columns\"\n"
				+ "                    @click=\"dialogOptionsColumnsVisible = true\"><img\n"
				+ "                        v-bind:src=\"platformhost + '/static/images/dashboards/icon_options_dots_bars.svg'\"></el-button>\n"
				+ "                <el-button size=\"small\" class=\"button-plus-create\" @click=\"dialogCreateVisible= true\"><img\n"
				+ "                        v-bind:src=\"platformhost + '/static/images/dashboards/icon_button_plus.svg'\"></el-button>\n"
				+ "            </el-col>\n" + "        </el-row>\n" + "\n"
				+ "        <!--el-search-table-pagination  -->\n"
				+ "        <el-search-table-pagination type=\"local\" :height=\"tableHeight\" @sort-change=\"sortChange\"\n"
				+ "            :data=\"tableData.filter(tableDatafilter)\" :page-sizes=\"[10, 25, 50]\" :columns=\"columns\"\n"
				+ "            :form-options=\"formOptions\">\n"
				+ "            <el-table-column :label=\"$t('column.options')\" slot=\"append\" width=\"120px\">\n"
				+ "                <template slot-scope=\"scope\">\n"
				+ "                    <el-button size=\"mini\" type=\"text\" @click=\"handleShow(scope.$index, scope.row)\"><img\n"
				+ "                            v-bind:src=\"platformhost + '/static/images/dashboards/icon_eye.svg'\">\n"
				+ "                    </el-button>\n"
				+ "                    <el-button size=\"mini\" type=\"text\" @click=\"handleEdit(scope.$index, scope.row)\"><img\n"
				+ "                            v-bind:src=\"platformhost + '/static/images/dashboards/edit.svg'\" class=\"edit-icon-blue\">\n"
				+ "                    </el-button>\n"
				+ "                    <el-button size=\"mini\" type=\"text\" @click=\"handleDelete(scope.$index, scope.row)\"><img\n"
				+ "                            v-bind:src=\"platformhost + '/static/images/dashboards/delete.svg'\" class=\"trash-icon-red\">\n"
				+ "                    </el-button>\n" + "                </template>\n"
				+ "            </el-table-column>\n" + "        </el-search-table-pagination>\n" + "    </div>\n"
				+ "    <!-- DELETE dialog -->\n"
				+ "    <el-dialog modal=\"false\" append-to-body=\"true\" :visible.sync=\"dialogDeleteVisible\" width=\"25%\">\n"
				+ "        <label class=\"el-dialog-title\">{{ $t(\"message.modal.delete.title\") }}</label></br>\n"
				+ "        <label\n"
				+ "            style=\"font-size: 12px;line-height: 16px; color: #505D66;\">{{ $t(\"message.modal.delete.subtitle\") }}</label>\n"
				+ "        <el-row class=\"el-row-modal-grey\">\n" + "            <el-col>\n"
				+ "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedDelete\">\n"
				+ "                    {{ $t(\"button.delete\") }}</el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\" @click=\"dialogDeleteVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</el-button>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </el-dialog>\n" + "    <!-- EDIT dialog -->\n"
				+ "    <el-dialog modal=\"true\" append-to-body=\"true\" :title=\"editTitle\" :visible.sync=\"dialogEditVisible\"\n"
				+ "        @opened=\"openEdit\" width=\"25%\">\n"
				+ "        <div :class=\"[idelem, 'editor_edit_holder']\" ></div>\n"
				+ "        <el-row class=\"el-row-modal-grey\">\n" + "            <el-col>\n"
				+ "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedEdit\">{{ $t(\"button.save\") }}\n"
				+ "                </el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\" @click=\"dialogEditVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</el-button>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </el-dialog>\n" + "    <!-- SHOW/HIDE COLUMNS dialog -->\n"
				+ "    <el-dialog modal=\"false\" append-to-body=\"false\" :title=\"$t('form.columns')\"\n"
				+ "        :visible.sync=\"dialogOptionsColumnsVisible\" width=\"25%\">\n" + "\n"
				+ "        <el-row v-for=\"visibleColumn in visibleColumns\" :key=\"visibleColumn.prop\" v-if=\"visibleColumn.label!='id'\">\n"
				+ "            <el-col :span=\"24\">\n" + "                </br>\n"
				+ "                <el-switch v-model=\"visibleColumn.visible\" :active-text=\"visibleColumn.prop\"\n"
				+ "                    @change=\"dialogOptionsColumnsVisible = false;dialogOptionsColumnsVisible = true;\"></el-switch>\n"
				+ "            </el-col>\n" + "        </el-row>\n" + "        <el-row class=\"el-row-modal-grey\">\n"
				+ "            <el-col>\n" + "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedChangeColumns\">\n"
				+ "                    {{ $t(\"button.apply\") }}</el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\"\n"
				+ "                    @click=\"dialogOptionsColumnsVisible = false\">{{ $t(\"button.cancel\") }}</el-button>\n"
				+ "            </el-col>\n" + "        </el-row>\n" + "    </el-dialog>\n"
				+ "     <!-- DOWNLOAD dialog -->\n"
				+ "    <el-dialog modal=\"false\" append-to-body=\"false\" :title=\"$t('message.download.all')\"\n"
				+ "        :visible.sync=\"dialogDownloadVisible\" width=\"25%\">\n" + "\n" + "\n"
				+ "        <el-row class=\"el-row-modal-grey\">\n" + "            <el-col>\n"
				+ "                </br>\n"
				+ "                  <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedDownloadOnlySelec\">\n"
				+ "                    {{ $t(\"button.only.selection.records\") }}</el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedDownloadAll\">\n"
				+ "                    {{ $t(\"button.all.records\") }}</el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\"\n"
				+ "                    @click=\"dialogDownloadVisible = false\">{{ $t(\"button.cancel\") }}</el-button>\n"
				+ "            </el-col>\n" + "        </el-row>\n" + "    </el-dialog>\n"
				+ "    <!-- DETAIL dialog -->\n"
				+ "    <el-dialog modal=\"true\" append-to-body=\"true\" :title=\"showTitle\" :visible.sync=\"dialogShowVisible\"\n"
				+ "        @opened=\"openShow\" width=\"25%\">\n"
				+ "        <div :class=\"[idelem, 'editor_show_holder']\" ></div>\n"
				+ "        <el-row class=\"el-row-modal-grey\">\n" + "            <el-col>\n"
				+ "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"dialogShowVisible = false\">\n"
				+ "                    {{ $t(\"button.close\") }}</el-button>\n" + "\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </el-dialog>\n" + "    <!-- CREATE dialog -->\n"
				+ "    <el-dialog modal=\"true\" append-to-body=\"true\" :title=\"$t('form.new.record.title')\"\n"
				+ "        :visible.sync=\"dialogCreateVisible\" @opened=\"openCreate\" width=\"25%\">\n"
				+ "        <div :class=\"[idelem, 'editor_new_holder']\" ></div>\n"
				+ "        <el-row class=\"el-row-modal-grey\">\n" + "            <el-col>\n"
				+ "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedCreate\">\n"
				+ "                    {{ $t(\"button.new\") }} <img style=\"margin-top: 6px;\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_button_plus.svg'\">\n"
				+ "                </el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\" @click=\"dialogCreateVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</el-button>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </el-dialog>\n" + "\n" + "    <!-- WHERE dialog -->\n"
				+ "    <el-dialog modal=\"true\" append-to-body=\"true\" :title=\"$t('form.where')\" :visible.sync=\"dialogAddSelectVisible\"\n"
				+ "        @opened=\"opendialogAddSelect\" width=\"25%\">\n" + "        <el-row type=\"flex\">\n"
				+ "            <el-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.select.fields\")}} <span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <el-select size=\"small\" v-model=\"selectedParametereWhere\" :placeholder=\"$t('form.select.field')\">\n"
				+ "                    <el-option v-for=\"col in columnsParams\" :key=\"col.prop\" :label=\"col.label\" :value=\"col.prop\">\n"
				+ "                    </el-option>\n" + "                </el-select>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "        <el-row type=\"flex\">\n" + "            <el-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.operator\")}}<span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <el-select size=\"small\" v-model=\"selectedOperatorWhere\" :placeholder=\"$t('form.select.operator')\">\n"
				+ "                    <el-option v-for=\"ope in operators\" :key=\"ope\" :label=\"ope\" :value=\"ope\">\n"
				+ "                    </el-option>\n" + "                </el-select>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "        <el-row type=\"flex\">\n" + "            <el-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.condition\")}} <span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <el-input size=\"small\" :placeholder=\"$t('form.write.here')\" v-model=\"inputValueWhere\"></el-input>\n"
				+ "            </el-col>\n" + "        </el-row>\n" + "        <el-row class=\"el-row-modal-grey\">\n"
				+ "            <el-col>\n" + "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedAddWhereParameter\">\n"
				+ "                    {{ $t(\"button.apply\") }}</el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\" @click=\"dialogAddSelectVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</el-button>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </el-dialog>\n" + "\n" + "\n" + "    <!-- ORDER BY dialog -->\n"
				+ "    <el-dialog modal=\"true\" append-to-body=\"true\" title=\"Order by\" :visible.sync=\"dialogAddOrderByVisible\"\n"
				+ "        @opened=\"opendialogAddOrderBy\" width=\"25%\">\n" + "        <el-row type=\"flex\">\n"
				+ "            <el-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.select.fields\")}}<span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <el-select size=\"small\" v-model=\"selectedParametereOrderBy\" :placeholder=\"$t('form.select.field')\">\n"
				+ "                    <el-option v-for=\"col in columnsParams\" :key=\"col.prop\" :label=\"col.label\" :value=\"col.prop\">\n"
				+ "                    </el-option>\n" + "                </el-select>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "        <el-row type=\"flex\">\n" + "            <el-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.order.type\")}}<span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <el-select size=\"small\" v-model=\"selectedOperatorOrderBy\" :placeholder=\"$t('form.select.operator')\">\n"
				+ "                    <el-option v-for=\"ope in orders\" :key=\"ope\" :label=\"ope\" :value=\"ope\">\n"
				+ "                    </el-option>\n" + "                </el-select>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "        <el-row class=\"el-row-modal-grey\">\n" + "            <el-col>\n"
				+ "                </br>\n"
				+ "                <el-button size=\"small\" class=\"el-apply-button float-right\" @click=\"aceptedAddOrderByParameter\">\n"
				+ "                    {{ $t(\"button.apply\") }}</el-button>\n"
				+ "                <el-button size=\"small\" class=\"el-cancel-button float-right\" @click=\"dialogAddOrderByVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</el-button>\n" + "            </el-col>\n"
				+ "        </el-row>\n" + "    </el-dialog>\n" + "\n" + "\n" + "</div>\n" + "");
		gadgetTemplate.setTemplateJS("vm.vueconfig = {\n"
				+ "   el: document.getElementById(vm.id).querySelector('vuetemplate .appgadget'),\n" + "   data: {\n"
				+ "      typeGadget: 'withWizard', //['withWizard','noWizard','searchOnly']\n"
				+ "      hideIdColumn: false, // show or hide id column\n"
				+ "      initialEntity: \"\" , //variable that initializes the entity with the value assigned to it\n"
				+ "\n" + "      showTable: false,\n" + "      showSelectOntology: true,\n"
				+ "      showWizard: false,\n" + "      disabledWizard: true,\n" + "      idPath: \"\",\n"
				+ "      ontologies: [],\n" + "      ontologyFieldsAndDesc: {},\n" + "      recordSelected: \"\",\n"
				+ "      selectedOntology: \"\",\n" + "      selectedOntologySchema: {},\n" + "\n"
				+ "      dialogDeleteVisible: false, //hide show dialogs\n" + "      dialogEditVisible: false,\n"
				+ "      dialogCreateVisible: false,\n" + "      dialogShowVisible: false,\n"
				+ "      dialogOptionsColumnsVisible: false,\n" + "      dialogAddSelectVisible: false,\n"
				+ "      dialogDownloadVisible:false,\n" + "      idelem:vm.id,\n" + "      executeSearch:false,\n"
				+ "      showMagnifyingGlass: true,\n" + "      jEditor: {},\n" + "      jShowEditor: {},\n"
				+ "      tableHeight: 100,\n" + "      resizeObserver: {},\n" + "      selectWizard: [],\n"
				+ "      selectWizardOptions: [],\n" + "      orderByWizard: [],\n"
				+ "      orderByWizardOptions: [],\n" + "      dialogAddOrderByVisible: false,\n"
				+ "      limitWizard: 100, // limit of records in the search for initialize at another value change on resetwizard too\n"
				+ "      offsetWizard: 0, //offset records in the search\n" + "      whereCondition: '',\n"
				+ "      uniqueID: '', // save path of id\n" + "      selectedParametereWhere: '',\n"
				+ "      selectedOperatorWhere: '',\n" + "      selectedParametereOrderBy: '',\n"
				+ "      selectedOperatorOrderBy: '',\n" + "      inputValueWhere: '',\n" + "      editTitle: '',\n"
				+ "      showTitle: '',\n" + "      downloadType:'',\n"
				+ "      visibleColumns: [], // list of visible columns\n" + "      columnsParams: [],\n"
				+ "      searchString: '', // text for local search\n" + "      formOptions: {\n"
				+ "         forms: []\n" + "      },\n" + "      orders: ['ASC', 'DESC'],\n"
				+ "      operators: ['=', '>', '<', '>=', '<=', '!='],\n" + "      ds: [],\n" + "      columns: [],\n"
				+ "      tableData: [],\n" + "      platformhost: __env.endpointControlPanel\n" + "   },\n"
				+ "   methods: {\n" + "      drawVueComponent: function (newData, oldData) {\n"
				+ "         //This will be call on new data\n" + "      },\n" + "      resizeEvent: function () {\n"
				+ "         //Resize event\n" + "\n" + "      },\n" + "      destroyVueComponent: function () {\n"
				+ "         vm.vueapp.$destroy();\n" + "      },\n" + "      receiveValue: function (data) {\n"
				+ "         //data received from datalink\n" + "      },\n"
				+ "      //function that initially reads the entities\n"
				+ "      loadEntities: function (search, loading) {\n" + "         var that = this;\n"
				+ "         vm.getEntities().then(function (data) {\n"
				+ "            that.ontologies = data.data.map(function (obj) {\n" + "               return {\n"
				+ "                  id: obj.id,\n" + "                  identification: obj.identification\n"
				+ "               }\n" + "            });\n" + "\n"
				+ "            if(that.initialEntity!=null && that.initialEntity!==\"\"){\n"
				+ "               if (that.ontologies.some(e => e.identification === that.initialEntity)) {\n"
				+ "                     that.selectedOntology = that.initialEntity;\n"
				+ "                     that.onChangeEntity(that.selectedOntology);\n"
				+ "                     that.showSelectOntology = true;\n" + "                     return;\n"
				+ "                  } else {\n" + "                     that.$notify({\n"
				+ "                        message: that.$t('error.message.ontology'),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "                  }\n"
				+ "            }\n" + "            var urlparam = urlParamService.generateFiltersForGadgetId(vm.id);\n"
				+ "            if (typeof urlparam !== 'undefined' && urlparam !== null && urlparam.length > 0) {\n"
				+ "               if (urlparam[0].exp != null) {\n"
				+ "                  var urlontology = urlparam[0].exp.replace(/\"/g, '');\n"
				+ "                  if (that.ontologies.some(e => e.identification === urlontology)) {\n"
				+ "                     that.selectedOntology = urlontology;\n"
				+ "                     that.onChangeEntity(that.selectedOntology);\n"
				+ "                     that.showSelectOntology = true;\n" + "                     return;\n"
				+ "                  } else {\n" + "                     that.$notify({\n"
				+ "                        message: that.$t('error.message.ontology'),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "                  }\n"
				+ "               }\n" + "            } else {\n" + "               that.showSelectOntology = false;\n"
				+ "            }\n" + "\n" + "         })\n" + "      },\n"
				+ "      //function that obtains the information of the selected ontology\n"
				+ "      loadHeadTable: function () {\n" + "         var that = this;\n"
				+ "         if (this.ontologies != null && this.ontologies.length > 0) {\n"
				+ "            for (var i = 0; i < this.ontologies.length; i++) {\n"
				+ "               if (this.ontologies[i].identification === this.selectedOntology) {\n"
				+ "                  vm.crudGetEntityInfo(this.ontologies[i].id).then(function (data) {\n"
				+ "                     that.uniqueID = data.data.uniqueId;\n"
				+ "                     that.selectedOntologySchema = that.changeDescriptionForTitle(data.data.jsonSchema);\n"
				+ "                  });\n" + "                  break;\n" + "               }\n" + "            }\n"
				+ "         }\n"
				+ "         vm.getOntologyFieldsAndDesc(this.selectedOntology).then(function (data) {\n"
				+ "            that.ontologyFieldsAndDesc = data.data;\n" + "            that.loadData();\n"
				+ "         })\n" + "      },\n"
				+ "      //function that gets the data and loads it to be displayed in the table\n"
				+ "      //difference if it is an initial query or the search button is pressed\n"
				+ "      loadData: function (fromSearch) {\n" + "         var that = this;\n"
				+ "         that.showTable = false;\n" + "         var selectStatement = {};\n"
				+ "         if (typeof fromSearch == 'undefined' || fromSearch == null || fromSearch == false) {\n"
				+ "            selectStatement = {\n" + "               ontology: this.selectedOntology,\n"
				+ "               columns: [],\n" + "               where: [],\n" + "               orderBy: [],\n"
				+ "               limit: this.limitWizard,\n" + "               offset: this.offsetWizard\n"
				+ "            };\n" + "         } else {\n" + "            selectStatement = {\n"
				+ "               ontology: this.selectedOntology,\n" + "               columns: [],\n"
				+ "               where: this.mapArrayToObjects(this.selectWizard),\n"
				+ "               orderBy: this.mapArrayToObjects(this.orderByWizard),\n"
				+ "               limit: this.limitWizard,\n" + "               offset: this.offsetWizard\n"
				+ "            };\n" + "         }\n"
				+ "         vm.crudQueryParams(selectStatement).then(function (data) {\n"
				+ "            that.showTable = true;\n" + "            that.disabledWizard = false;\n"
				+ "            //create columns from that.ontologyFieldsAndDesc\n"
				+ "            var keys = Object.keys(that.ontologyFieldsAndDesc);\n" + "\n"
				+ "            //validate error from data\n"
				+ "            if (typeof data.data.error !== 'undefined') {\n" + "               that.$notify({\n"
				+ "                  message: that.$t('error.message.querying.the.data'),\n"
				+ "                  type: 'error'\n" + "               });\n" + "               return {};\n"
				+ "            }\n" + "            if (keys != null && keys.length > 0) {\n"
				+ "               if (typeof fromSearch != 'undefined' && fromSearch != null && fromSearch) {\n"
				+ "                  var index = that.columns.findIndex(function (elem) {\n"
				+ "                     return elem.prop === that.uniqueID\n" + "                  });\n"
				+ "                  if (index < 0) {\n" + "                     that.idPath = that.uniqueID;\n"
				+ "                     that.columns.push({\n" + "                        prop: that.uniqueID,\n"
				+ "                        label: \"id\"\n" + "                     });\n" + "\n"
				+ "                  }\n" + "                  that.tableData = data.data.map(function (dat) {\n"
				+ "                     var refinedData = {};\n"
				+ "                     for (var i = 0; i < that.columns.length; i++) {\n"
				+ "                        let path = that.columns[i].prop.split('.');\n"
				+ "                           try {\n"
				+ "                                  refinedData[that.columns[i].prop] = path.reduce((a, v) => a[v], dat);              \n"
				+ "                              } catch (error) {\n"
				+ "                                   refinedData[that.columns[i].prop] = null; \n"
				+ "                              }" + "                     }\n" + "\n"
				+ "                     return refinedData;\n" + "                  })\n" + "               } else {\n"
				+ "                  that.columns = [];\n" + "\n"
				+ "                  var index = keys.findIndex(function (elem) {\n"
				+ "                     return elem === that.uniqueID\n" + "                  });\n"
				+ "                  if (index > -1) {\n" + "                     keys.splice(index, 1);\n"
				+ "                  }\n" + "                  that.idPath = that.uniqueID;\n"
				+ "                  that.columns.push({\n" + "                     prop: that.uniqueID,\n"
				+ "                     label: \"id\"\n" + "                  });\n"
				+ "                  keys = keys.sort(that.orderKeys);\n"
				+ "                  //initial construction of table columns\n"
				+ "                  for (var i = 0; i < keys.length; i++) {\n"
				+ "                     var description = that.ontologyFieldsAndDesc[keys[i]].description;\n"
				+ "                     if (description == null || typeof description == undefined || description.length == 0) {\n"
				+ "                        description = that.ontologyFieldsAndDesc[keys[i]].path;\n"
				+ "                     }\n"
				+ "                     description = that.$t(description) || description;\n"
				+ "                     that.columns.push({\n"
				+ "                        prop: that.ontologyFieldsAndDesc[keys[i]].path,\n"
				+ "                        thetype: that.ontologyFieldsAndDesc[keys[i]].type,\n"
				+ "                        label: description,\n" + "                        minWidth: 100,\n"
				+ "                        sortable: 'custom'\n" + "                     });\n" + "\n"
				+ "                  }\n" + "\n" + "                  //mapping the data to display\n"
				+ "                  that.tableData = data.data.map(function (dat) {\n"
				+ "                     var refinedData = {};\n"
				+ "                     for (var i = 0; i < that.columns.length; i++) {\n"
				+ "                        if(typeof that.columns[i].prop!='undefined' && that.columns[i].prop!=null){\n"
				+ "                           let path = that.columns[i].prop.split('.');\n"
				+ "                             try {\n"
				+ "                                  refinedData[that.columns[i].prop] = path.reduce((a, v) => a[v], dat);              \n"
				+ "                              } catch (error) {\n"
				+ "                                   refinedData[that.columns[i].prop] = null; \n"
				+ "                              }" + "                        }\n" + "                     }\n" + "\n"
				+ "                     return refinedData;\n" + "                  })\n" + "               }\n"
				+ "               //hide or show id columns\n" + "               if (that.hideIdColumn) {\n" + "\n"
				+ "                  var index = that.columns.findIndex(function (elem) {\n"
				+ "                     return elem.label === 'id'\n" + "                  });\n"
				+ "                  if (index > -1) {\n" + "                     that.columns.splice(index, 1);\n"
				+ "                  }\n" + "\n" + "               }\n"
				+ "               if (that.visibleColumns.length == 0 && that.columns.length > 0) {\n"
				+ "                  that.visibleColumns = Array.from(that.columns);\n"
				+ "                  that.columnsParams = Array.from(that.columns);\n"
				+ "                  that.visibleColumns.forEach(function (element) {\n"
				+ "                     element.visible = true;\n" + "                  });\n" + "               }\n"
				+ "\n" + "\n" + "            }\n" + "         })\n" + "      },\n"
				+ "      //function that is executed when the edition of a record is opened\n"
				+ "      openEdit: function () {\n" + "         var that = this;\n"
				+ "         vm.crudFindById(this.recordSelected, this.selectedOntology).then(function (data) {\n"
				+ "            delete data.data[0]._id;\n" + "            delete data.data[0].contextData;\n"
				+ "            if (typeof that.jEditor.destroy == 'function') that.jEditor.destroy();\n"
				+ "            that.jEditor = new JSONEditor(document.getElementsByClassName(vm.id+' editor_edit_holder')[0], {\n"
				+ "               schema: JSON.parse(that.selectedOntologySchema),\n"
				+ "               startval: data.data[0],\n" + "               theme: 'bootstrap3',\n"
				+ "               iconlib: 'fontawesome4',\n" + "               disable_properties: true,\n"
				+ "               disable_edit_json: true,\n" + "               disable_collapse: true,\n"
				+ "               disable_array_reorder: true,\n"
				+ "               disable_array_delete_all_rows: true,\n"
				+ "               disable_array_delete_last_row: true,\n" + "               show_errors: 'change'\n"
				+ "            });\n" + "         })\n" + "      },\n"
				+ "      //function that is executed when the detail of a record is opened\n"
				+ "      openShow: function () {\n" + "         var that = this;\n"
				+ "         vm.crudFindById(this.recordSelected, this.selectedOntology).then(function (data) {\n"
				+ "            delete data.data[0]._id;\n" + "            delete data.data[0].contextData;\n"
				+ "            if (typeof that.jShowEditor.destroy == 'function') that.jShowEditor.destroy();\n"
				+ "            that.jShowEditor = new JSONEditor(document.getElementsByClassName(vm.id+' editor_show_holder')[0], {\n"
				+ "               schema: JSON.parse(that.selectedOntologySchema),\n"
				+ "               startval: data.data[0],\n" + "               theme: 'bootstrap3',\n"
				+ "               mode: 'view',\n" + "               iconlib: 'fontawesome4',\n"
				+ "               disable_properties: true,\n" + "               disable_edit_json: true,\n"
				+ "               disable_collapse: true,\n" + "               disable_array_reorder: true,\n"
				+ "               disable_array_delete_all_rows: true,\n"
				+ "               disable_array_delete_last_row: true,\n" + "               show_errors: 'change'\n"
				+ "            });\n" + "            that.jShowEditor.disable();\n" + "         })\n" + "      },\n"
				+ "      //function that is executed when modal of creating a record is opened\n"
				+ "      openCreate: function () {\n" + "         var that = this;\n"
				+ "         vm.crudFindById(this.recordSelected, this.selectedOntology).then(function (data) {\n" + "\n"
				+ "            if (typeof that.jEditor.destroy == 'function') that.jEditor.destroy();\n"
				+ "            that.jEditor = new JSONEditor(document.getElementsByClassName(vm.id+' editor_new_holder')[0], {\n"
				+ "               schema: JSON.parse(that.selectedOntologySchema),\n"
				+ "               startval: undefined,\n" + "               theme: 'bootstrap3',\n"
				+ "               iconlib: 'fontawesome4',\n" + "               disable_properties: true,\n"
				+ "               disable_edit_json: true,\n" + "               disable_collapse: true,\n"
				+ "               disable_array_reorder: true,\n"
				+ "               disable_array_delete_all_rows: true,\n"
				+ "               disable_array_delete_last_row: true,\n" + "               show_errors: 'change'\n"
				+ "            });\n" + "         })\n" + "\n" + "\n" + "      },\n"
				+ "      //function that is executed when accepting a visibility change in the columns\n"
				+ "      aceptedChangeColumns: function () {\n" + "            //delete columns visible = false\n"
				+ "            //add columns visible = true if not exist\n" + "\n" + "            var that = this;\n"
				+ "            this.visibleColumns.forEach(function (visibleCol) {\n"
				+ "               if (that.columns.length > 0) {\n" + "                  var find = false;\n"
				+ "                  for (var i = 0; i < that.columns.length; i++) {\n"
				+ "                     if (that.columns[i].prop == visibleCol.prop) {\n"
				+ "                        find = true;\n" + "                        if (!visibleCol.visible) {\n"
				+ "                           that.columns.splice(i, 1);\n" + "                        }\n"
				+ "                        break;\n" + "                     }\n" + "                  }\n"
				+ "                  if (!find && visibleCol.visible) {\n"
				+ "                     var obj = Object.assign({}, visibleCol);\n"
				+ "                     delete obj.visible;\n" + "                     that.columns.push(obj);\n"
				+ "                  }\n" + "               } else {\n"
				+ "                  if (visibleCol.visible) {\n"
				+ "                     var obj = Object.assign({}, visibleCol);\n"
				+ "                     delete obj.visible;\n" + "                     that.columns.push(obj);\n"
				+ "                  }\n" + "               }\n" + "            });\n"
				+ "            this.dialogOptionsColumnsVisible = false;\n" + "            this.loadData(true);\n"
				+ "         }\n" + "\n" + "         ,\n"
				+ "      //function that is executed when clicking on edit a record\n"
				+ "      handleEdit: function (index, row) {\n" + "         this.recordSelected = row[this.idPath];\n"
				+ "         this.editTitle = this.$t('form.edit.record') + this.recordSelected;\n"
				+ "         this.dialogEditVisible = true;\n" + "\n" + "      },\n"
				+ "      //function that is executed when clicking on show a record\n"
				+ "      handleShow: function (index, row) {\n" + "         this.recordSelected = row[this.idPath];\n"
				+ "         this.showTitle = this.$t('form.detail.record') + this.recordSelected;\n"
				+ "         this.dialogShowVisible = true;\n" + "\n" + "      },\n"
				+ "      //function that is executed when clicking on delete a record\n"
				+ "      handleDelete: function (index, row) {\n" + "         this.recordSelected = row[this.idPath];\n"
				+ "         this.dialogDeleteVisible = true;\n" + "      },\n"
				+ "      //function that is executed when accepting to edit a record\n"
				+ "      aceptedEdit: function () {\n" + "         var that = this;\n"
				+ "         console.log(this.jEditor.getValue());\n"
				+ "         vm.crudUpdate(this.jEditor.getValue(), this.selectedOntology, this.recordSelected).then(function (data) {\n"
				+ "            that.dialogEditVisible = false;\n" + "            that.loadData();\n"
				+ "            that.$notify({\n" + "               message: that.$t('message.edited.successfully'),\n"
				+ "               type: 'success'\n" + "            });\n" + "         })\n" + "\n" + "      },\n"
				+ "      //function that is executed when accepting to create a new record\n"
				+ "      aceptedCreate: function () {\n" + "         var that = this;\n"
				+ "         console.log(this.jEditor.getValue());\n" + "\n"
				+ "         vm.crudInsert(this.jEditor.getValue(), this.selectedOntology).then(function (data) {\n"
				+ "            that.dialogCreateVisible = false;\n" + "            that.loadData();\n"
				+ "            that.$notify({\n" + "               message: that.$t('message.created.successfully'),\n"
				+ "               type: 'success'\n" + "            });\n" + "         })\n" + "\n" + "      },\n"
				+ "      //function that is executed when accepting to delete a record\n"
				+ "      aceptedDelete: function () {\n" + "         var that = this;\n"
				+ "         vm.crudDeleteById(this.recordSelected, this.selectedOntology).then(function (data) {\n"
				+ "            that.loadData();\n" + "            that.$notify({\n"
				+ "               message: that.$t('message.deleted.successfully'),\n"
				+ "               type: 'success'\n" + "            });\n" + "         })\n"
				+ "         this.dialogDeleteVisible = false\n" + "      },\n" + "      submit: function (_e) {\n"
				+ "         alert(JSON.stringify(this.model));\n" + "      },\n" + "      reset: function () {\n"
				+ "         this.$refs.JsonEditor.reset();\n" + "      },\n"
				+ "      //function that is executed when selecting an entity\n" + "      onChangeEntity(value) {\n"
				+ "         this.loadHeadTable();\n" + "         this.calculeTableheight();\n"
				+ "         this.visibleColumns = [];\n" + "         this.resetWizard();\n"
				+ "         this.executeSearch=false;\n" + "      },\n" + "\n" + "\n"
				+ "      opendialogAddSelect: function () {\n" + "\n" + "      },\n"
				+ "      opendialogAddOrderBy: function () {\n" + "\n" + "      },\n"
				+ "      //function that clears the wizard fields\n" + "      resetWizard: function () {\n"
				+ "         this.selectWizard = [];\n" + "         this.selectWizardOptions = [];\n"
				+ "         this.orderByWizard = [];\n" + "         this.orderByWizardOptions = [];\n"
				+ "         this.limitWizard = 100;\n" + "         this.offsetWizard = 0;\n" + "      },\n"
				+ "      searchWizard: function () {\n" + "         this.loadData(true);\n"
				+ "         this.executeSearch=true;\n" + "      },\n"
				+ "      //function that creates a new option in the where combo\n"
				+ "      aceptedAddWhereParameter: function () {\n"
				+ "         if (typeof this.selectedParametereWhere != 'undefined' && this.selectedParametereWhere != null &&\n"
				+ "            typeof this.selectedOperatorWhere != 'undefined' && this.selectedOperatorWhere != null &&\n"
				+ "            typeof this.inputValueWhere != 'undefined' && this.inputValueWhere != null) {\n"
				+ "            var paramDescription = '';\n" + "            var type = '';\n"
				+ "            for (var i = 0; i < this.columnsParams.length; i++) {\n"
				+ "               if (this.columnsParams[i].prop == this.selectedParametereWhere) {\n"
				+ "                  paramDescription = this.columnsParams[i].label;\n"
				+ "                  type = this.columnsParams[i].thetype;\n" + "                  break\n"
				+ "               }\n" + "            }\n" + "            if (type != 'number') {\n"
				+ "               this.inputValueWhere = \"'\" + this.inputValueWhere + \"'\";\n" + "            }\n"
				+ "            var resultDescription = paramDescription + ' ' + this.selectedOperatorWhere + ' ' + this.inputValueWhere;\n"
				+ "            var resultPath = {\n" + "               column: this.selectedParametereWhere,\n"
				+ "               operator: this.selectedOperatorWhere,\n" + "               condition: 'AND',\n"
				+ "               value: this.inputValueWhere\n" + "            };\n"
				+ "            this.selectWizardOptions.push({\n" + "               label: resultDescription,\n"
				+ "               value: JSON.stringify(resultPath)\n" + "            });\n"
				+ "            this.dialogAddSelectVisible = false;\n" + "         } else {\n"
				+ "            //show message need parameters\n" + "            that.$notify({\n"
				+ "               message: that.$t('error.message.incomplete'),\n" + "               type: 'warning'\n"
				+ "            });\n" + "         }\n" + "      },\n"
				+ "      aceptedAddOrderByParameter: function () {\n"
				+ "         if (typeof this.selectedOperatorOrderBy != 'undefined' && this.selectedOperatorOrderBy != null &&\n"
				+ "            typeof this.selectedParametereOrderBy != 'undefined' && this.selectedParametereOrderBy != null) {\n"
				+ "            var paramDescription = '';\n" + "            var type = '';\n"
				+ "            for (var i = 0; i < this.columnsParams.length; i++) {\n"
				+ "               if (this.columnsParams[i].prop == this.selectedParametereOrderBy) {\n"
				+ "                  paramDescription = this.columnsParams[i].label;\n" + "                  break\n"
				+ "               }\n" + "            }\n"
				+ "            var resultDescription = paramDescription + \" \" + this.selectedOperatorOrderBy;\n"
				+ "            var resultPath = {\n" + "               column: this.selectedParametereOrderBy,\n"
				+ "               order: this.selectedOperatorOrderBy\n" + "            };\n"
				+ "            this.orderByWizardOptions.push({\n" + "               label: resultDescription,\n"
				+ "               value: JSON.stringify(resultPath)\n" + "            });\n" + "\n"
				+ "            this.dialogAddOrderByVisible = false;\n" + "         }\n" + "      },\n"
				+ "      //initialize orderby\n" + "      dialogAddOrderByVisibleFunction: function () {\n"
				+ "         this.selectedOperatorOrderBy = null;\n"
				+ "         this.selectedParametereOrderBy = null;\n"
				+ "         this.dialogAddOrderByVisible = true;\n" + "      },\n" + "      //initialize where\n"
				+ "      dialogAddSelectVisibleFunction: function () {\n"
				+ "         this.selectedParametereWhere = null;\n" + "         this.selectedOperatorWhere = null;\n"
				+ "         this.inputValueWhere = \"\";\n" + "         this.dialogAddSelectVisible = true;\n"
				+ "      },\n" + "      orderColumns: function (a, b) {\n" + "         if (a.label == 'id') {\n"
				+ "            return 1;\n" + "         } else if (b.label == 'id') {\n" + "            return -1;\n"
				+ "         } else if (a.label > b.label) {\n" + "            return 1;\n"
				+ "         } else if (a.label < b.label) {\n" + "            return -1;\n" + "         }\n"
				+ "         return 0;\n" + "      },\n" + "      orderKeys: function (a, b) {\n"
				+ "         if (a == 'id') {\n" + "            return 1;\n" + "         } else if (b == 'id') {\n"
				+ "            return -1;\n" + "         } else if (a > b) {\n" + "            return 1;\n"
				+ "         } else if (a < b) {\n" + "            return -1;\n" + "         }\n"
				+ "         return 0;\n" + "      },\n" + "\n" + "      tableDatafilter: function (element) {\n"
				+ "         if (this.searchString == null || this.searchString.trim().length == 0) {\n"
				+ "            return true;\n" + "         }\n"
				+ "         return JSON.stringify(element).toLowerCase().indexOf(this.searchString.toLowerCase()) > -1;\n"
				+ "\n" + "      },\n" + "      aceptedDownloadAll: function(){\n" + "         var that = this;\n"
				+ "         vm.validationDownloadEntity(this.selectedOntology,this.downloadType).then(function(data){\n"
				+ "            if(data.data.message=='ok'){\n" + "               if(that.downloadType=='csv'){\n"
				+ "                  vm.downloadEntityAllCsv(that.selectedOntology);\n"
				+ "                  that.dialogDownloadVisible=false;\n" + "               }else{\n"
				+ "                  vm.downloadEntityAllJson(that.selectedOntology);\n"
				+ "                  that.dialogDownloadVisible=false;\n" + "               }\n"
				+ "            }else{\n" + "                that.$notify({\n"
				+ "                        message: that.$t(data.data.message),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "            }\n"
				+ "         })\n" + "      },\n" + "      aceptedDownloadOnlySelec: function () {\n"
				+ "         var selection = encodeURIComponent(JSON.stringify({ ontology: this.selectedOntology, columns: [], where: this.mapArrayToObjects(this.selectWizard), orderBy: this.mapArrayToObjects(this.orderByWizard), limit: this.limitWizard, offset: this.offsetWizard }));\n"
				+ "         var that = this;\n"
				+ "         vm.validationDownloadEntitySelected(this.selectedOntology, selection,this.downloadType).then(function (data) {\n"
				+ "            if (data.data.message == 'ok') {\n"
				+ "               if (that.downloadType == 'csv') {\n"
				+ "                  vm.downloadEntitySelectedCsv(that.selectedOntology, selection);\n"
				+ "                  that.dialogDownloadVisible = false;\n" + "               } else {\n"
				+ "                  vm.downloadEntitySelectedJson(that.selectedOntology, selection);\n"
				+ "                  that.dialogDownloadVisible = false;\n" + "               }\n"
				+ "            }else{\n" + "               that.$notify({\n"
				+ "                        message: that.$t(data.data.message),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "            }\n"
				+ "         })\n" + "      },\n" + "      downloadData: function (command) {\n"
				+ "         var that = this;\n" + "         if (command === 'allcsv') {\n"
				+ "            vm.validationDownloadEntity(this.selectedOntology,'csv').then(function (data) {\n"
				+ "               if (data.data.message == 'ok') {\n"
				+ "                  vm.downloadEntityAllCsv(that.selectedOntology);\n" + "               } else {\n"
				+ "                  that.$notify({\n"
				+ "                           message: that.$t(data.data.message),\n"
				+ "                           type: 'error'\n" + "                        });\n" + "               }\n"
				+ "            })\n" + "         } else if (command === 'alljson') {\n"
				+ "            vm.validationDownloadEntity(this.selectedOntology,'json').then(function (data) {\n"
				+ "               if (data.data.message == 'ok') {\n"
				+ "                  vm.downloadEntityAllJson(that.selectedOntology);\n" + "               }else{\n"
				+ "                  that.$notify({\n"
				+ "                        message: that.$t(data.data.message),\n"
				+ "                           type: 'error'\n" + "                  });\n" + "               }\n"
				+ "            })\n" + "         } else {\n" + "            this.downloadType = command;\n"
				+ "            this.dialogDownloadVisible = true;\n" + "\n" + "         }\n" + "      },\n"
				+ "      mapArrayToObjects: function (array) {\n" + "         var data = [];\n"
				+ "         if (typeof array != 'undefined' && array != null && array.length > 0) {\n"
				+ "            for (var i = 0; i < array.length; i++) {\n"
				+ "               data.push(JSON.parse(array[i]));\n" + "            }\n" + "         }\n"
				+ "         return data;\n" + "      },\n" + "      sortChange(column, key, order) {\n"
				+ "         var that = this;\n" + "         var type = this.columns.filter(function (elem) {\n"
				+ "            return elem.prop == column.prop\n" + "         });\n"
				+ "         if (typeof type !== 'undefined' && type != null && type.length > 0) {\n"
				+ "            type = type[0].thetype;\n" + "         } else {\n" + "            type = 'string';\n"
				+ "         }\n" + "         this.tableData.sort(function (a, b) {\n"
				+ "            if (column.order == 'descending') {\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) > that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return -1;\n" + "               }\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) < that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return 1;\n" + "               }\n" + "            } else {\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) < that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return -1;\n" + "               }\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) > that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return 1;\n" + "               }\n" + "            }\n" + "            return 0;\n"
				+ "         });\n" + "\n" + "         console.log(column, key, order)\n" + "      },\n"
				+ "      loadProperties:function(element,path,stack){\n" + "         if(element.properties){\n"
				+ "            var keys = Object.keys(element.properties);\n" + "            var dot='';\n"
				+ "            if (path.length>0){dot='.';}\n" + "            for(var i=0;i< keys.length; i++){\n"
				+ "                  this.loadProperties(element.properties[keys[i]],path+dot+keys[i],stack);\n"
				+ "               }\n" + "         }else{\n" + "            var keys = Object.keys(element);\n"
				+ "            var findRef = false;\n" + "            var ref = \"\";\n"
				+ "            for(var i=0;i< keys.length; i++){\n" + "               if( keys[i] == '$ref' ){\n"
				+ "                  findRef = true;\n" + "                  ref = element.$ref.substring(2);\n"
				+ "                  break;\n" + "               }\n" + "            }\n"
				+ "            if( findRef ){\n" + "               stack.push({'path':path,'ref':ref});\n"
				+ "            }else{\n" + "               if(element.description){\n"
				+ "                  element.title = this.$t(element.description);\n"
				+ "                  delete element.description;\n" + "               }else{\n"
				+ "                  element.title = this.$t(path);\n" + "               }\n" + "\n" + "            }\n"
				+ "         }\n" + "      },\n" + "\n"
				+ "      //This function maps the labels by titles in the outline for the edit, creation and detail forms\n"
				+ "      changeDescriptionForTitle: function (schema) {\n" + "\n"
				+ "         var root = JSON.parse(schema);\n" + "         var stack = [];\n"
				+ "         var result = this.loadProperties(root,'',stack);\n" + "         while(stack.length > 0){\n"
				+ "            var stackElement = stack.pop();\n"
				+ "            this.loadProperties(root[stackElement.ref],stackElement.path,stack);\n" + "         }\n"
				+ "         return JSON.stringify(root);\n" + "      },\n"
				+ "      formatFieldForSort: function (element, type) {\n" + "         if (type == 'number') {\n"
				+ "            return Number(element);\n" + "         } else if (type == 'string') {\n"
				+ "            return element + '';\n" + "         } else {\n" + "            return element;\n"
				+ "         }\n" + "      },\n" + "      sendValue: vm.sendValue,\n"
				+ "      sendFilter: vm.sendFilter,\n" + "      //calculate and resize the table\n"
				+ "      calculeTableheight: function () {\n" + "         var totalHeight = 240;\n"
				+ "         if (this.showWizard) {\n" + "            totalHeight = totalHeight + 108;\n"
				+ "         }\n"
				+ "         this.tableHeight = document.getElementById(vm.id).querySelector('vuetemplate').offsetHeight - totalHeight;\n"
				+ "      }\n" + "   },\n" + "   mounted() {\n" + "      if(vm.tparams && vm.tparams.parameters){\n"
				+ "         this.hideIdColumn=vm.tparams.parameters.hideIdColumn;\n"
				+ "         this.initialEntity=vm.tparams.parameters.initialEntity;\n"
				+ "         this.typeGadget=vm.tparams.parameters.typeGadget;\n" + "      }\n" + "\n"
				+ "      this.loadEntities();\n" + "      var that = this;\n" + "      //Resize event observer\n"
				+ "      this.resizeObserver = new ResizeObserver(function (entrie) {\n"
				+ "         that.calculeTableheight();\n" + "      });\n"
				+ "      this.resizeObserver.observe(document.getElementById(vm.id).querySelector('vuetemplate'));\n"
				+ "   },\n" + "   i18n: window.i18n\n" + "\n" + "}\n" + "//Init Vue app\n"
				+ "vm.vueapp = new Vue(vm.vueconfig);\n" + "");
		gadgetTemplate.setConfig(
				"{\"gform\":[{\"id\":1,\"type\":\"input-text\",\"name\":\"initialEntity\",\"default\":\"\",\"title\":\"initialEntity\"},{\"id\":3,\"type\":\"selector\",\"name\":\"typeGadget\",\"options\":[{\"value\":\"withWizard\",\"text\":\"withWizard\"},{\"value\":\"noWizard\",\"text\":\"noWizard\"},{\"value\":\"searchOnly\",\"text\":\"searchOnly\"}],\"title\":\"typeGadget\",\"default\":\"withWizard\"},{\"id\":4,\"type\":\"checkbox\",\"name\":\"hideIdColumn\",\"default\":false,\"title\":\"hideIdColumn\"}]}");
		gadgetTemplate.setUser(getUserAdministrator());
		gadgetTemplateRepository.save(gadgetTemplate);

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-8").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
		} else {
			gadgetTemplate = gadgetTemplateRepository.findById("MASTER-GadgetTemplate-8").get();
		}

		gadgetTemplate.setId("MASTER-GadgetTemplate-8");
		gadgetTemplate.setIdentification("gadget-import");
		gadgetTemplate.setPublic(true);
		gadgetTemplate.setType("vueJS");
		gadgetTemplate.setHeaderlibs(
				"<script src=\"/controlpanel/static/vendor/element-ui/index.js\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
						+ "<script src=\"/controlpanel/static/vendor/element-ui/locale/en.min.js\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\"></script>\n"
						+ "\n" + "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n"
						+ "<link rel=\"stylesheet\" href=\"/controlpanel/static/vendor/element-ui/theme-chalk/index.css\"  crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />\n"
						+ "\n" + "\n" + "<script>\n" + "ELEMENT.locale(ELEMENT.lang.en)\n"
						+ "var __env = __env || {};\n"
						+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
						+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
						+ "			\"form.entity\": \"Entidad\",\n"
						+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
						+ "			\"form.select\": \"Seleccionar\",\n"
						+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
						+ "			\"form.operator\": \"Operador\",\n"
						+ "			\"form.condition\": \"Condición\",\n"
						+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
						+ "			\"form.write.here\": \"Escriba aquí\",\n"
						+ "			\"form.select.field\": \"Seleccionar campo\",\n"
						+ "			\"form.orderby\": \"Ordenar por\",\n"
						+ "			\"form.order.type\": \"Tipo de pedido\",\n"
						+ "			\"form.where\": \"Where\",\n" + "			\"form.max.value\": \"Valor máximo\",\n"
						+ "			\"form.offset\": \"Desplazamiento\",\n"
						+ "			\"form.reset\": \"Restablecer\",\n" + "			\"form.search\": \"Buscar\",\n"
						+ "			\"form.records\": \"Registros\",\n" + "			\"form.columns\": \"Columnas\",\n"
						+ "			\"column.options\": \"Opciones\",\n"
						+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
						+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
						+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
						+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
						+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
						+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
						+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
						+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
						+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
						+ "			\"form.edit.record\": \"Editar registro\",\n"
						+ "			\"form.detail.record\": \"Registro detallado\",\n"
						+ "			\"button.cancel\": \"Cancelar\",\n" + "			\"button.delete\": \"Eliminar\",\n"
						+ "			\"button.save\": \"Guardar\",\n" + "			\"button.close\": \"Cerrar\",\n"
						+ "			\"button.new\": \"Nuevo\",\n" + "			\"button.apply\": \"Aplicar\",\n"
						+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
						+ "		    \"form.title.import\": \"Importar datos\",\n"
						+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
						+ "			\"form.download.csv\":\"Descargar CSV\",\n"
						+ "    		\"form.download.json\":\"Descargar JSON\",\n"
						+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
						+ "		    \"button.click\": \"haga click aquí\",\n"
						+ "		    \"button.click.upload\": \"para subirlo\",\n"
						+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n"
						+ "		    \"button.import\": \"Importar\",\n"
						+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
						+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
						+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
						+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
						+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
						+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
						+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
						+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
						+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
						+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
						+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
						+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
						+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
						+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
						+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
						+ "			\"button.all.records\": \"Todos los registros\",\n"
						+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
						+ "			\"error.message.download\": \"Error descargando datos\",\n"
						+ "			\"error.message.empty\": \"Error no existen registros\",\n"
						+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
						+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
						+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
						+ "			\"form.show.wizard\": \"Show search wizard\",\n"
						+ "			\"form.select\": \"Select\",\n"
						+ "			\"form.select.fields\": \"Select Fields\",\n"
						+ "			\"form.operator\": \"Operator\",\n"
						+ "			\"form.condition\": \"Condition\",\n"
						+ "			\"form.select.operator\": \"Select Operator\",\n"
						+ "			\"form.write.here\": \"Write here\",\n"
						+ "			\"form.select.field\": \"Select Field\",\n"
						+ "			\"form.orderby\": \"Order by\",\n"
						+ "			\"form.order.type\": \"Order Type\",\n" + "			\"form.where\": \"Where\",\n"
						+ "			\"form.max.value\": \"Max Value\",\n" + "			\"form.offset\": \"Offset\",\n"
						+ "			\"form.reset\": \"Reset\",\n" + "			\"form.search\": \"Search\",\n"
						+ "			\"form.records\": \"Records\",\n" + "			\"form.columns\": \"Columns\",\n"
						+ "			\"column.options\": \"Options\",\n"
						+ "			\"form.new.record.title\": \"New record\",\n"
						+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
						+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
						+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
						+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
						+ "			\"message.created.successfully\": \"Record created successfully\",\n"
						+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
						+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
						+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
						+ "			\"form.edit.record\": \"Edit record \",\n"
						+ "			\"form.detail.record\": \"Detail record \",\n"
						+ "			\"button.cancel\": \"Cancel\",\n" + "			\"button.delete\": \"Delete\",\n"
						+ "			\"button.save\": \"Save\",\n" + "			\"button.close\": \"Close\",\n"
						+ "			\"button.new\": \"New\",\n" + "			\"button.apply\": \"Apply\",\n"
						+ "		    \"form.select.entity\": \"Select Entity\",\n"
						+ "		    \"form.title.import\": \"Import records\",\n"
						+ "		    \"form.download.template\": \"Download Template\",\n"
						+ "			\"form.download.csv\":\"Download CSV\",\n"
						+ "    		\"form.download.json\":\"Download JSON\",\n"
						+ "		    \"button.drop\": \"Drop file or\",\n"
						+ "		    \"button.click\": \"click here\",\n"
						+ "		    \"button.click.upload\": \"to upload\",\n"
						+ "		    \"form.info.max\": \"Max. 2mb csv\",\n"
						+ "		    \"button.import\": \"Import\",\n"
						+ "		    \"button.showmore\": \"Show More Details\",\n"
						+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
						+ "		    \"message.success.loaded.1\": \"The\",\n"
						+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
						+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
						+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
						+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
						+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
						+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
						+ "		    \"error.message.processing\": \"Error processing data\",\n"
						+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
						+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
						+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
						+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
						+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
						+ "			\"button.all.records\": \"All the records\",\n"
						+ "			\"button.only.selection.records\": \"Only the selection\",\n"
						+ "			\"error.message.download\": \"Error downloading data\",\n"
						+ "			\"error.message.empty\": \"Error there are no records\",\n"
						+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
						+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
						+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n"
						+ "	var localLocale ='EN';\n" + "	try{\n"
						+ "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
						+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
						+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
						+ " // link messages with internacionalization json on controlpanel\n"
						+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");
		gadgetTemplate.setDescription("IMPORT gadget template");
		gadgetTemplate.setTemplate("<style>\n" + ".el-upload-list__item-name {\n" + "  max-height:30px;\n"
				+ "  font-size: small;}\n" + ".control-label {\n" + "  margin-top: 1px;\n" + "  color: #505D66;\n"
				+ "  font-weight: normal;\n" + "  width: fit-content;\n" + "  font-size: small; }\n"
				+ ".control-label .required,\n" + ".form-group .required {\n" + "  color: #A73535;\n"
				+ "  font-size: 12px;\n" + "  padding-left: 2px; }\n" + ".el-textarea__inner {\n"
				+ "  min-height: 40% !important;\n" + "} \n" + ".el-upload {\n" + "  width: 100%;\n" + "}\n"
				+ ".el-upload-dragger{\n" + "  width: 100%;\n" + "  height: 150px;\n" + "}\n" + "footer {\n"
				+ "  display: flex;\n" + "  flex-direction: row;\n" + "  align-items: center;\n"
				+ "  justify-content: flex-end;\n" + "  padding: 16px 24px;\n" + "  background: #F0F1F2;\n" + "}\n"
				+ ".livehtmlnotfull {\n" + "  display: block;\n" + "  width: calc(100%);\n" + "  position: absolute;\n"
				+ "  top: 50%;\n" + "  left: 50%;\n" + "  transform: translate(-50%, -50%);\n" + "  overflow: hidden;\n"
				+ "}\n" + ".custommargin {\n" + "  margin-left:15px; \n" + "  margin-right:15px;\n" + "}\n"
				+ ".textButtonColor {\n" + "  color: #1168A6 !important;\n" + "}\n" + ".icons-grey {\n"
				+ "  filter: invert(0%) sepia(0%) saturate(0%) hue-rotate(162deg) brightness(93%) contrast(88%);\n"
				+ "}\n" + ".icons-align {\n" + "  vertical-align: middle;\n" + "  margin-left: 5px;\n" + "}\n"
				+ "</style>\n" + "\n" + "<div class=\"gadget-app\">\n"
				+ "  <h5 class=\"gadget-title ng-binding ng-scope custommargin\">{{ $t(\"form.title.import\") }}</h5>\n"
				+ "  <div class=\"selectOnto custommargin\" v-if=\"showSelect\">\n"
				+ "    <label class=\"control-label\"> {{ $t(\"form.select.entity\") }} <span class=\"required\" aria-required=\"true\"> *  </span></label>\n"
				+ "    <el-select :disabled=\"showSelectOntology\" v-model=\"selectedOntology\" @change=\"onChangeOntology($event)\" filterable :placeholder=\"$t('form.select')\" \n"
				+ "      style=\"margin-top:5px; width: 100%;background: #F7F8F8;\" size=\"small\">\n"
				+ "      <el-option\n" + "        v-for=\"onto in ontologies\"\n"
				+ "        :key=\"onto.identification\"\n" + "        :label=\"onto.identification\"\n"
				+ "        :value=\"onto.identification\">\n" + "      </el-option>\n" + "    </el-select>\n"
				+ "  </div>\n" + "  <div class=\"custommargin\" v-if=\"showEntityName\">\n"
				+ "    <label class=\"control-label\" style=\"color:#060E14;\">{{ $t(\"form.entity\")}}: </label><label class=\"control-label\"> {{selectedOntology}} <label>\n"
				+ "  </div>\n" + "  <div class=\"downloadSchm custommargin\" style=\"margin-top: 10px;\">\n"
				+ "    <el-button class=\"textButtonColor\" style=\"margin-top: 5px;border: 1px solid #1168A6;box-sizing: border-box;border-radius: 2px;text-align: center;\" \n"
				+ "      size=\"small\" :disabled= \"downloaddisabled\" plain @click=\"dialogDownloadOptions = true\">{{ $t(\"form.download.template\") }} <i class=\"el-icon-download\"></i></el-button>\n"
				+ "     <el-popover placement=\"top-start\" title=\"Info\" width=\"240\" trigger=\"hover\" :content=\"$t('form.download.info')\">\n"
				+ "      <img class=\"icons-grey icons-align\" src=\"/controlpanel/static/images/dashboards/icon_info.svg\" slot=\"reference\">\n"
				+ "    </el-popover>\n" + "  </div>\n" + "  \n"
				+ "  <div id=\"uploadCSVFile\" class=\"custommargin\" style=\"margin-top: 15px;\">\n"
				+ "    <el-upload id=\"upload-csv\" drag style=\"width: 100%; height: 210px;\"\n"
				+ "      :action=\"urlimport\" :disabled= \"uploaddisabled\"\n"
				+ "      :auto-upload=\"false\" :file-list=\"fileList\" \n"
				+ "      :on-preview=\"handlePreview\" :on-remove=\"handleRemove\" \n"
				+ "      :limit=\"limitUpload\"\n" + "      accept=\".csv, .json\" :before-upload=\"beforeCSVUpload\"\n"
				+ "      ref=\"upload\"  :on-exceed=\"handleExceed\"\n"
				+ "      :on-error=\"handleError\" :on-success=\"handleSuccess\">\n"
				+ "      <i class=\"el-icon-upload2 fa-3x\" style=\"margin-top:25px;color: #505D66;\"></i>\n"
				+ "      <div class=\"el-upload__text\">{{ $t(\"button.drop\") }} <em class=\"textButtonColor\">{{ $t(\"button.click\") }}</em> {{ $t(\"button.click.upload\") }}</div>\n"
				+ "      <div slot=\"tip\" class=\"el-upload__tip\" style=\"font-size: 11px;line-height: 16px;color: #A7AEB2;\">{{ $t(\"form.info.max\") }}</div>\n"
				+ "    </el-upload>\n" + "  </div>\n" + "  <footer style=\"margin-top: 10px;\">\n"
				+ "    <div slot=\"tip\" style=\"text-align: right;\">\n"
				+ "      <el-button class=\"textButtonColor\" style=\"margin-left: 10px; background: #F0F1F2; border: none;text-align: center;\" size=\"small\" plain @click=\"clearFiles\">Cancel</el-button>\n"
				+ "      <el-button style=\"margin-left: 10px; background: #1168A6; border-radius: 2px;text-align: center;\" ref=\"importbutton\" size=\"small\" \n"
				+ "        :disabled=\"importdisabled\" type=\"primary\" @click=\"submitUpload\">{{ $t(\"button.import\") }} <i class=\"el-icon-upload2\"></i></el-button>\n"
				+ "    </div>\n" + "  </footer>\n"
				+ "  <el-dialog modal append-to-body title=\"Error\" :visible.sync=\"dialogCreateVisible\" width=\"35%\" @close=\"closeErrDialog\">\n"
				+ "    <div class=\"el-message-box__container\">\n"
				+ "      <div class=\"el-message-box__status el-icon-error\"></div>\n"
				+ "      <div class=\"el-message-box__message\">\n" + "        <span>{{msgerr}}</span>\n"
				+ "      </div>\n" + "    </div>\n" + "     <div style=\"margin-top: 5px;\">\n"
				+ "      <el-button class=\"textButtonColor\" v-if=\"showDetailBtn\" size=\"small\" @click=\"showErrDetails\" style=\"margin-left: 30px; border: none;text-align: center;\">{{ $t(\"button.showmore\") }}</el-button>\n"
				+ "      <el-input v-if=\"showDetails\" type=\"textarea\" v-model=\"detailerr\" readonly></el-input>\n"
				+ "    </div>\n" + "    <div style=\"text-align: right; margin-top: 10px;\">\n"
				+ "      <el-button class=\"textButtonColor\" @click=\"closeErrDialog\" style=\"margin-left: 10px; border: none;text-align: center;\" size=\"small\">{{ $t(\"button.cancel\") }}</el-button>\n"
				+ "    </div>\n" + "  </el-dialog>\n"
				+ "  <el-dialog modal append-to-body :title=\"$t('message.choose.download.format')\" :visible.sync=\"dialogDownloadOptions\" width=\"25%\" @close=\"closeDialog\">\n"
				+ "    <div class=\"el-message-box__container\" style=\"text-align: center;margin-top: 2px;\">\n"
				+ "          <el-button class=\"textButtonColor\" style=\"margin-left: 10px;margin-top: 10px;border: 1px solid #1168A6;box-sizing: border-box;border-radius: 2px;text-align: center;\" \n"
				+ "          size=\"small\" plain @click=\"getCSVSchema\"> {{ $t(\"form.download.csv\") }} <i class=\"el-icon-download\"></i></el-button>\n"
				+ "          <el-button class=\"textButtonColor\" style=\"margin-top: 10px;border: 1px solid #1168A6;box-sizing: border-box;border-radius: 2px;text-align: center;\" \n"
				+ "          size=\"small\" plain @click=\"getJSONSchema\"> {{ $t(\"form.download.json\") }} <i class=\"el-icon-download\"></i></el-button>\n"
				+ "    </div>\n" + "  </el-dialog>\n" + "</div>");
		gadgetTemplate.setTemplateJS("vm.vueconfig = {\n"
				+ "    el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),\n" + "    data:{\n"
				+ "        initialEntity: \"\" , //variable that initializes the entity with the value assigned to it\n"
				+ "        ontologies:[],\n" + "        selectedOntology:{},\n" + "        fileList:[],\n"
				+ "        urlimport:'',\n" + "        importdisabled: '',\n" + "        downloaddisabled: '',\n"
				+ "        uploaddisabled:'',\n" + "        detailerr:'',\n" + "        msgerr:'',\n"
				+ "        showDetails: false,\n" + "        dialogCreateVisible:false,\n" + "        limitUpload:1,\n"
				+ "        showDetailBtn: false,\n" + "        showSelectOntology: false,\n"
				+ "        showSelect: true,\n" + "        showEntityName: false,\n"
				+ "        dialogDownloadOptions: false\n" + "    },\n" + "    methods:{\n"
				+ "        drawVueComponent: function(newData,oldData){\n"
				+ "            //This will be call on new data\n" + "        },\n"
				+ "        resizeEvent: function(){\n" + "            //Resize event\n" + "        },\n"
				+ "        destroyVueComponent: function(){\n" + "            vm.vueapp.$destroy();\n" + "        },\n"
				+ "        receiveValue: function(data){\n" + "            //data received from datalink\n"
				+ "        },\n" + "        loadOntologies:function(search, loading) {\n"
				+ "            var that = this;\n" + "            vm.getEntities().then(function(data){\n"
				+ "                that.ontologies = data.data.map(function(obj){return {id:obj.id,identification:obj.identification}});\n"
				+ "\n" + "                if(that.initialEntity!=null && that.initialEntity!==\"\"){\n"
				+ "                    if (that.ontologies.some(e => e.identification === that.initialEntity)) {\n"
				+ "                            that.selectedOntology = that.initialEntity;\n"
				+ "                            that.importdisabled = false;\n"
				+ "                            that.downloaddisabled = false;\n"
				+ "                            that.uploaddisabled = false;\n"
				+ "                            that.showSelect = false;\n"
				+ "                            that.showEntityName = true;\n"
				+ "                            that.onChangeOntology( that.initialEntity);"
				+ "                        } else {\n"
				+ "                            that.msgerr = that.$t('error.message.ontology');                      \n"
				+ "                            that.dialogCreateVisible = true;\n" + "                        }\n"
				+ "                        that.showSelectOntology=true;\n" + "                        return;\n"
				+ "                }\n" + "\n"
				+ "                var urlparam = urlParamService.generateFiltersForGadgetId(vm.id);\n"
				+ "                if(typeof urlparam!== 'undefined' && urlparam!==null && urlparam.length>0){             \n"
				+ "                    if(urlparam[0].exp!=null){\n"
				+ "                        var urlontology = urlparam[0].exp.replace(/\"/g,'');\n"
				+ "                        if(that.ontologies.some(e => e.identification === urlontology)) {\n"
				+ "                            that.selectedOntology = urlontology;\n"
				+ "                            that.importdisabled = false;\n"
				+ "                            that.downloaddisabled = false;\n"
				+ "                            that.uploaddisabled = false;\n"
				+ "                            that.showSelect = false;\n"
				+ "                            that.showEntityName = true;\n"
				+ "                            that.onChangeOntology( that.initialEntity);"
				+ "                        } else {\n"
				+ "                            that.msgerr = that.$t('error.message.ontology');                      \n"
				+ "                            that.dialogCreateVisible = true;\n" + "                        }\n"
				+ "                        that.showSelectOntology=true;\n" + "                        return;\n"
				+ "                    }\n" + "                }else{\n"
				+ "                    that.showSelectOntology=false;\n" + "                }\n" + "            })\n"
				+ "        },\n" + "        getCSVSchema:function() {\n" + "            var that = this;\n"
				+ "            vm.isComplexSchema(this.selectedOntology).then(function(data){\n"
				+ "                if(data.data.message == 'ok'){\n"
				+ "                    vm.downloadEntitySchemaCsv(that.selectedOntology);\n"
				+ "                } else{\n"
				+ "                that.msgerr = that.$t(data.data.message);               \n"
				+ "                that.dialogCreateVisible = true;\n" + "                }\n" + "            });\n"
				+ "            that.dialogDownloadOptions = false;\n" + "        },\n"
				+ "        getJSONSchema: function() {\n" + "            var that = this;\n"
				+ "            vm.downloadEntitySchemaJson(that.selectedOntology);\n"
				+ "            that.dialogDownloadOptions = false;\n" + "        },\n"
				+ "        submitUpload:function() {\n" + "            this.$refs.upload.submit();\n" + "        },\n"
				+ "         onChangeOntology(value) {\n" + "            this.selectedOntology = value;\n"
				+ "            this.urlimport = \"/dashboardengine/api/insertDataEntity/\" + value;  \n"
				+ "            this.importdisabled = false;\n" + "            this.downloaddisabled = false;\n"
				+ "            this.uploaddisabled = false;\n" + "        },\n"
				+ "        beforeCSVUpload:function(file) { \n"
				+ "            const isLt2M = file.size / 1024 < 2000;\n" + "            if(!isLt2M) {\n"
				+ "                this.$alert(this.$t('error.message.exceed'), 'Warning', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'warning'\n"
				+ "                });\n" + "                return false;\n" + "            }\n" + "        },\n"
				+ "        handleError:function(err, file, fileList){\n" + "            var jsonerr;\n"
				+ "            \n" + "            try{\n" + "                jsonerr = JSON.parse(err.message);\n"
				+ "                this.msgerr = this.$t(jsonerr.message);\n"
				+ "                if(jsonerr.detail != ''){\n"
				+ "                    this.detailerr = JSON.stringify(jsonerr.detail);\n"
				+ "                    this.showDetailBtn = true;\n" + "                }   \n"
				+ "            } catch(objError){\n" + "                this.msgerr = err.name + \" \" + err.status;\n"
				+ "                this.detailerr = err.message;\n" + "                this.showDetailBtn = true;\n"
				+ "            }           \n" + "            this.dialogCreateVisible = true;\n" + "        },\n"
				+ "        showErrDetails: function(){\n" + "            if(this.showDetails === true){\n"
				+ "                this.showDetails = false;\n" + "            } else {\n" + "                try {\n"
				+ "                    var obj = JSON.parse(this.detailerr);\n"
				+ "                    this.detailerr = JSON.stringify(obj, undefined, 4);\n"
				+ "                    this.showDetails = true;\n" + "                } catch(objError){\n"
				+ "                    this.showDetails = true;\n" + "                }\n" + "            }\n"
				+ "        },\n" + "        closeErrDialog: function(){\n" + "            this.showDetails = false;\n"
				+ "            this.showDetailBtn = false;\n" + "            this.dialogCreateVisible = false;\n"
				+ "        },\n" + "        closeDialog: function() {\n"
				+ "            this.dialogDownloadOptions = false;\n" + "        },\n"
				+ "        handleSuccess: function(response, file, fileList){\n"
				+ "            if(response.message != '') {\n"
				+ "            this.$alert(this.$t(\"message.success.loaded.1\") +' \"' + file.name + '\" ' + this.$t(\"message.success.loaded.2\") + ' \\r\\n' + this.$t(\"message.success.inserted\") + response.message, 'Success', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'success'\n"
				+ "                });\n" + "            } else {\n"
				+ "                this.$alert(this.$t(\"message.success.loaded.1\") +' \"' + file.name + '\" ' + this.$t(\"message.success.loaded.2\"), 'Success', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'success'\n"
				+ "                });\n" + "            }\n" + "            this.$refs.upload.clearFiles();\n"
				+ "        },\n" + "        handlePreview: function(file){\n" + "        },\n"
				+ "        handleRemove: function(file, fileList){\n" + "        },\n"
				+ "        handleExceed: function(files, fileList){\n"
				+ "            this.$alert(this.$t(\"message.alert.onefile\"), 'Warning', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'warning'\n"
				+ "                });\n" + "        },\n" + "        clearFiles: function(){\n"
				+ "            this.$refs.upload.clearFiles();\n" + "        },\n"
				+ "        sendValue: vm.sendValue,\n" + "        sendFilter: vm.sendFilter\n" + "    },\n"
				+ "    mounted() {\n" + "        if(vm.tparams && vm.tparams.parameters){\n"
				+ "            this.initialEntity=vm.tparams.parameters.initialEntity; \n" + "        }\n"
				+ "        \n" + "        this.loadOntologies();\n" + "        this.onChangeOntology();\n"
				+ "        this.importdisabled = true;\n" + "        this.downloaddisabled = true;\n"
				+ "        this.uploaddisabled = true;\n" + "    },\n" + "    i18n: window.i18n\n" + "}\n" + "\n"
				+ "//Init Vue app\n" + "vm.vueapp = new Vue(vm.vueconfig);");
		gadgetTemplate.setConfig(
				"{\"gform\":[{\"id\":1,\"type\":\"input-text\",\"name\":\"initialEntity\",\"default\":\"\",\"title\":\"initialEntity\"}]}");
		gadgetTemplate.setUser(getUserAdministrator());
		gadgetTemplateRepository.save(gadgetTemplate);

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-12").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
		} else {
			gadgetTemplate = gadgetTemplateRepository.findById("MASTER-GadgetTemplate-12").get();
		}

		gadgetTemplate.setId("MASTER-GadgetTemplate-12");
		gadgetTemplate.setIdentification("ods-gadget-crud");
		gadgetTemplate.setPublic(true);
		gadgetTemplate.setType("vueJSODS");
		gadgetTemplate.setHeaderlibs("<script src=\"/controlpanel/static/vendor/jsoneditor/jsoneditor.js\"></script>\n"
				+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n" + "\n" + "\n" + "\n"
				+ "\n" + "\n" + "<script>\n" + "\n" + "var __env = __env || {};\n"
				+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
				+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
				+ "			\"form.entity\": \"Entidad\",\n"
				+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
				+ "			\"form.select\": \"Seleccionar\",\n"
				+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
				+ "			\"form.operator\": \"Operador\",\n" + "			\"form.condition\": \"Condición\",\n"
				+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
				+ "			\"form.write.here\": \"Escriba aquí\",\n"
				+ "			\"form.select.field\": \"Seleccionar campo\",\n"
				+ "			\"form.orderby\": \"Ordenar por\",\n"
				+ "			\"form.order.type\": \"Tipo de pedido\",\n" + "			\"form.where\": \"Where\",\n"
				+ "			\"form.max.value\": \"Valor máximo\",\n"
				+ "			\"form.offset\": \"Desplazamiento\",\n" + "			\"form.reset\": \"Restablecer\",\n"
				+ "			\"form.search\": \"Buscar\",\n" + "			\"form.records\": \"Registros\",\n"
				+ "			\"form.columns\": \"Columnas\",\n" + "			\"column.options\": \"Opciones\",\n"
				+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
				+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
				+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
				+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
				+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
				+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
				+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
				+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
				+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
				+ "			\"form.edit.record\": \"Editar registro\",\n"
				+ "			\"form.detail.record\": \"Registro detallado\",\n"
				+ "			\"button.cancel\": \"Cancelar\",\n" + "			\"button.delete\": \"Eliminar\",\n"
				+ "			\"button.save\": \"Guardar\",\n" + "			\"button.close\": \"Cerrar\",\n"
				+ "			\"button.new\": \"Nuevo\",\n" + "			\"button.apply\": \"Aplicar\",\n"
				+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
				+ "		    \"form.title.import\": \"Importar datos\",\n"
				+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
				+ "			\"form.download.csv\":\"Descargar CSV\",\n"
				+ "    		\"form.download.json\":\"Descargar JSON\",\n"
				+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
				+ "		    \"button.click\": \"haga click aquí\",\n"
				+ "		    \"button.click.upload\": \"para subirlo\",\n"
				+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n" + "		    \"button.import\": \"Importar\",\n"
				+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
				+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
				+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
				+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
				+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
				+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
				+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
				+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
				+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
				+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
				+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
				+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
				+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
				+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
				+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
				+ "			\"button.all.records\": \"Todos los registros\",\n"
				+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
				+ "			\"error.message.download\": \"Error descargando datos\",\n"
				+ "			\"error.message.empty\": \"Error no existen registros\",\n"
				+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
				+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
				+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
				+ "			\"form.show.wizard\": \"Show search wizard\",\n"
				+ "			\"form.select\": \"Select\",\n" + "			\"form.select.fields\": \"Select Fields\",\n"
				+ "			\"form.operator\": \"Operator\",\n" + "			\"form.condition\": \"Condition\",\n"
				+ "			\"form.select.operator\": \"Select Operator\",\n"
				+ "			\"form.write.here\": \"Write here\",\n"
				+ "			\"form.select.field\": \"Select Field\",\n" + "			\"form.orderby\": \"Order by\",\n"
				+ "			\"form.order.type\": \"Order Type\",\n" + "			\"form.where\": \"Where\",\n"
				+ "			\"form.max.value\": \"Max Value\",\n" + "			\"form.offset\": \"Offset\",\n"
				+ "			\"form.reset\": \"Reset\",\n" + "			\"form.search\": \"Search\",\n"
				+ "			\"form.records\": \"Records\",\n" + "			\"form.columns\": \"Columns\",\n"
				+ "			\"column.options\": \"Options\",\n"
				+ "			\"form.new.record.title\": \"New record\",\n"
				+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
				+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
				+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
				+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
				+ "			\"message.created.successfully\": \"Record created successfully\",\n"
				+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
				+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
				+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
				+ "			\"form.edit.record\": \"Edit record \",\n"
				+ "			\"form.detail.record\": \"Detail record \",\n"
				+ "			\"button.cancel\": \"Cancel\",\n" + "			\"button.delete\": \"Delete\",\n"
				+ "			\"button.save\": \"Save\",\n" + "			\"button.close\": \"Close\",\n"
				+ "			\"button.new\": \"New\",\n" + "			\"button.apply\": \"Apply\",\n"
				+ "		    \"form.select.entity\": \"Select Entity\",\n"
				+ "		    \"form.title.import\": \"Import records\",\n"
				+ "		    \"form.download.template\": \"Download Template\",\n"
				+ "			\"form.download.csv\":\"Download CSV\",\n"
				+ "    		\"form.download.json\":\"Download JSON\",\n"
				+ "		    \"button.drop\": \"Drop file or\",\n" + "		    \"button.click\": \"click here\",\n"
				+ "		    \"button.click.upload\": \"to upload\",\n"
				+ "		    \"form.info.max\": \"Max. 2mb csv\",\n" + "		    \"button.import\": \"Import\",\n"
				+ "		    \"button.showmore\": \"Show More Details\",\n"
				+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
				+ "		    \"message.success.loaded.1\": \"The\",\n"
				+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
				+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
				+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
				+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
				+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
				+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
				+ "		    \"error.message.processing\": \"Error processing data\",\n"
				+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
				+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
				+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
				+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
				+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
				+ "			\"button.all.records\": \"All the records\",\n"
				+ "			\"button.only.selection.records\": \"Only the selection\",\n"
				+ "			\"error.message.download\": \"Error downloading data\",\n"
				+ "			\"error.message.empty\": \"Error there are no records\",\n"
				+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
				+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
				+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n" + "	var localLocale ='EN';\n"
				+ "	try{\n" + "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
				+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
				+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
				+ " // link messages with internacionalization json on controlpanel\n"
				+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");

		gadgetTemplate.setId("MASTER-GadgetTemplate-12");
		gadgetTemplate.setIdentification("ods-gadget-crud");
		gadgetTemplate.setPublic(true);
		gadgetTemplate.setType("vueJSODS");
		gadgetTemplate.setHeaderlibs("<script src=\"/controlpanel/static/vendor/jsoneditor/jsoneditor.js\"></script>\n"
				+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n" + "\n" + "\n" + "\n"
				+ "\n" + "\n" + "<script>\n" + "\n" + "var __env = __env || {};\n"
				+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
				+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
				+ "			\"form.entity\": \"Entidad\",\n"
				+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
				+ "			\"form.select\": \"Seleccionar\",\n"
				+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
				+ "			\"form.operator\": \"Operador\",\n" + "			\"form.condition\": \"Condición\",\n"
				+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
				+ "			\"form.write.here\": \"Escriba aquí\",\n"
				+ "			\"form.select.field\": \"Seleccionar campo\",\n"
				+ "			\"form.orderby\": \"Ordenar por\",\n"
				+ "			\"form.order.type\": \"Tipo de pedido\",\n" + "			\"form.where\": \"Where\",\n"
				+ "			\"form.max.value\": \"Valor máximo\",\n"
				+ "			\"form.offset\": \"Desplazamiento\",\n" + "			\"form.reset\": \"Restablecer\",\n"
				+ "			\"form.search\": \"Buscar\",\n" + "			\"form.records\": \"Registros\",\n"
				+ "			\"form.columns\": \"Columnas\",\n" + "			\"column.options\": \"Opciones\",\n"
				+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
				+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
				+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
				+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
				+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
				+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
				+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
				+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
				+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
				+ "			\"form.edit.record\": \"Editar registro\",\n"
				+ "			\"form.detail.record\": \"Registro detallado\",\n"
				+ "			\"button.cancel\": \"Cancelar\",\n" + "			\"button.delete\": \"Eliminar\",\n"
				+ "			\"button.save\": \"Guardar\",\n" + "			\"button.close\": \"Cerrar\",\n"
				+ "			\"button.new\": \"Nuevo\",\n" + "			\"button.apply\": \"Aplicar\",\n"
				+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
				+ "		    \"form.title.import\": \"Importar datos\",\n"
				+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
				+ "			\"form.download.csv\":\"Descargar CSV\",\n"
				+ "    		\"form.download.json\":\"Descargar JSON\",\n"
				+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
				+ "		    \"button.click\": \"haga click aquí\",\n"
				+ "		    \"button.click.upload\": \"para subirlo\",\n"
				+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n" + "		    \"button.import\": \"Importar\",\n"
				+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
				+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
				+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
				+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
				+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
				+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
				+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
				+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
				+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
				+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
				+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
				+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
				+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
				+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
				+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
				+ "			\"button.all.records\": \"Todos los registros\",\n"
				+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
				+ "			\"error.message.download\": \"Error descargando datos\",\n"
				+ "			\"error.message.empty\": \"Error no existen registros\",\n"
				+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
				+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
				+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
				+ "			\"form.show.wizard\": \"Show search wizard\",\n"
				+ "			\"form.select\": \"Select\",\n" + "			\"form.select.fields\": \"Select Fields\",\n"
				+ "			\"form.operator\": \"Operator\",\n" + "			\"form.condition\": \"Condition\",\n"
				+ "			\"form.select.operator\": \"Select Operator\",\n"
				+ "			\"form.write.here\": \"Write here\",\n"
				+ "			\"form.select.field\": \"Select Field\",\n" + "			\"form.orderby\": \"Order by\",\n"
				+ "			\"form.order.type\": \"Order Type\",\n" + "			\"form.where\": \"Where\",\n"
				+ "			\"form.max.value\": \"Max Value\",\n" + "			\"form.offset\": \"Offset\",\n"
				+ "			\"form.reset\": \"Reset\",\n" + "			\"form.search\": \"Search\",\n"
				+ "			\"form.records\": \"Records\",\n" + "			\"form.columns\": \"Columns\",\n"
				+ "			\"column.options\": \"Options\",\n"
				+ "			\"form.new.record.title\": \"New record\",\n"
				+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
				+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
				+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
				+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
				+ "			\"message.created.successfully\": \"Record created successfully\",\n"
				+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
				+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
				+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
				+ "			\"form.edit.record\": \"Edit record \",\n"
				+ "			\"form.detail.record\": \"Detail record \",\n"
				+ "			\"button.cancel\": \"Cancel\",\n" + "			\"button.delete\": \"Delete\",\n"
				+ "			\"button.save\": \"Save\",\n" + "			\"button.close\": \"Close\",\n"
				+ "			\"button.new\": \"New\",\n" + "			\"button.apply\": \"Apply\",\n"
				+ "		    \"form.select.entity\": \"Select Entity\",\n"
				+ "		    \"form.title.import\": \"Import records\",\n"
				+ "		    \"form.download.template\": \"Download Template\",\n"
				+ "			\"form.download.csv\":\"Download CSV\",\n"
				+ "    		\"form.download.json\":\"Download JSON\",\n"
				+ "		    \"button.drop\": \"Drop file or\",\n" + "		    \"button.click\": \"click here\",\n"
				+ "		    \"button.click.upload\": \"to upload\",\n"
				+ "		    \"form.info.max\": \"Max. 2mb csv\",\n" + "		    \"button.import\": \"Import\",\n"
				+ "		    \"button.showmore\": \"Show More Details\",\n"
				+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
				+ "		    \"message.success.loaded.1\": \"The\",\n"
				+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
				+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
				+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
				+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
				+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
				+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
				+ "		    \"error.message.processing\": \"Error processing data\",\n"
				+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
				+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
				+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
				+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
				+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
				+ "			\"button.all.records\": \"All the records\",\n"
				+ "			\"button.only.selection.records\": \"Only the selection\",\n"
				+ "			\"error.message.download\": \"Error downloading data\",\n"
				+ "			\"error.message.empty\": \"Error there are no records\",\n"
				+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
				+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
				+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n" + "	var localLocale ='EN';\n"
				+ "	try{\n" + "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
				+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
				+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
				+ " // link messages with internacionalization json on controlpanel\n"
				+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");

		gadgetTemplate.setDescription("ODS CRUD gadget template");
		gadgetTemplate.setTemplate("<!-- Write your HTML <div></div> and CSS <style></style> here -->\n"
				+ "<!--Focus here and F11 to full screen editor-->\n" + "<style>\n" + "    div.ods-dialog__body h3 {\n"
				+ "        font-size: 14px !important;\n" + "        display: none !important;\n" + "    }\n" + "\n"
				+ "    .control-label {\n" + "        margin-top: 1px!important;\n"
				+ "        color: #505D66 !important;\n" + "        font-weight: normal !important;\n"
				+ "        width: fit-content !important;\n" + "        font-size: small !important;\n" + "    }\n"
				+ "\n" + "    .control-label .required,\n" + "    .form-group .required {\n"
				+ "        color: #A73535 !important;\n" + "        font-size: 12px !important;\n"
				+ "        padding-left: 2px !important;\n" + "    }\n" + "\n" + "    .ods-select {\n"
				+ "        display: block !important;\n" + "\n" + "\n" + "    }\n" + "\n" + "    .wizard-style {\n"
				+ "        top: 20px !important;\n" + "        margin-left: 15px !important;\n" + "    }\n" + "\n"
				+ "\n" + "    .ods-input__inner {\n" + "        background: #F7F8F8 !important;\n" + "    }\n" + "\n"
				+ "\n" + "    .records-title {\n" + "        margin-top: 6px !important;\n"
				+ "        font-size: 17px !important;\n" + "        line-height: 24px !important;\n"
				+ "        color: #051724 !important;\n" + "    }\n" + "\n" + "    .ods-dialog-title {\n"
				+ "        font-size: 17px !important;\n" + "        line-height: 24px !important;\n"
				+ "        color: #051724 !important;\n" + "    }\n" + "    .ods-dialog__header {\n"
				+ "        padding: 37px 20px 10px !important;\n" + "    }\n" + "    .search-menu-title {\n" + "\n"
				+ "        margin-left: 5px !important;\n" + "\n" + "    }\n" + "\n"
				+ "    .search-menu-title-magnifying-glass {\n" + "\n" + "        margin-left: 10px!important;\n"
				+ "        margin-bottom: -4px!important;\n" + "    }\n" + "\n" + "    .ods-row-modal-grey {\n"
				+ "        margin-bottom: -30px!important;\n" + "        margin-left: -20!important;\n"
				+ "        margin-right: -20!important;\n" + "        padding-bottom: 24px!important;\n"
				+ "        margin-top: 24px!important;\n" + "    }\n" + "\n" + "    /*.el-table .cell {\n"
				+ "        font-size: 12px !important;\n" + "    }\n" + "\n" + "    .el-table .el-table__cell {\n"
				+ "        padding: 5px 0 !important;\n" + "    }*/\n" + "\n" + "\n" + "    .download-icons-grey {\n"
				+ "          filter: invert(0%) sepia(0%) saturate(0%) hue-rotate(162deg) brightness(93%) contrast(88%);\n"
				+ "    }\n" + "    .el-form-item__contentel-form-item__content {\n" + "        display: none;\n"
				+ "    }\n" + "\n" + "\n" + "\n" + "    .row {\n" + "        display: -ms-flexbox !important;\n"
				+ "        display: flex !important;\n" + "        -ms-flex-wrap: wrap !important;\n"
				+ "        flex-wrap: wrap !important;\n" + "        width:100% !important;\n"
				+ "        margin-right: -15px !important;\n" + "        margin-left: -15px !important;\n" + "    }\n"
				+ "    .col-md-12 {\n" + "        -ms-flex: 0 0 100% !important;\n"
				+ "        flex: 0 0 100% !important;\n" + "        max-width: 100% !important;\n" + "    }\n"
				+ "    .col, .col-1, .col-10, .col-11, .col-12, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-auto, .col-lg, .col-lg-1, .col-lg-10, .col-lg-11, .col-lg-12, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-auto, .col-md, .col-md-1, .col-md-10, .col-md-11, .col-md-12, .col-md-2, .col-md-3, .col-md-4, .col-md-5, .col-md-6, .col-md-7, .col-md-8, .col-md-9, .col-md-auto, .col-sm, .col-sm-1, .col-sm-10, .col-sm-11, .col-sm-12, .col-sm-2, .col-sm-3, .col-sm-4, .col-sm-5, .col-sm-6, .col-sm-7, .col-sm-8, .col-sm-9, .col-sm-auto, .col-xl, .col-xl-1, .col-xl-10, .col-xl-11, .col-xl-12, .col-xl-2, .col-xl-3, .col-xl-4, .col-xl-5, .col-xl-6, .col-xl-7, .col-xl-8, .col-xl-9, .col-xl-auto {\n"
				+ "        position: relative !important;\n" + "        width: 100% !important;\n"
				+ "        padding-right: 15px !important;\n" + "        padding-left: 15px !important;\n" + "    }\n"
				+ "    /*label {\n" + "        display: inline-block !important;\n"
				+ "        margin-bottom: 0.5rem !important;\n" + "    }*/\n" + "    .form-group {\n"
				+ "        margin-bottom: 1rem !important;\n" + "    }\n" + "\n" + "    .form-control {\n"
				+ "        display: block !important;\n" + "        width: 100%!important;\n"
				+ "        height: calc(1.5em + 0.75rem + 2px)!important;\n"
				+ "        padding: 0.375rem 0.75rem!important;\n" + "        font-size: 1rem!important;\n"
				+ "        font-weight: 400!important;\n" + "        line-height: 1.5!important;\n"
				+ "        color: #495057!important;\n" + "        background-color: #fff!important;\n"
				+ "        background-clip: padding-box!important;\n" + "        border: 1px solid #ced4da!important;\n"
				+ "        border-radius: 0.25rem!important;\n"
				+ "        transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out!important;\n"
				+ "    }\n" + "     /*optgroup, select, textarea {\n" + "        margin: 0 !important;\n"
				+ "        font-family: inherit !important;\n" + "        font-size: inherit !important;\n"
				+ "        line-height: inherit !important;\n" + "    }\n" + "    button, input {\n"
				+ "        margin: 0 !important;\n" + "        font-family: inherit !important;\n" + "\n"
				+ "        line-height: inherit !important;\n" + "    }*/\n" + "    .form-control:focus {\n"
				+ "        color: #495057 !important;\n" + "        background-color: #fff !important;\n"
				+ "        border-color: #80bdff !important;\n" + "        outline: 0 !important;\n"
				+ "        box-shadow: 0 0 0 0.2rem rgb(0 123 255 / 25%) !important;\n" + "    }\n" + ".float-right{\n"
				+ "    float: right;\n" + "    margin-left: 10px!important;\n" + "    margin-right: 10px!important;\n"
				+ "}\n" + "\n" + "</style>\n"
				+ "<div class=\"appgadget\" style=\"padding-left:10px;padding-right:10px;\">\n"
				+ "    <!-- entity selector -->\n" + "    <ods-row >\n" + "        <ods-col :span=\"8\">\n"
				+ "            <label class=\"control-label\">{{ $t(\"form.entity\") }}<span class=\"required\" aria-required=\"true\">\n"
				+ "                    *</span></label></br>\n"
				+ "            <ods-select  :disabled=\"showSelectOntology\" size=\"small\"  v-model=\"selectedOntology\"\n"
				+ "                @change=\"onChangeEntity($event)\" filterable :placeholder=\"$t('form.select')\">\n"
				+ "                <ods-option v-for=\"onto in ontologies\" :key=\"onto.identification\" :label=\"onto.identification\"\n"
				+ "                    :value=\"onto.identification\">\n" + "                </ods-option>\n"
				+ "            </ods-select >\n" + "        </ods-col>\n" + "        <!-- wizard switch -->\n"
				+ "        <ods-col v-if=\"typeGadget=='withWizard'||typeGadget=='searchOnly'\" :span=\"8\">\n"
				+ "            <ods-switch class=\"wizard-style\" v-model=\"showWizard\" @change=\"calculeTableheight\" :disabled=\"disabledWizard\"\n"
				+ "                :active-text=\"$t('form.show.wizard')\"></ods-switch>\n" + "        </ods-col>\n"
				+ "    </ods-row>\n" + "        <ods-divider  direction=\"horizontal\"></ods-divider>\n"
				+ "    <ods-row>\n" + "        </ods-row>\n" + "    <!-- wizard  -->\n"
				+ "    <div class=\"crudWizard\" v-if=\"showWizard\">\n" + "        <ods-row  type=\"flex\"\n"
				+ "            :gutter=\"10\">\n" + "\n"
				+ "            <ods-col :xs=\"5\" :sm=\"5\" :md=\"5\" :lg=\"5\" :xl=\"5\">\n"
				+ "                <label class=\"control-label\">{{ $t(\"form.where\") }}</label></br>\n"
				+ "                <ods-select size=\"small\" v-model=\"selectWizard\" multiple collapse-tags :placeholder=\"$t('form.select')\">\n"
				+ "                    <ods-option v-for=\"item in selectWizardOptions\" :key=\"item.value\" :label=\"item.label\"\n"
				+ "                        :value=\"item.value\">\n" + "                    </ods-option>\n"
				+ "                </ods-select>\n" + "            </ods-col>\n"
				+ "            <ods-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\" style=\"margin-top: 12px;\">\n"
				+ "                <ods-button  icon=\"ods-icon-plus\" type=\"primary\"  @click=\"dialogAddSelectVisibleFunction\"></ods-button>\n"
				+ "            </ods-col>\n"
				+ "            <ods-col :xs=\"5\" :sm=\"5\" :md=\"5\" :lg=\"5\" :xl=\"5\">\n"
				+ "                <label v-if=\"typeGadget!='searchOnly'\" class=\"control-label\">{{ $t(\"form.orderby\") }}</label></br>\n"
				+ "                <ods-select v-if=\"typeGadget!='searchOnly'\" size=\"small\" v-model=\"orderByWizard\" multiple collapse-tags\n"
				+ "                    :placeholder=\"$t('form.select')\">\n"
				+ "                    <ods-option v-for=\"itemo in orderByWizardOptions\" :key=\"itemo.value\" :label=\"itemo.label\"\n"
				+ "                        :value=\"itemo.value\">\n" + "                    </ods-option>\n"
				+ "                </ods-select>\n" + "            </ods-col>\n"
				+ "            <ods-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\" style=\"margin-top: 12px;\">\n"
				+ "                <ods-button v-if=\"typeGadget!='searchOnly'\" icon=\"ods-icon-plus\" type=\"primary\"\n"
				+ "                    @click=\"dialogAddOrderByVisibleFunction\"></ods-button>\n"
				+ "            </ods-col>\n"
				+ "            <ods-col :xs=\"3\" :sm=\"3\" :md=\"3\" :lg=\"3\" :xl=\"3\" style=\"min-width:100px\">\n"
				+ "                <label v-if=\"typeGadget!='searchOnly'\" class=\"control-label\">{{ $t(\"form.max.value\") }}</label> </br>\n"
				+ "                <ods-input v-if=\"typeGadget!='searchOnly'\" type=\"number\" size=\"small\" v-model=\"limitWizard\"\n"
				+ "                    controls-position=\"right\" :min=\"0\">\n" + "                </ods-input>\n"
				+ "            </ods-col>\n"
				+ "            <ods-col :xs=\"3\" :sm=\"3\" :md=\"3\" :lg=\"3\" :xl=\"3\" style=\"min-width:100px\">\n"
				+ "                <label v-if=\"typeGadget!='searchOnly'\" class=\"control-label\"> {{ $t(\"form.offset\") }} </label></br>\n"
				+ "                <ods-input v-if=\"typeGadget!='searchOnly'\" type=\"number\" size=\"small\" v-model=\"offsetWizard\"\n"
				+ "                    controls-position=\"right\" :min=\"0\">\n" + "                </ods-input>\n"
				+ "            </ods-col>\n" + "\n"
				+ "            <ods-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\" style=\"min-width:100px; margin-top: 12px;\">\n"
				+ "\n"
				+ "                <ods-button   type=\"neutral\" class=\"float-right\" @click=\"resetWizard()\">{{ $t(\"form.reset\") }}\n"
				+ "                </ods-button>\n" + "            </ods-col>\n"
				+ "            <ods-col :xs=\"1\" :sm=\"1\" :md=\"1\" :lg=\"1\" :xl=\"1\" style=\"min-width:100px;margin-top: 12px;\">\n"
				+ "                <ods-button  type=\"primary\" class=\" float-right\" @click=\"searchWizard()\">\n"
				+ "                    {{ $t(\"form.search\") }}</ods-button>\n" + "\n" + "            </ods-col>\n"
				+ "             <ods-divider></ods-divider>\n" + "        </ods-row>\n" + "    </div>\n"
				+ "    <!-- div table -->\n" + "    <div v-if=\"showTable\">\n"
				+ "        <ods-row justify=\"center\" type=\"flex\" :gutter=\"10\">\n"
				+ "            <ods-col :xs=\"3\" :sm=\"3\" :md=\"3\" :lg=\"2\" :xl=\"2\" >\n"
				+ "                <label class=\"control-label records-title\">{{ $t(\"form.records\") }}</label>\n"
				+ "            </ods-col>\n"
				+ "            <ods-col :xs=\"5\" :sm=\"5\" :md=\"5\" :lg=\"6\" :xl=\"6\">\n" + "\n"
				+ "                <ods-input type=\"string\" class=\"search-menu-title\" size=\"small\"\n"
				+ "                    v-model=\"searchString\">\n"
				+ "                     <ods-icon   class=\"input-slot-icon\" slot=\"prepend\"  name=\"search\" style=\"margin-top: 15px;\"  size=\"16\" />\n"
				+ "                </ods-input>\n" + "\n" + "\n" + "            </ods-col>\n"
				+ "            <ods-col :offset=\"4\" :xs=\"12\" :sm=\"12\" :md=\"12\" :lg=\"12\" :xl=\"12\" style=\"text-align: right;\">\n"
				+ "                <ods-dropdown  style=\"margin-right: 10px;padding: 8px;\" @command=\"downloadData\">\n"
				+ "\n" + "                <ods-dropdown-menu slot=\"dropdown\">\n"
				+ "                    <ods-dropdown-item v-if=\"executeSearch\" command=\"csv\" ><img  class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.csv\") }}</ods-dropdown-item>\n"
				+ "                    <ods-dropdown-item v-if=\"executeSearch\" command=\"json\"  ><img  class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.json\") }}</ods-dropdown-item>\n"
				+ "                    <ods-dropdown-item v-if=\"!executeSearch\" command=\"allcsv\" ><img  class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.csv\") }}</ods-dropdown-item>\n"
				+ "                    <ods-dropdown-item v-if=\"!executeSearch\" command=\"alljson\"  ><img  class=\"download-icons-grey\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_download.svg'\">&nbsp;{{ $t(\"form.download.json\") }}</ods-dropdown-item>\n"
				+ "                </ods-dropdown-menu>\n" + "                </ods-dropdown>\n" + "\n"
				+ "                <ods-button   class=\"primary\" @click=\"dialogOptionsColumnsVisible = true\" icon=\"ods-icon-list\"></ods-button>\n"
				+ "                <ods-button  class=\"primary\" icon=\"ods-icon-plus\" @click=\"dialogCreateVisible= true\"></ods-button>\n"
				+ "            </ods-col>\n" + "        </ods-row>\n" + "\n"
				+ "       <!--ods table-------------------->\n" + "    <div>\n" + "      <ods-table\n"
				+ "        :data=\"paginatedData\"\n" + "        style=\"width: 100%;\"\n"
				+ "        :height=\"tableHeight\"\n" + "        :stripe=\"false\"\n" + "        :fit=\"true\"\n"
				+ "        :showHeader=\"true\"\n" + "        @sort-change=\"sortChange\">\n"
				+ "        <ods-table-column\n" + "          v-for=\"(column, i) in columns\"\n"
				+ "          :key=\"`column-${column.prop}`\"\n"
				+ "          :fixed=\"i === 0 && fixedFirstCol ? 'left' : i === columns.length - 1 && fixedLastCol ? 'right' : false\"\n"
				+ "          :prop=\"column.prop\"\n" + "          :sortable=\"sortable\">\n"
				+ "          <template slot=\"header\" slot-scope=\"scope\">\n"
				+ "            {{ columns[scope.$index] !== '' ? columns[scope.$index].label : scope.row }}\n"
				+ "          </template>\n" + "           <template slot-scope=\"scope\">\n" + "\n"
				+ "        <span style=\"margin-left: 10px\">{{ scope.row[column.prop] }}</span>\n"
				+ "      </template>\n" + "        </ods-table-column>\n" + "        <ods-table-column\n"
				+ "            :label=\"$t('column.options')\"\n" + "            width=\"120\">\n"
				+ "            <template slot-scope=\"scope\">\n"
				+ "                 <ods-button size=\"small\" type=\"neutral\" icon=\"ods-icon-eye\" @click=\"handleShow(scope.$index, scope.row)\" style=\"margin-left:0px\">\n"
				+ "                </ods-button>\n"
				+ "                <ods-button size=\"small\" type=\"neutral\" icon=\"ods-icon-edit\" @click=\"handleEdit(scope.$index, scope.row)\" style=\"margin-left:0px\">\n"
				+ "                </ods-button>\n"
				+ "                <ods-button size=\"small\"  type=\"neutral\" @click=\"handleDelete(scope.$index, scope.row)\" style=\"margin-left:0px\"> <ods-icon    name=\"delete\"  color=\"#a73535\" />\n"
				+ "                </ods-button>\n" + "            </template>\n" + "            </ods-table-column>\n"
				+ "      </ods-table>\n" + "    </div>\n" + "    <div>\n" + "      <ods-pagination\n"
				+ "        :current-page=\"page\"\n" + "        :page-size=\"pageSize\"\n"
				+ "        :page-sizes=\"pageSizes\"\n" + "        :page-count=\"pageCount\"\n"
				+ "        :total=\"totalItems\"\n" + "        @current-change=\"handlePageChange\"\n"
				+ "        @size-change=\"handlePageSizeChange\">\n" + "      </ods-pagination>\n" + "    </div>\n"
				+ "    <!-- ods table-------------------->\n" + "\n" + "    </div>\n" + "    <!-- DELETE dialog -->\n"
				+ "    <ods-dialog modal=\"false\" append-to-body=\"true\" :visible.sync=\"dialogDeleteVisible\" width=\"25%\">\n"
				+ "        <label class=\"ods-dialog-title\">{{ $t(\"message.modal.delete.title\") }}</label></br>\n"
				+ "        <label\n"
				+ "            style=\"font-size: 12px;line-height: 16px; color: #505D66;\">{{ $t(\"message.modal.delete.subtitle\") }}</label>\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                <ods-button type=\"primary\" class=\"float-right\" @click=\"aceptedDelete\">\n"
				+ "                    {{ $t(\"button.delete\") }}</ods-button>\n"
				+ "                <ods-button type=\"neutral\" class=\" float-right\" @click=\"dialogDeleteVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</ods-button>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "    </ods-dialog>\n" + "    <!-- EDIT dialog -->\n"
				+ "    <ods-dialog modal=\"true\" append-to-body=\"true\" :title=\"editTitle\" :visible.sync=\"dialogEditVisible\"\n"
				+ "        @opened=\"openEdit\" width=\"25%\">\n"
				+ "        <div :class=\"[idelem, 'editor_edit_holder']\"  ></div>\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                <ods-button type=\"primary\" class=\"float-right\" @click=\"aceptedEdit\">{{ $t(\"button.save\") }}\n"
				+ "                </ods-button>\n"
				+ "                <ods-button type=\"neutral\" class=\" float-right\" @click=\"dialogEditVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</ods-button>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "    </ods-dialog>\n" + "    <!-- SHOW/HIDE COLUMNS dialog -->\n"
				+ "    <ods-dialog modal=\"false\" append-to-body=\"false\" :title=\"$t('form.columns')\"\n"
				+ "        :visible.sync=\"dialogOptionsColumnsVisible\" width=\"25%\">\n" + "\n"
				+ "        <ods-row v-for=\"visibleColumn in visibleColumns\" :key=\"visibleColumn.prop\" v-if=\"visibleColumn.label!='id'\">\n"
				+ "            <ods-col :span=\"24\">\n" + "                </br>\n"
				+ "                <el-switch v-model=\"visibleColumn.visible\" :active-text=\"visibleColumn.prop\"\n"
				+ "                    @change=\"dialogOptionsColumnsVisible = false;dialogOptionsColumnsVisible = true;\"></el-switch>\n"
				+ "            </ods-col>\n" + "        </ods-row>\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                <ods-button type=\"primary\" class=\"float-right\" @click=\"aceptedChangeColumns\">\n"
				+ "                    {{ $t(\"button.apply\") }}</ods-button>\n"
				+ "                <ods-button type=\"neutral\" class=\" float-right\"\n"
				+ "                    @click=\"dialogOptionsColumnsVisible = false\">{{ $t(\"button.cancel\") }}</ods-button>\n"
				+ "            </ods-col>\n" + "        </ods-row>\n" + "    </ods-dialog>\n"
				+ "     <!-- DOWNLOAD dialog -->\n"
				+ "    <ods-dialog modal=\"false\" append-to-body=\"false\" :title=\"$t('message.download.all')\"\n"
				+ "        :visible.sync=\"dialogDownloadVisible\" width=\"25%\">\n" + "\n" + "\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                  <ods-button type=\"primary\" class=\"float-right\" @click=\"aceptedDownloadOnlySelec\">\n"
				+ "                    {{ $t(\"button.only.selection.records\") }}</ods-button>\n"
				+ "                <ods-button type=\"primary\" class=\"float-right\" @click=\"aceptedDownloadAll\">\n"
				+ "                    {{ $t(\"button.all.records\") }}</ods-button>\n"
				+ "                <ods-button type=\"neutral\" class=\"float-right\"\n"
				+ "                    @click=\"dialogDownloadVisible = false\">{{ $t(\"button.cancel\") }}</ods-button>\n"
				+ "            </ods-col>\n" + "        </ods-row>\n" + "    </ods-dialog>\n"
				+ "    <!-- DETAIL dialog -->\n"
				+ "    <ods-dialog modal=\"true\" append-to-body=\"true\" :title=\"showTitle\" :visible.sync=\"dialogShowVisible\"\n"
				+ "        @opened=\"openShow\" width=\"25%\">\n"
				+ "        <div :class=\"[idelem, 'editor_show_holder']\" ></div>\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                <ods-button type=\"neutral\"  class=\"float-right\" @click=\"dialogShowVisible = false\">\n"
				+ "                    {{ $t(\"button.close\") }}</ods-button>\n" + "\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "    </ods-dialog>\n" + "    <!-- CREATE dialog -->\n"
				+ "    <ods-dialog modal=\"true\" append-to-body=\"true\" :title=\"$t('form.new.record.title')\"\n"
				+ "        :visible.sync=\"dialogCreateVisible\" @opened=\"openCreate\" width=\"25%\">\n"
				+ "        <div :class=\"[idelem, 'editor_new_holder']\" ></div>\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                <ods-button type=\"primary\"  class=\"float-right\" @click=\"aceptedCreate\">\n"
				+ "                    {{ $t(\"button.new\") }} <img style=\"margin-top: 6px;\" v-bind:src=\"platformhost + '/static/images/dashboards/icon_button_plus.svg'\">\n"
				+ "                </ods-button>\n"
				+ "                <ods-button type=\"neutral\"  class=\"float-right\" @click=\"dialogCreateVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</ods-button>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "    </ods-dialog>\n" + "\n" + "    <!-- WHERE dialog -->\n"
				+ "    <ods-dialog modal=\"true\" append-to-body=\"true\" :title=\"$t('form.where')\" :visible.sync=\"dialogAddSelectVisible\"\n"
				+ "        @opened=\"opendialogAddSelect\" width=\"25%\">\n" + "        <ods-row type=\"flex\">\n"
				+ "            <ods-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.select.fields\")}} <span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <ods-select size=\"small\" v-model=\"selectedParametereWhere\" :placeholder=\"$t('form.select.field')\">\n"
				+ "                    <ods-option v-for=\"col in columnsParams\" :key=\"col.prop\" :label=\"col.label\" :value=\"col.prop\">\n"
				+ "                    </ods-option>\n" + "                </ods-select>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "        <ods-row type=\"flex\">\n" + "            <ods-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.operator\")}}<span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <ods-select size=\"small\" v-model=\"selectedOperatorWhere\" :placeholder=\"$t('form.select.operator')\">\n"
				+ "                    <ods-option v-for=\"ope in operators\" :key=\"ope\" :label=\"ope\" :value=\"ope\">\n"
				+ "                    </ods-option>\n" + "                </ods-select>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "        <ods-row type=\"flex\">\n" + "            <ods-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.condition\")}} <span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <ods-input size=\"small\" :placeholder=\"$t('form.write.here')\" v-model=\"inputValueWhere\"></ods-input>\n"
				+ "            </ods-col>\n" + "        </ods-row>\n"
				+ "        <ods-row class=\"ods-row-modal-grey\">\n" + "            <ods-col>\n"
				+ "                </br>\n"
				+ "                <ods-button  type=\"primary\" class=\" float-right\" @click=\"aceptedAddWhereParameter\">\n"
				+ "                    {{ $t(\"button.apply\") }}</ods-button>\n"
				+ "                <ods-button  type=\"neutral\" class=\" float-right\" @click=\"dialogAddSelectVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</ods-button>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "    </ods-dialog>\n" + "\n" + "\n" + "    <!-- ORDER BY dialog -->\n"
				+ "    <ods-dialog modal=\"true\" append-to-body=\"true\" title=\"Order by\" :visible.sync=\"dialogAddOrderByVisible\"\n"
				+ "        @opened=\"opendialogAddOrderBy\" width=\"25%\">\n" + "        <ods-row type=\"flex\">\n"
				+ "            <ods-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.select.fields\")}}<span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <ods-select  v-model=\"selectedParametereOrderBy\" :placeholder=\"$t('form.select.field')\">\n"
				+ "                    <ods-option v-for=\"col in columnsParams\" :key=\"col.prop\" :label=\"col.label\" :value=\"col.prop\">\n"
				+ "                    </ods-option>\n" + "                </ods-select>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "        <ods-row type=\"flex\">\n" + "            <ods-col :span=\"24\">\n"
				+ "                <label class=\"control-label\">{{$t(\"form.order.type\")}}<span class=\"required\" aria-required=\"true\">\n"
				+ "                        *</span></label></br>\n"
				+ "                <ods-select  v-model=\"selectedOperatorOrderBy\" :placeholder=\"$t('form.select.operator')\">\n"
				+ "                    <ods-option v-for=\"ope in orders\" :key=\"ope\" :label=\"ope\" :value=\"ope\">\n"
				+ "                    </ods-option>\n" + "                </ods-select>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "        <ods-row class=\"ods-row-modal-grey\">\n"
				+ "            <ods-col>\n" + "                </br>\n"
				+ "                <ods-button  type=\"primary\"  class=\"ods-self-end\" @click=\"aceptedAddOrderByParameter\">\n"
				+ "                    {{ $t(\"button.apply\") }}</ods-button>\n"
				+ "                <ods-button  type=\"neutral\"  class=\"ods-self-end\" @click=\"dialogAddOrderByVisible = false\">\n"
				+ "                    {{ $t(\"button.cancel\") }}</ods-button>\n" + "            </ods-col>\n"
				+ "        </ods-row>\n" + "    </ods-dialog>\n" + "\n" + "\n" + "</div>\n" + "");
		gadgetTemplate.setTemplateJS("vm.vueconfig = {\n"
				+ "   el: document.getElementById(vm.id).querySelector('vuetemplate .appgadget'),\n" + "   data: {\n"
				+ "      typeGadget: 'withWizard', //['withWizard','noWizard','searchOnly']\n"
				+ "      hideIdColumn: false, // show or hide id column\n"
				+ "      initialEntity: \"\" , //variable that initializes the entity with the value assigned to it\n"
				+ "\n" + "      showTable: false,\n" + "      showSelectOntology: true,\n"
				+ "      showWizard: false,\n" + "      disabledWizard: true,\n" + "      idPath: \"\",\n"
				+ "      ontologies: [],\n" + "      ontologyFieldsAndDesc: {},\n" + "      recordSelected: \"\",\n"
				+ "      selectedOntology: \"\",\n" + "      selectedOntologySchema: {},\n" + "\n"
				+ "      dialogDeleteVisible: false, //hide show dialogs\n" + "      dialogEditVisible: false,\n"
				+ "      dialogCreateVisible: false,\n" + "      dialogShowVisible: false,\n"
				+ "      dialogOptionsColumnsVisible: false,\n" + "      dialogAddSelectVisible: false,\n"
				+ "      dialogDownloadVisible:false,\n" + "      idelem:vm.id,\n" + "      executeSearch:false,\n"
				+ "      showMagnifyingGlass: true,\n" + "      jEditor: {},\n" + "      jShowEditor: {},\n"
				+ "      tableHeight: 100,\n" + "      resizeObserver: {},\n" + "      selectWizard: [],\n"
				+ "      selectWizardOptions: [],\n" + "      orderByWizard: [],\n"
				+ "      orderByWizardOptions: [],\n" + "      dialogAddOrderByVisible: false,\n"
				+ "      limitWizard: 100, // limit of records in the search for initialize at another value change on resetwizard too\n"
				+ "      offsetWizard: 0, //offset records in the search\n" + "      whereCondition: '',\n"
				+ "      uniqueID: '', // save path of id\n" + "      selectedParametereWhere: '',\n"
				+ "      selectedOperatorWhere: '',\n" + "      selectedParametereOrderBy: '',\n"
				+ "      selectedOperatorOrderBy: '',\n" + "      inputValueWhere: '',\n" + "      editTitle: '',\n"
				+ "      showTitle: '',\n" + "      downloadType:'',\n"
				+ "      visibleColumns: [], // list of visible columns\n" + "      columnsParams: [],\n"
				+ "      searchString: '', // text for local search\n" + "      formOptions: {\n"
				+ "         forms: []\n" + "      },\n" + "      orders: ['ASC', 'DESC'],\n"
				+ "      operators: ['=', '>', '<', '>=', '<=', '!='],\n" + "      ds: [],\n" + "      columns: [],\n"
				+ "      tableData: [],\n" + "      platformhost: __env.endpointControlPanel,\n" + "\n" + "\n"
				+ "      pageSizes: [\n" + "      10,\n" + "      20,\n" + "      30,\n" + "      40,\n" + "      50,\n"
				+ "      100\n" + "      ],\n" + "      pageSize:10,\n" + "      minimumPageSize:10,\n"
				+ "      page: 1,\n" + "      fixedFirstCol:false,\n" + "    fixedLastCol:false,\n"
				+ "      sortable:true\n" + "   },\n" + "     computed: {\n" + "      totalItems () {\n"
				+ "        return this.filteredData.length\n" + "      },\n" + "      pageCount () {\n"
				+ "        return Math.floor(this.totalItems / this.pageSize)\n" + "      },\n"
				+ "      paginatedData () {\n"
				+ "        return this.filteredData.slice(this.pageSize * this.page - this.pageSize, this.pageSize * this.page)\n"
				+ "      },\n" + "      filteredData () {\n"
				+ "            console.log(this.tableData.filter(this.tableDatafilter))\n"
				+ "        return this.tableData.filter(this.tableDatafilter)\n" + "      },\n"
				+ "      pageSizes () {\n" + "        return [\n" + "          this.minimumPageSize,\n"
				+ "          this.minimumPageSize * 2,\n" + "          this.minimumPageSize * 3,\n"
				+ "          this.minimumPageSize * 4,\n" + "          this.minimumPageSize * 5,\n"
				+ "          this.minimumPageSize * 10\n" + "        ]\n" + "      },\n" + "    },\n"
				+ "   methods: {\n" + "       handlePageSizeChange (pageSize) {\n"
				+ "          this.pageSize = pageSize\n" + "          this.handlePageChange(this.page)\n"
				+ "        },\n" + "        handlePageChange (page) {\n" + "          this.page = page\n"
				+ "        },\n" + "      drawVueComponent: function (newData, oldData) {\n"
				+ "         //This will be call on new data\n" + "      },\n" + "      resizeEvent: function () {\n"
				+ "         //Resize event\n" + "\n" + "      },\n" + "      destroyVueComponent: function () {\n"
				+ "         vm.vueapp.$destroy();\n" + "      },\n" + "      receiveValue: function (data) {\n"
				+ "         //data received from datalink\n" + "      },\n"
				+ "      //function that initially reads the entities\n"
				+ "      loadEntities: function (search, loading) {\n" + "         var that = this;\n"
				+ "         vm.getEntities().then(function (data) {\n"
				+ "            that.ontologies = data.data.map(function (obj) {\n" + "               return {\n"
				+ "                  id: obj.id,\n" + "                  identification: obj.identification\n"
				+ "               }\n" + "            });\n" + "\n"
				+ "            if(that.initialEntity!=null && that.initialEntity!==\"\"){\n"
				+ "               if (that.ontologies.some(e => e.identification === that.initialEntity)) {\n"
				+ "                     that.selectedOntology = that.initialEntity;\n"
				+ "                     that.onChangeEntity(that.selectedOntology);\n"
				+ "                     that.showSelectOntology = true;\n" + "                     return;\n"
				+ "                  } else {\n" + "                     that.$notify({\n"
				+ "                        message: that.$t('error.message.ontology'),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "                  }\n"
				+ "            }\n" + "            var urlparam = urlParamService.generateFiltersForGadgetId(vm.id);\n"
				+ "            if (typeof urlparam !== 'undefined' && urlparam !== null && urlparam.length > 0) {\n"
				+ "               if (urlparam[0].exp != null) {\n"
				+ "                  var urlontology = urlparam[0].exp.replace(/\"/g, '');\n"
				+ "                  if (that.ontologies.some(e => e.identification === urlontology)) {\n"
				+ "                     that.selectedOntology = urlontology;\n"
				+ "                     that.onChangeEntity(that.selectedOntology);\n"
				+ "                     that.showSelectOntology = true;\n" + "                     return;\n"
				+ "                  } else {\n" + "                     that.$notify({\n"
				+ "                        message: that.$t('error.message.ontology'),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "                  }\n"
				+ "               }\n" + "            } else {\n" + "               that.showSelectOntology = false;\n"
				+ "            }\n" + "\n" + "         })\n" + "      },\n"
				+ "      //function that obtains the information of the selected ontology\n"
				+ "      loadHeadTable: function () {\n" + "         var that = this;\n"
				+ "         if (this.ontologies != null && this.ontologies.length > 0) {\n"
				+ "            for (var i = 0; i < this.ontologies.length; i++) {\n"
				+ "               if (this.ontologies[i].identification === this.selectedOntology) {\n"
				+ "                  vm.crudGetEntityInfo(this.ontologies[i].id).then(function (data) {\n"
				+ "                     that.uniqueID = data.data.uniqueId;\n"
				+ "                     that.selectedOntologySchema = that.changeDescriptionForTitle(data.data.jsonSchema);\n"
				+ "                  });\n" + "                  break;\n" + "               }\n" + "            }\n"
				+ "         }\n"
				+ "         vm.getOntologyFieldsAndDesc(this.selectedOntology).then(function (data) {\n"
				+ "            that.ontologyFieldsAndDesc = data.data;\n" + "            that.loadData();\n"
				+ "         })\n" + "      },\n"
				+ "      //function that gets the data and loads it to be displayed in the table\n"
				+ "      //difference if it is an initial query or the search button is pressed\n"
				+ "      loadData: function (fromSearch) {\n" + "         var that = this;\n"
				+ "         that.showTable = false;\n" + "         var selectStatement = {};\n"
				+ "         if (typeof fromSearch == 'undefined' || fromSearch == null || fromSearch == false) {\n"
				+ "            selectStatement = {\n" + "               ontology: this.selectedOntology,\n"
				+ "               columns: [],\n" + "               where: [],\n" + "               orderBy: [],\n"
				+ "               limit: this.limitWizard,\n" + "               offset: this.offsetWizard\n"
				+ "            };\n" + "         } else {\n" + "            selectStatement = {\n"
				+ "               ontology: this.selectedOntology,\n" + "               columns: [],\n"
				+ "               where: this.mapArrayToObjects(this.selectWizard),\n"
				+ "               orderBy: this.mapArrayToObjects(this.orderByWizard),\n"
				+ "               limit: this.limitWizard,\n" + "               offset: this.offsetWizard\n"
				+ "            };\n" + "         }\n"
				+ "         vm.crudQueryParams(selectStatement).then(function (data) {\n"
				+ "            that.showTable = true;\n" + "            that.disabledWizard = false;\n"
				+ "            //create columns from that.ontologyFieldsAndDesc\n"
				+ "            var keys = Object.keys(that.ontologyFieldsAndDesc);\n" + "\n"
				+ "            //validate error from data\n"
				+ "            if (typeof data.data.error !== 'undefined') {\n" + "               that.$notify({\n"
				+ "                  message: that.$t('error.message.querying.the.data'),\n"
				+ "                  type: 'error'\n" + "               });\n" + "               return {};\n"
				+ "            }\n" + "            if (keys != null && keys.length > 0) {\n"
				+ "               if (typeof fromSearch != 'undefined' && fromSearch != null && fromSearch) {\n"
				+ "                  var index = that.columns.findIndex(function (elem) {\n"
				+ "                     return elem.prop === that.uniqueID\n" + "                  });\n"
				+ "                  if (index < 0) {\n" + "                     that.idPath = that.uniqueID;\n"
				+ "                     that.columns.push({\n" + "                        prop: that.uniqueID,\n"
				+ "                        label: \"id\"\n" + "                     });\n" + "\n"
				+ "                  }\n" + "                  that.tableData = data.data.map(function (dat) {\n"
				+ "                     var refinedData = {};\n"
				+ "                     for (var i = 0; i < that.columns.length; i++) {\n"
				+ "                        let path = that.columns[i].prop.split('.');\n"
				+ "                           try {\n"
				+ "                                  refinedData[that.columns[i].prop] = path.reduce((a, v) => a[v], dat);              \n"
				+ "                              } catch (error) {\n"
				+ "                                   refinedData[that.columns[i].prop] = null; \n"
				+ "                              }" + "                     }\n" + "\n"
				+ "                     return refinedData;\n" + "                  })\n" + "               } else {\n"
				+ "                  that.columns = [];\n" + "\n"
				+ "                  var index = keys.findIndex(function (elem) {\n"
				+ "                     return elem === that.uniqueID\n" + "                  });\n"
				+ "                  if (index > -1) {\n" + "                     keys.splice(index, 1);\n"
				+ "                  }\n" + "                  that.idPath = that.uniqueID;\n"
				+ "                  that.columns.push({\n" + "                     prop: that.uniqueID,\n"
				+ "                     label: \"id\"\n" + "                  });\n"
				+ "                  keys = keys.sort(that.orderKeys);\n"
				+ "                  //initial construction of table columns\n"
				+ "                  for (var i = 0; i < keys.length; i++) {\n"
				+ "                     var description = that.ontologyFieldsAndDesc[keys[i]].description;\n"
				+ "                     if (description == null || typeof description == undefined || description.length == 0) {\n"
				+ "                        description = that.ontologyFieldsAndDesc[keys[i]].path;\n"
				+ "                     }\n"
				+ "                     description = that.$t(description) || description;\n"
				+ "                     that.columns.push({\n"
				+ "                        prop: that.ontologyFieldsAndDesc[keys[i]].path,\n"
				+ "                        thetype: that.ontologyFieldsAndDesc[keys[i]].type,\n"
				+ "                        label: description,\n" + "                        minWidth: 100,\n"
				+ "                        sortable: 'custom'\n" + "                     });\n" + "\n"
				+ "                  }\n" + "\n" + "                  //mapping the data to display\n"
				+ "                  that.tableData = data.data.map(function (dat) {\n"
				+ "                     var refinedData = {};\n"
				+ "                     for (var i = 0; i < that.columns.length; i++) {\n"
				+ "                        if(typeof that.columns[i].prop!='undefined' && that.columns[i].prop!=null){\n"
				+ "                           let path = that.columns[i].prop.split('.');\n"
				+ "                              try {\n"
				+ "                                  refinedData[that.columns[i].prop] = path.reduce((a, v) => a[v], dat);              \n"
				+ "                              } catch (error) {\n"
				+ "                                   refinedData[that.columns[i].prop] = null; \n"
				+ "                              }" + "                        }\n" + "                     }\n" + "\n"
				+ "                     return refinedData;\n" + "                  })\n"
				+ "                  console.log(that.tableData.filter(that.tableDatafilter))\n" + "               }\n"
				+ "               //hide or show id columns\n" + "               if (that.hideIdColumn) {\n" + "\n"
				+ "                  var index = that.columns.findIndex(function (elem) {\n"
				+ "                     return elem.label === 'id'\n" + "                  });\n"
				+ "                  if (index > -1) {\n" + "                     that.columns.splice(index, 1);\n"
				+ "                  }\n" + "\n" + "               }\n"
				+ "               if (that.visibleColumns.length == 0 && that.columns.length > 0) {\n"
				+ "                  that.visibleColumns = Array.from(that.columns);\n"
				+ "                  that.columnsParams = Array.from(that.columns);\n"
				+ "                  that.visibleColumns.forEach(function (element) {\n"
				+ "                     element.visible = true;\n" + "                  });\n" + "               }\n"
				+ "\n" + "\n" + "            }\n" + "         })\n" + "      },\n"
				+ "      //function that is executed when the edition of a record is opened\n"
				+ "      openEdit: function () {\n" + "         var that = this;\n"
				+ "         vm.crudFindById(this.recordSelected, this.selectedOntology).then(function (data) {\n"
				+ "            delete data.data[0]._id;\n" + "            delete data.data[0].contextData;\n"
				+ "            if (typeof that.jEditor.destroy == 'function') that.jEditor.destroy();\n"
				+ "            that.jEditor = new JSONEditor(document.getElementsByClassName(vm.id+' editor_edit_holder')[0], {\n"
				+ "               schema: JSON.parse(that.selectedOntologySchema),\n"
				+ "               startval: data.data[0],\n" + "               theme: 'bootstrap3',\n"
				+ "               iconlib: 'fontawesome4',\n" + "               disable_properties: true,\n"
				+ "               disable_edit_json: true,\n" + "               disable_collapse: true,\n"
				+ "               disable_array_reorder: true,\n"
				+ "               disable_array_delete_all_rows: true,\n"
				+ "               disable_array_delete_last_row: true,\n" + "               show_errors: 'change'\n"
				+ "            });\n" + "         })\n" + "      },\n"
				+ "      //function that is executed when the detail of a record is opened\n"
				+ "      openShow: function () {\n" + "         var that = this;\n"
				+ "         vm.crudFindById(this.recordSelected, this.selectedOntology).then(function (data) {\n"
				+ "            delete data.data[0]._id;\n" + "            delete data.data[0].contextData;\n"
				+ "            if (typeof that.jShowEditor.destroy == 'function') that.jShowEditor.destroy();\n"
				+ "            that.jShowEditor = new JSONEditor(document.getElementsByClassName(vm.id+' editor_show_holder')[0], {\n"
				+ "               schema: JSON.parse(that.selectedOntologySchema),\n"
				+ "               startval: data.data[0],\n" + "               theme: 'bootstrap3',\n"
				+ "               mode: 'view',\n" + "               iconlib: 'fontawesome4',\n"
				+ "               disable_properties: true,\n" + "               disable_edit_json: true,\n"
				+ "               disable_collapse: true,\n" + "               disable_array_reorder: true,\n"
				+ "               disable_array_delete_all_rows: true,\n"
				+ "               disable_array_delete_last_row: true,\n" + "               show_errors: 'change'\n"
				+ "            });\n" + "            that.jShowEditor.disable();\n" + "         })\n" + "      },\n"
				+ "      //function that is executed when modal of creating a record is opened\n"
				+ "      openCreate: function () {\n" + "         var that = this;\n"
				+ "         vm.crudFindById(this.recordSelected, this.selectedOntology).then(function (data) {\n" + "\n"
				+ "            if (typeof that.jEditor.destroy == 'function') that.jEditor.destroy();\n"
				+ "            that.jEditor = new JSONEditor(document.getElementsByClassName(vm.id+' editor_new_holder')[0], {\n"
				+ "               schema: JSON.parse(that.selectedOntologySchema),\n"
				+ "               startval: undefined,\n" + "               theme: 'bootstrap3',\n"
				+ "               iconlib: 'fontawesome4',\n" + "               disable_properties: true,\n"
				+ "               disable_edit_json: true,\n" + "               disable_collapse: true,\n"
				+ "               disable_array_reorder: true,\n"
				+ "               disable_array_delete_all_rows: true,\n"
				+ "               disable_array_delete_last_row: true,\n" + "               show_errors: 'change'\n"
				+ "            });\n" + "         })\n" + "\n" + "\n" + "      },\n"
				+ "      //function that is executed when accepting a visibility change in the columns\n"
				+ "      aceptedChangeColumns: function () {\n" + "            //delete columns visible = false\n"
				+ "            //add columns visible = true if not exist\n" + "\n" + "            var that = this;\n"
				+ "            this.visibleColumns.forEach(function (visibleCol) {\n"
				+ "               if (that.columns.length > 0) {\n" + "                  var find = false;\n"
				+ "                  for (var i = 0; i < that.columns.length; i++) {\n"
				+ "                     if (that.columns[i].prop == visibleCol.prop) {\n"
				+ "                        find = true;\n" + "                        if (!visibleCol.visible) {\n"
				+ "                           that.columns.splice(i, 1);\n" + "                        }\n"
				+ "                        break;\n" + "                     }\n" + "                  }\n"
				+ "                  if (!find && visibleCol.visible) {\n"
				+ "                     var obj = Object.assign({}, visibleCol);\n"
				+ "                     delete obj.visible;\n" + "                     that.columns.push(obj);\n"
				+ "                  }\n" + "               } else {\n"
				+ "                  if (visibleCol.visible) {\n"
				+ "                     var obj = Object.assign({}, visibleCol);\n"
				+ "                     delete obj.visible;\n" + "                     that.columns.push(obj);\n"
				+ "                  }\n" + "               }\n" + "            });\n"
				+ "            this.dialogOptionsColumnsVisible = false;\n" + "            this.loadData(true);\n"
				+ "         }\n" + "\n" + "         ,\n"
				+ "      //function that is executed when clicking on edit a record\n"
				+ "      handleEdit: function (index, row) {\n" + "         this.recordSelected = row[this.idPath];\n"
				+ "         this.editTitle = this.$t('form.edit.record') + this.recordSelected;\n"
				+ "         this.dialogEditVisible = true;\n" + "\n" + "      },\n"
				+ "      //function that is executed when clicking on show a record\n"
				+ "      handleShow: function (index, row) {\n" + "         this.recordSelected = row[this.idPath];\n"
				+ "         this.showTitle = this.$t('form.detail.record') + this.recordSelected;\n"
				+ "         this.dialogShowVisible = true;\n" + "\n" + "      },\n"
				+ "      //function that is executed when clicking on delete a record\n"
				+ "      handleDelete: function (index, row) {\n" + "         this.recordSelected = row[this.idPath];\n"
				+ "         this.dialogDeleteVisible = true;\n" + "      },\n"
				+ "      //function that is executed when accepting to edit a record\n"
				+ "      aceptedEdit: function () {\n" + "         var that = this;\n"
				+ "         console.log(this.jEditor.getValue());\n"
				+ "         vm.crudUpdate(this.jEditor.getValue(), this.selectedOntology, this.recordSelected).then(function (data) {\n"
				+ "            that.dialogEditVisible = false;\n" + "            that.loadData();\n"
				+ "            that.$notify({\n" + "               message: that.$t('message.edited.successfully'),\n"
				+ "               type: 'success'\n" + "            });\n" + "         })\n" + "\n" + "      },\n"
				+ "      //function that is executed when accepting to create a new record\n"
				+ "      aceptedCreate: function () {\n" + "         var that = this;\n"
				+ "         console.log(this.jEditor.getValue());\n" + "\n"
				+ "         vm.crudInsert(this.jEditor.getValue(), this.selectedOntology).then(function (data) {\n"
				+ "            that.dialogCreateVisible = false;\n" + "            that.loadData();\n"
				+ "            that.$notify({\n" + "               message: that.$t('message.created.successfully'),\n"
				+ "               type: 'success'\n" + "            });\n" + "         })\n" + "\n" + "      },\n"
				+ "      //function that is executed when accepting to delete a record\n"
				+ "      aceptedDelete: function () {\n" + "         var that = this;\n"
				+ "         vm.crudDeleteById(this.recordSelected, this.selectedOntology).then(function (data) {\n"
				+ "            that.loadData();\n" + "            that.$notify({\n"
				+ "               message: that.$t('message.deleted.successfully'),\n"
				+ "               type: 'success'\n" + "            });\n" + "         })\n"
				+ "         this.dialogDeleteVisible = false\n" + "      },\n" + "      submit: function (_e) {\n"
				+ "         alert(JSON.stringify(this.model));\n" + "      },\n" + "      reset: function () {\n"
				+ "         this.$refs.JsonEditor.reset();\n" + "      },\n"
				+ "      //function that is executed when selecting an entity\n" + "      onChangeEntity(value) {\n"
				+ "         this.loadHeadTable();\n" + "         this.calculeTableheight();\n"
				+ "         this.visibleColumns = [];\n" + "         this.resetWizard();\n"
				+ "         this.executeSearch=false;\n" + "      },\n" + "\n" + "\n"
				+ "      opendialogAddSelect: function () {\n" + "\n" + "      },\n"
				+ "      opendialogAddOrderBy: function () {\n" + "\n" + "      },\n"
				+ "      //function that clears the wizard fields\n" + "      resetWizard: function () {\n"
				+ "         this.selectWizard = [];\n" + "         this.selectWizardOptions = [];\n"
				+ "         this.orderByWizard = [];\n" + "         this.orderByWizardOptions = [];\n"
				+ "         this.limitWizard = 100;\n" + "         this.offsetWizard = 0;\n" + "      },\n"
				+ "      searchWizard: function () {\n" + "         this.loadData(true);\n"
				+ "         this.executeSearch=true;\n" + "      },\n"
				+ "      //function that creates a new option in the where combo\n"
				+ "      aceptedAddWhereParameter: function () {\n"
				+ "         if (typeof this.selectedParametereWhere != 'undefined' && this.selectedParametereWhere != null &&\n"
				+ "            typeof this.selectedOperatorWhere != 'undefined' && this.selectedOperatorWhere != null &&\n"
				+ "            typeof this.inputValueWhere != 'undefined' && this.inputValueWhere != null) {\n"
				+ "            var paramDescription = '';\n" + "            var type = '';\n"
				+ "            for (var i = 0; i < this.columnsParams.length; i++) {\n"
				+ "               if (this.columnsParams[i].prop == this.selectedParametereWhere) {\n"
				+ "                  paramDescription = this.columnsParams[i].label;\n"
				+ "                  type = this.columnsParams[i].thetype;\n" + "                  break\n"
				+ "               }\n" + "            }\n" + "            if (type != 'number') {\n"
				+ "               this.inputValueWhere = \"'\" + this.inputValueWhere + \"'\";\n" + "            }\n"
				+ "            var resultDescription = paramDescription + ' ' + this.selectedOperatorWhere + ' ' + this.inputValueWhere;\n"
				+ "            var resultPath = {\n" + "               column: this.selectedParametereWhere,\n"
				+ "               operator: this.selectedOperatorWhere,\n" + "               condition: 'AND',\n"
				+ "               value: this.inputValueWhere\n" + "            };\n"
				+ "            this.selectWizardOptions.push({\n" + "               label: resultDescription,\n"
				+ "               value: JSON.stringify(resultPath)\n" + "            });\n"
				+ "            this.dialogAddSelectVisible = false;\n" + "         } else {\n"
				+ "            //show message need parameters\n" + "            that.$notify({\n"
				+ "               message: that.$t('error.message.incomplete'),\n" + "               type: 'warning'\n"
				+ "            });\n" + "         }\n" + "      },\n"
				+ "      aceptedAddOrderByParameter: function () {\n"
				+ "         if (typeof this.selectedOperatorOrderBy != 'undefined' && this.selectedOperatorOrderBy != null &&\n"
				+ "            typeof this.selectedParametereOrderBy != 'undefined' && this.selectedParametereOrderBy != null) {\n"
				+ "            var paramDescription = '';\n" + "            var type = '';\n"
				+ "            for (var i = 0; i < this.columnsParams.length; i++) {\n"
				+ "               if (this.columnsParams[i].prop == this.selectedParametereOrderBy) {\n"
				+ "                  paramDescription = this.columnsParams[i].label;\n" + "                  break\n"
				+ "               }\n" + "            }\n"
				+ "            var resultDescription = paramDescription + \" \" + this.selectedOperatorOrderBy;\n"
				+ "            var resultPath = {\n" + "               column: this.selectedParametereOrderBy,\n"
				+ "               order: this.selectedOperatorOrderBy\n" + "            };\n"
				+ "            this.orderByWizardOptions.push({\n" + "               label: resultDescription,\n"
				+ "               value: JSON.stringify(resultPath)\n" + "            });\n" + "\n"
				+ "            this.dialogAddOrderByVisible = false;\n" + "         }\n" + "      },\n"
				+ "      //initialize orderby\n" + "      dialogAddOrderByVisibleFunction: function () {\n"
				+ "         this.selectedOperatorOrderBy = null;\n"
				+ "         this.selectedParametereOrderBy = null;\n"
				+ "         this.dialogAddOrderByVisible = true;\n" + "      },\n" + "      //initialize where\n"
				+ "      dialogAddSelectVisibleFunction: function () {\n"
				+ "         this.selectedParametereWhere = null;\n" + "         this.selectedOperatorWhere = null;\n"
				+ "         this.inputValueWhere = \"\";\n" + "         this.dialogAddSelectVisible = true;\n"
				+ "      },\n" + "      orderColumns: function (a, b) {\n" + "         if (a.label == 'id') {\n"
				+ "            return 1;\n" + "         } else if (b.label == 'id') {\n" + "            return -1;\n"
				+ "         } else if (a.label > b.label) {\n" + "            return 1;\n"
				+ "         } else if (a.label < b.label) {\n" + "            return -1;\n" + "         }\n"
				+ "         return 0;\n" + "      },\n" + "      orderKeys: function (a, b) {\n"
				+ "         if (a == 'id') {\n" + "            return 1;\n" + "         } else if (b == 'id') {\n"
				+ "            return -1;\n" + "         } else if (a > b) {\n" + "            return 1;\n"
				+ "         } else if (a < b) {\n" + "            return -1;\n" + "         }\n"
				+ "         return 0;\n" + "      },\n" + "\n" + "      tableDatafilter: function (element) {\n"
				+ "         if (this.searchString == null || this.searchString.trim().length == 0) {\n"
				+ "            return true;\n" + "         }\n"
				+ "         return JSON.stringify(element).toLowerCase().indexOf(this.searchString.toLowerCase()) > -1;\n"
				+ "\n" + "      },\n" + "      aceptedDownloadAll: function(){\n" + "         var that = this;\n"
				+ "         vm.validationDownloadEntity(this.selectedOntology,this.downloadType).then(function(data){\n"
				+ "            if(data.data.message=='ok'){\n" + "               if(that.downloadType=='csv'){\n"
				+ "                  vm.downloadEntityAllCsv(that.selectedOntology);\n"
				+ "                  that.dialogDownloadVisible=false;\n" + "               }else{\n"
				+ "                  vm.downloadEntityAllJson(that.selectedOntology);\n"
				+ "                  that.dialogDownloadVisible=false;\n" + "               }\n"
				+ "            }else{\n" + "                that.$notify({\n"
				+ "                        message: that.$t(data.data.message),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "            }\n"
				+ "         })\n" + "      },\n" + "      aceptedDownloadOnlySelec: function () {\n"
				+ "         var selection = encodeURIComponent(JSON.stringify({ ontology: this.selectedOntology, columns: [], where: this.mapArrayToObjects(this.selectWizard), orderBy: this.mapArrayToObjects(this.orderByWizard), limit: this.limitWizard, offset: this.offsetWizard }));\n"
				+ "         var that = this;\n"
				+ "         vm.validationDownloadEntitySelected(this.selectedOntology, selection,this.downloadType).then(function (data) {\n"
				+ "            if (data.data.message == 'ok') {\n"
				+ "               if (that.downloadType == 'csv') {\n"
				+ "                  vm.downloadEntitySelectedCsv(that.selectedOntology, selection);\n"
				+ "                  that.dialogDownloadVisible = false;\n" + "               } else {\n"
				+ "                  vm.downloadEntitySelectedJson(that.selectedOntology, selection);\n"
				+ "                  that.dialogDownloadVisible = false;\n" + "               }\n"
				+ "            }else{\n" + "               that.$notify({\n"
				+ "                        message: that.$t(data.data.message),\n"
				+ "                        type: 'error'\n" + "                     });\n" + "            }\n"
				+ "         })\n" + "      },\n" + "      downloadData: function (command) {\n"
				+ "         var that = this;\n" + "         if (command === 'allcsv') {\n"
				+ "            vm.validationDownloadEntity(this.selectedOntology,'csv').then(function (data) {\n"
				+ "               if (data.data.message == 'ok') {\n"
				+ "                  vm.downloadEntityAllCsv(that.selectedOntology);\n" + "               } else {\n"
				+ "                  that.$notify({\n"
				+ "                           message: that.$t(data.data.message),\n"
				+ "                           type: 'error'\n" + "                        });\n" + "               }\n"
				+ "            })\n" + "         } else if (command === 'alljson') {\n"
				+ "            vm.validationDownloadEntity(this.selectedOntology,'json').then(function (data) {\n"
				+ "               if (data.data.message == 'ok') {\n"
				+ "                  vm.downloadEntityAllJson(that.selectedOntology);\n" + "               }else{\n"
				+ "                  that.$notify({\n"
				+ "                        message: that.$t(data.data.message),\n"
				+ "                           type: 'error'\n" + "                  });\n" + "               }\n"
				+ "            })\n" + "         } else {\n" + "            this.downloadType = command;\n"
				+ "            this.dialogDownloadVisible = true;\n" + "\n" + "         }\n" + "      },\n"
				+ "      mapArrayToObjects: function (array) {\n" + "         var data = [];\n"
				+ "         if (typeof array != 'undefined' && array != null && array.length > 0) {\n"
				+ "            for (var i = 0; i < array.length; i++) {\n"
				+ "               data.push(JSON.parse(array[i]));\n" + "            }\n" + "         }\n"
				+ "         return data;\n" + "      },\n" + "      sortChange(column, key, order) {\n"
				+ "         var that = this;\n" + "         var type = this.columns.filter(function (elem) {\n"
				+ "            return elem.prop == column.prop\n" + "         });\n"
				+ "         if (typeof type !== 'undefined' && type != null && type.length > 0) {\n"
				+ "            type = type[0].thetype;\n" + "         } else {\n" + "            type = 'string';\n"
				+ "         }\n" + "         this.tableData.sort(function (a, b) {\n"
				+ "            if (column.order == 'descending') {\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) > that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return -1;\n" + "               }\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) < that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return 1;\n" + "               }\n" + "            } else {\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) < that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return -1;\n" + "               }\n"
				+ "               if (that.formatFieldForSort(a[column.prop], type) > that.formatFieldForSort(b[column.prop], type)) {\n"
				+ "                  return 1;\n" + "               }\n" + "            }\n" + "            return 0;\n"
				+ "         });\n" + "\n" + "         console.log(column, key, order)\n" + "      },\n"
				+ "      loadProperties:function(element,path,stack){\n" + "         if(element.properties){\n"
				+ "            var keys = Object.keys(element.properties);\n" + "            var dot='';\n"
				+ "            if (path.length>0){dot='.';}\n" + "            for(var i=0;i< keys.length; i++){\n"
				+ "                  this.loadProperties(element.properties[keys[i]],path+dot+keys[i],stack);\n"
				+ "               }\n" + "         }else{\n" + "            var keys = Object.keys(element);\n"
				+ "            var findRef = false;\n" + "            var ref = \"\";\n"
				+ "            for(var i=0;i< keys.length; i++){\n" + "               if( keys[i] == '$ref' ){\n"
				+ "                  findRef = true;\n" + "                  ref = element.$ref.substring(2);\n"
				+ "                  break;\n" + "               }\n" + "            }\n"
				+ "            if( findRef ){\n" + "               stack.push({'path':path,'ref':ref});\n"
				+ "            }else{\n" + "               if(element.description){\n"
				+ "                  element.title = this.$t(element.description);\n"
				+ "                  delete element.description;\n" + "               }else{\n"
				+ "                  element.title = this.$t(path);\n" + "               }\n" + "\n" + "            }\n"
				+ "         }\n" + "      },\n" + "\n"
				+ "      //This function maps the labels by titles in the outline for the edit, creation and detail forms\n"
				+ "      changeDescriptionForTitle: function (schema) {\n" + "\n"
				+ "         var root = JSON.parse(schema);\n" + "         var stack = [];\n"
				+ "         var result = this.loadProperties(root,'',stack);\n" + "         while(stack.length > 0){\n"
				+ "            var stackElement = stack.pop();\n"
				+ "            this.loadProperties(root[stackElement.ref],stackElement.path,stack);\n" + "         }\n"
				+ "         return JSON.stringify(root);\n" + "      },\n"
				+ "      formatFieldForSort: function (element, type) {\n" + "         if (type == 'number') {\n"
				+ "            return Number(element);\n" + "         } else if (type == 'string') {\n"
				+ "            return element + '';\n" + "         } else {\n" + "            return element;\n"
				+ "         }\n" + "      },\n" + "      sendValue: vm.sendValue,\n"
				+ "      sendFilter: vm.sendFilter,\n" + "      //calculate and resize the table\n"
				+ "      calculeTableheight: function () {\n" + "         var totalHeight = 166;\n"
				+ "         if (this.showWizard) {\n" + "            totalHeight = totalHeight + 103;\n"
				+ "         }\n"
				+ "         this.tableHeight = document.getElementById(vm.id).querySelector('vuetemplate').offsetHeight - totalHeight;\n"
				+ "      }\n" + "   },\n" + "   mounted() {\n" + "      if(vm.tparams && vm.tparams.parameters){\n"
				+ "         this.hideIdColumn=vm.tparams.parameters.hideIdColumn;\n"
				+ "         this.initialEntity=vm.tparams.parameters.initialEntity;\n"
				+ "         this.typeGadget=vm.tparams.parameters.typeGadget;\n" + "      }\n" + "\n"
				+ "      this.loadEntities();\n" + "      var that = this;\n" + "      //Resize event observer\n"
				+ "      this.resizeObserver = new ResizeObserver(function (entrie) {\n"
				+ "         that.calculeTableheight();\n" + "      });\n"
				+ "      this.resizeObserver.observe(document.getElementById(vm.id).querySelector('vuetemplate'));\n"
				+ "   },\n" + "   i18n: window.i18n\n" + "\n" + "}\n" + "//Init Vue app\n"
				+ "vm.vueapp = new Vue(vm.vueconfig);\n" + "");
		gadgetTemplate.setConfig(
				"{\"gform\":[{\"id\":1,\"type\":\"input-text\",\"name\":\"initialEntity\",\"default\":\"\",\"title\":\"initialEntity\"},{\"id\":3,\"type\":\"selector\",\"name\":\"typeGadget\",\"options\":[{\"value\":\"withWizard\",\"text\":\"withWizard\"},{\"value\":\"noWizard\",\"text\":\"noWizard\"},{\"value\":\"searchOnly\",\"text\":\"searchOnly\"}],\"title\":\"typeGadget\",\"default\":\"withWizard\"},{\"id\":4,\"type\":\"checkbox\",\"name\":\"hideIdColumn\",\"default\":false,\"title\":\"hideIdColumn\"}]}");
		gadgetTemplate.setUser(getUserAdministrator());
		gadgetTemplateRepository.save(gadgetTemplate);

		if (gadgetTemplateRepository.findById("MASTER-GadgetTemplate-13").orElse(null) == null) {
			gadgetTemplate = new GadgetTemplate();
		} else {
			gadgetTemplate = gadgetTemplateRepository.findById("MASTER-GadgetTemplate-13").get();
		}

		gadgetTemplate.setId("MASTER-GadgetTemplate-13");
		gadgetTemplate.setIdentification("ods-gadget-import");
		gadgetTemplate.setPublic(true);
		gadgetTemplate.setType("vueJSODS");
		gadgetTemplate.setHeaderlibs("\n"
				+ "<script src=\"/controlpanel/static/vendor/vue-i18n/vue-i18n.js\"></script>\n" + "\n" + "\n"
				+ "<script>\n" + "\n" + "var __env = __env || {};\n"
				+ "if(typeof __env.i18njson=='undefined'|| __env.i18njson==null || typeof __env.i18njson.default=='undefined'){\n"
				+ "  __env.i18njson={\n" + "    default:\"EN\",\n" + "    languages:{\"ES\": {\n"
				+ "			\"form.entity\": \"Entidad\",\n"
				+ "			\"form.show.wizard\": \"Mostrar asistente de búsqueda\",\n"
				+ "			\"form.select\": \"Seleccionar\",\n"
				+ "			\"form.select.fields\": \"Seleccionar campos\",\n"
				+ "			\"form.operator\": \"Operador\",\n" + "			\"form.condition\": \"Condición\",\n"
				+ "			\"form.select.operator\": \"Seleccionar operador\",\n"
				+ "			\"form.write.here\": \"Escriba aquí\",\n"
				+ "			\"form.select.field\": \"Seleccionar campo\",\n"
				+ "			\"form.orderby\": \"Ordenar por\",\n"
				+ "			\"form.order.type\": \"Tipo de pedido\",\n" + "			\"form.where\": \"Where\",\n"
				+ "			\"form.max.value\": \"Valor máximo\",\n"
				+ "			\"form.offset\": \"Desplazamiento\",\n" + "			\"form.reset\": \"Restablecer\",\n"
				+ "			\"form.search\": \"Buscar\",\n" + "			\"form.records\": \"Registros\",\n"
				+ "			\"form.columns\": \"Columnas\",\n" + "			\"column.options\": \"Opciones\",\n"
				+ "			\"form.new.record.title\": \"Nuevo registro\",\n"
				+ "			\"error.message.ontology\": \"La entidad pasada por parámetro no existe\",\n"
				+ "			\"error.message.querying.the.data\": \"Se produjo un error al consultar los datos\",\n"
				+ "			\"error.message.incomplete\": \"No ha rellenado todos los campos correctamente\",\n"
				+ "			\"message.edited.successfully\": \"Registro editado correctamente\",\n"
				+ "			\"message.created.successfully\": \"Registro creado correctamente\",\n"
				+ "			\"message.deleted.successfully\": \"Registro eliminado correctamente\",\n"
				+ "			\"message.modal.delete.title\": \"¿Está seguro de eliminar el registro?\",\n"
				+ "			\"message.modal.delete.subtitle\": \"Esta acción es irreversible\",\n"
				+ "			\"form.edit.record\": \"Editar registro\",\n"
				+ "			\"form.detail.record\": \"Registro detallado\",\n"
				+ "			\"button.cancel\": \"Cancelar\",\n" + "			\"button.delete\": \"Eliminar\",\n"
				+ "			\"button.save\": \"Guardar\",\n" + "			\"button.close\": \"Cerrar\",\n"
				+ "			\"button.new\": \"Nuevo\",\n" + "			\"button.apply\": \"Aplicar\",\n"
				+ "		    \"form.select.entity\": \"Seleccionar Entidad\",\n"
				+ "		    \"form.title.import\": \"Importar datos\",\n"
				+ "		    \"form.download.template\": \"Descargar Esquema\",\n"
				+ "			\"form.download.csv\":\"Descargar CSV\",\n"
				+ "    		\"form.download.json\":\"Descargar JSON\",\n"
				+ "		    \"button.drop\": \"Arrastre el fichero o\",\n"
				+ "		    \"button.click\": \"haga click aquí\",\n"
				+ "		    \"button.click.upload\": \"para subirlo\",\n"
				+ "		    \"form.info.max\": \"Máx. 2mb csv\",\n" + "		    \"button.import\": \"Importar\",\n"
				+ "		    \"button.showmore\": \"Mostrar más detalles\",\n"
				+ "		    \"error.message.exceed\": \"El fichero no puede superar los 2MB\",\n"
				+ "		    \"message.success.loaded.1\": \"El fichero\",\n"
				+ "		    \"message.success.loaded.2\": \"se ha cargado correctamente.\",\n"
				+ "		    \"message.alert.onefile\": \"Sólo se puede subir un fichero. Elimine el fichero seleccionado para cargar uno nuevo.\",\n"
				+ "		    \"form.download.info\": \"El esquema se descargará con una entrada ejemplo con el formato de cada columna\",\n"
				+ "		    \"error.message.csvformat\": \"No se puede descargar el esquema en formato csv. La entidad tiene una estructura compleja.\",\n"
				+ "		    \"error.message.csvseparator\": \"Descargue el esquema. El caracter usado como separador en el csv debe ser ;\",\n"
				+ "		    \"error.message.fileType\": \"Tipo de fichero incorrecto. Sólo se permite formato CSV, XML y JSON\",\n"
				+ "		    \"error.message.processing\": \"Error en el procesado del dato\",\n"
				+ "		    \"error.message.insert\": \"Se ha producido un error en la insercción de los datos\",\n"
				+ "		    \"error.message.parsing\": \"Se ha producido un error en el parseo de los datos a insertar\",\n"
				+ "		    \"error.message.exists\": \"La entidad no existe\",\n"
				+ "		    \"message.success.inserted\": \"Registros insertados: \",\n"
				+ "			\"message.download.all\":\"¿Quieres descargar solo la selección o todos los registros?\",\n"
				+ "			\"button.all.records\": \"Todos los registros\",\n"
				+ "			\"button.only.selection.records\": \"Sólo la selección\",\n"
				+ "			\"error.message.download\": \"Error descargando datos\",\n"
				+ "			\"error.message.empty\": \"Error no existen registros\",\n"
				+ "			\"error.message.malformed.array\":\"La estructura de alguno de los arrays es incorrecta, el formato que se debe seguir es  param:{type:array,items:[{type:string}]}\",\n"
				+ "         \"message.choose.download.format\": \"Elija el formato en el que desea descargar el esquema\"\n"
				+ "		},\n" + "		\"EN\": {\n" + "			\"form.entity\": \"Entity\",\n"
				+ "			\"form.show.wizard\": \"Show search wizard\",\n"
				+ "			\"form.select\": \"Select\",\n" + "			\"form.select.fields\": \"Select Fields\",\n"
				+ "			\"form.operator\": \"Operator\",\n" + "			\"form.condition\": \"Condition\",\n"
				+ "			\"form.select.operator\": \"Select Operator\",\n"
				+ "			\"form.write.here\": \"Write here\",\n"
				+ "			\"form.select.field\": \"Select Field\",\n" + "			\"form.orderby\": \"Order by\",\n"
				+ "			\"form.order.type\": \"Order Type\",\n" + "			\"form.where\": \"Where\",\n"
				+ "			\"form.max.value\": \"Max Value\",\n" + "			\"form.offset\": \"Offset\",\n"
				+ "			\"form.reset\": \"Reset\",\n" + "			\"form.search\": \"Search\",\n"
				+ "			\"form.records\": \"Records\",\n" + "			\"form.columns\": \"Columns\",\n"
				+ "			\"column.options\": \"Options\",\n"
				+ "			\"form.new.record.title\": \"New record\",\n"
				+ "			\"error.message.ontology\": \"The entity passed by parameter does not exist\",\n"
				+ "			\"error.message.querying.the.data\": \"An error occurred while querying the data\",\n"
				+ "			\"error.message.incomplete\": \"You did not fill in all the fields correctly\",\n"
				+ "			\"message.edited.successfully\": \"Record edited successfully\",\n"
				+ "			\"message.created.successfully\": \"Record created successfully\",\n"
				+ "			\"message.deleted.successfully\": \"Record deleted successfully\",\n"
				+ "			\"message.modal.delete.title\": \"Are you sure of delete the record?\",\n"
				+ "			\"message.modal.delete.subtitle\": \"This action is irreversible.\",\n"
				+ "			\"form.edit.record\": \"Edit record \",\n"
				+ "			\"form.detail.record\": \"Detail record \",\n"
				+ "			\"button.cancel\": \"Cancel\",\n" + "			\"button.delete\": \"Delete\",\n"
				+ "			\"button.save\": \"Save\",\n" + "			\"button.close\": \"Close\",\n"
				+ "			\"button.new\": \"New\",\n" + "			\"button.apply\": \"Apply\",\n"
				+ "		    \"form.select.entity\": \"Select Entity\",\n"
				+ "		    \"form.title.import\": \"Import records\",\n"
				+ "		    \"form.download.template\": \"Download Template\",\n"
				+ "			\"form.download.csv\":\"Download CSV\",\n"
				+ "    		\"form.download.json\":\"Download JSON\",\n"
				+ "		    \"button.drop\": \"Drop file or\",\n" + "		    \"button.click\": \"click here\",\n"
				+ "		    \"button.click.upload\": \"to upload\",\n"
				+ "		    \"form.info.max\": \"Max. 2mb csv\",\n" + "		    \"button.import\": \"Import\",\n"
				+ "		    \"button.showmore\": \"Show More Details\",\n"
				+ "		    \"error.message.exceed\": \"The upload file size cannot exceed 2MB!\",\n"
				+ "		    \"message.success.loaded.1\": \"The\",\n"
				+ "		    \"message.success.loaded.2\": \"file has been loaded successfully.\",\n"
				+ "		    \"message.alert.onefile\": \"Only one file can be uploaded. Delete the selected file to load a new one.\",\n"
				+ "		    \"form.download.info\": \"The scheme will be downloaded with an example entry with the format of each column\",\n"
				+ "		    \"error.message.csvformat\": \"Cannot download schematic in csv format. The entity has a complex structure.\",\n"
				+ "		    \"error.message.csvseparator\": \"Download the template. The character used as a separator in the csv must be ;\",\n"
				+ "		    \"error.message.fileType\": \"Invalid file type. Only CSV, XML and JSON files are acceptable\",\n"
				+ "		    \"error.message.processing\": \"Error processing data\",\n"
				+ "		    \"error.message.insert\": \"There was an error inserting bulk data\",\n"
				+ "		    \"error.message.parsing\": \"There was an error parsing the data to insert\",\n"
				+ "		    \"error.message.exists\": \"The entity does not exist\",\n"
				+ "		    \"message.success.inserted\": \"Records inserted: \",\n"
				+ "			\"message.download.all\":\"Do you want to download only the selection or all the records?\",\n"
				+ "			\"button.all.records\": \"All the records\",\n"
				+ "			\"button.only.selection.records\": \"Only the selection\",\n"
				+ "			\"error.message.download\": \"Error downloading data\",\n"
				+ "			\"error.message.empty\": \"Error there are no records\",\n"
				+ "			\"error.message.malformed.array\": \"The structure of some of the arrays is incorrect, the format to follow is param: {type: array, items: [{type: string}]}\",\n"
				+ "         \"message.choose.download.format\": \"Choose the format in which you want to download the schematic\"\n"
				+ "		}\n" + "}\n" + "  }\n" + "}  \n" + "function getLocale(){\n" + "	var localLocale ='EN';\n"
				+ "	try{\n" + "		localLocale = getURLParameters()['lang'].toUpperCase();\n" + "	}catch(error){\n"
				+ "		localLocale ='EN';\n" + "	}\n" + "	return localLocale\n" + "}\n" + "\n"
				+ "window.i18n = new VueI18n({\n" + " locale: getLocale(),\n" + " fallbackLocale: 'EN',\n"
				+ " // link messages with internacionalization json on controlpanel\n"
				+ " messages: __env.i18njson.languages\n" + " });\n" + "\n" + "</script>");

		gadgetTemplate.setDescription("ODS Import gadget template");
		gadgetTemplate.setTemplate("<style>\n" + "  .ods-upload-list__item-name {\n" + "    max-height:30px;\n"
				+ "    font-size: small;}\n" + "  .control-label {\n" + "    margin-top: 1px;\n"
				+ "    color: #505D66;\n" + "    font-weight: normal;\n" + "    width: fit-content;\n"
				+ "    font-size: small; }\n" + "  .control-label .required,\n" + "  .form-group .required {\n"
				+ "    color: #A73535;\n" + "    font-size: 12px;\n" + "    padding-left: 2px; }\n"
				+ "  .ods-textarea__inner {\n" + "    min-height: 40% !important;\n" + "  } \n" + "  .ods-upload {\n"
				+ "    width: 100%;\n" + "  }\n" + "  .ods-upload-dragger{\n" + "    width: 100%;\n"
				+ "    height: 150px;\n" + "  }\n" + "  footer {\n" + "    display: flex;\n"
				+ "    flex-direction: row;\n" + "    align-items: center;\n" + "    justify-content: flex-end;\n"
				+ "    padding: 16px 24px;\n" + "    background: #F0F1F2;\n" + "  }\n" + "  .livehtmlnotfull {\n"
				+ "    display: block;\n" + "    width: calc(100%);\n" + "    position: absolute;\n" + "    top: 50%;\n"
				+ "    left: 50%;\n" + "    transform: translate(-50%, -50%);\n" + "    overflow: hidden;\n" + "  }\n"
				+ "  .custommargin {\n" + "    margin-left:15px; \n" + "    margin-right:15px;\n"
				+ "    margin-bottom: 10px;\n" + "  }\n" + "  \n" + "  .icons-grey {\n"
				+ "    filter: invert(0%) sepia(0%) saturate(0%) hue-rotate(162deg) brightness(93%) contrast(88%);\n"
				+ "  }\n" + "  .icons-align {\n" + "    vertical-align: middle;\n" + "    margin-left: 5px;\n" + "  }\n"
				+ "  </style>\n" + "  \n" + "  <div class=\"gadget-app\">\n"
				+ "    <h5 class=\"gadget-title ng-binding ng-scope custommargin\">{{ $t(\"form.title.import\") }}</h5>\n"
				+ "    <div class=\"selectOnto custommargin\" v-if=\"showSelect\">\n"
				+ "      <label class=\"control-label\"> {{ $t(\"form.select.entity\") }} <span class=\"required\" aria-required=\"true\"> *  </span></label>\n"
				+ "      <ods-select :disabled=\"showSelectOntology\" v-model=\"selectedOntology\" @change=\"onChangeOntology($event)\" filterable :placeholder=\"$t('form.select')\" \n"
				+ "        style=\"margin-top:5px; width: 100%;background: #F7F8F8;\" size=\"small\">\n"
				+ "        <ods-option\n" + "          v-for=\"onto in ontologies\"\n"
				+ "          :key=\"onto.identification\"\n" + "          :label=\"onto.identification\"\n"
				+ "          :value=\"onto.identification\">\n" + "        </ods-option>\n" + "      </ods-select>\n"
				+ "    </div>\n" + "    <div class=\"custommargin\" v-if=\"showEntityName\">\n"
				+ "      <label class=\"control-label\" style=\"color:#060E14;\">{{ $t(\"form.entity\")}}: </label><label class=\"control-label\"> {{selectedOntology}} <label>\n"
				+ "    </div>\n" + "    <div class=\"downloadSchm custommargin\" style=\"margin-top: 10px;\">\n"
				+ "      <ods-button class=\"primary\" style=\"margin-top: 5px;border: 1px solid #1168A6;box-sizing: border-box;border-radius: 2px;text-align: center;\" \n"
				+ "        size=\"small\" :disabled= \"downloaddisabled\" plain @click=\"dialogDownloadOptions = true\">{{ $t(\"form.download.template\") }} <i class=\"el-icon-download\"></i></ods-button>\n"
				+ "       <ods-popover placement=\"top-start\" title=\"Info\" width=\"240\" trigger=\"hover\" :content=\"$t('form.download.info')\">\n"
				+ "        <span class=\"ods-icon-info\"  slot=\"reference\" style=\"height:12px\">\n"
				+ "      </ods-popover>\n" + "    </div>\n"
				+ "    <div id=\"uploadCSVFile \"  class=\"custommargin\" style=\"margin-top: 15px;\">\n"
				+ "      <ods-upload id=\"upload-csv\" drag=\"true\" style=\"width: 100%; height: 210px;\"\n"
				+ "        :action=\"urlimport\" :disabled= \"uploaddisabled\"\n"
				+ "        :auto-upload=\"false\" :file-list=\"fileList\" \n"
				+ "        :on-preview=\"handlePreview\" :on-remove=\"handleRemove\" \n"
				+ "        :limit=\"limitUpload\"\n"
				+ "        accept=\".csv, .json\" :before-upload=\"beforeCSVUpload\"\n"
				+ "        ref=\"upload\"  :on-exceed=\"handleExceed\"\n"
				+ "        :on-error=\"handleError\" :on-success=\"handleSuccess\">\n"
				+ "        <i class=\"ods-icon-download fa-3x\" style=\"margin-top:25px;color: #505D66;\"></i>\n"
				+ "        <div class=\"ods-upload__text\">{{ $t(\"button.drop\") }} <em class=\"textButtonColor\">{{ $t(\"button.click\") }}</em> {{ $t(\"button.click.upload\") }}</div>\n"
				+ "        <div slot=\"tip\" class=\"ods-upload__tip\" style=\"font-size: 11px;line-height: 16px;color: #A7AEB2;\">{{ $t(\"form.info.max\") }}</div>\n"
				+ "      </ods-upload>\n" + "    \n" + "    </div>\n" + "    <footer style=\"margin-top: 10px;\">\n"
				+ "      <div slot=\"tip\" style=\"text-align: right;\">\n"
				+ "        <ods-button class=\"secundary\" style=\"margin-left: 10px; background: #F0F1F2;text-align: center;\" size=\"small\" plain @click=\"clearFiles\">Cancel</ods-button>\n"
				+ "        <ods-button style=\"margin-left: 10px; background: #1168A6; border-radius: 2px;text-align: center;\" ref=\"importbutton\" size=\"small\" \n"
				+ "          :disabled=\"importdisabled\" type=\"primary\" @click=\"submitUpload\">{{ $t(\"button.import\") }} <i class=\"el-icon-upload2\"></i></ods-button>\n"
				+ "      </div>\n" + "    </footer>\n"
				+ "    <ods-dialog modal append-to-body title=\"Error\" :visible.sync=\"dialogCreateVisible\" width=\"35%\" @close=\"closeErrDialog\">\n"
				+ "      <div class=\"ods-message-box__container\">       \n"
				+ "        <div class=\"ods-message-box__message\">\n" + "          <span>{{msgerr}}</span>\n"
				+ "        </div>\n" + "      </div>\n" + "       <div style=\"margin-top: 5px;\">\n"
				+ "        <ods-button class=\"primary\" v-if=\"showDetailBtn\" size=\"small\" @click=\"showErrDetails\" style=\"margin-left: 30px; border: none;text-align: center;\">{{ $t(\"button.showmore\") }}</ods-button>\n"
				+ "        <ods-input v-if=\"showDetails\" type=\"textarea\" v-model=\"detailerr\" readonly></ods-input>\n"
				+ "      </div>\n" + "      <div style=\"text-align: right; margin-top: 10px;\">\n"
				+ "        <ods-button class=\"textButtonColor\" @click=\"closeErrDialog\" style=\"margin-left: 10px; border: none;text-align: center;\" size=\"small\">{{ $t(\"button.cancel\") }}</ods-button>\n"
				+ "      </div>\n" + "    </ods-dialog>\n"
				+ "    <ods-dialog modal append-to-body :title=\"$t('message.choose.download.format')\" :visible.sync=\"dialogDownloadOptions\" width=\"25%\" @close=\"closeDialog\">\n"
				+ "      <div class=\"ods-message-box__container\" style=\"text-align: center;margin-top: 2px;\">\n"
				+ "            <ods-button class=\"textButtonColor\" style=\"margin-left: 10px;margin-top: 10px;border: 1px solid #1168A6;box-sizing: border-box;border-radius: 2px;text-align: center;\" \n"
				+ "            size=\"small\" plain @click=\"getCSVSchema\"> {{ $t(\"form.download.csv\") }} <i class=\"el-icon-download\"></i></ods-button>\n"
				+ "            <ods-button class=\"textButtonColor\" style=\"margin-top: 10px;border: 1px solid #1168A6;box-sizing: border-box;border-radius: 2px;text-align: center;\" \n"
				+ "            size=\"small\" plain @click=\"getJSONSchema\"> {{ $t(\"form.download.json\") }} <i class=\"el-icon-download\"></i></ods-button>\n"
				+ "      </div>\n" + "    </ods-dialog>\n" + "  </div>");
		gadgetTemplate.setTemplateJS("vm.vueconfig = {\n"
				+ "    el: document.getElementById(vm.id).querySelector('vuetemplate  .gadget-app'),\n" + "    data:{\n"
				+ "        initialEntity: \"\" , //variable that initializes the entity with the value assigned to it\n"
				+ "        ontologies:[],\n" + "        selectedOntology:{},\n" + "        fileList:[],\n"
				+ "        urlimport:'',\n" + "        importdisabled: '',\n" + "        downloaddisabled: '',\n"
				+ "        uploaddisabled:'',\n" + "        detailerr:'',\n" + "        msgerr:'',\n"
				+ "        showDetails: false,\n" + "        dialogCreateVisible:false,\n" + "        limitUpload:1,\n"
				+ "        showDetailBtn: false,\n" + "        showSelectOntology: false,\n"
				+ "        showSelect: true,\n" + "        showEntityName: false,\n"
				+ "        dialogDownloadOptions: false\n" + "    },\n" + "    methods:{\n"
				+ "        drawVueComponent: function(newData,oldData){\n"
				+ "            //This will be call on new data\n" + "        },\n"
				+ "        resizeEvent: function(){\n" + "            //Resize event\n" + "        },\n"
				+ "        destroyVueComponent: function(){\n" + "            vm.vueapp.$destroy();\n" + "        },\n"
				+ "        receiveValue: function(data){\n" + "            //data received from datalink\n"
				+ "        },\n" + "        loadOntologies:function(search, loading) {\n"
				+ "            var that = this;\n" + "            vm.getEntities().then(function(data){\n"
				+ "                that.ontologies = data.data.map(function(obj){return {id:obj.id,identification:obj.identification}});\n"
				+ "\n" + "                if(that.initialEntity!=null && that.initialEntity!==\"\"){\n"
				+ "                    if (that.ontologies.some(e => e.identification === that.initialEntity)) {\n"
				+ "                            that.selectedOntology = that.initialEntity;\n"
				+ "                            that.importdisabled = false;\n"
				+ "                            that.downloaddisabled = false;\n"
				+ "                            that.uploaddisabled = false;\n"
				+ "                            that.showSelect = false;\n"
				+ "                            that.showEntityName = true;\n"
				+ "                            that.onChangeOntology( that.initialEntity);"
				+ "                        } else {\n"
				+ "                            that.msgerr = that.$t('error.message.ontology');                      \n"
				+ "                            that.dialogCreateVisible = true;\n" + "                        }\n"
				+ "                        that.showSelectOntology=true;\n" + "                        return;\n"
				+ "                }\n" + "\n"
				+ "                var urlparam = urlParamService.generateFiltersForGadgetId(vm.id);\n"
				+ "                if(typeof urlparam!== 'undefined' && urlparam!==null && urlparam.length>0){             \n"
				+ "                    if(urlparam[0].exp!=null){\n"
				+ "                        var urlontology = urlparam[0].exp.replace(/\"/g,'');\n"
				+ "                        if(that.ontologies.some(e => e.identification === urlontology)) {\n"
				+ "                            that.selectedOntology = urlontology;\n"
				+ "                            that.importdisabled = false;\n"
				+ "                            that.downloaddisabled = false;\n"
				+ "                            that.uploaddisabled = false;\n"
				+ "                            that.showSelect = false;\n"
				+ "                             that.showEntityName = true;\n"
				+ "                            that.onChangeOntology( that.initialEntity);"
				+ "                        } else {\n"
				+ "                            that.msgerr = that.$t('error.message.ontology');                      \n"
				+ "                            that.dialogCreateVisible = true;\n" + "                        }\n"
				+ "                        that.showSelectOntology=true;\n" + "                        return;\n"
				+ "                    }\n" + "                }else{\n"
				+ "                    that.showSelectOntology=false;\n" + "                }\n" + "            })\n"
				+ "        },\n" + "        getCSVSchema:function() {\n" + "            var that = this;\n"
				+ "            vm.isComplexSchema(this.selectedOntology).then(function(data){\n"
				+ "                if(data.data.message == 'ok'){\n"
				+ "                    vm.downloadEntitySchemaCsv(that.selectedOntology);\n"
				+ "                } else{\n"
				+ "                that.msgerr = that.$t(data.data.message);               \n"
				+ "                that.dialogCreateVisible = true;\n" + "                }\n" + "            });\n"
				+ "            that.dialogDownloadOptions = false;\n" + "        },\n"
				+ "        getJSONSchema: function() {\n" + "            var that = this;\n"
				+ "            vm.downloadEntitySchemaJson(that.selectedOntology);\n"
				+ "            that.dialogDownloadOptions = false;\n" + "        },\n"
				+ "        submitUpload:function() {\n" + "            this.$refs.upload.submit();\n" + "        },\n"
				+ "         onChangeOntology(value) {\n" + "            this.selectedOntology = value;\n"
				+ "            this.urlimport = \"/dashboardengine/api/insertDataEntity/\" + value;  \n"
				+ "            this.importdisabled = false;\n" + "            this.downloaddisabled = false;\n"
				+ "            this.uploaddisabled = false;\n" + "        },\n"
				+ "        beforeCSVUpload:function(file) { \n"
				+ "            const isLt2M = file.size / 1024 < 2000;\n" + "            if(!isLt2M) {\n"
				+ "                this.$alert(this.$t('error.message.exceed'), 'Warning', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'warning'\n"
				+ "                });\n" + "                return false;\n" + "            }\n" + "        },\n"
				+ "        handleError:function(err, file, fileList){\n" + "            var jsonerr;\n"
				+ "            \n" + "            try{\n" + "                jsonerr = JSON.parse(err.message);\n"
				+ "                this.msgerr = this.$t(jsonerr.message);\n"
				+ "                if(jsonerr.detail != ''){\n"
				+ "                    this.detailerr = JSON.stringify(jsonerr.detail);\n"
				+ "                    this.showDetailBtn = true;\n" + "                }   \n"
				+ "            } catch(objError){\n" + "                this.msgerr = err.name + \" \" + err.status;\n"
				+ "                this.detailerr = err.message;\n" + "                this.showDetailBtn = true;\n"
				+ "            }           \n" + "            this.dialogCreateVisible = true;\n" + "        },\n"
				+ "        showErrDetails: function(){\n" + "            if(this.showDetails === true){\n"
				+ "                this.showDetails = false;\n" + "            } else {\n" + "                try {\n"
				+ "                    var obj = JSON.parse(this.detailerr);\n"
				+ "                    this.detailerr = JSON.stringify(obj, undefined, 4);\n"
				+ "                    this.showDetails = true;\n" + "                } catch(objError){\n"
				+ "                    this.showDetails = true;\n" + "                }\n" + "            }\n"
				+ "        },\n" + "        closeErrDialog: function(){\n" + "            this.showDetails = false;\n"
				+ "            this.showDetailBtn = false;\n" + "            this.dialogCreateVisible = false;\n"
				+ "        },\n" + "        closeDialog: function() {\n"
				+ "            this.dialogDownloadOptions = false;\n" + "        },\n"
				+ "        handleSuccess: function(response, file, fileList){\n"
				+ "            if(response.message != '') {\n"
				+ "            this.$alert(this.$t(\"message.success.loaded.1\") +' \"' + file.name + '\" ' + this.$t(\"message.success.loaded.2\") + ' \\r\\n' + this.$t(\"message.success.inserted\") + response.message, 'Success', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'success'\n"
				+ "                });\n" + "            } else {\n"
				+ "                this.$alert(this.$t(\"message.success.loaded.1\") +' \"' + file.name + '\" ' + this.$t(\"message.success.loaded.2\"), 'Success', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'success'\n"
				+ "                });\n" + "            }\n" + "            this.$refs.upload.clearFiles();\n"
				+ "        },\n" + "        handlePreview: function(file){\n" + "        },\n"
				+ "        handleRemove: function(file, fileList){\n" + "        },\n"
				+ "        handleExceed: function(files, fileList){\n"
				+ "            this.$alert(this.$t(\"message.alert.onefile\"), 'Warning', {\n"
				+ "                    confirmButtonText: 'OK',\n" + "                    type: 'warning'\n"
				+ "                });\n" + "        },\n" + "        clearFiles: function(){\n"
				+ "            this.$refs.upload.clearFiles();\n" + "        },\n"
				+ "        sendValue: vm.sendValue,\n" + "        sendFilter: vm.sendFilter\n" + "    },\n"
				+ "    mounted() {\n" + "        if(vm.tparams && vm.tparams.parameters){\n"
				+ "            this.initialEntity=vm.tparams.parameters.initialEntity; \n" + "        }\n"
				+ "        \n" + "        this.loadOntologies();\n" + "        this.onChangeOntology();\n"
				+ "        this.importdisabled = true;\n" + "        this.downloaddisabled = true;\n"
				+ "        this.uploaddisabled = true;\n" + "    },\n" + "    i18n: window.i18n\n" + "}\n" + "\n"
				+ "//Init Vue app\n" + "vm.vueapp = new Vue(vm.vueconfig);");
		gadgetTemplate.setConfig(
				"{\"gform\":[{\"id\":1,\"type\":\"input-text\",\"name\":\"initialEntity\",\"default\":\"\",\"title\":\"initialEntity\"}]}");
		gadgetTemplate.setUser(getUserAdministrator());
		gadgetTemplateRepository.save(gadgetTemplate);
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

		// if (webProjectRepository.findByIdentification(CESIUM) == null) {
		//
		// loadWebProyect();
		//
		// final WebProject webProject = new WebProject();
		//
		// webProject.setId("MASTER-webproject-1");
		// webProject.setDescription("Stand Alone Library of Cesium 1.60 nad
		// CesiumHeatMap");
		// webProject.setIdentification(CESIUM);
		// webProject.setUser(getUserDeveloper());
		// webProject.setMainFile("Cesium1.60/Cesium.js");
		//
		// webProjectRepository.save(webProject);
		//
		// }

	}

	private void initCategories() {
		log.info("init Categories");

		if (categoryRepository.findAll().isEmpty()) {
			final Category category = new Category();
			category.setIdentification("GeneralCategory");
			category.setDescription("General Category Description");
			category.setType(Category.Type.GENERAL);
			categoryRepository.save(category);

			final Subcategory subcategory = new Subcategory();
			subcategory.setIdentification("GeneralSubcategory");
			subcategory.setDescription("General Subcategory Description");
			subcategory.setCategory(category);
			subcategoryRepository.save(subcategory);
		}
	}

	public void initTypology() {
		log.info("init Typologies");
		final List<ODTypology> typologies = typologyRepository.findAll();
		if (typologies.isEmpty()) {
			log.info("No typologies...adding");

			final ODTypology typology = new ODTypology();
			typology.setId("MASTER-Typology-1");
			typology.setIdentification("TypologyExample");
			typology.setDescription("Typology example");
			typology.setUser(getUserAdministrator());

			typologyRepository.save(typology);
		}
	}

	public void initTypologyDataset() {
		log.info("init Typology-Dataset");
		final List<ODTypologyDataset> typologies = typologyDatasetRepository.findAll();
		if (typologies.isEmpty()) {
			log.info("No typology-dataset...adding");

			final ODTypologyDataset typology = new ODTypologyDataset();
			typology.setId("MASTER-Typology-Dataset-1");
			typology.setDatasetId("21ebc28f-b967-46e5-a8f6-0e977dee72fb");
			typology.setTypologyId("MASTER-Typology-1");

			typologyDatasetRepository.save(typology);
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
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					log.debug("Error: " + e);
				}
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

	@Autowired
	OntologyPrestoDatasourceRepository prestoDatasourceRepository;

	@Value("${onesaitplatform.database.prestodb.historicalCatalog:minio}")
	private String historicalCatalog;
	@Value("${onesaitplatform.database.prestodb.realtimedbCatalog:realtimedb}")
	private String realtimedbCatalog;

	private void initPrestoConnections() {
		if (prestoDatasourceRepository.findByIdentification(historicalCatalog) == null) {
			initPrestoConnection(historicalCatalog, PrestoDatasourceType.HIVE);
		}

		if (prestoDatasourceRepository.findByIdentification(realtimedbCatalog) == null) {
			initPrestoConnection(realtimedbCatalog, PrestoDatasourceType.MONGODB);
		}
	}

	private void initPrestoConnection(String catalogIdentification,
			OntologyPrestoDatasource.PrestoDatasourceType type) {
		final OntologyPrestoDatasource prestoConnection = new OntologyPrestoDatasource();
		prestoConnection.setIdentification(catalogIdentification);
		prestoConnection.setType(type);
		prestoConnection.setUser(getUserAdministrator());
		prestoConnection.setPublic(true);
		prestoDatasourceRepository.save(prestoConnection);
	}

}
