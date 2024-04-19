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
package com.minsait.onesait.platform.config.services.ontologyrest.operation;

import java.util.List;

import com.minsait.onesait.platform.config.model.OntologyRest;
import com.minsait.onesait.platform.config.model.OntologyRestOperation;
import com.minsait.onesait.platform.config.model.OntologyRestOperationParam;

public interface OntologyRestOperationService {

	public OntologyRestOperation getOntologyRestOperationByName(OntologyRest ontologyRest, String name);

	public List<OntologyRestOperation> getAllOperationsFromOntologyRest(OntologyRest ontologyRest);

	public List<OntologyRestOperationParam> getOntologyRestOperationParams(OntologyRestOperation ontologyRestOperation);
}
