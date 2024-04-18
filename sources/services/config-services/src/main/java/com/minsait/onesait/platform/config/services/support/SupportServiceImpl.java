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
package com.minsait.onesait.platform.config.services.support;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.SupportRequest;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.SupportRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SupportServiceImpl implements SupportService {

	@Autowired
	private SupportRepository supportRepository;
	@Autowired
	private UserRepository userRepository;

	@Override
	public void createSupportRequest(User user, String type, String text, String changeTo) {

		final JSONObject json = new JSONObject();
		final SupportRequest supportRequest = new SupportRequest();

		try {
			json.put("User", user.getUserId());
			json.put("Role", user.getRole().getId());
			json.put("Type", type);
			json.put("Request", text.replace("\"", "\\\""));
			json.put("Change To", changeTo);

			supportRequest.setJson(json.toString());
			supportRequest.setType(type);
			supportRequest.setUser(user);
			supportRequest.setStatus("SENT");

			supportRepository.save(supportRequest);

		} catch (JSONException e) {
			log.error("Error parsing message to Json: {}", e.getMessage());
		}
	}

	@Override
	public void updateStatus(SupportRequest supportRequest) {
		try {
			if (supportRequest.getStatus().equals("SENT")) {
				supportRequest.setStatus("READ");
				supportRepository.save(supportRequest);
			} else if (supportRequest.getStatus().equals("READ")) {
				supportRequest.setStatus("PROCESS");
				supportRepository.save(supportRequest);
			}
		} catch (final Exception e) {
			log.error("Error updating the status: {}", e.getMessage());
		}
	}

	@Override
	public void changeRole(User user, Role role) {
		try {

			user.setRole(role);
			userRepository.save(user);

		} catch (final Exception e) {
			log.error("Error Changing User Role: {}", e.getMessage());
		}
	}

}
