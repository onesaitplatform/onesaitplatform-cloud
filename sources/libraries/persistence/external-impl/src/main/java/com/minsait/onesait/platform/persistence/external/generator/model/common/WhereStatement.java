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
package com.minsait.onesait.platform.persistence.external.generator.model.common;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WhereStatement {
	@Size(min = 1)
	@NotNull
	private String column;
	@Size(min = 1)
	@NotNull
	private String operator;
	private String value;
	private String condition;
	private String function;

	public WhereStatement(){}

	public WhereStatement(final String column, final String operator, final String value) {
		this.column = column;
		this.operator = operator;
		this.value = value;
	}

	public WhereStatement(final String column, final String operator, final String value, final String condition, final String function) {
		this.column = column;
		this.operator = operator;
		this.value = value;
		this.condition = condition;
		this.function = function;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(final String column) {
		this.column = column != null ? column.trim() : null;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(final String operator) {
		this.operator = operator != null ? operator.trim() : null;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value != null ? value.trim() : null;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(final String condition) {
		this.condition = condition != null ? condition.trim() : null;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(final String function) {
		this.function = function != null ? function.trim() : null;
	}

	public boolean hasFunction(){
		return function != null && function.trim().length() > 1;
	}
}
