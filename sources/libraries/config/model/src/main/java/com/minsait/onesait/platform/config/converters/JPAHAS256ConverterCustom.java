/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.config.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.AttributeConverter;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.security.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

//@Converter
@Slf4j
public class JPAHAS256ConverterCustom implements AttributeConverter<String, String> {

	public static final String STORED_FLAG = "abcdefghijklmnopqrstuv";// Not match with valid password, cannot be
																		// introduced by
	// form
	private static final String SHA_PREFIX = "SHA256(";

	@Override
	public String convertToDatabaseColumn(String sensitive) {
		try {

			if (sensitive.startsWith(STORED_FLAG)) {// The password is encoded, The entity was stored previously and
													// password didn't change
				return sensitive.substring(STORED_FLAG.length());
			} else if (sensitive.startsWith(SHA_PREFIX)) {
				return encryptedSHA256Property(sensitive);
			} else {

				return PasswordEncoder.getInstance().encodeSHA256(sensitive);
			}
		} catch (final Exception e) {
			log.error("Error in convertToDatabaseColumn:" + e.getMessage(), e);
			throw new GenericRuntimeOPException(e);
		}

	}

	@Override
	public String convertToEntityAttribute(String sensitive) {
		return STORED_FLAG + sensitive;
	}

	private String encryptedSHA256Property(String encrypted) {
		final Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(encrypted);
		while (m.find()) {
			return m.group(1);
		}
		return encrypted;
	}
}