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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.User;

public interface UserRepository extends JpaRepository<User, String> {

	public static final String USER_REPOSITORY = "UserRepository";

	@Override
	@CacheEvict(cacheNames = USER_REPOSITORY, allEntries = true)
	<S extends User> List<S> saveAll(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = USER_REPOSITORY, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = USER_REPOSITORY, key = "#p0.userId")
	<S extends User> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CachePut(cacheNames = USER_REPOSITORY, key = "#p0.userId")
	User save(User entity);

	@Override
	@Transactional
	@CacheEvict(cacheNames = USER_REPOSITORY, key = "#p0.userId")
	void delete(User id);

	@Transactional
	@CacheEvict(cacheNames = USER_REPOSITORY, key = "#p0")
	void deleteByUserId(String userId);

	@Override
	@Transactional
	@CacheEvict(cacheNames = USER_REPOSITORY, allEntries = true)
	void deleteAll(Iterable<? extends User> entities);

	@Override
	@Transactional
	@CacheEvict(cacheNames = USER_REPOSITORY, allEntries = true)
	void deleteAll();

	@Override
	List<User> findAll();

	@Query("SELECT o FROM User AS o WHERE o.active=true")
	List<User> findAllActiveUsers();

	@Query("SELECT o FROM User AS o WHERE o.email=:email")
	List<User> findByEmail(@Param("email") String email);

	int countByEmail(String email);

	User findUserByEmail(String email);

	@Cacheable(cacheNames = USER_REPOSITORY, unless = "#result == null", key = "#p0")
	User findByUserIdAndPassword(String userId, String password);

	@Transactional
	@Cacheable(cacheNames = USER_REPOSITORY, unless = "#result == null", key = "#p0")
	User findByUserId(String userId);

	// Only for projects
	@Query("SELECT o FROM User AS o WHERE o.userId = :userId")
	User findByUserNoCache(@Param("userId") String userId);

	@Query("SELECT o FROM User AS o WHERE o.role !='ROLE_ADMINISTRATOR'")
	List<User> findUsersNoAdmin();

	@Query("SELECT o FROM User AS o WHERE (o.userId LIKE %:userId% OR o.fullName LIKE %:fullName% OR o.email LIKE %:email% OR o.role.name =:role)")
	List<User> findByUserIdOrFullNameOrEmailOrRoleType(@Param("userId") String userId,
			@Param("fullName") String fullName, @Param("email") String email, @Param("role") String role);

	@Query("SELECT o FROM User AS o WHERE (o.userId LIKE %:userId% OR o.fullName LIKE %:fullName% OR o.email LIKE %:email% OR o.role.name =:role OR o.active=:active)")
	List<User> findByUserIdOrFullNameOrEmailOrRoleTypeOrActive(@Param("userId") String userId,
			@Param("fullName") String fullName, @Param("email") String email, @Param("role") String role,
			@Param("active") boolean active);

	@Query("SELECT o FROM User AS o WHERE (o.userId != :userId AND o.role.id != :rolId) ORDER BY o.userId")
	List<User> findUserByIdentificationAndNoRol(@Param("userId") String userId, @Param("rolId") String rolId);

	@Query("SELECT o FROM User AS o WHERE (o.userId != :userId AND o.role.id = :rolId) ORDER BY o.userId")
	List<User> findUserByIdentificationAndRol(@Param("userId") String userId, @Param("rolId") String rolId);

	@Query("SELECT o FROM User AS o WHERE o.userId LIKE %:userId% AND o.active=true ORDER BY o.userId DESC")
	List<User> findByUsernameLike(@Param("userId") String userId);
}
