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
package com.minsait.onesait.platform.controlpanel.controller.opendata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.business.services.binaryrepository.factory.BinaryRepositoryServiceFactory;
import com.minsait.onesait.platform.business.services.dataset.DatasetService;
import com.minsait.onesait.platform.business.services.opendata.OpenDataPermissions;
import com.minsait.onesait.platform.business.services.opendata.organization.OrganizationService;
import com.minsait.onesait.platform.business.services.resources.ResourceService;
import com.minsait.onesait.platform.comms.protocol.binary.BinarySizeException;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.ODBinaryFilesDataset;
import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.ODTypologyDataset;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.opendata.binaryFiles.BinaryFilesDatasetService;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataLicense;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResourceDTO;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyDatasetService;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.exception.BinaryFileException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/opendata/datasets")
@Controller
@Slf4j
public class DatasetController {

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private DatasetService datasetService;
	@Autowired
	private OpenDataPermissions openDataPermissions;
	@Autowired
	private ResourceService resourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private TypologyService typologyService;
	@Autowired
	private TypologyDatasetService typologydatasetService;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private BinaryFilesDatasetService binaryFilesDatasetService;
	@Autowired
	private AppWebUtils webUtils;
	@Autowired
	private BinaryRepositoryServiceFactory binaryFactory;
	@Autowired
	private HttpSession httpSession;

	private static final String APP_ID = "appId";

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER,ROLE_DATASCIENTIST')")
	@GetMapping(value = "/list", produces = "text/html")
	public String listDatasets(Model model, @RequestParam(required = false) String name,
			@RequestParam(required = false) String tag, RedirectAttributes redirect) {

		// CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);

		if (name != null && name.equals("")) {
			name = null;
		}
		if (tag != null && tag.equals("")) {
			tag = null;
		}
		final String userToken = utils.getCurrentUserOauthToken();
		try {
			final List<OpenDataOrganization> organizationsFromUser = organizationService
					.getOrganizationsFromUser(userToken);
			final List<OpenDataPackage> datasets = datasetService.getDatasetsByUser(userToken, name, tag);
			final List<OpenDataPackageDTO> datasetsDTO = datasetService.getDTOFromDatasetList(datasets,
					organizationsFromUser);
			for (OpenDataPackageDTO odp : datasetsDTO) {
				final ODTypologyDataset typologydataset = typologydatasetService.getTypologyByDatasetId(odp.getId());
				if (typologydataset != null) {
					final ODTypology typology = typologyService.getTypologyById(typologydataset.getTypologyId());
					odp.setTypology(typology.getIdentification());
				}
			}

			final boolean createPermissions = datasetService.getCreatePermissions(organizationsFromUser);

			model.addAttribute("datasets", datasetsDTO);
			model.addAttribute("createPermissions", createPermissions);
		} catch (final ResourceAccessException e) {
			log.error("Error listing datasets: " + e.getMessage());
			utils.addRedirectMessage("datasets.error.accessing", redirect);
			return "redirect:/main";
		}
		return "opendata/datasets/list";
	}

	@GetMapping(value = "/create")
	public String createDataset(Model model) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateDataset(userToken, null)) {
			return "error/403";
		}

		List<OpenDataOrganization> organizationsFromUser = organizationService.getOrganizationsFromUser(userToken);
		organizationsFromUser = organizationsFromUser.stream()
				.filter(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"))
				.collect(Collectors.toList());
		final List<OpenDataLicense> licensesList = datasetService.getLicensesList();
		final List<ODTypology> typologiesList = typologyService.getAllTypologies();
		final List<BinaryFile> filesList = binaryFileService.getAllFiles(userService.getUser(utils.getUserId()));

		model.addAttribute("licensesList", licensesList);
		model.addAttribute("dataset", new OpenDataPackageDTO());
		model.addAttribute("organizations", organizationsFromUser);
		model.addAttribute("typologiesList", typologiesList);
		model.addAttribute("filesList", filesList);
		return "opendata/datasets/create";
	}

	@PostMapping(value = { "/create" }, produces = "text/html")
	public ResponseEntity<String> createDataset(Model model, @ModelAttribute OpenDataPackageDTO dataset,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToCreateDataset(userToken, dataset)) {
			return new ResponseEntity<String>("The user has not permissions to create a dataset", HttpStatus.FORBIDDEN);
		}

		try {
			dataset.setName(datasetService.getDatasetId(dataset.getTitle()));
			if (datasetService.existsDataset(dataset, userToken)) {
				log.error("This Dataset already exist");
				return new ResponseEntity<String>(
						utils.getMessage("datasets.error.exist", "This Dataset already exist"),
						HttpStatus.NOT_ACCEPTABLE);
			}
			datasetService.createDataset(dataset, userToken);
			log.debug("Dataset has been created succesfully");
			return new ResponseEntity<String>("", HttpStatus.OK);

		} catch (final HttpClientErrorException e) {
			log.error("Cannot create dataset in Open Data Portal: " + e.getResponseBodyAsString());
			return new ResponseEntity<String>(
					"Cannot create dataset in Open Data Portal: " + e.getResponseBodyAsString(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (final Exception e) {
			log.error("Cannot create dataset: " + e.getMessage());
			return new ResponseEntity<String>(utils.getMessage("datasets.error.created", "Cannot create dataset"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String showDataset(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final String userToken = utils.getCurrentUserOauthToken();

			OpenDataPackage dataset = null;
			if (id != null) {
				dataset = datasetService.getDatasetById(userToken, id);
			}
			if (dataset == null) {
				return "error/404";
			}
			if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToShowDataset(dataset)) {
				return "error/403";
			}

			final OpenDataPackageDTO datasetDTO = datasetService.getDTOFromDataset(dataset,
					dataset.getOrganization().getTitle(), dataset.getLicense_title());

			final ODTypologyDataset typologydataset = typologydatasetService.getTypologyByDatasetId(datasetDTO.getId());
			if (typologydataset != null) {
				final ODTypology typology = typologyService.getTypologyById(typologydataset.getTypologyId());
				datasetDTO.setTypology(typology.getIdentification());
			}
			final List<ODBinaryFilesDataset> files = binaryFilesDatasetService
					.getBinaryFilesByDatasetId(datasetDTO.getId());
			List<String> datasetFilesList = new ArrayList<String>();
			for (ODBinaryFilesDataset file : files) {
				String fileName = binaryFileService.getFile(file.getFilesId()).getFileName();
				datasetFilesList.add(fileName);
			}
			datasetDTO.setFiles(datasetFilesList);

			List<BinaryFile> binaryFiles = binaryFilesDatasetService
					.getBinaryFilesObjectByDatasetId(datasetDTO.getId());
			model.addAttribute("binaryFiles", binaryFiles);

			final List<OpenDataOrganization> organizationsFromUser = organizationService
					.getOrganizationsFromUser(userToken);
			final boolean modifyPermissions = datasetService.getModifyPermissions(organizationsFromUser,
					dataset.getOrganization().getId());

			final List<OpenDataResource> resources = dataset.getResources();
			final List<DatasetResource> configResources = datasetService.getConfigResources(resources);

			final List<OpenDataPackage> datasets = new ArrayList<>();
			datasets.add(dataset);
			final List<OpenDataResourceDTO> resourcesDTO = resourceService.getDTOFromResourceList(resources,
					configResources, datasets, organizationsFromUser);

			model.addAttribute("resourcesList", resourcesDTO);
			model.addAttribute("dataset", datasetDTO);
			model.addAttribute("modifyPermissions", modifyPermissions);
			return "opendata/datasets/show";
		} catch (final Exception e) {
			log.error("Error in Dataset controller: " + e.getMessage());
			return "error/500";
		}
	}

	@GetMapping(value = "/update/{id}")
	public String updateDataset(@PathVariable String id, Model model, HttpServletRequest request) {

		final String userToken = utils.getCurrentUserOauthToken();
		OpenDataPackage dataset = null;
		if (id != null) {
			dataset = datasetService.getDatasetById(userToken, id);
		}
		if (dataset == null) {
			return "error/404";
		}
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateDataset(userToken, dataset)) {
			return "error/403";
		}

		final OpenDataPackageDTO datasetDTO = datasetService.getDTOFromDataset(dataset, dataset.getOwner_org(),
				dataset.getLicense_id());

		final ODTypologyDataset typologydataset = typologydatasetService.getTypologyByDatasetId(datasetDTO.getId());
		if (typologydataset != null) {
			final ODTypology typology = typologyService.getTypologyById(typologydataset.getTypologyId());
			datasetDTO.setTypology(typology.getId());
		}
		final List<ODBinaryFilesDataset> files = binaryFilesDatasetService
				.getBinaryFilesByDatasetId(datasetDTO.getId());
		List<String> datasetFilesList = new ArrayList<String>();
		for (ODBinaryFilesDataset file : files) {
			datasetFilesList.add(file.getFilesId());
		}
		datasetDTO.setFiles(datasetFilesList);

		List<OpenDataOrganization> organizationsFromUser = organizationService.getOrganizationsFromUser(userToken);
		organizationsFromUser = organizationsFromUser.stream()
				.filter(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"))
				.collect(Collectors.toList());
		final List<OpenDataLicense> licensesList = datasetService.getLicensesList();
		final List<ODTypology> typologiesList = typologyService.getAllTypologies();
		final List<BinaryFile> filesList = binaryFileService.getAllFiles(userService.getUser(utils.getUserId()));

		model.addAttribute("typologiesList", typologiesList);
		model.addAttribute("filesList", filesList);
		model.addAttribute("licensesList", licensesList);
		model.addAttribute("organizations", organizationsFromUser);
		model.addAttribute("dataset", datasetDTO);
		return "opendata/datasets/create";
	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateDataset(@PathVariable String id, Model model, @ModelAttribute OpenDataPackageDTO datasetDTO,
			RedirectAttributes redirect, HttpServletRequest request) {
		final String userToken = utils.getCurrentUserOauthToken();
		if (datasetDTO != null) {
			if (!utils.isAdministrator()
					&& !openDataPermissions.hasPermissionsToManipulateDataset(userToken, datasetDTO)) {
				return "error/403";
			}
			try {
				final OpenDataPackage dataset = datasetService.getDatasetById(userToken, id);
				datasetDTO.setName(dataset.getName());
				if (dataset != null) {
					datasetService.updateDataset(datasetDTO, userToken);
					if (datasetDTO.getIsPublic() && dataset.getIsPrivate()) {
						resourceService.updatePlatformResourcesFromDataset(dataset,
								userService.getUser(utils.getUserId()));
					}
				}
			} catch (final HttpClientErrorException e) {
				log.error("Cannot update dataset in Open Data Portal: " + e.getResponseBodyAsString());
				utils.addRedirectMessage(" Cannot update dataset in Open Data Portal: " + e.getResponseBodyAsString(),
						redirect);
				return "redirect:/opendata/datasets/list";
			} catch (final Exception e) {
				log.debug("Cannot update dataset " + e.getMessage());
				utils.addRedirectMessage("datasets.error.updated", redirect);
				return "redirect:/opendata/datasets/list";
			}
		} else {
			return "redirect:/opendata/datasets/update/" + id;
		}
		log.debug("Dataset has been updated succesfully");
		return "redirect:/opendata/datasets/show/" + datasetDTO.getId();
	}

	@DeleteMapping("/delete/{id}")
	public String deleteDataset(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final String userToken = utils.getCurrentUserOauthToken();
		final OpenDataPackage dataset = datasetService.getDatasetById(userToken, id);
		if (!utils.isAdministrator() && !openDataPermissions.hasPermissionsToManipulateDataset(userToken, dataset)) {
			return "error/403";
		}
		if (dataset != null) {
			try {
				final List<OpenDataResource> resources = dataset.getResources();
				// Borramos sus recursos de la ConfigDB
				for (final OpenDataResource resource : resources) {
					resourceService.persistResource(resource.getId());
				}
				datasetService.deleteDataset(userToken, id);
			} catch (final EmptyResultDataAccessException e) {
				log.debug("The resource does not exist in ConfigDB: " + e.getMessage());
				datasetService.deleteDataset(userToken, id);
			} catch (final Exception e) {
				log.error("Could not delete the Dataset");
				utils.addRedirectMessage("datasets.error.delete", redirect);
			}
			log.debug("The Dataset has been deleted correctly");
		} else {
			log.debug("The Dataset does not exist");
		}
		return "redirect:/opendata/datasets/list";
	}

	@PostMapping(value = "/uploadFile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody String uploadFile(MultipartHttpServletRequest request) {

		final Iterator<String> itr = request.getFileNames();
		final String uploadedFile = itr.next();
		final MultipartFile file = request.getFile(uploadedFile);
		BinaryFile response = null;
		try {
			if (file != null) {
				if (utils.isFileExtensionForbidden(file)) {
					log.error("File type not allowed");
					throw new BinaryFileException("File type not allowed");
				}
				if (file.getSize() > utils.getMaxFileSizeAllowed()) {
					log.error("File size too large");
					throw new BinarySizeException("The file size is larger than max allowed");
				}
			}
			if (file.getSize() > webUtils.getMaxFileSizeAllowed().longValue())
				throw new BinarySizeException("The file size is larger than max allowed");

			String id = binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).addBinary(file, "datasetFile", null);
			response = binaryFileService.getFile(id);

		} catch (final Exception e) {
			log.error("Could not create binary file: {}", e);
			throw new BinaryFileException(e.getMessage());

		}
		return "{\"id\":\"" + response.getId() + "\", \"name\":\"" + response.getFileName() + "\"}";
	}
}
