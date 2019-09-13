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

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.User;

public interface DigitalTwinDeviceRepository extends JpaRepository<DigitalTwinDevice, String> {

	List<DigitalTwinDevice> findByIdentificationIgnoreCase(String DigitalTwinDevice);

	List<DigitalTwinDevice> findByUrl(String description);

	DigitalTwinDevice findByIdentification(String identification);

	DigitalTwinDevice findByLongitudeAndLatitude(String longitude, String latitude);

	List<DigitalTwinDevice> findAllByOrderByIdentificationAsc();

	List<DigitalTwinDevice> findByIdentificationContaining(String identification);

	DigitalTwinDevice findById(String id);

	@Transactional
	void deleteById(String id);

	List<DigitalTwinDevice> findByUser(User user);

	List<DigitalTwinDevice> findByTypeId(DigitalTwinType typeId);

	@Query("SELECT o.identification FROM DigitalTwinDevice AS o where o.typeId = :typeId")
	List<String> findNamesByTypeId(@Param("typeId") DigitalTwinType typeId);

	@Query("SELECT o.id FROM DigitalTwinDevice AS o where o.typeId = :typeId")
	List<String> findIdsByTypeId(@Param("typeId") DigitalTwinType typeId);

	@Query("SELECT o.id FROM DigitalTwinDevice AS o where o.user = :user")
	List<String> findIdsByUser(@Param("user") User user);

	@Query("SELECT o.id FROM DigitalTwinDevice AS o")
	List<String> findAllIds();

	@Query("SELECT o.id FROM DigitalTwinDevice AS o where o.user = :user AND o.typeId = :typeId")
	List<String> findIdsByUserAndTypeId(@Param("user") User user, @Param("typeId") DigitalTwinType typeId);

}
