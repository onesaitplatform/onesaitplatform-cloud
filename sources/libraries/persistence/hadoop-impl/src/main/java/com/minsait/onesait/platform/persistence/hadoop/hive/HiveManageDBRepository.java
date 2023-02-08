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
package com.minsait.onesait.platform.persistence.hadoop.hive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.CommonQuery;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.config.HdfsConfiguration;
import com.minsait.onesait.platform.persistence.hadoop.hive.table.HiveTable;
import com.minsait.onesait.platform.persistence.hadoop.hive.table.HiveTableGenerator;
import com.minsait.onesait.platform.persistence.hadoop.impala.ImpalaManageDBRepository;
import com.minsait.onesait.platform.persistence.hadoop.rowmapper.HiveDescribeColumnRowMapper;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("HiveManageDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
@ConditionalOnBean(name = { NameBeanConst.HIVE_TEMPLATE_JDBC_BEAN_NAME, NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME })
public class HiveManageDBRepository implements ManageDBRepository {

	@Autowired
	@Qualifier(NameBeanConst.HIVE_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate hiveJdbcTemplate;

	@Autowired
	private ImpalaManageDBRepository impalaManageDBRepository;

	@Autowired
	private HiveTableGenerator hiveTableGenerator;

	@Autowired
	private HdfsConfiguration hdfsConfiguration;

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		return null;
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		try {
			log.debug("create hive table for ontology " + ontology);
			HiveTable table = hiveTableGenerator.buildHiveTable(ontology, schema,
					hdfsConfiguration.getAbsolutePath(hdfsConfiguration.getOntologiesFolder(), ontology));
			hiveJdbcTemplate.execute(table.build());
			log.debug("hive table created successfully");
			impalaManageDBRepository.invalidateMetadata(table.getName());
			log.debug("impala invalidated metadata");
		} catch (DataAccessException e) {
			log.error("error creating hive table for ontology " + ontology, e);
			throw new DBPersistenceException(e);
		}
		return ontology;
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {

		List<DescribeColumnData> descriptors = new ArrayList<>();

		try {

			String sql = String.format(CommonQuery.DESCRIBE_TABLE, name);
			descriptors = hiveJdbcTemplate.query(sql, new HiveDescribeColumnRowMapper());

		} catch (DataAccessException e) {
			log.error("error describe hive table " + name, e);
			throw new DBPersistenceException(e);
		}

		return descriptors;
	}

	@Override
	public List<String> getListOfTables() {

		List<String> tables = null;

		try {
			tables = hiveJdbcTemplate.queryForList(CommonQuery.LIST_TABLES, String.class);
		} catch (DataAccessException e) {
			log.error("error getting all hive tables ", e);
			throw new DBPersistenceException(e);
		}

		return tables;
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		return new ArrayList<>();
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		//implement when using
	}

	@Override
	public void createIndex(String ontology, String attribute) {
		//not neccesary
	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		//not neccesary
	}

	@Override
	public void createIndex(String sentence) {
		//not neccesary
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		//not neccesary
	}

	@Override
	public List<String> getListIndexes(String ontology) {
		return new ArrayList<>();
	}

	@Override
	public String getIndexes(String ontology) {
		return null;
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		//not neccesary
	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String path) {
		return null;
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		return 0;
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		return null;
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {
		return null;
	}

}
