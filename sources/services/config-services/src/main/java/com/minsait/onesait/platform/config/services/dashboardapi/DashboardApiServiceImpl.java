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
package com.minsait.onesait.platform.config.services.dashboardapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.CollectionUtils;
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
import com.minsait.onesait.platform.config.services.dashboardapi.dto.GadgetDatasourceDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.MeasureDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.ResponseDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.SetupLayout;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.TargetDTO;
import com.minsait.onesait.platform.config.services.exceptions.GadgetDatasourceServiceException;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.gadget.GadgetService;
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

	private static final String SELECT_FROM = "select * from ";
	private static final String RTDB = "RTDB";
	private static final String QUERY = "query";
	private static final String PIE = "pie";
	private static final String BAR = "bar";
	private static final String LINE = "line";
	private static final String MIXED = "mixed";
	private static final String RADAR = "radar";
	private static final String WORDCLOUD = "wordcloud";
	private static final String TABLE = "table";
	private static final String MAP = "map";
	private static final String LIVEHTML = "livehtml";
	private static final int MAXVALUES = 1000;
	private static final String[] COLORS = { "rgba(40,146,215, 0.8)", "rgba(119,178,131, 0.8)",
			"rgba(178,131,119, 0.8)", "rgba(178,161,119, 0.8)", "rgba(247,179,121, 0.8)", "rgba(139,165,160, 0.8)",
			"rgba(254, 246, 240, 0.8)", "rgba(207, 206, 229, 0.8)" };

	private static final String CREATE_GADGET_RESPONSE = "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
	private static final String CANNOT_CREATE_GADGET = "Cannot create gadget: Information MapConf data is necessary ";
	private static final String GADGET_NOT_UPDATED_CORRECTLY = "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not updated correctly\",\"data\":{}}";

	private static final String NEW_GADGET = "newGadget";
	private static final String PROPERLY_CREATED_GADGET = "properly created gadget";
	private static final String TYPE_CONCAT = "\",\"type\":\"";
	private static final String FILTER = "filter";
	private static final String START = "start";
	private static final String FIELDS_CONCAT = "{\"fields\": [\"";
	private static final String NAME_CONCAT = "\"],\"name\":\"";
	private static final String BACKGROUND_COLOR_CONCAT = "\"backgroundColor\":\"";
	private static final String BORDER_COLOR_CONCAT = "\",\"borderColor\":\"";
	private static final String POINT_BACKGROUND_COLOR_CONCAT = "\",\"pointBackgroundColor\":\"";
	private static final String POINT_HOVER_BACKGROUND_COLOR_CONCAT = "\",\"pointHoverBackgroundColor\":\"";

	private final Random rand = new Random();

	@Override
	public String createGadget(String json, String userId) {
		final ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot create gadget: command Malformed");
				return CREATE_GADGET_RESPONSE;
			}
			if ((commandDTO.getInformation().getOntology() == null
					|| commandDTO.getInformation().getOntology().trim().length() == 0)
					&& (commandDTO.getInformation().getDataSource() == null
							|| commandDTO.getInformation().getDataSource().trim().length() == 0)) {
				log.error("Cannot create gadget: ontology or datasource is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly ontology or datasource is necessary\",\"data\":{}}";
			}
			if (commandDTO.getInformation().getDataSource() != null
					&& commandDTO.getInformation().getDataSource().trim().length() > 0
					&& commandDTO.getInformation().getGadgetType().equals(MAP)) {
				final GadgetDatasource datasource = gadgetDatasourceService
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
				if (CollectionUtils.isEmpty(commandDTO.getInformation().getAxes().getMeasuresX())
						|| CollectionUtils.isEmpty(commandDTO.getInformation().getAxes().getMeasuresY())) {
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
					log.error(CANNOT_CREATE_GADGET);
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly Information MapConf data is necessary\",\"data\":{}}";
				}
				if (commandDTO.getInformation().getOntology() == null
						|| commandDTO.getInformation().getOntology().trim().length() == 0) {
					log.error(CANNOT_CREATE_GADGET);
					return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly Information MapConf data is necessary\",\"data\":{}}";
				}
			} else if ((commandDTO.getInformation().getGadgetType().equals(TABLE)
					|| commandDTO.getInformation().getGadgetType().equals(WORDCLOUD))
					&& CollectionUtils.isEmpty(commandDTO.getInformation().getColumns())) {

				log.error("Cannot create gadget: Information Columns data is necessary ");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly Information Columns data is necessary\",\"data\":{}}";

			}

			if (commandDTO.getInformation().getOntology() != null && (commandDTO.getInformation().getRefresh() == null
					|| commandDTO.getInformation().getRefresh().trim().length() == 0)) {
				log.error("Cannot create gadget: refresh is necessary");
				return "{\"requestcode\":\"newGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly refresh is necessary\",\"data\":{}}";
			}
			Gadget gadget = null;

			final GadgetTemplate gadgetTemplate = gadgetTemplateService
					.getGadgetTemplateByIdentification(commandDTO.getInformation().getGadgetType(), userId);

			if (gadgetTemplate != null) {
				// Create gadget from template
				GadgetDatasource datasource;
				// Use exist datasource
				if (commandDTO.getInformation().getDataSource() != null
						&& commandDTO.getInformation().getDataSource().trim().length() > 0) {
					datasource = gadgetDatasourceService
							.getDatasourceByIdentification(commandDTO.getInformation().getDataSource());
				} else {
					// create new datasource
					datasource = createFromTemplate(commandDTO, userId);
				}
				final ResponseDTO responseDTO = new ResponseDTO();
				responseDTO.setGadgetDatasource(mapToGadgetDatasourceDTO(datasource));
				responseDTO.setGadgetTemplate(mapToGadgetTemplateDTO(gadgetTemplate));
				responseDTO.setRequestcode(NEW_GADGET);
				responseDTO.setStatus("OK");
				responseDTO.setSetupLayout(commandDTO.getInformation().getSetupLayout());
				responseDTO.setMessage(PROPERLY_CREATED_GADGET);
				final String id = commandDTO.getInformation().getGadgetName() + "_" + new Date().getTime();
				responseDTO.setId(id);
				responseDTO.setType(LIVEHTML);
				responseDTO.setFilters(createFiltersFromCommand(commandDTO, id));
				return mapper.writeValueAsString(responseDTO);

			} else {

				if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
					gadget = createMap(commandDTO, userId);
					final ResponseDTO responseDTO = new ResponseDTO();
					responseDTO.setRequestcode(NEW_GADGET);
					responseDTO.setStatus("OK");
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
					final ResponseDTO responseDTO = new ResponseDTO();
					responseDTO.setRequestcode(NEW_GADGET);
					responseDTO.setStatus("OK");
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

		} catch (

		final IOException e1) {
			log.error("Cannot create gadget", e1);
			return CREATE_GADGET_RESPONSE;
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot create gadget", e);
			return CREATE_GADGET_RESPONSE;
		}
	}

	@Override
	public String updateGadget(String json, String userId) {
		final ObjectMapper mapper = new ObjectMapper();
		CommandDTO commandDTO;
		try {
			commandDTO = mapper.readValue(json, CommandDTO.class);

			// Validations

			if (commandDTO == null || commandDTO.getInformation() == null) {
				log.error("Cannot update gadget: command Malformed");
				return GADGET_NOT_UPDATED_CORRECTLY;
			}

			if (commandDTO.getInformation().getDashboard() == null
					|| commandDTO.getInformation().getDashboard().trim().length() == 0) {
				log.error("Cannot update gadget: dashboard is necessary");
				return GADGET_NOT_UPDATED_CORRECTLY;
			}

			if (commandDTO.getInformation().getGadgetId() == null) {
				log.error("Cannot update gadget: id is necessary");
				return GADGET_NOT_UPDATED_CORRECTLY;
			}

			if (commandDTO.getInformation().getGadgetType().equals(PIE)
					|| commandDTO.getInformation().getGadgetType().equals(LINE)
					|| commandDTO.getInformation().getGadgetType().equals(RADAR)
					|| commandDTO.getInformation().getGadgetType().equals(MIXED)
					|| commandDTO.getInformation().getGadgetType().equals(BAR)) {
				// At least one measure is necessary
				if (commandDTO.getInformation().getAxes() == null) {
					log.error("Cannot update gadget: At least one measure is necessary X and Y");
					return GADGET_NOT_UPDATED_CORRECTLY;
				}
				if (CollectionUtils.isEmpty(commandDTO.getInformation().getAxes().getMeasuresX())
						|| CollectionUtils.isEmpty(commandDTO.getInformation().getAxes().getMeasuresY())) {
					log.error("Cannot update gadget: At least one measure is necessary X and Y");
					return GADGET_NOT_UPDATED_CORRECTLY;
				}
			} else if (commandDTO.getInformation().getGadgetType().equals(MAP)) {
				if (commandDTO.getInformation().getMapConf() == null
						|| commandDTO.getInformation().getMapConf().getIdentifier() == null
						|| commandDTO.getInformation().getMapConf().getLatitude() == null
						|| commandDTO.getInformation().getMapConf().getLongitude() == null
						|| commandDTO.getInformation().getMapConf().getName() == null) {
					log.error(CANNOT_CREATE_GADGET);
					return CREATE_GADGET_RESPONSE;
				}
			} else if ((commandDTO.getInformation().getGadgetType().equals(TABLE)
					|| commandDTO.getInformation().getGadgetType().equals(WORDCLOUD))
					&& CollectionUtils.isEmpty(commandDTO.getInformation().getColumns())) {

				log.error("Cannot create gadget: Information Columns data is necessary ");
				return CREATE_GADGET_RESPONSE;

			}

			Gadget gadget = null;
			final GadgetTemplate gadgetTemplate = gadgetTemplateService
					.getGadgetTemplateByIdentification(commandDTO.getInformation().getGadgetType(), userId);

			if (gadgetTemplate != null) {
				final List<String> listValues = new ArrayList<>();
				for (final Iterator<MeasureDTO> iterator = commandDTO.getInformation().getAxes().getMeasuresY()
						.iterator(); iterator.hasNext();) {
					final MeasureDTO measureDTOY = iterator.next();
					listValues.add(measureDTOY.getPath());
				}
				return "{\"requestcode\":\"updateGadget\",\"status\":\"Template\", \"filters\":"
						+ mapper.writeValueAsString(commandDTO.getInformation().getFilters()) + ",\"merge\":"
						+ commandDTO.getInformation().isMerge() + " , \"message\":\"properly created gadget\",\"id\":\""
						+ commandDTO.getInformation().getGadgetId() + TYPE_CONCAT
						+ commandDTO.getInformation().getGadgetType() + "\"}";

			} else {
				final Gadget gad = gadgetService.getGadgetById(userId, commandDTO.getInformation().getGadgetId());

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
				if (gadget == null)
					return "{\"requestcode\":\"updateGadget\",\"status\":\"OK\", \"message\":\"properly created gadget\",\"id\":\""
							+ "" + TYPE_CONCAT + "" + "\"}";

				return "{\"requestcode\":\"updateGadget\",\"status\":\"OK\", \"message\":\"properly created gadget\",\"id\":\""
						+ gadget.getId() + TYPE_CONCAT + gadget.getType() + "\"}";
			}

		} catch (final IOException e1) {
			log.error("Cannot update gadget", e1);
			return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		} catch (final GadgetDatasourceServiceException e) {
			log.error("Cannot update gadget", e);
			return "{\"requestcode\":\"updateGadget\",\"status\":\"ERROR\", \"message\":\"gadget not created correctly\",\"data\":{}}";
		}
	}

	private GadgetDatasource createFromTemplate(CommandDTO commandDTO, String userId) {
		// Creation datasource
		final String ontologyIdentification = commandDTO.getInformation().getOntology();
		final String query = SELECT_FROM + ontologyIdentification;

		final int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());

		final User user = userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		final long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);

		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
		datasource.setUser(user);

		datasource = gadgetDatasourceService.createGadgetDatasource(datasource);

		return datasource;
	}

	private GadgetDatasourceDTO mapToGadgetDatasourceDTO(GadgetDatasource datasource) {
		final GadgetDatasourceDTO gdDTO = new GadgetDatasourceDTO();
		gdDTO.setId(datasource.getId());
		gdDTO.setName(datasource.getIdentification());
		gdDTO.setRefresh(datasource.getRefresh());
		gdDTO.setType(datasource.getMode());

		return gdDTO;
	}

	private GadgetTemplateDTO mapToGadgetTemplateDTO(GadgetTemplate template) {
		final GadgetTemplateDTO gtDTO = new GadgetTemplateDTO();
		gtDTO.setId(template.getId());
		gtDTO.setIdentification(template.getIdentification());
		gtDTO.setUser(template.getUser().getUserId());
		gtDTO.setDescription(template.getDescription());
		gtDTO.setPublic(template.isPublic());

		return gtDTO;
	}

	private FiltersDTO[] createFiltersFromCommand(CommandDTO commandDTO, String id) throws IOException {

		if (commandDTO.getInformation().getFilters() != null && commandDTO.getInformation().getFilters().length > 0) {
			final ArrayList<FiltersDTO> filters = new ArrayList<>();
			for (final FilterDTO filter : commandDTO.getInformation().getFilters()) {
				if (filter.getType().equals("livefilter")) {
					createLiveFilter(id, filter, filters);
				} else if (filter.getType().equals("multiselectfilter")) {
					createMultiSelectFilter(id, filter, filters);
				} else if (filter.getType().equals("textfilter")) {
					createTextFilter(id, filter, filters);
				} else if (filter.getType().equals("numberfilter")) {
					createNumberFilter(id, filter, filters);
				} else {
					throw new IOException("Filter not defined");
				}
			}
			return filters.toArray(new FiltersDTO[0]);

		}
		return new FiltersDTO[0];
	}

	private void createNumberFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {
		final FiltersDTO numberfilter = new FiltersDTO();
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

		final TargetDTO targDTO = new TargetDTO(id, filter.getId(), filter.getField());
		final TargetDTO[] listTargDTO = new TargetDTO[1];
		listTargDTO[0] = targDTO;
		numberfilter.setTargetList(listTargDTO);
		filters.add(numberfilter);

	}

	private void createTextFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		final FiltersDTO textfilter = new FiltersDTO();
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

		final TargetDTO targDTO = new TargetDTO(id, filter.getId(), filter.getField());
		final TargetDTO[] listTargDTO = new TargetDTO[1];
		listTargDTO[0] = targDTO;
		textfilter.setTargetList(listTargDTO);
		filters.add(textfilter);
	}

	private void createMultiSelectFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		final FiltersDTO multiselectfilter = new FiltersDTO();
		multiselectfilter.setId(filter.getId());
		multiselectfilter.setType(filter.getType());
		multiselectfilter.setField(filter.getField());
		multiselectfilter.setName(filter.getName());
		multiselectfilter.setOp("IN");
		multiselectfilter.setTypeAction(FILTER);
		multiselectfilter.setInitialFilter(filter.isInitialFilter());
		multiselectfilter.setUseLastValue(false);
		multiselectfilter.setFilterChaining(false);
		multiselectfilter.setValue(START);
		multiselectfilter.setHide(filter.isHide());

		final DataDTO datamultiDTO = new DataDTO();
		datamultiDTO.setOptions(filter.getData().getOptions());
		datamultiDTO.setOptionsSelected(filter.getData().getOptionsSelected());
		multiselectfilter.setData(datamultiDTO);
		// setTargetList

		final TargetDTO targetDTO = new TargetDTO(id, filter.getField(), filter.getField());
		final TargetDTO[] listTargetDTO = new TargetDTO[1];
		listTargetDTO[0] = targetDTO;
		multiselectfilter.setTargetList(listTargetDTO);
		filters.add(multiselectfilter);
	}

	private void createLiveFilter(String id, FilterDTO filter, ArrayList<FiltersDTO> filters) {

		// Create livefilter
		final FiltersDTO livefilter = new FiltersDTO();

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
			final DataDTO dataDTO = new DataDTO();
			dataDTO.setRealtime(filter.getData().getRealtime());
			dataDTO.setSelectedPeriod(filter.getData().getSelectedPeriod());
			dataDTO.setStartDate(filter.getData().getStartDate());
			dataDTO.setEndDate(filter.getData().getEndDate());
			livefilter.setData(dataDTO);
		} else {
			final DataDTO dataDTO = new DataDTO();
			dataDTO.setRealtime(START);
			dataDTO.setSelectedPeriod(8);
			dataDTO.setStartDate("NOW(\"yyyy-MM-dd\'T\'HH:mm:ss\'Z\'\",\"hour\",-8)");
			dataDTO.setEndDate("NOW(\"yyyy-MM-dd\'T\'HH:mm:ss\'Z\'\",\"hour\",0)");
			livefilter.setData(dataDTO);
		}
		// setTargetList
		final TargetDTO targetDTO = new TargetDTO(id, filter.getField(), filter.getField());
		final TargetDTO[] listTargetDTO = new TargetDTO[1];
		listTargetDTO[0] = targetDTO;
		livefilter.setTargetList(listTargetDTO);
		filters.add(livefilter);

	}

	private Gadget updateGadgetAndMeasures(CommandDTO commandDTO, String userId) {
		final List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(userId,
				commandDTO.getInformation().getGadgetId());
		if (!CollectionUtils.isEmpty(listMeasures)) {
			final String idDataSource = listMeasures.get(0).getDatasource().getId();

			final Gadget gadget = gadgetService.getGadgetById(userId, commandDTO.getInformation().getGadgetId());
			final List<GadgetMeasure> measures = updateGadgetMeasures(commandDTO);
			gadgetService.addMeasuresGadget(gadget, idDataSource, measures);
			return gadget;
		}
		return new Gadget();
	}

	private Gadget updateMap(CommandDTO commandDTO, String userId) {
		final List<GadgetMeasure> listMeasures = gadgetService.getGadgetMeasuresByGadgetId(userId,
				commandDTO.getInformation().getGadgetId());
		if (!CollectionUtils.isEmpty(listMeasures)) {
			final String idDataSource = listMeasures.get(0).getDatasource().getId();
			final GadgetDatasource datasource = gadgetDatasourceService.getGadgetDatasourceById(idDataSource);
			final String ontologyIdentification = commandDTO.getInformation().getOntology();

			final String query = "select c." + commandDTO.getInformation().getMapConf().getIdentifier()
					+ " as identifier,c." + commandDTO.getInformation().getMapConf().getName() + " as name, c."
					+ commandDTO.getInformation().getMapConf().getLatitude() + " as latitude , c."
					+ commandDTO.getInformation().getMapConf().getLongitude() + " as longitude from "
					+ ontologyIdentification + " as c";

			datasource.setQuery(query);

			gadgetDatasourceService.updateGadgetDatasource(datasource);
			final Gadget gadget = gadgetService.getGadgetById(userId, commandDTO.getInformation().getGadgetId());
			final List<GadgetMeasure> measures = updateGadgetCoordinates();
			gadgetService.addMeasuresGadget(gadget, idDataSource, measures);
			return gadget;
		}

		return new Gadget();
	}

	private Gadget createGadgetAndMeasures(CommandDTO commandDTO, String userId) {

		final String ontologyIdentification = commandDTO.getInformation().getOntology();
		final String query = SELECT_FROM + ontologyIdentification;
		final String gadgetType = commandDTO.getInformation().getGadgetType();
		final int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());
		final User user = userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		final long time = new Date().getTime();
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
			datasource.setQuery(query);
			datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
			datasource.setUser(user);
			datasource = gadgetDatasourceService.createGadgetDatasource(datasource);
		}
		// Creation gadget
		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type
		final String configGadget = "";
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
		final String ontologyIdentification = commandDTO.getInformation().getOntology();

		final String query = "select c." + commandDTO.getInformation().getMapConf().getIdentifier()
				+ " as identifier,c." + commandDTO.getInformation().getMapConf().getName() + " as name, c."
				+ commandDTO.getInformation().getMapConf().getLatitude() + " as latitude , c."
				+ commandDTO.getInformation().getMapConf().getLongitude() + " as longitude from "
				+ ontologyIdentification + " as c ";
		final String gadgetType = commandDTO.getInformation().getGadgetType();
		final int refresh = Integer.parseInt(commandDTO.getInformation().getRefresh());

		final User user = userService.getUser(userId);
		GadgetDatasource datasource = new GadgetDatasource();
		final long time = new Date().getTime();
		datasource.setDbtype(RTDB);
		datasource.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		datasource.setMaxvalues(MAXVALUES);
		datasource.setMode(QUERY);
		datasource.setRefresh(refresh);
		datasource.setQuery(query);
		datasource.setOntology(ontologyService.getOntologyByIdentification(ontologyIdentification, userId));
		datasource.setUser(user);
		datasource = gadgetDatasourceService.createGadgetDatasource(datasource);

		// Creation gadget

		Gadget gadget = new Gadget();
		gadget.setIdentification(commandDTO.getInformation().getGadgetName() + "_" + time);
		// configuration depending on the type

		final List<GadgetMeasure> measures = createGadgetCoordinates(gadgetType, user, gadget);
		gadget = gadgetService.createGadget(gadget, datasource, measures);

		return gadget;
	}

	private List<GadgetMeasure> createGadgetAxes(CommandDTO commandDTO, String gadgetType, User user, Gadget gadget,
			String configGadget) {

		if (gadgetType != null && gadgetType.equals(PIE)) {
			configGadget = "{\"legend\":{\"display\":true,\"fullWidth\":false,\"position\":\"left\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":1}},\"elements\":{\"arc\":{\"borderWidth\":1,\"borderColor\":\"#fff\"}},\"maintainAspectRatio\":false,\"responsive\":true,\"responsiveAnimationDuration\":500,\"circumference\":\"6.283185307179586\",\"rotation\":\"6.283185307179586\",\"charType\":\"doughnut\"}";
		} else if (gadgetType != null && gadgetType.equals(LINE)) {
			configGadget = "{\"legend\":{\"display\":true,\"fullWidth\":false,\"position\":\"top\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,\"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":\"10\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":true,\"ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\"labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"2\",\"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",\"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}";
		} else if (gadgetType != null && gadgetType.equals(BAR)) {
			configGadget = "{\"legend\":{\"display\":true,\"fullWidth\":false,\"position\":\"top\",\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,\"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":\"10\"},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":true,\"ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\"labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"2\",\"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",\"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}";
		} else if (gadgetType != null && gadgetType.equals(MIXED)) {
			configGadget = "{\"legend\":{\"display\":false,\"fullWidth\":false,\"labels\":{\"padding\":10,\"fontSize\":11,\"usePointStyle\":false,\"boxWidth\":2}},\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true,\"fontFamily\":\"Soho\",\"padding\":4},\"stacked\":false,\"sort\":true,\"ticks\":{\"suggestedMin\":\"0\",\"suggestedMax\":\"1000\",\"maxTicksLimit\":10},\"gridLines\":{\"display\":false}}],\"xAxes\":[{\"stacked\":false,\"sort\":true,\"ticks\":{\"fontFamily\":\"Soho\"},\"scaleLabel\":{\"display\":true,\"labelString\":\"\",\"fontFamily\":\"Soho\",\"padding\":4},\"hideLabel\":\"2\",\"gridLines\":{\"display\":true,\"borderDash\":[2,4],\"color\":\"#CCC\",\"zeroLineBorderDash\":[2,4],\"zeroLineColor\":\"transparent\"}}]}}";
		} else if (gadgetType != null && gadgetType.equals(RADAR)) {
			configGadget = "{}";
		}

		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		final List<GadgetMeasure> measures = new ArrayList<>();
		int position = 0;
		for (final Iterator<MeasureDTO> iterator = commandDTO.getInformation().getAxes().getMeasuresY()
				.iterator(); iterator.hasNext();) {
			final MeasureDTO measureDTOY = iterator.next();
			for (final Iterator<MeasureDTO> iterator2 = commandDTO.getInformation().getAxes().getMeasuresX()
					.iterator(); iterator2.hasNext();) {
				final MeasureDTO measureDTOX = iterator2.next();
				final GadgetMeasure measure = new GadgetMeasure();

				final String config = FIELDS_CONCAT + measureDTOX.getPath() + "\",\"" + measureDTOY.getPath()
						+ NAME_CONCAT + measureDTOY.getName() + "\",\"config\": {"
						+ generateMeasureConfig(gadgetType, position, measureDTOY) + "}}";

				measure.setConfig(config);
				measures.add(measure);
				position++;
			}

		}
		return measures;
	}

	private String generateMeasureConfig(String gadgetType, int position, MeasureDTO measureDTO) {
		String config = "";
		if (gadgetType == null || measureDTO == null) {
			config = "";
		} else if (gadgetType.equals(PIE)) {
			config = "";
		} else if (gadgetType.equals(LINE)) {
			config = BACKGROUND_COLOR_CONCAT + getColor(position) + BORDER_COLOR_CONCAT + getColor(position)
					+ POINT_BACKGROUND_COLOR_CONCAT + getColor(position) + POINT_HOVER_BACKGROUND_COLOR_CONCAT
					+ getColor(position)
					+ "\",\"yAxisID\":\"#0\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\"";
		} else if (gadgetType.equals(BAR)) {
			config = BACKGROUND_COLOR_CONCAT + getColor(position) + BORDER_COLOR_CONCAT + getColor(position)
					+ POINT_BACKGROUND_COLOR_CONCAT + getColor(position) + "\",\"yAxisID\":\"#0\"";
		} else if (gadgetType.equals(MIXED)) {
			if (measureDTO.getType() != null && measureDTO.getType().equals(LINE)) {
				config = BACKGROUND_COLOR_CONCAT + getColor(position) + BORDER_COLOR_CONCAT + getColor(position)
						+ POINT_BACKGROUND_COLOR_CONCAT + getColor(position) + POINT_HOVER_BACKGROUND_COLOR_CONCAT
						+ getColor(position)
						+ "\",\"yAxisID\":\"#0\",\"type\":\"line\",\"fill\":false,\"steppedLine\":false,\"radius\":\"2\",\"pointRadius\":\"2\",\"pointHoverRadius\":\"2\"";
			} else if (measureDTO.getType() != null && measureDTO.getType().equals(BAR)) {
				config = BACKGROUND_COLOR_CONCAT + getColor(position) + BORDER_COLOR_CONCAT + getColor(position)
						+ POINT_BACKGROUND_COLOR_CONCAT + getColor(position) + POINT_HOVER_BACKGROUND_COLOR_CONCAT
						+ getColor(position)
						+ "\",\"yAxisID\":\"#0\",\"type\":\"bar\",\"fill\":false,\"steppedLine\":false,\"radius\":\"1\",\"pointRadius\":\"1\",\"pointHoverRadius\":\"1\"";
			} else {
				config = BACKGROUND_COLOR_CONCAT + getColor(position) + BORDER_COLOR_CONCAT + getColor(position)
						+ POINT_BACKGROUND_COLOR_CONCAT + getColor(position) + POINT_HOVER_BACKGROUND_COLOR_CONCAT
						+ getColor(position)
						+ "\",\"yAxisID\":\"#0\",\"type\":\"points\",\"fill\":false,\"steppedLine\":false,\"radius\":\"0\",\"pointRadius\":\"0\",\"pointHoverRadius\":\"0\"";
			}
		} else if (gadgetType.equals(RADAR)) {
			config = "";
		}

		return config;
	}

	private String getColor(int position) {
		String col = "";
		if (position < COLORS.length && position >= 0) {
			col = COLORS[position];
		} else {

			col = "rgba(" + rand.nextInt(256) + "," + rand.nextInt(256) + "," + rand.nextInt(256) + ",0.8)";

		}
		return col;
	}

	private List<GadgetMeasure> createGadgetColumns(CommandDTO commandDTO, String gadgetType, User user, Gadget gadget,
			String configGadget) {

		if (gadgetType != null && gadgetType.equals(WORDCLOUD)) {
			configGadget = "{}";
		} else if (gadgetType != null && gadgetType.equals(TABLE)) {
			configGadget = "{\"tablePagination\":{\"limit\":\"100\",\"page\":1,\"limitOptions\":[5,10,20,50,100],\"style\":{\"backGroundTHead\":\"#ffffff\",\"backGroundTFooter\":\"#ffffff\",\"trHeightHead\":\"40\",\"trHeightBody\":\"40\",\"trHeightFooter\":\"40\",\"textColorTHead\":\"#060e14\",\"textColorBody\":\"#555555\",\"textColorFooter\":\"#555555\"},\"options\":{\"rowSelection\":false,\"multiSelect\":false,\"autoSelect\":false,\"decapitate\":false,\"largeEditDialog\":false,\"boundaryLinks\":true,\"limitSelect\":true,\"pageSelect\":true}}}";
		}

		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		final List<GadgetMeasure> measures = new ArrayList<>();

		int position = 0;
		for (final Iterator<MeasureDTO> iterator2 = commandDTO.getInformation().getColumns().iterator(); iterator2
				.hasNext();) {
			final MeasureDTO measureDTO = iterator2.next();
			final GadgetMeasure measure = new GadgetMeasure();
			String config = "{}";
			if (gadgetType != null && gadgetType.equals(WORDCLOUD)) {
				config = FIELDS_CONCAT + measureDTO.getPath() + NAME_CONCAT + measureDTO.getName()
						+ "\",\"config\": {}}";
			} else if (gadgetType != null && gadgetType.equals(TABLE)) {
				config = FIELDS_CONCAT + measureDTO.getPath() + NAME_CONCAT + measureDTO.getName()
						+ "\",\"config\": {\"position\":\"" + position + "\"}}";
			}
			measure.setConfig(config);
			measures.add(measure);
			position++;
		}

		return measures;
	}

	private List<GadgetMeasure> updateGadgetMeasures(CommandDTO commandDTO) {
		// Create measaures for gadget
		final List<GadgetMeasure> measures = new ArrayList<>();
		if (commandDTO.getInformation().getGadgetType().equals(PIE)
				|| commandDTO.getInformation().getGadgetType().equals(LINE)
				|| commandDTO.getInformation().getGadgetType().equals(BAR)
				|| commandDTO.getInformation().getGadgetType().equals(MIXED)
				|| commandDTO.getInformation().getGadgetType().equals(RADAR)) {

			for (final Iterator<MeasureDTO> iterator = commandDTO.getInformation().getAxes().getMeasuresY()
					.iterator(); iterator.hasNext();) {
				final MeasureDTO measureDTOY = iterator.next();
				for (final Iterator<MeasureDTO> iterator2 = commandDTO.getInformation().getAxes().getMeasuresX()
						.iterator(); iterator2.hasNext();) {
					final MeasureDTO measureDTOX = iterator2.next();
					final GadgetMeasure measure = new GadgetMeasure();
					final String config = FIELDS_CONCAT + measureDTOX.getPath() + "\",\"" + measureDTOY.getPath()
							+ NAME_CONCAT + measureDTOY.getName() + "\",\"config\": {"
							+ generateMeasureConfig(commandDTO.getInformation().getGadgetType(), -1, measureDTOY)
							+ "}}";
					measure.setConfig(config);
					measures.add(measure);
				}

			}

		} else {
			// WORDCLOUD,TABLE
			for (final Iterator<MeasureDTO> iterator2 = commandDTO.getInformation().getColumns().iterator(); iterator2
					.hasNext();) {
				final MeasureDTO measureDTO = iterator2.next();
				final GadgetMeasure measure = new GadgetMeasure();
				String config = "{}";
				if (commandDTO.getInformation().getGadgetType().equals(WORDCLOUD)) {
					config = FIELDS_CONCAT + measureDTO.getPath() + NAME_CONCAT + measureDTO.getName()
							+ "\",\"config\": {}}";
				} else if (commandDTO.getInformation().getGadgetType().equals(TABLE)) {
					config = FIELDS_CONCAT + measureDTO.getPath() + NAME_CONCAT + measureDTO.getName()
							+ "\",\"config\": {\"position\":\"" + 0 + "\"}}";
				}
				measure.setConfig(config);
				measures.add(measure);
			}
		}

		return measures;
	}

	private List<GadgetMeasure> createGadgetCoordinates(String gadgetType, User user, Gadget gadget) {

		final String configGadget = "{\"center\":{\"lat\":31.952162238024975,\"lng\":5.625,\"zoom\":2},\"markersFilter\":\"identifier\",\"jsonMarkers\":\"\"}";
		gadget.setConfig(configGadget);
		gadget.setDescription("");
		gadget.setPublic(Boolean.FALSE);
		gadget.setType(gadgetType);
		gadget.setUser(user);

		// Create measaures for gadget
		final List<GadgetMeasure> measures = new ArrayList<>();
		final GadgetMeasure measure = new GadgetMeasure();
		final String config = "{\"fields\":[\"latitude\",\"longitude\",\"identifier\",\"name\"],\"name\":\"\",\"config\":{}}";
		measure.setConfig(config);
		measures.add(measure);
		return measures;
	}

	private List<GadgetMeasure> updateGadgetCoordinates() {
		// Create measaures for gadget
		final List<GadgetMeasure> measures = new ArrayList<>();
		final GadgetMeasure measure = new GadgetMeasure();
		final String config = "{\"fields\":[\"latitude\",\"longitude\",\"identifier\",\"name\"],\"name\":\"\",\"config\":{}}";
		measure.setConfig(config);
		measures.add(measure);
		return measures;
	}

}