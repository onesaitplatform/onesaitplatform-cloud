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
package com.minsait.onesait.platform.persistence.nebula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaEdge;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaSpace;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaTag;
import com.minsait.onesait.platform.persistence.nebula.service.NebulaGraphService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@Ignore
public class NebulaTests {

	@Autowired
	private NebulaGraphService service;
	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testBasics() throws JsonProcessingException {
		final NebulaSpace space = NebulaSpace.builder().name("basketballplayer").partitionNum(15).replicaFactor(1)
				.build();
		service.executeNGQL(space.getName(), "DROP SPACE " + space.getName());
		service.createSpace(space, new ArrayList<>(), new ArrayList<>());

		Map<String, String> atts = new HashMap<>();
		atts.put("name", "string");
		atts.put("age", "int");

		service.createTag(space.getName(), NebulaTag.builder().name("player").tagAttributes(atts).build());

		atts = new HashMap<>();
		atts.put("name", "string");

		service.createTag(space.getName(), NebulaTag.builder().name("team").tagAttributes(atts).build());

		atts = new HashMap<>();
		atts.put("start_year", "int");
		atts.put("end_year", "int");

		service.createEdge(space.getName(), NebulaEdge.builder().name("serve").edgeAttributes(atts).build());

		atts = new HashMap<>();
		atts.put("degree", "int");

		service.createEdge(space.getName(), NebulaEdge.builder().name("follow").edgeAttributes(atts).build());

		log.info(mapper.writeValueAsString(service.getHostsInfo()));
		log.info(mapper.writeValueAsString(service.getTags(space.getName())));
		log.info(mapper.writeValueAsString(service.getEdges(space.getName())));
		log.info(mapper.writeValueAsString(service.executeNGQL(space.getName(), "DESCRIBE TAG player;")));
		service.executeNGQL(space.getName(),
				"INSERT VERTEX player(name, age) VALUES \"player100\":(\"Tim Duncan\", 42);\n"
						+ "INSERT VERTEX player(name, age) VALUES \"player101\":(\"Tony Parker\", 36);\n"
						+ "INSERT VERTEX player(name, age) VALUES \"player102\":(\"LaMarcus Aldridge\", 33);\n"
						+ "INSERT VERTEX player(name, age) VALUES \"player103\":(\"Michael Jordan\", 52);\n"
						+ "INSERT VERTEX team(name) VALUES \"team203\":(\"Trail Blazers\"), \"team204\":(\"Spurs\");");
		service.executeNGQL(space.getName(), "INSERT EDGE follow(degree) VALUES \"player101\" -> \"player100\":(95);\n"
				+ "INSERT EDGE follow(degree) VALUES \"player101\" -> \"player102\":(90);\n"
				+ "INSERT EDGE follow(degree) VALUES \"player101\" -> \"player103\":(70);\n"
				+ "INSERT EDGE follow(degree) VALUES \"player102\" -> \"player100\":(75);\n"
				+ "INSERT EDGE follow(degree) VALUES \"player102\" -> \"player100\":(75);\n"
				+ "INSERT EDGE follow(degree) VALUES \"player103\" -> \"player101\":(55);\n"
				+ "INSERT EDGE serve(start_year, end_year) VALUES \"player101\" -> \"team204\":(1999, 2018),\"player102\" -> \"team203\":(2006,  2015),\"player103\" -> \"team203\":(1990,  2022);");
		log.info(mapper.writeValueAsString(
				service.executeNGQL(space.getName(), "FETCH PROP ON player \"player100\" YIELD properties(vertex);")));
		log.info(mapper.writeValueAsString(
				service.executeNGQL(space.getName(), "SHOW SPACES;")));
	}

}
