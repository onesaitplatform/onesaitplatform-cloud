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
package com.minsait.onesait.platform.controlpanel.services.gravitee;

import static com.minsait.onesait.platform.controlpanel.gravitee.dto.TransformHeadersPolicy.TRANSFORM_HEADERS_DESCRIPTION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiStates;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiCreate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPage;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageResponse;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageUpdate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPlan;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiUpdate;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.CorsApi;
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

	private static final String BEARER = "bearer ";

	private RestTemplate restTemplate;

	private static final String OAUTH_EXCHANGE_TOKEN_URL = "/auth/oauth2/onesait-account/exchange";
	private static final String IDENTITIES_ENDPOINT = "/configuration/identities";
	private static final String IDENTITY_PROVIDER_ID = "onesait-account";

	private static final String APIS_ENDPOINT = "/apis";
	private static final String PLANS_ENDPOINT = "/plans";
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

	@Value("${gravitee.headerValue:Gravitee-Server}")
	private String graviteeHeaderValue;
	@Value("${gravitee.clientId}")
	private String clientId;
	@Value("${gravitee.clientSecret}")
	private String clientSecret;
	@Value("${gravitee.corsEnabled:true}")
	private boolean corsEnabled;


	@Autowired
	private AppWebUtils utils;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private ObjectMapper mapper;

	@PostConstruct
	void initRestTemplate() {
		restTemplate = new RestTemplate(SSLUtil.getHttpRequestFactoryAvoidingSSLVerification());

		if (!oauthIdentityManagerExists()) {
			try {
				createDefaultIdentityProvider();
			} catch (final Exception e) {
				Log.error("Could not create identity provider");
			}
		}
	}

	@Override
	public GraviteeApi processApi(Api api) throws GenericOPException {
		// Create DTO
		try {
			ApiCreate apiCreate = null;
			ApiUpdate apiUpdate = null;
			if (!api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
				apiCreate = ApiCreate.createFromOPApi(api, resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE)
						.concat("server/api/v").concat(api.getNumversion() + "/").concat(api.getIdentification()));
				apiUpdate = createApi(apiCreate);
			} else {
				apiUpdate = getApiDTOFromSwagger(api);
			}
			// make public
			apiUpdate.setVisibility("public");

			// Save id for later
			final String apiId = apiUpdate.getId();

			// if api is internal then create header policy
			if (!api.getApiType().equals(ApiType.EXTERNAL_FROM_JSON)) {
				final ArrayNode policies = mapper.createArrayNode();
				policies.add(createHttpHeaderPolicy());
				((ObjectNode) apiUpdate.getPaths()).set("/", policies);
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
				final String url = resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.SWAGGERJSON) + "v" + api.getNumversion() + "/" + api.getIdentification() + "/swagger.json";
				api.setSwaggerJson(getText(url));
			}
			createSwaggerDocPage(apiId, api);

			// create plan
			createDefaultPlan(apiId);

			// start api & deploy
			startApi(apiId);
			deployApi(apiId);

			return GraviteeApi.builder().apiId(apiId).endpoint(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.GATEWAY) + ApiCreate.getApiContextPath(api))
					.build();
		} catch (final Exception e) {
			log.error("Something went wrong while publishing API to gravitee", e);
			throw e;
		}

	}

	@Override
	public ApiUpdate createApi(ApiCreate api) throws GenericOPException {

		return executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT,
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
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + apiId,
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

		return executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
				+ apiId + PLANS_ENDPOINT, HttpMethod.POST, this.getRequestEntity(plan), ApiPlan.class).getBody();

	}

	@Override
	public void startApi(String apiId) throws GenericOPException {

		final UriComponentsBuilder uri = UriComponentsBuilder
				.fromHttpUrl(
						resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + apiId)
				.queryParam(ACTION_PARAM, START);

		executeRequest(uri.toUriString(), HttpMethod.POST, this.getRequestEntity(null), JsonNode.class);

	}

	@Override
	public void stopApi(String apiId) throws GenericOPException {

		final UriComponentsBuilder uri = UriComponentsBuilder
				.fromHttpUrl(
						resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + apiId)
				.queryParam(ACTION_PARAM, STOP);

		executeRequest(uri.toUriString(), HttpMethod.POST, this.getRequestEntity(null), JsonNode.class);

	}

	@Override
	public void deployApi(String apiId) throws GenericOPException {

		this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
				+ apiId + DEPLOY_ENDPOINT, HttpMethod.POST, this.getRequestEntity(null), JsonNode.class);
	}

	@Override
	public ApiUpdate getApi(String apiId) throws GenericOPException {
		final String url = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
				+ apiId;
		return this.executeRequest(url, HttpMethod.GET, this.getRequestEntity(null), ApiUpdate.class).getBody();
	}

	@Override
	public void deleteApi(String apiId) throws GenericOPException {
		stopApi(apiId);
		getApiPlans(apiId).stream().forEach(p -> {
			try {
				deletePlan(apiId, p.getId());
			} catch (GraviteeException | GenericOPException e) {
				log.error("deleteApi", e);
			}
		});
		final String url = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
				+ apiId;
		executeRequest(url, HttpMethod.DELETE, this.getRequestEntity(null), JsonNode.class);
	}

	@Override
	public void changeLifeCycleState(String graviteeId, ApiStates state) throws GenericOPException {
		try {
			final ApiUpdate api = executeRequest(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + graviteeId,
					HttpMethod.GET, null, ApiUpdate.class).getBody();
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
				executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
						+ graviteeId, HttpMethod.PUT, this.getRequestEntity(api), ApiUpdate.class);
				api.setLifeCycleState(STATE_ARCHIVED);
			}

			executeRequest(
					resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + graviteeId,
					HttpMethod.PUT, this.getRequestEntity(api), ApiUpdate.class);
		} catch (final Exception e) {
			log.error("Error while updating gravitee api lifecycle state", e);
		}

	}

	@Override
	public void deletePlan(String apiId, String planId) throws GenericOPException {

		this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
				+ apiId + PLANS_ENDPOINT + "/" + planId, HttpMethod.DELETE, this.getRequestEntity(null), String.class);

	}

	@Override
	public List<ApiPlan> getApiPlans(String apiId) throws GenericOPException {
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + apiId
				+ PLANS_ENDPOINT,
				HttpMethod.GET, this.getRequestEntity(null), new ParameterizedTypeReference<List<ApiPlan>>() {
				}).getBody();
	}

	@Override
	public ApiPage createSwaggerDocPage(String apiId, Api api) throws GenericOPException {
		final ApiPage apiPage = ApiPage.defaultSwaggerDocPage(processSwaggerJSON(api.getSwaggerJson(), api));

		return this
				.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/"
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
				.fromHttpUrl(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + OAUTH_EXCHANGE_TOKEN_URL)
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
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + IMPORT_ENDPOINT
				+ SWAGGER_ENDPOINT,
				HttpMethod.POST,
				this.getRequestEntity(ImportSwaggerDescriptor.builder().withDocumentation(true).withPathMapping(true)
						.withPolicyPaths(true).type(Type.INLINE).payload(api.getSwaggerJson()).build()),
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

	private boolean oauthIdentityManagerExists() {
		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION,
					"Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
			this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + IDENTITIES_ENDPOINT
					+ "/" + IDENTITY_PROVIDER_ID, HttpMethod.GET, new HttpEntity<>(headers), String.class);
			return true;
		} catch (final Exception e) {
			log.error("oauthIdentityManagerExists", e);
			return false;
		}
	}

	private void createDefaultIdentityProvider() throws IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
		this.executeRequest(resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + IDENTITIES_ENDPOINT,
				HttpMethod.POST,
				new HttpEntity<>(IdentityProvider.getFromString(IdentityProvider.DEFAULT_OAUTH_RESOURCE_2_CREATE),
						headers),
				IdentityProvider.class);
		this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + IDENTITIES_ENDPOINT + "/"
						+ IDENTITY_PROVIDER_ID,
						HttpMethod.PUT,
						new HttpEntity<>(IdentityProvider.getFromString(IdentityProvider.DEFAULT_OAUTH_RESOURCE_2_UPDATE),
								headers),
						IdentityProvider.class);

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
		final HttpHeaders headers = new HttpHeaders();
		final HttpEntity<Object> entity = new HttpEntity<>(headers);
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + apiId
				+ PAGES_ENDPOINT,
				HttpMethod.GET, entity, new ParameterizedTypeReference<List<ApiPageResponse>>() {
				}).getBody();
	}

	@Override
	public ApiPageResponse updateApiPage(String apiId, String pageId, ApiPageUpdate apiPage) throws GenericOPException {
		return this.executeRequest(
				resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.MANAGEMENT) + APIS_ENDPOINT + "/" + apiId
				+ PAGES_ENDPOINT + "/" + pageId,
				HttpMethod.PUT, this.getRequestEntity(apiPage), ApiPageResponse.class)
				.getBody();
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

			final BufferedReader in = new BufferedReader(
					new InputStreamReader(
							inputStream));

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
			final ApiPageResponse updateApiPage = list.get(0);
			final ApiPageUpdate apiPage = new ApiPageUpdate(finalContent, updateApiPage.getOrder(), updateApiPage.isHomepage(),
					updateApiPage.isPublished(), updateApiPage.getName(), updateApiPage.getConfiguration());
			apiPage.setContent(finalContent);
			return updateApiPage(updateApiPage.getApi(), updateApiPage.getId(), apiPage);
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
			final SwaggerParseResult swaggerParseResult = openAPIParser.readContents(content, null,
					null);
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



}
