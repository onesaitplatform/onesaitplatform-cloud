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
package com.minsait.onesait.platform.config.services.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.transaction.Transactional;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.minsait.onesait.platform.config.components.AIConfiguration;
import com.minsait.onesait.platform.config.components.AllConfiguration;
import com.minsait.onesait.platform.config.components.BundleConfiguration;
import com.minsait.onesait.platform.config.components.CaasConfiguration;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.components.GoogleAnalyticsConfiguration;
import com.minsait.onesait.platform.config.components.JenkinsConfiguration;
import com.minsait.onesait.platform.config.components.MailConfiguration;
import com.minsait.onesait.platform.config.components.ModulesUrls;
import com.minsait.onesait.platform.config.components.OpenshiftConfiguration;
import com.minsait.onesait.platform.config.components.RancherConfiguration;
import com.minsait.onesait.platform.config.components.TwitterConfiguration;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Configuration.Type;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ConfigurationRepository;
import com.minsait.onesait.platform.config.services.exceptions.ConfigServiceException;
import com.minsait.onesait.platform.git.GitlabConfiguration;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private ConfigurationRepository configurationRepository;

	private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");

	private static final String DEFAULT = "default";

	@Override
	public List<Configuration> getAllConfigurations() {
		return configurationRepository.findAll();
	}

	@Override
	public List<Configuration> getAllConfigurations(User user) {
		if (user.isAdmin()) {
			return configurationRepository.findAll();
		} else {
			return configurationRepository.findByUser(user);
		}
	}

	@Override
	@Transactional
	public void deleteConfiguration(String id) {
		configurationRepository.deleteById(id);
	}

	@Override
	public List<Type> getAllConfigurationTypes(User user) {
		if (user.isAdmin()) {
			return Arrays.asList(Configuration.Type.values());
		} else {
			return Arrays.asList(Configuration.Type.EXTERNAL_CONFIG);
		}
	}

	@Override
	public Configuration getConfiguration(String id) {
		return configurationRepository.findById(id).orElse(null);
	}

	@Override
	public Configuration createConfiguration(Configuration configuration) {

		Configuration oldConfiguration = configurationRepository.findByTypeAndEnvironmentAndIdentification(
				configuration.getType(), configuration.getEnvironment(), configuration.getIdentification());
		if (oldConfiguration != null) {
			throw new ConfigServiceException(
					" A configuration definition already exists for that type and environment");
		}

		checkIfScriptIsCorrect(configuration);

		oldConfiguration = new Configuration();
		oldConfiguration.setUser(configuration.getUser());
		oldConfiguration.setType(configuration.getType());
		oldConfiguration.setYmlConfig(configuration.getYmlConfig());
		oldConfiguration.setDescription(configuration.getDescription());
		oldConfiguration.setIdentification(configuration.getIdentification());
		oldConfiguration.setEnvironment(configuration.getEnvironment());
		return configurationRepository.save(oldConfiguration);

	}

	@Override
	public void updateConfiguration(Configuration configuration) {
		configurationRepository.findById(configuration.getId()).ifPresent(oc -> {
			checkIfScriptIsCorrect(configuration);

			oc.setYmlConfig(configuration.getYmlConfig());
			oc.setType(configuration.getType());
			oc.setDescription(configuration.getDescription());
			oc.setIdentification(configuration.getIdentification());
			oc.setEnvironment(configuration.getEnvironment());
			configurationRepository.save(oc);
		});

	}

	@Override
	public TwitterConfiguration getTwitterConfiguration(String environment, String suffix) {
		try {
			final Configuration config = this.getConfiguration(Configuration.Type.TWITTER, environment, suffix);
			final AllConfiguration conf = getAllConfigurationFromDBConfig(config);
			if (conf == null) {
				return null;
			}
			return conf.getTwitter();
		} catch (final Exception e) {
			log.error("Error getting TwitterConfiguration", e);
			throw new ConfigServiceException("Error getting TwitterConfiguration", e);
		}
	}

	@Override
	public boolean existsConfiguration(Configuration configuration) {
		return configurationRepository.findById(configuration.getId()) != null;
	}

	@Override
	public Map fromYaml(String yaml) {
		final Yaml yamlParser = new Yaml();
		return (Map) yamlParser.load(yaml);
	}

	@Override
	public boolean isValidYaml(final String yml) {
		try {
			final Yaml yamlParser = new Yaml();
			yamlParser.load(yml);
			return true;
		} catch (final Exception e) {
			log.error("Error parsing file:" + e.getMessage());
			return false;
		}
	}

	@Override
	public List<Configuration> getConfigurations(Configuration.Type type) {
		return configurationRepository.findByType(type);
	}

	@Override
	public List<Configuration> getConfigurations(Configuration.Type type, User user) {
		return configurationRepository.findByTypeAndUser(type, user);
	}

	@Override
	public Configuration getConfiguration(Configuration.Type type, String environment, String suffix) {
		if (suffix == null) {
			return configurationRepository.findByTypeAndEnvironment(type, environment);
		} else {
			return configurationRepository.findByTypeAndEnvironmentAndIdentification(type, environment, suffix);
		}
	}

	@Override
	public Configuration getConfigurationByDescription(String description) {
		return configurationRepository.findByDescription(description);
	}

	@Override
	public Urls getEndpointsUrls(String environment) {
		final Configuration config = configurationRepository.findByTypeAndEnvironmentAndIdentification(
				Configuration.Type.ENDPOINT_MODULES, environment, "PlatformModules");
		final Constructor constructor = new Constructor(ModulesUrls.class);
		final Representer representer = new Representer();
		representer.getPropertyUtils().setSkipMissingProperties(true);
		final Yaml yamlUrls = new Yaml(constructor, representer);
		return yamlUrls.loadAs(config.getYmlConfig(), ModulesUrls.class).getOnesaitplatform().get("urls");

	}

	@Override
	public GitlabConfiguration getGitlabConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id).orElse(null);
		if (config == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(config).getGitlab();
	}

	@Override
	public GitlabConfiguration getGitlabConfiguration(String suffix, String environment) {
		final Configuration config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.GITLAB,
				environment, suffix);
		if (config == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(config).getGitlab();
	}

	@Override
	public GitlabConfiguration getDefautlGitlabConfiguration() {
		return getGitlabConfiguration("", DEFAULT);
	}

	@Override
	public RancherConfiguration getRancherConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id).orElse(null);
		if (config == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(config).getRancher();
	}

	@Override
	public OpenshiftConfiguration getOpenshiftConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id).orElse(null);
		if (config == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(config).getOpenshift();
	}

	@Override
	public CaasConfiguration getCaasConfiguration(String id) {
		final Configuration config = configurationRepository.findById(id).orElse(null);
		if (config == null) {
			return null;
		}
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);
		return yaml.loadAs(config.getYmlConfig(), CaasConfiguration.class);
	}

	@Override
	public Configuration getConfiguration(Type configurationType, String suffix) {
		return configurationRepository.findByTypeAndIdentificationIgnoreCase(configurationType, suffix);
	}

	@Override
	public MailConfiguration getMailConfiguration(String environment) {
		final Configuration configuration = configurationRepository.findByTypeAndEnvironment(Type.MAIL, environment);
		if (configuration == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(configuration).getMail();
	}

	@Override
	public GoogleAnalyticsConfiguration getGoogleAnalyticsConfiguration(String environment) {
		final Configuration configuration = configurationRepository.findByTypeAndEnvironment(Type.GOOGLE_ANALYTICS,
				environment);
		if (configuration == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(configuration).getGoogleanalytics();
	}

	@Override
	public RancherConfiguration getRancherConfiguration(String suffix, String environment) {
		final Configuration config = configurationRepository
				.findByTypeAndEnvironmentAndIdentification(Configuration.Type.RANCHER, environment, suffix);
		if (config == null) {
			return null;
		} else {
			return getAllConfigurationFromDBConfig(config).getRancher();
		}
	}

	@Override
	public RancherConfiguration getDefaultRancherConfiguration() {
		return getRancherConfiguration("", DEFAULT);
	}

	@Override
	public GlobalConfiguration getGlobalConfiguration(String environment) {
		final Configuration config = configurationRepository.findByTypeAndEnvironment(Type.OPEN_PLATFORM, environment);
		if (config == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(config).getOnesaitplatform();
	}

	@Override
	public JenkinsConfiguration getJenkinsConfiguration(String environment) {
		final Configuration config = configurationRepository.findByTypeAndEnvironmentAndIdentification(Type.JENKINS,
				environment, "jenkins");
		if (config == null) {
			return null;
		}
		return getAllConfigurationFromDBConfig(config).getJenkins();
	}

	@Override
	public JenkinsConfiguration getDefaultJenkinsConfiguration() {
		return getJenkinsConfiguration(DEFAULT);
	}

	private AllConfiguration getAllConfigurationFromDBConfig(Configuration config) {
		if (config == null) {
			return null;
		}
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);
		return yaml.loadAs(config.getYmlConfig(), AllConfiguration.class);
	}

	@Override
	public String getDefaultJenkinsXML(String suffix) {
		final Configuration configuration = this.getConfiguration(Type.JENKINS, DEFAULT, suffix);
		if (configuration == null) {
			return "";
		}
		return configuration.getYmlConfig();
	}

	@Override
	public Configuration getConfigurationByIdentification(String identification) {
		return configurationRepository.findByIdentification(identification);
	}

	private void checkIfScriptIsCorrect(Configuration configuration) {
		if (configuration.getType().equals(Type.DATACLASS)) {
			final Map<String, Object> dclassyml = (Map<String, Object>) fromYaml(configuration.getYmlConfig())
					.get("dataclass");
			final ArrayList<Map<String, Object>> rules = (ArrayList<Map<String, Object>>) dclassyml
					.get("dataclassrules");
			for (final Map<String, Object> rule : rules) {
				if (rule.get("ruletype").equals("property")) {
					final ArrayList<Map<String, Object>> changes = (ArrayList<Map<String, Object>>) rule.get("changes");
					if (changes != null) {
						checkScript(changes);
					} else {
						final ArrayList<Map<String, Object>> validations = (ArrayList<Map<String, Object>>) rule
								.get("validations");
						if (validations != null) {
							checkScript(validations);
						}

					}
				} else if (rule.get("ruletype").equals("entity")) {
					final ArrayList<Map<String, Object>> validations = (ArrayList<Map<String, Object>>) rule
							.get("validations");
					if (validations != null) {
						for (final Map<String, Object> validation : validations) {
							final Object scriptT = validation.get("script");
							if (scriptT != null) {
								final String[] scriptArray = scriptT.toString().split("\n", 2);
								final String scriptType = scriptArray[0];
								final String script = scriptArray[1];
								if ("groovy".equals(scriptType)) {
									final Binding binding = new Binding();
									binding.setVariable("rawdata", "{\"testScript\": \"test\"}");
									final GroovyShell shell = new GroovyShell(binding);
									shell.evaluate(script);
								} else if ("javascript".equals(scriptType)) {
									try {
										final String scriptPostprocessFunction = "function preprocess(rawdata){ "
												+ script + " }";
										final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
												scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
										scriptEngine.eval(new InputStreamReader(scriptInputStream));
										final Invocable inv = (Invocable) scriptEngine;
										inv.invokeFunction("preprocess", "{\"testScript\": \"test\"}");
									} catch (final NoSuchMethodException e) {
										log.error("Cannot eval preprocessing", e);
										throw new ConfigServiceException("There are errors in the "
												+ validation.get("name") + " validation script: " + e.getMessage());
									} catch (final ScriptException ex) {
										log.error("Cannot eval preprocessing", ex);
										throw new ConfigServiceException("There are errors in the "
												+ validation.get("name") + " validation script: " + ex.getMessage());
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void checkScript(ArrayList<Map<String, Object>> changes) {
		for (final Map<String, Object> change : changes) {
			final Object scriptT = change.get("script");
			if (scriptT != null && !scriptT.toString().contains("toDate(")) {
				final String[] scriptArray = scriptT.toString().split("\n", 2);
				final String scriptType = scriptArray[0];
				final String script = scriptArray[1];
				if ("groovy".equals(scriptType)) {
					try {
						final Binding binding = new Binding();
						binding.setVariable("value", "valueTest");
						final GroovyShell shell = new GroovyShell(binding);
						shell.evaluate(script);
					} catch (final CompilationFailedException e) {
						throw new ConfigServiceException(
								"There are errors in the " + change.get("name") + " change script: " + e.getMessage());
					}
				} else if ("javascript".equals(scriptType)) {
					try {
						final String scriptPostprocessFunction = "function preprocess(value){ " + script + " }";
						final ByteArrayInputStream scriptInputStream = new ByteArrayInputStream(
								scriptPostprocessFunction.getBytes(StandardCharsets.UTF_8));
						scriptEngine.eval(new InputStreamReader(scriptInputStream));
						final Invocable inv = (Invocable) scriptEngine;
						inv.invokeFunction("preprocess", "valueTest");
					} catch (final NoSuchMethodException e) {
						throw new ConfigServiceException(
								"There are errors in the " + change.get("name") + " change script: " + e.getMessage());
					} catch (final ScriptException ex) {
						throw new ConfigServiceException(
								"There are errors in the " + change.get("name") + " change script: " + ex.getMessage());
					}
				}
			}
		}
	}

	@Override
	public BundleConfiguration getBundleConfiguration() {
		final List<Configuration> configs = configurationRepository.findByType(Type.BUNDLE_GIT);
		if (!CollectionUtils.isEmpty(configs)) {
			final Constructor constructor = new Constructor(BundleConfiguration.class);
			final TypeDescription configDesc = new TypeDescription(BundleConfiguration.class);
			configDesc.putListPropertyType("gitConnections",
					com.minsait.onesait.platform.commons.git.GitlabConfiguration.class);
			constructor.addTypeDescription(configDesc);
			final Yaml yaml = new Yaml(constructor);
			return yaml.loadAs(configs.iterator().next().getYmlConfig(), BundleConfiguration.class);
		}
		return null;
	}
	
	@Override
	public AIConfiguration getAIConfiguration() {
		final List<Configuration> configs = configurationRepository.findByType(Type.AI);
		if (!CollectionUtils.isEmpty(configs)) {
			final Constructor constructor = new Constructor(AIConfiguration.class);
			final Yaml yaml = new Yaml(constructor);
			return yaml.loadAs(configs.iterator().next().getYmlConfig(), AIConfiguration.class);
		}
		return null;
	}

}
