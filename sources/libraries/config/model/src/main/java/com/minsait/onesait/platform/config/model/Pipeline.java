/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pipeline", uniqueConstraints = @UniqueConstraint(name = "UK_IDENTIFICATION", columnNames = {
		"IDENTIFICATION" }))
public class Pipeline extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum PipelineType {
		DATA_COLLECTOR, MICROSERVICE, DATA_COLLECTOR_EDGE
	}

	public enum PipelineStatus{
		EDITED, RUN_ERROR, STOPPED, FINISHED, RUNNING, START_ERROR, RUNNING_ERROR, DISCONNECTED, DISCONNECTING, CONNECTING, STOP_ERROR, INSTANCE_ERROR, CONNECT_ERROR, FINISHING, RETRY, STARTING, STARTING_ERROR, STOPPING, STOPPING_ERROR
	}

	@Column(name = "IDSTREAMSETS", length = 100, nullable = false)
	@Getter
	@Setter
	private String idstreamsets;

	@Column(name = "PUBLIC", nullable = false, columnDefinition = "BIT default 0")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "ID_INSTANCE", nullable = false, referencedColumnName = "ID")
	@Getter
	@Setter
	private DataflowInstance instance;

	@Transient
	@Getter
	@Setter
	private PipelineUserAccessType.Type accessType;

	@Transient
	@Getter
	@Setter
	private PipelineType type;

	@Transient
	@Getter
	@Setter
	private PipelineStatus status;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (super.getIdentification().hashCode());
		result = prime * result + (super.getUser().hashCode());
		result = prime * result + (idstreamsets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Pipeline other = (Pipeline) obj;
		if (super.getIdentification() == null) {
			if (other.getIdentification() != null)
				return false;
		} else if (!super.getIdentification().equals(other.getIdentification()))
			return false;
		if (super.getUser() == null) {
			if (other.getUser() != null)
				return false;
		} else if (!super.getUser().equals(other.getUser()))
			return false;
		if (idstreamsets == null) {
			if (other.idstreamsets != null)
				return false;
		} else if (!idstreamsets.equals(other.idstreamsets))
			return false;
		return true;
	}

}
