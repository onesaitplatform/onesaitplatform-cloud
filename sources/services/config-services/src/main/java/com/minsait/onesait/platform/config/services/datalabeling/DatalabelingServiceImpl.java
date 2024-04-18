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
package com.minsait.onesait.platform.config.services.datalabeling;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.services.datalabeling.dto.CloudStorageDTO;
import com.minsait.onesait.platform.config.services.datalabeling.dto.CloudStorageFullDTO;
import com.minsait.onesait.platform.config.services.datalabeling.dto.ProjectDTO;
import com.minsait.onesait.platform.config.services.generic.security.SecurityService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatalabelingServiceImpl implements DatalabelingService {

	@Autowired
	@Lazy
	private OPResourceService resourceService;

	@Autowired
	SecurityService securityService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ActiveProfileDetector profileDetector;

	@Value("${onesaitplatform.controlpanel.url:http://localhost:18000/controlpanel}")
	private String basePath;

	protected ObjectMapper objectMapper;

	private String dataLabelingPath;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		Urls urls = configurationService.getEndpointsUrls(profileDetector.getActiveProfile());
		try {
			this.dataLabelingPath = urls.getDatalabeling().getBase();
		} catch (NullPointerException e) {
			this.dataLabelingPath = "http://localhost:9005/";
		}
	}

	@Override
	public List<ProjectDTO> findProjects(String token) {
		List<ProjectDTO> projects = new ArrayList<ProjectDTO>();
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);

		final ResponseEntity<String> result = restTemplate.exchange(dataLabelingPath.concat("/api/projects"),
				HttpMethod.GET, entity, String.class);

		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to get projects " + "-" + result.getStatusCode().getReasonPhrase());
		}
		// map result
		final JSONObject response = new JSONObject(result.getBody());
		JSONArray bodyResult = response.getJSONArray("results");
		for (int i = 0; i < bodyResult.length(); i++) {
			JSONObject obj = bodyResult.getJSONObject(i);
			ProjectDTO proj = new ProjectDTO();
			proj.setIdentification(String.valueOf(obj.getInt("id")));
			proj.setTitle(obj.getString("title"));
			proj.setDescription(obj.getString("description"));
			proj.setCreatedAt(formatDate(obj.getString("created_at")));
			proj.setTaskNumber(String.valueOf(obj.getInt("task_number")));
			proj.setTotalAnnotationsNumber(String.valueOf(obj.getInt("total_annotations_number")));
			proj.setTotalPredictionsNumber(String.valueOf(obj.getInt("total_predictions_number")));

			projects.add(proj);
		}

		return projects;
	}

	@Override
	public ProjectDTO findProjectByID(String id, String token) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);

		final ResponseEntity<String> result = restTemplate.exchange(dataLabelingPath.concat("/api/projects/" + id),
				HttpMethod.GET, entity, String.class);

		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to get projects " + "-" + result.getStatusCode().getReasonPhrase());
		}
		// map result
		final JSONObject obj = new JSONObject(result.getBody());

		ProjectDTO proj = new ProjectDTO();
		proj.setIdentification(String.valueOf(obj.getInt("id")));
		proj.setTitle(obj.getString("title"));
		proj.setDescription(obj.getString("description"));
		proj.setCreatedAt(formatDate(obj.getString("created_at")));
		proj.setTaskNumber(String.valueOf(obj.getInt("task_number")));
		proj.setTotalAnnotationsNumber(String.valueOf(obj.getInt("total_annotations_number")));
		proj.setTotalPredictionsNumber(String.valueOf(obj.getInt("total_predictions_number")));

		return proj;
	}

	@Override
	public List<CloudStorageDTO> findImportStorages(String projectId, String token) {

		List<CloudStorageDTO> ics = new ArrayList<CloudStorageDTO>();
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);
		final ResponseEntity<String> result = restTemplate.exchange(
				dataLabelingPath.concat("/api/storages/s3?project=" + projectId), HttpMethod.GET, entity, String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to get import clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
		}
		// map result
		final JSONArray response = new JSONArray(result.getBody());
		if (response != null && response.length() > 0) {
			for (int i = 0; i < response.length(); i++) {
				JSONObject obj = response.getJSONObject(i);
				CloudStorageDTO cs = new CloudStorageDTO();
				cs.setIdentification(String.valueOf(obj.getInt("id")));
				cs.setTitle(obj.getString("title"));
				cs.setCreatedAt(formatDate(obj.getString("created_at")));
				if (!obj.isNull("last_sync")) {
					cs.setLastSync(formatDate(obj.getString("last_sync")));
				}
				cs.setBucket(obj.getString("bucket"));
				if (!obj.isNull("prefix")) {
					cs.setPrefix(obj.getString("prefix"));
				}
				if (!obj.isNull("regex_filter")) {
					cs.setRegexFilter(obj.getString("regex_filter"));
				}
				if (!obj.isNull("region_name")) {
					cs.setRegionName(obj.getString("region_name"));
				}
				cs.setS3Endpoint(obj.getString("s3_endpoint"));

				cs.setProject(String.valueOf(obj.getInt("project")));
				ics.add(cs);
			}

		}
		return ics;
	}

	@Override
	public List<CloudStorageDTO> findExportStorages(String projectId, String token) {

		List<CloudStorageDTO> ics = new ArrayList<CloudStorageDTO>();
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);
		final ResponseEntity<String> result = restTemplate.exchange(
				dataLabelingPath.concat("/api/storages/export/s3?project=" + projectId), HttpMethod.GET, entity,
				String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to get export clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
		}
		// map result
		final JSONArray response = new JSONArray(result.getBody());
		if (response != null && response.length() > 0) {
			for (int i = 0; i < response.length(); i++) {
				JSONObject obj = response.getJSONObject(i);
				CloudStorageDTO cs = new CloudStorageDTO();
				cs.setIdentification(String.valueOf(obj.getInt("id")));
				cs.setTitle(obj.getString("title"));
				cs.setCreatedAt(formatDate(obj.getString("created_at")));
				if (!obj.isNull("last_sync")) {
					cs.setLastSync(formatDate(obj.getString("last_sync")));
				}
				cs.setBucket(obj.getString("bucket"));
				if (!obj.isNull("prefix")) {
					cs.setPrefix(obj.getString("prefix"));
				}
				if (!obj.isNull("regex_filter")) {
					cs.setRegexFilter(obj.getString("regex_filter"));
				}
				if (!obj.isNull("region_name")) {
					cs.setRegionName(obj.getString("region_name"));
				}
				cs.setS3Endpoint(obj.getString("s3_endpoint"));

				cs.setProject(String.valueOf(obj.getInt("project")));
				ics.add(cs);
			}

		}
		return ics;
	}

	@Override
	public String deleteImportStorage(String identification, String token) {

		List<CloudStorageDTO> ics = new ArrayList<CloudStorageDTO>();
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);
		final ResponseEntity<String> result = restTemplate.exchange(
				dataLabelingPath.concat("/api/storages/s3/" + identification), HttpMethod.DELETE, entity, String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to delete import clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
			return "error";
		}
		return "ok";
	}

	@Override
	public String deleteExportStorage(String identification, String token) {

		List<CloudStorageDTO> ics = new ArrayList<CloudStorageDTO>();
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);
		final ResponseEntity<String> result = restTemplate.exchange(
				dataLabelingPath.concat("/api/storages/export/s3/" + identification), HttpMethod.DELETE, entity,
				String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to delete export clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
			return "error";
		}
		return "ok";
	}

	@Override
	public String deleteProject(String identification, String token) {

		List<CloudStorageDTO> ics = new ArrayList<CloudStorageDTO>();
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		final MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		final HttpEntity<?> entity = new HttpEntity<>(parameters, headers);
		final ResponseEntity<String> result = restTemplate.exchange(
				dataLabelingPath.concat("/api/projects/" + identification), HttpMethod.DELETE, entity, String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable to delete project " + "-" + result.getStatusCode().getReasonPhrase());
			return "error";
		}
		return "ok";
	}

	@Override
	public String createImportStorages(CloudStorageFullDTO cloudStorage, String token) {
		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		// create object json to send
		JSONObject cloudObj = new JSONObject();
		cloudObj.put("presign", cloudStorage.getPresign());
		cloudObj.put("title", cloudStorage.getTitle());
		cloudObj.put("last_sync", cloudStorage.getLastSync());
		cloudObj.put("bucket", cloudStorage.getBucket());
		cloudObj.put("prefix", cloudStorage.getPrefix());
		cloudObj.put("regex_filter", cloudStorage.getRegexFilter());
		cloudObj.put("use_blob_urls", cloudStorage.getUseBlobUrls());
		cloudObj.put("aws_access_key_id", cloudStorage.getAws_access_key_id());
		cloudObj.put("aws_secret_access_key", cloudStorage.getAws_secret_access_key());
		cloudObj.put("aws_session_token", cloudStorage.getAws_session_token());
		cloudObj.put("s3_endpoint", cloudStorage.getS3Endpoint());
		cloudObj.put("presign_ttl", 1);
		cloudObj.put("recursive_scan", cloudStorage.getRecursiveScan());
		cloudObj.put("project", cloudStorage.getProject());
		final HttpEntity<String> entity = new HttpEntity<String>(cloudObj.toString(), headers);
		final ResponseEntity<String> result = restTemplate.exchange(dataLabelingPath.concat("/api/storages/s3"),
				HttpMethod.POST, entity, String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable create import clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
		}
		return "ok";
	}

	@Override
	public String createExportStorages(CloudStorageFullDTO cloudStorage, String token) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		// create object json to send
		JSONObject cloudObj = new JSONObject();
		cloudObj.put("presign", cloudStorage.getPresign());
		cloudObj.put("title", cloudStorage.getTitle());
		cloudObj.put("last_sync", cloudStorage.getLastSync());
		cloudObj.put("bucket", cloudStorage.getBucket());
		cloudObj.put("prefix", cloudStorage.getPrefix());
		cloudObj.put("regex_filter", cloudStorage.getRegexFilter());
		cloudObj.put("use_blob_urls", cloudStorage.getUseBlobUrls());
		cloudObj.put("aws_access_key_id", cloudStorage.getAws_access_key_id());
		cloudObj.put("aws_secret_access_key", cloudStorage.getAws_secret_access_key());
		cloudObj.put("aws_session_token", cloudStorage.getAws_session_token());
		cloudObj.put("s3_endpoint", cloudStorage.getS3Endpoint());
		cloudObj.put("presign_ttl", 1);
		cloudObj.put("recursive_scan", cloudStorage.getRecursiveScan());
		cloudObj.put("project", cloudStorage.getProject());
		final HttpEntity<String> entity = new HttpEntity<String>(cloudObj.toString(), headers);
		final ResponseEntity<String> result = restTemplate.exchange(dataLabelingPath.concat("/api/storages/export/s3"),
				HttpMethod.POST, entity, String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable create import clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
		}
		return "ok";
	}

	@Override
	public String getUserToken(String user, String pass) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		// create object json to send
		JSONObject cloudObj = new JSONObject();
		cloudObj.put("username", user);
		cloudObj.put("password", pass);

		final HttpEntity<String> entity = new HttpEntity<String>(cloudObj.toString(), headers);
		final ResponseEntity<String> result = restTemplate
				.exchange(dataLabelingPath.concat("/api/current-user/gettoken"), HttpMethod.POST, entity, String.class);
		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable create import clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
		}
		final JSONObject response = new JSONObject(result.getBody());

		return response.getString("token");
	}

	@Override
	public Integer createProject(String token) {

		final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

		final HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Token " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		JSONObject proj = new JSONObject();

		proj.put("title", "New Project " + getDay());
		proj.put("description", "");
		proj.put("label_config", "<View></View>");
		proj.put("expert_instruction", "");
		proj.put("show_instruction", false);

		proj.put("show_skip_button", true);
		proj.put("enable_empty_annotation", true);
		proj.put("show_annotation_history", false);
		proj.put("organization", 1);
		proj.put("color", "#FFFFFF");
		proj.put("maximum_annotations", 1);
		proj.put("is_published", false);
		proj.put("model_version", "");
		proj.put("is_draft", false);

		proj.put("min_annotations_to_start_training", 0);
		proj.put("start_training_on_annotation_update", false);
		proj.put("show_collab_predictions", true);
		proj.put("sampling", "Sequential sampling");
		proj.put("show_ground_truth_first", false);
		proj.put("show_overlap_first", false);
		proj.put("overlap_cohort_percentage", 100);
		proj.put("control_weights", new JSONObject());
		proj.put("parsed_label_config", new JSONObject());
		proj.put("evaluate_predictions_automatically", false);
		proj.put("config_has_control_tags", false);
		proj.put("skip_queue", "REQUEUE_FOR_OTHERS");
		proj.put("reveal_preannotations_interactively", false);

		final HttpEntity<String> entity = new HttpEntity<String>(proj.toString(), headers);

		final ResponseEntity<String> result = restTemplate.exchange(dataLabelingPath.concat("/api/projects/"),
				HttpMethod.POST, entity, String.class);

		if (!result.getStatusCode().equals(HttpStatus.OK)) {
			log.error("Unable create import clouds storages " + "-" + result.getStatusCode().getReasonPhrase());
		}
		final JSONObject response = new JSONObject(result.getBody());
		Integer id = response.getInt("id");
		return id;
	}

	private String getDay() {
		Calendar calendar = Calendar.getInstance();
		String day = calendar.get(Calendar.DAY_OF_MONTH) + " / " + calendar.get(Calendar.MONTH) + " / "
				+ calendar.get(Calendar.YEAR);
		return day;
	}

	private String formatDate(String dateToFormat) {
		String result = "";
		if (dateToFormat != null && dateToFormat.length() > 0) {
			DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				date = df1.parse(dateToFormat.substring(0, 10));
			} catch (ParseException e) {

				e.printStackTrace();
			}
			String pattern = "dd/MM/yyyy";
			DateFormat df = new SimpleDateFormat(pattern);
			result = df.format(date);
		}
		return result;
	}

}
