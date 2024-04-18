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

public class PolicyStateTransitionCondition implements JsonpSerializable {

	private final String min_index_age;
	private final String min_rollover_age;
	private final Integer min_doc_count;
	private final String min_size;
	private final String cronExpression;// cron.cron.expression;
	private final String cronTimezone;// cron.cron.timezone;

	// ---------------------------------------------------------------------------------------------

	private PolicyStateTransitionCondition(Builder builder) {

		this.min_index_age = builder.min_index_age;
		this.min_rollover_age = builder.min_rollover_age;
		this.min_doc_count = builder.min_doc_count;
		this.min_size = builder.min_size;
		this.cronExpression = builder.cronExpression;
		this.cronTimezone = builder.cronTimezone;

	}

	public static PolicyStateTransitionCondition of(
			Function<Builder, ObjectBuilder<PolicyStateTransitionCondition>> fn) {
		return fn.apply(new Builder()).build();
	}
	/**
	 * API name: {@code min_index_age}
	 */
	@Nullable
	public final String min_index_age() {
		return this.min_index_age;
	}
	/**
	 * API name: {@code min_rollover_age}
	 */
	@Nullable
	public final String min_rollover_age() {
		return this.min_rollover_age;
	}
	/**
	 * API name: {@code min_doc_count}
	 */
	@Nullable
	public final Integer min_doc_count() {
		return this.min_doc_count;
	}
	/**
	 * API name: {@code min_size}
	 */
	@Nullable
	public final String min_size() {
		return this.min_size;
	}
	/**
	 * API name: {@code cronExpression}
	 */
	@Nullable
	public final String cronExpression() {
		return this.cronExpression;
	}
	/**
	 * API name: {@code cronTimezone}
	 */
	@Nullable
	public final String cronTimezone() {
		return this.cronTimezone;
	}

	// ---------------------------------------------------------------------------------------------
	/**
	 * Builder for {@link PolicyStateTransitionCondition}.
	 */

	public static class Builder extends ObjectBuilderBase implements ObjectBuilder<PolicyStateTransitionCondition> {

		private String min_index_age;
		private String min_rollover_age;
		private Integer min_doc_count;
		private String min_size;
		private String cronExpression;// cron.cron.expression;
		private String cronTimezone;// cron.cron.timezone;

		/**
		 * API name: {@code min_index_age}
		 */
		public final Builder min_index_age(String value) {
			this.min_index_age = value;
			return this;
		}

		/**
		 * API name: {@code min_rollover_age}
		 */
		public final Builder min_rollover_age(String value) {
			this.min_rollover_age = value;
			return this;
		}

		/**
		 * API name: {@code min_doc_count}
		 */
		public final Builder min_doc_count(Integer value) {
			this.min_doc_count = value;
			return this;
		}

		/**
		 * API name: {@code min_size}
		 */
		public final Builder min_size(String value) {
			this.min_size = value;
			return this;
		}

		/**
		 * API name: {@code cronExpression}
		 */
		public final Builder cronExpression(String value) {
			this.cronExpression = value;
			return this;
		}

		/**
		 * API name: {@code cronTimezone}
		 */
		public final Builder cronTimezone(String value) {
			this.cronTimezone = value;
			return this;
		}

		/**
		 * Builds a {@link PolicyStateTransitionCondition}.
		 *
		 * @throws NullPointerException if some of the required fields are null.
		 */
		public PolicyStateTransitionCondition build() {
			_checkSingleUse();

			return new PolicyStateTransitionCondition(this);
		}
	}
	// ---------------------------------------------------------------------------------------------

	/**
	 * Json deserializer for {@link PolicyStateTransitionCondition}
	 */

	@Override
	public void serialize(JsonGenerator generator, JsonpMapper mapper) {
		generator.writeStartObject();
		serializeInternal(generator, mapper);
		generator.writeEnd();
	}

	protected void serializeInternal(JsonGenerator generator, JsonpMapper mapper) {

		if (this.min_index_age != null) {
			generator.writeKey("min_index_age");
			generator.write(this.min_index_age);
		}

		if (this.min_rollover_age != null) {
			generator.writeKey("min_rollover_age");
			generator.write(this.min_rollover_age);
		}

		if (this.min_doc_count != null) {
			generator.writeKey("min_doc_count");
			generator.write(this.min_doc_count);
		}

		if (this.min_size != null) {
			generator.writeKey("min_size");
			generator.write(this.min_size);
		}

		if (this.cronExpression != null && this.cronTimezone != null) {
			generator.writeKey("cron");
			generator.writeStartObject();
			generator.writeKey("cron");
			generator.writeStartObject();
			generator.writeKey("cronExpression");
			generator.write(this.cronExpression);
			generator.writeKey("cronTimezone");
			generator.write(this.cronTimezone);
			generator.writeEnd();
			generator.writeEnd();
		}

	}
}
