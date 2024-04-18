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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.model.User;

public interface ClientPlatformInstanceRepository extends JpaRepository<ClientPlatformInstance, String> {

	public static final String CLIENTPLATFORMINSTANCE_REPOSITORY = "ClientPlatformInstanceRepository";

	@Override
	@CacheEvict(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, allEntries = true)
	<S extends ClientPlatformInstance> List<S> saveAll(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, allEntries = true)
	void flush();

	@Override
	@CacheEvict(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	<S extends ClientPlatformInstance> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	ClientPlatformInstance save(ClientPlatformInstance entity);

	@Modifying
	@CacheEvict(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, key = "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)")
	@Query("DELETE ClientPlatformInstance d WHERE d= :clientPlatformInstance")
	@Transactional
	void deleteById(@Param("clientPlatformInstance") ClientPlatformInstance clientPlatformInstance);

	@Override
	List<ClientPlatformInstance> findAll();

	@Override
	@CacheEvict(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, allEntries = true)
	@Transactional
	void deleteAll();

	List<ClientPlatformInstance> findByClientPlatform(ClientPlatform clientPlatform);

	@Cacheable(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, key = "#a0.identification.concat('-').concat(#a1)", unless = "#result == null")
	ClientPlatformInstance findByClientPlatformAndIdentification(ClientPlatform clientPlatform, String identification);

	@Cacheable(cacheNames = CLIENTPLATFORMINSTANCE_REPOSITORY, key = "#a0.concat('-').concat(#a1)", unless = "#result == null")
	@Query("SELECT cpi " + "FROM ClientPlatformInstance cpi INNER JOIN cpi.clientPlatform cp "
			+ "WHERE cpi.identification = :identification AND cp.identification = :clientPlatformIdentification")
	ClientPlatformInstance findByClientPlatformAndIdentification(
			@Param("clientPlatformIdentification") String clientPlatformIdentification,
			@Param("identification") String identification);

	@Modifying
	@Query("UPDATE ClientPlatformInstance c SET c.connected = :connected, c.disabled = :disabled WHERE c.updatedAt < :date")
	int updateClientPlatformInstanceStatusByUpdatedAt(@Param("connected") boolean connected,
			@Param("disabled") boolean disabled, @Param("date") Date date);

	// the method calling this one must deal with the cache updated
	// for example, a method that returns the session can use this annotation:
	// @CachePut(cacheNames =
	// ClientPlatformInstanceRepository.CLIENTPLATFORMINSTANCE_REPOSITORY, key =
	// "#p0.clientPlatform.identification.concat('-').concat(#p0.identification)",
	// unless = "#result == null")
	// where the key is the client platform identification concatenated with the
	// clientplatforminstance identification.
	@Transactional
	@Modifying
	@Query("UPDATE ClientPlatformInstance c SET " + "c.identification= :identification, "
			+ "c.clientPlatform= :clientPlatform, " + "c.protocol = :protocol, " + "c.location = :location, "
			+ "c.updatedAt = :updatedAt, " + "c.status = :status, " + "c.connected = :connected, "
			+ "c.disabled = :disabled " + "WHERE c.id = :id AND c.updatedAt < :updatedAt") // the updateAt condition is
																							// to avoid race conditions
																							// that cause that older
																							// values overwrite newer
																							// values
	public int updateClientPlatformInstance(@Param("clientPlatform") ClientPlatform clientPlatform,
			@Param("identification") String identification, @Param("protocol") String protocol,
			@Param("location") double[] location, @Param("updatedAt") Date updatedAt, @Param("status") String status,
			@Param("connected") boolean connected, @Param("disabled") boolean disabled, @Param("id") String id);

	// int updateClientPlatformInstance(@Param("clientPlatform") ClientPlatform
	// clientPlatform,
	// @Param("identification") String identification, @Param("sessionKey") String
	// sessionKey,
	// @Param("protocol") String protocol, @Param("location") double[] location,
	// @Param("updatedAt") Date updatedAt, @Param("status") String status,
	// @Param("connected") boolean connected,
	// @Param("disabled") boolean disabled, @Param("id") String id);

	@Transactional
	@Modifying
	@Query(value = " INSERT INTO onesaitplatform_config.client_platform_instance (id, created_at, updated_at, connected, disabled, identification, json_actions, location, protocol, status, tags, client_platform_id) "
			+ "    SELECT uuid(), :#{#cpi.createdAt}, :#{#cpi.updatedAt}, :#{#cpi.connected}, :#{#cpi.disabled}, :#{#cpi.identification}, :#{#cpi.jsonActions}, :#{#cpi.location}, :#{#cpi.protocol}, :#{#cpi.status}, :#{#cpi.tags}, :#{#cp} "
			+ "    FROM " + "     (SELECT client_platform_id, count(*) AS actual_devices "
			+ "      FROM onesaitplatform_config.client_platform_instance "
			+ "      WHERE client_platform_id = :#{#cp} AND identification <> :#{#cpi.identification} "
			+ "     ) AS actual " + "    WHERE :#{#limit} > 0 AND actual.actual_devices < :#{#limit} "
			+ " ON DUPLICATE KEY UPDATE updated_at = :#{#cpi.updatedAt}, connected = :#{#cpi.connected}, disabled = :#{#cpi.disabled}, json_actions = :#{#cpi.jsonActions}, location = :#{#cpi.location}, protocol = :#{#cpi.protocol}, status = :#{#cpi.status}, tags = :#{#cpi.tags}", nativeQuery = true)
	public int createOrUpdateClientPlatformInstance(@Param("cpi") ClientPlatformInstance entity,
			@Param("cp") String clientPlatformId, @Param("limit") int limit);

	@Transactional
	@Modifying
	@Query(value = " INSERT INTO client_platform_instance (id, created_at, updated_at, connected, disabled, identification, json_actions, location, protocol, status, tags, client_platform_id) "
			+ "  values ( :#{#cpi.id}, :#{#cpi.createdAt}, :#{#cpi.updatedAt}, :#{#cpi.connected}, :#{#cpi.disabled}, :#{#cpi.identification}, :#{#cpi.jsonActions}, :#{#cpi.location}, :#{#cpi.protocol}, :#{#cpi.status}, :#{#cpi.tags}, :#{#cp})", nativeQuery = true)
	public int createClientPlatformInstance( @Param("cpi") ClientPlatformInstance entity,
			@Param("cp") String clientPlatformId);
	
	@Transactional
	@Query(value = " SELECT COUNT(*) FROM client_platform_instance cpi WHERE cpi.client_platform_id = :#{#cp} AND cpi.identification = :#{#cpi.identification}", nativeQuery = true)
	public int getClientPlatformInstance( @Param("cpi") ClientPlatformInstance entity,
			@Param("cp") String clientPlatformId);
	
	@Transactional
	@Modifying
	@Query(value = " UPDATE client_platform_instance SET updated_at = :#{#cpi.updatedAt}, connected = :#{#cpi.connected}, disabled = :#{#cpi.disabled}, json_actions = :#{#cpi.jsonActions}, location = :#{#cpi.location}, "
			+ "protocol = :#{#cpi.protocol}, status = :#{#cpi.status}, tags = :#{#cpi.tags} WHERE client_platform_id = :#{#cp} AND identification =  :#{#cpi.identification}", nativeQuery = true)
	public int updateClientPlatformInstance( @Param("cpi") ClientPlatformInstance entity,
			@Param("cp") String clientPlatformId);


	@Override
	Optional<ClientPlatformInstance> findById(String id);

	@Query("SELECT t FROM ClientPlatformInstance t WHERE t.clientPlatform.user= :#{#user}")
	List<ClientPlatformInstance> findByUser(User user);
}
