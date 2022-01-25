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
package com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate;

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
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate.model.GadgetTemplateDTO;
import com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate.model.GadgetTemplateDTOCreate;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "Gadget templates management", tags = { "Gadget Templates management service" })
@RestController
@RequestMapping("api/gadgettemplates")
public class GadgetTemplateManagementController {

	@Autowired
	GadgetTemplateService templatesService;

	@Autowired
	UserRepository userRepo;

	@Autowired
	AppWebUtils utils;

	@ApiOperation(value = "Get user gadget templates")
	@ApiResponses(@ApiResponse(code = 200, message = "OK", response = String.class))
	@GetMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getUserTemplates() {

		final List<GadgetTemplate> userGadgets = templatesService.getUserGadgetTemplate(utils.getUserId());

		final List<GadgetTemplateDTO> dtos = new ArrayList<>();
		if (userGadgets != null) {
			new ArrayList<GadgetTemplateDTO>(userGadgets.size());
		} else {
			new ArrayList<GadgetTemplateDTO>(0);
		}
		for (GadgetTemplate t : userGadgets) {
			dtos.add(toGadgetTemplateDTO(t));
		}
		return new ResponseEntity<>(dtos, HttpStatus.OK);
	}

	@ApiOperation(value = "Get gadget template by identification")
	@GetMapping(value = "/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> getUserTemplates(
			@ApiParam(value = "identification of the template", required = true) @PathVariable("identification") String identification) {

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

	@ApiOperation(value = "Create gadget template")
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

		final GadgetTemplate template = toGadgetTemplate(dto);
		templatesService.createGadgetTemplate(template);
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@ApiOperation(value = "Update gadget template")
	@PutMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> updateGadgetTemplate(@Valid @RequestBody GadgetTemplateDTOCreate dto) {

		if (dto.getIdentification() == null || dto.getIdentification().isEmpty())
			return new ResponseEntity<>("Missing required fields. Required = [identification]", HttpStatus.BAD_REQUEST);

		final GadgetTemplate existing = templatesService.getGadgetTemplateByIdentification(dto.getIdentification());
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
		templatesService.updateGadgetTemplate(existing);
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete template")
	@DeleteMapping(value = "/{identification}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public ResponseEntity<?> deleteGadgetTemplate(
			@ApiParam(value = "identification", required = true) @PathVariable("identification") String identification) {

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

	private GadgetTemplateDTO toGadgetTemplateDTO(GadgetTemplate template) {

		GadgetTemplateDTO dto = new GadgetTemplateDTO();
		dto.setIdentification(template.getIdentification());
		dto.setDescription(template.getDescription());
		dto.setHtml(template.getTemplate());
		dto.setJs(template.getTemplateJS());
		dto.setPublic(template.isPublic());
		dto.setUser(template.getUser().getUserId());
		dto.setType(template.getType());
		return dto;
	}

	private void copyProperties(GadgetTemplate template, GadgetTemplateDTOCreate dto) {
		if (dto.getDescription() == null) {
			template.setDescription("");
		} else {
			template.setDescription(dto.getDescription());
		}
		template.setPublic(dto.isPublic());
		template.setTemplate(dto.getHtml());
		template.setTemplateJS(dto.getJs());
	}

	private GadgetTemplate toGadgetTemplate(GadgetTemplateDTOCreate dto) {
		final GadgetTemplate template = new GadgetTemplate();
		template.setIdentification(dto.getIdentification());
		template.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
		template.setType(dto.getType() == null ? "angularJS" : dto.getType());
		template.setPublic(dto.isPublic());
		template.setTemplate(dto.getHtml());
		template.setTemplateJS(dto.getJs());
		template.setUser(userRepo.findByUserId(utils.getUserId()));
		return template;
	}
}
