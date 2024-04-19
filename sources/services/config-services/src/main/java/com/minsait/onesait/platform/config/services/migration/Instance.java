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
package com.minsait.onesait.platform.config.services.migration;

import java.io.Serializable;

public class Instance {

	public static final Instance NO_INSTANCE = new Instance(Instance.class, "", null, null);

	private final Class<?> clazz;
	private final Serializable id;
	private final Serializable identification;
	private final Serializable version;

	public Instance(Class<?> clazz, Serializable id, Serializable identification, Serializable version) {
		this.clazz = clazz;
		this.id = id;
		this.identification = identification;
		this.version = version;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Serializable getId() {
		return id;
	}

	public Serializable getIdentification() {
		return identification;
	}

	public Serializable getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Instance))
			return false;
		Instance that = (Instance) o;
		return getClazz() != null && getClazz().equals(that.getClazz()) && getId() != null
				&& getId().equals(that.getId());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getClazz(), getId());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(clazz.getName());
		sb.append(":");
		sb.append(id);
		return sb.toString();
	}
}
