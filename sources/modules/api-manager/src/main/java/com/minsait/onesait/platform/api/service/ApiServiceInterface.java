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
package com.minsait.onesait.platform.api.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

public interface ApiServiceInterface {
	
	public static final String HTTPS = "HTTPS";
	public static final String HTTP = "HTTP";
	public static final String HTTP_RESPONSE_CODE = "http_response_code";

	public static final String AUTHENTICATION_HEADER = "X-OP-APIKey";
	public static final String AUTHENTICATION_HEADER_OLD = "X-SOFIA2-APIKey";
	public static final String JWT_TOKEN = "JWT_TOKEN";
	public static final String CACHEABLE = "Cacheable";

	public static final String FORM_PARAMETER_MAP = "FORM_PARAMETER_MAP";

	public static final String WEB_SERVICE_API = "webservice";
	public static final String FILTER_PARAM = "filter";
	public static final String TARGET_DB_PARAM = "targetdb";
	public static final String FORMAT_RESULT = "formatResult";
	public static final String QUERY_TYPE = "queryType";

	public static final String QUERY = "query";
	public static final String QUERY_BY_ID = "QUERY_BY_ID";
	public static final String DUMP = "DUMP";
	public static final String BODY = "BODY";
	public static final String METHOD = "METHOD";
	public static final String ODATA_DTO = "ODATA_DTO";
	public static final String API_OPERATION = "API_OPERATION";
	public static final String IS_EXTERNAL_API = "IS_EXTERNAL_API";
	public static final String OBJECT_ID = "OBJECT_ID";
	public static final String ONTOLOGY = "ONTOLOGY";
	public static final String OUTPUT = "OUTPUT";

	public static final String CONTENT_TYPE = "CONTENT_TYPE";
	public static final String CONTENT_TYPE_INPUT = "CONTENT_TYPE_INPUT";
	public static final String CONTENT_TYPE_OUTPUT = "CONTENT_TYPE_OUTPUT";
	public static final String REMOTE_ADDRESS = "remoteAddress";
	public static final String STATUS = "STATUS";
	public static final String REASON = "REASON";

	public static final String USER = "USER";
	public static final String API = "API";

	public static final String PATH_INFO = "PATH_INFO";

	public static final String WEB_SERVICE_PATH = "/ws";
	public static final String ISSQLLIKE = "ISSQLLIKE";

	public static final String QUERY_PARAMS = "QUERY_PARAMS";
	public static final String HEADERS = "HEADERS";
	public static final String REQUEST = "REQUEST";
	
	public static final String CONTEXT_USER = "$context.userId";	

	public Map<String, Object> processRequestData(HttpServletRequest request, HttpServletResponse response,
			String requestBody) throws GenericOPException;

	public Map<String, Object> processLogic(Map<String, Object> data) throws GenericOPException;

	public Map<String, Object> processOutput(Map<String, Object> data) throws GenericOPException, JSONException;

}