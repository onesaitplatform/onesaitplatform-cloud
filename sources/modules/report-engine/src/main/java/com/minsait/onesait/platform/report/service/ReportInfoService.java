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
package com.minsait.onesait.platform.report.service;

import java.io.InputStream;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.dto.report.ReportInfoDto;
import com.minsait.onesait.platform.config.dto.report.ReportType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;

public interface ReportInfoService {

	ReportInfoDto extract(InputStream is, ReportExtension reportExtension);

	byte[] generate(Report entity, ReportType pdf, Map<String, Object> parameters);

	void updateResource(Report report, String fileId, MultipartFile file) throws Exception;
}
