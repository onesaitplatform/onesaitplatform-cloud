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
package com.minsait.onesait.platform.config.repository;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface UserRepository extends JpaRepository<User, String> {

	public static final String USER_REPOSITORY = "UserRepository";

	@Override
	@CacheEvict(cacheNames = USER_REPOSITORY, allEntries = true)
	<S extends User> List<S> saveAll(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = USER_REPOSITORY, allEntries = true)
	void flush();

	@Override
	@CachePut(cacheNames = USER_REPOSITORY, key = "{#p0.userId.toLowerCase()}")
	<S extends User> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CachePut(cacheNames = USER_REPOSITORY, key = "{#p0.userId.toLowerCase()}")
	User save(User entity);

	@Override
	@Transactional
	@CacheEvict(cacheNames = USER_REPOSITORY, key = "{#p0.userId.toLowerCase()}")
	void delete(User id);

	@Transactional
	@CacheEvict(cacheNames = USER_REPOSITORY, key = "{#p0.toLowerCase()}")
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

	@Query("SELECT o.email FROM User AS o WHERE o.userId !=:userId")
	List<String> findAllEmailsNotUser(@Param("userId") String userId);

	@Query("SELECT o FROM User AS o WHERE o.active=true")
	List<User> findAllActiveUsers();

	@Query("SELECT COUNT(*) FROM User AS o WHERE o.active=true")
	int countAllActiveUsers();

	@Query("SELECT COUNT(*) FROM User AS o WHERE o.active=false")
	int countAllInactiveUsers();

	@Query("SELECT COUNT(*) FROM User AS o WHERE o.createdAt >= CURDATE()")
	int countNewUsers();

	@Query("SELECT o FROM User AS o WHERE o.email=:email")
	List<User> findByEmail(@Param("email") String email);

	int countByEmail(String email);

	User findUserByEmail(String email);

	@Cacheable(cacheNames = USER_REPOSITORY, unless = "#result == null", key = "{#p0.toLowerCase()}")
	User findByUserIdAndPassword(String userId, String password);

	@Transactional
	@Cacheable(cacheNames = USER_REPOSITORY, unless = "#result == null", key = "{#p0.toLowerCase()}")
	User findByUserId(String userId);

	// Only for projects
	@Query("SELECT o FROM User AS o WHERE o.userId = :userId")
	User findByUserNoCache(@Param("userId") String userId);

	@Query("SELECT o FROM User AS o WHERE o.role !='ROLE_ADMINISTRATOR'")
	List<User> findUsersNoAdmin();

	@Query("SELECT o FROM User AS o WHERE (o.userId LIKE :userId AND o.fullName LIKE :fullName AND o.email LIKE :email AND o.role.name LIKE :role)")
	List<User> findByUserIdAndFullNameAndEmailAndRoleType(@Param("userId") String userId,
			@Param("fullName") String fullName, @Param("email") String email, @Param("role") String role);

	@Query("SELECT o FROM User AS o WHERE (o.userId LIKE :userId AND o.fullName LIKE :fullName AND o.email LIKE :email AND o.role.name LIKE :role AND o.active=:active)")
	List<User> findByUserIdAndFullNameAndEmailAndRoleTypeAndActive(@Param("userId") String userId,
			@Param("fullName") String fullName, @Param("email") String email, @Param("role") String role,
			@Param("active") boolean active);

	@Query("SELECT o FROM User AS o WHERE (o.userId != :userId AND o.role.id != :rolId AND o.active=true) ORDER BY o.userId")
	List<User> findUserByIdentificationAndNoRol(@Param("userId") String userId, @Param("rolId") String rolId);

	@Query("SELECT o FROM User AS o WHERE (o.userId != :userId AND o.role.id = :rolId AND o.active=true)  ORDER BY o.userId")
	List<User> findUserByIdentificationAndRol(@Param("userId") String userId, @Param("rolId") String rolId);

	@Query("SELECT o FROM User AS o WHERE o.userId LIKE %:userId% AND o.active=true ORDER BY o.userId DESC")
	List<User> findByUsernameLike(@Param("userId") String userId);
	
	@Query("SELECT o FROM User AS o WHERE o.fullName LIKE %:fullNameLike% AND o.active=true ORDER BY o.userId DESC")
	List<User> findByFullNameLike(@Param("fullNameLike") String fullNameLike);
	
	@Query("SELECT o.email FROM User AS o WHERE o.userId= :userId")
	String findEmailByUserId(@Param("userId") String userId);

	@Query("SELECT o.fullName FROM User AS o WHERE o.userId= :userId")
	String findFullNameByUserId(@Param("userId") String userId);

	@Modifying
	@Transactional
	public void deleteByUserIdNotIn(Collection<String> ids);

	@Modifying
	@Transactional
	public default void deleteByIdNotIn(Collection<String> ids) {
		deleteByUserIdNotIn(ids);
	}

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.userId, o.userId, 'User', o.userId) FROM User AS o")
	public List<VersionableVO> findVersionableViews();

}
