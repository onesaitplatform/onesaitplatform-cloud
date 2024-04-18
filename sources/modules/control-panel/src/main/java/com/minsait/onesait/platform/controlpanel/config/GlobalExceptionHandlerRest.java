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
package com.minsait.onesait.platform.controlpanel.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice(basePackages = "com.minsait.onesait.platform.controlpanel.rest")
@Slf4j
public class GlobalExceptionHandlerRest extends ExceptionHandlerExceptionResolver {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Void> handleAccessDeniedException(HttpServletRequest req, Exception e) {
		log.error("Handling access denied exception", e);
		log.debug(e.getMessage(), e);
		return new ResponseEntity<>(HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) {
		log.error("Handling REST exception not handled", e);
		try {
			InputStream requestInputStream = req.getInputStream();
			log.error("REQUEST DATA: " + IOUtils.toString(requestInputStream, StandardCharsets.UTF_8), e);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			log.error(e1.getMessage(), e);
		}
		log.error(e.getMessage(), e);
		return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
