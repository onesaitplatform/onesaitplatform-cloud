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
package com.minsait.onesait.platform.persistence.presto.generator.model.common;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class HistoricalOptions implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Getter
	@Setter
	@NotNull
	private String fileFormat;
	@Getter
	@Setter
	@NotNull
	private String escapeCharacter;
	@Getter
	@Setter
	@NotNull
	private String quoteCharacter;
	@Getter
	@Setter
	private String separatorCharacter;
	@Getter
	@Setter
	private List<String> partitions;
	@Getter
	@Setter
	private String externalLocation;
	
	private static final String FORMAT = "FORMAT";
	private static final String CSV_ESCAPE = "CSV_ESCAPE";
	private static final String CSV_QUOTE = "CSV_QUOTE";
	private static final String CSV_SEPARATOR = "CSV_SEPARATOR";
	private static final String PARTITIONED_BY = "PARTITIONED_BY";
	private static final String EXTERNAL_LOCATION = "EXTERNAL_LOCATION";
	private static final String ARRAY = "ARRAY";
	private static final String EQUAL = "=";
	private static final String COMMA = ",";
	private static final String OPEN_PARENTHESIS = "(";
	private static final String CLOSE_PARENTHESIS = ")";
	private static final String OPEN_ARRAY = "[";
	private static final String CLOSE_ARRAY = "]";
	private static final String WITH = "WITH";
	private static final String QUOTE = "'";
	private static final String RN = "\r\n";
	
	public List<String> buildHistoricalOptions() {
		List<String> options = new ArrayList<>();

		options.add(RN + WITH);
		options.add(OPEN_PARENTHESIS);
		
		options.addAll(createOptionPartitions());
		options.addAll(createOption(FORMAT, fileFormat));
		options.addAll(createOption(CSV_ESCAPE, escapeCharacter));
		options.addAll(createOption(CSV_QUOTE, quoteCharacter));
		options.addAll(createOption(CSV_SEPARATOR, separatorCharacter));
		options.addAll(createOption(EXTERNAL_LOCATION, externalLocation));
		options.remove(options.size()-1);

		options.add(CLOSE_PARENTHESIS);

		return options;
	}
	
	private List<String> createOption(String key, String value) {
		List<String> str = new ArrayList<>();
		if (value != null && !value.isEmpty()) {
			str.add(RN);
			str.add(key);
			str.add(EQUAL);
			str.add(QUOTE + value + QUOTE);
			str.add(COMMA);
		}
		return str;
	}
	
	private List<String> createOptionPartitions() {
		List<String> str = new ArrayList<>();
		if (partitions != null && !partitions.isEmpty()) {
			str.add(RN);
			str.add(PARTITIONED_BY);
			str.add(EQUAL);
			str.add(ARRAY);
			str.add(OPEN_ARRAY);
			for (String partition: partitions) {
				str.add(QUOTE + partition + QUOTE);
				str.add(COMMA);	
			}
			str.remove(str.size()-1);
			str.add(CLOSE_ARRAY);	
			str.add(COMMA);
		}
		return str;
	}
	//			statement.setTableOptionsStrings(historicalOptions.buildHistoricalOptions());

}
