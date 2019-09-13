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
/**
MarketAssetHelper.java * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
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
package com.minsait.onesait.platform.controlpanel.controller.scalability;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.client.exception.MqttClientException;
import com.minsait.onesait.platform.controlpanel.controller.scalability.msgs.Injector;
import com.minsait.onesait.platform.controlpanel.controller.scalability.msgs.InjectorStatus;

import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class InsertionTask implements Runnable {

	private final Object lock = new Object();

	private final Client client;
	private final String ontology;
	private final String data;
	private final Injector injector;
	private final Date start;
	private final int delay;
	private final ConcurrentHashMap<Injector, InjectorStatus> statues;
	
	private static final long PERIODLIMIT = 5000l;
	
	private volatile boolean stop = false;
	private volatile int sent = 0;
	private volatile int sentPeriod = 0;
	private volatile int errors = 0;
	private volatile int errorsPeriod = 0;
	private volatile long timespent = 0;
	private volatile long timespentPeriod = 0;
	private volatile Date startPeriod;

	public InsertionTask(Client client, String ontology, String data, Injector injector, int delay,
			ConcurrentHashMap<Injector, InjectorStatus> statues) {
		this.client = client;
		this.ontology = ontology;
		this.data = data;
		this.injector = injector;
		this.delay = delay;
		start = new Date();
		startPeriod = start;
		this.statues = statues;
	}

	@Override
	public void run() {
		while (!stop) {
			synchronized (lock) {
				try {
					sent++;
					sentPeriod++;
					final Date ini = new Date();
					client.insertInstance(ontology, data);
					final Date end = new Date();
					final long time = end.getTime() - ini.getTime();
					timespent = timespent + time;
					timespentPeriod = timespentPeriod + time;

					updateStatus();

					// automatic stop after 10 minutes
					if (600000 < (end.getTime() - start.getTime())) {
						stop = true;
					}

				} catch (final Exception e) {
					log.error("Error inserting data", e);
					errors++;
					errorsPeriod++;
				}
			}
			try {
				Thread.sleep(delay);
			} catch (final Exception e) {
				log.error("Error sleeping task");
			}
		}
		try {
			client.disconnect();
		} catch (final MqttClientException e) {
			log.error("Mqtt exception thrown {}", e.getMessage());
		}

	}

	public void stop() {
		synchronized (lock) {
			stop = true;
		}
	}

	public boolean isStopped() {
		synchronized (lock) {
			return stop;
		}
	}

	private float getThroughput() {
		final float time = timespent / 1000.0f;
		return (sent - errors) / time;
	}

	private long runningTime(Date now) {
		return now.getTime() - start.getTime();
	}

	private void updateStatus() {
		final Date now = new Date();
		final long period = now.getTime() - startPeriod.getTime();
		if (PERIODLIMIT < period) {
			// push new status
			final float time = timespentPeriod / 1000f;
			final float throughputPeriod = (sentPeriod - errorsPeriod) / time;

			final InjectorStatus status = new InjectorStatus(injector.getInjector(), sent, errors, getThroughput(),
					runningTime(now), throughputPeriod, client.getProtocol());
			statues.put(injector, status);

			// start a new Period
			startPeriod = new Date();
			sentPeriod = 0;
			errorsPeriod = 0;
			timespentPeriod = 0;
		}
	}

}
