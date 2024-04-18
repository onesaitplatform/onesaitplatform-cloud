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
package com.minsait.onesait.platform.config.services.gis.viewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.BaseLayer;
import com.minsait.onesait.platform.config.model.Layer;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.BaseLayerRepository;
import com.minsait.onesait.platform.config.repository.LayerRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.exceptions.ViewerServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class ViewerServiceImpl implements ViewerService {

	private static final String ANONYMOUSUSER = "anonymousUser";

	@Autowired
	private UserService userService;

	@Autowired
	private ViewerRepository viewerRepository;

	@Autowired
	private LayerRepository layerRepository;

	@Autowired
	OntologyRepository ontologyRepository;

	@Autowired
	BaseLayerRepository baseLayerRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	@Override
	public List<Viewer> findAllViewers(String userId) {
		List<Viewer> viewers;
		final User sessionUser = userService.getUser(userId);

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			viewers = viewerRepository.findAll();
		} else {
			viewers = viewerRepository.findByIsPublicTrueOrUser(sessionUser);
		}

		return viewers;
	}

	@Override
	public List<BaseLayer> findAllBaseLayers() {
		return baseLayerRepository.findAll();
	}

	@Override
	public List<BaseLayer> getBaseLayersByTechnology(String technology) {
		return baseLayerRepository.findByTechnologyOrderByIdentificationAsc(technology);
	}

	@Override
	public Viewer create(Viewer viewer, String baseMap) {

		BaseLayer baseLayer = baseLayerRepository.findByIdentification(baseMap).get(0);
		viewer.setBaseLayer(baseLayer);

		this.metricsManagerLogControlPanelGisViewersCreation(viewer.getUser().getUserId(), "OK");
		return viewerRepository.save(viewer);
	}

	@Override
	public Boolean checkExist(Viewer viewer) {

		if (!viewerRepository.findByIdentification(viewer.getIdentification()).isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public Boolean hasUserViewPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);

		if (viewerRepository.findById(id).isPublic()) {
			return true;
		} else if (userId.equals(ANONYMOUSUSER) || user == null) {
			return viewerRepository.findById(id).isPublic();
		} else if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return true;
		} else {
			final boolean propietary = viewerRepository.findById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}
			return false;
		}
	}

	@Override
	public Viewer getViewerById(String id, String userId) {
		final User sessionUser = userService.getUser(userId);
		Viewer viewer = viewerRepository.findById(id);

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| viewer.getUser().equals(sessionUser) || viewer.isPublic()) {
			return viewer;
		} else {
			throw new ViewerServiceException("The user is not authorized");
		}
	}

	@Override
	public Viewer getViewerPublicById(String id) {
		return viewerRepository.findById(id);
	}

	@Override
	public void deleteViewer(Viewer viewer, String userId) {
		final User sessionUser = userService.getUser(userId);

		if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| viewer.getUser().equals(sessionUser) || viewer.isPublic()) {

			for (Layer layer : viewer.getLayers()) {
				layer.getViewers().remove(viewer);
				layerRepository.save(layer);
			}

			viewerRepository.delete(viewer);
		} else {
			throw new ViewerServiceException("The user is not authorized");
		}
	}

	@Override
	public List<Viewer> checkAllViewerByCriteria(String userId, String identification, String description) {
		List<Viewer> allViewers = new ArrayList<>();
		final User sessionUser = userService.getUser(userId);

		if (identification != null && description != null) {
			allViewers = viewerRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
			if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				return allViewers;
			} else {
				return this.getViewersWithPermission(allViewers, userId);
			}
		} else if (identification != null) {
			allViewers = viewerRepository.findByIdentificationContaining(identification);
			if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				return allViewers;
			} else {
				return this.getViewersWithPermission(allViewers, userId);
			}
		} else {
			allViewers = viewerRepository.findByDescriptionContaining(description);
			if (sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				return allViewers;
			} else {
				return this.getViewersWithPermission(allViewers, userId);
			}
		}
	}

	private List<Viewer> getViewersWithPermission(List<Viewer> allViewers, String userId) {
		List<Viewer> viewers = new ArrayList<>();
		for (Viewer viewer : allViewers) {
			if (this.hasUserViewPermission(viewer.getId(), userId)) {
				viewers.add(viewer);
			}
		}
		return viewers;
	}

	private void metricsManagerLogControlPanelGisViewersCreation(String userId, String result) {
		if (null != this.metricsManager) {
			this.metricsManager.logControlPanelGisViewersCreation(userId, result);
		}
	}

}
