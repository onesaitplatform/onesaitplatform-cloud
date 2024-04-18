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
package com.minsait.onesait.platform.controlpanel.services.gravitee;

import java.util.List;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiCreate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPage;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageResponse;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageUpdate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPlan;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiUpdate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.Application;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeApi;

public interface GraviteeService {

	ApiUpdate createApi(ApiCreate api) throws GenericOPException;

	ApiUpdate updateApi(ApiUpdate api) throws GenericOPException;

	void updateApiFromSwagger(Api api) throws GenericOPException;

	ApiPlan createDefaultPlan(String apiId) throws GenericOPException;

	void deletePlan(String apiId, String planId) throws GenericOPException;

	List<ApiPlan> getApiPlans(String apiId) throws GenericOPException;

	void startApi(String apiId) throws GenericOPException;

	void stopApi(String apiId) throws GenericOPException;

	void deployApi(String apiId) throws GenericOPException;

	ApiUpdate getApi(String apiId) throws GenericOPException;

	void deleteApi(String apiId) throws GenericOPException;

	ApiPage createSwaggerDocPage(String apiId, Api api) throws GenericOPException;

	GraviteeApi processApi(Api api, boolean jwtPlan, String clientId) throws GenericOPException;

	void changeLifeCycleState(String graviteeId, ApiStates state) throws GenericOPException;

	ApiPageResponse updateApiPage(String apiId, String pageId, ApiPageUpdate apiPage) throws GenericOPException;

	List<ApiPageResponse> getPublishedApiPages(String apiId) throws GenericOPException;

	public ApiPageResponse processUpdateAPIDocs(Api api, String content) throws GenericOPException;

	void createUpdateIdentityProvider();

	Application createApplication(String clientId);

	List<Application> getApplications();

	void createOauthPlan(String apiId, String clientId);

	void closePlan(String apiId, String planId) throws GenericOPException;

	Application getApplicationsByClientId(String clientId);

	boolean hasJWTPlan(String apiId);

	List<String> getApplicationsSubscribedToAPI(String apiId);

	void subscribeToAPI(String apiId, String application);

	void unsubscribeToAPI(String apiId, String application);

	String getURLIframe(String apiId, String iframe);

	boolean isApiSync(String apiId);

}
