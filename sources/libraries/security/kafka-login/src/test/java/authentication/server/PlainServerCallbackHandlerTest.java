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
package authentication.server;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PlainServerCallbackHandlerTest {

	private PlainServerCallbackHandler plainServerCallbackHandler;
	
	@Before
	public void setUp() throws Exception {
		plainServerCallbackHandler = new PlainServerCallbackHandler();
		plainServerCallbackHandler.configure(null, null, null);
	}
	
	@Test
	public void getUserToken() throws Exception {
		String token = "";
		String deviceId = "";
		boolean authenticate = plainServerCallbackHandler.authenticate(deviceId, token.toCharArray());
		assertTrue(authenticate);
	}
	
}
