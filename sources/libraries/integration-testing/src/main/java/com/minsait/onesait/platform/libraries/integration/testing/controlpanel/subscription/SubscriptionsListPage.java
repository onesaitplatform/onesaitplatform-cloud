/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object encapsulates the Sign-in page.
 */
public class SubscriptionsListPage {
	protected static WebDriver driver;

	private By createBy = By.cssSelector(".btn-primary");

	private By identificationColumn = By.cssSelector(".sorting_1");

	private By deleteConfirmation = By.cssSelector(".jconfirm-buttons > .btn-primary");

	private By filterBy = By.cssSelector("#subscriptions_filter .form-control");

	public SubscriptionsListPage(WebDriver driver) {
		this.driver = driver;
	}

	public void openList(String host) {
		driver.get(host + "controlpanel/subscriptions/list");
	}

	public void pushCreate() {
		driver.findElement(createBy).click();
	}

	public void waitUntilListVisible() {
		WebDriverWait wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.elementToBeClickable(filterBy));
	}

	public void waitUntilValidationVisible() {
		WebDriverWait wait = new WebDriverWait(driver, 20);
		wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmation));
	}

	public void pushEdit(String identifier) {

		List<WebElement> elements = driver.findElements(identificationColumn);
		if (elements.size() > 0) {
			int position = 0;
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				WebElement webElement = (WebElement) iterator.next();
				position++;
				if (webElement.getText().equals(identifier)) {
					break;
				}
			}
			driver.findElement(By.xpath("//*[@id=\"subscriptions\"]/tbody/tr[" + position + "]/td[7]/div/span[2]"))
					.click();
		}
	}

	public void pushDelete(String identifier) {

		List<WebElement> elements = driver.findElements(identificationColumn);
		if (elements.size() > 0) {
			int position = 0;
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				WebElement webElement = (WebElement) iterator.next();
				position++;
				if (webElement.getText().equals(identifier)) {
					break;
				}
			}
			driver.findElement(By.xpath("//*[@id=\"subscriptions\"]/tbody/tr[" + position + "]/td[7]/div/span[3]"))
					.click();
		}
	}

	public void pushDeleteConfirmation() {
		driver.findElement(deleteConfirmation).click();
	}

	public void findElement(String identifier) {
		driver.findElement(filterBy).click();
		driver.findElement(filterBy).sendKeys(identifier);
		driver.findElement(filterBy).sendKeys(Keys.ENTER);
	}

	public boolean existElement(String identifier) {

		List<WebElement> elements = driver.findElements(identificationColumn);

		if (elements.size() > 0) {
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				WebElement webElement = (WebElement) iterator.next();
				if (webElement.getText().equals(identifier)) {
					return true;
				}
			}

		}
		return false;
	}

}