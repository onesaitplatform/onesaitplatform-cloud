/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.gadget;

import java.util.List;

import com.minsait.onesait.platform.config.dto.GadgetDatasourceForList;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDatasourceDTOForList;

public interface GadgetDatasourceService {

	public List<GadgetDatasource> findAllDatasources();

	public List<GadgetDatasourceForList> findGadgetDatasourceWithIdentificationAndDescription(String identification,
			String description, String user);

	public List<String> getAllIdentifications();

	public GadgetDatasource getGadgetDatasourceById(String id);

	public GadgetDatasource createGadgetDatasource(GadgetDatasource gadgetDatasource);

	public boolean gadgetDatasourceExists(GadgetDatasource gadgetDatasource);

	public void updateGadgetDatasource(GadgetDatasource gadgetDatasource);

	public void deleteGadgetDatasource(String gadgetDatasourceId, String userId);

	public boolean hasUserPermission(String id, String userId);

	public boolean hasUserViewPermission(String id, String userId);

	public boolean hasUserEditPermission(String id, String userId);

	public List<GadgetDatasource> getUserGadgetDatasources(String userId);

	public List<GadgetDatasourceDTOForList> getUserGadgetDatasourcesForList(String userId);

	public String getSampleQueryGadgetDatasourceById(String datasourceId, String ontology, String user);

	public GadgetDatasource getDatasourceByIdentification(String dsIdentification);
	
	public boolean isGroupDatasourceById(String id);
	
	public String getAccessType (String id, String userId);
	
	public String getElementsAssociated (String datasourceId);
	
	public String getOntologyFromDatasource(String datasource);

	public String getMaxValuesFromQuery(String query);

	public List<Object> getGadgetsUsingDatasource(String id);
}
