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
package com.minsait.onesait.platform.config.services.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.adminpanel.AdminPanelConfiguration;
import com.minsait.onesait.platform.config.model.adminpanel.ModuleService;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.MicroserviceRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.main.dto.GroupModulesDTO;
import com.minsait.onesait.platform.config.services.main.dto.GroupServicesDTO;
import com.minsait.onesait.platform.config.services.main.dto.KpisDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private ClientPlatformRepository deviceRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private GadgetRepository gadgetRepository;

    @Autowired
    private NotebookRepository notebookRepository;

    @Autowired
    private PipelineRepository dataflowRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FlowDomainRepository iotflowRepository;

    @Autowired
    private BinaryFileRepository binaryfilesRepository;

    @Autowired
    private ConfigurationService configurationService;
    
    @Autowired
    private MicroserviceRepository microservicesRepository;

    @Override
    public ArrayList<KpisDTO> createKPIs() {

        KpisDTO kpisDTO = null;
        final ArrayList<KpisDTO> kpisDTOList = new ArrayList<>();

        // KPI Ontologies Number
        kpisDTO = new KpisDTO();
        final long ontologyNumber = ontologyRepository.count();
        kpisDTO.setValue(ontologyNumber);
        kpisDTO.setIdentification("Ontologies");

        kpisDTOList.add(kpisDTO);

        // KPI Users Number
        kpisDTO = new KpisDTO();
        final long usersNumber = userRepository.count();
        kpisDTO.setValue(usersNumber);
        kpisDTO.setIdentification("Users");

        kpisDTOList.add(kpisDTO);

        // KPI Apis Number
        kpisDTO = new KpisDTO();
        final long apiNumber = apiRepository.count();
        kpisDTO.setValue(apiNumber);
        kpisDTO.setIdentification("Apis");

        kpisDTOList.add(kpisDTO);

        // KPI Dashboards Number
        kpisDTO = new KpisDTO();
        final long dashboardNumber = dashboardRepository.count();
        kpisDTO.setValue(dashboardNumber);
        kpisDTO.setIdentification("Dashboards");

        kpisDTOList.add(kpisDTO);

        // KPI Gadgets Number
        kpisDTO = new KpisDTO();
        final long gadgetNumber = gadgetRepository.count();
        kpisDTO.setValue(gadgetNumber);
        kpisDTO.setIdentification("Gadgets");

        kpisDTOList.add(kpisDTO);

        // KPI Devices Number
        kpisDTO = new KpisDTO();
        final long deviceNumber = deviceRepository.count();
        kpisDTO.setValue(deviceNumber);
        kpisDTO.setIdentification("Devices");

        kpisDTOList.add(kpisDTO);

        // KPI Notebooks Number
        kpisDTO = new KpisDTO();
        final long notebookNumber = notebookRepository.count();
        kpisDTO.setValue(notebookNumber);
        kpisDTO.setIdentification("Notebooks");

        kpisDTOList.add(kpisDTO);

        // KPI DataFlow Number
        kpisDTO = new KpisDTO();
        final long dataflowNumber = dataflowRepository.count();
        kpisDTO.setValue(dataflowNumber);
        kpisDTO.setIdentification("Dataflows");

        kpisDTOList.add(kpisDTO);

        // KPI Projects Number
        kpisDTO = new KpisDTO();
        final long projectNumber = projectRepository.count();
        kpisDTO.setValue(projectNumber);
        kpisDTO.setIdentification("Projects");

        kpisDTOList.add(kpisDTO);

        // KPI IotFlow Number
        kpisDTO = new KpisDTO();
        final long iotflowNumber = iotflowRepository.count();
        kpisDTO.setValue(iotflowNumber);
        kpisDTO.setIdentification("IotFlows");

        kpisDTOList.add(kpisDTO);

        // KPI BinaryFiles Number
        kpisDTO = new KpisDTO();
        final long binaryfilesNumber = binaryfilesRepository.count();
        kpisDTO.setValue(binaryfilesNumber);
        kpisDTO.setIdentification("BinaryFiles");

        kpisDTOList.add(kpisDTO);
        
        // KPI Microservices Number
        kpisDTO = new KpisDTO();
        //final long microservicesNumber = microservicesRepository.count();
        final long microservicesNumber = microservicesRepository.findByActiveTrue().size();
        kpisDTO.setValue(microservicesNumber);
        kpisDTO.setIdentification("Microservices");

        kpisDTOList.add(kpisDTO);

        return kpisDTOList;
    }

    @Override
    public ArrayList<KpisDTO> createKPIsNew() {
        KpisDTO kpisDTO = null;
        final ArrayList<KpisDTO> kpisDTOList = new ArrayList<>();

        // KPI Users Number
        kpisDTO = new KpisDTO();
        final long usersNumber = userRepository.count();
        kpisDTO.setValue(usersNumber);
        kpisDTO.setIdentification("Users");

        kpisDTOList.add(kpisDTO);

        // KPI Users Number
        kpisDTO = new KpisDTO();
        final long newUsersNumber = userRepository.countNewUsers();
        kpisDTO.setValue(newUsersNumber);
        kpisDTO.setIdentification("NewUsers");

        kpisDTOList.add(kpisDTO);

        // KPI Users Number
        kpisDTO = new KpisDTO();
        final long activeUsersNumber = userRepository.countAllActiveUsers();
        kpisDTO.setValue(activeUsersNumber);
        kpisDTO.setIdentification("ActiveUsers");

        kpisDTOList.add(kpisDTO);

        // KPI Users Number
        kpisDTO = new KpisDTO();
        final long inactiveUsersNumber = userRepository.countAllInactiveUsers();
        kpisDTO.setValue(inactiveUsersNumber);
        kpisDTO.setIdentification("InactiveUsers");

        kpisDTOList.add(kpisDTO);

        return kpisDTOList;
    }

    @Override
    public ArrayList<GroupModulesDTO> getGroupModules() {

        final ArrayList<GroupModulesDTO> groupModules = new ArrayList<>();
        Configuration configuration = configurationService.getConfiguration(Type.CUSTOM, "ADMIN_PANEL");
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            AdminPanelConfiguration apconf = mapper.readValue(configuration.getYmlConfig(),
                AdminPanelConfiguration.class);

            List<String> modules = new ArrayList<>(apconf.getModules().keySet());
            int i = 1;
            for (String module : modules) {
                List<ModuleService> services = apconf.getModules().get(module);
                boolean moduleState = true;
                if (services.size() > 0) {
                    GroupModulesDTO group = new GroupModulesDTO();
                    group.setId(String.valueOf(i));
                    if("engine".equals(module)) {
                        group.setDescription("Engine");
                    } else if("intelligence".equals(module)) {
                        group.setDescription("Intelligence");
                    } else if("things".equals(module)) {
                        group.setDescription("Things");
                    } else if("advancedapimanager".equals(module)) {
                        group.setDescription("Advanced API Manager");
                    }
                    group.setIdentification(module);
                    group.setNumModules(services.size());
                    group.setState(moduleState);  //el estado depende del estado de los servicios? Editar nivel javascript
                    groupModules.add(group);

                    i++;
                }
            }
        } catch (final IOException e) {
            log.error("Could not deserialize Yaml of Master RTDB configuration");
            return null;
        }

        return groupModules;
    }
    
    @Override
    public ArrayList<GroupServicesDTO> getGroupServices() {
        final ArrayList<GroupServicesDTO> groupServices = new ArrayList<>();
        Configuration configuration = configurationService.getConfiguration(Type.CUSTOM, "ADMIN_PANEL");
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            AdminPanelConfiguration apconf = mapper.readValue(configuration.getYmlConfig(),
                AdminPanelConfiguration.class);

            List<String> modules = new ArrayList<>(apconf.getModules().keySet());
            for (String module : modules) {
                List<ModuleService> services = apconf.getModules().get(module);
                if (services.size() > 0) {
                    for(ModuleService service : services) {
                        //Hacer las llamadas a prometheus....
                        GroupServicesDTO gservice = new GroupServicesDTO();
                        gservice.setModule(module);
                        gservice.setName(service.getName());
                        gservice.setPodName(service.getPod());
                        gservice.setState(true);
                        //Esto se añade en el javascript con el nombre de los pods
//                        List<String> list = new ArrayList<>();
//                        list.add(service.getNamespace()); //accordion
//                        gservice.setServices(list);
                        
                        //if(!gservice.getState()) moduleState=false; //estado del modulo false si no 

                        groupServices.add(gservice);
                    }
                }
            }
        } catch (final IOException e) {
            log.error("Could not deserialize Yaml of Master RTDB configuration");
            return null;
        }
        return groupServices;
    }

}
