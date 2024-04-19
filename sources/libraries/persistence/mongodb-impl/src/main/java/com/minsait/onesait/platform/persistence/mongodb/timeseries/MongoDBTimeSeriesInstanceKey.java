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
package com.minsait.onesait.platform.persistence.mongodb.timeseries;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;

import lombok.Getter;
import lombok.Setter;

public class MongoDBTimeSeriesInstanceKey implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Getter
	@Setter
	private String ontology;
	@Getter
	@Setter
	private String signal;
	@Getter
	@Setter
	private List<String> tags;
	@Getter
	@Setter
	private String window;
	@Getter
	@Setter
	private Date timestamp;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MongoDBTimeSeriesInstanceKey o = (MongoDBTimeSeriesInstanceKey)obj;
		if (!ontology.equals(o.getOntology()))
			return false;
		if (!signal.equals(o.getSignal()))
			return false;
		if (window!=o.getWindow())
			return false;
		if (!timestamp.equals(o.getTimestamp()))
			return false;
		if (tags.size()!=o.getTags().size())
			return false;
		if (!tags.containsAll(o.getTags()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return java.util.Objects.hash(ontology,signal,tags,/*window,*/timestamp);
		/*final int prime = 31;
		int result = 1;
		result = prime * result + ((ontology == null) ? 0 : ontology.hashCode());
		result = prime * result + ((signal == null) ? 0 : signal.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((window == null) ? 0 : window.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;*/
	}
}
