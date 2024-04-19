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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import authentication.AuthenticateCallbackHandler;
import authentication.PlainAuthenticateCallback;
import authentication.PlainLoginModule;

public class PlainServerCallbackHandler implements AuthenticateCallbackHandler {

	private static final Logger log = Logger.getLogger(PlainServerCallbackHandler.class.getName());

	private static final String USER_AGENT = "Mozilla/5.0";

	private static final String BASE_JOIN = "http://iotbrokerservice:19000/iot-broker/rest/";
	private static final String DEFAULT_VERTICAL = "onesaitplatform";
	private static final String DEFAULT_TENANT = "development_onesaitplatform";
	private static final String ZOOKEPER = "zookeeper";
	private static final String SCHEMA_REGISTRY = "schema-registry";

	private ObjectMapper objectMapper;

	@Override
	public void configure(Map<String, ?> configs, String mechanism, List<AppConfigurationEntry> jaasConfigEntries) {
		log.info("PlainServerCallbackHandler Initialize");

		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		String username = null;
		for (Callback callback : callbacks) {
			if (callback instanceof NameCallback)
				username = ((NameCallback) callback).getDefaultName();
			else if (callback instanceof PlainAuthenticateCallback) {
				PlainAuthenticateCallback plainCallback = (PlainAuthenticateCallback) callback;
				boolean authenticated = authenticate(username, plainCallback.password());
				plainCallback.authenticated(authenticated);
			} else {
				throw new UnsupportedCallbackException(callback);
			}
		}
	}

	protected String login() {
		return "";
	}

	boolean authenticate(String username, char[] pass) throws IOException {
		if (username == null) {
			return false;
		} else {
			boolean ret = false;

			log.info("GET OPERATION " + username + ":" + new String(pass));

			String sessionKey = "";
			if (username.equals("admin")) {
				String expectedPass = "admin-secret";
				ret = expectedPass.equals(new String(pass));
			} else if (username.equals(ZOOKEPER)) {
				String expectedPass = ZOOKEPER;
				ret = expectedPass.equals(new String(pass));
			} else if (username.equals(SCHEMA_REGISTRY)) {
				String expectedPass = SCHEMA_REGISTRY;
				ret = expectedPass.equals(new String(pass));
			} else {
				try {
					sessionKey = join(username, new String(pass));
					if (sessionKey != null && !"".equals(sessionKey)) {
						ret = true;
					}
				} catch (Exception e) {
					log.warn(e.getMessage());
				}
			}
			log.info("RETURN GET OPERATION " + username + ":" + new String(pass) + " RET " + ret);

			return ret;
		}
	}

	private String join(String deviceId, String token) throws IOException {

		String joinUrl = "client/kafka/join/";
		String vertical = getDefaultVertical();
		String tenant = getDefaultTenant();
		String url = getBaseURL() + joinUrl;

		log.info("Calling URL:" + url);

		String charset = "UTF-8";

		// CHECK Multitenancy
		String[] kafkaClientParams = deviceId.split("-");

		if (kafkaClientParams.length == 3) {
			deviceId = kafkaClientParams[0]; 
			vertical = kafkaClientParams[1];
			tenant = kafkaClientParams[2];
		} else if (kafkaClientParams.length != 1) {
			return null;
		}

		StringBuilder query = new StringBuilder();
		query.append(String.format("token=%s", URLEncoder.encode(token, charset))).append("&")
				.append(String.format("clientPlatform=%s", URLEncoder.encode(deviceId, charset))).append("&")
				.append(String.format("clientPlatformId=%s", URLEncoder.encode(deviceId, charset))).append("&")
				.append(String.format("vertical=%s", URLEncoder.encode(vertical, charset))).append("&")
				.append(String.format("tenant=%s", URLEncoder.encode(tenant, charset)));

		URL obj = new URL(url + "?" + query.toString());
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept", "application/json");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);

		con.connect();

		String sessionKey = "";
		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			log.info("\nSending 'GET' request to URL : " + url);
			log.info("\nResponse Code OK: " + HttpURLConnection.HTTP_OK);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			log.info("\nGetting response...");
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String res = response.toString();

			JSONObject jsonObject = new JSONObject(res);
			sessionKey = jsonObject.getString("sessionKey");

			log.info("Session Key: " + sessionKey);
		}

		return sessionKey;
	}

	@Override
	public void close() {
		// Not implemented
	}

	private String getBaseURL() {
		String myEnv = PlainLoginModule.getURL();
		if (myEnv == null || "".equals(myEnv))
			return BASE_JOIN;
		else
			return myEnv;
	}

	private String getDefaultVertical() {
		String myEnv = PlainLoginModule.getDefaultVertical();
		if (myEnv == null || "".equals(myEnv))
			return DEFAULT_VERTICAL;
		else
			return myEnv;
	}

	private String getDefaultTenant() {
		String myEnv = PlainLoginModule.getDefaultTenant();
		if (myEnv == null || "".equals(myEnv))
			return DEFAULT_TENANT;
		else
			return myEnv;
	}

}