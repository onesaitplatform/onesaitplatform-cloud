/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.config.services.configuration;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.components.TwitterConfiguration;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.model.Configuration;

@Category(IntegrationTest.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigurationServiceIntegrationTest {

	@Autowired
	ConfigurationService service;

	@Test
	@Transactional
	public void given_AValidConfigurationYAML_When_ItIsValidated_Then_ValuesCanBeCorrectlyObtained() {
		final String yaml = "" + "twitter:\n"
				+ "      accessToken: 74682827-D6cX2uurqpxy6yWlg6wioRl49f9Rtt2pEXUu6YNUy\n"
				+ "      accessTokenSecret: Cmd9XOX9N8xMRvlYUz3Wg49ZCGFnanMJvJPI9QMfTXix2\n"
				+ "      consumerKey: PWgCyepuon5U8X9HqfUtNpntq\n"
				+ "      consumerSecret: zo6rbSh6J470t7CCz4ZtXhHEFhpt36TMPKYolJgIiLOpEW9oc4\n";
		Assert.assertTrue(service.isValidYaml(yaml));
		final Map<?, ?> values = service.fromYaml(yaml);
		final Map<?, ?> value = (Map<?, ?>) values.get("twitter");
		Assert.assertEquals(value.get("accessToken"), "74682827-D6cX2uurqpxy6yWlg6wioRl49f9Rtt2pEXUu6YNUy");
	}

	@Test
	@Transactional
	public void given_OneConfiguration_When_TwitterPropertiesAreRequested_TheCorrectValuesAreObtained() {
		final Configuration config = service.getConfiguration(Configuration.Type.TWITTER, "default", "lmgracia");
		final Map<?, ?> values = service.fromYaml(config.getYmlConfig());
		final Map<?, ?> value = (Map<?, ?>) values.get("twitter");
		Assert.assertEquals(value.get("accessToken"), "74682827-D6cX2uurqpxy6yWlg6wioRl49f9Rtt2pEXUu6YNUy");
	}

	@Test
	@Transactional
	public void given_OneConfiguration_When_TwitterWholeConfigurationIsRequested_ItIsObtained() {
		final TwitterConfiguration config = service.getTwitterConfiguration("default", "lmgracia");
		Assert.assertEquals(config.getAccessToken(), "74682827-D6cX2uurqpxy6yWlg6wioRl49f9Rtt2pEXUu6YNUy");
	}

	@Test
	@Transactional
	public void endpointsConfiguration_fromYaml() throws IOException {
		final Urls urls = service.getEndpointsUrls("default");
		Assert.assertTrue(urls.getIotbroker().getBase().contains("iot-broker"));
	}

	@Test
	@Transactional
	public void givenDockerYml_compileWithMustache() throws IOException {
		final HashMap<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("name", "Mustache");
		scopes.put("feature", "Perfect!");

		final Writer writer = new OutputStreamWriter(System.out);
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader("{{name}}, Hola que tal soy colosal , {{feature}}"),
				"example");
		mustache.execute(writer, scopes);
		writer.flush();
	}
}
