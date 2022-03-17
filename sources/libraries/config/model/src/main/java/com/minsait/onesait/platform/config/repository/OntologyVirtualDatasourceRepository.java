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
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.User;

public interface OntologyVirtualDatasourceRepository extends JpaRepository<OntologyVirtualDatasource, String> {

	OntologyVirtualDatasource findByIdentification(String identification);

	@Query("SELECT o.identification FROM OntologyVirtualDatasource AS o WHERE o.sgdb=:sgdb")
	List<String> findIdentificationsBySgdb(@Param("sgdb") VirtualDatasourceType sgdb);

	List<OntologyVirtualDatasource> findAllByOrderByIdentificationAsc();

	@Query("SELECT o FROM OntologyVirtualDatasource AS o "
			+ "WHERE (o.identification like %:datasouceName%) ORDER BY o.identification ASC")
	List<OntologyVirtualDatasource> findAllByDatasourceNameLikeOrderByDatasourceNameAsc(
			@Param("datasouceName") String datasouceName);

	Optional<OntologyVirtualDatasource> findById(String id);

	List<OntologyVirtualDatasource> findByIsPublicTrue();

	@Query("SELECT o FROM OntologyVirtualDatasource AS o WHERE o.datasourceDomain=:datasourceDomain")
	List<OntologyVirtualDatasource> findByDatasourceDomain(@Param("datasourceDomain") String datasourceDomain);

	List<OntologyVirtualDatasource> findByUserOrIsPublicTrue(User user);

	@Query("SELECT o FROM OntologyVirtualDatasource AS o "
			+ "WHERE (o.identification like %:datasouceName%) AND (o.user =:user OR o.isPublic=TRUE)"
			+ " ORDER BY o.identification ASC")
	List<OntologyVirtualDatasource> findAllByDatasourceNameLikeAndUserIdOrIsPublicTrueOrderByDatasourceNameAsc(
			@Param("datasouceName") String datasouceName, @Param("user") User user);

	OntologyVirtualDatasource findByIdAndUser(String id, User user);

	@Query("SELECT o FROM OntologyVirtualDatasource AS o WHERE o.id =:id AND (o.user =:user OR o.isPublic=TRUE)")
	OntologyVirtualDatasource findByIdAndUserIdOrIsPublicTrue(@Param("id") String id, @Param("user") User user);

}
