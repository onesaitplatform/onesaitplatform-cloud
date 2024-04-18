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
package com.minsait.onesait.platform.security.ldap.ri.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import com.minsait.onesait.platform.config.model.User;

public class LdapGroupMemberMapper implements AttributesMapper<List<User>> {

	private String member;

	public LdapGroupMemberMapper(String member) {
		this.member = member;
	}

	@Override
	public List<User> mapFromAttributes(Attributes attributes) throws NamingException {
		final List<User> users = new ArrayList<>();

		final NamingEnumeration<?> enumeration = attributes.get(member).getAll();
		while (enumeration.hasMoreElements()) {
			final String next = (String) enumeration.next();
			final User user = new User();
			final HashMap<String, String> mapFromAttr = (HashMap<String, String>) Arrays.asList(next.split(","))
					.stream().filter(s -> s.contains("uid")).map(s -> s.split("="))
					.collect(Collectors.toMap(e -> e[0], e -> e[1]));
			if (null != mapFromAttr.get("uid")) {
				user.setUserId(mapFromAttr.get("uid"));
				users.add(user);
			}
		}
		return users;
	}

}
