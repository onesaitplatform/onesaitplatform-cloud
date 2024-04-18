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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create;

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

import com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body.ISMPolicy;

import jakarta.json.stream.JsonGenerator;

public class CreateISMPolicyRequest implements JsonpSerializable {

	private final ISMPolicy policy;
	private final String name;

	// ---------------------------------------------------------------------------------------------
	/**
	 * Methods for {@link CreateISMPolicyRequest}.
	 */

	private CreateISMPolicyRequest(Builder builder) {
		//super(builder);
		this.policy = builder.policy;
		this.name = builder.name;
	}

	public static CreateISMPolicyRequest of(Function<Builder, ObjectBuilder<CreateISMPolicyRequest>> fn) {
		return fn.apply(new Builder()).build();
	}

	/**
	 * API name: {@code policy}
	 */
	@Nullable
	public final ISMPolicy policy() {
		return this.policy;
	}

	/**
	 * API name: {@code name}
	 */
	@Nullable
	public final String name() {
		return this.name;
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

		if (this.policy != null) {
			generator.writeKey("policy");
			this.policy.serialize(generator, mapper);
		}

	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Builder for {@link CreateISMPolicyRequest}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<CreateISMPolicyRequest> {

		private ISMPolicy policy;
		private String name;

		/**
		 * API name: {@code policy}
		 */
		public final Builder policy(ISMPolicy value) {
			this.policy = value;
			return this;
		}

		/**
		 * API name: {@code name}
		 */
		public final Builder name(String value) {
			this.name = value;
			return this;
		}

		/**
		 * Builds a {@link CreateISMPolicyRequest}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public CreateISMPolicyRequest build() {
			_checkSingleUse();

			return new CreateISMPolicyRequest(this);
		}

	}
	// ---------------------------------------------------------------------------------------------

	/**
	 * Endpoint "{@code _plugin/_ism/policies/}".
	 */

	public static final Endpoint<CreateISMPolicyRequest, CreateISMPolicyResponse, ErrorResponse> _ENDPOINT = new SimpleEndpoint<>(

			// Request method
			request -> {
				return "PUT";
			},

			// Request path
			request -> {

				if (request.name != null && !request.name().isEmpty()) {
					StringBuilder buf = new StringBuilder();
					buf.append("/_plugins");
					buf.append("/_ism");
					buf.append("/policies");
					buf.append("/");
					buf.append(request.name());
					return buf.toString();
				}

				throw SimpleEndpoint.noPathTemplateFound("path");

			},

			// Request parameters
			request ->  {
				return Collections.emptyMap();

			}, SimpleEndpoint.emptyMap(), true,
			CreateISMPolicyResponse._DESERIALIZER);
}
