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
package com.minsait.onesait.platform.rulesengine;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRule.Type;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DataModelRepository;
import com.minsait.onesait.platform.config.repository.DroolsRuleDomainRepository;
import com.minsait.onesait.platform.config.repository.DroolsRuleRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.rulesengine.drools.KieServicesManager;
import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;
import com.minsait.onesait.platform.rulesengine.service.RulesEngineService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = RulesEngineApplication.class)
@Category(IntegrationTest.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@Slf4j
public class RulesEngineIntegrationTest {
	@Autowired
	private DroolsRuleDomainRepository ruleDomainRepository;
	@Autowired
	private DroolsRuleRepository ruleRepository;
	@Autowired
	private RulesEngineService rulesEngineService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private KieServicesManager kieServices;
	@Autowired
	private DataModelRepository dataModelRepository;

	private OntologyJsonWrapper payload;

	private User admin;
	private static final String RULE_ROLE_NAME = "SET_ROLE_RULE";
	private static final String RULE_ONTOLOGY_NAME = "TEMPERATURE_ALARM";
	private static final String USER_ADMIN = "administrator";

	private static final String SENSOR_TAG_SCHEMA_FILE = "SensorTagSchema";
	private static final String SENSOR_TAG_RULE_FILE = "SensorTagRule";
	private static final String SENSOR_TAG = "SensorTag";

	private static final String SENSOR_ALARM = "SensorAlarm";
	private static final String SENSOR_ALARM_SCHEMA_FILE = "SensorAlarmSchema";

	private static final String EMPTY_BASE = "EmptyBase";

	private static final String RULE_ROLE = "package com.minsait.onesait.platform.rulesengine;\n"
			+ "import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;\n"
			+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper input;\n"
			+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper output;\n" + "\n"
			+ "dialect  \"mvel\"\n" + "\n" + "\n" + "\n" + "rule \"Assign role Manager\"\n" + "\n" + "    when\n"
			+ "        eval( input.getProperty(\"currentSalary\") < 1000000 && input.getProperty(\"experienceInYears\") > 10 )\n"
			+ "    then\n" + "        output.copyInputToOutput(input);\n"
			+ "        output.setProperty(\"role\", \"Manager\");\n" + "end\n" + "\n"
			+ "rule \"Assign role Consultant\"\n" + "\n" + "    when\n"
			+ "        eval( input.getProperty(\"currentSalary\") > 30000 && input.getProperty(\"currentSalary\") < 50000 && input.getProperty(\"experienceInYears\") < 8 )\n"
			+ "    then\n" + "        output.copyInputToOutput(input);\n"
			+ "        output.setProperty(\"role\", \"Consultant\");\n" + "end\n" + "\n"
			+ "rule \"Assign role Junior\"\n" + "\n" + "    when\n"
			+ "        eval( input.getProperty(\"currentSalary\") < 30000 && input.getProperty(\"experienceInYears\") < 3 )\n"
			+ "    then\n" + "        output.copyInputToOutput(input);\n"
			+ "        output.setProperty(\"role\", \"Junior\");\n" + "end";

	private static final String JSON_HIGH_TEMP = "{\"SensorTag\":{ \"temperature\":82.4,\"humidity\":28.6,\"id\":\"980FXG\"}}";
	private static final String JSON_LOW_TEMP = "{\"SensorTag\":{ \"temperature\":12.4,\"humidity\":28.6,\"id\":\"980FXG\"}}";

	@Before
	public void setUp() {
		admin = userRepository.findByUserId(USER_ADMIN);
		if (ruleDomainRepository.findByUser(admin) == null) {
			final DroolsRuleDomain domain = new DroolsRuleDomain();
			domain.setActive(true);
			domain.setIdentification("admin_domain");
			domain.setUser(admin);
			ruleDomainRepository.save(domain);
		}

		final DroolsRule restRule = new DroolsRule();
		restRule.setDRL(RULE_ROLE);
		restRule.setUser(admin);
		restRule.setIdentification(RULE_ROLE_NAME);
		restRule.setType(Type.REST);
		ruleRepository.save(restRule);

		payload = new OntologyJsonWrapper();

		createOntologies();

		final DroolsRule ontologyRule = new DroolsRule();
		ontologyRule.setDRL(loadFromResources(SENSOR_TAG_RULE_FILE));
		ontologyRule.setUser(admin);
		ontologyRule.setIdentification(RULE_ONTOLOGY_NAME);
		ontologyRule.setActive(true);
		ontologyRule.setType(Type.ONTOLOGY);
		ontologyRule.setTargetOntology(ontologyRepository.findByIdentification(SENSOR_ALARM));
		ontologyRule.setSourceOntology(ontologyRepository.findByIdentification(SENSOR_TAG));
		ruleRepository.save(ontologyRule);

		kieServices.initializeRuleEngineDomain(USER_ADMIN);
	}

	@After
	public void destroy() {
		ruleRepository.deleteByIdentification(RULE_ROLE_NAME);
		ruleRepository.deleteByIdentification(RULE_ONTOLOGY_NAME);
		ontologyRepository.deleteByIdentification(SENSOR_ALARM);
		ontologyRepository.deleteByIdentification(SENSOR_TAG);

	}

	@Test
	public void userCanExecuteItsOwnRule() throws GenericOPException {
		assertTrue(rulesEngineService.canUserExecuteRule(RULE_ROLE_NAME, USER_ADMIN));
	}

	@Test
	public void whenSalaryIsLessThan30000_ThenRoleIsJunior() throws GenericOPException {
		payload.setProperty("experienceInYears", 2);
		payload.setProperty("currentSalary", 23000);
		final OntologyJsonWrapper result = new OntologyJsonWrapper(
				rulesEngineService.executeRestRule(RULE_ROLE_NAME, payload.toJson()));
		assertTrue(result.getProperty("role").equals("Junior"));
	}

	@Test
	public void whenExperienceInYearsIs12_ThenRoleIsManager() throws GenericOPException {
		payload.setProperty("experienceInYears", 15);
		payload.setProperty("currentSalary", 123000);
		final OntologyJsonWrapper result = new OntologyJsonWrapper(
				rulesEngineService.executeRestRule(RULE_ROLE_NAME, payload.toJson()));
		assertTrue(result.getProperty("role").equals("Manager"));

	}

	@Test
	public void When_TemperatureIsHigherThan80_Then_AlarmIsTriggered() throws GenericOPException {
		final String output = rulesEngineService.executeRules(SENSOR_TAG, JSON_HIGH_TEMP, USER_ADMIN);
		final OntologyJsonWrapper outputWrapper = new OntologyJsonWrapper(output);
		assertTrue(outputWrapper.getProperty("type").equals("CRITICAL"));
	}

	@Test
	public void When_TemperatureIsHigherThan80_Then_AlarmIsTriggered_Async() throws GenericOPException {
		rulesEngineService.executeRulesAsync(SENSOR_TAG, JSON_HIGH_TEMP, null, null);

	}

	@Test
	public void When_TemperatureIsLessThan80_Then_AlarmIsNotTriggered() throws GenericOPException {
		final String output = rulesEngineService.executeRules(SENSOR_TAG, JSON_LOW_TEMP, USER_ADMIN);
		final OntologyJsonWrapper outputWrapper = new OntologyJsonWrapper(output);
		assertTrue(outputWrapper.getProperty("type") == null);
	}

	@Test
	public void When_TemperatureIsLessThan80_Then_AlarmIsNotTriggered_Async() throws GenericOPException {
		rulesEngineService.executeRulesAsync(SENSOR_TAG, JSON_LOW_TEMP, null, null);
	}

	@Transactional
	private void createOntologies() {
		final Ontology sensorTag = new Ontology();
		sensorTag.setIdentification(SENSOR_TAG);
		sensorTag.setUser(admin);
		sensorTag.setDataModel(dataModelRepository.findByIdentification(EMPTY_BASE).get(0));
		sensorTag.setJsonSchema(loadFromResources(SENSOR_TAG_SCHEMA_FILE));
		sensorTag.setActive(true);
		sensorTag.setMetainf("");
		sensorTag.setDescription("Sensor tag");
		sensorTag.setPublic(false);

		final Ontology sensorAlarm = new Ontology();
		sensorAlarm.setIdentification(SENSOR_ALARM);
		sensorAlarm.setUser(admin);
		sensorAlarm.setDataModel(dataModelRepository.findByIdentification(EMPTY_BASE).get(0));
		sensorAlarm.setJsonSchema(loadFromResources(SENSOR_ALARM_SCHEMA_FILE));
		sensorAlarm.setActive(true);
		sensorAlarm.setMetainf("");
		sensorAlarm.setPublic(false);
		sensorAlarm.setDescription("Sensor alarm");

		ontologyRepository.save(Arrays.asList(sensorTag, sensorAlarm));

	}

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					StandardCharsets.UTF_8);

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						StandardCharsets.UTF_8);
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}

}
