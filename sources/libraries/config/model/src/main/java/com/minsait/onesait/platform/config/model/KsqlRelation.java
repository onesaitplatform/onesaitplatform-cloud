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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "KSQL_RELATION", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "KSQL_FLOW_ID", "KSQL_RESOURCE_ID" }) })
public class KsqlRelation extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "KSQL_FLOW_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private KsqlFlow ksqlFlow;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "KSQL_RESOURCE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private KsqlResource ksqlResource;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "KSQL_PREDECESOR_RELATION", joinColumns = @JoinColumn(name = "KSQL_PREDECESSOR_ID"), inverseJoinColumns = @JoinColumn(name = "KSQL_SUCESSOR_ID"))
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@Getter
	@Setter
	private Set<KsqlRelation> predecessors = new HashSet<>();

	@ManyToMany(mappedBy = "predecessors", fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<KsqlRelation> successors = new HashSet<>();

	public void addPredecessor(KsqlRelation ksqlRelation) {
		predecessors.add(ksqlRelation);
	}

	public void removePredecessor(KsqlRelation ksqlRelation) {
		predecessors.remove(ksqlRelation);
	}

	public void addSucessor(KsqlRelation ksqlRelation) {
		successors.add(ksqlRelation);
	}

	public void removeSucessor(KsqlRelation ksqlRelation) {
		successors.remove(ksqlRelation);
	}

}
