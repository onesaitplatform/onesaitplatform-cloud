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
package com.minsait.onesait.platform.config.versioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class RestoreReport {

	public enum OperationResult{
		SUCCESS, FAILED
	}

	private List<String> errors = new ArrayList<>();
	private String resultMessage;
	private boolean finished = false;
	private String executionId;
	private Long initTime;
	private Long endTime;
	private Long timeTaken;
	private OperationResult operationResult;
	private int versionablesInRepository = 0;
	private Map<String, Set<String>> excludeResources = new HashMap<>();
	private Set<String> usersToBeRemoved = new HashSet<>();
}
