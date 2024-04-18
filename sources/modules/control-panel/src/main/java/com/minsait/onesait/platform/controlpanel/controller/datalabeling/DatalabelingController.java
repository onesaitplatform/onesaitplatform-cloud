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
package com.minsait.onesait.platform.controlpanel.controller.datalabeling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.datalabeling.DatalabelingService;
import com.minsait.onesait.platform.config.services.datalabeling.dto.CloudStorageDTO;
import com.minsait.onesait.platform.config.services.datalabeling.dto.CloudStorageFullDTO;
import com.minsait.onesait.platform.config.services.datalabeling.dto.ProjectDTO;
import com.minsait.onesait.platform.config.services.objectstorage.ObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/datalabeling")
@Slf4j
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
public class DatalabelingController {
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private DatalabelingService datalabelingService;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private ObjectStorageService objectStorageService;
	@Autowired
	private UserService userService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	private static final String STATUS_STR = "status";

	@Value("${onesaitplatform.database.minio.access-key:access-key}")
	private String accessKey;

	@Value("${onesaitplatform.database.minio.secret-key:secret-key}")
	private String secretKey;

	@Value("${onesaitplatform.datalabeling.user:user}")
	private String datalabelingUser;

	@Value("${onesaitplatform.datalabeling.password:pass}")
	private String datalabelingPass;

	private String token;
	private String minioBaseUrl;

	@PostConstruct
	public void init() {
		try {
			final Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());
			minioBaseUrl = urls.getMinio().getBase();
			token = datalabelingService.getUserToken(datalabelingUser, datalabelingPass);
		} catch (final Exception e) {
			log.error("Could not initialize DatalabelingController", e);
		}
	}

	@RequestMapping(value = "/list", produces = "text/html")
	public String list(Model uiModel, HttpServletRequest request) {
		final List<ProjectDTO> projects = datalabelingService.findProjects(token);
		uiModel.addAttribute("projects", projects);
		return "datalabeling/list";

	}

	@RequestMapping(value = "/projects/{id}", produces = "text/html")
	public String openProjects(@PathVariable("id") String id, Model uiModel, HttpServletRequest request) {

		if (id != null && id.length() > 0) {
			uiModel.addAttribute("url", getDatalabelingUrl() + "projects/" + id + "/data?page=1");
		} else {
			uiModel.addAttribute("url", getDatalabelingUrl());
		}
		return "datalabeling/ui";

	}

	@RequestMapping(value = "/projectsset/{id}", produces = "text/html")
	public String openProjectsSettings(@PathVariable("id") String id, Model uiModel, HttpServletRequest request) {

		if (id != null && id.length() > 0) {
			uiModel.addAttribute("url", getDatalabelingUrl() + "projects/" + id + "/settings");
		} else {
			uiModel.addAttribute("url", getDatalabelingUrl());
		}
		return "datalabeling/ui";

	}

	@RequestMapping(value = "/createproject", produces = "text/html")
	public String createProject(Model uiModel, HttpServletRequest request) {

		final Integer id = datalabelingService.createProject(token);
		uiModel.addAttribute("url", getDatalabelingUrl() + "projects/" + id + "/settings");

		return "datalabeling/ui";

	}

	@RequestMapping(value = "/projects", produces = "text/html")
	public String openAllProjects(Model uiModel, HttpServletRequest request) {
		uiModel.addAttribute("url", getDatalabelingUrl());
		return "datalabeling/ui";

	}

	@RequestMapping(value = "/cloudstoragessource/{id}")
	public String cloudStoragesSource(@PathVariable("id") String id, Model uiModel, HttpServletRequest request) {

		final List<CloudStorageDTO> cloudStorages = datalabelingService.findImportStorages(id, token);

		uiModel.addAttribute("cloudstorages", cloudStorages);
		uiModel.addAttribute("project", id);
		uiModel.addAttribute("type", "source");
		return "datalabeling/cloudstorages";
	}

	@RequestMapping(value = "/cloudstoragestarget/{id}")
	public String cloudstoragestarget(@PathVariable("id") String id, Model uiModel, HttpServletRequest request) {

		final List<CloudStorageDTO> cloudStorages = datalabelingService.findExportStorages(id, token);

		uiModel.addAttribute("cloudstorages", cloudStorages);
		uiModel.addAttribute("project", id);
		uiModel.addAttribute("type", "target");
		return "datalabeling/cloudstorages";
	}

	@RequestMapping(value = "/cloudstorage/{id}/{typec}")
	public String cloudStorage(@PathVariable("id") String id, @PathVariable("typec") String typec, Model uiModel,
			HttpServletRequest request) {
		final ProjectDTO project = datalabelingService.findProjectByID(id, token);

		final String bucketName = objectStorageService.getUserBucketName(utils.getUserId());
		final CloudStorageFullDTO cloudStorageDTO = new CloudStorageFullDTO();
		cloudStorageDTO.setBucket(bucketName);
		cloudStorageDTO.setPrefix(createPrefixFromTitle(project.getTitle()) + typec);
		cloudStorageDTO.setProject(id);
		cloudStorageDTO.setType(typec);
		cloudStorageDTO.setTitle(createPrefixFromTitle(project.getTitle()));
		cloudStorageDTO.setIdentification(createPrefixFromTitle(project.getTitle()));
		cloudStorageDTO.setPresign(false);
		cloudStorageDTO.setUseBlobUrls(false);
		cloudStorageDTO.setRecursiveScan(false);
		cloudStorageDTO.setCanDeleteObjects(true);
		cloudStorageDTO.setRegexFilter(".*csv");
		cloudStorageDTO.setPresignttl(1);
		uiModel.addAttribute("cloudstorage", cloudStorageDTO);
		if (typec.equals("source")) {
			return "datalabeling/cloudstoragesourcecreate";
		} else {
			return "datalabeling/cloudstoragetargetcreate";
		}
	}

	@PostMapping(value = { "/createcloudstoragesource" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public String createCloudStorageSource(Model model, CloudStorageFullDTO cloudStorageDTO,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("configuration.validation.error", redirect);
			log.debug("Missing fields");
			return "redirect:/cloudstorage/" + cloudStorageDTO.getProject() + "/" + cloudStorageDTO.getType();
		}

		createPath(cloudStorageDTO);
		final String result = datalabelingService.createImportStorages(cloudStorageDTO, token);

		response.put(STATUS_STR, "ok");

		return "redirect:/datalabeling/list";

	}

	@PostMapping(value = { "/createcloudstoragetarget" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
	public String createCloudStorageTarget(Model model, CloudStorageFullDTO cloudStorageDTO,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final Map<String, String> response = new HashMap<>();

		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("configuration.validation.error", redirect);
			log.debug("Missing fields");
			return "redirect:/cloudstorage/" + cloudStorageDTO.getProject() + "/" + cloudStorageDTO.getType();
		}

		createPath(cloudStorageDTO);
		datalabelingService.createExportStorages(cloudStorageDTO, token);

		response.put(STATUS_STR, "ok");

		return "redirect:/datalabeling/list";

	}

	@DeleteMapping("/deletesourcecloudstorage/{id}")
	public @ResponseBody String deleteSourceCloudStorage(Model model, @PathVariable("id") String id,
			RedirectAttributes redirect) {
		datalabelingService.deleteImportStorage(id, token);
		return "";
	}

	@DeleteMapping("/deletetargetcloudstorage/{id}")
	public @ResponseBody String deleteTargetCloudStorage(Model model, @PathVariable("id") String id,
			RedirectAttributes redirect) {
		datalabelingService.deleteExportStorage(id, token);
		return "";
	}

	@DeleteMapping("/deleteproject/{id}")
	public String deleteProject(Model model, @PathVariable("id") String id) {
		datalabelingService.deleteProject(id, token);
		return "redirect:/datalabeling/list";
	}

	private String createPrefixFromTitle(String title) {
		return title.replaceAll("[^a-zA-Z0-9]", "");

	}

	private String getDatalabelingUrl() {
		return "/../dlabeling/";
	}

	private void createPath(CloudStorageFullDTO cloudStorageDTO) {
		cloudStorageDTO.setAws_access_key_id(accessKey);
		cloudStorageDTO.setAws_secret_access_key(secretKey);
		cloudStorageDTO.setS3Endpoint(minioBaseUrl);

		final String requesterUser = utils.getUserId();
		final String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
		String objectStoreAuthToken = null;
		try {
			objectStoreAuthToken = objectStorageService.logIntoObjectStorageWithSuperUser();

			final String userToken = objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			final MultipartFile file = new MultipartFile() {

				@Override
				public void transferTo(File dest) throws IOException, IllegalStateException {

				}

				@Override
				public boolean isEmpty() {

					return true;
				}

				@Override
				public long getSize() {

					return 0;
				}

				@Override
				public String getOriginalFilename() {

					return ".metadata";
				}

				@Override
				public String getName() {

					return ".metadata";
				}

				@Override
				public InputStream getInputStream() throws IOException {
					final byte[] data = new byte[] { 1 };
					return new ByteArrayInputStream(data);
				}

				@Override
				public String getContentType() {

					return "text/plain";
				}

				@Override
				public byte[] getBytes() throws IOException {

					return null;
				}
			};

			objectStorageService.uploadObject(userToken, cloudStorageDTO.getBucket(), cloudStorageDTO.getPrefix(),
					file);

		} catch (final ObjectStoreLoginException e) {
			log.error("Error loing with superuser in MinIO", e);

		}
	}
}
