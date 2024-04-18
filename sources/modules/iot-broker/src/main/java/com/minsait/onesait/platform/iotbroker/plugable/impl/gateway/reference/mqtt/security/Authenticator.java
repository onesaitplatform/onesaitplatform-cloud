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
package com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt.security;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.json.JSONObject;

import com.hazelcast.core.HazelcastInstance;
import com.minsait.onesait.platform.iotbroker.plugable.impl.gateway.reference.mqtt.MqttSession;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import io.moquette.broker.security.IAuthenticator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Authenticator implements IAuthenticator {

	private static final String CHARSET = "UTF-8";
	private static final String BASE_JOIN = "http://localhost:19000/iot-broker/rest/";
	private static final String JOIN_URL = "client/kafka/join/";
	private static final String USER_AGENT = "Mozilla/5.0";

	private HazelcastInstance hzInstance = BeanUtil.getBean(HazelcastInstance.class);
	Map<String, MqttSession> mqttClientSessions = hzInstance.getMap("mqttClientSessions");

	@Override
	public boolean checkValid(String clientId, String digitalClient, byte[] token) {
		try {
			if (digitalClient == null || token == null) {
				// MQTT con protrocolo Plataforma no valida user/pass
				return true;
			}
			// Soporte MQTT nativo, validamos user/pass
			String vertical = null;
			String tenant = null;
			String[] split = digitalClient.split(":");
			if (split.length == 1) {
				tenant = Tenant2SchemaMapper.defaultTenantName(Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME);
				vertical = Tenant2SchemaMapper.DEFAULT_VERTICAL_NAME;
			} else {
				digitalClient = split[0];
				vertical = split[1];
				tenant = split[2];
			}

			StringBuilder query = new StringBuilder();

			query.append(
					String.format("token=%s", URLEncoder.encode(new String(token, StandardCharsets.UTF_8), CHARSET)))
					.append("&").append(String.format("clientPlatform=%s", URLEncoder.encode(digitalClient, CHARSET)))
					.append("&").append(String.format("clientPlatformId=%s", URLEncoder.encode(digitalClient, CHARSET)))
					.append("&").append(String.format("vertical=%s", URLEncoder.encode(vertical, CHARSET))).append("&")
					.append(String.format("tenant=%s", URLEncoder.encode(tenant, CHARSET)));

			URL obj = new URL(BASE_JOIN + JOIN_URL + "?" + query.toString());
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
				log.info("\nSending 'GET' request to URL : {}{}?{}", BASE_JOIN, JOIN_URL, query.toString());
				log.info("\nResponse Code OK: {}", HttpURLConnection.HTTP_OK);

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

				if (sessionKey != null) {
					MqttSession session = new MqttSession();
					session.setCliendId(clientId);
					session.setToken(new String(token));
					session.setDigitalClient(digitalClient);
					session.setSessionKey(sessionKey);
					mqttClientSessions.put(clientId, session);
				}
				return true;
			}

		} catch (Exception e) {
			log.error("Error authenticating MQTT client {} to broker.{}", digitalClient, e);
			return false;
		}
		return false;
	}

}
