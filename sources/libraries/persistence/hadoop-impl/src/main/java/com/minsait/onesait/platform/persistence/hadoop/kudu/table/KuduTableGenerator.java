/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.geometry.GeometryType;
import com.minsait.onesait.platform.persistence.hadoop.util.HiveFieldType;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonFieldType;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

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
	private static final String NPARTITIONS = "npartitions";
	private static final String DEFAULT_NPARTITIONS = "1";
	private static final String DESCRIPTION_LOWERCASE = "description";
	private static final String DEFAULT_LOWERCASE = "default";
	private static final String REQUIRED_LOWERCASE = "required";
	private static final String NULL_STR = "NULL";
	private static final String NOT_STR = "NOT";
	private static final String PRIMARY_KEY_STR = "PRIMARY KEY";
	private static final String PARTITIONS_STR = "PARTITIONS";
	private static final String DEFAULT_STR = "DEFAULT";
	private static final String COMMENT_STR = "COMMENT";
	private static final String STORED_AS_KUDU_STR = "STORED AS KUDU";
	private static final String PARTITION_BY_STR = "PARTITION BY";
	private static final String PARTITION_BY_HASH_STR = "PARTITION BY HASH";
	private static final String FIELD_TYPE_MSG = "In KUDU ontology, the properties must be primitive, geometry or timestamp";
	
	public KuduTable builTable(String ontologyName, String schema, Map<String, String> config) {

		log.debug("generate kudu table for ontology " + ontologyName);

		final JSONObject schemaObj = new JSONObject(schema);

		final JSONObject props = getProperties(schemaObj);
		final List<String> requiredProps = getRequiredProps(schemaObj);
		if (props.length() == 1 && ((JSONObject) props.get(requiredProps.get(0))).has("$ref")
				&& ((JSONObject) props.get(requiredProps.get(0))).has("type")) {
			log.error("Error in the type of properties");
			throw new DBPersistenceException(FIELD_TYPE_MSG);
		} else {
			return build(ontologyName, props, requiredProps, config);
		}
	}

	public JSONObject getProperties(JSONObject jsonObj) {
		return jsonObj.getJSONObject(JsonFieldType.PROPERTIES_FIELD);
	}

	public List<String> getRequiredProps(JSONObject jsonObj) {

		final List<String> requiredProperties = new ArrayList<>();

		if (jsonObj.has(REQUIRED_LOWERCASE)) {
			final JSONArray array = jsonObj.getJSONArray(REQUIRED_LOWERCASE);

			for (int i = 0; i < array.length(); i++) {
				requiredProperties.add(array.getString(i));
			}
		}

		return requiredProperties;
	}
	
	private void parseConfigMap(Map<String, String> cmap) {
		if (!cmap.containsKey(NPARTITIONS)) {
			cmap.put(NPARTITIONS, DEFAULT_NPARTITIONS);
		}
		if (!cmap.containsKey(PRIMARY_KEY)) {
			cmap.put(PRIMARY_KEY, null);
		}
		if (!cmap.containsKey(PARTITIONS)) {
			cmap.put(PARTITIONS, null);
		}
		
	}
	
	private String[] getPrimaryKeyFromConfigMap(Map<String, String> cmap) {
		String[] primarykey;
		if (cmap.get(PRIMARY_KEY) == null) {
			primarykey = new String[] { "_id" };
		}
		else if (!cmap.get(PRIMARY_KEY).equals(cmap.get(PRIMARY_KEY).toLowerCase())) {// Check if field is lowercase
			log.error("Error: primarykey fields must be lowercase");
			throw new DBPersistenceException("In KUDU ontology, the primarykey fields must be lowercase");
		} 
		else {
			primarykey = cmap.get(PRIMARY_KEY).trim().split("\\s*,\\s*");
		}
		return primarykey;
	}
	
	private String[] getPartitionsFromConfigMap(Map<String, String> cmap) {
		String[] partition;
		if (cmap.get(PARTITIONS) == null) {
			partition = null;
		}
		else if (!cmap.get(PARTITIONS).equals(cmap.get(PARTITIONS).toLowerCase())) {// Check if field is lowercase
			log.error("Error: partitions fields must be lowercase");
			throw new DBPersistenceException("In KUDU ontology, the partitions fields must be lowercase");
		}
		else {
			partition = cmap.get(PARTITIONS).trim().split("\\s*,\\s*");
		}
		return partition;
	}

	public KuduTable build(String name, JSONObject props, List<String> requiredProperties, Map<String, String> cmap) {

		String[] primarykey;
		String[] partition;
		int npartitions;

		checkOntologyName(name);
		
		if (cmap == null) {
			npartitions = 1;// No partition
			primarykey = new String[] { "_id" };
			partition = null;
		} else {
			parseConfigMap(cmap);
			npartitions = Integer.valueOf(cmap.get(NPARTITIONS));
			primarykey = getPrimaryKeyFromConfigMap(cmap);
			partition = getPartitionsFromConfigMap(cmap);
		}
		
		// old parsisin, remaining just in case above does not work properly
//		if (cmap == null) {
//			npartitions = 1;// No partition
//			primarykey = new String[] { "_id" };
//			partition = null;
//		} else {
//			npartitions = Integer.valueOf(cmap.get(NPARTITIONS));
//
//			if (!cmap.get(PRIMARY_KEY).equals(cmap.get(PRIMARY_KEY).toLowerCase())) {// Check if field is lowercase
//				log.error("Error: primarykey fields must be lowercase");
//				throw new DBPersistenceException("In KUDU ontology, the primarykey fields must be lowercase");
//			}
//
//			if (!cmap.get(PARTITIONS).equals(cmap.get(PARTITIONS).toLowerCase())) {// Check if field is lowercase
//				log.error("Error: partitions fields must be lowercase");
//				throw new DBPersistenceException("In KUDU ontology, the partitions fields must be lowercase");
//			}
//
//			primarykey = cmap.get(PRIMARY_KEY).trim().split("\\s*,\\s*");
//			partition = cmap.get(PARTITIONS).trim().split("\\s*,\\s*");
//		}

		final KuduTable table = new KuduTable(name, numReplicas, addresses, primarykey, partition, npartitions,
				includeKudutableName);

		@SuppressWarnings("unchecked")
		final Iterator<String> it = props.keys();

		final List<KuduColumn> columnsNoPK = new ArrayList<>();

		if (Arrays.stream(primarykey).anyMatch(JsonFieldType.PRIMARY_ID_FIELD::equals)) {
			table.getColumns().add(getPrimaryId());
		} else {
			columnsNoPK.add(getPrimaryId());
		}

		final List<KuduColumn> columnsContextData = getContexDataFields();

		for (final KuduColumn hc : columnsContextData) {
			if (Arrays.stream(primarykey).anyMatch(hc.getName()::equals)) {
				table.getColumns().add(hc);
			} else {
				columnsNoPK.add(hc);
			}
		}

		while (it.hasNext()) {
			final String key = it.next().trim();
			checkFieldName(key);

			final JSONObject o = (JSONObject) props.get(key);

			if (Arrays.stream(primarykey).anyMatch(key::equals)) {
				if (isPrimitive(o)) {
					table.getColumns()
							.add(new KuduColumn(key, pickPrimitiveType(key, o), requiredProperties.contains(key)));
				} else {
					table.getColumns().addAll(pickType(key, o, requiredProperties));
				}
			} else {
				if (isPrimitive(o)) {
					columnsNoPK.add(new KuduColumn(key, pickPrimitiveType(key, o), requiredProperties.contains(key)));
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

	public boolean isPrimitive(String s) {
		return JsonFieldType.getPRIMITIVE_TYPES().contains(s);
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
	
	public boolean isGeometry(String s) {
		return s.equals(JsonFieldType.GEOMETRY);
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
	
	public boolean isTimestamp(String s) {
		return s.equals("timestamp");
	}

	public List<KuduColumn> pickType(String key, JSONObject o, List<String> requiredProperties) {

		final List<KuduColumn> columns = new ArrayList<>();

		if (isGeometry(o)) {

			columns.add(new KuduColumn(key + HiveFieldType.LATITUDE_FIELD, HiveFieldType.DOUBLE_FIELD,
					requiredProperties.contains(key)));
			columns.add(new KuduColumn(key + HiveFieldType.LONGITUDE_FIELD, HiveFieldType.DOUBLE_FIELD,
					requiredProperties.contains(key)));

		} else if (isTimestamp(o)) {
			columns.add(new KuduColumn(key, HiveFieldType.TIMESTAMP_FIELD, requiredProperties.contains(key)));
		} else {
			log.error("Error picking type");
			throw new DBPersistenceException(FIELD_TYPE_MSG);
		}

		return columns;
	}
	
	public List<KuduColumn> pickType(String key, String s, boolean b) {

		final List<KuduColumn> columns = new ArrayList<>();

		if (isGeometry(s)) {

			columns.add(new KuduColumn(key + HiveFieldType.LATITUDE_FIELD, HiveFieldType.DOUBLE_FIELD,
					b));
			columns.add(new KuduColumn(key + HiveFieldType.LONGITUDE_FIELD, HiveFieldType.DOUBLE_FIELD,
					b));

		} else if (isTimestamp(s)) {
			columns.add(new KuduColumn(key, HiveFieldType.TIMESTAMP_FIELD, b));
		} else {
			log.error("Error picking type");
			throw new DBPersistenceException(FIELD_TYPE_MSG);
		}

		return columns;
	}

	public String pickPrimitiveType(String key, JSONObject o) {
		final String jsonType = (String) o.get(JsonFieldType.TYPE_FIELD);
		return pickPrimitiveType(jsonType);
	}

	public String pickPrimitiveType(String s) {
		String result = "";

		if ((JsonFieldType.STRING_FIELD).equalsIgnoreCase(s)) {
			result = HiveFieldType.STRING_FIELD;
		} else if ((JsonFieldType.NUMBER_FIELD).equalsIgnoreCase(s)) {
			result = HiveFieldType.FLOAT_FIELD;
		} else if ((JsonFieldType.INTEGER_FIELD).equalsIgnoreCase(s)) {
			result = HiveFieldType.INTEGER_FIELD;
		} else if ((JsonFieldType.BOOLEAN_FIELD).equalsIgnoreCase(s)) {
			result = HiveFieldType.BOOLEAN_FIELD;
		}

		return result;
	}
	
	public KuduColumn getPrimaryId() {
		return new KuduColumn(JsonFieldType.PRIMARY_ID_FIELD, HiveFieldType.STRING_FIELD, true);
	}

	public List<KuduColumn> getContexDataFields() {

		final List<KuduColumn> columns = new ArrayList<>();

		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE, HiveFieldType.STRING_FIELD, false));
		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_DEVICE, HiveFieldType.STRING_FIELD, false));
		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_DEVICE_TEMPLATE_CONNECTION, HiveFieldType.STRING_FIELD, false));

		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_CLIENT_SESSION, HiveFieldType.STRING_FIELD, false));
		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_USER, HiveFieldType.STRING_FIELD, false));
		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_TIMEZONE_ID, HiveFieldType.STRING_FIELD, false));
		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_TIMESTAMP, HiveFieldType.STRING_FIELD, false));

		columns.add(new KuduColumn(CONTEXT_DATA_FIELD_TIMESTAMP_MILLIS, HiveFieldType.BIGINT_FIELD, false));

		return columns;
	}
	
	private HashMap<String, String> getConfigMap(CreateStatementKudu statement) {
		final HashMap<String, String> cmap = new HashMap<>();
		if (statement.getEnablePartitionIndexes() != null && statement.getEnablePartitionIndexes()) {
			cmap.put(PARTITIONS, statement.getPartitions());
			cmap.put(PRIMARY_KEY, statement.getPrimaryKey());
			cmap.put(NPARTITIONS, statement.getNpartitions());
			return cmap;
		}
		return null;
	}
	
	public KuduTable buildCreateTable(String name, CreateStatementKudu statement) {

		String[] primarykey = null;
		String[] partition = null;
		int npartitions = 1;

		HashMap<String, String> cmap = getConfigMap(statement);
		
		checkOntologyName(name);
		
		if (cmap != null) {
			parseConfigMap(cmap);
			npartitions = Integer.valueOf(cmap.get(NPARTITIONS));
			primarykey = getPrimaryKeyFromConfigMap(cmap);
			partition = getPartitionsFromConfigMap(cmap);
		}
		
		final KuduTable table = new KuduTable(name, numReplicas, addresses, primarykey, partition, npartitions,
				includeKudutableName);

		final List<KuduColumn> columnsNoPK = new ArrayList<>();

		List<KuduColumn> columns = statement.getColumns();
		for (KuduColumn column : columns) {
			final String columnName = column.getName();
			final String columnType = column.getColumnType();
			checkFieldName(columnName);
			if (primarykey != null && Arrays.stream(primarykey).anyMatch(columnName::equals)) {
				if (isPrimitive(columnType)) {
					table.getColumns()
							.add(new KuduColumn(columnName, pickPrimitiveType(columnType), column.isRequired(), 
									column.getDefaultValue(), column.getComment()));
				} else {
					table.getColumns().addAll(pickType(columnName, columnType, column.isRequired()));
				}
			} else {
				if (isPrimitive(columnType)) {
					columnsNoPK.add(new KuduColumn(columnName, pickPrimitiveType(columnType), column.isRequired(),
							column.getDefaultValue(), column.getComment()));
				} else {
					columnsNoPK.addAll(pickType(columnName, columnType, column.isRequired()));
				}
			}
		}
		
		table.getColumns().addAll(columnsNoPK);

		return table;
	}
	
	public void checkOntologyName(String ontologyName) {
		if (!ontologyName.equals(ontologyName.toLowerCase())) {// Check if name is lowercase
			log.error("Error naming the ontology");
			throw new DBPersistenceException("In KUDU ontology, ontology name must be lowercase");
		}
	}
	
	public void checkFieldName(String fieldName) {
		if (!fieldName.equals(fieldName.toLowerCase())) {// Check if field is lowercase
			log.error("Error: properties fields must be lowercase");
			throw new DBPersistenceException("In KUDU ontology, the properties must be lowercase. Field: " + fieldName);
		}
	}

	public String completeSQLCreateStatement(String name, String sql, String schema) {
		Statement parse = null;
		String stmt = "";

		String bckConfig = getStrFromStatement(sql, STORED_AS_KUDU_STR);
		sql = sql.replace(bckConfig, "");
		String bckPartitions = getStrFromStatement(sql, PARTITION_BY_STR);
		sql = sql.replace(bckPartitions, "");
		
		try {
			parse = CCJSqlParserUtil.parse(sql);
		} catch (JSQLParserException e) {
			log.error("Error: parsing query");
			throw new DBPersistenceException("Error parsing query: " + sql);
		}
		
		if (parse instanceof CreateTable) {
			CreateTable createTable = (CreateTable)parse;
		    if (!name.equals(createTable.getTable().getName())) {
		    	log.error("Error: KUDU name ontology and table name must be the same");
				throw new DBPersistenceException("In KUDU ontology, name ontology and table name must be the same");
		    }
		    checkOntologyName(createTable.getTable().getName());
		    for (ColumnDefinition column: createTable.getColumnDefinitions()) {
		    	checkFieldName(column.getColumnName());
		    }
		    if (schema == null) {
		    	stmt = completeSQLForCreate(name, createTable).concat(bckPartitions).concat(buildConfigCreate(name, getNPartitions(bckPartitions)));
		    } else {
		    	final JSONObject schemaObj = new JSONObject(schema);
				final JSONObject properties = getProperties(schemaObj);
				final List<String> requiredProperties = new ArrayList<>();

				stmt = completeSQLForEdit(createTable, properties, requiredProperties).concat(bckPartitions).concat(bckConfig);
		    }
		}
		return stmt;
	}
	
	private String completeSQLForCreate(String name, CreateTable createTable) {	    
	    List<ColumnDefinition> orderedList = new ArrayList<>();
	    
	    //adding primary key columns ordered
	    if (createTable.getIndexes() == null || createTable.getIndexes().isEmpty()) {
	    	createTable.setIndexes(new ArrayList<>());
			createTable.getIndexes().add(getPrimaryKeyIndex(JsonFieldType.PRIMARY_ID_FIELD));
			orderedList.add(createNewColumnDefinition(JsonFieldType.PRIMARY_ID_FIELD,JsonFieldType.STRING_FIELD, true));
	    } else {
	    	final Index index = createTable.getIndexes().stream()
	    			.filter(o -> o.getType().equalsIgnoreCase(PRIMARY_KEY_STR))
	    			.findAny().orElse(null);
	    	if (index == null) {
	    		createTable.getIndexes().add(getPrimaryKeyIndex(JsonFieldType.PRIMARY_ID_FIELD));
	    		orderedList.add(createNewColumnDefinition(JsonFieldType.PRIMARY_ID_FIELD,JsonFieldType.STRING_FIELD, true));
	    	} else {
		    	for(String primaryKey: index.getColumnsNames()) {
		    		final ColumnDefinition primaryKeyColumn = createTable.getColumnDefinitions().stream()
		    				.filter(o -> o.getColumnName().equals(primaryKey))
		    				.findAny().orElse(null);
		    		if (primaryKeyColumn != null) {
		    			orderedList.add(primaryKeyColumn);
		    		}
		    	}
	    	}
	    }
	    
	    //adding not primary key columns
	    for (ColumnDefinition column: createTable.getColumnDefinitions()) {
	    	if (!orderedList.contains(column)) {
	    		orderedList.add(column);
	    	}
	    }
	
	    //adding id field if not exist
	    if (orderedList.stream().noneMatch(o -> o.getColumnName().equals(JsonFieldType.PRIMARY_ID_FIELD))) {
	    	orderedList.add(createNewColumnDefinition(JsonFieldType.PRIMARY_ID_FIELD,JsonFieldType.STRING_FIELD, true));
	    }	    	  
	    
    	//adding contextdata fields
	    final List<KuduColumn> columnsContextData = getContexDataFields();
	    for (KuduColumn columnContextData: columnsContextData) {
	    	orderedList.add(createNewColumnDefinition(columnContextData.getName(), columnContextData.getColumnType(), false));
	    }
	    createTable.setColumnDefinitions(orderedList);
	    
	    //modifying partitions 
	/*    if(createTable.getTableOptionsStrings() != null && createTable.getTableOptionsStrings().contains(PARTITIONS_STR)) {
	    	int index = createTable.getTableOptionsStrings().indexOf(PARTITIONS_STR) + 1;
	    	String partitionsValue =  (String) createTable.getTableOptionsStrings().get(index);
	    	try {
	    		partitions = Integer.parseInt((String) partitionsValue);
	    	} catch (NumberFormatException e) {
	    		log.error("Error casting number of partitions");
	    		throw new DBPersistenceException("Invalid value for number of partitions: " + partitionsValue);
	    	}
	    }
	 */   	
	    return createTable.toString();
	 
	}
	
	public String buildConfigCreate(String name, int partitions) {
		StringBuilder sentence = new StringBuilder();
		sentence.append(" STORED AS KUDU ");
		sentence.append("TBLPROPERTIES(");
		sentence.append("'kudu.master_addresses' = '");
		sentence.append(addresses);
		sentence.append("',");
		
		if(includeKudutableName) {
			sentence.append("'kudu.table_name' = '" + name + "',");
		}
		
		sentence.append("'kudu.num_tablet_replicas' = '");
		sentence.append(partitions);
		sentence.append("'");
		sentence.append(");");
		
		return sentence.toString();
	}
	
	private ColumnDefinition createNewColumnDefinition(String name, String type, Boolean required) {
	  ColumnDefinition column = new ColumnDefinition();
	  column.setColumnName(name);
	  ColDataType colDataType = new ColDataType();
	  colDataType.setDataType(type);
	  column.setColDataType(colDataType);
	  if (required) {
		  List<String> columnSpecStrings = new ArrayList<>();
		  columnSpecStrings.add(NOT_STR);
		  columnSpecStrings.add(NULL_STR);
		  column.setColumnSpecStrings(columnSpecStrings);
	  }
	  
	  return column;
	}
	
	private ColumnDefinition createNewColumnDefinition(String name, String type, Boolean required, 
			Object defaultValue, String comment) {
		  ColumnDefinition column = createNewColumnDefinition(name, type, required);
		  List<String> columnSpecStrings = column.getColumnSpecStrings();
		  if (columnSpecStrings == null) {
			  columnSpecStrings = new ArrayList<>();
		  }
		  if (defaultValue != null) {
			  columnSpecStrings.add(DEFAULT_STR);
			  columnSpecStrings.add((String) defaultValue);
		  }
		  if (comment != null) {
			  columnSpecStrings.add(COMMENT_STR);
			  columnSpecStrings.add(comment);
		  }
		  column.setColumnSpecStrings(columnSpecStrings);
		  
		  return column;
	}
	
	private Index getPrimaryKeyIndex(String name) {
		Index index = new Index();
    	index.setType(PRIMARY_KEY_STR);
    	List<String> list = new ArrayList<>();
    	list.add(name);
    	index.setColumnsNames(list);
    	return index;
	}
	
	public String completeSQLForEdit(CreateTable createTable, JSONObject properties, List<String> requiredProperties) {
		List<ColumnDefinition> orderedList = new ArrayList<>();
	    for (String key: properties.keySet()) {
	    	checkFieldName(key);
	    	final JSONObject field = (JSONObject)properties.get(key);
	    	if (field.has(REQUIRED_LOWERCASE)  && requiredProperties.stream().noneMatch(key::equals) &&
	    		 field.get(REQUIRED_LOWERCASE) instanceof Boolean && (Boolean)field.get(REQUIRED_LOWERCASE)) {
	    		requiredProperties.add(key);
	    	}
	    }
	    
	    //in kudu primary key always exists else something is wrong
	    final Index index = createTable.getIndexes().stream()
	    			.filter(o -> o.getType().equalsIgnoreCase(PRIMARY_KEY_STR))
	    			.findAny().orElse(null);
	    
	    //adding primary key columns
		for(String primaryKey: index.getColumnsNames()) {
			if (!properties.has(primaryKey) && !primaryKey.equals(JsonFieldType.PRIMARY_ID_FIELD)) {
    			log.error("Error matching primary key index table with schema");
    			throw new DBPersistenceException(
    					"Primary key field does not exist in json schema: " + primaryKey);
			} else {
				if (!requiredProperties.contains(primaryKey)) {
					requiredProperties.add(primaryKey);
				}
	    		if (primaryKey.equals(JsonFieldType.PRIMARY_ID_FIELD)) {
	    	    	orderedList.add(createNewColumnDefinition(JsonFieldType.PRIMARY_ID_FIELD,JsonFieldType.STRING_FIELD, true));
	    		} else {
	    			final JSONObject primaryKeyColumn = (JSONObject)properties.get(primaryKey);
	    			if (isPrimitive(primaryKeyColumn)) {
	    				String type = pickPrimitiveType(primaryKey, primaryKeyColumn);
	    				orderedList.add(createNewColumnDefinition(primaryKey, type, requiredProperties.contains(primaryKey), 
	    						getDefaultValue(type,primaryKeyColumn), getDescription(primaryKeyColumn))); 
	    			} else {
	    				orderedList.addAll(pickTypeColumnDefinition(primaryKey, primaryKeyColumn, requiredProperties));
	    			}
	    		}	
			}
		}
   	
	    //adding not primary key columns
		for (String key: properties.keySet()) {
			final JSONObject column = (JSONObject)properties.get(key);
	    	if (orderedList.stream().noneMatch(o -> o.getColumnName().equals(key))) {
				if (isPrimitive(column)) {
					String type = pickPrimitiveType(key, column);
					orderedList.add(createNewColumnDefinition(key, type, requiredProperties.contains(key),
							getDefaultValue(type, column), getDescription(column))); 
				} else {
					orderedList.addAll(pickTypeColumnDefinition(key, column, requiredProperties));
				}
	    	}
	    }
	
	    //adding id field if not exist
	    if (orderedList.stream().noneMatch(o -> o.getColumnName().equals(JsonFieldType.PRIMARY_ID_FIELD))) {
	    	orderedList.add(createNewColumnDefinition(JsonFieldType.PRIMARY_ID_FIELD,JsonFieldType.STRING_FIELD, true));
	    }	    	  
	    
    	//adding contextdata fields
	    final List<KuduColumn> columnsContextData = getContexDataFields();
	    for (KuduColumn columnContextData: columnsContextData) {
	    	if (orderedList.stream().noneMatch(o -> o.getColumnName().equals(columnContextData.getName()))) {
	    		orderedList.add(createNewColumnDefinition(columnContextData.getName(), columnContextData.getColumnType(), false));
	    	}
	    }
	    	    
	    createTable.setColumnDefinitions(orderedList);
	    		    	
	    return createTable.toString();
		  
	}
	
	private List<ColumnDefinition> pickTypeColumnDefinition(String key, JSONObject o, List<String> requiredProperties) {
		final List<ColumnDefinition> columns = new ArrayList<>();

		if (isGeometry(o)) {
			
			columns.add(createNewColumnDefinition(key + HiveFieldType.LATITUDE_FIELD, HiveFieldType.DOUBLE_FIELD, 
					requiredProperties.contains(key), getDefaultValue(HiveFieldType.DOUBLE_FIELD, o), getDescription(o)));
			columns.add(createNewColumnDefinition(key + HiveFieldType.LONGITUDE_FIELD, HiveFieldType.DOUBLE_FIELD, 
					requiredProperties.contains(key), getDefaultValue(HiveFieldType.DOUBLE_FIELD, o), getDescription(o)));

		} else if (isTimestamp(o)) {
			columns.add(createNewColumnDefinition(key, HiveFieldType.TIMESTAMP_FIELD, 
					requiredProperties.contains(key), getDefaultValue(HiveFieldType.TIMESTAMP_FIELD,o), getDescription(o)));
		} else {
			log.error("Error picking type");
			throw new DBPersistenceException(FIELD_TYPE_MSG);
		}

		return columns;
	}
	
	public String getStrFromStatement(String statement, String str) {
		int index = statement.toUpperCase().indexOf(str);
		if (index == -1)
			return "";
		else 
			return statement.substring(statement.toUpperCase().indexOf(str), statement.length());
	}
	
	public int getNPartitions(String statement) {
		int partitions = numReplicas;
		if (statement.toUpperCase().indexOf(PARTITION_BY_HASH_STR) != -1) {
			int index = statement.toUpperCase().indexOf(PARTITIONS_STR);
			if (index != -1) {
				String str = statement.substring(index, statement.length());
				try {
					partitions = Integer.valueOf(str.replaceFirst(".*?(\\d+).*", "$1"));
				} catch (NumberFormatException e) {
		    		log.error("Error casting number of partitions");
		    		throw new DBPersistenceException("Invalid value for number of partitions");
		    	}
			}
		}
		return partitions;
			
	}
	
	
	private Object getDefaultValue(String type, JSONObject o) {
		if (o.has(DEFAULT_LOWERCASE)) {
			if (type.equals(JsonFieldType.STRING_FIELD) || type.equals(HiveFieldType.TIMESTAMP_FIELD))
				return "'".concat((String)o.get(DEFAULT_LOWERCASE)).concat("'");
			else 
				return o.get(DEFAULT_LOWERCASE);
		} else {
			return null;
		}
	}
	
	private String getDescription(JSONObject o) {
		if (o.has(DESCRIPTION_LOWERCASE)) {
			return "'".concat((String) o.get(DESCRIPTION_LOWERCASE)).concat("'");
		} else {
			return null;
		}
	}
}
