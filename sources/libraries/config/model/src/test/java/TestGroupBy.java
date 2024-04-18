/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestGroupBy {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void testGroupByOneToMany() throws JsonMappingException, JsonProcessingException {
		final String o1 = "{\"id\":\"administrator\",\"fullName\":\"Administrator\",\"token\":\"78gd\", \"token_id\": \"1\"}";
		final String o2 = "{\"id\":\"administrator\",\"fullName\":\"Administrator\",\"token\":\"78fafgd\", \"token_id\": \"2\"}";
		final String o3 = "{\"id\":\"administrator\",\"fullName\":\"Administrator\",\"token\":\"7845gd\", \"token_id\": \"3\"}";

		final JsonNode n1 = MAPPER.readValue(o1, JsonNode.class);
		final JsonNode n2 = MAPPER.readValue(o2, JsonNode.class);
		final JsonNode n3 = MAPPER.readValue(o3, JsonNode.class);

		final List<String> parentProps = List.of("id", "fullName");
		final List<String> childProps = List.of("token", "token_id");

		System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(List.of(n1, n2, n3)));

		final Map<ObjectNode, List<JsonNode>> result = List.of(n1, n2, n3).stream().collect(Collectors.groupingBy(o -> {
			final ObjectNode n = MAPPER.createObjectNode();
			parentProps.forEach(p -> {
				n.set(p, o.get(p));
			});
			return n;
		}));
		final List<ObjectNode> r = result.entrySet().stream().map(e -> {
			final ObjectNode workingO = e.getKey();
			// if one to many
			final ArrayNode a = MAPPER.createArrayNode();
			workingO.set("tokens", a);
			e.getValue().forEach(t -> {
				final ObjectNode token = MAPPER.createObjectNode();
				childProps.forEach(cp -> {
					token.set(cp, t.get(cp));
				});
				a.add(token);
			});

			return workingO;
		}).collect(Collectors.toList());

		System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(r));

	}

	@Test
	public void testGroupByOneToOne() throws JsonMappingException, JsonProcessingException {
		final String o1 = "{\"id\":\"administrator\",\"fullName\":\"Administrator\",\"token\":\"78gd\", \"token_id\": \"1\"}";

		final JsonNode n1 = MAPPER.readValue(o1, JsonNode.class);
		System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(List.of(n1)));

		final List<String> parentProps = List.of("id", "fullName");
		final List<String> childProps = List.of("token", "token_id");

		final Map<ObjectNode, List<JsonNode>> result = List.of(n1).stream().collect(Collectors.groupingBy(o -> {
			final ObjectNode n = MAPPER.createObjectNode();
			parentProps.forEach(p -> {
				n.set(p, o.get(p));
			});
			return n;
		}));
		final List<ObjectNode> r = result.entrySet().stream().map(e -> {
			final ObjectNode workingO = e.getKey();
			// if one to one
			final ObjectNode a = MAPPER.createObjectNode();
			e.getValue().forEach(t -> {
				final ObjectNode token = MAPPER.createObjectNode();
				childProps.forEach(cp -> {
					token.set(cp, t.get(cp));
				});
				workingO.set("token", token);
			});

			// Borro props del hijo al final
//			childProps.forEach(p -> workingO.remove(p));
			return workingO;
		}).collect(Collectors.toList());

		System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(r));

	}

}
