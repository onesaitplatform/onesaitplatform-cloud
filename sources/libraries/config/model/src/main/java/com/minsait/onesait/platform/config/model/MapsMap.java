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
package com.minsait.onesait.platform.config.model;

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Configurable
@Entity
@Table(name = "MAPS_MAP", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
@EntityListeners(AuditEntityListener.class)
@ToString
@Slf4j
public class MapsMap extends OPResource implements Versionable<MapsMap> {

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;

	@Column(name = "CONFIG")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String config;

	@Column(name = "TYPE", length = 512)
	@Getter
	@Setter
	private String type;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MapsMap)) {
			return false;
		}
		return getIdentification() != null && getIdentification().equals(((MapsMap) o).getIdentification());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

	@JsonSetter("config")
	public void setConfigJson(Object node) {
		try {
			config = new ObjectMapper().writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			config = null;
		}
	}

	@Override
	public String serialize() {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);

		try {
			node.set("config", mapper.readTree(config));
		} catch (final Exception e) {
			// NO-OP
		}
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			log.error("Could not serialize versionable of class {} with id {}", this.getClass(), getId());
			return null;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<MapsMap> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<MapsMap> mapsMap = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (mapsMap != null && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(MapsMap.class.getSimpleName()))) {
			addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
			mapsMap = null;
		}
		return mapsMap;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
