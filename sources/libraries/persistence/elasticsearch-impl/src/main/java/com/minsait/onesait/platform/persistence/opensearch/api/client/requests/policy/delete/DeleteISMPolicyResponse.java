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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.delete;

import java.util.function.Function;

import org.opensearch.client.json.JsonpDeserializable;
import org.opensearch.client.json.JsonpDeserializer;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.ObjectBuilderDeserializer;
import org.opensearch.client.json.ObjectDeserializer;
import org.opensearch.client.util.ObjectBuilder;

import jakarta.json.stream.JsonGenerator;

@JsonpDeserializable
public class DeleteISMPolicyResponse {
	
	private final String _index;
	private final String _id;
	private final Integer  _version;
    private final String result;
    private final Boolean forced_refresh;
    //TODO: Implement shards response, not usefull for us right now...
    // private final Shards shards;
    private final Integer _seq_no;
    private final Integer _primary_term;
    
    private DeleteISMPolicyResponse(Builder builder) {
		this._index = builder._index;
		this._id = builder._id;
		this._version = builder._version;
		this.result = builder.result;
		this.forced_refresh = builder.forced_refresh;
		this._seq_no = builder._seq_no;
		this._primary_term = builder._primary_term;
	}

	public static DeleteISMPolicyResponse of(Function<Builder, ObjectBuilder<DeleteISMPolicyResponse>> fn) {
		return fn.apply(new Builder()).build();
	}

	public final String _index() {
		return this._index;
	}
	public final String _id() {
		return this._id;
	}
	public final Integer _version() {
		return this._version;
	}
	public final String result() {
		return this.result;
	}
	public final Boolean forced_refresh() {
		return this.forced_refresh;
	}
	public final Integer _seq_no() {
		return this._seq_no;
	}
	public final Integer _primary_term() {
		return this._primary_term;
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

		generator.writeKey("_index");
		generator.write(this._index);
		generator.writeKey("_id");
		generator.write(this._id);
		generator.writeKey("_version");
		generator.write(this._version);
		generator.writeKey("result");
		generator.write(this.result);
		generator.writeKey("forced_refresh");
		generator.write(this.forced_refresh);
		generator.writeKey("_seq_no");
		generator.write(this._seq_no);
		generator.writeKey("_primary_term");
		generator.write(this._primary_term);

	}
	// ---------------------------------------------------------------------------------------------

	/**
	 * Builder for {@link DeleteISMPolicyResponse}.
	 */

	public static class Builder implements ObjectBuilder<DeleteISMPolicyResponse> {

		private  String _index;
		private  String _id;
		private  Integer  _version;
	    private  String result;
	    private  Boolean forced_refresh;
	    private  Integer _seq_no;
	    private  Integer _primary_term;

		protected Builder _index(String value) {
			this._index = value;
			return this;
		}
		protected Builder _id(String value) {
			this._id = value;
			return this;
		}
		protected Builder _version(Integer value) {
			this._version = value;
			return this;
		}
		protected Builder result(String value) {
			this.result = value;
			return this;
		}
		protected Builder forced_refresh(Boolean value) {
			this.forced_refresh = value;
			return this;
		}
		protected Builder _seq_no(Integer value) {
			this._seq_no = value;
			return this;
		}
		protected Builder _primary_term(Integer value) {
			this._primary_term = value;
			return this;
		}

		/**
		 * Builds a {@link DeleteISMPolicyResponse}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public DeleteISMPolicyResponse build() {
			return new DeleteISMPolicyResponse(this);
		}
	}
	// ---------------------------------------------------------------------------------------------

		/**
		 * Json deserializer for {@link DeleteISMPolicyResponse}
		 */
		public static final JsonpDeserializer<DeleteISMPolicyResponse> _DESERIALIZER = ObjectBuilderDeserializer
				.lazy(Builder::new, DeleteISMPolicyResponse::setupDeleteISMPolicyResponseDeserializer);

		protected static void setupDeleteISMPolicyResponseDeserializer(
				ObjectDeserializer<DeleteISMPolicyResponse.Builder> op) {
			op.add(Builder::_index, JsonpDeserializer.stringDeserializer(), "_index");
			op.add(Builder::_id, JsonpDeserializer.stringDeserializer(), "_id");
			op.add(Builder::_version, JsonpDeserializer.integerDeserializer(), "_version");
			op.add(Builder::result, JsonpDeserializer.stringDeserializer(), "result");
			op.add(Builder::forced_refresh, JsonpDeserializer.booleanDeserializer(), "forced_refresh");
			op.add(Builder::_seq_no, JsonpDeserializer.integerDeserializer(), "_seq_no");
			op.add(Builder::_primary_term, JsonpDeserializer.integerDeserializer(), "_primary_term");

		}
}
