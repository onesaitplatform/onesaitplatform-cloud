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

import com.minsait.onesait.platform.serverless.dto.ApplicationUpdate;
import com.minsait.onesait.platform.serverless.dto.fn.FnApplication;
import com.minsait.onesait.platform.serverless.dto.fn.FnFunction;
import com.minsait.onesait.platform.serverless.model.Application;
import com.minsait.onesait.platform.serverless.model.Function;

public interface FnService {

	public boolean create(String appName);

	public void update(ApplicationUpdate application, String appId);

	public boolean delete(String appName);

	public String getAppId(String appName);

	public FnApplication getApp(String appId);

	public FnFunction getFunction(String fnId);

	public FnFunction deploy(Application app, Function function, String basePath);

	public void deleteFunction(String fnId);

	void updateFunction(FnFunction function);

	void removeVar(String appName, String fnName, String var);

}
