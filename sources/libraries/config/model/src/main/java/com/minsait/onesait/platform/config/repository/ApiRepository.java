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

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface ApiRepository extends JpaRepository<Api, String> {

	@Override

	void flush();

	@Override

	<S extends Api> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	Api save(Api entity);

	@Override
	@Transactional
	void delete(Api id);

	@Override
	@Transactional
	void deleteAll();

	List<Api> findByIdentificationIgnoreCase(String identification);

	List<Api> findByDescription(String description);

	List<Api> findByIdentification(String identification);

	List<Api> findByDescriptionContaining(String description);

	List<Api> findByIdentificationContaining(String identification);

	List<Api> findByUser(User user);

	List<Api> findByIdentificationAndUser(String identification, User user);

	List<Api> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	List<Api> findByUserAndIdentificationLikeAndDescriptionLike(User user, String identification, String description);

	List<Api> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Api> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	List<Api> findByUserAndIdentificationContaining(User user, String identification);

	List<Api> findByUserAndDescriptionContaining(User user, String description);

	Api findByIdentificationAndNumversionAndApiType(String identification, Integer apiVersion, ApiType apiType);

	Api findByIdentificationAndNumversion(String identification, Integer apiVersion);

	List<Api> findByIdentificationAndApiType(String identification, ApiType apiType);

	List<Api> findByUserAndIsPublicTrue(User userId);

	@Query("SELECT a FROM Api AS a WHERE (a.user.userId = :userId OR a.identification LIKE %:apiId%)")
	List<Api> findApisByIdentificationOrUser(@Param("apiId") String apiId, @Param("userId") String userId);

	@Query("SELECT a FROM Api AS a WHERE (a.user.userId = :userId OR a.identification LIKE %:apiId% OR a.state = :state)")
	List<Api> findApisByIdentificationOrStateOrUser(@Param("apiId") String apiId, @Param("state") ApiStates state,
			@Param("userId") String userId);

	@Query("SELECT a FROM Api AS a WHERE (a.user.userId LIKE %:userId% AND (a.identification LIKE %:apiId% OR a.state LIKE %:state%)) AND a.isPublic IS true")
	List<Api> findApisByIdentificationOrStateAndUserAndIsPublicTrue(@Param("apiId") String apiId,
			@Param("state") String state, @Param("userId") String userId);

	@Query("SELECT a FROM Api AS a WHERE (a.user.userId = :userId) OR (a.state = 'PUBLISHED' AND a.isPublic=true)")
	List<Api> findMyApisOrApisPublicAndPublished(@Param("userId") String userId);

	@Query("SELECT a FROM Api as a WHERE a.isPublic = false AND (a.state = 'PUBLISHED' or a.state = 'DEVELOPMENT') ORDER BY a.identification asc")
	List<Api> findApisNotPublicAndPublishedOrDevelopment();

	@Query("SELECT a FROM Api as a WHERE a.user.userId = :userId AND a.isPublic = false AND (a.state = 'PUBLISHED' or a.state = 'DEVELOPMENT') ORDER BY a.identification asc")
	List<Api> findApisByUserNotPublicAndPublishedOrDevelopment(@Param("userId") String userId);

	@Query("SELECT a FROM Api as a WHERE (((:userloggedRole = 'ROLE_ADMINISTRATOR') OR (a.user.userId = :userloggedId) OR ((a.isPublic IS true) AND (a.state != 'CREATED' AND a.state != 'DELETED')) OR ((a.id IN (SELECT ua.api.id FROM UserApi AS ua WHERE ua.api.id = a.id and ua.user.userId = :userloggedId)) AND (a.state != 'CREATED' AND a.state != 'DELETED'))) AND (a.identification LIKE %:apiId% AND a.user.userId LIKE %:userId%)) ORDER BY a.identification asc")
	List<Api> findApisByIdentificationOrUserForAdminOrOwnerOrPublicOrPermission(
			@Param("userloggedId") String userloggedId, @Param("userloggedRole") String userloggedRole,
			@Param("apiId") String apiId, @Param("userId") String userId);

	@Query("SELECT a FROM Api as a WHERE (((:userloggedRole = 'ROLE_ADMINISTRATOR') OR (a.user.userId = :userloggedId) OR ((a.isPublic IS true) AND (a.state != 'CREATED' AND a.state != 'DELETED')) OR ((a.id IN (SELECT ua.api.id FROM UserApi AS ua WHERE ua.api.id = a.id and ua.user.userId = :userloggedId)) AND (a.state != 'CREATED' AND a.state != 'DELETED'))) AND (a.identification LIKE %:apiId% AND a.state = :state AND a.user.userId LIKE %:userId%)) ORDER BY a.identification asc")
	List<Api> findApisByIdentificationOrStateOrUserForAdminOrOwnerOrPublicOrPermission(
			@Param("userloggedId") String userloggedId, @Param("userloggedRole") String userloggedRole,
			@Param("apiId") String apiId, @Param("state") ApiStates state, @Param("userId") String userId);

	List<Api> findByOntology(Ontology ontology);

	@Query("SELECT a.identification FROM Api as a WHERE a.ontology.identification = :ontology ORDER BY a.identification asc")
	List<String> findIdentificationByOntology(@Param("ontology") String ontology);

	@Query("SELECT a FROM Api as a WHERE a.user.userId = :userId ORDER BY a.createdAt desc")
	List<Api> findByUserOrderByDate(@Param("userId") String userId);

	@Query("SELECT a FROM Api as a ORDER BY a.createdAt desc")
	List<Api> findAllOrderByDate();

}