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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "SUPPORT_REQUEST")
public class SupportRequest extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public enum Type {
		NOTIFICATION, ROLE_CHANGE;
	}

	public enum Status {
		SENT, READ, PROCESS;
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Column(name = "TYPE", length = 24)
	@NotNull
	@Getter
	@Setter
	private String type;

	public void setTypeEnum(SupportRequest.Type type) {
		this.type = type.toString();
	}

	@Column(name = "STATUS", length = 24)
	@NotNull
	@Getter
	@Setter
	private String status;

	public void setStatusEnum(SupportRequest.Status status) {
		this.status = status.toString();
	}

	@Column(name = "JSON", nullable = false)
	@NotNull
	@Lob
	@org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
	@JsonRawValue
	@Getter
	@Setter
	private String json;

}
