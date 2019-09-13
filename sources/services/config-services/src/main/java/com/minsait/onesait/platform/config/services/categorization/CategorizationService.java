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
package com.minsait.onesait.platform.config.services.categorization;

import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.User;

public interface CategorizationService {
	
	public void deactivate(String id);
	
	public void setActive(String id, User user);
	
	public void createCategorization(String name, String json, User user);
	
	public void editCategorization(String id, String json);
	
	public void addAuthorization(Categorization categorization, User user, String accessType);
	
	public boolean hasUserPermission (User user, Categorization categorization);

}
