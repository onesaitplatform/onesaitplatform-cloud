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
import com.minsait.onesait.platform.config.model.AppChild;
import com.minsait.onesait.platform.config.model.AppChildExport;
import com.minsait.onesait.platform.config.model.AppExport;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleChild;
import com.minsait.onesait.platform.config.model.AppRoleChildExport;
import com.minsait.onesait.platform.config.model.AppRoleExport;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserChild;
import com.minsait.onesait.platform.config.model.AppUserChildExport;
import com.minsait.onesait.platform.config.model.AppUserExport;
import com.minsait.onesait.platform.config.model.AppUserList;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessList;

public final class EntitiesCast {
	public static AppUser castAppUserList(AppUserList aul, AppRole ar) {
		AppUser au = new AppUser();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppUserChild castAppUserChild(AppUser aul, AppRoleChild ar) {
		AppUserChild au = new AppUserChild();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppUser castAppUser(AppUserChild aul, AppRole ar) {
		AppUser au = new AppUser();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppUserExport castAppUserExport(AppUserChildExport aul, AppRoleExport ar) {
		AppUserExport au = new AppUserExport();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppRole castAppRoleList(AppRoleList arl) {
		if (arl != null) {
			AppRole ar = new AppRole();
			ar.setApp(null);
			Set<AppUser> sau = new HashSet<AppUser>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (AppUserList aul : arl.getAppUsers()) {
				sau.add(castAppUserList(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static AppRoleChild castAppRoleChild(AppRole arl) {
		if (arl != null) {
			AppRoleChild ar = new AppRoleChild();
			ar.setApp(null);
			Set<AppUserChild> sau = new HashSet<AppUserChild>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (AppUser aul : arl.getAppUsers()) {
				sau.add(castAppUserChild(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static AppRole castAppRole(AppRoleChild arl) {
		if (arl != null) {
			AppRole ar = new AppRole();
			ar.setApp(null);
			Set<AppUser> sau = new HashSet<AppUser>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (AppUserChild aul : arl.getAppUsers()) {
				sau.add(castAppUser(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static Set<AppRole> castAppRoles(Set<AppRoleChild> arl) {
		if (arl != null) {
			Set<AppRole> approle = new HashSet<AppRole>();
			for (AppRoleChild app : arl) {
				AppRole ar = new AppRole();
				ar.setApp(null);
				Set<AppUser> sau = new HashSet<AppUser>();
				ar.setDescription(app.getDescription());
				ar.setAppUsers(sau);
				ar.setCreatedAt(app.getCreatedAt());
				ar.setCreatedAt(app.getUpdatedAt());
				ar.setName(app.getName());
				ar.setId(app.getId());
				for (AppUserChild aul : app.getAppUsers()) {
					sau.add(castAppUser(aul, ar));
				}
				approle.add(ar);
			}

			return approle;
		} else {
			return null;
		}
	}

	public static AppRoleExport castAppRoleExport(AppRoleChildExport arl) {
		if (arl != null) {
			AppRoleExport ar = new AppRoleExport();
			ar.setApp(null);
			Set<AppUserExport> sau = new HashSet<AppUserExport>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (AppUserChildExport aul : arl.getAppUsers()) {
				sau.add(castAppUserExport(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static App castAppList(AppList al, boolean onlyApp) {
		if (al != null) {
			App app = new App();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());
			Set<AppRole> sar = new HashSet<AppRole>();
			if (!onlyApp) {
				for (AppRoleList arl : al.getAppRoles()) {
					sar.add(castAppRoleList(arl));

				}
				app.setAppRoles(sar);
				Set<AppList> sal = al.getChildApps();

				Set<AppChild> sa = new HashSet<AppChild>();
				for (AppList ali : sal) {
					sa.add(castAppChildList(ali, onlyApp));
				}
				app.setChildApps(sa);
			}
			return app;
		} else {
			return null;
		}
	}

	public static AppChild castAppChildList(AppList al, boolean onlyApp) {
		if (al != null) {
			AppChild app = new AppChild();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());
			Set<AppRole> sar = new HashSet<AppRole>();
			if (!onlyApp) {
				for (AppRoleList arl : al.getAppRoles()) {
					sar.add(castAppRoleList(arl));

				}
				app.setAppRoles(sar);
				Set<AppList> sal = al.getChildApps();

				Set<AppChild> sa = new HashSet<AppChild>();
				for (AppList ali : sal) {
					sa.add(castAppChildList(ali, onlyApp));
				}
				app.setChildApps(sa);
			}
			return app;
		} else {
			return null;
		}
	}

	public static AppChild castAppChild(App al) {
		if (al != null) {
			AppChild app = new AppChild();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());
			app.setAppRoles(al.getAppRoles());
			app.setChildApps(al.getChildApps());
			return app;
		} else {
			return null;
		}
	}

	public static AppExport castAppExport(AppChildExport al) {
		if (al != null) {
			AppExport app = new AppExport();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());

			Set<AppRoleExport> sar = new HashSet<>();
			for (AppRoleChildExport arl : al.getAppRoles()) {
				sar.add(castAppRoleExport(arl));

			}
			app.setAppRoles(sar);
			Set<AppChildExport> sal = al.getChildApps();

			Set<AppChildExport> sa = new HashSet<>();
			for (AppChildExport ali : sal) {
				sa.add(ali);
			}
			app.setChildApps(sa);

			Set<AppRoleExport> are = new HashSet<>();
			for (AppRoleChildExport aprce : al.getAppRoles()) {
				are.add(castAppRoleExport(aprce));
			}
			app.setAppRoles(are);
			app.setChildApps(al.getChildApps());
			return app;
		} else {
			return null;
		}
	}

	public static Set<AppUser> castAppUser(Set<AppUserChild> al) {
		if (al != null) {
			Set<AppUser> roles = new HashSet<>();
			for (AppUserChild user : al) {
				AppUser appUser = new AppUser();
				appUser.setId(user.getId());
				appUser.setRole(castAppRole(user.getRole()));
				appUser.setUser(user.getUser());
				roles.add(appUser);
			}

			return roles;
		} else {
			return null;
		}
	}

	public static Set<AppUserChild> castAppUserChild(Set<AppUser> al) {
		if (al != null) {
			Set<AppUserChild> roles = new HashSet<>();
			for (AppUser user : al) {
				AppUserChild appUser = new AppUserChild();
				appUser.setId(user.getId());
				appUser.setRole(castAppRoleChild(user.getRole()));
				appUser.setUser(user.getUser());
				roles.add(appUser);
			}

			return roles;
		} else {
			return null;
		}
	}

	public static Project castProjectList(ProjectList pl, boolean onlyApp) {
		if (pl != null) {
			Project p = new Project();
			p.setId(pl.getId());
			p.setIdentification(pl.getIdentification());
			p.setDescription(pl.getDescription());
			p.setCreatedAt(pl.getCreatedAt());
			p.setUpdatedAt(pl.getUpdatedAt());
			p.setApp(castAppList(pl.getApp(), onlyApp));
			p.setUser(pl.getUser());
			p.setUsers(pl.getUsers());
			p.setType(pl.getType());
			p.setWebProject(pl.getWebProject());
			return p;
		} else {
			return null;
		}
	}

	public static ProjectResourceAccess castProjectResourceAccessList(ProjectResourceAccessList pral) {
		if (pral != null) {
			ProjectResourceAccess p = new ProjectResourceAccess();
			p.setId(pral.getId());
			p.setProject(pral.getProject());
			p.setProject(pral.getProject());
			p.setUser(pral.getUser());
			p.setResource(pral.getResource());
			p.setAccess(pral.getAccess());
			return p;
		} else {
			return null;
		}
	}
}