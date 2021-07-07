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
package com.minsait.onesait.platform.config.converters;

import static com.minsait.onesait.platform.config.converters.JPAHAS256ConverterCustom.STORED_FLAG;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.multitenant.config.model.MasterUser;
import com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy;

@Component
public class MasterUserConverter implements Converter<User, MasterUser> {
	JPAHAS256ConverterCustom shaConverter = new JPAHAS256ConverterCustom();
	@Override
	public MasterUser convert(User user) {
		return MasterUser.builder().email(user.getEmail()).userId(user.getUserId()).password(user.getRawPassword())
				.extraFields(user.getExtraFields()).fullName(user.getFullName()).active(user.isActive()).build();
	}

	public MasterUser convertToMasterUserNoRaw(User user) {
		return MasterUser.builder().email(user.getEmail()).userId(user.getUserId()).password(STORED_FLAG+shaConverter.convertToDatabaseColumn(user.getPassword()))
				.extraFields(user.getExtraFields()).fullName(user.getFullName()).active(user.isActive()).build();
	}

	public MasterUserLazy convertToLazy(User user) {
		return MasterUserLazy.builder().email(user.getEmail()).userId(user.getUserId()).password(user.getRawPassword())
				.extraFields(user.getExtraFields()).fullName(user.getFullName()).active(user.isActive()).build();
	}


	public MasterUserLazy convertToLazyNoRaw(User user) {
		return MasterUserLazy.builder().email(user.getEmail()).userId(user.getUserId()).password(STORED_FLAG+shaConverter.convertToDatabaseColumn(user.getPassword()))
				.extraFields(user.getExtraFields()).fullName(user.getFullName()).active(user.isActive()).build();
	}

}
