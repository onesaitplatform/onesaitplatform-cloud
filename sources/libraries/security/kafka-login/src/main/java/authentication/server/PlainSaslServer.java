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
package authentication.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

import org.apache.kafka.common.errors.SaslAuthenticationException;

import authentication.PlainAuthenticateCallback;

public class PlainSaslServer implements SaslServer {

	private static final Logger log = Logger.getLogger(PlainSaslServer.class.getName());

	public static final String PLAIN_MECHANISM = "PLAIN";
	public static final String INCOMPLETEEXCHANGE_MSG = "Authentication exchange has not completed";

	private final CallbackHandler callbackHandler;
	private boolean complete;
	private String authorizationId;

	public PlainSaslServer(PlainServerCallbackHandler callbackHandler) {
		this.callbackHandler = callbackHandler;
	}

	/**
	 * @throws SaslAuthenticationException if username/password combination is
	 *                                     invalid or if the requested authorization
	 *                                     id is not the same as username.
	 *                                     <p>
	 *                                     <b>Note:</b> This method may throw
	 *                                     {@link SaslAuthenticationException} to
	 *                                     provide custom error messages to clients.
	 *                                     But care should be taken to avoid
	 *                                     including any information in the
	 *                                     exception message that should not be
	 *                                     leaked to unauthenticated clients. It may
	 *                                     be safer to throw {@link SaslException}
	 *                                     in some cases so that a standard error
	 *                                     message is returned to clients.
	 *                                     </p>
	 */
	@Override
	public byte[] evaluateResponse(byte[] response) throws SaslException {
		/*
		 * Message format (from https://tools.ietf.org/html/rfc4616):
		 *
		 * message = [authzid] UTF8NUL authcid UTF8NUL passwd authcid = 1*SAFE ; MUST
		 * accept up to 255 octets authzid = 1*SAFE ; MUST accept up to 255 octets
		 * passwd = 1*SAFE ; MUST accept up to 255 octets UTF8NUL = %x00 ; UTF-8 encoded
		 * NUL character
		 *
		 * SAFE = UTF1 / UTF2 / UTF3 / UTF4 ;; any UTF-8 encoded Unicode character
		 * except NUL
		 */

		String[] tokens;
		try {
			tokens = new String(response, StandardCharsets.UTF_8).split("\u0000");
		} catch (Exception e) {
			throw new SaslException("UTF-8 encoding not supported", e);
		}
		if (tokens.length != 3)
			throw new SaslException("Invalid SASL/PLAIN response: expected 3 tokens, got " + tokens.length);
		String authorizationIdFromClient = tokens[0];
		String username = tokens[1];
		String password = tokens[2];

		if (username.isEmpty()) {
			throw new SaslException("Authentication failed: username not specified");
		}
		if (password.isEmpty()) {
			throw new SaslException("Authentication failed: password not specified");
		}

		NameCallback nameCallback = new NameCallback("username", username);
		PlainAuthenticateCallback authenticateCallback = new PlainAuthenticateCallback(password.toCharArray());
		try {
			callbackHandler.handle(new Callback[] { nameCallback, authenticateCallback });
		} catch (Exception e) {
			throw new SaslAuthenticationException("Authentication failed: credentials for user could not be verified",
					e);
		}
		if (!authenticateCallback.authenticated())
			throw new SaslAuthenticationException("Authentication failed: Invalid username or password.");
		if (!authorizationIdFromClient.isEmpty() && !authorizationIdFromClient.equals(username))
			throw new SaslAuthenticationException(
					"Authentication failed: Client requested an authorization id that is different from username");

		this.authorizationId = username;

		complete = true;
		return new byte[0];
	}

	@Override
	public String getAuthorizationID() {
		if (!complete)
			throw new IllegalStateException(INCOMPLETEEXCHANGE_MSG);
		return authorizationId;
	}

	@Override
	public String getMechanismName() {
		return PLAIN_MECHANISM;
	}

	@Override
	public Object getNegotiatedProperty(String propName) {
		if (!complete)
			throw new IllegalStateException(INCOMPLETEEXCHANGE_MSG);
		return null;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
		if (!complete)
			throw new IllegalStateException(INCOMPLETEEXCHANGE_MSG);
		return Arrays.copyOfRange(incoming, offset, offset + len);
	}

	@Override
	public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
		if (!complete)
			throw new IllegalStateException(INCOMPLETEEXCHANGE_MSG);
		return Arrays.copyOfRange(outgoing, offset, offset + len);
	}

	@Override
	public void dispose() throws SaslException {
		// no need to do anything
	}

	public static class PlainSaslServerFactory implements SaslServerFactory {

		@Override
		public SaslServer createSaslServer(String mechanism, String protocol, String serverName, Map<String, ?> props,
				CallbackHandler cbh) throws SaslException {

			log.info("PlainSaslServerFactory Initialize with props:" + props.toString());
			//String url = (String) props.get("url");

			if (!PLAIN_MECHANISM.equals(mechanism))
				throw new SaslException(
						String.format("Mechanism \'%s\' is not supported. Only PLAIN is supported.", mechanism));
			return new PlainSaslServer(new PlainServerCallbackHandler());
		}

		@Override
		public String[] getMechanismNames(Map<String, ?> props) {
			if (props == null)
				return new String[] { PLAIN_MECHANISM };
			String noPlainText = (String) props.get(Sasl.POLICY_NOPLAINTEXT);
			if ("true".equals(noPlainText))
				return new String[] {};
			else
				return new String[] { PLAIN_MECHANISM };
		}
	}

}
