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
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.model.User;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

	List<Subscription> findAllByOrderByIdentificationAsc();

	Optional<Subscription> findById(String id);

	List<Subscription> findByIdentificationContainingAndDescriptionContainingOrderByIdentificationAsc(
			String identification, String description);

	List<Subscription> findByUserAndIdentificationContainingAndDescriptionContainingOrderByIdentificationAsc(User user,
			String identification, String description);

	List<Subscription> findByIdentification(String identification);
	
	@Cacheable(cacheNames = "SubscriptionRepositoryByOntologyIdentification")
	List<Subscription> findByOntologyIdentification(String ontologyIdentification);
	
	@Override
	@CacheEvict(cacheNames = "SubscriptionRepositoryByOntologyIdentification", allEntries = true)
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = "SubscriptionRepositoryByOntologyIdentification", allEntries = true)
	@Transactional
	void delete(Subscription entity);

	@Override
	@CacheEvict(cacheNames = "SubscriptionRepositoryByOntologyIdentification", allEntries = true)
	@Transactional
	<S extends Subscription> S save(S flow);

	@Override
	@CacheEvict(cacheNames = "SubscriptionRepositoryByOntologyIdentification", allEntries = true)
	@Transactional
	void flush();

	@Query("SELECT o.identification FROM Subscription AS o where o.ontology.identification=:ontology ORDER BY o.identification ASC")
	List<String> findIdentificationByOntology(@Param("ontology") String ontology);

}
