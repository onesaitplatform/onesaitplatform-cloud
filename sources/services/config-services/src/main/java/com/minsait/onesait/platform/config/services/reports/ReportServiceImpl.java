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
package com.minsait.onesait.platform.config.services.reports;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ProjectResourceAccess.ResourceAccessType;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ReportRepository;
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
		log.debug("INI. Find report by Id: {}", id);
		return reportRepository.findOne(id);
	}

	@Transactional
	@Override
	public void saveOrUpdate(Report report) {
		log.debug("INI. Save report: {}", report);
		reportRepository.save(report);
	}

	@Transactional
	@Override
	public void disable(String id) {
		log.debug("INI. Disable report id: {}", id);
		final Report entity = reportRepository.findOne(id);

		if (entity != null) {
			log.debug("Disable > Find report {}", entity);
			entity.setActive(Boolean.FALSE);
			reportRepository.save(entity);
		}
	}

	@Transactional
	@Override
	public void delete(String id) {
		reportRepository.delete(id);

	}

	@Override
	public Report findByIdentificationOrId(String id) {
		return reportRepository.findByIdentificationOrId(id, id);
	}

	@Override
	public boolean hasUserPermission(String userId, Report report, ResourceAccessType accessType) {
		final User user = userService.getUser(userId);
		if (user != null) {
			if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString()) || user.equals(report.getUser())
					|| report.getIsPublic())
				return true;
			else
				return resourceService.hasAccess(userId, report.getId(), ResourceAccessType.MANAGE);

		}
		return false;
	}
}