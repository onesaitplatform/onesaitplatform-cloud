package com.minsait.onesait.microservice.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.minsait.onesait.microservice.model.${WRAPPER_CLASS};
import com.minsait.onesait.microservice.repository.${ONTOLOGY_CAP}Repository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${ONTOLOGY}")
@Api(value = "Restaurants REST service", tags = { "${ONTOLOGY}" })
@ApiResponses({ @ApiResponse(code = 429, message = "Too Many Requests"),
		@ApiResponse(code = 500, message = "Error processing request"),
		@ApiResponse(code = 403, message = "Forbidden") })
@Slf4j
public class ${ONTOLOGY_CAP}Service {

	@Autowired
	private ${ONTOLOGY_CAP}Repository ontologyRepository;

	@GetMapping
	@ApiOperation(response = ${WRAPPER_CLASS}[].class, httpMethod = "GET", value = "Return all ${ONTOLOGY}")
	@ApiResponse(code = 429, message = "Too Many Requests")
	public ResponseEntity<List<${WRAPPER_CLASS}>> getAll${ONTOLOGY}s() {
		log.info("Getting all registered ${ONTOLOGY}s");
		final List<${WRAPPER_CLASS}> ontologies = ontologyRepository.findAll();

		return new ResponseEntity<>(ontologies, HttpStatus.OK);
	}
}
