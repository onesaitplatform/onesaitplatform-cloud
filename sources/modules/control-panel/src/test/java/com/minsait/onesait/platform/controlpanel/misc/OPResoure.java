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
package com.minsait.onesait.platform.controlpanel.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Tag;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.TagRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class OPResoure {

	@Autowired
	private OPResourceRepository opResourceRepository;
	@Autowired
	private TagRepository tagRepository;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void testBasics() {
		final Tag t1 = new Tag();
		t1.setName("BPO");
		final Tag t2 = new Tag();
		t2.setName("EURA");
//		opResourceRepository.findAll().forEach(or -> {
//			final OPResourceVO vo = new OPResourceVO();
//			vo.setId(or.getId());
//			vo.setName(or.getIdentification());
//			vo.setType(or.getClass().getSimpleName());
//			t1.getResources().add(vo);
//			t2.getResources().add(vo);
//		});
		tagRepository.save(t1);
		tagRepository.save(t2);
//		t = new Tag();
//		t.setName("Prosumers");
//		t.getResources().add(r);
//		tagRepository.save(t);
	}

//	@Test
	public void testFind() throws JsonProcessingException {
		final long time = System.currentTimeMillis();
		tagRepository.findAll();
		log.info("TOtal time: {}", System.currentTimeMillis() - time);
	}

}
