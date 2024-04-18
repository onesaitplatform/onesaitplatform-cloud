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
package com.minsait.onesait.platform.config.services.ontology;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true)
public class OntologyConfiguration {
	
	@Getter final String schema;
	@Getter final String baseUrl;
	@Getter final String authCheck;
	@Getter final String authMethod;
	@Getter final String header;
	@Getter final String token;
	@Getter final String oauthUser;
	@Getter final String oauthPass;
	@Getter final String basicUser;
	@Getter final String basicPass;
	@Getter final String infer;
	@Getter final String wadl;
	@Getter final String swagger;
	@Getter final String[] headers;
	@Getter final String[] operations;
	@Getter final String objectId;
	@Getter final String datasource;
	@Getter final String enablePartitionIndexes;
	@Getter final String primarykey;
	@Getter final String partitions;
	@Getter final String npartitions;

	public OntologyConfiguration(HttpServletRequest request) {
		schema = request.getParameter("schema");
		baseUrl = request.getParameter("urlBase");
		authCheck = request.getParameter("authCheck");
		authMethod = request.getParameter("authMethod");
		header = request.getParameter("header");
		token = request.getParameter("token");
		oauthUser = request.getParameter("oauthUser");
		oauthPass = request.getParameter("oauthPass");
		basicUser = request.getParameter("basicUser");
		basicPass = request.getParameter("basicPass");
		infer = request.getParameter("infer");
		wadl = request.getParameter("wadl");
		swagger = request.getParameter("swagger");
		headers = request.getParameterValues("headers");
		operations = request.getParameterValues("operations");
		objectId = request.getParameter("objectId");
		datasource = request.getParameter("datasource");
		enablePartitionIndexes = request.getParameter("enablePartitionIndexes");
		primarykey = request.getParameter("primarykey");
		partitions = request.getParameter("partitions");
		npartitions = request.getParameter("npartitions");
	}

}
