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
package com.minsait.onesait.platform.onesaitplatform.plugin.manager.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.onesaitplatform.plugin.manager.model.Plugin;

public interface PluginRepository extends JpaRepository<Plugin, String> {

	@Query("SELECT c FROM Plugin c WHERE c.module= :module OR c.module= 'ALL'")
	List<Plugin> findByModule(@Param("module") com.minsait.onesait.platform.plugin.Module module);

	@Query("SELECT c FROM Plugin c WHERE c.module= :module AND c.jarFile= :jarFile")
	List<Plugin> findByModuleAndJarFile(@Param("module") com.minsait.onesait.platform.plugin.Module module,
			@Param("jarFile") String jarFile);

	@Modifying
	@Transactional
	@Query("DELETE FROM Plugin c WHERE c.module= :module AND c.jarFile= :jarFile")
	int deleteByModuleAndJarFile(@Param("module") com.minsait.onesait.platform.plugin.Module module,
			@Param("jarFile") String jarFile);

	@Modifying
	@Transactional
	@Query("UPDATE Plugin c SET c.loaded = :loaded WHERE c.id = :id")
	int updateLoadedState(@Param("loaded") boolean loaded, @Param("id") String id);

}
