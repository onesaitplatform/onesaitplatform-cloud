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
package com.minsait.onesait.platform.config.services.gadget;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.gadget.dto.GadgetDTO;

public interface GadgetService {

	public List<Gadget> findAllGadgets();

	public List<Gadget> findGadgetWithIdentificationAndType(String identification, String type, String user);

	public List<String> getAllIdentifications();

	public Gadget getGadgetById(String userID, String gadgetId);

	public void createGadget(GadgetDTO gadget);

	public List<Gadget> getUserGadgetsByType(String userID, String type);

	public List<GadgetMeasure> getGadgetMeasuresByGadgetId(String userID, String gadgetId);

	public boolean hasUserPermission(String id, String userId);

	public void updateGadget(GadgetDTO gadget, String gadgetDatasourceIds, String jsonMeasures, User user);

	public void updateInstance(String id, String config);

	public Gadget createGadget(GadgetDTO gadget, String gadgetDatasourceIds, String jsonMeasures, User user);

	public void deleteGadget(String gadgetId, String userId) throws JsonProcessingException;

	public Boolean existGadgetWithIdentification(String identification);

	public Gadget createGadget(Gadget g, GadgetDatasource datasource, List<GadgetMeasure> gadgetMeasures,
			String category, String subcategory);

	public void updateGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> measures, String category,
			String subcategory);

	public void addMeasuresGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> measures, String category,
			String subcategory);

	public String getElementsAssociated(String gadgetId);

	public boolean hasUserViewPermission(String id, String userId);

	public Gadget getGadgetByIdentification(String userID, String gadgetIdentification);

	public List<String> getAllIdentificationsByUser(String userId);

	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description);

	public List<String> getGadgetTypes(String userId);

	ResponseEntity<byte[]> generateImg(String id, int waittime, int height, int width, boolean fullpage, String params,
			String oauthtoken);

	ResponseEntity<byte[]> generatePDF(String id, int waittime, int height, int width, String params,
			String oauthtoken);

	public String cloneGadget(Gadget gadget, String identification, User user);

}
