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
package com.minsait.onesait.platform.config.components;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogOntologyDeserializer extends StdDeserializer<LogOntology> {

	private static final long serialVersionUID = 1L;

	private static final String DEVICE = "device";
	private static final String STATUS = "status";
	private static final String OPTIONS = "extraOptions";
	private static final String MESSAGE = "message";
	private static final String TIMESTAMP = "timestamp";
	private static final String LOCATION = "location";
	private static final String LEVEL = "level";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String COORDINATES = "coordinates";
	private static final String COMMANDID = "commandId";

	public LogOntologyDeserializer() {
		this(null);
	}

	public LogOntologyDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public LogOntology deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

		JsonNode node = p.getCodec().readTree(p);

		String device = node.get(DEVICE).asText();
		String status = node.get(STATUS).asText();
		String level = node.get(LEVEL).asText();
		String message = node.get(MESSAGE).asText();
		String extraOptions = null;
		if (node.get(OPTIONS) != null)
			extraOptions = node.get(OPTIONS).asText();
		Point2D.Double location = null;
		if (node.get(LOCATION) != null)
			location = new Point2D.Double(node.get(LOCATION).get(COORDINATES).get(LATITUDE).asDouble(),
					node.get(LOCATION).get(COORDINATES).get(LONGITUDE).asDouble());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date timestamp = null;
		try {
			timestamp = df.parse(node.get(TIMESTAMP).asText());
		} catch (ParseException e) {
			log.error("Could not parse date");
		}
		String commandId = null;
		if (node.get(COMMANDID) != null)
			commandId = node.get(COMMANDID).asText();

		return new LogOntology(device, location, extraOptions, level, message, status, timestamp, commandId);
	}
}
