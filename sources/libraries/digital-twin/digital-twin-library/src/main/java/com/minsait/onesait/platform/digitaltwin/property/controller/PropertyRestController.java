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
package com.minsait.onesait.platform.digitaltwin.property.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.digitaltwin.event.manager.EventManager;
import com.minsait.onesait.platform.digitaltwin.status.IDigitalTwinStatus;
import com.minsait.onesait.platform.digitaltwin.transaction.TransactionManager;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(value = "/properties")
public class PropertyRestController {

	@Autowired
	private IDigitalTwinStatus digitalTwinStatus;

	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private EventManager eventManager;

	@RequestMapping(value = "/{propertyName}", method = RequestMethod.GET)
	public Response getProperty(@PathVariable("propertyName") String propertyName) {
		try {
			if (digitalTwinStatus.validate(OperationType.OUT, propertyName)) {
				return Response.ok(digitalTwinStatus.getProperty(propertyName).toString()).build();
			} else {
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} catch (Exception e) {
			return Response.status(Status.FORBIDDEN).build();
		}
	}

	@RequestMapping(value = "/{propertyName}", method = RequestMethod.PUT)
	public Response setProperty(@PathVariable("propertyName") String propertyName, @RequestBody String property,
			HttpServletRequest request) {

		if (digitalTwinStatus.validate(OperationType.IN, propertyName)) {
			try {
				String idTransaction = request.getHeader("Transaction-Id");
				JSONObject propJSON = new JSONObject(property);
				if (idTransaction != null && idTransaction != "") {
					transactionManager.setProperty(propertyName, property, idTransaction);
				} else {
					// MODIFIED O WORK WITH PROPERTIES OF TYPE ONTOLOGY --> UPDATE SHADOW ONLY THE
					// PROPERTY SETTED
					JSONArray jsonArrayProperties = propJSON.getJSONArray(propertyName);
					digitalTwinStatus.setProperty(propertyName, jsonArrayProperties);

					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put(propertyName, jsonArrayProperties.toString());

					eventManager.updateShadow(properties);
				}
			} catch (JSONException e) {
				log.error("Invalid JSON property: " + property, e);
				return Response.status(Status.BAD_REQUEST).build();
			} catch (Exception e) {
				return Response.status(Status.FORBIDDEN).build();
			}
			return Response.ok().build();
		} else {
			log.error("Invalid Operation Type for property: " + propertyName);
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}
}
