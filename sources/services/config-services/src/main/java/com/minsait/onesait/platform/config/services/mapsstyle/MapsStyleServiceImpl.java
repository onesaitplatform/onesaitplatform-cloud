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
package com.minsait.onesait.platform.config.services.mapsstyle;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.MapsStyle;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MapsStyleRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.MapsStyleServiceException;
import com.minsait.onesait.platform.config.services.mapsstyle.dto.MapsStyleDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MapsStyleServiceImpl implements MapsStyleService {

	@Autowired
	private MapsStyleRepository mapsStyleRepository;

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Lazy
	private OPResourceService resourceService;

	@Override
	@Transactional
	public List<MapsStyle> getStylesForUser(String userId, String identification) {
		final User user = userService.getUserNoCache(userId);
		List<MapsStyle> mapsStyles = new LinkedList<>();
		if (user.isAdmin()) {
			if (identification == null || identification.trim().length() == 0) {
				mapsStyles = mapsStyleRepository.findAll();
			} else {
				mapsStyles = mapsStyleRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null || identification.trim().length() == 0) {
				mapsStyles = mapsStyleRepository.findByUser(user);
			} else {
				mapsStyles = mapsStyleRepository.findByUserIdentificationContaining(user, identification);
			}
		}
		return mapsStyles;
	}

	@Override
	@Transactional
	public List<MapsStyleDTO> getStylesForUserWithEmpty(String userId, String identification) {
		final User user = userService.getUserNoCache(userId);
		List<MapsStyle> mapsStyles = new LinkedList<>();
		if (user.isAdmin()) {
			if (identification == null || identification.trim().length() == 0) {
				mapsStyles = mapsStyleRepository.findAll();
			} else {
				mapsStyles = mapsStyleRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null || identification.trim().length() == 0) {
				mapsStyles = mapsStyleRepository.findByUser(user);
			} else {
				mapsStyles = mapsStyleRepository.findByUserIdentificationContaining(user, identification);
			}
		}
		mapsStyles.add(0, new MapsStyle());
		return mapArrayToDTO(mapsStyles);
	}

	private List<MapsStyleDTO> mapArrayToDTO(List<MapsStyle> mapsStyles) {

		List<MapsStyleDTO> mapsStyleDTOs = new LinkedList<>();

		if (mapsStyles != null && mapsStyles.size() > 0) {
			for (Iterator iterator = mapsStyles.iterator(); iterator.hasNext();) {
				MapsStyle mapsStyle = (MapsStyle) iterator.next();
				mapsStyleDTOs.add(mapToDTO(mapsStyle));

			}
		}
		return mapsStyleDTOs;

	}

	private MapsStyleDTO mapToDTO(MapsStyle mapsStyle) {
		MapsStyleDTO dto = new MapsStyleDTO();
		dto.setConfig(mapsStyle.getConfig());
		dto.setDescription(mapsStyle.getDescription());
		dto.setId(mapsStyle.getId());
		dto.setIdentification(mapsStyle.getIdentification());
		dto.setCreatedAt(mapsStyle.getCreatedAt());
		dto.setUpdatedAt(mapsStyle.getUpdatedAt());
		if (mapsStyle.getUser() != null) {
			dto.setUser(mapsStyle.getUser().getUserId());
		}
		return dto;

	}

	@Override
	@Transactional
	public List<MapsStyle> getByIdentifier(String identification) {

		return mapsStyleRepository.findByIdentification(identification);
	}

	@Override
	@Transactional
	public MapsStyle getById(String identification) {

		return mapsStyleRepository.findById(identification).orElse(null);
	}

	@Override
	@Transactional
	public MapsStyle getByIdANDUser(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			return mapsStyleRepository.findById(id).orElse(null);
		}
		throw new MapsStyleServiceException("Cannot getMapsStyle does not exist or don't have permission");
	}

	@Override
	public void save(MapsStyle mapsStyle) {
		try {
			Date date = new Date();
			mapsStyle.setCreatedAt(date);
			mapsStyle.setUpdatedAt(date);
			mapsStyleRepository.save(mapsStyle);
		} catch (final Exception e) {
			throw new MapsStyleServiceException("Cannot create MapsStyle");
		}
	}

	@Override
	public void update(MapsStyle mapsStyle) {
		if (exists(mapsStyle)) {
			final MapsStyle mapStyleDB = mapsStyleRepository.findById(mapsStyle.getId()).orElse(new MapsStyle());
			if (mapStyleDB.getIdentification() == null) {
				mapStyleDB.setIdentification(mapsStyle.getIdentification());
			}
			mapStyleDB.setConfig(mapsStyle.getConfig());
			mapStyleDB.setDescription(mapsStyle.getDescription());
			mapStyleDB.setCreatedAt(mapsStyle.getCreatedAt());
			mapStyleDB.setUpdatedAt(new Date());
			mapsStyleRepository.save(mapStyleDB);
		} else {
			throw new MapsStyleServiceException("Cannot update MapsStyle that does not exist");
		}

	}

	@Override
	public boolean exists(MapsStyle mapsStyle) {
		return mapsStyleRepository.findByIdentification(mapsStyle.getIdentification()) != null;

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (mapsStyleRepository.findById(id).isPresent()) {
			return mapsStyleRepository.findById(id).get().getUser().getUserId().equals(userId);
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
		if (!mapsStyleRepository.findById(id).isPresent())
			return null;
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)
				|| mapsStyleRepository.findById(id).get().getUser().getUserId().equals(userId)
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
			final MapsStyle mapsStyle = mapsStyleRepository.findById(id).orElse(null);
			if (mapsStyle != null) {
				mapsStyleRepository.delete(mapsStyle);
			} else {
				throw new MapsStyleServiceException("Cannot delete MapsStyle that does not exist");
			}
		}

	}

	@Override
	public String clone(MapsStyle originalMapsStyle, String identification, User user) {
		final MapsStyle cloneMapsStyle = new MapsStyle();

		try {
			Date date = new Date();
			cloneMapsStyle.setIdentification(identification);
			cloneMapsStyle.setUser(user);
			cloneMapsStyle.setConfig(originalMapsStyle.getConfig());
			cloneMapsStyle.setDescription(originalMapsStyle.getDescription());
			cloneMapsStyle.setCreatedAt(date);
			cloneMapsStyle.setUpdatedAt(date);
			mapsStyleRepository.save(cloneMapsStyle);
			return cloneMapsStyle.getId();
		} catch (final Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

}
