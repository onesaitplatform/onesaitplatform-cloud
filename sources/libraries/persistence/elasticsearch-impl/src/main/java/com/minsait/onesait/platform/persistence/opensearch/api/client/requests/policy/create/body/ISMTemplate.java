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

public class ISMTemplate implements JsonpSerializable {

	private final List<String> index_patterns;
	private final Integer priority;

	// ---------------------------------------------------------------------------------------------

	private ISMTemplate(Builder builder) {

		this.index_patterns = ApiTypeHelper.unmodifiable(builder.index_patterns);
		this.priority = builder.priority;

	}

	public static ISMTemplate of(Function<Builder, ObjectBuilder<ISMTemplate>> fn) {
		return fn.apply(new Builder()).build();
	}
	/**
	 * API name: {@code index_patterns}
	 */
	@Nullable
	public final List<String> index_patterns() {
		return this.index_patterns;
	}
	/**
	 * API name: {@code priority}
	 */
	@Nullable
	public final Integer priority() {
		return this.priority;
	}

	// ---------------------------------------------------------------------------------------------
	/**
	 * Builder for {@link ISMTemplate}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<ISMTemplate> {

		private List<String> index_patterns = new ArrayList<>();
		private Integer priority;
		
		/**
		 * API name: {@code index_patterns}
		 */
		public final Builder index_patterns(List<String> value) {
			this.index_patterns = _listAddAll(this.index_patterns, value);
			return this;
		}
		
		/**
		 * A comma-separated list of index patterns to apply the Policy to
		 * <p>
		 * API name: {@code index_patterns}
		 * <p>
		 * Adds one or more values to <code>index_patterns</code>.
		 */
		public final Builder index_patterns(String value, String... values) {
			this.index_patterns = _listAdd(this.index_patterns, value, values);
			return this;
		}


		/**
		 * API name: {@code priority}
		 */
		public final Builder priority(Integer value) {
			this.priority = value;
			return this;
		}

		/**
		 * Builds a {@link ISMTemplate}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public ISMTemplate build() {
			_checkSingleUse();

			return new ISMTemplate(this);
		}
	}

	// ---------------------------------------------------------------------------------------------

	/**
	 * Json deserializer for {@link ISMTemplate}
	 */
	@Override
	public void serialize(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeStartObject();
		serializeInternal(generator, mapper);
		generator.writeEnd();
	}

	protected void serializeInternal(JsonGenerator generator, JsonpMapper mapper) {

		if (this.priority != null) {
			generator.writeKey("priority");
			generator.write(this.priority);
		}

		if (ApiTypeHelper.isDefined(this.index_patterns)) {
			generator.writeKey("index_patterns");
			generator.writeStartArray();
			for (String item0 : this.index_patterns) {
				generator.write(item0);
			}
			generator.writeEnd();
		}

	}
}
