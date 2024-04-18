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
package com.minsait.onesait.platform.controlpanel.service.kubernetes;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.minsait.onesait.platform.config.dto.DeploymentDTO;
import com.minsait.onesait.platform.config.dto.NodeDTO;

public interface KubernetesManagerService {

	boolean pauseDeployment(String deploymentName) throws JsonMappingException, JsonProcessingException;

	boolean resumeDeployment(String deploymentName) throws JsonMappingException, JsonProcessingException;

	boolean restartDeployment(String deploymentName) throws JsonMappingException, JsonProcessingException;

	boolean scaleDeployment(String deploymentName, int scale) throws JsonMappingException, JsonProcessingException;

	boolean updateDeploymentImage(String deploymentName, String image) throws JsonMappingException, JsonProcessingException;

	List<DeploymentDTO> getModulesByNamespace() throws IOException;

	List<NodeDTO> getNodeMetrics() throws JsonMappingException, JsonProcessingException;

	DeploymentDTO getModuleInfoByNamespaceAndServiceName(String serviceName) throws IOException;

}
