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
package com.minsait.onesait.platform.config.services.dashboardconf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.repository.DashboardConfRepository;
import com.minsait.onesait.platform.config.services.exceptions.DashboardConfServiceException;

@Service
public class DashboardConfServiceImpl implements DashboardConfService {

	@Autowired
	private DashboardConfRepository dashboardConfRepository;

	@Override
	public List<DashboardConf> findAllDashboardConf() {
		return this.dashboardConfRepository.findAll();
	}

	@Override
	public List<String> getAllIdentifications() {
		List<DashboardConf> dashboardConfs = this.dashboardConfRepository.findAllByOrderByIdentificationAsc();
		List<String> names = new ArrayList<>();
		for (DashboardConf dc : dashboardConfs) {
			names.add(dc.getIdentification());
		}
		return names;
	}

	@Override
	public DashboardConf getDashboardConfById(String id) {
		return this.dashboardConfRepository.findById(id);
	}

	@Override
	public List<DashboardConf> getDashboardConfByIdentification(String identification) {
		return this.dashboardConfRepository.findByIdentification(identification);
	}

	@Override
	public void saveDashboardConf(DashboardConf dashboardConf) {
		try {
			dashboardConfRepository.save(dashboardConf);
		} catch (Exception e) {
			throw new DashboardConfServiceException("Cannot create dashboard configuration");
		}
	}

	@Override
	public void deleteDashboardConf(String id) {
		DashboardConf dc = this.dashboardConfRepository.findById(id);
		if (dc != null) {
			this.dashboardConfRepository.delete(dc);
		} else
			throw new DashboardConfServiceException("Cannot delete dashboard configuration that does not exist");
	}

}
