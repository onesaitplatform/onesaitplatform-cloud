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
package com.minsait.onesait.platform.api.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.api.rest.api.fiql.ApiFIQL;
import com.minsait.onesait.platform.api.service.api.ApiServiceRest;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.config.services.apimanager.dto.OperacionDTO;

@RestController
public class OauthApiController {

	private static final String BASE_PATH = "/api-manager/server/api";

	@Autowired
	private ApiServiceRest apiService;

	@Autowired
	private ApiFIQL apiFIQL;

	@RequestMapping(method = RequestMethod.GET, value = "/oauth-api/api-names")
	@ResponseBody
	public List<String> getAPIs() {

		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		String name = a.getName();

		List<Api> list = apiService.findApisByUser(name, null);
		return (list.stream().map(Api::getIdentification).collect(Collectors.toList()));

	}

	private List<String> composeURL(ApiDTO api) {
		int version = api.getVersion();
		String vVersion = "v" + version;
		String identification = api.getIdentification();

		String base = BASE_PATH + "/" + vVersion + "/" + identification + "/";

		ArrayList<OperacionDTO> ops = api.getOperations();
		return (ops.stream().filter(x -> "GET".equals(x.getOperation().name())).map(x -> base + x.getPath())
				.collect(Collectors.toList()));

	}

	@RequestMapping(method = RequestMethod.GET, value = "/oauth-api/api-ops")
	@ResponseBody
	public List<String> getAPIOps() {
		List<String> collect = new ArrayList<>();
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		String name = a.getName();

		List<Api> list = apiService.findApisByUser(name, null);
		for (Api api : list) {
			List<String> l = composeURL(apiFIQL.toApiDTO(api));
			collect.addAll(l);
		}

		return collect;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/oauth-api/apis")
	@ResponseBody
	public List<ApiDTO> getAPIInfos() {

		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		String name = a.getName();

		List<Api> list = apiService.findApisByUser(name, null);
		return (list.stream().map(x -> apiFIQL.toApiDTO(x)).collect(Collectors.toList()));
	}

}