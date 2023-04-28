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
package com.minsait.onesait.platform.config.services.bpm;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.BPMTenant;
import com.minsait.onesait.platform.config.model.BPMTenantAuthorization;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.BPMTenantRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BPMTenantServiceImpl implements BPMTenantService {

	@Autowired
	private BPMTenantRepository bpmTenantRepository;
	@Autowired
	private UserService userService;

	@Override
	public List<BPMTenant> getTenantsForUser(String userId) {
		final String selfTenant = BPMTenant.TENANT_PREFIX + userId;
		if (getTenant(selfTenant) == null)
			createTenant(userId);
		final List<BPMTenant> tenants = bpmTenantRepository.findTenantsByUserId(userId);
		tenants.add(getTenant(selfTenant));
		return tenants;
	}

	@Override
	public List<String> getTenantNamesForUser(String userId) {
		return getTenantsForUser(userId).stream().map(BPMTenant::getIdentification).collect(Collectors.toList());
	}

	@Override
	public void createTenantAuthorization(String tenantUser, String userId) {
		final String tenant = BPMTenant.TENANT_PREFIX + tenantUser;
		BPMTenant tenantDb = getTenant(tenant);
		if (tenantDb == null && isValidTenantId(tenant, userId))
			tenantDb = createTenant(userId);
		if (tenantDb == null) {
			log.error("Tenant is not in DB and has invalid format, tenants have to be in the format of {} , input: {}",
					BPMTenant.TENANT_PREFIX + "{user}", tenant);
			throw new OPResourceServiceException("Invalidad tenant id");
		}
		final BPMTenantAuthorization auth = new BPMTenantAuthorization();
		auth.setAuthorizedUser(userService.getUser(userId));
		auth.setBpmTenant(tenantDb);
		tenantDb.getBpmTenantAuthorizations().add(auth);
		bpmTenantRepository.save(tenantDb);

	}

	@Override
	public void createTenantAuthorizationWhitId(String tenantId, String userId) {
		BPMTenant tenantDb = getTenant(tenantId);
		if (tenantDb == null)
			tenantDb = createTenant(extractUserFromTenant(tenantId));
		final BPMTenantAuthorization auth = new BPMTenantAuthorization();
		auth.setAuthorizedUser(userService.getUser(userId));
		auth.setBpmTenant(tenantDb);
		tenantDb.getBpmTenantAuthorizations().add(auth);
		bpmTenantRepository.save(tenantDb);

	}

	@Override
	public BPMTenant createTenant(String userId) {
		final String tenantId = BPMTenant.TENANT_PREFIX + userId;
		if (getTenant(tenantId) != null)
			return getTenant(tenantId);
		final BPMTenant tenant = new BPMTenant();
		tenant.setIdentification(tenantId);
		tenant.setUser(userService.getUser(userId));
		return bpmTenantRepository.save(tenant);

	}

	@Override
	public BPMTenant createTenantWithId(String tenantId) {
		if (getTenant(tenantId) != null)
			return getTenant(tenantId);
		final BPMTenant tenant = new BPMTenant();
		tenant.setIdentification(tenantId);
		tenant.setUser(userService.getUser(extractUserFromTenant(tenantId)));
		return bpmTenantRepository.save(tenant);

	}

	@Override
	public BPMTenant getTenant(String tenant) {
		return bpmTenantRepository.findByIdentification(tenant);

	}

	private boolean isValidTenantId(String tenant, String userId) {
		return (BPMTenant.TENANT_PREFIX + userId).equalsIgnoreCase(tenant);
	}

	@Override
	public List<BPMTenant> list(User user) {
		final String selfTenant = BPMTenant.TENANT_PREFIX + user.getUserId();
		if (getTenant(selfTenant) == null)
			createTenant(user.getUserId());
		if (userService.isUserAdministrator(user))
			return bpmTenantRepository.findAll();
		else
			return Arrays.asList(bpmTenantRepository.findByIdentification(BPMTenant.TENANT_PREFIX + user.getUserId()));
	}

	@Override
	public Set<BPMTenantAuthorization> getTenantAuthorizations(String tenantId) {
		if (getTenant(tenantId) != null) {
			return getTenant(tenantId).getBpmTenantAuthorizations();
		} else {
			log.error("Tenant {} does not exist.", tenantId);
			throw new OPResourceServiceException("Tenant " + tenantId + " does not exist.");
		}
	}

	@Override
	@Transactional
	public void removeAuthorization(String tenantId, String userId) {
		final BPMTenant tenant = getTenant(tenantId);
		if (tenant != null) {
			tenant.getBpmTenantAuthorizations().removeIf(a -> a.getAuthorizedUser().getUserId().equals(userId));
			bpmTenantRepository.save(tenant);
		} else {
			log.error("Tenant {} does not exist.", tenantId);
			throw new OPResourceServiceException("Tenant " + tenantId + " does not exist.");
		}

	}

	@Override
	public boolean hasUserPermissions(String tenantId, String userId) {
		final BPMTenant tenant = getTenant(tenantId);
		if (tenant != null) {
			return tenant.getUser().getUserId().equals(userId) || userService.getUser(userId).isAdmin();
		} else {
			log.error("Tenant {} does not exist.", tenantId);
			throw new OPResourceServiceException("Tenant " + tenantId + " does not exist.");
		}
	}

	private String extractUserFromTenant(String tenantId) {
		MultitenancyContextHolder.getTenantName();
		return tenantId.replaceFirst(BPMTenant.TENANT_PREFIX, "");
	}
}
