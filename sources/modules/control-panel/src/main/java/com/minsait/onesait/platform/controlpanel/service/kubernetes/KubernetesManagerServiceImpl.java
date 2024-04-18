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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.minsait.onesait.platform.config.dto.ContainerDTO;
import com.minsait.onesait.platform.config.dto.DeploymentDTO;
import com.minsait.onesait.platform.config.dto.NodeDTO;
import com.minsait.onesait.platform.config.dto.PodDTO;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.ContainerMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KubernetesManagerServiceImpl implements KubernetesManagerService {


	private KubernetesClient kubernetesClient;
	private Map<String, Object> kubernetesConfiguration;
	
	@Autowired
	ConfigurationService configurationService;

	@PostConstruct
	public void init() {
		Configuration config = configurationService.getConfiguration(Type.CUSTOM, "KubernetesManager");
		this.kubernetesConfiguration = (Map<String, Object>) configurationService.fromYaml(config.getYmlConfig()).get("kubernetesconfiguration");
		
		String credentials = kubernetesConfiguration.get("credentials").toString();
		
		if (kubernetesConfiguration.get("type").toString().equalsIgnoreCase("Kubernetes")) {
			final Config configKube = Config.fromKubeconfig(credentials);
			this.kubernetesClient = new KubernetesClientBuilder().withConfig(configKube).build();
		} else {
			String serverUrl = kubernetesConfiguration.get("server_url").toString();
			final Config configKube = new ConfigBuilder().withNamespace(kubernetesConfiguration.get("namespace").toString())
					.withMasterUrl(serverUrl).withUsername(kubernetesConfiguration	.get("username").toString()).withPassword(credentials)
					.build();
			this.kubernetesClient = new KubernetesClientBuilder().withConfig(configKube).build().adapt(OpenShiftClient.class);
		}
	}
	
	@Override
	public List<DeploymentDTO> getModulesByNamespace() throws IOException {
 		DeploymentList dpList = kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString()).list();
 		StatefulSetList stateList = kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).list();
		List<DeploymentDTO> moduleList = new ArrayList<DeploymentDTO>();
		PodList pods = kubernetesClient.pods().inNamespace(kubernetesConfiguration.get("namespace").toString()).list();
		dpList.getItems().forEach(d -> {
			DeploymentDTO dp = new DeploymentDTO(d.getMetadata().getLabels().get("app"),
													d.getMetadata().getName(),
													d.getMetadata().getNamespace(),
													0, "Workload", true,
													d.getSpec().getReplicas(),
													d.getMetadata().getLabels(),
													d.getMetadata().getCreationTimestamp(),
													d.getMetadata().getAnnotations(),
													d.getSpec().getTemplate().getSpec().getContainers().get(0).getImage()
			);
			moduleList.add(dp);
		});
		stateList.getItems().forEach(d -> {
			DeploymentDTO dp = new DeploymentDTO(d.getMetadata().getName(),
													d.getMetadata().getName(),
													d.getMetadata().getNamespace(),
													0, "Stateful Set", true,
													d.getSpec().getReplicas(),
													d.getMetadata().getLabels(),
													d.getMetadata().getCreationTimestamp(),
													d.getMetadata().getAnnotations(),
													d.getSpec().getTemplate().getSpec().getContainers().get(0).getImage()
			);
			moduleList.add(dp);
		});
		pods.getItems().forEach(d -> {
			String serviceName = d.getMetadata().getLabels().get("app");
			moduleList.forEach(m -> {
				if (m.getServiceName()!=null && m.getServiceName().equalsIgnoreCase(serviceName)) {	
					if (d.getStatus().getPhase().equalsIgnoreCase("running") ) {
						m.setNumOfPods(m.getNumOfPods() + 1);
					}
					m.setCreatedAt(d.getStatus().getStartTime());
				}
			});
		});

		PodMetricsList podMetricsList = kubernetesClient.top().pods().inNamespace(kubernetesConfiguration.get("namespace").toString()).metrics();

		podMetricsList.getItems().forEach(p -> {

			p.getContainers().forEach(c -> {
				moduleList.forEach(m -> {
					if (m.getServiceName()!=null && m.getServiceName().equalsIgnoreCase(p.getMetadata().getLabels().get("app"))) {
						m.setMemory(m.getMemory() + Float.parseFloat(c.getUsage().get("memory").getAmount()) / 1000);
						m.setCpu(m.getCpu() + Float.parseFloat(c.getUsage().get("cpu").getAmount()) / 1000000000 );
					}
				});
			});
		});
		float totalcpu= 0f;
		for(DeploymentDTO dp: moduleList) {
			totalcpu = totalcpu +dp.getCpu();	
		}
		log.info("total: " +totalcpu);
		return moduleList;
	}
	
	@Override
	public DeploymentDTO getModuleInfoByNamespaceAndServiceName(String serviceName) throws IOException{
 		DeploymentList dpList = kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString()).list();
		StatefulSetList stateList = kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).list();
		PodList pods = kubernetesClient.pods().inNamespace(kubernetesConfiguration.get("namespace").toString()).list();
		DeploymentDTO module = new DeploymentDTO();
		for (Deployment d : dpList.getItems()) {
			if (serviceName.equals(d.getMetadata().getLabels().get("app"))){
				module = new DeploymentDTO(d.getMetadata().getLabels().get("app"),
													d.getMetadata().getName(),
													d.getMetadata().getNamespace(),
													0, "Workload", true,
													d.getSpec().getReplicas(),
													d.getMetadata().getLabels(),
													d.getMetadata().getCreationTimestamp(),
													d.getMetadata().getAnnotations(),
													d.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
				
				PodDTO podDTOTemplate = new PodDTO(d.getSpec().getTemplate().getMetadata().getLabels(),
													d.getSpec().getTemplate().getMetadata().getAnnotations(),
													d.getSpec().getTemplate().getSpec().getDnsPolicy(),
													d.getSpec().getTemplate().getMetadata().getLabels().get("app"),
													d.getSpec().getTemplate().getSpec().getTerminationGracePeriodSeconds().toString(),
													getStorage(d.getSpec().getTemplate().getSpec().getVolumes()));

				List<ContainerDTO> containerDTOList = new ArrayList<ContainerDTO>();
				
				for (Container container : d.getSpec().getTemplate().getSpec().getContainers()) {
				
					ContainerDTO containerDTOTemplate = new ContainerDTO(serviceName,
													container.getName(),
													container.getImage(),
													container.getImagePullPolicy(),
													getPorts(container.getPorts()),
													container.getCommand().toString(),
													getEnvVar(container.getEnv()),
													getContainerStorage(container.getVolumeMounts()));
					
					containerDTOList.add(containerDTOTemplate);
				}			
				podDTOTemplate.setContainerList(containerDTOList);
				module.setPodDTOTemplate(podDTOTemplate);
			}
		}
		
		for (StatefulSet d : stateList.getItems()) {
			if (serviceName.equals(d.getMetadata().getName())){
				module = new DeploymentDTO(d.getMetadata().getName(),
													d.getMetadata().getName(),
													d.getMetadata().getNamespace(),
													0, "Stateful Set", true,
													d.getSpec().getReplicas(),
													d.getMetadata().getLabels(),
													d.getMetadata().getCreationTimestamp(),
													d.getMetadata().getAnnotations(),
													d.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
				
				PodDTO podDTOTemplate = new PodDTO(d.getSpec().getTemplate().getMetadata().getLabels(),
													d.getSpec().getTemplate().getMetadata().getAnnotations(),
													d.getSpec().getTemplate().getSpec().getDnsPolicy(),
													d.getSpec().getTemplate().getMetadata().getLabels().get("app"),
													d.getSpec().getTemplate().getSpec().getTerminationGracePeriodSeconds().toString(),
													getStorage(d.getSpec().getTemplate().getSpec().getVolumes()));

				List<ContainerDTO> containerDTOList = new ArrayList<ContainerDTO>();
				
				for (Container container : d.getSpec().getTemplate().getSpec().getContainers()) {
				
					ContainerDTO containerDTOTemplate = new ContainerDTO(serviceName,
													container.getName(),
													container.getImage(),
													container.getImagePullPolicy(),
													getPorts(container.getPorts()),
													container.getCommand().toString(),
													getEnvVar(container.getEnv()),
													getContainerStorage(container.getVolumeMounts()));
					
					containerDTOList.add(containerDTOTemplate);
				}
				
				podDTOTemplate.setContainerList(containerDTOList);
				module.setPodDTOTemplate(podDTOTemplate);
			}
		};
		
		for (Pod d : pods.getItems()) {
			if (module.getServiceName()!=null && module.getServiceName().equalsIgnoreCase(d.getMetadata().getLabels().get("app"))) {
				
				PodDTO podDTO = new PodDTO(d.getMetadata().getLabels(),
													d.getMetadata().getAnnotations(),
													d.getSpec().getDnsPolicy(),
													d.getMetadata().getLabels().get("app"),
													d.getSpec().getTerminationGracePeriodSeconds().toString(),
													getStorage(d.getSpec().getVolumes()));
				
				podDTO.setStartTime(d.getStatus().getStartTime());
				module.setCreatedAt(d.getStatus().getStartTime());
			
				List<ContainerDTO> containerDTOList = new ArrayList<ContainerDTO>();
				
				for (Container container : d.getSpec().getContainers()) {
					ContainerDTO containerDTO = new ContainerDTO(serviceName,
													container.getName(),
													container.getImage(),
													container.getImagePullPolicy(),
													getPorts(container.getPorts()),
													container.getCommand().toString(),
													getEnvVar(container.getEnv()),
													getContainerStorage(container.getVolumeMounts()));
					
							//	containerResourceCPUReservation;
							//	containerResourceMemoryReservation;
							//	containerResourceCPULimit;
							//	containerResourceMemoryLimit;
							//	containerResourceGPULimit;
					
					containerDTOList.add(containerDTO);
				}
				
				podDTO.setContainerList(containerDTOList);
				
				if (d.getStatus().getPhase().equalsIgnoreCase("running") ) {
					module.setNumOfPods(module.getNumOfPods() + 1);
				}
				
				module.setPodDTO(podDTO);
			}
		};
		
		PodMetricsList podMetricsList = kubernetesClient.top().pods().inNamespace(kubernetesConfiguration.get("namespace").toString()).metrics();

		for (PodMetrics p : podMetricsList.getItems()) {
			for (ContainerMetrics c : p.getContainers()) {
				if (module.getServiceName()!=null && module.getServiceName().equalsIgnoreCase(p.getMetadata().getLabels().get("app"))) {
					module.setMemory(module.getMemory() + Float.parseFloat(c.getUsage().get("memory").getAmount()) / 1000);
					module.setCpu(module.getCpu() + Float.parseFloat(c.getUsage().get("cpu").getAmount()) / 1000000000 );
				}
			};
		};
		float totalcpu= 0f;
		totalcpu = totalcpu +module.getCpu();	
		log.info("total: " +totalcpu);
		return module;
	}

	@Override
	public List<NodeDTO> getNodeMetrics() throws JsonMappingException, JsonProcessingException {
		List<NodeDTO> list = new ArrayList<NodeDTO>();
		kubernetesClient.nodes().list().getItems().forEach(n ->{
			
			NodeDTO dto = new NodeDTO();
			dto.setCpu( n.getStatus().getAllocatable().get("cpu").toString());
			dto.setMemory( n.getStatus().getAllocatable().get("memory").getAmount().toString());
			dto.setName(n.getMetadata().getName());
			dto.setStorage(n.getStatus().getAllocatable().get("ephemeral-storage").getAmount().toString());
			dto.setNumpods(n.getStatus().getAllocatable().get("pods").getAmount().toString());
			list.add(dto);
			n.getStatus().getConditions().forEach(c -> {
				log.info(c.getStatus());
				log.info(c.getMessage());
				log.info(c.getReason());
				log.info(c.getType());
			});
		});
		return list;
	}

	@Override
	public boolean resumeDeployment(String deploymentName) throws JsonMappingException, JsonProcessingException {
		try {
			Deployment dep = kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString())
					.withName(deploymentName).rolling().resume();
			dep.getStatus();
			return true;
		} catch (Exception e) {
		}
		try {
			kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.rolling().resume();
			return true;
		} catch (Exception e) {
			log.error("Error:", e);

		}
		return false;
	}

	@Override
	public boolean pauseDeployment(String deploymentName) throws JsonMappingException, JsonProcessingException {
		try {
			Deployment dep = kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString())
					.withName(deploymentName).rolling().pause();
			return true;
		} catch (Exception e) {
		}
		try {
			kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.rolling().pause();
			return true;
		} catch (Exception e) {
			log.error("Error", e);
		}
		return false;
	}

	@Override
	public boolean restartDeployment(String deploymentName) {
		try {
			kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.rolling().restart();
			return true;
		} catch (Exception e) {
		}
		try {
			kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.rolling().restart();
			return true;
		} catch (Exception e) {
			log.error("Error", e);
		}
		return false;
	}

	@Override
	public boolean updateDeploymentImage(String deploymentName, String image) {
		try {
			kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.rolling().updateImage(image);
			return true;
		} catch (Exception e) {
		}
		try {
			kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.rolling().updateImage(image);
			return true;
		} catch (Exception e) {
			log.error("Error", e);

		}
		return false;
	}

	@Override
	public boolean scaleDeployment(String deploymentName, int scale) {
		try {
			kubernetesClient.apps().deployments().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.scale(scale);
			return true;
		} catch (Exception e) {
		}
		try {
			kubernetesClient.apps().statefulSets().inNamespace(kubernetesConfiguration.get("namespace").toString()).withName(deploymentName)
					.scale(scale);
			return true;
		} catch (Exception e) {
			log.error("Error", e);
		}
		return false;
	}
	
	private Map <String,String> getEnvVar(List<EnvVar> env) {
		Map <String,String> envVarList = new HashMap<String,String>();
		for (EnvVar envVar : env) {
			envVarList.put(envVar.getName(), envVar.getValue());
		}
		return envVarList;
	}

	private Map <String,String> getContainerStorage(List<VolumeMount> volumeMounts) {
		Map <String,String> storageMap = new HashMap<String,String>();
		for (VolumeMount containerStorage : volumeMounts) {
			storageMap.put(containerStorage.getName(), containerStorage.getMountPath());
		}
		return storageMap;
	}

	private Map <String,Integer> getPorts(List<ContainerPort> ports) {
		Map <String,Integer> portsMap = new HashMap<String,Integer>();
		for (ContainerPort port : ports) {
			portsMap.put(port.getName(), port.getContainerPort());
		}
		return portsMap;
	}

	private Map<String, String> getStorage(List<Volume> volumes) {
		Map <String,String> storage = new HashMap<String,String>();
		
		for (Volume volume : volumes) {
			if (volume.getPersistentVolumeClaim()!=null) {
				storage.put(volume.getName(), volume.getPersistentVolumeClaim().getClaimName());
			}
		}
		return storage;
	}
}
