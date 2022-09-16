/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.controlpanel.rest.management.category;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microsoft.sqlserver.jdbc.StringUtils;
import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.rest.management.category.model.CategoryDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.category.model.SubcategoryDTO;
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



@Tag(name = "Categories management")
@RestController
@RequestMapping("api/categories")
public class CategoryManagementController {

	@Autowired
	CategoryService categoryService;
	
	@Autowired
	SubcategoryService subcategoryService;

	private static final String MSG_ERROR_JSON_RESPONSE = "{\"ERROR\":\"%s\"}";
	private static final String MSG_OK_JSON_RESPONSE = "{\"OK\":\"%s\"}";
	private static final String MSG_CATEGORY_NOT_EXIST = "Category does not exist";	
	private static final String MSG_CATEGORY_DELETED = "Category has been deleted successfully";
	private static final String MSG_SUBCATEGORY_NOT_EXIST = "Subcategory does not exist";	
	private static final String MSG_SUBCATEGORY_DELETED = "Subcategory has been deleted successfully";
	private static final String MSG_CATEGORY_MISSING_FIELDS = "Some fields are missing: Identification / Description / Type";
	private static final String MSG_SUBCATEGORY_MISSING_FIELDS = "Some fields are missing: Identification / Description";
	private static final String MSG_TYPE_CANNOT_BE_MODIFIED = "Type cannot be modified";
	private static final String MSG_ERROR_IDENTIFICATION_FORMAT = "Identification must use alphanumeric characters and '-', '_'";
	
	@Operation(summary = "Get categories")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=CategoryDTO.class))))
	@GetMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<Object> getCategories(
			@RequestParam(value = "type", required = false) Category.Type type) {

		List<Category> list = new ArrayList<>(); 
		if (type == null) {
			list = categoryService.findAllCategories();
		} else {
			list = categoryService.getCategoriesByType(type);
		}
		
		List<CategoryDTO> result = new ArrayList<>();
		list.forEach(l -> 
			result.add(CategoryDTO.builder().identification(l.getIdentification())
					.description(l.getDescription()).type(l.getType()).build()));
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Get category by identification")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=CategoryDTO.class))))
	@GetMapping(value = "/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<Object> getCategory(
			@Parameter(description= "Identification of the category", required = true) @PathVariable("identification") String identification) {

		final Category category = categoryService.getCategoryByIdentification(identification);
		if (category == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		} else {
			final CategoryDTO result = CategoryDTO.builder().identification(category.getIdentification())
					.description(category.getDescription()).type(category.getType()).build();
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
	}

	@Operation(summary = "Create category")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=CategoryDTO.class))))
	@PostMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<Object> createCategory(
			@ApiParam(value="CategoryDTO") @Valid @RequestBody CategoryDTO dto) {

		if (StringUtils.isEmpty(dto.getIdentification()) || StringUtils.isEmpty(dto.getDescription())
				|| StringUtils.isEmpty(dto.getDescription())) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_MISSING_FIELDS), HttpStatus.BAD_REQUEST);
		}
		if (!dto.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ERROR_IDENTIFICATION_FORMAT), HttpStatus.BAD_REQUEST);
		}
		
		try {
			final Category category = getCategoryFromDTO(dto);
			categoryService.createCategory(category);
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);	
		} 
	}

	@Operation(summary = "Update category")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=CategoryDTO.class))))
	@PutMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<Object> updateGadgetTemplate(
			@ApiParam(value="CategoryDTO") @Valid @RequestBody CategoryDTO dto) {

		if (StringUtils.isEmpty(dto.getIdentification()) || StringUtils.isEmpty(dto.getDescription())
				|| dto.getType() == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_MISSING_FIELDS), HttpStatus.BAD_REQUEST);
		}
		
		final Category category = categoryService.getCategoryByIdentification(dto.getIdentification());
		if (category == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		} 
		if (category.getType() != dto.getType()) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_TYPE_CANNOT_BE_MODIFIED), HttpStatus.NOT_FOUND);
		}
		
		try {
			category.setDescription(dto.getDescription());
			categoryService.updateCategory(category);
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);	
		} 
	}

	@Operation(summary = "Delete category")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=Object.class))))
	@DeleteMapping(value = "/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<Object> deleteCategory(
			@Parameter(description= "identification", required = true) @PathVariable("identification") String identification) {
		
		final Category category = categoryService.getCategoryByIdentification(identification);
		if (category == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		} 
		try {
			categoryService.deleteCategory(category.getIdentification());
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_CATEGORY_DELETED), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);	
		}
	}

	@Operation(summary = "Get subcategories by category")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=SubcategoryDTO.class))))
	@GetMapping(value = "/{identification}/subcategories")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<Object> getSubcategories(
			@Parameter(description= "Category identification", required = true) @PathVariable("identification") String identification) {

		final Category category = categoryService.getCategoryByIdentification(identification);
		if (category == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		} 
		
		final List<Subcategory> list = subcategoryService.findSubcategoriesByCategory(category);
				
		List<SubcategoryDTO> result = new ArrayList<>();
		list.forEach(l -> 
			result.add(SubcategoryDTO.builder().identification(l.getIdentification())
					.description(l.getDescription()).build()));
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@Operation(summary = "Add subcategory to category")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=Object.class))))
	@PostMapping(value = "/{identification}/subcategory")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<Object> addSubcategory(
			@Parameter(description= "Category identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "SubcategoryDTO") @Valid @RequestBody SubcategoryDTO dto) {

		if (StringUtils.isEmpty(dto.getIdentification()) || StringUtils.isEmpty(dto.getDescription())) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_SUBCATEGORY_MISSING_FIELDS), HttpStatus.BAD_REQUEST);
		}
		if (!dto.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_ERROR_IDENTIFICATION_FORMAT), HttpStatus.BAD_REQUEST);
		}
		
		final Category category = categoryService.getCategoryByIdentification(identification);
		if (category == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		}
		
		try {
			final Subcategory subcategory = getSubcategoryFromDTO(dto);
			subcategoryService.createSubcategory(subcategory, category.getId());
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.BAD_REQUEST);	
		} 
	}
	
	@Operation(summary = "Delete subcategory from category")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=Object.class))))
	@DeleteMapping(value = "/{identification}/subcategory/{subcategoryIdentification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<Object> removeSubcategory(
			@Parameter(description= "Category identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "Subcategory identification", required = true) @PathVariable("subcategoryIdentification") String subcategoryIdentification) {

		final Category category = categoryService.getCategoryByIdentification(identification);
		if (category == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_CATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		}
		final Subcategory subcategory = subcategoryService.getSubcategoryByIdentificationAndCategory(subcategoryIdentification, category);
		if (subcategory == null) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, MSG_SUBCATEGORY_NOT_EXIST), HttpStatus.NOT_FOUND);
		}
		try {
			subcategoryService.deleteSubcategory(subcategory.getIdentification());
			return new ResponseEntity<>(String.format(MSG_OK_JSON_RESPONSE, MSG_SUBCATEGORY_DELETED), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(String.format(MSG_ERROR_JSON_RESPONSE, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);	
		}
	}	
	
	private Category getCategoryFromDTO(CategoryDTO dto) {
		Category category = new Category();
		category.setIdentification(dto.getIdentification());
		category.setDescription(dto.getDescription());
		category.setType(dto.getType());
		return category;
	}
	
	private Subcategory getSubcategoryFromDTO(SubcategoryDTO dto) {
		Subcategory subcategory = new Subcategory();
		subcategory.setIdentification(dto.getIdentification());
		subcategory.setDescription(dto.getDescription());
		return subcategory;
	}
}
