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
package com.minsait.onesait.platform.config.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class DeploymentDTO {
	

	private String deploymentName;
	private String serviceName;
	private String namespace;
	private String type;
	private String description;
	private boolean active;
	float cpu;
	float memory;
	private int replicas;
	private Map<String,String> labels;
	private Map<String,String> annotations;
	private String image;
	private String createdAt;
	private int numOfPods ;
	private PodDTO podDTO;
	private PodDTO podDTOTemplate;
	
	public DeploymentDTO(String app, String name, String namespace, int numpods, String type, boolean active,
			Integer replicas, Map<String, String> labels, String creationTimestamp, Map<String, String> annotations,
			String image) {
		super();
		this.serviceName = app;
		if (app==null) {
			this.serviceName = name;
		}
		this.deploymentName = name;
		this.numOfPods = numpods;
		this.type = type;
		this.active = active;
		this.replicas = replicas;
		this.labels = labels;
		this.createdAt = creationTimestamp;
		this.annotations = annotations;
		this.image = image;
	}
}
