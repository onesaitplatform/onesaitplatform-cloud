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
package com.minsait.onesait.platform.business.services.crud;

import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;

public interface CrudService {

	public String processQuery(String query, String ontologyID, Type method, String body, String objectId,
			String userId);

	public String queryParams(SelectStatement selectStatement, String userId);

	public boolean useQuasar();

	public String getUniqueColumn(String ontology, boolean findById);

	public String findById(String ontologyID, String oid, String userId);

}
