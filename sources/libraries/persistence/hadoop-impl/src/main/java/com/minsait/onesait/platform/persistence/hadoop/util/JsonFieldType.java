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
package com.minsait.onesait.platform.persistence.hadoop.util;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public class JsonFieldType {

	public static final String PROPERTIES_FIELD = "properties";
	public static final String OBJECT_FIELD = "object";
	public static final String TYPE_FIELD = "type";
	public static final String NUMBER_FIELD = "number";
	public static final String INTEGER_FIELD = "integer";
	public static final String BOOLEAN_FIELD = "boolean";
	public static final String STRING_FIELD = "string";
	public static final String GEOMETRY = "geometry";
	public static final String PRIMARY_ID_FIELD = "_id";
	public static final String CONTEXT_DATA_FIELD = "contextdata";

	@Getter
	protected static final List<String> PRIMITIVE_TYPES = Arrays.asList(STRING_FIELD, NUMBER_FIELD, INTEGER_FIELD,
			BOOLEAN_FIELD);

	private JsonFieldType() {
		super();
	}

}
