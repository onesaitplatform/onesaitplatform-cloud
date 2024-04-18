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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "FLOW")
public class Flow extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "FLOW_DOMAIN_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	@JsonIgnore
	private FlowDomain flowDomain;

	@NotNull
	@Getter
	@Setter
	@Column(name = "IDENTIFICATION", length = 200, nullable = false)
	private String identification;

	@NotNull
	@Getter
	@Setter
	@Column(name = "NODE_RED_FLOW_ID", length = 50, nullable = false)
	private String nodeRedFlowId;

	@NotNull
	@Getter
	@Setter
	@Column(name = "ACTIVE", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	private Boolean active;

	@OneToMany(mappedBy = "flow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<FlowNode> nodes = new HashSet<>();

	@JsonSetter("nodes")
	public void setNodesJson(Set<FlowNode> nodes) {
		nodes.forEach(n ->{
			n.setFlow(this);
			this.nodes.add(n);
		});
	}

}
