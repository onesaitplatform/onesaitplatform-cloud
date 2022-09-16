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
package com.minsait.onesait.platform.config.model.base;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.listener.AuditEntityListener;
import com.minsait.onesait.platform.config.model.listener.VersioningListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
// @JsonIgnoreProperties(value = {"createdAt", "updatedAt"},
// allowGetters = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EntityListeners({ AuditingEntityListener.class, VersioningListener.class , AuditEntityListener.class})
@ToString
public abstract class AuditableEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@CreatedDate
	@Getter
	@Setter
	private Date createdAt;

	@Column(name = "UPDATED_AT", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@LastModifiedDate
	@Getter
	@Setter
	private Date updatedAt;

	@JsonGetter("createdAt")
	public Long getCreatedAtJson() {
		return createdAt == null ? null : createdAt.getTime();
	}

	@JsonSetter("createdAt")
	public void setCreatedAJson(Long millis) {
		if (millis != null) {
			createdAt = new Date(millis);
		}
	}

	@JsonGetter("updatedAt")
	public Long getUpdatedAtJson() {
		return updatedAt == null ? null : updatedAt.getTime();
	}

	@JsonSetter("updateAt")
	public void setUpdatedAJson(Long millis) {
		if (millis != null) {
			updatedAt = new Date(millis);
		}
	}

}
