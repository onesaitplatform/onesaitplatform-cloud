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
package com.minsait.onesait.platform.serverless.service;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.serverless.dto.ApplicationCreate;
import com.minsait.onesait.platform.serverless.dto.ApplicationInfo;
import com.minsait.onesait.platform.serverless.dto.ApplicationUpdate;
import com.minsait.onesait.platform.serverless.dto.FunctionCreate;
import com.minsait.onesait.platform.serverless.dto.FunctionInfo;
import com.minsait.onesait.platform.serverless.dto.FunctionUpdate;

public interface ApplicationService {
	public ApplicationInfo create(ApplicationCreate app);

	public ApplicationInfo update(ApplicationUpdate appUpdate, String appName);

	public void delete(String appName);

	public List<ApplicationInfo> list();

	public List<ApplicationInfo> list(String username);

	public FunctionInfo create(FunctionCreate function, String appName);

	public ApplicationInfo find(String appName);

	public FunctionInfo getFunction(String appName, String fnName);

	public FunctionInfo deploy(String appName, String fnName);

	public FunctionInfo updateFunction(String appName, String fnName, FunctionUpdate functionUpdate);

	public void deleteFunction(String appName, String fnName);

	public void updateFunctionsVersion(String appName, String fnName, String version);

	public ObjectNode getFunctionsEnvironment(String appName, String fnName);

	public void updateFunctionsEnvironmnet(String appName, String fnName, ObjectNode config);

}
