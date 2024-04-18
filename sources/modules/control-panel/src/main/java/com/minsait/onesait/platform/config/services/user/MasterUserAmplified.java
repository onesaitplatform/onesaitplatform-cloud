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
package com.minsait.onesait.platform.config.services.user;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MasterUserAmplified extends UserAmplified {
	@JsonInclude(Include.NON_NULL)
	Integer failedAtemps;
	@JsonInclude(Include.NON_NULL)
	String lastLogin;
	@JsonInclude(Include.NON_NULL)
	String lastPswdUpdate;
	@JsonInclude(Include.NON_NULL)
	String resetPass;

	public MasterUserAmplified(com.minsait.onesait.platform.multitenant.config.model.MasterUser user) {
		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
		setUsername(user.getUserId());
		setMail(user.getEmail());
		setFullName(user.getFullName());
		failedAtemps = user.getFailedAtemps();
		if (user.getLastLogin() != null && user.getLastLogin().toString().length() > 0)
			lastLogin = dateFormat.format(user.getLastLogin());
		if (user.getLastPswdUpdate() != null && user.getLastPswdUpdate().toString().length() > 0)
			lastPswdUpdate = dateFormat.format(user.getLastPswdUpdate());
		if (user.getResetPass() != null && user.getResetPass().toString().length() > 0)
			resetPass = dateFormat.format(user.getResetPass());
	}

	public MasterUserAmplified(com.minsait.onesait.platform.multitenant.config.model.MasterUserLazy user) {
		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
		setUsername(user.getUserId());
		setMail(user.getEmail());
		setFullName(user.getFullName());
		failedAtemps = user.getFailedAtemps();
		if (user.getLastLogin() != null && user.getLastLogin().toString().length() > 0)
			lastLogin = dateFormat.format(user.getLastLogin());
		if (user.getLastPswdUpdate() != null && user.getLastPswdUpdate().toString().length() > 0)
			lastPswdUpdate = dateFormat.format(user.getLastPswdUpdate());
		if (user.getResetPass() != null && user.getResetPass().toString().length() > 0)
			resetPass = dateFormat.format(user.getResetPass());
	}

	public MasterUserAmplified(Object muser) {
		final DateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
		if (((Object[]) muser)[0] != null && ((Object[]) muser)[0].toString().length() > 0) {
			this.setUsername(((Object[]) muser)[0].toString());
		}
		if (((Object[]) muser)[1] != null && ((Object[]) muser)[1].toString().length() > 0) {
			this.setMail(((Object[]) muser)[1].toString());
		}
		if (((Object[]) muser)[2] != null && ((Object[]) muser)[2].toString().length() > 0) {
			this.setFullName(((Object[]) muser)[2].toString());
		}
		if (((Object[]) muser)[3] != null && ((Object[]) muser)[3].toString().length() > 0) {
			this.setRole(((Object[]) muser)[3].toString());
		}
		try {
			if (((Object[]) muser)[4] != null && ((Object[]) muser)[4].toString().length() > 0) {
				this.setCreated(dateFormat.format(originalDateFormat.parse(((Object[]) muser)[4].toString())));
			}
			if (((Object[]) muser)[5] != null && ((Object[]) muser)[5].toString().length() > 0) {
				this.setUpdated(dateFormat.format(originalDateFormat.parse(((Object[]) muser)[5].toString())));
			}
			if (((Object[]) muser)[6] != null && ((Object[]) muser)[6].toString().length() > 0) {
				this.setDeleted(dateFormat.format(originalDateFormat.parse(((Object[]) muser)[6].toString())));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (((Object[]) muser)[7] != null) {
			this.setActive((Boolean)((Object[]) muser)[7]);
		}
		if (((Object[]) muser)[8] != null && ((Object[]) muser)[8].toString().length() > 0) {
			this.setExtraFields(((Object[]) muser)[8].toString());
		}	
		if (((Object[]) muser)[9] != null && ((Object[]) muser)[9].toString().length() > 0) {
			this.setAvatar(((byte[]) ((Object[]) muser)[9]));
		}	
		if (((Object[]) muser)[10] != null && ((Object[]) muser)[10].toString().length() > 0) {
			this.setTenant(((Object[]) muser)[10].toString());
		}	
		if (((Object[]) muser)[11] != null) {
			this.failedAtemps =(Integer)((Object[]) muser)[11];
		}
		try {
			if (((Object[]) muser)[12] != null && ((Object[]) muser)[12].toString().length() > 0) {
				this.lastLogin = dateFormat.format(originalDateFormat.parse(((Object[]) muser)[12].toString()));
			}	
			if (((Object[]) muser)[13] != null && ((Object[]) muser)[13].toString().length() > 0) {
				this.lastPswdUpdate = dateFormat.format(originalDateFormat.parse(((Object[]) muser)[13].toString()));
			}	
			if (((Object[]) muser)[14] != null && ((Object[]) muser)[14].toString().length() > 0) {
				this.resetPass = dateFormat.format(originalDateFormat.parse(((Object[]) muser)[14].toString()));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
