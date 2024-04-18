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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
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
@Table(name = "KSQL_FLOW", uniqueConstraints = { @UniqueConstraint(columnNames = { "IDENTIFICATION", "USER_ID" }) })
public class KsqlFlow extends OPResource {

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;

	@NotNull
	@Lob
	@Getter
	@Setter
	@Column(name = "JSON_FLOW", nullable = false)
	@Type(type = "org.hibernate.type.TextType")
	private String jsonFlow;

	@OneToMany(mappedBy = "ksqlFlow")
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private Set<KsqlRelation> resourcesRelations = new HashSet<>();

	public void addResourceRelation(KsqlRelation ksqlRelation) {
		resourcesRelations.add(ksqlRelation);
		ksqlRelation.setKsqlFlow(this);
	}

	public void removeResourceRealtion(KsqlRelation ksqlRelation) {
		resourcesRelations.remove(ksqlRelation);
		ksqlRelation.setKsqlFlow(null);
	}

}
