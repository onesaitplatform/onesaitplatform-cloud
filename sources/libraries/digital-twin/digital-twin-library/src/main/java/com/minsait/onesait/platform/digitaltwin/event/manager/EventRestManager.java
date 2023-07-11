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
package com.minsait.onesait.platform.digitaltwin.event.manager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.digitaltwin.event.model.EventMessage;
import com.minsait.onesait.platform.digitaltwin.event.model.LogMessage;
import com.minsait.onesait.platform.digitaltwin.event.model.PingMessage;
import com.minsait.onesait.platform.digitaltwin.event.model.RegisterMessage;
import com.minsait.onesait.platform.digitaltwin.event.model.ShadowMessage;
import com.minsait.onesait.platform.digitaltwin.exception.DigitaltwinRuntimeException;
import com.minsait.onesait.platform.digitaltwin.exception.NetworkInterfaceNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventRestManager implements EventManager {

	private final int timeOut = (int) TimeUnit.SECONDS.toMillis(10);

	private static final String BROKERMESSAGE = "Broker message {}";

	@Value("${api.key}")
	private String apikey;

	@Value("${device.id}")
	private String deviceId;

	@Value("${device.rest.local.schema}")
	private String localUrlSchema;

	@Value("${device.rest.local.network.interface}")
	private String networkInterface;

	@Value("${device.rest.local.network.ipv6}")
	private Boolean ipv6;

	@Value("${server.port}")
	private String localPort;

	@Value("${server.ip}")
	private String ip;

	@Value("${device.rest.basepath}")
	private String localBasePath;

	@Value("${onesaitplatform.digitaltwin.broker.rest}")
	private String brokerEndpoint;

	@Value("${device.register.fail.retry.seconds:60}")
	private int registerRetryInterval;

	@Value("${device.ping.interval.seconds:10}")
	private int pingInterval;

	private RestTemplate restTemplate;

	private HttpHeaders headers;

	@PostConstruct
	public void init() {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;

		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			log.error("Error configuring SSL verification", e);
			throw new DigitaltwinRuntimeException("Problem configuring SSL verification", e);
		}

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		RequestConfig config = RequestConfig.custom().setSocketTimeout(timeOut).setConnectTimeout(timeOut)
				.setConnectionRequestTimeout(timeOut).build();

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setDefaultRequestConfig(config).build();

		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setHttpClient(httpClient);

		this.restTemplate = new RestTemplate(httpRequestFactory);
		this.headers = new HttpHeaders();
		this.headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		this.headers.setContentType(MediaType.APPLICATION_JSON);
		this.headers.set("Authorization", this.apikey);

		this.register();
	}

	private void register() {
		new RegistryAndKeepAliveThread().start();
	}

	/**
	 * Thread that manages the KeepAlive registry to platform
	 * 
	 * @author minsait by Indra
	 *
	 *
	 */
	class RegistryAndKeepAliveThread extends Thread {

		@Override
		public void run() {

			try {
				if (ip == null)
					ip = getLocalIp();
			} catch (Exception e) {
				log.error("Unable to get local IP to register device in broker", e);
				throw new DigitaltwinRuntimeException("Problem getting local IP", e);
			}

			RegisterMessage registerMessage = new RegisterMessage();
			registerMessage.setId(deviceId);
			registerMessage.setEndpoint(localUrlSchema + "://" + ip + ":" + localPort + "/" + localBasePath);

			PingMessage pingMessage = new PingMessage();
			pingMessage.setId(deviceId);

			HttpEntity<RegisterMessage> registerEntity = new HttpEntity<RegisterMessage>(registerMessage, headers);
			HttpEntity<PingMessage> pingEntity = new HttpEntity<PingMessage>(pingMessage, headers);

			while (true) {
				boolean register = false;

				// Send Register message to platform
				while (!register) {
					try {
						log.info("Attemp to register in broker {}", brokerEndpoint);

						ResponseEntity<String> resp = restTemplate.exchange(brokerEndpoint + "/event/register",
								HttpMethod.POST, registerEntity, String.class);
						if (resp.getStatusCode() == HttpStatus.OK) {
							log.info("Registered in broker {}", brokerEndpoint);
							register = true;
						} else {
							log.warn("HTTP code {} registering in broker {}", resp.getStatusCode(), brokerEndpoint);
							log.warn(BROKERMESSAGE, resp.getBody());
							try {
								Thread.sleep(registerRetryInterval * 1000l);
							} catch (Exception e) {
							}
						}
					} catch (Exception e) {
						log.error("Error trying to register in broker", e);
						register = false;
						try {
							Thread.sleep(registerRetryInterval * 1000l);
						} catch (Exception ex) {
						}
					}
				}

				// Send ping message to platform
				while (register) {
					try {
						log.info("Attemp to ping broker {}", brokerEndpoint);
						ResponseEntity<String> resp = restTemplate.exchange(brokerEndpoint + "/event/ping",
								HttpMethod.POST, pingEntity, String.class);
						if (resp.getStatusCode() == HttpStatus.OK) {
							log.info("Successful ping in broker {}", brokerEndpoint);
							try {
								Thread.sleep(registerRetryInterval * 1000l);
							} catch (Exception e) {
							}

						} else {
							log.warn("HTTP code {} trying to ping broker {}", resp.getStatusCode(), brokerEndpoint);
							log.warn(BROKERMESSAGE, resp.getBody());
							register = false;
						}
					} catch (Exception e) {
						log.error("Error trying to ping broker", e);
						register = false;
					}
				}
			}
		}

	}

	@Override
	public void updateShadow(Map<String, Object> status) {
		ShadowMessage shadowMessage = new ShadowMessage();
		shadowMessage.setId(deviceId);
		shadowMessage.setStatus(status);

		HttpEntity<ShadowMessage> shadowEntity = new HttpEntity<ShadowMessage>(shadowMessage, headers);

		log.info("Attemp to update shadow in broker {}", brokerEndpoint);
		ResponseEntity<String> resp = restTemplate.exchange(brokerEndpoint + "/event/shadow", HttpMethod.POST,
				shadowEntity, String.class);
		if (resp.getStatusCode() == HttpStatus.OK) {
			log.info("Updated shadow in broker {}", brokerEndpoint);
		} else {
			log.warn("HTTP code {} updating shadow in broker {}", resp.getStatusCode(), brokerEndpoint);
			log.warn(BROKERMESSAGE, resp.getBody());
		}
	}

	@Override
	public void log(String trace) {
		LogMessage logMessage = new LogMessage();
		logMessage.setId(deviceId);
		logMessage.setLog(trace);

		HttpEntity<LogMessage> logEntity = new HttpEntity<LogMessage>(logMessage, headers);

		log.info("Attemp to log in broker {}", brokerEndpoint);
		ResponseEntity<String> resp = restTemplate.exchange(brokerEndpoint + "/event/log", HttpMethod.POST, logEntity,
				String.class);
		if (resp.getStatusCode() == HttpStatus.OK) {
			log.info("Log in broker {}", brokerEndpoint);
		} else {
			log.warn("HTTP code {} log in broker {}", resp.getStatusCode(), brokerEndpoint);
			log.warn(BROKERMESSAGE, resp.getBody());
		}
	}

	@Override
	public void sendCustomEvent(Map<String, Object> status, String eventName) {
		EventMessage eventMessage = new EventMessage();
		eventMessage.setId(deviceId);
		eventMessage.setStatus(status);
		eventMessage.setEvent(eventName);

		HttpEntity<EventMessage> eventEntity = new HttpEntity<EventMessage>(eventMessage, headers);

		log.info("Attemp to sen a custom event {} in broker {}", eventName, brokerEndpoint);
		ResponseEntity<String> resp = restTemplate.exchange(brokerEndpoint + "/event/custom", HttpMethod.POST,
				eventEntity, String.class);
		if (resp.getStatusCode() == HttpStatus.OK) {
			log.info("Send custom event {} in broker {}", eventName, brokerEndpoint);
		} else {
			log.warn("HTTP code {} send custom event {} in broker {}", eventName, resp.getStatusCode(), brokerEndpoint);
			log.warn(BROKERMESSAGE, resp.getBody());
		}
	}

	private String getLocalIp() throws SocketException, NetworkInterfaceNotFoundException {
		Enumeration<NetworkInterface> enInterfaces = NetworkInterface.getNetworkInterfaces();
		while (enInterfaces.hasMoreElements()) {
			NetworkInterface ifc = enInterfaces.nextElement();
			if (networkInterface.equalsIgnoreCase(ifc.getName())) {
				Enumeration<InetAddress> enAddresses = ifc.getInetAddresses();
				while (enAddresses.hasMoreElements()) {
					String ip = enAddresses.nextElement().getHostAddress();
					if (ipv6) {
						if (ip.contains(":")) {
							return ip.substring(0, ip.indexOf('%'));
						}
					} else {
						if (!ip.contains(":")) {
							return ip;
						}
					}
				}
			}
		}
		throw new NetworkInterfaceNotFoundException();

	}

}
