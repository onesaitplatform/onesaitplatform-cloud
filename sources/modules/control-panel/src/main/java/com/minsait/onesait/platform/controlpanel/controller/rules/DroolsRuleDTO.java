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
package com.minsait.onesait.platform.controlpanel.controller.rules;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.Ontology;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class DroolsRuleDTO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private Ontology sourceOntology;
	private Ontology targetOntology;
	private DroolsRule.Type type;
	@Parameter
	private String DRL;
	private MultipartFile table;
	private String identification;
	private boolean active;
	private boolean decisionTable;

	public static DroolsRuleDTO convert(DroolsRule r) {
		return DroolsRuleDTO.builder().id(r.getId()).DRL(r.getDRL() != null ? r.getDRL() : null).type(r.getType())
				.sourceOntology(r.getSourceOntology() != null ? r.getSourceOntology() : null)
				.targetOntology(r.getTargetOntology() != null ? r.getTargetOntology() : null)
				.identification(r.getIdentification()).active(r.isActive())
				.decisionTable(r.getDecisionTable() != null ? true : false).build();
	}
}
