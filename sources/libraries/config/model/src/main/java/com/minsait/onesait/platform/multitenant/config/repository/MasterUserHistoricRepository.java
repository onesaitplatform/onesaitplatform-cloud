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
package com.minsait.onesait.platform.multitenant.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserHistoric;

public interface MasterUserHistoricRepository extends JpaRepository<MasterUserHistoric, String> {

	public List<MasterUserHistoric> findByMasterUser(MasterUser masterUser);

	public List<MasterUserHistoric> findByMasterUserAndPassword(MasterUser masterUser, String password);

	@Query(value = "SELECT * FROM master_user_historic as c WHERE c.master_user_id=:userID order by c.created_at DESC limit :limitValue", nativeQuery = true)
	List<MasterUserHistoric> findByMasterUserLastNvalues(@Param("userID") String userID,
			@Param("limitValue") int limitValue);

}
