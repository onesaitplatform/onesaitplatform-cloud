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
package com.minsait.onesait.platform.config.versioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.DataflowInstance;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetFavorite;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.model.MapsMap;
import com.minsait.onesait.platform.config.model.MapsProject;
import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyAI;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.AppRepository;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformInstanceSimulationRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.DataflowInstanceRepository;
import com.minsait.onesait.platform.config.repository.DroolsRuleRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetFavoriteRepository;
import com.minsait.onesait.platform.config.repository.GadgetMeasureRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateTypeRepository;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.MapsLayerRepository;
import com.minsait.onesait.platform.config.repository.MapsMapRepository;
import com.minsait.onesait.platform.config.repository.MapsProjectRepository;
import com.minsait.onesait.platform.config.repository.MapsStyleRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.OntologyAIRepository;
import com.minsait.onesait.platform.config.repository.OntologyKPIRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.OntologyTimeSeriesRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.ReportRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;

@Component
public class VersioningRepositoryFacade {

	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private DashboardConfRepository dashboardConfRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private GadgetFavoriteRepository gadgetFavoriteRepository;
	@Autowired
	private GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;
	@Autowired
	private GadgetTemplateTypeRepository gadgetTemplateTypeRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private BinaryFileRepository binaryFileRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private ClientPlatformInstanceSimulationRepository clientPlatformInstanceSimulationRepository;
	@Autowired
	private ConfigurationRepository configurationRepository;
	@Autowired
	private PipelineRepository pipelineRepository;
	@Autowired
	private DataflowInstanceRepository dataflowInstanceRepository;
	@Autowired
	private WebProjectRepository webProjectRepository;
	@Autowired
	private NotebookRepository notebookRepository;
	@Autowired
	private FlowDomainRepository flowDomainRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private AppRepository appRepository;
	@Autowired
	private OntologyKPIRepository ontologyKPIRepository;
	@Autowired
	private OntologyTimeSeriesRepository ontologyTimeSeriesRepository;
	@Autowired
	private DataModelRepository dataModelRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ReportRepository reportRepository;
	@Autowired
	private LayerRepository layerRepository;
	@Autowired
	private ViewerRepository viewerRepository;
	@Autowired
	private DroolsRuleRepository droolsRuleRepository;
	@Autowired
	private OntologyAIRepository ontologyAIRepository;
	@Autowired
	private MapsMapRepository mapsMapRepository;
	@Autowired
	private MapsLayerRepository mapsLayerRepository;
	@Autowired
	private MapsStyleRepository mapsStyleRepository;
	@Autowired
	private MapsProjectRepository mapsProjectRepository;

	private static final String USER = "User";
	private static final String DASHBOARD = "Dashboard";
	private static final String DASHBOARD_CONF = "DashboardConf";
	private static final String GADGET = "Gadget";
	private static final String GADGET_DATASOURCE = "GadgetDatasource";
	private static final String GADGET_FAVORITE = "GadgetFavorite";
	private static final String GADGET_MEASURE = "GadgetMeasure";
	private static final String GADGET_TEMPLATE = "GadgetTemplate";
	private static final String GADGET_TEMPLATE_TYPE = "GadgetTemplateType";
	private static final String ONTOLOGY = "Ontology";
	private static final String ONTOLOGY_KPI = "OntologyKPI";
	private static final String ONTOLOGY_TIMESERIES = "OntologyTimeSeries";
	private static final String API = "Api";
	private static final String BINARY_FILE = "BinaryFile";
	private static final String CLIENT_PLATFORM = "ClientPlatform";
	private static final String CLIENT_PLATFORM_INSTANCE_SIMULATION = "ClientPlatformInstanceSimulation";
	private static final String CONFIGURATION = "Configuration";
	private static final String PIPELINE_DATAFLOW = "Pipeline";
	private static final String DATAFLOW_INSTANCE = "DataflowInstance";
	private static final String WEB_PROJECT = "WebProject";
	private static final String NOTEBOOK = "Notebook";
	private static final String FLOW_DOMAIN = "FlowDomain";
	private static final String PROJECT = "Project";
	private static final String APP = "App";
	private static final String DATA_MODEL = "DataModel";
	private static final String REPORT = "Report";
	private static final String LAYER = "Layer";
	private static final String VIEWER = "Viewer";
	private static final String DROOLS_RULE = "DroolsRule";
	private static final String ONTOLOGY_AI = "OntologyAI";
	private static final String MAPS_MAP = "MapsMap";
	private static final String MAPS_PROJECT = "MapsProject";
	private static final String MAPS_LAYER = "MapsLayer";
	private static final String MAPS_STYLE = "MapsStyle";

	public static final List<String> PRIORITY_PROCESSING;
	static {
		final List<String> list = new ArrayList<>();
		list.add(USER);
		list.add(DATA_MODEL);
		list.add(ONTOLOGY);
		list.add(DATAFLOW_INSTANCE);
		list.add(GADGET_TEMPLATE);
		list.add(GADGET_DATASOURCE);
		list.add(GADGET);
		list.add(CLIENT_PLATFORM);
		list.add(FLOW_DOMAIN);
		list.add(API);
		list.add(APP);
		list.add(BINARY_FILE);
		list.add(LAYER);
		PRIORITY_PROCESSING = Collections.unmodifiableList(list);
	}

	@SuppressWarnings("unchecked")
	public <R extends JpaRepository<T, I>, T, I> R getJpaRepository(T versionable) {
		switch (versionable.getClass().getSimpleName()) {
		case USER:
			return (R) userRepository;
		case DASHBOARD:
			return (R) dashboardRepository;
		case DASHBOARD_CONF:
			return (R) dashboardConfRepository;
		case GADGET:
			return (R) gadgetRepository;
		case GADGET_DATASOURCE:
			return (R) gadgetDatasourceRepository;
		case GADGET_FAVORITE:
			return (R) gadgetFavoriteRepository;
		case GADGET_MEASURE:
			return (R) gadgetMeasureRepository;
		case GADGET_TEMPLATE:
			return (R) gadgetTemplateRepository;
		case GADGET_TEMPLATE_TYPE:
			return (R) gadgetTemplateTypeRepository;
		case ONTOLOGY:
			return (R) ontologyRepository;
		case ONTOLOGY_KPI:
			return (R) ontologyKPIRepository;
		case ONTOLOGY_TIMESERIES:
			return (R) ontologyTimeSeriesRepository;
		case API:
			return (R) apiRepository;
		case BINARY_FILE:
			return (R) binaryFileRepository;
		case CLIENT_PLATFORM:
			return (R) clientPlatformRepository;
		case CLIENT_PLATFORM_INSTANCE_SIMULATION:
			return (R) clientPlatformInstanceSimulationRepository;
		case CONFIGURATION:
			return (R) configurationRepository;
		case PIPELINE_DATAFLOW:
			return (R) pipelineRepository;
		case DATAFLOW_INSTANCE:
			return (R) dataflowInstanceRepository;
		case WEB_PROJECT:
			return (R) webProjectRepository;
		case NOTEBOOK:
			return (R) notebookRepository;
		case FLOW_DOMAIN:
			return (R) flowDomainRepository;
		case PROJECT:
			return (R) projectRepository;
		case APP:
			return (R) appRepository;
		case DATA_MODEL:
			return (R) dataModelRepository;
		case REPORT:
			return (R) reportRepository;
		case LAYER:
			return (R) layerRepository;
		case VIEWER:
			return (R) viewerRepository;
		case DROOLS_RULE:
			return (R) droolsRuleRepository;
		case ONTOLOGY_AI:
			return (R) ontologyAIRepository;
		case MAPS_MAP:
			return (R) mapsMapRepository;
		case MAPS_LAYER:
			return (R) mapsLayerRepository;
		case MAPS_STYLE:
			return (R) mapsStyleRepository;
		case MAPS_PROJECT:
			return (R) mapsProjectRepository;
		default:
			throw new VersioningException(
					"Entity of type " + versionable.getClass().getSimpleName() + " not configured");

		}
	}

	@SuppressWarnings("unchecked")
	public <S> S save(Versionable<S> versionable) {
		switch (versionable.getClass().getSimpleName()) {
		case USER:
			return (S) userRepository.save((User) versionable);
		case DASHBOARD:
			return (S) dashboardRepository.save((Dashboard) versionable);
		case DASHBOARD_CONF:
			return (S) dashboardConfRepository.save((DashboardConf) versionable);
		case GADGET:
			return (S) gadgetRepository.save((Gadget) versionable);
		case GADGET_DATASOURCE:
			return (S) gadgetDatasourceRepository.save((GadgetDatasource) versionable);
		case GADGET_FAVORITE:
			return (S) gadgetFavoriteRepository.save((GadgetFavorite) versionable);
		case GADGET_MEASURE:
			return (S) gadgetMeasureRepository.save((GadgetMeasure) versionable);
		case GADGET_TEMPLATE:
			return (S) gadgetTemplateRepository.save((GadgetTemplate) versionable);
		case GADGET_TEMPLATE_TYPE:
			return (S) gadgetTemplateTypeRepository.save((GadgetTemplateType) versionable);
		case ONTOLOGY:
			return (S) ontologyRepository.save((Ontology) versionable);
		case ONTOLOGY_KPI:
			return (S) ontologyKPIRepository.save((OntologyKPI) versionable);
		case ONTOLOGY_TIMESERIES:
			return (S) ontologyTimeSeriesRepository.save((OntologyTimeSeries) versionable);
		case API:
			return (S) apiRepository.save((Api) versionable);
		case BINARY_FILE:
			return (S) binaryFileRepository.save((BinaryFile) versionable);
		case CLIENT_PLATFORM:
			return (S) clientPlatformRepository.save((ClientPlatform) versionable);
		case CLIENT_PLATFORM_INSTANCE_SIMULATION:
			return (S) clientPlatformInstanceSimulationRepository.save((ClientPlatformInstanceSimulation) versionable);
		case CONFIGURATION:
			return (S) configurationRepository.save((Configuration) versionable);
		case PIPELINE_DATAFLOW:
			return (S) pipelineRepository.save((Pipeline) versionable);
		case DATAFLOW_INSTANCE:
			return (S) dataflowInstanceRepository.save((DataflowInstance) versionable);
		case WEB_PROJECT:
			return (S) webProjectRepository.save((WebProject) versionable);
		case NOTEBOOK:
			return (S) notebookRepository.save((Notebook) versionable);
		case FLOW_DOMAIN:
			return (S) flowDomainRepository.save((FlowDomain) versionable);
		case PROJECT:
			return (S) projectRepository.save((Project) versionable);
		case APP:
			return (S) appRepository.save((App) versionable);
		case DATA_MODEL:
			return (S) dataModelRepository.save((DataModel) versionable);
		case REPORT:
			return (S) reportRepository.save((Report) versionable);
		case LAYER:
			return (S) layerRepository.save((Layer) versionable);
		case VIEWER:
			return (S) viewerRepository.save((Viewer) versionable);
		case DROOLS_RULE:
			return (S) droolsRuleRepository.save((DroolsRule) versionable);
		case ONTOLOGY_AI:
			return (S) ontologyAIRepository.save((OntologyAI) versionable);
		case MAPS_MAP:
			return (S) mapsMapRepository.save((MapsMap) versionable);
		case MAPS_LAYER:
			return (S) mapsLayerRepository.save((MapsLayer) versionable);
		case MAPS_STYLE:
			return (S) mapsStyleRepository.save((MapsStyle) versionable);
		case MAPS_PROJECT:
			return (S) mapsProjectRepository.save((MapsProject) versionable);
		default:
			throw new VersioningException(
					"Entity of type " + versionable.getClass().getSimpleName() + " not configured");

		}
	}
}
