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
package com.minsait.onesait.platform.persistence.hadoop.kudu.table;

import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.persistence.hadoop.hive.table.HiveColumn;

import lombok.Getter;
import lombok.Setter;

public class KuduTable {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private List<HiveColumn> columns = new ArrayList<>();

	@Getter
	@Setter
	private int numReplicas;

	@Getter
	@Setter
	private String addresses;
	
	@Getter
	@Setter
	private String[] partition;

	@Getter
	@Setter
	private String[] primarykey;
	
	@Getter
	@Setter
	private int npartitions;
	
	@Getter
	@Setter
	private boolean includeKudutableName;
	
	public KuduTable(String name, int numReplicas, String addresses, String[] primarykey, String[] partition, int npartitions, boolean includeKudutableName ) {
		super();
		this.name = name;
		this.numReplicas = numReplicas;
		this.addresses = addresses;
		this.partition = partition;
		this.npartitions = npartitions;
		this.primarykey = primarykey;
		this.includeKudutableName= includeKudutableName;
	}

	public String build() {
		StringBuilder sentence = new StringBuilder();

		sentence.append("CREATE TABLE IF NOT EXISTS ");
		sentence.append(name);
		sentence.append(" (");

		if (columns != null && !columns.isEmpty()) {
			int numOfColumns = columns.size();
			int i = 0;

			for (HiveColumn column : columns) {
				sentence.append(column.getName()).append(" ").append(column.getColumnType());

				if (column.isRequired()) {
					sentence.append(" NOT NULL");
				}

				if (i < numOfColumns - 1) {
					sentence.append(", ");
				}
				i++;
			}
		}
		sentence.append(",\n PRIMARY KEY (" + String.join(",", primarykey) + ")");
		sentence.append(")");
		if(npartitions > 1) {
			sentence.append(" PARTITION BY HASH(" + String.join(",", partition) + ")");
			sentence.append(" PARTITIONS " + npartitions);
		}
		sentence.append(" STORED AS KUDU ");
		sentence.append("TBLPROPERTIES(");
		sentence.append("'kudu.master_addresses' = '");
		sentence.append(addresses);
		sentence.append("',");
		
		if(includeKudutableName) {
			sentence.append("'kudu.table_name' = '" + name + "',");
		}
		
		sentence.append("'kudu.num_tablet_replicas' = '");
		sentence.append(numReplicas);
		sentence.append("'");
		sentence.append(");");

		return sentence.toString();
	}

}
