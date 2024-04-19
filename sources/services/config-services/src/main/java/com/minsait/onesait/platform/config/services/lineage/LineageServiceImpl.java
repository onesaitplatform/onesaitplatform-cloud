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
package com.minsait.onesait.platform.config.services.lineage;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.LineageRelations;
import com.minsait.onesait.platform.config.model.LineageRelations.Group;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.LineageRelationsRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class LineageServiceImpl implements LineageService {

	@Autowired
	private LineageRelationsRepository lineageRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private NotebookRepository notebookRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	private PipelineRepository pipelineRepository;
	@Autowired
	private ApiRepository apiRepository;
	@Autowired
	private UserService userService;

	@Override
	public List<LineageRelations> findByTargetOrSource(OPResource resource, User user) {
		if (user.isAdmin()) {
			return lineageRepository.findByTargetOrSource(resource, resource);
		} else {
			List<LineageRelations> relations = lineageRepository.findByTargetOrSource(resource, resource);
			relations = relations.stream().filter(c -> (c.getUser().equals(user) || c.getUser().isAdmin()))
					.collect(Collectors.toList());
			return relations;
		}
	}

	@Override
	public void deleteRelation(String node, String nodeType, String userId) {
		User user = userService.getUser(userId);
		OPResource resource = getOPResource(nodeType, node);
		List<LineageRelations> relations = lineageRepository.findByTargetOrSourceAndUser(user, resource, resource);
		lineageRepository.deleteAll(relations);
	}

	@Override
	@Transactional
	public void createRelation(String source, String target, String sourceType, String targetType, String userId) {
		User user = userService.getUser(userId);
		LineageRelations relation = new LineageRelations();
		OPResource sourceEntity = getOPResource(sourceType, source);
		OPResource targetEntity = getOPResource(targetType, target);

		relation.setSource(sourceEntity);
		relation.setTarget(targetEntity);
		relation.setSourceGroup(Group.valueOf(sourceType));
		relation.setTargetGroup(Group.valueOf(targetType));
		relation.setUser(user);

		lineageRepository.save(relation);
	}

	private OPResource getOPResource(String type, String identification) {
		OPResource entity = null;
		if (Group.valueOf(type.toUpperCase()).equals(Group.ONTOLOGY)) {
			entity = ontologyRepository.findByIdentification(identification);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.DIGITALCLIENT)) {
			entity = clientPlatformRepository.findByIdentification(identification);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.DASHBOARD)) {
			entity = dashboardRepository.findByIdentification(identification).get(0);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.NOTEBOOK)) {
			entity = notebookRepository.findByIdentification(identification);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.GADGET)) {
			entity = gadgetRepository.findByIdentification(identification);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.DATAFLOW)) {
			entity = pipelineRepository.findByIdentification(identification);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.DATASOURCE)) {
			entity = gadgetDatasourceRepository.findByIdentification(identification);
		} else if (Group.valueOf(type.toUpperCase()).equals(Group.API)) {
			String apiId = identification.split("-")[0].trim();
			String apiVersion = identification.split("-")[1].trim().substring(1);
			entity = apiRepository.findByIdentificationAndNumversion(apiId, Integer.parseInt(apiVersion));
		}
		return entity;
	}

}
