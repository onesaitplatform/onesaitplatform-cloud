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
package com.minsait.onesait.platform.controlpanel.rest.management.virtual.datasources;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.business.services.virtual.datasources.VirtualDatasourceService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.rest.management.virtual.datasources.model.VirtualDatasorceDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;


@RestController
@Tag(name = "External Database Connections")
@RequestMapping("api/externaldatabaseconnections")
@EnableAutoConfiguration
@Slf4j
public class VirtualDatasourcesRestControllerImpl implements VirtualDatasourcesRestController {

	@Autowired
	private VirtualDatasourceService virtualDatasourcesService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	AppWebUtils utils;

	private static final String EMPTY_RESPONSE_VIRTUALCONNECTIONS = "[]";
	private static final String EMPTY_RESPONSE_CONNECTION_ERROR = "Connection Error";
	private static final String EMPTY_RESPONSE_CONNECTION_OK = "Connection Successful";
	private static final String ERROR_USER_NOT_ALLOWED = "User is not authorized";
	private static final String ERROR_DOMAIN_EXISTS = "Datasource Domain already exists";
	
	
	@Operation(summary="Get all External Database Connections")
	@GetMapping
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK"))
	public ResponseEntity<?> getAll() {
		if (utils.isAdministrator() || utils.isDeveloper()) {
			final List<OntologyVirtualDatasource> externalDatabaseConnections = virtualDatasourcesService.getAllByDatasourceNameAndUser("", utils.getUserId() );			
			return ResponseEntity.ok().body(externalDatabaseConnections.stream().map(VirtualDatasorceDTO::convertNoCredentials).collect(Collectors.toList()));
		} else {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Operation(summary= "Get External Database Connection by identification or Id")
	@GetMapping(value = "/{id}")
	public ResponseEntity<?> getVirtualDatasourceById(
			@Parameter(description= "External Database Connection id or identification", required = true) @PathVariable("id") String virtualDatasourceId) {
		if (utils.isAdministrator() || utils.isDeveloper()) {
			OntologyVirtualDatasource externalDatabaseConnection = virtualDatasourcesService.getDatasourceByIdAndUserId(virtualDatasourceId, utils.getUserId());
			if (externalDatabaseConnection == null) {
				List<OntologyVirtualDatasource> externalDatabaseConnectionList = virtualDatasourcesService.getAllByDatasourceNameAndUser(virtualDatasourceId, utils.getUserId());
				if (externalDatabaseConnectionList.size()>0) {
					return ResponseEntity.ok().body(VirtualDatasorceDTO.convert(externalDatabaseConnectionList.get(0)));
				}
			} else {
				return ResponseEntity.ok().body(VirtualDatasorceDTO.convert(externalDatabaseConnection));
			}
			return new ResponseEntity<>(EMPTY_RESPONSE_VIRTUALCONNECTIONS, HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Operation(summary= "Create new External Database Connection")
	@PostMapping
	public ResponseEntity<?> createVirtualDatasource(
			@Parameter(description= "Externa Database Connection Body", required = true) @Valid @RequestBody VirtualDatasorceDTO virtualDatasourceBody) {
		if (utils.isAdministrator() || utils.isDeveloper()) {
			final User user = userService.getUser(utils.getUserId());
			OntologyVirtualDatasource ontologyVirtualDatasource = VirtualDatasorceDTO.convertFromDTO(virtualDatasourceBody);
			ontologyVirtualDatasource.setUser(user);
			try {
				virtualDatasourcesService.createDatasource(ontologyVirtualDatasource);
				log.info("External Database Connection succesfully created");
			} catch (Exception e) {
				log.error("External Database Connection create ERROR", e);
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
			return ResponseEntity.ok().body(VirtualDatasorceDTO.convert(ontologyVirtualDatasource));
		} else {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Operation(summary= "Update External Database Connection")
	@PutMapping
	public ResponseEntity<?> updateVirtualDatasource(
				@Parameter(description= "Externa Database Connection Body", required = true) @Valid @RequestBody VirtualDatasorceDTO virtualDatasourceBody,
				@Parameter(description = "Mantain Credentials", name = "MantainCredentials") @RequestParam(value = "mantainCredentials", required = false, defaultValue = "false") boolean mantainCredentials) {
		if (utils.isAdministrator() || utils.isDeveloper()) {			
			final User user = userService.getUser(utils.getUserId());
	
			OntologyVirtualDatasource ontologyVirtualDatasourceUpdate = VirtualDatasorceDTO.convertFromDTO(virtualDatasourceBody);
			
			OntologyVirtualDatasource externalDatabaseConnectionBDC = virtualDatasourcesService.getDatasourceByIdAndUserId(ontologyVirtualDatasourceUpdate.getId(), user.getUserId());
	
			if (externalDatabaseConnectionBDC == null) {
				List<OntologyVirtualDatasource> externalDatabaseConnectionBDCList = virtualDatasourcesService.getAllByDatasourceNameAndUser(ontologyVirtualDatasourceUpdate.getIdentification(), user.getUserId());
				if (externalDatabaseConnectionBDCList.size()>0) {
					externalDatabaseConnectionBDC = externalDatabaseConnectionBDCList.get(0);
				} else {
					return new ResponseEntity<>(EMPTY_RESPONSE_VIRTUALCONNECTIONS, HttpStatus.NOT_FOUND);
				}
			}
	
			if (utils.isAdministrator() || externalDatabaseConnectionBDC.getUser().equals(user)) {
				try {
					
					List<OntologyVirtualDatasource> domainVirtualDatasets = virtualDatasourcesService.getDatasourceByDomain(ontologyVirtualDatasourceUpdate.getDatasourceDomain());
					
				    if (domainVirtualDatasets!= null && domainVirtualDatasets.size()>0 && !domainVirtualDatasets.get(0).getId().equals(ontologyVirtualDatasourceUpdate.getId())) {
				    	return new ResponseEntity<>(ERROR_DOMAIN_EXISTS, HttpStatus.BAD_REQUEST);
				    }

				    ontologyVirtualDatasourceUpdate.setUser(user);
					ontologyVirtualDatasourceUpdate.setTestOnBorrow(externalDatabaseConnectionBDC.getTestOnBorrow());
					ontologyVirtualDatasourceUpdate.setTestWhileIdle(externalDatabaseConnectionBDC.getTestWhileIdle());
					ontologyVirtualDatasourceUpdate.setValidationQueryTimeout(externalDatabaseConnectionBDC.getValidationQueryTimeout());
					
					virtualDatasourcesService.updateOntology(ontologyVirtualDatasourceUpdate, mantainCredentials, externalDatabaseConnectionBDC.getCredentials());
					log.info("External Database Connection succesfully updated");
					
					return ResponseEntity.ok().body(VirtualDatasorceDTO.convert(ontologyVirtualDatasourceUpdate));
				} catch (Exception e) {
					log.error("External Database Connection Update ERROR", e);
					return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
				}	
			} else {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Operation(summary= "Delete External Database Connection by identification or Id")
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<?> deleteVirtualDatasourceById(
			@Parameter(description= "External Database Connection id or identification", required = true) @PathVariable("id") String virtualDatasourceId) {
		if (utils.isAdministrator() || utils.isDeveloper()) {		
			final User user = userService.getUser(utils.getUserId());
			
			OntologyVirtualDatasource externalDatabaseConnection = virtualDatasourcesService.getDatasourceByIdAndUserId(virtualDatasourceId, user.getUserId());
			
			if (externalDatabaseConnection == null) {
				List<OntologyVirtualDatasource> externalDatabaseConnectionList = virtualDatasourcesService.getAllByDatasourceNameAndUser(virtualDatasourceId, user.getUserId());
				if (externalDatabaseConnectionList.size()==0) {
					return new ResponseEntity<>(EMPTY_RESPONSE_VIRTUALCONNECTIONS, HttpStatus.NOT_FOUND);
				} else {
					externalDatabaseConnection = externalDatabaseConnectionList.get(0);
				}
			}
			if (utils.isAdministrator() || externalDatabaseConnection.getUser().equals(user)) {
				try {
					virtualDatasourcesService.deleteDatasource(externalDatabaseConnection);
					log.info("External Database Connection succesfully deleted");
					return new ResponseEntity<>(HttpStatus.OK);
				} catch (Exception e) {
					log.error("External Database Connection delete ERROR", e);
					return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
				}
			} else {
				return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Operation(summary= "Check External Database Connection by identification or Id")
	@GetMapping(value = "/checkconnection/{id}")
	public ResponseEntity<?> checkVirtualDatasourceById(
			@Parameter(description= "External Database Connection id or identification", required = true) @PathVariable("id") String virtualDatasourceId) {
		if (utils.isAdministrator() || utils.isDeveloper()) {		
		
			OntologyVirtualDatasource externalDatabaseConnection = virtualDatasourcesService.getDatasourceByIdAndUserId(virtualDatasourceId, utils.getUserId());
			
			if (externalDatabaseConnection == null) {
				List<OntologyVirtualDatasource> externalDatabaseConnectionList = virtualDatasourcesService.getAllByDatasourceNameAndUser(virtualDatasourceId, utils.getUserId());
				if (externalDatabaseConnectionList.size()==0) {
					return new ResponseEntity<>(EMPTY_RESPONSE_VIRTUALCONNECTIONS, HttpStatus.NOT_FOUND);
				} else {
					externalDatabaseConnection = externalDatabaseConnectionList.get(0);
				}
			}
			
			try {
				Boolean success = virtualDatasourcesService.checkConnectionExtern(externalDatabaseConnection.getIdentification());
				
				if (success) {
					return new ResponseEntity<>(EMPTY_RESPONSE_CONNECTION_OK, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(EMPTY_RESPONSE_CONNECTION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} catch (GenericOPException e) {
				log.error("External Database Connection Check Connection ERROR", e);
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(ERROR_USER_NOT_ALLOWED, HttpStatus.UNAUTHORIZED);
		}
	}
	
}
