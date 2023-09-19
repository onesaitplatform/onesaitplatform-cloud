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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create;

import java.util.function.Function;

import org.opensearch.client.json.JsonpDeserializable;
import org.opensearch.client.json.JsonpDeserializer;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.ObjectBuilderDeserializer;
import org.opensearch.client.json.ObjectDeserializer;
import org.opensearch.client.opensearch.indices.PutIndexTemplateResponse;
import org.opensearch.client.util.ObjectBuilder;

import jakarta.json.stream.JsonGenerator;

@JsonpDeserializable
public class CreateISMPolicyResponse {
	private final String _id;

	private CreateISMPolicyResponse(Builder builder) {
		this._id = builder._id;
	}

	public static CreateISMPolicyResponse of(Function<Builder, ObjectBuilder<CreateISMPolicyResponse>> fn) {
		return fn.apply(new Builder()).build();
	}

	public final String _id() {
		return this._id;
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

		generator.writeKey("_id");
		generator.write(this._id);

	}
	// ---------------------------------------------------------------------------------------------

	/**
	 * Builder for {@link CreateISMPolicyResponse}.
	 */

	public static class Builder implements ObjectBuilder<CreateISMPolicyResponse> {

		private String _id;

		protected Builder _id(String value) {
			this._id = value;
			return this;
		}

		/**
		 * Builds a {@link CreateISMPolicyResponse}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public CreateISMPolicyResponse build() {
			return new CreateISMPolicyResponse(this);
		}
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Json deserializer for {@link CreateISMPolicyResponse}
	 */
	public static final JsonpDeserializer<CreateISMPolicyResponse> _DESERIALIZER = ObjectBuilderDeserializer
			.lazy(Builder::new, CreateISMPolicyResponse::setupCreateISMPolicyResponseDeserializer);

	protected static void setupCreateISMPolicyResponseDeserializer(
			ObjectDeserializer<CreateISMPolicyResponse.Builder> op) {
		op.add(Builder::_id, JsonpDeserializer.stringDeserializer(), "_id");

	}

}
