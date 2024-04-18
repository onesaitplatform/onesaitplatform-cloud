/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.WebProjectService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@Controller
public class GraphController {

	private static final String GENERIC_USER_NAME = "USER";
	@Autowired
	private GraphUtil graphUtil;

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private WebProjectService webProjectService;
	@Autowired
	private UserService userService;

	@GetMapping("/getgraph")
	public @ResponseBody String getGraph(Model model, @RequestParam(value = "all", required = false) Boolean all) {
		final List<GraphDTO> arrayLinks = new LinkedList<>();

		final List<WebProject> webprojects = webProjectService
				.getWebProjectsWithDescriptionAndIdentification(utils.getUserId(), null, null);
		final User user = (null == all || all) ? null : userService.getUser(utils.getUserId());
		arrayLinks.add(GraphDTO.constructSingleNode(GENERIC_USER_NAME, null, GENERIC_USER_NAME, utils.getUserId(),
				utils.getUserId()));
		arrayLinks.addAll(graphUtil.constructGraphWithOntologies(null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithClientPlatforms(null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithVisualization(null, null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithAPIs(null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithDigitalTwins(null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithFlows(null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithWebProjects(webprojects, user));
		arrayLinks.addAll(graphUtil.constructGraphWithNotebooks(null, user));
		arrayLinks.addAll(graphUtil.constructGraphWithDataFlows(null, user));
		return arrayLinks.toString();
	}

	@GetMapping("/getgraph/project/{id}")
	public @ResponseBody String getGraph4Project(Model model, @PathVariable("id") String projectId) {
		final Project project = projectService.getById(projectId);
		if (project == null)
			return getGraph(model, true);
		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final List<OPResource> resources = new ArrayList<>(
				projectService.getResourcesForProjectAndUser(projectId, utils.getUserId()));

		List<WebProject> webproject = null;
		if (project.getWebProject() != null)
			webproject = Collections.singletonList(project.getWebProject());

		arrayLinks.add(GraphDTO.constructSingleNode(GENERIC_USER_NAME, null, GENERIC_USER_NAME, utils.getUserId(),
				utils.getUserId()));
		arrayLinks.addAll(graphUtil.constructGraphWithOntologies(resources.stream().filter(r -> r instanceof Ontology)
				.map(r -> (Ontology) r).collect(Collectors.toList()), null));
		arrayLinks.addAll(
				graphUtil.constructGraphWithClientPlatforms(resources.stream().filter(r -> r instanceof ClientPlatform)
						.map(r -> (ClientPlatform) r).collect(Collectors.toList()), null));
		arrayLinks.addAll(graphUtil.constructGraphWithVisualization(
				resources.stream().filter(r -> r instanceof Gadget).map(r -> (Gadget) r).collect(Collectors.toList()),
				resources.stream().filter(r -> r instanceof Dashboard).map(r -> (Dashboard) r)
						.collect(Collectors.toList()),
				null));
		arrayLinks.addAll(graphUtil.constructGraphWithAPIs(
				resources.stream().filter(r -> r instanceof Api).map(r -> (Api) r).collect(Collectors.toList()), null));
		arrayLinks.addAll(
				graphUtil.constructGraphWithDigitalTwins(resources.stream().filter(r -> r instanceof DigitalTwinDevice)
						.map(r -> (DigitalTwinDevice) r).collect(Collectors.toList()), null));
		arrayLinks.addAll(graphUtil.constructGraphWithFlows(resources.stream().filter(r -> r instanceof FlowDomain)
				.map(r -> (FlowDomain) r).collect(Collectors.toList()), null));
		arrayLinks.addAll(graphUtil.constructGraphWithWebProjects(webproject, null));
		arrayLinks.addAll(graphUtil.constructGraphWithNotebooks(resources.stream().filter(r -> r instanceof Notebook)
				.map(r -> (Notebook) r).collect(Collectors.toList()), null));
		arrayLinks.addAll(graphUtil.constructGraphWithDataFlows(resources.stream().filter(r -> r instanceof Pipeline)
				.map(r -> (Pipeline) r).collect(Collectors.toList()), null));

		arrayLinks.stream().forEach(g -> {
			if (g.getLinkSource() != null && g.getLinkSource().toLowerCase().contains("list"))
				g.setLinkSource(null);
			if (g.getLinkTarget() != null && g.getLinkTarget().toLowerCase().contains("list"))
				g.setLinkTarget(null);
		});
		return arrayLinks.toString();

	}

}
