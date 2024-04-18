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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_REST")
public class OntologyRest extends AuditableEntityWithUUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum SecurityType {
		NONE, API_KEY, OAUTH, BASIC
	}

	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Ontology ontologyId;

	@Column(name = "BASE_URL", length = 1024, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String baseUrl;

	// @Column(name = "INFER_OPS", nullable = false)
	// @NotNull
	// @Getter
	// @Setter
	// private boolean inferOps;
	//
	// @Column(name = "WADL_URL", length = 1024, nullable = false)
	// @NotNull
	// @Getter
	// @Setter
	// private String wadlUrl;

	@Column(name = "SWAGGER_URL", length = 1024, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String swaggerUrl;

	@Column(name = "SECURITY_TYPE", length = 512, nullable = false)
	@NotNull
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private SecurityType securityType;

	@Column(name = "JSON_SCHEMA")
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String jsonSchema;

	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "SECURITY_ID", referencedColumnName = "ID", nullable = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private OntologyRestSecurity securityId;

	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "HEADER_ID", referencedColumnName = "ID", nullable = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private OntologyRestHeaders headerId;
	
	@OneToMany(mappedBy = "ontologyRestId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<OntologyRestOperation> operations = new HashSet<>();

}
