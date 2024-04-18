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
package com.minsait.onesait.platform.business.services.opendata.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.business.services.opendata.OpenDataApi;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataRole;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataUser;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.RoleListResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.UserListResponse;

@Service
public class OpenDataUserServiceImpl implements OpenDataUserService {
	
	@Autowired
	private OpenDataApi api;
	
	@Override
	public List<OpenDataUser> getAllUsers(){
		UserListResponse userList = (UserListResponse) api.getOperation("user_list", null, UserListResponse.class);
		return userList.getResult();
	}
	
	@Override
	public List<OpenDataRole> getAllRoles(){
		RoleListResponse rolesList = (RoleListResponse) api.getOperation("member_roles_list", null, RoleListResponse.class);
		return rolesList.getResult();
	}

}
