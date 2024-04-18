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
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.services.dataflow.DataflowServiceImpl.PipelineTypes;

public interface DataflowService {

	public Pipeline createPipeline(String name, PipelineTypes type, String description, String userId) throws UnsupportedEncodingException;

	public void removePipeline(String id, String userId);

	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, Object body,
			String user) throws URISyntaxException, IOException;

	public ResponseEntity<byte[]> sendHttpBinary(HttpServletRequest requestServlet, HttpMethod httpMethod, String body,
			String user) throws URISyntaxException, IOException;

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, Object body, String user,
			String dataflowId) throws URISyntaxException, IOException;

	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, String user,
			String dataflowId) throws URISyntaxException, IOException;

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, Object body, HttpHeaders headers,
			String user) throws URISyntaxException, IOException;

	public ResponseEntity<byte[]> sendHttpBinary(String url, HttpMethod httpMethod, String body, HttpHeaders headers,
			String user) throws URISyntaxException, IOException;

	public Pipeline getPipeline(String identification, String userId);

	public List<Pipeline> getPipelines(String userId);

	public boolean hasUserPermissionForPipeline(String pipelineId, String userId);

	public boolean hasUserViewPermission(String pipelineId, String userId);

	public boolean hasUserEditPermission(String pipelineId, String userId);

	public ResponseEntity<String> startPipeline(String userId, String pipelineIdentification, String parameters);

	public ResponseEntity<String> stopPipeline(String userId, String pipelineIdentification);

	public ResponseEntity<String> statusPipeline(String userId, String pipelineIdentification);

	public ResponseEntity<String> statusPipelines(String userId);

	public ResponseEntity<String> exportPipeline(String userId, String pipelineIdentification);

	public ResponseEntity<String> importPipeline(String userId, String pipelineIdentification, String config);

	public ResponseEntity<String> updatePipeline(String userId, String pipelineIdentification, String config);

	public ResponseEntity<String> clonePipeline(String userId, String pipelineIdentificationOri,
			String pipelineIdentificationDest);
	
	public ResponseEntity<String> metricsPipeline(String userId, String pipelineIdentification);
	
	public ResponseEntity<String> pipelines(String userId, String filterText, String label, int offset, int len, String orderBy, String order, boolean includeStatus);

	ResponseEntity<String> resetOffsetPipeline(String userId, String pipelineIdentification);

	void createUserAccess(String dataflowId, String userId, String accessType);

	void deleteUserAccess(String dataflowUserAccessId);

	void changePublic(Pipeline dataflowId);

	public String getVersion();
}
