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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
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

	public static final String MEMBER_OF_GROUP = "member";

	private static final String OBJECT_CLASS_STR = "objectClass";
	private static final String PERSON_STR = "person";
	private static final String GROUP_OF_NAMES = "groupOfNames";

	public User createUser(User user, String password) {
		user.setPassword(password);
		user.setActive(true);
		user.setRole(roleRepository.findById(defaultRole));
		log.debug("Importing user {} from LDAP server", user.getUserId());
		return userRepository.save(user);
	}

	public void createUser(String userId, String dn) {
		final AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter(OBJECT_CLASS_STR, PERSON_STR));
		filter.and(new EqualsFilter(userIdAtt, userId));
		final List<User> matches;
		try {
			matches = ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filter.encode(),
					new LdapUserMapper(userIdAtt, userMailAtt, userCnAtt));
		} catch (final RuntimeException e) {
			log.error("Could not map user from LDAP");
			throw new RuntimeException("Could not import user from LDAP");
		}

		if (matches.isEmpty()) {
			log.error("User not found in LDAP server, it may not exist or it may not be objectClass=person");
			throw new UsernameNotFoundException("User not found in LDAP server");
		}
		final User user = matches.get(0);
		user.setUserId(userId);
		createUser(user, defaultPassword);
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
		filterAnd.and(new EqualsFilter(OBJECT_CLASS_STR, GROUP_OF_NAMES));
		filterAnd.and(new EqualsFilter(userCnAtt, cn));
		final List<List<User>> users = ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filterAnd.encode(),
				new LdapGroupMemberMapper(MEMBER_OF_GROUP));
		if (!users.isEmpty())
			return users.stream().flatMap(List::stream).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	public List<String> getAllGroups() {
		final Filter filter = new EqualsFilter(OBJECT_CLASS_STR, GROUP_OF_NAMES);
		return ldapTemplateBase.search(LdapUtils.emptyLdapName(), filter.encode(), new LdapGroupNameMapper());

	}

	public List<String> getAllGroups(String dn) {
		if (StringUtils.isEmpty(dn))
			return getAllGroups();
		final Filter filter = new EqualsFilter(OBJECT_CLASS_STR, GROUP_OF_NAMES);
		return ldapTemplateNoBase.search(LdapUtils.newLdapName(dn), filter.encode(), new LdapGroupNameMapper());

	}
}