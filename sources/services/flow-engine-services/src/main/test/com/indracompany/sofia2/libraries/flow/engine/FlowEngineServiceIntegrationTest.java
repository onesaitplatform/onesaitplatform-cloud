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
package com.indracompany.sofia2.libraries.flow.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.junit4.SpringRunner;

import com.indracompany.sofia2.commons.flow.engine.dto.FlowEngineDomain;
import com.indracompany.sofia2.commons.flow.engine.dto.FlowEngineDomainStatus;

@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FlowEngineServiceIntegrationTest {

	// TODO this test should not expect FlowEngine to be up. Erase this tests.

	private FlowEngineService flowEngineService;
	private String baseUrl = "http://localhost:8082/flowengine/admin";
	private int restTimeout = 5000;
	private String domainId;
	private int port = 8005;
	private int servicePort = 7005;
	private String home = "/tmp/user/";
	private FlowEngineDomain domain;

	@Before
	public void setup() {
		flowEngineService = FlowEngineServiceFactory.getFlowEngineService(this.baseUrl, this.restTimeout);
		domainId = "DomainTest-" + UUID.randomUUID();
		domain = FlowEngineDomain.builder().domain(domainId).port(port).servicePort(servicePort).home(home).build();
	}

	@Test
	public void test1_createDomain() {

		flowEngineService.createFlowengineDomain(domain);
		FlowEngineDomain domainCreated = flowEngineService.getFlowEngineDomain(domain.getDomain());
		Assert.assertTrue(domainCreated != null);
	}

	@Test
	public void test2_startDomain() {
		FlowEngineDomain domainCreated = flowEngineService.getFlowEngineDomain(domainId);
		flowEngineService.startFlowEngineDomain(domainCreated);
		Assert.assertTrue(true);
	}

	@Test
	public void test3_getAllDomins() {
		List<FlowEngineDomainStatus> domains = flowEngineService.getAllFlowEnginesDomains();
		Assert.assertTrue(domains != null);
	}

	@Test
	public void test4_getDomin() {
		FlowEngineDomain domain = flowEngineService.getFlowEngineDomain(domainId);
		Assert.assertTrue(domain != null);
	}

	@Test
	public void test5_getDominListStatus() {
		List<String> domainList = new ArrayList<>();
		domainList.add(domainId);
		List<FlowEngineDomainStatus> domainStatus = flowEngineService.getFlowEngineDomainStatus(domainList);
		Assert.assertTrue(domainStatus != null);
	}

	@Test
	public void test6_stopDomain() {
		flowEngineService.stopFlowEngineDomain(domainId);
		Assert.assertTrue(true);
	}

	@Test
	public void test7_deleteDomain() {
		flowEngineService.deleteFlowEngineDomain(domainId);
		FlowEngineDomain domain = flowEngineService.getFlowEngineDomain(domainId);
		Assert.assertTrue(domain == null);
	}

	@After
	public void cleanUp() {
		// Deletion of created testing entities
	}
}
