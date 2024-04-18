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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
@Table(name = "GADGET", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
@EntityListeners(AuditEntityListener.class)
@ToString
@Slf4j
public class Gadget extends OPResource implements Versionable<Gadget> {

	private static final long serialVersionUID = 1L;
	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "TYPE", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private GadgetTemplate type;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "INSTANCE")
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	private boolean instance;

	@Column(name = "CONFIG")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String config;
	
	@Basic(fetch = FetchType.EAGER)
	@Column(name = "IMAGE", length = 50000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] image;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Gadget)) {
			return false;
		}
		return getIdentification() != null && getIdentification().equals(((Gadget) o).getIdentification());
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

	@JsonSetter("type")
	public void setTypeJson(String id) {
		if (StringUtils.hasText(id)) {
			final GadgetTemplate t = new GadgetTemplate();
			t.setId(id);
			type = t;
		}
	}

	@Override
	public String serialize() {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("type", type == null ? null : type.getId());
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
	public Versionable<Gadget> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Gadget> gadget = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (gadget != null && type != null && !CollectionUtils.isEmpty(excludedIds)
				&& !CollectionUtils.isEmpty(excludedIds.get(GadgetTemplate.class.getSimpleName()))
				&& excludedIds.get(GadgetTemplate.class.getSimpleName()).contains(type.getId())) {
			addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
			gadget = null;
		}
		return gadget;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
