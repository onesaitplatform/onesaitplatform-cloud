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
package com.minsait.onesait.platform.controlpanel.controller.rules;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.minsait.onesait.platform.config.model.DroolsRule;

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
public class RuleDTO {

	private String id;
	private String inputOntology;
	private String outputOntology;
	private DroolsRule.Type type;
	@Parameter
	private String drl;
	private String identification;
	private boolean active;
	private String extension;
	private byte[] decisionTable;

	public static RuleDTO convert(DroolsRule r) {
		return RuleDTO.builder().id(r.getId()).drl(r.getDRL()).type(r.getType())
				.inputOntology(r.getSourceOntology() != null ? r.getSourceOntology().getIdentification() : null)
				.outputOntology(r.getTargetOntology() != null ? r.getTargetOntology().getIdentification() : null)
				.identification(r.getIdentification()).active(r.isActive()).decisionTable(r.getDecisionTable()).build();
	}
}
