/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;
import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebProjectServiceImpl implements WebProjectService {

	private static final String ERROR_ZIPPING_FILES = "Error zipping files ";
	private static final String WTOP_ZIP = "wtop.zip";
	@Autowired
	private WebProjectRepository webProjectRepository;

	@Autowired
	private UserService userService;

	private static final String USER_UNAUTH = "The user is not authorized";
	private static final String SLASH_STRING = "/";
	private static final String ERROR_DELETING_FOLDER = "Error deleting folder: {}";

	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18080/web/}")
	private String rootWWW;

	@Value("${onesaitplatform.webproject.rootfolder.path:/usr/local/webprojects/}")
	private String rootFolder;

	@Value("${onesaitplatform.webproject.template.zip:http://localhost:18000/controlpanel/static/wtop/wtop.zip}")
	private String wtop;

	@Override
	public List<WebProjectDTO> getWebProjectsWithDescriptionAndIdentification(String userId, String identification,
			String description) {
		List<WebProject> webProjects;
		final User user = userService.getUser(userId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (userService.isUserAdministrator(user)) {
			webProjects = webProjectRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
		} else {
			webProjects = webProjectRepository.findByUserAndIdentificationContainingAndDescriptionContaining(user,
					identification, description);
		}

		return webProjects.stream().map(WebProjectDTO::convert).collect(Collectors.toList());
	}

	@Override
	public List<String> getWebProjectsIdentifications(String userId) {
		List<WebProject> webProjects;
		final List<String> identifications = new ArrayList<>();
		final User user = userService.getUser(userId);

		if (userService.isUserAdministrator(user)) {
			webProjects = webProjectRepository.findAllByOrderByIdentificationAsc();
		} else {
			webProjects = webProjectRepository.findByUserOrderByIdentificationAsc(user);
		}

		for (final WebProject webProject : webProjects) {
			identifications.add(webProject.getIdentification());
		}

		return identifications;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<WebProject> webProjects = webProjectRepository.findAll();
		final List<String> allIdentifications = new ArrayList<>();

		for (final WebProject webProject : webProjects) {
			allIdentifications.add(webProject.getIdentification());
		}

		return allIdentifications;
	}

	@Override
	public boolean webProjectExists(String identification) {
		return webProjectRepository.findByIdentification(identification) != null;
	}

	@Override
	public void createWebProject(WebProjectDTO webProject, String userId) {
		if (!webProjectExists(webProject.getIdentification())) {
			log.debug("Web Project does not exist, creating..");
			final User user = userService.getUser(userId);
			final WebProject wp = WebProjectDTO.convert(webProject, user);
			if (wp.getMainFile().isEmpty()) {
				wp.setMainFile("index.html");
			}
			createFolderWebProject(wp.getIdentification(), userId);
			webProjectRepository.save(wp);

		} else {
			throw new WebProjectServiceException(
					"Web Project with identification: " + webProject.getIdentification() + " already exists");
		}
	}

	@Override
	public WebProjectDTO getWebProjectById(String webProjectId, String userId) {
		final WebProject webProject = webProjectRepository.findById(webProjectId).orElse(null);
		final User user = userService.getUser(userId);
		if (webProject != null) {
			if (hasUserPermissionToEditWebProject(user, webProject)) {
				return WebProjectDTO.convert(webProject);
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		} else {
			return null;
		}

	}

	public boolean hasUserPermissionToEditWebProject(User user, WebProject webProject) {
		if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			return webProject.getUser().getUserId().equals(user.getUserId());
		}
	}

	public String getWebProjectURL(String identification) {
		final WebProject webProject = webProjectRepository.findByIdentification(identification);
		return rootWWW + webProject.getIdentification() + SLASH_STRING + webProject.getMainFile();
	}

	@Override
	public void updateWebProject(WebProjectDTO webProject, String userId) {
		final WebProject wp = webProjectRepository.findByIdentification(webProject.getIdentification());
		final User user = userService.getUser(userId);

		if (wp != null) {
			if (hasUserPermissionToEditWebProject(user, wp)) {
				if (webProjectExists(wp.getIdentification())) {
					if (!StringUtils.isEmpty(webProject.getDescription())) {
						wp.setDescription(webProject.getDescription());
					}
					if (!StringUtils.isEmpty(webProject.getMainFile())) {
						wp.setMainFile(webProject.getMainFile());
					}
					updateFolderWebProject(webProject.getIdentification(), userId);
					webProjectRepository.save(wp);
				} else {
					throw new WebProjectServiceException(
							"Web Project with identification:" + webProject.getIdentification() + " not exist");
				}
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		} else {
			throw new WebProjectServiceException("Web project does not exist");
		}
	}

	@Override
	public void deleteWebProject(String webProjectId, String userId) {
		webProjectRepository.findById(webProjectId).ifPresent(wp -> {
			final User user = userService.getUser(userId);

			if (hasUserPermissionToEditWebProject(user, wp)) {
				deleteFolder(rootFolder + wp.getIdentification() + SLASH_STRING);
				webProjectRepository.delete(wp);
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		});

	}

	@Override
	public void deleteWebProjectById(String id, String userId) {
		webProjectRepository.findById(id).ifPresent(wp -> {
			final User user = userService.getUser(userId);

			if (hasUserPermissionToEditWebProject(user, wp)) {
				deleteFolder(rootFolder + wp.getIdentification() + SLASH_STRING);
				webProjectRepository.delete(wp);
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		});

	}

	@Override
	public void uploadZip(MultipartFile file, String userId) {

		final String folder = rootFolder + userId + SLASH_STRING;

		deleteFolder(folder);
		uploadFileToFolder(file, folder);
		unzipFile(folder, file.getOriginalFilename());
	}

	@Override
	public void uploadWebTemplate(String userId) {

		final String folder = rootFolder + userId + SLASH_STRING;
		deleteFolder(folder);
		uploadWebTemplateToFolder(folder);
		unzipFile(folder, WTOP_ZIP);
	}

	private void uploadWebTemplateToFolder(String path) {

		final String fileName = WTOP_ZIP;

		try {
			final InputStream is = new URL(wtop).openStream();
			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + fileName;
			final OutputStream os = new FileOutputStream(new File(fullPath));

			IOUtils.copy(is, os);

			is.close();
			os.close();
		} catch (final IOException e) {
			throw new WebProjectServiceException("Error uploading files " + e);
		}

		log.debug("File: " + path + fileName + " uploaded");
	}

	private void uploadFileToFolder(MultipartFile file, String path) {

		final String fileName = file.getOriginalFilename();
		byte[] bytes;
		try {
			bytes = file.getBytes();

			final InputStream is = new ByteArrayInputStream(bytes);

			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + fileName;
			final OutputStream os = new FileOutputStream(new File(fullPath));

			IOUtils.copy(is, os);

			is.close();
			os.close();
		} catch (final IOException e) {
			throw new WebProjectServiceException("Error uploading files " + e);
		}

		log.debug("File: " + path + fileName + " uploaded");
	}

	private void deleteFolder(String path) {
		final File folder = new File(path);
		final File[] files = folder.listFiles();
		if (files != null) {
			for (final File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f.getAbsolutePath());
				} else {
					deleteFile(f);
				}
			}
		}
		deleteFile(folder);
	}

	private void deleteFile(File file) {
		try {
			Files.delete(file.toPath());
		} catch (final IOException e) {
			log.debug(ERROR_DELETING_FOLDER, file.getPath());
		}
	}

	private void createFolderWebProject(String identification, String userId) {

		final File file = new File(rootFolder + userId + SLASH_STRING);
		if (file.exists() && file.isDirectory()) {
			final File newFile = new File(rootFolder + identification + SLASH_STRING);
			if (!file.renameTo(newFile)) {
				throw new WebProjectServiceException("Cannot create web project folder " + identification);
			}
			log.debug("New folder for Web Project " + identification + " has been created");
		}
	}

	private void updateFolderWebProject(String identification, String userId) {

		final File file = new File(rootFolder + userId + SLASH_STRING);
		if (file.exists() && file.isDirectory()) {
			deleteFolder(rootFolder + identification + SLASH_STRING);
			final File newFile = new File(rootFolder + identification + SLASH_STRING);
			if (!file.renameTo(newFile)) {
				throw new WebProjectServiceException("Cannot create web project folder " + identification);
			}
			log.debug("Folder for Web Project " + identification + " has been created");
		}
	}

	private void unzipFile(String path, String fileName) {

		final File folder = new File(path + fileName);
		log.debug("Unzipping zip file: " + folder);

		DataInputStream is = null;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(folder))) {
			final byte[] buffer = new byte[4];
			final byte[] zipbf = new byte[] { 0x50, 0x4B, 0x03, 0x04 };

			is = new DataInputStream(new FileInputStream(folder));
			is.readFully(buffer);
			is.close();
			if (!Arrays.equals(buffer, zipbf)) {
				throw new WebProjectServiceException("Error: Invalid file");
			}

			ZipEntry ze;

			while (null != (ze = zis.getNextEntry())) {
				if (ze.isDirectory()) {
					final File f = new File(path + ze.getName());
					f.mkdirs();
				} else {
					log.debug("Unzipping file: " + ze.getName());
					final FileOutputStream fos = new FileOutputStream(path + ze.getName());
					IOUtils.copy(zis, fos);
					fos.close();
					zis.closeEntry();
				}
			}

		} catch (final IOException e) {
			throw new WebProjectServiceException("Error unzipping files " + e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					log.debug("Error: " + e);
				}
			}
		}

		if (folder.exists()) {
			deleteFile(folder);
		}

	}

	@Override
	public byte[] downloadZip(String identification, String userId) {

		final String path = rootFolder;
		final String fileName = identification + ".zip";

		final ByteArrayOutputStream zipByte = new ByteArrayOutputStream();
		final ZipOutputStream zipOut = new ZipOutputStream(zipByte);

		final File fileToZip = new File(path + identification);

		log.debug("Zipping file: " + path + fileName);

		try {
			if (fileToZip.isDirectory()) {
				final File[] fileList = fileToZip.listFiles();
				for (final File file : fileList) {
					zipFile(file, file.getName(), zipOut);
				}
			}
			zipOut.close();
			zipByte.close();
		} catch (final IOException e) {
			log.error(ERROR_ZIPPING_FILES + e);
			throw new WebProjectServiceException(ERROR_ZIPPING_FILES + e);
		}

		return zipByte.toByteArray();
	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

		try {
			if (fileToZip.isDirectory()) {
				if (fileName.endsWith("/")) {
					zipOut.putNextEntry(new ZipEntry(fileName));
					zipOut.closeEntry();
				} else {
					zipOut.putNextEntry(new ZipEntry(fileName + "/"));
					zipOut.closeEntry();
				}
				final File[] fileList = fileToZip.listFiles();
				for (final File file : fileList) {
					zipFile(file, fileName + "/" + file.getName(), zipOut);
				}
				return;
			}
			copyFilesToZip(fileToZip, fileName, zipOut);

		} catch (final IOException e) {
			log.error(ERROR_ZIPPING_FILES + e);
			throw e;
		}
	}

	private void copyFilesToZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			final ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);

			IOUtils.copy(fis, zipOut);
		} catch (final IOException e) {
			log.error(ERROR_ZIPPING_FILES + e);
			throw e;
		}
	}

	@Override
	public WebProjectDTO getWebProjectByName(String name, String userId) {
		final WebProject webProject = webProjectRepository.findByIdentification(name);
		final User user = userService.getUser(userId);
		if (webProject != null) {
			if (hasUserPermissionToEditWebProject(user, webProject)) {
				return WebProjectDTO.convert(webProject);
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		} else {
			return null;
		}
	}

	@Override
	public List<WebProjectDTO> getAllWebProjects() {
		return webProjectRepository.findAll().stream().map(WebProjectDTO::convert).collect(Collectors.toList());
	}

}
