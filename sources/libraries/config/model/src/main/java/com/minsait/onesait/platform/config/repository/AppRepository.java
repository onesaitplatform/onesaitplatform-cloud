/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.User;

public interface AppRepository extends JpaRepository<App, String> {
	
	@Query("SELECT o FROM App AS o WHERE (o.identification = :identification) ORDER BY o.identification ASC")
	App findByIdentification(@Param("identification") String identification);

	@Query("SELECT o FROM AppList AS o WHERE (o.identification like %:identification%) ORDER BY o.identification ASC")
	List<AppList> findByIdentificationLike(@Param("identification") String identification);

	@Query("SELECT o FROM App AS o WHERE o.identification=:identification")
	App findByIdentificationEquals(@Param("identification") String identification);

	@Query("SELECT o FROM AppList AS o WHERE (o.user=:user AND o.identification like %:identification%) ORDER BY o.identification ASC")
	List<AppList> findByUserANDIdentification(@Param("user") User user, @Param("identification") String identification);
	
	@Query("SELECT o FROM AppList As o")
	List<AppList> findAllList();
	
}