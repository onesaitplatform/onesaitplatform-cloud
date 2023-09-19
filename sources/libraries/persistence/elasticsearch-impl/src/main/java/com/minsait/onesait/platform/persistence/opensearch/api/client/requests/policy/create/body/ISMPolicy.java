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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.opensearch.client.json.JsonpDeserializable;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.util.ApiTypeHelper;
import org.opensearch.client.util.ObjectBuilder;
import org.opensearch.client.util.ObjectBuilderBase;

import jakarta.json.stream.JsonGenerator;

@JsonpDeserializable
public class ISMPolicy implements JsonpSerializable {

	private final String description;
	private final String default_state;
	private final List<PolicyState> states;
	private final ISMTemplate ism_template;

	// ---------------------------------------------------------------------------------------------
	

	private ISMPolicy(Builder builder) {

		this.description = builder.description;
		this.default_state = builder.default_state;
		this.states = ApiTypeHelper.unmodifiable(builder.states);
		this.ism_template = builder.ism_template;

	}

	public static ISMPolicy of(Function<Builder, ObjectBuilder<ISMPolicy>> fn) {
		return fn.apply(new Builder()).build();
	}

	/**
	 * API name: {@code description}
	 */
	@Nullable
	public final String description() {
		return this.description;
	}
	/**
	 * API name: {@code default_state}
	 */
	@Nullable
	public final String default_state() {
		return this.default_state;
	}
	/**
	 * API name: {@code states}
	 */
	@Nullable
	public final List<PolicyState> states() {
		return this.states;
	}/**
	 * API name: {@code states}
	 */
	@Nullable
	public final ISMTemplate ism_template() {
		return this.ism_template;
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Builder for {@link ISMPolicy}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<ISMPolicy> {

		private String description;
		private String default_state;
		private List<PolicyState> states = new ArrayList<>();
		@Nullable
		private ISMTemplate ism_template;

		/**
		 * API name: {@code description}
		 */
		public final Builder description(String value) {
			this.description = value;
			return this;
		}

		/**
		 * API name: {@code default_state}
		 */
		public final Builder default_state(String value) {
			this.default_state = value;
			return this;
		}

		/**
		 * API name: {@code states}
		 */
		public final Builder states(List<PolicyState> value) {
			this.states = _listAddAll(this.states, value);
			return this;
		}
		
		/**
		 * A comma-separated list of policies states to create
		 * <p>
		 * API name: {@code states}
		 * <p>
		 * Adds one or more values to <code>states</code>.
		 */
		public final Builder states(PolicyState value, PolicyState... values) {
			this.states = _listAdd(this.states, value, values);
			return this;
		}

		/**
		 * API name: {@code timestamp_field}
		 */
		public final Builder ism_template(@Nullable ISMTemplate value) {
			this.ism_template = value;
			return this;
		}

		/**
		 * Builds a {@link ISMPolicy}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public ISMPolicy build() {
			_checkSingleUse();

			return new ISMPolicy(this);
		}
	}

		// ---------------------------------------------------------------------------------------------

		/**
		 * Json deserializer for {@link ISMPolicy}
		 */
	@Override
	public void serialize(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeStartObject();
		serializeInternal(generator, mapper);
		generator.writeEnd();
	}
	
	protected void serializeInternal(JsonGenerator generator, JsonpMapper mapper) {

		if (this.description != null) {
			generator.writeKey("description");
			generator.write(this.description);
		}

		if (this.default_state != null) {
			generator.writeKey("default_state");
			generator.write(this.default_state);
		}
		
		if (ApiTypeHelper.isDefined(this.states)) {
			generator.writeKey("states");
			generator.writeStartArray();
			for (PolicyState item0 : this.states) {
				item0.serialize(generator, mapper);

			}
			generator.writeEnd();

		}
		
		if (this.ism_template != null) {
			generator.writeKey("ism_template");
			this.ism_template.serialize(generator, mapper);
		}

	}


}
