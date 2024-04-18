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
package com.minsait.onesait.platform.config.repository;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.User;

public interface GadgetRepository extends JpaRepository<Gadget, String> {

	List<Gadget> findByUser(User user);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.type.id=:type)  ")
	List<Gadget> findByType(@Param("type") String type);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.type.id=:type)  ORDER BY g.identification ASC")
	List<Gadget> findByTypeOrderByIdentificationAsc(@Param("type") String type);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.user=:user AND g.identification LIKE %:identification%)  ORDER BY g.identification ASC")
	List<Gadget> findByUserAndIdentificationContaining(@Param("user") User user, @Param("identification") String identification);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.user=:user AND g.type.id LIKE %:type%)  ORDER BY g.identification ASC")
	List<Gadget> findByUserAndTypeContaining(@Param("user") User user, @Param("type") String type);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.user=:user AND g.type.id=:type AND g.identification=:identification)  ORDER BY g.identification ASC")
	List<Gadget> findByIdentificationAndTypeAndUser(@Param("identification") String identification, @Param("type") String type, @Param("user") User user);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.type.id=:type AND g.identification=:identification)  ORDER BY g.identification ASC")
	List<Gadget> findByIdentificationAndType(@Param("identification") String identification, @Param("type") String type);

	List<Gadget> findAllByOrderByIdentificationAsc();

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.type.id LIKE %:type% AND g.identification LIKE %:identification%)  ORDER BY g.identification ASC")
	List<Gadget> findByIdentificationContainingAndTypeContaining(@Param("identification") String identification, @Param("type") String type);

	List<Gadget> findByIdentificationContaining(String identification);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.type.id LIKE %:type%)  ")
	List<Gadget>  findByTypeContaining(@Param("type") String type);


	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.user=:user AND g.type.id LIKE %:type% AND g.identification LIKE %:identification%)  ORDER BY g.identification ASC")
	List<Gadget> findByUserAndIdentificationContainingAndTypeContaining(@Param("user") User user, @Param("identification") String identification, @Param("type") String type);

	Gadget findByIdentification(String identification);

	@Query("SELECT g " + "FROM Gadget AS g " + "WHERE (g.user=:user AND g.type.id=:type)  ORDER BY g.identification ASC")
	List<Gadget> findByUserAndType(@Param("user") User user, @Param("type") String type);

	@Query("SELECT g " + "FROM Gadget AS g "
			+ "WHERE ( g.identification=:identification)  ORDER BY g.identification ASC")
	List<Gadget> existByIdentification(@Param("identification") String identification);

	@Query("SELECT g " + "FROM Gadget AS g "
			+ "WHERE (g.user=:user AND g.identification=:identification)  ORDER BY g.identification ASC")
	List<Gadget> existByIdentificationUser(@Param("user") User user, @Param("identification") String identification);

	@Query("SELECT distinct(g.type.id) FROM Gadget AS g ORDER BY g.type.id")
	List<String> findGadgetTypes();

	@Query("SELECT distinct(g.type.id) FROM Gadget AS g WHERE (g.user=:user) ORDER BY g.type.id")
	List<String> findGadgetTypesbyUser(@Param("user") User user);

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

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}
