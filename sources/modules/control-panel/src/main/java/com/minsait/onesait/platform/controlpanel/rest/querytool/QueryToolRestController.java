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
package com.minsait.onesait.platform.controlpanel.rest.querytool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.exceptions.QueryNativeFormatException;
import com.minsait.onesait.platform.persistence.factory.ManageDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "QUERYTOOL Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400",description = "Bad request"),
	@ApiResponse(responseCode = "500",description = "Internal server error"), @ApiResponse(responseCode = "403",description = "Forbidden") })
@RequestMapping("api/querytool")
@Slf4j
public class QueryToolRestController {

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private QueryToolService queryToolService;
	@Autowired
	private ManageDBRepositoryFactory manageFactory;
	@Autowired
	private AppWebUtils utils;

	public static final String QUERY_SQL = "SQL";
	public static final String QUERY_NATIVE = "NATIVE";
	private static final String CONTEXT_USER = "$context.userId";
	private static final String RUNQUERYERROR = "Error in runQuery";

	@Operation(summary = "returns the data resulting from executing the query. The query field and the ontology field are mandatory, the offset field of not entering will be initialized to 0)")
	@GetMapping
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=String.class)), responseCode = "200",description = "OK"))
	public ResponseEntity<?> query(
			@Parameter(description= "Query Allowed values:(Select * from ontology)", required = false) @RequestParam(value = "query", required = true, defaultValue = "") String query,
			@Parameter(description= "Allowed values: (ontology identification)", required = false) @RequestParam(value = "ontology", required = true, defaultValue = "") String ontology,
			@Parameter(description= "Allowed values: (SQL,NATIVE)", required = false) @RequestParam(value = "querytype", required = true, defaultValue = "") String querytype,
			@Parameter(description= "Allowed numeric values. Ignored if empty", required = false) @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {

		if (query.trim().length() == 0) {
			log.error("query not can be empty");
			return new ResponseEntity<>("query not can be empty", HttpStatus.BAD_REQUEST);
		}
		if (querytype.trim().length() == 0) {
			log.error("querytype not can be empty");
			return new ResponseEntity<>("querytype not can be empty, Please select queryType Native or SQL",
					HttpStatus.BAD_REQUEST);
		}
		if (ontology.trim().length() == 0) {
			log.error("ontology not can be empty");
			return new ResponseEntity<>("ontology not can be empty", HttpStatus.BAD_REQUEST);
		}
		return getQuery(query, querytype, ontology, offset);
	}

	private ResponseEntity<?> getQuery(String query, String queryType, String ontologyIdentification, int offset) {

		String queryResult = null;

		final Ontology ontology = ontologyService.getOntologyByIdentification(ontologyIdentification,
				utils.getUserId());

		try {
			if (ontologyService.hasUserPermissionForQuery(utils.getUserId(), ontologyIdentification)) {
				final ManageDBRepository manageDB = manageFactory.getInstance(ontologyIdentification);
				if (!ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.API_REST)
						&& manageDB.getListOfTables4Ontology(ontologyIdentification).isEmpty()
						) {
					manageDB.createTable4Ontology(ontologyIdentification, "{}", null);
				}
				query = query.replace(CONTEXT_USER, utils.getUserId());
				if (queryType.toUpperCase().equals(QUERY_SQL)
						&& !ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.querySQLAsJson(utils.getUserId(), ontologyIdentification, query,	offset);

					return new ResponseEntity<>(queryResult, HttpStatus.OK);

				} else if (queryType.toUpperCase().equals(QUERY_NATIVE)
						|| ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
					queryResult = queryToolService.queryNativeAsJson(utils.getUserId(), ontologyIdentification, query);
					return new ResponseEntity<>(queryResult, HttpStatus.OK);

				} else {
					return new ResponseEntity<>("querytype not can be empty, Please select queryType Native or SQL",
							HttpStatus.BAD_REQUEST);
				}
			} else {
				return new ResponseEntity<>("You don't have permissions for this ontology", HttpStatus.BAD_REQUEST);
			}

		} catch (final QueryNativeFormatException e) {
			log.error(RUNQUERYERROR, e);
			return new ResponseEntity<>("Malformed Query.", HttpStatus.BAD_REQUEST);
		} catch (final DBPersistenceException e) {
			log.error(RUNQUERYERROR, e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (final Exception e) {
			log.error(RUNQUERYERROR, e);
			return new ResponseEntity<>(utils.getMessage("querytool.query.native.error", e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}

	}

}
