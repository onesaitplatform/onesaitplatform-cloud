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
package com.minsait.onesait.platform.persistence.hadoop.impala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.DependsOn;
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
import com.minsait.onesait.platform.persistence.hadoop.rowmapper.ImpalaDescribeColumnRowMapper;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component(NameBeanConst.IMPALA_MANAGE_DB_REPO_BEAN_NAME)
@Scope("prototype")
@Lazy
@Slf4j
@DependsOn( {NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME})
public class ImpalaManageDBRepository implements ManageDBRepository {

	private static final String INVALIDATE_METADATA = "invalidate metadata %s";

	@Autowired
	@Qualifier(NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate impalaJdbcTemplate;

	public void invalidateMetadata(String name) {
		try {
			String sql = String.format(INVALIDATE_METADATA, name);
			impalaJdbcTemplate.execute(sql);
		} catch (DataAccessException e) {
			log.error("error invalidating hive table " + name, e);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		return null;
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		return null;
	}

	@Override
	public List<String> getListOfTables() {

		List<String> tables = null;

		try {
			tables = impalaJdbcTemplate.queryForList(CommonQuery.LIST_TABLES, String.class);
		} catch (DataAccessException e) {
			log.error("error getting all impala tables ", e);
			throw new DBPersistenceException(e);
		}

		return tables;
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		return Collections.emptyList();
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		try {
			String sql = String.format(CommonQuery.DROP_TABLE, ontology);
			impalaJdbcTemplate.execute(sql);
		} catch (DataAccessException e) {
			log.error("error drop impala table ", e);
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public void createIndex(String ontology, String attribute) {
		// no_needed
	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		// no_needed
	}

	@Override
	public void createIndex(String sentence) {
		// no_needed
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		// no_needed
	}

	@Override
	public List<String> getListIndexes(String ontology) {
		return Collections.emptyList();
	}

	@Override
	public String getIndexes(String ontology) {
		return null;
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		// no_needed
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
	public List<DescribeColumnData> describeTable(String name) {

		List<DescribeColumnData> descriptors = new ArrayList<>();

		try {

			String sql = String.format(CommonQuery.DESCRIBE_TABLE, name);
			descriptors = impalaJdbcTemplate.query(sql, new ImpalaDescribeColumnRowMapper());

		} catch (DataAccessException e) {
			log.error("error describe impala table " + name, e);
			throw new DBPersistenceException(e);
		}

		return descriptors;
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {

		return null;
	}

	public String showCreateTable(String name) {
		String queryResponse;
		try {

			String sql = String.format(CommonQuery.SHOW_CREATE_TABLE, name);
			queryResponse = impalaJdbcTemplate.queryForObject(sql, String.class);

		} catch (DataAccessException e) {
			log.error("Error show create table impala table " + name, e);
			throw new DBPersistenceException(e);
		}

		return queryResponse;
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {

		return null;
	}

}
