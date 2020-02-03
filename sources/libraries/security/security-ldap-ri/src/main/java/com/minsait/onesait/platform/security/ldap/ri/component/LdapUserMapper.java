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
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

import com.minsait.onesait.platform.config.model.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class LdapUserMapper implements AttributesMapper<User> {

	private static final String DEFAULT_MAIL_SUFFIX = "@ldap.com";

	private String userIdAtt;
	private String userMailAtt;
	private String userCnAtt;

	@Override
	public User mapFromAttributes(Attributes attrs) throws NamingException {
		final User user = new User();
		user.setUserId((String) attrs.get(userIdAtt).get());
		user.setFullName((String) attrs.get(userCnAtt).get());
		if (attrs.get(userMailAtt) != null)
			user.setEmail((String) attrs.get(userMailAtt).get());
		else
			user.setEmail(user.getUserId() + DEFAULT_MAIL_SUFFIX);
		return user;
	}

}