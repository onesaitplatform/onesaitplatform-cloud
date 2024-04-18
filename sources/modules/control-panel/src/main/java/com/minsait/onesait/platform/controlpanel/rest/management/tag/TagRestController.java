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
package com.minsait.onesait.platform.controlpanel.rest.management.tag;

import java.util.List;

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

import com.minsait.onesait.platform.config.dto.OPResourceVO;
import com.minsait.onesait.platform.config.services.exceptions.TagServiceException;
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

	@GetMapping("/names")
	@Operation(summary = "List Tags Names")
	public ResponseEntity<List<String>> listTagNames() {
		final List<String> tags = tagService.getTagNames();
		return ResponseEntity.ok().body(tags);
	}
	
	@GetMapping
	@Operation(summary = "List All Tags")
	public ResponseEntity<List<com.minsait.onesait.platform.config.model.Tag>> listTags() {
		final List<com.minsait.onesait.platform.config.model.Tag> tags = tagService.getAllTags();
		return ResponseEntity.ok().body(tags);
	}
	
	@PostMapping("/{name}")
	@Operation(summary = "Create Tag by name(s)")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> createTagName(@PathVariable("name") String name) {
		tagService.createTag(name);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping
	@Operation(summary = "Add Tag Resource by id")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createTags(@RequestBody List<TagCreate> tags) {
		try {
			List<com.minsait.onesait.platform.config.model.Tag> res = tagService.createTags(tags);
			return new ResponseEntity<>(res, HttpStatus.CREATED);
		} catch (final TagServiceException exception) {
			return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/{name}")
	@Operation(summary = "Delete Tag by name")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> deleteTag(@PathVariable("name") String name) {
		tagService.deleteTag(name);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/{name}/resources/{ids}")
	@Operation(summary = "Delete Tag Resources by ids (separated by ,)")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> deleteTagResources(@PathVariable("name") String name, @PathVariable("ids") String ids) {
		tagService.deleteByResourceIdsAndTag(name, Arrays.asList(ids.split(",")));
		return ResponseEntity.ok().build();
	}
	
	@GetMapping(value = { "/like/{name}"})	
	@Operation(summary = "Search Resource by Tag name like")
	public ResponseEntity<List<com.minsait.onesait.platform.config.model.Tag>> findTagsLike(@PathVariable(required = false) String name) {
		final List<com.minsait.onesait.platform.config.model.Tag> tags = tagService.findResourceTagsByNameLike(name);
		return ResponseEntity.ok().body(tags);
	}
	
	@GetMapping(value = { "/{name}/resources"})	
	@Operation(summary = "Search Resource by Tag name")
	public ResponseEntity<List<OPResourceVO>> findTag(@PathVariable(required = true) String name) {
		final com.minsait.onesait.platform.config.model.Tag tag = tagService.findResourceTagsByName(name);
		return ResponseEntity.ok().body(tag.getResources());
	}
	
}
