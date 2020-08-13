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
package com.minsait.onesait.platform.iotbroker.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
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
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPErrorCode;
import com.minsait.onesait.platform.iotbroker.common.exception.AuthenticationException;
import com.minsait.onesait.platform.iotbroker.common.exception.SSAPProcessorException;
import com.minsait.onesait.platform.iotbroker.mock.pojo.PojoGenerator;
import com.minsait.onesait.platform.iotbroker.mock.ssap.SSAPMessageGenerator;
import com.minsait.onesait.platform.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
@Ignore
@Slf4j
public class JoinProcessorWithTokenTest {

	@Autowired
	MessageProcessorDelegate processor;

	@MockBean
	SecurityPluginManager securityPluginManager;

	SSAPMessage<SSAPBodyJoinMessage> ssapJoin;

	@MockBean
	DeviceManager deviceManager;

	IoTSession session = new IoTSession();

	// @MockBean
	// IotBrokerAuditableAspect iotBrokerAuditableAspect;

	private void auditMocks() {
	}

	@Before
	public void setup() throws SSAPProcessorException {
		session = new IoTSession();
		session.setClientPlatform("someDevice");
		ssapJoin = SSAPMessageGenerator.generateJoinMessageWithToken();
		when(deviceManager.registerActivity(any(), any(), any(), any())).thenReturn(true);
		when(securityPluginManager.getSession(any())).thenReturn(Optional.of(session));
		auditMocks();
	}

	@Test
	public void given_OneJoinProcessor_When_OneValidSessionIsUsed_Then_TheResponseIndicatesTheOperationWasPerformed()
			throws AuthenticationException {
		final String assignedSessionKey = UUID.randomUUID().toString();
		session.setUserID("valid_user_id");
		session.setSessionKey(assignedSessionKey);
		when(securityPluginManager.authenticate(anyString(), anyString(), anyString(), anyString()))
				.thenReturn(Optional.of(session));
		ssapJoin.getBody().setToken(UUID.randomUUID().toString());
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = processor.process(ssapJoin,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertEquals(assignedSessionKey, responseMessage.getSessionKey());
	}

	@Test
	public void given_OneJoinProcessor_When_ItUsesAnInvalidToken_Then_TheResponseIndicatesAuthenticationError()
			throws AuthenticationException {
		when(securityPluginManager.authenticate(anyString(), anyString(), anyString(), anyString()))
				.thenReturn(Optional.empty());
		ssapJoin.getBody().setToken(UUID.randomUUID().toString());
		final SSAPMessage<SSAPBodyReturnMessage> responseMessage = processor.process(ssapJoin,
				PojoGenerator.generateGatewayInfo());

		Assert.assertNotNull(responseMessage);
		Assert.assertNotNull(responseMessage.getBody());
		Assert.assertEquals(SSAPErrorCode.AUTENTICATION, responseMessage.getBody().getErrorCode());
	}

	@Test
	public void given_OneJoinProcessor_When_AnEmptyOrNullTokenIsUsed_Then_TheResponseIndicatesProcessorError() {

		// Token is an Empty string
		{
			ssapJoin.getBody().setToken("");
			final SSAPMessage<SSAPBodyReturnMessage> responseMessage = processor.process(ssapJoin,
					PojoGenerator.generateGatewayInfo());

			Assert.assertNotNull(responseMessage);
			Assert.assertNotNull(responseMessage.getBody());
			Assert.assertEquals(SSAPErrorCode.PROCESSOR, responseMessage.getBody().getErrorCode());

		}

		// Token is NULL
		{
			ssapJoin.getBody().setToken(null);
			final SSAPMessage<SSAPBodyReturnMessage> responseMessage = processor.process(ssapJoin,
					PojoGenerator.generateGatewayInfo());

			Assert.assertNotNull(responseMessage);
			Assert.assertNotNull(responseMessage.getBody());
			Assert.assertEquals(SSAPErrorCode.PROCESSOR, responseMessage.getBody().getErrorCode());

		}

	}

}
