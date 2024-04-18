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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.model.Tenant;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbExportDeleteService;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbMaintenanceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableScheduling
public class RtdbMaintainerJob {
	@Value("${onesaitplatform.database.elasticsearch.database:onesaitplatform_rtdb_es}")
	private String onesaitplatformRtdbEs;
	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String onesaitplatformRtdb;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private RtdbExportDeleteService rtdbExportDeleteService;
	@Autowired
	private VerticalRepository verticalRepository;

	@Autowired
	private RtdbMaintenanceService maintenanceService;

	@Value("${onesaitplatform.rtdb-opsclean-kill}")
	private int killThreshold;

	private static final int CORE_POOL_SIZE = 10;
	private static final int MAXIMUM_THREADS = 15;
	private static final long KEEP_ALIVE = 20;
	private static final long DEFAULT_TIMEOUT = 10;

	public void execute(JobExecutionContext context) throws InterruptedException {

		final List<Vertical> verticals = verticalRepository.findAll();
		verticals.forEach(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());

			final List<Tenant> tenants = new ArrayList<>(v.getTenants());
			final String verticalSchema = v.getSchema();

			final List<Ontology> ontologies = ontologyService.getCleanableOntologies().stream()
					.filter(o -> o.getRtdbCleanLapse().getMilliseconds() > 0).collect(Collectors.toList());

			if (!ontologies.isEmpty()) {

				final TimeUnit timeUnit = (TimeUnit) context.getJobDetail().getJobDataMap().get("timeUnit");
				long timeout = context.getJobDetail().getJobDataMap().getLongValue("timeout");
				if (timeout == 0)
					timeout = DEFAULT_TIMEOUT;

				final BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(ontologies.size());
				final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_THREADS, KEEP_ALIVE,
						TimeUnit.SECONDS, blockingQueue);

				final List<CompletableFuture<String>> futureList = ontologies.stream()
						.map(o -> CompletableFuture.supplyAsync(() -> {
							MultitenancyContextHolder.setVerticalSchema(verticalSchema);
							String query = null;
							for (final Tenant t : tenants) {
								// TO-DO identify tenant holder of ontology schema in rtdb
								MultitenancyContextHolder.setTenantName(t.getName());
								query = rtdbExportDeleteService.performExport(o);
								rtdbExportDeleteService.performDelete(o, query);
							}
							return query;
						}, executor)).collect(Collectors.toList());

				final CompletableFuture<Void> globalResut = CompletableFuture
						.allOf(futureList.toArray(new CompletableFuture[futureList.size()]));

				try {

					globalResut.get(timeout, timeUnit);

				} catch (ExecutionException | RuntimeException e) {
					log.error("Error while trying to export and delete ontologies", e);
				} catch (final TimeoutException e) {
					log.error("Timeout Exception while executing batch job Rtdb Maintainer", e);
				} catch (final Exception e) {
					log.error("Exception while executing rtdb maintenance process", e);
				}

				maintenanceService.getTmpGenCollections().stream()
						.forEach(s -> maintenanceService.deleteTmpGenCollection(s));

			}
		});

	}

	@Scheduled(fixedDelayString = "${onesaitplatform.rtdb-opsclean-delay}")
	public void killStuckOps() {
		maintenanceService.getCurrentOpsGT(killThreshold).stream().forEach(l -> {
			maintenanceService.killOp(l);
		});

	}

}
