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
import lombok.Setter;

@NoArgsConstructor(force = true)
@Getter
@Setter
public class OntologyConfiguration {

	private String schema;

	private String baseUrl;

	private String authCheck;

	private String authMethod;

	private String header;

	private String token;

	private String oauthUser;

	private String oauthPass;

	private String basicUser;

	private String basicPass;

	private String infer;

	private String wadl;

	private String swagger;

	private String[] headers;

	private String[] operations;

	private String objectId;
	private String objectGeometry;
	private String datasource;
	private String datasourceTableName;

	private String enablePartitionIndexes;

	private String primarykey;

	private String partitions;

	private String npartitions;

	private boolean allowsCreateTable = false;

	private String sqlStatement = null;

	private String partitionKey;

	private String uniqueKeys;

	public OntologyConfiguration(HttpServletRequest request) {
		// rest ontology
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
		swagger = request.getParameter("swagger");
		headers = request.getParameterValues("headers");
		operations = request.getParameterValues("operations");
		// (deprecated)
		infer = request.getParameter("infer");
		wadl = request.getParameter("wadl");
		// virtual
		objectId = request.getParameter("objectId");
		objectGeometry = request.getParameter("objectGeometry");
		datasource = request.getParameter("datasource");

		if (request.getParameter("allowsCreateTable") != null) {

			allowsCreateTable = request.getParameter("allowsCreateTable").equals("on")
					|| request.getParameter("allowsCreateTable").equals("true");
			schema = request.getParameter("jsonSchema"); // form of ontologyVistualCreate.js
			sqlStatement = request.getParameter("sqlStatement");
		}

		datasourceTableName = request.getParameter("datasourceTableName");

		// otras
		enablePartitionIndexes = request.getParameter("enablePartitionIndexes");
		primarykey = request.getParameter("primarykey");
		partitions = request.getParameter("partitions");
		npartitions = request.getParameter("npartitions");

		uniqueKeys = request.getParameter("uniquekeys");

		partitionKey = request.getParameter("partitionkey");

	}

}
