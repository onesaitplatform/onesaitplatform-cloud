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
package com.minsait.onesait.platform.persistence.mongodb.audit.aop;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.CalendarUtil;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.audit.bean.OPEventFactory;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;

@Service
public class QuasarAuditProcessor {

	@Autowired
	private OntologyRepository ontologyRepository;

	private static final String MAP_REDUCE_DETECTED = "Detected mapReduce operation";

	public QuasarAuditEvent getWarningEvent(String query, String result, String collection) {
		final Date today = new Date();
		final Ontology ontology = ontologyRepository.findByIdentification(collection);
		return QuasarAuditEvent.builder().id(UUID.randomUUID().toString()).timeStamp(today.getTime())
				.formatedTimeStamp(CalendarUtil.builder().build().convert(today)).message(MAP_REDUCE_DETECTED)
				.ontology(ontology.getIdentification()).user(AuditConst.ANONYMOUS_USER).module(Module.SQLENGINE)
				.type(EventType.QUERY).operationType(OperationType.QUERY.name())
				.resultOperation(ResultOperationType.WARNING).query(query).result(result).build();
	}

	public OPAuditError getErrorEvent(String query, String collection, Exception ex) {

		final Ontology ontology = ontologyRepository.findByIdentification(collection);
		final String message = "Exception detected while executing query: " + query + " ; Ontology:"
				+ ontology.getIdentification();

		return OPEventFactory.builder().build().createAuditEventError(message, Module.SQLENGINE, ex);

	}

}
