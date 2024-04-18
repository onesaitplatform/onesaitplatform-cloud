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
package com.minsait.onesait.platform.config.services.gadgettemplate;

import java.util.List;

import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateExportDto;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateImportResponsetDto;

public interface GadgetTemplateService {

	public List<GadgetTemplate> findAllGadgetTemplates();

	public List<GadgetTemplate> findGadgetTemplateWithIdentificationAndDescription(String identification,
			String description, String user);

	public List<String> getAllIdentifications();

	public GadgetTemplate getGadgetTemplateById(String id);

	public GadgetTemplate getGadgetTemplateByIdentification(String identification, String userId);

	public boolean hasUserPermission(String id, String userId);

	public void updateGadgetTemplate(GadgetTemplateDTO gadgettemplate);

	public void createGadgetTemplate(GadgetTemplateDTO gadgettemplate);

	public void deleteGadgetTemplate(String id, String userId);

	public List<GadgetTemplate> getUserGadgetTemplate(String userId);

	public List<GadgetTemplate> getUserGadgetTemplate(String userId, String type);

	GadgetTemplate getGadgetTemplateByIdentification(String identification);

	public List<GadgetTemplateType> getTemplateTypes();

	public GadgetTemplateType getTemplateTypeById(String id);

	public GadgetTemplateExportDto exportGradgetTemplate(String identification, String userId);

	public List<GadgetTemplateExportDto> exportGradgetTemplateByUser(String userId);

	public List<GadgetTemplateImportResponsetDto> importGradgetTemplateByUser(String userId,
			List<GadgetTemplateExportDto> gadgetTemplates, Boolean override);

	public GadgetTemplateDTO getGadgetTemplateDTOById(String id);
	
	public GadgetTemplateDTO getGadgetTemplateDTOByIdentification(String identification);

    public String cloneGadgetTemplate(GadgetTemplate gadgetTemplate, String identification, User user);
}
