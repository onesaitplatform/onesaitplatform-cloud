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
package com.minsait.onesait.platform.config.services.dataflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.PipelineUserAccessType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.PipelineRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessRepository;
import com.minsait.onesait.platform.config.repository.PipelineUserAccessTypeRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.dataflow.configuration.DataflowServiceConfiguration;
import com.minsait.onesait.platform.config.services.exceptions.DataflowServiceException;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataflowServiceImpl implements DataflowService {

	@Autowired
	private DataflowServiceConfiguration configuration;

	@Autowired
	private PipelineRepository pipelineRepository;

	@Autowired
	private PipelineUserAccessRepository pipelineUserAccessRepository;

	@Autowired
	private PipelineUserAccessTypeRepository pipelineUserAccessTypeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	@Qualifier("serviceClientRest")
	private RestTemplate rt;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	private String dataflowServiceUrl;

	@Value("${onesaitplatform.analytics.dataflow.streamsetsHostname:localhost}")
	private String dataflowHost;

	@Value("${onesaitplatform.analytics.dataflow.streamsetsPort:18630}")
	private String dataflowPort;

	@Value("${onesaitplatform.analytics.dataflow.StreamsetsProtocol:http}")
	private String dataflowProtocol;

	@Value("${onesaitplatform.analytics.dataflow.version:3.10.0}")
	private String dataflowVersion;

	@Value("${onesaitplatform.services.dataflow.user:admin}")
	private String dataflowUser;

	@Value("${onesaitplatform.services.dataflow.pass:admin}")
	private String dataflowPass;

	private static final String WITH_NAME_STR = " with name: ";
	private static final String DATAFLOW_HEADER = "X-Streamsets-ID";
	private static final String UTF8_STR = "UTF-8";
	private static final String PIPELINEID = "pipelineId";

	public enum PipelineTypes {
		DATA_COLLECTOR, MICROSERVICE, DATA_COLLECTOR_EDGE
	}

	private final ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		final StringBuilder sb = new StringBuilder();
		sb.append(dataflowProtocol);
		sb.append("://");
		sb.append(dataflowHost);
		sb.append(":");
		sb.append(dataflowPort);
		dataflowServiceUrl = sb.toString();
	}

	private String encryptRestUserpass(User user, String dataflowId) {

		String key = "";

		if (user != null && user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			key = configuration.getDataflowAdminUsername() + ":" + configuration.getDataflowAdminPass();
		}

		if (user == null) {// Create case always "manage"
			key = configuration.getDataflowUsername() + ":" + configuration.getDataflowPass();
		}

		if (user != null && StringUtils.isEmpty(key) && !StringUtils.isEmpty(dataflowId)) {

			final Pipeline pipeline = pipelineRepository.findByIdstreamsets(dataflowId);
			if (pipeline != null && (pipeline.getUser().equals(user) || hasUserPermissionInPipeline(pipeline, user)))
				key = configuration.getDataflowUsername() + ":" + configuration.getDataflowPass();

			// check project access to dataflow
			if (StringUtils.isEmpty(key) && pipeline != null) {
				key = getKeyAuthFromProjectAccess(user, pipeline);
			}

		}

		// Default guest
		if (StringUtils.isEmpty(key))
			key = configuration.getDataflowGuest() + ":" + configuration.getDataflowGuestPass();
		final String encryptedKey = new String(Base64.encode(key.getBytes()), StandardCharsets.UTF_8);
		key = "Basic " + encryptedKey;
		return key;
	}

	private String getKeyAuthFromProjectAccess(User user, Pipeline pipeline) {
		String key = "";
		final ResourceAccessType rat = resourceService.getResourceAccess(user.getUserId(), pipeline.getId());
		if (null != rat) {
			switch (rat) {
			case MANAGE:
				key = configuration.getDataflowUsername() + ":" + configuration.getDataflowPass();
				break;
			case VIEW:
			default:
				key = configuration.getDataflowGuest() + ":" + configuration.getDataflowGuestPass();
				break;
			}
		}
		return key;
	}

	private Pipeline sendStreamsetsCreatePut(String path, String name, String user) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final List<String> lHeaderRequestBy = new ArrayList<>();
		lHeaderRequestBy.add("OnesaitPlatform");
		headers.put("X-Requested-By", lHeaderRequestBy);
		headers.add(DATAFLOW_HEADER, "");
		String idstreamsets;
		ResponseEntity<String> responseEntity;

		log.info("Creating pipeline for user: " + user + WITH_NAME_STR + name);

		try {
			responseEntity = sendHttp(path, HttpMethod.PUT, "", headers, null);
		} catch (final Exception e) {
			log.error("Exception in PUT in creation PUT");
			metricsManagerLogControlPanelDataflowsCreation(user, "KO");
			throw e;
		}

		final int statusCode = responseEntity.getStatusCodeValue();

		if (statusCode / 100 != 2) {
			log.error("Exception executing creation PUT, status code: " + statusCode);
			metricsManagerLogControlPanelDataflowsCreation(user, "KO");
			throw new DataflowServiceException("Exception executing creation PUT, status code: " + statusCode);
		}

		try {
			final JSONObject createResponseObj = new JSONObject(responseEntity.getBody());
			idstreamsets = createResponseObj.getString(PIPELINEID);
		} catch (final JSONException e) {
			log.error("Exception parsing answer in create post");
			metricsManagerLogControlPanelDataflowsCreation(user, "KO");
			throw new DataflowServiceException("Exception parsing answer in create post: ", e);
		}

		final Pipeline pl = saveDBPipeline(name, idstreamsets, user);
		log.info("Pipeline for user: " + user + WITH_NAME_STR + name + ", successfully created");

		metricsManagerLogControlPanelDataflowsCreation(user, "OK");
		return pl;
	}

	private Pipeline saveDBPipeline(String identification, String idStreamsets, String user) {
		final Pipeline pl = new Pipeline();
		pl.setIdentification(identification);
		pl.setIdstreamsets(idStreamsets);
		pl.setUser(userRepository.findByUserId(user));
		return pipelineRepository.save(pl);
	}

	@Override
	public Pipeline createPipeline(String name, PipelineTypes type, String description, String userId)
			throws UnsupportedEncodingException {
		if(pipelineRepository.findByIdentification(name) != null) {
			throw new IllegalArgumentException("Pipeline already exists in database");
		} else {
			return sendStreamsetsCreatePut("rest/v1/pipeline/" + UriUtils.encode(name, UTF8_STR)	+ "?autoGeneratePipelineId=true&pipelineType="+type.toString()+"&description=" + description, name, userId);
		}
	}

	@Override
	public void removePipeline(String id, String userId) {
		ResponseEntity<String> responseEntity;
		final Pipeline pl = pipelineRepository.findByIdstreamsets(id);
		if (resourceService.isResourceSharedInAnyProject(pl))
			throw new OPResourceServiceException(
					"This DataFlow is shared within a Project, revoke access from project prior to deleting");
		final String name = pl.getIdentification();
		final User user = userRepository.findByUserId(userId);
		log.info("Delete pipeline for user: " + user + WITH_NAME_STR + name);

		if (hasUserEditPermission(pl.getIdstreamsets(), userId)) {

			responseEntity = sendHttp("rest/v1/pipeline/" + pl.getIdstreamsets(), HttpMethod.DELETE, "", userId, id);

			final HttpStatus statusCode = responseEntity.getStatusCode();

			if (statusCode != HttpStatus.OK && statusCode != HttpStatus.ACCEPTED) {
				log.error("Exception executing delete pipeline, status code: " + responseEntity.getStatusCodeValue());
				throw new DataflowServiceException(
						"Exception executing delete pipeline, status code: " + responseEntity.getStatusCodeValue());
			}

			pipelineRepository.delete(pl);
			log.info("Pipeline for user: " + user + WITH_NAME_STR + name + ", successfully deleted");
		} else {
			log.error("Exception executing delete pipeline, permission denied");
			throw new DataflowServiceException("Error delete pipeline, permission denied");
		}
	}

	@Override
	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, Object body,
			String user) {
		return sendHttp(
				requestServlet.getServletPath()
						+ (requestServlet.getQueryString() != null ? "?" + requestServlet.getQueryString() : ""),
				httpMethod, body, user, requestServlet.getContentType() == null ? MediaType.APPLICATION_JSON.toString()
						: requestServlet.getContentType(),
				requestServlet.getHeader(DATAFLOW_HEADER));
	}

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, Object body, String user,
			String contentType, String dataflowId) {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		headers.add(DATAFLOW_HEADER, dataflowId);
		return sendHttp(url, httpMethod, body, headers, user);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, Object body, String user,
			String dataflowId)  {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(DATAFLOW_HEADER, dataflowId);
		return sendHttp(url, httpMethod, body, headers, user);
	}

	@Override
	public ResponseEntity<byte[]> sendHttpBinary(HttpServletRequest requestServlet, HttpMethod httpMethod, String body,
			String user) throws URISyntaxException, IOException {
		return sendHttpBinary(requestServlet.getServletPath(), httpMethod, body, user,
				requestServlet.getHeader(DATAFLOW_HEADER));
	}

	@Override
	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, String user,
			String dataflowId) throws URISyntaxException, IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(DATAFLOW_HEADER, dataflowId);
		return sendHttpBinary(url, httpMethod, body, headers, user);
	}

	@Override
	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, Object body, HttpHeaders headers,
			String userId) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		final User user = userId != null ? userRepository.findByUserId(userId) : null;

		headers.add("Authorization", encryptRestUserpass(user, headers.getFirst(DATAFLOW_HEADER)));

		if (body instanceof MultipartFile) {
			try {
				final MultipartFile file = (MultipartFile) body;
				final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
				fileMap.add(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"file\"; filename=\""+file.getOriginalFilename()+"\"");
				final HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);

				final MultiValueMap<String, Object> newBody = new LinkedMultiValueMap<>();
				newBody.add("file", fileEntity);

				body = newBody;
			} catch (final Exception e) {
				log.error(e.getMessage());
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}

		final List<String> lHeaderRequestBy = new ArrayList<>();
		lHeaderRequestBy.add("OnesaitPlatform");
		headers.put("X-Requested-By", lHeaderRequestBy);

		final HttpEntity<Object> request = new HttpEntity<>(body, headers);

		log.debug("Sending method " + httpMethod.toString() + " Dataflow");
		try {
			log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Dataflow");
			return restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(url.toLowerCase().indexOf("rest"))), httpMethod,
					request, String.class);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, HttpHeaders headers,
			String userId) throws URISyntaxException, IOException {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final User user = userRepository.findByUserId(userId);
		headers.add("Authorization", encryptRestUserpass(user, headers.getFirst(DATAFLOW_HEADER)));

		final org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body,
				headers);
		log.debug("Sending method " + httpMethod.toString() + " Dataflow");
		ResponseEntity<byte[]> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
		try {
			response = restTemplate.exchange(
					new URI(configuration.getBaseURL() + url.substring(url.toLowerCase().indexOf("rest"))), httpMethod,
					request, byte[].class);
		} catch (final Exception e) {
			log.error(e.getMessage());
		}
		log.debug("Execute method " + httpMethod.toString() + " '" + url + "' Dataflow");
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", response.getHeaders().getContentType().toString());
		return new ResponseEntity<>(response.getBody(), responseHeaders,
				HttpStatus.valueOf(response.getStatusCode().value()));
	}

	@Override
	public Pipeline getPipeline(String identification, String userId) {
		final Pipeline pl = pipelineRepository.findByIdentification(identification);
		if (hasUserViewPermission(pl.getIdstreamsets(), userId)) {
			return pl;
		} else {
			return null;
		}
	}

	@Override
	public List<Pipeline> getPipelines(String userId) {
		final User user = userRepository.findByUserId(userId);
		if (!user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			return pipelineRepository.findByUserAndAccess(user);
		} else {
			return pipelineRepository.findAll();
		}
	}

	private boolean hasUserPermissionInPipeline(Pipeline pl, String userId) {
		final User user = userRepository.findByUserId(userId);
		return hasUserPermissionInPipeline(pl, user);
	}

	private boolean hasUserPermissionInPipeline(Pipeline pl, User user) {
		final PipelineUserAccessType pipelineUserAccessType = pipelineUserAccessTypeRepository
				.findById("ACCESS-TYPE-1");
		if (user == null || pl == null)
			return false;
		return user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| pl.getUser().getUserId().equals(user.getUserId())
				|| pipelineUserAccessRepository.findByPipelineAndUserAndAccess(pl, user, pipelineUserAccessType) != null
				|| pl.isPublic();
	}

	@Override
	public boolean hasUserPermissionForPipeline(String pipelineId, String userId) {
		final Pipeline pl = pipelineRepository.findByIdstreamsets(pipelineId);
		if (pl != null)
			return this.hasUserPermissionInPipeline(pl, userId);
		return false;
	}

	@Override
	public boolean hasUserViewPermission(String pipelineId, String userId) {
		final Pipeline pl = pipelineRepository.findByIdstreamsets(pipelineId);
		return hasUserPermissionForPipeline(pipelineId, userId)
				|| resourceService.hasAccess(userId, pl.getId(), ResourceAccessType.VIEW);
	}

	@Override
	public boolean hasUserEditPermission(String pipelineId, String userId) {
		final Pipeline pl = pipelineRepository.findByIdstreamsets(pipelineId);
		return hasUserPermissionForPipeline(pipelineId, userId)
				|| resourceService.hasAccess(userId, pl.getId(), ResourceAccessType.MANAGE);
	}

	@Override
	public ResponseEntity<String> startPipeline(String userId, String pipelineIdentification, String parameters) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);

		if (pipeline != null && hasUserViewPermission(pipeline.getIdstreamsets(), userId)) {
			final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
					dataflowPass);
			return StreamsetsApiWrapper.pipelineStart(rt, headers, dataflowServiceUrl, pipeline.getIdstreamsets(),
					parameters);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public ResponseEntity<String> stopPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
		if (pipeline != null && hasUserViewPermission(pipeline.getIdstreamsets(), userId)) {
			final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
					dataflowPass);
			return StreamsetsApiWrapper.pipelineStop(rt, headers, dataflowServiceUrl, pipeline.getIdstreamsets());
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public ResponseEntity<String> statusPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
		final User user = userRepository.findByUserId(userId);
		if (pipeline != null && hasUserPermissionInPipeline(pipeline, user)) {
			final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
					dataflowPass);
			return StreamsetsApiWrapper.pipelineStatus(rt, headers, dataflowServiceUrl, pipeline.getIdstreamsets());
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public ResponseEntity<String> statusPipelines(String userId) {
		final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
				dataflowPass);

		final ResponseEntity<String> response = StreamsetsApiWrapper.pipelinesStatus(rt, headers, dataflowServiceUrl);
		final String body = response.getBody();
		try {
			final ObjectNode result = mapper.createObjectNode();

			// filter response to return the status of pipelines allowed for the user.
			final ObjectNode status = (ObjectNode) mapper.readTree(body);
			final Iterator<Entry<String, JsonNode>> it = status.fields();
			while (it.hasNext()) {
				final Entry<String, JsonNode> pipelineStatus = it.next();
				if (hasUserPermissionForPipeline(pipelineStatus.getKey(), userId)) {
					result.set(pipelineStatus.getKey(), pipelineStatus.getValue());
				}
			}

			return new ResponseEntity<>(result.toString(), HttpStatus.OK);

		} catch (final IOException e) {
			return new ResponseEntity<>("Unknown format in pipelines status response",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<String> exportPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
		final User user = userRepository.findByUserId(userId);
		if (pipeline != null && hasUserPermissionInPipeline(pipeline, user)) {
			final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,

					dataflowPass);
			return StreamsetsApiWrapper.pipelineExport(rt, headers, dataflowServiceUrl, pipeline.getIdstreamsets());
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public ResponseEntity<String> importPipeline(String userId, String pipelineIdentification, String config) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);

		if (pipeline != null) {
			return new ResponseEntity<>("The pipeline [" + pipelineIdentification + "] already exists",
					HttpStatus.CONFLICT);
		} else {
			final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
					dataflowPass);
			final JSONObject modifiedConfig = new JSONObject(config);
			modifiedConfig.getJSONObject("pipelineConfig").put("title", pipelineIdentification);
			final ResponseEntity<String> response = StreamsetsApiWrapper.pipelineImport(rt, headers, dataflowServiceUrl,
					pipelineIdentification, false, true, modifiedConfig.toString());
			log.debug(response.getBody());
			if (response.getStatusCode() == HttpStatus.OK) {
				final JSONObject createResponseObj = new JSONObject(response.getBody());
				final String pipelineId = createResponseObj.getJSONObject("pipelineConfig").getString(PIPELINEID);
				final Pipeline pipelineCreated = saveDBPipeline(pipelineIdentification, pipelineId, userId);

				if (pipelineCreated != null) {
					return response;
				} else {
					return new ResponseEntity<>("Error creating the pipeline in configdb",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				return new ResponseEntity<>("Error creating the pipeline in streamsets",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@Override
	public ResponseEntity<String> updatePipeline(String userId, String pipelineIdentification, String config) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
		final User user = userRepository.findByUserId(userId);


		if (pipeline == null) {
			return new ResponseEntity<>("The pipeline does not exist", HttpStatus.NOT_FOUND);
		} else {
			if (user != null && hasUserPermissionInPipeline(pipeline, user)) {
				final ResponseEntity<String> statusPipelineResponse = statusPipeline(user.getUserId(),
						pipelineIdentification);
				if (statusPipelineResponse.getStatusCode() != HttpStatus.OK) {
					return new ResponseEntity<>("The pipeline does not exist in streamsets", HttpStatus.NOT_FOUND);
				} else {
					final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(),
							dataflowUser, dataflowPass);
					final ResponseEntity<String> response = StreamsetsApiWrapper.pipelineImport(rt, headers,

							dataflowServiceUrl, pipeline.getIdstreamsets(), true, false, config);
					if (response.getStatusCode() == HttpStatus.OK) {
						return response;
					} else {
						return new ResponseEntity<>("Error creating the pipeline in streamsets",
								HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			} else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		}
	}

	@Override
	public ResponseEntity<String> clonePipeline(String userId, String pipelineIdentificationOri,
			String pipelineIdentificationDest) {
		final Pipeline pipeLineDest = pipelineRepository.findByIdentification(pipelineIdentificationDest);
		if (pipeLineDest == null) {
			final ResponseEntity<String> exportResponse = exportPipeline(userId, pipelineIdentificationOri);
			if (exportResponse.getStatusCode() == HttpStatus.OK) {
				final JSONObject configOriObject = new JSONObject(exportResponse.getBody());
				return importPipeline(userId, pipelineIdentificationDest, configOriObject.toString());
			} else {
				return exportResponse;
			}
		} else {
			return new ResponseEntity<>("The pipeline [" + pipelineIdentificationDest + "] already exists",
					HttpStatus.CONFLICT);
		}
	}
	
    @Override
    public ResponseEntity<String> pipelines(String userId, String filterText, String label, int offset, int len,
            String orderBy, String order, boolean includeStatus) {
        final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
                dataflowPass);
        final ResponseEntity<String> response = 
                StreamsetsApiWrapper.pipelines(rt, headers, dataflowServiceUrl, filterText, label, offset, len, orderBy, order, includeStatus);
        final String body = response.getBody();
        
        try {
            final ArrayNode result = mapper.createArrayNode();
            final ArrayNode nodes = (ArrayNode) mapper.readTree(body);

            // filter response to return the status of pipelines allowed for the user.
            Iterator<JsonNode> it;
            Iterator<JsonNode> itStatus;
            
            if (includeStatus) { // json will be [[{pipe1},{pipe2}],[{status1},{status2}]]
                Iterator<JsonNode> arrayIt = nodes.iterator();
                JsonNode pipelines = arrayIt.next();
                it = pipelines.iterator();
                JsonNode statuses = arrayIt.next();
                itStatus = statuses.iterator();
            } else { // json will be [{pipe1},{pipe2}]                
                it = nodes.iterator();
                itStatus = null;
            }
                                               
                       
            while (it.hasNext()) {
                final ObjectNode pipeline = (ObjectNode) it.next();
                JsonNode findValue = pipeline.findValue(PIPELINEID);
                String id = findValue.asText();
                
                if (hasUserPermissionForPipeline(id, userId)) {
                    if (includeStatus) {
                        JsonNode status = itStatus.next();
                        pipeline.set("OP_pipelineStatus", status);
                    }
                    result.add(pipeline);
                }
                
            }

            return new ResponseEntity<>(result.toString(), HttpStatus.OK);

        } catch (final IOException e) {
            return new ResponseEntity<>("Unknown format in pipelines status response",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ResponseEntity<String> metricsPipeline(String userId, String pipelineIdentification) {
        final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
        if (pipeline != null && hasUserViewPermission(pipeline.getIdstreamsets(), userId)) {
            final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
                    dataflowPass);
            return StreamsetsApiWrapper.pipelineMetrics(rt, headers, dataflowServiceUrl, pipeline.getIdstreamsets());
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

	@Override
	public ResponseEntity<String> resetOffsetPipeline(String userId, String pipelineIdentification) {
		final Pipeline pipeline = pipelineRepository.findByIdentification(pipelineIdentification);
		if (pipeline != null && hasUserEditPermission(pipeline.getIdstreamsets(), userId)) {
			final HttpHeaders headers = StreamsetsApiWrapper.setBasicAuthorization(new HttpHeaders(), dataflowUser,
					dataflowPass);
			return StreamsetsApiWrapper.resetOffset(rt, headers, dataflowServiceUrl, pipeline.getIdstreamsets());
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@Override
	public void createUserAccess(String dataflowId, String userId, String accessType) {
		if (dataflowId != null && !dataflowId.equals("") && userId != null && !userId.equals("") && accessType != null
				&& !accessType.equals("")) {
			final User user = userRepository.findByUserId(userId);
			final Pipeline pipeline = pipelineRepository.findByIdstreamsets(dataflowId);
			final PipelineUserAccessType pipelineUserAccessType = pipelineUserAccessTypeRepository.findById(accessType);

			final PipelineUserAccess pipelineUserAccess = new PipelineUserAccess();
			pipelineUserAccess.setPipeline(pipeline);
			pipelineUserAccess.setUser(user);
			pipelineUserAccess.setPipelineUserAccessType(pipelineUserAccessType);
			pipelineUserAccessRepository.save(pipelineUserAccess);

		}
	}

	@Override
	public void changePublic(Pipeline pipeline) {
		if (pipeline != null) {
			pipeline.setPublic(!pipeline.isPublic());
			pipelineRepository.save(pipeline);
		}
	}

	@Override
	public void deleteUserAccess(String pipelineUserAccessId) {
		pipelineUserAccessRepository.delete(pipelineUserAccessId);
	}

	private void metricsManagerLogControlPanelDataflowsCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelDataflowsCreation(userId, result);
		}
	}

	@Override
	public String getVersion() {
		return this.dataflowVersion;
	}



}
