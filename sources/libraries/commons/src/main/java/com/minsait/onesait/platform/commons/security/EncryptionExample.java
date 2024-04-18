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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptionExample {
	public static void main(String[] argv) {

		try {

			SecretKey myDesKey = new SecretKeySpec("Sofia2En".getBytes(), "AES");

			Cipher desCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

			// Initialize the cipher for encryption
			desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);

			// sensitive information
			byte[] text = "changeIt!".getBytes();
			if (log.isDebugEnabled()) {
				log.debug("Text [Byte Format] {}: ", text);
				log.debug("Text : {}", new String(text));
			}
			// Encrypt the text
			byte[] textEncrypted = desCipher.doFinal(text);
			
			if (log.isDebugEnabled()) {
				log.debug("Text Encrypted [Byte Format]: {}", textEncrypted);
				log.debug("Text Encrypted : {}", new String(textEncrypted));
			}
			// Initialize the same cipher for decryption
			desCipher.init(Cipher.DECRYPT_MODE, myDesKey);

			// Decrypt the text
			byte[] textDecrypted = desCipher.doFinal(textEncrypted);

			if (log.isDebugEnabled()) {
				log.debug("Text Decryted : {}", new String(textDecrypted));
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}
}
