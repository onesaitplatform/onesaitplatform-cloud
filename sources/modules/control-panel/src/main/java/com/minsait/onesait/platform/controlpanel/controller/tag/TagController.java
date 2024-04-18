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
package com.minsait.onesait.platform.controlpanel.controller.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.base.OPResource.Resources;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.project.ProjectResourceDTO;
import com.minsait.onesait.platform.config.services.tag.TagCreate;
import com.minsait.onesait.platform.config.services.tag.TagService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
@RequestMapping("tags")
public class TagController {

	@Autowired
	private TagService tagService;
	@Autowired
	private OPResourceService resourceService;
	@Autowired
	private AppWebUtils utils;

	@GetMapping
	public String listTags(Model model) {
		model.addAttribute("tags", tagService.getAllTags());	
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("resourceTypes", Resources.values());
		model.addAttribute("resourcesMatch", new ArrayList<>());
		model.addAttribute("urlsMap", getUrlsMap());
		return "tags/create";
	}
	
	@PostMapping("/name")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String createTag(Model model, @RequestParam("name") String name) {
		tagService.createTag(name);
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("resourceTypes", Resources.values());
		return "tags/fragments/resources-tab";
	}

	@GetMapping("resources")
	public String resources(Model model, @RequestParam("identification") String identification,
			@RequestParam("type") Resources resource) {
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("resourceTypes", Resources.values());
		model.addAttribute("resourcesMatch", getAllResourcesDTO2(identification, resource));
		return "tags/fragments/resources-modal";
	}
	
	@DeleteMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String deleteTag(Model model, @RequestParam("name") String name) {
		tagService.deleteTag(name);
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("resourceTypes", Resources.values());
		return "tags/fragments/resources-tab";
	}

	@PostMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String createTags(Model model, @RequestBody List<TagCreate> tags) {
		tagService.createTags(tags);
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("resourceTypes", Resources.values());
		return "tags/fragments/resources-tab";
	}
	
	@DeleteMapping("/tagId")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_DATASCIENTIST')")
	public String deleteResourceByResourceIdAndTagId(Model model, @RequestParam("resourceId") String resourceId, @RequestParam("tagId") String tagId) {
		tagService.deleteResourceByResourceIdAndTagId(resourceId, tagId);
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("resourceTypes", Resources.values());

		return "tags/fragments/resources-tab";
	}
	
	@DeleteMapping("/unlink")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String deleteByTagId(Model model, @RequestParam("tagId") String tagId) {
		tagService.deleteResourcesByTagId(tagId);
		model.addAttribute("tagNames", tagService.getTagNames());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("resourceTypes", Resources.values());
		return "tags/fragments/resources-tab";
	}
	
	private List<ProjectResourceDTO> getAllResourcesDTO2(String identification, Resources type) {
		String type_resource;
		if (type.name().equals("DATAFLOW")) {
			type_resource = "PIPELINE";
		} else {
			type_resource = type.name();
		}
		final Collection<OPResource> resources2 = resourceService.getResourcesByType(utils.getUserId(), type_resource);

		if (type_resource.equals("API")) {
			return resources2.stream()
					.filter(r -> r.getIdentification().toLowerCase().contains(identification.toLowerCase()))
					.map(r -> ProjectResourceDTO.builder().id(r.getId())
							.identification(r.getIdentification() + " - V" + ((Api) r).getNumversion())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		} else if (type_resource.equals("CONFIGURATION")) {
			return resources2.stream()
					.filter(r -> (r.getIdentification().toLowerCase().contains(identification.toLowerCase())
							&& ((Configuration) r).getType().equals(Configuration.Type.EXTERNAL_CONFIG)))
					.map(r -> ProjectResourceDTO.builder().id(r.getId()).identification(r.getIdentification())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		} else {
			return resources2.stream()
					.filter(r -> r.getIdentification().toLowerCase().contains(identification.toLowerCase()))
					.map(r -> ProjectResourceDTO.builder().id(r.getId()).identification(r.getIdentification())
							.type(r.getClass().getSimpleName()).build())
					.collect(Collectors.toList());
		}
	}
	
	private Map<String, String> getUrlsMap() {
		final Map<String, String> urls = new HashMap<>();
		urls.put(Resources.API.name(), "apimanager");
		urls.put(Resources.CLIENTPLATFORM.name(), "devices");
		urls.put(Resources.BINARYFILE.name(), "files");
		urls.put(Resources.DASHBOARD.name(), "dashboards");
		urls.put(Resources.GADGET.name(), "gadgets");
		urls.put(Resources.DIGITALTWINDEVICE.name(), "digitaltwindevices");
		urls.put(Resources.FLOWDOMAIN.name(), "flows");
		urls.put(Resources.NOTEBOOK.name(), "notebooks");
		urls.put(Resources.ONTOLOGY.name(), "ontologies");
		urls.put(Resources.DATAFLOW.name(), "dataflow");
		urls.put(Resources.GADGETDATASOURCE.name(), "datasources");
		urls.put(Resources.ONTOLOGYVIRTUALDATASOURCE.name(), "virtualdatasources");
		urls.put(Resources.CONFIGURATION.name(), "configurations");
		urls.put(Resources.GADGETTEMPLATE.name(), "gadgettemplates");
		urls.put(Resources.REPORT.name(), "reports");
		urls.put(Resources.FORM.name(), "forms");
		urls.put(Resources.MICROSERVICE.name(), "microservices");
		return urls;
	}
}
