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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.util.ObjectBuilder;
import org.opensearch.client.util.ObjectBuilderBase;

import jakarta.json.stream.JsonGenerator;

public class ISMPolicyAction implements JsonpSerializable {
	private final String name;
	private final Map<String, Object> properties;
	
	// ---------------------------------------------------------------------------------------------

		private ISMPolicyAction(Builder builder) {
			this.name = builder.name;
			this.properties = builder.properties;
		}

		public static ISMPolicyAction of(Function<Builder, ObjectBuilder<ISMPolicyAction>> fn) {
			return fn.apply(new Builder()).build();
		}
		/**
		 * API name: {@code state_name}
		 */
		@Nullable
		public final String name() {
			return this.name;
		}
		/**
		 * API name: {@code conditions}
		 */
		@Nullable
		public final Map<String, Object> properties() {
			return this.properties;
		}
		// ---------------------------------------------------------------------------------------------
		/**
		 * Builder for {@link ISMPolicyAction}.
		 */

		public static class Builder extends ObjectBuilderBase implements ObjectBuilder<ISMPolicyAction> {

			private String name;
			private Map<String, Object> properties = new HashMap<>();

			/**
			 * API name: {@code state_name}
			 */
			public final Builder name(String value) {
				this.name = value;
				return this;
			}

			/**
			 * API name: {@code conditions}
			 */
			public final Builder properties(Map<String, Object> value) {
				this.properties = value;
				return this;
			}

			/**
			 * Builds a {@link PolicyStateTransition}.
			 *
			 * @throws NullPointerException if some of the required fields are null.
			 */
			public ISMPolicyAction build() {
				_checkSingleUse();

				return new ISMPolicyAction(this);
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

			if (this.name != null) {
				generator.writeKey(this.name);
			}

			generator.writeStartObject();
			if (this.properties != null && !this.properties.isEmpty()) {
				for (Entry <String, Object> entry: this.properties().entrySet()) {
					generator.writeKey(entry.getKey());
					//TODO only two data types implemented!! not all actions will work.
					if(entry.getValue() instanceof String) {
						generator.write((String)entry.getValue());
					}else if (entry.getValue() instanceof Integer) {
						generator.write((Integer)entry.getValue());
					}
				}
				
			}
			generator.writeEnd();
		}
}
