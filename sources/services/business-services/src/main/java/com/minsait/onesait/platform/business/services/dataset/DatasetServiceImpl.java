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
package com.minsait.onesait.platform.business.services.dataset;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.minsait.onesait.platform.business.services.opendata.OpenDataApi;
import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.model.ODBinaryFilesDataset;
import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.ODTypologyDataset;
import com.minsait.onesait.platform.config.repository.DatasetResourceRepository;
import com.minsait.onesait.platform.config.services.exceptions.OpenDataServiceException;
import com.minsait.onesait.platform.config.services.opendata.binaryFiles.BinaryFilesDatasetService;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataLicense;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageList;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataTag;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.LicenseListResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.PackageSearchResponse;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.PackageShowResponse;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyDatasetService;
import com.minsait.onesait.platform.config.services.opendata.typology.TypologyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private OpenDataApi api;
	@Autowired
	private DatasetResourceRepository resourceRepository;
	@Autowired
	private TypologyDatasetService typologyDatasetService;
	@Autowired
	private TypologyService typologyService;
	@Autowired
	private BinaryFilesDatasetService binaryFileDatasetService;

	private static final int ROWS_LIMIT = 1000;
	private static final String DELETED = "deleted";

	@Override
	public OpenDataPackageList getDatasetsListByUser(String userToken, String query, String sort, Integer rows,
			Integer start) {
		String parameters = "";
		if (query != null) {
			parameters += "&q=" + query;
		}
		if (sort != null) {
			parameters += "&sort=" + sort;
		}

		return getDatasetsByUserWithPagination(userToken, parameters, rows, start);
	}

	@Override
	public List<OpenDataPackage> getDatasetsByUser(String userToken, String name, String tag) {
		String parameters = "";
		if (name != null) {
			parameters += "&q=title:*" + name + "*";
		}
		if (tag != null) {
			parameters += "&fq=tags:*" + tag + "*";
		}
		return getDatasetsByUserWithPagination(userToken, parameters, null, null).getResults();
	}

	@Override
	public List<OpenDataPackage> getDatasetsByUser(String userToken) {
		return getDatasetsByUserWithPagination(userToken, null, null, null).getResults();
	}

	private OpenDataPackageList getDatasetsByUserWithPagination(String userToken, String parameters, Integer rows,
			Integer start) {
		final String endpoint = "package_search?include_private=true";
		if (parameters == null) {
			parameters = "";
		}
		if (start == null) {
			start = 0;
		}
		String paginate = "&rows=" + ROWS_LIMIT + "&start=" + start;
		if (rows != null && rows < ROWS_LIMIT) {
			paginate = "&rows=" + rows + "&start=" + start;
		}

		PackageSearchResponse responsePackages = (PackageSearchResponse) api
				.getOperation(endpoint + parameters + paginate, userToken, PackageSearchResponse.class);

		if (responsePackages.getSuccess()) {
			final List<OpenDataPackage> datasets = responsePackages.getResult().getResults();
			final int count = responsePackages.getResult().getCount();
			int counter = 0;
			if (rows == null && count > ROWS_LIMIT) {
				counter = (int) Math.ceil((float) count / ROWS_LIMIT);
			} else if (rows != null && rows > ROWS_LIMIT) {
				counter = (int) Math.ceil((float) rows / ROWS_LIMIT);
			}
			for (int i = 1; i < counter; i++) {
				paginate = "&rows=" + ROWS_LIMIT + "&start=" + ((ROWS_LIMIT * i) + start);
				responsePackages = (PackageSearchResponse) api.getOperation(endpoint + parameters + paginate, userToken,
						PackageSearchResponse.class);
				if (responsePackages.getSuccess()) {
					datasets.addAll(responsePackages.getResult().getResults());
				}
			}
			if (rows != null && datasets.size() > rows) {
				return new OpenDataPackageList(count, datasets.subList(0, rows));
			}
			return new OpenDataPackageList(count, datasets);
		}
		return new OpenDataPackageList(0, new ArrayList<>());
	}

	@Override
	public List<OpenDataPackage> getDatasetsByUserWithPermissions(String userToken,
			List<OpenDataOrganization> orgsFromUser) {
		final List<OpenDataPackage> datasets = getDatasetsByUser(userToken);
		final List<OpenDataPackage> datasetsFromUser = new ArrayList<>();
		for (final OpenDataPackage dataset : datasets) {
			final String orgId = dataset.getOwner_org();
			final Optional<OpenDataOrganization> foundOrg = orgsFromUser.stream()
					.filter(elem -> elem.getId().equals(orgId)).findFirst();
			if (foundOrg.isPresent()) {
				datasetsFromUser.add(dataset);
			}
		}
		return datasetsFromUser;
	}

	@Override
	public List<OpenDataPackageDTO> getDTOFromDatasetList(List<OpenDataPackage> datasets,
			List<OpenDataOrganization> orgsFromUser) {
		final List<OpenDataPackageDTO> dtos = new ArrayList<>();
		for (final OpenDataPackage pkg : datasets) {
			final OpenDataPackageDTO obj = new OpenDataPackageDTO();
			obj.setName(pkg.getTitle());
			obj.setIsPublic(!pkg.getIsPrivate());
			obj.setOrganization(pkg.getOrganization().getTitle());
			obj.setId(pkg.getId());

			final String organizationId = pkg.getOrganization().getId();
			final Optional<OpenDataOrganization> foundOrg = orgsFromUser.stream()
					.filter(elem -> elem.getId().equals(organizationId)).findFirst();
			if (foundOrg.isPresent()) {
				obj.setRole(foundOrg.get().getCapacity());
			} else {
				// Dataset publico que no pertenece a sus organizaciones
				obj.setRole("");
			}

			Date created = null;
			Date modified = null;
			try {
				created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(pkg.getMetadata_created());
				if (pkg.getMetadata_modified() != null) {
					modified = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(pkg.getMetadata_modified());
				}
			} catch (final ParseException e) {
				e.printStackTrace();
			}
			obj.setCreatedAt(created);
			obj.setUpdatedAt(modified);

			final List<String> tagsNames = new ArrayList<String>();
			if (pkg.getTags() != null && !pkg.getTags().isEmpty()) {
				for (final OpenDataTag tag : pkg.getTags()) {
					tagsNames.add(tag.getName());
				}
			}
			obj.setTags(tagsNames);
			dtos.add(obj);
		}
		return dtos;
	}

	@Override
	public boolean getCreatePermissions(List<OpenDataOrganization> organizationsFromUser) {
		return organizationsFromUser.stream()
				.anyMatch(elem -> elem.getCapacity().equals("admin") || elem.getCapacity().equals("editor"));
	}

	@Override
	public List<OpenDataLicense> getLicensesList() {
		final LicenseListResponse responseLicenses = (LicenseListResponse) api.getOperation("license_list", null,
				LicenseListResponse.class);
		if (responseLicenses.getSuccess()) {
			return responseLicenses.getResult();
		}
		return new ArrayList<>();
	}

	@Override
	public boolean existsDataset(OpenDataPackageDTO dataset, String userToken) {
		final String datasetName = dataset.getName();
		try {
			final PackageShowResponse responsePackage = (PackageShowResponse) api
					.getOperation("package_show?id=" + datasetName, userToken, PackageShowResponse.class);
			if (responsePackage.getSuccess() && responsePackage.getResult().getState().equals(DELETED)) {
				throw new OpenDataServiceException("Dataset exists in deleted state");
			}
			return responsePackage.getSuccess();
		} catch (final HttpClientErrorException e) {
			return e.getStatusCode() != HttpStatus.NOT_FOUND;
		}
	}

	@Override
	public OpenDataPackage createDataset(OpenDataPackageDTO datasetDTO, String userToken) {
		final OpenDataPackage dataset = new OpenDataPackage();
		dataset.setIsPrivate(!datasetDTO.getIsPublic());
		dataset.setOwner_org(datasetDTO.getOrganization());
		dataset.setTitle(datasetDTO.getTitle());
		dataset.setName(datasetDTO.getName());
		dataset.setLicense_id(datasetDTO.getLicense());

		if (datasetDTO.getDescription() != null && !datasetDTO.getDescription().equals("")) {
			dataset.setNotes(datasetDTO.getDescription());
		}
		if (datasetDTO.getTags() != null && !datasetDTO.getTags().isEmpty()) {
			final List<OpenDataTag> tagsList = new ArrayList<>();
			for (final String tag : datasetDTO.getTags()) {
				final OpenDataTag newTag = new OpenDataTag();
				newTag.setName(tag);
				tagsList.add(newTag);
			}
			dataset.setTags(tagsList);
		}
		final PackageShowResponse responsePackage = (PackageShowResponse) api
				.postOperation("package_create", userToken, dataset, PackageShowResponse.class).getBody();
		if (responsePackage != null && responsePackage.getSuccess()) {
			final String datasetId = responsePackage.getResult().getId();

			final ODTypology typology = typologyService.getTypologyById(datasetDTO.getTypology());
			if (typology != null) {
				final ODTypologyDataset typologyDataset = new ODTypologyDataset();
				typologyDataset.setDatasetId(datasetId);
				typologyDataset.setTypologyId(typology.getId());
				typologyDatasetService.createNewTypologyDataset(typologyDataset);
			}
			final List<String> filesList = datasetDTO.getFiles();
			for (final String idFile : filesList) {
				final ODBinaryFilesDataset binaryFileDataset = new ODBinaryFilesDataset();
				binaryFileDataset.setDatasetId(datasetId);
				binaryFileDataset.setFilesId(idFile);
				binaryFileDatasetService.createNewBinaryFiles(binaryFileDataset);
			}

			return responsePackage.getResult();
		} else {
			return null;
		}
	}

	@Override
	public OpenDataPackage getDatasetById(String userToken, String id) {
		try {
			final PackageShowResponse responsePackage = (PackageShowResponse) api.getOperation("package_show?id=" + id,
					userToken, PackageShowResponse.class);
			if (responsePackage.getSuccess()) {
				final OpenDataPackage dataset = responsePackage.getResult();
				if (dataset.getState().equals(DELETED)) {
					throw new OpenDataServiceException("Dataset exists in deleted state");
				}
				return dataset;
			} else {
				return null;
			}
		} catch (final HttpClientErrorException e) {
			log.error("Error getting dataset " + e.getMessage());
			return null;
		}
	}

	@Override
	public OpenDataPackageDTO getDTOFromDataset(OpenDataPackage dataset, String organization, String license) {
		final OpenDataPackageDTO datasetDTO = new OpenDataPackageDTO();
		datasetDTO.setId(dataset.getId());
		datasetDTO.setIsPublic(!dataset.getIsPrivate());
		datasetDTO.setTitle(dataset.getTitle());
		datasetDTO.setDescription(dataset.getNotes());

		datasetDTO.setOrganization(organization);
		datasetDTO.setLicense(license);

		final List<String> tags = new ArrayList<>();
		final List<OpenDataTag> tagsList = dataset.getTags();
		for (int i = 0; i < tagsList.size(); i++) {
			tags.add(tagsList.get(i).getName());
		}
		datasetDTO.setTags(tags);
		return datasetDTO;
	}

	@Override
	public boolean getModifyPermissions(List<OpenDataOrganization> organizationsFromUser, String orgId) {
		boolean modifyPermissions = false;
		final Optional<OpenDataOrganization> foundOrg = organizationsFromUser.stream()
				.filter(elem -> elem.getId().equals(orgId)).findFirst();
		if (foundOrg.isPresent()) {
			final String role = foundOrg.get().getCapacity();
			if (role.equals("admin") || role.equals("editor")) {
				modifyPermissions = true;
			}
		}
		return modifyPermissions;
	}

	@Override
	public List<DatasetResource> getConfigResources(List<OpenDataResource> resources) {
		final List<String> resourceIds = new ArrayList<>();
		List<DatasetResource> configResources = new ArrayList<>();

		for (final OpenDataResource res : resources) {
			resourceIds.add(res.getId());
		}
		if (!resourceIds.isEmpty()) {
			configResources = resourceRepository.findResourcesByIdsList(resourceIds);
		}

		return configResources;
	}

	@Override
	public DatasetResource getConfigResource(OpenDataResource resource) {
		return resourceRepository.findResourceById(resource.getId());
	}

	@Override
	public OpenDataPackage updateDataset(OpenDataPackageDTO datasetDTO, String userToken) {
		final OpenDataPackage dataset = new OpenDataPackage();
		dataset.setId(datasetDTO.getId());
		dataset.setTitle(datasetDTO.getTitle());
		dataset.setName(datasetDTO.getName());
		dataset.setIsPrivate(!datasetDTO.getIsPublic());
		dataset.setOwner_org(datasetDTO.getOrganization());
		dataset.setLicense_id(datasetDTO.getLicense());

		if (datasetDTO.getDescription() != null) {
			dataset.setNotes(datasetDTO.getDescription());
		} else {
			dataset.setNotes("");
		}

		final List<OpenDataTag> newTags = new ArrayList<>();
		for (final String tag : datasetDTO.getTags()) {
			final OpenDataTag newTag = new OpenDataTag();
			newTag.setName(tag);
			newTags.add(newTag);
		}
		dataset.setTags(newTags);

		final PackageShowResponse responsePackage = (PackageShowResponse) api
				.postOperation("package_patch", userToken, dataset, PackageShowResponse.class).getBody();
		if (responsePackage != null && responsePackage.getSuccess()) {

			final String datasetId = responsePackage.getResult().getId();

			final ODTypology newtypology = typologyService.getTypologyById(datasetDTO.getTypology());
			final ODTypologyDataset oldTypologyDataset = typologyDatasetService.getTypologyByDatasetId(datasetId);
			ODTypology oldtypology = new ODTypology();
			if (oldTypologyDataset != null) {
				oldtypology = typologyService.getTypologyById(oldTypologyDataset.getTypologyId());
				if (newtypology == null || !newtypology.getId().equals(oldtypology.getId())) {
					typologyDatasetService.deleteTypologyDatasetById(oldTypologyDataset.getId());
				}
			}
			if (newtypology != null && !newtypology.getId().equals(oldtypology.getId())) {
				final ODTypologyDataset typologyDataset = new ODTypologyDataset();
				typologyDataset.setDatasetId(datasetId);
				typologyDataset.setTypologyId(newtypology.getId());
				typologyDatasetService.createNewTypologyDataset(typologyDataset);
			}

			final List<String> newfilesList = datasetDTO.getFiles();
			final List<String> oldFilesList = getOldFileList(datasetId);

			if (oldFilesList != null || !oldFilesList.isEmpty()) {
				binaryFileDatasetService.deleteBinaryFilesDatasetByDataset(datasetId);
			}
			for (final String idFile : newfilesList) {
				final ODBinaryFilesDataset binaryFileDataset = new ODBinaryFilesDataset();
				binaryFileDataset.setDatasetId(datasetId);
				binaryFileDataset.setFilesId(idFile);
				binaryFileDatasetService.createNewBinaryFiles(binaryFileDataset);
			}

			return responsePackage.getResult();
		} else {
			return null;
		}

	}

	private List<String> getOldFileList(String datasetId) {
		final List<String> returnList = new ArrayList<String>();
		final List<ODBinaryFilesDataset> oldBinaryFileDataset = binaryFileDatasetService
				.getBinaryFilesByDatasetId(datasetId);
		for (final ODBinaryFilesDataset file : oldBinaryFileDataset) {
			returnList.add(file.getFilesId());
		}
		return returnList;
	}

	@Override
	public void deleteDataset(String userToken, String id) {
		final OpenDataPackage dataset = new OpenDataPackage();
		dataset.setId(id);
		try {
			api.postOperation("package_delete", userToken, dataset, PackageShowResponse.class);
			// purge para borrado físico
			final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("id", id);
			api.postOperation("dataset_purge", userToken, map, String.class);

			typologyDatasetService.deleteTypologyDatasetByDatasetId(id);
			binaryFileDatasetService.deleteBinaryFilesDatasetByDataset(id);
		} catch (final HttpClientErrorException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getDatasetId(String identification) {
		String str = identification.replaceAll("\\s+", "-").toLowerCase();
		str = Normalizer.normalize(str, Normalizer.Form.NFKD);
		return str.replaceAll("[^a-z,^A-Z,^0-9,^-]", "");
	}

	@Override
	public String getLicenseIdByLicenseTitle(String licenseTitle) {
		final OpenDataLicense license = getLicensesList().stream().filter(l -> licenseTitle.equals(l.getTitle()))
				.findAny().orElse(null);
		if (license == null) {
			throw new OpenDataServiceException("License does not exist");
		} else {
			return license.getId();
		}
	}

}
