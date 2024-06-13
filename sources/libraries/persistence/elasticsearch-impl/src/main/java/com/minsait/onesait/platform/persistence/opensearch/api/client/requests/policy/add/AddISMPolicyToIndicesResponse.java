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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.add;

import java.util.function.Function;

import org.opensearch.client.json.JsonpDeserializable;
import org.opensearch.client.json.JsonpDeserializer;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.ObjectBuilderDeserializer;
import org.opensearch.client.json.ObjectDeserializer;
import org.opensearch.client.util.ObjectBuilder;

import jakarta.json.stream.JsonGenerator;

@JsonpDeserializable
public class AddISMPolicyToIndicesResponse {

	private final Integer updated_indices;
	private final Boolean failures;
	// TODO: There are more fields to parse, non of the usefull for us right now

	private AddISMPolicyToIndicesResponse(Builder builder) {
		this.updated_indices = builder.updated_indices;
		this.failures = builder.failures;
	}

	public static AddISMPolicyToIndicesResponse of(Function<Builder, ObjectBuilder<AddISMPolicyToIndicesResponse>> fn) {
		return fn.apply(new Builder()).build();
	}

	public final Integer updated_indices() {
		return this.updated_indices;
	}

	public final Boolean failures() {
		return this.failures;
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

		generator.writeKey("updated_indices");
		generator.write(this.updated_indices);
		generator.writeKey("failures");
		generator.write(this.failures);

	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Builder for {@link AddISMPolicyToIndicesResponse}.
	 */

	public static class Builder implements ObjectBuilder<AddISMPolicyToIndicesResponse> {

		private Integer updated_indices;
		private Boolean failures;

		protected Builder updated_indices(Integer value) {
			this.updated_indices = value;
			return this;
		}

		protected Builder failures(Boolean value) {
			this.failures = value;
			return this;
		}

		/**
		 * Builds a {@link AddISMPolicyToIndicesResponse}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public AddISMPolicyToIndicesResponse build() {
			return new AddISMPolicyToIndicesResponse(this);
		}
	}

	// ---------------------------------------------------------------------------------------------
	/**
	 * Json deserializer for {@link AddISMPolicyToIndicesResponse}
	 */
	public static final JsonpDeserializer<AddISMPolicyToIndicesResponse> _DESERIALIZER = ObjectBuilderDeserializer
			.lazy(Builder::new, AddISMPolicyToIndicesResponse::setupAddISMPolicyToIndicesResponseDeserializer);

	protected static void setupAddISMPolicyToIndicesResponseDeserializer(
			ObjectDeserializer<AddISMPolicyToIndicesResponse.Builder> op) {
		op.add(Builder::updated_indices, JsonpDeserializer.integerDeserializer(), "updated_indices");
		op.add(Builder::failures, JsonpDeserializer.booleanDeserializer(), "failures");

	}

}