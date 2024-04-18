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
package com.minsait.onesait.platform.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.minsait.onesait.platform.bean.CrudParamsDTO;
import com.minsait.onesait.platform.bean.OntologyCrudDTO;
import com.minsait.onesait.platform.business.services.crud.CrudService;
import com.minsait.onesait.platform.business.services.resources.ResourceServiceImpl;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyFieldDTO;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.security.AppWebUtils;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class EngineRestController {

	@Autowired
	private CrudService crudService;
	@Autowired
	private OntologyService ontologyConfigService;
	@Autowired
	private ResourceServiceImpl resourceService;
	@Autowired
	private IntegrationResourcesService integrationResourcesService;
	@Autowired
	private AppWebUtils utils;

	@Autowired
	private OntologyService ontologyService;

	@Value("${onesaitplatform.binary-repository.tmp.file.path:/tmp/files/}")
	private String tmpDir;
	private ObjectMapper mapper = new ObjectMapper();

	@Value("${onesaitplatform.database.mongodb.queries.defaultLimit:1000}")
	private int queryDefaultLimit;

	private static final String ERROR_TRUE = "{\"error\":\"true\"}";
	private static final String EMPTY = "[]";

	@GetMapping(value = "/getEntityCrudInfo/{id}")
	public @ResponseBody ResponseEntity<OntologyCrudDTO> getEntityCrudInfo(@PathVariable("id") String id) {
		final Ontology ontology = ontologyService.getOntologyByIdInsert(id, utils.getUserId());
		final OntologyCrudDTO ontologyDTO = new OntologyCrudDTO();
		ontologyDTO.setIdentification(ontology.getIdentification());
		ontologyDTO.setJsonSchema(ontology.getJsonSchema());
		ontologyDTO.setDatasource(ontology.getRtdbDatasource().name());
		ontologyDTO.setUniqueId(crudService.getUniqueColumn(ontology.getIdentification(), false));
		ontologyDTO.setQuasar(crudService.useQuasar());
		return new ResponseEntity<>(ontologyDTO, HttpStatus.OK);
	}

	@GetMapping(value = "/getOntologyFieldsAndDesc/{id}")
	public @ResponseBody ResponseEntity<Map<String, OntologyFieldDTO>> getOntologyFieldsAndDesc(
			@PathVariable("id") String id) throws IOException {
		Map<String, OntologyFieldDTO> ontology = ontologyService.getOntologyFieldsAndDesc(id, utils.getUserId());
		// clean duplicates
		if (ontology != null && ontology.size() > 1) {
			Set<String> keyset = ontology.keySet();
			Map<String, Integer> result = new TreeMap<>();

			for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
				String keyObserved = (String) iterator.next();
				result.put(keyObserved, 0);
				for (Iterator iterator2 = keyset.iterator(); iterator2.hasNext();) {
					String otherKey = (String) iterator2.next();
					if (otherKey.contains(keyObserved + '.')) {
						result.replace(keyObserved, result.get(keyObserved) + 1);
					}
				}
			}
			for (Map.Entry<String, Integer> entry : result.entrySet()) {
				if (entry.getValue() > 1) {
					ontology.remove(entry.getKey());
				}
			}
		}
		return new ResponseEntity<>(ontology, HttpStatus.OK);
	}

	@GetMapping(value = "/getEntities")
	public @ResponseBody ResponseEntity<List<com.minsait.onesait.platform.bean.OntologyDTO>> getOntologies() {
		final List<OntologyDTO> ontologies = ontologyConfigService.getAllOntologiesForList(utils.getUserId(), "", "",
				"", "");
		List<com.minsait.onesait.platform.bean.OntologyDTO> ontologiesResult = new ArrayList<com.minsait.onesait.platform.bean.OntologyDTO>();
		if (ontologies != null && ontologies.size() > 0) {
			for (Iterator iterator = ontologies.iterator(); iterator.hasNext();) {
				OntologyDTO ontologyDTO = (OntologyDTO) iterator.next();
				if (!ontologyDTO.getIsAuthorizationsPermissions().equals("ALL")) {
					iterator.remove();
				}

			}
			ontologiesResult = mapOntolotyDTO(ontologies);
		}
		return new ResponseEntity<>(ontologiesResult, HttpStatus.OK);
	}

	@GetMapping(value = "/getEntitiesQueryPermission")
	public @ResponseBody ResponseEntity<List<com.minsait.onesait.platform.bean.OntologyDTO>> getEntitiesQueryPermission() {
		final List<OntologyDTO> ontologies = ontologyConfigService.getAllOntologiesForList(utils.getUserId(), "", "",
				"", "");
		List<com.minsait.onesait.platform.bean.OntologyDTO> ontologiesResult = new ArrayList<com.minsait.onesait.platform.bean.OntologyDTO>();
		if (ontologies != null && ontologies.size() > 0) {
			for (Iterator iterator = ontologies.iterator(); iterator.hasNext();) {
				OntologyDTO ontologyDTO = (OntologyDTO) iterator.next();

			}
			ontologiesResult = mapOntolotyDTO(ontologies);
		}
		return new ResponseEntity<>(ontologiesResult, HttpStatus.OK);
	}

	private List<com.minsait.onesait.platform.bean.OntologyDTO> mapOntolotyDTO(List<OntologyDTO> ontologies) {
		List<com.minsait.onesait.platform.bean.OntologyDTO> ontolgiesResult = new ArrayList<com.minsait.onesait.platform.bean.OntologyDTO>();
		for (OntologyDTO ontologyDTO : ontologies) {
			com.minsait.onesait.platform.bean.OntologyDTO onto = new com.minsait.onesait.platform.bean.OntologyDTO();
			onto.setActive(ontologyDTO.isActive());

			onto.setCreatedAt(ontologyDTO.getCreatedAt());
			onto.setDataModel(ontologyDTO.getDataModel());
			onto.setDescription(ontologyDTO.getDescription());
			onto.setId(ontologyDTO.getId());
			onto.setIdentification(ontologyDTO.getIdentification());
			onto.setPublic(ontologyDTO.isPublic());
			onto.setRtdbDatasource(ontologyDTO.getRtdbDatasource());
			onto.setRtdbDatasourceType(ontologyDTO.getRtdbDatasourceType());
			onto.setUpdatedAt(ontologyDTO.getUpdatedAt());
			onto.setUser(ontologyDTO.getUser().getUserId());
			ontolgiesResult.add(onto);
		}

		return ontolgiesResult;

	}

	@RequestMapping(path = "/queryParams", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String queryParams(@Valid @RequestBody final SelectStatement selectStatement,
			final BindingResult result) {
		try {
			if (!result.hasErrors()) {

				ontologyService.getOntologyByIdentificationInsert(selectStatement.getOntology(), utils.getUserId());

				return crudService.queryParams(selectStatement, utils.getUserId());
			} else {
				throw new IllegalArgumentException("Parameters could not be mapped to a select statement");
			}
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/findById" }, produces = "application/json")
	public @ResponseBody String findById(@RequestBody final CrudParamsDTO crudDTO) {
		return crudService.findById(crudDTO.getOntologyID(), crudDTO.getOid(), utils.getUserId());
	}

	@PostMapping(value = { "/deleteById" }, produces = "application/json")
	public @ResponseBody String deleteById(@RequestBody final CrudParamsDTO crudDTO) {
		try {
			ontologyService.getOntologyByIdInsert(crudDTO.getOntologyID(), utils.getUserId());
			return crudService.processQuery("", crudDTO.getOntologyID(), ApiOperation.Type.DELETE, "", crudDTO.getOid(),
					utils.getUserId());
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/insert" }, produces = "application/json")
	public @ResponseBody String insert(@RequestBody final CrudParamsDTO crudDTO) {
		try {
			ontologyService.getOntologyByIdInsert(crudDTO.getOntologyID(), utils.getUserId());
			return crudService.processQuery("", crudDTO.getOntologyID(), ApiOperation.Type.POST, crudDTO.getData(), "",
					utils.getUserId());
		} catch (final Exception e) {
			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/update" }, produces = "application/json")
	public @ResponseBody String update(@RequestBody final CrudParamsDTO crudDTO) {
		try {
			ontologyService.getOntologyByIdInsert(crudDTO.getOntologyID(), utils.getUserId());
			return crudService.processQuery("", crudDTO.getOntologyID(), ApiOperation.Type.PUT, crudDTO.getData(),
					crudDTO.getOid(), utils.getUserId());
		} catch (final Exception e) {
			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/isComplexSchema" }, produces = "application/json")
	public @ResponseBody String isComplexSchema(@RequestBody final CrudParamsDTO crudDTO) {
		final Ontology ontology = ontologyService.getOntologyByIdentification(crudDTO.getOntologyID(),
				utils.getUserId());
		JSONObject jsonSchema = new JSONObject(ontology.getJsonSchema());
		JSONObject schema;
		try {
			schema = jsonSchema.getJSONObject("datos");
			schema = schema.getJSONObject("properties");
			JSONObject prop = jsonSchema.getJSONObject("properties");
			String rootName = prop.keys().next();
			String finalschema = "{\"" + rootName + "\": {\"type\":\"object\",\"properties\":" + schema.toString()
					+ "}}";
			schema = new JSONObject(finalschema);
		} catch (JSONException e) {
			schema = jsonSchema.getJSONObject("properties");
		}
		return containsComplexStructure(schema);
	}

	private String containsComplexStructure(JSONObject schema) {
		Iterator<String> it = schema.keys();
		String result = "";
		while (it.hasNext()) {
			String key = it.next();
			String type = schema.get(key).toString();
			JSONObject jsonType = new JSONObject(type);
			Object compareType = jsonType.get("type");

			if (compareType.toString().equalsIgnoreCase("array") || compareType instanceof JSONArray) {
				Iterator<String> it2 = jsonType.keys();
				while (it2.hasNext()) {
					String otherkey = it2.next();
					if (otherkey.equalsIgnoreCase("items")) {
						JSONArray items;
						try {
							items = jsonType.getJSONArray(otherkey);
						} catch (JSONException e) {
							String errmsg = "{\"message\":\"error.message.malformed.array\", \"detail\":\"\"}";
							return errmsg;
						}
						for (int i = 0; i < items.length(); i++) {
							JSONObject item = items.getJSONObject(i);
							if (item.getString("type").equalsIgnoreCase("object")) {
								// array de objetos
								String errmsg = "{\"message\":\"error.message.csvformat\", \"detail\":\"\"}";
								return errmsg;
							}
							try {
								// array de array
								JSONArray items2 = item.getJSONArray("items");
								for (int j = 0; j < items2.length(); j++) {
									JSONObject item2 = items2.getJSONObject(j);
									try {
										item2.getString("type");
									} catch (Exception e) {
										// array nivel 3 isComplexStructure
										String errmsg = "{\"message\":\"error.message.csvformat\", \"detail\":\"\"}";
										return errmsg;
									}
								}
							} catch (JSONException e) {
								// array simple
							}
						}
					}
				}
			} else if (compareType.toString().equalsIgnoreCase("object")) {
				JSONObject newschema = schema.getJSONObject(key);
				newschema = newschema.getJSONObject("properties");
				result = containsComplexStructure(newschema);
			}
		}
		if (result == "") {
			result = "{\"message\":\"ok\"}";
		}
		return result;
	}

	@GetMapping(value = "/downloadEntitySchemaCsv/{ontologyName}")
	public @ResponseBody ResponseEntity<InputStreamResource> downloadEntitySchemaCsv(
			@PathVariable("ontologyName") String ontologyName) throws Exception {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyName, utils.getUserId());
		JSONObject jsonSchema = new JSONObject(ontology.getJsonSchema());
		JSONObject schema;
		try {
			schema = jsonSchema.getJSONObject("datos");
			schema = schema.getJSONObject("properties");
			JSONObject prop = jsonSchema.getJSONObject("properties");
			String rootName = prop.keys().next();
			String finalschema = "{\"" + rootName + "\": {\"type\":\"object\",\"properties\":" + schema.toString()
					+ "}}";
			schema = new JSONObject(finalschema);
		} catch (JSONException e) {
			schema = jsonSchema.getJSONObject("properties");
		}

		HashMap<String, String> headerOntology = getHeaderByOntologySchema(schema);
		HashMap<String, String> headerOntologySorted = headerOntology.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
		String keys = new ObjectMapper().writeValueAsString(headerOntologySorted);
		keys = "[" + keys + "]";

		JsonNode jsonTree = new ObjectMapper().readTree(keys);
		List<String[]> csvData = new ArrayList<>();
		JsonNode firstObject = jsonTree.elements().next();
		List<String> headers = new ArrayList<>();
		firstObject.fieldNames().forEachRemaining(fieldName -> {
			headers.add(fieldName);
		});
		csvData.add(headers.toArray(new String[0]));
		Iterator<JsonNode> iterator = jsonTree.elements();
		while (iterator.hasNext()) {
			JsonNode obj = iterator.next();
			List<String> data = new ArrayList<>();
			obj.fields().forEachRemaining(field -> {
				JsonNode node = field.getValue();
				if (node.isObject())
					try {
						data.add(new ObjectMapper().writeValueAsString(field.getValue()));
					} catch (JsonProcessingException e) {
						log.error("Error parsing query result to CSV format to export", e);
					}
				else
					data.add(field.getValue().asText());
			});
			csvData.add(data.toArray(new String[0]));
		}
		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".csv");
		CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		writer.writeAll(csvData);
		writer.close();
		File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".csv");

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/downloadEntitySchemaJson/{ontologyName}")
	public @ResponseBody ResponseEntity<InputStreamResource> downloadEntitySchemaJson(
			@PathVariable("ontologyName") String ontologyName) throws Exception {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyName, utils.getUserId());
		JSONObject jsonSchema = new JSONObject(ontology.getJsonSchema());
		JSONObject schema;
		try {
			schema = jsonSchema.getJSONObject("datos");
			schema = schema.getJSONObject("properties");
			JSONObject prop = jsonSchema.getJSONObject("properties");
			String rootName = prop.keys().next();
			String finalschema = "{\"" + rootName + "\": {\"type\":\"object\",\"properties\":" + schema.toString()
					+ "}}";
			schema = new JSONObject(finalschema);
		} catch (JSONException e) {
			schema = jsonSchema.getJSONObject("properties");
		}

		String result = "";
		result = createJsonSchema(schema, result);
		if (result.substring(result.length() - 1).equals(",")) {
			result = result.substring(0, result.length() - 1);
		}
		result = "[" + result + "]";

		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".json");

		outputfile.write(result);
		outputfile.close();

		File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".json");

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@PostMapping(value = { "/insertDataEntity/{ontologyName}" }, produces = "application/json")
	public ResponseEntity<String> insertDataEntity(@PathVariable("ontologyName") String ontologyName,
			@RequestPart("file") MultipartFile file) {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyName);
		if (ontology == null) {
			return new ResponseEntity<>("{message:\"error.message.exists\", detail:\"\"}", HttpStatus.NOT_FOUND);
		}

		String userId = utils.getUserId();
		String jsonData = null;
		try {
			jsonData = getJsonFromFile(file);
		} catch (Exception e) {
			return new ResponseEntity<>("{message:\"error.message.csvseparator\", detail:\"\"}",
					HttpStatus.NOT_ACCEPTABLE);
		}

		if (jsonData == null || jsonData.equals("")) {
			return new ResponseEntity<>("{message:\"error.message.fileType\", detail:\"\"}", HttpStatus.NOT_ACCEPTABLE);
		}

		try {
			OperationResultModel result = resourceService.insertDataIntoOntology(ontologyName, jsonData, userId);
			String resultMessage = result.getMessage();
			if (!resultMessage.equals("OK")) {
				if (resultMessage.startsWith("Error processing data:")) {
					String[] detailmsga = resultMessage.split("by:", 2);
					String detailmsg = detailmsga[1].replaceAll("}", "},");
					detailmsg = detailmsg.replaceAll(",,", ",");
					detailmsg = detailmsg.substring(0, detailmsg.length() - 1);
					detailmsg = "[" + detailmsg + "]";

					JSONObject errorResponse = new JSONObject(
							"{message: \"error.message.processing\", detail: " + detailmsg + "}");
					return new ResponseEntity<>(errorResponse.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					return new ResponseEntity<>("{message: \"error.message.insert\", detail: \""
							+ result.getMessage().replaceAll("\"", "'") + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				JSONObject jsonResult = new JSONObject(result.getResult());
				JSONObject response;
				try {
					int count = jsonResult.getJSONObject("data").getInt("count");
					response = new JSONObject("{status: \"ok\", message: \"" + count + "\"}");
				} catch (JSONException e) {
					response = new JSONObject("{status: \"ok\", message: \"\"}");
				}
				return ResponseEntity.ok().body(response.toString());
			}
		} catch (IOException e) {
			return new ResponseEntity<>(
					"{message: \"error.message.parsing\", detail: \"" + e.getMessage().replaceAll("\"", "'") + "\"}",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private List<String> getHeader(String ontologyName) {
		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyName, utils.getUserId());
		JSONObject jsonSchema = new JSONObject(ontology.getJsonSchema());
		JSONObject schema;
		try {
			schema = jsonSchema.getJSONObject("datos");
			schema = schema.getJSONObject("properties");
			JSONObject prop = jsonSchema.getJSONObject("properties");
			String rootName = prop.keys().next();
			String finalschema = "{\"" + rootName + "\": {\"type\":\"object\",\"properties\":" + schema.toString()
					+ "}}";
			if (!ontology.getRtdbDatasource().equals(Ontology.RtdbDatasource.TIMESCALE)) {
				schema = new JSONObject(finalschema);
			}
		} catch (JSONException e) {
			schema = jsonSchema.getJSONObject("properties");
		}

		HashMap<String, String> headerOntology = getHeaderByOntologySchema(schema);
		HashMap<String, String> headerOntologySorted = headerOntology.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));

		ArrayList<String> result = new ArrayList<>(headerOntologySorted.keySet());

		return result;
	}

	private List<String> getHeaderOntology(JSONObject json) {

		HashMap<String, String> result = new HashMap<String, String>();
		Iterator<String> it = json.keys();

		while (it.hasNext()) {
			String newKey = it.next();
			if (!newKey.equals("_id") && !newKey.equals("contextData")) {
				inspectJson(result, newKey, newKey, json);
			}
		}
		ArrayList<String> arrayList = new ArrayList<>(result.keySet());
		return arrayList;
	}

	private void inspectJson(HashMap<String, String> result, String key, String complete, JSONObject json) {
		if (json.get(key) instanceof JSONObject) {
			JSONObject sonJson = json.getJSONObject(key);
			Iterator<String> it = sonJson.keys();
			while (it.hasNext()) {
				String newKey = it.next();
				inspectJson(result, newKey, complete + "." + newKey, sonJson);
			}
		} else {
			result.put(complete, complete);
		}
	}

	private HashMap<String, String> getHeaderByOntologySchema(JSONObject json) {
		HashMap<String, String> result = new HashMap<String, String>();
		Iterator<String> it = json.keys();

		while (it.hasNext()) {
			String newKey = it.next();
			inspectOntoSchemaJson(result, newKey, newKey, json, false);
		}
		return result;
	}

	private void inspectOntoSchemaJson(HashMap<String, String> result, String key, String complete, JSONObject json,
			boolean secondround) {
		if (json.get(key) instanceof JSONObject) {
			JSONObject jsonType = json.getJSONObject(key);
			Iterator<String> it = jsonType.keys();
			while (it.hasNext()) {
				String newKey = it.next();
				if (secondround) {
					inspectOntoSchemaJson(result, newKey, complete + "." + newKey, jsonType, false);
				} else if (newKey.equalsIgnoreCase("type")) {
					String type = "";
					if (jsonType.get(newKey) instanceof JSONArray) {
						JSONArray arrList = (JSONArray) jsonType.get(newKey);
						for (Iterator iterator = arrList.iterator(); iterator.hasNext();) {
							String arrayType = (String) iterator.next();
							if (!arrayType.equals("null")) {
								type = arrayType;
								break;
							}

						}
					} else {
						type = jsonType.getString(newKey);
					}
					if (type.equalsIgnoreCase("object")) {
						while (it.hasNext()) {
							String otherkey = it.next();
							if (otherkey.equalsIgnoreCase("properties")) {
								inspectOntoSchemaJson(result, otherkey, complete, jsonType, true);
							}
						}
					} else if (type.equalsIgnoreCase("array")) {
						while (it.hasNext()) {
							String otherkey = it.next();
							if (otherkey.equalsIgnoreCase("items")) {
								JSONArray items = jsonType.getJSONArray(otherkey);
								for (int i = 0; i < items.length(); i++) {
									JSONObject item = items.getJSONObject(i);
									try {
										// array de array
										JSONArray items2 = item.getJSONArray("items");
										for (int j = 0; j < items2.length(); j++) {
											JSONObject item2 = items2.getJSONObject(j);
											try {
												String itemType = item2.getString("type");
												String exampleType = getExampleType(itemType, item2);
												result.put(complete + "." + i + "." + j, exampleType);
											} catch (Exception e) {
												log.error("Error complex structure");
											}
										}
									} catch (JSONException e) {
										// array simple
										String itemType = item.getString("type");
										String exampleType = getExampleType(itemType, item);
										result.put(complete + "." + i, exampleType);
									}
								}
							}
						}
					} else {
						String exampleType = getExampleType(type, jsonType);
						result.put(complete, exampleType);
					}
				}
			}
		} else {
			String typeS = json.getString("type");
			String exampleType = getExampleType(typeS, json);
			result.put(complete, exampleType);
		}
	}

	private String getExampleType(String type, JSONObject json) {
		String format;
		try {
			format = json.getString("format");
		} catch (Exception e) {
			format = "";
		}
		if (type.equalsIgnoreCase("string") && format.equalsIgnoreCase("date-time")) {
			return "1999-01-01T00:00:00.000Z";
		} else if (type.equalsIgnoreCase("string") && format.equalsIgnoreCase("date")) {
			return "1999-01-01";
		} else if (type.equalsIgnoreCase("string")) {
			return "textExample";
		} else if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("number")) {
			return "0";
		} else if (type.contains("boolean")) {
			return "false";
		} else {
			return "";
		}
	}

	private String createJsonSchema(JSONObject schema, String result) {
		Iterator<String> it = schema.keys();
		while (it.hasNext()) {
			String property = it.next();
			if (result.isEmpty()) {
				result = "{\"" + property + "\": ";
			} else {
				result += "\"" + property + "\": ";
			}
			if (schema.get(property) instanceof JSONObject) {
				JSONObject jsonType = schema.getJSONObject(property);
				Iterator<String> it2 = jsonType.keys();
				while (it2.hasNext()) {
					String newKey = it2.next();
					if (newKey.equalsIgnoreCase("type")) {
						String type = jsonType.getString(newKey);
						if (type.equalsIgnoreCase("object")) {
							while (it2.hasNext()) {
								String otherkey = it2.next();
								if (otherkey.equalsIgnoreCase("properties")) {
									JSONObject properties = jsonType.getJSONObject(otherkey);
									result = createJsonSchema(properties, result + "{");
								}
							}
						} else if (type.equalsIgnoreCase("array")) {
							while (it2.hasNext()) {
								String otherkey = it2.next();
								if (otherkey.equalsIgnoreCase("items")) {
									JSONArray items = jsonType.getJSONArray(otherkey);
									result = getArrayResult(result, items);
								}
							}
						} else {
							String example = getExampleType(type, jsonType);
							if (isNumeric(example)) {
								result += example + ",";
							} else {
								result += "\"" + example + "\",";
							}
						}
					}
				}
			}
		}
		if (result.substring(result.length() - 1).equals(",")) {
			result = result.substring(0, result.length() - 1);
		}
		result += "},";
		return result;
	}

	private String getArrayResult(String result, JSONArray items) {
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			if (i == 0)
				result += "[";
			try {
				// array de array
				JSONArray items2 = item.getJSONArray("items");
				result = getArrayResult(result, items2);
			} catch (JSONException e) {
				// array simple
				String itemType = item.getString("type");
				if (itemType.equalsIgnoreCase("object")) {
					JSONObject properties = item.getJSONObject("properties");
					result = createJsonSchema(properties, result + "{");
				} else {
					String example = getExampleType(itemType, item);
					if (isNumeric(example)) {
						result += example + ",";
					} else {
						result += "\"" + example + "\",";
					}
				}
			}
			if (i == items.length() - 1) {
				if (result.substring(result.length() - 1).equals(",")) {
					result = result.substring(0, result.length() - 1);
				}
				result += "],";
			}
		}
		return result;
	}

	private String[] parseToCsv(JSONObject jObj, List<String> headerList) {
		ArrayList<String> result = new ArrayList<String>();
		for (Iterator iterator = headerList.iterator(); iterator.hasNext();) {
			String column = (String) iterator.next();
			result.add(recursiveParseObjCsv(jObj, column));
		}
		return result.toArray(new String[0]);
	}

	private String recursiveParseObjCsv(JSONObject jObj, String key) {
		StringTokenizer tokens = new StringTokenizer(key, ".");
		JSONObject sonJson = jObj;
		while (tokens.hasMoreTokens()) {
			String subKey = tokens.nextToken();
			if (sonJson.get(subKey) instanceof JSONObject) {
				sonJson = sonJson.getJSONObject(subKey);
			} else if (sonJson.get(subKey) instanceof JSONArray) {
				JSONArray jarray = (JSONArray) sonJson.get(subKey);
				subKey = tokens.nextToken();
				if (jarray.get(Integer.valueOf(subKey)) instanceof JSONObject) {
					sonJson = (JSONObject) jarray.get(Integer.valueOf(subKey));
				} else if (jarray.get(Integer.valueOf(subKey)) instanceof JSONArray) {
					JSONArray jarrayNested = (JSONArray) jarray.get(Integer.valueOf(subKey));
					subKey = tokens.nextToken();
					if (jarrayNested.get(Integer.valueOf(subKey)) instanceof JSONObject) {
						sonJson = (JSONObject) jarrayNested.get(Integer.valueOf(subKey));
					} else if (jarrayNested.get(Integer.valueOf(subKey)) instanceof JSONArray) {
						return null;
					} else {
						if (jarrayNested.get(Integer.valueOf(subKey)) instanceof String) {
							return "\"" + jarrayNested.get(Integer.valueOf(subKey)) + "\"";
						} else if (sonJson.isNull(subKey)) {
							return null;
						} else {
							return String.valueOf(jarrayNested.get(Integer.valueOf(subKey)));
						}
					}
				} else {
					if (jarray.get(Integer.valueOf(subKey)) instanceof String) {
						return "\"" + jarray.get(Integer.valueOf(subKey)) + "\"";
					} else if (sonJson.isNull(subKey)) {
						return null;
					} else {
						return String.valueOf(jarray.get(Integer.valueOf(subKey)));
					}
				}
			} else {
				return returnValue(sonJson, subKey);
			}
		}
		return null;
	}

	private String returnValue(JSONObject sonJson, String subKey) {
		if (sonJson.get(subKey) instanceof String) {
			return "\"" + sonJson.get(subKey) + "\"";
		} else if (sonJson.isNull(subKey)) {
			return null;
		} else {
			return String.valueOf(sonJson.get(subKey));
		}
	}

	@GetMapping(value = "/downloadEntityAllCsv/{ontologyName}")
	public @ResponseBody ResponseEntity<InputStreamResource> downloadEntityAllCsv(
			@PathVariable("ontologyName") String ontologyName) throws Exception {
		String suboutput = "";
		int offset = 0;

		SelectStatement selectStatement = new SelectStatement();
		selectStatement.setOntology(ontologyName);
		selectStatement.setLimit(1);
		selectStatement.setOffset(offset);
		suboutput = crudService.queryParams(selectStatement, utils.getUserId());

		JSONArray jArray = new JSONArray(suboutput);
		// empty file
		if (jArray.length() == 0) {
			final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
			FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".csv");
			CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
			writer.close();
			File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".csv");

			final HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
			respHeaders.setContentLength(finalFile.length());
			final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
			deleteDirectory(finalFile);
			return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
		}
		// JSONObject firstElement = jArray.getJSONObject(0);
		List<String> headerList = getHeader(ontologyName);

		List<String[]> csvData = new ArrayList<>();
		csvData.add(headerList.toArray(new String[0]));
		long max = getMaxRegisters();
		while (!suboutput.replaceAll("\\s", "").equals(EMPTY)) {
			selectStatement = new SelectStatement();
			selectStatement.setOntology(ontologyName);
			selectStatement.setLimit(max);
			selectStatement.setOffset(offset);
			suboutput = crudService.queryParams(selectStatement, utils.getUserId());
			if (!suboutput.replaceAll("\\s", "").equals(EMPTY)) {
				offset += max;
				JSONArray jsonArray = new JSONArray(suboutput);
				for (Object o : jsonArray) {
					if (o instanceof JSONObject) {
						csvData.add(parseToCsv((JSONObject) o, headerList));
					}
				}

			}
		}
		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".csv");
		CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END);
		writer.writeAll(csvData);
		writer.close();
		File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".csv");

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/downloadEntityAllJson/{ontologyName}")
	public @ResponseBody ResponseEntity<InputStreamResource> downloadEntityAllJson(
			@PathVariable("ontologyName") String ontologyName) throws Exception {
		String outputJsonFile = "";
		String suboutput = "";
		int offset = 0;
		long max = getMaxRegisters();
		while (!suboutput.replaceAll("\\s", "").equals(EMPTY)) {
			SelectStatement selectStatement = new SelectStatement();
			selectStatement.setOntology(ontologyName);
			selectStatement.setLimit(max);
			selectStatement.setOffset(offset);
			suboutput = crudService.queryParams(selectStatement, utils.getUserId());
			if (!suboutput.replaceAll("\\s", "").equals(EMPTY)) {
				offset += max;
				JSONArray jsonArray = new JSONArray(suboutput);
				suboutput = deleteIdAndContext(jsonArray);
				suboutput = suboutput.replaceFirst("\\[", "");
				StringBuilder sb = new StringBuilder(suboutput);
				int i = suboutput.lastIndexOf("]");
				sb.deleteCharAt(i);
				suboutput = sb.toString() + ",";
				outputJsonFile = outputJsonFile + suboutput;
			}
		}

		StringBuilder sb = new StringBuilder(outputJsonFile);
		int i = outputJsonFile.lastIndexOf(",");
		sb.deleteCharAt(i);
		outputJsonFile = "[" + sb.toString() + "]";

		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".json");

		outputfile.write(outputJsonFile);
		outputfile.close();

		File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".json");

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/downloadEntitySelectedCsv/{ontologyName}")
	public @ResponseBody ResponseEntity<InputStreamResource> downloadEntitySelectedCsv(
			@PathVariable("ontologyName") String ontologyName, @RequestParam String selec) throws Exception {

		String suboutput = "";
		int offset = 0;

		SelectStatement selectStatement = new SelectStatement();
		selectStatement.setOntology(ontologyName);
		selectStatement.setLimit(1);
		selectStatement.setOffset(offset);
		suboutput = crudService.queryParams(selectStatement, utils.getUserId());

		JSONArray jArray = new JSONArray(suboutput);
		if (jArray.length() == 0) {
			final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
			FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".csv");
			CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
			writer.close();
			File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".csv");

			final HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
			respHeaders.setContentLength(finalFile.length());
			final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
			deleteDirectory(finalFile);
			return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
		}

		List<String> headerList = getHeader(ontologyName);

		List<String[]> csvData = new ArrayList<>();
		csvData.add(headerList.toArray(new String[0]));

		ObjectMapper objectMapper = new ObjectMapper();
		SelectStatement selectStat = objectMapper.readValue(selec, SelectStatement.class);

		int max = getMaxRegisters();
		long total = 0;
		long setedLimit = 0;
		if (selectStat.getLimit() != null) {
			if (selectStat.getLimit() > max) {
				total = selectStat.getLimit();
				setedLimit = max;
			} else {
				total = selectStat.getLimit();
				setedLimit = selectStat.getLimit();
			}
		}
		while (total > 0) {
			selectStatement = objectMapper.readValue(selec, SelectStatement.class);
			selectStatement.setOntology(ontologyName);
			selectStatement.setLimit(setedLimit);
			selectStatement.setOffset(offset);
			suboutput = crudService.queryParams(selectStatement, utils.getUserId());
			if (suboutput.replaceAll("\\s", "").equals(EMPTY)) {
				total = 0;

			} else {
				JSONArray jsonArray = new JSONArray(suboutput);
				for (Object o : jsonArray) {
					if (o instanceof JSONObject) {
						csvData.add(parseToCsv((JSONObject) o, headerList));
					}
				}

				total = total - setedLimit;
				if (total > 0) {
					if (total >= max) {
						offset += max;
						setedLimit = max;
					} else {
						offset += max;
						setedLimit = total;
					}
				}
			}
		}
		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".csv");
		CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END);
		writer.writeAll(csvData);
		writer.close();
		File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".csv");

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
	}

	@GetMapping(value = "/validationDownloadEntitySelected/{ontologyName}/{type}")
	public @ResponseBody String validationDownloadEntitySelected(@PathVariable("ontologyName") String ontologyName,
			@PathVariable("type") String type, @RequestParam String selec) throws Exception {
		String okmsg = "{\"message\":\"ok\"}";
		String emptymsg = "{\"message\":\"error.message.empty\"}";
		String errmsg = "{\"message\":\"error.message.download\"}";
		if (type.equals("csv")) {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyName, utils.getUserId());
			JSONObject jsonSchema = new JSONObject(ontology.getJsonSchema());
			JSONObject schema;
			try {
				schema = jsonSchema.getJSONObject("datos");
				schema = schema.getJSONObject("properties");
				JSONObject prop = jsonSchema.getJSONObject("properties");
				String rootName = prop.keys().next();
				String finalschema = "{\"" + rootName + "\": {\"type\":\"object\",\"properties\":" + schema.toString()
						+ "}}";
				schema = new JSONObject(finalschema);
			} catch (JSONException e) {
				schema = jsonSchema.getJSONObject("properties");
			}
			String iscomplex = containsComplexStructure(schema);

			if (!iscomplex.equals(okmsg)) {
				return iscomplex;
			}
		}
		String suboutput = "";
		ObjectMapper objectMapper = new ObjectMapper();
		SelectStatement selectStat = objectMapper.readValue(selec, SelectStatement.class);
		selectStat.setOntology(ontologyName);
		selectStat.setLimit(1);
		selectStat.setOffset(0);
		suboutput = crudService.queryParams(selectStat, utils.getUserId());
		JSONArray jArray = new JSONArray(suboutput);
		if (jArray.length() == 0) {
			return emptymsg;
		}
		try {
			JSONObject firstElement = jArray.getJSONObject(0);
			List<String> headerList = getHeaderOntology(firstElement);
		} catch (Exception e) {
			return errmsg;
		}
		return okmsg;
	}

	@GetMapping(value = "/validationDownloadEntity/{ontologyName}/{type}")
	public @ResponseBody String validationDownloadEntity(@PathVariable("ontologyName") String ontologyName,
			@PathVariable("type") String type) throws Exception {

		String okmsg = "{\"message\":\"ok\"}";
		String emptymsg = "{\"message\":\"error.message.empty\"}";
		String errmsg = "{\"message\":\"error.message.download\"}";
		if (type.equals("csv")) {
			final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyName, utils.getUserId());
			JSONObject jsonSchema = new JSONObject(ontology.getJsonSchema());
			JSONObject schema;
			try {
				schema = jsonSchema.getJSONObject("datos");
				schema = schema.getJSONObject("properties");
				JSONObject prop = jsonSchema.getJSONObject("properties");
				String rootName = prop.keys().next();
				String finalschema = "{\"" + rootName + "\": {\"type\":\"object\",\"properties\":" + schema.toString()
						+ "}}";
				schema = new JSONObject(finalschema);
			} catch (JSONException e) {
				schema = jsonSchema.getJSONObject("properties");
			}
			String iscomplex = containsComplexStructure(schema);

			if (!iscomplex.equals(okmsg)) {
				return iscomplex;
			}
		}
		String suboutput = "";
		int offset = 0;

		SelectStatement selectStatement = new SelectStatement();
		selectStatement.setOntology(ontologyName);
		selectStatement.setLimit(1);
		selectStatement.setOffset(offset);
		suboutput = crudService.queryParams(selectStatement, utils.getUserId());

		JSONArray jArray = new JSONArray(suboutput);

		if (jArray.length() == 0) {
			return emptymsg;
		}
		try {
			JSONObject firstElement = jArray.getJSONObject(0);
			List<String> headerList = getHeaderOntology(firstElement);
		} catch (Exception e) {
			return errmsg;
		}
		return okmsg;
	}

	@GetMapping(value = "/downloadEntitySelectedJson/{ontologyName}")
	public @ResponseBody ResponseEntity<InputStreamResource> downloadEntitySelectedJson(
			@PathVariable("ontologyName") String ontologyName, @RequestParam String selec) throws Exception {
		String outputJsonFile = "";
		String suboutput = "";
		int offset = 0;

		ObjectMapper objectMapper = new ObjectMapper();

		SelectStatement selectStat = objectMapper.readValue(selec, SelectStatement.class);

		int max = getMaxRegisters();
		long total = 0;
		long setedLimit = 0;
		if (selectStat.getLimit() != null) {
			if (selectStat.getLimit() > max) {
				total = selectStat.getLimit();
				setedLimit = max;
			} else {
				total = selectStat.getLimit();
				setedLimit = selectStat.getLimit();
			}
		}
		while (total > 0) {
			SelectStatement selectStatement = objectMapper.readValue(selec, SelectStatement.class);
			selectStatement.setOntology(ontologyName);
			selectStatement.setLimit(setedLimit);
			selectStatement.setOffset(offset);
			suboutput = crudService.queryParams(selectStatement, utils.getUserId());
			if (suboutput.replaceAll("\\s", "").equals(EMPTY)) {
				total = 0;
				outputJsonFile = outputJsonFile + ",";
			} else {
				JSONArray jsonArray = new JSONArray(suboutput);
				suboutput = deleteIdAndContext(jsonArray);
				suboutput = suboutput.replaceFirst("\\[", "");
				StringBuilder sb = new StringBuilder(suboutput);
				int i = suboutput.lastIndexOf("]");
				sb.deleteCharAt(i);
				suboutput = sb.toString() + ",";
				outputJsonFile = outputJsonFile + suboutput;

				total = total - setedLimit;

				if (total > 0) {
					if (total >= max) {
						offset += max;
						setedLimit = max;
					} else {
						offset += max;
						setedLimit = total;
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder(outputJsonFile);
		int i = outputJsonFile.lastIndexOf(",");
		sb.deleteCharAt(i);
		outputJsonFile = "[" + sb.toString() + "]";

		final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
		FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + ontologyName + ".json");

		outputfile.write(outputJsonFile);
		outputfile.close();

		File finalFile = new File(file.getAbsolutePath() + File.separator + ontologyName + ".json");

		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
		respHeaders.setContentLength(finalFile.length());
		final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
		deleteDirectory(finalFile);
		return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);

	}

	private String deleteIdAndContext(JSONArray jsonArray) {

		for (Object o : jsonArray) {
			if (o instanceof JSONObject) {
				JSONObject obj = (JSONObject) o;
				obj.remove("_id");
				obj.remove("contextData");
			}
		}
		return jsonArray.toString();
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private File createFile(String path) {
		final File file = new File(path);
		if (!file.exists()) {
			final Boolean success = file.mkdirs();
			if (!success) {
				log.error("Creating values file for deploy OP falied.");
				return null;
			}
		} else {
			log.error("Creating values file for deploy OP falied, the temporary directory don't exist: "
					+ file.getAbsolutePath());
			return null;
		}
		return file;
	}

	private String getJsonFromFile(MultipartFile file) throws Exception {
		String jsonData = null;
		try {
			String contentType = file.getContentType();
			String fileName = file.getOriginalFilename();
			InputStreamReader inputStream = new InputStreamReader(file.getInputStream());

			if (contentType.equals("text/csv") || fileName.contains(".csv")) {
				jsonData = getJsonFromCSVEngine(inputStream);
			} else if (contentType.equals("text/xml") || contentType.equals("application/xml")
					|| fileName.contains(".xml")) {
				jsonData = resourceService.getJsonFromXML(inputStream);
			} else if (contentType.equals("application/json") || fileName.contains(".json")) {
				BufferedReader reader = new BufferedReader(inputStream);
				StringBuilder responseStrBuilder = new StringBuilder();
				String str;
				while ((str = reader.readLine()) != null) {
					responseStrBuilder.append(str);
				}
				jsonData = responseStrBuilder.toString();
			}

			if (jsonData != null && !jsonData.equals("") && jsonData.substring(0, 1).equals("{")) {
				Map<String, Object> obj = mapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
				});
				List<Map<String, Object>> result = resourceService.processMap(obj);
				jsonData = mapper.writeValueAsString(result);
			}
			return jsonData;
		} catch (IOException e1) {
			e1.printStackTrace();
			return jsonData;
		}
	}

	private String getJsonFromCSVEngine(InputStreamReader input) throws Exception {
		final CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).setColumnSeparator(';').build();
		final CsvMapper csvMapper = new CsvMapper();
		csvMapper.enable(CsvGenerator.Feature.STRICT_CHECK_FOR_QUOTING);
		List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();

		final JSONArray jsonArray = new JSONArray();
		for (Object data : readAll) {
			String jsondatastr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
			JSONObject jsondata = new JSONObject(jsondatastr);
			List<String> headers = new ArrayList<String>();
			jsondata.keys().forEachRemaining(headers::add);

			JSONObject fatherJson = new JSONObject();
			Iterator<String> iterator = jsondata.keys();
			if (iterator != null) {
				List<String> calculated = new ArrayList<>();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = jsondata.get(key).toString();
					if (key.contains(".")) {
						String[] split = key.split("\\.");
						String father = split[0];
						List<String> allStartsWith = headers.stream().filter(field -> field.startsWith(father))
								.collect(Collectors.toList());
						allStartsWith.replaceAll(x -> x.replaceAll(father + ".", ""));

						JSONObject sonJson = new JSONObject();
						if (!calculated.contains(father)) {
							try {
								searchSons(allStartsWith, sonJson, father, "", jsondata);
								fatherJson.put(father, sonJson);
							} catch (Exception e) {
								if (e.getMessage().equals("isArray")) {
									ArrayList<Object> array = new ArrayList<>();
									for (String a : allStartsWith) {
										String avalue = jsondata.getString(father + "." + a);
										array.add(getValueWithFormat(avalue));
									}
									fatherJson.put(father, array);

								} else if (e.getMessage().equals("isDoubleArray")) {
									ArrayList<ArrayList<Object>> array = new ArrayList<>();
									String prev = null;
									for (String a : allStartsWith) {
										if (prev == null || !a.startsWith(prev)) {
											String[] arraySplit = a.split("\\.");
											List<String> listArray = allStartsWith.stream()
													.filter(field -> field.startsWith(arraySplit[0]))
													.collect(Collectors.toList());
											prev = arraySplit[0];
											ArrayList<Object> array2 = new ArrayList<>();
											for (String a2 : listArray) {
												String avalue = jsondata.getString(father + "." + a2);
												array2.add(getValueWithFormat(avalue));
											}
											array.add(array2);
										}
									}
									fatherJson.put(father, array);
								}
							}
							calculated.add(father);
						}
					} else {
						fatherJson.put(key, getValueWithFormat(value));
					}
				}
			}
			jsonArray.put(fatherJson);
		}
		return jsonArray.toString();
	}

	private void searchSons(List<String> allStartsWith, JSONObject sonJson, String father, String prevfather,
			JSONObject jsondata) throws Exception {
		Collections.sort(allStartsWith);
		List<String> startsWithfson = new ArrayList<>();
		for (String columnSon : allStartsWith) {
			if (columnSon.contains(".")) {
				String[] sonSplit = columnSon.split("\\.");
				String fatherson = sonSplit[0];
				try {
					if (isNumeric(fatherson)) {
						throw new Exception("isDoubleArray");
					} else {
						startsWithfson = allStartsWith.stream().filter(field -> field.startsWith(fatherson))
								.collect(Collectors.toList());
						startsWithfson.replaceAll(x -> x.replaceAll(fatherson + ".", ""));
						JSONObject son2Json = new JSONObject();
						String testArray = startsWithfson.get(0);

						if (isNumeric(testArray) && !testArray.contains(".")) {
							ArrayList<Object> array = new ArrayList<>();
							for (String a : startsWithfson) {
								String value;
								if (prevfather == "")
									value = jsondata.getString(father + "." + fatherson + "." + a);
								else
									value = jsondata.getString(prevfather + "." + father + "." + fatherson + "." + a);
								array.add(getValueWithFormat(value));
							}
							sonJson.put(fatherson, array);

						} else if (isNumeric(testArray) && testArray.contains(".")) {
							ArrayList<ArrayList<Object>> array = new ArrayList<>();
							String prev = null;
							for (String a : startsWithfson) {
								if (prev == null || !a.startsWith(prev)) {
									String[] arraySplit = a.split("\\.");
									List<String> listArray = startsWithfson.stream()
											.filter(field -> field.startsWith(arraySplit[0]))
											.collect(Collectors.toList());
									prev = arraySplit[0];
									ArrayList<Object> array2 = new ArrayList<>();
									for (String a2 : listArray) {
										String value;
										if (prevfather == "")
											value = jsondata.getString(father + "." + fatherson + "." + a2);
										else
											value = jsondata
													.getString(prevfather + "." + father + "." + fatherson + "." + a2);
										array2.add(getValueWithFormat(value));
									}
									array.add(array2);
								}
							}
							sonJson.put(fatherson, array);
						} else {
							if (prevfather == "")
								searchSons(startsWithfson, son2Json, fatherson, father, jsondata);
							else
								searchSons(startsWithfson, son2Json, fatherson, prevfather + "." + father, jsondata);

							sonJson.put(fatherson, son2Json);
						}
					}
				} catch (Exception e) {
					if (e.getMessage().equals("isDoubleArray")) {
						throw new Exception("isDoubleArray");
					}
				}
			} else {
				if (!isNumeric(columnSon)) {
					if (prevfather == "") {
						String value = jsondata.getString(father + "." + columnSon);
						sonJson.put(columnSon, getValueWithFormat(value));
					} else {
						String value = jsondata.getString(prevfather + "." + father + "." + columnSon);
						sonJson.put(columnSon, getValueWithFormat(value));
					}
				} else {
					throw new Exception("isArray");
				}
			}
		}
	}

	private Object getValueWithFormat(String value) {
		if (isNumeric(value)) {
			return Double.parseDouble(value);
		} else if (isBoolean(value)) {
			return Boolean.parseBoolean(value);
		} else {
			return value;
		}
	}

	private static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private static boolean isBoolean(String str) {
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			return true;
		}
		return false;
	}

	private int getMaxRegisters() {
		try {
			return ((Integer) integrationResourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("queries-limit")).intValue();
		} catch (final Exception e) {
			return queryDefaultLimit;
		}
	}
}
