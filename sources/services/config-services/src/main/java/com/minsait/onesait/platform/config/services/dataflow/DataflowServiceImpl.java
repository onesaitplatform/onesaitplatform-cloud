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
package com.minsait.onesait.platform.config.services.dataflow;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.dto.PipelineForList;
import com.minsait.onesait.platform.config.model.DataflowInstance;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.PipelineUserAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataflowInstanceRepository;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.dataflow.beans.InstanceBuilder;
import com.minsait.onesait.platform.config.services.dataflow.configuration.DataflowServiceConfiguration;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataflowServiceImpl implements DataflowService {

	private static final String AUTHORIZATION = "Authorization";
	private static final String DATAFLOW_HEADER = "X-Streamsets-ID";
	private static final String UTF8_STR = "UTF-8";
	private static final String PIPELINEID = "pipelineId";
	private static final String BASIC = "Basic ";

	public enum RemoveInstanceAction {
		EXPORT, REMOVE, HARD_REMOVE
	}

	@Autowired
	private DataflowServiceConfiguration serviceConfiguration;

	@Autowired
	private PipelineRepository pipelineRepository;

	@Autowired
	private DataflowInstanceRepository instancesRepository;

	@Autowired
	private PipelineUserAccessRepository pipelineUserAccessRepository;

	@Autowired
	private PipelineUserAccessTypeRepository pipelineUserAccessTypeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Lazy
	private OPResourceService resourceService;

	@Autowired
	private UserService userService;

	@Autowired
	@Qualifier("serviceClientRest")
	private RestTemplate rt;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	@Autowired
	private SecurityService securityService;

	private final ObjectMapper mapper = new ObjectMapper();
	private DataflowInstance defaultinstance;

	@PostConstruct
	public void init() {
		final DataflowInstance byDefault = instancesRepository.findByDefaultInstance(true);
		setDefaultDataflowInstance(byDefault);
	}

	@Override
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, Object body,
			String userId) {
		final String url = requestServlet.getServletPath()
				+ (requestServlet.getQueryString() != null ? "?" + requestServlet.getQueryString() : "");
		final String contentType = requestServlet.getContentType() == null ? MediaType.APPLICATION_JSON_UTF8_VALUE
				: requestServlet.getContentType();
		final String streamsetsId = requestServlet.getHeader(DATAFLOW_HEADER);
		final User user = getUserById(userId);

		final HttpHeaders headers = getAuthorizationHeadersForUserAndStreamsetsId(user, streamsetsId);
		headers.setContentType(MediaType.parseMediaType(contentType));

		final DataflowInstance instance = getInstanceForUserAndStreamsetsId(user, streamsetsId);

		return sendHttp(url, httpMethod, body, headers, instance);
	}

	// Only accessible when administrator
	@Override
	public ResponseEntity<String> sendHttpWithInstance(HttpServletRequest requestServlet, HttpMethod httpMethod,
			Object body, String instanceId) {
		final String url = requestServlet.getServletPath()
				+ (requestServlet.getQueryString() != null ? "?" + requestServlet.getQueryString() : "");
		final String contentType = requestServlet.getContentType() == null ? MediaType.APPLICATION_JSON_UTF8_VALUE
				: requestServlet.getContentType();

		final DataflowInstance instance = getDataflowInstanceById(instanceId);
		final HttpHeaders headers = getAuthorizationHeadersForInstance(instance);
		headers.setContentType(MediaType.parseMediaType(contentType));

		return sendHttp(url, httpMethod, body, headers, instance);
	}

	@Override
	public ResponseEntity<String> sendHttpFile(HttpServletRequest requestServlet, MultipartFile file, String userId) {
		final String url = requestServlet.getServletPath()
				+ (requestServlet.getQueryString() != null ? "?" + requestServlet.getQueryString() : "");
		final String streamsetsId = requestServlet.getHeader(DATAFLOW_HEADER);
		final User user = getUserById(userId);

		final HttpHeaders headers = getAuthorizationHeadersForUserAndStreamsetsId(user, streamsetsId);
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		final DataflowInstance instance = getInstanceForUserAndStreamsetsId(user, streamsetsId);

		try {
			final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
			fileMap.add(HttpHeaders.CONTENT_DISPOSITION,
					"form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"");
			final HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);

			final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", fileEntity);

			return sendHttp(url, HttpMethod.POST, body, headers, instance);
		} catch (final Exception e) {
			log.error(e.getMessage());
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	// Only accessible when administrator
	@Override
	public ResponseEntity<String> sendHttpFileWithInstance(HttpServletRequest requestServlet, MultipartFile file,
			String instanceId) {
		final String url = requestServlet.getServletPath()
				+ (requestServlet.getQueryString() != null ? "?" + requestServlet.getQueryString() : "");
		final DataflowInstance instance = getDataflowInstanceById(instanceId);
		final HttpHeaders headers = getAuthorizationHeadersForInstance(instance);
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		try {
			final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
			fileMap.add(HttpHeaders.CONTENT_DISPOSITION,
					"form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"");
			final HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);

			final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", fileEntity);

			return sendHttp(url, HttpMethod.POST, body, headers, instance);
		} catch (final Exception e) {
			log.error(e.getMessage());
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	private ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, Object body, HttpHeaders headers,
			DataflowInstance instance) {
		if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.DELETE || httpMethod == HttpMethod.PUT) {
			headers.add("X-Requested-By", "OnesaitPlatform");
		}

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		final HttpEntity<Object> request = new HttpEntity<>(body, headers);
		try {
			final URI uri = new URI(instance.getUrl() + url.substring(url.toLowerCase().indexOf("/rest")));
			log.debug("[DATAFLOW] Execute method {} to path '{}'", httpMethod.toString(), uri.getPath());
			return restTemplate.exchange(uri, httpMethod, request, String.class);
		} catch (final URISyntaxException e) {
			log.error(e.getMessage());
			throw new BadRequestException(e.getMessage(), e);
		}
	}

	private DataflowInstance getInstanceForUserAndStreamsetsId(final User user, final String streamsetsId) {
		if (streamsetsId == null) {
			return getDataflowInstanceForUser(user);
		} else {
			final Pipeline pipeline = getPipelineByIdStreamsets(streamsetsId);
			return pipeline.getInstance();
		}
	}

	private User getUserById(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			throw new BadRequestException("User is null or empty, not authorized");
		} else {
			final User user = userRepository.findByUserId(userId);
			if (user == null) {
				throw new NotFoundException("User not found, not authorized");
			} else {
				return user;
			}
		}
	}

	private HttpHeaders getAuthorizationHeadersForUserAndStreamsetsId(final User user, final String streamsetsId) {
		if (streamsetsId == null) {
			return getAuthorizationHeadersForUser(user);
		} else {
			final Pipeline pipeline = getPipelineByIdStreamsets(streamsetsId);
			return getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		}
	}

	private HttpHeaders getAuthorizationHeadersForUser(User user) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		final String credentials = getCredentials(user);
		httpHeaders.add(AUTHORIZATION, credentials);
		return httpHeaders;
	}

	private HttpHeaders getAuthorizationHeadersForPipelineAndUser(Pipeline pipeline, User user) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		final String credentials = getCredentials(user, pipeline);
		httpHeaders.add(AUTHORIZATION, credentials);
		return httpHeaders;
	}

	// Only accessible by admins
	private HttpHeaders getAuthorizationHeadersForInstance(DataflowInstance instance) {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(AUTHORIZATION, BASIC + instance.getAdminCredentials());
		return httpHeaders;
	}

	private String getCredentials(User user) {
		final String credentials;
		final DataflowInstance instance = getDataflowInstanceForUser(user);
		if (user.isAdmin()) {
			credentials = instance.getAdminCredentials();
		} else if (userService.isUserAnalytics(user)) {
			credentials = instance.getUserCredentials();
		} else {
			// Other roles can't create resources
			throw new NotAuthorizedException("User " + user.getUserId() + " have no rights over this resource");
		}
		return BASIC + credentials;
	}

	private String getCredentials(User user, Pipeline pipeline) {
		final String credentials;
		final DataflowInstance instance = pipeline.getInstance();
		if (user.isAdmin()) {
			credentials = instance.getAdminCredentials();
		} else if (hasUserEditPermission(pipeline, user)) {
			credentials = instance.getUserCredentials();
		} else if (hasUserViewPermission(pipeline, user)) {
			credentials = instance.getGuestCredentials();
		} else {
			throw new NotAuthorizedException("User " + user.getUserId() + " have no rights over this resource");
		}
		return BASIC + credentials;
	}

	@Override
	public ResponseEntity<byte[]> getyHttpBinary(HttpServletRequest requestServlet, String body, String userId) {
		final String path = requestServlet.getServletPath();
		final String streamsetsId = requestServlet.getParameter(PIPELINEID);
		final HttpMethod httpMethod = HttpMethod.valueOf(requestServlet.getMethod());

		final User user = getUserById(userId);
		final HttpHeaders headers = getAuthorizationHeadersForUserAndStreamsetsId(user, streamsetsId);
		headers.setContentType(MediaType.APPLICATION_JSON);

		final DataflowInstance instance = getInstanceForUserAndStreamsetsId(user, streamsetsId);

		return getHttpBinary(path, httpMethod, body, headers, instance);
	}


	@Override
	@Cacheable(key="#p0 + #p1", cacheNames="DataFlowIcons", unless = "#result == null")
	public byte[] getyHttpBinary(String lib, String id, HttpServletRequest requestServlet, String body, String user) {
		return getyHttpBinary(requestServlet, body, user).getBody();
	}



	private ResponseEntity<byte[]> getHttpBinary(String url, HttpMethod httpMethod, String body, HttpHeaders headers,
			DataflowInstance instance) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		final HttpEntity<String> request = new HttpEntity<>(body, headers);
		log.debug("Sending method {} Dataflow", httpMethod.toString());

		final ResponseEntity<byte[]> response;
		try {
			final URI uri = new URI(instance.getUrl() + url.substring(url.toLowerCase().indexOf("/rest")));
			response = restTemplate.exchange(uri, httpMethod, request, byte[].class);
		} catch (final Exception e) {
			log.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}

		log.debug("Execute method {} '{}' Dataflow", httpMethod.toString(), url);
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());

		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	@Override
	public Pipeline getPipelineById(String id) {
		if (id == null || id.trim().isEmpty()) {
			throw new BadRequestException("Pipeline id is null or empty");
		} else {
			final Pipeline pipeline = pipelineRepository.findById(id).orElse(null);
			return checkPipeline(pipeline);
		}
	}

	@Override
	public Pipeline getPipelineByIdentification(String identification) {
		if (identification == null || identification.trim().isEmpty()) {
			throw new BadRequestException("Pipeline dentification is null or empty");
		} else {
			final Pipeline pipeline = pipelineRepository.findByIdentification(identification);
			return checkPipeline(pipeline);
		}
	}

	@Override
	public Pipeline getPipelineByIdStreamsets(String streamsetsId) {
		if (streamsetsId == null || streamsetsId.trim().isEmpty()) {
			throw new BadRequestException("Pipeline streamsets id is null or empty");
		} else {
			final Pipeline pipeline = pipelineRepository.findByIdstreamsets(streamsetsId);
			return checkPipeline(pipeline);
		}
	}

	private Pipeline checkPipeline(Pipeline pipeline) {
		if (pipeline != null) {
			return pipeline;
		} else {
			throw new NotFoundException("Pipeline not found");
		}
	}

	@Override
	public boolean hasUserEditPermission(Pipeline pipeline, String userId) {
		final User user = getUserById(userId);
		return hasUserEditPermission(pipeline, user);
	}

	@Override
	public boolean hasUserViewPermission(Pipeline pipeline, String userId) {
		final User user = getUserById(userId);
		return hasUserViewPermission(pipeline, user);
	}

	private boolean hasUserViewPermission(Pipeline pipeline, User user) {
		final PipelineUserAccessType pUAView = new PipelineUserAccessType();
		pUAView.setId("ACCESS-TYPE-2"); // VIEW Access

		return hasUserEditPermission(pipeline, user) || pipeline.isPublic()
				|| hasUserPermissionInPipeline(pipeline, user, pUAView)
				|| resourceService.hasAccess(user.getUserId(), pipeline.getId(), ResourceAccessType.VIEW);
	}

	private boolean hasUserEditPermission(Pipeline pipeline, User user) {
		final PipelineUserAccessType pUA = new PipelineUserAccessType();
		pUA.setId("ACCESS-TYPE-1"); // EDIT Access

		return isUserOwner(pipeline, user) || hasUserPermissionInPipeline(pipeline, user, pUA)
				|| resourceService.hasAccess(user.getUserId(), pipeline.getId(), ResourceAccessType.MANAGE);
	}

	private boolean hasUserPermissionInPipeline(Pipeline pipeline, User user,
			PipelineUserAccessType pipelineUserAccessType) {
		return pipelineUserAccessRepository.findByPipelineAndUserAndAccess(pipeline, user,
				pipelineUserAccessType) != null;
	}

	private void checkUserViewPipelineAccess(Pipeline pipeline, User user) {
		if (!hasUserViewPermission(pipeline, user)) {
			throw new NotAuthorizedException("User can not access this resource");
		}
	}

	private void checkUserEditPipelineAccess(Pipeline pipeline, User user) {
		if (!hasUserEditPermission(pipeline, user)) {
			throw new NotAuthorizedException("User can not access this resource");
		}
	}

	private boolean isUserOwner(Pipeline pipeline, User user) {
		return user.isAdmin() || pipeline.getUser().equals(user);
	}

	private void checkUserIsOwner(Pipeline pipeline, User user) {
		if (!isUserOwner(pipeline, user)) {
			throw new NotAuthorizedException("You are not the owner or administrator");
		}
	}

	@Override
	public List<Pipeline> getPipelines(final String userId) {
		final User user = getUserById(userId);
		return getPipelines(user);
	}

	@Override
	public List<String> getIdentificationByUser(String userId) {
		final User user = getUserById(userId);
		if (user.isAdmin()) {
			return pipelineRepository.findIdentifications();
		} else {
			return pipelineRepository.findIdentificationsByUserAndAccess(user);
		}
	}

	private List<Pipeline> getPipelines(final User user) {
		if (user.isAdmin()) {
			return pipelineRepository.findAll();
		} else {
			return pipelineRepository.findByUserAndAccess(user);
		}
	}

	@Override
	public List<Pipeline> getPipelinesWithStatus(final String userId) {
		final User user = getUserById(userId);

		final List<Pipeline> pipelines = getPipelines(userId).stream()
				.map(pipeline -> addPipelineAccessType(pipeline, user)).collect(Collectors.toList());

		try {
			final ObjectNode response = getJSONPipelineStatus(user);
			final Map<String, Pipeline.PipelineStatus> pipelineStatusMap = getPipelinesStatusMap(response);
			for (final Pipeline pipeline : pipelines) {
				final Pipeline.PipelineStatus status = pipelineStatusMap.get(pipeline.getIdstreamsets());
				if (status == null) {
					pipeline.setStatus(Pipeline.PipelineStatus.INSTANCE_ERROR);
				} else {
					pipeline.setStatus(status);
				}
			}
		} catch (final Exception ignored) {
			// Ignored
			log.error(ignored.getMessage(), ignored);
		}
		return pipelines;
	}

	private Pipeline addPipelineAccessType(Pipeline pipeline, User user) {
		if (hasUserEditPermission(pipeline, user)) {
			pipeline.setAccessType(PipelineUserAccessType.Type.EDIT);
		} else {
			pipeline.setAccessType(PipelineUserAccessType.Type.VIEW);
		}
		return pipeline;
	}

	private Map<String, Pipeline.PipelineStatus> getPipelinesStatusMap(final ObjectNode pipelinesStatus) {
		final Map<String, Pipeline.PipelineStatus> statusMap = new HashMap<>();
		pipelinesStatus.forEach(instanceNode -> instanceNode.forEach(pipelineNode -> {
			final Pipeline.PipelineStatus status = Pipeline.PipelineStatus.valueOf(pipelineNode.get("status").asText());
			statusMap.put(pipelineNode.get("pipelineId").asText(), status);
		}));
		return statusMap;
	}

	@Override
	public Pipeline createPipeline(final Pipeline pipeline, final String userId) {
		if (pipelineRepository.findByIdentification(pipeline.getIdentification()) != null) {
			throw new ClientErrorException("Pipeline already exists in database",
					org.apache.http.HttpStatus.SC_CONFLICT);
		} else {
			log.info("Creating pipeline '{}' for user: {}", pipeline.getIdentification(), userId);
			try {
				final User user = getUserById(userId);
				final HttpHeaders headers = getAuthorizationHeadersForUser(user);
				final String basePath = getDataflowInstanceForUser(user).getUrl();
				final String encodedName = UriUtils.encode(pipeline.getIdentification(), UTF8_STR);

				final ResponseEntity<String> response = StreamsetsApiWrapper.pipelineCreate(rt, headers, basePath,
						encodedName, pipeline.getType());

				final HttpStatus statusCode = response.getStatusCode();
				if (statusCode != HttpStatus.CREATED) {
					log.error("Exception executing create pipeline, status code: {} Message: {}",
							response.getStatusCodeValue(), response.getBody());
					metricsManagerLogControlPanelDataflowsCreation(userId, "KO");
					throw new ClientErrorException("Exception executing create pipeline, status code: "
							+ response.getStatusCodeValue() + " Message: " + response.getBody(), statusCode.value());
				} else {
					return createPipelineFromResponse(pipeline, userId, user, response);
				}
			} catch (final Exception e) {
				log.error("Encoding not supported on name {}", pipeline.getIdentification(), e);
				metricsManagerLogControlPanelDataflowsCreation(userId, "KO");
				throw new BadRequestException("Encoding not supported with name " + pipeline.getIdentification()
				+ " Message: " + e.getMessage());
			}
		}
	}

	private Pipeline createPipelineFromResponse(Pipeline pipeline, String userId, User user,
			ResponseEntity<String> response) {
		try {
			final JSONObject createResponseObj = new JSONObject(response.getBody());
			final String dataflowId = createResponseObj.getString(PIPELINEID);
			final Pipeline pl = saveDBPipeline(pipeline.getIdentification(), dataflowId, user);
			log.info("Pipeline '{}' for user '{}' successfully created", pipeline.getIdentification(), userId);
			metricsManagerLogControlPanelDataflowsCreation(userId, "OK");
			return pl;
		} catch (final JSONException e) {
			log.error("Exception parsing answer in create pipeline. Response: {}", response.getBody(), e);
			metricsManagerLogControlPanelDataflowsCreation(userId, "KO");
			throw new BadRequestException("Exception parsing answer creating pipeline. Response: " + response.getBody(),
					e);
		}
	}

	private Pipeline saveDBPipeline(final String identification, final String streamsetsId, final User user) {
		final DataflowInstance instance = getDataflowInstanceForUser(user);

		final Pipeline pl = new Pipeline();
		pl.setIdentification(identification);
		pl.setIdstreamsets(streamsetsId);
		pl.setUser(user);
		pl.setInstance(instance);
		return pipelineRepository.save(pl);
	}

	private Pipeline updateDBPipeline(final String identification, final String streamsetsId, final User user) {
		final Pipeline pl = pipelineRepository.findByIdentification(identification);
		pl.setIdstreamsets(streamsetsId);
		return pipelineRepository.save(pl);
	}

	@Override
	public void removePipeline(final String pipelineId, final String userId) {
		deletePipeline(pipelineId, userId, true);
	}

	@Override
	public void removeHardPipeline(final String pipelineId, final String userId) {
		deletePipeline(pipelineId, userId, false);
	}

	private void deletePipeline(final String pipelineId, final String userId, final boolean removeFromInstance) {
		final Pipeline pipeline = getPipelineById(pipelineId);
		if (resourceService.isResourceSharedInAnyProject(pipeline)) {
			throw new BadRequestException(
					"This DataFlow is shared within a Project, revoke access from project prior to deleting");
		} else {
			final User user = getUserById(userId);
			checkUserEditPipelineAccess(pipeline, user);

			// If instance can be accessed
			if (removeFromInstance) {
				final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
				final String basePath = pipeline.getInstance().getUrl();
				final ResponseEntity<String> response = StreamsetsApiWrapper.pipelineRemove(rt, headers, basePath,
						pipeline.getIdstreamsets());
				final HttpStatus statusCode = response.getStatusCode();
				if (statusCode != HttpStatus.OK && statusCode != HttpStatus.ACCEPTED) {
					log.error("Exception executing delete pipeline, status code:  {}", response.getStatusCodeValue());
					throw new BadRequestException(
							"Exception executing delete pipeline, status code: " + response.getStatusCodeValue());
				}
			}

			pipelineRepository.delete(pipeline);
			log.info("Pipeline with id {} for user {} and , successfully deleted", pipeline.getIdentification(),
					user.getUserId());
		}
	}

	@Override
	public Pipeline renamePipeline(final String pipelineId, final String userId, final String newIdentification) {
		final Pipeline newPipeline = pipelineRepository.findByIdentification(newIdentification);
		if (newPipeline != null) {
			throw new ClientErrorException(
					"The pipeline with identification [" + newIdentification + "] already exists",
					Response.Status.CONFLICT);
		} else {
			final Pipeline pipeline = getPipelineById(pipelineId);
			final User user = getUserById(userId);
			checkUserEditPipelineAccess(pipeline, user);
			pipeline.setIdentification(newIdentification);
			return pipelineRepository.save(pipeline);
		}
	}

	@Override
	public ResponseEntity<String> startPipeline(String userId, String pipelineIdentification, String parameters) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserEditPipelineAccess(pipeline, user);

		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final String basePath = pipeline.getInstance().getUrl();

		return StreamsetsApiWrapper.pipelineStart(rt, headers, basePath, pipeline.getIdstreamsets(), parameters);
	}

	@Override
	public ResponseEntity<String> stopPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserEditPipelineAccess(pipeline, user);

		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final String basePath = pipeline.getInstance().getUrl();

		return StreamsetsApiWrapper.pipelineStop(rt, headers, basePath, pipeline.getIdstreamsets());
	}

	@Override
	public ResponseEntity<String> resetOffsetPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserEditPipelineAccess(pipeline, user);

		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final String basePath = pipeline.getInstance().getUrl();

		return StreamsetsApiWrapper.resetOffset(rt, headers, basePath, pipeline.getIdstreamsets());
	}

	@Override
	public ResponseEntity<String> statusPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserViewPipelineAccess(pipeline, user);

		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final String basePath = pipeline.getInstance().getUrl();

		return StreamsetsApiWrapper.pipelineStatus(rt, headers, basePath, pipeline.getIdstreamsets());
	}

	@Override
	public ResponseEntity<String> getPipelinesStatus(final String userId) {
		final User user = getUserById(userId);
		final ObjectNode result = getJSONPipelineStatus(user);
		return new ResponseEntity<>(result.toString(), HttpStatus.OK);
	}

	private ObjectNode getJSONPipelineStatus(User user) {
		final ObjectNode result = mapper.createObjectNode();

		if (user.isAdmin()) {
			final List<DataflowInstance> instances = getAllDataflowInstances();
			for (final DataflowInstance instance : instances) {
				final HttpHeaders headers = getAuthorizationHeadersForInstance(instance);
				setStatusToResult(user, headers, result, instance);
			}
		} else {
			final List<DataflowInstance> instances = getPipelines(user).stream().map(Pipeline::getInstance).distinct()
					.collect(Collectors.toList());

			for (final DataflowInstance instance : instances) {
				// Although this action is performed as admin, later the permissions are checked
				final HttpHeaders headers = getAuthorizationHeadersForInstance(instance);
				setStatusToResult(user, headers, result, instance);
			}
		}
		return result;
	}

	private void setStatusToResult(User user, HttpHeaders headers, ObjectNode result, DataflowInstance instance) {
		final Optional<ArrayNode> status = getPipelinesStatusForInstance(headers, instance, user);
		if (status.isPresent()) {
			result.set(instance.getIdentification(), status.get());
		} else {
			result.put(instance.getIdentification(), "Response from instance is not readable");
		}
	}

	private Optional<ArrayNode> getPipelinesStatusForInstance(final HttpHeaders headers,
			final DataflowInstance instance, final User user) {
		try {
			final ResponseEntity<String> response = StreamsetsApiWrapper.pipelinesStatus(rt, headers,
					instance.getUrl());
			final String body = response.getBody();
			final ArrayNode result = mapper.createArrayNode();

			// Filter response to return the status of pipelines allowed for the user.
			final ObjectNode status = (ObjectNode) mapper.readTree(body);
			final Iterator<Entry<String, JsonNode>> it = status.fields();
			while (it.hasNext()) {
				final Entry<String, JsonNode> pipelineStatus = it.next();
				final Pipeline pipeline = pipelineRepository.findByIdstreamsets(pipelineStatus.getKey());
				// Instance could have pipelines not stored in config
				if (pipeline != null && pipeline.getInstance().equals(instance)
						&& hasUserViewPermission(pipeline, user)) {
					result.add(pipelineStatus.getValue());
				}
			}

			return Optional.of(result);
		} catch (final IOException | ResourceAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public ResponseEntity<String> getPipelineConfiguration(String userId, String pipelineIdentification) {
		Pipeline pipeline = null;
		try {
			pipeline = getPipelineByIdentification(pipelineIdentification);
		} catch (final NotFoundException e) {
			throw new BadRequestException(e.getMessage() + ": " + pipelineIdentification);
		}
		final User user = getUserById(userId);
		checkUserViewPipelineAccess(pipeline, user);
		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final ResponseEntity<String> exportResponse = StreamsetsApiWrapper.pipelineConfiguration(rt, headers,
				pipeline.getInstance().getUrl(), pipeline.getIdstreamsets());
		if (exportResponse.getStatusCode() == HttpStatus.OK) {
			return exportResponse;
		} else {
			throw new BadRequestException("Error getting pipeline configuration from streamsets. Status: "
					+ exportResponse.getStatusCode() + " Response: " + exportResponse.getBody());
		}
	}

	@Override
	public ResponseEntity<String> exportPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserViewPipelineAccess(pipeline, user);
		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final ResponseEntity<String> exportResponse = exportPipeline(pipeline, headers);
		if (exportResponse.getStatusCode() == HttpStatus.OK) {
			return exportResponse;
		} else {
			throw new BadRequestException("Error exporting the pipeline in streamsets. Status: "
					+ exportResponse.getStatusCode() + " Response: " + exportResponse.getBody());
		}
	}

	private ResponseEntity<String> exportPipeline(Pipeline pipeline, HttpHeaders headers) {
		final String basePath = pipeline.getInstance().getUrl();
		return StreamsetsApiWrapper.pipelineExport(rt, headers, basePath, pipeline.getIdstreamsets());
	}

	@Override
	public ResponseEntity<String> importPipelineData(final String userId, final String pipelineIdentification,
			final String config, final boolean overwrite) {

		final User user = getUserById(userId);
		final HttpHeaders headers = getAuthorizationHeadersForUser(user);
		final DataflowInstance instance = getDataflowInstanceForUser(user);
		final ResponseEntity<String> response = importPipeline(pipelineIdentification, config, headers, instance);

		if (response.getStatusCode() == HttpStatus.OK) {
			final JSONObject createResponseObj = new JSONObject(response.getBody());
			final String pipelineId = createResponseObj.getJSONObject("pipelineConfig").getString(PIPELINEID);
			final Pipeline pipelineUpdated = updateDBPipeline(pipelineIdentification, pipelineId, user);

			if (pipelineUpdated != null) {
				return response;
			} else {
				throw new BadRequestException("Error creating the pipeline in configdb");
			}
		} else {
			throw new BadRequestException("Error importing the pipeline in streamsets. Status: "
					+ response.getStatusCode() + " Response: " + response.getBody());
		}

	}

	@Override
	public ResponseEntity<String> importPipeline(final String userId, final String pipelineIdentification,
			final String config, final boolean overwrite) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
		if (pipeline != null) {
			if (overwrite) {
				return updatePipeline(userId, pipelineIdentification, config);
			} else {
				throw new ClientErrorException(
						"The pipeline with identification [" + pipelineIdentification + "] already exists",
						Response.Status.CONFLICT);
			}
		} else {
			final User user = getUserById(userId);
			final HttpHeaders headers = getAuthorizationHeadersForUser(user);
			final DataflowInstance instance = getDataflowInstanceForUser(user);
			final ResponseEntity<String> response = importPipeline(pipelineIdentification, config, headers, instance);

			log.debug(response.getBody());
			if (response.getStatusCode() == HttpStatus.OK) {
				final JSONObject createResponseObj = new JSONObject(response.getBody());
				final String pipelineId = createResponseObj.getJSONObject("pipelineConfig").getString(PIPELINEID);
				final Pipeline pipelineCreated = saveDBPipeline(pipelineIdentification, pipelineId, user);

				if (pipelineCreated != null) {
					return response;
				} else {
					throw new BadRequestException("Error creating the pipeline in configdb");
				}
			} else {
				throw new BadRequestException("Error importing the pipeline in streamsets. Status: "
						+ response.getStatusCode() + " Response: " + response.getBody());
			}
		}
	}

	private ResponseEntity<String> importPipeline(final String pipelineIdentification, final String config,
			final HttpHeaders headers, final DataflowInstance instance) {
		final JSONObject modifiedConfig = new JSONObject(config);
		modifiedConfig.getJSONObject("pipelineConfig").put("title", pipelineIdentification);

		return StreamsetsApiWrapper.pipelineImport(rt, headers, instance.getUrl(), pipelineIdentification, false, true,
				modifiedConfig.toString());
	}

	@Override
	public ResponseEntity<String> updatePipeline(String userId, String pipelineIdentification, String config) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserEditPipelineAccess(pipeline, user);

		final ResponseEntity<String> statusPipelineResponse = statusPipeline(userId, pipelineIdentification);
		if (statusPipelineResponse.getStatusCode() != HttpStatus.OK) {
			throw new BadRequestException("The pipeline does not exist in streamsets. Status: "
					+ statusPipelineResponse.getStatusCode() + " Response: " + statusPipelineResponse.getBody());
		} else {
			final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
			final String basePath = pipeline.getInstance().getUrl();
			final ResponseEntity<String> response = StreamsetsApiWrapper.pipelineImport(rt, headers, basePath,
					pipeline.getIdstreamsets(), true, false, config);
			if (response.getStatusCode() == HttpStatus.OK) {
				return response;
			} else {
				throw new BadRequestException("Error creating the pipeline in streamsets. Status: "
						+ response.getStatusCode() + " Response: " + response.getBody());
			}
		}
	}

	@Override
	public ResponseEntity<String> clonePipeline(String userId, String pipelineIdentificationOri,
			String pipelineIdentificationDest) {
		final Pipeline pipeLineDest = pipelineRepository.findByIdentification(pipelineIdentificationDest);
		if (pipeLineDest == null) {
			final ResponseEntity<String> exportResponse = exportPipeline(userId, pipelineIdentificationOri);
			final JSONObject configOriObject = new JSONObject(exportResponse.getBody());
			return importPipeline(userId, pipelineIdentificationDest, configOriObject.toString(), false);
		} else {
			throw new BadRequestException(
					"The pipeline [" + pipelineIdentificationDest + "] already exists in the platform");
		}
	}

	@Override
	public ResponseEntity<String> pipelines(String userId, String filterText, String label, int offset, int len,
			String orderBy, String order, boolean includeStatus) {
		final User user = getUserById(userId);
		final HttpHeaders headers = getAuthorizationHeadersForUser(user);
		final ObjectNode result = mapper.createObjectNode();

		if (user.isAdmin()) {
			final List<DataflowInstance> instances = getAllDataflowInstances();

			for (final DataflowInstance instance : instances) {
				setPipelinesToResult(userId, filterText, label, offset, len, orderBy, order, includeStatus, headers,
						result, instance);
			}
		} else {
			final DataflowInstance instance = getDataflowInstanceForUser(user);
			setPipelinesToResult(userId, filterText, label, offset, len, orderBy, order, includeStatus, headers, result,
					instance);
		}

		return new ResponseEntity<>(result.toString(), HttpStatus.OK);
	}

	private void setPipelinesToResult(String userId, String filterText, String label, int offset, int len,
			String orderBy, String order, boolean includeStatus, HttpHeaders headers, ObjectNode result,
			DataflowInstance instance) {
		final Optional<ArrayNode> pipelines = getPipelinesForInstance(instance, headers, filterText, label, offset, len,
				orderBy, order, userId, includeStatus);
		if (pipelines.isPresent()) {
			result.set(instance.getIdentification(), pipelines.get());
		} else {
			result.put(instance.getIdentification(), "Response from instance is not readable");
		}
	}

	private Optional<ArrayNode> getPipelinesForInstance(DataflowInstance instance, HttpHeaders headers,
			String filterText, String label, int offset, int len, String orderBy, String order, String userId,
			boolean includeStatus) {

		final ResponseEntity<String> response = StreamsetsApiWrapper.pipelines(rt, headers, instance.getUrl(),
				filterText, label, offset, len, orderBy, order, includeStatus);
		final String body = response.getBody();

		try {
			final ArrayNode result = mapper.createArrayNode();
			final ArrayNode nodes = (ArrayNode) mapper.readTree(body);

			// Filter response to return the status of pipelines allowed for the user.
			Iterator<JsonNode> it;
			Iterator<JsonNode> itStatus;

			if (includeStatus) { // json will be [[{pipe1},{pipe2}],[{status1},{status2}]]
				final Iterator<JsonNode> arrayIt = nodes.iterator();
				final JsonNode pipelines = arrayIt.next();
				it = pipelines.iterator();
				final JsonNode statuses = arrayIt.next();
				itStatus = statuses.iterator();
			} else { // json will be [{pipe1},{pipe2}]
				it = nodes.iterator();
				itStatus = null;
			}

			while (it.hasNext()) {
				final ObjectNode jsonPipeline = (ObjectNode) it.next();
				final JsonNode findValue = jsonPipeline.findValue(PIPELINEID);
				final String idStreamsets = findValue.asText();

				final Pipeline pipeline = pipelineRepository.findByIdstreamsets(idStreamsets);
				if (pipeline != null) {
					if (hasUserViewPermission(pipeline, userId)) {
						if (includeStatus) {
							final JsonNode status = itStatus.next();
							jsonPipeline.set("OP_pipelineStatus", status);
						}
						result.add(jsonPipeline);
					}
				}
			}

			return Optional.of(result);
		} catch (final IOException e) {
			return Optional.empty();
		}
	}

	@Override
	public ResponseEntity<String> metricsPipeline(String userId, String identification) {
		final Pipeline pipeline = getPipelineByIdentification(identification);
		final User user = getUserById(userId);
		checkUserViewPipelineAccess(pipeline, user);

		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final String basePath = pipeline.getInstance().getUrl();

		return StreamsetsApiWrapper.pipelineMetrics(rt, headers, basePath, pipeline.getIdstreamsets());
	}

	@Override
	public PipelineUserAccess createUserAccess(String pipelineId, String ownerId, String accessType, String userId) {
		final User owner = getUserById(ownerId);
		final User user = getUserById(userId);
		final Pipeline pipeline = getPipelineById(pipelineId);
		checkUserIsOwner(pipeline, owner);
		if (accessType != null && !accessType.equals("")) {
			final PipelineUserAccessType pipelineUserAccessType = pipelineUserAccessTypeRepository.findById(accessType)
					.orElse(null);
			if (pipelineUserAccessType != null) {
				final PipelineUserAccess pipelineUserAccess = new PipelineUserAccess();
				pipelineUserAccess.setPipeline(pipeline);
				pipelineUserAccess.setUser(user);
				pipelineUserAccess.setPipelineUserAccessType(pipelineUserAccessType);
				return pipelineUserAccessRepository.save(pipelineUserAccess);
			} else {
				throw new BadRequestException("Access type " + accessType + " not found");
			}
		} else {
			throw new BadRequestException("Access type is null or empty");
		}
	}

	@Override
	public Pipeline changePublic(String pipelineId, String userId) {
		final Pipeline pipeline = getPipelineById(pipelineId);
		final User user = getUserById(userId);
		checkUserIsOwner(pipeline, user);
		pipeline.setPublic(!pipeline.isPublic());
		return pipelineRepository.save(pipeline);
	}

	@Override
	public void deleteUserAccess(String pipelineUserAccessId, String userId) {
		final User user = getUserById(userId);
		final PipelineUserAccess userAccess = pipelineUserAccessRepository.findById(pipelineUserAccessId).orElse(null);
		if (userAccess != null) {
			if (userAccess.getPipeline() == null) {
				throw new NotFoundException("Pipeline not found");
			} else {
				checkUserIsOwner(userAccess.getPipeline(), user);
				pipelineUserAccessRepository.deleteById(pipelineUserAccessId);
			}
		} else {
			throw new NotFoundException("User access not found");
		}
	}

	private void metricsManagerLogControlPanelDataflowsCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelDataflowsCreation(userId, result);
		}
	}

	@Override
	public String getVersion() {
		// Only supported one version for now
		return serviceConfiguration.getDataflowVersion();
	}

	/* DATAFLOW INSTANCES */

	@Override
	public List<DataflowInstance> getAllDataflowInstances() {
		return instancesRepository.findAll();
	}

	@Override
	public DataflowInstance getDefaultDataflowInstance() {
		return defaultinstance;
	}

	@Override
	public void setDefaultDataflowInstance(final DataflowInstance newDataflowInstance) {
		defaultinstance = newDataflowInstance;
	}

	@Override
	public DataflowInstance getDataflowInstanceByIdentification(final String identification) {
		if (identification == null || identification.trim().isEmpty()) {
			log.error("Instance identification is null or empty");
			throw new BadRequestException("Instance identification is null or empty");
		}
		final DataflowInstance instance = instancesRepository.findByIdentification(identification);
		if (instance == null) {
			log.error("Instance {} not found", identification);
			throw new NotFoundException("Instance " + identification + " not found in repository");
		} else {
			return instance;
		}
	}

	@Override
	public DataflowInstance getDataflowInstanceById(final String id) {
		if (id == null || id.trim().isEmpty()) {
			log.error("Instance id is null or empty");
			throw new BadRequestException("Instance id is null or empty");
		}
		final DataflowInstance instance = instancesRepository.findById(id).orElse(null);
		if (instance == null) {
			log.error("Instance {} not found", id);
			throw new NotFoundException("Instance " + id + " not found in repository");
		} else {
			return instance;
		}
	}

	@Override
	public DataflowInstance getDataflowInstanceForUserId(final String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			log.error("User id is null or empty");
			throw new BadRequestException("User id is null or empty");
		}
		final User user = getUserById(userId);
		return getDataflowInstanceForUser(user);
	}

	private DataflowInstance getDataflowInstanceForUser(final User user) {
		final DataflowInstance instance = instancesRepository.findByUser(user);
		if (instance == null) {
			return defaultinstance;
		} else {
			return instance;
		}
	}

	@Override
	public DataflowInstance createDataflowInstance(final InstanceBuilder instanceBuilder) {
		final DataflowInstance instanceFromCreate = instanceBuilder.buildInstance();
		getUserById(instanceFromCreate.getUser().getUserId()); // Check user exist

		// Check credentials are set on create
		if (instanceFromCreate.getAdminCredentials() == null || instanceFromCreate.getUserCredentials() == null
				|| instanceFromCreate.getGuestCredentials() == null) {
			throw new BadRequestException("All user credentials must be set on create");
		}

		// Set default instance
		if (instanceFromCreate.isDefaultInstance()) {
			updateDefaultDataflowInstance(instanceFromCreate);
			instanceFromCreate.setUser(null);
		}

		return instancesRepository.save(instanceFromCreate);
	}

	@Override
	public DataflowInstance updateDataflowInstance(final String id, final InstanceBuilder instanceBuilder) {
		final DataflowInstance instance = getDataflowInstanceById(id);
		final DataflowInstance instanceFromUpdate = instanceBuilder.buildInstance();

		instance.setIdentification(instanceFromUpdate.getIdentification());
		instance.setUrl(instanceFromUpdate.getUrl());

		// Don't update instance credentials if they are null
		if (instanceFromUpdate.getAdminCredentials() != null) {
			instance.setAdminCredentials(instanceFromUpdate.getAdminCredentials());
		}
		if (instanceFromUpdate.getUserCredentials() != null) {
			instance.setUserCredentials(instanceFromUpdate.getUserCredentials());
		}
		if (instanceFromUpdate.getGuestCredentials() != null) {
			instance.setGuestCredentials(instanceFromUpdate.getGuestCredentials());
		}

		// Set default instance
		if (instanceFromUpdate.isDefaultInstance()) {
			updateDefaultDataflowInstance(instance);

			instance.setUser(null);
			instance.setDefaultInstance(true);
		} else {
			if (instance.isDefaultInstance()) {
				throw new ClientErrorException("You can't set a user to a default instance",
						org.apache.http.HttpStatus.SC_CONFLICT);
			} else {
				// Set user
				final User user;
				if (instanceFromUpdate.getUser() != null && !instanceFromUpdate.getUser().getUserId().isEmpty()) {
					user = getUserById(instanceFromUpdate.getUser().getUserId());
				} else {
					user = null;
				}
				instance.setUser(user);
			}
		}

		return instancesRepository.save(instance);
	}

	private void updateDefaultDataflowInstance(DataflowInstance newDefaultInstance) {
		final DataflowInstance defaultInstance = getDefaultDataflowInstance();
		// If its the same we do not persist in the DB as it's going to be updated later
		if (!newDefaultInstance.getId().equalsIgnoreCase(defaultInstance.getId())) {
			defaultInstance.setDefaultInstance(false);
			instancesRepository.save(defaultInstance);
		}
		// Update the default instance with the new values
		setDefaultDataflowInstance(newDefaultInstance);
	}

	@Override
	public void deleteDataflowInstance(final DataflowInstance dataflowInstance, final RemoveInstanceAction action,
			final User user) {
		if (dataflowInstance.isDefaultInstance()) {
			throw new ClientErrorException(
					"This instance can not be deleted, is the default instance. Make other instance default first",
					org.apache.http.HttpStatus.SC_CONFLICT);
		} else {
			final List<Pipeline> pipelines = getPipelinesForInstance(dataflowInstance);
			switch (action) {
			case EXPORT:
				final HttpHeaders headersFrom = getAuthorizationHeadersForInstance(dataflowInstance);
				final HttpHeaders headersDefault = getAuthorizationHeadersForInstance(getDefaultDataflowInstance());

				for (final Pipeline pipeline : pipelines) {
					final String newStreamsetsId = movePipelineInInstances(pipeline, headersFrom, headersDefault);
					pipeline.setIdstreamsets(newStreamsetsId);
					pipeline.setInstance(getDefaultDataflowInstance());
				}

				pipelineRepository.saveAll(pipelines);
				break;
			case HARD_REMOVE:
				pipelineRepository.deleteAll(pipelines);
				break;
			case REMOVE:
			default:
				if (!pipelineRepository.findByInstance(dataflowInstance).isEmpty()) {
					throw new ClientErrorException(
							"This instance has pipelines associated. Hard remove it or export to the default instance.",
							org.apache.http.HttpStatus.SC_CONFLICT);
				} else {
					pipelineRepository.deleteAll(pipelines);
				}
				break;
			}
			instancesRepository.delete(dataflowInstance);
		}
	}

	private List<Pipeline> getPipelinesForInstance(DataflowInstance instance) {
		return pipelineRepository.findByInstance(instance);
	}

	private String movePipelineInInstances(final Pipeline pipeline, final HttpHeaders headersFrom,
			final HttpHeaders headersDefault) {
		final ResponseEntity<String> exportResponse = exportPipeline(pipeline, headersFrom);
		if (exportResponse.getStatusCode() == HttpStatus.OK) {
			final JSONObject configOriObject = new JSONObject(exportResponse.getBody());
			final ResponseEntity<String> importResponse = importPipeline(pipeline.getIdentification(),
					configOriObject.toString(), headersDefault, getDefaultDataflowInstance());

			if (importResponse.getStatusCode() != HttpStatus.OK) {
				throw new BadRequestException("Pipeline not imported in instance '"
						+ getDefaultDataflowInstance().getIdentification() + "' Status" + importResponse.getStatusCode()
						+ " Response: " + importResponse.getBody());
			} else {
				final JSONObject createResponseObj = new JSONObject(importResponse.getBody());
				return createResponseObj.getString(PIPELINEID);
			}
		} else {
			throw new BadRequestException(
					"Pipeline not exported from instance '" + getDefaultDataflowInstance().getIdentification()
					+ "' Status" + exportResponse.getStatusCode() + " Response: " + exportResponse.getBody());
		}
	}

	@Override
	public void deleteDataflowInstance(final String id, final String action, final String userId) {
		final DataflowInstance instance = getDataflowInstanceById(id);
		final RemoveInstanceAction actionType = RemoveInstanceAction.valueOf(action);
		final User user = getUserById(userId);
		deleteDataflowInstance(instance, actionType, user);
	}

	@Override
	public ResponseEntity<String> restartDataflowInstance(final String instanceId) {
		final DataflowInstance instance = getDataflowInstanceById(instanceId);
		final HttpHeaders headers = getAuthorizationHeadersForInstance(instance);
		return StreamsetsApiWrapper.restartInstance(rt, headers, instance.getUrl());
	}

	@Override
	public List<User> getFreeAnalyticsUsers() {
		final List<User> usersInInstances = getAllDataflowInstances().stream().map(DataflowInstance::getUser)
				.filter(Objects::nonNull).collect(Collectors.toList());

		return userRepository.findAll().stream().filter(User::isActive).filter(this::userRoleFilterNewInstance)
				.filter(user -> !isUserAssigned(user, usersInInstances)).collect(Collectors.toList());
	}

	private boolean userRoleFilterNewInstance(User user) {
		return user.isAdmin() || userService.isUserAnalytics(user);
	}

	private boolean isUserAssigned(final User user, final List<User> users) {
		return users.stream().anyMatch(userInstance -> userInstance.equals(user));
	}

	@Override
	public List<Pipeline> getPipelinesForListWithProjectsAccess(String userId) {

		final User user = userRepository.findByUserId(userId);
		final List<PipelineForList> pipelineForLists = pipelineRepository.findAllPipelineList();
		if (!user.isAdmin()) {
			securityService.setSecurityToInputList(pipelineForLists, user, "Pipeline");
		}
		final List<Pipeline> pipelineList = new ArrayList<>();
		for (final PipelineForList p: pipelineForLists) {
			if (p.getAccessType() != null) {
				final Pipeline pipeline = getPipelineById(p.getId());
				pipelineList.add(pipeline);
			}
		}

		return pipelineList;
	}

	@Override
	public ResponseEntity<String> getPipelineCommittedOffsets(String userId, String pipelineIdentification) {
		final Pipeline pipeline = getPipelineByIdentification(pipelineIdentification);
		final User user = getUserById(userId);
		checkUserViewPipelineAccess(pipeline, user);

		final HttpHeaders headers = getAuthorizationHeadersForPipelineAndUser(pipeline, user);
		final String basePath = pipeline.getInstance().getUrl();
		final ResponseEntity<String> response = StreamsetsApiWrapper.getCommittedOffsets(rt, headers, basePath, pipeline.getIdstreamsets());

		if (response.getStatusCode() == HttpStatus.OK) {
			return response;
		} else {
			throw new BadRequestException("Error getting pipeline committed offsets from streamsets. Status: "
					+ response.getStatusCode() + " Response: " + response.getBody());
		}
	}

	@Override
	public List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification) {
		final User user = getUserById(userId);
		if (user.isAdmin()) {
			return pipelineRepository.findAllDto(identification);
		} else {
			return pipelineRepository.findDtoByUserAndPermissions(user, identification);
		}
	}


}
