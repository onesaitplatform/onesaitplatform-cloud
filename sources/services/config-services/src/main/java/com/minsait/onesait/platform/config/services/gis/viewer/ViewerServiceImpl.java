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
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.exceptions.ViewerServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class ViewerServiceImpl implements ViewerService {

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
	public Boolean hasUserViewPermission(String id, String userId, String userIdToken) {
		final User sessionUser = userService.getUser(userId);
		Viewer viewer = viewerRepository.findById(id);
		boolean isViewerPublic = viewer.isPublic();
		boolean isSessionUserAdmin = sessionUser.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString());
		boolean hasUserViewPerm;

		if (userIdToken != null) {
			hasUserViewPerm = isViewerPublic || isSessionUserAdmin || userIdToken.equals(viewer.getUser().getUserId());
		} else {
			hasUserViewPerm = isViewerPublic || isSessionUserAdmin || viewer.getUser().equals(sessionUser);
		}
		return hasUserViewPerm;
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

	private void metricsManagerLogControlPanelGisViewersCreation(String userId, String result) {
		if (null != this.metricsManager) {
			this.metricsManager.logControlPanelGisViewersCreation(userId, result);
		}
	}

}
