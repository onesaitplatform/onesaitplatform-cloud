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
package com.minsait.onesait.platform.systemconfig.init.graylog.stream;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StreamRequestDTO {

	@Getter
	@Setter
	String title;
	
	@Getter
	@Setter
	String description;
	
	@Getter
	@Setter
	@JsonProperty("matching_type")
	String matchingType;
	
	@Getter
	@Setter
	@JsonProperty("remove_matches_from_default_stream")
	boolean removeMatchesFromDefaultStream;
	
	@Getter
	@Setter
	@JsonProperty("index_set_id")
	String indexSetId;
	
	@Getter
	@Setter
	List<StreamRuleDTO> rules;
	
	
}
