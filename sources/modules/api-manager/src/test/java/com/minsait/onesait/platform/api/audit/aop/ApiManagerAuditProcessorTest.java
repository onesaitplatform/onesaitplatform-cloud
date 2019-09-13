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
package com.minsait.onesait.platform.api.audit.aop;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.minsait.onesait.platform.api.audit.bean.ApiManagerAuditEvent;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.audit.bean.AuditConst;
import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ApiManagerAuditProcessorTest {

	@InjectMocks
	ApiManagerAuditProcessor apiManagerAuditProcessor;

	private final String REMOTE_ADDRESS = "0.0.0.1";
	private final String REASON = "Reason dummy";
	private final String USER_ID = "userTest";
	private final String ONTOLOGY_NAME = "testOntology";
	private final String API_IDENTIFICATION = "testAPI";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void given_getStoppedEvent_no_stop_status() {
		final Map<String, Object> data = new HashMap<>();
		data.put(Constants.STATUS, ChainProcessingStatus.FOLLOW);
		final ApiManagerAuditEvent event = apiManagerAuditProcessor.getStoppedEvent(data);
		Assert.assertNull(event);
	}

	@Test
	public void given_getStoppedEvent_stop_status() {
		final Map<String, Object> data = new HashMap<>();
		data.put(Constants.STATUS, ChainProcessingStatus.STOP);

		data.put(Constants.REASON, REASON);

		data.put(Constants.REMOTE_ADDRESS, REMOTE_ADDRESS);
		data.put(Constants.ONTOLOGY, null);
		data.put(Constants.METHOD, ApiOperation.Type.GET.name());

		data.put(Constants.QUERY, "query test");
		data.put(Constants.BODY, "body test");
		data.put(Constants.API, getApiTest());
		data.put(Constants.USER, null);

		final ApiManagerAuditEvent event = apiManagerAuditProcessor.getStoppedEvent(data);

		Assert.assertNull(event.getOntology());
		assertEquals(AuditConst.ANONYMOUS_USER, event.getUser());
		assertEquals(REMOTE_ADDRESS, event.getRemoteAddress());
		assertEquals(OperationType.QUERY.name(), event.getOperationType());
		assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		assertEquals(Module.APIMANAGER, event.getModule());
		assertEquals(EventType.APIMANAGER, event.getType());
		assertEquals(REASON, event.getMessage());
	}

	@Test
	public void given_insert_event_getEvent() {

		final Map<String, Object> data = new HashMap<>();

		final String body = "{'test':'test'}";

		data.put(Constants.REMOTE_ADDRESS, REMOTE_ADDRESS);
		data.put(Constants.ONTOLOGY, getOntologyTest());
		data.put(Constants.METHOD, ApiOperation.Type.POST.name());
		data.put(Constants.BODY, body);
		data.put(Constants.USER, getUserTest());
		data.put(Constants.API, getApiTest());

		final ApiManagerAuditEvent event = apiManagerAuditProcessor.getEvent(data);

		assertEquals(ONTOLOGY_NAME, event.getOntology());
		assertEquals(REMOTE_ADDRESS, event.getRemoteAddress());
		assertEquals(OperationType.INSERT.name(), event.getOperationType());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(Module.APIMANAGER, event.getModule());
		assertEquals(EventType.APIMANAGER, event.getType());
		assertEquals(body, event.getData());
		Assert.assertNull(event.getQuery());
		assertEquals(USER_ID, event.getUser());
	}

	@Test
	public void given_event_getErrorEvent() {

		final Map<String, Object> data = new HashMap<>();
		final Exception ex = new Exception();

		final OPAuditError event = apiManagerAuditProcessor.getErrorEvent(data, ex);

		assertEquals(EventType.ERROR, event.getType());
		Assert.assertNotNull(event.getMessage());
		assertEquals(ex.getMessage(), event.getErrorMessage());
		assertEquals(Module.APIMANAGER, event.getModule());
	}

	@Test
	public void given_event_completeEvent_user_is_null() {
		ApiManagerAuditEvent event = ApiManagerAuditEvent.builder().build();

		final Map<String, Object> retVal = new HashMap<>();
		retVal.put(Constants.USER, null);

		event = apiManagerAuditProcessor.completeEvent(event);
		assertEquals(AuditConst.ANONYMOUS_USER, event.getUser());
	}

	@Test
	public void given_event_completeEvent_user_is_not_null() {

		ApiManagerAuditEvent event = ApiManagerAuditEvent.builder().user(USER_ID).build();
		event = apiManagerAuditProcessor.completeEvent(event);

		assertEquals(USER_ID, event.getUser());
	}

	@Test
	public void given_getAuditOperationFromMethod_with_an_unexisting_method() {
		final OperationType operationType = apiManagerAuditProcessor.getAuditOperationFromMethod("");
		Assert.assertNull(operationType);
	}

	@Test
	public void given_getAuditOperationFromMethod_with_null_method() {
		final OperationType operationType = apiManagerAuditProcessor.getAuditOperationFromMethod(null);
		Assert.assertNull(operationType);
	}

	@Test
	public void given_getAuditOperationFromMethod_with_get_method() {
		final OperationType operationType = apiManagerAuditProcessor
				.getAuditOperationFromMethod(ApiOperation.Type.GET.name());
		assertEquals(OperationType.QUERY, operationType);
	}

	@Test
	public void given_getAuditOperationFromMethod_with_post_method() {
		final OperationType operationType = apiManagerAuditProcessor
				.getAuditOperationFromMethod(ApiOperation.Type.POST.name());
		assertEquals(OperationType.INSERT, operationType);
	}

	@Test
	public void given_getAuditOperationFromMethod_with_put_method() {
		final OperationType operationType = apiManagerAuditProcessor
				.getAuditOperationFromMethod(ApiOperation.Type.PUT.name());
		assertEquals(OperationType.UPDATE, operationType);
	}

	@Test
	public void given_getAuditOperationFromMethod_with_delete_method() {
		final OperationType operationType = apiManagerAuditProcessor
				.getAuditOperationFromMethod(ApiOperation.Type.DELETE.name());
		assertEquals(OperationType.DELETE, operationType);
	}

	private User getUserTest() {
		final User user = new User();
		user.setUserId(USER_ID);
		return user;
	}

	private Api getApiTest() {
		final Api api = new Api();
		api.setIdentification(API_IDENTIFICATION);
		return api;
	}

	private Ontology getOntologyTest() {
		final Ontology ontology = new Ontology();
		ontology.setIdentification(ONTOLOGY_NAME);
		return ontology;
	}
}
