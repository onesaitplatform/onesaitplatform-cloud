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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.converters.JPAHAS256ConverterCustom;
import com.minsait.onesait.platform.config.converters.MasterUserConverter;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.repository.FlowDomainRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.digitaltwin.device.DigitalTwinDeviceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterDigitalTwinDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserHistoric;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDigitalTwinDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserHistoricRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthAccessTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthRefreshTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.TenantRepository;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.multitenant.exception.TenantDBException;
import com.minsait.onesait.platform.multitenant.util.VerticalResolver;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MultitenancyServiceImpl implements MultitenancyService {

	@Autowired
	private MasterUserConverter converter;
	@Autowired
	private MasterUserRepository masterUserRepository;
	@Autowired
	private VerticalRepository verticalRepository;
	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private MasterUserTokenRepository masterUserTokenRepository;
	@Autowired
	private MasterDeviceTokenRepository masterDeviceTokenRepository;
	@Autowired
	private MasterDigitalTwinDeviceTokenRepository masterDigitalTwinDeviceTokenRepository;
	@Autowired
	private VerticalResolver verticalResolver;
	@Autowired
	private UserService userService;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private FlowDomainRepository flowDomainRepository;
	@Autowired
	private MasterUserHistoricRepository masterUserHistoricRepository;
	@Autowired
	private OAuthAccessTokenRepository oauthAccessTokenRepository;
	@Autowired
	private OAuthRefreshTokenRepository oauthRefreshTokenRepository;
	@Autowired
	private ClientPlatformService deviceService;
	@Autowired
	private DigitalTwinDeviceService digitalTwinDeviceService;
	@Autowired
	private UserRepository userRepository;

	@Override
	public Optional<MasterUser> findUser(String userId) {
		return Optional.ofNullable(masterUserRepository.findByUserId(userId));
	}

	@Override
	public Optional<String> getSingleVerticalSchema(MasterUser user) {
		try {
			return Optional.of(verticalResolver.getSingleVerticalSchemaIfPossible(user));
		} catch (final TenantDBException e) {
			return Optional.empty();
		}

	}

	@Override
	public Optional<String> getSingleVertical(MasterUser user) {

		return Optional.ofNullable(verticalResolver.getSingleVerticalIfPossible(user).getName());

	}

	@Override
	public Optional<String> getSingleTenant(MasterUser user) {
		return Optional.ofNullable(verticalResolver.getTenantIfPossible(user).getName());

	}

	@Override
	public boolean belongsToSingleVertical(MasterUser user) {
		return verticalResolver.hasSingleTenantSchemaAssociated(user);
	}

	@Override
	public MasterUser mapFromUser(User user) {
		return converter.convert(user);
	}

	@Override
	public MasterUser create(MasterUser user) {
		return masterUserRepository.save(user);
	}

	@Override
	public MasterUser create(User user) {
		return create(mapFromUser(user));
	}

	@Override
	public List<Vertical> getVerticals(MasterUser user) {
		return verticalResolver.getVerticals(user);
	}

	@Override
	public List<MasterUser> getUsers() {
		return masterUserRepository.findAll();
	}

	@Override
	public Optional<MasterUserToken> getMasterTokenByToken(String token) {
		return Optional.ofNullable(masterUserTokenRepository.findByToken(token));
	}

	@Override
	public Optional<Vertical> getVertical(String vertical) {
		return Optional.ofNullable(verticalRepository.findByNameOrSchema(vertical));
	}

	@Override
	public Optional<String> getVerticalSchema(String vertical) {
		return Optional.ofNullable(verticalRepository.findSchemaByNameOrSchema(vertical));
	}

	@Override
	public List<Vertical> getAllVerticals() {
		return verticalRepository.findAll();
	}

	@Override
	public void createVertical(Vertical vertical) {
		Tenant tenant = new Tenant();
		tenant.setName(Tenant2SchemaMapper.defaultTenantName(vertical.getName()));
		tenant = tenantRepository.save(tenant);
		tenant.getVerticals().add(vertical);
		vertical.setSchema(Tenant2SchemaMapper.mapSchema(vertical.getName()));
		vertical.getTenants().add(tenant);
		verticalRepository.save(vertical);

	}

	@Override
	public List<Tenant> getTenantsForCurrentVertical() {
		final Optional<Vertical> v = getVertical(MultitenancyContextHolder.getVerticalSchema());
		if (v.isPresent()) {
			return new ArrayList<>(v.get().getTenants());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public void createTenant(Tenant tenant) {
		final Optional<Vertical> v = getVertical(MultitenancyContextHolder.getVerticalSchema());
		if (v.isPresent()) {
			tenant.getVerticals().add(v.get());
			tenant = tenantRepository.save(tenant);
			v.get().getTenants().add(tenant);
			verticalRepository.save(v.get());
		}

	}

	@Override
	public void createTenant(Tenant tenant, Vertical vertical) {
		final Optional<Vertical> v = getVertical(vertical.getName());
		if (v.isPresent()) {
			tenant.getVerticals().add(v.get());
			tenant = tenantRepository.save(tenant);
			v.get().getTenants().add(tenant);
			verticalRepository.save(v.get());
		}

	}

	@Override
	public void createTenant(Vertical vertical, Tenant tenant, User user) {
		if (vertical == null) {
			createTenant(tenant);
			MultitenancyContextHolder.setTenantName(tenant.getName());
			userService.registerRoleDeveloper(user);
		} else {
			final String currentVertical = MultitenancyContextHolder.getVerticalSchema();
			MultitenancyContextHolder.setVerticalSchema(vertical.getSchema());
			createTenant(tenant, vertical);
			MultitenancyContextHolder.setTenantName(tenant.getName());
			userService.registerRoleDeveloper(user);
			MultitenancyContextHolder.setVerticalSchema(currentVertical);
		}

	}

	@Override
	public List<MasterUser> getUsers(String tenant) {
		final Tenant t = tenantRepository.findByName(tenant);
		if (t != null) {
			return new ArrayList<>(t.getUsers());
		}
		return new ArrayList<>();
	}

	@Override
	public Optional<Tenant> getTenant(String tenant) {
		return Optional.ofNullable(tenantRepository.findByName(tenant));
	}

	@Override
	public void promoteRole(String vertical, Authentication auth) {
		final Optional<Vertical> v = getVertical(vertical);
		v.ifPresent(vt -> {
			final UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
			final User user = getUserOrReplicate(auth.getName(), vt, principal.getTenant());
			if (user != null) {
				final Collection<? extends GrantedAuthority> grantedAuthorities = Arrays
						.asList(new SimpleGrantedAuthority(user.getRole().getId()));
				final UserPrincipal prmotedPrincipal = new UserPrincipal(principal.getUsername(), "",
						grantedAuthorities, vt, principal.getTenant());
				final Authentication newAuthentication = new UsernamePasswordAuthenticationToken(prmotedPrincipal,
						principal.getPassword(), grantedAuthorities);
				SecurityContextHolder.getContext().setAuthentication(newAuthentication);
			}
			MultitenancyContextHolder.clear();

		});
	}

	private User getUserOrReplicate(String userId, Vertical vertical, String tenant) {
		MultitenancyContextHolder.setVerticalSchema(vertical.getSchema());
		final User user = userService.getUser(userId);
		if (user == null) {
			final Tenant t = tenantRepository.findByName(tenant);
			final Optional<User> singularity = t.getVerticals().stream().map(v -> {
				MultitenancyContextHolder.setVerticalSchema(v.getSchema());
				return userService.getUser(userId);
			}).filter(Objects::nonNull).findFirst();
			singularity.ifPresent(u -> {
				MultitenancyContextHolder.setVerticalSchema(vertical.getSchema());
				MultitenancyContextHolder.setTenantName(tenant);
				u = userService.saveExistingUser(u);
				try {
					userTokenService.generateToken(u);
				} catch (final GenericOPException e) {
					log.info("could not create token while replicating user");
				}
			});
			return singularity.orElse(null);
		} else {
			return user;
		}
	}

	@Override
	public List<Vertical> getVerticals(String user) {
		final MasterUser mUser = masterUserRepository.findByUserId(user);
		return this.getVerticals(mUser);
	}

	@Override
	public List<Tenant> getAllTenants() {
		return new ArrayList<>(tenantRepository.findAll());
	}

	@Override
	public void addTenant(String vertical, String tenant) {
		final Optional<Vertical> v = getVertical(vertical);
		v.ifPresent(vt -> {
			final Tenant t = tenantRepository.findByName(tenant);
			if (t != null) {
				vt.getTenants().add(t);
				verticalRepository.save(vt);
			}
		});

	}

	@Override
	public List<Integer> getAllDomainsPorts() {
		final String currentVertical = MultitenancyContextHolder.getVerticalSchema();
		final List<Integer> ports = getAllVerticals().stream().map(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			return flowDomainRepository.findAllDomainPorts();
		}).flatMap(List::stream).collect(Collectors.toList());
		MultitenancyContextHolder.setVerticalSchema(currentVertical);
		return ports;
	}

	@Override
	public List<Integer> getAllDomainServicePorts() {
		final String currentVertical = MultitenancyContextHolder.getVerticalSchema();
		final List<Integer> ports = getAllVerticals().stream().map(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			return flowDomainRepository.findAllServicePorts();
		}).flatMap(List::stream).collect(Collectors.toList());
		MultitenancyContextHolder.setVerticalSchema(currentVertical);
		return ports;
	}

	@Override
	public FlowDomain getFlowDomainByIdentification(String identification) {
		final String currentVertical = MultitenancyContextHolder.getVerticalSchema();
		final FlowDomain domain = getAllVerticals().stream().map(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			return flowDomainRepository.findByIdentification(identification);
		}).filter(Objects::nonNull).findFirst().orElse(null);
		MultitenancyContextHolder.setVerticalSchema(currentVertical);
		return domain;
	}

	@Override
	public MasterUser increaseFailedAttemp(String userId) {
		final MasterUser masterUser = masterUserRepository.findByUserId(userId);
		if (masterUser != null) {
			if (masterUser.getFailedAtemps() == null) {
				masterUser.setFailedAtemps(1);
			} else {
				masterUser.setFailedAtemps(masterUser.getFailedAtemps() + 1);
			}
			masterUserRepository.save(masterUser);
		}
		return masterUser;

	}

	@Override
	public MasterUser resetFailedAttemp(String userId) {
		final MasterUser masterUser = masterUserRepository.findByUserId(userId);
		if (masterUser != null) {
			masterUser.setFailedAtemps(0);
			masterUser.setLastLogin(new Date());
			masterUserRepository.save(masterUser);
		}
		return masterUser;

	}

	@Override
	public MasterUser setResetPass(String userId) {
		final MasterUser masterUser = masterUserRepository.findByUserId(userId);
		if (masterUser != null) {
			masterUser.setResetPass(new Date());
			masterUserRepository.save(masterUser);
		}
		return masterUser;

	}

	@Override
	public boolean isValidPass(String userId, String newPass, int numberLastEntriesToCheck) {
		if (numberLastEntriesToCheck < 0) {
			return true;
		}
		final List<MasterUserHistoric> list = masterUserHistoricRepository.findByMasterUserLastNvalues(userId,
				numberLastEntriesToCheck + 1);
		final JPAHAS256ConverterCustom converter = new JPAHAS256ConverterCustom();
		final String newPassConverted = converter.convertToDatabaseColumn(newPass);
		for (final MasterUserHistoric masterUserHistoric : list) {
			if (masterUserHistoric.getPassword().equals(newPassConverted)) {
				return false;
			}
		}
		return true;

	}

	@Override
	public List<MasterUser> getUsersForCurrentVertical() {
		return masterUserRepository.findByVertical(MultitenancyContextHolder.getVerticalSchema());
	}

	@Override
	public List<MasterUser> getActiveUsersForCurrentVertical(boolean active) {
		return masterUserRepository.findByVerticalAndActive(MultitenancyContextHolder.getVerticalSchema(), active);
	}

	@Override
	public MasterDeviceToken getMasterDeviceToken(String token) {
		return masterDeviceTokenRepository.findByTokenName(token);
	}

	@Transactional
	@Override
	public void changeUserTenant(String userId, String tenant) {
		final MasterUser user = masterUserRepository.findByUserId(userId);
		if (user != null) {
			getTenant(tenant).ifPresent(t -> {
				if (!user.getTenant().getName().equals(t.getName())) {
					user.setTenant(t);
					masterUserRepository.save(user);
					oauthAccessTokenRepository.findByUserName(userId).forEach(token -> {
						oauthRefreshTokenRepository.deleteById(token.getRefreshToken());
						oauthAccessTokenRepository.deleteById(token.getTokenId());
					});
					masterUserTokenRepository.findByUserId(userId).forEach(ut -> {
						ut.setTenant(t);
						masterUserTokenRepository.save(ut);
					});

					deviceService.getAllClientPlatformByCriteria(userId, null, null).forEach(d -> {
						d.getTokens().clear();
						deviceService.update(d);
					});

					digitalTwinDeviceService.getAllByUserId(userId).forEach(dtd -> {
						final MasterDigitalTwinDeviceToken dtdToken = masterDigitalTwinDeviceTokenRepository
								.findByTokenName(dtd.getDigitalKey());
						dtdToken.setTenant(tenant);
						masterDigitalTwinDeviceTokenRepository.save(dtdToken);
					});
				}
			});
		}
	}

	@Override
	@Transactional
	public void removeFromDefaultTenant(String userId, String tenant) {
		final MasterUser user = masterUserRepository.findByUserId(userId);
		if (user != null) {
			getTenant(tenant).ifPresent(t -> {
				if (t.getVerticals().stream()
						.noneMatch(v -> v.getSchema().equals(MultitenancyContextHolder.getVerticalSchema()))) {
					MultitenancyContextHolder.setIgnoreRemoveEvent(true);
					userRepository.deleteByUserId(userId);
				}
			});
		}

	}

	@Override
	public List<MasterUserToken> getAdminTokensForVerticals() {
		return masterUserTokenRepository.findAdminUsers();
	}

	@Override
	public MasterUser getUser(String userId) {
		return masterUserRepository.findByUserId(userId);
	}

	@Override
	public Vertical getVerticalFromSchema(String schema) {
		return verticalRepository.findByNameOrSchema(schema);
	}

	@Override
	public long countTenantUsers(String tenantName) {
		return tenantRepository.countUsersByTenantName(tenantName);
	}

	@Override
	public MasterUserLazy getUserLazy(String userId) {
		return masterUserRepository.findLazyByUserId(userId);
	}

	@Override
	public MasterUser updateLastLogin(String userId) {
		final MasterUser masterUser = masterUserRepository.findByUserId(userId);
		if (masterUser != null) {
			masterUser.setLastLogin(new Date());
			masterUserRepository.save(masterUser);
		}
		return masterUser;

	}

}
