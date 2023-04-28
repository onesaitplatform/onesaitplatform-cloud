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

import java.util.List;

import com.minsait.onesait.platform.config.model.DatasetResource;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataLicense;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageDTO;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackageList;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;

public interface DatasetService {

	public OpenDataPackageList getDatasetsListByUser(String userToken, String query, String sort, Integer rows, Integer start);
	
	public List<OpenDataPackage> getDatasetsByUser(String userToken, String name, String tag);
	
	public List<OpenDataPackage> getDatasetsByUser(String userToken);

	public List<OpenDataPackage> getDatasetsByUserWithPermissions(String userToken,
			List<OpenDataOrganization> orgsFromUser);

	public List<OpenDataPackageDTO> getDTOFromDatasetList(List<OpenDataPackage> datasets,
			List<OpenDataOrganization> orgsFromUser);

	public boolean getCreatePermissions(List<OpenDataOrganization> organizationsFromUser);

	public List<OpenDataLicense> getLicensesList();

	public boolean existsDataset(OpenDataPackageDTO dataset, String userToken);

	public OpenDataPackage createDataset(OpenDataPackageDTO datasetDTO, String userToken);

	public OpenDataPackage getDatasetById(String userToken, String id);

	public OpenDataPackageDTO getDTOFromDataset(OpenDataPackage dataset, String organization, String license);

	public boolean getModifyPermissions(List<OpenDataOrganization> organizationsFromUser, String orgId);

	public List<DatasetResource> getConfigResources(List<OpenDataResource> resources);

	public DatasetResource getConfigResource(OpenDataResource resource);

	public OpenDataPackage updateDataset(OpenDataPackageDTO datasetDTO, String userToken);

	public void deleteDataset(String userToken, String id);
	
	public String getDatasetId(String identification);

	public String getLicenseIdByLicenseTitle(String licenseTitle);

}
