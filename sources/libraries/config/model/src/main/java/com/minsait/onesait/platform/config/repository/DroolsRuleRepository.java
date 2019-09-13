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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface DroolsRuleRepository extends JpaRepository<DroolsRule, String> {

	@Cacheable(cacheNames = "DroolsRulesByUser", unless = "#result == null", key = "#p0.userId")
	List<DroolsRule> findByUser(User user);

	@SuppressWarnings("unchecked")
	@Override
	@CacheEvict(allEntries = true, cacheNames = { "DroolsRulesByUser", "DroolsRulesBySourceOntology" })
	DroolsRule save(DroolsRule rule);

	@Cacheable(cacheNames = "DroolsRulesBySourceOntology", unless = "#result == null", key = "#p0.identification")
	List<DroolsRule> findBySourceOntologyAndActiveTrue(Ontology sourceOntology);

	// @Cacheable(cacheNames = "DroolsRulesByIdentification", unless = "#result ==
	// null", key = "#p0")
	DroolsRule findByIdentification(String identification);

	int countByUser(User user);

	@CacheEvict(allEntries = true, cacheNames = { "DroolsRulesByUser", "DroolsRulesBySourceOntology" })
	@Modifying
	@Query("update DroolsRule dr set dr.active = :active where dr.identification = :identification")
	int updateActiveByIdentification(@Param("active") boolean active, @Param("identification") String identification);

	@CacheEvict(allEntries = true, cacheNames = { "DroolsRulesByUser", "DroolsRulesBySourceOntology" })
	@Modifying
	@Query("update DroolsRule dr set dr.DRL = :drl where dr.identification = :identification")
	int updateDRLByIdentification(@Param("drl") String drl, @Param("identification") String identification);

	@CacheEvict(allEntries = true, cacheNames = { "DroolsRulesByUser", "DroolsRulesBySourceOntology" })
	@Transactional
	void deleteByIdentification(String identification);
}
