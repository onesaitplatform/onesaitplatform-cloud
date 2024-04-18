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
package com.minsait.onesait.platform.business.services.ontology.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.AccessType;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.nebula.exception.NebulaException;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaEdge;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaSpace;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaTag;
import com.minsait.onesait.platform.persistence.nebula.service.NebulaGraphService;
import com.minsait.onesait.platform.persistence.nebula.service.NebulaGraphServiceImpl.NebulaType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NebulaGraphBusinessServiceImpl implements NebulaGraphBusinessService {

	private static final String DATAMODEL_EMPTY_BASE = "EmptyBase";

	@Autowired
	private NebulaGraphService nebulaGraphService;
	@Autowired
	private UserService userService;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private DataModelRepository dataModelRepository;
	@Autowired
	private OntologyService ontologyService;

	@Override
	public void createNebulaGraphEntity(NebulaGraphEntity entity, boolean createEntity) {
		try {
			nebulaGraphService.createSpace(NebulaSpace.builder().name(entity.getName())
					.partitionNum(entity.getPartitions()).replicaFactor(entity.getReplicas()).build(), entity.getTags(),
					entity.getEdges());
			if (createEntity) {
				createAssociatedOntology(entity);
			}
		} catch (final Exception e) {
			log.warn("Failed to create nebula graph entity {} , rolling back actions...", entity.getName());
			nebulaGraphService.dropSpace(entity.getName());
			throw e;
		}
	}

	@Override
	public void updateNebulaGraphEntity(NebulaGraphUpdateEntity entity) {
		final List<NebulaTag> currentTags = nebulaGraphService.getTags(entity.getIdentification());
		final List<NebulaEdge> currentEdges = nebulaGraphService.getEdges(entity.getIdentification());
		processEdgesToUpdate(entity.getEdges(), currentEdges, entity.getIdentification());
		processTagsToUpdate(entity.getTags(), currentTags, entity.getIdentification());
	}

	@Override
	public void deleteNebulaGraphEntity(String identification, String userId) {
		if (ontologyService.hasUserPermission(userService.getUser(userId), AccessType.INSERT,
				ontologyRepository.findByIdentification(identification))) {
			try {
				ontologyRepository.deleteByIdentification(identification);
				nebulaGraphService.dropSpace(identification);
			} catch (final Exception e) {
				log.error("Could not delete ontology {}", identification, e);
				throw e;
			}
		}
	}

	@Override
	public void deleteNebulaGraphEntity(String identification) {

		try {
			nebulaGraphService.dropSpace(identification);
		} catch (final Exception e) {
			log.error("Could not delete ontology {}", identification, e);
			throw e;
		}

	}

	private void createAssociatedOntology(NebulaGraphEntity entity) {
		final DataModel dm = dataModelRepository.findByIdentification(DATAMODEL_EMPTY_BASE).get(0);
		final Ontology o = new Ontology();
		o.setIdentification(entity.getName());
		o.setUser(userService.getUser(entity.getUser()));
		o.setRtdbDatasource(RtdbDatasource.NEBULA_GRAPH);
		o.setJsonSchema(dm.getJsonSchema());
		o.setActive(true);
		o.setContextDataEnabled(false);
		o.setDescription(entity.getDescription());
		o.setMetainf(entity.getMetainf());
		o.setSupportsJsonLd(false);
		o.setRtdbToHdb(false);
		o.setDataModel(dm);
		ontologyRepository.save(o);
	}

	private void validateEntiy(NebulaGraphEntity entity) {
		// TO-DO validate
		throw new NebulaException("Validation error");
	}

	private void processTagsToUpdate(List<NebulaTag> newTags, List<NebulaTag> currentTags, String space) {
		newTags.forEach(nt -> {
			final Optional<NebulaTag> existing = currentTags.stream().filter(ct -> nt.getName().equals(ct.getName()))
					.findFirst();
			if (!existing.isPresent()) {
				nebulaGraphService.createTag(space, nt);
			} else {
				final Map<String, String> currentTagAtts = existing.get().getTagAttributes();
				final Map<String, String> newTagAtts = nt.getTagAttributes();

				final Map<String, String> toAdd = new HashMap<>();
				final Map<String, String> toChange = new HashMap<>();
				final List<String> toDrop = new ArrayList<>();

				computeAddDropChange(toAdd, toChange, toDrop, currentTagAtts, newTagAtts);

				nebulaGraphService.alter(space, NebulaType.TAG, nt.getName(), toAdd, toDrop, toChange);

			}
		});
		currentTags.forEach(ct -> {
			final boolean persists = newTags.stream().anyMatch(nt -> ct.getName().equals(nt.getName()));
			if (!persists) {
				nebulaGraphService.dropTag(space, ct.getName());
			}

		});
	}

	private void processEdgesToUpdate(List<NebulaEdge> newEdges, List<NebulaEdge> currentEdges, String space) {
		newEdges.forEach(ne -> {
			final Optional<NebulaEdge> existing = currentEdges.stream().filter(ce -> ne.getName().equals(ce.getName()))
					.findFirst();
			if (!existing.isPresent()) {
				nebulaGraphService.createEdge(space, ne);
			} else {
				final Map<String, String> currentEdgeAtts = existing.get().getEdgeAttributes();
				final Map<String, String> newEdgeAtts = ne.getEdgeAttributes();

				final Map<String, String> toAdd = new HashMap<>();
				final Map<String, String> toChange = new HashMap<>();
				final List<String> toDrop = new ArrayList<>();

				computeAddDropChange(toAdd, toChange, toDrop, currentEdgeAtts, newEdgeAtts);

				nebulaGraphService.alter(space, NebulaType.EDGE, ne.getName(), toAdd, toDrop, toChange);
			}
		});
		currentEdges.forEach(ce -> {
			final boolean persists = newEdges.stream().anyMatch(ne -> ce.getName().equals(ne.getName()));
			if (!persists) {
				nebulaGraphService.dropEdge(space, ce.getName());
			}

		});
	}

	private void computeAddDropChange(Map<String, String> toAdd, Map<String, String> toChange, List<String> toDrop,
			Map<String, String> currentAtts, Map<String, String> newAtts) {
		newAtts.entrySet().stream().forEach(e -> {
			if (!currentAtts.containsKey(e.getKey())) {
				toAdd.put(e.getKey(), e.getValue());
			} else {
				// CONTAINS BCAUSE MAYBE INT64 F.E.
				if (!currentAtts.get(e.getKey()).contains(e.getValue())) {
					toChange.put(e.getKey(), e.getValue());
				}
			}
		});

		currentAtts.entrySet().stream().forEach(e -> {
			if (!newAtts.containsKey(e.getKey())) {
				toDrop.add(e.getKey());
			}
		});
	}

	@Override
	public List<NebulaTag> getTags(String space) {
		return nebulaGraphService.getTags(space);
	}

	@Override
	public List<NebulaEdge> getEdges(String space) {
		return nebulaGraphService.getEdges(space);
	}

	@Override
	public NebulaSpace getSpace(String space) {
		return nebulaGraphService.getSpace(space);
	}

}
