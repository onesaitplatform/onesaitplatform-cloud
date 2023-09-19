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
package com.minsait.onesait.platform.config.services.email;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Email;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;

@Service
public interface EmailService {

	List<Email> findAllEmailsByUserId(String userId);

	List<Email> findAllEmails(String userId);

	void saveOrUpdate(Email email);

	Email findByIdentificationOrId(String id);

	Email findById(String id);

	boolean hasUserPermission(String userId, Email email, ResourceAccessType accessType);

	void delete(Email email);

	String createEmailContent(InputStream stream);

	
	
	
}
