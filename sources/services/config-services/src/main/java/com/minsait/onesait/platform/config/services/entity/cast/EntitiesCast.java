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
package com.minsait.onesait.platform.config.services.entity.cast;

import java.util.HashSet;
import java.util.Set;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserList;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;

public final class EntitiesCast {
	public static AppUser castAppUserList(AppUserList aul) {
		AppUser au = new AppUser();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		return au;
	}
	
	public static AppRole castAppRole(AppRoleList arl) {
		if(arl !=null) {
			AppRole ar = new AppRole();
			ar.setApp(null);
			Set<AppUser> sau = new HashSet<AppUser>();
			for(AppUserList aul : arl.getAppUsers()) {
				sau.add(castAppUserList(aul));
			}
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			return ar;
		}
		else {
			return null;
		}
	}
	
	public static App castAppList(AppList al, boolean onlyApp) {
		if(al !=null) {
			App app = new App();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setUser(al.getUser());
			Set<AppRole> sar = new HashSet<AppRole>();
			if(!onlyApp) {
				for(AppRoleList arl: al.getAppRoles()) {
					sar.add(castAppRole(arl));
					
				}
				app.setAppRoles(sar);
				Set<AppList> sal = al.getChildApps();
				
				Set<App> sa = new HashSet<App>();
				for(AppList ali : sal) {
					sa.add(castAppList(ali,onlyApp));
				}
				app.setChildApps(sa);
			}
			return app;
		}
		else {
			return null;
		}
	}
	
	public static Project castProjectList(ProjectList pl, boolean onlyApp) {
		if(pl !=null) {
			Project p = new Project();
			p.setId(pl.getId());
			p.setIdentification(pl.getIdentification());
			p.setDescription(pl.getDescription());
			p.setCreatedAt(pl.getCreatedAt());
			p.setUpdatedAt(pl.getUpdatedAt());
			p.setApp(castAppList(pl.getApp(),onlyApp));
			p.setUser(pl.getUser());
			p.setUsers(pl.getUsers());
			p.setType(pl.getType());
			p.setWebProject(pl.getWebProject());
			return p;
		}
		else {
			return null;
		}
	}
}
