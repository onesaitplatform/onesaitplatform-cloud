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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "LINEAGE_RELATIONS")
public class LineageRelations extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Group {
		ONTOLOGY, DASHBOARD, API, DATAFLOW, NOTEBOOK, DIGITALCLIENT, GADGET, DATASOURCE, MICROSERVICE, FLOW
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SOURCE", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	@NotNull
	private OPResource source;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TARGET", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	@NotNull
	private OPResource target;

	@Column(name = "SOURCE_GROUP", length = 50, unique = false, nullable = false)
	@Getter
	@Setter
	@NotNull
	private Group sourceGroup;

	@Column(name = "TARGET_GROUP", length = 50, unique = false, nullable = false)
	@Getter
	@Setter
	@NotNull
	private Group targetGroup;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

}
