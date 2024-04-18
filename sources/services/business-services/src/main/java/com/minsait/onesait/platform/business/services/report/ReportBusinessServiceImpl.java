/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.business.services.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.business.services.binaryrepository.factory.BinaryRepositoryServiceFactory;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.reports.ReportService;

@Service
public class ReportBusinessServiceImpl implements ReportBusinessService {

	@Autowired
	private BinaryRepositoryServiceFactory binaryFactory;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private ReportService reportService;

	@Override
	public void updateResource(Report report, String fileId, MultipartFile file) throws Exception {
		report.getResources().removeIf(r -> r.getId().equals(fileId));
		binaryFactory.getInstance(RepositoryType.MONGO_GRIDFS).updateBinary(fileId, file, null);
		report.getResources().add(binaryFileService.getFile(fileId));
		reportService.saveOrUpdate(report);

	}

}
