/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.importtool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessService;
import com.minsait.onesait.platform.business.services.ontology.OntologyBusinessServiceException;
import com.minsait.onesait.platform.business.services.resources.ResourceServiceImpl;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("api/importtool")
@Tag(name = "Import tool")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
public class ImportToolRestController {

	@Autowired
	private ResourceServiceImpl resourceService;
	@Autowired
	private DataModelService dataModelService;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyBusinessService ontologyBusinessService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private AppWebUtils utils;

	public static final String ONTOLOGY_PATTERN = "^[a-zA-Z0-9_]*$";
	private final ObjectMapper mapper = new ObjectMapper();

	@Operation(summary = "Insert file data into an ontology")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> insert(@RequestParam(required = true, value = "newOntology", defaultValue = "true") boolean newOntology,
			@RequestParam(required = true, value = "ontologyName") String ontologyName,
			@RequestParam(required = false, value = "ontologyDescription") String ontologyDescription,
			@RequestPart("file") MultipartFile file) {
		if (newOntology) {
			if (ontologyDescription == null || ontologyDescription.equals("")) {
				return new ResponseEntity<>("Mandatory ontology description when creating a new ontology", HttpStatus.BAD_REQUEST);
			}
			if (!ontologyName.matches(ONTOLOGY_PATTERN)) {
				return new ResponseEntity<>("Invalid ontology name", HttpStatus.NOT_ACCEPTABLE);
			}
			if(ontologyService.getOntologyByIdentification(ontologyName) != null) {
				return new ResponseEntity<>("The ontology " + ontologyName + " already exists", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			if(ontologyService.getOntologyByIdentification(ontologyName) == null) {
				return new ResponseEntity<>("The ontology " + ontologyName + " does not exist", HttpStatus.NOT_FOUND);
			}
		}

		final String userId = utils.getUserId();
		final String jsonData = getJsonFromFile(file);
		if (jsonData == null || jsonData.equals("")) {
			return new ResponseEntity<>("Invalid file type. Only CSV, XML and JSON files are acceptable", HttpStatus.NOT_ACCEPTABLE);
		}

		if (newOntology) {
			try {
				final String firstJson = getFirstElement(jsonData);
				final String jsonSchema = JsonSchemaGenerator.outputAsString(ontologyName, "Info " + ontologyName, firstJson);

				final Ontology ontology = new Ontology();
				ontology.setJsonSchema(jsonSchema);
				ontology.setIdentification(ontologyName);
				ontology.setActive(true);
				ontology.setDataModel(dataModelService.getDataModelByName("EmptyBase"));
				ontology.setDescription(ontologyDescription);
				ontology.setUser(userService.getUser(userId));
				ontology.setMetainf("imported,json");
				ontology.setRtdbDatasource(Ontology.RtdbDatasource.valueOf("MONGO"));

				ontologyBusinessService.createOntology(ontology, ontology.getUser().getUserId(), null);
			} catch (final IOException e) {
				return new ResponseEntity<>("There was an error creating the JSON schema: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (final OntologyBusinessServiceException e) {
				return new ResponseEntity<>("There was an error creating the ontology: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		try {
			final OperationResultModel result = resourceService.insertDataIntoOntology(ontologyName, jsonData, userId);
			if (!result.getMessage().equals("OK")) {
				return new ResponseEntity<>("There was an error inserting bulk data: " + result.getMessage().replaceAll("\"", "'"), HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				final JSONObject jsonResult = new JSONObject(result.getResult());
				final int count = jsonResult.getJSONObject("data").getInt("count");
				final JSONObject response = new JSONObject("{status: \"ok\", message: "+count + " records have been inserted}");
				return ResponseEntity.ok().body(response.toString());
			}
		} catch (final IOException e) {
			return new ResponseEntity<>("There was an error parsing the data to insert: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String getJsonFromFile(MultipartFile file) {
		String jsonData = null;
		try {
			final String contentType = file.getContentType();
			final String fileName = file.getOriginalFilename();
			final InputStreamReader inputStream =  new InputStreamReader(file.getInputStream());

			if (contentType.equals("text/csv") || fileName.contains(".csv")) {
				jsonData = resourceService.getJsonFromCSV(inputStream);
			} else if (contentType.equals("text/xml") || contentType.equals("application/xml") || fileName.contains(".xml")) {
				jsonData = resourceService.getJsonFromXML(inputStream);
			} else if (contentType.equals("application/json") || fileName.contains(".json")) {
				final BufferedReader reader = new BufferedReader(inputStream);
				final StringBuilder responseStrBuilder = new StringBuilder();
				String str;
				while((str = reader.readLine())!= null){
					responseStrBuilder.append(str);
				}
				jsonData = responseStrBuilder.toString();
			}

			if (jsonData != null && !jsonData.equals("") && jsonData.substring(0,1).equals("{")) {
				final Map<String, Object> obj = mapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {});
				final List<Map<String,Object>> result = resourceService.processMap(obj);
				jsonData = mapper.writeValueAsString(result);
			}
			return jsonData;
		} catch (final IOException e1) {
			e1.printStackTrace();
			return jsonData;
		}
	}

	private String getFirstElement(String jsonData) {
		final JSONArray jsonArray = new JSONArray(jsonData);
		final JSONObject firstElement = jsonArray.getJSONObject(0);
		return firstElement.toString();
	}
}
