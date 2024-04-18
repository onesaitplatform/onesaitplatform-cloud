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
package com.minsait.onesait.platform.persistence.opensearch.api.client.requests.policy.create.body;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.util.ObjectBuilder;
import org.opensearch.client.util.ObjectBuilderBase;

import jakarta.json.stream.JsonGenerator;

public class PolicyStateTransition implements JsonpSerializable {

	private final String state_name;
	private final PolicyStateTransitionCondition conditions;

	// ---------------------------------------------------------------------------------------------

	private PolicyStateTransition(Builder builder) {
		this.state_name = builder.state_name;
		this.conditions = builder.conditions;
	}

	public static PolicyStateTransition of(Function<Builder, ObjectBuilder<PolicyStateTransition>> fn) {
		return fn.apply(new Builder()).build();
	}
	/**
	 * API name: {@code state_name}
	 */
	@Nullable
	public final String state_name() {
		return this.state_name;
	}
	/**
	 * API name: {@code conditions}
	 */
	@Nullable
	public final PolicyStateTransitionCondition conditions() {
		return this.conditions;
	}

	// ---------------------------------------------------------------------------------------------
	/**
	 * Builder for {@link PolicyStateTransition}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<PolicyStateTransition> {

		private String state_name;
		private PolicyStateTransitionCondition conditions;

		/**
		 * API name: {@code state_name}
		 */
		public final Builder state_name(String value) {
			this.state_name = value;
			return this;
		}

		/**
		 * API name: {@code conditions}
		 */
		public final Builder conditions(PolicyStateTransitionCondition value) {
			this.conditions = value;
			return this;
		}

		/**
		 * Builds a {@link PolicyStateTransition}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public PolicyStateTransition build() {
			_checkSingleUse();

			return new PolicyStateTransition(this);
		}
	}
	// ---------------------------------------------------------------------------------------------

	/**
	 * Json deserializer for {@link PolicyStateTransition}
	 */

	@Override
	public void serialize(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeStartObject();
		serializeInternal(generator, mapper);
		generator.writeEnd();
	}

	protected void serializeInternal(JsonGenerator generator, JsonpMapper mapper) {

		if (this.state_name != null) {
			generator.writeKey("state_name");
			generator.write(this.state_name);
		}

		if (this.conditions != null) {
			generator.writeKey("conditions");
			this.conditions.serialize(generator, mapper);
		}
	}
}
