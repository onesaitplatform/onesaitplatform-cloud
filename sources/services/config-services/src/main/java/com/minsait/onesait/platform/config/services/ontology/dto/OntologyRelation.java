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
package com.minsait.onesait.platform.config.services.ontology.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class OntologyRelation implements Comparable<OntologyRelation>, Serializable {

	@Getter
	@Setter
	private String srcOntology;
	@Getter
	@Setter
	private String dstOntology;
	@Getter
	@Setter
	private String srcAttribute;
	@Getter
	@Setter
	private String dstAttribute;

	@Override
	public int compareTo(OntologyRelation o) {
		final int first = dstOntology.compareTo(o.getDstOntology());
		if (first == 0) {
			final int second = srcAttribute.compareTo(o.getSrcAttribute());
			if (second == 0)
				return dstAttribute.compareTo(o.getDstAttribute());
			else
				return second;
		} else
			return first;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OntologyRelation))
			return false;
		else {
			final OntologyRelation other = (OntologyRelation) o;
			return (srcOntology.equals(other.getSrcOntology()) && dstOntology.equals(other.getDstOntology())
					&& srcAttribute.equals(other.getSrcAttribute()) && dstAttribute.equals(other.getDstAttribute()));
		}
	}

	@Override
	public int hashCode() {

		return (srcOntology + dstOntology + srcAttribute + dstAttribute).hashCode();
	}
}
