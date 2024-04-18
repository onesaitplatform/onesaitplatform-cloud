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
package com.minsait.onesait.platform.security.dashboard.engine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderByStt {
	private String field;
	private boolean asc;

	public OrderByStt() {
	}

	public OrderByStt(String field, boolean asc) {
		this.field = field;
		this.asc = asc;
	}

	@Override
	public String toString() {
		return "field: '" + this.field + "', asc: '" + this.asc + "'";
	}
}
