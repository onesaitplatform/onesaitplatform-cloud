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
package com.minsait.onesait.platform.controlpanel.services.codeproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.CodeProject;
import com.minsait.onesait.platform.config.services.codeproject.CodeProjectService;
import com.minsait.onesait.platform.config.services.codeproject.dto.CodeProjectDTO;
import com.minsait.onesait.platform.config.services.exceptions.CodeProjectException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.git.GitlabConfiguration;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CodeprojectBusinessServiceImpl implements CodeprojectBusinessService {

	@Autowired
	private CodeProjectService codeprojectService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AppWebUtils utils;
	
	@Override
	public CodeProject createCodeproject(CodeProjectDTO codeproject) {
		
		final CodeProject project = new CodeProject();
		project.setIdentification(codeproject.getName());
		project.setName(codeproject.getName());
		project.setActive(true);
		project.setUser(userService.getUser(utils.getUserId()));
		try {
			final GitlabConfiguration gitConfig = new GitlabConfiguration();
			gitConfig.setSite(codeproject.getRepo());
			gitConfig.setPrivateToken(codeproject.getPrivateToken());
			gitConfig.setUser(codeproject.getUsername());
			project.setGitlabConfiguration(gitConfig);
		} catch (final Exception e) {
			log.error("Error while creating Gitlab repository");
			throw new CodeProjectException("Could not create gitlab project " + e.getMessage());
		}
		return codeprojectService.create(project);
	}
	
	@Override
	public void deleteCodeproject(CodeProject codeproject) {
		codeprojectService.delete(codeproject);
	}
}
