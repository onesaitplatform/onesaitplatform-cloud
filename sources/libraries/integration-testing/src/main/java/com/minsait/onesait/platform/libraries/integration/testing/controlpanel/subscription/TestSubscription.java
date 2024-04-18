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
package com.minsait.onesait.platform.libraries.integration.testing.controlpanel.subscription;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.minsait.onesait.platform.libraries.integration.testing.IntegrationTestingApp;
import com.minsait.onesait.platform.libraries.integration.testing.controlpanel.login.SignInPage;

@SpringBootTest(classes = IntegrationTestingApp.class)

public class TestSubscription {

	private WebDriver driver;
	private Map<String, Object> vars;
	JavascriptExecutor js;

	// @Value("${host:http://localhost:18000/}")
	// private String host;
	private final String host = "http://localhost:18000/";
	@Value("${username:developer}")
	private String username;
	@Value("${password:Changed2019!}")
	private String pass_word;

	private final String IDENTIFIER = "subscriptionTEST100";

	@BeforeClass
	public void setUp() {
		driver = new ChromeDriver();
		js = (JavascriptExecutor) driver;
		vars = new HashMap<String, Object>();
		login();
	}

	@AfterClass
	public void tearDown() {
		driver.quit();
	}

	@Test
	public void op185subscriptionCreation() {
		final SubscriptionsListPage subscriptionListPage = new SubscriptionsListPage(driver);
		subscriptionListPage.openList(host);
		subscriptionListPage.pushCreate();
		final SubscriptionsFormPage subscriptionsFormPage = new SubscriptionsFormPage(driver);
		final String ontology = "HelsinkiPopulation";
		final String description = "Test Subscription HelsinkiPopulation";
		final String queryField = "$.Helsinki.year";
		final String projection = "$.Helsinki.year";
		final String queryOperator = "igual";
		subscriptionsFormPage.fillForm(IDENTIFIER, ontology, description, queryField, projection, queryOperator);
		subscriptionsFormPage.create();
		subscriptionListPage.waitUntilListVisible();
		subscriptionListPage.findElement(IDENTIFIER);
		assertTrue(subscriptionListPage.existElement(IDENTIFIER));

	}

	@Test
	public void op186subscriptionModification() {
		final SubscriptionsListPage subscriptionListPage = new SubscriptionsListPage(driver);
		subscriptionListPage.openList(host);
		subscriptionListPage.findElement(IDENTIFIER);
		assert (subscriptionListPage.existElement(IDENTIFIER));
		subscriptionListPage.pushEdit(IDENTIFIER);
		final SubscriptionsFormPage subscriptionsFormPage = new SubscriptionsFormPage(driver);
		final String projection = "$";
		subscriptionsFormPage.fillFormUpdate(projection);
		subscriptionsFormPage.update();
		subscriptionListPage.waitUntilListVisible();
		assertTrue(subscriptionListPage.existElement(IDENTIFIER));
	}

	@Test
	public void op187subscriptionDelete() {
		final SubscriptionsListPage subscriptionListPage = new SubscriptionsListPage(driver);
		subscriptionListPage.openList(host);
		subscriptionListPage.findElement(IDENTIFIER);
		assertTrue(subscriptionListPage.existElement(IDENTIFIER));
		subscriptionListPage.pushDelete(IDENTIFIER);
		subscriptionListPage.waitUntilValidationVisible();
		subscriptionListPage.pushDeleteConfirmation();
		subscriptionListPage.findElement(IDENTIFIER);
		assertFalse(subscriptionListPage.existElement(IDENTIFIER));
	}

	public void login() {
		final SignInPage signInPage = new SignInPage(driver);
		signInPage.loginValidUser(host);
		signInPage.loginValidUser(username, pass_word);
	}

}