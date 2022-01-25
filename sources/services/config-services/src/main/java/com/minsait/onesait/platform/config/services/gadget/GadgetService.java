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
package com.minsait.onesait.platform.config.services.gadget;

import java.util.List;

import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.Gadget;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.GadgetMeasure;

public interface GadgetService {

	public List<Gadget> findAllGadgets();

	public List<Gadget> findGadgetWithIdentificationAndType(String identification, String type, String user);

	public List<String> getAllIdentifications();

	public Gadget getGadgetById(String userID, String gadgetId);

	public void createGadget(Gadget gadget);

	public List<Gadget> getUserGadgetsByType(String userID, String type);

	public List<GadgetMeasure> getGadgetMeasuresByGadgetId(String userID, String gadgetId);

	public boolean hasUserPermission(String id, String userId);

	public void updateGadget(Gadget gadget, String gadgetDatasourceIds, String jsonMeasures);

	public Gadget createGadget(Gadget gadget, String gadgetDatasourceIds, String jsonMeasures);

	public void deleteGadget(String gadgetId, String userId);

	public Boolean existGadgetWithIdentification(String identification);

	public Gadget createGadget(Gadget g, GadgetDatasource datasource, List<GadgetMeasure> gadgetMeasures);

	public void updateGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> measures);

	public void addMeasuresGadget(Gadget gadget, String datasourceId, List<GadgetMeasure> measures);

	public String getElementsAssociated(String gadgetId);

	public boolean hasUserViewPermission(String id, String userId);

	public Gadget getGadgetByIdentification(String userID, String gadgetIdentification);

	public List<String> getGadgetTypes();

	List<String> getAllIdentificationsByUser(String userId);

	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification, String description);

}
