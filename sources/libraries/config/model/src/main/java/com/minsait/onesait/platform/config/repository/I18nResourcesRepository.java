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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.I18nResources;

public interface I18nResourcesRepository extends JpaRepository<I18nResources, String>{

    I18nResources findById(String id);
    
	@Query("SELECT ir FROM I18nResources AS ir WHERE ir.i18n.id = :i18n")
    List<I18nResources> findByInternationalizationId(@Param("i18n") String i18n);
	
	@Query("SELECT ir FROM I18nResources AS ir WHERE ir.opResource.id = :opResource")
    List<I18nResources> findByOPResourceId(@Param("opResource") String opResource);
	
	@Query("SELECT ir FROM I18nResources AS ir WHERE ir.opResource.id = :opResource AND ir.i18n.id = :i18n")
	I18nResources findByInterAndOPResIds(@Param("i18n") String i18n, @Param("opResource") String opResource);
	
}