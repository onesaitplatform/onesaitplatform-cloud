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
package com.minsait.onesait.platform.config.services.reports;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.BinaryFileRepository;
import com.minsait.onesait.platform.config.repository.ReportRepository;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
	private ReportRepository reportRepository;
	@Autowired
	private UserService userService;

	@Autowired
	private OPResourceService resourceService;

	@Autowired
	private BinaryFileRepository binaryFileRepository;

	@Override
	public List<Report> findAllActiveReports() {
		return reportRepository.findByActiveTrue();
	}

	@Override
	public List<Report> findAllActiveReportsByUserId(String userId) {
		return reportRepository.findByUserAndActiveTrueOrIsPublicTrueAndActiveTrue(userService.getUser(userId));
	}

	@Override
	public Report findById(String id) {
		if (log.isDebugEnabled()) {
			log.debug("INI. Find report by Id: {}", id);
		}
		return reportRepository.findById(id).orElse(null);
	}

	@Transactional
	@Override
	public void saveOrUpdate(Report report) {
		if (log.isDebugEnabled()) {
			log.debug("INI. Save report: {}", report);
		}
		reportRepository.save(report);
	}

	@Transactional
	@Override
	public void disable(String id) {
		if (log.isDebugEnabled()) {
			log.debug("INI. Disable report id: {}", id);
		}
		final Report entity = reportRepository.findById(id).orElse(null);

		if (entity != null) {
			if (log.isDebugEnabled()) {
				log.debug("Disable > Find report {}", entity);
			}
			entity.setActive(Boolean.FALSE);
			reportRepository.save(entity);
		}
	}

	@Transactional
	@Override
	public void delete(String id) {
		reportRepository.deleteById(id);

	}

	@Override
	public Report findByIdentificationOrId(String id) {
		return reportRepository.findByIdentificationOrId(id, id);
	}

	@Override
	public boolean hasUserPermission(String userId, Report report, ResourceAccessType accessType) {
		final User user = userService.getUser(userId);
		if (user != null) {
			if (userService.isUserAdministrator(user) || user.equals(report.getUser()) || report.getIsPublic())
				return true;
			else
				return resourceService.hasAccess(userId, report.getId(), ResourceAccessType.MANAGE);

		}
		return false;
	}

	@Override
	public Collection<BinaryFile> findResourcesForUser(String userId) {

		List<Report> reports;
		if (userService.getUser(userId).isAdmin())
			reports = findAllActiveReports();
		else
			reports = findAllActiveReportsByUserId(userId);
		return reports.stream().map(Report::getResources).flatMap(Set::stream)
				.collect(Collectors.toMap(BinaryFile::getId, bf -> bf, (bf1, bf2) -> bf1)).values();

	}

	@Override
	public List<BinaryFile> findResourcesAvailableExcludingSelf(String userId, String reportId) {
		final Report report = findById(reportId);
		if (report != null) {
			return findResourcesForUser(userId).stream()
					.filter(bf -> report.getResources().stream().noneMatch(r -> r.getId().equals(bf.getId())))
					.collect(Collectors.toList());
		} else {
			throw new OPResourceServiceException("Report doesn't exist");
		}
	}

	@Override
	@Transactional
	public void addBinaryFileToResource(Report report, String binaryFileId) {
		final BinaryFile file = binaryFileRepository.findById(binaryFileId).orElse(null);
		if (file != null) {
			report.getResources().removeIf(bf -> bf.getId().equals(file.getId()));
			report.getResources().add(file);
			reportRepository.save(report);
		}
	}

	@Override
	public void deleteResource(Report report, String resourceId) {
		report.getResources().removeIf(r -> r.getId().equals(resourceId));
		saveOrUpdate(report);

	}

	@Override
	public int countAssociatedReportsToResource(String resourceId) {
		return reportRepository.countByResourceId(resourceId);
	}
}