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

import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_CLIENT_SESSION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE_TEMPLATE;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_TIMESTAMP;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_TIMEZONE_ID;
import static com.minsait.onesait.platform.persistence.hadoop.common.ContextDataNameFields.CONTEXT_DATA_FIELD_USER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.geometry.GeometryType;
import com.minsait.onesait.platform.persistence.hadoop.hive.table.HiveColumn;
import com.minsait.onesait.platform.persistence.hadoop.util.HiveFieldType;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonFieldType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KuduTableGenerator {

	@Value("${onesaitplatform.database.kudu.numreplicas:1}")
	private int numReplicas;

	@Value("${onesaitplatform.database.kudu.address:localhost:7051}")
	private String addresses;

	@Value("${onesaitplatform.database.kudu.includeKudutableName:false}")
	private boolean includeKudutableName;

	private static final String FORMAT = "format";
	private static final String PRIMARY_KEY = "primarykey";
	private static final String PARTITIONS = "partitions";

	public KuduTable builTable(String ontologyName, String schema, Map<String, String> config) {

		log.debug("generate kudu table for ontology " + ontologyName);

		final JSONObject schemaObj = new JSONObject(schema);

		final JSONObject props = getProperties(schemaObj);
		final List<String> requiredProps = getRequiredProps(schemaObj);
		if (props.length() == 1 && ((JSONObject) props.get(requiredProps.get(0))).has("$ref")
				&& ((JSONObject) props.get(requiredProps.get(0))).has("type")) {
			log.error("Error in the type of properties");
			throw new DBPersistenceException(
					"In KUDU ontology, the properties must be primitive, geometry or timestamp");
		} else {
			return build(ontologyName, props, requiredProps, config);
		}
	}

	public JSONObject getProperties(JSONObject jsonObj) {
		return jsonObj.getJSONObject(JsonFieldType.PROPERTIES_FIELD);
	}

	public List<String> getRequiredProps(JSONObject jsonObj) {

		final List<String> requiredProperties = new ArrayList<>();

		if (jsonObj.has("required")) {
			final JSONArray array = jsonObj.getJSONArray("required");

			for (int i = 0; i < array.length(); i++) {
				requiredProperties.add(array.getString(i));
			}
		}

		return requiredProperties;
	}

	public KuduTable build(String name, JSONObject props, List<String> requiredProperties, Map<String, String> cmap) {

		String[] primarykey;
		String[] partition;
		int npartitions;

		if (!name.equals(name.toLowerCase())) {// Check if field is lowercase
			log.error("Error naming the ontology");
			throw new DBPersistenceException("In KUDU ontology, ontology name must be lowercase");
		}

		if (cmap == null) {
			npartitions = 1;// No partition
			primarykey = new String[] { "_id" };
			partition = null;
		} else {
			npartitions = Integer.valueOf(cmap.get("npartitions"));

			if (!cmap.get(PRIMARY_KEY).equals(cmap.get(PRIMARY_KEY).toLowerCase())) {// Check if field is lowercase
				log.error("Error: primarykey fields must be lowercase");
				throw new DBPersistenceException("In KUDU ontology, the primarykey fields must be lowercase");
			}

			if (!cmap.get(PARTITIONS).equals(cmap.get(PARTITIONS).toLowerCase())) {// Check if field is lowercase
				log.error("Error: partitions fields must be lowercase");
				throw new DBPersistenceException("In KUDU ontology, the partitions fields must be lowercase");
			}

			primarykey = cmap.get(PRIMARY_KEY).trim().split("\\s*,\\s*");
			partition = cmap.get(PARTITIONS).trim().split("\\s*,\\s*");
		}

		final KuduTable table = new KuduTable(name, numReplicas, addresses, primarykey, partition, npartitions,
				includeKudutableName);

		@SuppressWarnings("unchecked")
		final Iterator<String> it = props.keys();

		final List<HiveColumn> columnsNoPK = new ArrayList<>();

		if (Arrays.stream(primarykey).anyMatch(JsonFieldType.PRIMARY_ID_FIELD::equals)) {
			table.getColumns().add(getPrimaryId());
		} else {
			columnsNoPK.add(getPrimaryId());
		}

		final List<HiveColumn> columnsContextData = getContexDataFields();

		for (final HiveColumn hc : columnsContextData) {
			if (Arrays.stream(primarykey).anyMatch(hc.getName()::equals)) {
				table.getColumns().add(hc);
			} else {
				columnsNoPK.add(hc);
			}
		}

		while (it.hasNext()) {
			final String key = it.next().trim();

			if (!key.equals(key.toLowerCase())) {// Check if field is lowercase
				log.error("Error: properties fields must be lowercase");
				throw new DBPersistenceException("In KUDU ontology, the properties must be lowercase. Field: " + key);
			}

			final JSONObject o = (JSONObject) props.get(key);

			if (Arrays.stream(primarykey).anyMatch(key::equals)) {
				if (isPrimitive(o)) {
					table.getColumns()
							.add(new HiveColumn(key, pickPrimitiveType(key, o), requiredProperties.contains(key)));
				} else {
					table.getColumns().addAll(pickType(key, o, requiredProperties));
				}
			} else {
				if (isPrimitive(o)) {
					columnsNoPK.add(new HiveColumn(key, pickPrimitiveType(key, o), requiredProperties.contains(key)));
				} else {
					columnsNoPK.addAll(pickType(key, o, requiredProperties));
				}
			}
		}

		table.getColumns().addAll(columnsNoPK);

		return table;
	}

	public boolean isPrimitive(JSONObject o) {
		final String jsonType = (String) o.get(JsonFieldType.TYPE_FIELD);
		return JsonFieldType.getPRIMITIVE_TYPES().contains(jsonType)
				&& !((o.has(FORMAT) && "date-time".equals(o.get(FORMAT))) && "string".equals(jsonType));
	}

	public boolean isGeometry(JSONObject o) {

		boolean result = false;

		try {
			final String jsonType = (String) o.get(JsonFieldType.TYPE_FIELD);

			if ((JsonFieldType.OBJECT_FIELD).equalsIgnoreCase(jsonType) && o.has(JsonFieldType.PROPERTIES_FIELD)
					&& o.getJSONObject(JsonFieldType.PROPERTIES_FIELD).has(JsonFieldType.TYPE_FIELD)
					&& o.getJSONObject(JsonFieldType.PROPERTIES_FIELD).getJSONObject(JsonFieldType.TYPE_FIELD)
							.has("enum")) {
				final JSONArray enume = o.getJSONObject(JsonFieldType.PROPERTIES_FIELD)
						.getJSONObject(JsonFieldType.TYPE_FIELD).getJSONArray("enum");
				final String point = enume.getString(0);

				result = GeometryType.POINT.name().equalsIgnoreCase(point);
			}
		} catch (final Exception e) {
			log.error("error checking if a object is a geometry");
		}
		return result;
	}

	public boolean isTimestamp(JSONObject o) {

		boolean result = false;

		try {
			final String jsonType = (String) o.get(JsonFieldType.TYPE_FIELD);

			if ((JsonFieldType.OBJECT_FIELD).equalsIgnoreCase(jsonType)
					&& o.get(JsonFieldType.PROPERTIES_FIELD) != null) {
				final JSONObject other = (JSONObject) o.get(JsonFieldType.PROPERTIES_FIELD);
				if (other.get("$date") != null) {
					result = true;
				}
			} else if (o.has(FORMAT) && "date-time".equals(o.get(FORMAT))
					&& JsonFieldType.STRING_FIELD.equals(jsonType)) {
				return true;
			}
		} catch (final Exception e) {
			log.error("error checking if a object is a timestamp");
		}
		return result;
	}

	public List<HiveColumn> pickType(String key, JSONObject o, List<String> requiredProperties) {

		final List<HiveColumn> columns = new ArrayList<>();

		if (isGeometry(o)) {

			columns.add(new HiveColumn(key + HiveFieldType.LATITUDE_FIELD, HiveFieldType.DOUBLE_FIELD,
					requiredProperties.contains(key)));
			columns.add(new HiveColumn(key + HiveFieldType.LONGITUDE_FIELD, HiveFieldType.DOUBLE_FIELD,
					requiredProperties.contains(key)));

		} else if (isTimestamp(o)) {
			columns.add(new HiveColumn(key, HiveFieldType.TIMESTAMP_FIELD, requiredProperties.contains(key)));
		} else {
			log.error("Error picking type");
			throw new DBPersistenceException(
					"In KUDU ontology, the properties must be primitive, geometry or timestamp");
		}

		return columns;
	}

	public String pickPrimitiveType(String key, JSONObject o) {
		String result = "";

		final String jsonType = (String) o.get(JsonFieldType.TYPE_FIELD);

		if ((JsonFieldType.STRING_FIELD).equalsIgnoreCase(jsonType)) {
			result = HiveFieldType.STRING_FIELD;
		} else if ((JsonFieldType.NUMBER_FIELD).equalsIgnoreCase(jsonType)) {
			result = HiveFieldType.FLOAT_FIELD;
		} else if ((JsonFieldType.INTEGER_FIELD).equalsIgnoreCase(jsonType)) {
			result = HiveFieldType.INTEGER_FIELD;
		} else if ((JsonFieldType.BOOLEAN_FIELD).equalsIgnoreCase(jsonType)) {
			result = HiveFieldType.BOOLEAN_FIELD;
		}

		return result;
	}

	public HiveColumn getPrimaryId() {
		return new HiveColumn(JsonFieldType.PRIMARY_ID_FIELD, HiveFieldType.STRING_FIELD, true);
	}

	public List<HiveColumn> getContexDataFields() {

		final List<HiveColumn> columns = new ArrayList<>();

		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE, HiveFieldType.STRING_FIELD, false));
		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_DEVICE, HiveFieldType.STRING_FIELD, false));
		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION, HiveFieldType.STRING_FIELD, false));

		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_CLIENT_SESSION, HiveFieldType.STRING_FIELD, false));
		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_USER, HiveFieldType.STRING_FIELD, false));
		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_TIMEZONE_ID, HiveFieldType.STRING_FIELD, false));
		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_TIMESTAMP, HiveFieldType.STRING_FIELD, false));

		columns.add(new HiveColumn(CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS, HiveFieldType.BIGINT_FIELD, false));

		return columns;
	}
}
