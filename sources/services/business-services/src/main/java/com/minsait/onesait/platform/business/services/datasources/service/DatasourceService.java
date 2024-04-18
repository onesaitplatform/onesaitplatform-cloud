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
package com.minsait.onesait.platform.business.services.datasources.service;

import com.minsait.onesait.platform.business.services.datasources.dto.InputMessage;
import com.minsait.onesait.platform.business.services.datasources.exception.DatasourceException;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;

public interface DatasourceService {

	public String solveDatasource(InputMessage im, Ontology ont, GadgetDatasource gd, String userId)
			throws DatasourceException, OntologyDataUnauthorizedException, GenericOPException;

	public GadgetDatasource getGadgetDatasourceFromIdentification(String gds, String userId);

	public Ontology getOntologyFromDatasource(GadgetDatasource gd, String userId);

	public String getDataById(String entityId, String oid, String userId);

	public String deleteDataById(String entityId, String oid, String userId);

	public String insertData(String entityId, String userId, String data);

	public String update(String entityId, String oid, String userId, String data);
}
