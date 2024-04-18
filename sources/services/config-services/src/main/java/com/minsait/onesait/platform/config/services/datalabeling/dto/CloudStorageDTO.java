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
package com.minsait.onesait.platform.config.services.datalabeling.dto;

import lombok.Getter;
import lombok.Setter;

public class CloudStorageDTO {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String type;

	@Getter
	@Setter
	private String title;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String createdAt;

	@Getter
	@Setter
	private String lastSync;

	@Getter
	@Setter
	private String bucket;
	@Getter
	@Setter
	private String prefix;
	@Getter
	@Setter
	private String regionName;

	@Getter
	@Setter
	private String s3Endpoint;

	@Getter
	@Setter
	private String project;

	@Getter
	@Setter
	private String regexFilter;

}