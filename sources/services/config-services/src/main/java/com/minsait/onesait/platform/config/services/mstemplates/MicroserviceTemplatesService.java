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
package com.minsait.onesait.platform.config.services.mstemplates;

import java.util.List;

import com.minsait.onesait.platform.config.model.MicroserviceTemplate;
import com.minsait.onesait.platform.config.model.User;

public interface MicroserviceTemplatesService {

	String createNewTemplate(MicroserviceTemplate service, String userId);

	MicroserviceTemplate save(MicroserviceTemplate service);

	MicroserviceTemplate getById(String id);

	void delete(String id);

	boolean hasUserPermission(MicroserviceTemplate msTemplate, User user);

	List<String> getAllIdentificationsByUser(String userId);
	
	List<MicroserviceTemplate> getAllMicroserviceTemplatesByCriterials(String identification, String user);

	List<MicroserviceTemplate> findMicroserviceTemplatesWithIdentificationAndDescription(String identification, String description,
			String user);
	
	MicroserviceTemplate getMsTemplateById(String id, String userId);
	
	MicroserviceTemplate getMsTemplateByIdentification(String identification, String userId);

	boolean hasUserEditPermission(String id, String userId);

	boolean mstemplateExists(String identification);

	String updateMsTemplate(MicroserviceTemplate mstemplate, String userId);

	MicroserviceTemplate getMsTemplateEditById(String id, String userId);

	boolean hasUserViewPermission(String id, String userId);

	String getCredentialsString(String userId);

	List<MicroserviceTemplate> getAllMicroserviceTemplatesByUser(String userId);
}
