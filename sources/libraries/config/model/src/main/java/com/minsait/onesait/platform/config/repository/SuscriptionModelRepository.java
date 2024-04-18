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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.minsait.onesait.platform.config.model.SuscriptionNotificationsModel;

public interface SuscriptionModelRepository extends JpaRepository<SuscriptionNotificationsModel, String> {
	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	<S extends SuscriptionNotificationsModel> List<S> save(Iterable<S> entities);

	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	void flush();

	@Override
	List<SuscriptionNotificationsModel> findAll();

	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	<S extends SuscriptionNotificationsModel> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	SuscriptionNotificationsModel save(SuscriptionNotificationsModel entity);

	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	@Transactional
	void delete(SuscriptionNotificationsModel id);

	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	@Transactional
	void deleteAll();

	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	@Transactional
	void deleteBySuscriptionId(String id);

	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	@Transactional
	void deleteByOntologyName(String ontologyName);

	@Cacheable(cacheNames = "SuscriptionModelRepositoryByOntologyName", unless = "#result==null or #result.size()==0", key = "#p0")
	List<SuscriptionNotificationsModel> findAllByOntologyName(String ontologyName);

	@Cacheable(cacheNames = "SuscriptionModelRepositoryBySubscriptionId", unless = "#result == null", key = "#p0")
	SuscriptionNotificationsModel findAllBySuscriptionId(String suscriptionId);

	@Override
	@CacheEvict(cacheNames = { "SuscriptionModelRepositoryByOntologyName",
			"SuscriptionModelRepositoryBySubscriptionId" }, allEntries = true)
	@Modifying
	@Transactional
	void delete(String id);

}