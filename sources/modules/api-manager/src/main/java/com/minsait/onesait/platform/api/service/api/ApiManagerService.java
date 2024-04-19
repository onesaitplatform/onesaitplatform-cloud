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
package com.minsait.onesait.platform.api.service.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.exception.BadRequestException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.HeaderType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ApiOperationRepository;
import com.minsait.onesait.platform.config.repository.ApiRepository;

import lombok.extern.slf4j.Slf4j;

@Service("apiManagerService")
@Slf4j
public class ApiManagerService {

	private final List<String> keyWords = new ArrayList<String>() {
		{
			add(" OR ");
			add(" AND ");
			add("DROP ");
			add(";");
			add("%3B");
		}
	};

	private static final String WRONG_PARAMETER_TYPE = "com.indra.sofia2.api.service.wrongparametertype";

	@Autowired
	private ApiRepository apiRepository;

	@Autowired
	private ApiOperationRepository apiOperationRepository;

	public ApiRepository getApiRepository() {
		return apiRepository;
	}

	public void setApiRepository(ApiRepository apiRepository) {
		this.apiRepository = apiRepository;
	}

	public Api getApi(String pathInfo) {
		final ApiType apitipo = null;
		final String apiVersion = getApiVersion(pathInfo);
		final String apiIdentifier = getApiIdentifier(pathInfo);

		return getApi(apiIdentifier, Integer.parseInt(apiVersion), apitipo);
	}

	public String getApiVersion(String pathInfo) {
		final Pattern pattern = Pattern.compile("(.*)/api/v(.*)/");
		final Matcher matcher = pattern.matcher(pathInfo);
		if (matcher.find()) {
			final String param = matcher.group(2);
			return param.substring(0, param.indexOf('/'));
		} else {
			String version = pathInfo;

			if (version.startsWith("/")) {
				version = version.substring(1);
			}

			final int slashIndex = version.indexOf('/');

			if (slashIndex == -1) {
				throw new BadRequestException("com.indra.sofia2.api.service.notvalidformat");
			}

			version = version.substring(0, slashIndex);
			if (version.startsWith("v")) {
				version = version.substring(1);
			}

			if (version == null || version.equals("")) {
				throw new BadRequestException("com.indra.sofia2.api.service.notapiversion");
			}

			return version;
		}
	}

	public String getApiIdentifier(String pathInfo) {

		final String apiVersion = getApiVersion(pathInfo);

		String apiIdentifier = pathInfo.substring(pathInfo.indexOf(apiVersion + "/") + (apiVersion + "/").length());

		int slashIndex = apiIdentifier.indexOf('/');
		if (slashIndex == -1) {
			slashIndex = apiIdentifier.length();
		}

		apiIdentifier = apiIdentifier.substring(0, slashIndex);
		if (apiIdentifier == null || apiIdentifier.equals("")) {
			throw new BadRequestException("com.indra.sofia2.api.service.notapiid");
		}

		return apiIdentifier;
	}

	public Api getApi(String apiIdentifier, int apiVersion, ApiType apiType) {
		if (apiType != null) {
			return apiRepository.findByIdentificationAndNumversionAndApiType(apiIdentifier, apiVersion, apiType);
		} else {
			return apiRepository.findByIdentificationAndNumversion(apiIdentifier, apiVersion);
		}
	}

	public boolean isPathQuery(String pathInfo) {
		final String apiIdentifier = getApiIdentifier(pathInfo);
		final String objectId = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length());

		return objectId.length() == 0 || !objectId.startsWith("/");
	}

	public ApiOperation getCustomSQL(String pathInfo, Api api, String httpVerb) {

		final String apiIdentifier = getApiIdentifier(pathInfo);

		String opIdentifier = pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length());
		if (opIdentifier.startsWith("\\") || opIdentifier.startsWith("/")) {
			opIdentifier = opIdentifier.substring(1);
		}

		opIdentifier = opIdentifier.split("/")[0];

		final List<ApiOperation> operaciones = apiOperationRepository.findByApiOrderByOperationDesc(api);
		Type opType = null;
		if (httpVerb != null) {
			opType = Type.valueOf(httpVerb);
		}
		for (final ApiOperation operacion : operaciones) {
			if (operacion.getIdentification().equals(opIdentifier)
					|| !api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY)
							&& operacion.getPath().startsWith("/" + opIdentifier)) {
				if (opType != null) {
					if (opType.equals(operacion.getOperation())) {
						return operacion;
					}
				} else {
					return operacion;
				}
			}
		}
		return null;
	}

	public ApiOperation getCustomSQLDefault(String pathInfo, Api api, String operation) {

		final String apiIdentifier = getApiIdentifier(pathInfo);

		final List<ApiOperation> operaciones = apiOperationRepository.findByApiOrderByOperationDesc(api);

		final String match = apiIdentifier + "_" + operation;

		for (final ApiOperation operacion : operaciones) {
			if (operacion.getIdentification().equals(match)) {
				return operacion;
			}
		}
		return null;
	}

	public String getOperationPath(String pathInfo) {
		final String apiIdentifier = getApiIdentifier(pathInfo);

		return pathInfo.substring(pathInfo.indexOf(apiIdentifier) + apiIdentifier.length());
	}

	public ApiOperation getFlowEngineApiOperation(String pathInfo, Api api, String method,
			Map<String, String[]> queryParams) {

		final String opIdentifier = getOperationPath(pathInfo);

		final List<ApiOperation> operations = apiOperationRepository.findByApiAndOperation(api, Type.valueOf(method));
		// Checks non path param operations
		for (final ApiOperation operation : operations) {
			if (!hasPathParams(operation) && operation.getPath().equals(opIdentifier)
					&& matchesQueryParams(operation, queryParams)) {
				return operation;
			}
		}

		// Checks path param operations
		for (final ApiOperation operation : operations) {
			if (!operation.getApiqueryparameters().isEmpty()) {
				final String regexp = operation.getPath().replaceAll("\\{([^\\}]+)\\}", "(.*)");
				if (opIdentifier.matches(regexp) && matchesQueryParams(operation, queryParams)) {
					return operation;
				}
			}
		}

		// We need to allow path params on queries that dont were defined without them
		for (final ApiOperation op : operations) {
			return op;
		}
		return null;

	}

	private boolean matchesQueryParams(ApiOperation operation, Map<String, String[]> queryParams) {
		int matchCount = 0;
		for (final Entry<String, String[]> queryParam : queryParams.entrySet()) {
			for (final ApiQueryParameter operationParam : operation.getApiqueryparameters()) {
				if (operationParam.getHeaderType() == HeaderType.QUERY
						&& queryParam.getKey().equals(operationParam.getName())) {
					matchCount++;
				}
			}
		}
		return matchCount == queryParams.size();
	}

	private boolean hasPathParams(ApiOperation operation) {
		for (final ApiQueryParameter param : operation.getApiqueryparameters()) {
			if (param.getHeaderType() == HeaderType.PATH) {
				return true;
			}
		}
		return false;
	}

	private String getCustomParamValue(ApiQueryParameter customqueryparameter, HttpServletRequest request,
			ApiOperation customSQL, String body) {
		switch (customqueryparameter.getHeaderType()) {
		case PATH:
			String paramvalue = null;
			final String apiIdentifier = getApiIdentifier(request.getRequestURI());
			final String relativePath = request.getServletPath()
					.substring(request.getServletPath().indexOf(apiIdentifier) + apiIdentifier.length());
			final String[] splitParams = customSQL.getPath().split("/");

			for (int i = 0; i < splitParams.length; i++) {
				if (splitParams[i].equalsIgnoreCase("{" + customqueryparameter.getName() + "}") && paramvalue == null) {
					paramvalue = relativePath.split("/")[i + 1];
				}

			}
			return paramvalue;
		case BODY:
			return body;
		default:
			return null;
		}
	}

	private String processCustomParamValue(ApiQueryParameter apiQueryParameter, String paramValue) {
		switch (apiQueryParameter.getDataType()) {
		case DATE:
			try {
				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				df.parse(paramValue);
				return "\"" + paramValue + "\"";
			} catch (final Exception e) {
				final Object[] params = { "$" + apiQueryParameter.getName(), "Date" };
				throw new BadRequestException(WRONG_PARAMETER_TYPE + params[0]);
			}
		case STRING:
			try {
				// Escape string value
				return getEscapedString(paramValue);
			} catch (final Exception e) {
				final Object[] params = { "$" + apiQueryParameter.getName(), "String" };
				throw new BadRequestException(WRONG_PARAMETER_TYPE + params[0]);
			}
		case NUMBER:
			try {
				Double.parseDouble(paramValue);
			} catch (final NumberFormatException e) {
				final Object[] params = { "$" + apiQueryParameter.getName(), "Integer" };
				throw new BadRequestException(WRONG_PARAMETER_TYPE + params[0]);
			}
		default:
			return paramValue;
		}
	}

	private String getEscapedString(String paramValue) {
		return "'" + paramValue.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'") + "'";
	}

	public Map<String, String> getCustomParametersValues(HttpServletRequest request, String body,
			Set<ApiQueryParameter> queryParametersCustomQuery, ApiOperation customSQL) {

		final HashMap<String, String> customqueryparametersvalues = new HashMap<>();
		for (final ApiQueryParameter customqueryparameter : queryParametersCustomQuery) {
			String paramvalue = request.getParameter(customqueryparameter.getName());
			if (paramvalue == null) {
				paramvalue = getCustomParamValue(customqueryparameter, request, customSQL, body);
			}

			if (paramvalue != null) {
				if (hasPossibleSQLInjectionSyntax(paramvalue)) {
					throw new IllegalArgumentException("Value contains forbidden SQL Syntax");
				}
				paramvalue = processCustomParamValue(customqueryparameter, paramvalue);
				customqueryparametersvalues.put(customqueryparameter.getName(), paramvalue);
			}
		}
		return customqueryparametersvalues;
	}

	public String buildQuery(String queryDb, Map<String, String> queryParametersValues, User user) {
		for (final Map.Entry<String, String> entry : queryParametersValues.entrySet()) {
			queryDb = queryDb.replace("{$" + entry.getKey() + "}", queryParametersValues.get(entry.getKey()));
		}
		queryDb = queryDb.replace(Constants.CONTEXT_USER, user.getUserId());
		return queryDb;
	}

	public String getObjectidFromPathQuery(String pathInfo, ApiOperation customSQL) {
		String identifier = null;

		if (customSQL == null) {
			identifier = getApiIdentifier(pathInfo);
		} else {
			return "";
		}

		String objectId = pathInfo.substring(pathInfo.indexOf(identifier) + identifier.length());

		if (!objectId.startsWith("/")) {
			return null;
		}
		objectId = objectId.substring(1);

		int slashIndex = objectId.indexOf('/');
		final int parentIndex = objectId.indexOf('(');
		if (slashIndex == -1) {
			slashIndex = objectId.length();
		}
		if (parentIndex != -1 && parentIndex < slashIndex) {
			slashIndex = parentIndex;
		}
		return objectId.substring(0, slashIndex);

	}

	public boolean isSQLLIKE(String query, String queryType) {
		if (query != null && query.length() > 0 && queryType != null && queryType.length() > 0) {
			return queryType.startsWith("SQL");
		}
		return false;
	}

	public String readPayload(HttpServletRequest request) {
		final StringBuilder buffer = new StringBuilder();
		BufferedReader reader;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

		} catch (final IOException e) {
			log.error("Error reading payload", e);
		}
		return buffer.toString();
	}

	public boolean hasPossibleSQLInjectionSyntax(String objectId) {
		if (objectId != null) {
			for (final String keyWord : keyWords) {
				final int length = keyWord.length();
				for (int i = objectId.length() - length; i >= 0; i--) {
					if (objectId.regionMatches(true, i, keyWord, 0, length)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getFieldValue(String objectId) {
		if (isNumeric(objectId)) {
			return objectId;
		} else {
			return getEscapedString(objectId);
		}
	}

	private boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		} else {
			try {
				Double.parseDouble(strNum);
			} catch (final NumberFormatException nfe) {
				return false;
			}
		}
		return true;
	}

}
