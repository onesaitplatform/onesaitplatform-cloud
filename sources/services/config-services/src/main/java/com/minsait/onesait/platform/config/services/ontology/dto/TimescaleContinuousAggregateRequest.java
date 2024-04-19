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
package com.minsait.onesait.platform.config.services.ontology.dto;

import lombok.Getter;
import lombok.Setter;

public class TimescaleContinuousAggregateRequest {
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String aggregateQuery;
	@Getter
	@Setter
	private String bucketAggregationUnit;
	@Getter
	@Setter
	private Integer bucketAggregation;
	@Getter
	@Setter
	private String schedulingPolicyUnit;
	@Getter
	@Setter
	private Integer schedulingPolicy;
	@Getter
	@Setter
	private String startOffsetUnit;
	@Getter
	@Setter
	private Integer startOffset;
	@Getter
	@Setter
	private String endOffsetUnit;
	@Getter
	@Setter
	private Integer endOffset;
}
