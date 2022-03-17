	
/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

}
