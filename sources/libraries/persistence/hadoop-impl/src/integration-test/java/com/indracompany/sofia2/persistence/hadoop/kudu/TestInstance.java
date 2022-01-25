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
package com.indracompany.sofia2.persistence.hadoop.kudu;

import com.minsait.onesait.platform.commons.model.ContextData;

import lombok.Getter;
import lombok.Setter;

public class TestInstance {

	@Getter
	@Setter
	private String field1;

	@Getter
	@Setter
	private Long field2;

	@Getter
	@Setter
	private Boolean field3;

	@Getter
	@Setter
	private Integer field4;

	@Getter
	@Setter
	private ContextData contextdata;
}
