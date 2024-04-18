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
package com.minsait.onesait.platform.config.services.gis.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.BaseLayer;
import com.minsait.onesait.platform.config.model.Layer;
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

		if (sessionUser.isAdmin()) {
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

		final BaseLayer baseLayer = baseLayerRepository.findByIdentification(baseMap).get(0);
		viewer.setBaseLayer(baseLayer);

		metricsManagerLogControlPanelGisViewersCreation(viewer.getUser().getUserId(), "OK");
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
		final Optional<Viewer> opt = viewerRepository.findById(id);
		if (!opt.isPresent())
			return false;
		final Viewer viewer = opt.get();
		if (viewer.isPublic()) {
			return true;
		} else if (userId.equals(ANONYMOUSUSER) || user == null) {
			return viewer.isPublic();
		} else if (user.isAdmin()) {
			return true;
		} else {
			return viewer.getUser().getUserId().equals(userId);
		}
	}

	@Override
	public Viewer getViewerById(String id, String userId) {
		final User sessionUser = userService.getUser(userId);
		final Viewer viewer = viewerRepository.findById(id).orElse(null);

		if (sessionUser.isAdmin() || viewer.getUser().equals(sessionUser) || viewer.isPublic()) {
			return viewer;
		} else {
			throw new ViewerServiceException("The user is not authorized");
		}
	}

	@Override
	public Viewer getViewerPublicById(String id) {
		return viewerRepository.findById(id).orElse(null);
	}

	@Override
	public void deleteViewer(Viewer viewer, String userId) {
		final User sessionUser = userService.getUser(userId);

		if (sessionUser.isAdmin() || viewer.getUser().equals(sessionUser) || viewer.isPublic()) {

			for (final Layer layer : viewer.getLayers()) {
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
			if (sessionUser.isAdmin()) {
				return allViewers;
			} else {
				return getViewersWithPermission(allViewers, userId);
			}
		} else if (identification != null) {
			allViewers = viewerRepository.findByIdentificationContaining(identification);
			if (sessionUser.isAdmin()) {
				return allViewers;
			} else {
				return getViewersWithPermission(allViewers, userId);
			}
		} else {
			allViewers = viewerRepository.findByDescriptionContaining(description);
			if (sessionUser.isAdmin()) {
				return allViewers;
			} else {
				return getViewersWithPermission(allViewers, userId);
			}
		}
	}

	private List<Viewer> getViewersWithPermission(List<Viewer> allViewers, String userId) {
		final List<Viewer> viewers = new ArrayList<>();
		for (final Viewer viewer : allViewers) {
			if (hasUserViewPermission(viewer.getId(), userId)) {
				viewers.add(viewer);
			}
		}
		return viewers;
	}

	private void metricsManagerLogControlPanelGisViewersCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelGisViewersCreation(userId, result);
		}
	}

	@Override
	public Viewer getViewerByIdentification(String identification) {
		final List<Viewer> viewerList = viewerRepository.findByIdentification(identification);
		if (viewerList != null && !viewerList.isEmpty())
			return viewerList.get(0);
		return null;
	}

}
