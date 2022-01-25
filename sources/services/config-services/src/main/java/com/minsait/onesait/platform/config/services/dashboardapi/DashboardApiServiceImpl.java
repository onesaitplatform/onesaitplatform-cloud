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
package com.minsait.onesait.platform.config.services.dashboardapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.CommandDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.DataDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.FilterDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.FiltersDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.GadgetConfDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.GadgetDatasourceDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.MeasureDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.OrderByDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.ResponseDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.ResponseSynopticDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.SetupLayout;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.TargetDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.WhereDTO;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
import com.minsait.onesait.platform.config.services.gadget.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardApiServiceImpl implements DashboardApiService {

	@Autowired
	private GadgetService gadgetService;

	@Autowired
	private UserService userService;

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private GadgetTemplateService gadgetTemplateService;

	@Autowired
	private OntologyService ontologyService;

	private final String SELECT_FROM = "select * from ";
	private final String RTDB = "RTDB";
	private final String QUERY = "query";
	private final String PIE = "pie";
	private final String BAR = "bar";
	private final String LINE = "line";
	private final String MIXED = "mixed";
	private final String RADAR = "radar";
	private final String WORDCLOUD = "wordcloud";
	private final String TABLE = "table";
	private final String MAP = "map";
	private final String LIVEHTML = "livehtml";
	private final int MAXVALUES = 1000;
	private final String[] COLORS = { "rgba(40,146,215, 0.8)", "rgba(119,178,131, 0.8)", "rgba(178,131,119, 0.8)",
			"rgba(178,161,119, 0.8)", "rgba(247,179,121, 0.8)", "rgba(139,165,160, 0.8)", "rgba(254, 246, 240, 0.8)",
			"rgba(207, 206, 229, 0.8)" };
	private static String AND = " and ";
	private static String COMA = " , ";
	private static String GROUPBY = " group by ";
	private static String ORDERBY = " order by ";
	private static String WHERE = " where ";
	private static String LIMIT = " limit ";
	private static String FROM = " from ";
	private static String SELECT = "select ";
	private static String NONE = "NONE";
	private static String AS_RESULT = " as result";
	private static String SUM = "SUM";
	private static String MIN = "MIN";
	private static String MAX = "MAX";
	private static String AVG = "AVG";
	private static String COUNT = "COUNT";
	private static String LASTVALUE = "LASTVALUE";

	private final String SYNOPTICDATASOURCE = "scada";
	private final String NEWGADGET = "newGadget";
	private final String OK = "OK";
	private final String PROPERLY_CREATED_GADGET = "properly created gadget";
	private final String LIVEFILTER = "livefilter";
	private final String MULTISELECTFILTER = "multiselectfilter";
	// datasources
	private final String MULTISELECTDSFILTER = "multiselectdsfilter";
	private final String MULTISELECTNUMBERDSFILTER = "multiselectnumberdsfilter";
	private final String SIMPLESELECTDSFILTER = "simpleselectdsfilter";
	private final String SIMPLESELECTNUMBERDSFILTER = "simpleselectnumberdsfilter";

	private final String TEXTFILTER = "textfilter";
	private final String NUMBERFILTER = "numberfilter";
	private final String FILTER_NOT_DEFINED = "Filter not defined";
	private final String FILTER = "filter";
	private final String IN = "IN";
	private final String START = "start";
	private final String START_DATE = "NOW(\"yyyy-MM-dd\'T\'HH:mm:ss\'Z\'\",\"hour\",-8)";
	private final String END_DATE = "NOW(\"yyyy-MM-dd\'T\'HH:mm:ss\'Z\'\",\"hour\",0)";
	private final String SETSYNOPTICELEMENTDATASOURCE = "setSynopticElementDataSource";
	private final String CONFIG_GADGET_TABLE = "{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\",\"trHeightFooter\":\"40\",\"textColorTHead\":\"#060e14\",\"textColorBody\":\"#555555\",\"textColorFooter\":\"#555555\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}";
	private final String GADGET_INITIAL_COORDINATES = "{\"center\":{\"lat\":31.952162238024975,\"lng\":5.625,\"zoom\":2},\"markersFilter\":\"identifier\",\"jsonMarkers\":\"\"}";
	private final String GADGET_MEASURE_INITIAL = "{\"fields\":[\"latitude\",\"longitude\",\"identifier\",\"name\"],\"name\":\"\",\"config\":{}}";
	private final String CONFIG_GADGET_MIX = "{\"legend\":{\"display\":false,\"fullWidth\":false,\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,\"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":10},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":true,\"ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\"labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"2\",\"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",\"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}";
	private final String CONFIG_GADGET_LB = "{\"legend\":{\"display\":true,\"fullWidth\":false,\"position\":\"top\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,\"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":\"10\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":true,\"ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\"labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"2\",\"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",\"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}";
	private final String CONFIG_GADGET_PIE = "{\"legend\":{\"display\":true,\"fullWidth\":false,\"position\":\"left\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":1}},\"elements\":{\"arc\":{\"borderWidth\":1,"
			+ "\"borderColor\":\"#fff\"}},\"maintainAspectRatio\":false,\"responsive\":true,\"responsiveAnimationDuration\":500,\"circumference\":\"6.283185307179586\",\"rotation\":\"6.283185307179586\",\"charType\":\"doughnut\"}";

	@Override
	public String createGadget(String json, String userId) {
		ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot create gadget: command Malformed");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getDataSource() != null
					&& commandDTO.getInformation().getDataSource().trim().length() > 0
					&& commandDTO.getInformation().getGadgetType().equals(MAP)) {
				GadgetDatasource datasource = gadgetDatasourceService
						.getDatasourceByIdentification(commandDTO.getInformation().getDataSource());
				if (datasource == null) {
					log.error("Cannot create gadget: valid datasource is necessary");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly valid datasource is necessary\",\"data\":{}}";
				}
			}
			if (commandDTO.getInformation().getDashboard() == null
					|| commandDTO.getInformation().getDashboard().trim().length() == 0) {
				log.error("Cannot create gadget: dashboard is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly dashboard is necessary\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getGadgetName() == null) {
				log.error("Cannot create gadget: gadgetName is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly gadgetName is necessary\",\"data\":{}}";
			}

			// if setupLayout == null , create a class instance
			if (commandDTO.getInformation().getSetupLayout() == null) {
				commandDTO.getInformation().setSetupLayout(new SetupLayout());
			}

			if (commandDTO.getInformation().getGadgetType() == null
					|| commandDTO.getInformation().getGadgetType().trim().length() == 0) {
				log.error("Cannot create gadget: gadgetType is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly gadgetType is necessary\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getGadgetType().equals(PIE)
					|| commandDTO.getInformation().getGadgetType().equals(LINE)
					|| commandDTO.getInformation().getGadgetType().equals(RADAR)
					|| commandDTO.getInformation().getGadgetType().equals(MIXED)
					|| commandDTO.getInformation().getGadgetType().equals(BAR)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAxes() == null) {
					log.error("Cannot create gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly At least one measure is necessary X and Y\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getAxes().getMeasuresX().size() == 0
						|| commandDTO.getInformation().getAxes().getMeasuresY().size() == 0) {
					log.error("Cannot create gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly At least one measure is necessary X and Y\",\"data\":{}}";
				}
			}

			else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				if (commandDTO.getInformation().getMapConf() == null
						|| commandDTO.getInformation().getMapConf().getIdentifier() == null
						|| commandDTO.getInformation().getMapConf().getLatitude() == null
						|| commandDTO.getInformation().getMapConf().getLongitude() == null
						|| commandDTO.getInformation().getMapConf().getName() == null) {
					log.error("Cannot create gadget: Information MapConf data is necessary ");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly Information MapConf data is necessary\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getOntology() == null
						|| commandDTO.getInformation().getOntology().trim().length() == 0) {
					log.error("Cannot create gadget: Information MapConf data is necessary ");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly Information MapConf data is necessary\",\"data\":{}}";
				}
			} else if (commandDTO.getInformation().getGadgetType().equals(TABLE)
					|| commandDTO.getInformation().getGadgetType().equals(WORDCLOUD)) {
				if (commandDTO.getInformation().getColumns() == null
						|| commandDTO.getInformation().getColumns().size() == 0) {
					log.error("Cannot create gadget: Information Columns data is necessary ");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly Information Columns data is necessary\",\"data\":{}}";
				}
			}

			if (commandDTO.getInformation().getOntology() != null && (commandDTO.getInformation().getRefresh() == null
					|| commandDTO.getInformation().getRefresh().trim().length() == 0)) {
				log.error("Cannot create gadget: refresh is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly refresh is necessary\",\"data\":{}}";
			}
			Gadget gadget = null;

			GadgetTemplate gadgetTemplate = gadgetTemplateService
					.getGadgetTemplateByIdentification(commandDTO.getInformation().getGadgetType(), userId);

			if (gadgetTemplate != null) {
				// Create gadget from template
				GadgetDatasource datasource = null;
				// Use exist datasource
				if (commandDTO.getInformation().getDataSource() != null
						&& commandDTO.getInformation().getDataSource().trim().length() > 0) {
					datasource = gadgetDatasourceService
							.getDatasourceByIdentification(commandDTO.getInformation().getDataSource());
				} else if (commandDTO.getInformation().getOntology() != null
						&& commandDTO.getInformation().getOntology().trim().length() > 0) {
					// create new datasource
					datasource = createFromTemplate(commandDTO, userId);
				}
				ResponseDTO responseDTO = new ResponseDTO();
				if (datasource != null) {
					responseDTO.setGadgetDatasource(mapToGadgetDatasourceDTO(datasource));
				}
				responseDTO.setGadgetTemplate(mapToGadgetTemplateDTO(gadgetTemplate));
				responseDTO.setRequestcode(NEWGADGET);
				responseDTO.setStatus(OK);
				responseDTO.setSetupLayout(commandDTO.getInformation().getSetupLayout());
				responseDTO.setMessage(PROPERLY_CREATED_GADGET);
				String id = commandDTO.getInformation().getGadgetName() + "_" + new Date().getTime();
				responseDTO.setId(id);
				responseDTO.setType(LIVEHTML);
				responseDTO.setFilters(createFiltersFromCommand(commandDTO, id));
				responseDTO.setCustomMenuOptions(commandDTO.getInformation().getCustomMenuOptions());
				return mapper.writeValueAsString(responseDTO);

			} else {
				if ((commandDTO.getInformation().getOntology() == null
						|| commandDTO.getInformation().getOntology().trim().length() == 0)
						&& (commandDTO.getInformation().getDataSource() == null
								|| commandDTO.getInformation().getDataSource().trim().length() == 0)) {
					log.error("Cannot create gadget: ontology or datasource is necessary");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly ontology or datasource is necessary\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
					gadget = createMap(commandDTO, userId);
					ResponseDTO responseDTO = new ResponseDTO();
					responseDTO.setRequestcode(NEWGADGET);
					responseDTO.setStatus(OK);
					responseDTO.setSetupLayout(commandDTO.getInformation().getSetupLayout());
					responseDTO.setMessage(PROPERLY_CREATED_GADGET);
					responseDTO.setId(gadget.getId());
					responseDTO.setType(gadget.getType());
					responseDTO.setFilters(createFiltersFromCommand(commandDTO, gadget.getId()));
					return mapper.writeValueAsString(responseDTO);
				} else if (commandDTO.getInformation().getGadgetType().equals(PIE)
						|| commandDTO.getInformation().getGadgetType().equals(LINE)
						|| commandDTO.getInformation().getGadgetType().equals(BAR)
						|| commandDTO.getInformation().getGadgetType().equals(MIXED)
						|| commandDTO.getInformation().getGadgetType().equals(RADAR)
						|| commandDTO.getInformation().getGadgetType().equals(TABLE)
						|| commandDTO.getInformation().getGadgetType().equals(WORDCLOUD)) {
					gadget = createGadgetAndMeasures(commandDTO, userId);
					ResponseDTO responseDTO = new ResponseDTO();
					// responseDTO.setGadgetDatasource();
					// responseDTO.setGadgetTemplate(mapToGadgetTemplateDTO(gadgetTemplate));
					responseDTO.setRequestcode(NEWGADGET);
					responseDTO.setStatus(OK);
					responseDTO.setSetupLayout(commandDTO.getInformation().getSetupLayout());
					responseDTO.setMessage(PROPERLY_CREATED_GADGET);
					responseDTO.setId(gadget.getId());
					responseDTO.setType(gadget.getType());
					responseDTO.setFilters(createFiltersFromCommand(commandDTO, gadget.getId()));
					return mapper.writeValueAsString(responseDTO);
				} else {
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly, type not valid\",\"data\":{}}";
				}
			}

		} catch (IOException e1) {
			log.error("Cannot create gadget", e1);
			return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot create gadget", e);
			return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		}
	}

	@Override
	public String updateGadget(String json, String userId) {
		ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot update gadget: command Malformed");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getDashboard() == null
					|| commandDTO.getInformation().getDashboard().trim().length() == 0) {
				log.error("Cannot update gadget: dashboard is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getGadgetId() == null) {
				log.error("Cannot update gadget: id is necessary");
				return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
			}

			if (commandDTO.getInformation().getGadgetType().equals(PIE)
					|| commandDTO.getInformation().getGadgetType().equals(LINE)
					|| commandDTO.getInformation().getGadgetType().equals(RADAR)
					|| commandDTO.getInformation().getGadgetType().equals(MIXED)
					|| commandDTO.getInformation().getGadgetType().equals(BAR)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAxes() == null) {
					log.error("Cannot update gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getAxes().getMeasuresX().size() == 0
						|| commandDTO.getInformation().getAxes().getMeasuresY().size() == 0) {
					log.error("Cannot update gadget: At least one measure is necessary X and Y");
					return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";
				}
			} else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				if (commandDTO.getInformation().getMapConf() == null
						|| commandDTO.getInformation().getMapConf().getIdentifier() == null
						|| commandDTO.getInformation().getMapConf().getLatitude() == null
						|| commandDTO.getInformation().getMapConf().getLongitude() == null
						|| commandDTO.getInformation().getMapConf().getName() == null) {
					log.error("Cannot create gadget: Information MapConf data is necessary ");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
				}
			} else if (commandDTO.getInformation().getGadgetType().equals(TABLE)
					|| commandDTO.getInformation().getGadgetType().equals(WORDCLOUD)) {
				if (commandDTO.getInformation().getColumns() == null
						|| commandDTO.getInformation().getColumns().size() == 0) {
					log.error("Cannot create gadget: Information Columns data is necessary ");
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
				}
			}

			Gadget gadget = null;
			GadgetTemplate gadgetTemplate = gadgetTemplateService
					.getGadgetTemplateByIdentification(commandDTO.getInformation().getGadgetType(), userId);

			if (commandDTO.getInformation().getGadgetType() == null
					|| commandDTO.getInformation().getGadgetType().trim().length() == 0 || gadgetTemplate != null) {

				return "{\"requestcode\":\"updateGadget\",\"status\":\"Template\", \"filters\":"
						+ mapper.writeValueAsString(commandDTO.getInformation().getFilters())
						+ ",\"customMenuOptions\":"
						+ mapper.writeValueAsString(commandDTO.getInformation().getCustomMenuOptions()) + ", \"merge\":"
						+ commandDTO.getInformation().isMerge() + " , \"message\":\"properly created gadget\",\"id\":\""
						+ commandDTO.getInformation().getGadgetId() + "\",\"type\":\""
						+ commandDTO.getInformation().getGadgetType() + "\"}";

			} else {
				Gadget gad = gadgetService.getGadgetById(userId, commandDTO.getInformation().getGadgetId());

				/*
				 * if (gad.getType().equals(TREND)) { gadget = updateTrend(commandDTO, userId);
				 * } else
				 */
				if (commandDTO.getInformation().getGadgetType().equals(PIE)
						|| commandDTO.getInformation().getGadgetType().equals(LINE)
						|| commandDTO.getInformation().getGadgetType().equals(BAR)
						|| commandDTO.getInformation().getGadgetType().equals(MIXED)
						|| commandDTO.getInformation().getGadgetType().equals(RADAR)
						|| commandDTO.getInformation().getGadgetType().equals(TABLE)
						|| commandDTO.getInformation().getGadgetType().equals(WORDCLOUD)) {
					gadget = updateGadgetAndMeasures(commandDTO, userId);
				} else if (gad.getType().equals(MAP)) {
					gadget = updateMap(commandDTO, userId);
				}
				return "{\"requestcode\":\"updateGadget\",\"status\":\"OK\", \"message\":\"properly created gadget\",\"id\":\""
						+ gadget.getId() + "\",\"type\":\"" + gadget.getType() + "\"}";
			}

		} catch (IOException e1) {
			log.error("Cannot update gadget", e1);
			return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot update gadget", e);
			return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		}
	}

	private GadgetDatasource createFromTemplate(CommandDTO commandDTO, String userId) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String query = SELECT_FROM + ontologyIdentification;

		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());

		User user = this.userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);

		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
		datasource.setUser(user);

		datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);

		return datasource;
	}

	private GadgetDatasourceDTO mapToGadgetDatasourceDTO(GadgetDatasource datasource) {
		GadgetDatasourceDTO gdDTO = new GadgetDatasourceDTO();
		gdDTO.setId(datasource.getId());
		gdDTO.setName(datasource.getIdentification());
		gdDTO.setRefresh(datasource.getRefresh());
		gdDTO.setType(datasource.getMode());

		return gdDTO;
	}

	private GadgetTemplateDTO mapToGadgetTemplateDTO(GadgetTemplate template) {
		GadgetTemplateDTO gtDTO = new GadgetTemplateDTO();
		gtDTO.setId(template.getId());
		gtDTO.setIdentification(template.getIdentification());
		gtDTO.setUser(template.getUser().getUserId());
		gtDTO.setDescription(template.getDescription());
		gtDTO.setPublic(template.isPublic());
		gtDTO.setTemplate(template.getTemplate());
		gtDTO.setTemplateJS(template.getTemplateJS());

		return gtDTO;
	}

	private FiltersDTO[] createFiltersFromCommand(CommandDTO commandDTO, String id) throws IOException {

		if (commandDTO.getInformation().getFilters() != null && commandDTO.getInformation().getFilters().length > 0) {
			ArrayList<FiltersDTO> filters = new ArrayList<FiltersDTO>();
			for (FilterDTO filter : commandDTO.getInformation().getFilters()) {
				if (filter.getType().equals(LIVEFILTER)) {
					createLiveFilter(id, filter, filters);
				} else if (filter.getType().equals(MULTISELECTFILTER)) {
					createMultiSelectFilter(id, filter, filters);
				} else if (filter.getType().equals(TEXTFILTER)) {
					createTextFilter(id, filter, filters);
				} else if (filter.getType().equals(NUMBERFILTER)) {
					createNumberFilter(id, filter, filters);
				} else if (filter.getType().equals(MULTISELECTDSFILTER)) {
					createMultiSelectDSFilter(id, filter, filters);
				} else if (filter.getType().equals(MULTISELECTNUMBERDSFILTER)) {
					createMultiSelectDSFilter(id, filter, filters);
				} else if (filter.getType().equals(SIMPLESELECTDSFILTER)) {
					createSimpleSelectDSFilter(id, filter, filters);
				} else if (filter.getType().equals(SIMPLESELECTNUMBERDSFILTER)) {
					createSimpleSelectDSFilter(id, filter, filters);
				}

				else {
					throw new IOException(FILTER_NOT_DEFINED);
				}
			}
			return filters.toArray(new FiltersDTO[0]);

		}
		return null;
	}

	private void createNumberFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {
		FiltersDTO numberfilter = new FiltersDTO();
		numberfilter.setId(filter.getId());
		numberfilter.setType(filter.getType());
		numberfilter.setField(filter.getField());
		numberfilter.setName(filter.getName());
		numberfilter.setOp(filter.getOp());
		numberfilter.setTypeAction(FILTER);
		numberfilter.setInitialFilter(filter.isInitialFilter());
		numberfilter.setUseLastValue(true);
		numberfilter.setFilterChaining(false);
		numberfilter.setValue(filter.getValue());
		numberfilter.setHide(filter.isHide());

		TargetDTO targDTO = new TargetDTO(id, filter.getId(), filter.getField());
		TargetDTO[] listTargDTO = new TargetDTO[1];
		listTargDTO[0] = targDTO;
		numberfilter.setTargetList(listTargDTO);
		filters.add(numberfilter);

	}

	private void createTextFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		FiltersDTO textfilter = new FiltersDTO();
		textfilter.setId(filter.getId());
		textfilter.setType(filter.getType());
		textfilter.setField(filter.getField());
		textfilter.setName(filter.getName());
		textfilter.setOp(filter.getOp());
		textfilter.setTypeAction(FILTER);
		textfilter.setInitialFilter(filter.isInitialFilter());
		textfilter.setUseLastValue(true);
		textfilter.setFilterChaining(false);
		textfilter.setValue(filter.getValue());
		textfilter.setHide(filter.isHide());

		TargetDTO targDTO = new TargetDTO(id, filter.getId(), filter.getField());
		TargetDTO[] listTargDTO = new TargetDTO[1];
		listTargDTO[0] = targDTO;
		textfilter.setTargetList(listTargDTO);
		filters.add(textfilter);
	}

	private void createMultiSelectFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		FiltersDTO multiselectfilter = new FiltersDTO();
		multiselectfilter.setId(filter.getId());
		multiselectfilter.setType(filter.getType());
		multiselectfilter.setField(filter.getField());
		multiselectfilter.setName(filter.getName());
		multiselectfilter.setOp(IN);
		multiselectfilter.setTypeAction(FILTER);
		multiselectfilter.setInitialFilter(filter.isInitialFilter());
		multiselectfilter.setUseLastValue(false);
		multiselectfilter.setFilterChaining(false);
		multiselectfilter.setValue(START);
		multiselectfilter.setHide(filter.isHide());

		DataDTO datamultiDTO = new DataDTO();
		datamultiDTO.setOptions(filter.getData().getOptions());
		datamultiDTO.setOptionsDescription(filter.getData().getOptionsDescription());
		datamultiDTO.setOptionsSelected(filter.getData().getOptionsSelected());
		multiselectfilter.setData(datamultiDTO);
		// setTargetList

		TargetDTO targetDTO = new TargetDTO(id, filter.getField(), filter.getField());
		TargetDTO[] listTargetDTO = new TargetDTO[1];
		listTargetDTO[0] = targetDTO;
		multiselectfilter.setTargetList(listTargetDTO);
		filters.add(multiselectfilter);
	}

	private void createMultiSelectDSFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		FiltersDTO multiselectfilter = new FiltersDTO();
		multiselectfilter.setId(filter.getId());
		multiselectfilter.setType(filter.getType());
		multiselectfilter.setField(filter.getField());
		multiselectfilter.setName(filter.getName());
		multiselectfilter.setOp(IN);
		multiselectfilter.setTypeAction(FILTER);
		multiselectfilter.setInitialFilter(filter.isInitialFilter());
		multiselectfilter.setUseLastValue(false);
		multiselectfilter.setFilterChaining(false);
		multiselectfilter.setValue(START);
		multiselectfilter.setHide(filter.isHide());

		DataDTO datamultiDTO = new DataDTO();

		datamultiDTO.setDs(filter.getData().getDs());
		datamultiDTO.setDsFieldValue(filter.getData().getDsFieldValue());
		if (filter.getData().getDsFieldDes() == null) {
			datamultiDTO.setDsFieldDes(filter.getData().getDsFieldValue());
		} else {
			datamultiDTO.setDsFieldDes(filter.getData().getDsFieldDes());
		}
		multiselectfilter.setData(datamultiDTO);
		// setTargetList

		TargetDTO targetDTO = new TargetDTO(id, filter.getField(), filter.getField());
		TargetDTO[] listTargetDTO = new TargetDTO[1];
		listTargetDTO[0] = targetDTO;
		multiselectfilter.setTargetList(listTargetDTO);
		filters.add(multiselectfilter);
	}

	private void createSimpleSelectDSFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		FiltersDTO multiselectfilter = new FiltersDTO();
		multiselectfilter.setId(filter.getId());
		multiselectfilter.setType(filter.getType());
		multiselectfilter.setField(filter.getField());
		multiselectfilter.setName(filter.getName());
		multiselectfilter.setOp(filter.getOp());
		multiselectfilter.setTypeAction(FILTER);
		multiselectfilter.setInitialFilter(filter.isInitialFilter());
		multiselectfilter.setUseLastValue(false);
		multiselectfilter.setFilterChaining(false);
		multiselectfilter.setValue(START);
		multiselectfilter.setHide(filter.isHide());

		DataDTO datamultiDTO = new DataDTO();

		datamultiDTO.setDs(filter.getData().getDs());
		datamultiDTO.setDsFieldValue(filter.getData().getDsFieldValue());
		if (filter.getData().getDsFieldDes() == null) {
			datamultiDTO.setDsFieldDes(filter.getData().getDsFieldValue());
		} else {
			datamultiDTO.setDsFieldDes(filter.getData().getDsFieldDes());
		}
		multiselectfilter.setData(datamultiDTO);
		// setTargetList

		TargetDTO targetDTO = new TargetDTO(id, filter.getField(), filter.getField());
		TargetDTO[] listTargetDTO = new TargetDTO[1];
		listTargetDTO[0] = targetDTO;
		multiselectfilter.setTargetList(listTargetDTO);
		filters.add(multiselectfilter);
	}

	private void createLiveFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		// Create livefilter
		FiltersDTO livefilter = new FiltersDTO();

		livefilter.setId(filter.getId());
		livefilter.setType(filter.getType());
		livefilter.setField("");
		livefilter.setName("");
		livefilter.setOp("");
		livefilter.setTypeAction(FILTER);
		livefilter.setInitialFilter(filter.isInitialFilter());
		livefilter.setUseLastValue(false);
		livefilter.setFilterChaining(false);
		livefilter.setValue(START);
		livefilter.setHide(filter.isHide());
		if (filter.getData() != null) {
			DataDTO dataDTO = new DataDTO();
			dataDTO.setRealtime(filter.getData().getRealtime());
			dataDTO.setSelectedPeriod(filter.getData().getSelectedPeriod());
			dataDTO.setStartDate(filter.getData().getStartDate());
			dataDTO.setEndDate(filter.getData().getEndDate());
			livefilter.setData(dataDTO);
		} else {
			DataDTO dataDTO = new DataDTO();
			dataDTO.setRealtime(START);
			dataDTO.setSelectedPeriod(8);
			dataDTO.setStartDate(START_DATE);
			dataDTO.setEndDate(END_DATE);
			livefilter.setData(dataDTO);
		}
		// setTargetList
		TargetDTO targetDTO = new TargetDTO(id, filter.getField(), filter.getField());
		TargetDTO[] listTargetDTO = new TargetDTO[1];
		listTargetDTO[0] = targetDTO;
		livefilter.setTargetList(listTargetDTO);
		filters.add(livefilter);

	}

	private Gadget updateGadgetAndMeasures(CommandDTO commandDTO, String userId) {
		User user = this.userService.getUser(userId);
		List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(userId,
				commandDTO.getInformation().getGadgetId());
		String idDataSource = "";
		for (Iterator iterator = listMeasures.iterator(); iterator.hasNext();) {
			GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
			idDataSource = gadgetMeasure.getDatasource().getId();
			break;
		}
		Gadget gadget = gadgetService.getGadgetById(userId, commandDTO.getInformation().getGadgetId());
		List<GadgetMeasure> measures = updateGadgetMeasures(commandDTO, user);
		gadgetService.addMeasuresGadget(gadget, idDataSource, measures);
		return gadget;
	}

	private Gadget updateMap(CommandDTO commandDTO, String userId) {
		List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(userId,
				commandDTO.getInformation().getGadgetId());
		String idDataSource = "";
		for (Iterator iterator = listMeasures.iterator(); iterator.hasNext();) {
			GadgetMeasure gadgetMeasure = (GadgetMeasure) iterator.next();
			idDataSource = gadgetMeasure.getDatasource().getId();
			break;
		}
		GadgetDatasource datasource = this.gadgetDatasourceService.getGadgetDatasourceById(idDataSource);
		String ontologyIdentification = commandDTO.getInformation().getOntology();

		StringBuilder query = new StringBuilder();
		query.append("select c.");
		query.append(commandDTO.getInformation().getMapConf().getIdentifier());
		query.append(" as identifier,c.");
		query.append(commandDTO.getInformation().getMapConf().getName());
		query.append(" as name, c.");
		query.append(commandDTO.getInformation().getMapConf().getLatitude());
		query.append(" as latitude , c.");
		query.append(commandDTO.getInformation().getMapConf().getLongitude());
		query.append(" as longitude from ");
		query.append(ontologyIdentification);
		query.append("as c ");

		datasource.setQuery(query.toString());

		this.gadgetDatasourceService.updateGadgetDatasource(datasource);
		Gadget gadget = gadgetService.getGadgetById(userId, commandDTO.getInformation().getGadgetId());
		List<GadgetMeasure> measures = updateGadgetCoordinates(commandDTO);
		gadgetService.addMeasuresGadget(gadget, idDataSource, measures);
		return gadget;
	}

	private Gadget createGadgetAndMeasures(CommandDTO commandDTO, String userId) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		StringBuilder query = new StringBuilder();
		query.append(SELECT_FROM);
		query.append(ontologyIdentification);
		// String identificationDashboard = commandDTO.getInformation().getDashboard();
		String gadgetType = commandDTO.getInformation().getGadgetType();
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());
		User user = this.userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		if (commandDTO.getInformation().getDataSource() != null
				&& commandDTO.getInformation().getDataSource().trim().length() > 0) {
			datasource = gadgetDatasourceService
					.getDatasourceByIdentification(commandDTO.getInformation().getDataSource());
		} else {
			// create new datasource
			datasource.setDbtype(RTDB);
			datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
			datasource.setMaxvalues(MAXVALUES);
			datasource.setMode(QUERY);
			datasource.setRefresh(refresh);
			datasource.setQuery(query.toString());
			datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
			datasource.setUser(user);
			datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);
		}
		// Creation gadget
		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type
		String configGadget = "";
		List<GadgetMeasure> measures;
		if (gadgetType.equals(PIE) || gadgetType.equals(LINE) || gadgetType.equals(BAR) || gadgetType.equals(MIXED)
				|| gadgetType.equals(RADAR)) {
			measures = createGadgetAxes(commandDTO, gadgetType, user, gadget, configGadget);
		} else {
			measures = createGadgetColumns(commandDTO, gadgetType, user, gadget, configGadget);
		}
		gadget = gadgetService.createGadget(gadget, datasource, measures);
		return gadget;
	}

	private Gadget createMap(CommandDTO commandDTO, String userId) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		StringBuilder query = new StringBuilder();
		query.append("select c.");
		query.append(commandDTO.getInformation().getMapConf().getIdentifier());
		query.append(" as identifier,c.");
		query.append(commandDTO.getInformation().getMapConf().getName());
		query.append(" as name, c.");
		query.append(commandDTO.getInformation().getMapConf().getLatitude());
		query.append(" as latitude , c.");
		query.append(commandDTO.getInformation().getMapConf().getLongitude());
		query.append(" as longitude from ");
		query.append(ontologyIdentification);
		query.append(" as c ");
		String gadgetType = commandDTO.getInformation().getGadgetType();
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());
		User user = this.userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);
		datasource.setRefresh(refresh);
		datasource.setQuery(query.toString());
		datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
		datasource.setUser(user);
		datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);
		// Creation gadget
		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type
		String configGadget = "";
		List<GadgetMeasure> measures = createGadgetCoordinates(commandDTO, gadgetType, user, gadget, configGadget);
		gadget = gadgetService.createGadget(gadget, datasource, measures);
		return gadget;
	}

	private List<GadgetMeasure> createGadgetAxes(CommandDTO commandDTO, String gadgetType, User user, Gadget gadget,
			String configGadget) {
		if (gadgetType != null && gadgetType.equals(PIE)) {
			configGadget = setConfigGadgetPie(commandDTO.getInformation().getGadgetConf());
		} else if (gadgetType != null && (gadgetType.equals(LINE) || gadgetType.equals(BAR))) {
			configGadget = setConfigGadgetLB(commandDTO.getInformation().getGadgetConf());
		} else if (gadgetType != null && gadgetType.equals(MIXED)) {
			configGadget = setConfigGadgetMix(commandDTO.getInformation().getGadgetConf());
		} else if (gadgetType != null && gadgetType.equals(RADAR)) {
			configGadget = "{}";
		}

		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();
		int position = 0;
		for (Iterator iterator = commandDTO.getInformation().getAxes().getMeasuresY().iterator(); iterator.hasNext();) {
			MeasureDTO measureDTOY = (MeasureDTO) iterator.next();
			for (Iterator iterator2 = commandDTO.getInformation().getAxes().getMeasuresX().iterator(); iterator2
					.hasNext();) {
				MeasureDTO measureDTOX = (MeasureDTO) iterator2.next();
				GadgetMeasure measure = new GadgetMeasure();

				StringBuilder config = new StringBuilder();
				config.append("{\"fields\": [\"");
				config.append(measureDTOX.getPath());
				config.append("\",\"");
				config.append(measureDTOY.getPath());
				config.append("\"],\"name\":\"");
				config.append(measureDTOY.getName());
				config.append("\",\"config\": {");
				config.append(generateMeasureConfig(gadgetType, position, measureDTOY));
				config.append("}}");
				measure.setConfig(config.toString());
				measures.add(measure);
				position++;
			}

		}
		return measures;
	}

	private String setConfigGadgetTable(GadgetConfDTO gadgetConfDTO) {
		JSONObject jo = new JSONObject(CONFIG_GADGET_TABLE);
		if (gadgetConfDTO != null && gadgetConfDTO.getGadgetTableConfDTO() != null) {
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationLimit() != null) {
				jo.getJSONObject("tablePagination").put("limit",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationLimit());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationPage() != null) {
				jo.getJSONObject("tablePagination").put("page",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationPage());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationLimitOptions() != null) {
				jo.getJSONObject("tablePagination").put("limitOptions",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationLimitOptions());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleBackGroundTHead() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("backGroundTHead",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleBackGroundTHead());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleBackGroundTFooter() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("backGroundTFooter",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleBackGroundTFooter());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTrHeightHead() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("trHeightHead",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTrHeightHead());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTrHeightBody() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("trHeightBody",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTrHeightBody());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTrHeightFooter() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("trHeightFooter",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTrHeightFooter());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTextColorTHead() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("textColorTHead",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTextColorTHead());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTextColorBody() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("textColorBody",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTextColorBody());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTextColorFooter() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("style").put("textColorFooter",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationStyleTextColorFooter());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsRowSelection() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("rowSelection",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsRowSelection());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsMultiSelect() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("multiSelect",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsMultiSelect());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsAutoSelect() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("autoSelect",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsAutoSelect());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsDecapitate() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("decapitate",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsDecapitate());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsLargeEditDialog() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("largeEditDialog",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsLargeEditDialog());
			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsBoundaryLinks() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("boundaryLinks",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsBoundaryLinks());

			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsLimitSelect() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("limitSelect",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsLimitSelect());

			}
			if (gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsPageSelect() != null) {
				jo.getJSONObject("tablePagination").getJSONObject("options").put("pageSelect",
						gadgetConfDTO.getGadgetTableConfDTO().getTablePaginationOptionsPageSelect());

			}

		}
		return jo.toString();
	}

	private String setConfigGadgetMix(GadgetConfDTO gadgetConfDTO) {

		JSONObject jo = new JSONObject(CONFIG_GADGET_MIX);
		if (gadgetConfDTO != null && gadgetConfDTO.getGadgetMixConfDTO() != null) {
			if (gadgetConfDTO.getGadgetMixConfDTO().getLegendDisplay() != null) {
				jo.getJSONObject("legend").put("display", gadgetConfDTO.getGadgetMixConfDTO().getLegendDisplay());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getLegendFullWidth() != null) {
				jo.getJSONObject("legend").put("fullWidth", gadgetConfDTO.getGadgetMixConfDTO().getLegendFullWidth());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsPadding() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("padding",
						gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsPadding());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsFontSize() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("fontSize",
						gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsFontSize());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsUsePointStyle() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("usePointStyle",
						gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsUsePointStyle());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsBoxWidth() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("boxWidth",
						gadgetConfDTO.getGadgetMixConfDTO().getLegendLabelsBoxWidth());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesId() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("id",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesId());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("display",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesDisplay());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesType() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("type",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesType());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesPosition() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("position",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesPosition());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelLabelString() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("labelString", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelLabelString());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("display", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelDisplay());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelFontFamily() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("fontFamily", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelFontFamily());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelPadding() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("padding", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesScaleLabelPadding());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesStacked() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("stacked",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesStacked());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesSort() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("sort",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesSort());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesTicksSuggestedMin() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("ticks")
						.put("suggestedMin", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesTicksSuggestedMin());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesTicksSuggestedMax() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("ticks")
						.put("suggestedMax", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesTicksSuggestedMax());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesTicksMaxTicksLimit() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("ticks")
						.put("maxTicksLimit", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesTicksMaxTicksLimit());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesGridLinesDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("display", gadgetConfDTO.getGadgetMixConfDTO().getScalesYAxesGridLinesDisplay());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesStacked() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).put("stacked",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesStacked());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesSort() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).put("sort",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesSort());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesTicksFontFamily() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("ticks")
						.put("fontFamily", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesTicksFontFamily());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesScaleLabelLabelString() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("labelString", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesScaleLabelLabelString());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesScaleLabelDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("display", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesScaleLabelDisplay());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesScaleLabelPadding() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("padding", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesScaleLabelPadding());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesHideLabel() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).put("hideLabel",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesHideLabel());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("display", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesDisplay());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesBorderDash() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("borderDash", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesBorderDash());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesColor() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("color", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesColor());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesZeroLineBorderDash() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines").put(
						"zeroLineBorderDash",
						gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesZeroLineBorderDash());
			}
			if (gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesZeroLineColor() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines").put(
						"zeroLineColor", gadgetConfDTO.getGadgetMixConfDTO().getScalesXAxesGridLinesZeroLineColor());
			}
		}
		return jo.toString();
	}

	private String setConfigGadgetLB(GadgetConfDTO gadgetConfDTO) {
		JSONObject jo = new JSONObject(CONFIG_GADGET_LB);
		if (gadgetConfDTO != null && gadgetConfDTO.getGadgetLBConfDTO() != null) {
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendDisplay() != null) {
				jo.getJSONObject("legend").put("display", gadgetConfDTO.getGadgetLBConfDTO().getLegendDisplay());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendFullWidth() != null) {
				jo.getJSONObject("legend").put("fullWidth", gadgetConfDTO.getGadgetLBConfDTO().getLegendFullWidth());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendPosition() != null) {
				jo.getJSONObject("legend").put("position", gadgetConfDTO.getGadgetLBConfDTO().getLegendPosition());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsPadding() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("padding",
						gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsPadding());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsFontSize() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("fontSize",
						gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsFontSize());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsUsePointStyle() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("usePointStyle",
						gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsUsePointStyle());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsBoxWidth() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("boxWidth",
						gadgetConfDTO.getGadgetLBConfDTO().getLegendLabelsBoxWidth());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesId() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("id",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesId());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("display",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesDisplay());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesType() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("type",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesType());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesPosition() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("position",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesPosition());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelLabelString() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("labelString", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelLabelString());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("display", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelDisplay());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelFontFamily() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("fontFamily", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelFontFamily());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelPadding() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("padding", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesScaleLabelPadding());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesStacked() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("stacked",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesStacked());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesSort() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).put("sort",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesSort());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesTicksSuggestedMin() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("ticks")
						.put("suggestedMin", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesTicksSuggestedMin());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesTicksSuggestedMax() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("ticks")
						.put("suggestedMax", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesTicksSuggestedMax());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesTicksMaxTicksLimit() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("ticks")
						.put("maxTicksLimit", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesTicksMaxTicksLimit());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesGridLinesDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("yAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("display", gadgetConfDTO.getGadgetLBConfDTO().getScalesYAxesGridLinesDisplay());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesStacked() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).put("stacked",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesStacked());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesSort() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).put("sort",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesSort());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesTicksFontFamily() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("ticks")
						.put("fontFamily", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesTicksFontFamily());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesScaleLabelLabelString() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("labelString", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesScaleLabelLabelString());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesScaleLabelDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("display", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesScaleLabelDisplay());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesScaleLabelPadding() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("scaleLabel")
						.put("padding", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesScaleLabelPadding());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesHideLabel() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).put("hideLabel",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesHideLabel());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesDisplay() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("display", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesDisplay());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesBorderDash() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("borderDash", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesBorderDash());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesColor() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines")
						.put("color", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesColor());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesZeroLineBorderDash() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines").put(
						"zeroLineBorderDash",
						gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesZeroLineBorderDash());
			}
			if (gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesZeroLineColor() != null) {
				jo.getJSONObject("scales").getJSONArray("xAxes").getJSONObject(0).getJSONObject("gridLines").put(
						"zeroLineColor", gadgetConfDTO.getGadgetLBConfDTO().getScalesXAxesGridLinesZeroLineColor());
			}
		}
		return jo.toString();
	}

	private String setConfigGadgetPie(GadgetConfDTO gadgetConfDTO) {
		JSONObject jo = new JSONObject(CONFIG_GADGET_PIE);
		if (gadgetConfDTO != null && gadgetConfDTO.getGadgetPIEConfDTO() != null) {
			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendDisplay() != null) {
				jo.getJSONObject("legend").put("display", gadgetConfDTO.getGadgetPIEConfDTO().getLegendDisplay());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendFullWidth() != null) {
				jo.getJSONObject("legend").put("fullWidth", gadgetConfDTO.getGadgetPIEConfDTO().getLegendFullWidth());
			}

			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendPosition() != null) {
				jo.getJSONObject("legend").put("position", gadgetConfDTO.getGadgetPIEConfDTO().getLegendPosition());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsPadding() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("padding",
						gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsPadding());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsFontSize() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("fontSize",
						gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsFontSize());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsUsePointStyle() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("usePointStyle",
						gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsUsePointStyle());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsBoxWidth() != null) {
				jo.getJSONObject("legend").getJSONObject("labels").put("boxWidth",
						gadgetConfDTO.getGadgetPIEConfDTO().getLegendLabelsBoxWidth());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getElementsArcBorderWidth() != null) {
				jo.getJSONObject("elements").getJSONObject("arc").put("borderWidth",
						gadgetConfDTO.getGadgetPIEConfDTO().getElementsArcBorderWidth());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getElementsArcBorderColor() != null) {
				jo.getJSONObject("elements").getJSONObject("arc").put("borderColor",
						gadgetConfDTO.getGadgetPIEConfDTO().getElementsArcBorderColor());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getMaintainAspectRatio() != null) {
				jo.put("maintainAspectRatio", gadgetConfDTO.getGadgetPIEConfDTO().getMaintainAspectRatio());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getResponsive() != null) {
				jo.put("responsive", gadgetConfDTO.getGadgetPIEConfDTO().getResponsive());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getResponsiveAnimationDuration() != null) {
				jo.put("responsiveAnimationDuration",
						gadgetConfDTO.getGadgetPIEConfDTO().getResponsiveAnimationDuration());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getCircumference() != null) {
				jo.put("circumference", gadgetConfDTO.getGadgetPIEConfDTO().getCircumference());
			}
			if (gadgetConfDTO.getGadgetPIEConfDTO().getCharType() != null) {
				jo.put("rotation", gadgetConfDTO.getGadgetPIEConfDTO().getCharType());
			}
		}
		return jo.toString();
	}

	private String generateMeasureConfig(String gadgetType, int position, MeasureDTO measureDTO) {
		// String config = "";
		StringBuilder config = new StringBuilder();

		if (gadgetType != null && gadgetType.equals(PIE)) {
			config.append("");
		} else if (gadgetType.equals(LINE)) {
			config.append("\"backgroundColor\":\"");
			config.append(getColor(position));
			config.append("\",\"borderColor\":\"");
			config.append(getColor(position));
			config.append("\",\"pointBackgroundColor\":\"");
			config.append(getColor(position));
			config.append("\",\"pointHoverBackgroundColor\":\"");
			config.append(getColor(position));
			config.append("\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"");
		} else if (gadgetType.equals(BAR)) {
			config.append("\"backgroundColor\":\"");
			config.append(getColor(position));
			config.append("\",\"borderColor\":\"");
			config.append(getColor(position));
			config.append("\",\"pointBackgroundColor\":\"");
			config.append(getColor(position));
			config.append("\",\"yAxisID\":\"#0\"");
		} else if (gadgetType.equals(MIXED)) {
			if (measureDTO.getType() != null && measureDTO.getType().equals(LINE)) {

				config.append("\"backgroundColor\":\"");
				config.append(getColor(position));
				config.append("\",\"borderColor\":\"");
				config.append(getColor(position));
				config.append("\",\"pointBackgroundColor\":\"");
				config.append(getColor(position));
				config.append("\",\"pointHoverBackgroundColor\":\"");
				config.append(getColor(position));
				config.append(
						"\",\"yAxisID\":\"#0\",\"type\":\"line\",\"fill\":false,\"steppedLine\":false,\"radius\":\"2\",\"pointRadius\":\"2\",\"pointHoverRadius\":\"2\"");

			} else if (measureDTO.getType() != null && measureDTO.getType().equals(BAR)) {
				config.append("\"backgroundColor\":\"");
				config.append(getColor(position));
				config.append("\",\"borderColor\":\"");
				config.append(getColor(position));
				config.append("\",\"pointBackgroundColor\":\"");
				config.append(getColor(position));
				config.append("\",\"pointHoverBackgroundColor\":\"");
				config.append(getColor(position));
				config.append(
						"\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"");

			} else {
				config.append("\"backgroundColor\":\"");
				config.append(getColor(position));
				config.append("\",\"borderColor\":\"");
				config.append(getColor(position));
				config.append("\",\"pointBackgroundColor\":\"");
				config.append(getColor(position));
				config.append("\",\"pointHoverBackgroundColor\":\"");
				config.append(getColor(position));
				config.append(
						"\",\"yAxisID\":\"#0\",\"type\":\"points\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"");

			}
		} else if (gadgetType.equals(RADAR)) {
			config.append("");
		} else {
			config.append("");
		}

		return config.toString();
	}

	private String getColor(int position) {
		String col = "";
		if (position < COLORS.length && position >= 0) {
			col = COLORS[position];
		} else {
			Random random = new Random();
			col = "rgba(" + random.nextInt(256) + "," + random.nextInt(256) + "," + random.nextInt(256) + ",0.8)";

		}
		return col;
	}

	private List<GadgetMeasure> createGadgetColumns(CommandDTO commandDTO, String gadgetType, User user, Gadget gadget,
			String configGadget) {

		if (gadgetType != null && gadgetType.equals(WORDCLOUD)) {
			configGadget = "{}";
		} else if (gadgetType != null && gadgetType.equals(TABLE)) {

			configGadget = setConfigGadgetTable(commandDTO.getInformation().getGadgetConf());
		}

		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();

		int position = 0;
		for (Iterator iterator2 = commandDTO.getInformation().getColumns().iterator(); iterator2.hasNext();) {
			MeasureDTO measureDTO = (MeasureDTO) iterator2.next();
			GadgetMeasure measure = new GadgetMeasure();
			// String config = "{}";
			StringBuilder config = new StringBuilder();
			if (gadgetType.equals(WORDCLOUD)) {
				config.append("{\"fields\": [\"");
				config.append(measureDTO.getPath());
				config.append("\"],\"name\":\"");
				config.append(measureDTO.getName());
				config.append("\",\"config\": {}}");
			} else if (gadgetType.equals(TABLE)) {
				config.append("{\"fields\": [\"");
				config.append(measureDTO.getPath());
				config.append("\"],\"name\":\"");
				config.append(measureDTO.getName());
				config.append("\",\"config\": {\"position\":\"");
				config.append(position);
				config.append("\"}}");
			} else {
				config.append("{}");
			}
			measure.setConfig(config.toString());
			measures.add(measure);
			position++;
		}

		return measures;
	}

	private List<GadgetMeasure> updateGadgetMeasures(CommandDTO commandDTO, User user) {
		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();
		if (commandDTO.getInformation().getGadgetType().equals(PIE)
				|| commandDTO.getInformation().getGadgetType().equals(LINE)
				|| commandDTO.getInformation().getGadgetType().equals(BAR)
				|| commandDTO.getInformation().getGadgetType().equals(MIXED)
				|| commandDTO.getInformation().getGadgetType().equals(RADAR)) {

			for (Iterator iterator = commandDTO.getInformation().getAxes().getMeasuresY().iterator(); iterator
					.hasNext();) {
				MeasureDTO measureDTOY = (MeasureDTO) iterator.next();
				for (Iterator iterator2 = commandDTO.getInformation().getAxes().getMeasuresX().iterator(); iterator2
						.hasNext();) {
					MeasureDTO measureDTOX = (MeasureDTO) iterator2.next();
					GadgetMeasure measure = new GadgetMeasure();

					StringBuilder config = new StringBuilder();
					config.append("{\"fields\": [\"");
					config.append(measureDTOX.getPath());
					config.append("\",\"");
					config.append(measureDTOY.getPath());
					config.append("\"],\"name\":\"");
					config.append(measureDTOY.getName());
					config.append("\",\"config\": {");
					config.append(generateMeasureConfig(commandDTO.getInformation().getGadgetType(), -1, measureDTOY));
					config.append("}}");
					measure.setConfig(config.toString());
					measures.add(measure);
				}

			}

		} else {
			// WORDCLOUD,TABLE
			for (Iterator iterator2 = commandDTO.getInformation().getColumns().iterator(); iterator2.hasNext();) {
				MeasureDTO measureDTO = (MeasureDTO) iterator2.next();
				GadgetMeasure measure = new GadgetMeasure();

				StringBuilder config = new StringBuilder();

				if (commandDTO.getInformation().getGadgetType().equals(WORDCLOUD)) {
					config.append("{\"fields\": [\"");
					config.append(measureDTO.getPath());
					config.append("\"],\"name\":\"");
					config.append(measureDTO.getName());
					config.append("\",\"config\": {}}");

				} else if (commandDTO.getInformation().getGadgetType().equals(TABLE)) {
					config.append("{\"fields\": [\"");
					config.append(measureDTO.getPath());
					config.append("\"],\"name\":\"");
					config.append(measureDTO.getName());
					config.append("\",\"config\": {\"position\":\"" + 0 + "\"}}");
				} else {
					config.append("{}");
				}
				measure.setConfig(config.toString());
				measures.add(measure);
			}
		}

		return measures;
	}

	private List<GadgetMeasure> createGadgetCoordinates(CommandDTO commandDTO, String gadgetType, User user,
			Gadget gadget, String configGadget) {

		configGadget = GADGET_INITIAL_COORDINATES;
		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();
		GadgetMeasure measure = new GadgetMeasure();
		String config = GADGET_MEASURE_INITIAL;
		measure.setConfig(config);
		measures.add(measure);
		return measures;
	}

	private List<GadgetMeasure> updateGadgetCoordinates(CommandDTO commandDTO) {
		// Create measaures for gadget
		List<GadgetMeasure> measures = new ArrayList<GadgetMeasure>();
		GadgetMeasure measure = new GadgetMeasure();
		String config = GADGET_MEASURE_INITIAL;
		measure.setConfig(config);
		measures.add(measure);
		return measures;
	}

	@Override
	public String setSynopticElementDataSource(String json, String userId) {
		ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot set Synoptic dataSource: command Malformed");
				return "{\"requestcode\":\"setSynopticElementDataSource\",\"status\":\"ERROR\", \"message\":\"command Malformed\",\"data\":{}}";
			}

			ResponseSynopticDTO responseDTO = new ResponseSynopticDTO();
			GadgetDatasource datasource;

			if (commandDTO == null || commandDTO.getInformation().getSynopticElement() == null
					|| commandDTO.getInformation().getSynopticElement().getClassType() == null) {
				log.error("Cannot set Synoptic dataSource: command Malformed");
				return "{\"requestcode\":\"setSynopticElementDataSource\",\"status\":\"ERROR\", \"message\":\"command Malformed SynopticElement.ClassType is necessary\",\"data\":{}}";
			}
			// If has ontology or datasource create or update datasource
			if (!((commandDTO.getInformation().getOntology() == null
					|| commandDTO.getInformation().getOntology().trim().length() == 0)
					&& (commandDTO.getInformation().getDataSource() == null
							|| commandDTO.getInformation().getDataSource().trim().length() == 0))) {

				if (commandDTO.getInformation().getOntology() != null
						&& (commandDTO.getInformation().getRefresh() == null
								|| commandDTO.getInformation().getRefresh().trim().length() == 0)) {
					log.error("Cannot set Synoptic dataSource: refresh is necessary");
					return "{\"requestcode\":\"setSynopticElementDataSource\",\"status\":\"ERROR\", \"message\":\"SynopticElementDataSource not created correctly refresh is necessary\",\"data\":{}}";
				}
				if (commandDTO == null || commandDTO.getInformation().getSynopticElement() == null
						|| commandDTO.getInformation().getSynopticElement().getProjectionField() == null) {
					log.error("Cannot set Synoptic dataSource: command Malformed");
					return "{\"requestcode\":\"setSynopticElementDataSource\",\"status\":\"ERROR\", \"message\":\"command Malformed SynopticElement.ProjectionField is necessary\",\"data\":{}}";
				}
				// update
				if (commandDTO.getInformation().getDataSource() != null) {
					datasource = updateDatasourceForSynoptic(commandDTO, userId);
				} else {
					// create
					datasource = createDatasourceForSynoptic(commandDTO, userId);
				}
				responseDTO.setGadgetDatasource(mapDatasourceDTO(datasource));
			}
			responseDTO.setRequestcode(SETSYNOPTICELEMENTDATASOURCE);
			responseDTO.setStatus(OK);
			responseDTO.setMessage("properly set SynopticElement Datasource");
			responseDTO.setSynopticElement(commandDTO.getInformation().getSynopticElement());
			return mapper.writeValueAsString(responseDTO);

		} catch (IOException e1) {
			log.error("Cannot set Synoptic dataSource", e1);
			return "{\"requestcode\":\"setSynopticElementDataSource\",\"status\":\"ERROR\", \"message\":\"set Synoptic Element Datasource  error\",\"data\":{}}";
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot set Synoptic dataSource", e);
			return "{\"requestcode\":\"setSynopticElementDataSource\",\"status\":\"ERROR\", \"message\":\"set Synoptic Element Datasource  error\",\"data\":{}}";
		}
	}

	private com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO mapDatasourceDTO(
			GadgetDatasource datasource) {
		com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO dto = new com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTO();
		dto.setConfig(datasource.getConfig());
		dto.setDbtype(datasource.getDbtype());
		dto.setDescription(datasource.getDescription());
		dto.setId(datasource.getId());
		dto.setIdentification(datasource.getIdentification());
		dto.setMaxvalues(datasource.getMaxvalues());
		dto.setMode(datasource.getMode());
		if (datasource.getOntology() != null) {
			dto.setOntology(new OntologyDTO(datasource.getOntology().getUser().getUserId(),
					datasource.getOntology().getIdentification(), datasource.getOntology().getDescription()));
		}
		dto.setQuery(datasource.getQuery());
		dto.setRefresh(datasource.getRefresh());
		return dto;
	}

	private String generateQueryFromCommand(CommandDTO commandDTO) {

		StringBuilder query = new StringBuilder();
		query.append(SELECT);
		String aggregationType = null;
		// select
		if (commandDTO.getInformation().getSynopticElement().getAggregationType() == null) {
			aggregationType = NONE;
		} else {
			aggregationType = commandDTO.getInformation().getSynopticElement().getAggregationType().trim()
					.toUpperCase();
		}
		if (aggregationType.equals(NONE)) {
			query.append(commandDTO.getInformation().getSynopticElement().getProjectionField() + AS_RESULT);
		} else if (aggregationType.equals(SUM) || aggregationType.equals(MIN) || aggregationType.equals(MAX)
				|| aggregationType.equals(AVG) || aggregationType.equals(COUNT) || aggregationType.equals(LASTVALUE)) {
			query.append(" ");
			query.append(aggregationType);
			query.append(" ( ");
			query.append(commandDTO.getInformation().getSynopticElement().getProjectionField());
			query.append(" ) ");
			query.append(AS_RESULT);

		}

		// from

		query.append(FROM + commandDTO.getInformation().getOntology());
		// where
		if (commandDTO.getInformation().getSynopticElement().getWhere() != null
				&& commandDTO.getInformation().getSynopticElement().getWhere().size() > 0) {
			StringBuilder whereResult = new StringBuilder();
			whereResult.append(WHERE);
			for (Iterator iterator = commandDTO.getInformation().getSynopticElement().getWhere().iterator(); iterator
					.hasNext();) {
				WhereDTO whereDto = (WhereDTO) iterator.next();
				whereResult.append(whereDto.getPath());
				whereResult.append(" ");
				whereResult.append(whereDto.getOp());
				whereResult.append(" ");
				whereResult.append(whereDto.getValue());
				whereResult.append(AND);
			}
			query.append(whereResult.substring(0, whereResult.length() - AND.length()).toString());
		}
		if (commandDTO.getInformation().getSynopticElement().getAggregationField() != null) {
			query.append(GROUPBY);
			query.append(commandDTO.getInformation().getSynopticElement().getAggregationField());
		}

		if (commandDTO.getInformation().getSynopticElement().getOrderBy() != null
				&& commandDTO.getInformation().getSynopticElement().getOrderBy().size() > 0) {
			StringBuilder orderByResult = new StringBuilder();
			orderByResult.append(ORDERBY);
			for (Iterator iterator = commandDTO.getInformation().getSynopticElement().getOrderBy().iterator(); iterator
					.hasNext();) {
				OrderByDTO orderByDto = (OrderByDTO) iterator.next();
				orderByResult.append(orderByDto.getField());
				orderByResult.append(" ");
				// ASC or DESC
				orderByResult.append(orderByDto.getOperator());
				orderByResult.append(COMA);

			}
			query.append(orderByResult.substring(0, orderByResult.length() - COMA.length()).toString());

		}

		if (commandDTO.getInformation().getSynopticElement().getLimit() != null) {
			query.append(LIMIT);
			query.append(commandDTO.getInformation().getSynopticElement().getLimit());
		}
		return query.toString();
	}

	private GadgetDatasource createDatasourceForSynoptic(CommandDTO commandDTO, String userId) {
		// Creation datasource
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String query = generateQueryFromCommand(commandDTO);
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());
		User user = this.userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		long time = new Date().getTime();
		StringBuilder identi = new StringBuilder();
		identi.append(SYNOPTICDATASOURCE);
		identi.append(userId);
		identi.append(time);
		datasource.setDbtype(RTDB);
		datasource.setIdentification(identi.toString());
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);
		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
		datasource.setUser(user);
		datasource = this.gadgetDatasourceService.createGadgetDatasource(datasource);
		return datasource;
	}

	private GadgetDatasource updateDatasourceForSynoptic(CommandDTO commandDTO, String userId) {
		// Creation datasource
		GadgetDatasource datasource;
		datasource = gadgetDatasourceService.getDatasourceByIdentification(commandDTO.getInformation().getDataSource());
		String ontologyIdentification = commandDTO.getInformation().getOntology();
		String query = generateQueryFromCommand(commandDTO);
		int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());
		User user = this.userService.getUser(userId);
		datasource.setDbtype(RTDB);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);
		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
		datasource.setUser(user);
		this.gadgetDatasourceService.updateGadgetDatasource(datasource);

		return datasource;
	}

}