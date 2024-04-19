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
package com.minsait.onesait.platform.serverless;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.serverless.model.Application;
import com.minsait.onesait.platform.serverless.model.Function;
import com.minsait.onesait.platform.serverless.repository.ApplicationRepository;

import lombok.extern.slf4j.Slf4j;

@Ignore
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class MainTest {

	@Autowired
	ApplicationRepository appRepository;

	//	@Test
	public void addFn() {
		final Application app = appRepository.findByName("onesait");
		final Function fn = new Function();
		fn.setApplication(app);
		fn.setFnId("01FBRTXB6318N63T8ZJ000000X");
		fn.setName("myfunc");
		fn.setPathToYaml("mount-randomizer/");
		app.getFunctions().add(fn);
		appRepository.save(app);
	}

	@Test
	public void matchPattern() {
		final Pattern p = Pattern.compile("Deploying (.*) to app");
		final String toMatch = "Deploying myfunc to app: onesait";
		final Matcher matcher = p.matcher(toMatch);
		if(matcher.find()) {
			log.info("Matched String {}", matcher.group(1));
		}
	}

}
