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
package org.apache.zeppelin.onesaitplatform.help;

public class IotBrokerHelp {
	
	public final static String HEADER = "* onesaitplatform help *";
	public final static String DEBUG_MODE = "To use setDebugMode(), please conect to onesait platform with sentence setDebugMode(true / false)";
	public final static String INIT_CONNECTION = "To use initConnection(), please conect to onesait platform with sentence initConnection(\"name_iotClient\", \"token_iotClient\")";
	public final static String SQL_QUERY = "To use sql query, please use sentence like 'select * from table'";
	public final static String NATIVE_QUERY = "To use native query, please use sentence like 'db.collection.find().limit(3)')";
	public final static String AS_Z_TABLE = "To use asZTable(), please use sentence like asZTable(select c.table.col1, c.table.col2. from table AS c))";
	public final static String INSERT = "To use insert(), please use sentence like insert('[{},{},...{}]' / z.get(\"context_features\"))";
	public final static String PAGINATED_QUERY = "To use paginatedQuery(), please use sentence like paginatedQuery(db.collection.find() / select * from table)" 
												+ " - (it appends offset N limit M (SQL) or .skip(N).limit(M) (NATIVE) so these words must not be in the query expression)";
	
}
