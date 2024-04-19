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
package com.minsait.onesait.platform.persistence.nebula.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.persistence.nebula.exception.NebulaException;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaEdge;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaSpace;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaTag;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.NotValidConnectionException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NebulaGraphServiceImpl implements NebulaGraphService {

	private static final String INDEX_SUFFIX = "_index";
	private static final String RESULTS_POINTER = "/results/0/data";
	private static final String COLUMNS_POINTER = "/results/0/columns";
	private static final String ERRORS = "errors";
	private static final String SHOW_HOSTS = "SHOW HOSTS";
	private static final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public enum NebulaType {
		TAG, EDGE
	}

	@Autowired
	private NebulaPool pool;

	private static final String USER = "root";
	private static final String PASS = "nebula";

	@Override
	public void createSpace(NebulaSpace space, List<NebulaTag> tags, List<NebulaEdge> edges) {
		final Session session = getSession();
		final String createSpace = "CREATE SPACE IF NOT EXISTS " + space.getName() + "(partition_num="
				+ space.getPartitionNum() + ", replica_factor=" + space.getReplicaFactor()
				+ ", vid_type=fixed_string(30));";
		executeQuery(createSpace, session);
		final long start = System.currentTimeMillis();
		while (executeQuery("USE " + space.getName(), session).contains("SpaceNotFound")) {
			// WAIT TILL SPACE CREATION
		}
		final long end = System.currentTimeMillis();
		log.debug("Took {} milliseconds to create nebula space", end - start);
		tags.forEach(t -> createTag(space.getName(), t, session));
		edges.forEach(e -> createEdge(space.getName(), e, session));
		releaseSession(session);
	}

	@Override
	public void createSpace(NebulaSpace space) {
		final Session session = getSession();
		final String createSpace = "CREATE SPACE IF NOT EXISTS " + space.getName() + "(partition_num="
				+ space.getPartitionNum() + ", replica_factor=" + space.getReplicaFactor()
				+ ", vid_type=fixed_string(30));";
		executeQuery(createSpace, session);
		final long start = System.currentTimeMillis();
		while (executeQuery("USE " + space.getName(), session).contains("SpaceNotFound")) {
			// WAIT TILL SPACE CREATION
		}
		final long end = System.currentTimeMillis();
		log.debug("Took {} milliseconds to create nebula space", end - start);
		releaseSession(session);
	}

	@Override
	public void createTag(String space, NebulaTag tag) {
		final Session session = getSession();
		final StringBuilder builder = new StringBuilder();
		builder.append("USE " + space + "; CREATE TAG IF NOT EXISTS " + tag.getName() + "(");
		tag.getTagAttributes().entrySet().forEach(t -> {
			if (!builder.toString().endsWith(",") && !builder.toString().endsWith("(")) {
				builder.append(",");
			}
			builder.append(t.getKey() + " " + t.getValue());
		});
		builder.append(");");
		executeQuery(builder.toString(), session);
		createIndex(NebulaType.TAG, tag.getName(), session);
		releaseSession(session);
	}

	@Override
	public void dropTag(String space, String tagName) {
		final Session session = getSession();
		final StringBuilder builder = new StringBuilder();
		dropIndex(NebulaType.TAG, space, tagName, session);
		builder.append("USE " + space + "; DROP TAG IF EXISTS " + tagName + ";");
		executeQuery(builder.toString(), session);
		releaseSession(session);
	}

	@Override
	public void dropEdge(String space, String edgeName) {
		final Session session = getSession();
		final StringBuilder builder = new StringBuilder();
		dropIndex(NebulaType.EDGE, space, edgeName, session);
		builder.append("USE " + space + "; DROP EDGE IF EXISTS " + edgeName + ";");
		executeQuery(builder.toString(), session);
		releaseSession(session);
	}

	private void createTag(String space, NebulaTag tag, Session session) {
		final StringBuilder builder = new StringBuilder();
		builder.append("CREATE TAG IF NOT EXISTS " + tag.getName() + "(");
		tag.getTagAttributes().entrySet().forEach(t -> {
			if (!builder.toString().endsWith(",") && !builder.toString().endsWith("(")) {
				builder.append(",");
			}
			builder.append(t.getKey() + " " + t.getValue());
		});
		builder.append(");");
		executeQuery(builder.toString(), session);
		createIndex(NebulaType.TAG, tag.getName(), session);
	}

	private void createIndex(NebulaType type, String name, Session session) {
		final String indexName = name + INDEX_SUFFIX;
		String query = "CREATE " + type.name() + " INDEX IF NOT EXISTS " + indexName + " on " + name + "();";
		executeQuery(query, session);
		query = "REBUILD " + type.name() + " INDEX " + indexName;
		executeQuery(query, session);
	}

	private void dropIndex(NebulaType type, String space, String name, Session session) {
		// TO-DO on drop tag and drop edge, maybe more indexes exist, we need to delete
		// them all
		final String indexName = name + INDEX_SUFFIX;
		final String query = "USE " + space + "; DROP " + type.name() + " INDEX IF EXISTS " + indexName + ";";
		executeQuery(query, session);
	}

	@Override
	public void createEdge(String space, NebulaEdge edge) {
		final Session session = getSession();
		final StringBuilder builder = new StringBuilder();
		builder.append("USE " + space + "; CREATE EDGE IF NOT EXISTS " + edge.getName() + "(");
		edge.getEdgeAttributes().entrySet().forEach(t -> {
			if (!builder.toString().endsWith(",") && !builder.toString().endsWith("(")) {
				builder.append(",");
			}
			builder.append(t.getKey() + " " + t.getValue());
		});
		builder.append(");");
		executeQuery(builder.toString(), session);
		createIndex(NebulaType.EDGE, edge.getName(), session);
		releaseSession(session);
	}

	public void createEdge(String space, NebulaEdge edge, Session session) {
		final StringBuilder builder = new StringBuilder();
		builder.append("CREATE EDGE IF NOT EXISTS " + edge.getName() + "(");
		edge.getEdgeAttributes().entrySet().forEach(t -> {
			if (!builder.toString().endsWith(",") && !builder.toString().endsWith("(")) {
				builder.append(",");
			}
			builder.append(t.getKey() + " " + t.getValue());
		});
		builder.append(");");
		executeQuery(builder.toString(), session);
		createIndex(NebulaType.EDGE, edge.getName(), session);
	}

	@Override
	public JsonNode getHostsInfo() {
		final Session session = getSession();
		final String result = executeQuery(SHOW_HOSTS, session);
		releaseSession(session);
		return responseToJSON(result);
	}

	@Override
	public JsonNode getTagNames(String space) {
		final Session session = getSession();
		final String result = executeQuery("USE " + space + "; " + "SHOW TAGS;", session);
		releaseSession(session);
		return responseToJSON(result);
	}

	@Override
	public JsonNode getEdgeNames(String space) {
		final Session session = getSession();
		final String result = executeQuery("USE " + space + "; " + "SHOW EDGES;", session);
		releaseSession(session);
		return responseToJSON(result);
	}

	@Override
	public JsonNode executeNGQL(String space, String ngqlQuery) {
		final Session session = getSession();
		final String result = executeQuery("USE " + space + "; " + ngqlQuery, session);
		releaseSession(session);
		return responseToJSON(result);
	}

	@Override
	public String executeNGQLString(String space, String ngqlQuery) {
		try {
			return mapper.writeValueAsString(executeNGQL(space, ngqlQuery));
		} catch (final JsonProcessingException e) {
			log.error("Error executing query {} for entity {}", ngqlQuery, space, e);
			throw new NebulaException("Error executing query " + ngqlQuery);
		}
	}

	@Override
	public List<String> getSpaces() {
		final Session session = getSession();
		final String result = executeQuery("SHOW SPACES;", session);
		releaseSession(session);
		final JsonNode node = responseToJSON(result);
		final List<String> spaces = new ArrayList<>();
		if (node != null) {
			node.forEach(n -> spaces.add(n.get("Name").asText()));
		}
		return spaces;
	}

	@Override
	public void dropSpace(String space) {
		final Session session = getSession();
		final String result = executeQuery("DROP SPACE " + space + ";", session);
		releaseSession(session);
		responseToJSON(result);
	}

	private Session getSession() {
		try {
			return pool.getSession(USER, PASS, false);
		} catch (NotValidConnectionException | IOErrorException | AuthFailedException
				| ClientServerIncompatibleException e) {
			log.error("Could not acquire nebula session", e);
			throw new NebulaException("Could not acquire nebula session");
		}
	}

	private void releaseSession(Session session) {
		if (session != null) {
			session.release();
		}
	}

	private String executeQuery(String query, Session session) {
		try {
			log.trace("Executing nebula query {}", query);
			final String result = session.executeJson(query);
			log.trace("Result of query is {}", result);
			return result;
		} catch (final Exception e) {
			log.error("Could not execute query {}", query, e);
			throw new NebulaException("Could not execute query " + query);
		}
	}

	private JsonNode responseToJSON(String response) {
		try {
			final JsonNode parsedResponse = mapper.readValue(response, JsonNode.class);
			if (!parsedResponse.path(ERRORS).isMissingNode() && parsedResponse.path(ERRORS).isArray()
					&& parsedResponse.path(ERRORS).get(0).path("code").asInt() != 0) {
				log.error("Nebula graph query execution result contains errors {}", parsedResponse.path(ERRORS));
				return parsedResponse.path(ERRORS);
			}
			final ArrayNode an = mapper.createArrayNode();
			if (!parsedResponse.path("results").isMissingNode()
					&& !parsedResponse.at(RESULTS_POINTER).isMissingNode()) {
				for (int i = 0; i < parsedResponse.at(RESULTS_POINTER).size(); i++) {
					final ObjectNode n = mapper.createObjectNode();
					final ArrayNode r = (ArrayNode) parsedResponse.at(RESULTS_POINTER);
					for (int j = 0; j < parsedResponse.at(COLUMNS_POINTER).size(); j++) {
						n.set(parsedResponse.at(COLUMNS_POINTER).get(j).asText(), r.get(i).get("row").get(j));
					}
					an.add(n);
				}
			}
			return an;
		} catch (final Exception e) {
			return null;
		}
	}

	@Override
	public List<NebulaTag> getTags(String space) {
		final JsonNode tagNames = getTagNames(space);
		final List<String> names = new ArrayList<>();
		tagNames.forEach(n -> {
			if(!n.path("Name").isMissingNode()) {
				names.add(n.get("Name").asText());
			}
		});
		final List<NebulaTag> tags = new ArrayList<>();
		final Session session = getSession();
		executeQuery("USE  " + space + ";", session);
		names.forEach(n -> {
			try {
				final String r = executeQuery("DESCRIBE TAG " + n, session);
				final JsonNode tagNode = responseToJSON(r);
				final Map<String, String> atts = new HashMap<>();
				tagNode.forEach(an -> atts.put(an.get("Field").asText(), an.get("Type").asText()));
				tags.add(NebulaTag.builder().name(n).tagAttributes(atts).build());
			} catch (final Exception e) {
				log.warn("Could not process tag {}", n, e.getMessage());
			}
		});
		releaseSession(session);
		return tags;
	}

	@Override
	public List<NebulaEdge> getEdges(String space) {
		final JsonNode edgeNames = getEdgeNames(space);
		final List<String> names = new ArrayList<>();
		edgeNames.forEach(n -> {
			if(!n.path("Name").isMissingNode()) {
				names.add(n.get("Name").asText());
			}
		});
		final List<NebulaEdge> tags = new ArrayList<>();
		final Session session = getSession();
		executeQuery("USE  " + space + ";", session);
		names.forEach(n -> {
			try {
				final String r = executeQuery("DESCRIBE EDGE " + n, session);
				final JsonNode tagNode = responseToJSON(r);
				final Map<String, String> atts = new HashMap<>();
				tagNode.forEach(an -> atts.put(an.get("Field").asText(), an.get("Type").asText()));
				tags.add(NebulaEdge.builder().name(n).edgeAttributes(atts).build());
			} catch (final Exception e) {
				log.warn("Could not process edge {}", n, e.getMessage());
			}
		});
		releaseSession(session);
		return tags;
	}

	@Override
	public void alter(String space, NebulaType nebulaType, String elementName, Map<String, String> toAdd,
			List<String> toDrop, Map<String, String> toChange) {
		if (toAdd.isEmpty() && toDrop.isEmpty() && toChange.isEmpty()) {
			return;
		}
		final Session session = getSession();
		final StringBuilder builder = new StringBuilder();
		dropIndex(nebulaType, space, elementName, session);
		builder.append("USE " + space + ";");
		builder.append("ALTER " + nebulaType.name() + " " + elementName + " ");
		if (!toDrop.isEmpty()) {
			builder.append("DROP (" + String.join(",", toDrop) + ") ");
		}
		if (!toAdd.isEmpty()) {
			if (!toDrop.isEmpty()) {
				builder.append(", ");
			}
			builder.append("ADD (");
			toAdd.entrySet().forEach(t -> {
				if (!builder.toString().endsWith(",") && !builder.toString().endsWith("(")) {
					builder.append(",");
				}
				builder.append(t.getKey() + " " + t.getValue());
			});
			builder.append(")");
		}
		if (!toChange.isEmpty()) {
			if (!toDrop.isEmpty() || !toAdd.isEmpty()) {
				builder.append(", ");
			}
			builder.append("CHANGE (");
			toChange.entrySet().forEach(t -> {
				if (!builder.toString().endsWith(",") && !builder.toString().endsWith("(")) {
					builder.append(",");
				}
				builder.append(t.getKey() + " " + t.getValue());
			});
			builder.append(")");
		}
		builder.append(";");
		executeQuery(builder.toString(), session);
		createIndex(nebulaType, elementName, session);
		releaseSession(session);

	}

	@Override
	public NebulaSpace getSpace(String space) {
		final JsonNode result = executeNGQL(space, "DESCRIBE SPACE " + space);
		try {
			return mapper.treeToValue(result.get(0), NebulaSpace.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			log.error("Error getting space {}", space, e);
			throw new NebulaException(e);
		}
	}

}
