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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.rulesengine.drools.KieServicesManagerImpl;
import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;

@Ignore
public class TestSimpleRule {
	private static final String RULE_DRL = "package com.minsait.onesait.platform.rulesengine;\n"
			+ "import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;\n"
			+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper ontology;\n" + "\n"
			+ "dialect  \"mvel\"\n" + "\n" + "rule \"process ontology temperature\"\n" + "    when\n"
			+ "        eval( ontology.getJsonProperty(\"temperature\") > 30)\n" + "    then\n"
			+ "        ontology.setJsonProperty(\"event\", \"Turn on the air\");\n" + "end";

	private static final String RULE_ROLE = "package com.minsait.onesait.platform.rulesengine.roles;\n"
			+ "import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;\n"
			+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper ontology;\n" + "\n"
			+ "dialect  \"mvel\"\n" + "\n" + "rule \"Assign role\"\n" + "    when\n"
			+ "        eval( ontology.getJsonProperty(\"currentSalary\") < 1000000 && ontology.getJsonProperty(\"experienceInYears\") > 10 )\n"
			+ "    then\n" + "        ontology.setJsonProperty(\"role\", \"Manager\");\n" + "end";
	private static final String RULE_ROLE_2 = "package com.minsait.onesait.platform.rulesengine.roles;\n"
			+ "import com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper;\n"
			+ "global com.minsait.onesait.platform.rulesengine.model.OntologyJsonWrapper ontology;\n" + "\n"
			+ "dialect  \"mvel\"\n" + "\n" + "rule \"Assign role 2\"\n" + "    when\n"
			+ "        eval( ontology.getJsonProperty(\"currentSalary\") < 1000000 && ontology.getJsonProperty(\"experienceInYears\") > 10 )\n"
			+ "    then\n" + "        ontology.setJsonProperty(\"role2\", \"Manager\");\n" + "end";

	private final OntologyJsonWrapper ontologyTemp = new OntologyJsonWrapper();
	private final OntologyJsonWrapper ontologyApplicant = new OntologyJsonWrapper();

	@Before
	public void setUp() {
		ontologyTemp.setProperty("temperature", 39);
		ontologyTemp.setProperty("humidity", 19);

		ontologyApplicant.setProperty("name", "Luis");
		ontologyApplicant.setProperty("currentSalary", "85000");
		ontologyApplicant.setProperty("experienceInYears", "12");

	}

	@Test
	public void loadRuleThenFire() throws GenericOPException {
		final KieServicesManagerImpl loader = new KieServicesManagerImpl();
		final String user = "admin";
		loader.initializeRuleEngineDomain(user);
		loader.addRule(user, RULE_ROLE, "rule_role_1");
		final KieSession session = loader.getKieSession(user);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));
	}

	@Test
	public void loadMultipleRulesAndFire() throws GenericOPException {
		// first rule for user admin
		final KieServicesManagerImpl loader = new KieServicesManagerImpl();
		final String admin = "admin";
		loader.initializeRuleEngineDomain(admin);
		loader.addRule(admin, RULE_ROLE, "rule_role_1");
		KieSession session = loader.getKieSession(admin);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));
		ontologyApplicant.removeProperty("role");

		// second rule for developer
		final String developer = "developer";
		loader.initializeRuleEngineDomain(developer);
		loader.addRule(developer, RULE_DRL, "rule_temp_1");
		session = loader.getKieSession(developer);
		session.setGlobal("ontology", ontologyTemp);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyTemp.getProperty("event")).equals("Turn on the air"));
		ontologyTemp.removeProperty("event");

		// execute rule 2 again
		session = loader.getKieSession(developer);
		session.setGlobal("ontology", ontologyTemp);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyTemp.getProperty("event")).equals("Turn on the air"));
		ontologyTemp.removeProperty("event");

		// execute rule 1 again
		session = loader.getKieSession(admin);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));
		ontologyApplicant.removeProperty("role");
		// execute rule 1 with other user
		session = loader.getKieSession(developer);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 0);
		session.dispose();
		ontologyApplicant.removeProperty("role");

	}

	@Test
	public void whenTryingToFireRulesFromAnotherUser_then0RulesAreFired() throws GenericOPException {
		final KieServicesManagerImpl loader = new KieServicesManagerImpl();
		final String admin = "admin";
		loader.initializeRuleEngineDomain(admin);
		loader.addRule(admin, RULE_ROLE, "rule_role_1");
		final String developer = "developer";
		loader.initializeRuleEngineDomain(developer);
		loader.addRule(developer, RULE_DRL, "rule_temp_1");

		KieSession session = loader.getKieSession(developer);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 0);
		session.dispose();

		session = loader.getKieSession(admin);
		session.setGlobal("ontology", ontologyTemp);
		assertTrue(session.fireAllRules() == 0);
		session.dispose();

	}

	@Test
	public void loadTheSameRuleInDifferentJARs() throws GenericOPException {
		final KieServicesManagerImpl loader = new KieServicesManagerImpl();
		final String admin = "admin";
		loader.initializeRuleEngineDomain(admin);
		loader.addRule(admin, RULE_ROLE, "rule_role_1");
		loader.addRule(admin, RULE_ROLE_2, "rule_role_2");
		final String developer = "developer";
		loader.initializeRuleEngineDomain(developer);
		loader.addRule(developer, RULE_ROLE, "rule_role_1");

		KieSession session = loader.getKieSession(developer);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));
		ontologyApplicant.removeProperty("role");

		session = loader.getKieSession(admin);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 2);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));
		ontologyApplicant.removeProperty("role");

	}

	@Test
	public void loadTwoRules_thenFire_thenDeleteOneRule_andOnlyOneGetsFired() throws GenericOPException {
		final KieServicesManagerImpl loader = new KieServicesManagerImpl();
		final String admin = "admin";
		loader.initializeRuleEngineDomain(admin);
		loader.addRule(admin, RULE_ROLE, "rule_role_1");
		loader.addRule(admin, RULE_ROLE_2, "rule_role_2");

		KieSession session = loader.getKieSession(admin);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 2);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));
		ontologyApplicant.removeProperty("role");
		ontologyApplicant.removeProperty("role2");

		// delete rule 2
		loader.removeRule(admin, "rule_role_2");
		session = loader.getKieSession(admin);
		session.setGlobal("ontology", ontologyApplicant);
		assertTrue(session.fireAllRules() == 1);
		session.dispose();
		assertTrue(((String) ontologyApplicant.getProperty("role")).equals("Manager"));

	}
}
