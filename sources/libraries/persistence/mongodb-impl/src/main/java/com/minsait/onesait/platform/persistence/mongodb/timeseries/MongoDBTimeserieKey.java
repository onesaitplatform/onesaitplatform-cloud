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
package com.minsait.onesait.platform.persistence.mongodb.timeseries;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeExclude;

import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.WindowType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class MongoDBTimeserieKey {
	@Getter
	@Setter
	private Date timestamp;
	@Getter
	@Setter
	private String propertyName;
	@Getter
	@Setter
	private Set<String> tags;
	@Getter
	@Setter
	private WindowType windowType;
	@Getter
	@Setter
	private Integer windowFrecuency;
	@Getter
	@Setter
	private FrecuencyUnit windowFrecuencyUnit;
	@Getter
	@Setter
	private Optional<String> rootElement;
	
	// Other needed objects but already in the key, just for efficiency
	@EqualsExclude
	@HashCodeExclude
	@Getter
	@Setter
	private Map<OntologyTimeSeriesProperty, Object> mtags;

}
