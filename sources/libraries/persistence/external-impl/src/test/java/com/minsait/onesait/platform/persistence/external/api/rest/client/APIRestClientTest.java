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
package com.minsait.onesait.platform.persistence.external.api.rest.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Ignore("Select public API accesible from Internet")
public class APIRestClientTest {

	private String GET_API = "http://sfebb1.indra.es:6969/serviciobb/consultas/BBService/";
	private String GET_OPERATION = "peticion";
	private String GET_PARAM_NAME_1 = "paramsIn";
	private String GET_PARAM_VALUE_1 = "{\"idservice\":\"BB_DS_EE_MAPA_TORRES_MEX\",\"parametros\":{\"FILTROTORRE\":\"08-\",\"CLASE\":\"\",\"TIPEMP\":\"\",\"UMBRAL\":0}}";

	@Autowired
	private APIRestClient apiRestClient;

	@Before
	public void doBefore() throws Exception {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testInvokeGet() {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(GET_PARAM_NAME_1, GET_PARAM_VALUE_1);

		APIRestResponse resp = apiRestClient.invokeGet(GET_API, Optional.empty(), Optional.empty(),
				Optional.of(queryParams));

		System.out.println(resp.getBody());
		log.info(resp.getBody());
		log.info("" + resp.getResponse());

	}

}
