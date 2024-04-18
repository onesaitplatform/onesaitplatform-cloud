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
package com.minsait.onesait.platform.controlpanel.services.project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxDumper;
import com.github.odiszapc.nginxparser.NgxEntry;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NginxServiceImpl implements NginxService {

	@Autowired
	private ConfigurationService configurationService;
	@Value("${onesaitplatform.docker.rancher.server_name:s4citiespro.westeurope.cloudapp.azure.com}")
	private String serverName;
	@Value("${onesaitplatform.docker.tmp: /tmp/}")
	private String tmpPath;
	public static final String CONF_FILE = "nginx.conf";
	@Value("${onesaitplatform.docker.mandatory-services:controlpanelservice,monitoringuiservice,configdb,configinit,quasar,realtimedb,schedulerdb, loadbalancerservice}")
	private String[] mandatoryServices;
	private static final Map<String, String> serviceToRouteMap;
	static {
		final Map<String, String> aMap = new HashMap<>();
		aMap.put("controlpanelservice", "controlpanel");
		aMap.put("apimanagerservice", "api-manager");
		aMap.put("dashboardengineservice", "dashboardengine");
		aMap.put("iotbrokerservice", "iotbroker");
		aMap.put("digitaltwinbrokerservice", "digitaltwinbroker");
		aMap.put("flowengineservice", "nodered");
		aMap.put("monitoringuiservice", "monitoring");
		aMap.put("zeppelin", "notebooks");
		serviceToRouteMap = Collections.unmodifiableMap(aMap);
	}

	@Override
	public String generateNginxConfFile(List<String> services) throws IOException {

		services.addAll(Arrays.asList(mandatoryServices));

		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put("SERVER_NAME", serverName);
		final Writer writer = new StringWriter();
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Configuration nginxConf = configurationService.getConfiguration(Configuration.Type.NGINX, "Nginx");
		final Mustache mustache = mf.compile(new StringReader(nginxConf.getYmlConfig()), "Nginx");
		mustache.execute(writer, scopes);

		final NgxConfig conf = NgxConfig.read(IOUtils.toInputStream(writer.toString()));

		final List<NgxEntry> locations = conf.findAll(NgxConfig.BLOCK, "http", "server", "location");
		locations.forEach(l -> {
			final List<String> filtered = services.stream()
					.filter(s -> l.toString().startsWith(
							"location /".concat(serviceToRouteMap.get(s) == null ? s : serviceToRouteMap.get(s))))
					.collect(Collectors.toList());

			if (filtered.isEmpty())
				conf.remove(l);
			if (l.toString().startsWith("location /controlpanel/notebooks") && services.contains("controlpanelservice")
					&& !services.contains("zeppelin"))
				conf.remove(l);
		});
		final NgxDumper dumper = new NgxDumper(conf);
		createConfFile(dumper.dump());
		return dumper.dump();
	}

	private void createConfFile(String nginxDump) throws IOException {
		final File file = new File(tmpPath + CONF_FILE);
		if (file.exists() && file.isFile()) {
			boolean deleted = file.delete();
			if (log.isDebugEnabled()) {
				log.debug("deleted:{}",deleted);
			}
		}
		boolean newFile = file.createNewFile();
		if (log.isDebugEnabled()) {
			log.debug("created:{}",newFile);
		}

		try (final FileWriter writer = new FileWriter(tmpPath + CONF_FILE)) {
			writer.write(nginxDump);
			writer.flush();
		} catch (Exception e) {
			log.error("Could not open FileWriter: ", e);
		}
	}

	@Override
	public String getAbsolutePathToConf() {
		return tmpPath + CONF_FILE;
	}

}
