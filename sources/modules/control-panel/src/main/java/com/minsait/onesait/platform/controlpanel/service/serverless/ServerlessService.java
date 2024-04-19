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
package com.minsait.onesait.platform.controlpanel.service.serverless;

import java.util.Collection;

import com.minsait.onesait.platform.controlpanel.controller.serverless.ApplicationCreate;
import com.minsait.onesait.platform.controlpanel.controller.serverless.ApplicationInfo;
import com.minsait.onesait.platform.controlpanel.controller.serverless.ApplicationUpdate;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionCreate;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionInfo;
import com.minsait.onesait.platform.controlpanel.controller.serverless.FunctionUpdate;

public interface ServerlessService {

	public Collection<ApplicationInfo> getApplications();

	public ApplicationInfo getApplication(String name);

	public void createApplication(ApplicationCreate appCreate);

	public void updateApplication(ApplicationUpdate appUpdate);

	public void deleteApplication(String name);

	public void deployFunction(String appName, String fnName);

	public void deleteFunction(String appName, String fnName);

	public void updateFunction(String appName, String fnName, FunctionUpdate functionUpdate);

	public void createFunction(String appName, String fnName,  FunctionCreate functionCreate);

	public FunctionInfo getFunction(String appName, String fnName);


}
