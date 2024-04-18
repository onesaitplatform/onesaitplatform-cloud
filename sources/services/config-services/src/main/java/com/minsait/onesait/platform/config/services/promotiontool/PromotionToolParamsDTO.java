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
package com.minsait.onesait.platform.config.services.promotiontool;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class PromotionToolParamsDTO {

	@Getter
	@Setter
	private String rtdbOriginHost;

	@Getter
	@Setter
	private String configdbOriginHost;

	@Getter
	@Setter
	private String flowEngineOriginHost;

	@Getter
	@Setter
	private String notebooksOriginHost;

	@Getter
	@Setter
	private String rtdbTargetHost;

	@Getter
	@Setter
	private String configdbTargetHost;

	@Getter
	@Setter
	private String flowEngineTargetHost;

	@Getter
	@Setter
	private String notebooksTargetHost;

	@Getter
	@Setter
	private Boolean exportOnOrigin;

	@Getter
	@Setter
	private Boolean importOnTarget;

	@Getter
	@Setter
	private ArrayList<String> tenants;

}
