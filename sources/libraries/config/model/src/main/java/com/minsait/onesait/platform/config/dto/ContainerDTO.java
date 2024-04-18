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

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContainerDTO {
	
	private String serviceName;
	private String containerName;
	//private String containerType;
	private String containerImage;
	private String containerImagePullPolicy;
	//private String containerImagePullSecrets;
	private Map<String,Integer> containerPorts;
	private String command;
	private Map<String,String> containerEnvVariables;
	private String containerResourceCPUReservation;
	private String containerResourceMemoryReservation;
	private String containerResourceCPULimit;
	private String containerResourceMemoryLimit;
	private String containerResourceGPULimit;
	private Map<String,String> containerStorage;
	private String creationDate;
	
	public ContainerDTO(String serviceName, String containerName, String image, String imagePullPolicy,	Map<String, Integer> containerPorts, String command, Map<String,String> containerEnvVariables, Map<String, String> containerStorage) {
		this.serviceName = serviceName;
		this.containerName = containerName;
		this.containerImage = image;
		this.containerImagePullPolicy = imagePullPolicy;
		this.containerPorts = containerPorts;
		this.command = command;
		this.containerEnvVariables = containerEnvVariables;
		this.containerStorage = containerStorage;
	}
}
