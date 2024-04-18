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
package com.minsait.onesait.platform.api.rest.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiDTO;
import com.minsait.onesait.platform.config.services.apimanager.dto.ApiQueryParameterDTO;
import com.minsait.onesait.platform.config.services.apimanager.dto.OperacionDTO;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;

public class RestSwaggerReader {

	private static final String INFO_VERSION = "Apache 2.0 License";
	private static final String INFO_TITLE = "Platform  API Manager";
	private static final String INFO_DESCRIPTION = "Platform";

	private static final String LICENSE_NAME = "1.0.0";
	private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";

	private static final String CONTACT_NAME = "Platform Team";
	private static final String CONTACT_URL = "https://dev.onesaitplatform.com";
	private static final String CONTACT_EMAIL = "support@onesaitplatform.com";

	private static final String DATA_TYPE_VALUE_SEPARATOR = "|";

	private static final String XSOFIAEXTENSION = "x-sofia2-extension";

	private static List<String> produces = new ArrayList<>(Arrays.asList(MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_PLAIN, "text/csv", "application/ld+json"));
	private static List<String> consumes = new ArrayList<>(
			Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML, "application/ld+json"));

	private static Map<String, Response> responses = new HashMap<>();
	private static List<Scheme> schemes = new ArrayList<>();

	private static List<String> excludeParms = Arrays.asList("query", "queryType", "targetdb", "cacheable");
	static {

		final Response r1 = new Response();
		r1.setDescription("No Content");
		responses.put("204", r1);

		final Response r2 = new Response();
		r2.setDescription("Bad Request");
		r2.setResponseSchema(new ModelImpl());
		responses.put("400", r2);

		final Response r3 = new Response();
		r3.setDescription("Unauthorized");
		r3.setResponseSchema(new ModelImpl());
		responses.put("401", r3);

		final Response r4 = new Response();
		r4.setDescription("Internal Server Error");
		r4.setResponseSchema(new ModelImpl());
		responses.put("501", r4);

		final Response r5 = new Response();
		r5.setDescription("OK");
		r5.setResponseSchema(new ModelImpl());
		responses.put("200", r5);

		schemes.add(Scheme.HTTPS);
		schemes.add(Scheme.HTTP);

	}

	public Swagger read(ApiDTO apiDto, BeanConfig config) {
		final Swagger swagger = new Swagger();

		final Info info = new Info();
		info.setDescription(INFO_DESCRIPTION);
		final License license = new License();
		license.setName(LICENSE_NAME);
		license.setUrl(LICENSE_URL);

		info.setLicense(license);

		info.setTitle(INFO_TITLE);
		info.setVersion(INFO_VERSION);

		final Contact contact = new Contact();
		contact.setName(CONTACT_NAME);
		contact.setUrl(CONTACT_URL);
		contact.setEmail(CONTACT_EMAIL);
		info.setContact(contact);
		swagger.setInfo(info);

		swagger.setConsumes(consumes);
		swagger.setProduces(produces);
		swagger.setResponses(responses);

		final int version = apiDto.getVersion();
		final String vVersion = "v" + version;
		final String identification = apiDto.getIdentification();

		info.setDescription(INFO_DESCRIPTION + " - " + identification + " - " + vVersion);
		if (StringUtils.hasText(config.getHost())) {
			swagger.setHost(config.getHost());
		}
		swagger.setBasePath(config.getBasePath());

		swagger.setSchemes(schemes);

		final List<Tag> tags = new ArrayList<>();
		final Tag tag = new Tag();
		tag.setName(apiDto.getIdentification());
		tags.add(tag);
		swagger.setTags(tags);

		swagger.setVendorExtension(XSOFIAEXTENSION, populateApiDTOLite(apiDto));

		final ArrayList<OperacionDTO> operations = apiDto.getOperations();

		for (final OperacionDTO operacionDTO : operations) {
			parse(swagger, operacionDTO);
		}
		return swagger;
	}

	private ApiDTO populateApiDTOLite(ApiDTO apidto) {
		final ApiDTO api2 = SerializationUtils.clone(apidto);
		api2.setOperations(null);
		api2.setAuthentication(null);
		return api2;
	}

	private void createPARAMETER(Swagger swagger, Operation op, String name, String description, String parameterType,
			String dataType, List<String> value) {
		final Parameter sofia2Api = RestSwaggerReaderHelper.populateParameter(swagger, name, description, true,
				parameterType, dataType, null, value);
		op.addParameter(sofia2Api);
		op.setConsumes(consumes);
		op.setProduces(produces);
		op.setResponses(responses);
	}

	private void parse(Swagger swagger, OperacionDTO operacionDTO) {

		final String description = operacionDTO.getDescription();
		final String operation = operacionDTO.getOperation().name();
		String path = operacionDTO.getPath();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		final List<ApiQueryParameterDTO> queryParams = operacionDTO.getQueryParams().stream()
				.filter(p -> !excludeParms.contains(p.getName())).collect(Collectors.toList());

		Path swaggerPath = swagger.getPath(path);
		if (swaggerPath == null) {
			swaggerPath = new Path();
			swagger.path(path, swaggerPath);
		}

		final Operation op = new Operation();
		op.operationId(description.replaceAll(" ", "_"));

		final String method = operation.toLowerCase(Locale.US);
		// DONT NEED THIS ANYMORE, OPENAPI 3 SEC DEFINITIONS INSTEAD
//		createPARAMETER(swagger, op, Constants.AUTHENTICATION_HEADER, Constants.AUTHENTICATION_HEADER,
//				ApiQueryParameter.HeaderType.HEADER.name(), ApiQueryParameter.DataType.STRING.name().toLowerCase(),
//				null);

		swaggerPath = swaggerPath.set(method, op);

		for (final ApiQueryParameterDTO apiQueryParameterDTO : queryParams) {

			final String desc = apiQueryParameterDTO.getDescription();
			final String name = apiQueryParameterDTO.getName();
			final String type = apiQueryParameterDTO.getDataType().name();
			final String value = apiQueryParameterDTO.getValue();
			final String condition = apiQueryParameterDTO.getHeaderType().name();

			final Parameter parameter = RestSwaggerReaderHelper.populateParameter(swagger, name, desc, true, condition,
					type.toLowerCase(), null, splitStringValue(value));

			op.addParameter(parameter);
		}
		op.setTags(swagger.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()));
		op.setConsumes(consumes);
		op.setProduces(produces);
		op.setResponses(responses);
		op.addSecurity(Constants.AUTHENTICATION_HEADER, null);
		op.addSecurity(Constants.JWT, null);

	}

	private static List<String> splitStringValue(String value) {
		final List<String> enumValue = new ArrayList<>();
		if (value == null) {
			return Collections.emptyList();
		}

		if (value.contains(RestSwaggerReader.DATA_TYPE_VALUE_SEPARATOR)) {
			enumValue.add(value);
			return enumValue;
		} else {
			final StringTokenizer st = new StringTokenizer(value, RestSwaggerReader.DATA_TYPE_VALUE_SEPARATOR);
			while (st.hasMoreTokens()) {
				final String token = st.nextToken();
				enumValue.add(token);
			}
		}
		return enumValue;
	}

}
