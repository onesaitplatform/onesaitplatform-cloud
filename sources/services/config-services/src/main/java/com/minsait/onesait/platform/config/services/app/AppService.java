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
package com.minsait.onesait.platform.config.services.app;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppRoleListOauth;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserListOauth;
import com.minsait.onesait.platform.config.services.app.dto.AppCreateDTO;
import com.minsait.onesait.platform.config.services.app.dto.Realm;

public interface AppService {

	public List<App> getAllApps();

	public List<AppList> getAllAppsList();

	public List<App> getAppsByUser(String sessionUserId, String identification);

	public List<AppList> getAppsByUserList(String sessionUserId, String identification);

	public List<AppRole> getAllRoles();

	public List<AppRoleListOauth> getAllRolesList();

	public void createApp(App app);

	public App getById(String id);

	public void updateApp(AppCreateDTO appDTO);

	public void updateApp(AppList appDTO);

	public void deleteApp(App app);

	public String createUserAccess(String appId, String userId, String roleId);

	public void deleteUserAccess(String appUserId);

	public void deleteUserAccess(String userId, String roleName, String appIdentification);

	public void removeAuthorizations(String userId, String appIdentification);

	public Set<AppUser> findUsersByRole(AppRole role);

	public Map<String, String> createAssociation(String fatherRoleId, String childRoleId);

	public void createAssociation(String fatherRoleName, String childRoleName, String fatherAppId, String childAppId);

	public void deleteAssociation(String fatherRoleName, String childRoleName, String fatherAppId, String childAppId);

	public AppRole getByRoleNameAndApp(String roleName, App app);

	public AppRoleList getByRoleNameAndAppList(String roleName, AppList app);

	public AppRoleListOauth getByRoleNameAndAppListOauth(String roleName, AppList app);

	public void updateApp(App app);

	public void deleteRole(AppRole role);

	public AppRole findRole(String roleId);

	public Realm getRealmByAppIdentification(String realmId);

	public boolean isUserInApp(String userId, String realmId);

	public App getAppByIdentification(String identification);

	public AppList getAppListByIdentification(String identification);

	public AppList getAppListById(String id);

	public List<AppUserListOauth> getAppUsersByUserIdAndApp(String userId, String appIdentification);
	
	public List<AppUserListOauth> getAppUsersByApp(String appIdentification);

	public List<AppUserListOauth> getAppUsersByUserIdAndRoleAndApp (String userId, String role, String appIdentification);

	public List<AppUserListOauth> getAppUsersByAppAndUserIdLike(String appIdentification, String userIdLike);

	public List<AppRoleListOauth> getAppRolesListOauth(String appIdentification);

	long countUsersInApp(String appIdentification);

}
