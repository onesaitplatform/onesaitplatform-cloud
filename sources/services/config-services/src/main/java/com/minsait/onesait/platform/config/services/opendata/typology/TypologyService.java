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
package com.minsait.onesait.platform.config.services.opendata.typology;

import java.util.List;

import com.minsait.onesait.platform.config.model.ODTypology;

public interface TypologyService {

	List<ODTypology> findTypologyWithIdentificationAndDescription(String identification, String description,
			String user);

	List<String> getAllIdentifications();

	List<ODTypology> getAllTypologies();

	void deleteTypology(String id, String userId);

	void saveTypology(String id, ODTypology typology, String userId);

	ODTypology getTypologyById(String id);

	String createNewTypology(ODTypology typologyCreate, String userId);

	boolean hasUserPermission(String id, String userId);

	boolean typologyExists(String identification);

	String updatePublicTypology(ODTypology typology, String userId);

	List<ODTypology> getByUserId(String userId);

	ODTypology getTypologyByIdentification(String identification);

	boolean hasUserEditPermission(String id, String userId);

	ODTypology getTypologyEditById(String id, String userId);

	String getCredentialsString(String userId);

	boolean hasUserViewPermission(String id, String userId);

	void deleteTypologyByIdentification(String typologyId, String userId);

	String getTypologyIdByTypologyIdentification(String identification);

}
