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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.delete;

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

public class DeleteISMPolicyRequest implements JsonpSerializable {
	
	private final String name;
	
	// ---------------------------------------------------------------------------------------------
		/**
		 * Methods for {@link CreateISMPolicyRequest}.
		 */

		private DeleteISMPolicyRequest(Builder builder) {
			//super(builder);
			this.name = builder.name;
		}

		public static DeleteISMPolicyRequest of(Function<Builder, ObjectBuilder<DeleteISMPolicyRequest>> fn) {
			return fn.apply(new Builder()).build();
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

		}
		// ---------------------------------------------------------------------------------------------

		/**
		 * Builder for {@link DeleteISMPolicyRequest}.
		 */

		public static class Builder extends ObjectBuilderBase implements ObjectBuilder<DeleteISMPolicyRequest> {

			private String name;

			/**
			 * API name: {@code name}
			 */
			public final Builder name(String value) {
				this.name = value;
				return this;
			}

			/**
			 * Builds a {@link DeleteISMPolicyRequest}.
			 *
			 * @throws NullPointerException if some of the required fields are null.
			 */
			public DeleteISMPolicyRequest build() {
				_checkSingleUse();

				return new DeleteISMPolicyRequest(this);
			}

		}
		// ---------------------------------------------------------------------------------------------
		
		/**
		 * Endpoint "{@code _plugin/_ism/policies/}".
		 */

		public static final Endpoint<DeleteISMPolicyRequest, DeleteISMPolicyResponse, ErrorResponse> _ENDPOINT = new SimpleEndpoint<>(

				// Request method
				request -> {
					return "DELETE";
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
				DeleteISMPolicyResponse._DESERIALIZER);
	
}
