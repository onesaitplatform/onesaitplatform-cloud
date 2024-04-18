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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordEncoder {
	private static PasswordEncoder instance;
	private static final int ITERATION_COUNT = 5;

	private static final String SALT_KEY = "PveFT7isDjGYFTaYhc2Fzw==";

	private PasswordEncoder() {
	}

	public static synchronized PasswordEncoder getInstance() {
		if (instance == null) {
			return new PasswordEncoder();
		} else
			return instance;
	}

	public synchronized String encodeSHA256(String password) throws GenericOPException {
		return encodeSHA256(password, SALT_KEY);

	}

	public synchronized String encodeSHA256(String password, String saltKey) throws GenericOPException {
		String encodedPassword = null;
		byte[] salt = base64ToByte(saltKey);

		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			digest.update(salt);
		} catch (NoSuchAlgorithmException e) {
			log.error("encodeSHA256", e);
			throw new GenericOPException(e);
		}

		byte[] btPass = null;
		try {
			btPass = digest.digest(password.getBytes("UTF-8"));
			for (int i = 0; i < ITERATION_COUNT; i++) {
				digest.reset();
				btPass = digest.digest(btPass);
			}

			encodedPassword = byteToBase64(btPass);
			return encodedPassword;
		} catch (UnsupportedEncodingException e) {
			log.error("encodeSHA256", e);
			throw new GenericOPException(e);
		}
	}

	private byte[] base64ToByte(String str) {
		return Base64.getDecoder().decode(str);
	}

	private String byteToBase64(byte[] bt) {
		return Base64.getEncoder().encodeToString(bt);
	}

	public static void main(String[] args) throws Exception {
		String pass = "Secrete@343";
		String hash1 = null;
		String hash2 = null;

		// Assume from UI
		PasswordEncoder encoder1 = PasswordEncoder.getInstance();
		hash1 = encoder1.encodeSHA256(pass, SALT_KEY);

		// Assume the same present in db
		PasswordEncoder encoder2 = PasswordEncoder.getInstance();
		hash2 = encoder2.encodeSHA256(pass, SALT_KEY);

		if (hash1.equalsIgnoreCase(hash2))
			log.debug("Both hash Matches..");
		else
			log.debug("Hash matches fails..");
	}
}