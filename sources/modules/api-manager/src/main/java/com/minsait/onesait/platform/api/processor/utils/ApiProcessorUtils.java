/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.api.processor.utils;

import org.codehaus.jackson.map.ObjectMapper;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class ApiProcessorUtils {

	private ApiProcessorUtils() {
	}

	public static String generateErrorMessage(String cause, String error, String message) {
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(message);
			
			return "{\"result\":\"" + cause + "\", \"message\":\"" + error + "\", \"details\":" + message + "}";
		}catch (Exception e){
			return "{\"result\":\"" + cause + "\", \"message\":\"" + error + "\", \"details\":\"" + message + "\"}";

		}
		
		
		
	}

	public static Swagger getSwaggerFromJson(String json) {
		final SwaggerParser swaggerParser = new SwaggerParser();
		return swaggerParser.parse(json);
	}
}
