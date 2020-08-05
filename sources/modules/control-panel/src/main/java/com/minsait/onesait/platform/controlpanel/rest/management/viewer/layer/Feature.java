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
package com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer;

import java.util.Map;

import com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer.geometry.Geometry;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Feature {

	private String type = "Feature";

	private String oid;

	private Geometry geometry;

	private Map<String, String> properties;

	public Feature(String oid, Geometry geometry, Map<String, String> properties) {
		super();
		this.type = "Feature";
		this.oid = oid;
		this.geometry = geometry;
		this.properties = properties;
	}

}
