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
package com.minsait.onesait.platform.commons.security;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

public abstract class PasswordEncoderBean {

	public static final String ENV_VARIABLE = "ENCRYPTION_ALG";

	@Value("${onesaitplatform.encryption.iteration-count:5}")
	protected int iterations;

	@Value("${onesaitplatform.encryption.key:NA}")
	protected String key;

	public synchronized String encodeSHA256(String password) throws GenericOPException {
		return encodeSHA256(password, key);
	}

	public abstract String encodeSHA256(String password, String saltKey) throws GenericOPException;

	protected byte[] base64ToByte(String str) {
		return Base64.getDecoder().decode(str);
	}

	protected String byteToBase64(byte[] bt) {
		return Base64.getEncoder().encodeToString(bt);
	}

	public abstract boolean supportsEncryption();
}
