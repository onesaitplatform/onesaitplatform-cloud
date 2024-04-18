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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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

@Entity
@Table(name = "USER")
@Configurable
public class User extends AuditableEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "USER_ID", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Size(min = 4, message = "user.userid.error")
	private String userId;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "USER_PROJECT", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "PROJECT_ID"))
	@Getter
	@Setter
	@JsonIgnore
	private Set<Project> projects = new HashSet<>();

	@Column(name = "EMAIL", length = 255, nullable = false)
	@NotNull
	@javax.validation.constraints.Pattern(regexp = "^[-A-Za-z0-9~!$%^&*_=+}{\\'?]+(\\.[-a-z0-9~!$%^&*_=+}{\\'?]+)*@([a-z0-9_][-a-z0-9_]*(\\.[-a-z0-9_]+)*\\.(aero|arpa|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,5})?$", message = "user.create.empty.email")
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
		if (this.password != null && this.password.startsWith(JPAHAS256ConverterCustom.STORED_FLAG)) {
			return this.password.substring(JPAHAS256ConverterCustom.STORED_FLAG.length());
		} else {
			return this.password;
		}

	}

	@Column(name = "ACTIVE", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean active;

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
		if (!(o instanceof User))
			return false;
		return getUserId() != null && getUserId().equals(((User) o).getUserId());
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
