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
package com.minsait.onesait.platform.persistence.hadoop.hive.table;

import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.persistence.hadoop.hdfs.HdfsConst;

import lombok.Getter;
import lombok.Setter;

public class HiveTable {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private List<HiveColumn> columns = new ArrayList<>();

	@Getter
	@Setter
	private String hdfsDir;

	public String build() {
		StringBuilder sentence = new StringBuilder();

		sentence.append("CREATE EXTERNAL TABLE IF NOT EXISTS ");
		sentence.append(name);
		sentence.append(" (");

		if (columns != null) {
			int numOfColumns = columns.size();
			int i = 0;
			for (HiveColumn column : columns) {
				sentence.append(column.getName()).append(" ").append(column.getColumnType());
				if (i < numOfColumns - 1) {
					sentence.append(", ");
				}
				i++;
			}
		}

		sentence.append(") ROW FORMAT DELIMITED FIELDS TERMINATED BY '");
		sentence.append(HdfsConst.SEPARATOR_FIELD);
		sentence.append("' LINES TERMINATED BY '\n' STORED AS TEXTFILE LOCATION '");
		sentence.append(hdfsDir);
		sentence.append("'");

		// drop sentence

		return sentence.toString();
	}

}
