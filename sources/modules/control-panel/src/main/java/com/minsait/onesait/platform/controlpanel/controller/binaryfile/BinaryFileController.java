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
package com.minsait.onesait.platform.controlpanel.controller.binaryfile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.net.HttpHeaders;
import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.binaryrepository.model.BinaryFileData;
import com.minsait.onesait.platform.business.services.binaryrepository.factory.BinaryRepositoryServiceFactory;
import com.minsait.onesait.platform.business.services.ontology.OntologyServiceStatusBean;
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.comms.protocol.binary.BinarySizeException;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.ODBinaryFilesDataset;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.objectstorage.MinioObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.ObjectStorageService;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreBucketCreateException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreatePolicyException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreCreateUserException;
import com.minsait.onesait.platform.config.services.objectstorage.exception.ObjectStoreLoginException;
import com.minsait.onesait.platform.config.services.opendata.binaryFiles.BinaryFilesDatasetService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.usertoken.UserTokenService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/files")
@Slf4j
public class BinaryFileController {

	@Autowired
	private BinaryRepositoryServiceFactory binaryFactory;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	BinaryFilesDatasetService binaryFilesDatasetService;
	@Autowired
	private AppWebUtils webUtils;
	@Autowired
	private UserService userService;
	@Autowired
	private HttpSession httpSession;
	@Autowired
	private UserTokenService userTokenService;
	@Autowired
	private ObjectStorageService objectStorageService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profileDetector;
	@Autowired
	private OntologyServiceStatusBean serviceStatusBean;
	@Autowired
	private MessageSource messageSource;

	private String minioBaseUrl;

	private String minioAdminExternalUrl;

	private String minioBrowserExternalUrl;

	private String cookieDomain;

	private static final String APP_ID = "appId";
	private static final String REDIRECT_GRIDFS_FILES_LIST = "redirect:/files/gridfs";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";
	private static final String REDIRECT_MINIO_FILES_LIST = "redirect:/files/minio";
	public static final String MODULE_NOT_ACTIVE_KEY = "moduleNotActive";
	private static final String REDIRECT_GCP_FILES_LIST = "redirect:/files/gcp";

	@Value("${onesaitplatform.controlpanel.binaryfiles.limit:200}")
	private int maxLoadedFiles;

	@Value("${onesaitplatform.controlpanel.binaryfiles.showAuditFiles:false}")
	private boolean showAuditFiles;

	@PostConstruct
	public void init() {
		Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());

		this.minioBaseUrl = urls.getMinio().getBase();
		this.minioAdminExternalUrl = urls.getMinio().getAdmin().getExternal();
		this.minioBrowserExternalUrl = urls.getMinio().getBrowser().getExternal();
		this.cookieDomain = urls.getMinio().getCookiedomain();

		if (!this.minioAdminExternalUrl.endsWith("/")) {
			this.minioAdminExternalUrl += "/";
		}

		if (!this.minioBrowserExternalUrl.endsWith("/")) {
			this.minioBrowserExternalUrl += "/";
		}
	}

	@GetMapping(value = { "/list/{redirect}", "/list" })
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER')")
	public String create(Model model, @PathVariable("redirect") Optional<Boolean> redirect) {
		if (model.asMap().containsKey(MODULE_NOT_ACTIVE_KEY)) {
			model.addAttribute("message", model.asMap().get(MODULE_NOT_ACTIVE_KEY));
		}
		if (!redirect.isPresent()) {
			// CLEANING APP_ID FROM SESSION
			httpSession.removeAttribute(APP_ID);
		}
		return "binaryfiles/list";
	}

	@GetMapping("gridfs")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER')")
	@Transactional
	public String list(Model model, @RequestParam(required = false) String fileName,
			@RequestParam(required = false) String fileId, @RequestParam(required = false) String fileExt,
			@RequestParam(required = false) String metaData, @RequestParam(required = false) String owner) {

		if (fileName != null && fileName.trim().equals("")) {
			fileName = null;
		}
		if (fileId != null && fileId.trim().equals("")) {
			fileId = null;
		}
		if (fileExt != null && fileExt.trim().equals("")) {
			fileExt = null;
		}
		if (metaData != null && metaData.trim().equals("")) {
			metaData = null;
		}
		if (owner != null && owner.trim().equals("")) {
			owner = null;
		}

		User userlogged = userService.getUser(webUtils.getUserId());

		if (fileName == null && fileId == null && fileExt == null && metaData == null && owner == null) {
			log.debug("No params for filtering, loading all files");

			long files = binaryFileService.countFiles(userlogged, showAuditFiles);

			if (files < maxLoadedFiles) {
				final List<BinaryFile> list = binaryFileService.getAllFiles(userlogged, showAuditFiles).stream()
						.filter(bf -> (bf.getRepository() != RepositoryType.MINIO_S3
								&& bf.getRepository() != RepositoryType.GCP))
						.collect(Collectors.toList());

				final Map<String, String> accessMap = new HashMap<>();
				final List<BinaryFile> filteredList = list.stream()
						.filter(bf -> !bf.getUser().getUserId().equals(webUtils.getUserId()))
						.collect(Collectors.toList());
				filteredList.forEach(bf -> bf.getFileAccesses().forEach(bfa -> {
					if (bfa.getUser().getUserId().equals(webUtils.getUserId()))
						accessMap.put(bf.getId(), bfa.getAccessType().name());
				}));
				model.addAttribute("files", list);
				model.addAttribute("accessMap", accessMap);
			} else {
				model.addAttribute("limit", maxLoadedFiles);
			}
		} else {
			final List<BinaryFile> list = binaryFileService
					.getAllFilesFiltered(userlogged, fileName, fileId, fileExt, metaData, owner, true).stream()
					.filter(bf -> (bf.getRepository() != RepositoryType.MINIO_S3
							&& bf.getRepository() != RepositoryType.GCP))
					.collect(Collectors.toList());

			final Map<String, String> accessMap = new HashMap<>();
			final List<BinaryFile> filteredList = list.stream()
					.filter(bf -> !bf.getUser().getUserId().equals(webUtils.getUserId())).collect(Collectors.toList());
			filteredList.forEach(bf -> bf.getFileAccesses().forEach(bfa -> {
				if (bfa.getUser().getUserId().equals(webUtils.getUserId()))
					accessMap.put(bf.getId(), bfa.getAccessType().name());
			}));
			model.addAttribute("files", list);
			model.addAttribute("accessMap", accessMap);
		}

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
			httpSession.removeAttribute(APP_ID);
		}

		model.addAttribute("accessTypes", BinaryFileAccess.Type.values());
		model.addAttribute("users", userService.getAllUsers());
		model.addAttribute("repos", RepositoryType.values());

		return "binaryfiles/gridfs";
	}

	@PostMapping("gridfs")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String addBinary(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) RepositoryType repository,
			@RequestParam(value = "appID", required = false) String appID, RedirectAttributes redirectAttributes) {

		if (file.getSize() <= 0) {
			webUtils.addRedirectMessage("binaryfiles.error.empty", redirectAttributes);
			return REDIRECT_GRIDFS_FILES_LIST;
		}

		if (webUtils.isFileExtensionForbidden(file)) {
			webUtils.addRedirectMessage("binaryfiles.error.extensionnotallowed", redirectAttributes);
			return REDIRECT_GRIDFS_FILES_LIST;
		}

		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue()) {
				throw new BinarySizeException("The file size is larger than max allowed");
			}

			binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).addBinary(file, metadata, repository, null);

			if (appID != null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.BINARYFILE.toString());
				httpSession.setAttribute("resourceIdentificationAdded", file.getOriginalFilename());
				return REDIRECT_PROJECT_SHOW + appID;
			}

			return REDIRECT_GRIDFS_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);

			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				httpSession.removeAttribute(APP_ID);
				return REDIRECT_PROJECT_SHOW + projectId.toString();
			}

			return REDIRECT_GRIDFS_FILES_LIST;
		}

	}

	@GetMapping("/gridfs/{id}")
	public ResponseEntity<ByteArrayResource> getBinaryGridfs(@PathVariable("id") String fileId,
			@RequestParam(value = "disposition", required = false) String disposition) {
		try {

			final BinaryFileData file = binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).getBinaryFile(fileId);
			final ByteArrayResource resource = new ByteArrayResource(
					((ByteArrayOutputStream) file.getData()).toByteArray());

			if (StringUtils.isEmpty(disposition)) {
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(file.getFileName()))
						.contentLength(resource.contentLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
			} else {
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline")
						.contentType(MediaType.parseMediaType(file.getContentType()))
						.contentLength(resource.contentLength()).body(resource);
			}

		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("/gridfs/public")
	@ResponseBody
	public String changePublicGridfs(@RequestParam("id") String fileId) {
		if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {
			binaryFileService.changePublic(fileId);
			return "ok";
		} else {
			return "ko";
		}
	}

	@DeleteMapping("/gridfs/{fileId}")
	public @ResponseBody String deleteGridfs(Model model, @PathVariable String fileId, RedirectAttributes ra) {
		try {
			if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {

				List<ODBinaryFilesDataset> bfilesDatasetList = binaryFilesDatasetService
						.getBinaryFilesByFilesId(fileId);
				if (bfilesDatasetList == null || bfilesDatasetList.isEmpty()) {
					binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).removeBinary(fileId);
				} else {
					throw new BinaryRepositoryException(
							"The file cannot be deleted. There is a dataset with this file.");
				}

			}
		} catch (final RuntimeException e) {
			webUtils.addRedirectException(e, ra);
			return e.getMessage();
		} catch (BinaryRepositoryException ex) {
			webUtils.addRedirectException(ex, ra);
			return ex.getMessage();
		}
		return "{\"ok\":true}";
	}

	@PostMapping("/gridfs/update")
	public String update(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata, @RequestParam("fileId") String fileId,
			RedirectAttributes redirectAttributes) {
		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue())
				throw new BinarySizeException("The file size is larger than max allowed");
			binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).updateBinary(fileId, file, metadata);
			return REDIRECT_GRIDFS_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not update binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);
			return REDIRECT_GRIDFS_FILES_LIST;
		}

	}

	@GetMapping("/gridfs/metadata/{fileId}")
	@ResponseBody
	public String getMetadata(@PathVariable String fileId) {
		if (binaryFileService.hasUserPermissionRead(fileId, userService.getUser(webUtils.getUserId())))
			return binaryFileService.getFile(fileId).getMetadata();
		else
			return "forbidden";

	}

	@GetMapping(value = "/gridfs/authorization/{fileId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@Transactional
	public @ResponseBody ResponseEntity<List<BinaryFileAccessDTO>> getAuthorizationGridfss(
			@PathVariable String fileId) {

		try {
			final List<BinaryFileAccess> accesses = binaryFileService.getAuthorizations(fileId,
					userService.getUser(webUtils.getUserId()));
			final List<BinaryFileAccessDTO> authorizationsDTO = new ArrayList<>();
			accesses.stream().forEach(a -> authorizationsDTO.add(new BinaryFileAccessDTO(a)));
			return new ResponseEntity<>(authorizationsDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/gridfs/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BinaryFileAccessDTO> createAuthorizationGridfs(@RequestParam String accesstype,
			@RequestParam String fileId, @RequestParam String user) throws GenericOPException {

		try {
			final BinaryFileAccess binaryFileAccessCreated = binaryFileService.createBinaryFileAccess(fileId, user,
					accesstype, userService.getUser(webUtils.getUserId()));
			final BinaryFileAccessDTO binaryFileAccessDTO = new BinaryFileAccessDTO(binaryFileAccessCreated);
			return new ResponseEntity<>(binaryFileAccessDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/gridfs/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorizationGridfs(@RequestParam String id) throws GenericOPException {

		try {
			binaryFileService.deleteBinaryFileAccess(id, userService.getUser(webUtils.getUserId()));
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/gridfs/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<BinaryFileAccessDTO> updateAuthorizationGridfs(@RequestParam String id,
			@RequestParam String accesstype) throws GenericOPException {

		try {
			final BinaryFileAccess binaryFileAccessUpdated = binaryFileService.updateBinaryFileAccess(id, accesstype,
					userService.getUser(webUtils.getUserId()));
			final BinaryFileAccessDTO binaryFileAccessDTO = new BinaryFileAccessDTO(binaryFileAccessUpdated);

			return new ResponseEntity<>(binaryFileAccessDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/gridfs/maxsize", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Long>> maxSizeGridfs() {
		final Map<String, Long> map = new HashMap<>();
		map.put("maxsize", webUtils.getMaxFileSizeAllowed());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@GetMapping(value = "/minio/adminconsole")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_SYS_ADMIN')")
	public String adminConsole(HttpServletResponse response, Model model) throws Exception {

		String requesterUser = webUtils.getUserId();

		String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();

		String objectStoreAuthToken = null;
		try {
			objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
		} catch (ObjectStoreLoginException e) {
			log.error("Error loing with superuser in MinIO", e);
			throw e;
		}

		try {
			// Hace login con el token del usuario, crea la cookie y redirige al iframe que
			// tiene embebida la consola
			String userTokenForCookie = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			response.setHeader("Set-Cookie", "token=" + userTokenForCookie + "; Domain=" + this.cookieDomain
					+ "; Path=/; Secure; SameSite=None");

		} catch (ObjectStoreLoginException e) {
			log.error("Error login user in MinIO", e);
		}

		model.addAttribute("url", this.minioAdminExternalUrl);
		return "binaryfiles/console"; // Embebe la consola en un iframe
	}

	@Transactional
	@GetMapping(value = "/minio")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public String listMinio(HttpServletResponse response, Model model, RedirectAttributes redirect) throws Exception {

		if (!serviceStatusBean.isMinIOActive()) {
			redirect.addFlashAttribute(MODULE_NOT_ACTIVE_KEY,
					messageSource.getMessage("service.minio.down", null, LocaleContextHolder.getLocale()));
			return "redirect:/files/list";
		}

		String requesterUser = webUtils.getUserId();

		String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();

		String objectStoreAuthToken = null;
		try {
			objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
		} catch (ObjectStoreLoginException e) {
			log.error("Error loing with superuser in MinIO", e);
			throw e;
		}

		try {

			boolean existsUserInObjectStore = this.objectStorageService.existUserInObjectStore(objectStoreAuthToken,
					requesterUser);
			if (!existsUserInObjectStore) { // Creates User
				objectStorageService.createBucketForUser(requesterUser);
				objectStorageService.createPolicyForBucketUser(objectStoreAuthToken, requesterUser);
				objectStorageService.createUserInObjectStore(objectStoreAuthToken, requesterUser, requesterUserToken);
			}

		} catch (ObjectStoreBucketCreateException | ObjectStoreCreatePolicyException
				| ObjectStoreCreateUserException e) {
			log.error("Error creating user in MinIO", e);
			throw e;
		}

		try {
			// Hace login con el token del usuario, crea la cookie y redirige al iframe que
			// tiene embebida la consola
			String userTokenForCookie = this.objectStorageService.logIntoBrowserObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			response.setHeader("Set-Cookie", "token=" + userTokenForCookie + "; Domain=" + this.cookieDomain
					+ "; Path=/; Secure; SameSite=None");

		} catch (ObjectStoreLoginException e) {
			log.error("Error login user in MinIO", e);
		}

		final List<BinaryFile> lFilesOwner = this.objectStorageService.listUserFiles(requesterUser);

		final List<BinaryFile> lFilesAllowed = binaryFileService
				.getAllFilesUserIsAllowed(userService.getUser(webUtils.getUserId())).stream()
				.filter(bf -> bf.getRepository() == RepositoryType.MINIO_S3).collect(Collectors.toList());

		final Map<String, String> accessMap = new HashMap<>();

		lFilesAllowed.forEach(bf -> bf.getFileAccesses().forEach(bfa -> {
			if (bfa.getUser().getUserId().equals(webUtils.getUserId()))
				accessMap.put(bf.getId(), bfa.getAccessType().name());
		}));

		final List<BinaryFile> lAllFiles = Stream.concat(lFilesOwner.stream(), lFilesAllowed.stream())
				.collect(Collectors.toList());

		// Elimina duplicados (Hechos públicos por el propio usuario)
		Map<String, BinaryFile> mFiles = new HashMap<String, BinaryFile>();
		lAllFiles.forEach(file -> {
			BinaryFile storedFile = mFiles.get(file.getPath());
			if (null == storedFile) {
				mFiles.put(file.getPath(), file);
			} else {
				mFiles.put(file.getPath(), file.isPublic() ? file : storedFile);// Conserva el publico
			}
		});

		lAllFiles.clear();
		mFiles.values().forEach(v -> lAllFiles.add(v));

		model.addAttribute("files", lAllFiles);
		model.addAttribute("accessMap", accessMap);
		model.addAttribute("accessTypes", BinaryFileAccess.Type.values());
		model.addAttribute("users", userService.getAllUsers());
		model.addAttribute("repos", RepositoryType.values());
		model.addAttribute("userBucket", objectStorageService.getUserBucketName(requesterUser));
		model.addAttribute("url", this.minioBrowserExternalUrl + "buckets/"
				+ this.objectStorageService.getUserBucketName(requesterUser) + "/browse");
		model.addAttribute("s3Server", minioBaseUrl);
		return "binaryfiles/minio"; // Embebe la consola en un iframe
	}

	@GetMapping("/minio/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public ResponseEntity<Resource> getBinaryMinio(@PathVariable("id") String fileId,
			@RequestParam(value = "disposition", required = false) String disposition) {
		try {
			String requesterUser = webUtils.getUserId();
			String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			BinaryFile bFileFromBDC = this.binaryFileService.getFile(fileId);

			String filePath = null;
			if (null != bFileFromBDC) {
				filePath = bFileFromBDC.getPath();
			} else {
				filePath = this.objectStorageService.decodeTemporalId(fileId);
			}

			return this.objectStorageService.downloadFile(userToken, filePath);

		} catch (final IOException e) {
			log.error("Error downloading file", e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (ObjectStoreLoginException e) {
			log.error("Error authenticating with object store server", e);
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

	}

	@GetMapping(value = "/minio/authorization/{fileId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public @ResponseBody ResponseEntity<List<BinaryFileAccessDTO>> getAuthorizationsMinio(@PathVariable String fileId) {
		String objectPath = this.objectStorageService.decodeTemporalId(fileId);

		try {
			final List<BinaryFileAccessDTO> authorizationsDTO = new ArrayList<>();

			List<BinaryFile> files = binaryFileService.getFileByPath(objectPath);
			if (files != null && files.size() > 0) {// El fichero ha sido guardado en BD previamente
				fileId = files.get(0).getId();

				final List<BinaryFileAccess> accesses = binaryFileService.getAuthorizations(fileId,
						userService.getUser(webUtils.getUserId()));
				accesses.stream().forEach(a -> authorizationsDTO.add(new BinaryFileAccessDTO(a)));
			}

			return new ResponseEntity<>(authorizationsDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@Transactional
	@PostMapping(value = "/minio/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public ResponseEntity<BinaryFileAccessDTO> createAuthorizationMinio(@RequestParam String accesstype,
			@RequestParam String fileId, @RequestParam String user) throws GenericOPException {

		try {

			String requesterUser = webUtils.getUserId();
			String objectPath = this.objectStorageService.decodeTemporalId(fileId);

			String persistedFileId;

			List<BinaryFile> lBFiles = binaryFileService.getFileByPath(objectPath);
			if (null != lBFiles && lBFiles.size() > 0) {
				persistedFileId = lBFiles.get(0).getId();
			} else {
				BinaryFile bFile = this.objectStorageService.buildBinaryFile(requesterUser, objectPath);
				binaryFileService.createBinaryFile(bFile);
				persistedFileId = binaryFileService.getFileByPath(objectPath).get(0).getId();
			}

			final BinaryFileAccess binaryFileAccessCreated = binaryFileService.createBinaryFileAccess(persistedFileId,
					user, accesstype, userService.getUser(webUtils.getUserId()));

			final BinaryFileAccessDTO binaryFileAccessDTO = new BinaryFileAccessDTO(binaryFileAccessCreated);

			// Crea la politica en MinIO
			String objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();

			String newUserToken = userTokenService.getToken(userService.getUser(user)).getToken();

			this.createPolicyInObjectStoreAndSetToUser(objectStoreAuthToken, objectPath, user, newUserToken,
					BinaryFileAccess.Type.valueOf(accesstype));

			return new ResponseEntity<>(binaryFileAccessDTO, HttpStatus.CREATED);

		} catch (final RuntimeException | ObjectStoreCreatePolicyException | ObjectStoreBucketCreateException
				| ObjectStoreCreateUserException | ObjectStoreLoginException e) {
			log.error("Error creating authorizacion", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@Transactional
	@DeleteMapping("minio/{fileId}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public @ResponseBody String deleteMinio(Model model, @PathVariable String fileId, RedirectAttributes ra) {
		try {

			BinaryFile bFileFromBDC = this.binaryFileService.getFile(fileId);

			String filePath = null;
			if (null != bFileFromBDC) {
				filePath = bFileFromBDC.getPath();
			} else {
				filePath = this.objectStorageService.decodeTemporalId(fileId);
			}

			String requesterUser = webUtils.getUserId();
			String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();

			String objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			// Borrar el fichero en el Object Store
			if (!this.objectStorageService.removeObject(userToken, filePath)) {
				log.warn("user not allowed to remove file");
				return "{\"ok\":false}";
			}

			// Borrar las politicas asociadas al fichero que pudieran existir (READ o WRITE
			// al haber concedido permisos)
			String policyReadName = "Allow_READ_" + filePath.replace('/', '_').replace('.', '_');
			String policyWriteName = "Allow_WRITE_" + filePath.replace('/', '_').replace('.', '_');

			if (this.objectStorageService.existPolicy(objectStoreAuthToken, policyReadName)) {
				this.objectStorageService.removePolicy(objectStoreAuthToken, policyReadName);
			}

			if (this.objectStorageService.existPolicy(objectStoreAuthToken, policyWriteName)) {
				this.objectStorageService.removePolicy(objectStoreAuthToken, policyWriteName);
			}

			// Borrar las asociaciaciones asociadas el fichero en BDC
			List<BinaryFile> lFilesFromBDC = this.binaryFileService.getFileByPath(filePath);
			if (null != lFilesFromBDC && lFilesFromBDC.size() > 0) {
				this.binaryFileService.deleteFile(lFilesFromBDC.get(0).getId());
			}

		} catch (ObjectStoreLoginException e) {
			log.error("Error removing file", e);
			webUtils.addRedirectException(e, ra);
			return e.getMessage();
		}
		return "{\"ok\":true}";
	}

	/**
	 * Crea la politica si no existe y la asignarla al usuario de Minio
	 * 
	 */
	private void createPolicyInObjectStoreAndSetToUser(String authToken, String pathToFile, String userToAllow,
			String newUserToken, BinaryFileAccess.Type accessType)
			throws ObjectStoreCreatePolicyException, ObjectStoreBucketCreateException, ObjectStoreCreateUserException {

		String policyName = "Allow_" + accessType.name() + "_" + pathToFile.replace('/', '_').replace('.', '_');

		// Consulta si existe la politica y si no la crea
		if (!this.objectStorageService.existPolicy(authToken, policyName)) { // La politica no existe, se crea
			switch (accessType) {
			case READ:
				this.objectStorageService.createPolicyToReadFile(authToken, policyName, pathToFile);
				break;
			case WRITE:
				this.objectStorageService.createPolicyToWriteFile(authToken, policyName, pathToFile);
				break;
			default:
				throw new ObjectStoreCreatePolicyException("Access type not supported");
			}

		}

		// Comprueba si el usuario existe en MinIO y si no lo crea
		boolean existsUserInObjectStore = this.objectStorageService.existUserInObjectStore(authToken, userToAllow);
		if (!existsUserInObjectStore) { // Creates User
			this.objectStorageService.createBucketForUser(userToAllow);
			this.objectStorageService.createPolicyForBucketUser(authToken, userToAllow);
			this.objectStorageService.createUserInObjectStore(authToken, userToAllow, newUserToken);
		}

		// Asocia la politica al usuario --> Primero hay que recuperar las politicas que
		// tiene asociadas y luego llamar al set-policy separandolas por comas en el
		// pathparam
		String[] currentPoliciesForUser = this.objectStorageService.getUserInObjectStore(authToken, userToAllow)
				.getPolicy();

		List<String> lCurrentPoliciesForUser = new ArrayList<String>(Arrays.asList(currentPoliciesForUser));
		lCurrentPoliciesForUser.add(policyName);

		this.objectStorageService.setPoliciesForUser(authToken, lCurrentPoliciesForUser, userToAllow);

	}

	@Transactional
	@PostMapping(value = "/minio/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public ResponseEntity<String> deleteAuthorizationMinio(@RequestParam String id) throws GenericOPException {

		try {

			this.deletePolicyInObjectStore(id);

			User requesterUser = userService.getUser(webUtils.getUserId());
			binaryFileService.deleteBinaryFileAccess(id, requesterUser);

			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException | ObjectStoreLoginException | ObjectStoreCreatePolicyException e) {
			log.error("Error revoking permission to user", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/minio")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public String addBinaryMinio(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "filePath", required = false) String filePath,
			@RequestParam(value = "repository", required = false) RepositoryType repository,
			RedirectAttributes redirectAttributes) {

		if (file.getSize() <= 0) {
			webUtils.addRedirectMessage("binaryfiles.error.empty", redirectAttributes);
			return REDIRECT_MINIO_FILES_LIST;
		}

		if (webUtils.isFileExtensionForbidden(file)) {
			webUtils.addRedirectMessage("binaryfiles.error.extensionnotallowed", redirectAttributes);
			return REDIRECT_MINIO_FILES_LIST;
		}

		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue()) {
				throw new BinarySizeException("The file size is larger than max allowed");
			}

			String requesterUser = webUtils.getUserId();
			String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			this.objectStorageService.uploadObject(userToken,
					this.objectStorageService.getUserBucketName(requesterUser), filePath, file);

			return REDIRECT_MINIO_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);
			return REDIRECT_MINIO_FILES_LIST;
		}

	}

	@PostMapping("/minio/update")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public String updateMinio(@RequestParam("file") MultipartFile file, @RequestParam("fileId") String fileId,
			RedirectAttributes redirectAttributes) {
		try {

			String requesterUser = webUtils.getUserId();
			String requesterUserToken = userTokenService.getToken(userService.getUser(requesterUser)).getToken();
			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				throw e;
			}

			String userToken = this.objectStorageService.logIntoAdministrationObjectStorage(requesterUser,
					requesterUserToken, objectStoreAuthToken);

			BinaryFile bFileFromBDC = this.binaryFileService.getFile(fileId);

			String filePath = null;
			if (null != bFileFromBDC) {
				filePath = bFileFromBDC.getPath();
				binaryFileService.updateUpdateTime(fileId);
			} else {
				filePath = this.objectStorageService.decodeTemporalId(fileId);
				List<BinaryFile> tempfile = this.binaryFileService.getFileByPath(filePath);
				if (tempfile != null && tempfile.size() > 0) {
					binaryFileService.updateUpdateTime(tempfile.get(0).getId());
				}
			}

			if (filePath.indexOf('/') > 0) {
				filePath = filePath.substring(filePath.indexOf('/') + 1);// Elimina el Bucket
				if (filePath.indexOf('/') > 0) {// Elimina el nombre del fichero
					filePath = filePath.substring(0, filePath.lastIndexOf('/') + 1);
				} else {// El fichero estaba en la raiz
					filePath = "/";
				}
			}

			this.objectStorageService.uploadObject(userToken,
					this.objectStorageService.getUserBucketName(requesterUser), filePath, file);

			return REDIRECT_MINIO_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not update binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);
			return REDIRECT_MINIO_FILES_LIST;
		}

	}

	private void deletePolicyInObjectStore(String authorizationId)
			throws ObjectStoreLoginException, ObjectStoreCreatePolicyException {
		BinaryFileAccess bFileAccess = binaryFileService.getAuthorizationById(authorizationId);

		if (null != bFileAccess) {
			String accessType = bFileAccess.getAccessType().name();
			String pathToFile = bFileAccess.getBinaryFile().getPath();
			String userToDeny = bFileAccess.getUser().getUserId();

			String objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			String[] currentPoliciesForUser = this.objectStorageService
					.getUserInObjectStore(objectStoreAuthToken, userToDeny).getPolicy();

			String policyToDenyName = "Allow_" + accessType + "_" + pathToFile.replace('/', '_').replace('.', '_');

			List<String> newPoliciesForUser = new ArrayList<String>();
			for (String policy : currentPoliciesForUser) {
				if (!policy.equals(policyToDenyName)) {
					newPoliciesForUser.add(policy);
				}
			}

			this.objectStorageService.setPoliciesForUser(objectStoreAuthToken, newPoliciesForUser, userToDeny);

			if (this.objectStorageService.getUsersForPolicy(objectStoreAuthToken, policyToDenyName).length == 0) {
				this.objectStorageService.removePolicy(objectStoreAuthToken, policyToDenyName);
			}

		}
	}

	@PostMapping(value = "/minio/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	public @ResponseBody ResponseEntity<BinaryFileAccessDTO> updateAuthorization(@RequestParam String id,
			@RequestParam String accesstype) throws GenericOPException {

		try {

			this.deletePolicyInObjectStore(id);

			String objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();

			BinaryFileAccess bFileAccess = binaryFileService.getAuthorizationById(id);

			if (null != bFileAccess) {
				String pathToFile = bFileAccess.getBinaryFile().getPath();
				String userToModify = bFileAccess.getUser().getUserId();

				String newUserToken = userTokenService.getToken(userService.getUser(userToModify)).getToken();
				this.createPolicyInObjectStoreAndSetToUser(objectStoreAuthToken, pathToFile, userToModify, newUserToken,
						BinaryFileAccess.Type.valueOf(accesstype));
			}

			final BinaryFileAccess binaryFileAccessUpdated = binaryFileService.updateBinaryFileAccess(id, accesstype,
					userService.getUser(webUtils.getUserId()));
			final BinaryFileAccessDTO binaryFileAccessDTO = new BinaryFileAccessDTO(binaryFileAccessUpdated);

			return new ResponseEntity<>(binaryFileAccessDTO, HttpStatus.OK);
		} catch (final RuntimeException | ObjectStoreCreatePolicyException | ObjectStoreBucketCreateException
				| ObjectStoreCreateUserException | ObjectStoreLoginException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/minio/public")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR, ROLE_DEVELOPER, ROLE_DATASCIENTIST, ROLE_SYS_ADMIN')")
	@ResponseBody
	@Transactional
	public String changePublicMinio(@RequestParam("id") String fileId) {
		// Se gestiona mediante un grupo en MinIO "onesait_platform_public_files". Todos
		// los usuarios que se creen pertenecerán a ese grupo
		// Cuando se haga publico un fichero, se asociará la politicia al grupo
		try {
			String requesterUser = webUtils.getUserId();

			BinaryFile bFileFromBDC = this.binaryFileService.getFile(fileId);

			boolean setPublic = false;
			String filePath = null;
			if (null != bFileFromBDC) {
				filePath = bFileFromBDC.getPath();

				if (!bFileFromBDC.isPublic()) {
					// Set file as public
					this.binaryFileService.changePublic(bFileFromBDC.getId());
					setPublic = true;
				} else {
					this.binaryFileService.deleteFile(bFileFromBDC.getId());
				}

			} else {// Si el fichero no estaba en BDC es privado, lo pasamos a publico
				filePath = this.objectStorageService.decodeTemporalId(fileId);
				// Creates file as Public in BDC
				BinaryFile bfile = this.objectStorageService.buildBinaryFile(requesterUser, filePath);
				bfile.setPublic(true);
				this.binaryFileService.createBinaryFile(bfile);
				setPublic = true;
			}

			String objectStoreAuthToken = null;
			try {
				objectStoreAuthToken = this.objectStorageService.logIntoObjectStorageWithSuperUser();
			} catch (ObjectStoreLoginException e) {
				log.error("Error loing with superuser in MinIO", e);
				return "ko";
			}

			if (setPublic) {
				this.setPublicFile(objectStoreAuthToken, filePath);
			} else {
				this.setPrivateFile(objectStoreAuthToken, filePath);
			}

		} catch (ObjectStoreCreatePolicyException e) {
			log.error("Error setting file to public", e);
			return "ko";
		}

		return "ok";
	}

	@GetMapping(value = "/minio/maxsize", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Long>> maxSizeMinio() {
		final Map<String, Long> map = new HashMap<>();
		map.put("maxsize", webUtils.getMaxFileSizeAllowed());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	private void setPublicFile(String objectStoreAuthToken, String filePath) throws ObjectStoreCreatePolicyException {

		String policyName = "Allow_PUBLIC_" + filePath.replace('/', '_').replace('.', '_');

		// Creates the policy
		if (!this.objectStorageService.existPolicy(objectStoreAuthToken, policyName)) {
			this.objectStorageService.createPolicyToReadFile(objectStoreAuthToken, policyName, filePath);
		}

		// Set the policy to the group of all users to share public files
		this.objectStorageService.setPoliciesMulti(objectStoreAuthToken,
				new ArrayList<>(Arrays.asList(new String[] { policyName })), new ArrayList<String>(), new ArrayList<>(
						Arrays.asList(new String[] { MinioObjectStorageService.ONESAIT_PLATFORM_PUBLIC_FILES_GROUP })));
	}

	private void setPrivateFile(String objectStoreAuthToken, String filePath) throws ObjectStoreCreatePolicyException {

		String policyName = "Allow_PUBLIC_" + filePath.replace('/', '_').replace('.', '_');

		// Removes the policy
		if (this.objectStorageService.existPolicy(objectStoreAuthToken, policyName)) {
			this.objectStorageService.createPolicyToReadFile(objectStoreAuthToken, policyName, filePath);

			this.objectStorageService.removePolicy(objectStoreAuthToken, policyName);

//			// Set the policy to the group of all users to share public files
//			this.objectStorageService.setPoliciesMulti(objectStoreAuthToken,
//					new ArrayList<>(Arrays.asList(new String[] { policyName })), new ArrayList<String>(),
//					new ArrayList<>());
		}

	}

	@GetMapping("gcp")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER')")
	@Transactional
	public String listGcp(Model model) {
		final List<BinaryFile> list = binaryFileService.getAllFiles(userService.getUser(webUtils.getUserId()), true)
				.stream().filter(bf -> bf.getRepository() == RepositoryType.GCP).collect(Collectors.toList());

		final Map<String, String> accessMap = new HashMap<>();
		final List<BinaryFile> filteredList = list.stream()
				.filter(bf -> !bf.getUser().getUserId().equals(webUtils.getUserId())).collect(Collectors.toList());
		filteredList.forEach(bf -> bf.getFileAccesses().forEach(bfa -> {
			if (bfa.getUser().getUserId().equals(webUtils.getUserId()))
				accessMap.put(bf.getId(), bfa.getAccessType().name());
		}));
		model.addAttribute("files", list);
		model.addAttribute("accessMap", accessMap);
		model.addAttribute("accessTypes", BinaryFileAccess.Type.values());
		model.addAttribute("users", userService.getAllUsers());
		model.addAttribute("repos", RepositoryType.values());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
			httpSession.removeAttribute(APP_ID);
		}

		return "binaryfiles/gcp";
	}

	@PostMapping("gcp")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String addBinaryGcp(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) RepositoryType repository,
			@RequestParam(value = "appID", required = false) String appID,
			@RequestParam(value = "filePath", required = false) String filePath,
			RedirectAttributes redirectAttributes) {

		if (file.getSize() <= 0) {
			webUtils.addRedirectMessage("binaryfiles.error.empty", redirectAttributes);
			return REDIRECT_GCP_FILES_LIST;
		}

		if (webUtils.isFileExtensionForbidden(file)) {
			webUtils.addRedirectMessage("binaryfiles.error.extensionnotallowed", redirectAttributes);
			return REDIRECT_GCP_FILES_LIST;
		}

		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue()) {
				throw new BinarySizeException("The file size is larger than max allowed");
			}

			binaryFactory.getInstance(RepositoryType.GCP).addBinary(file, metadata, repository, filePath);

			if (appID != null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.BINARYFILE.toString());
				httpSession.setAttribute("resourceIdentificationAdded", file.getOriginalFilename());
				return REDIRECT_PROJECT_SHOW + appID;
			}

			return REDIRECT_GCP_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);

			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				httpSession.removeAttribute(APP_ID);
				return REDIRECT_PROJECT_SHOW + projectId.toString();
			}

			return REDIRECT_GCP_FILES_LIST;
		}

	}

	@GetMapping("/gcp/{id}")
	public ResponseEntity<ByteArrayResource> getBinaryGcp(@PathVariable("id") String fileId,
			@RequestParam(value = "disposition", required = false) String disposition) {
		try {

			final BinaryFileData file = binaryFactory.getInstance(RepositoryType.GCP).getBinaryFile(fileId);
			final ByteArrayResource resource = new ByteArrayResource(
					((ByteArrayOutputStream) file.getData()).toByteArray());

			if (StringUtils.isEmpty(disposition)) {
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(file.getFileName()))
						.contentLength(resource.contentLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
			} else {
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline")
						.contentType(MediaType.parseMediaType(file.getContentType()))
						.contentLength(resource.contentLength()).body(resource);
			}

		} catch (final BinaryRepositoryException e) {

			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		} catch (final IOException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("/gcp/public")
	@ResponseBody
	public String changePublicGcp(@RequestParam("id") String fileId) {
		if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {
			binaryFileService.changePublic(fileId);
			return "ok";
		} else {
			return "ko";
		}
	}

	@DeleteMapping("/gcp/{fileId}")
	public @ResponseBody String deleteGcp(Model model, @PathVariable String fileId, RedirectAttributes ra) {
		try {
			if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {

				List<ODBinaryFilesDataset> bfilesDatasetList = binaryFilesDatasetService
						.getBinaryFilesByFilesId(fileId);
				if (bfilesDatasetList == null || bfilesDatasetList.isEmpty()) {
					binaryFactory.getInstance(RepositoryType.GCP).removeBinary(fileId);
				} else {
					throw new BinaryRepositoryException(
							"The file cannot be deleted. There is a dataset with this file.");
				}

			}
		} catch (final RuntimeException e) {
			webUtils.addRedirectException(e, ra);
			return e.getMessage();
		} catch (BinaryRepositoryException ex) {
			webUtils.addRedirectException(ex, ra);
			return ex.getMessage();
		}
		return "{\"ok\":true}";
	}

	@PostMapping("/gcp/update")
	public String updateGcp(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata, @RequestParam("fileId") String fileId,
			RedirectAttributes redirectAttributes) {
		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue())
				throw new BinarySizeException("The file size is larger than max allowed");
			binaryFactory.getInstance(RepositoryType.GCP).updateBinary(fileId, file, metadata);
			return REDIRECT_GCP_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not update binary file: {}", e);
			webUtils.addRedirectException(e, redirectAttributes);
			return REDIRECT_GCP_FILES_LIST;
		}

	}

	@GetMapping("/gcp/metadata/{fileId}")
	@ResponseBody
	public String getMetadataGcp(@PathVariable String fileId) {
		if (binaryFileService.hasUserPermissionRead(fileId, userService.getUser(webUtils.getUserId())))
			return binaryFileService.getFile(fileId).getMetadata();
		else
			return "forbidden";

	}

	@GetMapping(value = "/gcp/authorization/{fileId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@Transactional
	public @ResponseBody ResponseEntity<List<BinaryFileAccessDTO>> getAuthorizationGcp(@PathVariable String fileId) {

		try {
			final List<BinaryFileAccess> accesses = binaryFileService.getAuthorizations(fileId,
					userService.getUser(webUtils.getUserId()));
			final List<BinaryFileAccessDTO> authorizationsDTO = new ArrayList<>();
			accesses.stream().forEach(a -> authorizationsDTO.add(new BinaryFileAccessDTO(a)));
			return new ResponseEntity<>(authorizationsDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/gcp/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BinaryFileAccessDTO> createAuthorizationGcp(@RequestParam String accesstype,
			@RequestParam String fileId, @RequestParam String user) throws GenericOPException {

		try {
			final BinaryFileAccess binaryFileAccessCreated = binaryFileService.createBinaryFileAccess(fileId, user,
					accesstype, userService.getUser(webUtils.getUserId()));
			final BinaryFileAccessDTO binaryFileAccessDTO = new BinaryFileAccessDTO(binaryFileAccessCreated);
			return new ResponseEntity<>(binaryFileAccessDTO, HttpStatus.CREATED);

		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/gcp/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorizationGcp(@RequestParam String id) throws GenericOPException {

		try {
			binaryFileService.deleteBinaryFileAccess(id, userService.getUser(webUtils.getUserId()));
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/gcp/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<BinaryFileAccessDTO> updateAuthorizationGcp(@RequestParam String id,
			@RequestParam String accesstype) throws GenericOPException {

		try {
			final BinaryFileAccess binaryFileAccessUpdated = binaryFileService.updateBinaryFileAccess(id, accesstype,
					userService.getUser(webUtils.getUserId()));
			final BinaryFileAccessDTO binaryFileAccessDTO = new BinaryFileAccessDTO(binaryFileAccessUpdated);

			return new ResponseEntity<>(binaryFileAccessDTO, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

}
