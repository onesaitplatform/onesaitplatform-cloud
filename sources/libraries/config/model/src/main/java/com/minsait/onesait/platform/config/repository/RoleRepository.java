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

import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.Role;

public interface RoleRepository extends JpaRepository<Role, String> {

	@Override

	<S extends Role> List<S> save(Iterable<S> entities);

	@Override

	void flush();

	@Override

	<S extends Role> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override

	Role save(Role entity);

	@Override

	void delete(Role id);

	@Override

	void deleteAll();

	long countByName(String name);

	Role findByName(String name);

	Role findById(String id);

	long countById(String id);

	@Override

	List<Role> findAll();

}
