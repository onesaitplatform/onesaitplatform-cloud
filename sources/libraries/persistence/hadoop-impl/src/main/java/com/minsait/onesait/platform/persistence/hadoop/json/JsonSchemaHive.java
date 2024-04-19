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
package com.minsait.onesait.platform.persistence.hadoop.json;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class JsonSchemaHive {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private List<JsonType> fields = new ArrayList<>();

	public String build() {

		StringBuilder sentence = new StringBuilder();

		sentence.append("{ \"$schema\": \"http://json-schema.org/draft-04/schema#\",");
		sentence.append("\"title\": \"");
		sentence.append(name);
		sentence.append(" Schema\",");
		sentence.append("\"type\": \"object\",");
		sentence.append("\"required\": [\"");
		sentence.append(name);
		sentence.append("\"],");
		sentence.append("\"properties\": {");
		sentence.append("\"");
		sentence.append(name);
		sentence.append("\": {");
		sentence.append(" \"type\": \"string\",");
		sentence.append("\"$ref\": \"#/datos\"");
		sentence.append("} },");
		sentence.append("\"datos\": {");
		sentence.append("\"description\": \"Info ");
		sentence.append(name);
		sentence.append("\",");
		sentence.append("\"type\": \"object\",");
		sentence.append("\"required\": [");

		if (fields != null) {
			int numOfColumns = fields.size();
			int i = 0;
			for (JsonType column : fields) {
				sentence.append("\"" + column.getName() + "\"");
				if (i < numOfColumns - 1) {
					sentence.append(", ");
				}
				i++;
			}
		}

		sentence.append("],");
		sentence.append("\"properties\": {");
		sentence.append("");

		if (fields != null) {
			int numOfColumns = fields.size();
			int i = 0;
			for (JsonType column : fields) {
				sentence.append(column.convert());
				if (i < numOfColumns - 1) {
					sentence.append(", ");
				}
				i++;
			}
		}

		sentence.append("}, \"additionalProperties\": false } }");

		return sentence.toString();
	}
}
