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
package com.minsait.onesait.platform.persistence.external.virtual;

import java.util.List;

import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;

public interface VirtualOntologyDBRepository extends BasicOpsDBRepository {

	
	public List<String> getTables(String datasourceName);

	/**
	 * Returns the first element of the table. Used to generate document schemas.
	 * @param datasource
	 * @param query
	 * @return List<String>
	 */
	public List<String> getInstanceFromTable(String datasource, String query);

	public String executeQuery(String ontology, String query);

	public List<String> getTableMetadata(String datasource, String collection);

}
