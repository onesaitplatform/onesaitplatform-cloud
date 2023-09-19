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

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.util.ApiTypeHelper;
import org.opensearch.client.util.ObjectBuilder;
import org.opensearch.client.util.ObjectBuilderBase;

import jakarta.json.stream.JsonGenerator;

public class PolicyState implements JsonpSerializable {

	private final String name;
	// Actions are too complex for what we want to use right now...
	// TODO: Implement all actions on "ISMPolicyAction.java"
	// https://opensearch.org/docs/latest/im-plugin/ism/policies/#actions
	private final List<ISMPolicyAction> actions;
	private final List<PolicyStateTransition> transitions;

	// ---------------------------------------------------------------------------------------------

	private PolicyState(Builder builder) {
		this.name = builder.name;
		this.actions = ApiTypeHelper.unmodifiable(builder.actions);
		this.transitions = ApiTypeHelper.unmodifiable(builder.transitions);
	}

	public static PolicyState of(Function<Builder, ObjectBuilder<PolicyState>> fn) {
		return fn.apply(new Builder()).build();
	}
	/**
	 * API name: {@code name}
	 */
	@Nullable
	public final String name() {
		return this.name;
	}
	/**
	 * API name: {@code actions}
	 */
	@Nullable
	public final List<ISMPolicyAction> actions() {
		return this.actions;
	}
	/**
	 * API name: {@code transitions}
	 */
	@Nullable
	public final List<PolicyStateTransition> transitions() {
		return this.transitions;
	}

	// ---------------------------------------------------------------------------------------------
	/**
	 * Builder for {@link PolicyState}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<PolicyState> {

		private String name;
		private List<ISMPolicyAction> actions = new ArrayList<>();
		private List<PolicyStateTransition> transitions = new ArrayList<>();

		/**
		 * API name: {@code name}
		 */
		public final Builder name(String value) {
			this.name = value;
			return this;
		}

		/**
		 * API name: {@code actions}
		 */
		public final Builder actions(List<ISMPolicyAction> value) {
			this.actions = _listAddAll(this.actions, value);
			return this;
		}

		/**
		 * A comma-separated list of actions for the Policy State
		 * <p>
		 * API name: {@code actions}
		 * <p>
		 * Adds one or more values to <code>actions</code>.
		 */
		public final Builder actions(ISMPolicyAction value, ISMPolicyAction... values) {
			this.actions = _listAdd(this.actions, value, values);
			return this;
		}

		/**
		 * API name: {@code transitions}
		 */
		public final Builder transitions(List<PolicyStateTransition> value) {
			this.transitions = _listAddAll(this.transitions, value);
			return this;
		}

		/**
		 * A comma-separated list of transactions for the Policy State
		 * <p>
		 * API name: {@code transitions}
		 * <p>
		 * Adds one or more values to <code>transitions</code>.
		 */
		public final Builder transitions(PolicyStateTransition value, PolicyStateTransition... values) {
			this.transitions = _listAdd(this.transitions, value, values);
			return this;
		}

		/**
		 * Builds a {@link PolicyState}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public PolicyState build() {
			_checkSingleUse();

			return new PolicyState(this);
		}
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Json deserializer for {@link PolicyState}
	 */
	// ---------------------------------------------------------------------------------------------
	@Override
	public void serialize(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeStartObject();
		serializeInternal(generator, mapper);
		generator.writeEnd();
	}

	protected void serializeInternal(JsonGenerator generator, JsonpMapper mapper) {

		if (this.name != null) {
			generator.writeKey("name");
			generator.write(this.name);
		}

		if (ApiTypeHelper.isDefined(this.actions)) {
			generator.writeKey("actions");
			generator.writeStartArray();
			for (ISMPolicyAction item0 : this.actions) {
				item0.serialize(generator, mapper);
			}
			generator.writeEnd();
		}

		if (ApiTypeHelper.isDefined(this.transitions)) {
			generator.writeKey("transitions");
			generator.writeStartArray();
			for (PolicyStateTransition item0 : this.transitions) {
				item0.serialize(generator, mapper);
			}
			generator.writeEnd();
		}
	}
}
