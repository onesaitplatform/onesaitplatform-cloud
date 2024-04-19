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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;
import com.minsait.onesait.platform.config.versioning.HTML;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "DASHBOARD", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
@Configurable
@EntityListeners({ AuditEntityListener.class })
@ToString
@Slf4j
public class Dashboard extends OPResource implements Versionable<Dashboard> {

	private static final long serialVersionUID = 1L;

	public enum DashboardType {
		DASHBOARD, SYNOPTIC
	}

	@Column(name = "DESCRIPTION", length = 100, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "JSON18N")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String jsoni18n;

	@Column(name = "CUSTOMCSS")
	@Getter
	@Setter
	private String customcss;

	@Column(name = "CUSTOMJS")
	@Getter
	@Setter
	private String customjs;

	@Column(name = "PUBLIC")
	@Type(type = "org.hibernate.type.BooleanType")
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "MODEL")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String model;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "IMAGE", length = 100000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] image;

	@Column(name = "HEADERLIBS")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String headerlibs;

	@Column(name = "TYPE", length = 45)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private DashboardType type;

	@Column(name = "GENERATE_IMAGE")
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@Getter
	@Setter
	private boolean generateImage;

	@OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<DashboardUserAccess> accesses = new HashSet<>();

	@JsonSetter("accesses")
	public void setAccessesJson(Set<DashboardUserAccess> accesses) {
		accesses.forEach(a -> {
			a.setDashboard(this);
			this.accesses.add(a);
		});
	}

	@JsonGetter("model")
	public Object getModelJson() {
		try {
			return new ObjectMapper().readTree(model);
		} catch (final Exception e) {
			return model;
		}
	}

	@JsonSetter("model")
	public void setModelJson(Object node) {
		try {
			model = new ObjectMapper().writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			model = null;
		}
	}

	@JsonSetter("image")
	public void setImageJson(String imageBase64) {
		if (StringUtils.hasText(imageBase64)) {
			try {
				image = Base64.getDecoder().decode(imageBase64);
			} catch (final Exception e) {
				log.warn("Error reading dashboard image", e);
			}
		}
	}

	@JsonGetter("image")
	public String getImageJson() {
		if (image != null && image.length > 0) {
			try {
				return Base64.getEncoder().encodeToString(image);
			} catch (final Exception e) {
				log.warn("Error serializing dashboard image", e);
			}
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	@Override
	public String serialize() throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> map = mapper.convertValue(this, new TypeReference<Map<String, Object>>() {
		});
		map.put("headerlibs", new HTML(headerlibs));
		prettyHTML5Gadget((Map<String, Object>) map.get("model"));
		return VersioningUtils.versioningYaml(this.getClass()).dump(map);
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@SuppressWarnings("unchecked")
	private void prettyHTML5Gadget(Map<String, Object> model) {
		if (model.containsKey("pages")) {
			final ArrayList<Object> pages = (ArrayList<Object>) model.get("pages");
			final int npages = pages.size();
			for (int i = 0; i < npages; i++) {
				final Map<String, Object> page = (Map<String, Object>) pages.get(i);
				final ArrayList<Object> layers = (ArrayList<Object>) page.get("layers");
				final Map<String, Object> layer = (Map<String, Object>) layers.get(0);
				final ArrayList<Object> gridboard = (ArrayList<Object>) layer.get("gridboard");
				// reset gridboard
				for (int j = 0; j < gridboard.size(); j++) {
					final Map<String, Object> grid = (Map<String, Object>) gridboard.get(j);
					if (grid.get("content") != null && ((String) grid.get("content")).contains("<")) {
						grid.put("content", new HTML((String) grid.get("content")));
					}
					if (grid.get("contentcode") != null && ((String) grid.get("contentcode")).contains("{")) {
						grid.put("contentcode", new HTML((String) grid.get("contentcode")));
					}
				}
			}
		}
	}

	@Override
	public Versionable<Dashboard> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Dashboard> dashboard = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (dashboard != null && !accesses.isEmpty() && !CollectionUtils.isEmpty(excludedUsers)) {
			accesses.removeIf(ua -> excludedUsers.contains(ua.getUser().getUserId()));
			dashboard = this;
		}
		return dashboard;
	}

}
