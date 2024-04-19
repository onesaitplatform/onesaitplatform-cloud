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
package com.minsait.onesait.platform.iotbroker.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodySubscribeMessage;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
@Ignore
public class SubscribeProcessorTest {

	@Autowired
	MessageProcessorDelegate subscribeProcessor;

	@MockBean
	SecurityPluginManager securityPluginManager;

	// @Autowired
	// SuscriptionModelRepository repositoy;

	@MockBean
	RouterService routerService;

	SSAPMessage<SSAPBodySubscribeMessage> ssapSbuscription;
	IoTSession session;
	@MockBean
	DeviceManager deviceManager;

	// @MockBean
	// IotBrokerAuditableAspect iotBrokerAuditableAspect;

	private void auditMocks() {

	}

	private void securityMocks() {
		session = PojoGenerator.generateSession();
		when(deviceManager.registerActivity(any(), any(), any(), any())).thenReturn(true);

		when(securityPluginManager.getSession(anyString())).thenReturn(Optional.of(session));
		when(securityPluginManager.checkSessionKeyActive(any())).thenReturn(true);
		when(securityPluginManager.checkAuthorization(any(), any(), any())).thenReturn(true);
	}

	@Before
	public void setUp() throws IOException, Exception {
		// repositoy.deleteByOntologyName(Person.class.getSimpleName());
		securityMocks();
		auditMocks();

	}

	@Test
	public void given_OneSubsctiptionProcessorWhenSubscriptionArrivesThenSubscriptionIsStoredAndReturned()
			throws Exception {

//		final OperationResultModel value = RouterServiceGenerator.generateSubscriptionOk(UUID.randomUUID().toString());
//		when(routerSuscriptionService.suscribe(any())).thenReturn(value);
//
//		final SSAPMessage<SSAPBodyReturnMessage> response = subscribeProcessor.process(ssapSbuscription,
//				PojoGenerator.generateGatewayInfo());
//		Assert.assertNotNull(response);
//		Assert.assertEquals(SSAPMessageDirection.RESPONSE, response.getDirection());
//		Assert.assertEquals(SSAPMessageTypes.SUBSCRIBE, response.getMessageType());
//		Assert.assertNotNull(response.getBody());
//		Assert.assertNotNull(response.getBody().getData());
//		final JsonNode data = response.getBody().getData();
//		Assert.assertNotNull(data.at("/subscriptionId").asText());

	}

}
