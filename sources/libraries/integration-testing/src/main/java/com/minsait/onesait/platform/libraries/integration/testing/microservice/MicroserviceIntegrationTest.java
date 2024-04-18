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
package com.minsait.onesait.platform.libraries.integration.testing.microservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.minsait.onesait.platform.libraries.integration.testing.IntegrationTestingApp;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = IntegrationTestingApp.class)
@Slf4j
public class MicroserviceIntegrationTest extends AbstractTestNGSpringContextTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Value("${microservice}")
	private String microservice;

	private final static String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRJZCI6Im9uZXNhaXRwbGF0Zm9ybSIsInVzZXJfbmFtZSI6ImFkbWluaXN0cmF0b3IiLCJ2ZXJ0aWNhbCI6Im9uZXNhaXRwbGF0Zm9ybSIsInZlcnRpY2FscyI6WyJvbmVzYWl0cGxhdGZvcm0iXSwiYXV0aG9yaXRpZXMiOlsiUk9MRV9BRE1JTklTVFJBVE9SIl0sImNsaWVudF9pZCI6Im9uZXNhaXRwbGF0Zm9ybSIsInByaW5jaXBhbCI6ImFkbWluaXN0cmF0b3IiLCJzY29wZSI6WyJvcGVuaWQiXSwibmFtZSI6ImFkbWluaXN0cmF0b3IiLCJleHAiOjE1OTE5MDA0NDgsImdyYW50VHlwZSI6InBhc3N3b3JkIiwicGFyYW1ldGVycyI6eyJ2ZXJ0aWNhbCI6bnVsbCwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwidXNlcm5hbWUiOiJhZG1pbmlzdHJhdG9yIn0sImp0aSI6Ijk3N2U5ZmVjLWE4ODgtNGFiYi05NWM1LTliZTdkNDU4NDk5MiIsInRlbmFudCI6ImRldmVsb3BtZW50X29uZXNhaXRwbGF0Zm9ybSJ9.GbTXWMAshDBzBwVjY3tA__HIjMGA3IaHIylDhocNJ3k";

	private final Map<String, Long> counter = new HashMap<>();

	@Test(alwaysRun = true, invocationCount = 5000, threadPoolSize = 40, timeOut = 1000000)
	public void a_testLoadBalancing() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, BEARER_TOKEN);

		final ResponseEntity<String> response = restTemplate.exchange(microservice + "/user/ip", HttpMethod.GET,
				new HttpEntity<>(null, headers), String.class);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
		final String ip = response.getBody();
		if (counter.containsKey(ip)) {
			counter.replace(ip, counter.get(ip) + 1);
		} else {
			counter.put(ip, Long.valueOf(0));
		}

	}

	@Test
	public void b_logResults() {
		counter.entrySet().forEach(e -> log.info("IP: {} , hits: {}", e.getKey(), e.getValue()));
	}

}
