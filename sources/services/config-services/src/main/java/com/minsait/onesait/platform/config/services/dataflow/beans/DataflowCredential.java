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
package com.minsait.onesait.platform.config.services.dataflow.beans;

import lombok.Setter;
import org.springframework.security.crypto.codec.Base64;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;

public class DataflowCredential {

	public enum Type {
		ADMINISTRATOR, MANAGER, GUEST
	}

	public DataflowCredential() {
	}

	@NotNull
	@Setter
	@Min(1)
	private String user;

	@NotNull
	@Setter
	@Min(1)
	private String password;

	@NotNull
	@Setter
	private Type type;

	public String getEncryptedCredentials(){
		final String credentials = user+":"+password;
		return new String(Base64.encode(credentials.getBytes()), StandardCharsets.UTF_8);
	}

}
