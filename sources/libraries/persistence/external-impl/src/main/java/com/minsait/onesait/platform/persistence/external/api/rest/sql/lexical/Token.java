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
package com.minsait.onesait.platform.persistence.external.api.rest.sql.lexical;

import lombok.Getter;
import lombok.Setter;

public class Token {

	public enum TokenType {
		FOP, OPERATOR, QUERY_TYPE, WHERE, FROM, OPERATION_SEPARATOR, ALL_FIELDS, FIELD_SEPARATOR, LEFT_PARETHESES, RIGHT_PARENTHESES, END, WORD, JSON, INTO, SET, VALUES, SKIP, OFFSET, LIMIT, AS
	}

	@Getter
	@Setter
	private TokenType type;
	@Getter
	@Setter
	private String content;

	public Token(TokenType type, String content) {
		this.type = type;
		this.content = content;
	}

	@Override
	public String toString() {
		return type + "-" + content;
	}
}
