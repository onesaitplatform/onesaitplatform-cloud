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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard.fiql;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Dashboard.DashboardType;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardUserAccessDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.CommandDTO;
import com.minsait.onesait.platform.config.services.dashboardapi.dto.UpdateCommandDTO;
import com.minsait.onesait.platform.config.services.exceptions.DashboardServiceException;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.controlpanel.rest.management.dashboard.DashboardDTO;

@Service
public class DashboardFIQL {

	@Autowired
	private DashboardConfRepository dashboardConfRepository;
	
	@Autowired
	private InternationalizationService internationalizationService;
	
	public DashboardCreateDTO fromCommandToDashboardCreate(CommandDTO commandDTO, String id, String userId) {
		DashboardCreateDTO dashboard = new DashboardCreateDTO();
		if (id != null)
			dashboard.setId(id);
		dashboard.setIdentification(commandDTO.getInformation().getDashboard());
		String description = "";
		if (commandDTO.getInformation().getDashboardDescription() != null) {
			description = commandDTO.getInformation().getDashboardDescription();
		}
		dashboard.setDescription(description);
		if (commandDTO.getIsPublic() != null) {
			dashboard.setPublicAccess(commandDTO.getIsPublic());
		} else {
			dashboard.setPublicAccess(Boolean.FALSE);
		}
		//
		final List<DashboardConf> listStyles = dashboardConfRepository.findAll();
		String initialStyleId = null;
		String initialIdentification = null;
		if (commandDTO.getInformation().getDashboardStyle() == null) {
			initialIdentification = "notitle";
		} else {
			initialIdentification = commandDTO.getInformation().getDashboardStyle();
		}
		for (final Iterator iterator = listStyles.iterator(); iterator.hasNext();) {
			final DashboardConf dashboardCon = (DashboardConf) iterator.next();
			if (dashboardCon.getIdentification().equals(initialIdentification)) {
				initialStyleId = dashboardCon.getId();
				dashboard.setHeaderlibs(dashboardCon.getHeaderlibs());
				break;
			}
		}
		if (commandDTO.getInformation() != null && commandDTO.getInformation().getDashboardType() != null) {
			dashboard.setType(commandDTO.getInformation().getDashboardType());
		}
		dashboard.setDashboardConfId(initialStyleId);
		if (commandDTO.getInformation() != null && commandDTO.getInformation().getDashboardGenerateImage() != null) {
			dashboard.setGenerateImage(commandDTO.getInformation().getDashboardGenerateImage());
		} else {
			dashboard.setGenerateImage(false);
		}
		dashboard.setHasImage(true);
		if(commandDTO.getInformation() != null && commandDTO.getInformation().getI18n() != null) {
			StringBuilder i18n = new StringBuilder();
			for (String s: commandDTO.getInformation().getI18n()) {
				final Internationalization internationalization = internationalizationService.getInternationalizationByIdentification(s, userId);
				if (internationalization == null) {
					throw new DashboardServiceException("Internationalization " + s + " does not exist.");
				}
				i18n.append(internationalization.getId()).append(",");
			}
			dashboard.setI18n(i18n.toString());
		}
		dashboard.setCategory(commandDTO.getInformation().getCategory());
		dashboard.setSubcategory(commandDTO.getInformation().getSubcategory());
		
		return dashboard;
	}

	public CommandDTO fromUpdateToCommand(UpdateCommandDTO updateDTO) {
		final CommandDTO dto = updateDTO;
		if (updateDTO.getIdentification() != null)
			dto.getInformation().setDashboard(updateDTO.getIdentification());
		if (updateDTO.getDescription() != null)
			dto.getInformation().setDashboardDescription(updateDTO.getDescription());
		return dto;
	}

	public List<DashboardUserAccessDTO> dashAuthstoDTO(List<DashboardUserAccess> dashaccesses) {
		final ArrayList<DashboardUserAccessDTO> dashAuths = new ArrayList<>();
		for (DashboardUserAccess dashua : dashaccesses) {
			DashboardUserAccessDTO dashAccDTO = new DashboardUserAccessDTO();
			dashAccDTO.setUserId(dashua.getUser().getUserId());
			dashAccDTO.setAccessType(dashua.getDashboardUserAccessType().getName());
			dashAuths.add(dashAccDTO);
		}
		return dashAuths;
	}

	public DashboardDTO toDashboardDTO(Dashboard dashboard, String url, String viewUrl, String categoryId,
			String subCategoryId, int nGadgets, List<DashboardUserAccess> dashAuths) {
		List<DashboardUserAccessDTO> dashAuthsDTO = null;
		if (dashAuths != null) {
			dashAuthsDTO = dashAuthstoDTO(dashAuths);
		}
		DashboardType dst;
		if (dashboard.getType() == null) {
			dst = DashboardType.DASHBOARD;
		} else {
			dst = dashboard.getType();
		}
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<String> i18n = new ArrayList<String>();
		internationalizationService.getInternationalizationsByResourceId(dashboard.getId()).stream().forEach(e -> i18n.add(e.getIdentification()));;
		
		DashboardDTO dashboardDTO =
				DashboardDTO.builder().identification(dashboard.getIdentification()).id(dashboard.getId())
				.description(dashboard.getDescription()).user(dashboard.getUser().getUserId())
				.url(url + dashboard.getId()).isPublic(dashboard.isPublic()).category(categoryId)
				.subcategory(subCategoryId).nGadgets(nGadgets).headerlibs(dashboard.getHeaderlibs())
				.createdAt(ft.format(dashboard.getCreatedAt())).modifiedAt(ft.format(dashboard.getUpdatedAt()))
				.viewUrl(viewUrl + dashboard.getId()).dashboardAuths(dashAuthsDTO).type(dst)
				.image(dashboard.getImage()).generateImage(dashboard.isGenerateImage()).i18n(i18n).build();
		if (dashboardDTO.getImage() == null) {
			byte[] byteArray = "".getBytes();
			dashboardDTO.setImage(byteArray);
		}
		
		return dashboardDTO;
	}

}
