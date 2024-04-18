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
package com.minsait.onesait.platform.controlpanel.controller.ksql.relation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ksql.resource.KsqlResourceService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;

@Component
public class KsqlRelationUtil {

	@Autowired
	private KsqlResourceService ksqlResourceService;
	@Autowired
	private OntologyService ontologyService;

	public KsqlResource convertFromDTO(KsqlResourceDTO dto, String sessionUserId) {

		KsqlResource entity = ksqlResourceService.getKsqlResourceById(dto.getResourceId());
		if (entity == null) {
			entity = new KsqlResource();
			entity.setStatementText(dto.getStatement());
			entity.setDescription(dto.getDescription());
			if (dto.getOntology() != null) {
				Ontology ontology = ontologyService.getOntologyByIdentification(dto.getOntology(), sessionUserId);
				entity.setOntology(ontology);
			}
			entity.setResourceType(FlowResourceType.valueOf(dto.getResourceType()));
			ksqlResourceService.parseStatementTextAndGetDependencies(entity);

		}
		return entity;

	}

	public KsqlResource getOriginalFromDTO(KsqlResourceDTO dto) {
		return ksqlResourceService.getKsqlResourceById(dto.getResourceId());
	}

}
