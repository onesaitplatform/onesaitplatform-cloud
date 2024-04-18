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
package com.minsait.onesait.platform.config.model.interfaces;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.commons.git.IEResourcesContextHolder;
import com.minsait.onesait.platform.config.versioning.VersioningIOServiceImpl;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

public interface Versionable<T> {

	public static final ObjectMapper mapper = new YAMLMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Slf4j
	final class LogHolder {
	}

	public enum SpecialVersionable {
		Pipeline, FlowDomain, Notebook, WebProject
	}

	public default String serialize() throws IOException {
		try {
			return mapper.writeValueAsString(this);
		} catch (final Exception e) {
			LogHolder.log.error("Error serializing versionable of class {} with id {}", getClass(), this.getId(), e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public default T deserialize(String content) throws IOException {
		try {
			LogHolder.log.trace("Deserializing entity init");
			final T versionable = (T) mapper.readValue(content, getClass());
			LogHolder.log.trace("Deserializing entity end");
			return versionable;
		} catch (final Exception e) {
			LogHolder.log.error("Error deserializing versionable of class {} wiith content {}", getClass(), content, e);
			throw e;
		}
	}

	public String fileName();

	public Object getId();

	public String getUserJson();

	public void setOwnerUserId(String userId);

	public default String pathToVersionable(boolean toYamlFile) {
		String path = VersioningIOServiceImpl.DIR
				+ Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()) + "/"
				+ getClass().getSimpleName();
		if (toYamlFile) {
			path = path + "/" + this.fileName();
		} else {
			// TO-DO look for thread local IE resources
			if (IEResourcesContextHolder.getDirectory() != null) {
				path = IEResourcesContextHolder.getDirectory() + "/" + getClass().getSimpleName();
			}
		}
		return path;

	}

	public default List<String> zipFileNames() {
		return null;
	}

	public default Versionable<T> runExclusionsByUser(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		if (!CollectionUtils.isEmpty(excludedUsers) && excludedUsers.contains(getUserJson())) {
			addIdToExclusions(getClass().getSimpleName(), (String) this.getId(), excludedIds);
			return null;
		}
		return this;
	}

	public default Versionable<T> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		if (!CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(getClass().getSimpleName()))
				&& excludedIds.get(getClass().getSimpleName()).contains(getId())
				|| !CollectionUtils.isEmpty(excludedUsers) && excludedUsers.contains(getUserJson())) {
			addIdToExclusions(getClass().getSimpleName(), (String) this.getId(), excludedIds);
			return null;
		}
		return this;
	}

	public default Versionable<T> runInclusions(Map<String, Set<String>> includedIds) {
		if (!CollectionUtils.isEmpty(includedIds)
				&& !CollectionUtils.isEmpty(includedIds.get(getClass().getSimpleName()))
				&& includedIds.get(getClass().getSimpleName()).contains(getId())) {
			addIdToInclusions(getClass().getSimpleName(), (String) this.getId(), includedIds);
			return this;
		}
		return null;
	}

	public default void addIdToInclusions(String className, String id, Map<String, Set<String>> includedIds) {
		LogHolder.log.debug("Excluding versionable with id {} of class {}", id, className);
		if (CollectionUtils.isEmpty(includedIds)) {
			includedIds = new HashMap<>();
		}
		if (CollectionUtils.isEmpty(includedIds.get(className))) {
			includedIds.put(className, new HashSet<>());
		}
		includedIds.get(className).add(id);
	}

	public default void addIdToExclusions(String className, String id, Map<String, Set<String>> excludedIds) {
		LogHolder.log.debug("Excluding versionable with id {} of class {}", id, className);
		if (CollectionUtils.isEmpty(excludedIds)) {
			excludedIds = new HashMap<>();
		}
		if (CollectionUtils.isEmpty(excludedIds.get(className))) {
			excludedIds.put(className, new HashSet<>());
		}
		excludedIds.get(className).add(id);
	}

}
