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
package com.minsait.onesait.platform.config.services.form;

import java.io.IOException;
import java.util.List;

import com.minsait.onesait.platform.config.model.Form;

public interface FormService {

	public void create(FormCreateDTO form, String userId);

	public Form getDBForm(String id);

	public FormDTO getForm(String id);

	public List<FormDTO> getForms(String userId);

	public void deleteForm(String code, String userId);

	public FormDTO updateForm(FormCreateDTO form, String id, String userId);

	public String generateFormFromEntity(String entity, String userId) throws IOException;

	void createModifyI18nResource(String idForm, String i18n, String userId);

	public void clone(String code, String newName, String userId);

}
