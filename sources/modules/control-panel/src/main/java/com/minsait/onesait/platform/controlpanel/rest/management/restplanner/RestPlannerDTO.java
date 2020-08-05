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
package com.minsait.onesait.platform.controlpanel.rest.management.restplanner;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.http.HttpMethod;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestPlannerDTO {

	@ApiModelProperty(required = true)
	@NotNull
	private String identification;
	@ApiModelProperty(required = true)
	private String description;
	@ApiModelProperty(required = true)
	private String cron;
	@ApiModelProperty(required = true)
	private String url;
	@ApiModelProperty(required = true)
	private HttpMethod method;
	@ApiModelProperty
	private String headers;
	@ApiModelProperty
	private String body;
	@ApiModelProperty
	private Date dateFrom;
	@ApiModelProperty
	private Date dateTo;
}
