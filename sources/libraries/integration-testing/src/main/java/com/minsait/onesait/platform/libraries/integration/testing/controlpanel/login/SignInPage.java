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
package com.minsait.onesait.platform.libraries.integration.testing.controlpanel.login;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

/**
 * Page Object encapsulates the Sign-in page.
 */
public class SignInPage {
	protected static WebDriver driver;

	// <input name="user_name" type="text" value="">
	private By usernameBy = By.id("username");
	// <input name="password" type="password" value="">
	private By passwordBy = By.id("password");

	public SignInPage(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * Login as valid user
	 *
	 * @param userName
	 * @param password
	 * @return HomePage object
	 */
	public MainPage loginValidUser(String userName, String password) {
		driver.findElement(usernameBy).sendKeys(userName);
		driver.findElement(passwordBy).sendKeys(password);
		driver.findElement(passwordBy).sendKeys(Keys.ENTER);
		return new MainPage(driver);
	}

	public void loginValidUser(String host) {
		driver.get(host + "controlpanel/login");
	}

}