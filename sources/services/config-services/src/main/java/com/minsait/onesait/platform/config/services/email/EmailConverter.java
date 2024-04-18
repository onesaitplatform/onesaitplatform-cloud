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
package com.minsait.onesait.platform.config.services.email;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.dto.email.EmailDTO;
import com.minsait.onesait.platform.config.model.Email;
import com.minsait.onesait.platform.config.model.User;


import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailConverter {

	public EmailDTO convert(Email email) {

		final EmailDTO emailDTO = EmailDTO.builder().id(email.getId().toString())
				.identification(email.getIdentification()).description(email.getDescription())
				.fileName(email.getIdentification() + ".docx").created(email.getCreatedAt()).owner(email.getUser().getId().toString()).build();

		return emailDTO;
	}
	
	public Email convert(EmailDTO email) {
		log.debug("INI. Convert entity Report: {}  -->  ReportDto");

		if (email.getFile().isEmpty()) {
			log.error("Report template must be non empty");
			throw new OPResourceServiceException("Report template must be non empty");
		}

		final Email entity = new Email();

		entity.setIdentification(email.getIdentification());
		entity.setDescription(email.getDescription());
		entity.setFile(getFileBytes(email.getFile()));

		// Inner
		entity.setUser(findUser());
		return entity;
	}
	
	public Email merge(Email target, EmailDTO source) {
		final Email entity = target;

		entity.setIdentification(source.getIdentification());
		entity.setDescription(source.getDescription());
		if (!source.getFile().isEmpty()) {
			entity.setFile(getFileBytes(source.getFile()));
		}

		return entity;
	}
	
	private byte[] getFileBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (final IOException e) {
			throw new OPResourceServiceException("Error getting bytes of input file");
		}
	}
	
	private User findUser() {
		final User user = new User();
		user.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		return user;
	}

}
