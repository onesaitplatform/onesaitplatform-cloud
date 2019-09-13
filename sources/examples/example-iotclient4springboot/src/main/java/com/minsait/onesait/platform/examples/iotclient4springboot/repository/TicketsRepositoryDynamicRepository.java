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
package com.minsait.onesait.platform.examples.iotclient4springboot.repository;

import java.util.List;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicRepository;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerParam;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.TicketOntology;

@IoTBrokerRepository
public interface TicketsRepositoryDynamicRepository {

	@IoTBrokerQuery
	List<TicketOntology> getTicketByDynamicQuery(@IoTBrokerDynamicRepository String ontology,
			@IoTBrokerDynamicQuery String query);

	@IoTBrokerQuery
	List<TicketOntology> getTicketByDynamicQuery(@IoTBrokerDynamicRepository String ontology,
			@IoTBrokerDynamicQuery String query, @IoTBrokerParam("$user") String user);

}
