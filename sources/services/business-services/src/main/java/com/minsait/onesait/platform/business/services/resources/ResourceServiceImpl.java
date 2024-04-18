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
package com.minsait.onesait.platform.business.services.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.DashboardUserAccessType;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessRepository;
import com.minsait.onesait.platform.config.repository.DashboardUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserApiRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {

	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired(required = false)
	@Qualifier("routerServiceImpl")
	private RouterService routerService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private UserService userService;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private ViewerRepository viewerRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private DashboardUserAccessRepository dashboardUserAccessRepository;
	@Autowired
	private DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	private UserApiRepository userApiRepository;

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public List<String> getFilesFormats() {
		final List<String> formats = new ArrayList<>();
		formats.add("csv");
		formats.add("xml");
		formats.add("json");
		return formats;
	}

	@Override
	public List<Map<String, Object>> getResourceFromUrl(String url, Map<String, String> resultMap) throws IOException {
		final URL newUrl = new URL(url);
		final URLConnection urlConn = newUrl.openConnection();
		final InputStreamReader input = new InputStreamReader(urlConn.getInputStream());
		resultMap.put("name", url);
		final String contentType = urlConn.getContentType();
		String json = "";
		if (contentType.equals("text/csv") || url.contains(".csv")) {
			json = getJsonFromCSV(input);
			resultMap.put("format", "application/vnd.ms-excel");
		} else if (contentType.equals("text/xml") || contentType.contentEquals("application/xml")
				|| url.contains(".xml")) {
			json = getJsonFromXML(input);
			resultMap.put("format", "text/xml");
		} else if (contentType.equals("application/json") || url.contains(".json")) {
			final BufferedReader reader = new BufferedReader(input);
			final StringBuilder responseStrBuilder = new StringBuilder();
			String str;
			while ((str = reader.readLine()) != null) {
				responseStrBuilder.append(str);
			}
			json = responseStrBuilder.toString();
			resultMap.put("format", "application/json");
		}
		json = json.replace("\\\"", "");
		if (!json.equals("")) {
			if (json.substring(0, 1).equals("{")) {
				final Map<String, Object> obj = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
				});
				final List<Map<String, Object>> result = processMap(obj);
				return result;
			} else {
				return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
				});
			}
		} else {
			resultMap.put("format", "");
			return new ArrayList<>();
		}
	}

	@Override
	public Ontology createOntology(String ontologyIdentification, String ontologyDescription, String schema,
			String userId) throws IOException {
		final Ontology ontology = new Ontology();
		ontology.setJsonSchema(completeSchema(schema, ontologyIdentification, ontologyDescription).toString());
		ontology.setIdentification(ontologyIdentification);
		ontology.setActive(true);
		ontology.setDataModel(dataModelService.getDataModelByName("EmptyBase"));
		ontology.setDescription(ontologyDescription);
		ontology.setUser(userService.getUser(userId));
		ontology.setMetainf("imported,json");
		ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf("MONGO"));
		return ontology;
	}

	@Override
	public String insertDataIntoOntology(String ontology, String data, String userId)
			throws JsonProcessingException, IOException {
		final JsonNode node = mapper.readTree(data);
		final OperationModel operation = new OperationModel.Builder(ontology, OperationType.INSERT, userId,
				Source.INTERNAL_ROUTER).body(node.toString()).queryType(QueryType.NATIVE).build();
		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(operation);
		final OperationResultModel response = routerService.insert(modelNotification);
		return response.getMessage();
	}

	public String getJsonFromCSV(InputStreamReader input) throws IOException {
		final CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
		final CsvMapper csvMapper = new CsvMapper();
		csvMapper.enable(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING);
		final List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll);
	}

	public String getJsonFromXML(InputStreamReader input) throws IOException {
		final BufferedReader streamReader = new BufferedReader(input);
		final StringBuilder responseStrBuilder = new StringBuilder();

		String result;
		while ((result = streamReader.readLine()) != null) {
			responseStrBuilder.append(result);
		}

		final JSONObject xmlJSONObj = XML.toJSONObject(responseStrBuilder.toString());
		return xmlJSONObj.toString(4);
	}

	public List<Map<String, Object>> processMap(Map<String, Object> obj) {
		final List<Map<String, Object>> result = new ArrayList<>();
		for (final Entry<String, Object> entry : obj.entrySet()) {

			if (entry.getValue() instanceof List) {
				return (List<Map<String, Object>>) entry.getValue();
			} else if (entry.getValue() instanceof Map) {
				return processMap((Map<String, Object>) entry.getValue());
			}
		}
		return result;
	}

	private JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = mapper.readTree(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", "http://json-schema.org/draft-04/schema#");
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}
}
