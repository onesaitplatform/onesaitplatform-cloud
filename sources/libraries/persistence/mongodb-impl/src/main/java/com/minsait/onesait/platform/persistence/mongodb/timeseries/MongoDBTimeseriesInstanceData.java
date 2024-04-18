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

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;

import lombok.Getter;
import lombok.Setter;

public class MongoDBTimeseriesInstanceData {
	@Getter
	@Setter
	private JSONObject jsonInstance;
	@Getter
	@Setter
	private String instance;
	@Getter
	@Setter
	private Entry<OntologyTimeSeriesProperty, Object> field;
	@Getter
	@Setter
	private Calendar realTimestamp;
	@Getter
	@Setter
	private Map<OntologyTimeSeriesProperty, Object> informativeFields;
	
}
