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
package com.minsait.onesait.platform.business.services.opendata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataOrganization;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.OrganizationShowResponse;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Component
public class OpenDataApi {

	private String openDataUrl;

	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@PostConstruct
	public void openDataSetUp() {
		openDataUrl = integrationResourcesService.getUrl(Module.OPEN_DATA, ServiceUrl.BASE);
	}

	private static final String API_ENDPOINT = "/api/3/action/";

	public Object getOperation(String endpoint, String userToken, Class result) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		if (userToken != null) {
			headers.set("Authorization", "Bearer " + userToken);
		}
		final HttpEntity entity = new HttpEntity(headers);
		return restTemplate.exchange(openDataUrl + API_ENDPOINT + endpoint, HttpMethod.GET, entity, result).getBody();
	}

	public ResponseEntity postOperation(String endpoint, String userToken, Object payload, Class result) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter());
		final HttpHeaders headers = new HttpHeaders();
		if (userToken != null) {
			headers.set("Authorization", "Bearer " + userToken);
		}
		if (endpoint.equals("organization_create") && payload instanceof OpenDataOrganization
				&& ((OpenDataOrganization) payload).getImage_upload() != null) {
			headers.set("Content-Type", "multipart/form-data");
		}
		if ("dataset_purge".equals(endpoint)) {
			headers.set("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		}

		final HttpEntity entity = new HttpEntity<>(payload, headers);
		return restTemplate.exchange(openDataUrl + API_ENDPOINT + endpoint, HttpMethod.POST, entity, result);
	}

	public ResponseEntity download(String userToken, OpenDataResource resource, String format) {
		String downloadUrl = resource.getUrl();
		if (!format.equals("") && !format.equals("other")) {
			downloadUrl += "?format=" + format;
		}
		try {
			final URL url = new URL(downloadUrl);
			final HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestProperty("Authorization", "Bearer " + userToken);
			final int responseCode = httpConn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				final InputStream inputStream = httpConn.getInputStream();
				final InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
				final HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.setContentType(MediaType.parseMediaType(httpConn.getContentType()));

				final String raw = httpConn.getHeaderField("Content-Disposition");
				String fileName;
				if (raw != null && raw.indexOf("=") != -1) {
					fileName = raw.split("=")[1];
				} else {
					fileName = FilenameUtils.getName(url.getPath());
				}
				httpHeaders.set("Content-Disposition", "attachment; filename=" + fileName);
				return new ResponseEntity(inputStreamResource, httpHeaders, HttpStatus.OK);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String accessPortal() {
		return openDataUrl + "/user/login";
	}

	public ResponseEntity postOperationWithFile(String endpoint, String userToken, OpenDataOrganization organization) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		File tempFile = null;
		try {
			final MultipartFile multipartFile = organization.getImage_upload();
			final String filename = multipartFile.getOriginalFilename();
			final String fullPath = "/tmp/" + filename;
			final InputStream is = new ByteArrayInputStream(multipartFile.getBytes());
			tempFile = new File(fullPath);
			final OutputStream os = new FileOutputStream(tempFile);
			IOUtils.copy(is, os);

			parameters.add("image_upload", new FileSystemResource(tempFile));
			is.close();
			os.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		parameters.add("id", organization.getId());
		parameters.add("name", organization.getName());
		parameters.add("title", organization.getTitle());
		if (organization.getDescription() != null && !organization.getDescription().equals("")) {
			parameters.add("description", organization.getDescription());
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + userToken);
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		final HttpEntity entity = new HttpEntity<>(parameters, headers);
		final ResponseEntity response = restTemplate.exchange(openDataUrl + API_ENDPOINT + endpoint, HttpMethod.POST,
				entity, OrganizationShowResponse.class);

		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}

		return response;

	}
}
