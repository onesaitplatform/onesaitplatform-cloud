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
package com.minsait.onesait.platform.config.services.ontology;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(force = true)
@Getter
@Setter
public class OntologyConfiguration {
	private static final String ALLOWS_CUSTOM_CONFIG = "allowsCustomConfig";
	private static final String ALLOWS_TEMPLATE_CONFIG = "allowsTemplateConfig";
	private static final String ALLOWS_CREATE_TABLE = "allowsCreateTable";
	private static final String ALLOWS_CUSTOM_ID_CONFIG = "allowsCustomIdConfig";
	private static final String ALLOWS_UPSERT_BY_ID = "allowsUpsertById";

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
	private String datasourceDatabase;
	private String datasourceSchema;

	private String enablePartitionIndexes;

	private String primarykey;

	private String partitions;

	private String npartitions;

	private boolean allowsCreateTable = false;

	private String sqlStatement = null;

	private String partitionKey;

	private String uniqueKeys;

	private String shards;

	private String replicas;

	private boolean allowsCustomElasticConfig = false;

	private boolean allowsTemplateConfig = false;

	private String patternField;

	private String patternFunction;

	private String substringStart;

	private String substringEnd;

	private boolean allowsCustomIdConfig = false;
	private boolean allowsUpsertById = false;
	private String customIdField;
	private String ttlRetentionPeriod;

	private String datasourceCatalog;
	private String bucketName;
	private String mqttTopicName;

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

		if (request.getParameter(ALLOWS_CREATE_TABLE) != null) {

			allowsCreateTable = request.getParameter(ALLOWS_CREATE_TABLE).equals("on")
					|| request.getParameter(ALLOWS_CREATE_TABLE).equals("true");
			schema = request.getParameter("jsonSchema"); // form of ontologyVistualCreate.js
			sqlStatement = request.getParameter("sqlStatement");
		}

		datasourceTableName = request.getParameter("datasourceTableName");
		datasourceDatabase = request.getParameter("datasourceDatabase");
		datasourceSchema = request.getParameter("datasourceSchema");

		// otras
		enablePartitionIndexes = request.getParameter("enablePartitionIndexes");
		primarykey = request.getParameter("primarykey");
		partitions = request.getParameter("partitions");
		npartitions = request.getParameter("npartitions");

		uniqueKeys = request.getParameter("uniquekeys");

		partitionKey = request.getParameter("partitionkey");

		// elasticsearch

		if (request.getParameter(ALLOWS_CUSTOM_CONFIG) != null) {
			allowsCustomElasticConfig = request.getParameter(ALLOWS_CUSTOM_CONFIG).equals("on")
					|| request.getParameter(ALLOWS_CUSTOM_CONFIG).equals("true");
		}
		shards = request.getParameter("shards");
		replicas = request.getParameter("replicas");
		if (request.getParameter(ALLOWS_TEMPLATE_CONFIG) != null) {
			allowsTemplateConfig = request.getParameter(ALLOWS_TEMPLATE_CONFIG).equals("on")
					|| request.getParameter(ALLOWS_TEMPLATE_CONFIG).equals("true");
		}

		patternField = request.getParameter("patternField");
		patternFunction = request.getParameter("patternFunction");
		substringStart = request.getParameter("substringStart");
		substringEnd = request.getParameter("substringEnd");

		if (request.getParameter(ALLOWS_CUSTOM_ID_CONFIG) != null) {
			allowsCustomIdConfig = request.getParameter(ALLOWS_CUSTOM_ID_CONFIG).equals("on")
					|| request.getParameter(ALLOWS_CUSTOM_ID_CONFIG).equals("true");
		}

		if (request.getParameter(ALLOWS_UPSERT_BY_ID) != null) {
			allowsUpsertById = request.getParameter(ALLOWS_UPSERT_BY_ID).equals("on")
					|| request.getParameter(ALLOWS_UPSERT_BY_ID).equals("true");
		}

		customIdField = request.getParameter("customIdField");
		ttlRetentionPeriod = request.getParameter("ttlRetentionPeriod");
		// presto

		datasourceCatalog = request.getParameter("datasourceCatalog");
		bucketName = request.getParameter("bucketName");
		mqttTopicName = request.getParameter("nameTopicMqtt");
	}

}
