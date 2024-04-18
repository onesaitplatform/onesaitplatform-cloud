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
package com.minsait.onesait.platform.systemconfig.init;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.config.ConfigDBTenantConfig;
import com.minsait.onesait.platform.config.converters.MasterUserConverter;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterDigitalTwinDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDigitalTwinDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.TenantRepository;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.multitenant.update-mode")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitConfigMultitenantUpdateDB {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MasterUserRepository masterUserRepository;
	@Autowired
	private MasterUserConverter userToMasterConverter;
	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private VerticalRepository verticalRepository;
	@Autowired
	private UserTokenRepository userTokenRepository;
	@Autowired
	private MasterUserTokenRepository masterUserTokenRepository;
	@Autowired
	private TokenRepository tokenRepository;
	@Autowired
	private MasterDeviceTokenRepository masterDeviceTokenRepository;
	@Autowired
	private DigitalTwinDeviceRepository digitalTwinDeviceRepository;
	@Autowired
	private MasterDigitalTwinDeviceTokenRepository masterDigitalTwinRepository;

	@Before
	public void setDBTenant() {
		Optional.ofNullable(System.getenv().get(ConfigDBTenantConfig.CONFIGDB_TENANT_ENVVAR)).ifPresent(s -> {
			MultitenancyContextHolder.setVerticalSchema(s);
			MultitenancyContextHolder.setTenantName(
					Tenant2SchemaMapper.defaultTenantName(Tenant2SchemaMapper.extractVerticalNameFromSchema(s)));
		});
	}

	@After
	public void unsetDBTenant() {
		MultitenancyContextHolder.clear();
	}

	@PostConstruct
	@Test
	public void updateDB() {
		log.info("Migrating data to master DB as variable UPDATE_MODE_MULITENANT is set to true...");
		try {
			migrateUsers();
		} catch (final Exception e) {
			log.warn("Could not migrate users to master DB");
		}
		try {
			migrateUserTokens();
		} catch (final Exception e) {
			log.warn("Could not migrate User tokens to master DB");
		}
		try {
			migrateDeviceTokens();
		} catch (final Exception e) {
			log.warn("Could not migrate Device tokens to master DB");
		}
		try {
			migrateDigitalTwinTokens();

		} catch (final Exception e) {
			log.warn("Could not migrate Digital twins to master DB");
		}
	}

	private void migrateUsers() {
		log.debug("Migrating users to master...");
		final List<User> users = userRepository.findAll();
		users.stream().map(userToMasterConverter::convert).forEach(mu -> {
			try {
				mu.setTenant(tenantRepository.findByName(MultitenancyContextHolder.getTenantName()));
				masterUserRepository.save(mu);
			} catch (final Exception e) {
				log.warn("Update mode activated for multitenant, error while adding user {} {}", mu.getUserId(), e);
			}
		});
	}

	private void migrateUserTokens() {
		log.debug("Migrating users tokens to master...");
		final List<UserToken> tokens = userTokenRepository.findAll();
		tokens.stream()
				.map(t -> MasterUserToken.builder().token(t.getToken())
						.vertical(verticalRepository.findByNameOrSchema(MultitenancyContextHolder.getVerticalSchema()))
						.tenant(tenantRepository.findByName(MultitenancyContextHolder.getTenantName()))
						.masterUser(masterUserRepository.findByUserId(t.getUser().getUserId())).build())
				.forEach(mt -> {
					try {
						masterUserTokenRepository.save(mt);
					} catch (final Exception e) {
						log.warn("Update mode activated for multitenant, user token {}  exist.",
								mt.getToken());
					}
				});
	}

	private void migrateDeviceTokens() {
		log.debug("Migrating device tokens to master...");
		final List<Token> tokens = tokenRepository.findAll();
		tokens.stream()
				.map(t -> MasterDeviceToken.builder().tenant(MultitenancyContextHolder.getTenantName())
						.verticalSchema(MultitenancyContextHolder.getVerticalSchema()).tokenName(t.getTokenName())
						.build())
				.forEach(mt -> {
					try {
						masterDeviceTokenRepository.save(mt);
					} catch (final Exception e) {
						log.warn("Update mode activated for multitenant, token {} exist.",
								mt.getTokenName());
					}
				});
	}

	private void migrateDigitalTwinTokens() {
		log.debug("Migrating digital twins tokens to master...");
		final List<DigitalTwinDevice> devices = digitalTwinDeviceRepository.findAll();

		devices.stream()
				.map(d -> MasterDigitalTwinDeviceToken.builder().tokenName(d.getDigitalKey())
						.tenant(MultitenancyContextHolder.getTenantName())
						.verticalSchema(MultitenancyContextHolder.getVerticalSchema()).build())
				.forEach(md -> {
					try {
						masterDigitalTwinRepository.save(md);
					} catch (final Exception e) {
						log.warn("Update mode activated for multitenant, digital twin token {} exist.",
								md.getTokenName());
					}
				});
		;

	}

}
