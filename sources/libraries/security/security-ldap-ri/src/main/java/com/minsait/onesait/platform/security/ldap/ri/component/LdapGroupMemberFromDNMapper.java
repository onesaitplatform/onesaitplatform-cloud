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
package com.minsait.onesait.platform.security.ldap.ri.component;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class LdapGroupMemberFromDNMapper implements AttributesMapper<List<String>> {

	private String member;

	public LdapGroupMemberFromDNMapper(String member) {
		this.member = member;
	}

	@Override
	public List<String> mapFromAttributes(Attributes attributes) throws NamingException {
		final List<String> membersDN = new ArrayList<>();

		final NamingEnumeration<?> enumeration = attributes.get(member).getAll();
		while (enumeration.hasMoreElements()) {
			final String next = (String) enumeration.next();
			membersDN.add(next);
		}
		return membersDN;
	}

}
