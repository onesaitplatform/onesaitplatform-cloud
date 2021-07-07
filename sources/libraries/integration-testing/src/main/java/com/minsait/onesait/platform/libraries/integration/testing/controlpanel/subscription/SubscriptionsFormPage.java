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
package com.minsait.onesait.platform.libraries.integration.testing.controlpanel.subscription;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object encapsulates the Sign-in page.
 */
public class SubscriptionsFormPage {
	protected static WebDriver driver;

	private By identificationBy = By.id("identification");
	private By ontologyBy = By.id("ontology");
	private By descriptiondBy = By.id("description");
	private By queryFieldBy = By.id("queryField");
	private By projectionBy = By.id("projection");
	private By queryOperatorBy = By.id("queryOperator");

	private By createBtnBy = By.id("createBtn");
	private By updateBtnBy = By.id("updateBtn");

	public SubscriptionsFormPage(WebDriver driver) {
		this.driver = driver;
	}

	public void create() {
		driver.findElement(createBtnBy).click();
	}

	public void update() {
		driver.findElement(updateBtnBy).click();
	}

	public void fillForm(String identification, String ontology, String description, String queryField,
			String projection, String queryOperator) {

		driver.findElement(identificationBy).click();
		driver.findElement(identificationBy).sendKeys(identification);
		driver.findElement(descriptiondBy).click();
		driver.findElement(descriptiondBy).sendKeys(description);
		driver.findElement(projectionBy).click();
		driver.findElement(projectionBy).sendKeys(projection);
		driver.findElement(queryFieldBy).click();
		driver.findElement(queryFieldBy).sendKeys(queryField);

		driver.findElement(ontologyBy).click();

		WebElement dropdownOntology = driver.findElement(ontologyBy);
		dropdownOntology.findElement(By.xpath("//option[. = '" + ontology + "']")).click();

		driver.findElement(ontologyBy).click();
		WebElement dropdownQueryOperator = driver.findElement(queryOperatorBy);
		dropdownQueryOperator.findElement(By.xpath("//*[@id=\"queryOperator\"]/option[@value='" + queryOperator + "']"))
				.click();

	}

	public void fillFormUpdate(String projection) {
		driver.findElement(projectionBy).click();
		driver.findElement(projectionBy).sendKeys(projection);

	}

}