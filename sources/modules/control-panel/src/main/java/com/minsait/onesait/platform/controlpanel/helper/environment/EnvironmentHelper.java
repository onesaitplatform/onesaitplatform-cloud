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
package com.minsait.onesait.platform.controlpanel.helper.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.dto.NodeDTO;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.controlpanel.controller.environments.dto.DeploymentDTO;
import com.minsait.onesait.platform.controlpanel.controller.environments.dto.EnvironmentDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnvironmentHelper {

	@Autowired
	ConfigurationService configurationService;
	
	public EnvironmentDTO setConfiguration() {
		EnvironmentDTO environmentDTO = new EnvironmentDTO();
		Configuration config = configurationService.getConfiguration(Type.CUSTOM, "KubernetesManager");
		final Map<String, Object> kubernetesConfiguration = (Map<String, Object>) configurationService
				.fromYaml(config.getYmlConfig()).get("kubernetesconfiguration");
		List<String> nodeList = Stream.of(kubernetesConfiguration.get("node_list").toString().split(",", -1)) .collect(Collectors.toList());
		
		environmentDTO.setNodeList(nodeList);
		environmentDTO.setNamespace(kubernetesConfiguration.get("namespace").toString());
		return environmentDTO;
	}
	
	public EnvironmentDTO setGlobalMetrics(List<NodeDTO> nodeMetrics, EnvironmentDTO environment) {
		double totalMemory = Float.parseFloat("0");
		double totalCpu = Float.parseFloat("0");
		
		for (Iterator<NodeDTO> iterator = nodeMetrics.iterator(); iterator.hasNext();) {
			NodeDTO nodeDTO = (NodeDTO) iterator.next();
			if ((environment.getNodeList().size()==0) || environment.getNodeList().contains(nodeDTO.getName())) {
				totalMemory = new BigDecimal(totalMemory + (Float.parseFloat(nodeDTO.getMemory())/976600)).setScale(2, RoundingMode.HALF_UP).doubleValue();
				totalCpu = new BigDecimal(totalCpu + Float.parseFloat(nodeDTO.getCpu())).setScale(2, RoundingMode.HALF_UP).doubleValue();
			}
		}
		
		environment.setTotalMemory(totalMemory);
		environment.setTotalCpu(totalCpu);
		return environment;
	}
	
    public EnvironmentDTO setEnvironmentData(List<com.minsait.onesait.platform.config.dto.DeploymentDTO> deploymentList, EnvironmentDTO environment) {
        environment.setDeployments(deploymentList.stream()
                .map(deploymentDTO -> new DeploymentDTO(deploymentDTO.getDeploymentName(), 
                										deploymentDTO.getType(),
                										deploymentDTO.getCpu(), 
                										deploymentDTO.getMemory(), 
                										deploymentDTO.getNumOfPods(),
                										deploymentDTO.getImage(),
                										deploymentDTO.getCreatedAt()))
                .collect(Collectors.toList()));
        
        
        double memorySum = deploymentList.stream().collect(Collectors.summarizingDouble(com.minsait.onesait.platform.config.dto.DeploymentDTO::getMemory)).getSum();
        environment.setUsedMemory(new BigDecimal(memorySum/1000).setScale(2, RoundingMode.HALF_UP).doubleValue());
        double cpuSum = deploymentList.stream().collect(Collectors.summarizingDouble(com.minsait.onesait.platform.config.dto.DeploymentDTO::getCpu)).getSum();
        environment.setUsedCpu(new BigDecimal(cpuSum).setScale(2, RoundingMode.HALF_UP).doubleValue());
        
        environment.setNumMicroservices(deploymentList.size());
        return environment;
	}
}