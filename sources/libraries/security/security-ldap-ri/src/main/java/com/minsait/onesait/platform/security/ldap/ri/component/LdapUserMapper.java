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
package com.minsait.onesait.platform.security.ldap.ri.component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import com.minsait.onesait.platform.config.model.User;

public class LdapUserMapper implements AttributesMapper<User> {

	@Override
	public User mapFromAttributes(Attributes attrs) throws NamingException {
		final User user = new User();
		user.setUserId((String) attrs.get("uid").get());
		user.setFullName((String) attrs.get("cn").get());

		final Attribute sn = attrs.get("sn");
		if (sn != null) {
			user.setFullName(user.getFullName() + " " + (String) sn.get());
		}
		final Attribute mail = attrs.get("mail");
		if (mail != null) {
			user.setEmail((String) mail.get());
		}
		return user;
	}

}
