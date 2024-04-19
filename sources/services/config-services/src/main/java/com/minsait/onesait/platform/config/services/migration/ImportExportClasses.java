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
package com.minsait.onesait.platform.config.services.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@ConfigurationProperties(prefix = "onesaitplatform.migrationconfig")
@Component
public class ImportExportClasses {

	private List<String> classes = new ArrayList<>();

	public List<String> getClasses() {
		return classes;
	}

	public Set<String> getBlackList() {
		return Sets.newHashSet(classes);
	}

	public Set<String> getWhiteList() {
		return Sets.newHashSet(classes);
	}

	public Set<String> getTrimList() {
		return Sets.newHashSet(classes);
	}

	public Set<String> getBlackProjectlist() {
		return Sets.newHashSet(classes);
	}

}
