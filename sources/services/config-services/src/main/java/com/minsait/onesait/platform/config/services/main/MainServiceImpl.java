/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.repository.ClientPlatformRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.NotebookRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.ProjectRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.main.dto.KpisDTO;

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
	private MarketAssetRepository assetRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private FlowDomainRepository iotflowRepository;

	@Autowired
	private BinaryFileRepository binaryfilesRepository;

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

		// KPI Assets Number
		kpisDTO = new KpisDTO();
		final long assetsNumber = assetRepository.count();
		kpisDTO.setValue(assetsNumber);
		kpisDTO.setIdentification("Assets");

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

		return kpisDTOList;
	}

}
