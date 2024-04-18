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
package com.minsait.onesait.platform.encryptor.aop;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.minsait.onesait.platform.encryptor.config.JasyptConfig;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
public class EncryptableAspect {

	private static final String ENC_PREFIX = "ENC(";

	@Around(value = "get(* *) && @annotation(com.minsait.onesait.platform.encryptor.aop.Encryptable) && target(obj)")
	public Object fieldAccess(ProceedingJoinPoint joinPoint, Object obj) throws Throwable {
		try {
			final String attribute = joinPoint.getSignature().getName();
			final Field field = obj.getClass().getDeclaredField(attribute);
			field.setAccessible(true);
			// if (!field.getType().getSimpleName().equals("String")) {
			if (!(field.getType().getSimpleName() instanceof String)) {
				throw new IllegalArgumentException("@Ecrypted can only be used on String attributes");
			}

			final String value = (String) field.get(obj);

			if (value != null && value.startsWith(ENC_PREFIX)) {
				// final String capAttribute = attribute.substring(0, 1).toUpperCase() +
				// attribute.substring(1);
				// final Method setter =
				// obj.getClass().getDeclaredMethod("set".concat(capAttribute), String.class);
				// setter.invoke(obj,
				// JasyptConfig.getEncryptor().decrypt(encryptedProperty(value)));
				if (log.isDebugEnabled()) {
					log.debug("decrypted value for attribute {}", attribute);
				}
				return JasyptConfig.getEncryptor().decrypt(encryptedProperty(value));
			} else {
				log.debug("Attribute declared as @Encryptable but value is not encrypted");

			}

		} catch (final Exception e) {
			log.error("Could not manage @Encryptable field", e);
		}
		return joinPoint.proceed();
	}

	public String encryptedProperty(String encrypted) {
		final Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(encrypted);
		while (m.find()) {
			return m.group(1);
		}
		return encrypted;
	}

}
