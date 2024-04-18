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
package com.minsait.onesait.platform.config.services.main.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class GroupServicesDTO implements Serializable {

	private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String module;
    
	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String cpu;

	@Getter
	@Setter
	private String memory;

    @Getter
    @Setter
    private String podName;
    
    @Getter
    @Setter
    private List<String> services;

	@Getter
	@Setter
	private Integer pods;
	
	@Getter
    @Setter
    private Boolean state;

}
