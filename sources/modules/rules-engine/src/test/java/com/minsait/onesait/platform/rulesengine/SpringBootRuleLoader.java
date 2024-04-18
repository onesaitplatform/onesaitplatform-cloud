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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRule.Type;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DroolsRuleDomainRepository;
import com.minsait.onesait.platform.config.repository.DroolsRuleRepository;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;
import com.minsait.onesait.platform.rulesengine.service.RulesEngineService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RulesEngineApplication.class)
@Category(IntegrationTest.class)
public class SpringBootRuleLoader {

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

	private OntologyJsonWrapper payload;

	private static final String RULE_ROLE_NAME = "SET_ROLE_RULE";
	private static final String USER_ADMIN = "administrator";
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

	@Before
	public void setUp() {
		final User admin = userRepository.findByUserId(USER_ADMIN);
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

	}

	@After
	public void destroy() {
		ruleRepository.deleteByIdentification(RULE_ROLE_NAME);
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

}
