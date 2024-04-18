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
package com.minsait.onesait.platform.api.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.opendevl.JFlat;
import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
import com.minsait.onesait.platform.api.processor.ApiProcessorDelegate;
import com.minsait.onesait.platform.api.processor.utils.ApiProcessorUtils;
import com.minsait.onesait.platform.api.rule.DefaultRuleBase.ReasonType;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.ApiServiceInterface;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;

@Service

public class ApiServiceImpl extends ApiManagerService implements ApiServiceInterface {

	@Autowired
	private RuleManager ruleManager;

	@Autowired
	private ApiProcessorDelegate processorDelegate;

	private static final String TEXT_CSV = "text/csv";
	private static final String JSON = "JSON";
	private static final String CSV = "CSV";
	private static final String XML_STRING = "XML";

	public enum ChainProcessingStatus {
		STOP, FOLLOW
	}

	@Override
	@ApiManagerAuditable
	public Map<String, Object> processRequestData(HttpServletRequest request, HttpServletResponse response,
			String requestBody) throws GenericOPException {

		final Facts facts = new Facts();
		facts.put(RuleManager.REQUEST, request);
		facts.put(RuleManager.RESPONSE, response);

		final Map<String, Object> dataFact = new HashMap<>();
		dataFact.put(Constants.BODY, requestBody);

		facts.put(RuleManager.FACTS, dataFact);
		ruleManager.fire(facts);

		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final Boolean stopped = facts.get(RuleManager.STOP_STATE);
		String reason = "";
		String reasonType;

		if (stopped != null && stopped) {
			reason = facts.get(RuleManager.REASON);
			reasonType = facts.get(RuleManager.REASON_TYPE);

			if (reasonType.equals(ReasonType.API_LIMIT.name())) {
				data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.TOO_MANY_REQUESTS);
			} else if (reasonType.equals(ReasonType.SECURITY.name())) {
				data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.FORBIDDEN);
			} else {
				data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final String messageError = ApiProcessorUtils.generateErrorMessage(reasonType,
					"Stopped Execution, Found Stop State", reason);
			data.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);
			data.put(Constants.REASON, messageError);

			// Add output to body for camel processing without exceptions
			// TO-DO REMOVE THIS
			data.put(Constants.OUTPUT, messageError);

		} else {
			data.put(Constants.STATUS, "FOLLOW");
		}
		return data;

	}

	@Override
	@ApiManagerAuditable
	public Map<String, Object> processLogic(Map<String, Object> data) throws GenericOPException {
		final Api api = (Api) data.get(Constants.API);
		data = processorDelegate.proxyProcessor(api).process(data);

		return data;
	}

	@Override
	@ApiManagerAuditable
	public Map<String, Object> processOutput(Map<String, Object> data) throws GenericOPException, JSONException {

		final String contentTypeOutput = getContentTypeOutput(data);

		String output = (String) data.get(Constants.OUTPUT);

		if (StringUtils.isEmpty(output)) {
			output = "{}";
		}

		final JSONObject jsonObj = toJSONObject(output);
		final JSONArray jsonArray = toJSONArray(output);
		String outputBody = output;

		if (contentTypeOutput.equalsIgnoreCase(MediaType.APPLICATION_ATOM_XML_VALUE)
				|| contentTypeOutput.equalsIgnoreCase("application/xml")) {
			if (jsonObj != null)
				outputBody = XML.toString(jsonObj);
			if (jsonArray != null)
				outputBody = XML.toString(jsonArray);

		} else if (contentTypeOutput.equalsIgnoreCase(TEXT_CSV)) {
			try {
				if (jsonObj != null) {
					List<Object[]> json2csv;

					json2csv = new JFlat(outputBody).json2Sheet().headerSeparator(".").getJsonAsSheet();

					outputBody = deserializeCSV2D(json2csv);
				}
				if (jsonArray != null) {
					List<Object[]> json2csv;

					json2csv = new JFlat(outputBody).json2Sheet().headerSeparator(".").getJsonAsSheet();

					outputBody = deserializeCSV2D(json2csv);
				}
			} catch (final Exception e) {
				throw new GenericOPException(e);
			}

		}

		data.put(Constants.OUTPUT, outputBody);
		data.put(Constants.CONTENT_TYPE, contentTypeOutput);
		return data;

	}

	private JSONObject toJSONObject(String input) {
		JSONObject jsonObj = null;

		try {
			jsonObj = new JSONObject(input);
		} catch (final JSONException e) {
			return null;
		}
		return jsonObj;
	}

	private JSONArray toJSONArray(String input) {
		JSONArray jsonObj = null;
		try {
			jsonObj = new JSONArray(input);
		} catch (final JSONException e) {
			return null;
		}
		return jsonObj;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static JSONObject getJsonFromMap(Map<String, Object> map) throws JSONException {
		final JSONObject jsonData = new JSONObject();
		for (final Map.Entry<String, Object> e : map.entrySet()) {
			Object value = map.get(e.getKey());
			if (value instanceof Map<?, ?>) {
				value = getJsonFromMap((Map<String, Object>) value);
			}
			jsonData.put(e.getKey(), value);
		}
		return jsonData;
	}

	private static String deserializeCSV2D(List<Object[]> matrix) {
		final StringBuilder builder = new StringBuilder();
		final int size = matrix.get(0).length;
		matrix.forEach(a -> {
			final List<Object> columns = Arrays.asList(a);
			for (int i = 0; i < size; i++) {
				builder.append(columns.get(i));
				if (i + 1 != size)
					builder.append(",");
			}
			builder.append(System.getProperty("line.separator"));
		});
		return builder.toString();
	}

	private String getContentTypeOutput(Map<String, Object> data) {

		final String formatResult = (String) data.get(Constants.FORMAT_RESULT);

		final String contentType = (String) data.get(Constants.CONTENT_TYPE_OUTPUT);

		if (!StringUtils.isEmpty(formatResult)) {
			switch (formatResult.toUpperCase()) {
			case XML_STRING:
				return MediaType.APPLICATION_ATOM_XML_VALUE;
			case CSV:
				return TEXT_CSV;
			case JSON:
				return MediaType.APPLICATION_JSON_VALUE;
			default:
				break;
			}
		}
		if (!StringUtils.isEmpty(contentType)) {
			return contentType;
		}

		return MediaType.TEXT_PLAIN_VALUE;

	}
}
