/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;
import com.minsait.onesait.platform.config.model.OntologyKPI;
import com.minsait.onesait.platform.config.model.OntologyRestHeaders;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.RetentionUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.WindowType;
import com.minsait.onesait.platform.config.model.OntologyUserAccess;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyKPIDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.DataModelDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.KpiDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyCreateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyKpiDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyRestDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyRestHeaderDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyTimeSeriesDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyTimeSeriesPropertyDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyTimeSeriesWindowDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyUserAccessSimplified;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyVirtualDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.ontology.model.OntologyVirtualDataSourceDTO;

import edu.emory.mathcs.backport.java.util.Collections;
import lombok.Getter;

@Component
public class OntologyDTOConverter {

	@Autowired
	UserService userService;
	@Autowired
	OntologyService ontologyService;
	@Autowired
	OntologyBusinessService ontologyBusinessService;
	@Autowired
	OntologyTimeSeriesService ontologyTimeSeriesService;
	@Autowired
	DataModelService dataModelService;

	@Getter
	ObjectMapper mapper;

	@PostConstruct
	private void init() {
		mapper = new ObjectMapper();
	}

	public Date parseDateTime(String dateString) throws ParseException {
		return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dateString);
	}

	public OntologyDTO jsonStringToOntologyDTO(String jsonString) throws IOException {
		return mapper.readValue(jsonString, OntologyDTO.class);
	}

	public Object ontologyDTOToAnyOntologyDTO(OntologyDTO ontologyDTO, Class<?> ontlogyClass) {

		return mapper.convertValue(ontologyDTO, ontlogyClass);
	}

	public boolean canConvert(String jsonString, Class<?> ontlogyClass) {
		try {
			mapper.readValue(jsonString, ontlogyClass);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean canConvert(OntologyDTO ontologyDTO, Class<?> ontlogyClass) {
		try {
			mapper.convertValue(ontologyDTO, ontlogyClass);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Ontology ontologyCreateDTOToOntology(OntologyCreateDTO ontologyCreate, User user) {
		Ontology ontology = new Ontology();

		ontology.setUser(user);
		ontology.setIdentification(ontologyCreate.getIdentification());
		ontology.setDescription(ontologyCreate.getDescription());
		ontology.setMetainf(ontologyCreate.getMetainf());
		ontology.setActive(ontologyCreate.isActive());
		ontology.setPublic(ontologyCreate.isPublic());
		ontology.setAllowsCypherFields(ontologyCreate.isAllowsCypherFields());
		ontology.setJsonSchema(ontologyCreate.getJsonSchema());
		ontology.setRtdbClean(ontologyCreate.isRtdbClean());
		ontology.setRtdbCleanLapse(ontologyCreate.getRtdbCleanLapse());
		if (ontologyCreate.getRtdbDatasource() == null) {
			ontology.setRtdbDatasource(RtdbDatasource.MONGO);
		} else {
			ontology.setRtdbDatasource(ontologyCreate.getRtdbDatasource());
		}
		ontology.setRtdbToHdb(ontologyCreate.isRtdbToHdb());
		if (ontologyCreate.getRtdbToHdbStorage() == null) {
			ontology.setRtdbToHdbStorage(RtdbToHdbStorage.MONGO_GRIDFS);
		} else {
			ontology.setRtdbToHdbStorage(ontologyCreate.getRtdbToHdbStorage());
		}
		ontology.setAllowsCreateNotificationTopic(ontologyCreate.isAllowsCreateNotificationTopic());

		return ontology;
	}

	public Ontology ontologyDTOToOntology(OntologyDTO ontologyDTO, User user) {
		Ontology ontology = new Ontology();

		ontology.setUser(user);
		ontology.setIdentification(ontologyDTO.getIdentification());
		ontology.setDescription(ontologyDTO.getDescription());
		ontology.setMetainf(ontologyDTO.getMetainf());
		ontology.setActive(ontologyDTO.isActive());
		ontology.setPublic(ontologyDTO.isPublic());
		ontology.setAllowsCypherFields(ontologyDTO.isAllowsCypherFields());
		ontology.setJsonSchema(ontologyDTO.getJsonSchema());
		ontology.setRtdbClean(ontologyDTO.isRtdbClean());
		if (ontologyDTO.getRtdbCleanLapse() == null)
			ontology.setRtdbCleanLapse(RtdbCleanLapse.NEVER);
		else
			ontology.setRtdbCleanLapse(ontologyDTO.getRtdbCleanLapse());
		if (ontologyDTO.getRtdbDatasource() == null) {
			ontology.setRtdbDatasource(RtdbDatasource.MONGO);
		} else {
			ontology.setRtdbDatasource(ontologyDTO.getRtdbDatasource());
		}
		ontology.setRtdbToHdb(ontologyDTO.isRtdbToHdb());
		if (ontologyDTO.getRtdbToHdbStorage() == null) {
			ontology.setRtdbToHdbStorage(RtdbToHdbStorage.MONGO_GRIDFS);
		} else {
			ontology.setRtdbToHdbStorage(ontologyDTO.getRtdbToHdbStorage());
		}
		ontology.setDataModel(datamodelDTOToDataModel(ontologyDTO.getDataModel()));
		ontology.setAllowsCreateNotificationTopic(ontologyDTO.isAllowsCreateNotificationTopic());
		ontology.setAllowsCreateTopic(ontologyDTO.isAllowsCreateTopic());

		return ontology;
	}

	public DataModel datamodelDTOToDataModel(DataModelDTO dataModelDTO) {
		DataModel datamodel = new DataModel();

		datamodel.setUser(userService.getUser(dataModelDTO.getUser()));
		datamodel.setJsonSchema(dataModelDTO.getJsonSchema());
		datamodel.setIdentification(dataModelDTO.getName());
		datamodel.setType(dataModelDTO.getType());
		datamodel.setTypeEnum(DataModel.MainType.valueOf(dataModelDTO.getType()));
		datamodel.setDescription(dataModelDTO.getDescription());
		datamodel.setLabels(dataModelDTO.getLabels());

		return datamodel;
	}

	public List<OntologyUserAccessSimplified> ontologyUserAccessesToOntologyUserAccessSimplified(
			Set<OntologyUserAccess> authorizations) {
		List<OntologyUserAccessSimplified> autorizationsDTO = new ArrayList<>();
		for (OntologyUserAccess userac : authorizations) {
			autorizationsDTO.add(new OntologyUserAccessSimplified(userac));
		}

		Collections.sort(autorizationsDTO, new Comparator<OntologyUserAccessSimplified>() {
			@Override
			public int compare(OntologyUserAccessSimplified s1, OntologyUserAccessSimplified s2) {
				return s1.getUserId().compareToIgnoreCase(s2.getUserId());
			}
		});

		return autorizationsDTO;
	}

	public Ontology ontologyKPIDTOToOntology(OntologyKPIDTO ontologyKPIDTO, User user) throws IOException {
		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(ontologyBusinessService
				.completeSchema(ontologyKPIDTO.getSchema(), ontologyKPIDTO.getName(), ontologyKPIDTO.getDescription())
				.toString());
		ontology.setIdentification(ontologyKPIDTO.getName());
		ontology.setActive(ontologyKPIDTO.isActive());
		ontology.setPublic(ontologyKPIDTO.isPublic());
		ontology.setDataModel(dataModelService.getDataModelByName(OntologyService.DATAMODEL_DEFAULT_NAME));
		ontology.setDescription(ontologyKPIDTO.getDescription());
		ontology.setUser(user);
		ontology.setMetainf(ontologyKPIDTO.getMetainf());
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf(ontologyKPIDTO.getDatasource()));
		return ontology;
	}

	public OntologyKPI ontologyKPIDTOToOntologyKPI(OntologyKPIDTO ontologyKPIDTO, Ontology ontology, User user) {
		final OntologyKPI oKPI = new OntologyKPI();
		oKPI.setCron(ontologyKPIDTO.getCron());
		oKPI.setDateFrom(ontologyKPIDTO.getDateFrom());
		oKPI.setDateTo(ontologyKPIDTO.getDateTo());
		oKPI.setActive(Boolean.FALSE);
		oKPI.setOntology(ontology);
		oKPI.setQuery(ontologyKPIDTO.getQuery());
		oKPI.setUser(user);
		oKPI.setPostProcess(ontologyKPIDTO.getPostProcess());
		return oKPI;
	}

	public OntologyTimeSeriesProperty ontologyTimeSeriesPropertyDTOToOntologyTimeSeriesProperty(Ontology ontology,
			OntologyTimeSeriesPropertyDTO propertyDTO) {
		OntologyTimeSeriesProperty property = new OntologyTimeSeriesProperty();
		property.setOntologyTimeSeries(ontologyTimeSeriesService
				.getOntologyByOntology(ontologyService.getOntologyByIdentification(ontology.getIdentification())));
		property.setPropertyDataType(propertyDTO.getPropertyDataType());
		property.setPropertyName(propertyDTO.getPropertyName());
		property.setPropertyType(propertyDTO.getPropertyType());

		return property;
	}

	public List<OntologyTimeSeriesProperty> ontologyTimeSeriesPropertesDTOToOntologyTimeSeriesProperties(
			Ontology ontology, List<OntologyTimeSeriesPropertyDTO> propertiesDTO) {
		List<OntologyTimeSeriesProperty> properties = new ArrayList<>();
		for (OntologyTimeSeriesPropertyDTO propDTO : propertiesDTO) {
			properties.add(ontologyTimeSeriesPropertyDTOToOntologyTimeSeriesProperty(ontology, propDTO));
		}

		return properties;
	}

	public OntologyTimeSeriesWindow ontologyTimeSeriesWindowDTOToOntologyTimeSeriesWindow(Ontology ontology,
			OntologyTimeSeriesWindowDTO windowsDTO) {
		OntologyTimeSeriesWindow window = new OntologyTimeSeriesWindow();
		window.setOntologyTimeSeries(ontologyTimeSeriesService
				.getOntologyByOntology(ontologyService.getOntologyByIdentification(ontology.getIdentification())));
		window.setAggregationFunction(windowsDTO.getAggregationFunction());
		window.setBdh(windowsDTO.isBdh());
		window.setFrecuency(windowsDTO.getFrecuency());
		window.setFrecuencyUnit(windowsDTO.getFrecuencyUnit());
		window.setRetentionBefore(windowsDTO.getRetentionBefore());
		window.setRetentionUnit(RetentionUnit.valueOf(windowsDTO.getRetentionUnit()));
		window.setWindowType(WindowType.valueOf(windowsDTO.getWindowType()));

		return window;
	}

	public List<OntologyTimeSeriesWindow> ontologyTimeSeriesWindowsDTOToOntologyTimeSeriesWindows(Ontology ontology,
			List<OntologyTimeSeriesWindowDTO> windowsDTO) {
		List<OntologyTimeSeriesWindow> windows = new ArrayList<>();
		for (OntologyTimeSeriesWindowDTO windowDTO : windowsDTO) {
			windows.add(ontologyTimeSeriesWindowDTOToOntologyTimeSeriesWindow(ontology, windowDTO));
		}

		return windows;
	}

	public OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTOToOntologyTimeSeriesServiceDTO(
			OntologyTimeSeriesDTO ontology, User user) {
		OntologyTimeSeriesServiceDTO seriesDTO = new OntologyTimeSeriesServiceDTO();
		seriesDTO.setActive(ontology.isActive());
		seriesDTO.setIdentification(ontology.getIdentification());
		seriesDTO.setAllowsCypherFields(ontology.isAllowsCypherFields());
		seriesDTO.setDescription(ontology.getDescription());
		seriesDTO.setJsonSchema(ontology.getJsonSchema());
		seriesDTO.setPublic(ontology.isPublic());
		seriesDTO.setMetainf(ontology.getMetainf());
		seriesDTO.setRtdbDatasource(ontology.getRtdbDatasource().toString());
		seriesDTO.setDataModel(datamodelDTOToDataModel(ontology.getDataModel()));
		seriesDTO.setOntologyKPI(null);
		seriesDTO.setRtdbClean(false);
		seriesDTO.setRtdbToHdb(ontology.isRtdbToHdb());
		seriesDTO.setAllowsCreateTopic(ontology.isAllowsCreateTopic());
		seriesDTO.setUser(user);
		seriesDTO.setTags(new String[] {});
		seriesDTO.setFieldnames(new String[] {});
		seriesDTO.setWindowtypes(new String[] {});
//		Ontology ont = ontologyService.getOntologyByIdentification(ontology.getIdentification());
//		seriesDTO.setTimeSeriesProperties(
//				new HashSet<>(ontologyTimeSeriesPropertesDTOToOntologyTimeSeriesProperties(ont, ontology.getProperties())));
//		seriesDTO.setTimeSeriesWindow(
//				new HashSet<>(ontologyTimeSeriesWindowsDTOToOntologyTimeSeriesWindows(ont, ontology.getWindows())));

		Set<OntologyTimeSeriesProperty> properties = new HashSet<>();
		for (OntologyTimeSeriesPropertyDTO propertyDTO : ontology.getProperties()) {
			OntologyTimeSeriesProperty property = new OntologyTimeSeriesProperty();
			property.setPropertyDataType(propertyDTO.getPropertyDataType());
			property.setPropertyName(propertyDTO.getPropertyName());
			property.setPropertyType(propertyDTO.getPropertyType());
			properties.add(property);
		}
		seriesDTO.setTimeSeriesProperties(properties);

		Set<OntologyTimeSeriesWindow> windows = new HashSet<>();
		for (OntologyTimeSeriesWindowDTO windowsDTO : ontology.getWindows()) {
			OntologyTimeSeriesWindow window = new OntologyTimeSeriesWindow();
			window.setAggregationFunction(windowsDTO.getAggregationFunction());
			window.setBdh(windowsDTO.isBdh());
			window.setFrecuency(windowsDTO.getFrecuency());
			window.setFrecuencyUnit(windowsDTO.getFrecuencyUnit());
			window.setRetentionBefore(windowsDTO.getRetentionBefore());
			if (windowsDTO.getRetentionUnit() != null) {
				window.setRetentionUnit(RetentionUnit.valueOf(windowsDTO.getRetentionUnit()));
			}
			window.setWindowType(WindowType.valueOf(windowsDTO.getWindowType()));
			windows.add(window);
		}
		seriesDTO.setTimeSeriesWindow(windows);

		return seriesDTO;
	}

	public List<KpiDTO> ontologyKpisToListOntologyKpisDTO(List<OntologyKPI> kpis) {
		List<KpiDTO> kpisDTO = new ArrayList<>();
		for (OntologyKPI kpi : kpis) {
			kpisDTO.add(new KpiDTO(kpi));
		}
		return kpisDTO;
	}

	public OntologyKPIDTO ontologyKpiDTOToOntologyKPIDTO(OntologyKpiDTO ontologyKpisDTO) throws ParseException {
		OntologyKPIDTO ontologyServiceDTO = new OntologyKPIDTO();
		ontologyServiceDTO.setDatasource(RtdbDatasource.MONGO.name());
		ontologyServiceDTO.setActive(ontologyKpisDTO.isActive());
		ontologyServiceDTO.setAllowsCypherFields(ontologyKpisDTO.isAllowsCypherFields());
		ontologyServiceDTO.setSchema(ontologyKpisDTO.getJsonSchema());
		ontologyServiceDTO.setDescription(ontologyKpisDTO.getDescription());
		ontologyServiceDTO.setMetainf(ontologyKpisDTO.getMetainf());
		ontologyServiceDTO.setName(ontologyKpisDTO.getIdentification());
		ontologyServiceDTO.setPublic(ontologyKpisDTO.isPublic());
		if (ontologyKpisDTO.getKpi().getDateFrom() != null) {
			ontologyServiceDTO.setDateFrom(parseDateTime(ontologyKpisDTO.getKpi().getDateFrom()));
		} else {
			ontologyServiceDTO.setDateFrom(null);
		}
		if (ontologyKpisDTO.getKpi().getDateTo() != null) {
			ontologyServiceDTO.setDateTo(parseDateTime(ontologyKpisDTO.getKpi().getDateTo()));
		} else {
			ontologyServiceDTO.setDateTo(null);
		}
		ontologyServiceDTO.setJobName(ontologyKpisDTO.getKpi().getJobName());
		ontologyServiceDTO.setNewOntology(true);
		ontologyServiceDTO.setPostProcess(ontologyKpisDTO.getKpi().getPostProcess());
		ontologyServiceDTO.setQuery(ontologyKpisDTO.getKpi().getQuery());
		ontologyServiceDTO.setCron(ontologyKpisDTO.getKpi().getCron());

		return ontologyServiceDTO;
	}

	public List<OntologyRestHeaderDTO> ontologyRestHeadersToListOntologyRestHeaderDTO(OntologyRestHeaders headers) {
		List<OntologyRestHeaderDTO> headersDTO = new ArrayList<>();
		JsonArray jsonArray = new JsonParser().parse(headers.getConfig()).getAsJsonArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			headersDTO.add(new OntologyRestHeaderDTO(jsonArray.get(i).getAsJsonObject().toString()));
		}
		return headersDTO;
	}

	public OntologyVirtualDatasource ontologyVirtualDatasourceDTOToOntologyVirtualDataSource(
			OntologyVirtualDataSourceDTO datasourceDTO, User user) {
		OntologyVirtualDatasource datasource = new OntologyVirtualDatasource();
		datasource.setUser(user);
		datasource.setUserId(datasourceDTO.getUser());
		datasource.setConnectionString(datasourceDTO.getConnectionString());
		datasource.setCredentials(datasourceDTO.getCredentials());
		datasource.setIdentification(datasourceDTO.getName());
		datasource.setDatasourceDomain(datasourceDTO.getDomain());
		datasource.setPublic(datasourceDTO.isPublic());
		datasource.setPoolSize(datasourceDTO.getPoolSize());
		datasource.setQueryLimit(datasourceDTO.getQueryLimit());
		datasource.setSgdb(datasourceDTO.getSgbd());

		return datasource;
	}

	public OntologyConfiguration ontologyRestDTOToOntologyConfiguration(OntologyRestDTO ontologyDTO) {
		OntologyConfiguration configuration = new OntologyConfiguration();

		configuration.setBaseUrl(ontologyDTO.getBaseUrl());
		configuration.setSchema(ontologyDTO.getJsonSchema());
		String[] hds = { ontologyDTO.headersToJsonArray().toString() };
		configuration.setHeaders(hds);
		if (ontologyDTO.getSecurityType() != null) {
			configuration.setAuthMethod(ontologyDTO.getSecurityType().name());
		} else {
			configuration.setAuthMethod(null);
		}
		configuration.setSwagger(ontologyDTO.getSwaggerUrl());
		String[] ops = { ontologyDTO.operationsToJsonArray().toString() };
		configuration.setOperations(ops);
		configuration.setHeader(ontologyDTO.getSecurity().getHeader());
		configuration.setToken(ontologyDTO.getSecurity().getToken());
		configuration.setBasicUser(ontologyDTO.getSecurity().getUser());
		configuration.setBasicPass(ontologyDTO.getSecurity().getPassword());
		configuration.setOauthUser(ontologyDTO.getSecurity().getUser());
		configuration.setOauthPass(ontologyDTO.getSecurity().getPassword());

		return configuration;
	}

	public OntologyConfiguration ontologyTimeSeriesDTOToOntologyConfiguration(OntologyTimeSeriesDTO ontologyDTO) {
		// for future logic
		return new OntologyConfiguration();
	}

	public OntologyConfiguration ontologyVirtualDTOToOntologyConfiguration(OntologyVirtualDTO ontologyDTO,
			String datasourceId) {
		OntologyConfiguration configuration = new OntologyConfiguration();
		configuration.setDatasource(ontologyDTO.getDatasource().getName());
		configuration.setObjectId(datasourceId);
		configuration.setAllowsCreateTable(ontologyDTO.isAllowsCreateTable());
		configuration.setSqlStatement(ontologyDTO.getSqlStatement());
		return configuration;
	}

	public OntologyConfiguration ontologyKpiDTOToOntologyConfiguration(OntologyKPIDTO ontologyDTO) {
		// for future logic
		return new OntologyConfiguration();
	}

}
