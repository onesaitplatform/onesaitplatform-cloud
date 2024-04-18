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
package com.minsait.onesait.platform.api.rule.rules;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rest.api.ApiManagerEntryPoint;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.util.RequestDumpUtil;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;

@Component
@Rule
public class InitialFactsRule {

	@Autowired
	private RequestDumpUtil rUtil;

	@Priority
	public int getPriority() {
		return 1;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = facts.get(RuleManager.REQUEST);
		return request != null;
	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		final HttpServletRequest request = facts.get(RuleManager.REQUEST);

		final Map<String, Object> data = facts.get(RuleManager.FACTS);

		final String query = Optional.ofNullable(rUtil.getValueFromRequest(Constants.QUERY, request)).orElse("");
		final String queryType = Optional.ofNullable(rUtil.getValueFromRequest(Constants.QUERY_TYPE, request))
				.orElse(QueryType.NONE.name());
		String contentTypeInput = request.getContentType();
		if (contentTypeInput == null)
			contentTypeInput = MediaType.APPLICATION_JSON_VALUE;
		final String contentTypeOutput = Optional.ofNullable(rUtil.getValueFromRequest("accept", request)).orElse("");

		String headerToken = rUtil.getAuthenticationHeader(request);
		headerToken = Optional.ofNullable(headerToken).orElse("");

		final String jwtToken = rUtil.extractJWTToken(request);

		final String method = request.getMethod();
		String pathInfo = request.getServletPath().substring(ApiManagerEntryPoint.ENTRY_POINT_SERVLET_URI.length());
		if (!pathInfo.endsWith("/")) {
			pathInfo = pathInfo.concat("/");
		}

		final String queryDb = Optional.ofNullable(rUtil.getValueFromRequest(Constants.FILTER_PARAM, request))
				.orElse("");
		final String targetDb = Optional.ofNullable(rUtil.getValueFromRequest(Constants.TARGET_DB_PARAM, request))
				.orElse("");
		final String formatResult = Optional.ofNullable(rUtil.getValueFromRequest(Constants.FORMAT_RESULT, request))
				.orElse("");
		final String cacheable = Optional.ofNullable(rUtil.getValueFromRequest(Constants.CACHEABLE, request))
				.orElse("");
		data.put(Constants.REQUEST, request);
		data.put(Constants.HEADERS, request.getHeaderNames());
		data.put(Constants.QUERY_PARAMS, request.getParameterMap());
		data.put(Constants.QUERY, query);
		data.put(Constants.QUERY_TYPE, queryType);
		data.put(Constants.AUTHENTICATION_HEADER, headerToken);
		data.put(Constants.PATH_INFO, pathInfo);
		data.put(Constants.FILTER_PARAM, queryDb);
		data.put(Constants.TARGET_DB_PARAM, targetDb);
		data.put(Constants.FORMAT_RESULT, formatResult);
		data.put(Constants.METHOD, method);
		data.put(Constants.CONTENT_TYPE_INPUT, contentTypeInput);
		data.put(Constants.CONTENT_TYPE_OUTPUT, contentTypeOutput);
		data.put(Constants.CACHEABLE, cacheable);
		data.put(Constants.JWT_TOKEN, jwtToken);
		data.put(Constants.REMOTE_ADDRESS, request.getRemoteAddr());
		facts.put(RuleManager.ACTION, method);

	}
}