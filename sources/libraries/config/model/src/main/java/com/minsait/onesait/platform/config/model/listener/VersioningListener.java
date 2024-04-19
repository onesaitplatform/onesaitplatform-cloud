/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.config.model.listener;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.git.VersioningCommitContextHolder;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.VersioningManager;
import com.minsait.onesait.platform.config.versioning.VersioningManager.EventType;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VersioningListener {

	private static VersioningManager versioningManager;

	public static void initialize() {
		try {
			versioningManager = BeanUtil.getBean(VersioningManager.class);
		} catch (final Exception e) {
			versioningManager = null;
		}
	}

	@PostPersist
	@SuppressWarnings("unchecked")
	public <T> void postPersistVersionable(Object o) {
		if (o instanceof Versionable && versioningManager != null && versioningManager.isActive()
				&& VersioningCommitContextHolder.isProcessPostCreate()) {
			final Versionable<T> versionable = (Versionable<T>) o;
			log.debug("Fired serialization for new Versionable Entity of type {}",
					versionable.getClass().getSimpleName());
			versioningManager.serialize(versionable, getCurrentUser(),
					VersioningCommitContextHolder.getCommitMessage(), EventType.CREATE);
		}
	}

	@PostUpdate
	@SuppressWarnings("unchecked")
	public <T> void postUpdateVersionable(Object o) {
		if (o instanceof Versionable && versioningManager != null && versioningManager.isActive()
				&& VersioningCommitContextHolder.isProcessPostUpdate()) {
			final Versionable<T> versionable = (Versionable<T>) o;
			log.debug("Fired serialization for updated Versionable Entity of type {} with id {}",
					versionable.getClass().getSimpleName(), versionable.getId());
			versioningManager.serialize(versionable, getCurrentUser(),
					VersioningCommitContextHolder.getCommitMessage(),EventType.UPDATE);
		}
	}

	@PostRemove
	@SuppressWarnings("unchecked")
	public <T> void postRemoveVersionable(Object o) {
		if (o instanceof Versionable && versioningManager != null && versioningManager.isActive()
				&& VersioningCommitContextHolder.isProcessPostDelete()) {
			final Versionable<T> versionable = (Versionable<T>) o;
			log.debug("Fired removal of serialized Versionable Entity of type {} with id {}",
					versionable.getClass().getSimpleName(), versionable.getId());
			versioningManager.removeSerialization(versionable, getCurrentUser(),
					VersioningCommitContextHolder.getCommitMessage(), EventType.DELETE);
		}
	}

	private String getCurrentUser() {
		return StringUtils.isEmpty(VersioningCommitContextHolder.getUserId())
				? SecurityContextHolder.getContext().getAuthentication().getName()
						: VersioningCommitContextHolder.getUserId();
	}

}
