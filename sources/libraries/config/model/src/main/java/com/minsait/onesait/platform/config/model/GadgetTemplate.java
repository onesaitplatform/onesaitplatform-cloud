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

import java.io.IOException;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.HTML;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "GADGET_TEMPLATE", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class GadgetTemplate extends OPResource implements Versionable<GadgetTemplate> {

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;

	@Column(name = "TYPE", length = 100)
	@Getter
	@Setter
	private String type;

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "TEMPLATE")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String template;

	@Column(name = "TEMPLATEJS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String templateJS;

	@Column(name = "HEADERLIBS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String headerlibs;

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

	@JsonGetter("config")
	public Object getConfigJson() {
		try {
			return new ObjectMapper().readTree(config);
		} catch (final Exception e) {
			return config;
		}
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
	public String serialize() throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> map = mapper.convertValue(this, new TypeReference<Map<String, Object>>() {
		});
		map.put("templateJS", new HTML(templateJS));
		map.put("template", new HTML(template));
		map.put("headerlibs", new HTML(headerlibs));
		return VersioningUtils.versioningYaml(this.getClass()).dump(map);
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}
}
