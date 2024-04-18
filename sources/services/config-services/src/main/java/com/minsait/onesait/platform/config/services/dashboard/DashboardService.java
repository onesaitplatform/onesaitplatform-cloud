/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.dashboard;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.DashboardUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardCreateDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardExportDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardImportResponsetDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardOrder;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardSimplifiedDTO;
import com.minsait.onesait.platform.config.services.dashboard.dto.DashboardUserAccessDTO;

public interface DashboardService {

	List<DashboardDTO> findDashboardWithIdentificationAndDescription(String identification, String description,	String user);
	
	List<DashboardDTO> findDashboardWithIdentificationAndType(String identification, String type, String user);

	List<String> getAllIdentifications();

	void deleteDashboard(String id, String userId);

	void saveDashboard(String id, Dashboard dashboard, String userId);

	Dashboard getDashboardById(String id, String userId);

	String cloneDashboard(Dashboard originalDashboard, String identification, User user);

	String createNewDashboard(DashboardCreateDTO dashboardCreateDTO, String userId);

	boolean hasUserPermission(String id, String userId);

	boolean dashboardExists(String identification);

	void saveDashboardModel(String id, String model, String userId);

	List<DashboardUserAccess> getDashboardUserAccesses(Dashboard dashboard);

	String saveUpdateAccess(DashboardCreateDTO dashboard, String userId);

	String updatePublicDashboard(DashboardCreateDTO dashboard, String userId);

	String cleanDashboardAccess(DashboardCreateDTO dashboard, String userId);

	Dashboard getDashboardEditById(String id, String userId);

	boolean hasUserEditPermission(String id, String userId);

	boolean hasUserViewPermission(String id, String userId);

	byte[] getImgBytes(String id);

	String deleteDashboardAccess(String dashboardId, String userId);

	List<Dashboard> getByUserId(String userId);

	Dashboard getDashboardByIdentification(String identification, String userId);

	void updateDashboardSimplified(String identification, DashboardSimplifiedDTO dashboard, String userId);

	List<Dashboard> getByUserIdOrdered(String userId, DashboardOrder order);

	int getNumGadgets(Dashboard dashboard);

	DashboardExportDTO addGadgets(DashboardExportDTO dashboard);

	DashboardImportResponsetDTO importDashboard(DashboardExportDTO dashboard, String userId, boolean overwrite,
			boolean importAuthorizations);

	boolean dashboardExistsById(String id);

	String getElementsAssociated(String dashboardId);

	ResponseEntity<byte[]> generateImgFromDashboardId(String id, int waittime, int height, int width, boolean fullpage,
			String params, String oauthtoken);

	ResponseEntity<byte[]> generatePDFFromDashboardId(String id, int waittime, int height, int width, String params,
			String oauthtoken);

	DashboardUserAccess getDashboardUserAccessByIdentificationAndUser(String dashboardId, User user);

	String insertDashboardUserAccess(Dashboard dashboard, List<DashboardUserAccessDTO> dtos, boolean updated);

	String deleteDashboardUserAccess(List<DashboardUserAccessDTO> dtos, String dashboardIdentification,
			boolean deleteAll);

	JSONObject getAllInternationalizationJSON(Dashboard dashboard);
	
	long getClientMaxHeartbeatTime();

	DashboardExportDTO exportDashboardDTO(String dashboardId, String userId);

	// List<DashboardUserAccess> addDashboardUserAccess(List<DashboardUserAccess>
	// usersAccessType, boolean updated);

}
