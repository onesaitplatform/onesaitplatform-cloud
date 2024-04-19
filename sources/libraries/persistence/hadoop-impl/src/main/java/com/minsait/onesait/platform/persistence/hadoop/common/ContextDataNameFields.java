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
package com.minsait.onesait.platform.persistence.hadoop.common;

public class ContextDataNameFields {

	public static final String CONTEXT_DATA_FIELD_PREFIX = "contextData__";

	public static final String FIELD_DEVICE_TEMPLATE = "deviceTemplate";
	public static final String FIELD_DEVICE = "device";
	public static final String FIELD_DEVICE_TEMPLATE_CONNECTION = "clientConnection";
	public static final String FIELD_CLIENT_SESSION = "clientSession";
	public static final String FIELD_USER = "user";
	public static final String FIELD_TIMEZONE_ID = "timezoneId";
	public static final String FIELD_TIMESTAMP = "timestamp";
	public static final String FIELD_TIMESTAMP_MILLIS = "timestampMillis";

	public static final String CONTEXT_DATA_FIELD_DEVICE_TEMPLATE = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_DEVICE_TEMPLATE.toLowerCase();
	public static final String CONTEXT_DATA_FIELD_DEVICE = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_DEVICE.toLowerCase();
	public static final String CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION = CONTEXT_DATA_FIELD_PREFIX.toLowerCase()
			+ FIELD_DEVICE_TEMPLATE_CONNECTION.toLowerCase();

	public static final String CONTEXT_DATA_FIELD_CLIENT_SESSION = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_CLIENT_SESSION.toLowerCase();
	public static final String CONTEXT_DATA_FIELD_USER = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_USER.toLowerCase();
	public static final String CONTEXT_DATA_FIELD_TIMEZONE_ID = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_TIMEZONE_ID.toLowerCase();
	public static final String CONTEXT_DATA_FIELD_TIMESTAMP = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_TIMESTAMP.toLowerCase();
	public static final String CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS = CONTEXT_DATA_FIELD_PREFIX.toLowerCase() + FIELD_TIMESTAMP_MILLIS.toLowerCase();

	private ContextDataNameFields() {
		super();
	}

}
