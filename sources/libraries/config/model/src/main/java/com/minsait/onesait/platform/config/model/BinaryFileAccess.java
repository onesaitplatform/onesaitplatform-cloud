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
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "BINARY_FILE_ACCESS")
@Configurable
public class BinaryFileAccess extends AuditableEntityWithUUID {

	private static final long serialVersionUID = -941716111942240660L;

	public enum Type {
		READ, WRITE
	}

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "FILE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private BinaryFile binaryFile;

	@ManyToOne
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Column(name = "ACCESS_TYPE", nullable = false)
	@Getter
	@Setter
	@Enumerated
	private Type accessType;

	@JsonSetter("user")
	public void setUserJson(String userId) {
		if (StringUtils.hasText(userId)) {
			final User u = new User();
			u.setUserId(userId);
			user = u;
		}
	}
	@JsonGetter("user")
	public String getUserJson() {
		return user == null ? null : user.getUserId();
	}

}
