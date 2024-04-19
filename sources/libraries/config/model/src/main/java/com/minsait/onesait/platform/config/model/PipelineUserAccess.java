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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PIPELINE_USER_ACCESS", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "PIPELINE_ID", "USER_ID" }) })
@Configurable
public class PipelineUserAccess extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "PIPELINE_USER_ACCESS_TYPE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private PipelineUserAccessType pipelineUserAccessType;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PIPELINE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Pipeline pipeline;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@JsonGetter("pipelineUserAccessType")
	public String getpipelineUserAccessTypeJson() {
		return pipelineUserAccessType.getId();
	}

	@JsonSetter("pipelineUserAccessType")
	public void setpipelineUserAccessTypeJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final PipelineUserAccessType d = new PipelineUserAccessType();
			d.setId(id);
			pipelineUserAccessType = d;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PipelineUserAccess)) {
			return false;
		}
		final PipelineUserAccess that = (PipelineUserAccess) o;
		return getPipelineUserAccessType() != null
				&& getPipelineUserAccessType().equals(that.getPipelineUserAccessType()) && getPipeline() != null
				&& getPipeline().equals(that.getPipeline()) && getUser() != null && getUser().equals(that.getUser());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getPipelineUserAccessType(), getPipeline(), getUser());
	}

	@Override
	public String toString() {
		final String space = "-";
		final StringBuilder sb = new StringBuilder();
		sb.append(getPipeline().getIdentification());
		sb.append(space);
		sb.append(getUser());
		sb.append(space);
		sb.append(getPipelineUserAccessType().getName());
		return sb.toString();
	}

}
