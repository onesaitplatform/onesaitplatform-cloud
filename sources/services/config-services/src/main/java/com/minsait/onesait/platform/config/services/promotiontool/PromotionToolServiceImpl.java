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
package com.minsait.onesait.platform.config.services.promotiontool;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PromotionToolServiceImpl implements PromotionToolService {

	@Autowired
	private MultitenancyService multitenancyService;

	@Override
	@Transactional
	public List<String> getTenants() {
		final List<String> tenantList = new LinkedList<>();

		List<Tenant> tenants = multitenancyService.getTenantsForCurrentVertical();

		for (Tenant tenant : tenants)
			tenantList.add(tenant.getName());

		return tenantList;
	}

	public String getConfigDBs(List<String> tenants) {
		if (tenants.size() == 0)
			return "[]";
		String result = "[";
		Set<Vertical> verticales_migrar = new HashSet<Vertical>();
		for (String tenant_str : tenants) {
			Optional<Tenant> tenant = multitenancyService.getTenant(tenant_str);
			if (tenant.isPresent()) {
				Set<Vertical> verticals = tenant.get().getVerticals();
				for (Vertical v : verticals) {
					if (!verticales_migrar.contains(v))
						verticales_migrar.add(v);
				}
			}
		}

		for (Vertical vert : verticales_migrar) {
			result = result + vert.getSchema() + ",";
		}
		result = result + "onesaitplatform_master_config]";
		return result;
	}

	public String getRealTimeDBs(List<String> tenants) {
		if (tenants.size() == 0)
			return "[]";
		String result = "[";
		for (String tenant : tenants) {
			if (tenant.equals("development_onesaitplatform")) {
				result = result + "'onesaitplatform_rtdb'";
			} else {
				result = result + "onesaitplatform_rtdb" + "_" + tenant;
			}
			result = result + ",";
		}
		result = result.substring(0, result.length() - 1) + "]";
		return result;
	}

}
