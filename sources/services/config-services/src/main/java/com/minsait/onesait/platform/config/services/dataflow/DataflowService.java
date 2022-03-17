/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.dto.OPResourceDTO;
import com.minsait.onesait.platform.config.model.DataflowInstance;
import com.minsait.onesait.platform.config.model.Pipeline;
import com.minsait.onesait.platform.config.model.PipelineUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.dataflow.beans.InstanceBuilder;

public interface DataflowService {

	List<Pipeline> getPipelinesWithStatus(String userId);

	Pipeline createPipeline(Pipeline pipeline, String userId);

	void removePipeline(String id, String userId);

	ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, Object body, String user);

	ResponseEntity<String> sendHttpWithInstance(HttpServletRequest requestServlet, HttpMethod httpMethod, Object body,
			String instanceId);

	ResponseEntity<String> sendHttpFile(HttpServletRequest requestServlet, MultipartFile file, String userId);

	ResponseEntity<String> sendHttpFileWithInstance(HttpServletRequest requestServlet, MultipartFile file,
			String instance);

	ResponseEntity<byte[]> getyHttpBinary(HttpServletRequest requestServlet, String body, String user);

	Pipeline getPipelineById(String id);

	Pipeline getPipelineByIdentification(String identification);

	Pipeline getPipelineByIdStreamsets(String id);

	boolean hasUserViewPermission(Pipeline pipeline, String userId);

	boolean hasUserEditPermission(Pipeline pipeline, String userId);

	List<Pipeline> getPipelines(String userId);

	void removeHardPipeline(String pipelineId, String userId);

	Pipeline renamePipeline(String pipelineId, String userId, String newIdentification);

	ResponseEntity<String> startPipeline(String userId, String pipelineIdentification, String parameters);

	ResponseEntity<String> stopPipeline(String userId, String pipelineIdentification);

	ResponseEntity<String> statusPipeline(String userId, String pipelineIdentification);

	ResponseEntity<String> getPipelinesStatus(String userId);

	ResponseEntity<String> getPipelineConfiguration(String userId, String pipelineIdentification);

	ResponseEntity<String> exportPipeline(String userId, String pipelineIdentification);

	ResponseEntity<String> importPipeline(String userId, String pipelineIdentification, String config,
			boolean overwrite);

	ResponseEntity<String> updatePipeline(String userId, String pipelineIdentification, String config);

	ResponseEntity<String> clonePipeline(String userId, String pipelineIdentificationOri,
			String pipelineIdentificationDest);

	ResponseEntity<String> metricsPipeline(String userId, String pipelineIdentification);

	ResponseEntity<String> pipelines(String userId, String filterText, String label, int offset, int len,
			String orderBy, String order, boolean includeStatus);

	PipelineUserAccess createUserAccess(String dataflowId, String userId, String accessType, String userIdAccessTo);

	Pipeline changePublic(String streamsetsId, String userId);

	ResponseEntity<String> resetOffsetPipeline(String userId, String pipelineIdentification);

	void deleteUserAccess(String dataflowUserAccessId, String userId);

	String getVersion();

	/* DATAFLOW INSTANCES */

	List<DataflowInstance> getAllDataflowInstances();

	DataflowInstance getDefaultDataflowInstance();

	void setDefaultDataflowInstance(DataflowInstance newDataflowInstance);

	DataflowInstance getDataflowInstanceByIdentification(String identification);

	DataflowInstance getDataflowInstanceById(String id);

	DataflowInstance getDataflowInstanceForUserId(String userId);

	DataflowInstance createDataflowInstance(InstanceBuilder instance);

	DataflowInstance updateDataflowInstance(String identification, InstanceBuilder newInstance);

	void deleteDataflowInstance(DataflowInstance dataflowInstance, DataflowServiceImpl.RemoveInstanceAction action,
			User user);

	void deleteDataflowInstance(String id, String action, String userId);

	ResponseEntity<String> restartDataflowInstance(String instanceId);

	List<User> getFreeAnalyticsUsers();

	ResponseEntity<String> importPipelineData(String userId, String pipelineIdentification, String config,
			boolean overwrite);

	List<Pipeline> getPipelinesForListWithProjectsAccess(String userId);

	List<String> getIdentificationByUser(String userId);

	List<OPResourceDTO> getDtoByUserAndPermissions(String userId, String identification);

	ResponseEntity<String> getPipelineCommittedOffsets(String userId, String pipelineIdentification);

}
