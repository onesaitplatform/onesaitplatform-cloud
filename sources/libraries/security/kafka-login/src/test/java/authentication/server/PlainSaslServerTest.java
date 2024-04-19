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
package authentication.server;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.config.types.Password;
import org.apache.kafka.common.errors.SaslAuthenticationException;
import org.apache.kafka.common.security.JaasContext;
import org.junit.Before;
import org.junit.Test;

import authentication.PlainLoginModule;

public class PlainSaslServerTest {
	private static final String USER_A = "admin";
	private static final String PASSWORD_A = "admin-secret";
	private static final String USER_B = "admin3";
	private static final String PASSWORD_B = "admin3-secret";

	private PlainSaslServer saslServer;

	@Before
	public void setUp() throws Exception {
		TestJaasConfig jaasConfig = new TestJaasConfig();
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("user_" + USER_A, PASSWORD_A);
		options.put("user_" + USER_B, PASSWORD_B);
		jaasConfig.addEntry("jaasContext", PlainLoginModule.class.getName(), options);

		//JaasContext jaasContext = new JaasContext("jaasContext", JaasContext.Type.SERVER, jaasConfig);
		JaasContext jaasContext = new JaasContext("jaasContext", JaasContext.Type.SERVER, jaasConfig,new Password("admin-secret"));
		PlainServerCallbackHandler callbackHandler = new PlainServerCallbackHandler();
		callbackHandler.configure(null, "PLAIN", jaasContext.configurationEntries());
		saslServer = new PlainSaslServer(callbackHandler);
	}

	@Test
	public void noAuthorizationIdSpecified() throws Exception {
		byte[] nextChallenge = saslServer.evaluateResponse(saslMessage("", USER_A, PASSWORD_A));
		assertEquals(0, nextChallenge.length);
	}

	@Test
	public void authorizatonIdEqualsAuthenticationId() throws Exception {
		byte[] nextChallenge = saslServer.evaluateResponse(saslMessage(USER_A, USER_A, PASSWORD_A));
		assertEquals(0, nextChallenge.length);
	}

	@Test(expected = SaslAuthenticationException.class)
	public void authorizatonIdNotEqualsAuthenticationId() throws Exception {
		saslServer.evaluateResponse(saslMessage(USER_B, USER_A, PASSWORD_A));
	}

	private byte[] saslMessage(String authorizationId, String userName, String password) {
		String nul = "\u0000";
		String message = String.format("%s%s%s%s%s", authorizationId, nul, userName, nul, password);
		return message.getBytes(StandardCharsets.UTF_8);
	}
}
