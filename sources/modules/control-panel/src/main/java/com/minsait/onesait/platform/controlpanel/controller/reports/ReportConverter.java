/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.reports;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.controlpanel.controller.reports.model.ReportDto;
import com.minsait.onesait.platform.controlpanel.services.report.UploadFileException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportConverter {

	@Autowired
	private AppWebUtils appWebUtils;

	public Report convert(ReportDto report) {
		log.debug("INI. Convert entity Report: {}  -->  ReportDto");

		if (report.getFile().isEmpty()) {
			log.error("Report template musbe not empty");
			throw new UploadFileException("Report template musbe not empty");
		}

		final Report entity = new Report();

		entity.setIdentification(report.getIdentification());
		entity.setDescription(report.getDescription());
		entity.setIsPublic(report.getIsPublic());
		entity.setFile(getReportBytes(report.getFile()));
		entity.setExtension(getReportExtension(report.getFile()));

		// Inner
		entity.setActive(Boolean.TRUE);
		entity.setUser(findUser());

		return entity;
	}

	public ReportDto convert(Report report) {
		log.debug("INI. Convert entity Report: {}", report);

		final ReportDto reportDto = ReportDto.builder().id(report.getId()).identification(report.getIdentification())
				.description(report.getDescription()).owner(report.getUser().getUserId()).created(report.getCreatedAt())
				.isPublic(report.getIsPublic()).dataSourceUrl(report.getDataSourceUrl()).build();

		log.debug("END. Converted ReportDto: {}", reportDto);

		return reportDto;
	}

	public Report merge(Report target, ReportDto source) {
		final Report entity = target;

		entity.setIdentification(source.getIdentification());
		entity.setDescription(source.getDescription());
		entity.setIsPublic(source.getIsPublic());
		if (!source.getFile().isEmpty()) {
			entity.setFile(getReportBytes(source.getFile()));
			entity.setExtension(getReportExtension(source.getFile()));
		}
		entity.setDataSourceUrl(source.getDataSourceUrl());

		return entity;
	}

	// -- Inner methods -- //
	private byte[] getReportBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (final IOException e) {
			throw new UploadFileException();
		}
	}

	private ReportExtension getReportExtension(MultipartFile file) {

		return ReportExtension.valueOf(FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase());
	}

	private User findUser() {
		final User user = new User();
		user.setUserId(appWebUtils.getUserId());
		return user;
	}

}
