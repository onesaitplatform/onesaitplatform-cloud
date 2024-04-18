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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configurable
@Entity
@Table(name = "API", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION", "NUM_VERSION" }))
@EntityListeners(AuditEntityListener.class)
@ToString
public class Api extends OPResource implements Versionable<Api> {

	private static final long serialVersionUID = 1L;

	public enum ApiStates {
		CREATED, PUBLISHED, DELETED, DEPRECATED, DEVELOPMENT;
	}

	public enum ApiCategories {
		ALL, ADVERTISING, BUSINESS, COMMUNICATION, EDUCATION, ENTERTAINMENT, MEDIA, MEDICAL, OTHER, SOCIAL, SPORTS,
		TOOLS, TRAVEL;
	}

	public enum ApiType {
		IOT, EXTERNAL, INTERNAL_ONTOLOGY, EXTERNAL_FROM_JSON, NODE_RED
	}

	public enum ClientJS {
		REACT_JS
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private Ontology ontology;

	@Basic(fetch = FetchType.EAGER)
	@Column(name = "IMAGE", length = 100000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] image;

	@Column(name = "SSL_CERTIFICATE")
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean ssl_certificate;

	@OneToMany(mappedBy = "api", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@Getter
	@Setter
	private Set<UserApi> userApiAccesses = new HashSet<>();

	@Column(name = "NUM_VERSION")
	@Getter
	@Setter
	private Integer numversion;

	@Column(name = "DESCRIPTION", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "CATEGORY", length = 255)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private ApiCategories category;

	@Column(name = "ENDPOINT_EXT", length = 512)
	@Getter
	@Setter
	private String endpointExt;

	@Column(name = "STATE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private ApiStates state;

	@Column(name = "META_INF", length = 512)
	@Getter
	@Setter
	private String metaInf;

	@Column(name = "IMAGE_TYPE", length = 20)
	@Getter
	@Setter
	private String imageType;

	@Column(name = "IS_PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "API_CACHE_TIMEOUT")
	@Getter
	@Setter
	private Integer apicachetimeout;

	@Column(name = "API_LIMIT")
	@Getter
	@Setter
	private Integer apilimit;

	@Column(name = "API_TYPE", length = 50)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private ApiType apiType;

	@Column(name = "ASSESSMENT", precision = 10)
	@Getter
	@Setter
	private Double assessment;

	@Column(name = "SWAGGER_JSON")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String swaggerJson;

	@Column(name = "GRAVITEE_ID", length = 100)
	@Getter
	@Setter
	private String graviteeId;

	@OneToMany(mappedBy = "api", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<ApiOperation> apiOperations = new HashSet<>();

	@JsonSetter("apiOperations")
	public void setOperationsJson(Set<ApiOperation> apiOperations) {
		apiOperations.forEach(o -> {
			o.setApi(this);
			this.apiOperations.add(o);
		});
	}

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (StringUtils.hasText(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			ontology = o;
		}
	}

	@JsonSetter("userApiAccesses")
	public void setUserApiAccessesJson(Set<UserApi> userApiAccesses) {
		userApiAccesses.forEach(s -> {
			s.setApi(this);
			this.userApiAccesses.add(s);
		});
	}

	@JsonSetter("image")
	public void setImageJson(String imageBase64) {
		if (StringUtils.hasText(imageBase64)) {
			try {
				image = Base64.getDecoder().decode(imageBase64);
			} catch (final Exception e) {

			}
		}
	}

	@JsonGetter("image")
	public String getImageJson() {
		if (image != null && image.length > 0) {
			try {
				return Base64.getEncoder().encodeToString(image);
			} catch (final Exception e) {

			}
		}
		return null;

	}

	@Override
	public String serialize() throws IOException {
		final YAMLMapper mapper = new YAMLMapper();
		final ObjectNode node = new YAMLMapper().valueToTree(this);
		node.put("ontology", ontology == null ? null : ontology.getId());
		try {
			return mapper.writeValueAsString(node);
		} catch (final JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public String fileName() {
		return getIdentification() + "-v" + numversion + ".yaml";
	}

	@Override
	public Versionable<Api> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Api> api = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (api != null) {
			if (!userApiAccesses.isEmpty() && !CollectionUtils.isEmpty(excludedUsers)) {
				userApiAccesses.removeIf(ua -> excludedUsers.contains(ua.getUser().getUserId()));
				api = this;
			}
			if (ontology != null && !CollectionUtils.isEmpty(excludedIds)
					&& !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
					&& excludedIds.get(Ontology.class.getSimpleName()).contains(ontology.getId())) {
				addIdToExclusions(this.getClass().getSimpleName(), getId(), excludedIds);
				api = null;
			}
		}
		return api;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}

}
