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
package com.minsait.onesait.platform.rtdbmaintainer.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;
import com.minsait.onesait.platform.rtdbmaintainer.audit.aop.RtdbMaintainerAuditable;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbExportDeleteService;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbToHdbService;

@Service
public class RtdbExportDeleteServiceImpl implements RtdbExportDeleteService {

	@Autowired
	private ManageDBPersistenceServiceFacade manageDBPersistenceServiceFacade;
	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsPersistenceServiceFacade;
	@Autowired
	private RtdbToHdbService rtdbToHdbService;
	@Value("${onesaitplatform.rtdb-to-hdb.path}")
	private String pathToExport;
	private static final String DEFAULT_PATH = "default";

	@Override
	@RtdbMaintainerAuditable
	public String performExport(Ontology ontology) {
		long millisecondsQuery = System.currentTimeMillis();
		if (ontology.getRtdbCleanLapse() != null)
			millisecondsQuery = millisecondsQuery - ontology.getRtdbCleanLapse().getMilliseconds();
		String path;

		if (ontology.isRtdbToHdb()) {
			final SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-MM-hh-mm");

			path = pathToExport + ontology.getUser().getUserId() + File.separator + ontology.getIdentification() + "_"
					+ format.format(new Date(millisecondsQuery)) + ".json";

		} else {
			path = DEFAULT_PATH;
		}
		final ExportData exportData = manageDBPersistenceServiceFacade.exportToJson(ontology.getRtdbDatasource(),
				ontology.getIdentification(), millisecondsQuery, path);

		if (ontology.isRtdbToHdb())
			rtdbToHdbService.postProcessExport(ontology, exportData);
		else
			rtdbToHdbService.deleteTmpFile(exportData);

		return exportData.getFilterQuery();
	}

	@Override
	@RtdbMaintainerAuditable
	public void performDelete(Ontology ontology, String query) {
		manageDBPersistenceServiceFacade.deleteAfterExport(ontology.getRtdbDatasource(), ontology.getIdentification(),
				query);

	}

	@Override
	public boolean hasOntologiesToExport(Ontology ontology) {
		long millisecondsQuery = System.currentTimeMillis();
		millisecondsQuery = millisecondsQuery - ontology.getRtdbCleanLapse().getMilliseconds();
		final long count = basicOpsPersistenceServiceFacade.countNative(ontology.getIdentification(),
				"{'contextData.timestampMillis':{$lte:" + millisecondsQuery + "}}");
		return (count > 0);
	}
}
