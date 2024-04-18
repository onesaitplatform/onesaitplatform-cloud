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
package com.minsait.onesait.platform.business.services.ontology;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.minsait.onesait.platform.commons.kafka.KafkaService;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyOpsDBRepository;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicService;

//@RunWith(MockitoJUnitRunner.StrictStubs.class)
@RunWith(MockitoJUnitRunner.class)
public class OntologyBusinessServiceImplTest {

	@InjectMocks
	OntologyBusinessServiceImpl service;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private OntologyLogicService ontologyLogicService;

	@Mock
	private KafkaService kafkaService;

	@Mock
	private VirtualOntologyOpsDBRepository virtualRepo;

	@Mock
	private UserService userService;

	@Test()
	public void given_OntologyDataAndUser_When_OntologyIdentificationIsNotValid_Then_TheAnExceptionIsThrown() {
		when(ontologyService.isIdValid(any())).thenReturn(false);
		final Ontology ontology = new Ontology();
		final String userId = "me";
		try {
			service.createOntology(ontology, userId, null);
		} catch (final OntologyBusinessServiceException e) {
			assertTrue("An invalid identifier should cause an exception",
					e.getError().equals(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologySchemaIsNotValid_Then_TheAnExceptionIsThrown() {
		final Ontology ontology = new Ontology();
		ontology.setJsonSchema("{bad json schema}");
		final String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		doThrow(new OntologyDataJsonProblemException()).when(ontologyService).checkOntologySchema(any());

		try {
			service.createOntology(ontology, userId, null);
		} catch (final OntologyBusinessServiceException e) {
			assertTrue("An invalid json schema should cause an exception",
					e.getError().equals(OntologyBusinessServiceException.Error.NO_VALID_SCHEMA));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithExternalTableFailsCreationgTable_Then_TheAnExceptionIsThrown() {
		final Ontology ontology = new Ontology();
		ontology.setRtdbDatasource(RtdbDatasource.VIRTUAL);
		ontology.setIdentification("test");
		final OntologyConfiguration config = new OntologyConfiguration();
		config.setAllowsCreateTable(true);
		final String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		doNothing().when(ontologyLogicService).createOntology(any(), any());

		try {
			service.createOntology(ontology, userId, config);
		} catch (final OntologyBusinessServiceException e) {
			assertTrue("An error creating the external table should cause an exception",
					e.getError().equals(OntologyBusinessServiceException.Error.EXTERNAL_TABLE_CREATION_ERROR));
		}
	}

	@Test()
	public void given_OntologyDataAndUser_When_OntologyWithExternalTableFailsCreatingTheOntology_Then_TheAnExceptionIsThrown() {
		final Ontology ontology = new Ontology();
		ontology.setRtdbDatasource(RtdbDatasource.VIRTUAL);
		ontology.setIdentification("test");
		final OntologyConfiguration config = new OntologyConfiguration();
		config.setAllowsCreateTable(true);
		final String userId = "me";

		when(ontologyService.isIdValid(any())).thenReturn(true);
		doThrow(new OntologyServiceException("Error creating ontology")).when(ontologyLogicService)
				.createOntology(any(), any());
		doThrow(new RuntimeException()).when(ontologyLogicService).removeOntology(any());
		try {
			service.createOntology(ontology, userId, config);
		} catch (final OntologyBusinessServiceException e) {
			assertTrue("An exception should be thrown when it is not possible to undo the external table creation",
					e.getError().equals(OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR));
		}
	}

}
