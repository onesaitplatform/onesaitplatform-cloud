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
package com.minsait.onesait.platform.config.services.flow.engine;

import java.util.List;
import java.util.UUID;

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
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.FlowNode;
import com.minsait.onesait.platform.config.model.FlowNode.MessageType;
import com.minsait.onesait.platform.config.model.NotificationEntity;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.services.flow.FlowService;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.config.services.flownode.FlowNodeService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.user.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class FlowNodeServiceIntegrationTest {

	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private FlowService flowService;

	@Autowired
	private FlowNodeService nodeService;

	@Autowired
	private UserService userService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyRepository ontologyRepository;

	private String domainIdentification;
	private String ontologyId;

	@Before
	public void setUp() {
		// Create one domain, flow and notificator node
		final String temp = UUID.randomUUID().toString().substring(0, 30);
		ontologyId = "OntTest_" + temp;
		domainIdentification = "DomainTest_" + temp;
		final User user = userService.getUser("developer");

		final Ontology ontology = new Ontology();
		ontology.setJsonSchema("{}");
		ontology.setDescription("Ontology for testing purposes.");
		ontology.setIdentification(ontologyId);
		ontology.setActive(true);
		ontology.setRtdbClean(false);
		ontology.setRtdbToHdb(false);
		ontology.setPublic(true);
		ontology.setUser(user);
		ontologyService.createOntology(ontology, null);

		final FlowDomain domain = domainService.createFlowDomain(domainIdentification, user);

		final Flow flow = new Flow();
		flow.setActive(true);
		flow.setIdentification("Test Flow 1" + temp);
		flow.setFlowDomain(domain);
		flow.setNodeRedFlowId("nodeRedFlowId");

		flowService.createFlow(flow, domain);

		// Create Node with properties

		final FlowNode node = new FlowNode();
		node.setFlow(flow);
		node.setNodeRedNodeId("nodeRedNodeId");
		node.setIdentification("nodeIdentification" + temp);
		node.setFlowNodeType(FlowNode.Type.HTTP_NOTIFIER);
		node.setMessageType(MessageType.INSERT);
		node.setOntology(ontologyService.getOntologyByIdentification(ontologyId, user.getUserId()));
		node.setPartialUrl("/notificationPointTest");
		nodeService.createFlowNode(node, flow);
	}

	@Test
	@Transactional
	public void given_SomeNotificationEntities_When_ItIsSearchedByOntologyIdAndType_Then_TheCorrectNotificationEntitiesAreReturned() {
		final List<NotificationEntity> notificationEntities = nodeService.getNotificationsByOntologyAndMessageType(ontologyId,
				"INSERT");
		Assert.assertTrue(notificationEntities != null && !notificationEntities.isEmpty());
	}

	@After
	public void cleanUp() {
		domainService.deleteFlowdomain(domainIdentification);
		ontologyRepository.delete(ontologyService.getOntologyByIdentification(ontologyId, "developer"));
	}
}
