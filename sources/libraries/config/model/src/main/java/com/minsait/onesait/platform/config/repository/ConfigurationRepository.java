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

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.User;

public interface ConfigurationRepository extends JpaRepository<Configuration, String> {

	@Override
	<S extends Configuration> List<S> saveAll(Iterable<S> entities);

	@Override
	void flush();

	@Override
	<S extends Configuration> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override
	Configuration save(Configuration entity);

	@Override
	void delete(Configuration id);

	@Override
	void deleteAll();

	List<Configuration> findByUser(User user);

	Configuration findByIdentification(String identification);

	Configuration findByDescription(String description);

	List<Configuration> findByType(Type type);

	List<Configuration> findByTypeAndUser(Type type, User user);

	Configuration findByTypeAndEnvironmentAndIdentification(Type type, String environment, String identification);

	List<Configuration> findByUserAndType(User userId, Type type);

	@Override
	@Transactional
	void deleteById(String id);

	Configuration findByTypeAndEnvironment(Type type, String environment);

	Configuration findByTypeAndIdentificationIgnoreCase(Type type, String identification);

	@Override
	List<Configuration> findAll();

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

}
