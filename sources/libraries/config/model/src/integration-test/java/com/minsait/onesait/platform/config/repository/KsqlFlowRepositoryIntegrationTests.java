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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.KsqlResource.KsqlResourceType;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class KsqlFlowRepositoryIntegrationTests {

	@Autowired
	private KsqlFlowRepository ksqlFlowRepository;

	@Autowired
	private KsqlResourceRepository ksqlResourceRepository;

	@Autowired
	private KsqlRelationRepository ksqlRelationRepository;

	@Autowired
	private UserRepository userRepository;

	@Before
	public void init() {

		final KsqlFlow ksqlFlow = new KsqlFlow();
		ksqlFlow.setIdentification("ksqlFlowTest000");
		ksqlFlow.setDescription("Ksql Flow for testing purposes.");
		ksqlFlow.setUser(userRepository.findByUserId("developer"));
		ksqlFlow.setJsonFlow("{}");
		ksqlFlowRepository.save(ksqlFlow);

		final KsqlResource ksqlResource = new KsqlResource();

		ksqlResource.setDescription("Ksql Test Resource 1");
		ksqlResource.setIdentification("ontologyInit");
		ksqlResource.setOntology(null);
		ksqlResource.setResourceType(FlowResourceType.ORIGIN);
		String statemetnText = "create stream ontologyInit (ontology varchar) WITH (kafka_topic='jsonTest',value_Format='JSON');";
		ksqlResource.setStatementText(statemetnText);
		ksqlResource.setIdentification("ontologyInit");
		ksqlResource.setKafkaTopic("jsonTest");
		ksqlResource.setKsqlType(KsqlResourceType.STREAM);

		final KsqlRelation ksqlRelation = new KsqlRelation();
		ksqlRelation.setKsqlFlow(ksqlFlow);

		ksqlRelation.setKsqlResource(ksqlResource);
		ksqlFlow.addResourceRelation(ksqlRelation);

		ksqlResourceRepository.save(ksqlResource);
		ksqlRelationRepository.save(ksqlRelation);
		ksqlFlowRepository.save(ksqlFlow);

		final KsqlResource ksqlResource2 = new KsqlResource();

		ksqlResource2.setDescription("Ksql Test Resource 2");
		ksqlResource2.setIdentification("ontologyFields");
		ksqlResource2.setOntology(null);
		ksqlResource2.setResourceType(FlowResourceType.PROCESS);
		statemetnText = "create stream ontologyFields as select EXTRACTJSONFIELD(ontology, '$.viewtime') as viewtime, EXTRACTJSONFIELD(ontology, '$.userid') as  userid, EXTRACTJSONFIELD(ontology, '$.pageid') as pageid from ontologyInit;";
		ksqlResource2.setStatementText(statemetnText);
		ksqlResource2.setKafkaTopic("ONTOLOGYFIELDS");
		ksqlResource2.setKsqlType(KsqlResourceType.STREAM);

		final KsqlRelation ksqlRelation2 = new KsqlRelation();
		ksqlRelation2.setKsqlFlow(ksqlFlow);
		ksqlRelation2.setKsqlResource(ksqlResource2);
		ksqlFlow.addResourceRelation(ksqlRelation2);
		ksqlRelation2.addPredecessor(ksqlRelation);
		ksqlRelation.addSucessor(ksqlRelation);

		ksqlResourceRepository.save(ksqlResource2);
		ksqlRelationRepository.save(ksqlRelation2);
		ksqlRelationRepository.save(ksqlRelation);
		ksqlFlowRepository.save(ksqlFlow);

	}

	@Test
	public void given_SomeKsqlFlowExists_When_IsSearchedById_Then_TheCorrectObjectIsObtained() {
		final List<KsqlFlow> ksqlFlowList = ksqlFlowRepository.findByIdentification("ksqlFlowTest000");
		// ksqlFlowList.get(0).getResourcesRelations()
		// .forEach(ksqlRelation ->
		// log.info(ksqlRelation.getKsqlResource().getIdentification()));
		Assert.assertTrue(ksqlFlowList.size() > 0);
	}

	@After
	public void cleanUp() {

		final KsqlFlow flow = ksqlFlowRepository.findByIdentification("ksqlFlowTest000").get(0);

		final List<KsqlRelation> elements = ksqlRelationRepository.findByKsqlFlow(flow);

		for (final KsqlRelation element : elements) {
			element.getPredecessors().forEach(predecessor -> {
				predecessor.removeSucessor(element);
				ksqlRelationRepository.save(predecessor);
			});
			element.getSuccessors().forEach(successor -> {
				successor.removePredecessor(element);
				ksqlRelationRepository.save(successor);
			});

			final String resourceIdentification = element.getKsqlResource().getIdentification();
			ksqlRelationRepository.deleteByKsqlFlowIdentificationAndKsqlResourceIdentification(flow.getIdentification(),
					resourceIdentification);
			ksqlResourceRepository.deleteByIdentification(resourceIdentification);
		}

		ksqlFlowRepository.deleteByIdentificationAndUserUserId("ksqlFlowTest000", "developer");
	}
}
