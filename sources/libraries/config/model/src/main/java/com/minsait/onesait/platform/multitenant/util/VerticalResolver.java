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
package com.minsait.onesait.platform.multitenant.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.multitenant.exception.TenantDBException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class VerticalResolver implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private ApplicationContext context;

	private VerticalRepository verticalRepository;

	public String getSingleVerticalSchemaIfPossible(MasterUser master) throws TenantDBException {
		if (master.getTenant() == null) {
			return Tenant2SchemaMapper.DEFAULT_SCHEMA;
		} else {

			final Tenant tenant = master.getTenant();
			final Set<Vertical> verticals = tenant.getVerticals();
			if (!CollectionUtils.isEmpty(verticals) && verticals.size() == 1)
				return verticals.iterator().next().getSchema();
		}
		// Maybe is already set in MContext
		final Vertical v = verticalRepository.findByNameOrSchema(MultitenancyContextHolder.getVerticalSchema());
		if (v != null)
			return v.getSchema();

		throw new TenantDBException(
				"User has more than one vertical or tenant associated, can not infer database schema");

	}

	public boolean hasSingleTenantSchemaAssociated(MasterUser master) {
		try {
			return master.getTenant().getVerticals().size() == 1;
		} catch (final Exception e) {
			log.debug("User has more than one vertical associated");
			return false;
		}
	}

	public Vertical getSingleVerticalIfPossible(MasterUser master) {
		if (master.getTenant() != null) {
			final Set<Vertical> verticals = master.getTenant().getVerticals();
			if (!CollectionUtils.isEmpty(verticals) && verticals.size() == 1)
				return verticals.iterator().next();

		}
		return verticalRepository.findByNameOrSchema(MultitenancyContextHolder.getVerticalSchema());

	}

	public Tenant getTenantIfPossible(MasterUser master) {
		if (master.getTenant() != null) {
			return master.getTenant();
		} else {
			return new Tenant();
		}
	}

	public List<Vertical> getVerticals(MasterUser master) {
		return master.getTenant().getVerticals().stream().collect(Collectors.toList());
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		verticalRepository = context.getBean(VerticalRepository.class);

	}

}
