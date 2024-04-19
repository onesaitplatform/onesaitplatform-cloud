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

import java.io.IOException;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.HTML;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "GADGET_TEMPLATE_TYPE", uniqueConstraints = @UniqueConstraint(name = "UK_ID", columnNames = { "ID" }))

public class GadgetTemplateType extends AuditableEntityWithUUID implements Versionable<GadgetTemplateType> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@Getter
	@Setter
	private String id;

	@NotNull
	@Getter
	@Setter
	@Column(name = "IDENTIFICATION", length = 200, nullable = true)
	private String identification;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "TEMPLATE")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String template;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "TEMPLATEJS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String templateJS;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "HEADERLIBS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String headerlibs;

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
		return getIdentification() + "_" + getId() + ".yaml";
	}

	@Override
	@JsonIgnore
	public String getUserJson() {
		return null;
	}
}
