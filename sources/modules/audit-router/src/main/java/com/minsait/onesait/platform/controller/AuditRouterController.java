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
package com.minsait.onesait.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.repository.OntologyRepository;

@RestController
public class AuditRouterController {

	@Autowired
	private OntologyRepository ontologyRepository;

	@GetMapping("entity/{identification}/datasource")
	public ResponseEntity<String> getEntityDatasource(@PathVariable("identification") String identification){
		final Ontology o = ontologyRepository.findByIdentification(identification);
		if(o != null ) {
			return ResponseEntity.ok(o.getRtdbDatasource().name());
		}else {
			return ResponseEntity.notFound().build();
		}
	}
}
