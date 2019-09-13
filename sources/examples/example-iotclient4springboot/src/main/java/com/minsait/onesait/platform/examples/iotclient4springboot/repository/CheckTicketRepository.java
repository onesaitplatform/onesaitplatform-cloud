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
package com.minsait.onesait.platform.examples.iotclient4springboot.repository;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.springboot.fromjson.DeleteResult;
import com.minsait.onesait.platform.client.springboot.fromjson.UpdateResult;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.Ticket;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.TicketOntology;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CheckTicketRepository {

	@Autowired
	private TicketsRepository ticketRepository;

	@Autowired
	private TicketsRepositoryDynamicRepository ticketDynamicRepository;

	@PostConstruct
	public void testCRUD() {
		List<TicketOntology> tickets = null;
		try {

			ticketRepository.insertTickets(getTicketsOntologyStatusFake());
			UpdateResult upResult = ticketRepository.updateBySQL("FAKE");

			log.info("Modified " + upResult.getModified());

			DeleteResult delResult = ticketRepository.deleteBySQL("FAKE");

			log.info("Deleted " + delResult.getDeleted());

			ticketRepository.insertTickets(getTicketsOntology());
			log.info("Inserted 3 tickets");

			tickets = ticketDynamicRepository.getTicketByDynamicQuery("Ticket", "select _id,* from Ticket");
			log.info("tickets size: " + tickets.size());
			tickets = ticketDynamicRepository.getTicketByDynamicQuery("Ticket",
					"select _id,* from Ticket where contextData.user='$user'", "developer");
			log.info("tickets size: " + tickets.size());

			ticketRepository.updateStatusOfTickets("WORKING");
			tickets = ticketRepository.selectTicketByStatus("WORKING");
			log.info("There is " + tickets.size() + " with status:WORKING");

			ticketRepository.deleteTicketByStatus("WORKING");
			tickets = ticketRepository.selectTicketByStatus("WORKING");
			log.info("Deleted tickets in WORKING. Now there is " + tickets.size() + "with status:WORKING");

			TicketOntology tOnt = getTicketOntology();
			String idTicket = ticketRepository.insertTicket(tOnt);
			log.info("Ticket inserted");
			tickets = ticketRepository.getTicketsPendingByNativeQuery();
			log.info("tickets pending:" + tickets.size());

			log.info("tickets _id,*, returned:" + tickets.size());

			tickets = ticketRepository.getAllFieldsOfTickets();
			log.info("tickets _id,*, returned:" + tickets.size());
			tickets = ticketRepository.getFieldsOfTickets();
			log.info("tickets only several fields, returned:" + tickets.size());

			tickets = ticketRepository.getTicketsPendingByNativeQueryAnotherWay();
			log.info("tickets in native way, returned:" + tickets.size());

			int numberOfTickets = ticketRepository.getNumberOfTickets();
			log.info("At first there is " + numberOfTickets + " tickets");

			tOnt = getTicketOntology();
			idTicket = ticketRepository.insertTicket(tOnt);
			log.info("Ticket inserted");

			numberOfTickets = ticketRepository.getNumberOfTickets();
			log.info("Now there is " + numberOfTickets + " tickets");

			TicketOntology tInserted = ticketRepository.getTicketById(idTicket);

			tInserted.getTicket().setIdentification("IdChangedinUpdate");
			ticketRepository.updateTicket(idTicket, tInserted);
			TicketOntology tUpdated = ticketRepository.getTicketById(idTicket);
			if (!tUpdated.getTicket().getIdentification().equals("IdChangedinUpdate"))
				throw new Exception("Error en updated");

			tickets = ticketRepository.getAllTickets();
			log.info("Number of tickets: " + tickets.size());
			tickets = ticketRepository.getTicketsByUser("developer");
			log.info("Number of tickets: " + tickets.size());

			tickets = ticketRepository.getTicketByDynamicQuery("select _id,* from Ticket");
			log.info("Number of tickets: " + tickets.size());

			tickets = ticketRepository
					.getTicketByDynamicQuery("select _id,* from Ticket where contextData.user='$user'", "developer");
			log.info("Number of tickets: " + tickets.size());

			ticketRepository.deleteTicket(idTicket);
			int finalNumberOfTickets = ticketRepository.getNumberOfTickets();
			log.info("Finally there is " + finalNumberOfTickets + " tickets");
			if (numberOfTickets != finalNumberOfTickets + 1)
				throw new Exception("Error in number of tickets");

		} catch (Exception e) {
			log.error("Captured exception", e);
		}
	}

	private JsonNode getCoordinates() throws Exception {
		String jsonString = "{\"coordinates\":{\"latitude\":20.408,\"longitude\":12.371},\"type\":\"Point\"}}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(jsonString);
		return actualObj;
	}

	private JsonNode getFile() throws Exception {
		String jsonString = "{\"data\":\"\",\"media\":{\"name\":\"\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}}";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(jsonString);
		return actualObj;
	}

	private TicketOntology getTicketOntology() throws Exception {
		TicketOntology tOnt = new TicketOntology();
		Ticket ticket = new Ticket();
		ticket.setCoordinates(getCoordinates());
		ticket.setDescription("Example Ticket-" + System.currentTimeMillis());
		ticket.setEmail("lmgracia@indra,es");
		ticket.setFile(getFile());
		ticket.setIdentification("ExampleId-" + System.currentTimeMillis());
		ticket.setResponse_via("C/Tordo 30");
		ticket.setName("Example Ticket-" + System.currentTimeMillis());
		ticket.setStatus("PENDING");
		ticket.setType(null);
		tOnt.setTicket(ticket);
		return tOnt;
	}

	private List<TicketOntology> getTicketsOntology() throws Exception {
		List<TicketOntology> list = new ArrayList<>();
		list.add(getTicketOntology());
		list.add(getTicketOntology());
		list.add(getTicketOntology());
		return list;
	}

	private List<TicketOntology> getTicketsOntologyStatusFake() throws Exception {
		List<TicketOntology> list = new ArrayList<>();
		list.add(getTicketOntologyStatusFake());
		list.add(getTicketOntologyStatusFake());
		list.add(getTicketOntologyStatusFake());
		return list;
	}

	private TicketOntology getTicketOntologyStatusFake() throws Exception {
		TicketOntology tOnt = new TicketOntology();
		Ticket ticket = new Ticket();
		ticket.setCoordinates(getCoordinates());
		ticket.setDescription("Example Ticket-" + System.currentTimeMillis());
		ticket.setEmail("lmgracia@indra,es");
		ticket.setFile(getFile());
		ticket.setIdentification("ExampleId-" + System.currentTimeMillis());
		ticket.setResponse_via("C/Tordo 30");
		ticket.setName("Example Ticket-" + System.currentTimeMillis());
		ticket.setStatus("STATUS_FAKE");
		ticket.setType(null);
		tOnt.setTicket(ticket);
		return tOnt;
	}
}