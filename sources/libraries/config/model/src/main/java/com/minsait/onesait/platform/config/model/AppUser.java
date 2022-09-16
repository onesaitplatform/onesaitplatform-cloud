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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "APP_ROLE_USER")
@Configurable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser extends AppUserParent {

	/**
	 *
	 */
	private static final long serialVersionUID = -5195902973038606645L;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ROLE", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	@JsonIgnore
	private AppRole role;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@JsonSetter("user")
	public void setUserJson(String userId) {
		if (!StringUtils.isEmpty(userId)) {
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
