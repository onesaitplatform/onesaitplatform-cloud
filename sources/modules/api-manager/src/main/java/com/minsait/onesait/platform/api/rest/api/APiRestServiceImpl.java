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
package com.minsait.onesait.platform.api.rest.api;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.service.api.ApiSecurityService;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.config.model.User;

@Component("apiRestServiceImpl")
public class APiRestServiceImpl implements ApiRestService {

	Locale locale = LocaleContextHolder.getLocale();

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiFIQL apiFIQL;
	@Autowired
	private ApiSecurityService apiSecurityService;

	@Override
	public Response getApi(String identificacion, String tokenUsuario) throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		final Api api = apiService.findApi(identificacion, tokenUsuario);
		if (api != null)
			return Response.ok(apiFIQL.toApiDTO(api)).build();
		else
			return Response.status(Status.NOT_FOUND).build();
	}

	@Override
	public Response getApiFilter(String identificacion, String estado, String usuario, String tokenUsuario)
			throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		final List<Api> apis = apiService.findApis(identificacion, tokenUsuario);
		if (!CollectionUtils.isEmpty(apis))
			return Response.ok(apiFIQL.toApiDTO(apis)).build();
		else
			return Response.status(Status.NOT_FOUND).build();
	}

	@Override
	public Response create(ApiDTO api, String tokenUsuario) throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		apiService.createApi(api, tokenUsuario);
		final String[] params = { api.getIdentification() };
		return Response.ok(params).build();
	}

	@Override
	public Response update(ApiDTO api, String tokenUsuario) throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		apiService.updateApi(api, tokenUsuario);
		final String[] params = { api.getIdentification() };
		return Response.ok(params).build();
	}

	@Override
	public Response delete(ApiDTO api, String tokenUsuario) throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		apiService.removeApi(api, tokenUsuario);
		final String[] params = { api.getIdentification(), api.getVersion().toString() };
		return Response.ok(params).build();
	}

	@Override
	public Response deleteByIdentificacionNumversion(String identificacion, String numversion, String tokenUsuario)
			throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		apiService.removeApiByIdentificacionNumversion(identificacion, numversion, tokenUsuario);
		final Object params[] = { identificacion, numversion };
		return Response.ok(params).build();
	}

	@Override
	public Response getApiUsuario(String idUsuario, String tokenUsuario) throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(tokenUsuario);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		final List<Api> apis = apiService.findApisByUser(idUsuario, tokenUsuario);
		if (!CollectionUtils.isEmpty(apis))
			return Response.ok(apiFIQL.toApiDTO(apis)).build();
		return Response.status(Status.NOT_FOUND).build();
	}

	public ApiServiceRest getApiService() {
		return apiService;
	}

	public void setApiService(ApiServiceRest apiService) {
		this.apiService = apiService;
	}

	public ApiFIQL getApiFIQL() {
		return apiFIQL;
	}

	public void setApiFIQL(ApiFIQL apiFIQL) {
		this.apiFIQL = apiFIQL;
	}

	@Override
	public Response create(String indentifier, ApiStates api, String token) throws GenericOPException {
		final User user = apiSecurityService.getUserByApiToken(token);
		if (user == null)
			return Response.status(Status.UNAUTHORIZED).build();
		final Api apiRes = apiService.changeState(indentifier, api, token);
		if (apiRes != null) {
			final Object[] params = { apiRes };
			return Response.ok(params).build();
		}
		return Response.serverError().build();

	}

}