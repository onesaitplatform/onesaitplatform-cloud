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
package com.minsait.onesait.platform.config.model.listener;

import java.util.Date;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.transaction.Transactional;

import com.minsait.onesait.platform.config.converters.JPAHAS256ConverterCustom;
import com.minsait.onesait.platform.config.converters.MasterUserConverter;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.MasterDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterDigitalTwinDeviceToken;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserHistoric;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserToken;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.TenantLazy;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.IoTSessionRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterDigitalTwinDeviceTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserHistoricRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepositoryLazy;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.TenantRepository;
import com.minsait.onesait.platform.multitenant.config.repository.TenantRepositoryLazy;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.multitenant.exception.TenantDBException;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityListener {

	private static MasterUserRepository masterUserRepository;

	private static MasterUserRepositoryLazy masterUserRepositoryLazy;

	private static MasterUserTokenRepository masterUserTokenRepository;

	private static MasterUserHistoricRepository masterUserHistoricRepository;

	private static MasterDeviceTokenRepository masterDeviceTokenRepository;

	private static TenantRepository tenantRepository;

	private static TenantRepositoryLazy tenantRepositoryLazy;

	private static VerticalRepository verticalRepository;

	private static MasterDigitalTwinDeviceTokenRepository masterDigitalTwinDeviceTokenRepository;

	private static MasterUserConverter converter;

	private static IoTSessionRepository sessionRepository;

	public static void initialize() {
		masterUserHistoricRepository = BeanUtil.getBean(MasterUserHistoricRepository.class);
		masterUserRepository = BeanUtil.getBean(MasterUserRepository.class);
		masterUserTokenRepository = BeanUtil.getBean(MasterUserTokenRepository.class);
		verticalRepository = BeanUtil.getBean(VerticalRepository.class);
		tenantRepository = BeanUtil.getBean(TenantRepository.class);
		masterDeviceTokenRepository = BeanUtil.getBean(MasterDeviceTokenRepository.class);
		converter = BeanUtil.getBean(MasterUserConverter.class);
		masterDigitalTwinDeviceTokenRepository = BeanUtil.getBean(MasterDigitalTwinDeviceTokenRepository.class);
		sessionRepository = BeanUtil.getBean(IoTSessionRepository.class);
		masterUserRepositoryLazy = BeanUtil.getBean(MasterUserRepositoryLazy.class);
		tenantRepositoryLazy = BeanUtil.getBean(TenantRepositoryLazy.class);
	}

	@PrePersist
	public void createUser(Object o) throws TenantDBException {
		if (o instanceof User) {
			final User user = (User) o;
			if (null != masterUserRepository.findLazyByUserId(user.getUserId())) {
				log.warn("Could not add Master user{}, already exists", user.getUserId());
			} else {
				final MasterUserLazy master = converter.convertToLazyNoRaw(user);
				if (master.getTenant() == null) {
					master.setTenant(getCurrentTenantLazy());
				}
				final Date createDate = new Date();
				master.setFailedAtemps(0);
				master.setLastPswdUpdate(createDate);
				master.setResetPass(null);
				master.setLastLogin(createDate);
				masterUserRepositoryLazy.save(master);
				final MasterUserHistoric masterUserHistoric = MasterUserHistoric.builder()
						.masterUser(MasterUser.builder().userId(user.getUserId()).build()).password(user.getPassword())
						.build();
				masterUserHistoricRepository.save(masterUserHistoric);
			}
		}
	}

	@PreUpdate
	public void updateUser(Object o) {
		if (o instanceof User) {
			final User user = (User) o;
			MasterUserLazy master = converter.convertToLazyNoRaw(user);
			master.setTenant(masterUserRepositoryLazy.findByUserId(user.getUserId()).getTenant());
			final JPAHAS256ConverterCustom shaConverter = new JPAHAS256ConverterCustom();

			final String newPass = shaConverter.convertToDatabaseColumn(user.getRawPassword());

			final boolean changePass = !masterUserRepository.findByUserId(user.getUserId()).getPassword()
					.equals(newPass);
			if (changePass) {
				final Date createDate = new Date();
				master.setFailedAtemps(0);
				master.setLastPswdUpdate(createDate);
				master.setResetPass(null);
			}
			master = masterUserRepositoryLazy.save(master);
			if (changePass) {
				final MasterUserHistoric masterUserHistoric = MasterUserHistoric.builder()
						.masterUser(converter.convert(user)).password(user.getPassword()).build();
				masterUserHistoricRepository.save(masterUserHistoric);
			}

		}
	}

	@PostRemove
	@Transactional
	public void removeMaster(Object object) {
		if (!MultitenancyContextHolder.isIgnoreRemoveEvent()) {
			if (object instanceof UserToken) {
				masterUserTokenRepository.deleteByToken(((UserToken) object).getToken());
				log.debug("Created Master User Token");
			} else if (object instanceof Token) {
				sessionRepository.findByClientPlatform(((Token) object).getClientPlatform().getIdentification())
				.forEach(s -> sessionRepository.deleteBySessionKey(s.getSessionKey()));
				masterDeviceTokenRepository.deleteByTokenName(((Token) object).getTokenName());
			} else if (object instanceof User) {
				// avoid deleting master users from non authorized verticals
				final User user = (User) object;
				final MasterUserLazy mUser = masterUserRepository.findLazyByUserId(user.getUserId());
				if (mUser != null) {
					if (mUser.getTenant().getVerticals().size() == 1) {
						masterUserRepositoryLazy.deleteByUserId(user.getUserId());
					}
				}
			} else if (object instanceof DigitalTwinDevice) {
				masterDigitalTwinDeviceTokenRepository.deleteByTokenName(((DigitalTwinDevice) object).getDigitalKey());
			}
		}
	}

	@PostPersist
	public void replicateCreation(Object object) {
		if (object instanceof UserToken) {
			final UserToken userToken = (UserToken) object;
			if (masterUserTokenRepository.findByToken(userToken.getToken()) == null) {
				createMasterUserToken(userToken);
				log.debug("Created Master User Token");
			}

		} else if (object instanceof Token) {
			final Token token = (Token) object;
			if (masterDeviceTokenRepository.findByTokenName(token.getTokenName()) == null) {
				createMasterDeviceToken(token);
				log.debug("Created Master Device Token");
			}
		} else if (object instanceof DigitalTwinDevice) {
			final DigitalTwinDevice device = (DigitalTwinDevice) object;
			if (masterDigitalTwinDeviceTokenRepository.findByTokenName(device.getDigitalKey()) == null) {
				createMasterDigitalTwinDeviceToken(device);
				log.debug("Created Digital Twin device");
			}
		}
	}

	private void createMasterDigitalTwinDeviceToken(DigitalTwinDevice digitalTwinDevice) {
		try {
			final MasterDigitalTwinDeviceToken token = MasterDigitalTwinDeviceToken.builder()
					.tokenName(digitalTwinDevice.getDigitalKey()).tenant(getCurrentTenant().getName())
					.verticalSchema(getCurrentVertical().getSchema()).build();
			masterDigitalTwinDeviceTokenRepository.save(token);
		} catch (final Exception e) {
			log.warn("Could not create Master Digital Twin Device", e);
		}
	}

	private void createMasterDeviceToken(Token token) {
		try {
			final MasterDeviceToken masterToken = MasterDeviceToken.builder().tenant(getCurrentTenant().getName())
					.verticalSchema(getCurrentVertical().getSchema()).tokenName(token.getTokenName()).build();
			masterDeviceTokenRepository.save(masterToken);
		} catch (final Exception e) {
			log.warn("Could not create Master Device token", e);
		}
	}

	private void createMasterUserToken(UserToken token) {
		try {
			final MasterUserToken masterToken = MasterUserToken.builder().token(token.getToken())
					.masterUser(MasterUser.builder().userId(token.getUser().getUserId()).build())
					.tenant(getCurrentTenant()).vertical(getCurrentVertical()).build();
			masterUserTokenRepository.save(masterToken);
		} catch (final Exception e) {
			log.warn("Could not create Master token", e);
		}
	}

	private Vertical getCurrentVertical() {
		return verticalRepository.findBySchema(MultitenancyContextHolder.getVerticalSchema());
	}

	private Tenant getCurrentTenant() {
		return tenantRepository.findByName(MultitenancyContextHolder.getTenantName());
	}

	private TenantLazy getCurrentTenantLazy() {
		return tenantRepositoryLazy.findLazyByName(MultitenancyContextHolder.getTenantName());
	}

}
