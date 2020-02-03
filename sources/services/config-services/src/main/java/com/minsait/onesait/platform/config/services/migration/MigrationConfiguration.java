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
import com.minsait.onesait.platform.config.model.base.AuditableEntity;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import avro.shaded.com.google.common.collect.Sets;
import lombok.Getter;

public class MigrationConfiguration {

	private Map<Class<?>, Set<Serializable>> config = new HashMap<>();
	@Getter
	private Set<String> blacklist;
	@Getter
	private Set<String> whitelist;
	@Getter
	private Set<String> trimlist;
	@Getter
	private Set<String> blackProjectlist;
	private final static String[] users = { "administrator", "developer", "demo_developer", "user", "demo_user",
			"analytics", "partner", "sysadmin", "operations", "dataviewer" };
	private final static Set<String> masterUsers = Collections.unmodifiableSet(Sets.newHashSet(users));
	private List<Instance> dataList = new ArrayList<Instance>();

	public MigrationConfiguration() {
		blacklist = MigrationUtils.blacklist().getBlackList();
		whitelist = MigrationUtils.whitelist().getWhiteList();
		trimlist = MigrationUtils.trimlist().getTrimList();
		blackProjectlist = MigrationUtils.blackProjectlist().getBlackProjectlist();
	}

	public boolean add(Instance instance) {
		return add(instance.getClazz(), instance.getId(), instance.getIdentification(), instance.getVersion());
	}

	public boolean add(Class<?> clazz, Serializable id, Serializable identification, Serializable version) {
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
				dataList.add(new Instance(clazz, id, identification, version));
				return true;
			}
		}
		return false;
	}

	public boolean addUser(Class<?> clazz, Serializable id) {
		Set<Serializable> ids;
		if (!config.containsKey(clazz)) {
			ids = new HashSet<>();
			config.put(clazz, ids);
		} else {
			ids = config.get(clazz);
		}
		if (idInsertable(clazz, id)) {
			ids.add(id);
			dataList.add(new Instance(clazz, id, null, null));
			return true;
		}
		return false;
	}

	public boolean addProject(Class<?> clazz, Serializable id, Serializable identification) {
		if (!blackProjectlist.contains(clazz.getName())) {
			Set<Serializable> ids;
			if (!config.containsKey(clazz)) {
				ids = new HashSet<>();
				config.put(clazz, ids);
			} else {
				ids = config.get(clazz);
			}
			if (idInsertable(clazz, id)) {
				ids.add(id);
				dataList.add(new Instance(clazz, id, identification, null));
				return true;
			}
		}
		return false;
	}

	public static boolean idInsertable(Class<?> clazz, Serializable id) {
		if (AuditableEntityWithUUID.class.isAssignableFrom(clazz)
				|| AuditableEntityWithUUID.class.isAssignableFrom(clazz)
				|| AuditableEntity.class.isAssignableFrom(clazz)) {
			return (null != id && (!id.getClass().equals(String.class) || (!((String) id).startsWith("MASTER-"))));
		} else if (User.class.isAssignableFrom(clazz)) {
			return !masterUsers.contains(id);
		}
		return false;
	}

	public Set<Serializable> get(Class<?> clazz) {
		return Collections.unmodifiableSet(config.get(clazz));
	}

	public Set<Serializable> removeClazz(Class<?> clazz) {
		return config.remove(clazz);
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
