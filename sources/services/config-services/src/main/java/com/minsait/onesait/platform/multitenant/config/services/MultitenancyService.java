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
package com.minsait.onesait.platform.multitenant.config.services;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;

public interface MultitenancyService {

	List<Vertical> getAllVerticals();

	List<Tenant> getAllTenants();

	Optional<MasterUser> findUser(String userId);

	List<Tenant> getTenantsForCurrentVertical();

	Optional<Tenant> getTenant(String tenant);

	Optional<String> getSingleVerticalSchema(MasterUser user);

	Optional<String> getSingleVertical(MasterUser user);

	Optional<String> getSingleTenant(MasterUser user);

	boolean belongsToSingleVertical(MasterUser user);

	MasterUser mapFromUser(User user);

	MasterUser create(MasterUser user);

	MasterUser create(User user);

	List<Vertical> getVerticals(MasterUser user);

	List<Vertical> getVerticals(String user);

	List<MasterUser> getUsers();

	List<MasterUser> getUsers(String tenant);

	List<MasterUser> getUsersForCurrentVertical();

	List<MasterUser> getActiveUsersForCurrentVertical(boolean active);

	Optional<MasterUserToken> getMasterTokenByToken(String token);

	Optional<Vertical> getVertical(String vertical);

	Optional<String> getVerticalSchema(String vertical);

	void createVertical(Vertical vertical);

	void createTenant(Tenant tenant);

	void createTenant(Vertical vertical, Tenant tenant, User user);

	void createTenant(Tenant tenant, Vertical vertical);

	void promoteRole(String vertical, Authentication auth);

	void addTenant(String vertical, String tenant);

	List<Integer> getAllDomainsPorts();

	List<Integer> getAllDomainServicePorts();

	FlowDomain getFlowDomainByIdentification(String identification);

	MasterUser increaseFailedAttemp(String userId);

	MasterUser resetFailedAttemp(String userId);

	boolean isValidPass(String userId, String newPass, int numberLastEntriesToCheck);

	MasterUser setResetPass(String userId);

	MasterDeviceToken getMasterDeviceToken(String token);

	void changeUserTenant(String userId, String tenant);

	void removeFromDefaultTenant(String userId, String tenant);

	List<MasterUserToken> getAdminTokensForVerticals();

	MasterUser getUser(String userId);

	MasterUserLazy getUserLazy(String userId);

	Vertical getVerticalFromSchema(String schema);

	void updateLastLogin(String userId);

	long countTenantUsers(String tenantName);

	List<?> getAllLazy();
}
