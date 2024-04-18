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
package com.minsait.onesait.platform.flowengine.audit.aop;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.minsait.onesait.platform.audit.bean.OPAuditError;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.EventType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.Module;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.OperationType;
import com.minsait.onesait.platform.audit.bean.OPAuditEvent.ResultOperationType;
import com.minsait.onesait.platform.commons.flow.engine.dto.FlowEngineDomain;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.flowdomain.FlowDomainService;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.FlowEngineInvokeRestApiOperationRequest;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.MailRestDTO;
import com.minsait.onesait.platform.flowengine.api.rest.pojo.NotebookInvokeDTO;
import com.minsait.onesait.platform.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.minsait.onesait.platform.flowengine.api.rest.service.impl.FlowEngineNodeServiceImpl;
import com.minsait.onesait.platform.flowengine.audit.bean.FlowEngineAuditEvent;
import com.minsait.onesait.platform.flowengine.exception.NotAuthorizedException;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class FlowEngineAuditProcessorTest {

	@InjectMocks
	private FlowEngineAuditProcessor flowEngineAuditProcessor;

	@Mock
	private FlowDomainService domainService;

	@Mock
	private FlowEngineValidationNodeService flowEngineValidationNodeService;
	
	@Mock
	private FlowEngineNodeServiceImpl flowengineNodeService;

	private final String DOMAIN_ID = "dummyDomainId";
	private final String USER_ID = "dummyUserId";
	private final String RESULT_OK = "OK";
	private final String RESULT_KO = "NOTOK";
	private final String ONTOLOGY_ID = "dummyOntologyId";
	private final String INSTANCE = "{\"dummy\":\"dummyvalue\"}";
	private final String API_NAME = "dummyApiName";
	private final int API_VERSION = 1;
	private final String API_OPERATION_NAME = "dummyApiOperationName";
	private final String API_OPERATINN_METHOD = "GET";
	private final String NOTEBOOK_ID= "dummyNotebookId";
	private final String PIPELINE_ID= "dummyPipelineId";
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	public User getTestUser() {
		User user = new User();
		user.setUserId(USER_ID);
		return user;
	}

	public FlowDomain getTestFlowDomain() {
		FlowDomain flowDomain = new FlowDomain();
		flowDomain.setActive(true);
		flowDomain.setIdentification(DOMAIN_ID);
		flowDomain.setUser(getTestUser());
		return flowDomain;
	}
	
	public FlowEngineInvokeRestApiOperationRequest getTestApiInvocation() {
		FlowEngineInvokeRestApiOperationRequest request = new FlowEngineInvokeRestApiOperationRequest();
		request.setDomainName(DOMAIN_ID);
		request.setApiName(API_NAME);
		request.setApiVersion(API_VERSION);
		request.setOperationName(API_OPERATION_NAME);
		request.setOperationMethod(API_OPERATINN_METHOD);
		return request;
	}
	
	public NotebookInvokeDTO getTestNotebookInvokeDTO(){
		NotebookInvokeDTO dto = new NotebookInvokeDTO();
		dto.setDomainName(DOMAIN_ID);
		dto.setExecuteNotebook(true);
		dto.setNotebookId(NOTEBOOK_ID);
		return dto;
	}

	public MailRestDTO getTestMail(){
		MailRestDTO mail = new MailRestDTO();
		mail.setBody("dummyBody");
		String[] to = {"test@test.com"};
		mail.setTo(to);
		mail.setSubject("DummySubject");
		return mail;
	}
	
	@Test
	public void given_get_event_retVal_error() {

		String methodName = "startFlowEngineDomain";

		FlowEngineDomain domain = FlowEngineDomain.builder().domain(DOMAIN_ID).build();
		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());

		FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(methodName, RESULT_KO, domain,
				OperationType.START);
		Assert.assertEquals(OperationType.START.name(), event.getOperationType());
		Assert.assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		Assert.assertEquals(EventType.FLOWENGINE, event.getType());
		Assert.assertEquals(Module.FLOWENGINE, event.getModule());
		Assert.assertEquals(getTestUser().getUserId(), event.getUser());
		Assert.assertEquals(domain.getDomain(), event.getDomain());
		Assert.assertEquals(RESULT_KO, event.getMessage());

	}

	@Test
	public void given_get_event_retVal_ok() {

		String methodName = "startFlowEngineDomain";

		FlowEngineDomain domain = FlowEngineDomain.builder().domain(DOMAIN_ID).build();

		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());

		FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(methodName, RESULT_OK, domain,
				OperationType.START);
		Assert.assertEquals(OperationType.START.name(), event.getOperationType());
		Assert.assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		Assert.assertEquals(EventType.FLOWENGINE, event.getType());
		Assert.assertEquals(Module.FLOWENGINE, event.getModule());
		Assert.assertEquals(getTestUser().getUserId(), event.getUser());
		Assert.assertEquals(domain.getDomain(), event.getDomain());

	}

	@Test
	public void given_get_event_incorrect_credentials() {

		when(domainService.getFlowDomainById(DOMAIN_ID)).thenThrow(new NotAuthorizedException(""));

		FlowEngineAuditEvent event = flowEngineAuditProcessor.getEvent(ONTOLOGY_ID, "", QueryType.NATIVE.name(), null,
				"message", DOMAIN_ID, OperationType.QUERY);

		Assert.assertNull(event);

	}

	@Test
	public void given_create_error_event() {
		Exception ex = new Exception();
		String message = "dummyMessage";

		OPAuditError event = flowEngineAuditProcessor.createErrorEvent(USER_ID, message, ex);

		assertEquals(EventType.ERROR, event.getType());
		Assert.assertNotNull(event.getMessage());
		assertEquals(ex.getMessage(), event.getErrorMessage());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(USER_ID, event.getUser());

	}

	@Test
	public void given_get_error_event_incorrect_credentials() {

		when(domainService.getFlowDomainById(DOMAIN_ID)).thenThrow(new NotAuthorizedException(""));

		OPAuditError error = flowEngineAuditProcessor.getErrorEvent("", DOMAIN_ID, new Exception());

		Assert.assertNull(error);
	}

	@Test
	public void given_get_error_event() {
		Exception ex = new Exception();
		String methodName = "dummyMethod";
		FlowEngineDomain domain = FlowEngineDomain.builder().domain(DOMAIN_ID).build();

		FlowDomain flowDomain = new FlowDomain();
		flowDomain.setUser(getTestUser());

		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());

		OPAuditError event = flowEngineAuditProcessor.getErrorEvent(methodName, domain, ex);

		assertEquals(EventType.ERROR, event.getType());
		Assert.assertNotNull(event.getMessage());
		assertEquals(ex.getMessage(), event.getErrorMessage());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(USER_ID, event.getUser());
	}

	@Test
	public void given_get_query_event() {

		
		String query = "";

		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());

		FlowEngineAuditEvent event = flowEngineAuditProcessor.getQueryEvent(ONTOLOGY_ID, query, QueryType.NATIVE.name(),
				"", DOMAIN_ID);

		assertEquals(EventType.FLOWENGINE, event.getType());
		Assert.assertNull(event.getData());
		assertEquals(ONTOLOGY_ID, event.getOntology());
		assertEquals(query, event.getQuery());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(OperationType.QUERY.name(), event.getOperationType());
		assertEquals(USER_ID, event.getUser());
	}

	@Test
	public void given_get_insert_event() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());

		FlowEngineAuditEvent event = flowEngineAuditProcessor.getInsertEvent(ONTOLOGY_ID, INSTANCE, "", DOMAIN_ID);

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(ONTOLOGY_ID, event.getOntology());
		assertEquals(INSTANCE, event.getData());
		Assert.assertNull(event.getQuery());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(OperationType.INSERT.name(), event.getOperationType());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_invoke_API_event_ok() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineInvokeRestApiOperationRequest request = getTestApiInvocation();
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processInvokeAPI(request, new ResponseEntity<>(HttpStatus.OK));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_invoke_API_event_error() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineInvokeRestApiOperationRequest request = getTestApiInvocation();
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processInvokeAPI(request, new ResponseEntity<>(HttpStatus.NOT_FOUND));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	

	@Test
	public void given_get_invoke_notebook_event_ok() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processInvokeNotebook(getTestNotebookInvokeDTO(), new ResponseEntity<>(HttpStatus.OK));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_invoke_notebook_event_error() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processInvokeNotebook(getTestNotebookInvokeDTO(), new ResponseEntity<>(HttpStatus.NOT_FOUND));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	

	@Test
	public void given_get_check_dataflow_status_event_ok() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processCheckDataflowStatus(DOMAIN_ID, PIPELINE_ID, new ResponseEntity<>(HttpStatus.OK));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_check_dataflow_status_event_error() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processCheckDataflowStatus(DOMAIN_ID, PIPELINE_ID, new ResponseEntity<>(HttpStatus.NOT_FOUND));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_stop_dataflow_event_ok() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processStopDataflow(DOMAIN_ID, PIPELINE_ID, new ResponseEntity<>(HttpStatus.OK));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_stop_dataflow_event_error() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processStopDataflow(DOMAIN_ID, PIPELINE_ID, new ResponseEntity<>(HttpStatus.NOT_FOUND));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_start_dataflow_event_ok() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processStartDataflow(DOMAIN_ID, PIPELINE_ID, new ResponseEntity<>(HttpStatus.OK));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_start_dataflow_event_error() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		when(domainService.getFlowDomainByIdentification(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processStartDataflow(DOMAIN_ID, PIPELINE_ID, new ResponseEntity<>(HttpStatus.NOT_FOUND));

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.ERROR, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	@Test
	public void given_get_send_mail_event() {


		when(domainService.getFlowDomainById(DOMAIN_ID)).thenReturn(getTestFlowDomain());
		MailRestDTO mail = getTestMail();
		when(domainService.getFlowDomainByIdentification(mail.getDomainName())).thenReturn(getTestFlowDomain());
		FlowEngineAuditEvent event = flowEngineAuditProcessor.processSendMail(mail);

		assertEquals(EventType.FLOWENGINE, event.getType());
		assertEquals(Module.FLOWENGINE, event.getModule());
		assertEquals(ResultOperationType.SUCCESS, event.getResultOperation());
		assertEquals(USER_ID, event.getUser());
	}
	
	

}
