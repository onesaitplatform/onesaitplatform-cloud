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
/*******************************************************************************
 * Â© Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
package com.minsait.onesait.platform.commons.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ContextData implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private String deviceTemplate;
	@Getter
	private String device;
	@Getter
	private String clientConnection;
	@Getter
	private String clientSession;
	@Getter
	private String user;
	@Getter
	private String timezoneId;
	@Getter
	private String timestamp;
	@Getter
	private long timestampMillis;
	@Getter
	private String source;
	
	
	
	public ContextData(JsonNode node) {

		this.setDeviceTemplate(node);

		this.setDevice(node);
		
		this.setClientConnection(node);

		this.setClientSession(node);
		
		this.setUser(node);
		
		this.setTimeZone(node);

		this.setTimestamp(node);

		this.setTimestampMillis(node);
		
		this.setSource(node);
	}

	public ContextData(ContextData other) {
		user = other.user;
		deviceTemplate = other.deviceTemplate;
		device = other.device;
		clientConnection = other.clientConnection;
		clientSession = other.clientSession;
		timezoneId = other.timezoneId;
		timestamp = other.timestamp;
		timestampMillis = other.timestampMillis;
		source = other.source;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof ContextData))
			return false;
		final ContextData that = (ContextData) other;
		return Objects.equals(user, that.user) && Objects.equals(device, that.device)
				&& Objects.equals(deviceTemplate, that.deviceTemplate)
				&& Objects.equals(clientConnection, that.clientConnection)
				&& Objects.equals(clientSession, that.clientSession) && Objects.equals(timezoneId, that.timezoneId)
				&& Objects.equals(timestamp, that.timestamp) && Objects.equals(timestampMillis, that.timestampMillis)
				&& Objects.equals(source, that.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, deviceTemplate, device, clientConnection, clientSession, timezoneId, timestamp,
				source);
	}

	private ContextData(Builder build) {
		user = build.user;
		timezoneId = build.timezoneId;
		timestamp = build.timestamp;
		clientConnection = build.clientConnection;
		deviceTemplate = build.deviceTemplate;
		device = build.device;
		clientSession = build.clientSession;
		timestampMillis = build.timestampMillis;
		source = build.source;
	}

	public static Builder builder(String user, String timezoneId, String timestamp, long timestampMillis,
			String source) {
		return new Builder(user, timezoneId, timestamp, timestampMillis, source);
	}

	public static class Builder {
		private String deviceTemplate;
		private String device;
		private String clientConnection;
		private String clientSession;
		private final String user;
		private final String timezoneId;
		private final String timestamp;
		private final long timestampMillis;
		private final String source;

		public Builder(String user, String timezoneId, String timestamp, long timestampMillis, String source) {
			this.user = user;
			this.timezoneId = timezoneId;
			this.timestamp = timestamp;
			this.timestampMillis = timestampMillis;
			this.source = source;
		}

		public ContextData build() {
			return new ContextData(this);
		}

		public Builder clientSession(String clientSession) {
			this.clientSession = clientSession;
			return this;
		}

		public Builder clientConnection(String clientConnection) {
			this.clientConnection = clientConnection;
			return this;
		}

		public Builder device(String device) {
			this.device = device;
			return this;
		}

		public Builder deviceTemplate(String deviceTemplate) {
			this.deviceTemplate = deviceTemplate;
			return this;
		}
	}
	
	private void setDeviceTemplate(JsonNode node) {
		final JsonNode deviceTemplateFound = node.findValue("deviceTemplate");
		if (deviceTemplateFound != null) {
			this.deviceTemplate = deviceTemplateFound.asText();
		} else {
			this.deviceTemplate = "";
		}
	}
	
	private void setDevice(JsonNode node) {
		final JsonNode deviceFound = node.findValue("device");
		if (deviceFound != null) {
			this.device = deviceFound.asText();
		} else {
			this.device = "";
		}
	}
	
	private void setClientConnection(JsonNode node) {
		final JsonNode clientConnectionFound = node.findValue("clientConnection");
		if (clientConnectionFound != null) {
			this.clientConnection = clientConnectionFound.asText();
		} else {
			this.clientConnection = "";
		}
	}
	
	private void setClientSession(JsonNode node) {
		final JsonNode clientSessionFound = node.findValue("clientSession");
		if (clientSessionFound != null) {
			this.clientSession = clientSessionFound.asText();
		} else {
			this.clientSession = "";
		}
	}
	
	private void setUser(JsonNode node) {
		final JsonNode userFound = node.findValue("user");
		if (userFound != null) {
			this.user = userFound.asText();
		} else {
			this.user = "";
		}
	}
	
	private void setTimeZone(JsonNode node) {
		final JsonNode timezoneIdFound = node.findValue("timezoneId");
		if (timezoneIdFound != null) {
			this.timezoneId = timezoneIdFound.asText();
		} else {
			this.timezoneId = CalendarAdapter.getServerTimezoneId();
		}
	}
	
	private void setTimestamp(JsonNode node) {
		final JsonNode timestampFound = node.findValue("timestamp");
		if (timestampFound != null) {
			this.timestamp = timestampFound.asText();
		} else {
			this.timestamp = Calendar.getInstance(TimeZone.getTimeZone(this.timezoneId)).getTime().toString();
		}
	}
	
	private void setTimestampMillis(JsonNode node) {
		final JsonNode timestampMillisFound = node.findValue("timestampMillis");
		if (timestampMillisFound != null) {
			this.timestampMillis = timestampMillisFound.asLong();
		} else {
			this.timestampMillis = System.currentTimeMillis();
		}
	}
	
	private void setSource(JsonNode node) {
		final JsonNode sourceFound = node.findValue("source");
		if (sourceFound != null) {
			this.source = sourceFound.toString();
		} else {
			this.source = "";
		}
	}
}
