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
package com.minsait.onesait.platform.config.services.mapsproject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.model.MapsMap;
import com.minsait.onesait.platform.config.model.MapsProject;
import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MapsProjectRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.MapsProjectServiceException;
import com.minsait.onesait.platform.config.services.exceptions.MapsProjectServiceException.ErrorType;
import com.minsait.onesait.platform.config.services.mapslayer.MapsLayerService;
import com.minsait.onesait.platform.config.services.mapsmap.MapsMapService;
import com.minsait.onesait.platform.config.services.mapsproject.dto.MapsProjectDTO;
import com.minsait.onesait.platform.config.services.mapsstyle.MapsStyleService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MapsProjectServiceImpl implements MapsProjectService {

	@Autowired
	private MapsProjectRepository mapsProjectRepository;

	@Autowired
	private UserService userService;

	@Autowired
	@Lazy
	private OPResourceService resourceService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MapsStyleService mapsStyleService;
	@Autowired
	private MapsLayerService mapsLayerService;
	@Autowired
	private MapsMapService mapsMapService;

	@Override
	@Transactional
	public List<MapsProject> getProjectsForUser(String userId, String identification) {
		final User user = userService.getUserNoCache(userId);
		List<MapsProject> mapsProjects = new LinkedList<>();
		if (user.isAdmin()) {
			if (identification == null || identification.trim().length() == 0) {
				mapsProjects = mapsProjectRepository.findAll();
			} else {
				mapsProjects = mapsProjectRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null || identification.trim().length() == 0) {
				mapsProjects = mapsProjectRepository.findByUserOrPublic(user);
			} else {
				mapsProjects = mapsProjectRepository.findByUserIdentificationContainingOrPublic(user, identification);
			}
		}
		return mapsProjects;
	}

	@Override
	@Transactional
	public List<MapsProjectDTO> getProjectsForUserDTO(String userId, String identification) {
		final User user = userService.getUserNoCache(userId);
		List<MapsProject> mapsProjects = new LinkedList<>();
		if (user.isAdmin()) {
			if (identification == null || identification.trim().length() == 0) {
				mapsProjects = mapsProjectRepository.findAll();
			} else {
				mapsProjects = mapsProjectRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null || identification.trim().length() == 0) {
				mapsProjects = mapsProjectRepository.findByUserOrPublic(user);
			} else {
				mapsProjects = mapsProjectRepository.findByUserIdentificationContainingOrPublic(user, identification);
			}
		}
		List<MapsProjectDTO> mapsProjectsDTO = new LinkedList<>();

		if (mapsProjects != null && mapsProjects.size() > 0) {
			for (Iterator iterator = mapsProjects.iterator(); iterator.hasNext();) {
				MapsProject mapsProject = (MapsProject) iterator.next();
				mapsProjectsDTO.add(mapToDTO(mapsProject));

			}
		}
		return mapsProjectsDTO;
	}

	@Override
	@Transactional
	public List<MapsProject> getByIdentifier(String identification) {

		return mapsProjectRepository.findByIdentification(identification);
	}

	@Override
	@Transactional
	public MapsProject getById(String identification) {

		return mapsProjectRepository.findById(identification).orElse(null);
	}

	@Override
	@Transactional
	public MapsProject getByIdANDUser(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			return mapsProjectRepository.findById(id).orElse(null);
		}
		throw new MapsProjectServiceException("Cannot get MapsProject does not exist or don't have permission");
	}

	@Override
	public void save(MapsProject mapsProject) {
		try {
			Date date = new Date();
			mapsProject.setCreatedAt(date);
			mapsProject.setUpdatedAt(date);
			JSONObject obj = new JSONObject(mapsProject.getConfig());
			if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")) {
				obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("id",
						mapsProject.getIdentification());
			}
			mapsProject.setConfig(obj.toString());
			mapsProjectRepository.save(mapsProject);
		} catch (final Exception e) {
			throw new MapsProjectServiceException("Cannot create MapsProject");
		}
	}

	@Override
	public void update(MapsProject mapsProject) {
		if (exists(mapsProject)) {
			final MapsProject mapStyleDB = mapsProjectRepository.findById(mapsProject.getId())
					.orElse(new MapsProject());
			if (mapStyleDB.getIdentification() == null) {
				mapStyleDB.setIdentification(mapsProject.getIdentification());
			}

			JSONObject obj = new JSONObject(mapsProject.getConfig());
			if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")) {
				obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("id",
						mapsProject.getIdentification());
			}
			mapStyleDB.setConfig(obj.toString());
			mapStyleDB.setDescription(mapsProject.getDescription());
			mapStyleDB.setCreatedAt(mapsProject.getCreatedAt());
			mapStyleDB.setPublic(mapsProject.isPublic());
			mapStyleDB.setUpdatedAt(new Date());
			mapsProjectRepository.save(mapStyleDB);
		} else {
			throw new MapsProjectServiceException("Cannot update MapsProject that does not exist");
		}

	}

	@Override
	public boolean exists(MapsProject mapsProject) {
		return mapsProjectRepository.findByIdentification(mapsProject.getIdentification()) != null;

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (mapsProjectRepository.findById(id).isPresent()) {
			return mapsProjectRepository.findById(id).get().getUser().getUserId().equals(userId);
		} else {
			return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
		}
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		return hasUserPermission(id, userId) || resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
	}

	@Override
	public String getAccessType(String id, String userId) {
		if (!mapsProjectRepository.findById(id).isPresent())
			return null;
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)
				|| mapsProjectRepository.findById(id).get().getUser().getUserId().equals(userId)
				|| resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE)) {
			return ResourceAccessType.MANAGE.toString();
		} else if (resourceService.hasAccess(userId, id, ResourceAccessType.VIEW)) {
			return ResourceAccessType.VIEW.toString();
		}
		return null;
	}

	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		return hasUserPermission(id, userId) || resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
	}

	@Override
	public void delete(String id, boolean deleteDepencies, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final MapsProject mapsProject = mapsProjectRepository.findById(id).orElse(null);
			if (mapsProject != null) {
				if (!deleteDepencies) {
					mapsProjectRepository.delete(mapsProject);
				} else {
					// Delete styles
					JSONObject obj = new JSONObject(mapsProject.getConfig());

					if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("toolsOptions")) {

						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("measureToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("measureToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("measureToolOptions").getString("style"),
												userId);
							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.has("selectByGeomToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("selectByGeomToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("selectByGeomToolOptions").getString("style"),
												userId);
							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("showCoordToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("showCoordToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("showCoordToolOptions").getString("style"),
												userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("infoToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("infoToolOptions").has("style")) {
								mapsStyleService.deleteByIdentification(
										obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
												.getJSONObject("infoToolOptions").getString("style"),
										userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("geocoderToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("geocoderToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("geocoderToolOptions").getString("style"),
												userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("geocoderToolOptions").has("styleSelect")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("geocoderToolOptions").getString("styleSelect"),
												userId);

							}
						}

						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("routingToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("routingToolOptions").getString("style"),
												userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").has("stopStyle")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("routingToolOptions").getString("stopStyle"),
												userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").has("blockStyle")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("routingToolOptions").getString("blockStyle"),
												userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").has("routeStyle")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("routingToolOptions").getString("routeStyle"),
												userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.has("selectByAttrToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("selectByAttrToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("selectByAttrToolOptions").getString("style"),
												userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("bufferToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("bufferToolOptions").has("style")) {
								mapsStyleService.deleteByIdentification(
										obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
												.getJSONObject("bufferToolOptions").getString("style"),
										userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("intersectToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("intersectToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("intersectToolOptions").getString("style"),
												userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("proximityToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("proximityToolOptions").has("style")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("proximityToolOptions").getString("style"),
												userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("gotoToolOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("gotoToolOptions").has("style")) {
								mapsStyleService.deleteByIdentification(
										obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
												.getJSONObject("gotoToolOptions").getString("style"),
										userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("gotoToolOptions").has("styleSelect")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("gotoToolOptions").getString("styleSelect"),
												userId);

							}
						}
						if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("editionOptions")) {
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions").has("modifyStyle")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("editionOptions").getString("modifyStyle"),
												userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions").has("addStyle")) {
								mapsStyleService.deleteByIdentification(
										obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
												.getJSONObject("editionOptions").getString("addStyle"),
										userId);

							}
							if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions").has("deleteStyle")) {
								mapsStyleService
										.deleteByIdentification(
												obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
														.getJSONObject("editionOptions").getString("deleteStyle"),
												userId);
							}
						}
					}

					// Delete Maps

					if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mapOptions")) {
						JSONArray mapOptions = obj.getJSONObject("mapConfig").getJSONArray("mapOptions");
						for (int i = 0; i < mapOptions.length(); i++) {
							JSONObject map = mapOptions.getJSONObject(i);

							deleteMapsMap(map, userId);
						}
					}

					mapsProjectRepository.delete(mapsProject);
				}
			} else {
				throw new MapsProjectServiceException("Cannot delete MapsProject that does not exist");
			}
		}

	}

	@Override
	public String clone(MapsProject originalMapsProject, String identification, User targetUser, User sessionUser) {

		try {
			JSONObject objexp = new JSONObject(originalMapsProject.getConfig());
			String oldIdentification = objexp.getJSONObject("mapConfig").getJSONObject("mainMapOptions")
					.getString("id");
			JSONObject obj = new JSONObject(exportMapsProject(oldIdentification, sessionUser));
			if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")
					&& obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").has("id")) {
				obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("id", identification);
			}
			return this.createMapsProject(obj, identification, false, targetUser);

		} catch (final Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	@Transactional
	@Override
	public String importMapsProject(String originalMapProject, boolean overwrite, User user) {
		JSONObject obj = new JSONObject(originalMapProject);
		String id = "";
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")
				&& obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").has("id")) {
			id = obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").getString("id");
		}

		if (!user.isAdmin()) {
			List<MapsProject> mps = mapsProjectRepository.findByIdentification(id);
			if (mps != null && mps.size() > 0) {
				if (mps.get(0).getUser().equals(user)) {
					throw new MapsProjectServiceException(ErrorType.UNAUTHORIZED, "UNAUTHORIZED");
				}
			}
		}
		return createMapsProject(obj, id, overwrite, user);
	}

	@Transactional
	private String createMapsProject(JSONObject obj, String id, boolean overwrite, User user) {
		HashMap<String, String> idList = new HashMap<String, String>();
		List<MapsProject> projects = this.getByIdentifier(id);
		if (projects == null || projects.size() == 0) {
			// Not exist create
			obj = createProjectStyles(obj, id, overwrite, user, idList);
			obj = createProjectMaps(obj, id, overwrite, user, idList);
			return createNewMapsproject(obj, id, user);
		} else {
			// exist
			if (overwrite) {
				MapsProject project = projects.get(0);
				obj = createProjectStyles(obj, id, overwrite, user, idList);
				obj = createProjectMaps(obj, id, overwrite, user, idList);
				project.setConfig(obj.toString());
				project.setUser(user);
				this.save(project);
				return project.getIdentification();
			} else {
				// create new id
				obj = createProjectStyles(obj, id, overwrite, user, idList);
				obj = createProjectMaps(obj, id, overwrite, user, idList);
				return createNewMapsproject(obj, id, user);
			}
		}

	}

	@Transactional
	// this method save maps from projects
	private JSONObject createProjectMaps(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mapOptions")) {
			JSONArray mapOptions = obj.getJSONObject("mapConfig").getJSONArray("mapOptions");
			for (int i = 0; i < mapOptions.length(); i++) {
				JSONObject map = mapOptions.getJSONObject(i);
				JSONObject mapLink = new JSONObject();

				if (map.has("zIndex")) {

					mapLink.put("zIndex", map.getInt("zIndex"));
				} else {
					mapLink.put("zIndex", i);
				}
				mapLink.put("id", createMapsMap(map, text, overwrite, user, idList));

				mapOptions.put(i, mapLink);
			}
			obj.getJSONObject("mapConfig").put("mapOptions", mapOptions);
		}
		return obj;
	}

	@Transactional
	// this method save the project
	private String createNewMapsproject(JSONObject obj, String text, User user) {
		MapsProject p = new MapsProject();
		p.setIdentification(text);
		p.setDescription("Maps project " + text);
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")) {
			obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").getString("id");
			obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("id", p.getIdentification());
			obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("description", "Maps project " + text);
		}
		p.setConfig(obj.toString());
		p.setUser(user);
		this.save(p);
		return p.getIdentification();
	}

	@Transactional
	// this method save project styles
	private JSONObject createProjectStyles(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("toolsOptions")) {

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("measureToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("measureToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("measureToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("measureToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("measureToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByGeomToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByGeomToolOptions").has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("selectByGeomToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
							.getJSONObject("selectByGeomToolOptions").put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("selectByGeomToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("showCoordToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("showCoordToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("showCoordToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("infoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("infoToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions").put(
							"style",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("infoToolOptions").getJSONObject("style"),
									text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("geocoderToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("geocoderToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("geocoderToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("styleSelect")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("geocoderToolOptions").getJSONObject("styleSelect").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
							.put("styleSelect",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("geocoderToolOptions").getJSONObject("styleSelect"),
											text, overwrite, user, idList));

				}
			}

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("routingToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("routingToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("stopStyle")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("routingToolOptions").getJSONObject("stopStyle").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("stopStyle",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("stopStyle"),
											text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("blockStyle")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("routingToolOptions").getJSONObject("blockStyle").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("blockStyle",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("blockStyle"),
											text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("routeStyle")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("routingToolOptions").getJSONObject("routeStyle").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("routeStyle",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("routeStyle"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByAttrToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByAttrToolOptions").has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("selectByAttrToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
							.getJSONObject("selectByAttrToolOptions").put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("selectByAttrToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("bufferToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("bufferToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions").put(
							"style",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("bufferToolOptions").getJSONObject("style"),
									text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("intersectToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("intersectToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("intersectToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("proximityToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("proximityToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("proximityToolOptions").getJSONObject("style"),
											text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("gotoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("style")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("gotoToolOptions").getJSONObject("style").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions").put(
							"style",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("gotoToolOptions").getJSONObject("style"),
									text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("styleSelect")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
								.getJSONObject("gotoToolOptions").getJSONObject("styleSelect").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions").put(
							"styleSelect",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("gotoToolOptions").getJSONObject("styleSelect"),
									text, overwrite, user, idList));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("editionOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("modifyStyle")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
								.getJSONObject("modifyStyle").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"modifyStyle",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("editionOptions").getJSONObject("modifyStyle"),
									text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("addStyle")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
								.getJSONObject("addStyle").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"addStyle",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("editionOptions").getJSONObject("addStyle"),
									text, overwrite, user, idList));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("deleteStyle")
						&& !obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
								.getJSONObject("deleteStyle").isEmpty()) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"deleteStyle",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("editionOptions").getJSONObject("deleteStyle"),
									text, overwrite, user, idList));

				}
			}
		}
		return obj;
	}

	@Transactional
	// Create maps layers
	private String createMapsMap(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			MapsMap maps = mapsMapService.getByIdentificationANDUser(id, user.getUserId());
			if (maps == null) {
				List<MapsMap> map = mapsMapService.getByIdentifier(id);
				if (map.size() > 0) {
					idList.remove(id);
					obj = createMapsLayers(obj, text, overwrite, user, idList);
					return createNewMapsmap(obj, text, user, existIdOrNew(text, id, false, idList));
				} else {
					// Not exist create
					obj = createMapsLayers(obj, text, overwrite, user, idList);
					return createNewMapsmap(obj, text, user, id);
				}
			} else {
				// exist
				if (overwrite) {
					MapsMap map = maps;
					obj = createMapsLayers(obj, text, overwrite, user, idList);
					map.setConfig(obj.toString());
					mapsMapService.save(map);
					return map.getIdentification();
				} else {
					// create new id
					obj = createMapsLayers(obj, text, overwrite, user, idList);
					return createNewMapsmap(obj, text, user, null);
				}
			}
		} else {
			// create new id
			obj = createMapsLayers(obj, text, overwrite, user, idList);
			return createNewMapsmap(obj, text, user, null);
		}
	}

	// Delete maps layers
	private void deleteMapsMap(JSONObject obj, String user) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			List<MapsMap> maps = mapsMapService.getByIdentifier(id);
			if (maps != null && maps.size() > 0) {

				deleteMapsLayers(new JSONObject(maps.get(0).getConfig()), user);
				mapsMapService.delete(maps.get(0).getId(), user);
			}
		}
	}

	private void deleteMapsLayers(JSONObject obj, String user) {
		if (obj.has("services")) {
			JSONArray services = obj.getJSONArray("services");
			for (int i = 0; i < services.length(); i++) {
				JSONObject layer = services.getJSONObject(i);
				deleteMapsLayer(layer, user);

			}

		}

	}

	// Create maps layers
	private void deleteMapsLayer(JSONObject obj, String user) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			List<MapsLayer> layers = mapsLayerService.getByIdentifier(id);
			if (layers != null && layers.size() > 0) {
				// delete styles
				JSONObject layerObj = new JSONObject(layers.get(0).getConfig());
				if (layerObj.has("style")) {
					mapsStyleService.deleteByIdentification(layerObj.getString("style"), user);
				}
				if (layerObj.has("styleSelect")) {
					mapsStyleService.deleteByIdentification(layerObj.getString("styleSelect"), user);

				}
				// delete layer
				mapsLayerService.delete(layers.get(0).getId(), user);
			}
		}
	}

	private String createNewMapsmap(JSONObject obj, String text, User user, String id) {
		MapsMap map = new MapsMap();

		if (id == null) {
			map.setIdentification(randomIdentfication(text));
			obj.put("id", map.getIdentification());

		} else {
			map.setIdentification(id);
			obj.put("id", id);
		}
		obj.put("description", "Maps project " + text);
		map.setDescription("Maps project " + text);
		if (obj.getString("idDiv") == null || obj.getString("idDiv").trim().length() == 0) {
			obj.put("idDiv", randomIdentfication(text));
		}
		if (obj.get("optionView") == null) {
			JSONObject optionView = new JSONObject();
			optionView.put("id", randomIdentfication(text));
			obj.put("optionView", optionView);
		} else if (obj.get("optionView") != null && (obj.getJSONObject("optionView").getString("id") == null
				|| obj.getJSONObject("optionView").getString("id").trim().length() == 0)) {
			obj.getJSONObject("optionView").put("id", randomIdentfication(text));
		}
		map.setConfig(obj.toString());
		map.setUser(user);
		mapsMapService.save(map);
		return map.getIdentification();
	}

	@Transactional
	private JSONObject createMapsLayers(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {
		if (obj.has("services")) {
			JSONArray services = obj.getJSONArray("services");
			for (int i = 0; i < services.length(); i++) {
				JSONObject layer = services.getJSONObject(i);
				JSONObject layerLink = new JSONObject();

				if (layer.has("zIndex")) {

					layerLink.put("zIndex", layer.getInt("zIndex"));
				} else {
					layerLink.put("zIndex", i);
				}
				layerLink.put("id", createMapsLayer(layer, text, overwrite, user, idList));

				services.put(i, layerLink);
			}
			obj.put("services", services);
		}
		return obj;
	}

	@Transactional
	// Create maps layers
	private String createMapsLayer(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			id = existIdOrNew(text, id, overwrite, idList);
			MapsLayer layers = mapsLayerService.getByIdentificationANDUser(id, user.getUserId());
			if (layers == null) {
				List<MapsLayer> layer = mapsLayerService.getByIdentifier(id);
				if (layer.size() > 0) {
					idList.remove(id);
					obj = createMapsLayerStyles(obj, text, overwrite, user, idList);
					return createNewMapsLayer(obj, text, user, existIdOrNew(text, id, false, idList));
				} else {
					// Not exist create
					obj = createMapsLayerStyles(obj, text, overwrite, user, idList);
					return createNewMapsLayer(obj, text, user, id);
				}
			} else {
				// exist
				// if (overwrite) {
				MapsLayer ms = layers;
				obj = createMapsLayerStyles(obj, text, overwrite, user, idList);
				ms.setConfig(obj.toString());
				ms.setUser(user);
				mapsLayerService.save(ms);
				return ms.getIdentification();
				// } else {
				// create new id
				// obj = createMapsLayerStyles(obj, text, overwrite, user, idList);
				// return createNewMapsLayer(obj, text, user, id);
				// }
			}
		} else {
			// create new id
			obj = createMapsLayerStyles(obj, text, overwrite, user, idList);
			return createNewMapsLayer(obj, text, user, existIdOrNew(text, null, overwrite, idList));
		}
	}

	private String createNewMapsLayer(JSONObject obj, String text, User user, String id) {
		MapsLayer ml = new MapsLayer();
		/*
		 * if (id == null) { ml.setIdentification(randomIdentfication(text));
		 * obj.put("id", ml.getIdentification()); } else {
		 */
		ml.setIdentification(id);
		obj.put("id", id);
		/* } */
		obj.put("description", "Maps project " + text);
		ml.setDescription("Maps project " + text);
		ml.setUser(user);
		ml.setConfig(obj.toString());
		mapsLayerService.save(ml);
		return ml.getIdentification();
	}

	@Transactional
	private JSONObject createMapsLayerStyles(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {

		if (obj.has("style") && !obj.getJSONObject("style").isEmpty()) {
			obj.put("style", createMapsStyle(obj.getJSONObject("style"), text, overwrite, user, idList));
		}
		if (obj.has("styleSelect") && !obj.getJSONObject("styleSelect").isEmpty()) {
			obj.put("styleSelect", createMapsStyle(obj.getJSONObject("styleSelect"), text, overwrite, user, idList));
		}
		return obj;
	}

	@Transactional
	// Create maps styles
	private String createMapsStyle(JSONObject obj, String text, boolean overwrite, User user,
			HashMap<String, String> idList) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			id = existIdOrNew(text, id, overwrite, idList);
			MapsStyle style = mapsStyleService.getByIdentificationANDUser(id, user.getUserId());

			if (style == null) {
				List<MapsStyle> styles = mapsStyleService.getByIdentifier(id);
				if (styles.size() > 0) {
					idList.remove(id);
					return createNewStyle(obj, text, user, existIdOrNew(text, id, false, idList));
				} else {
					// Not exist create
					return createNewStyle(obj, text, user, id);
				}
			} else {
				// exist
				// if (overwrite && !idList.containsKey(id)) {
				MapsStyle ms = style;
				ms.setConfig(obj.toString());
				mapsStyleService.save(ms);
				return ms.getIdentification();
				// } else {
				// create new id
				// return createNewStyle(obj, text, user, id);
				// }
			}

		} else {
			// create new id
			return createNewStyle(obj, text, user, existIdOrNew(text, null, overwrite, idList));
		}
	}

	private String existIdOrNew(String text, String id, boolean overwrite, HashMap<String, String> idList) {
		if (idList.containsKey(id)) {
			return idList.get(id);
		} else if (id == null) {
			return randomIdentfication(text);
		} else {
			if (overwrite) {
				idList.put(id, id);
				return id;
			} else {
				String newId = randomIdentfication(text);
				idList.put(id, newId);
				return newId;
			}

		}
	}

	@Transactional
	private String createNewStyle(JSONObject obj, String text, User user, String id) {
		MapsStyle ms = new MapsStyle();
		/* if (id != null) { */
		ms.setIdentification(id);
		obj.put("id", id);
		/*
		 * } else { ms.setIdentification(randomIdentfication(text)); obj.put("id",
		 * ms.getIdentification()); }
		 */
		obj.put("description", "Maps project " + text);
		ms.setDescription("Maps project " + text);
		ms.setUser(user);
		ms.setConfig(obj.toString());
		mapsStyleService.save(ms);
		return ms.getIdentification();
	}

	private String randomIdentfication(String text) {
		Random random = new Random();
		int rn = random.nextInt(999) + 0;
		return text + "-" + new Date().getTime() + rn;
	}

	@Override
	public String exportMapsProject(String id, User user) {
		List<MapsProject> mps = mapsProjectRepository.findByIdentification(id);
		if (mps == null || mps.size() == 0) {
			throw new MapsProjectServiceException(ErrorType.NOT_FOUND, "NOT FOUND");
		}

		MapsProject mp = mps.get(0);
		if (!user.isAdmin() && !mp.isPublic() && !mp.getUser().equals(user)) {
			throw new MapsProjectServiceException(ErrorType.NOT_FOUND, "NOT FOUND");
		}

		// load Project JSON

		JSONObject obj = new JSONObject(mp.getConfig());

		// Load Project Styles
		obj = loadMapProjectsStyles(obj);
		// Load Project Maps with layers and layers styles
		obj = loadMapProjectsMaps(obj);

		return obj.toString();
	}

	private JSONObject loadStyle(JSONObject ob, String field) {
		if (ob.get(field) instanceof String) {
			ob.put(field, loadMapStyle(ob.getString(field)));
		} else {
			ob.put(field, new JSONObject());
		}
		return ob;
	}

	private JSONObject loadMapProjectsStyles(JSONObject obj) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("toolsOptions")) {

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("measureToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("measureToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("measureToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("measureToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByGeomToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByGeomToolOptions").has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("selectByGeomToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("selectByGeomToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("showCoordToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("showCoordToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("showCoordToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("infoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("infoToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("infoToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("geocoderToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("geocoderToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("geocoderToolOptions"), "style"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("styleSelect")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("geocoderToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("geocoderToolOptions"), "styleSelect"));

				}
			}

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("routingToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("routingToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions"), "style"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("stopStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("routingToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions"), "stopStyle"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("blockStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("routingToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions"), "blockStyle"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("routeStyle")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("routingToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions"), "routeStyle"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByAttrToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByAttrToolOptions").has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("selectByAttrToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("selectByAttrToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("bufferToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("bufferToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("bufferToolOptions"), "style"));
				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("intersectToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("intersectToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("intersectToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("proximityToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("proximityToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("proximityToolOptions"), "style"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("gotoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("style")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("gotoToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("gotoToolOptions"), "style"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("styleSelect")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("gotoToolOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("gotoToolOptions"), "styleSelect"));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("editionOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("modifyStyle")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("editionOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions"), "modifyStyle"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("addStyle")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("editionOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions"), "addStyle"));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("deleteStyle")) {

					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").put("editionOptions",
							loadStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions"), "deleteStyle"));

				}
			}
		}
		return obj;

	}

	private JSONObject loadMapProjectsMaps(JSONObject obj) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mapOptions")) {
			JSONArray mapOptions = obj.getJSONObject("mapConfig").getJSONArray("mapOptions");
			for (int i = 0; i < mapOptions.length(); i++) {
				JSONObject map = mapOptions.getJSONObject(i);
				JSONObject mapResult = loadMapMap(map.getString("id"));
				mapResult.put("zIndex", map.getInt("zIndex"));
				mapResult.put("id", map.getString("id"));
				mapOptions.put(i, mapResult);
			}
			obj.getJSONObject("mapConfig").put("mapOptions", mapOptions);
		}
		return obj;
	}

	private JSONObject loadMapMap(String id) {
		if (id == null || id.equals("")) {
			return new JSONObject();
		}
		List<MapsMap> mss = mapsMapService.getByIdentifier(id);
		if (mss != null && mss.size() > 0) {
			MapsMap ms = mss.get(0);

			JSONObject job = new JSONObject(ms.getConfig());
			// Layers
			job = loadMapsLayers(job);
			return job;
		} else {
			throw new MapsProjectServiceException(ErrorType.NOT_FOUND, "NOT FOUND maps with identification : " + id);
		}
	}

	private JSONObject loadMapsLayers(JSONObject obj) {
		if (obj.has("services")) {
			JSONArray services = obj.getJSONArray("services");
			for (int i = 0; i < services.length(); i++) {
				JSONObject layer = services.getJSONObject(i);
				JSONObject layerResult = loadLayer(layer.getString("id"));
				layerResult.put("zIndex", layer.getInt("zIndex"));
				services.put(i, layerResult);
			}
			obj.put("services", services);
		}
		return obj;
	}

	private JSONObject loadLayer(String id) {
		List<MapsLayer> mls = mapsLayerService.getByIdentifier(id);
		if (mls != null && mls.size() > 0) {
			MapsLayer ms = mls.get(0);

			JSONObject job = new JSONObject(ms.getConfig());
			// load layer styles
			job = loadMapsLayerStyles(job);
			return job;
		} else {
			throw new MapsProjectServiceException(ErrorType.NOT_FOUND,
					"NOT FOUND map layer with identification : " + id);
		}
	}

	private JSONObject loadMapsLayerStyles(JSONObject obj) {

		if (obj.has("style")) {
			obj = loadStyle(obj, "style");
		}
		if (obj.has("styleSelect")) {
			obj = loadStyle(obj, "styleSelect");
		}
		return obj;
	}

	private JSONObject loadMapStyle(String id) {
		if (id == null || id.equals("")) {
			return new JSONObject();
		}
		List<MapsStyle> mss = mapsStyleService.getByIdentifier(id);
		if (mss != null && mss.size() > 0) {
			MapsStyle ms = mss.get(0);
			JSONObject jo = new JSONObject(ms.getConfig());
			jo.put("id", id);
			return jo;
		} else {
			throw new MapsProjectServiceException(ErrorType.NOT_FOUND,
					"NOT FOUND maps style with identification : " + id);
		}
	}

	private MapsProjectDTO mapToDTO(MapsProject mapsProject) {
		MapsProjectDTO dto = new MapsProjectDTO();
		dto.setDescription(mapsProject.getDescription());
		dto.setId(mapsProject.getId());
		dto.setIdentification(mapsProject.getIdentification());
		dto.setCreatedAt(mapsProject.getCreatedAt());
		dto.setUpdatedAt(mapsProject.getUpdatedAt());
		if (mapsProject.getUser() != null) {
			dto.setUser(mapsProject.getUser().getUserId());
		}
		return dto;

	}

}
