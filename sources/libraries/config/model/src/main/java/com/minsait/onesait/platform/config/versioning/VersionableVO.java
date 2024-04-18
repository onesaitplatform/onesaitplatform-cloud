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
package com.minsait.onesait.platform.config.versioning;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VersionableVO {
	private String name;
	private String id;
	private String className;
	private String userId;

	public VersionableVO(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public VersionableVO(String name, String id, String className) {
		this.name = name;
		this.id = id;
		this.className = className;
	}

	public VersionableVO(String name, String id, String className, String userId) {
		this.name = name;
		this.id = id;
		this.className = className;
		this.userId = userId;
	}

}
