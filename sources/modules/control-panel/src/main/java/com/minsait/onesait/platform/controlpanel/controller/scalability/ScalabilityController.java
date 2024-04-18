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
package com.minsait.onesait.platform.controlpanel.controller.scalability;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.controlpanel.controller.scalability.msgs.BasicMsg;
import com.minsait.onesait.platform.controlpanel.controller.scalability.msgs.Connection;
import com.minsait.onesait.platform.controlpanel.controller.scalability.msgs.Injector;
import com.minsait.onesait.platform.controlpanel.controller.scalability.msgs.InjectorStatus;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/scalability")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
@Slf4j
public class ScalabilityController {

	private Object lock = new Object();

	private ConcurrentHashMap<Injector, InsertionTask> tasks = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Injector, InjectorStatus> statues = new ConcurrentHashMap<>();
	private Connection connection;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private BeanFactory beanFactory;

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/connection", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Connection> setConnection(@RequestBody Connection connection) {
		synchronized (lock) {
			if (tasks.size() <= 0) {
				this.connection = connection;
				return new ResponseEntity<>(connection, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@PostMapping(value = "/startInjector", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Injector> startInjector(@RequestParam int injector,
			@RequestParam String protocol, @RequestParam int delay, @RequestBody String data) throws IOException {

		Client client;
		Injector inject = null;
		InsertionTask task;

		int appliedDelay = delay;

		synchronized (lock) {
			try {
				switch (protocol) {
				case "mqtt":
					client = new ClientMqttWrapper(connection.getMqttURL());
					break;
				case "rest":
					client = new ClientRestWrapper(connection.getRestURL());
					break;
				case "kafka":
					client = new ClientKafkaProducerWrapper(connection.getKafkaURL());
					if (appliedDelay < 200) {
						appliedDelay = 200;
					}
					break;
				default:
					return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
				client.connect(connection.getToken(), connection.getClientPlatform(),
						connection.getClientPlatformInstance() + "-" + injector, true);
				inject = new Injector(injector, data);
				task = beanFactory.getBean(InsertionTask.class, client, connection.getOntology(), data, inject,
						appliedDelay, statues);
				tasks.putIfAbsent(inject, task);
				InjectorStatus emptyStatus = new InjectorStatus(injector, 0, 0, 0.0f, 0l, 0.0f, protocol);
				statues.putIfAbsent(inject, emptyStatus);
			} catch (Exception e) {
				log.error("Error connectiong with the server", e);
				if (inject != null) {
					tasks.remove(inject);
					statues.remove(inject);
				}
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			taskExecutor.execute(task);
		}

		return new ResponseEntity<>(inject, HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Collection<InjectorStatus>> getStatus() {
		Collection<InjectorStatus> values = statues.values();
		return new ResponseEntity<>(values, HttpStatus.OK);
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/getDataConnection", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<Connection> getDataConnection() {
		synchronized (lock) {
			return new ResponseEntity<>(connection, HttpStatus.OK);
		}
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/stopInjector", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<BasicMsg> stopInjector(@RequestParam int injector) {
		Injector inj = new Injector(injector, null);
		InsertionTask task = tasks.get(inj);
		if (task != null) {
			task.stop();
		}
		tasks.remove(inj);
		statues.remove(inj);
		BasicMsg msg = new BasicMsg("Injector Removed");
		return new ResponseEntity<>(msg, HttpStatus.OK);
	}
}
