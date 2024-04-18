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
package com.minsait.onesait.platform.persistence.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.BsonArray;
import org.bson.conversions.Bson;
import org.bson.types.BasicBSONList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoQueryAndParams {

	@Getter
	@Setter
	private String originalQuery;
	@Getter
	@Setter
	private Bson finalQuery = null;
	@Getter
	@Setter
	private int limit = -1;

	@Getter
	@Setter
	private Bson sort = null;

	@Getter
	@Setter
	private int skip = -1;

	@Getter
	@Setter
	private Bson projection = null;

	@Getter
	@Setter
	private List<Bson> aggregateQuery = null;

	@Getter
	@Setter
	private boolean aggregateAllowDiskUse = false;

	public MongoQueryAndParams() {
		// default constructor
	}

	public void parseQuery(String originalQuery, int limit, int skip) throws Exception {
		this.originalQuery = originalQuery;
		this.limit = limit;
		this.skip = skip;
		//
		String query = originalQuery;
		String subquery = null;
		StringBuffer sb;
		String temp = null;
		try {
			sb = new StringBuffer();
			final Pattern pattern = Pattern
					.compile("\\{\\\\*\"_id\\\\*\"\\s*:\\s*\\{\\s*\"\\$oid\"\\s*:\\s*\\\\*\"(.*)\\\\*\"\\s*}\\s*}");
			final Matcher matcher = pattern.matcher(query);
			boolean changed = false;
			while (matcher.find()) {
				changed = true;
				matcher.group(0);
				temp = matcher.group(1);
				matcher.appendReplacement(sb, "{\"_id\":ObjectId(\"" + temp + "\")}");
			}
			matcher.appendTail(sb);
			if (changed)
				query = sb.toString();

			if (query.indexOf(".aggregate(") != -1) {
				subquery = query.substring(query.indexOf(".aggregate("), query.length());
				temp = subquery.substring(0 + 11, subquery.length() - 1);

				processOptionsFromAggregate(temp);

				final BsonArray parse = BsonArray.parse(temp);
				final BasicDBList dbList = new BasicDBList();
				dbList.addAll(parse);

				final BasicBSONList listBson = dbList;

				aggregateQuery = new ArrayList<>();
				for (int i = 0; i < listBson.size(); i++) {
					aggregateQuery.add((Bson) listBson.get(i));
				}
				if (skip != -1) {
					aggregateQuery.add((Bson) BasicDBObject.parse("{$skip:" + skip + "}"));
				}
				if (limit != -1) {
					aggregateQuery.add((Bson) BasicDBObject.parse("{$limit:" + limit + "}"));
				}

			} else {

				if (query.indexOf(".limit(") != -1) {
					subquery = query.substring(query.indexOf(".limit("), query.length());
					temp = subquery.substring(0 + 7, subquery.indexOf(")"));
					if (this.limit > Integer.parseInt(temp)) {
						this.limit = Integer.parseInt(temp);
					}
				}
				if (query.indexOf(".sort(") != -1) {
					subquery = query.substring(query.indexOf(".sort("), query.length());
					temp = subquery.substring(0 + 6, subquery.indexOf(")"));
					sort = (Bson) BasicDBObject.parse(temp);
				}
				if (query.indexOf(".skip(") != -1) {
					subquery = query.substring(query.indexOf(".skip("), query.length());
					temp = subquery.substring(0 + 6, subquery.indexOf(")"));
					this.skip = Integer.parseInt(temp);
				}
				//
				if (query.indexOf(".find(") != -1) {
					subquery = query.substring(query.indexOf(".find("), query.length());
					temp = subquery.substring(0 + 6, indexOfParenthesisSubstring(subquery, 0 + 6));
					if (temp.trim().equals(""))
						temp = "{}";
				} else {
					if (!query.startsWith("{")) {
						sb = new StringBuffer(query);
						sb.insert(0, "{");
						sb.append("}");
						query = sb.toString();
					}
					temp = query;
				}

				finalQuery = BasicDBObject.parse(temp);
				final BsonArray parseArray = BsonArray.parse("[" + temp + "]");
				final BasicDBList listBson = new BasicDBList();
				listBson.addAll(parseArray);

				if (listBson.size() == 2) {
					projection = (Bson) listBson.get(1);
				}
			}

		} catch (final Exception e) {
			log.error("Error parseQuery: {}", e.getMessage(), e);
			throw e;
		}
	}

	private void processOptionsFromAggregate(String aggregate) throws MongoQueryException {
		int endOfOptions = aggregate.lastIndexOf('}');
		int endOfPipe = aggregate.lastIndexOf(']');

		boolean thereAreOptions = endOfOptions > 0 && endOfPipe > 0 && endOfOptions > endOfPipe;

		if (thereAreOptions) {

			int beginOperation = getInitOptions(aggregate, endOfOptions);

			if (beginOperation == -1) {
				throw new MongoQueryException("Malformed mongodb aggregate operation");
			} else {
				getAllowDiskUseValue(aggregate, beginOperation, endOfOptions);
			}
		}
	}

	private int getInitOptions(String aggregate, int endOfOptions) {
		int counter = 1;
		int initOptions = endOfOptions;
		while (counter > 0 && initOptions > -1) {
			initOptions--;
			char c = aggregate.charAt(initOptions);
			if (c == '{') {
				counter--;
			} else if (c == '}') {
				counter++;
			}
		}
		return initOptions;
	}

	private void getAllowDiskUseValue(String aggregate, int beginOperation, int endOfOptions)
			throws MongoQueryException {
		String optionsString = aggregate.substring(beginOperation, endOfOptions + 1);
		BasicDBObject options = BasicDBObject.parse(optionsString);
		if (options.size() > 0) {
			if (options.size() > 1) {
				throw new MongoQueryException("Option not supported");
			} else {
				// only allowDiskUse is supported
				if (!options.containsKey((Object) "allowDiskUse")) {
					throw new MongoQueryException("Option not supported");
				} else {
					Boolean allowDiskUse = (Boolean) options.get((Object) "allowDiskUse");
					aggregateAllowDiskUse = allowDiskUse.booleanValue();
				}
			}
		}
	}

	private int indexOfParenthesisSubstring(String s, int offset) {
		int count = 1;
		for (int i = offset; i < s.length(); i++) {
			if (s.charAt(i) == '(') {
				count++;
			} else if (s.charAt(i) == ')') {
				count--;
			}
			if (count == 0) {
				return i;
			}
		}

		return -1;
	}

}
