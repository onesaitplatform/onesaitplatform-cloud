/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.cache;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.cache.CacheBusinessService;
import com.minsait.onesait.platform.business.services.cache.CacheBusinessServiceException;
import com.minsait.onesait.platform.config.model.Cache;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


import lombok.extern.slf4j.Slf4j;

@Tag(name = "Cache Management")
@RestController
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
@RequestMapping("api/caches")
@Slf4j
public class CacheRestController {

    @Autowired
    private CacheBusinessService cacheBS;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AppWebUtils utils; 
    
    private ObjectMapper om = new ObjectMapper();
    
    private static final String STATUS_OK = "{\"status\": \"ok\"}";

    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Create a new cache structure")
    @PostMapping("/{identification}/")
    public ResponseEntity<String> create(
            @Parameter(description= "Identification for the cache structure", required = true) 
            @PathVariable("identification") 
            String identification,
            @RequestBody(required=true) CacheDTO cacheDTO) throws JsonProcessingException {
    	if (log.isDebugEnabled()) {
    		 log.debug("Recieved request to create a new cached map {}", identification);
    	}
        
        if (!identification.matches(AppWebUtils.IDENTIFICATION_PATERN)) {
            return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'", HttpStatus.BAD_REQUEST);
        }
        
        User user = userService.getUserByIdentification(utils.getUserId());
        
        Cache cache = new Cache();
        
        //asign the correct user
        cache.setUser(user);
        cache.setIdentification(identification);
        
        cache.setType(cacheDTO.getType());
        cache.setEvictionPolicy(cacheDTO.getEvictionPolicy());
        cache.setMaxSizePolicy(cacheDTO.getMaxSizePolicy());
        cache.setSize(cacheDTO.getSize());        
        
        try {            
            Cache cacheMapInfo = cacheBS.<String,String>createCache(cache);
            return ResponseEntity.ok().body(om.writeValueAsString(cacheMapInfo));
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }      
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Delete a map")
    @DeleteMapping("/maps/{identification}/")
    public ResponseEntity<String> deleteMap(
            @Parameter(description= "Identification for the map", required = true) 
            @PathVariable("identification") 
            String identification) {
    	if (log.isDebugEnabled()) {
    		log.debug("Recieved request to delete a cached map {}", identification);
    	}
        
        User user = userService.getUserByIdentification(utils.getUserId());
        
        try {
            cacheBS.deleteMap(identification, user);
            return ResponseEntity.ok().body(STATUS_OK);
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Put value of a type in map")
    @PostMapping("/maps/{identification}/put/{key}/")
    public ResponseEntity<String> putIntoMap(
            @Parameter(description= "Identification of the map where put data", required = true) 
            @PathVariable("identification") 
            String identification,
            @Parameter(description= "Key to store the data", required = true) 
            @PathVariable("key") 
            String key,
            @RequestBody(required=true) String value) {
    	if (log.isDebugEnabled()) {
    		log.debug("Recieved request to put data into cached map {} with key {} and value {}", identification, key , value);
    	}
        
        User user = userService.getUser(utils.getUserId());
        
        try {
            cacheBS.putIntoMap(identification, key, value, user);
            return ResponseEntity.ok().body(STATUS_OK);
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Put values of a type in map")
    @PostMapping("/maps/{identification}/putMany/")
    public ResponseEntity<String> putManyIntoMap(
            @Parameter(description= "Identification of the map where put data", required = true) 
            @PathVariable("identification") 
            String identification,
            @RequestBody(required=true) Map<String, String> values) throws IOException {
    	if (log.isDebugEnabled()) {
    		log.debug("Recieved request to put several data into cached map {}", identification);
    	}
        
        User user = userService.getUser(utils.getUserId());
        
        try {
            cacheBS.putAllIntoMap(identification, values, user);
            return ResponseEntity.ok().body(STATUS_OK);
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Get one value from a map")
    @GetMapping("/maps/{identification}/get/{key}/")
    public ResponseEntity<String> getFromMap(
            @Parameter(description= "Identification of the map to get data", required = true) 
            @PathVariable("identification") 
            String identification,
            @Parameter(description= "Key to search the data", required = true) 
            @PathVariable("key") 
            String key){
    	if (log.isDebugEnabled()) {
    		log.debug("Recieved request to get data from cached map {} with key {}", identification, key);
    	}
        
        User user = userService.getUser(utils.getUserId());
        
        try {
            String value = cacheBS.getFromMap(identification, user, key);
            return ResponseEntity.ok().body(value);
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Get all values from a map")
    @GetMapping("/maps/{identification}/getAll/")
    public ResponseEntity<Map<String, String>> getAllFromMap(
            @Parameter(description= "Identification of the map to get data", required = true) 
            @PathVariable("identification") 
            String identification){
    	if (log.isDebugEnabled()) {
    		 log.debug("Recieved request to get all data from cached map {}", identification);
    	}
        
        User user = userService.getUser(utils.getUserId());
        
        try {
            Map<String, String> values = cacheBS.getAllFromMap(identification, user);            
            return ResponseEntity.ok().body(values);
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @Operation(summary = "Get all values from a map")
    @PostMapping("/maps/{identification}/getMany/")
    public ResponseEntity<Map<String, String>> getManyFromMap(
            @Parameter(description= "Identification of the map to get data", required = true) 
            @PathVariable("identification") 
            String identification,
            @RequestBody(required=true) Set<String> keys){
    	if (log.isDebugEnabled()) {
    		log.debug("Recieved request to get several data from cached map {}", identification);
    	}
       
        User user = userService.getUser(utils.getUserId());
        
        try {
            Map<String, String> values = cacheBS.getManyFromMap(identification, user, keys);
            return ResponseEntity.ok().body(values);
        } catch (CacheBusinessServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        
    }
}
