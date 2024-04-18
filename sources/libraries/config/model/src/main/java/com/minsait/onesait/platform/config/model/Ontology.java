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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "ONTOLOGY", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))
public class Ontology extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum AccessType {
		ALL, QUERY, INSERT;
	}

	public enum RtdbDatasource {

		MONGO, ELASTIC_SEARCH, KUDU, API_REST, DIGITAL_TWIN, VIRTUAL, COSMOS_DB
	}

	public enum RtdbToHdbStorage {
		MONGO_GRIDFS, DIRECTORY
	}

	public enum RtdbCleanLapse {
		ONE_DAY(24l * 60l * 60l * 1000l), TWO_DAYS(2 * 24l * 60l * 60l * 1000l), THREE_DAYS(
				3 * 24 * 60l * 60l * 1000l), FIVE_DAYS(5 * 24 * 60l * 60l * 1000l), ONE_WEEK(
						7 * 24 * 60l * 60l * 1000l), TWO_WEEKS(2 * 7 * 24 * 60l * 60l * 1000l), ONE_MONTH(
								4 * 7 * 24 * 60l * 60l * 1000l), THREE_MONTHS(
										3 * 4 * 7 * 24 * 60l * 60l * 1000l), SIX_MONTHS(
												6 * 4 * 7 * 24 * 60l * 60l * 1000l), ONE_YEAR(
														12 * 4 * 7 * 24 * 60l * 60l * 1000l), NEVER(0);

		private final long milliseconds;

		private RtdbCleanLapse(long milliseconds) {
			this.milliseconds = milliseconds;
		}

		public long getMilliseconds() {
			return milliseconds;
		}

	}

	@Column(name = "JSON_SCHEMA", nullable = false)
	@NotNull
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String jsonSchema;

	@Column(name = "XML_Diagram")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Getter
	@Setter
	private String xmlDiagram;

	@Column(name = "ONTOLOGY_CLASS", length = 50)
	@Getter
	@Setter
	private String ontologyClass;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "DATA_MODEL_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private DataModel dataModel;

	@Column(name = "ACTIVE", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean active;

	@Column(name = "RTDBCLEAN", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean rtdbClean;

	@Column(name = "RTDBCLEAN_LAPSE", nullable = true)
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RtdbCleanLapse rtdbCleanLapse;

	@Column(name = "RTDBHDB", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean rtdbToHdb;

	@Column(name = "RTDBHDB_STORAGE")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private RtdbToHdbStorage rtdbToHdbStorage = RtdbToHdbStorage.MONGO_GRIDFS;

	@Column(name = "PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@NotNull
	@Setter
	private String description;

	@Column(name = "METAINF", length = 1024)
	@Getter
	@Setter
	private String metainf;

	@Column(name = "DATA_MODEL_VERSION", length = 50)
	@Getter
	@Setter
	private String dataModelVersion;

	@OneToMany(mappedBy = "ontology", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<OntologyUserAccess> ontologyUserAccesses = new HashSet<>();

	@OneToOne(mappedBy = "ontology", fetch = FetchType.EAGER)
	@JsonManagedReference
	@Getter
	@Setter
	private OntologyKPI ontologyKPI;

	@OneToOne(mappedBy = "ontology", fetch = FetchType.EAGER)
	@JsonManagedReference
	@Getter
	@Setter
	private OntologyTimeSeries ontologyTimeSeries;

	@Column(name = "RTDB_DATASOURCE", length = 255)
	@Getter
	@Setter
	@NotNull
	@Enumerated(EnumType.STRING)
	private RtdbDatasource rtdbDatasource = Ontology.RtdbDatasource.MONGO;

	@Column(name = "ALLOW_CYPHER_FIELD", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean allowsCypherFields;

	@Column(name = "ALLOW_CREATE_TOPIC", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean allowsCreateTopic;

	@Column(name = "TOPIC", length = 256)
	@Getter
	@Setter
	private String topic;

	@Column(name = "ALLOW_CREATE_NOTIFICATION_TOPIC", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean allowsCreateNotificationTopic;

	@Column(name = "NOTIFICATION_TOPIC", length = 256)
	@Getter
	@Setter
	private String notificationTopic;

	@Column(name = "PARTITION_KEY", length = 256, nullable = true)
	@Getter
	@Setter
	private String partitionKey;

	public void addOntologyUserAccess(OntologyUserAccess ontologyUserAccess) {
		ontologyUserAccess.setOntology(this);
		ontologyUserAccesses.add(ontologyUserAccess);
	}

	public void removeOntologyUserAccess(OntologyUserAccess ontologyUserAccess) {
		ontologyUserAccesses.remove(ontologyUserAccess);
		ontologyUserAccess.setOntology(null);
	}

	public boolean existOntologyUserAccessesWithUserActive() {
		for (final Iterator<OntologyUserAccess> iterator = ontologyUserAccesses.iterator(); iterator.hasNext();) {
			final OntologyUserAccess ontologyUserAccess = iterator.next();
			if (ontologyUserAccess.getUser().isActive()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@PostLoad
	protected void trim() {
		if (getIdentification() != null) {
			setIdentification(getIdentification().replaceAll(" ", ""));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Ontology))
			return false;
		return getIdentification() != null && getIdentification().equals(((Ontology) o).getIdentification());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getIdentification());
	}

	@Override
	public String toString() {
		return getIdentification();
	}

}
