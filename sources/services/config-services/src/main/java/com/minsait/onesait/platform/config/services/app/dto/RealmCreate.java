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
package com.minsait.onesait.platform.config.services.app.dto;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class RealmCreate {
	
	
	@Getter
	@Setter
	@NotNull
	protected String realmId;
	@Getter
	@Setter
	@NotNull
	protected String name;
	@Getter
	@Setter
	@NotNull
	protected String description;
	@Getter
	@Setter
	protected String secret;
	@Getter
	@Setter
	private Integer tokenValiditySeconds;
	@Getter
	@Setter
	protected Set<RealmRole> roles;
	@Getter
	@Setter
	@JsonIgnore
	protected User user;

	public RealmCreate(App app) {

		realmId = app.getAppId();
		
		if (null!=app.getSecret()) {
			secret = app.getSecret();
		}
		if (null!=app.getTokenValiditySeconds()) {
			tokenValiditySeconds = app.getTokenValiditySeconds();
		}
		name = app.getName();
		description = app.getDescription();

		roles = app.getAppRoles().stream().map(r -> new RealmRole(r.getName(), r.getDescription()))
				.collect(Collectors.toSet());
	}
}
