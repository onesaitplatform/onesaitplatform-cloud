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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.git.GitlabConfiguration;

@Component
public class MicroservicesGitServiceManager {

	@Autowired
	private List<MicroservicesGitRestService> gitServices;

	public MicroservicesGitRestService dispatchService(GitlabConfiguration gitConfiguration) {
		return gitServices.stream().filter(s -> s.supports(gitConfiguration)).findFirst().orElse(null);
	}
}