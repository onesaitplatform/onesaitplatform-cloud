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
package com.minsait.onesait.platform.api.rest.swagger;

import java.util.Map;

import io.swagger.models.AbstractModel;
import io.swagger.models.properties.Property;

public class StringModel extends AbstractModel {
	private Map<String, Property> properties;
	private String type;
	private String description;
	private Property items;
	private Object example;
	private Integer minItems;
	private Integer maxItems;

	public StringModel() {
		this.type = "";
	}

	public StringModel description(String description) {
		this.setDescription(description);
		return this;
	}

	public StringModel items(Property items) {
		this.setItems(items);
		return this;
	}

	public StringModel minItems(int minItems) {
		this.setMinItems(minItems);
		return this;
	}

	public StringModel maxItems(int maxItems) {
		this.setMaxItems(maxItems);
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Property getItems() {
		return items;
	}

	public void setItems(Property items) {
		this.items = items;
	}

	public Map<String, Property> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Property> properties) {
		this.properties = properties;
	}

	public Object getExample() {
		return example;
	}

	public void setExample(Object example) {
		this.example = example;
	}

	public Integer getMinItems() {
		return minItems;
	}

	public void setMinItems(Integer minItems) {
		this.minItems = minItems;
	}

	public Integer getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(Integer maxItems) {
		this.maxItems = maxItems;
	}

}