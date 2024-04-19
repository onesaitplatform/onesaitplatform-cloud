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
package com.minsait.onesait.platform.multitenant;

import static com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME;

import org.springframework.util.StringUtils;

public class MultitenancyContextHolder {

	private static final ThreadLocal<String> VERTICAL_CONTEXT = new ThreadLocal<>();

	private static final ThreadLocal<String> TENANT_CONTEXT = new ThreadLocal<>();

	// USed where user can acces more than 1 vertical
	private static final ThreadLocal<Boolean> FORCED = new ThreadLocal<>();

	public static void setVerticalSchema(String schema) {
		VERTICAL_CONTEXT.set(schema);
	}

	public static String getVerticalSchema() {
		final String schema = VERTICAL_CONTEXT.get();
		if (!StringUtils.isEmpty(schema))
			return schema;
		else
			return Tenant2SchemaMapper.DEFAULT_SCHEMA;
	}

	public static void setTenantName(String tenant) {
		TENANT_CONTEXT.set(tenant);
	}

	public static String getTenantName() {
		final String tenantName = TENANT_CONTEXT.get();
		if (!StringUtils.isEmpty(tenantName))
			return tenantName;
		else
			return Tenant2SchemaMapper.defaultTenantName(DEFAULT_VERTICAL_NAME);
	}

	public static void setForced(boolean bool) {
		FORCED.set(bool);
	}

	public static boolean isForced() {
		final Boolean forced = FORCED.get();
		if (forced != null)
			return forced;
		return false;
	}

	public static void clear() {
		VERTICAL_CONTEXT.remove();
		TENANT_CONTEXT.remove();
		FORCED.remove();
	}
}
