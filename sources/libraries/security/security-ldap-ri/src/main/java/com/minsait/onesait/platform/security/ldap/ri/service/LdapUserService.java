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
package com.minsait.onesait.platform.security.ldap.ri.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.security.ldap.ri.component.LdapGroupMemberFromDNMapper;
import com.minsait.onesait.platform.security.ldap.ri.component.LdapGroupMemberMapper;
import com.minsait.onesait.platform.security.ldap.ri.component.LdapGroupNameMapper;
import com.minsait.onesait.platform.security.ldap.ri.component.LdapUserMapper;
import com.minsait.onesait.platform.security.ldap.ri.config.LdapConfig;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "ldap")
public class LdapUserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private UserTokenRepository userTokenRepository;

	@Autowired
	@Qualifier(LdapConfig.LDAP_TEMPLATE_BASE)
	private LdapTemplate ldapTemplateBase;
	@Autowired
	@Qualifier(LdapConfig.LDAP_TEMPLATE_NO_BASE)
	private LdapTemplate ldapTemplateNoBase;

	@Value("${ldap.defaultRole}")
	private String defaultRole;
	@Value("${onesaitplatform.authentication.default_password:changeIt9900!}")
	private String defaultPassword;
	@Value("${ldap.attributesMap.userId}")
	private String userIdAtt;
	@Value("${ldap.attributesMap.mail}")
	private String userMailAtt;
	@Value("${ldap.attributesMap.cn}")
	private String userCnAtt;
	@Value("${ldap.attributesMap.groupOfNames}")
	private String groupOfNamesAtt;
	@Value("${ldap.rolesmemberattribute}")
	private String memberAtt;

	@Value("${ldap.platformRolesGroup.administrator:null}")
	private String administratorDn;
	@Value("${ldap.platformRolesGroup.datascientist:null}")
	private String datascientistDn;
	@Value("${ldap.platformRolesGroup.dataviewer:null}")
	private String dataviewerDn;
	@Value("${ldap.platformRolesGroup.developer:null}")
	private String developerDn;
	@Value("${ldap.platformRolesGroup.devops:null}")
	private String devopsDn;
	@Value("${ldap.platformRolesGroup.operations:null}")
	private String operationsDn;
	@Value("${ldap.platformRolesGroup.partner:null}")
	private String partnerDn;
	@Value("${ldap.platformRolesGroup.platformAdmin:null}")
	private String platformAdminDn;
	@Value("${ldap.platformRolesGroup.sysAdmin:null}")
	private String sysAdminDn;
	@Value("${ldap.platformRolesGroup.user:null}")
	private String userDn;

	private static final String MEMBER_OF_GROUP = "member";
	private static final String OBJECT_CLASS_STR = "objectClass";
	private static final String PERSON_STR = "person";

	public User createUser(User user, String password, List<String> groups) {
		user.setPassword(password);
		user.setActive(true);

		if (groups != null && groups.size() > 0) {
			user.setRole(getRole(groups));
		} else {
			user.setRole(roleRepository.findById(defaultRole).orElse(null));
		}

		log.debug("Importing user {} from LDAP server", user.getUserId());
		User createdUser = userRepository.save(user);
		try {
			generateToken(user);
		} catch (final Exception e) {
			log.debug("Error creating userToken");
		}

		return createdUser;
	}

	public void updateUserRole(User user, List<String> groups) {
		final Role currentRole = user.getRole();
		Role ldapRole;
		if (groups != null && groups.size() > 0) {
			ldapRole = getRole(groups);
		} else {
			ldapRole = roleRepository.findById(defaultRole).orElse(null);
		}

		if (!currentRole.getId().equals(ldapRole.getId())) {
			log.debug("Updating user {} from LDAP server", user.getUserId());
			user.setRole(ldapRole);
			userRepository.save(user);
		}
	}

	public void createUser(String userId, String dn) {
		final AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(OBJECT_CLASS_STR, PERSON_STR));
		filter.and(new EqualsFilter(userIdAtt, userId));
		List<User> matches;
		try {
			matches = ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filter.encode(),
					new LdapUserMapper(userIdAtt, userMailAtt, userCnAtt));
		} catch (final RuntimeException e) {
			log.error("Could not map user from LDAP");
			throw new RuntimeException("Could not import user from LDAP");
		}

		if (matches.isEmpty()) {// Es posible que el usuario este en otro grupo y en el rol solo se tenga una
								// referencia a su DN
			try {
				final AndFilter filter2 = new AndFilter();
				filter2.and(new EqualsFilter(OBJECT_CLASS_STR, PERSON_STR));
				filter2.and(new EqualsFilter(userIdAtt, userId));
				matches = ldapTemplateBase.search(LdapUtils.emptyLdapName(), filter2.encode(),
						new LdapUserMapper(userIdAtt, userMailAtt, userCnAtt));
			} catch (final Exception e) {
				log.error("Could not map user from LDAP", e);
			}
		}

		if (matches.isEmpty()) {
			log.error("User not found in LDAP server, it may not exist or it may not be objectClass=person");
			throw new UsernameNotFoundException("User not found in LDAP server");
		}
		final User user = matches.get(0);
		user.setUserId(userId);

		final List<String> groups = ldapTemplateBase
				.search(LdapUtils.emptyLdapName(), filter.encode(), new AttributesMapper<List<String>>() {
					@Override
					public List<String> mapFromAttributes(Attributes attributes) throws NamingException {
						final Enumeration<String> enMember = (Enumeration<String>) attributes.get(memberAtt).getAll();
						return Collections.list(enMember);
					}
				}).get(0);

		createUser(user, defaultPassword, groups);
	}

	public List<User> getAllUsers() {
		final Filter filter = new EqualsFilter(OBJECT_CLASS_STR, PERSON_STR);
		return ldapTemplateBase.search(LdapUtils.emptyLdapName(), filter.encode(),
				new LdapUserMapper(userIdAtt, userMailAtt, userCnAtt));
	}

	public List<User> getAllUsers(String dn) {
		if (StringUtils.isEmpty(dn))
			return getAllUsers();
		final Filter filter = new EqualsFilter(OBJECT_CLASS_STR, PERSON_STR);
		return ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filter.encode(),
				new LdapUserMapper(userIdAtt, userMailAtt, userCnAtt));
	}

	public List<User> getAllUsersFromGroup(String dn, String cn) {
		final AndFilter filterAnd = new AndFilter();
		filterAnd.and(new EqualsFilter(OBJECT_CLASS_STR, groupOfNamesAtt));
		filterAnd.and(new EqualsFilter(userCnAtt, cn));
		final List<List<User>> users = ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filterAnd.encode(),
				new LdapGroupMemberMapper(MEMBER_OF_GROUP));

		if (!users.isEmpty() && users.get(0).isEmpty()) {// en el atributo member es posible que tengamos el DN del
															// usuario en vez del uid (Esto pasa en Logrono)
			final List<List<String>> membersDn = ldapTemplateNoBase.search(LdapUtils.newLdapName(dn),
					filterAnd.encode(), new LdapGroupMemberFromDNMapper(MEMBER_OF_GROUP));

			final List<User> usersInGroup = new ArrayList<>();
			membersDn.get(0).stream().forEach(member -> {
				final List<User> currentUser = getAllUsers(member);
				if (currentUser != null && !currentUser.isEmpty()) {
					usersInGroup.add(getAllUsers(member).get(0));
				}
			});

			return usersInGroup;

		}

		if (!users.isEmpty())
			return users.stream().flatMap(List::stream).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	public List<String> getAllGroups() {
		final Filter filter = new EqualsFilter(OBJECT_CLASS_STR, groupOfNamesAtt);
		return ldapTemplateBase.search(LdapUtils.emptyLdapName(), filter.encode(), new LdapGroupNameMapper());

	}

	public List<String> getAllGroups(String dn) {
		if (StringUtils.isEmpty(dn))
			return getAllGroups();
		final Filter filter = new EqualsFilter(OBJECT_CLASS_STR, groupOfNamesAtt);
		return ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filter.encode(), new LdapGroupNameMapper());

	}

	private Role getRole(List<String> groups) {
		if (null != administratorDn && groups.contains(administratorDn)) {
			return roleRepository.findById("ROLE_ADMINISTRATOR").orElse(null);
		} else if (null != datascientistDn && groups.contains(datascientistDn)) {
			return roleRepository.findById("ROLE_DATASCIENTIST").orElse(null);
		} else if (null != dataviewerDn && groups.contains(dataviewerDn)) {
			return roleRepository.findById("ROLE_DATAVIEWER").orElse(null);
		} else if (null != developerDn && groups.contains(developerDn)) {
			return roleRepository.findById("ROLE_DEVELOPER").orElse(null);
		} else if (null != devopsDn && groups.contains(devopsDn)) {
			return roleRepository.findById("ROLE_DEVOPS").orElse(null);
		} else if (null != operationsDn && groups.contains(operationsDn)) {
			return roleRepository.findById("ROLE_OPERATIONS").orElse(null);
		} else if (null != partnerDn && groups.contains(partnerDn)) {
			return roleRepository.findById("ROLE_PARTNER").orElse(null);
		} else if (null != platformAdminDn && groups.contains(platformAdminDn)) {
			return roleRepository.findById("ROLE_PLATFORM_ADMIN").orElse(null);
		} else if (null != sysAdminDn && groups.contains(sysAdminDn)) {
			return roleRepository.findById("ROLE_SYS_ADMIN").orElse(null);
		} else if (null != userDn && groups.contains(userDn)) {
			return roleRepository.findById("ROLE_USER").orElse(null);
		} else {
			return roleRepository.findById(defaultRole).orElse(null);
		}

	}

	private UserToken generateToken(User user) throws GenericOPException {
		UserToken userToken = new UserToken();
		if (user.getUserId() != null) {
			userToken.setUser(user);
			userToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
			if (this.userTokenRepository.findByToken(userToken.getToken()) == null) {
				userToken = this.userTokenRepository.save(userToken);
			} else {
				throw new GenericOPException("Token with value " + userToken.getToken() + " already exists");
			}
		}
		return userToken;
	}
}