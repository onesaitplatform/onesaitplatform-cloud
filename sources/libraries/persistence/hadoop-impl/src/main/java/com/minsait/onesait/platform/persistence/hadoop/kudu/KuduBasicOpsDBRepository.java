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
import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.IMPALA_TEMPLATE_JDBC_BEAN_NAME;
import static com.minsait.onesait.platform.persistence.hadoop.common.NameBeanConst.KUDU_CLIENT;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.client.Delete;
import org.apache.kudu.client.Insert;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduSession;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.OperationResponse;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.SessionConfiguration.FlushMode;
import org.apache.kudu.client.Update;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.BulkWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResult;
import com.minsait.onesait.platform.commons.model.ComplexWriteResultType;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.hadoop.common.CommonQuery;
import com.minsait.onesait.platform.persistence.hadoop.config.condition.HadoopEnabledCondition;
import com.minsait.onesait.platform.persistence.hadoop.resultset.KuduResultSetExtractor;
import com.minsait.onesait.platform.persistence.hadoop.resultset.SingleKuduResultSetExtractor;
import com.minsait.onesait.platform.persistence.hadoop.rowmapper.SingleKuduRowMapper;
import com.minsait.onesait.platform.persistence.hadoop.util.JsonRelationalHelperKuduImpl;
import com.minsait.onesait.platform.persistence.interfaces.BasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.models.ErrorResult;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;

@Component("KuduBasicOpsDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
@Conditional(HadoopEnabledCondition.class)
public class KuduBasicOpsDBRepository implements BasicOpsDBRepository {

	@Autowired
	@Qualifier(IMPALA_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate impalaJdbcTemplate;

	@Autowired
	@Qualifier(KUDU_CLIENT)
	private KuduClient kuduClient;

	@Autowired
	private JsonRelationalHelperKuduImpl jsonRelationalHelperKuduImpl;

	@Value("${onesaitplatform.database.kudu.client.prefix:impala::default.}")
	private String impalaPrefix;

	@Value("${onesaitplatform.database.kudu.client.flushMode:MANUAL_FLUSH}")
	private String sessionFlushMode;

	@Value("${onesaitplatform.database.kudu.client.sessionTimeout:60000}")
	private int sessionTimeout;

	private static final CCJSqlParserManager parserManager = new CCJSqlParserManager();

	private String insert(KuduTable kuduTable, String instance, KuduSession kuduSession, Schema schema) {
		try {
			String id = UUID.randomUUID().toString();
			Insert insert = kuduTable.newInsert();
			jsonRelationalHelperKuduImpl.instanceToPartialRow(schema, instance, insert.getRow(), id, false);
			kuduSession.apply(insert);
			return id;
		} catch (KuduException e) {
			log.error("Error inserting", e);
			throw new DBPersistenceException("Error generating insert " + e);
		}
	}

	private void delete(KuduTable kuduTable, Schema schema, KuduSession kuduSession, String id, String instance) {
		try {
			Delete delete = kuduTable.newDelete();
			jsonRelationalHelperKuduImpl.instanceToPartialRow(schema, instance, delete.getRow(), id, true);
			kuduSession.apply(delete);
		} catch (KuduException e) {
			log.error("Error deleting", e);
			throw new DBPersistenceException("Error generating delete " + e);
		}
	}

	private void deletePartialBulk(KuduTable kuduTable, KuduSession kuduSession, List<BulkWriteResult> result,
			List<String> instances, List<OperationResponse> lopr) {
		List<ColumnSchema> lpkey = kuduTable.getSchema().getPrimaryKeyColumns();
		for (int i = 0; i < lopr.size(); i++) {
			if (!lopr.get(i).hasRowError()) {
				delete(kuduTable, kuduTable.getSchema(), kuduSession, result.get(i).getId(), instances.get(i));
			}
		}
		try {
			kuduSession.flush();
		} catch (KuduException e) {
			throw new DBPersistenceException("Error removing partial row in flush ", e);
		}
		if (kuduSession.getPendingErrors().getRowErrors().length > 0) {
			throw new DBPersistenceException("Error removing partial row " + kuduSession.getPendingErrors().toString());
		}
	}

	@Override
	public String insert(String ontology, String instance) {
		try {
			KuduTable ktable = kuduClient.openTable(impalaPrefix + ontology);
			KuduSession session = kuduClient.newSession();
			session.setFlushMode(FlushMode.valueOf(sessionFlushMode));
			String id = insert(ktable, instance, session, ktable.getSchema());
			List<OperationResponse> lopr = session.flush();
			session.close();
			return id;
		} catch (KuduException e) {
			log.error("Error getting table", e);
			throw new DBPersistenceException("Error getting table for insert " + e);
		}
	}

	@Override
	public ComplexWriteResult insertBulk(String ontology, List<String> instances, boolean order, boolean includeIds) {
		List<BulkWriteResult> resultAux = new ArrayList<>();
		List<BulkWriteResult> result = new ArrayList<>();
		List<DBPersistenceException> errors = new ArrayList<>();

		if (instances != null) {
			KuduTable ktable;
			try {
				ktable = kuduClient.openTable(impalaPrefix + ontology);
			} catch (KuduException e) {
				log.error("Error in insertBulk", e);
				throw new DBPersistenceException("Error getting table for insertBulk " + e);
			}
			KuduSession session = kuduClient.newSession();
			session.setFlushMode(FlushMode.valueOf(sessionFlushMode));

			for (String instance : instances) {

				BulkWriteResult insertResult = new BulkWriteResult();

				try {

					String id = insert(ktable, instance, session, ktable.getSchema());
					insertResult.setId(id);
					insertResult.setOk(true);

				} catch (Exception e) {
					log.error("error inserting bulk instance " + instance, e);
					insertResult.setOk(false);
					errors.add(
							new DBPersistenceException(new ErrorResult(ErrorResult.ErrorType.ERROR, e.getMessage())));
					break;
				}

				resultAux.add(insertResult);
			}
			try {
				List<OperationResponse> lopr = session.flush();
				for (int i = 0; i < lopr.size(); i++) {
					OperationResponse opres = lopr.get(i);
					if (opres.hasRowError()) {
						deletePartialBulk(ktable, session, resultAux, instances, lopr);
						if (opres.getRowError().getErrorStatus().isAlreadyPresent()) {
							errors.add(new DBPersistenceException(new ErrorResult(ErrorResult.ErrorType.DUPLICATED,
									opres.getRowError().getErrorStatus().toString())));
						}
						break;
					} else {
						result.add(resultAux.get(i));
					}
				}
				session.close();
			} catch (KuduException e) {
				log.error("Error closing", e);
				throw new DBPersistenceException("Error flush/close session " + e);
			}
			if (!errors.isEmpty()) {
				List<ErrorResult> eResult = errors.stream().map(dbpex -> dbpex.getErrorsResult().get(0))
						.collect(Collectors.toList());
				log.error("Error processing bulk insert request");
				throw new DBPersistenceException(eResult, "Error processing bulk insert request");
			}
		}

		ComplexWriteResult complexWriteResult = new ComplexWriteResult();
		complexWriteResult.setType(ComplexWriteResultType.BULK);
		complexWriteResult.setData(result);

		return complexWriteResult;

	}

	@Override
	public MultiDocumentOperationResult updateNative(String ontology, String updateStmt, boolean includeIds) {
		KuduTable ktable;
		try {
			ktable = kuduClient.openTable(impalaPrefix + ontology);
		} catch (KuduException e) {
			log.error("Error in updateNative ", e);
			throw new DBPersistenceException("Error getting table for updateNative " + e);
		}
		KuduSession session = kuduClient.newSession();
		session.setFlushMode(FlushMode.valueOf(sessionFlushMode));
		Update upd = ktable.newUpdate();
		PartialRow prow = upd.getRow();
		try {
			translateUpdateToRow(updateStmt, prow, ktable.getSchema());
		} catch (JSQLParserException e) {
			log.error("Error updating ", e);
			throw new DBPersistenceException("Error parsing update for table " + e);
		}

		MultiDocumentOperationResult result = new MultiDocumentOperationResult();

		try {
			session.apply(upd);
			List<OperationResponse> lopr = session.flush();
			session.close();
			if (lopr.get(0).getRowError() != null && lopr.get(0).getRowError().getErrorStatus().isNotFound()) {
				result.setCount(0);
			} else {
				result.setCount(1);
			}
		} catch (KuduException e) {
			log.error("Error updating instance ", e);
			throw new DBPersistenceException("Error updating instance " + e);
		}
		return result;
	}

	private void translateUpdateToRow(String query, PartialRow prow, Schema schema) throws JSQLParserException {
		final net.sf.jsqlparser.statement.update.Update statement = (net.sf.jsqlparser.statement.update.Update) parserManager
				.parse(new StringReader(query));
		final List<Column> columns = statement.getColumns();
		final List<Expression> expressions = statement.getExpressions();
		Expression where = statement.getWhere();
		for (int i = 0; i < columns.size(); i++) {
			String key = columns.get(i).getColumnName();
			String o = expressions.get(i).toString();
			switch (schema.getColumn(key).getType()) {
			case STRING:
				prow.addString(key, o.substring(1, o.length() - 1));
				break;
			case UNIXTIME_MICROS:
				prow.addTimestamp(key, new Timestamp(
						ISODateTimeFormat.dateTimeParser().parseDateTime(o.substring(1, o.length() - 1)).getMillis()));
				break;
			case BOOL:
				prow.addBoolean(key, Boolean.getBoolean(o));
				break;
			case FLOAT:
				prow.addFloat(key, Float.parseFloat(o));
				break;
			case DOUBLE:
				prow.addDouble(key, Double.parseDouble(o));
				break;
			case DECIMAL:
				prow.addDecimal(key, new BigDecimal(o));
				break;
			case INT8:
			case INT16:
			case INT32:
			case INT64:
				prow.addInt(key, Integer.parseInt(o));
				break;
			default:
				break;
			}
		}

		String[] whereList = where.toString().split("(?i) and ");
		for (String whereExp : whereList) {
			String[] fieldVal = whereExp.split(" = ");
			if (fieldVal.length != 2) {
				throw new DBPersistenceException(
						"Incompatible where expresion, kudu only support update by primary key, found: " + whereExp);
			} else {
				String key = fieldVal[0].trim();
				String o = fieldVal[1].trim();
				switch (schema.getColumn(key).getType()) {
				case STRING:
					prow.addString(key, o.substring(1, o.length() - 1));
					break;
				case UNIXTIME_MICROS:
					prow.addTimestamp(key,
							Timestamp.valueOf(o.substring(1, o.length() - 1).replace("T", " ").replace("Z", "")));
					break;
				case BOOL:
					prow.addBoolean(key, Boolean.getBoolean(o));
					break;
				case FLOAT:
					prow.addFloat(key, Float.parseFloat(o));
					break;
				case DOUBLE:
					prow.addDouble(key, Double.parseDouble(o));
					break;
				case DECIMAL:
					prow.addDecimal(key, new BigDecimal(o));
					break;
				case INT8:
				case INT16:
				case INT32:
				case INT64:
					prow.addInt(key, Integer.parseInt(o));
					break;
				default:
					break;
				}
			}
		}

	}

	@Override
	public MultiDocumentOperationResult updateNative(String collection, String query, String data, boolean includeIds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public MultiDocumentOperationResult deleteNative(String collection, String query, boolean includeIds) {
		int count = impalaJdbcTemplate.update(query);
		MultiDocumentOperationResult result = new MultiDocumentOperationResult();
		result.setCount(count);
		return result;
	}

	@Override
	public List<String> queryNative(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryNative(String ontology, String query, int offset, int limit) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryNativeAsJson(String ontology, String query) {
		return queryNativeAsJson(ontology, query, 0, -1);
	}

	@Override
	public String queryNativeAsJson(String ontology, String query, int offset, int limit) {
		return impalaJdbcTemplate.query(query, new KuduResultSetExtractor());
	}

	@Override
	public String findById(String ontology, String objectId) {
		try {
			String sql = String.format(CommonQuery.FIND_BY_ID, ontology, objectId);
			return impalaJdbcTemplate.query(sql, new SingleKuduResultSetExtractor());
		} catch (Exception e) {
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public String querySQLAsJson(String ontology, String query) {
		return querySQLAsJson(ontology, query, 0);
	}

	@Override
	public String querySQLAsTable(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String querySQLAsJson(String ontology, String query, int offset) {
		return queryNativeAsJson(ontology, query, offset, -1);
	}

	@Override
	public String querySQLAsTable(String ontology, String query, int offset) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String findAllAsJson(String ontology) {
		try {
			String sql = String.format(CommonQuery.FIND_ALL, ontology);
			return impalaJdbcTemplate.query(sql, new KuduResultSetExtractor());
		} catch (Exception e) {
			log.error("Error in findAllAsJson", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public String findAllAsJson(String ontology, int limit) {
		String query = String.format(CommonQuery.FIND_ALL_WITH_LIMIT, ontology, limit);
		return queryNativeAsJson(ontology, query);
	}

	@Override
	public List<String> findAll(String ontology) {
		try {
			String sql = String.format(CommonQuery.FIND_ALL, ontology);
			return impalaJdbcTemplate.query(sql, new SingleKuduRowMapper());
		} catch (Exception e) {
			log.error("Error in findAll", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public List<String> findAll(String ontology, int limit) {
		try {
			String sql = String.format(CommonQuery.FIND_ALL_WITH_LIMIT, ontology, limit);
			return impalaJdbcTemplate.query(sql, new SingleKuduRowMapper());
		} catch (Exception e) {
			log.error("findAll", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public long count(String ontology) {
		try {
			String sql = String.format(CommonQuery.COUNT, ontology);
			return impalaJdbcTemplate.queryForObject(sql, Integer.class);
		} catch (Exception e) {
			log.error("Error in count", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public MultiDocumentOperationResult delete(String ontology, boolean includeIds) {
		try {
			String sql = String.format(CommonQuery.DELETE_ALL, ontology);
			int count = impalaJdbcTemplate.update(sql);

			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(count);
			return result;
		} catch (Exception e) {
			log.error("Error deleting", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public long countNative(String collectionName, String query) {
		try {
			return impalaJdbcTemplate.queryForObject(query, Integer.class);
		} catch (Exception e) {
			log.error("Error in countNative", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public MultiDocumentOperationResult deleteNativeById(String ontologyName, String objectId) {
		try {
			String sql = String.format(CommonQuery.DELETE_BY_ID, ontologyName, objectId);
			int count = impalaJdbcTemplate.update(sql);

			MultiDocumentOperationResult result = new MultiDocumentOperationResult();
			result.setCount(count);
			return result;
		} catch (Exception e) {
			log.error("Error deleting native", e);
			throw new DBPersistenceException(new ErrorResult(ErrorResult.PersistenceType.IMPALA, e.getMessage()),
					e.getMessage());
		}
	}

	@Override
	public MultiDocumentOperationResult updateNativeByObjectIdAndBodyData(String ontologyName, String objectId,
			String body) {
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String ontology, String updateStmt)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryUpdateTransactionCompensationNative(String collection, String query, String data)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(String ontologyName, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection, String query)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public List<String> queryDeleteTransactionCompensationNative(String collection) {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

	@Override
	public String queryDeleteTransactionCompensationNativeById(String collection, String objectId)
			throws DBPersistenceException {
		// TODO Auto-generated method stub
		throw new DBPersistenceException(NOT_IMPLEMENTED, new NotImplementedException(NOT_IMPLEMENTED));
	}

}
