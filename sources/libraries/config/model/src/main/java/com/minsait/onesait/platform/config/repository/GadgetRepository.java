/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.User;

public interface GadgetRepository extends JpaRepository<Gadget, String> {

	List<Gadget> findByUser(User user);

	List<Gadget> findByType(String type);

	List<Gadget> findByTypeOrderByIdentificationAsc(String type);

	List<Gadget> findByUserAndIdentificationContaining(User user, String identification);

	List<Gadget> findByUserAndTypeContaining(User user, String type);

	List<Gadget> findByIdentificationAndTypeAndUser(String identification, String type, User user);

	List<Gadget> findByIdentificationAndType(String identification, String type);

	List<Gadget> findAllByOrderByIdentificationAsc();

	List<Gadget> findByIdentificationContainingAndTypeContaining(String identification, String type);

	List<Gadget> findByIdentificationContaining(String identification);

	List<Gadget> findByTypeContaining(String type);

	List<Gadget> findByUserAndIdentificationContainingAndTypeContaining(User user, String identification, String type);

	Gadget findByIdentification(String identification);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.user=:user AND g.type=:type)  ORDER BY g.identification ASC")
	List<Gadget> findByUserAndType(@Param("user") User user, @Param("type") String type);

	@Query("SELECT g " + "FROM Gadget AS g "
			+ "WHERE ( g.identification=:identification)  ORDER BY g.identification ASC")
	List<Gadget> existByIdentification(@Param("identification") String identification);

	@Query("SELECT g " + "FROM Gadget AS g "
			+ "WHERE (g.user=:user AND g.identification=:identification)  ORDER BY g.identification ASC")
	List<Gadget> existByIdentificationUser(@Param("user") User user, @Param("identification") String identification);

	@Query("SELECT distinct(g.type) FROM Gadget AS g ORDER BY g.type")
	List<String> findGadgetTypes();

	@Query("SELECT g.identification FROM Gadget AS g ORDER BY g.identification ASC")
	List<String> findAllIdentifications();

	@Query("SELECT g.identification FROM Gadget AS g WHERE g.user=:user ORDER BY g.identification ASC")
	List<String> findAllIdentificationsByUser(@Param("user") User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, o.description, o.createdAt, o.updatedAt, o.user, 'GADGET', 0) FROM Gadget AS o WHERE (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<OPResourceDTO> findAllDto(@Param("identification") String identification,
			@Param("description") String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, o.description, o.createdAt, o.updatedAt, o.user, 'GADGET', 0) FROM Gadget AS o WHERE o.user=:user AND (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<OPResourceDTO> findDtoByUserAndPermissions(@Param("user") User user,
			@Param("identification") String identification, @Param("description") String description);
}
