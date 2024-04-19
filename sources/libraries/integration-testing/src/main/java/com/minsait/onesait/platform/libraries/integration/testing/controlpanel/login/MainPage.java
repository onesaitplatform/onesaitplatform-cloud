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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object encapsulates the Home Page
 */
public class MainPage {
	protected static WebDriver driver;

	// <h1>Hello userName</h1>
	private By messageBy = By.tagName("h1");

	public MainPage(WebDriver driver) {
		this.driver = driver;
	}

	public boolean testloadmain() {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		if (null != wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("headerImg")))) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Get message (h1 tag)
	 *
	 * @return String message text
	 */
	public String getMessageText() {
		return driver.findElement(messageBy).getText();
	}

	public MainPage manageProfile() {
		// Page encapsulation to manage profile functionality
		return new MainPage(driver);
	}
	/*
	 * More methods offering the services represented by Home Page of Logged User.
	 * These methods in turn might return more Page Objects for example click on
	 * Compose mail button could return ComposeMail class object
	 */
}