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
package com.minsait.onesait.platform.controlpanel.gravitee.dto;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
@Slf4j
public class GroovyPolicy {

	private static final String BASE_URL = "BASE_URL";

	public static final String X_OP_APIKEY_POLICY = "import io.gravitee.policy.groovy.PolicyResult.State\n"
			+ "import java.net.URL\n" + "if (request.headers.containsKey('X-OP-APIKey')) {\n"
			+ "    def api = request.path().split(\"/\")[1];\n"
			+ "	def get = new URL(\"{{BASE_URL}}api-manager/services/management/apis/\" + api).openConnection();\n"
			+ "	get.setRequestProperty(\"X-OP-APIKey\", request.headers.getFirst('X-OP-APIKey'));\n"
			+ "	def getRC = get.getResponseCode();\n" + "	if(!getRC.equals(200)) {\n"
			+ "	    result.state = State.FAILURE;\n" + "		result.code = 403;\n"
			+ "		result.error = \"NOT AUTHORIZED\";\n" + "	}\n" + "\n" + "\n" + "}else{\n"
			+ "	result.state = State.FAILURE;\n" + "	result.code = 500;\n"
			+ "	result.error = \"X-OP-APIKey header missing\";\n" + "}\n" + "";

	private String onRequestScript;

	public static GroovyPolicy xOpApiKeyPolicy(String baseUrl) {
		return GroovyPolicy.builder().onRequestScript(compileServerName(X_OP_APIKEY_POLICY, baseUrl)).build();

	}

	public JsonNode toJsonNode() {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.valueToTree(this);
	}

	private static String compileServerName(String template, String baseUrl) {
		final Writer writer = new StringWriter();
		final StringReader reader = new StringReader(template);
		final HashMap<String, String> scopes = new HashMap<>();
		scopes.put(BASE_URL, baseUrl);
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(reader, "groovy policy");
		mustache.execute(writer, scopes);
		return writer.toString();
	}

}
