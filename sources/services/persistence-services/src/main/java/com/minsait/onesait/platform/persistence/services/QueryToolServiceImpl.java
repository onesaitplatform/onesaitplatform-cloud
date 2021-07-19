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
package com.minsait.onesait.platform.persistence.services;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.ConfigDBTenantConfig;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.templates.PlatformQuery;
import com.minsait.onesait.platform.config.services.templates.QueryTemplateService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.exceptions.QueryNativeFormatException;
import com.minsait.onesait.platform.persistence.factory.QueryAsTextDBRepositoryFactory;
import com.minsait.onesait.platform.persistence.mongodb.quasar.connector.QuasarMongoDBbHttpConnector;
import com.minsait.onesait.platform.persistence.mongodb.tools.sql.Sql2NativeTool;
import com.minsait.onesait.platform.persistence.services.util.DataAccessQueryProcessor;
import com.minsait.onesait.platform.persistence.services.util.QueryParsers;
import com.minsait.onesait.platform.persistence.services.util.SQLParser;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;

@Component
@Slf4j
public class QueryToolServiceImpl implements QueryToolService {

	@PersistenceContext(unitName = ConfigDBTenantConfig.PERSISTENCE_UNIT_NAME_TENANT)
	private EntityManager entityManager;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyDataService ontologyDataService;

	@Autowired
	private QueryTemplateService queryTemplateService;

	@Autowired
	private QueryAsTextDBRepositoryFactory queryAsTextDBRepositoryFactory;

	@Autowired
	private QuasarMongoDBbHttpConnector quasarConnector;

	@Autowired
	private IntegrationResourcesService resourcesService;

	@Autowired
	private Sql2NativeTool sql2NativeTool;

	private static final String JOIN_REGEX = "(LEFT OUTER JOIN|RIGHT OUTER JOIN|INNER JOIN|FULL JOIN|CROSS)\\W+(\\w+)";
	private static final String USER = "User:";
	private static final String HASNT_PERMISSION_QUERY = " has not permission to query ontology ";

	@Autowired(required = false)
	private MetricsManager metricsManager;

	@Value("#{'${onesaitplatform.database.excludeParse:dual}'.split(',')}")
	private List<String> excludeParse;

	private boolean useQuasar() {
		try {
			return ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
					.get("mongodb-use-quasar")).booleanValue();
		} catch (final RuntimeException e) {
			return true;
		}
	}

	private void hasUserPermission(String user, String ontology, String query) {
		if (!ontologyService.hasUserPermissionForQuery(user, ontology)) {
			log.error("Error: no permission to query ontology");
			throw new DBPersistenceException(USER + user + HASNT_PERMISSION_QUERY + ontology);
		}
		if (isJoin(query)) {
			try {
				SQLParser.getTables(query).stream().forEach(s -> {
					if (excludeParse.indexOf(s.toLowerCase()) == -1) {
						if (!ontologyService.hasUserPermissionForQuery(user, s))
							throw new DBPersistenceException(USER + user + HASNT_PERMISSION_QUERY + s);
						final Ontology source = ontologyService.getOntologyByIdentification(ontology);
						final Ontology destination = ontologyService.getOntologyByIdentification(s);
						if (destination == null || source == null
								|| !source.getRtdbDatasource().equals(destination.getRtdbDatasource()))
							throw new DBPersistenceException(
									"Ontologies: " + ontology + " and " + s + " are not in the same repository");
					}
				});
			} catch (final JSQLParserException e) {
				log.error("Malformed query", e);
				throw new DBPersistenceException("Malformed query");
			}

		}
		if ((query.toLowerCase().indexOf("update") != -1 || query.toLowerCase().indexOf("remove") != -1
				|| query.toLowerCase().indexOf("delete") != -1 || query.toLowerCase().indexOf("createindex") != -1)
				&& !ontologyService.hasUserPermissionForInsert(user, ontology)) {
			throw new DBPersistenceException(
					USER + user + " has not permission to update, insert or remove on ontology " + ontology);
		}
	}

	private void hasClientPlatformPermisionForQuery(String clientPlatform, String ontology) {
		if (!ontologyService.hasClientPlatformPermisionForQuery(clientPlatform, ontology)) {
			throw new DBPersistenceException("Client Platform:" + clientPlatform + HASNT_PERMISSION_QUERY + ontology);
		}
	}

	@Override
	public String queryNativeAsJson(String user, String ontology, String query, int offset, int limit) {
		try {
			hasUserPermission(user, ontology, query);
			if (ontologyService.hasEncryptionEnabled(ontology))
				query = ontologyDataService.encryptQuery(query, true);
			String result = queryAsTextDBRepositoryFactory.getInstance(ontology, user).queryNativeAsJson(ontology,
					query, offset, limit);
			result = ontologyDataService.decryptAllUsers(result, ontology);
			metricsManagerLogControlPanelQueries(user, ontology, "OK");
			return result;
		} catch (final Exception e) {
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			log.error("Error queryNativeAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String queryNativeAsJson(String user, String ontology, String query) {
		try {
			hasUserPermission(user, ontology, query);
			if (ontologyService.hasEncryptionEnabled(ontology))
				query = ontologyDataService.encryptQuery(query, true);
			String result = queryAsTextDBRepositoryFactory.getInstance(ontology, user).queryNativeAsJson(ontology,
					query);
			result = ontologyDataService.decryptAllUsers(result, ontology);
			metricsManagerLogControlPanelQueries(user, ontology, "OK");
			return result;

		} catch (final QueryNativeFormatException e) {
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			throw e;
		} catch (final Exception e) {
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			log.error("Error queryNativeAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	private String querySQLAsJson(String user, String ontology, String query, int offset, boolean checkTemplates) {
		try {
			hasUserPermission(user, ontology, query);

			query = QueryParsers.parseFunctionNow(query);
			if (checkTemplates) {
				final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
				if (newQuery != null) {

					switch (newQuery.getType()) {
					case SQL:
						return querySQLAsJson(user, ontology, newQuery.getQuery(), offset, false);
					case NATIVE:
						return queryNativeAsJson(user, ontology, newQuery.getQuery());
					default:
						throw new IllegalStateException("Only SQL or NATIVE queries are supported");
					}
				}
			}

			query = applyDataAccess(query, user);

			return queryAsTextDBRepositoryFactory.getInstance(ontology, user).querySQLAsJson(ontology, query, offset);

		} catch (final QueryNativeFormatException e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw e;
		} catch (final DBPersistenceException e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw e;
		} catch (final Exception e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	private String querySQLAsJson(String user, String ontology, String query, int offset, boolean checkTemplates,
			int limit) {
		try {
			hasUserPermission(user, ontology, query);

			query = QueryParsers.parseFunctionNow(query);
			if (checkTemplates) {
				final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
				if (newQuery != null) {

					switch (newQuery.getType()) {
					case SQL:
						return querySQLAsJson(user, ontology, newQuery.getQuery(), offset, false, limit);
					case NATIVE:
						return queryNativeAsJson(user, ontology, newQuery.getQuery());
					default:
						throw new IllegalStateException("Only SQL or NATIVE queries are supported");
					}
				}
			}

			query = applyDataAccess(query, user);

			return queryAsTextDBRepositoryFactory.getInstance(ontology, user).querySQLAsJson(ontology, query, offset,
					limit);

		} catch (final QueryNativeFormatException e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw e;
		} catch (final DBPersistenceException e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw e;
		} catch (final Exception e) {
			log.error("Error querySQLAsJson:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJson(String user, String ontology, String query, int offset)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		try {
			if (ontologyService.hasEncryptionEnabled(ontology))
				query = ontologyDataService.encryptQuery(query, false);
			String result = querySQLAsJson(user, ontology, query, offset, true);
			result = ontologyDataService.decryptAllUsers(result, ontology);
			metricsManagerLogControlPanelQueries(user, ontology, "OK");
			return result;
		} catch (final DBPersistenceException e) {
			log.error("Error processing query", e);
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			throw e;
		} catch (final OntologyDataUnauthorizedException e) {
			log.error("Error processing query", e);
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			throw e;
		}
	}

	@Override
	public String queryNativeAsJsonForPlatformClient(String clientPlatform, String ontology, String query, int offset,
			int limit) {

		try {
			hasClientPlatformPermisionForQuery(clientPlatform, ontology);
			if (ontologyService.hasEncryptionEnabled(ontology))
				query = ontologyDataService.encryptQuery(query, true);
			String result = queryAsTextDBRepositoryFactory.getInstanceClientPlatform(ontology, clientPlatform)
					.queryNativeAsJson(ontology, query, offset, limit);
			result = ontologyDataService.decryptAllUsers(result, ontology);
			metricsManagerLogControlPanelQueries(clientPlatform, ontology, "OK");

			return result;
		} catch (final Exception e) {
			metricsManagerLogControlPanelQueries(clientPlatform, ontology, "KO");
			log.error("Error queryNativeAsJsonForPlatformClient:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	private String querySQLAsJsonForPlatformClient(String clientPlatform, String ontology, String query, int offset,
			boolean checkTemplates) {
		try {
			hasClientPlatformPermisionForQuery(clientPlatform, ontology);
			if (checkTemplates) {
				final PlatformQuery newQuery = queryTemplateService.getTranslatedQuery(ontology, query);
				if (newQuery != null) {

					switch (newQuery.getType()) {
					case SQL:
						return querySQLAsJsonForPlatformClient(clientPlatform, ontology, newQuery.getQuery(), offset,
								false);
					case NATIVE:
						return queryNativeAsJsonForPlatformClient(clientPlatform, ontology, newQuery.getQuery(), offset,
								0);
					default:
						throw new IllegalStateException("Only SQL or NATIVE queries are supported");
					}
				}
			}

			query = QueryParsers.parseFunctionNow(query);
			return queryAsTextDBRepositoryFactory.getInstanceClientPlatform(ontology, clientPlatform)
					.querySQLAsJson(ontology, query, offset);
		} catch (final Exception e) {
			log.error("Error querySQLAsJsonForPlatformClient:" + e.getMessage());
			throw new DBPersistenceException(e);
		}
	}

	@Override
	public String querySQLAsJsonForPlatformClient(String clientPlatform, String ontology, String query, int offset)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		if (ontologyService.hasEncryptionEnabled(ontology))
			query = ontologyDataService.encryptQuery(query, false);
		String result = querySQLAsJsonForPlatformClient(clientPlatform, ontology, query, offset, true);
		result = ontologyDataService.decryptAllUsers(result, ontology);
		metricsManagerLogControlPanelQueries(clientPlatform, ontology, "OK");
		return result;
	}

	@Override
	public String compileSQLQueryAsJson(String user, Ontology ontology, String query, int offset) {
		hasUserPermission(user, ontology.getIdentification(), query);
		if (ontology != null && ontology.getRtdbDatasource().equals(RtdbDatasource.MONGO)) {
			if (query.trim().toLowerCase().startsWith("update") || query.trim().toLowerCase().startsWith("delete")
					|| (query.trim().toLowerCase().startsWith("select") && !useQuasar())) {
				final ObjectMapper mapper = new ObjectMapper();
				final ObjectNode result = mapper.createObjectNode();
				result.put("sqlQuery", query);
				try {
					final String nativeQuery = sql2NativeTool.translateSql(query);
					result.put("nativeQuery", nativeQuery);
					return mapper.writeValueAsString(result);
				} catch (final Exception e) {
					throw new DBPersistenceException("SQL Syntax Error");
				}

			} else {
				return quasarConnector.compileQueryAsJson(ontology.getIdentification(), query, offset);
			}
		} else {
			throw new DBPersistenceException("You can only compile SQL with Mongo Ontologies");
		}
	}

	@Override
	@Transactional
	public List<String> getTables() {
		final List<String> tableList = new LinkedList<>();
		final org.hibernate.Session session = entityManager.unwrap(Session.class);
		session.doWork(connection -> {
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet rs = metaData.getTables(null, null, "%", null);
			while (rs.next()) {
				tableList.add(rs.getString(3));
			}
		});

		return tableList;
	}

	@Override
	@Transactional
	public Map<String, String> getTableColumns(String tableName) {
		final Map<String, String> tableList = new TreeMap<>();
		final org.hibernate.Session session = entityManager.unwrap(Session.class);
		session.doWork(connection -> {
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet rs = metaData.getColumns(null, null, tableName, "%");
			while (rs.next()) {
				tableList.put(rs.getString(4), rs.getString(6));
			}
		});

		return tableList;
	}

	@Override
	@Transactional
	public List<String> querySQLtoConfigDB(String query) {
		final List<String> queryResult = new LinkedList<>();
		final org.hibernate.Session session = entityManager.unwrap(Session.class);
		session.doWork(connection -> {
			final PreparedStatement statement = connection.prepareStatement(query);
			final ResultSet rs = statement.executeQuery();
			final int ncol = rs.getMetaData().getColumnCount();
			int i = 0;
			String row = "";
			while (rs.next()) {
				row = "{";
				i = 0;
				while (i < ncol) {
					row = row + "\"" + rs.getMetaData().getColumnName(i + 1) + "\": \"" + rs.getString(i + 1) + "\",";
					i++;
					if (i == ncol) {
						row = row.substring(0, row.length() - 1);
					}
				}
				queryResult.add(row + "}");
			}
		});
		return queryResult;
	}

	@Override
	@Transactional
	public List<String> updateSQLtoConfigDB(String query) {
		final List<String> queryResult = new LinkedList<>();
		final org.hibernate.Session session = entityManager.unwrap(Session.class);
		session.doWork(connection -> {
			final PreparedStatement statement = connection.prepareStatement(query);
			final int outputResult = statement.executeUpdate();
			queryResult.add(outputResult + " rows affected");
		});
		return queryResult;
	}

	private String applyDataAccess(String query, String user) throws JSQLParserException {

		boolean quasarActive = ((Boolean) resourcesService.getGlobalConfiguration().getEnv().getDatabase()
				.get("mongodb-use-quasar")).booleanValue();

		if (!quasarActive) {

			Map<String, String> queryDataAccess = ontologyService.getUserDataAccess(user);

			if (queryDataAccess.size() > 0) {
				DataAccessQueryProcessor dataAccessQueryProcessor = new DataAccessQueryProcessor(query,
						queryDataAccess);
				query = dataAccessQueryProcessor.process();

				log.info("Query replaced: " + query);
			}
		}

		return query;

	}

	private boolean isJoin(String query) {
		String joinOntology = "";
		final Pattern p = Pattern.compile(JOIN_REGEX, Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(query);
		while (m.find()) {
			joinOntology = m.group(2);
		}
		return !StringUtils.isEmpty(joinOntology);
	}

	private void metricsManagerLogControlPanelQueries(String userId, String ontology, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelQueries(userId, ontology, result);
		}
	}

	@Override
	public String querySQLAsJson(String user, String ontology, String query, int offset, int limit)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		try {
			if (ontologyService.hasEncryptionEnabled(ontology))
				query = ontologyDataService.encryptQuery(query, false);
			String result = querySQLAsJson(user, ontology, query, offset, true, limit);
			result = ontologyDataService.decryptAllUsers(result, ontology);
			metricsManagerLogControlPanelQueries(user, ontology, "OK");
			return result;
		} catch (final DBPersistenceException e) {
			log.error("Error processing query", e);
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			throw e;
		} catch (final OntologyDataUnauthorizedException e) {
			log.error("Error processing query", e);
			metricsManagerLogControlPanelQueries(user, ontology, "KO");
			throw e;
		}

	}

}
