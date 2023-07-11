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
package com.minsait.onesait.platform.config.services.mapslayer;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.MapsLayer;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MapsLayerRepository;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.MapsLayerServiceException;
import com.minsait.onesait.platform.config.services.mapslayer.dto.MapsLayerDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MapsLayerServiceImpl implements MapsLayerService {

	@Autowired
	private MapsLayerRepository mapsLayerRepository;
	@Autowired
	private ProjectResourceAccessRepository projectResourceAccessRepository;

	@Autowired
	private UserService userService;
	@Autowired
	@Lazy
	private OPResourceService resourceService;
	@Autowired
	private UserRepository userRepository;

	@Override
	@Transactional
	public List<MapsLayerDTO> getLayersForUser(String userId, String identification) {
		final User user = userService.getUserNoCache(userId);
		List<MapsLayer> mapsLayers = new LinkedList<>();
		if (user.isAdmin()) {
			if (identification == null || identification.trim().length() == 0) {
				mapsLayers = mapsLayerRepository.findAll();
			} else {
				mapsLayers = mapsLayerRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null || identification.trim().length() == 0) {
				mapsLayers = mapsLayerRepository.findByUser(user);
			} else {
				mapsLayers = mapsLayerRepository.findByUserIdentificationContaining(user, identification);
			}
		}
		return mapArrayToDTO(mapsLayers);
	}

	private List<MapsLayerDTO> mapArrayToDTO(List<MapsLayer> mapsLayers) {

		List<MapsLayerDTO> mapsLayerDTOs = new LinkedList<>();

		if (mapsLayers != null && mapsLayers.size() > 0) {
			for (Iterator iterator = mapsLayers.iterator(); iterator.hasNext();) {
				MapsLayer mapsLayer = (MapsLayer) iterator.next();
				mapsLayerDTOs.add(mapToDTO(mapsLayer));

			}
		}
		return mapsLayerDTOs;

	}

	private MapsLayerDTO mapToDTO(MapsLayer mapsLayer) {
		MapsLayerDTO dto = new MapsLayerDTO();
		dto.setConfig(mapsLayer.getConfig());
		dto.setDescription(mapsLayer.getDescription());
		dto.setId(mapsLayer.getId());
		dto.setIdentification(mapsLayer.getIdentification());
		dto.setCreatedAt(mapsLayer.getCreatedAt());
		dto.setUpdatedAt(mapsLayer.getUpdatedAt());
		if (mapsLayer.getUser() != null) {
			dto.setUser(mapsLayer.getUser().getUserId());
		}
		return dto;
	}

	@Override
	@Transactional
	public List<MapsLayer> getByIdentifier(String identification) {

		return mapsLayerRepository.findByIdentification(identification);
	}

	@Override
	@Transactional
	public MapsLayer getById(String identification) {

		return mapsLayerRepository.findById(identification).orElse(null);
	}

	@Override
	@Transactional
	public MapsLayer getByIdANDUser(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			return mapsLayerRepository.findById(id).orElse(null);
		}
		throw new MapsLayerServiceException("Cannot get MapsLayer does not exist or don't have permission");
	}

	@Override
	public void save(MapsLayer mapsLayer) {
		try {
			Date date = new Date();
			mapsLayer.setCreatedAt(date);
			mapsLayer.setUpdatedAt(date);
			mapsLayerRepository.save(mapsLayer);
		} catch (final Exception e) {
			throw new MapsLayerServiceException("Cannot create MapsLayer");
		}
	}

	@Override
	public void update(MapsLayer mapsLayer) {
		if (exists(mapsLayer)) {
			final MapsLayer mapStyleDB = mapsLayerRepository.findById(mapsLayer.getId()).orElse(new MapsLayer());
			if (mapStyleDB.getIdentification() == null) {
				mapStyleDB.setIdentification(mapsLayer.getIdentification());
			}
			mapStyleDB.setConfig(mapsLayer.getConfig());
			mapStyleDB.setDescription(mapsLayer.getDescription());
			mapStyleDB.setCreatedAt(mapsLayer.getCreatedAt());
			mapStyleDB.setUpdatedAt(new Date());
			mapsLayerRepository.save(mapStyleDB);
		} else {
			throw new MapsLayerServiceException("Cannot update MapsLayer that does not exist");
		}

	}

	@Override
	public boolean exists(MapsLayer mapsLayer) {
		return mapsLayerRepository.findByIdentification(mapsLayer.getIdentification()) != null;

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (mapsLayerRepository.findById(id).isPresent()) {
			return mapsLayerRepository.findById(id).get().getUser().getUserId().equals(userId);
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
		if (!mapsLayerRepository.findById(id).isPresent())
			return null;
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)
				|| mapsLayerRepository.findById(id).get().getUser().getUserId().equals(userId)
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
			final MapsLayer mapsLayer = mapsLayerRepository.findById(id).orElse(null);
			if (mapsLayer != null) {
				mapsLayerRepository.delete(mapsLayer);
			} else {
				throw new MapsLayerServiceException("Cannot delete MapsLayer that does not exist");
			}
		}

	}

	@Override
	public String clone(MapsLayer originalMapsLayer, String identification, User user) {
		final MapsLayer cloneMapsLayer = new MapsLayer();

		try {
			Date date = new Date();
			cloneMapsLayer.setIdentification(identification);
			cloneMapsLayer.setUser(user);
			cloneMapsLayer.setConfig(originalMapsLayer.getConfig());
			cloneMapsLayer.setDescription(originalMapsLayer.getDescription());
			cloneMapsLayer.setCreatedAt(date);
			cloneMapsLayer.setUpdatedAt(date);
			mapsLayerRepository.save(cloneMapsLayer);
			return cloneMapsLayer.getId();
		} catch (final Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

}
