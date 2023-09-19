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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.add;

import java.util.Collections;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.transport.Endpoint;
import org.opensearch.client.transport.endpoints.SimpleEndpoint;
import org.opensearch.client.util.ObjectBuilder;
import org.opensearch.client.util.ObjectBuilderBase;

import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.CreateISMPolicyRequest;

import jakarta.json.stream.JsonGenerator;

public class AddISMPolicyToIndicesRequest implements JsonpSerializable {

	private final String indices;
	private final String policy_id;

	// ---------------------------------------------------------------------------------------------
	/**
	 * Methods for {@link CreateISMPolicyRequest}.
	 */

	private AddISMPolicyToIndicesRequest(Builder builder) {
		// super(builder);
		this.indices = builder.indices;
		this.policy_id = builder.policy_id;
	}

	public static AddISMPolicyToIndicesRequest of(Function<Builder, ObjectBuilder<AddISMPolicyToIndicesRequest>> fn) {
		return fn.apply(new Builder()).build();
	}

	/**
	 * API name: {@code indices}
	 */
	@Nullable
	public final String indices() {
		return this.indices;
	}

	/**
	 * API name: {@code policy_id}
	 */
	@Nullable
	public final String policy_id() {
		return this.policy_id;
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Serialize this object to JSON.
	 */
	public void serialize(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeStartObject();
		serializeInternal(generator, mapper);
		generator.writeEnd();
	}

	protected void serializeInternal(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeKey("policy_id");
		generator.write(this.policy_id);
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Builder for {@link AddISMPolicyToIndicesRequest}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<AddISMPolicyToIndicesRequest> {

		private String indices;
		private String policy_id;

		/**
		 * API name: {@code indices}
		 */
		public final Builder indices(String value) {
			this.indices = value;
			return this;
		}

		/**
		 * API name: {@code policy_id}
		 */
		public final Builder policy_id(String value) {
			this.policy_id = value;
			return this;
		}

		/**
		 * Builds a {@link AddISMPolicyToIndicesRequest}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public AddISMPolicyToIndicesRequest build() {
			_checkSingleUse();

			return new AddISMPolicyToIndicesRequest(this);
		}

	}
	// ---------------------------------------------------------------------------------------------

	/**
	 * Endpoint "{@code _plugin/_ism/add/}".
	 */
	public static final Endpoint<AddISMPolicyToIndicesRequest, AddISMPolicyToIndicesResponse, ErrorResponse> _ENDPOINT = new SimpleEndpoint<>(

			// Request method
			request -> {
				return "POST";
			},

			// Request path
			request -> {

				if (request.indices != null && !request.indices().isEmpty()) {
					StringBuilder buf = new StringBuilder();
					buf.append("/_plugins");
					buf.append("/_ism");
					buf.append("/add");
					buf.append("/");
					buf.append(request.indices());
					return buf.toString();
				}

				throw SimpleEndpoint.noPathTemplateFound("path");

			},

			// Request parameters
			request -> {
				return Collections.emptyMap();

			}, SimpleEndpoint.emptyMap(), true, AddISMPolicyToIndicesResponse._DESERIALIZER);
}
