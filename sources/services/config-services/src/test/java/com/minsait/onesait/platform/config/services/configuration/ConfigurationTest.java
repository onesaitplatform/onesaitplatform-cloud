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
package com.minsait.onesait.platform.config.services.configuration;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxEntry;
import com.minsait.onesait.platform.config.components.AllConfiguration;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;;

public class ConfigurationTest {

	@Test
	public void givenDockerYml_compileWithMustache() throws IOException {
		final HashMap<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("DOMAIN_NAME", "S4cities_Domain");
		scopes.put("WORKER2DEPLOY", "master");

		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(TestResources.dockerYMLSample), "DockerYML");
		mustache.execute(writer, scopes);
		System.out.println(writer.toString());
		org.junit.Assert.assertTrue(writer.toString().contains((String) scopes.get("DOMAIN_NAME")));
		writer.flush();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void givenDockerYml_CompileWithMustache_AndDeleteOneService() throws IOException {
		final HashMap<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("DOMAIN_NAME", "S4cities_Domain");
		scopes.put("WORKER2DEPLOY", "master");

		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(TestResources.dockerYMLSample), "DockerYML");
		mustache.execute(writer, scopes);

		final Yaml yaml = new Yaml();
		final Map<String, Map> ymlMap = (Map<String, Map>) yaml.load(writer.toString());

		final String serviceToDelete = "zookeeper";

		((Map<String, Map>) ymlMap.get("services")).keySet().removeIf(s -> s.equals(serviceToDelete));

		final String newDockerYml = yaml.dump(ymlMap);

		org.junit.Assert.assertTrue(newDockerYml.contains((String) scopes.get("DOMAIN_NAME")));
		org.junit.Assert.assertFalse(newDockerYml.contains(serviceToDelete));
		writer.flush();

	}

	@Test
	public void parseNginxConfFile_andRemoveControlpanelFromConf() throws IOException {
		final HashMap<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("SERVER_NAME", "S4cities_Domain");
		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(TestResources.nginxConf), "Nginx");
		mustache.execute(writer, scopes);
		final List<String> services = new ArrayList<>();
		services.add("controlpanelservice");
		final Map<String, String> myMap = new HashMap<>();
		myMap.put("controlpanelservice", "controlpanel");
		final NgxConfig conf = NgxConfig.read(IOUtils.toInputStream(writer.toString()));

		final List<NgxEntry> locations = conf.findAll(NgxConfig.BLOCK, "http", "server", "location");
		// locations.forEach(l -> {
		// if (l.toString().startsWith("location /controlpanel"))
		// conf.remove(l);
		// });
		locations.forEach(l -> {
			final List<String> filtered = services.stream()
					.filter(s -> l.toString().startsWith("location /".concat(myMap.get(s) == null ? s : myMap.get(s))))
					.collect(Collectors.toList());

			if (!filtered.isEmpty())
				conf.remove(l);
		});
		final NgxDumper dumper = new NgxDumper(conf);
		System.out.println(dumper.dump());
		org.junit.Assert
				.assertTrue(conf.findAll(NgxConfig.BLOCK, "http", "server", "location /controlpanel ").isEmpty());
	}

	@Test
	public void parseGlobalConfiguration() {
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);
		final AllConfiguration allConfig = yaml.loadAs(TestResources.globalConfig, AllConfiguration.class);
		final GlobalConfiguration globalConfig = allConfig.getOnesaitplatform();
		final Object expiration = globalConfig.getEnv().getIotbroker().get("session-expiration");
		Assert.assertTrue(expiration instanceof Long);
		final long millis = ((Long) expiration).longValue();
		Assert.assertEquals(600000l, millis);

	}
}
