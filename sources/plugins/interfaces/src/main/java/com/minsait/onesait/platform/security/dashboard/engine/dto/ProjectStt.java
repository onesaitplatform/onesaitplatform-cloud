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
package com.minsait.onesait.platform.security.dashboard.engine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStt {
	private String field;
	private String op;
	private String alias;

	public ProjectStt() {
	}

	public ProjectStt(String field, String op) {
		this.field = field;
		this.op = op;
		this.alias = null;
	}

	public ProjectStt(String field, String op, String exp, String alias) {
		this.field = field;
		this.op = op;
		this.alias = alias;
	}

	public ProjectStt(String field, String op, String alias) {
		this.field = field;
		this.op = op;
		this.alias = alias;
	}

	@Override
	public String toString() {
		return "field: '" + this.field + "', op: '" + this.op + "', alias: '" + this.alias + "'";
	}
}
