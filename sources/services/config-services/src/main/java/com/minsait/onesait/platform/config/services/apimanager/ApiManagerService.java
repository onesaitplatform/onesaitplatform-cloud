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
package com.minsait.onesait.platform.config.services.apimanager;

import java.io.IOException;
import java.util.List;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.UserApi;

public interface ApiManagerService {

	public List<Api> loadAPISByFilter(String filter, String state, String user, String loggeduser);

	public String createApi(Api api, String objetoOperaciones, String objetoAutenticacion);

	public Integer calculateNumVersion(String numversionData);

	public void updateApi(Api apiMultipartMap, String deprecateApis, String operationsObject,
			String authenticationObject);

	public UserApi updateAuthorization(String apiId, String userId);

	public void removeAuthorizationById(String id);

	public void removeAuthorizationByApiAndUser(String apiId, String userId);

	public byte[] getImgBytes(String id);

	public void updateState(String id, String state);

	public void generateToken(String userId) throws GenericOPException;

	public void removeToken(String userId, String token) throws IOException;

	public void removeAPI(String id);

	public boolean hasUserEditAccess(String apiId, String userId);

	public boolean hasUserAccess(String apiId, String userId);

	public void updateApiPostProcess(String apiId, String postProcessFx);

	public boolean postProcess(Api api);

	public String getPostProccess(Api api);

	public List<ApiOperation> getOperations(Api api);

	public List<ApiOperation> getOperationsByMethod(Api api, Type method);

	public Api getById(String id);

	public void updateApi(Api api);

	public UserApi getUserApiAccessById(String id);

	public String createApiRest(Api api, List<ApiOperation> operations, List<UserApi> authentications);

	public String updateApiRest(Api apinew, Api apimemory, List<ApiOperation> operations,
			List<UserApi> authentications);

	public boolean validateState(ApiStates oldState, String newState);

}
