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
package com.minsait.onesait.platform.libraries.integration.testing.controlpanel.login;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.minsait.onesait.platform.libraries.integration.testing.IntegrationTestingApp;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = IntegrationTestingApp.class)
@Slf4j
public class TestLogin extends AbstractTestNGSpringContextTests {

	private WebDriver driver;
	private Map<String, Object> vars;
	JavascriptExecutor js;

	// private String host = "https://development.onesaitplatform.com/";
	// @Value("${host:http://localhost:18000/}")
	private final String host = "http://localhost:18000/";
	@Value("${username:developer}")
	private String username;
	@Value("${password:Changed2019!}")
	private String pass_word;

	@BeforeClass
	public void setUp() {
		driver = new ChromeDriver();
		js = (JavascriptExecutor) driver;
		vars = new HashMap<String, Object>();
	}

	@AfterClass
	public void tearDown() {
		driver.quit();
	}

	@org.testng.annotations.Test
	public void op144testLogin() {
		final SignInPage signInPage = new SignInPage(driver);
		signInPage.loginValidUser(host);
		final MainPage homePage = signInPage.loginValidUser(username, pass_word);
		assert (homePage.testloadmain());
	}

}