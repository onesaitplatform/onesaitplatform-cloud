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
package com.minsait.onesait.platform.controlpanel.rest.management.tag;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.dto.ResourceTagVO;
import com.minsait.onesait.platform.config.services.tag.TagCreate;
import com.minsait.onesait.platform.config.services.tag.TagService;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/tags")
@Tag(name = "Tags")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"),
		@ApiResponse(responseCode = "403", description = "Forbidden") })
@Slf4j
public class TagRestController {

	@Autowired
	private TagService tagService;

	@GetMapping
	@Operation(summary = "List Tags")
	public ResponseEntity<List<com.minsait.onesait.platform.config.model.Tag>> listTags() {
		final List<com.minsait.onesait.platform.config.model.Tag> tags = tagService.getAllTags();
		return ResponseEntity.ok().body(tags);
	}

	@PostMapping
	@Operation(summary = "Create Tags")
	public ResponseEntity<String> createTags(@RequestBody List<TagCreate> tags) {
		tagService.createTags(tags);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/name/{name}")
	@Operation(summary = "Create Tag by name(s)")
	public ResponseEntity<String> createTagName(@PathVariable("name") String name) {
		tagService.createTag(name);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/name/{name}")
	@Operation(summary = "Delete tag by name")
	public ResponseEntity<String> deleteTag(@PathVariable("name") String name) {
		tagService.deleteTag(name);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/name/{name}/resources/{ids}")
	@Operation(summary = "Delete tag resources by ids")
	public ResponseEntity<String> deleteTagResources(@PathVariable("ids") String ids) {
		tagService.deleteByResourceIds(Arrays.asList(ids.split(",")));
		return ResponseEntity.ok().build();
	}
	
	
	@GetMapping(value = { "/name/{name}", "/name/" }, produces = "application/json")	
	@Operation(summary = "Search Resource by name")
	public ResponseEntity<List<ResourceTagVO>> findTags(@PathVariable(required = false) String name) {
		if (name == null) name = ""; 
		final List<ResourceTagVO> tags = tagService.findResourceTagsByName(name);
		return ResponseEntity.ok().body(tags);
	}
	
}
