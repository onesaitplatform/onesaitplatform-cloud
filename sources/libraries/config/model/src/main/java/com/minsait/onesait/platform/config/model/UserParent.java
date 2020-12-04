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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.converters.JPAHAS256ConverterCustom;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;

import lombok.Getter;
import lombok.Setter;

@Configurable
@MappedSuperclass
public class UserParent extends AuditableEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "USER_ID", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Size(min = 4, message = "user.userid.error")
	private String userId;

	@Column(name = "EMAIL", length = 255, nullable = false)
	@NotNull
	@javax.validation.constraints.Pattern(regexp = "^[-A-Za-z0-9~!$%^&*_=+}{\\'?]+(\\.[-a-z0-9~!$%^&*_=+}{\\'?]+)*@([a-z0-9_][-a-z0-9_]*(\\.[-a-z0-9_]+)*\\.([a-z]+)|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,5})?$", message = "user.create.empty.email")
	@Getter
	@Setter
	private String email;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Role role;

	@Column(name = "PASSWORD", length = 128, nullable = false)
	@NotNull
	@Setter
	@Convert(converter = JPAHAS256ConverterCustom.class)
	private String password;

	public String getPassword() {
		if (password != null && password.startsWith(JPAHAS256ConverterCustom.STORED_FLAG)) {
			return password.substring(JPAHAS256ConverterCustom.STORED_FLAG.length());
		} else {
			return password;
		}

	}

	public String getRawPassword() {

		return password;

	}

	@Column(name = "ACTIVE", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean active = true;

	@Column(name = "FULL_NAME", length = 255)
	@NotNull
	@Size(min = 4, message = "user.fullname.error")
	@Getter
	@Setter
	private String fullName;

	@Column(name = "DATE_DELETED")
	@Temporal(TemporalType.DATE)
	@Getter
	@Setter
	private Date dateDeleted;

	@Column(name = "AVATAR", nullable = true)
	@Lob
	@Getter
	@Setter
	private byte[] avatar;

	@Column(name = "EXTRA_FIELDS", nullable = true)
	@Lob
	@Getter
	@Setter
	private String extraFields;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof UserParent))
			return false;
		return getUserId() != null && getUserId().equals(((UserParent) o).getUserId());
	}

	@JsonIgnore
	public boolean isAdmin() {
		return getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getUserId());
	}

	@Override
	public String toString() {
		return getUserId();
	}

}
