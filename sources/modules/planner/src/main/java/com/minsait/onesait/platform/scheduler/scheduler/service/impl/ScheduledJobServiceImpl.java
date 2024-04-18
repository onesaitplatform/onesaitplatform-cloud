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
package com.minsait.onesait.platform.scheduler.scheduler.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.domain.ScheduledJob;
import com.minsait.onesait.platform.scheduler.repository.ScheduledJobRepository;
import com.minsait.onesait.platform.scheduler.scheduler.service.ScheduledJobService;

@Service
public class ScheduledJobServiceImpl implements ScheduledJobService {

	@Autowired
	private ScheduledJobRepository scheduledJobRepository;

	@Override
	public List<ScheduledJob> getAllScheduledJobs() {
		return scheduledJobRepository.findAll();
	}

	@Override
	public List<ScheduledJob> getScheduledJobsByUsername(String username) {
		return scheduledJobRepository.findAllByUserId(username);
	}

	@Override
	public void createScheduledJob(ScheduledJob job) {
		scheduledJobRepository.save(job);
	}

	@Override
	public ScheduledJob findByJobName(String jobName) {
		return scheduledJobRepository.findByJobName(jobName);
	}

	@Override
	@Transactional
	@Modifying

	public void deleteById(Long id) {
		scheduledJobRepository.deleteById(id);
	}

}
