/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.base.OPResource;

import avro.shaded.com.google.common.collect.Sets;

public class MigrationConfiguration {

	private Map<Class<?>, Set<Serializable>> config = new HashMap<>();
	private Set<String> blacklist;
	private static final String[] users = { "administrator", "developer", "demo_developer", "user", "demo_user",
			"analytics", "partner", "sysadmin", "operations", "dataviewer" };
	private static final Set<String> masterUsers = Collections.unmodifiableSet(Sets.newHashSet(users));
	private List<Instance> dataList = new ArrayList<>();

	public MigrationConfiguration() {
		blacklist = MigrationUtils.blacklist().getBlackList();
	}

	public boolean add(Instance instance) {
		return add(instance.getClazz(), instance.getId());
	}

	public boolean add(Class<?> clazz, Serializable id) {
		if (!blacklist.contains(clazz.getName())) {
			Set<Serializable> ids;
			if (!config.containsKey(clazz)) {
				ids = new HashSet<>();
				config.put(clazz, ids);
			} else {
				ids = config.get(clazz);
			}
			if (idInsertable(clazz, id)) {
				ids.add(id);
				dataList.add(new Instance(clazz, id));
				return true;
			}
		}
		return false;
	}

	public static boolean idInsertable(Class<?> clazz, Serializable id) {
		if (AuditableEntityWithUUID.class.isAssignableFrom(clazz) || OPResource.class.isAssignableFrom(clazz)) {
			String idStr = (String) id;
			return !idStr.startsWith("MASTER-");
		} else if (User.class.isAssignableFrom(clazz)) {
			return !masterUsers.contains(id);
		}
		return false;
	}

	public Set<Serializable> get(Class<?> clazz) {
		return Collections.unmodifiableSet(config.get(clazz));
	}

	public Set<Class<?>> getTypes() {
		return Collections.unmodifiableSet(config.keySet());
	}

	public boolean contains(Class<?> clazz, Serializable id) {
		if (config.containsKey(clazz)) {
			return config.get(clazz).contains(id);
		}
		return false;
	}

	public int size() {
		return dataList.size();
	}

	public Instance getInstance(int i) {
		return dataList.get(i);
	}

}
