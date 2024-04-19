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
package com.minsait.onesait.platform.persistence.mongodb.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MongoDbCredentials {

	@Value("${onesaitplatform.database.mongodb.authenticationDatabase:admin}")
	@Getter
	@Setter
	private String authenticationDatabase;

	@Value("${onesaitplatform.database.mongodb.useAuth:false}")
	@Getter
	@Setter
	private boolean enableMongoDbAuthentication;

	@Value("${onesaitplatform.database.mongodb.username:username}")
	@Getter
	@Setter
	private String username;

	@Value("${onesaitplatform.database.mongodb.password:password}")
	@Getter
	@Setter
	private String password;

	@PostConstruct
	public void init() {
		if (authenticationDatabase.isEmpty() || username.isEmpty() || password.isEmpty()) {
			log.warn(
					"The authentication database, the username or the password has not been specified. MongoDB authentication will be DISABLED.");
			enableMongoDbAuthentication = false;
		}
	}

}
