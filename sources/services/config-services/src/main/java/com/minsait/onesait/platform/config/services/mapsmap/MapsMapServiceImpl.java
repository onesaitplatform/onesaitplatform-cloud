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
package com.minsait.onesait.platform.config.services.mapsmap;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.MapsMap;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MapsMapRepository;
import com.minsait.onesait.platform.config.repository.ProjectResourceAccessRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.MapsMapServiceException;
import com.minsait.onesait.platform.config.services.mapsmap.dto.MapsMapDTO;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MapsMapServiceImpl implements MapsMapService {

	@Autowired
	private MapsMapRepository mapsMapRepository;
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
	public List<MapsMapDTO> getMapsForUser(String userId, String identification) {
		final User user = userService.getUserNoCache(userId);
		List<MapsMap> mapsMaps = new LinkedList<>();
		if (user.isAdmin()) {
			if (identification == null || identification.trim().length() == 0) {
				mapsMaps = mapsMapRepository.findAll();
			} else {
				mapsMaps = mapsMapRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null || identification.trim().length() == 0) {
				mapsMaps = mapsMapRepository.findByUser(user);
			} else {
				mapsMaps = mapsMapRepository.findByUserIdentificationContaining(user, identification);
			}
		}

		return mapArrayToDTO(mapsMaps);
	}

	private List<MapsMapDTO> mapArrayToDTO(List<MapsMap> mapsMaps) {

		List<MapsMapDTO> mapsMapDTOs = new LinkedList<>();

		if (mapsMaps != null && mapsMaps.size() > 0) {
			for (Iterator iterator = mapsMaps.iterator(); iterator.hasNext();) {
				MapsMap mapsMap = (MapsMap) iterator.next();
				mapsMapDTOs.add(mapToDTO(mapsMap));

			}
		}
		return mapsMapDTOs;

	}

	private MapsMapDTO mapToDTO(MapsMap mapsMap) {
		MapsMapDTO dto = new MapsMapDTO();
		dto.setConfig(mapsMap.getConfig());
		dto.setDescription(mapsMap.getDescription());
		dto.setId(mapsMap.getId());
		dto.setIdentification(mapsMap.getIdentification());
		dto.setCreatedAt(mapsMap.getCreatedAt());
		dto.setUpdatedAt(mapsMap.getUpdatedAt());
		if (mapsMap.getUser() != null) {
			dto.setUser(mapsMap.getUser().getUserId());
		}
		return dto;

	}

	@Override
	@Transactional
	public List<MapsMap> getByIdentifier(String identification) {

		return mapsMapRepository.findByIdentification(identification);
	}

	@Override
	@Transactional
	public MapsMap getByIdentificationANDUser(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (mapsMapRepository.findByUserIdentificationContaining(user, identification).size() > 0) {
			return mapsMapRepository.findByUserIdentificationContaining(user, identification).get(0);
		} else {
			return null;
		}
	}

	@Override
	@Transactional
	public MapsMap getById(String identification) {

		return mapsMapRepository.findById(identification).orElse(null);
	}

	@Override
	@Transactional
	public MapsMap getByIdANDUser(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			return mapsMapRepository.findById(id).orElse(null);
		}
		throw new MapsMapServiceException("Cannot get MapsMap does not exist or don't have permission");
	}

	@Override
	public void save(MapsMap mapsMap) {
		try {
			Date date = new Date();
			mapsMap.setCreatedAt(date);
			mapsMap.setUpdatedAt(date);
			addIds(mapsMap);
			mapsMapRepository.save(mapsMap);
		} catch (final Exception e) {
			throw new MapsMapServiceException("Cannot create MapsMap");
		}
	}

	private void addIds(MapsMap mapsMap) {
		JSONObject obj = new JSONObject(mapsMap.getConfig());
		if (obj.getString("idDiv") == null || obj.getString("idDiv").trim().length() == 0) {
			obj.put("idDiv", randomIdentfication("id"));
		}
		if (obj.get("optionView") == null) {
			JSONObject optionView = new JSONObject();
			optionView.put("id", randomIdentfication("id"));
			obj.put("optionView", optionView);
		} else if (obj.get("optionView") != null && (obj.getJSONObject("optionView").getString("id") == null
				|| obj.getJSONObject("optionView").getString("id").trim().length() == 0)) {
			obj.getJSONObject("optionView").put("id", randomIdentfication("id"));
		}
		mapsMap.setConfig(obj.toString());
	}

	@Override
	public void update(MapsMap mapsMap) {
		if (exists(mapsMap)) {
			final MapsMap mapStyleDB = mapsMapRepository.findById(mapsMap.getId()).orElse(new MapsMap());
			if (mapStyleDB.getIdentification() == null) {
				mapStyleDB.setIdentification(mapsMap.getIdentification());
			}
			addIds(mapsMap);
			mapStyleDB.setConfig(mapsMap.getConfig());
			mapStyleDB.setDescription(mapsMap.getDescription());
			mapStyleDB.setCreatedAt(mapsMap.getCreatedAt());
			mapStyleDB.setUpdatedAt(new Date());
			mapsMapRepository.save(mapStyleDB);
		} else {
			throw new MapsMapServiceException("Cannot update MapsMap that does not exist");
		}

	}

	@Override
	public boolean exists(MapsMap mapsMap) {
		return mapsMapRepository.findByIdentification(mapsMap.getIdentification()) != null;

	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else if (mapsMapRepository.findById(id).isPresent()) {
			return mapsMapRepository.findById(id).get().getUser().getUserId().equals(userId);
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
		if (!mapsMapRepository.findById(id).isPresent())
			return null;
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)
				|| mapsMapRepository.findById(id).get().getUser().getUserId().equals(userId)
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
			final MapsMap mapsMap = mapsMapRepository.findById(id).orElse(null);
			if (mapsMap != null) {
				mapsMapRepository.delete(mapsMap);
			} else {
				throw new MapsMapServiceException("Cannot delete MapsMap that does not exist");
			}
		}

	}

	@Override
	public String clone(MapsMap originalMapsMap, String identification, User user) {
		final MapsMap cloneMapsMap = new MapsMap();

		try {
			Date date = new Date();
			cloneMapsMap.setIdentification(identification);
			cloneMapsMap.setUser(user);
			cloneMapsMap.setConfig(originalMapsMap.getConfig());
			cloneMapsMap.setDescription(originalMapsMap.getDescription());
			cloneMapsMap.setCreatedAt(date);
			cloneMapsMap.setUpdatedAt(date);
			mapsMapRepository.save(cloneMapsMap);
			return cloneMapsMap.getId();
		} catch (final Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private String randomIdentfication(String text) {
		Random random = new Random();
		int rn = random.nextInt(999) + 0;
		return text + "-" + new Date().getTime() + rn;
	}

}
