/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
import com.minsait.onesait.platform.config.model.AppExport;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleChild;
import com.minsait.onesait.platform.config.model.AppRoleExport;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.model.AppUserChild;
import com.minsait.onesait.platform.config.model.AppUserExport;
import com.minsait.onesait.platform.config.model.AppUserList;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.ProjectList;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessList;

public final class EntitiesCast {
	public static AppUser castAppUserList(AppUserList aul, AppRole ar) {
		final AppUser au = new AppUser();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppUserChild castAppUserChild(AppUser aul, AppRoleChild ar) {
		final AppUserChild au = new AppUserChild();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppUser castAppUser(AppUserChild aul, AppRole ar) {
		final AppUser au = new AppUser();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppUserExport castAppUserExport(AppUserExport aul, AppRoleExport ar) {
		final AppUserExport au = new AppUserExport();
		au.setId(aul.getId());
		au.setUser(aul.getUser());
		au.setRole(ar);
		return au;
	}

	public static AppRole castAppRoleList(AppRoleList arl) {
		if (arl != null) {
			final AppRole ar = new AppRole();
			ar.setApp(null);
			final Set<AppUser> sau = new HashSet<AppUser>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (final AppUserList aul : arl.getAppUsers()) {
				sau.add(castAppUserList(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static AppRoleChild castAppRoleChild(AppRole arl) {
		if (arl != null) {
			final AppRoleChild ar = new AppRoleChild();
			ar.setApp(null);
			final Set<AppUserChild> sau = new HashSet<AppUserChild>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (final AppUser aul : arl.getAppUsers()) {
				sau.add(castAppUserChild(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static AppRole castAppRole(AppRoleChild arl) {
		if (arl != null) {
			final AppRole ar = new AppRole();
			ar.setApp(null);
			final Set<AppUser> sau = new HashSet<AppUser>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(sau);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			for (final AppUserChild aul : arl.getAppUsers()) {
				sau.add(castAppUser(aul, ar));
			}
			return ar;
		} else {
			return null;
		}
	}

	public static Set<AppRole> castAppRoles(Set<AppRoleChild> arl) {
		if (arl != null) {
			final Set<AppRole> approle = new HashSet<AppRole>();
			for (final AppRoleChild app : arl) {
				final AppRole ar = new AppRole();
				ar.setApp(null);
				final Set<AppUser> sau = new HashSet<AppUser>();
				ar.setDescription(app.getDescription());
				ar.setAppUsers(sau);
				ar.setCreatedAt(app.getCreatedAt());
				ar.setCreatedAt(app.getUpdatedAt());
				ar.setName(app.getName());
				ar.setId(app.getId());
				for (final AppUserChild aul : app.getAppUsers()) {
					sau.add(castAppUser(aul, ar));
				}
				approle.add(ar);
			}

			return approle;
		} else {
			return null;
		}
	}

	public static AppRoleExport castAppRoleExport(AppRoleExport arl) {
		if (arl != null) {
			final AppRoleExport ar = new AppRoleExport();
			ar.setApp(null);
			final Set<AppUserExport> sau = new HashSet<AppUserExport>();
			ar.setDescription(arl.getDescription());
			ar.setAppUsers(null);
			ar.setCreatedAt(arl.getCreatedAt());
			ar.setCreatedAt(arl.getUpdatedAt());
			ar.setName(arl.getName());
			ar.setId(arl.getId());
			/*for (final AppUserExport aul : arl.getAppUsers()) {
				sau.add(castAppUserExport(aul, ar));
			}*/
			return ar;
		} else {
			return null;
		}
	}

	public static App castAppList(AppList al, boolean onlyApp) {
		if (al != null) {
			final App app = new App();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());
			final Set<AppRole> sar = new HashSet<AppRole>();
			if (!onlyApp) {
				for (final AppRoleList arl : al.getAppRoles()) {
					sar.add(castAppRoleList(arl));

				}
				if (al.getProject() != null) {
					final Project p = new Project();
					p.setIdentification(al.getProject().getIdentification());
					p.setId(al.getProject().getId());
					app.setProject(p);
				}
				app.setAppRoles(sar);
				final Set<AppList> sal = al.getChildApps();

				final Set<App> sa = new HashSet<App>();
				for (final AppList ali : sal) {
					sa.add(castAppList(ali, onlyApp));
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
			final AppChild app = new AppChild();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());
			final Set<AppRole> sar = new HashSet<AppRole>();
			if (!onlyApp) {
				for (final AppRoleList arl : al.getAppRoles()) {
					sar.add(castAppRoleList(arl));

				}
				app.setAppRoles(sar);
				final Set<AppList> sal = al.getChildApps();

				final Set<AppChild> sa = new HashSet<AppChild>();
				for (final AppList ali : sal) {
					sa.add(castAppChildList(ali, onlyApp));
				}
				app.setChildApps(sa);
			}
			return app;
		} else {
			return null;
		}
	}

	public static App castAppChild(App al) {
		if (al != null) {
			final App app = new App();
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

	public static AppExport castAppExport(AppExport al) {
		if (al != null) {
			final AppExport app = new AppExport();
			app.setId(al.getId());
			app.setIdentification(al.getIdentification());
			app.setDescription(al.getDescription());
			app.setCreatedAt(al.getCreatedAt());
			app.setUpdatedAt(al.getUpdatedAt());
			app.setTokenValiditySeconds(al.getTokenValiditySeconds());
			app.setSecret(al.getSecret());
			app.setUserExtraFields(al.getUserExtraFields());
			app.setUser(al.getUser());

			final Set<AppRoleExport> sar = new HashSet<>();
			for (final AppRoleExport arl : al.getAppRoles()) {
				sar.add(castAppRoleExport(arl));

			}
			app.setAppRoles(sar);
			final Set<AppExport> sal = al.getChildApps();

			final Set<AppExport> sa = new HashSet<>();
			for (final AppExport ali : sal) {
				sa.add(ali);
			}
			app.setChildApps(sa);

			final Set<AppRoleExport> are = new HashSet<>();
			for (final AppRoleExport aprce : al.getAppRoles()) {
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
			final Set<AppUser> roles = new HashSet<>();
			for (final AppUserChild user : al) {
				final AppUser appUser = new AppUser();
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
			final Set<AppUserChild> roles = new HashSet<>();
			for (final AppUser user : al) {
				final AppUserChild appUser = new AppUserChild();
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
			final Project p = new Project();
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
			final ProjectResourceAccess p = new ProjectResourceAccess();
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