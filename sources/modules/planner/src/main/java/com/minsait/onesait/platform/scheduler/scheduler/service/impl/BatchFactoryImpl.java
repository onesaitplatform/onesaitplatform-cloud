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
package com.minsait.onesait.platform.scheduler.scheduler.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.scheduler.SchedulerType;
import com.minsait.onesait.platform.scheduler.scheduler.BatchScheduler;
import com.minsait.onesait.platform.scheduler.scheduler.service.BatchSchedulerFactory;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BatchFactoryImpl implements BatchSchedulerFactory {

	@Autowired
	List<BatchScheduler> schedulers;

	Map<String, BatchScheduler> schedulerMap;

	@Autowired
	public BatchFactoryImpl(List<BatchScheduler> schedulers) {

		this.schedulers = schedulers;

		schedulerMap = schedulers.stream().collect(Collectors.toMap(x -> {
			try {
				return x.getSchedulerName();
			} catch (SchedulerException e) {
				log.error("error loading schedulers ", e);
			}
			return null;
		}, x -> x));

	}

	@Override
	public BatchScheduler getScheduler(SchedulerType schedulerType) throws NotFoundException {
		BatchScheduler scheduler = schedulerMap.get(schedulerType.getSchedulerName());

		if (scheduler == null) {
			throw new NotFoundException("Scheduler for type " + schedulerType + " not found");
		}

		return scheduler;

	}

	@Override
	public List<BatchScheduler> getSchedulers() {
		return schedulers;
	}

	@Override
	public BatchScheduler getScheduler(String schedulerName) throws NotFoundException {
		return getScheduler(SchedulerType.valueOf(schedulerName));
	}

}
