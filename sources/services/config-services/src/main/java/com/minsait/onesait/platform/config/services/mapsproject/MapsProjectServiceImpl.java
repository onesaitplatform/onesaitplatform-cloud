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
package com.minsait.onesait.platform.config.services.mapsproject;

import java.util.Date;
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
			mapStyleDB.setConfig(mapsProject.getConfig());
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
	public void delete(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			final MapsProject mapsProject = mapsProjectRepository.findById(id).orElse(null);
			if (mapsProject != null) {
				mapsProjectRepository.delete(mapsProject);
			} else {
				throw new MapsProjectServiceException("Cannot delete MapsProject that does not exist");
			}
		}

	}

	@Override
	public String clone(MapsProject originalMapsProject, String identification, User user) {

		try {
			JSONObject objexp = new JSONObject(originalMapsProject.getConfig());
			String oldIdentification = objexp.getJSONObject("mapConfig").getJSONObject("mainMapOptions")
					.getString("id");
			JSONObject obj = new JSONObject(exportMapsProject(oldIdentification, user));
			if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")
					&& obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").has("id")) {
				obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("id", identification);
			}
			return this.createMapsProject(obj, identification, false, user);

		} catch (final Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

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

	private String createMapsProject(JSONObject obj, String id, boolean overwrite, User user) {

		List<MapsProject> projects = this.getByIdentifier(id);
		if (projects == null || projects.size() == 0) {
			// Not exist create
			obj = createProjectStyles(obj, id, overwrite, user);
			obj = createProjectMaps(obj, id, overwrite, user);
			return createNewMapsproject(obj, id, user);
		} else {
			// exist
			if (overwrite) {
				MapsProject project = projects.get(0);
				obj = createProjectStyles(obj, id, overwrite, user);
				obj = createProjectMaps(obj, id, overwrite, user);
				project.setConfig(obj.toString());
				project.setUser(user);
				this.save(project);
				return project.getIdentification();
			} else {
				// create new id
				obj = createProjectStyles(obj, id, overwrite, user);
				obj = createProjectMaps(obj, id, overwrite, user);
				return createNewMapsproject(obj, id, user);
			}
		}

	}

	// this method save maps from projects
	private JSONObject createProjectMaps(JSONObject obj, String text, boolean overwrite, User user) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mapOptions")) {
			JSONArray mapOptions = obj.getJSONObject("mapConfig").getJSONArray("mapOptions");
			for (int i = 0; i < mapOptions.length(); i++) {
				JSONObject map = mapOptions.getJSONObject(i);
				JSONObject mapLink = new JSONObject();
				int zIndex = 0;
				if (map.has("zIndex")) {
					zIndex = map.getInt("zIndex");
				}
				mapLink.put("id", createMapsMap(map, text, overwrite, user));
				mapLink.put("zIndex", zIndex);

				mapOptions.put(i, mapLink);
			}
			obj.getJSONObject("mapConfig").put("mapOptions", mapOptions);
		}
		return obj;
	}

	// this method save the project
	private String createNewMapsproject(JSONObject obj, String text, User user) {
		MapsProject p = new MapsProject();
		p.setIdentification(text);
		p.setDescription("Imported for Maps project " + text);
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("mainMapOptions")) {
			obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").getString("id");
			obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("id", p.getIdentification());
			obj.getJSONObject("mapConfig").getJSONObject("mainMapOptions").put("description",
					"Imported for Maps project " + text);
		}
		p.setConfig(obj.toString());
		p.setUser(user);
		this.save(p);
		return p.getIdentification();
	}

	// this method save project styles
	private JSONObject createProjectStyles(JSONObject obj, String text, boolean overwrite, User user) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("toolsOptions")) {

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("mesaureToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("mesaureToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("mesaureToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("mesaureToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByGeomToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByGeomToolOptions").has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
							.getJSONObject("selectByGeomToolOptions").put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("selectByGeomToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("showCoordToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("showCoordToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("infoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions").put(
							"style",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("infoToolOptions").getJSONObject("style"),
									text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("geocoderToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("geocoderToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("styleSelect")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
							.put("styleSelect",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("geocoderToolOptions").getJSONObject("styleSelect"),
											text, overwrite, user));

				}
			}

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("routingToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("stopStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("stopStyle",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("stopStyle"),
											text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("blockStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("blockStyle",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("blockStyle"),
											text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("routeStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("routeStyle",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("routingToolOptions").getJSONObject("routeStyle"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByAttrToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByAttrToolOptions").has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
							.getJSONObject("selectByAttrToolOptions").put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("selectByAttrToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("bufferToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions").put(
							"style",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("bufferToolOptions").getJSONObject("style"),
									text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("intersectToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("intersectToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("proximityToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
							.put("style",
									createMapsStyle(
											obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
													.getJSONObject("proximityToolOptions").getJSONObject("style"),
											text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("gotoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions").put(
							"style",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("gotoToolOptions").getJSONObject("style"),
									text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("styleSelect")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions").put(
							"styleSelect",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("gotoToolOptions").getJSONObject("styleSelect"),
									text, overwrite, user));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("editionOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("modifyStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"modifyStyle",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("editionOptions").getJSONObject("modifyStyle"),
									text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("addStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"addStyle",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("editionOptions").getJSONObject("addStyle"),
									text, overwrite, user));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("deleteStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"deleteStyle",
							createMapsStyle(
									obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("editionOptions").getJSONObject("deleteStyle"),
									text, overwrite, user));

				}
			}
		}
		return obj;
	}

	// Create maps layers
	private String createMapsMap(JSONObject obj, String text, boolean overwrite, User user) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			List<MapsMap> maps = mapsMapService.getByIdentifier(id);
			if (maps == null || maps.size() == 0) {
				// Not exist create
				obj = createMapsLayers(obj, text, overwrite, user);
				return createNewMapsmap(obj, text, user);
			} else {
				// exist
				if (overwrite) {
					MapsMap map = maps.get(0);
					obj = createMapsLayers(obj, text, overwrite, user);
					map.setConfig(obj.toString());
					mapsMapService.save(map);
					return map.getIdentification();
				} else {
					// create new id
					obj = createMapsLayers(obj, text, overwrite, user);
					return createNewMapsmap(obj, text, user);
				}
			}
		} else {
			// create new id
			obj = createMapsLayers(obj, text, overwrite, user);
			return createNewMapsmap(obj, text, user);
		}
	}

	private String createNewMapsmap(JSONObject obj, String text, User user) {
		MapsMap map = new MapsMap();
		map.setIdentification(randomIdentfication(text));
		obj.put("id", map.getIdentification());
		obj.put("description", "Imported for Maps project " + text);
		map.setDescription("Imported for Maps project " + text);
		map.setConfig(obj.toString());
		map.setUser(user);
		mapsMapService.save(map);
		return map.getIdentification();
	}

	private JSONObject createMapsLayers(JSONObject obj, String text, boolean overwrite, User user) {
		if (obj.has("services")) {
			JSONArray services = obj.getJSONArray("services");
			for (int i = 0; i < services.length(); i++) {
				JSONObject layer = services.getJSONObject(i);
				JSONObject layerLink = new JSONObject();
				int zIndex = 0;
				if (layer.has("zIndex")) {
					zIndex = layer.getInt("zIndex");
				}
				layerLink.put("id", createMapsLayer(layer, text, overwrite, user));
				layerLink.put("zIndex", zIndex);
				services.put(i, layerLink);
			}
			obj.put("services", services);
		}
		return obj;
	}

	// Create maps layers
	private String createMapsLayer(JSONObject obj, String text, boolean overwrite, User user) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			List<MapsLayer> layers = mapsLayerService.getByIdentifier(id);
			if (layers == null || layers.size() == 0) {
				// Not exist create
				obj = createMapsLayerStyles(obj, text, overwrite, user);
				return createNewMapsLayer(obj, text, user);
			} else {
				// exist
				if (overwrite) {
					MapsLayer ms = layers.get(0);
					obj = createMapsLayerStyles(obj, text, overwrite, user);
					ms.setConfig(obj.toString());
					ms.setUser(user);
					mapsLayerService.save(ms);
					return ms.getIdentification();
				} else {
					// create new id
					obj = createMapsLayerStyles(obj, text, overwrite, user);
					return createNewMapsLayer(obj, text, user);
				}
			}
		} else {
			// create new id
			obj = createMapsLayerStyles(obj, text, overwrite, user);
			return createNewMapsLayer(obj, text, user);
		}
	}

	private String createNewMapsLayer(JSONObject obj, String text, User user) {
		MapsLayer ml = new MapsLayer();
		ml.setIdentification(randomIdentfication(text));
		obj.put("id", ml.getIdentification());
		obj.put("description", "Imported for Maps project " + text);
		ml.setDescription("Imported for Maps project " + text);
		ml.setUser(user);
		ml.setConfig(obj.toString());
		mapsLayerService.save(ml);
		return ml.getIdentification();
	}

	private JSONObject createMapsLayerStyles(JSONObject obj, String text, boolean overwrite, User user) {

		if (obj.has("style")) {
			obj.put("style", createMapsStyle(obj.getJSONObject("style"), text, overwrite, user));
		}
		if (obj.has("styleSelect")) {
			obj.put("styleSelect", createMapsStyle(obj.getJSONObject("styleSelect"), text, overwrite, user));
		}
		return obj;
	}

	// Create maps styles
	private String createMapsStyle(JSONObject obj, String text, boolean overwrite, User user) {
		if (obj.has("id")) {
			String id = obj.getString("id");
			List<MapsStyle> styles = mapsStyleService.getByIdentifier(id);

			if (styles == null || styles.size() == 0) {
				// Not exist create
				return createNewStyle(obj, text, user);
			} else {
				// exist
				if (overwrite) {
					MapsStyle ms = styles.get(0);
					ms.setConfig(obj.toString());
					mapsStyleService.save(ms);
					return ms.getIdentification();
				} else {
					// create new id
					return createNewStyle(obj, text, user);
				}
			}

		} else {
			// create new id
			return createNewStyle(obj, text, user);
		}
	}

	private String createNewStyle(JSONObject obj, String text, User user) {
		MapsStyle ms = new MapsStyle();
		ms.setIdentification(randomIdentfication(text));
		obj.put("id", ms.getIdentification());
		obj.put("description", "Imported for Maps project " + text);
		ms.setDescription("Imported for Maps project " + text);
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

	private JSONObject loadMapProjectsStyles(JSONObject obj) {
		if (obj.has("mapConfig") && obj.getJSONObject("mapConfig").has("toolsOptions")) {

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("mesaureToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("mesaureToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("mesaureToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("mesaureToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByGeomToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByGeomToolOptions").has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
							.getJSONObject("selectByGeomToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("selectByGeomToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("showCoordToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("showCoordToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("showCoordToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("infoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("infoToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("infoToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("geocoderToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("geocoderToolOptions").getString("style")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
						.has("styleSelect")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("geocoderToolOptions")
							.put("styleSelect",
									loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
											.getJSONObject("geocoderToolOptions").getString("styleSelect")));

				}
			}

			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("routingToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").getString("style")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("stopStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("stopStyle", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").getString("stopStyle")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("blockStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("blockStyle", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").getString("blockStyle")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
						.has("routeStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("routingToolOptions")
							.put("routeStyle", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("routingToolOptions").getString("routeStyle")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("selectByAttrToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
						.getJSONObject("selectByAttrToolOptions").has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
							.getJSONObject("selectByAttrToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("selectByAttrToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("bufferToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("bufferToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("bufferToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("intersectToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("intersectToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("intersectToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("proximityToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("proximityToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("proximityToolOptions").getString("style")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("gotoToolOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("style")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
							.put("style", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("gotoToolOptions").getString("style")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions")
						.has("styleSelect")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("gotoToolOptions").put(
							"styleSelect", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("gotoToolOptions").getString("styleSelect")));

				}
			}
			if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").has("editionOptions")) {
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("modifyStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"modifyStyle", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions").getString("modifyStyle")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("addStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
							.put("addStyle", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions").getString("addStyle")));

				}
				if (obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions")
						.has("deleteStyle")) {
					obj.getJSONObject("mapConfig").getJSONObject("toolsOptions").getJSONObject("editionOptions").put(
							"deleteStyle", loadMapStyle(obj.getJSONObject("mapConfig").getJSONObject("toolsOptions")
									.getJSONObject("editionOptions").getString("deleteStyle")));

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
			obj.put("style", loadMapStyle(obj.getString("style")));
		}
		if (obj.has("styleSelect")) {
			obj.put("styleSelect", loadMapStyle(obj.getString("styleSelect")));
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
			return new JSONObject(ms.getConfig());
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
