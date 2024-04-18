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
package com.minsait.onesait.platform.config.services.bpm;

import java.util.List;
import java.util.Set;

import com.minsait.onesait.platform.config.model.BPMTenant;
import com.minsait.onesait.platform.config.model.BPMTenantAuthorization;
import com.minsait.onesait.platform.config.model.User;

public interface BPMTenantService {

	List<BPMTenant> getTenantsForUser(String userId);

	List<String> getTenantNamesForUser(String userId);

	void createTenantAuthorization(String tenantUser, String userId);

	BPMTenant createTenant(String userId);

	BPMTenant getTenant(String tenant);

	List<BPMTenant> list(User user);

	BPMTenant createTenantWithId(String tenantId);

	void createTenantAuthorizationWhitId(String tenantId, String userId);

	Set<BPMTenantAuthorization> getTenantAuthorizations(String tenantId);

	void removeAuthorization(String tenantId, String userId);

	boolean hasUserPermissions(String tenantId, String userId);

}
