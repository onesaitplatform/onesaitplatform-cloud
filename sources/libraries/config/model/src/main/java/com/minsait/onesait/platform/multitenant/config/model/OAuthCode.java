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
package com.minsait.onesait.platform.multitenant.config.model;

import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_code")
public class OAuthCode {

	@Id
	@Column(name = "code", unique = true, nullable = false)
	private String code;

	@Lob
	@Column(name = "authentication")
	private Blob authentication;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Blob getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Blob authentication) {
		this.authentication = authentication;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OAuthCode))
			return false;

		OAuthCode oAuthCode = (OAuthCode) o;

		boolean result = true;
		if (authentication != null ? !authentication.equals(oAuthCode.authentication)
				: oAuthCode.authentication != null)
			result = false;
		if (code != null ? !code.equals(oAuthCode.code) : oAuthCode.code != null)
			result = false;

		return result;
	}

	@Override
	public int hashCode() {
		int result = code != null ? code.hashCode() : 0;
		result = 31 * result + (authentication != null ? authentication.hashCode() : 0);
		return result;
	}
}
