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
package com.minsait.onesait.platform.controlpanel.rest.management.gadget.model;

import java.util.List;

import javax.persistence.Lob;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class GadgetDTOCreate {

	@Getter
	@Setter
	@ApiModelProperty(required = true)
	private String identification;

	@Getter
	@Setter
	@ApiModelProperty(required = true)
	private String description;

	@Getter
	@Setter
	@ApiModelProperty(required = true)
	private GadgetDatasourceDTO datasource;

	@Lob
	@Getter
	@Setter
	@ApiModelProperty(required = true)
	private String config;

	@Lob
	@Getter
	@Setter
	private List<String> gadgetMeasures;

	@Getter
	@Setter
	@ApiModelProperty(required = true)
	private String type;

	@Getter
	@Setter
	private Boolean instance;
	
    @Getter
    @Setter
    private String category;

    @Getter
    @Setter
    private String subcategory;

}
