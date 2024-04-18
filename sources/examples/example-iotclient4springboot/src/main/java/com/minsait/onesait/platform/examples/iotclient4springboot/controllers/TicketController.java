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
package com.minsait.onesait.platform.examples.iotclient4springboot.controllers;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.examples.iotclient4springboot.model.Greeting;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.TicketOntology;
import com.minsait.onesait.platform.examples.iotclient4springboot.repository.TicketsRepository;

@RestController
public class TicketController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@Autowired
	private TicketsRepository ticketRepository = null;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@RequestMapping("/getTickets")
	public List<TicketOntology> getTickets(@RequestParam(value = "name", defaultValue = "World") String name) {
		return ticketRepository.getAllTickets();
	}

	@RequestMapping("/getTicketsByStatus")
	public String getTicketsByStatus(@RequestParam(value = "status", defaultValue = "PENDING") String status) {
		return "";
	}
}
