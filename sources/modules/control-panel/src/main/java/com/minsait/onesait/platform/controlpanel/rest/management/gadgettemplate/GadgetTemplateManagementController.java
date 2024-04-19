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
package com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.GadgetTemplateServiceException;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateExportDto;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateImportResponsetDto;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate.model.GadgetTemplateDTOList;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate.model.GadgetTemplateDTOCreate;
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



@Tag(name = "Gadget templates management")
@RestController
@RequestMapping("api/gadgettemplates")
public class GadgetTemplateManagementController {

	@Autowired
	GadgetTemplateService templatesService;

	@Autowired
	UserRepository userRepo;
	
	@Autowired
	CategoryRelationService categoryRelationService;
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	SubcategoryService subcategoryService;
	
	@Autowired
	AppWebUtils utils;

	@Operation(summary = "Get user gadget templates")
	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=String.class))))
	@GetMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getUserTemplates() {

		final List<GadgetTemplate> userGadgets = templatesService.getUserGadgetTemplate(utils.getUserId());

		final List<GadgetTemplateDTOList> dtos = new ArrayList<>();
		if (userGadgets != null) {
			new ArrayList<GadgetTemplateDTOList>(userGadgets.size());
		} else {
			new ArrayList<GadgetTemplateDTOList>(0);
		}
		for (GadgetTemplate t : userGadgets) {
			dtos.add(toGadgetTemplateDTO(t));
		}
		return new ResponseEntity<>(dtos, HttpStatus.OK);
	}

	@Operation(summary = "Get gadget template by identification")
	@GetMapping(value = "/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getUserTemplates(
			@Parameter(description= "identification of the template", required = true) @PathVariable("identification") String identification) {

		final GadgetTemplate template = templatesService.getGadgetTemplateByIdentification(identification);

		if (template == null) {
			return new ResponseEntity<>("The gadget template does not exist", HttpStatus.NOT_FOUND);
		}
		if (!templatesService.hasUserPermission(template.getId(), utils.getUserId()) && !template.isPublic()) {
			return new ResponseEntity<>("The user does not have permission to view the gadget template",
					HttpStatus.UNAUTHORIZED);
		}

		return new ResponseEntity<>(toGadgetTemplateDTO(template), HttpStatus.OK);

	}

	@Operation(summary = "Create gadget template")
	@PostMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> createGadgetTemplate(@Valid @RequestBody GadgetTemplateDTOCreate dto) {

		if (dto.getIdentification() == null || dto.getIdentification().isEmpty() || dto.getHtml() == null
				|| dto.getJs() == null)
			return new ResponseEntity<>("Missing required fields. Required = [identification, html, js]",
					HttpStatus.BAD_REQUEST);
		GadgetTemplate existing = templatesService.getGadgetTemplateByIdentification(dto.getIdentification());
		if (existing != null)
			return new ResponseEntity<>(
					String.format("The gadget template with identification %s already exists", dto.getIdentification()),
					HttpStatus.BAD_REQUEST);
		if (!dto.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		final GadgetTemplateDTO template = toGadgetTemplate(dto);
		templatesService.createGadgetTemplate(template);
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@Operation(summary = "Update gadget template")
	@PutMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> updateGadgetTemplate(@Valid @RequestBody GadgetTemplateDTOCreate dto) {

		if (dto.getIdentification() == null || dto.getIdentification().isEmpty())
			return new ResponseEntity<>("Missing required fields. Required = [identification]", HttpStatus.BAD_REQUEST);

		final GadgetTemplateDTO existing = templatesService.getGadgetTemplateDTOByIdentification(dto.getIdentification());
		if (existing == null) {
			return new ResponseEntity<>(
					String.format("The gadget template with identification %s does not exist", dto.getIdentification()),
					HttpStatus.BAD_REQUEST);
		}
		if (!templatesService.hasUserPermission(existing.getId(), utils.getUserId())) {
			return new ResponseEntity<>("The user does not have permission to edit the gadget template",
					HttpStatus.UNAUTHORIZED);
		}

		copyProperties(existing, dto);
		try {
			templatesService.updateGadgetTemplate(existing);
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Delete template")
	@DeleteMapping(value = "/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> deleteGadgetTemplate(
			@Parameter(description= "identification", required = true) @PathVariable("identification") String identification) {

		GadgetTemplate gT = templatesService.getGadgetTemplateByIdentification(identification);
		if (gT == null) {
			return new ResponseEntity<>("The gadget template does not exist", HttpStatus.BAD_REQUEST);
		}
		if (!templatesService.hasUserPermission(gT.getId(), utils.getUserId())) {
			return new ResponseEntity<>("The user does not have permission to delete the gadget template",
					HttpStatus.UNAUTHORIZED);
		}

		templatesService.deleteGadgetTemplate(gT.getId(), utils.getUserId());

		return new ResponseEntity<>("The gadget template has been removed", HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=GadgetTemplateExportDto.class))))
	@Operation(summary = "Export gadget template by identification")
	@GetMapping("/export/{identification}")
	public ResponseEntity<?> exportGadgetTemplate(
			@Parameter(description= "Gadget Template identification", required = true) @PathVariable("identification") String identification) {
		GadgetTemplateExportDto gadgetTemplateExportDto;
		try {
			gadgetTemplateExportDto = templatesService.exportGradgetTemplate(identification, utils.getUserId());
		} catch (GadgetTemplateServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(gadgetTemplateExportDto, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=List.class))))
	@Operation(summary = "Export gadget template by user")
	@GetMapping("/export")
	public ResponseEntity<?> exportGadgetTemplateByUser() {
		List<GadgetTemplateExportDto> gadgetTemplatesExportDto;
		try {
			gadgetTemplatesExportDto = templatesService.exportGradgetTemplateByUser(utils.getUserId());
		} catch (GadgetTemplateServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(gadgetTemplatesExportDto, HttpStatus.OK);
	}

	@ApiResponses(@ApiResponse(responseCode = "200", description = "OK", content=@Content(schema=@Schema(implementation=List.class))))
	@Operation(summary = "Import gadget templates")
	@PostMapping("/import")
	public ResponseEntity<?> importDashboard(
			@Parameter(description= "Overwrite Gadget Template if exists") @RequestParam(required = false, defaultValue = "false") boolean overwrite,
			@Parameter(description= "GadgetTemplateExportDto", required = true) @Valid @RequestBody List<GadgetTemplateExportDto> gadgetTemplatesExportDto,
			Errors errors) {
		try {
			List<GadgetTemplateImportResponsetDto> result = templatesService
					.importGradgetTemplateByUser(utils.getUserId(), gadgetTemplatesExportDto, overwrite);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (GadgetTemplateServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private GadgetTemplateDTOList toGadgetTemplateDTO(GadgetTemplate template) {

		GadgetTemplateDTOList dto = new GadgetTemplateDTOList();
		dto.setIdentification(template.getIdentification());
		dto.setDescription(template.getDescription());
		dto.setHtml(template.getTemplate());
		dto.setJs(template.getTemplateJS());
		dto.setPublic(template.isPublic());
		dto.setUser(template.getUser().getUserId());
		dto.setType(template.getType());
		dto.setConfig(template.getConfig());
		
		final CategoryRelation cr = categoryRelationService.getByIdType(template.getId());
		if (cr != null) {
			final Category c = categoryService.getCategoryById(cr.getCategory());
			if (c != null)
				dto.setCategory(c.getIdentification());
			final Subcategory s = subcategoryService.getSubcategoryById(cr.getSubcategory());
			if (s != null)
				dto.setSubcategory(s.getIdentification());
		}
		
		dto.setCreatedAt(template.getCreatedAt().toString());
		dto.setUpdatedAt(template.getUpdatedAt().toString());
		
		return dto;
	}

	private void copyProperties(GadgetTemplateDTO template, GadgetTemplateDTOCreate dto) {
		if (dto.getDescription() == null) {
			template.setDescription("");
		} else {
			template.setDescription(dto.getDescription());
		}
		template.setPublic(dto.isPublic());
		template.setTemplate(dto.getHtml());
		template.setTemplateJS(dto.getJs());
		template.setCategory(dto.getCategory());
		template.setSubcategory(dto.getSubcategory());
	}

	private GadgetTemplateDTO toGadgetTemplate(GadgetTemplateDTOCreate dto) {
		final GadgetTemplateDTO template = new GadgetTemplateDTO();
		template.setIdentification(dto.getIdentification());
		template.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
		template.setType(dto.getType() == null ? "angularJS" : dto.getType());
		template.setPublic(dto.isPublic());
		template.setTemplate(dto.getHtml());
		template.setTemplateJS(dto.getJs());
		template.setUser(userRepo.findByUserId(utils.getUserId()));
		template.setCategory(dto.getCategory());
		template.setSubcategory(dto.getSubcategory());
		return template;
	}
}
