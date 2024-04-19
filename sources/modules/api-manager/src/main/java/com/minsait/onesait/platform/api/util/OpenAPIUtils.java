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
package com.minsait.onesait.platform.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

@Component
public class OpenAPIUtils {

	public List<String> mapServersToEndpoint(List<Server> servers) {
		final List<String> endpoints = new ArrayList<>();
		for (final Server server : servers) {
			final ServerVariables serverVariables = server.getVariables();
			final String serverUrl = server.getUrl();
			if (CollectionUtils.isEmpty(serverVariables)) {
				endpoints.add(serverUrl);
			} else {
				List<String> evaluatedUrls = Arrays.asList(serverUrl);
				for (final Entry<String, ServerVariable> serverVar : serverVariables.entrySet()) {
					evaluatedUrls = evaluateServerUrlsForOneVar(serverVar.getKey(), serverVar.getValue(),
							evaluatedUrls);
				}
				endpoints.addAll(evaluatedUrls);
			}
		}
		return endpoints;
	}

	private List<String> evaluateServerUrlsForOneVar(String varName, ServerVariable serverVar,
			List<String> templateUrls) {
		final List<String> evaluatedUrls = new ArrayList<>();
		for (final String templateUrl : templateUrls) {
			final Matcher matcher = Pattern.compile("\\{" + varName + "\\}").matcher(templateUrl);
			if (matcher.find()) {
				if (CollectionUtils.isEmpty(serverVar.getEnum()) && serverVar.getDefault() != null) {
					evaluatedUrls.add(templateUrl.replace(matcher.group(0), serverVar.getDefault()));
				} else {
					for (final String enumValue : serverVar.getEnum()) {
						evaluatedUrls.add(templateUrl.replace(matcher.group(0), enumValue));
					}
				}
			}
		}
		return evaluatedUrls;
	}
}
