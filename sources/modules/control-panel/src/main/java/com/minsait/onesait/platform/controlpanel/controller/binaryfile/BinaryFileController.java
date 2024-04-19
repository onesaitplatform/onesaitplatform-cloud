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
package com.minsait.onesait.platform.controlpanel.controller.binaryfile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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
import com.minsait.onesait.platform.business.services.binaryrepository.BinaryRepositoryLogicService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.comms.protocol.binary.BinarySizeException;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.ODBinaryFilesDataset;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/files")
@Slf4j
public class BinaryFileController {

	@Autowired
	private BinaryRepositoryLogicService binaryRepositoryLogicService;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private AppWebUtils webUtils;
	@Autowired
	private UserService userService;

	private static final String REDIRECT_FILES_LIST = "redirect:/files/list";

	@GetMapping("list")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER')")
	@Transactional
	public String list(Model model) {
		final List<BinaryFile> list = binaryFileService.getAllFiles(userService.getUser(webUtils.getUserId()));
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
		return "binaryfiles/list";
	}

	@PostMapping
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String addBinary(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata,
			@RequestParam(value = "repository", required = false) RepositoryType repository,
			RedirectAttributes redirectAttributes) {

		if (file.getSize() <= 0) {
			webUtils.addRedirectMessage("binaryfiles.error.empty", redirectAttributes);
			return REDIRECT_FILES_LIST;
		}

		if (webUtils.isFileExtensionForbidden(file)) {
			webUtils.addRedirectMessage("binaryfiles.error.extensionnotallowed", redirectAttributes);
			return REDIRECT_FILES_LIST;
		}

		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue())
				throw new BinarySizeException("The file size is larger than max allowed");
			binaryRepositoryLogicService.addBinary(file, metadata, repository);
			return REDIRECT_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);
			return REDIRECT_FILES_LIST;
		}

	}

	@GetMapping("/{id}")
	public ResponseEntity<ByteArrayResource> getBinary(@PathVariable("id") String fileId,
			@RequestParam(value = "disposition", required = false) String disposition) {
		try {

			final BinaryFileData file = binaryRepositoryLogicService.getBinaryFile(fileId);
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

	@PostMapping("/public")
	@ResponseBody
	public String changePublic(@RequestParam("id") String fileId) {
		if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {
			binaryFileService.changePublic(fileId);
			return "ok";
		} else {
			return "ko";
		}
	}

	@DeleteMapping("/{fileId}")
	public @ResponseBody String delete(Model model, @PathVariable String fileId, RedirectAttributes ra) {
		try {
			if (binaryFileService.isUserOwner(fileId, userService.getUser(webUtils.getUserId()))) {
				binaryRepositoryLogicService.removeBinary(fileId);
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

	@PostMapping("/update")
	public String update(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "metadata", required = false) String metadata, @RequestParam("fileId") String fileId,
			RedirectAttributes redirectAttributes) {
		try {
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue())
				throw new BinarySizeException("The file size is larger than max allowed");
			binaryRepositoryLogicService.updateBinary(fileId, file, metadata);
			return REDIRECT_FILES_LIST;

		} catch (final Exception e) {
			log.error("Could not update binary file: {}", e);
			webUtils.addRedirectMessage("binaryfiles.error", redirectAttributes);
			return REDIRECT_FILES_LIST;
		}

	}

	@GetMapping("/metadata/{fileId}")
	@ResponseBody
	public String getMetadata(@PathVariable String fileId) {
		if (binaryFileService.hasUserPermissionRead(fileId, userService.getUser(webUtils.getUserId())))
			return binaryFileService.getFile(fileId).getMetadata();
		else
			return "forbidden";

	}

	@GetMapping(value = "/authorization/{fileId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@Transactional
	public @ResponseBody ResponseEntity<List<BinaryFileAccessDTO>> getAuthorizations(@PathVariable String fileId) {

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

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BinaryFileAccessDTO> createAuthorization(@RequestParam String accesstype,
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

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) throws GenericOPException {

		try {
			binaryFileService.deleteBinaryFileAccess(id, userService.getUser(webUtils.getUserId()));
			return new ResponseEntity<>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<BinaryFileAccessDTO> updateAuthorization(@RequestParam String id,
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

	@GetMapping(value = "/maxsize", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Long>> maxSize() {
		final Map<String, Long> map = new HashMap<>();
		map.put("maxsize", webUtils.getMaxFileSizeAllowed());
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
