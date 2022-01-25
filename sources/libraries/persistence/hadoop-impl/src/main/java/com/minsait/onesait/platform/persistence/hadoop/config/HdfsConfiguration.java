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
package com.minsait.onesait.platform.persistence.hadoop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;
import com.minsait.onesait.platform.persistence.hadoop.hdfs.HdfsConst;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@Conditional(HadoopEnabledCondition.class)
public class HdfsConfiguration {

	@Getter
	@Value("${onesaitplatform.database.hdfs.url}${onesaitplatform.database.hdfs.basePath}")
	private String hdfsWorkingDirectory;

	@Getter
	@Value("${onesaitplatform.database.hdfs.ontologies.folder:ontologies}")
	private String ontologiesFolder;

	@Getter
	@Value("${onesaitplatform.database.hdfs.uploads.folder:uploads}")
	private String uploadsFolder;

	public String getAbsolutePath(String... relativePathSteps) {
		StringBuilder result = new StringBuilder(hdfsWorkingDirectory);
		for (String pathStep : relativePathSteps) {
			String normalizedPathStep = pathStep;
			if (normalizedPathStep.startsWith(HdfsConst.SEPARATOR_FIELD)) {
				normalizedPathStep = normalizedPathStep.substring(1, normalizedPathStep.length());
			}
			result.append(HdfsConst.SEPARATOR_FIELD + normalizedPathStep);
		}
		return result.toString();
	}

}
