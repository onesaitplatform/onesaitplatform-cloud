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
package com.minsait.onesait.platform.config.services.processtrace;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.minsait.onesait.platform.config.model.ProcessTrace;
import com.minsait.onesait.platform.config.model.User;

public interface ProcessTraceService {

	public List<ProcessTrace> getProcessTraceByUser(User user, String identification, String description);

	public ProcessTrace createProcessTrace(ProcessTrace processTrace);

	public ProcessTrace getById(String id);

	public void updateProcessTrace(ProcessTrace processTrace);

	public void deleteProcessTrace(ProcessTrace processTrace);

	public ProcessTrace getByIdentification(String identification);

	public List<ProcessTrace> getAll();

	public void checkProcessExecution(String processId)
			throws JsonGenerationException, JsonMappingException, IOException;

}
