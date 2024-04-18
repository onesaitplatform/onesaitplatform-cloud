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
package com.minsait.onesait.platform.persistence.hadoop.kudu;

import static com.minsait.onesait.platform.persistence.hadoop.common.HadoopMessages.NOT_IMPLEMENTED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;
import com.minsait.onesait.platform.persistence.hadoop.impala.ImpalaManageDBRepository;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.KuduTable;
import com.minsait.onesait.platform.persistence.hadoop.kudu.table.KuduTableGenerator;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("KuduManageDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
@Conditional(HadoopEnabledCondition.class)
@DependsOn({NameBeanConst.IMPALA_MANAGE_DB_REPO_BEAN_NAME})
public class KuduManageDBRepository implements ManageDBRepository {

	// Static regex extract info from show create table sentence
	private static final String PRIMARY_KEY_PATTERN = "(?m)^ *PRIMARY KEY \\((.*)\\) *$";
	private static final String PARTITION_BY_PATTERN = "(?m)^ *PARTITION BY HASH \\((.*)\\) PARTITIONS ([0-9]+) *$";

	@Autowired
	@Qualifier(NameBeanConst.IMPALA_MANAGE_DB_REPO_BEAN_NAME)
	private ManageDBRepository impalaManageDBRepository;

	@Autowired
	private KuduTableGenerator kuduTableGenerator;

	@Autowired
	@Qualifier(NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate jdbcTemplate;

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@PostConstruct
	public void init() {
		log.info("Kudu Repository Enabled");
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {

		try {
			log.debug("create kudu table for ontology " + ontology);
			KuduTable table = kuduTableGenerator.builTable(ontology, schema, config);
			jdbcTemplate.execute(table.build());
			log.debug("kudu table created successfully");
		} catch (DataAccessException | DBPersistenceException e) {
			log.error("error creating kudu table for ontology " + ontology, e);
			throw new DBPersistenceException(e);
		}

		return ontology;
	}

	@Override
	public List<String> getListOfTables() {
		return impalaManageDBRepository.getListOfTables();
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		final List<String> collections = getListOfTables();
		final ArrayList<String> result = new ArrayList<>();
		// NOT ADD WHEN IT IS NOT IN THE COLLECTIONS
		// result.add(ontology);
		for (final String collection : collections) {
			if (collection.startsWith(ontology))
				result.add(collection);
		}
		return result;
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public void createIndex(String ontology, String attribute) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public void createIndex(String sentence) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<String> getListIndexes(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public String getIndexes(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String path) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED);
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		return impalaManageDBRepository.describeTable(name);
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		return createTableToConfigMap(((ImpalaManageDBRepository) impalaManageDBRepository).showCreateTable(ontology));
	}

	private HashMap<String, String> createTableToConfigMap(String createSentence) {
		log.debug(createSentence);
		HashMap<String, String> internalProp = new HashMap<>();
		Pattern patternPKey = Pattern.compile(PRIMARY_KEY_PATTERN);
		Pattern patternPartBy = Pattern.compile(PARTITION_BY_PATTERN);
		Matcher matcherPKey = patternPKey.matcher(createSentence);
		String pkey = null;
		String partBy = null;
		String nPart = null;
		if (matcherPKey.find()) {
			pkey = matcherPKey.group(1);
			internalProp.put("primarykey", pkey.trim().replace(", ", ","));
		}

		Matcher matcherPartBy = patternPartBy.matcher(createSentence);
		if (matcherPartBy.find()) {
			partBy = matcherPartBy.group(1);
			nPart = matcherPartBy.group(2);
			internalProp.put("partitions", partBy.trim().replace(", ", ","));
			internalProp.put("npartitions", nPart);
		}

		return internalProp;
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {
		String bckCreateTable = ((ImpalaManageDBRepository) impalaManageDBRepository).showCreateTable(identification);
		try {
			impalaManageDBRepository.removeTable4Ontology(identification);
			createTable4Ontology(identification, jsonSchema, config);
		} catch (DBPersistenceException e) {
			// Recover original table
			log.debug("executing previous create table of " + identification + " : " + bckCreateTable);
			jdbcTemplate.execute(bckCreateTable);
			log.debug("executed previous create table of " + identification);
			throw new DBPersistenceException(e);
		}
		return identification;
	}

}
