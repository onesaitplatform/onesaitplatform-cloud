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
package com.minsait.onesait.platform.controlpanel.controller.ksql.flow.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.KsqlRelation;
@Service
public class KsqlRelationFIQL {
	
	public KsqlRelationDTO toKsqlRelationDTO(KsqlRelation ksqlRelation) {
		KsqlRelationDTO dto = new KsqlRelationDTO();
		dto.setId(ksqlRelation.getId());
		dto.setCreatedAt(ksqlRelation.getCreatedAt());
		KsqlResourceDTO resourceDTO = new KsqlResourceDTO();
		resourceDTO.setId(ksqlRelation.getKsqlResource().getId());
		resourceDTO.setIdentification(ksqlRelation.getKsqlResource().getIdentification());
		resourceDTO.setDescription(ksqlRelation.getKsqlResource().getDescription());
		resourceDTO.setKsqlType(ksqlRelation.getKsqlResource().getKsqlType());
		resourceDTO.setResourceType(ksqlRelation.getKsqlResource().getResourceType());
		resourceDTO.setStatementText(ksqlRelation.getKsqlResource().getStatementText());
		OntologyJsonSchemaDto ontology = null;
		if (ksqlRelation.getKsqlResource().getOntology() != null) {
			ontology = new OntologyJsonSchemaDto();
			ontology.setAllowsCreateTopic(ksqlRelation.getKsqlResource().getOntology().isAllowsCreateTopic());
			ontology.setIdentification(ksqlRelation.getKsqlResource().getOntology().getIdentification());
			ontology.setJsonSchema(ksqlRelation.getKsqlResource().getOntology().getJsonSchema());
		}
		resourceDTO.setOntology(ontology);
		dto.setKsqlResource(resourceDTO);
		return dto;
	}

	public List<KsqlRelationDTO> toKsqlRelationDTO(List<KsqlRelation> ksqlRelationList) {
		List<KsqlRelationDTO> dtoList = new ArrayList<>();
		for(KsqlRelation relation: ksqlRelationList){
			dtoList.add(toKsqlRelationDTO(relation));
		}
		return dtoList;
	}
}
