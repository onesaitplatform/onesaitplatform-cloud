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
package com.minsait.onesait.platform.multitenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class Tenant2SchemaMapper {

	public static final String DEFAULT_SCHEMA_PREFIX = "onesaitplatform_config_";
	public static final String DEFAULT_RTDB_SCHEMA_PREFIX = "onesaitplatform_rtdb_";
	public static final String DEFAULT_SCHEMA = "onesaitplatform_config";

	public static final String DEFAULT_VERTICAL_NAME = "onesaitplatform";
	public static final String VERTICAL_HTTP_HEADER = "X-Vertical-Schema";
	public static final String VERTICAL_KEY_STRING = "Vertical";
	public static final String VERTICAL_SCHEMA_KEY_STRING = "Vertical_Schema";

	public static final String DEFAULT_TENANT_NAME = "development";
	public static final String TENANT_HTTP_HEADER = "X-Tenant";
	public static final String TENANT_KEY_STRING = "Tenant";

	public static String DEFAULT_RTDB_SCHEMA;

	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	public void setDefaultRTDBDatabase(String value) {
		DEFAULT_RTDB_SCHEMA = value;
	}

	public static String mapSchema(String vertical) {
		// TO-DO validation in some way for characters in tenant
		return DEFAULT_SCHEMA_PREFIX + vertical;
	}

	public static String mapRtdbSchema(String tenant, String vertical) {
		// TO-DO validation in some way for characters in tenant
		return DEFAULT_SCHEMA_PREFIX + vertical;
	}

	public static String getRtdbSchema() {
		final String tenant = MultitenancyContextHolder.getTenantName();
		final String verticalSchema = MultitenancyContextHolder.getVerticalSchema();
		if (defaultTenantName(extractVerticalNameFromSchema(verticalSchema)).equals(tenant)
				&& DEFAULT_SCHEMA.equals(verticalSchema)) {
			return DEFAULT_RTDB_SCHEMA;
		} else {
			return DEFAULT_RTDB_SCHEMA_PREFIX + tenant;
		}
	}

	public static String getCachePrefix() {

		return extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()) + "_";
	}

	public static String defaultTenantName(String vertical) {
		return DEFAULT_TENANT_NAME + "_" + vertical;
	}

	public static String extractVerticalNameFromSchema(String verticalSchema) {
		if (verticalSchema.equals(DEFAULT_SCHEMA)) {
			return DEFAULT_VERTICAL_NAME;
		}
		try {

			final String[] words = verticalSchema.split(DEFAULT_SCHEMA_PREFIX);
			return words[words.length - 1];

		} catch (final Exception e) {

			log.error("Could not extract vertical from vertical schema {}", verticalSchema, e);
		}
		return verticalSchema;
	}
	public static String verticalToSchema(String vertical) {
		if (vertical.equals(DEFAULT_VERTICAL_NAME)) {
			return DEFAULT_SCHEMA;
		} else {
			return mapSchema(vertical);
		}
	}

}
