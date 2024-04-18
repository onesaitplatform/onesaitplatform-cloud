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
package com.minsait.onesait.platform.config.services.mstemplates;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.config.model.MicroserviceTemplate;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.MicroserviceTemplateRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.exceptions.MicroserviceTemplateException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

@Service
public class MicroserviceTemplatesServiceImpl implements MicroserviceTemplatesService {

	@Autowired
	private MicroserviceTemplateRepository msTemplateRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	@Autowired
	@Lazy
	private OPResourceService resourceService;

	private static final String MSTEMPLATE_NOT_EXIST = "Microservice Template does not exist in the database";

	@Override
	public String createNewTemplate(MicroserviceTemplate template, String userId) {
		if (mstemplateExists(template.getIdentification()))
			throw new MicroserviceTemplateException("Microservice template already exists in Database");

		final MicroserviceTemplate mstemplateAux = getNewMsTemplate(template, userId);

		return mstemplateAux.getId();
	}

	private MicroserviceTemplate getNewMsTemplate(MicroserviceTemplate mstemplate, String userId) {
		final MicroserviceTemplate mst = new MicroserviceTemplate();

		mst.setDescription(mstemplate.getDescription());
		mst.setIdentification(mstemplate.getIdentification());
		mst.setDockerRelativePath(mstemplate.getDockerRelativePath());
		mst.setGitBranch(mstemplate.getGitBranch());
		mst.setGitPassword(mstemplate.getGitPassword());
		mst.setGitRepository(mstemplate.getGitRepository());
		mst.setGitUser(mstemplate.getGitUser());
		mst.setLanguage(mstemplate.getLanguage());
		mst.setPublic(mstemplate.isPublic());
		mst.setRelativePath(mstemplate.getRelativePath());
		mst.setUser(userService.getUserByIdentification(userId));
		mst.setGraalvm(mstemplate.isGraalvm());

		return msTemplateRepository.save(mst);
	}


	@Override
	public MicroserviceTemplate save(MicroserviceTemplate service) {
		return msTemplateRepository.save(service);
	}


	@Override
	public MicroserviceTemplate getById(String id) {
		return msTemplateRepository.findById(id).orElse(null);
	}
	
	@Override
	public MicroserviceTemplate getMsTemplateById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return msTemplateRepository.findById(id).orElse(null);
		}
		throw new MicroserviceTemplateException(
				"Cannot view Microservice Template that does not exist or don't have permission");
	}

	@Override
	public void delete(String id) {
		final MicroserviceTemplate msTemplate = msTemplateRepository.findById(id).orElse(null);
		if (msTemplate != null) {
			msTemplateRepository.delete(msTemplate);
		}
	}

	@Override
	public boolean hasUserPermission(MicroserviceTemplate microservice, User user) {
		return (userService.isUserAdministrator(user) || microservice.getUser().equals(user));
	}

	@Override
	public MicroserviceTemplate getMsTemplateEditById(String id, String userId) {
		if (hasUserEditPermission(id, userId)) {
			return msTemplateRepository.findMicroserviceTemplateById(id);
		}
		throw new MicroserviceTemplateException("Cannot view Microservice Template that does not exist or don't have permission");
	}
	
	@Transactional
	@Override
	public String updateMsTemplate(MicroserviceTemplate mstemplate, String userId) {
		if (!mstemplateExists(mstemplate.getIdentification())) {
			throw new MicroserviceTemplateException(MSTEMPLATE_NOT_EXIST);
		} else {
			final MicroserviceTemplate d = msTemplateRepository.findMicroserviceTemplateById(mstemplate.getId());
			d.setDescription(mstemplate.getDescription());
			d.setDockerRelativePath(mstemplate.getDockerRelativePath());
			d.setGitBranch(mstemplate.getGitBranch());
			d.setGitPassword(mstemplate.getGitPassword());
			d.setGitRepository(mstemplate.getGitRepository());
			d.setGitUser(mstemplate.getGitUser());
			if(mstemplate.getLanguage() != null)
				d.setLanguage(mstemplate.getLanguage());
			d.setPublic(mstemplate.isPublic());
			d.setRelativePath(mstemplate.getRelativePath());
			d.setGraalvm(mstemplate.isGraalvm());
			return d.getId();
		}
	}

	@Override
	public List<String> getAllIdentificationsByUser(String userId) {
		User user = userService.getUser(userId);
		if (user.isAdmin()) {
			return msTemplateRepository.findAllIdentifications();
		} else {
			return msTemplateRepository.findAllIdentificationsByUser(user);
		}
	}
	
	@Override
	public List<MicroserviceTemplate> getAllMicroserviceTemplatesByUser(String userId) {
		User user = userService.getUser(userId);
		if (user.isAdmin()) {
			return msTemplateRepository.findAll();
		} else {
			return msTemplateRepository.findByUserOrderByIdentificationAsc(user);
		}
	}

	@Override
	public List<MicroserviceTemplate> getAllMicroserviceTemplatesByCriterials(String identification, String userId) {
		List<MicroserviceTemplate> msTemplates;

		identification = identification == null ? "" : identification;
		
		final User user = userRepository.findByUserId(userId);
		
		if (userService.isUserAdministrator(user)) {
			msTemplates = msTemplateRepository
					.findByIdentificationContaining(identification);
		} else {
			msTemplates = msTemplateRepository
					.findByUserAndIdentificationContainingOrderByIdentificationAsc(user, identification);
		}
		

		return msTemplates.stream().map(temp -> {
			final MicroserviceTemplate obj = new MicroserviceTemplate();
			obj.setCreatedAt(temp.getCreatedAt());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			obj.setUpdatedAt(temp.getUpdatedAt());
			obj.setUser(temp.getUser());
			return obj;
		}).collect(Collectors.toList());

	}
	
	@Override
	public boolean hasUserEditPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			final Optional<MicroserviceTemplate> opt = msTemplateRepository.findById(id);
			if (opt.isPresent()) {
				final boolean propietary = opt.get().getUser().getUserId().equals(userId);
				if (propietary) {
					return true;
				}
				return resourceService.hasAccess(userId, id, ResourceAccessType.MANAGE);
			} else {
				return false;
			}
		}
	}
	
	@Override
	public boolean mstemplateExists(String identification) {
		MicroserviceTemplate mstemplate = msTemplateRepository.findByIdentification(identification);
		if(mstemplate!=null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<MicroserviceTemplate> findMicroserviceTemplatesWithIdentificationAndDescription(String identification,
			String description, String user) {
		List<MicroserviceTemplate> mstemplates;

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		mstemplates = msTemplateRepository.findByIdentificationContainingAndDescriptionContaining(identification,
				description);

		return mstemplates.stream().map(temp -> {
			final MicroserviceTemplate obj = new MicroserviceTemplate();
			obj.setCreatedAt(temp.getCreatedAt());
			obj.setDescription(temp.getDescription());
			obj.setId(temp.getId());
			obj.setIdentification(temp.getIdentification());
			obj.setUpdatedAt(temp.getUpdatedAt());
			obj.setUser(temp.getUser());
			return obj;
		}).collect(Collectors.toList());
	}

	@Override
	public boolean hasUserViewPermission(String id, String userId) {
		final User user = userService.getUserByIdentification(userId);

		if (user.isAdmin()) {
			return true;
		} else {
			final boolean propietary = msTemplateRepository.findMicroserviceTemplateById(id).getUser().getUserId().equals(userId);
			if (propietary) {
				return true;
			}

			return resourceService.hasAccess(userId, id, ResourceAccessType.VIEW);
		}
	}

	@Override
	public String getCredentialsString(String userId) {
		final User user = userService.getUserByIdentification(userId);
		return user.getUserId();
	}

	@Override
	public MicroserviceTemplate getMsTemplateByIdentification(String identification, String userId) {
		identification = identification == null ? "" : identification;
		MicroserviceTemplate msTemplate = null;
		final User user = userRepository.findByUserId(userId);
		
		if (userService.isUserAdministrator(user)) {
			msTemplate = msTemplateRepository.findByIdentification(identification);
		} else {
			msTemplate = msTemplateRepository.findMicroserviceTemplateByIdentificationAndUser(identification, user);
		}
		return msTemplate;
	}
	
}
