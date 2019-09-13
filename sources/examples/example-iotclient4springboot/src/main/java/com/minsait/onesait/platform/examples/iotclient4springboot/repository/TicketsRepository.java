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

import java.util.List;

import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDelete;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerDynamicQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerInsert;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerParam;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery.QueryOf;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerUpdate;
import com.minsait.onesait.platform.client.springboot.fromjson.BulkResult;
import com.minsait.onesait.platform.client.springboot.fromjson.DeleteResult;
import com.minsait.onesait.platform.client.springboot.fromjson.UpdateResult;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import com.minsait.onesait.platform.examples.iotclient4springboot.model.TicketOntology;

@IoTBrokerRepository("Ticket")
public interface TicketsRepository {

	@IoTBrokerQuery("select * from Ticket")
	List<TicketOntology> getAllTickets();

	@IoTBrokerQuery("select * from Ticket where contextData.user='$user'")
	List<TicketOntology> getTicketsByUser(@IoTBrokerParam("$user") String user);

	@IoTBrokerQuery("select _id,* from Ticket")
	List<TicketOntology> getAllFieldsOfTickets();

	@IoTBrokerQuery("select _id,Ticket.status from Ticket")
	List<TicketOntology> getFieldsOfTickets();

	@IoTBrokerQuery("select * from Ticket where _id=OID(\"$id\")")
	TicketOntology getTicketById(@IoTBrokerParam("$id") String id);

	@IoTBrokerQuery
	List<TicketOntology> getTicketByDynamicQuery(@IoTBrokerDynamicQuery String query);

	@IoTBrokerQuery
	List<TicketOntology> getTicketByDynamicQuery(@IoTBrokerDynamicQuery String query,
			@IoTBrokerParam("$user") String user);

	@IoTBrokerQuery(value = "{\"Ticket.status\":\"PENDING\"}", queryType = SSAPQueryType.NATIVE)
	List<TicketOntology> getTicketsPendingByNativeQuery();

	@IoTBrokerQuery(value = "db.Ticket.find({'Ticket.status':'PENDING'})", queryType = SSAPQueryType.NATIVE)
	List<TicketOntology> getTicketsPendingByNativeQueryAnotherWay();

	@IoTBrokerQuery(value = "{'contextData.user':'developer'},{$set: {'Ticket.status':'$status'}}", queryType = SSAPQueryType.NATIVE, is = QueryOf.UPDATE)
	void updateStatusOfTickets(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery(value = "{'contextData.user':'developer'},{$set: {'Ticket.status':'$status'}}", queryType = SSAPQueryType.NATIVE, is = QueryOf.UPDATE)
	UpdateResult updateStatusOfTicketsWithIds(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery(value = "{'Ticket.status':'$status'}", queryType = SSAPQueryType.NATIVE, is = QueryOf.DELETE)
	void deleteTicketByStatus(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery(value = "{'Ticket.status':'$status'}", queryType = SSAPQueryType.NATIVE, is = QueryOf.DELETE)
	DeleteResult deleteTicketByStatusWithIds(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery(value = "{'Ticket.status':'$status'}", queryType = SSAPQueryType.NATIVE, is = QueryOf.QUERY)
	List<TicketOntology> selectTicketByStatus(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery("select count(*) from Ticket")
	int getNumberOfTickets();

	@IoTBrokerQuery(value = "db.Ticket.count({'Ticket.status':'$status'})", queryType = SSAPQueryType.NATIVE, is = QueryOf.QUERY)
	int getNumberOfTicketsNative(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery(value = "db.Ticket.count({})", queryType = SSAPQueryType.NATIVE, is = QueryOf.QUERY)
	int countAllTicketsNative();

	@IoTBrokerInsert
	String insertTicket(TicketOntology ticket);

	@IoTBrokerInsert
	String insertTickets(List<TicketOntology> tickets);

	@IoTBrokerInsert
	BulkResult insertTicketsResponseAsObject(List<TicketOntology> tickets);

	@IoTBrokerUpdate
	void updateTicket(String idTicket, TicketOntology ticket);

	@IoTBrokerUpdate
	UpdateResult updateTicketWithResult(String idTicket, TicketOntology ticket);

	@IoTBrokerDelete
	void deleteTicket(String id);

	@IoTBrokerDelete
	DeleteResult deleteTicketWithResult(String id);

	@IoTBrokerQuery(value = "UPDATE Ticket set Ticket.status = '$status' WHERE Ticket.status = 'STATUS_FAKE'", queryType = SSAPQueryType.SQL, is = QueryOf.UPDATE)
	UpdateResult updateBySQL(@IoTBrokerParam("$status") String status);

	@IoTBrokerQuery(value = "DELETE FROM Ticket WHERE Ticket.status = '$status'", queryType = SSAPQueryType.SQL, is = QueryOf.DELETE)
	DeleteResult deleteBySQL(@IoTBrokerParam("$status") String type);

}
