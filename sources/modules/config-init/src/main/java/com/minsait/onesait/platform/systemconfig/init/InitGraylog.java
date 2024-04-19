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
package com.minsait.onesait.platform.systemconfig.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.encryptor.aop.Encryptable;
import com.minsait.onesait.platform.systemconfig.init.graylog.index.IndexSetDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.index.IndexSetResponseDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.input.InputConfigurationDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.input.InputListResponseDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.input.InputRequestDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.node.NodeDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.node.NodesResponseDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.session.SessionRequestDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.session.SessionResponseDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.stream.StreamRequestDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.stream.StreamResponseDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.stream.StreamRuleDTO;
import com.minsait.onesait.platform.systemconfig.init.graylog.stream.StreamsSetRequestDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "onesaitplatform.init.graylog")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitGraylog {

	@Value("${onesaitplatform.graylog.user:admin}")
	private String graylogUser;
	@Value("${onesaitplatform.graylog.password}")
	@Encryptable
	private String graylogPassword;
	@Value("${onesaitplatform.graylog.externalUri:http://127.0.0.1:9000}")
	String graylogExternalUri;

	private static final String SESSION_PATH = "/api/system/sessions";
	private static final String INPUT_PATH = "/api/system/inputs";
	private static final String STREAM_PATH = "/api/streams";
	private static final String NODE_PATH = "/api/system/cluster/nodes";
	private static final String XREQUESTEDBY = "X-Requested-By";
	private static final String REQUESTER = "onesatiPlatformClient";
	private static final String INDEX_SET_PATH = "/api/system/indices/index_sets?skip=0&limit=0&stats=false";

	private static final String DEFAULT_INPUT_NAME = "GELF TCP";

	private final RestTemplate restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

	@PostConstruct
	@Test
	public void init() throws GenericOPException {
		// SET MODULES
		Map<String, String> modules = getModuleAppNames();

		try {
			// GET SESSION TOKEN
			log.info("Login into Graylog. Asking for session token...");
			final SessionResponseDTO sessionToken = getSessionToken();
			log.info("Login completed.");

			// GET NODES INFO AND GET MASTER

			log.info("Reading Graylog nodes info ...");
			final NodesResponseDTO nodes = getGraylogNodes(sessionToken.getSession_id());
			log.info("Graylog nodes info successfully read. Getting master...");
			NodeDTO master = null;
			for (NodeDTO node : nodes.getNodes()) {
				if (Boolean.TRUE.equals(node.getIs_master())) {
					master = node;
					break;
				}
			}
			if (master == null) {
				log.error("Graylog master was not found. Config aborted.");
			} else {
				log.info("Graylog master found.");
				// CREATE INPUT
				log.info("Creating INPUT...");
				createGraylogInput(sessionToken.getSession_id(), master);
				log.info("GELF TCP Input created.");
				// CREATE STREAM
				log.info("Creating Streams...");
				for (Entry<String, String> module : modules.entrySet()) {

					log.info("Creating Stream for {} ..." + module.getKey());
					setGraylogStream(sessionToken.getSession_id(), module.getKey(), module.getValue());
					log.info("Stream created for {}." + module.getKey());
				}
				log.info("All Strams have been created");
				// CREATE SIDECAR CONFIGS
			}
		} catch (Exception e) {
			log.error("Error while configuring Graylog. Message={}, Cause={}", e.getMessage(), e.getCause());
		}
	}

	private SessionResponseDTO getSessionToken() {
		ResponseEntity<SessionResponseDTO> result;
		SessionRequestDTO sessionRequest = new SessionRequestDTO();
		sessionRequest.setUsername(graylogUser);
		sessionRequest.setPassword(graylogPassword);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		headers.add(XREQUESTEDBY, REQUESTER);

		HttpEntity<?> entity = new HttpEntity<>(sessionRequest, headers);

		String url = graylogExternalUri + SESSION_PATH;

		result = restTemplate.postForEntity(url, entity, SessionResponseDTO.class);

		return result.getBody();
	}

	private void createGraylogInput(String sessionToken, NodeDTO node) {

		if (!existsDefaultOnesaitPlatformInput(sessionToken)) {
			InputConfigurationDTO inputConfig = InputConfigurationDTO.builder().bindAddress("0.0.0.0")
					.decompressSizeLimit(8388608).maxMessageSize(2097152).numberWorkerThreads(4).overrideSource(null)
					.port(12201).recvBufferSize(1048576).tcpKeepalive(false).tlsCertFile("").tlsClientAuth("disabled")
					.tlsClientAuthCertFile("").tlsEnable(false).tlsKeyFile("").tlsKeyPassword("").useNullDelimiter(true)
					.build();
			InputRequestDTO inputRequest = new InputRequestDTO();
			inputRequest.setNode(node.getNode_id());
			inputRequest.setType("org.graylog2.inputs.gelf.tcp.GELFTCPInput");
			inputRequest.setTitle(DEFAULT_INPUT_NAME);
			inputRequest.setGlobal(false);
			inputRequest.setConfiguration(inputConfig);

			HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
			HttpEntity<?> entity = new HttpEntity<>(inputRequest, headers);
			String url = graylogExternalUri + INPUT_PATH;

			restTemplate.postForEntity(url, entity, String.class);

		} else {
			log.info("An input with the default name already exits in Graylog. Creation Skipped.");
		}
	}

	private NodesResponseDTO getGraylogNodes(String sessionToken) {
		ResponseEntity<NodesResponseDTO> result;

		HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
		HttpEntity<?> entity = new HttpEntity<>(null, headers);

		String url = graylogExternalUri + NODE_PATH;

		result = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, NodesResponseDTO.class);
		return result.getBody();
	}

	private void setGraylogStream(String sessionToken, String title, String moduleName) {
		// CHECK IF STREAM EXISTS
		if (existsStreamByTitle(sessionToken, title)) {
			log.info("A stream already exist for title: {}. Skipping Stream creation.", title);
			return;
		}
		// Retrieve index "index_prefix": "graylog",
		String indexSetId = getGraylogDefaultIndexId(sessionToken);
		ResponseEntity<StreamResponseDTO> result;
		HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
		StreamRuleDTO streamRule = StreamRuleDTO.builder().field("app_name").value(moduleName)
				.description(title + " rule").type(1).inverted(false).build();
		List<StreamRuleDTO> rules = new ArrayList<>();
		rules.add(streamRule);
		StreamRequestDTO streamRequest = StreamRequestDTO.builder().title(title).description(title).rules(rules)
				.indexSetId(indexSetId).matchingType("AND").removeMatchesFromDefaultStream(true).build();
		HttpEntity<?> entity = new HttpEntity<>(streamRequest, headers);
		String url = graylogExternalUri + STREAM_PATH;
		result = restTemplate.postForEntity(url, entity, StreamResponseDTO.class);
		startGraylogStream(sessionToken, result.getBody().getStreamId());

	}

	private void startGraylogStream(String sessionToken, String streamId) {

		HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		String url = graylogExternalUri + STREAM_PATH + "/" + streamId + "/resume";
		restTemplate.postForEntity(url, entity, String.class);
	}

	private boolean existsDefaultOnesaitPlatformInput(String sessionToken) {

		HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		String url = graylogExternalUri + INPUT_PATH;
		ResponseEntity<InputListResponseDTO> response = restTemplate.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, InputListResponseDTO.class);
		for (InputRequestDTO input : response.getBody().getInputs()) {
			if (input.getTitle().equals(DEFAULT_INPUT_NAME)) {
				return true;
			}
		}
		return false;
	}

	private boolean existsStreamByTitle(String sessionToken, String title) {
		HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		String url = graylogExternalUri + STREAM_PATH;
		ResponseEntity<StreamsSetRequestDTO> response = restTemplate.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, StreamsSetRequestDTO.class);
		for (StreamRequestDTO stream : response.getBody().getStreams()) {
			if (stream.getTitle().equals(title)) {
				return true;
			}
		}
		return false;
	}

	private String getGraylogDefaultIndexId(String sessionToken) {
		ResponseEntity<IndexSetResponseDTO> result;
		HttpHeaders headers = getAuthHeadersForGraylog(sessionToken);
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		String url = graylogExternalUri + INDEX_SET_PATH;

		result = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, IndexSetResponseDTO.class);

		for (IndexSetDTO index : result.getBody().getIndexSets()) {
			if (index.getIndexPrefix().equals("graylog")) {
				return index.getId();
			}
		}
		return null;
	}

	private HttpHeaders getAuthHeadersForGraylog(String sessionToken) {
		String plainCreds = sessionToken + ":session";
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		headers.add(XREQUESTEDBY, REQUESTER);
		return headers;
	}

	private Map<String, String> getModuleAppNames() {
		Map<String, String> modules = new HashMap<>();
		modules.put("Control Panel", "onesaitplatform-control-panel");
		modules.put("Cache Server", "onesaitplatform-cache-server");
		modules.put("API Manager", "onesaitplatform-api-manager");
		modules.put("Device Simulator", "onesaitplatform-device-simulator");
		modules.put("Rules enginne", "onesaitplatform-rules-engine");
		modules.put("Video Broker", "onesaitplatform-video-broker");
		modules.put("Config Init", "systemconfig-init");
		modules.put("Dashboard Engine", "onesaitplatform-dashboard-engine");
		modules.put("Flow Engine", "onesaitplatform-flow-engine");
		modules.put("Digital Twin Broker", "onesaitplatform-digitaltwin-broker");
		modules.put("IoT Broker", "onesaitplatform-iot-broker");
		modules.put("Monitorin UI", "onesaitplatform-monitoring-ui");
		modules.put("Oauth Server", "onesaitplatform-oauth-server");
		modules.put("RTDB Maintainer", "onesaitplatform-rtdb-maintainer");
		modules.put("Semantic Inf Broker", "onesaitplatform-semantic-inf-broker");
		modules.put("Repor Engine", "onesaitplatform-report-engine");
		modules.put("Microservices Gateway", "microservices-gateway");
		modules.put("REST Planner", "onesaitplatform-rest-planner");
		modules.put("BPM Engine", "onesaitplatform-bpm-engine");
		modules.put("Kafka", "Kafka-broker-1");
		modules.put("Zookeeper", "Zookeeper-server-1");
		modules.put("Dataflow", "Streamsets-server-1");
		modules.put("Notebooks", "Zeppelin-server-1");
		return modules;
	}
}
