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

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.binaryrepository.exception.BinaryRepositoryException;
import com.minsait.onesait.platform.business.services.binaryrepository.factory.BinaryRepositoryServiceFactory;
import com.minsait.onesait.platform.config.dto.report.ReportDto;
import com.minsait.onesait.platform.config.dto.report.ReportResourceDTO;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFile.RepositoryType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Report.ReportExtension;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.binaryfile.BinaryFileService;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportConverter {

	@Autowired
	private BinaryRepositoryServiceFactory binaryFactory;
	@Autowired
	private BinaryFileService binaryFileService;
	@Autowired
	private IntegrationResourcesService resourcesService;

	private String storage;

	@PostConstruct
	public void init() {
		try {
			storage = resourcesService.getGlobalConfiguration().getEnv().getReport().get("resource-storage").toString();
		} catch (Exception e) {
			storage = RepositoryType.MONGO_GRIDFS.name();
			log.warn("resource-storage property not found, chose GRIDFS by default {}", e);
		}
	}

	public Report convert(ReportDto report) {
		log.debug("INI. Convert entity Report: {}  -->  ReportDto");

		if (report.getFile().isEmpty()) {
			log.error("Report template must be non empty");
			throw new OPResourceServiceException("Report template must be non empty");
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

		final Set<BinaryFile> resources = report.getAdditionalFiles().stream().map(mf -> {
			if (!mf.isEmpty()) {
				try {
					final String fileId = binaryFactory.getInstance(RepositoryType.valueOf(storage)).addBinary(mf, null,
							null);
					return binaryFileService.getFile(fileId);
				} catch (final Exception e) {
					log.error("Could not add file {}", mf.getOriginalFilename());

				}
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toSet());
		entity.getResources().addAll(resources);
		return entity;
	}

	public ReportDto convert(Report report) {
		if (log.isDebugEnabled()) {
			log.debug("INI. Convert entity Report: {}", report);
		}
		
		final ReportDto reportDto = ReportDto.builder().id(report.getId()).identification(report.getIdentification())
				.description(report.getDescription()).owner(report.getUser().getUserId()).created(report.getCreatedAt())
				.fileName(report.getIdentification() + "." + report.getExtension().toString().toLowerCase())
				.resources(report.getResources().stream()
						.map(r -> ReportResourceDTO.builder().id(r.getId()).fileName(r.getFileName()).build())
						.collect(Collectors.toList()))
				.isPublic(report.getIsPublic()).dataSourceUrl(report.getDataSourceUrl()).build();

		if (log.isDebugEnabled()) {
			log.debug("END. Converted ReportDto: {}", reportDto);
		}

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
		source.getAdditionalFiles().forEach(r -> {
			if (!r.isEmpty()) {
				target.getResources().removeIf(bf -> bf.getFileName().equalsIgnoreCase(r.getOriginalFilename()));
				try {
					final String fileId = binaryFactory.getInstance(RepositoryType.valueOf(storage)).addBinary(r, null,
							null);
					target.getResources().add(binaryFileService.getFile(fileId));
				} catch (BinaryRepositoryException | IOException e) {
					log.error("Could not add file {}", r.getOriginalFilename());
				}
			}
		});
		return entity;
	}

	// -- Inner methods -- //
	private byte[] getReportBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (final IOException e) {
			throw new OPResourceServiceException("Error getting bytes of input file");
		}
	}

	private ReportExtension getReportExtension(MultipartFile file) {

		return ReportExtension.valueOf(FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase());
	}

	private User findUser() {
		final User user = new User();
		user.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		return user;
	}

}
