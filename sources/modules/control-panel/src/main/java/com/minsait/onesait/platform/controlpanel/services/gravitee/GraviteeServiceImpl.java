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
package com.minsait.onesait.platform.controlpanel.services.gravitee;

import static com.minsait.onesait.platform.controlpanel.gravitee.dto.TransformHeadersPolicy.TRANSFORM_HEADERS_DESCRIPTION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.business.services.api.APIBusinessService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiCreate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiOauthResource;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPage;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageResponse;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageUpdate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPlan;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiStateSync;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiUpdate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.Application;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.CorsApi;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.FlowPath;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.FlowPath.PathOperator;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeApi;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeException;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GroovyPolicy;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.HttpHeader;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.IdentityProvider;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ImportSwaggerDescriptor;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ImportSwaggerDescriptor.Type;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.PathPolicy;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.TransformHeadersPolicy;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import io.swagger.models.Swagger;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(value = "gravitee.enable", havingValue = "true")
@Slf4j
public class GraviteeServiceImpl implements GraviteeService {

	private static final String ONESAITPLATFORM = "onesaitplatform";

	private static final String BEARER = "bearer ";

	private RestTemplate restTemplate;

	private static final String GRAVITEE_3_PREFIX = "/organizations/DEFAULT/environments/DEFAULT";
	private static final String OAUTH_EXCHANGE_TOKEN_URL = "/auth/oauth2/onesait-account/exchange";
	private static final String IDENTITIES_ENDPOINT = "/configuration/identities";
	private static final String IDENTITY_PROVIDER_ID = "onesait-account";

	private static final String APIS_ENDPOINT = "/apis";
	private static final String PLANS_ENDPOINT = "/plans";
	private static final String SUBCRIPTIONS_ENDPOINT = "/subscriptions/";
	private static final String PAGES_ENDPOINT = "/pages";
	private static final String DEPLOY_ENDPOINT = "/deploy";
	private static final String IMPORT_ENDPOINT = "/import";
	private static final String SWAGGER_ENDPOINT = "/swagger";

	private static final String TOKEN_PARAM = "token";
	private static final String ACTION_PARAM = "action";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String STOP = "stop";
	private static final String START = "start";

	private static final String STATE_UNPUBLISHED = "UNPUBLISHED";
	private static final String STATE_ARCHIVED = "ARCHIVED";

	private static final String REQUEST = "REQUEST";

	private static final String CORS = "cors";
	private static final String SSL = "ssl";
	private static final String HTTP_HEADER_REQUESTED_WITH = "X-Requested-With";

	private static final String APPLICATIONS_ENDPOINT = "/applications";

	@Value("${gravitee.headerValue:Gravitee-Server}")
	private String graviteeHeaderValue;
	@Value("${gravitee.clientId}")
	private String clientId;
	@Value("${gravitee.clientSecret}")
	private String clientSecret;
	@Value("${gravitee.corsEnabled:true}")
	private boolean corsEnabled;
	@Value("${onesaitplatform.authentication.oauth.osp-keycloak:true}")
	private boolean keycloakEnabled;
	@Value("${gravitee.version:1.30}")
	private String graviteeVersion;
	@Value("${gravitee.migrate-apis:false}")
	private boolean migrateApis;

	@Autowired
	private AppWebUtils utils;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private APIBusinessService apiBusinessService;
	@Autowired
	private AppService appService;

	@PostConstruct
	void initRestTemplate() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());
		restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter());
		try {
			recreateIdentityProvider();
		} catch (final Exception e) {
			log.error("Could not create identity provider", e);
		}
		if (migrateApis) {
			try {
				migrateApis();
			} catch (final Exception e) {
				log.error("Could not migrate apis", e);
			}
		}
	}

	private void recreateIdentityProvider() throws IOException {
		final JsonNode ip = oauthIdentityManagerExists();
		if (ip == null) {
			createDefaultIdentityProvider();

		} else {
			if (keycloakEnabled && !ip.get("configuration").get("tokenEndpoint").asText().contains("realms")) {
				updateIdentityProvider();
			} else if (!keycloakEnabled
					&& !ip.get("configuration").get("tokenEndpoint").asText().contains("oauth-server")) {
				updateIdentityProvider();
			}
		}
		createDefaultApplication();
	}

	private void migrateApis() {
		final List<ApiUpdate> apis = this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint(), HttpMethod.GET,
				new HttpEntity<>(adminHttpHeaders()), new ParameterizedTypeReference<List<ApiUpdate>>() {
				}).getBody();
		apis.forEach(a -> {
			migrateAPI(a, a.getId());
			try {
				deployApi(a.getId());
			} catch (final GenericOPException e) {
				log.error("Could not deploy api {}", a.getId());
			}
		});
	}

	@Override
	public void createUpdateIdentityProvider() {
		try {
			recreateIdentityProvider();
		} catch (final Exception e) {
			log.error("Could not create identity provider", e);
		}
	}

	@Override
	public GraviteeApi processApi(Api api, boolean jwtPlan, String clientId) throws GenericOPException {
		// Create DTO
		try {
			ApiUpdate apiUpdate = null;
			if (!api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
//				apiCreate = ApiCreate.createFromOPApi(api, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE)
//						.concat("server/api/v").concat(api.getNumversion() + "/").concat(api.getIdentification()));
				apiUpdate = getApiDTOFromSwaggerInternal(api);
//				apiUpdate = createApi(apiCreate);
			} else {
				apiUpdate = getApiDTOFromSwagger(api);
			}
			// make public
			apiUpdate.setVisibility("public");

			// Save id for later
			final String apiId = apiUpdate.getId();
			api.setGraviteeId(apiId);
			// if api is internal then create header policy
			if (!api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
//				final ArrayNode policies = mapper.createArrayNode();
//				if (apiUpdate.getPaths() == null) {
//					apiUpdate.setPaths(mapper.createObjectNode());
//				}
//				policies.add(createHttpHeaderPolicy());
//				((ObjectNode) apiUpdate.getPaths()).set("/", policies);
				apiUpdate.getPaths().forEach(p -> {
					((ArrayNode) p).add(createHttpHeaderPolicy());
				});
			} else {
				apiUpdate.getPaths().forEach(p -> {
					((ArrayNode) p).add(PathPolicy.builder()
							.groovy(GroovyPolicy
									.xOpApiKeyPolicy(resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE)))
							.build().toJsonNode());
					((ArrayNode) p).add(createHttpHeaderPolicy());
				});

			}
			// update public api
			updateApi(apiUpdate);

			// create Docs
			if (!api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
				final String url = resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON) + "v"
						+ api.getNumversion() + "/" + api.getIdentification() + "/swagger.json";
				api.setSwaggerJson(getText(url));
			}
			// createSwaggerDocPage(apiId, api);
			// updateSwagger
			final String swagger = apiBusinessService.getSwagger(api);
			processUpdateAPIDocs(api, swagger);
			// create plan
			createDefaultPlan(apiId);

			// start api & deploy
			startApi(apiId);
			// migrate api
			migrateAPI(apiUpdate, apiId);
			deployApi(apiId);

			if (jwtPlan && graviteeVersion.startsWith("3")) {
				createOauthPlan(apiId, clientId);
			}

			return GraviteeApi.builder().apiId(apiId).endpoint(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY) + ApiCreate.getApiContextPath(api))
					.build();
		} catch (final Exception e) {
			log.error("Something went wrong while publishing API to gravitee", e);
			throw e;
		}

	}

	private void migrateAPI(ApiUpdate apiUpdate, String apiId) {
		if (graviteeVersion.startsWith("3")) {
			try {
				executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
						+ apiId + "/_migrate", HttpMethod.POST, this.getRequestEntity(apiUpdate), ApiUpdate.class)
						.getBody();
			} catch (final Exception e) {
				log.warn("Could not migrate api {}", apiId);
			}
		}
	}

	@Override
	public ApiUpdate createApi(ApiCreate api) throws GenericOPException {

		return executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint(),
				HttpMethod.POST, this.getRequestEntity(api), ApiUpdate.class).getBody();

	}

	@Override
	public ApiUpdate updateApi(ApiUpdate api) throws GenericOPException {
		final String apiId = api.getId();
		api.setId(null);
		((ObjectNode) api.getProxy()).set(CORS, CorsApi.defaultCorsPolicy(corsEnabled).toJsonNode());

		((ArrayNode) api.getProxy().path("groups").path(0).path("endpoints"))
				.forEach(e -> ((ObjectNode) e).set(SSL, getSSLIgnoreConfig()));

		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId,
				HttpMethod.PUT, this.getRequestEntity(api), ApiUpdate.class).getBody();

	}

	@Override
	public ApiPlan createDefaultPlan(String apiId) throws GenericOPException {
		ApiPlan plan = null;
		try {
			plan = ApiPlan.defaultPlan(apiId);

		} catch (final IOException e) {
			throw new GraviteeException("Could not create default plan");
		}

		return executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
				+ apiId + PLANS_ENDPOINT, HttpMethod.POST, this.getRequestEntity(plan), ApiPlan.class).getBody();

	}

	@Override
	public void startApi(String apiId) throws GenericOPException {

		final UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId)
				.queryParam(ACTION_PARAM, START);

		executeRequest(uri.toUriString(), HttpMethod.POST, this.getRequestEntity(null), JsonNode.class);

	}

	@Override
	public void stopApi(String apiId) throws GenericOPException {

		final UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId)
				.queryParam(ACTION_PARAM, STOP);

		try {
			executeRequest(uri.toUriString(), HttpMethod.POST, this.getRequestEntity(null), JsonNode.class);
		} catch (final Exception e) {
			log.error("Error stopping API: {}", e.getMessage(), e);
		}

	}

	@Override
	public void deployApi(String apiId) throws GenericOPException {

		this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
				+ apiId + DEPLOY_ENDPOINT, HttpMethod.POST, this.getRequestEntity(null), JsonNode.class);
	}

	@Override
	public ApiUpdate getApi(String apiId) throws GenericOPException {
		final String url = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
				+ apiId;
		return this.executeRequest(url, HttpMethod.GET, this.getRequestEntity(null), ApiUpdate.class).getBody();
	}

	@Override
	public void deleteApi(String apiId) throws GenericOPException {
		stopApi(apiId);
		getApiPlans(apiId).stream().forEach(p -> {
			try {
				closePlan(apiId, p.getId());
			} catch (GraviteeException | GenericOPException e) {
				log.error("deleteApi", e);
			}
		});
		final String url = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
				+ apiId;
		executeRequest(url, HttpMethod.DELETE, this.getRequestEntity(null), JsonNode.class);
	}

	@Override
	public void changeLifeCycleState(String graviteeId, ApiStates state) throws GenericOPException {
		try {
			final ApiUpdate api = executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT)
					+ getAPIEndpoint() + "/" + graviteeId, HttpMethod.GET, getRequestEntity(null), ApiUpdate.class)
					.getBody();
			api.setId(null);
			api.setLifeCycleState(state.name());
			if (state.equals(ApiStates.DELETED)) {
				stopApi(graviteeId);
				getApiPlans(graviteeId).stream().forEach(p -> {
					try {
						deletePlan(graviteeId, p.getId());
					} catch (GraviteeException | GenericOPException e) {
						log.error("deleteApi", e);
					}
				});
				api.setLifeCycleState(STATE_UNPUBLISHED);
				executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
						+ graviteeId, HttpMethod.PUT, this.getRequestEntity(api), ApiUpdate.class);
				api.setLifeCycleState(STATE_ARCHIVED);
			}

			executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
					+ graviteeId, HttpMethod.PUT, this.getRequestEntity(api), ApiUpdate.class);
		} catch (final Exception e) {
			log.error("Error while updating gravitee api lifecycle state", e);
		}

	}

	@Override
	public void deletePlan(String apiId, String planId) throws GenericOPException {
		// delete subs
		this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
				+ apiId + PLANS_ENDPOINT + "/" + planId, HttpMethod.DELETE, this.getRequestEntity(null), String.class);

	}

	@Override
	public void closePlan(String apiId, String planId) throws GenericOPException {

		this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId
						+ PLANS_ENDPOINT + "/" + planId + "/_close",
				HttpMethod.POST, this.getRequestEntity(null), Object.class);

	}

	@Override
	public List<ApiPlan> getApiPlans(String apiId) throws GenericOPException {
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId
						+ PLANS_ENDPOINT,
				HttpMethod.GET, this.getRequestEntity(null), new ParameterizedTypeReference<List<ApiPlan>>() {
				}).getBody();
	}

	@Override
	public ApiPage createSwaggerDocPage(String apiId, Api api) throws GenericOPException {
		final ApiPage apiPage = ApiPage
				.defaultSwaggerDocPage(processSwaggerJSON(apiBusinessService.getSwagger(api), api));

		return this
				.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
						+ apiId + PAGES_ENDPOINT, HttpMethod.POST, this.getRequestEntity(apiPage), ApiPage.class)
				.getBody();
	}

	private <T> ResponseEntity<T> executeRequest(String url, HttpMethod method, HttpEntity<?> reqEntity,
			Class<T> responseType) {
		try {
			return restTemplate.exchange(url, method, reqEntity, responseType);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			throw new GraviteeException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}

	}

	private HttpHeaders getHeaders() {
		final String graviteeOauthToken = exchangeOauthToken();
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, BEARER_PREFIX.concat(graviteeOauthToken));
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		return headers;
	}

	private <T> HttpEntity<T> getRequestEntity(T body) {
		HttpEntity<T> reqEntity = null;
		if (null == body) {
			reqEntity = new HttpEntity<>(getHeaders());
		} else {
			reqEntity = new HttpEntity<>(body, getHeaders());
		}
		return reqEntity;
	}

	private String exchangeOauthToken() {

		String token = utils.getCurrentUserOauthToken();
		if (token.toLowerCase().startsWith(BEARER)) {
			token = token.substring(BEARER.length());
		}
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(
						resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getOauthExchangeEndpoint())
				.queryParam(TOKEN_PARAM, token);

		final ResponseEntity<JsonNode> response = this.executeRequest(uriBuilder.toUriString(), HttpMethod.POST, null,
				JsonNode.class);
		return response.getBody().get(TOKEN_PARAM).asText();

	}

	private <T> ResponseEntity<List<T>> executeRequest(String url, HttpMethod method, HttpEntity<Object> requestEntity,
			ParameterizedTypeReference<List<T>> parameterizedTypeReference) {
		try {
			return restTemplate.exchange(url, method, requestEntity, parameterizedTypeReference);

		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			throw new GraviteeException(
					"HttpResponse code : " + e.getStatusCode() + " , cause: " + e.getResponseBodyAsString());
		}

	}

	private ApiUpdate getApiDTOFromSwagger(Api api) {

		final ApiUpdate apiUpdate = this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + IMPORT_ENDPOINT
						+ SWAGGER_ENDPOINT,
				HttpMethod.POST,
				this.getRequestEntity(ImportSwaggerDescriptor.builder().withDocumentation(true).withPathMapping(true)
						.withPolicyPaths(true).type(Type.INLINE).payload(apiBusinessService.getSwagger(api)).build()),
				ApiUpdate.class).getBody();
		((ObjectNode) apiUpdate.getProxy()).put("context_path", ApiCreate.getApiContextPath(api));

		// Gravitee io v1.29+ virtual host support
		if (!apiUpdate.getProxy().path("virtual_hosts").isMissingNode()) {
			final JsonNode virtualHost = mapper.createObjectNode();
			((ObjectNode) virtualHost).put("path", ApiCreate.getApiContextPath(api));
			((ArrayNode) apiUpdate.getProxy().path("virtual_hosts")).removeAll();
			((ArrayNode) apiUpdate.getProxy().path("virtual_hosts")).add(virtualHost);
		}
		return apiUpdate;
	}

	private ApiUpdate getApiDTOFromSwaggerInternal(Api api) {

		final ApiUpdate apiUpdate = this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + IMPORT_ENDPOINT
						+ SWAGGER_ENDPOINT,
				HttpMethod.POST,
				this.getRequestEntity(ImportSwaggerDescriptor.builder().withDocumentation(true).withPathMapping(true)
						.withPolicyPaths(true).type(Type.INLINE).payload(apiBusinessService.getSwagger(api)).build()),
				ApiUpdate.class).getBody();
		((ObjectNode) apiUpdate.getProxy()).put("context_path", ApiCreate.getApiContextPath(api));

		// Gravitee io v1.29+ virtual host support
		if (!apiUpdate.getProxy().path("virtual_hosts").isMissingNode()) {
			final JsonNode virtualHost = mapper.createObjectNode();
			((ObjectNode) virtualHost).put("path", ApiCreate.getApiContextPath(api));
			((ArrayNode) apiUpdate.getProxy().path("virtual_hosts")).removeAll();
			((ArrayNode) apiUpdate.getProxy().path("virtual_hosts")).add(virtualHost);
		}
		return apiUpdate;
	}

	private JsonNode oauthIdentityManagerExists() {
		try {
			return this.executeRequest(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getIdentitiesEndpoint() + "/"
							+ IDENTITY_PROVIDER_ID,
					HttpMethod.GET, new HttpEntity<>(adminHttpHeaders()), JsonNode.class).getBody();
		} catch (final Exception e) {
			log.error("oauthIdentityManagerExists", e);
			return null;
		}
	}

	private void createDefaultIdentityProvider() throws IOException {
		try {
			this.executeRequest(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getIdentitiesEndpoint(),
					HttpMethod.POST,
					new HttpEntity<>(IdentityProvider.getFromString(getIdentityCreateResource()), adminHttpHeaders()),
					IdentityProvider.class);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			throw new GraviteeException("HttpResponse code for POST identity manager : " + e.getStatusCode()
					+ " , cause: " + e.getResponseBodyAsString());
		}
		updateIdentityProvider();
	}

	private void updateIdentityProvider() throws IOException {
		try {
			this.executeRequest(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getIdentitiesEndpoint() + "/"
							+ IDENTITY_PROVIDER_ID,
					HttpMethod.PUT,
					new HttpEntity<>(IdentityProvider.getFromString(getIdentityUpdateResource()), adminHttpHeaders()),
					IdentityProvider.class);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			throw new GraviteeException("HttpResponse code for PUT identity manager: " + e.getStatusCode()
					+ " , cause: " + e.getResponseBodyAsString());
		}
	}

	private JsonNode createHttpHeaderPolicy() {

		return PathPolicy.builder().enabled(true).policy(TransformHeadersPolicy.builder().scope(REQUEST)
				.removeHeaders(new String[] {})
				.addHeaders(new HttpHeader[] {
						HttpHeader.builder().name(HTTP_HEADER_REQUESTED_WITH).value(graviteeHeaderValue).build() })
				.build()).description(TRANSFORM_HEADERS_DESCRIPTION).build().toJsonNode();
	}

	private JsonNode getSSLIgnoreConfig() {
		try {
			return mapper.readTree("{\"trustAll\":true,\"hostnameVerifier\":false}");
		} catch (final Exception e) {
			log.error("Error parsing SSL ignore configuration");
			return mapper.createObjectNode();
		}
	}

	@Override
	public List<ApiPageResponse> getPublishedApiPages(String apiId) throws GenericOPException {
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId
						+ PAGES_ENDPOINT,
				HttpMethod.GET, getRequestEntity(null), new ParameterizedTypeReference<List<ApiPageResponse>>() {
				}).getBody();
	}

	@Override
	public ApiPageResponse updateApiPage(String apiId, String pageId, ApiPageUpdate apiPage) throws GenericOPException {
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/" + apiId
						+ PAGES_ENDPOINT + "/" + pageId,
				HttpMethod.PUT, this.getRequestEntity(apiPage), ApiPageResponse.class).getBody();
	}

	private String getText(String url) {
		try {
			final URL newURL = new URL(url);
			final HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();

			final int responseCode = connection.getResponseCode();
			InputStream inputStream;
			if (200 <= responseCode && responseCode <= 299) {
				inputStream = connection.getInputStream();
			} else {
				inputStream = connection.getErrorStream();
			}

			final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			final StringBuilder response = new StringBuilder();
			String currentLine;

			while ((currentLine = in.readLine()) != null) {
				response.append(currentLine).append('\r').append('\n');
			}

			in.close();

			return response.toString();
		} catch (final IOException e) {
			return "";
		}
	}

	@Override
	public ApiPageResponse processUpdateAPIDocs(Api api, String content) throws GenericOPException {
		final List<ApiPageResponse> list = getPublishedApiPages(api.getGraviteeId());
		if (!list.isEmpty()) {
			final String finalContent = processSwaggerJSON(content, api);
			final ApiPageResponse updateApiPage = list.stream()
					.filter(apr -> apr.getType().equals(ApiPage.Type.SWAGGER.name())
							&& apr.getName().equalsIgnoreCase("swagger"))
					.findFirst().orElse(null);
			if (updateApiPage != null) {
				final ApiPageUpdate apiPage = new ApiPageUpdate(finalContent, updateApiPage.getOrder(),
						updateApiPage.isHomepage(), true, updateApiPage.getName(), ApiPage.tryItFlag());
				apiPage.setContent(finalContent);
				return updateApiPage(updateApiPage.getApi(), updateApiPage.getId(), apiPage);
			}
		}
		return new ApiPageResponse();
	}

	private String processSwaggerJSON(String content, Api api) {
		final SwaggerParser swaggerParser = new SwaggerParser();
		final Swagger swagger = swaggerParser.parse(content);
		if (swagger != null) {
			// SWAGGER PARSER
			swagger.setHost(getGraviteeHost());
			swagger.setBasePath(getGraviteeBasePath(api));
			return Json.pretty(swagger);
		} else {
			// OPENAPI PARSER
			final OpenAPIParser openAPIParser = new OpenAPIParser();
			final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(content, null, null);
			final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
			final Server server = new Server();
			server.setUrl(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY) + getGraviteeBasePath(api));
			openAPI.setServers(Arrays.asList(server));
			return io.swagger.v3.core.util.Json.pretty(openAPI);
		}
	}

	private String getGraviteeBasePath(Api api) {
		return "/".concat(api.getIdentification().concat("/v").concat(String.valueOf(api.getNumversion())));
	}

	private String getGraviteeHost() {
		return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY).replaceAll("^(http://|https://)", "");
	}

	private String getAPIEndpoint() {
		if (graviteeVersion.startsWith("3")) {
			return GRAVITEE_3_PREFIX + APIS_ENDPOINT;
		} else {
			return APIS_ENDPOINT;
		}
	}

	private String getApplicationsEndpoint() {
		if (graviteeVersion.startsWith("3")) {
			return GRAVITEE_3_PREFIX + APPLICATIONS_ENDPOINT;
		} else {
			return APPLICATIONS_ENDPOINT;
		}
	}

	private String getIdentityCreateResource() {
		if (keycloakEnabled) {
			return IdentityProvider.DEFAULT_KEYCLOAK_RESOURCE_2_CREATE;
		} else {
			return IdentityProvider.DEFAULT_OAUTH_RESOURCE_2_CREATE;
		}

	}

	private String getIdentityUpdateResource() {
		if (graviteeVersion.startsWith("3")) {
			if (keycloakEnabled) {
				return IdentityProvider.DEFAULT_KEYCLOAK_RESOURCE_2_UPDATE_G3;
			} else {
				return IdentityProvider.DEFAULT_OAUTH_RESOURCE_2_UPDATE_G3;
			}
		} else {
			if (keycloakEnabled) {
				return IdentityProvider.DEFAULT_KEYCLOAK_RESOURCE_2_UPDATE;
			} else {
				return IdentityProvider.DEFAULT_OAUTH_RESOURCE_2_UPDATE;
			}

		}
	}

	private String getOauthExchangeEndpoint() {
		if (graviteeVersion.startsWith("3")) {
			return GRAVITEE_3_PREFIX + OAUTH_EXCHANGE_TOKEN_URL;
		} else {
			return OAUTH_EXCHANGE_TOKEN_URL;
		}
	}

	private String getIdentitiesEndpoint() {
		if (graviteeVersion.startsWith("3")) {
			return GRAVITEE_3_PREFIX + IDENTITIES_ENDPOINT;
		} else {
			return IDENTITIES_ENDPOINT;
		}
	}

	@Override
	public void updateApiFromSwagger(Api api) throws GenericOPException {
		final ApiUpdate prev = this.getApi(api.getGraviteeId());
		if (prev.getFlowMode().equalsIgnoreCase("default")) {
			final String swagger = apiBusinessService.getSwagger(api);
			final ApiUpdate apiUpdate = this.executeRequest(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT)
							+ getAPIEndpoint() + "/" + api.getGraviteeId() + IMPORT_ENDPOINT + SWAGGER_ENDPOINT,
					HttpMethod.PUT,
					this.getRequestEntity(ImportSwaggerDescriptor.builder().withDocumentation(true)
							.withPathMapping(true).withPolicyPaths(true).type(Type.INLINE).payload(swagger).build()),
					ApiUpdate.class).getBody();
			if (!apiUpdate.getProxy().path("virtual_hosts").isMissingNode()) {
				final JsonNode virtualHost = mapper.createObjectNode();
				((ObjectNode) virtualHost).put("path", ApiCreate.getApiContextPath(api));
				((ArrayNode) apiUpdate.getProxy().path("virtual_hosts")).removeAll();
				((ArrayNode) apiUpdate.getProxy().path("virtual_hosts")).add(virtualHost);
			}
			copyPrevPolicies(prev, apiUpdate);
			// Update api
			this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
					+ api.getGraviteeId(), HttpMethod.PUT, this.getRequestEntity(apiUpdate), ApiUpdate.class).getBody();
			processUpdateAPIDocs(api, swagger);
			deployApi(api.getGraviteeId());
		} else {
			final OpenAPI openAPI = apiBusinessService.getOpenAPI(api);
			final Set<String> currentPaths = openAPI.getPaths().keySet().stream()
					.map(s -> templatePathToGraviteePath(s)).collect(Collectors.toSet());
			prev.getFlows().removeIf(p -> !currentPaths.contains(p.getPathOperator().getPath()));
			final List<String> pathsToFlow = currentPaths.stream()
					.filter(p -> prev.getFlows().stream().noneMatch(fp -> fp.getPathOperator().getPath().equals(p)))
					.toList();
			pathsToFlow.forEach(pf -> {
				final FlowPath flowPath = new FlowPath();
				flowPath.setId(UUID.randomUUID().toString());
				flowPath.setConsumers(new ArrayList<>());
				flowPath.setEnabled(true);
				flowPath.setName(pf);
				flowPath.setMethods(new ArrayList<>());
				if (api.getApiType().equals(ApiType.INTERNAL_ONTOLOGY) || api.getApiType().equals(ApiType.NODE_RED)) {
					flowPath.setPre(List.of(TransformHeadersPolicy.getTHV3()));
				} else {
					flowPath.setPre(new ArrayList<>());
				}
				flowPath.setPost(new ArrayList<>());
				final PathOperator po = new PathOperator();
				po.setPath(pf);
				po.setOperator("STARTS_WITH");
				flowPath.setPathOperator(po);
				prev.getFlows().add(flowPath);
			});
			this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
					+ api.getGraviteeId(), HttpMethod.PUT, this.getRequestEntity(prev), ApiUpdate.class).getBody();
			processUpdateAPIDocs(api, apiBusinessService.getSwagger(api));
			deployApi(api.getGraviteeId());

		}
	}

	private void copyPrevPolicies(ApiUpdate prev, ApiUpdate current) {
		if (prev.getPaths() != null && prev.getPaths().size() > 0) {
			final Iterator<String> paths = prev.getPaths().fieldNames();
			while (paths.hasNext()) {
				final String path = paths.next();
				final Iterator<String> cPaths = current.getPaths().fieldNames();
				while (cPaths.hasNext()) {
					final String cPath = cPaths.next();
					if (cPath.equals(path)) {
						((ObjectNode) current.getPaths()).set(cPath, prev.getPaths().get(cPath));
					}
				}

			}
		} else if (prev.getFlows() != null && prev.getFlows().size() > 0) {
			// TO-DO if flow mode
		}
	}

	private String templatePathToGraviteePath(String path) {
		String graviteePath = path;
		final Pattern pattern = Pattern.compile("\\{([a-zA-Z]+)\\}");
		final Matcher matcher = pattern.matcher(path);
		while (matcher.find()) {
			graviteePath = graviteePath.replace(matcher.group(0), ":" + matcher.group(1));
		}
		return graviteePath;

	}

	private void createDefaultApplication() {
		final boolean exists = getApplications().stream().anyMatch(a -> a.getSettings() != null
				&& a.getSettings().getApp() != null && ONESAITPLATFORM.equals(a.getSettings().getApp().getClient_id()));
		if (exists) {
			log.info("Gravitee default application already exists");
		} else {
			createApplication(ONESAITPLATFORM);
		}
	}

	@Override
	public Application createApplication(String clientId) {
		final Application currentApp = getApplicationsByClientId(clientId);
		if (currentApp == null) {
			final Application app = Application.fromClientID(clientId);
			try {
				return this.executeRequest(
						resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getApplicationsEndpoint(),
						HttpMethod.POST, this.getRequestEntity(app), Application.class).getBody();
			} catch (final Exception e) {
				log.warn("Error creating application for clientId: {}", clientId, e);
				return null;
			}
		} else {
			return currentApp;
		}

	}

	@Override
	public List<Application> getApplications() {
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getApplicationsEndpoint(),
				HttpMethod.GET, new HttpEntity<>(adminHttpHeaders()),
				new ParameterizedTypeReference<List<Application>>() {
				}).getBody();
	}

	@Override
	public Application getApplicationsByClientId(String clientId) {
		return getApplications().stream().filter(app -> app.getSettings() != null && app.getSettings().getApp() != null
				&& clientId.equals(app.getSettings().getApp().getClient_id())).findFirst().orElse(null);
	}

	@Override
	// ONLY v3+
	public void createOauthPlan(String apiId, String clientId) {
		try {
			String clientSecret = ONESAITPLATFORM;
			if (!StringUtils.hasText(clientId)) {
				clientId = ONESAITPLATFORM;
			} else {
				if (!clientId.equals(ONESAITPLATFORM)) {
					clientSecret = appService.getAppListByIdentification(clientId).getSecret();
				}
			}
			final ApiUpdate api = getApi(apiId);
			if (api.getResources().isEmpty() || api.getResources().stream()
					.noneMatch(or -> or.getName().equals(ApiOauthResource.AUTH_SERVER_PRIMARY))) {
				api.getResources()
						.add(ApiOauthResource.defaultApiOauthResource(keycloakEnabled, clientId, clientSecret));
				this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint()
						+ "/" + apiId, HttpMethod.PUT, this.getRequestEntity(api), ApiUpdate.class).getBody();

				// CREATE API PLAN, CLOSE KEYLESS
				final List<ApiPlan> plans = getApiPlans(apiId);
				plans.stream()
						.filter(ap -> ap.getStatus().name().equalsIgnoreCase(ApiPlan.Status.PUBLISHED.name())
								&& ap.getSecurity().name().equalsIgnoreCase(ApiPlan.Security.KEY_LESS.name()))
						.forEach(ap -> {
							try {
								closePlan(apiId, ap.getId());
							} catch (final Exception e) {
								log.error("Could not close Keyless plan");
							}
						});
				ApiPlan oauthPlan = ApiPlan.oauthPlan();
				if (plans.stream()
						.noneMatch(ap -> ap.getStatus().name().equalsIgnoreCase(ApiPlan.Status.PUBLISHED.name())
								&& ap.getSecurity().name().equalsIgnoreCase(ApiPlan.Security.OAUTH2.name()))) {
					oauthPlan = this.executeRequest(
							resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
									+ apiId + PLANS_ENDPOINT,
							HttpMethod.POST, this.getRequestEntity(oauthPlan), ApiPlan.class).getBody();

				}
				// SUBSCRIBE WITH APP
				subscribeToAPIPlan(apiId, clientId, oauthPlan);

			}
		} catch (final Exception e) {
			log.error("Could not create oauth plan", e);
		}

	}

	@Override
	public boolean hasJWTPlan(String apiId) {
		try {
			return getApiPlans(apiId).stream()
					.anyMatch(ap -> ap.getStatus().name().equalsIgnoreCase(ApiPlan.Status.PUBLISHED.name())
							&& ap.getSecurity().name().equalsIgnoreCase(ApiPlan.Security.OAUTH2.name()));
		} catch (final GenericOPException e) {
			log.error("Error while checking JWT Plan", e);
			return false;
		}

	}

	@Override
	public List<String> getApplicationsSubscribedToAPI(String apiId) {
		final JsonNode subscriptions = getSubscriptions(apiId);
		final List<String> apps = new ArrayList<>();
		for (final JsonNode s : subscriptions.get("data")) {
			if (!s.get("client_id").isMissingNode()) {
				apps.add(s.get("client_id").asText());
			}
		}
		return apps;
	}

	private JsonNode getSubscriptions(String apiId) {
		return this
				.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
						+ apiId + SUBCRIPTIONS_ENDPOINT, HttpMethod.GET, this.getRequestEntity(null), JsonNode.class)
				.getBody();
	}

	private void subscribeToAPIPlan(String apiId, String appId, ApiPlan plan) throws GenericOPException {
		final Application app = createApplication(appId);
		this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getApplicationsEndpoint() + "/"
						+ app.getId() + SUBCRIPTIONS_ENDPOINT + "?plan=" + plan.getId(),
				HttpMethod.POST, new HttpEntity<>(plan, adminHttpHeaders()), Object.class).getBody();
		deployApi(apiId);
	}

	@Override
	public void subscribeToAPI(String apiId, String application) {
		try {
			final ApiPlan oauth = getApiPlans(apiId).stream()
					.filter(ap -> ap.getStatus().name().equalsIgnoreCase(ApiPlan.Status.PUBLISHED.name())
							&& ap.getSecurity().name().equalsIgnoreCase(ApiPlan.Security.OAUTH2.name()))
					.findFirst().orElse(null);
			if (oauth != null) {
				subscribeToAPIPlan(apiId, application, oauth);
			}
		} catch (final Exception e) {
			log.error("Could not subscribe to API {} with application {}", apiId, application, e);
		}

	}

	@Override
	public void unsubscribeToAPI(String apiId, String application) {
		final JsonNode subs = getSubscriptions(apiId);
		for (final JsonNode s : subs.get("data")) {
			if (!s.get("client_id").isMissingNode() && s.get("client_id").asText().equals(application)) {
				this.executeRequest(
						resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getApplicationsEndpoint()
								+ "/" + s.get("application").asText() + SUBCRIPTIONS_ENDPOINT + "/"
								+ s.get("id").asText(),
						HttpMethod.DELETE, new HttpEntity<>(adminHttpHeaders()), Object.class).getBody();
			}
		}

	}

	private HttpHeaders adminHttpHeaders() {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
		return headers;
	}

	@Override
	public String getURLIframe(String apiId, String iframe) {
		if (graviteeVersion.startsWith("3")) {
			if (iframe.equalsIgnoreCase("policies")) {
				return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI).concat("/environments/default/apis/")
						.concat(apiId).concat("/policy-studio/design");
			} else if (iframe.equalsIgnoreCase("analytics")) {
				return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI).concat("/environments/default/apis/")
						.concat(apiId).concat("/analytics");
			}
		} else {
			if (iframe.equalsIgnoreCase("policies")) {
				return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI).concat("/management/apis/").concat(apiId)
						.concat("/policies");
			} else if (iframe.equalsIgnoreCase("analytics")) {
				return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI).concat("/management/apis/").concat(apiId)
						.concat("/analytics");
			}
		}
		return resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI);
	}

	@Override
	public boolean isApiSync(String apiId) {
		return executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + getAPIEndpoint() + "/"
				+ apiId + "/state", HttpMethod.GET, getRequestEntity(null), ApiStateSync.class).getBody().isSync();
	}

}
