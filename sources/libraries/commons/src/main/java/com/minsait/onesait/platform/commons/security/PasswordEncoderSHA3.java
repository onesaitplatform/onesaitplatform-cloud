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
package com.minsait.onesait.platform.commons.security;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PasswordEncoderSHA3 extends PasswordEncoderBean {
	public static final String SHA_3 = "SHA3";

	@Override
	public synchronized String encodeSHA256(String password, String saltKey) throws GenericOPException {
		final byte[] salt = base64ToByte(saltKey);
		String encodedPassword = null;

		SHA3.DigestSHA3 digestSHA3256 = null;
		try {
			digestSHA3256 = new SHA3.Digest256();
			digestSHA3256.reset();
			digestSHA3256.update(salt);
		} catch (final Exception e) {
			log.error("Could not get algorithm", e);
			throw new GenericOPException(e.getMessage());
		}

		byte[] btPass = null;
		try {
			btPass = digestSHA3256.digest(password.getBytes(StandardCharsets.UTF_8.name()));
			for (int i = 0; i < iterations; i++) {
				digestSHA3256.reset();
				btPass = digestSHA3256.digest(btPass);
			}

			encodedPassword = byteToBase64(btPass);
			return encodedPassword;
		} catch (final Exception e) {
			log.error("encodeSHA3-256", e);
			throw new GenericOPException(e);
		}
	}

	@Override
	public boolean supportsEncryption() {
		final String var = System.getenv(ENV_VARIABLE);
		return !StringUtils.isEmpty(var) && SHA_3.equalsIgnoreCase(var);
	}

}
