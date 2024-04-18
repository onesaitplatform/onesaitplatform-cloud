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
package com.minsait.onesait.platform.config.model;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "API", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION", "NUM_VERSION" }))
public class Api extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum ApiStates {
		CREATED, PUBLISHED, DELETED, DEPRECATED, DEVELOPMENT;
	}

	public enum ApiCategories {
		ALL, ADVERTISING, BUSINESS, COMMUNICATION, EDUCATION, ENTERTAINMENT, MEDIA, MEDICAL, OTHER, SOCIAL, SPORTS, TOOLS, TRAVEL;
	}

	public enum ApiType {
		IOT, EXTERNAL, INTERNAL_ONTOLOGY, EXTERNAL_FROM_JSON, NODE_RED
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

	@Column(name = "SSL_CERTIFICATE", columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean ssl_certificate;

	@OneToMany(mappedBy = "api", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@Getter
	@Setter
	private Set<UserApi> userApiAccesses;

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

	@Column(name = "IS_PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "CACHE_TIMEOUT")
	@Getter
	@Setter
	private Integer cachetimeout;

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
	@Getter
	@Setter
	private String swaggerJson;

	@Column(name = "GRAVITEE_ID", length = 100)
	@Getter
	@Setter
	private String graviteeId;

}
