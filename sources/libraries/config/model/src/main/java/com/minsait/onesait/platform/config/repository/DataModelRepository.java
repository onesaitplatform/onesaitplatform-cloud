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
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.DataModel;

public interface DataModelRepository extends JpaRepository<DataModel, String> {

	@Override
	@Cacheable(cacheNames = "DataModelRepositoryAll", unless = "#result==null or #result.size()==0")
	List<DataModel> findAll();

	@Override
	@Cacheable(cacheNames = "DataModelRepositoryById", unless = "#result == null", key = "#p0")
	Optional<DataModel> findById(String id);

	@Cacheable(cacheNames = "DataModelRepositoryByIdentification", unless = "#result==null or #result.size()==0", key = "#p0")
	List<DataModel> findByIdentification(String identification);

	@Cacheable(cacheNames = "DataModelRepositoryByType", unless = "#result==null or #result.size()==0", key = "#p0")
	List<DataModel> findByType(String type);

	long countByType(String type);

	@Query("SELECT o " + "FROM DataModel AS o " + "WHERE o.id LIKE %:id% OR "
			+ "o.identification LIKE %:identification% OR " + "o.description LIKE %:description%")
	List<DataModel> findByIdOrIdentificationOrDescription(@Param(value = "id") String id,
			@Param(value = "identification") String identification, @Param(value = "description") String description);

	@Query("SELECT o " + "FROM DataModel AS o " + "WHERE o.identification LIKE %:identification% ")
	DataModel findDatamodelsByIdentification(@Param(value = "identification") String identification);

	@Override
	@CacheEvict(cacheNames = { "DataModelRepositoryAll", "DataModelRepositoryById",
			"DataModelRepositoryByIdentification", "DataModelRepositoryByType" }, allEntries = true)
	@Modifying
	@Transactional
	void deleteById(String id);

	@Override
	@CacheEvict(cacheNames = { "DataModelRepositoryAll", "DataModelRepositoryById",
			"DataModelRepositoryByIdentification", "DataModelRepositoryByType" }, allEntries = true)
	@Modifying
	@Transactional
	void delete(DataModel entity);

	@Override
	@CacheEvict(cacheNames = { "DataModelRepositoryAll", "DataModelRepositoryById",
			"DataModelRepositoryByIdentification", "DataModelRepositoryByType" }, allEntries = true)
	DataModel save(DataModel datamodel);

	@Override
	@CacheEvict(cacheNames = { "DataModelRepositoryAll", "DataModelRepositoryById",
			"DataModelRepositoryByIdentification", "DataModelRepositoryByType" }, allEntries = true)
	void flush();

	@Modifying
	@Transactional
	@CacheEvict(cacheNames = { "DataModelRepositoryAll", "DataModelRepositoryById",
			"DataModelRepositoryByIdentification", "DataModelRepositoryByType" }, allEntries = true)
	void deleteByIdNotIn(Collection<String> ids);
}
