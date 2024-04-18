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
import java.io.InputStream;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;
import org.apache.commons.codec.binary.Base64;

import com.minsait.onesait.platform.config.model.Email;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.repository.EmailRepository;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	UserService userService;

	@Autowired
	EmailRepository emailRepository;

	@Autowired
	private OPResourceService resourceService;

	@Override
	public List<Email> findAllEmailsByUserId(String userId) {
		return emailRepository.findAllByUser(userService.getUser(userId));
	}

	@Override
	public List<Email> findAllEmails(String userId) {
		return emailRepository.findAll();
	}

	@Override
	public Email findByIdentificationOrId(String id) {
		return emailRepository.findByIdentificationOrId(id, id);
	}

	@Override
	public Email findById(String id) {
		return emailRepository.findById(id).get();
	}

	@Override
	public boolean hasUserPermission(String userId, Email email, ResourceAccessType accessType) {
		final User user = userService.getUser(userId);
		if (user != null) {
			if (userService.isUserAdministrator(user) || user.equals(email.getUser()))
				return true;
			else
				return resourceService.hasAccess(userId, email.getId(), ResourceAccessType.MANAGE);

		}
		return false;
	}

	@Transactional
	@Override
	public void saveOrUpdate(Email email) {
		emailRepository.save(email);
	}

	@Override
	public void delete(Email email) {
		emailRepository.delete(email);
	}
	
	@Override
	public String createEmailContent(InputStream stream) {
		String html="";
		DocumentConverter converter = new DocumentConverter()
        	    .imageConverter(image -> {
        	    	
        	        String base64 = streamToBase64(image.getInputStream());
        	        String src = "data:" + image.getContentType() + ";base64," + base64;
        	        Map<String, String> attributes = new HashMap<>();
        	        attributes.put("src", src);
        	        return attributes;
        });
		Result<String> result;
		try {
			result = converter.convertToHtml(stream);
			html = result.getValue();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return html;

	}

	private String streamToBase64(InputStream stream) {
		try {
			stream.available();
			byte[] fileContent = stream.readAllBytes();
			String encodedString = Base64.encodeBase64String(fileContent);
			return encodedString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}

}
