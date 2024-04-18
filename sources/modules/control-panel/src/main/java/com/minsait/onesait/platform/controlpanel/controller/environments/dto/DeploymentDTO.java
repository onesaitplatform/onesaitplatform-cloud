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
package com.minsait.onesait.platform.controlpanel.controller.environments.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentDTO {
	public String moduleName;
    public String moduleType;
    public boolean statusModule;
    public double moduleCPU;
    public double moduleMemory;
    public int numPodsModule;
    public String image;
    public String createdAt;

    public DeploymentDTO(String deploymentName, String type, float cpu, float memory, int numOfPods, String image, String createdAt) {
		this.moduleName = deploymentName;
		this.moduleType = type;
	    this.numPodsModule = numOfPods;
	    this.image = image;
	    this.createdAt = createdAt;
		this.moduleCPU = new BigDecimal(cpu).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    this.moduleMemory = new BigDecimal(memory/1000).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    this.statusModule = (numOfPods>0 && cpu>0.0 && memory>0.0);
	}
}
