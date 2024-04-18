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
package com.minsait.onesait.platform.config.components;

import lombok.Getter;

public enum OntologyVirtualSchemaFieldType {

	STRING("string"), OBJECT("object"), NUMBER("number"), INTEGER("integer"), GEOMERTY("geometry"),
//    GEOMERTY_POINT("geometry-point"),
//	GEOMERTY_LINESTRING("geometry-linestring"),
//	GEOMERTY_POLYGON("geometry-polygon"),
//	GEOMERTY_MULTIPOINT("geometry-multipoint"),
//	GEOMERTY_MULTILINESTRING("geometry-multilinestring"),
//	GEOMERTY_MULTIPOLYGON("geometry-multipolygon"),
	FILE("file"), DATE("date"), TIMESTAMP_MONGO("timestamp-mongo"), TIMESTAMP("timestamp"), ARRAY("array"),
	BOOLEAN("boolean");

	@Getter
	private String value;

	OntologyVirtualSchemaFieldType(String value) {
		this.value = value;
	}

	public static OntologyVirtualSchemaFieldType valueOff(String value) {
		for (OntologyVirtualSchemaFieldType v : values())
			if (v.getValue().equalsIgnoreCase(value))
				return v;
		throw new IllegalArgumentException();
	}

}
