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
package com.minsait.onesait.platform.config.services.webproject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.services.webproject.NPMCommandResult.NPMCommandResultStatus;

public interface WebProjectService {

	List<WebProjectDTO> getWebProjectsWithDescriptionAndIdentification(String userId, String identification,
			String description);

	List<String> getWebProjectsIdentifications(String userId);

	List<String> getAllIdentifications();

	void createWebProject(WebProjectDTO webProject, String userId);

	void updateWebProject(WebProjectDTO webProject, String userId);

	WebProjectDTO getWebProjectById(String webProjectId, String userId);

	WebProjectDTO getWebProjectByName(String name, String userId);

	List<WebProjectDTO> getAllWebProjects();

	void deleteWebProjectById(String webProjectId, String userId);

	void uploadZip(MultipartFile file, String userId);

	void uploadWebTemplate(String userId);

	void deleteWebProject(String webProjectId, String userId);

	boolean webProjectExists(String identification);

	byte[] downloadZip(String file, String userId);

	void loadGitDetails(WebProjectDTO web);

	void uploadZip(File file, String userId);

	void unzipFile(String path, String fileName);

	void deleteFolder(String path);

	void compileNPM(WebProjectDTO web, String userId) throws IOException;

	void resetCurrentStatus();

	String getCurrentStatus();

	NPMCommandResultStatus getNpmStatus();

	void setNpmInstall(boolean val);

	boolean isNpmInstall();

	void cloneGitAndDownload(WebProjectDTO webProject, RestTemplate template, HttpEntity<?> httpEntity, String url,
			String urlDelete, String userId);

}
