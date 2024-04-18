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
package com.minsait.onesait.platform.business.services.gadget;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import net.sf.jsqlparser.JSQLParserException;

public interface GadgetDatasourceBusinessService {

	public String getSampleGadgetDatasourceById(String datasourceId, String user) throws GadgetDatasourceBusinessServiceException, DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException, JSQLParserException;
	
	public String getSampleGadgetDatasourceById(String datasourceId, String user, int limit, boolean forFilter) throws GadgetDatasourceBusinessServiceException, DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException, JSQLParserException;
	
	public List<GadgetDatasourceFieldDTO> getFieldsGadgetDatasourceById(String datasourceId, String user, boolean forFilter) throws GadgetDatasourceBusinessServiceException, DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException, JsonParseException, JsonMappingException, IOException, JSQLParserException;
	
}
