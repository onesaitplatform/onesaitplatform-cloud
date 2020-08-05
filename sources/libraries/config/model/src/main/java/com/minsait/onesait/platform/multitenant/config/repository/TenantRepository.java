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
package com.minsait.onesait.platform.multitenant.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;

public interface TenantRepository extends JpaRepository<Tenant, String> {

	List<Tenant> findByVerticalsIn(Vertical vertical);

	Tenant findByName(String name);

	// @Query("SELECT t FROM Tenant AS t WHERE t IN (SELECT v.tenants FROM Vertical
	// AS v WHERE v.name=:vertical) AND t.name=:tenant")
	// Tenant findByVerticalAndTenant(@Param("vertical") String vertical,
	// @Param("tenant") String tenant);

	// @Query("SELECT t.users FROM Tenant AS t WHERE t IN (SELECT v.tenants FROM
	// Vertical AS v WHERE v.name=:vertical) AND t.name=:tenant")
	// List<MasterUser> findUsersByVerticalAndTenant(@Param("vertical") String
	// vertical, @Param("tenant") String tenant);
}
