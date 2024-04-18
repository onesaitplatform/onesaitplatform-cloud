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
package com.minsait.onesait.platform.examples.iotclient4springboot.repository;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.minsait.onesait.platform.examples.iotclient4springboot.model.TestTimeOntology;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class CheckTestTimeRepository {

	@Autowired
	private TestTimeRepository testTimeRepository;

	@PostConstruct
	public void testCRUD() {
		List<TestTimeOntology> l = testTimeRepository.getTestTimeByDynamicQuery("select * from TestTime");

		log.info("" + l.get(0).getTestTime().getTimestamp().get$date());

		List<TestTimeOntology> l2 = testTimeRepository.getTestTimeByDynamicQueryNative("db.TestTime.find({})");

		log.info("" + l2.get(0).getTestTime().getTimestamp().get$date());
	}

}
