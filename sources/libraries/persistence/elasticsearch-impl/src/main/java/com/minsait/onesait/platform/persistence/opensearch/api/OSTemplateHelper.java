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
package com.minsait.onesait.platform.persistence.opensearch.api;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonObject;
import com.minsait.onesait.platform.config.model.OntologyElastic;

public class OSTemplateHelper { 
	
	static String getValueFromInstanceField(JsonObject instanceObject, String field) {
		final String[] fields = field.split("\\.");
		for (int i = 0; i < fields.length - 1; i++) {
			instanceObject = instanceObject.getAsJsonObject(fields[i]);
		}
		return instanceObject.get(fields[fields.length - 1]).getAsString();

	}

	static String getIndexFromInstance(OntologyElastic ontology, JsonObject instanceObject) {

		final String PATTERN_SEPARATOR = "-";
		String index = "";
		DateTime dateTime = null;
		final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
				.withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC());
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ontology.getOntologyId().getIdentification()).append(PATTERN_SEPARATOR);

		// get value from field
		String fieldValue = getValueFromInstanceField(instanceObject, ontology.getPatternField());

		switch (ontology.getPatternFunction()) {
		case NONE:
			index = stringBuilder.append(fieldValue).toString().toLowerCase();
			break;
		case SUBSTR:
			if (fieldValue.length() <= ontology.getSubstringEnd()) {
				// TODO: THrow exception out of bounds index
			}
			final int endIndex = ontology.getSubstringEnd() == -1 ? fieldValue.length() : ontology.getSubstringEnd();
			fieldValue = fieldValue.substring(ontology.getSubstringStart(), endIndex);
			index = stringBuilder.append(fieldValue).toString().toLowerCase();
			break;
		case YEAR:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(dateTime.getYear()).toString().toLowerCase();
			break;
		case YEAR_MONTH:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(dateTime.getYear()).append(PATTERN_SEPARATOR)
					.append(String.format("%02d", dateTime.getMonthOfYear())).toString().toLowerCase();
			break;
		case YEAR_MONTH_DAY:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(dateTime.getYear()).append(PATTERN_SEPARATOR)
					.append(String.format("%02d", dateTime.getMonthOfYear())).append(PATTERN_SEPARATOR)
					.append(String.format("%02d", dateTime.getDayOfMonth())).toString().toLowerCase();
			break;
		case MONTH:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(String.format("%02d", dateTime.getMonthOfYear())).toString().toLowerCase();
			break;
		case DAY:
			dateTime = DateTime.parse(fieldValue, formatter);
			index = stringBuilder.append(String.format("%03d", dateTime.getDayOfYear())).toString().toLowerCase();
			break;
		default:
			// TODO: Throw exception
		}
		return index;
	}

}
