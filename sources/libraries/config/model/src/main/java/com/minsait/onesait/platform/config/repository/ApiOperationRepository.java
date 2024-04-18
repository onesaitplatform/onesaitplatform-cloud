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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

public interface ApiOperationRepository extends JpaRepository<ApiOperation, String> {

	@Override
	void flush();

	@Override
	<S extends ApiOperation> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	ApiOperation save(ApiOperation entity);

	@Override
	@Transactional
	void delete(ApiOperation id);

	@Override
	@Transactional
	void deleteAll();

	public List<ApiOperation> findByIdentificationIgnoreCase(String identification);

	public List<ApiOperation> findByDescription(String description);

	public List<ApiOperation> findByIdentification(String identification);

	public List<ApiOperation> findByDescriptionContaining(String description);

	public List<ApiOperation> findByIdentificationContaining(String identification);

	public List<ApiOperation> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	public List<ApiOperation> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	public List<ApiOperation> findByApiIdOrderByOperationDesc(String identification);

	public List<ApiOperation> findByApiOrderByOperationDesc(Api api);

	public List<ApiOperation> findAllByApi(Api api);

	public List<ApiOperation> findByApiAndOperation(Api api, Type operation);

	public List<ApiOperation> findByApiAndIdentification(Api api, String identification);

	@Query("SELECT o " + "FROM ApiOperation AS o " + "WHERE o.operation = 'POST' AND o.api.ontology = :ontologyId")
	List<ApiOperation> findByOntologyAndPostMethod(@Param("ontologyId") Ontology ontologyId);

	@Query("SELECT o " + "FROM ApiOperation AS o " + "WHERE o.operation = 'GET' AND o.api.ontology = :ontologyId")
	List<ApiOperation> findByOntologyAndGetMethod(@Param("ontologyId") Ontology ontologyId);

	@Query("SELECT o FROM ApiOperation AS o WHERE o.api.user= :user")
	List<ApiOperation> findByUser(@Param("user") User user);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}