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
package com.minsait.onesait.platform.config.services.gadgetfavorite;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetFavorite;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.GadgetDatasourceRepository;
import com.minsait.onesait.platform.config.repository.GadgetFavoriteRepository;
import com.minsait.onesait.platform.config.repository.GadgetRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.GadgetFavoriteServiceException;
import com.minsait.onesait.platform.config.services.gadgettemplate.GadgetTemplateService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GadgetFavoriteServiceImpl implements GadgetFavoriteService {

	@Autowired
	private GadgetRepository gadgetRepository;

	@Autowired
	private GadgetFavoriteRepository gadgetFavoriteRepository;
	@Autowired
	private GadgetDatasourceRepository gadgetDatasourceRepository;

	@Autowired
	private GadgetTemplateService gadgetTemplateService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	private static final String DATASOURCE_NOT_EXIST = "datasource does not exist";
	private static final String GADGET_NOT_EXIST = "gadget does not exist";
	private static final String GADGETTEMPLATE_NOT_EXIST = "gadget template does not exist";

	@Override
	public void create(String identification, String idGadget, String idTemplate, String idDatasource, String type,
			String config, String metainf, String userId) {

		if (null == gadgetFavoriteRepository.findByIdentification(identification)) {
			GadgetFavorite gadgetFavorite = new GadgetFavorite();
			if (null != idDatasource && idDatasource.length() > 0) {
				GadgetDatasource gadgetDatasource = gadgetDatasourceRepository.findByIdentification(idDatasource);
				if (gadgetDatasource == null) {
					throw new GadgetFavoriteServiceException(DATASOURCE_NOT_EXIST);
				}
				gadgetFavorite.setDatasource(gadgetDatasource);
			}
			if (null != idGadget && idGadget.length() > 0) {
				Gadget gadget = gadgetRepository.findById(idGadget).orElse(null);
				if (gadget == null) {
					throw new GadgetFavoriteServiceException(GADGET_NOT_EXIST);
				}
				gadgetFavorite.setGadget(gadget);
			}
			if (null != idTemplate && idTemplate.length() > 0) {
				GadgetTemplate gagetTemplate = gadgetTemplateService.getGadgetTemplateByIdentification(idTemplate);
				if (gagetTemplate == null) {
					throw new GadgetFavoriteServiceException(GADGETTEMPLATE_NOT_EXIST);
				}
				gadgetFavorite.setGadgetTemplate(gagetTemplate);
			}
			gadgetFavorite.setIdentification(identification);
			gadgetFavorite.setType(type);
			gadgetFavorite.setConfig(config);
			final User user = userRepository.findByUserId(userId);
			gadgetFavorite.setUser(user);
			gadgetFavorite.setMetainf(metainf);
			gadgetFavoriteRepository.save(gadgetFavorite);
		}
	}

	@Override
	public void delete(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (identification != null) {
			List<GadgetFavorite> gadgets;

			if (userService.isUserAdministrator(user)) {
				gadgets = gadgetFavoriteRepository.findByIdentificationContaining(identification);
			} else {
				gadgets = gadgetFavoriteRepository.findByUserAndIdentificationContaining(user, identification);
			}
			for (Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
				GadgetFavorite gadgetFavorite = (GadgetFavorite) iterator.next();
				if (gadgetFavorite.getIdentification().equals(identification)) {
					gadgetFavoriteRepository.delete(gadgetFavorite);
					break;
				}
			}
		}
	}
	
	@Override
	public void deleteByUserId(String userlogged, String userId) {
		final User user = userRepository.findByUserId(userId);
		User userInvoker = userRepository.findByUserId(userlogged);
		if (user == null) {
			throw new GadgetFavoriteServiceException("The user entered does not exist");
		}
		
		List<GadgetFavorite> gadgets;
		
		if (userService.isUserAdministrator(userInvoker)) {
			gadgets = gadgetFavoriteRepository.findByUser(user);
		} else {
			if (!userId.equals(userlogged)) {
				throw new GadgetFavoriteServiceException("Operation failed, you do not have administrator permission");
			}
			gadgets = gadgetFavoriteRepository.findByUser(userInvoker);
		}
			
		for (Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
			GadgetFavorite gadgetFavorite = (GadgetFavorite) iterator.next();
			gadgetFavoriteRepository.delete(gadgetFavorite);
		}
		
		if (gadgets.size() == 0) {
			throw new GadgetFavoriteServiceException("Operation failed, you don't have any favorite gadget");
		}
	}

	@Override
	public void update(String identification, String idGadget, String idTemplate, String idDatasource, String type,
			String config, String metainf, String userId) {
		GadgetFavorite gadgetFavorite = gadgetFavoriteRepository.findByIdentification(identification);
		if (null != gadgetFavorite) {
			if (null != idDatasource && idDatasource.length() > 0) {
				GadgetDatasource gadgetDatasource = gadgetDatasourceRepository.findByIdentification(idDatasource);
				if (gadgetDatasource == null) {
					throw new GadgetFavoriteServiceException(DATASOURCE_NOT_EXIST);
				}
				gadgetFavorite.setDatasource(gadgetDatasource);
			} else {
				gadgetFavorite.setDatasource(null);
			}
			if (null != idGadget && idGadget.length() > 0) {
				Gadget gadget = gadgetRepository.findById(idGadget).orElse(null);
				if (gadget == null) {
					throw new GadgetFavoriteServiceException(GADGET_NOT_EXIST);
				}
				gadgetFavorite.setGadget(gadget);
			} else {
				gadgetFavorite.setGadget(null);
			}
			if (null != idTemplate && idTemplate.length() > 0) {
				GadgetTemplate gagetTemplate = gadgetTemplateService.getGadgetTemplateByIdentification(idTemplate);
				if (gagetTemplate == null) {
					throw new GadgetFavoriteServiceException(GADGETTEMPLATE_NOT_EXIST);
				}
				gadgetFavorite.setGadgetTemplate(gagetTemplate);
			} else {
				gadgetFavorite.setGadgetTemplate(null);
			}
			gadgetFavorite.setType(type);
			gadgetFavorite.setConfig(config);
			gadgetFavorite.setMetainf(metainf);
			gadgetFavoriteRepository.save(gadgetFavorite);
		}

	}

	@Override
	public Boolean existWithIdentification(String identification) {
		if (identification != null) {
			List<GadgetFavorite> gadgets = gadgetFavoriteRepository.existByIdentification(identification);
			;
			if (!gadgets.isEmpty()) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public List<GadgetFavorite> findAll(String userlogged) {	
		final User user = userRepository.findByUserId(userlogged);
		return gadgetFavoriteRepository.findByUser(user);
	}
	
	@Override
	public List<GadgetFavorite> findAllGadgetFavorite(String userlogged, String userId) {	

		User userInvoker = userRepository.findByUserId(userlogged);
		
		List<GadgetFavorite> gadgets;
		
		if ( userId == null && userService.isUserAdministrator(userInvoker)) {
			gadgets = gadgetFavoriteRepository.findAllByOrderByIdentificationAsc();
		} else if (userService.isUserAdministrator(userInvoker)) {
			User user = userRepository.findByUserId(userId);
			gadgets = gadgetFavoriteRepository.findByUser(user);
		} else if(!userService.isUserAdministrator(userInvoker) && userId != null) { 
			throw new GadgetFavoriteServiceException("If you are not an administrator, do not enter any UserId, you will only be able to see yours");
		}else {
			gadgets = gadgetFavoriteRepository.findByUser(userInvoker);
		}
		
		final List<GadgetFavorite> identifications = new ArrayList<>();
		for (Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
			GadgetFavorite gadgetFavorite = (GadgetFavorite) iterator.next();
			identifications.add(gadgetFavorite);
		}
		
		return identifications;
	}

	@Override
	public List<String> getAllIdentifications(String userlogged, String userId) {
		
		User userInvoker = userRepository.findByUserId(userlogged);
		
		List<GadgetFavorite> gadgets;
		
		if ( userId == null && userService.isUserAdministrator(userInvoker)) {
			gadgets = gadgetFavoriteRepository.findAllByOrderByIdentificationAsc();
		} else if (userService.isUserAdministrator(userInvoker)) {
			User user = userRepository.findByUserId(userId);
			gadgets = gadgetFavoriteRepository.findByUser(user);
		} else if(!userService.isUserAdministrator(userInvoker) && userId != null) { 
			throw new GadgetFavoriteServiceException("If you are not an administrator, do not enter any UserId, you will only be able to see yours");
		}else {
			gadgets = gadgetFavoriteRepository.findByUser(userInvoker);
		}
		
		final List<String> identifications = new ArrayList<>();
		for (Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
			GadgetFavorite gadgetFavorite = (GadgetFavorite) iterator.next();
			identifications.add(gadgetFavorite.getIdentification());
		}
		java.util.Collections.sort(identifications, Collator.getInstance());
		return identifications;
	}

	@Override
	public GadgetFavorite findByIdentification(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		
		if (identification != null) {
			
			List<GadgetFavorite> gadgets;

			if (userService.isUserAdministrator(user)) {
				gadgets = gadgetFavoriteRepository.findByIdentificationContaining(identification);
			} else {
				gadgets = gadgetFavoriteRepository.findByUserAndIdentificationContaining(user, identification);
			}
			
			for (Iterator iterator = gadgets.iterator(); iterator.hasNext();) {
				GadgetFavorite gadgetFavorite = (GadgetFavorite) iterator.next();
				if (gadgetFavorite.getIdentification().equals(identification)) {
					return gadgetFavorite;
				}
			}
		}
		return null;
	}

	@Override
	public GadgetFavorite findById(String id, String userId) {
		if (id != null) {
			GadgetFavorite gadgetFavorite = gadgetFavoriteRepository.getOne(id);
			if (gadgetFavorite.getUser().getUserId().equals(userId)) {
				return gadgetFavorite;
			}
		}
		return null;
	}

}
