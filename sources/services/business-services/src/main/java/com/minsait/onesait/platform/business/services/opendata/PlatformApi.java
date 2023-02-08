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
package com.minsait.onesait.platform.business.services.opendata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Component
public class PlatformApi {
	
	@Autowired
	private IntegrationResourcesService integrationResourcesService;
	
	public boolean isPlatformDashboard(String resourceUrl) {
		String dashboardEndpoint = integrationResourcesService.getUrl(Module.DASHBOARDENGINE, ServiceUrl.ONLYVIEW);
		return resourceUrl.contains(dashboardEndpoint);
	}
	
	public String getDashboardIdFromUrl(String url) {
		final int endSubstring = url.contains("?") ? url.indexOf("?") : url.length();
		final String dashboardId = url.substring(url.indexOf("/view/") + 6, endSubstring);
		return dashboardId;
	}
	
	public boolean isPlatformViewer(String resourceUrl) {
		String viewerEndpoint = integrationResourcesService.getUrl(Module.GIS_VIEWER, ServiceUrl.VIEW);
		return resourceUrl.contains(viewerEndpoint);
	}
	
	public String getViewerIdFromUrl(String url) {
		final int endSubstring = url.contains("?") ? url.indexOf("?") : url.length();
		final String viewerId = url.substring(url.indexOf("/view/") + 6, endSubstring);
		return viewerId;
	}
	
	public boolean isPlatformApi(String resourceUrl) {
		String apiEndpoint = integrationResourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON);
		return resourceUrl.contains(apiEndpoint);
	}
	
	public String getApiIdentificationFromUrl(String url) {
		final String[] splittedArray = url.split("/");
		final String apiIdentification = splittedArray[splittedArray.length - 2];
		return apiIdentification;
	}

}
