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
package com.minsait.onesait.platform.persistence.mongodb;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.model.ContextData;

import lombok.Getter;
import lombok.Setter;

public class MongoDbContextData {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private ContextData contextData;

	private MongoDbDate timestamp;

	public MongoDbContextData() {
	}

	public MongoDbContextData(JsonNode node) {
		contextData = new ContextData(node);
		this.timestamp = new MongoDbDate(node.findValue("timestamp"));
	}

	public MongoDbContextData(ContextData cd) {
		contextData = cd;
		this.timestamp = new MongoDbDate();
	}

	public String toJson() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			throw new GenericRuntimeOPException("Unable to serialize contextData.");
		}
	}

	@Override
	public String toString() {
		return "MongoDbContextData [" + "timestamp=" + timestamp + ", user=" + contextData.getUser()
				+ ", deviceTemplate=" + contextData.getDeviceTemplate() + ", clientConnection="
				+ contextData.getClientConnection() + ", clientSession=" + contextData.getClientSession()
				+ ", timeZoneId=" + contextData.getTimezoneId();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof MongoDbContextData))
			return false;
		MongoDbContextData that = (MongoDbContextData) other;
		return this.timestamp.equals(that.timestamp) && this.contextData.equals(that.contextData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(timestamp, contextData);
	}
}
