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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "CACHE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class Cache extends OPResource {

	private static final long serialVersionUID = -2471699854333596017L;

	public enum Type {
		MAP
	}

	/**
	 * Maximum Size Policy, the same that are defined in hazelcast
	 */
	public enum MaxSizePolicy {
		PER_NODE, PER_PARTITION, USED_HEAP_PERCENTAGE, USED_HEAP_SIZE, FREE_HEAP_PERCENTAGE, FREE_HEAP_SIZE,
		USED_NATIVE_MEMORY_SIZE, USED_NATIVE_MEMORY_PERCENTAGE, FREE_NATIVE_MEMORY_SIZE, FREE_NATIVE_MEMORY_PERCENTAGE
	}

	/**
	 * Eviction policy, the same that are defined in hazelcast
	 */
	public enum EvictionPolicy {
		LRU, LFU, NONE, RANDOM
	}

	@Column(name = "TYPE", length = 50, nullable = false)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(name = "MAX_SIZE_POLICY", length = 50, nullable = false)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private MaxSizePolicy maxSizePolicy;

	@Column(name = "SIZE", length = 50, nullable = false)
	@Getter
	@Setter
	@NotNull
	private int size;

	@Column(name = "EVICTION_POLICY", length = 10, nullable = false)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private EvictionPolicy evictionPolicy;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Cache)) {
			return false;
		}
		Cache that = (Cache) o;
		return getIdentification() != null && getIdentification().equals(that.getIdentification());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

}
