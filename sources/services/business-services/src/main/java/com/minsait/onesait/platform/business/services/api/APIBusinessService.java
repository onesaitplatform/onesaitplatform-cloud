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
package com.minsait.onesait.platform.business.services.api;

import java.io.File;
import java.util.List;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ClientJS;

import io.swagger.v3.oas.models.OpenAPI;

public interface APIBusinessService {

	File generateJSClient(ClientJS framework, List<String> apiIds, String userId);

	String getSwaggerJSONInternal(Api api);

	OpenAPI getSwaggerJSONInternalOpenAPI(Api api);

	String getSwaggerJSONExternal(Api api);

	OpenAPI getSwaggerJSONExternalOpenAPI(Api api);

	String getOtherApiWithSwagger(Api api, Integer numVersion);

	OpenAPI getOtherApiWithSwaggerOpenAPI(Api api, Integer numVersion);

	String getSwagger(Api api);

	OpenAPI getOpenAPI(Api api);
}
