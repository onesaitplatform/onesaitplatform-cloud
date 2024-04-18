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
package com.minsait.onesait.platform.commons.security;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PasswordEncoderSHA2 extends PasswordEncoderBean {
	public static final String SHA_2 = "SHA2";

	@Override
	public synchronized String encodeSHA256(String password, String saltKey) throws GenericOPException {
		String encodedPassword = null;
		final byte[] salt = base64ToByte(saltKey);

		final SHA256.Digest digest = new SHA256.Digest();
		digest.reset();
		digest.update(salt);

		byte[] btPass = null;
		try {
			btPass = digest.digest(password.getBytes(StandardCharsets.UTF_8.name()));
			for (int i = 0; i < iterations; i++) {
				digest.reset();
				btPass = digest.digest(btPass);
			}

			encodedPassword = byteToBase64(btPass);
			return encodedPassword;
		} catch (final UnsupportedEncodingException e) {
			log.error("encodeSHA256", e);
			throw new GenericOPException(e);
		}
	}

	@Override
	public boolean supportsEncryption() {
		final String var = System.getenv(ENV_VARIABLE);
		return !StringUtils.hasText(var) || SHA_2.equalsIgnoreCase(var);
	}

}
