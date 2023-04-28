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

import com.minsait.onesait.platform.config.dto.GadgetDatasourceForList;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface GadgetDatasourceRepository extends JpaRepository<GadgetDatasource, String> {

	GadgetDatasource findByIdentification(String identification);

	List<GadgetDatasource> findByUser(User user);

	List<GadgetDatasource> findByIdentificationLike(String identification);

	List<GadgetDatasource> findByOntology(Ontology ontology);

	List<GadgetDatasource> findByUserAndOntology(User user, Ontology ontology);

	List<GadgetDatasource> findByMode(String mode);

	List<GadgetDatasource> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	List<GadgetDatasource> findByIdentificationContaining(String identification);

	List<GadgetDatasource> findByDescriptionContaining(String description);

	List<GadgetDatasource> findByUserAndIdentificationContainingAndDescriptionContaining(User user,
			String identification, String description);

	List<GadgetDatasource> findByUserAndDescriptionContaining(User user, String description);

	List<GadgetDatasource> findByUserAndIdentificationContaining(User user, String identification);

	List<GadgetDatasource> findAllByOrderByIdentificationAsc();

	List<String> findIdentificationByOrderByIdentificationAsc();

	List<GadgetDatasource> findByUserOrderByIdentificationAsc(User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.GadgetDatasourceForList(o.id, o.identification, o.description, o.user, 'null', o.createdAt, o.updatedAt, o.mode, o.dbtype, o.ontology.identification, o.refresh, o.maxvalues, o.query) "
			+ "FROM GadgetDatasource AS o " + " ORDER BY o.identification ASC")
	List<GadgetDatasourceForList> findAllForListByOrderByIdentificationAsc();

	@Query("SELECT new com.minsait.onesait.platform.config.dto.GadgetDatasourceForList(o.id, o.identification, o.description, o.user, 'null', o.createdAt, o.updatedAt, o.mode, o.dbtype, o.ontology.identification, o.refresh, o.maxvalues, o.query) "
			+ " FROM GadgetDatasource AS o " + " WHERE o.user=:user" + " ORDER BY o.identification ASC")
	List<GadgetDatasourceForList> findForListByUserOrderByIdentificationAsc(@Param("user") User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.GadgetDatasourceForList(o.id, o.identification, o.description, o.user, 'null', o.createdAt, o.updatedAt, o.mode, o.dbtype, o.ontology.identification, o.refresh, o.maxvalues, o.query)  "
			+ "FROM GadgetDatasource AS o "
			+ "WHERE (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<GadgetDatasourceForList> findForListByIdentificationContainingAndDescriptionContaining(
			@Param("identification") String identification, @Param("description") String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.GadgetDatasourceForList(o.id, o.identification, o.description, o.user, 'null', o.createdAt, o.updatedAt, o.mode, o.dbtype, o.ontology.identification, o.refresh, o.maxvalues, o.query)  "
			+ "FROM GadgetDatasource AS o " + "WHERE o.user=:user " + " AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<GadgetDatasourceForList> findForListByUserAndIdentificationContainingAndDescriptionContaining(
			@Param("user") User user, @Param("identification") String identification,
			@Param("description") String description);

	@Query("SELECT o.identification FROM GadgetDatasource AS o where o.ontology.identification=:ontology ORDER BY o.identification ASC")
	List<String> findIdentificationByOntology(@Param("ontology") String ontology);

	@Query("SELECT o.identification FROM GadgetDatasource AS o ORDER BY o.identification ASC")
	List<String> findAllIdentifications();

	@Query("SELECT o.identification FROM GadgetDatasource AS o where o.user=:user ORDER BY o.identification ASC")
	List<String> findIdentificationByUser(@Param("user") User user);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, o.description, o.createdAt, o.updatedAt, o.user, 'DATASOURCE', 0) FROM GadgetDatasource AS o WHERE (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<OPResourceDTO> findAllDto(@Param("identification") String identification,
			@Param("description") String description);

	@Query("SELECT new com.minsait.onesait.platform.config.dto.OPResourceDTO(o.identification, o.description, o.createdAt, o.updatedAt, o.user, 'DATASOURCE', 0) FROM GadgetDatasource AS o WHERE o.user=:user AND (o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<OPResourceDTO> findDtoByUserAndPermissions(@Param("user") User user,
			@Param("identification") String identification, @Param("description") String description);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}