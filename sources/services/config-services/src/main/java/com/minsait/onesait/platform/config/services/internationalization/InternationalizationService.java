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
package com.minsait.onesait.platform.config.services.internationalization;

import java.util.List;

import com.minsait.onesait.platform.config.model.Internationalization;

public interface InternationalizationService {

	List<Internationalization> findInternationalizationWithIdentificationAndDescription(String identification, String description,
			String user);

	List<String> getAllIdentifications();
	
	List<Internationalization> getAllInternationalizations();

	void deleteInternationalization(String id, String userId);

	void saveInternationalization(String id, Internationalization internationalization, String userId);

	Internationalization getInternationalizationById(String id, String userId);

	String createNewInternationalization(Internationalization internationalizationCreate, String userId, boolean restApi);

	boolean hasUserPermission(String id, String userId);

	boolean internationalizationExists(String identification);

	String updatePublicInternationalization(Internationalization internationalization, String userId);
	
	List<Internationalization> getByUserIdOrPublic(String userId);

	List<Internationalization> getByUserId(String userId);

	Internationalization getInternationalizationByIdentification(String identification, String userId);

    boolean hasUserEditPermission(String id, String userId);

    Internationalization getInternationalizationEditById(String id, String userId);

    String getCredentialsString(String userId);

    boolean hasUserViewPermission(String id, String userId);

	void deleteInternationalizationByIdentification(String internationalizationId, String userId);
	
	List<Internationalization> getInternationalizationsByResourceId(String resourceId);

}
