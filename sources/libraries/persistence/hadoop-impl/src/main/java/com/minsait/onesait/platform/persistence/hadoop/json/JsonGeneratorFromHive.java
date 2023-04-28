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
package com.minsait.onesait.platform.persistence.hadoop.json;

import java.util.List;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.persistence.hadoop.util.HiveFieldType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JsonGeneratorFromHive {

	public JsonSchemaHive parse(String name, List<DescribeColumnData> columns) {

		log.debug("parse hive table named " + name);
		JsonSchemaHive schema = new JsonSchemaHive();
		schema.setName(name);

		for (DescribeColumnData column : columns) {
			JsonType jsonType = null;
			switch (column.getDataType()) {
			case HiveFieldType.STRING_FIELD:
				jsonType = new StringType(column.getColName());
				break;
			case HiveFieldType.INTEGER_FIELD:
			case HiveFieldType.BIGINT_FIELD:
				jsonType = new IntegerType(column.getColName());
				break;
			case HiveFieldType.FLOAT_FIELD:
			case HiveFieldType.DOUBLE_FIELD:
				jsonType = new NumberType(column.getColName());
				break;
			case HiveFieldType.TIMESTAMP_FIELD:
				jsonType = new TimestampType(column.getColName());
				break;
			case HiveFieldType.BOOLEAN_FIELD:
				jsonType = new BooleanType(column.getColName());
				break;
			default:
				jsonType = new StringType(column.getColName());
				break;
			}

			schema.getFields().add(jsonType);
		}
		return schema;
	}

}
