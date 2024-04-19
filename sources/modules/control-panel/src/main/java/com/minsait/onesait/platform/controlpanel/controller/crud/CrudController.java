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
package com.minsait.onesait.platform.controlpanel.controller.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.business.services.virtual.datasources.VirtualDatasourceService;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.crud.dto.OntologyDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.external.generator.SQLGenerator;
import com.minsait.onesait.platform.persistence.external.generator.model.common.WhereStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

@Controller
@RequestMapping("/crud")
public class CrudController {

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private AppWebUtils utils;

	@Autowired
	private UserService userService;

	@Autowired
	private RouterService routerService;

	@Autowired
	private VirtualDatasourceService virtualDatasourceService;

	@Autowired
	private SQLGenerator sqlGenerator;
	
	@Autowired
	private IntegrationResourcesService resourcesService;

	private static final String ERROR_TRUE = "{\"error\":\"true\"}";

	@GetMapping(value = "/admin/{id}", produces = "text/html")
	public String edit(Model model, @PathVariable("id") String id) {
		final Ontology ontology = ontologyService.getOntologyById(id, utils.getUserId());
		final OntologyDTO ontologyDTO = new OntologyDTO();
		ontologyDTO.setIdentification(ontology.getIdentification());
		ontologyDTO.setJsonSchema(ontology.getJsonSchema());
		ontologyDTO.setDatasource(ontology.getRtdbDatasource().name());
		model.addAttribute("ontology", ontologyDTO);
		model.addAttribute("uniqueId", getUniqueColumn(ontology.getIdentification(), false));
		return "crud/admin";
	}

	public String processQuery(final String query, final String ontologyID, final ApiOperation.Type method,
			final String body, final String objectId) {
		final User user = userService.getUser(utils.getUserId());
		final OperationType operationType;
		final String payload;
		switch (method) {
		case POST:
			operationType = OperationType.INSERT;
			payload = body;
			break;
		case PUT:
			operationType = OperationType.UPDATE;
			payload = body;
			break;
		case DELETE:
			operationType = OperationType.DELETE;
			payload = body;
			break;
		case GET:
			payload = query;
			operationType = OperationType.QUERY;
			break;
		default:
			payload = body;
			operationType = OperationType.QUERY;
			break;
		}

		final OperationModel model = OperationModel
				.builder(ontologyID, operationType, user.getUserId(), OperationModel.Source.INTERNAL_ROUTER)
				.body(payload).queryType(QueryType.SQL).objectId(objectId).deviceTemplate("").build();
		final NotificationModel modelNotification = new NotificationModel();

		modelNotification.setOperationModel(model);

		final OperationResultModel result = routerService.query(modelNotification);

		if (!StringUtils.isEmpty(result)) {
			if (!result.isStatus()) {
				final JSONObject jsonObject = new JSONObject();
				jsonObject.put("error", result.getMessage());
				return jsonObject.toString();
			}

			String output = result.getResult();

			if (operationType == OperationType.INSERT) {
				try {
					final JSONObject obj = new JSONObject(output);
					if (obj.has(InsertResult.DATA_PROPERTY)) {
						output = obj.get(InsertResult.DATA_PROPERTY).toString();
					}
				} catch (final Exception e) {
					output = "{}";
				}
			}
			return output;
		} else {
			return null;
		}

	}

	@PostMapping(value = { "/query" }, produces = "application/json")
	public @ResponseBody String query(String ontologyID, String query) {
		try {
			return processQuery(query, ontologyID, ApiOperation.Type.GET, "", "");
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@RequestMapping(path = "/queryParams", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String queryParams(@Valid @RequestBody final SelectStatement selectStatement,
			final BindingResult result) {
		try {
			if (!result.hasErrors()) {
				final Ontology.RtdbDatasource datasource = getDataSourceForOntology(selectStatement.getOntology());
				if (datasource.equals(Ontology.RtdbDatasource.MONGO)) {
					if (selectStatement.getColumns() != null && !selectStatement.getColumns().isEmpty()) {
						selectStatement.setColumns(selectStatement.getColumns().stream()
								.map(column -> selectStatement.getOntology() + "." + column)
								.collect(Collectors.toList()));
						selectStatement.getColumns().add("_id");
					} else {
						if (useQuasar()) {
							selectStatement.setColumns(Arrays.asList("_id", selectStatement.getOntology() + ".*"));
							selectStatement.setLimit(selectStatement.getLimit() + selectStatement.getOffset() );
						} else {
							selectStatement.setColumns(Arrays.asList("*"));
							selectStatement.setAlias("c");
						}
					}
					if (selectStatement.getWhere() != null && !selectStatement.getWhere().isEmpty()) {
						if (useQuasar()) {
							selectStatement.getWhere().stream().forEach(where -> {
								where.setColumn(selectStatement.getOntology() + "." + where.getColumn());
							});
						}
						else{
							selectStatement.getWhere().stream().forEach(where -> {
								where.setColumn("c" + "." + where.getColumn());
							});
						}
					}
					if (selectStatement.getOrderBy() != null && !selectStatement.getOrderBy().isEmpty()) {
						if (useQuasar()) {
							selectStatement.getOrderBy().stream().forEach(order -> {
								order.setColumn(selectStatement.getOntology() + "." + order.getColumn());
							});
						}
						else {
							selectStatement.getOrderBy().stream().forEach(order -> {
								order.setColumn("c" + "." + order.getColumn());
							});
						}
					}
				}

				if (datasource.equals(Ontology.RtdbDatasource.ELASTIC_SEARCH)) {
					if (selectStatement.getColumns() == null) {
						selectStatement.setColumns(new ArrayList<>());
						selectStatement.getColumns().add("o.*");
					}
					selectStatement.getColumns().add("o._id");
					selectStatement.setAlias("o");
				}

				final String query = sqlGenerator.generate(selectStatement, false).getStatement();
				return processQuery(query, selectStatement.getOntology(), ApiOperation.Type.GET, "", "");
			} else {
				throw new IllegalArgumentException("Parameters could not be mapped to a select statement");
			}
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/findById" }, produces = "application/json")
	public @ResponseBody String findById(final String ontologyID, final String oid) {
		try {
			final Ontology.RtdbDatasource datasource = getDataSourceForOntology(ontologyID);
			final SelectStatement selectStatement = sqlGenerator.buildSelect().setColumns(getColumnsQuery(ontologyID))
					.setOntology(ontologyID).setLimit(1).setOffset(0);

			if (datasource.equals(Ontology.RtdbDatasource.MONGO)) {
				selectStatement.setWhere(
						Arrays.asList(new WhereStatement(getUniqueColumn(ontologyID, true), "=", oid, "", "OID")));
			} else {
				selectStatement
						.setWhere(Arrays.asList(new WhereStatement(getUniqueColumn(ontologyID, true), "=", oid)));
			}

			final String query = selectStatement.generate(false).getStatement();
			final String result = processQuery(query, ontologyID, ApiOperation.Type.GET, "", oid);

			if (datasource.equals(Ontology.RtdbDatasource.MONGO)) {
				final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyID, utils.getUserId());
				if (useQuasar()) {
					return findDatesAndReplace(result, ontology.getJsonSchema());
				}
				else{
					return result;
				}
			} else {
				return result;
			}
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	private List<String> getColumnsQuery(final String ontology) {
		final Ontology.RtdbDatasource datasource = ontologyRepository.findByIdentification(ontology)
				.getRtdbDatasource();

		if (useQuasar()) {
			if (datasource.equals(Ontology.RtdbDatasource.MONGO)) {
				return Arrays.asList(ontology);
			} else {
				return new ArrayList<>();
			}
		} else {
			final ArrayList list = new ArrayList<>();
			list.add("*");
			return list;
		}

	}

	private String getUniqueColumn(final String ontology, boolean findById) {
		final Ontology.RtdbDatasource datasource = getDataSourceForOntology(ontology);

		if (datasource.equals(Ontology.RtdbDatasource.VIRTUAL)) {
			return virtualDatasourceService.getUniqueColumn(ontology);
		} else if (datasource.equals(Ontology.RtdbDatasource.ELASTIC_SEARCH)) {
			return "_id";
		} else if (datasource.equals(Ontology.RtdbDatasource.COSMOS_DB)) {
			return "id";
		} else {
			if (useQuasar() || useLegacySQL() || findById) {
				return "_id";
			} else {
				return "_id.$oid";
			}
		}
	}

	private Ontology.RtdbDatasource getDataSourceForOntology(final String ontology) {
		return ontologyRepository.findByIdentification(ontology).getRtdbDatasource();
	}

	@PostMapping(value = { "/deleteById" }, produces = "application/json")
	public @ResponseBody String deleteById(String ontologyID, String oid) {
		try {
			return processQuery("", ontologyID, ApiOperation.Type.DELETE, "", oid);
		} catch (final Exception e) {
			return ERROR_TRUE;
		}
	}

	@PostMapping(value = { "/insert" }, produces = "application/json")
	public @ResponseBody String insert(String ontologyID, String body) {
		try {
			return processQuery("", ontologyID, ApiOperation.Type.POST, body, "");
		} catch (final Exception e) {
			return "{\"exception\":\"true\"}";
		}
	}

	@PostMapping(value = { "/update" }, produces = "application/json")
	public @ResponseBody String update(String ontologyID, String body, String oid) {
		try {
			return processQuery("", ontologyID, ApiOperation.Type.PUT, body, oid);
		} catch (final Exception e) {
			return "{\"exception\":\"true\"}";
		}
	}

	private String findDatesAndReplace(String text, String ontology) {
		// find $date on schema
		final String pat = "\\x24date";
		final StringBuffer stringBuffer = new StringBuffer();
		final Pattern pattern = Pattern.compile(pat);
		final Matcher matcher = pattern.matcher(ontology);
		if (matcher.find()) {
			// if $date then find date and replace
			final String patDate = "(\"\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d+\\p{Punct}*\\d*Z\")";
			final Pattern patternDate = Pattern.compile(patDate);
			final Matcher matcherDate = patternDate.matcher(text);
			while (matcherDate.find()) {
				matcherDate.appendReplacement(stringBuffer, "{\"\\$date\":" + matcherDate.group(1) + "}");
			}
			matcherDate.appendTail(stringBuffer);
			return stringBuffer.toString();
		}
		return text;
	}
	
	private boolean useQuasar() {
		try {
			return ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-use-quasar")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private boolean useLegacySQL() {
		try {
			return ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-use-legacysql")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}
	}

}
