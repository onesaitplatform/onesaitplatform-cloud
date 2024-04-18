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
package com.minsait.onesait.platform.controlpanel.controller.environments;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.config.dto.DeploymentDTO;
import com.minsait.onesait.platform.config.dto.NodeDTO;
import com.minsait.onesait.platform.controlpanel.controller.environments.dto.EnvironmentDTO;
import com.minsait.onesait.platform.controlpanel.helper.environment.EnvironmentHelper;
import com.minsait.onesait.platform.controlpanel.service.kubernetes.KubernetesManagerService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/environments")
@Slf4j
public class EnvironmentController {

    @Autowired
    private EnvironmentHelper environmentHelper;

	@Autowired
	private AppWebUtils utils;
    
	@Autowired
	KubernetesManagerService kubernetesClient;

    private static final String ENVIRONMENT_LIST = "environments/list";
    private static final String DEPLOYMENTS_LIST = "environments/fragments/environments";
    private static final String DEPLOYMENT_DETAIL = "environments/fragments/deploymentdetail";
    private static final String KPI_LIST = "environments/fragments/kpis";
    private static final String KPI_HOME_LIST = "fragments/home-environment-kpis";
	private static final String STATUS_OK = "{\"status\" : \"ok\"}";
	private static final String STATUS_ERROR = "{\"status\" : \"error\"}";
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/list", produces = "text/html")
    public String list(Model model) {
    	try {
    		List<NodeDTO> nodeMetrics  = kubernetesClient.getNodeMetrics();
    		List<DeploymentDTO> deploymentList = kubernetesClient.getModulesByNamespace();
    		
    		EnvironmentDTO environmentDTO = environmentHelper.setConfiguration();
    		environmentDTO = environmentHelper.setGlobalMetrics(nodeMetrics, environmentDTO);
    		environmentDTO = environmentHelper.setEnvironmentData(deploymentList, environmentDTO);
	        
	        model.addAttribute("environment", environmentDTO);
	        model.addAttribute("deploymentsDetails", deploymentList);
		} catch (IOException e) {
			e.printStackTrace();
			model.addAttribute("environment", new EnvironmentDTO());
			model.addAttribute("error", STATUS_ERROR);
		} catch (RuntimeException e) {
			e.printStackTrace();
			model.addAttribute("environment", new EnvironmentDTO());
			model.addAttribute("error", STATUS_ERROR);
		}
        return ENVIRONMENT_LIST;
    }
    
    @GetMapping(value = "/getdeploymentlist", produces = "text/html")
    public String getDeploymentList(Model model) {
    	if (utils.isAdministrator()) {
	    	try {
	    		List<NodeDTO> nodeMetrics  = kubernetesClient.getNodeMetrics();
	    		List<DeploymentDTO> deploymentList = kubernetesClient.getModulesByNamespace();
	    		
	    		EnvironmentDTO environmentDTO = environmentHelper.setConfiguration();
	    		environmentDTO = environmentHelper.setGlobalMetrics(nodeMetrics, environmentDTO);
	    		environmentDTO = environmentHelper.setEnvironmentData(deploymentList, environmentDTO);
		        
		        model.addAttribute("environment", environmentDTO);
		        model.addAttribute("deploymentsDetails", deploymentList);
			} catch (IOException e) {
				e.printStackTrace();
				model.addAttribute("environment", new EnvironmentDTO());
			} catch (RuntimeException e) {
				e.printStackTrace();
				model.addAttribute("environment", new EnvironmentDTO());
			}
	        return DEPLOYMENTS_LIST;
    	} else {
    		return "/main";
    	}
    }
    
    @GetMapping(value = "/getkpis", produces = "text/html")
    public String getKPIs(Model model) {
    	EnvironmentDTO environmentDTO = getEnvironmentKPIs();
    	model.addAttribute("environment", environmentDTO);
    	if (environmentDTO.getNamespace()!=null) {
	        return KPI_LIST;
    	} else {
    		return "/main";
    	}
    }
    
    @GetMapping(value = "/gethomekpis", produces = "text/html")
    public String getHomeKPIs(Model model) {
    	EnvironmentDTO environmentDTO = getEnvironmentKPIs();
    	model.addAttribute("environment", environmentDTO);
    	if (environmentDTO.getNamespace()!=null) {
	        return KPI_HOME_LIST;
    	} else {
    		return "/main";
    	}
    }  
    
    @GetMapping(value = "/getdeployment/{servicename}", produces = "text/html")
    public String getDeployment(Model model, @PathVariable("servicename") String servicename) {
    	DeploymentDTO deploymentDTO = getDeploymentDetails(servicename);
    	model.addAttribute("deployment", deploymentDTO);
    	if (deploymentDTO.getServiceName()!=null) {
	        return DEPLOYMENT_DETAIL;
    	} else {
    		return "/main";
    	}
    }
    
    private DeploymentDTO getDeploymentDetails(String servicename) {
    	DeploymentDTO deploymentDTO = new DeploymentDTO();
    	if (utils.isAdministrator()) {
    		try {
    			deploymentDTO = kubernetesClient.getModuleInfoByNamespaceAndServiceName(servicename);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
    	}
		return deploymentDTO;
	}

	private EnvironmentDTO getEnvironmentKPIs() {
    	EnvironmentDTO environmentDTO = new EnvironmentDTO();
    	if (utils.isAdministrator()) {	
	    	try {
	    		List<NodeDTO> nodeMetrics  = kubernetesClient.getNodeMetrics();
	    		List<DeploymentDTO> deploymentList = kubernetesClient.getModulesByNamespace();
	    		environmentDTO = environmentHelper.setConfiguration();
	    		environmentDTO = environmentHelper.setGlobalMetrics(nodeMetrics, environmentDTO);
	    		environmentDTO = environmentHelper.setEnvironmentData(deploymentList, environmentDTO);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
    	}
    	return environmentDTO;
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/pause/{deployment}", produces = "text/html")
    public ResponseEntity<String> pause(Model model, @PathVariable("deployment") String deployment) {
		try {
			if (kubernetesClient.pauseDeployment(deployment)) {
				return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/resume/{deployment}", produces = "text/html")
    public ResponseEntity<String> resume(Model model, @PathVariable("deployment") String deployment) {
		try {
			if (kubernetesClient.resumeDeployment(deployment)) {
				return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}  catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/restart/{deployment}", produces = "text/html")
    public ResponseEntity<String> restart(Model model, @PathVariable("deployment") String deployment) {
		try {
			if (kubernetesClient.restartDeployment(deployment)) {
				return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/scale/{deployment}/{scale}", produces = "text/html")
    public ResponseEntity<String> scale(Model model, @PathVariable("deployment") String deployment, @PathVariable("scale") String scale) {
		try {
			if (kubernetesClient.scaleDeployment(deployment, Integer.parseInt(scale))) {
				return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
    @PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
    @GetMapping(value = "/updateImage/{deployment}", produces = "text/html")
    public ResponseEntity<String> updateImage(Model model, @PathVariable("deployment") String deployment, @RequestHeader("image") String image) {
		try {
			if (kubernetesClient.updateDeploymentImage(deployment, image)) {
				return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (IOException e) {
			log.error("Error", e);
			return new ResponseEntity<>(STATUS_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
    
}
