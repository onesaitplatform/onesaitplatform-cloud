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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY_VIRTUAL")
public class OntologyVirtual extends AuditableEntityWithUUID {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private Ontology ontologyId;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "DATASOURCE_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private OntologyVirtualDatasource datasourceId;
	
	@Column(name = "DATASOURCE_TABLE_NAME", length = 128, nullable = true)
	@Getter
	@Setter
	private String datasourceTableName;

	@Column(name = "OBJECT_ID", length = 50, nullable = true)
	@Getter
	@Setter
	private String objectId;

	@Column(name = "OBJECT_GEOMETRY", length = 512, nullable = true)
	@Getter
	@Setter
	private String objectGeometry;

}
