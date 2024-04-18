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
package com.minsait.onesait.platform.persistence.external.generator.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.minsait.onesait.platform.config.repository.OntologyVirtualRepository;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyRelation;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataService;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.persistence.external.generator.model.common.ExpandReplacement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.external.virtual.parser.JSONResultsetExtractorJSON;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;

@Slf4j
public class ExpandFnHelper {

	private final OntologyDataService ontologyDataService;
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final String parentOntology;
	private final String expandStatement;
	private final String expandReplacedStatement;
	private final ExpandReplacement expandReplacement;
	private final List<String> excludeParse;
	private Set<OntologyRelation> relations;
	private static final String QUERY_CHILD_ENTITY = "SELECT c.* FROM %s AS c WHERE c.%s IN(%s)";

	public ExpandFnHelper(String parentOntology, String expandStatement, String expandReplacedStatement,
			NamedParameterJdbcTemplate jdbcTemplate, List<String> excludeParse, ExpandReplacement expandReplacement) {
		this.parentOntology = parentOntology;
		this.expandStatement = expandStatement;
		this.expandReplacedStatement = expandReplacedStatement;
		this.jdbcTemplate = jdbcTemplate;
		this.excludeParse = excludeParse;
		this.expandReplacement = expandReplacement;
		this.ontologyDataService = BeanUtil.getBean(OntologyDataService.class);
	}

	public List<JSONObject> query(PreparedStatement ps) throws JSQLParserException {
		log.info("Invoke SGDB with jdbcTemplate");
		final List<JSONObject> mainObjs = jdbcTemplate.query(ps.getStatement(), ps.getParams(),
				new JSONResultsetExtractorJSON(ps.getStatement(), expandReplacedStatement, true));
		log.info("Receive result from SGDB with jdbcTemplate");
		runQueryChild(parentOntology, mainObjs, getRelations(parentOntology));
		return mainObjs;
	}

	private void runQueryChild(String ontology, List<JSONObject> mainObjs, Set<OntologyRelation> relations)
			throws JSQLParserException {
		for (final OntologyRelation relation : relations) {
			final String childQuery = generateQueryForChild(relation.getDstOntology(), relation.getDstAttribute(),
					mainObjs.stream().map(jo -> jo.get(relation.getSrcAttribute())).toList());
			if (childQuery != null) {
				final List<JSONObject> childObjs = jdbcTemplate.query(childQuery,
						new JSONResultsetExtractorJSON(childQuery, childQuery, true));
				final Set<OntologyRelation> relationsChild = getRelations(relation.getDstOntology());
				if (!relationsChild.isEmpty()) {
					// ANTES DE SEGUIR INVOCO CHILDREN
					runQueryChild(relation.getDstOntology(), childObjs, relationsChild);
				}
				childObjs.forEach(co -> {
					mainObjs.forEach(mo -> {
						final Object idMain = mo.get(relation.getSrcAttribute());
						final Object fkChild = co.get(relation.getDstAttribute());
						// TO-DO if non strings
						if ((idMain instanceof String && idMain.equals(fkChild)) || (idMain == fkChild)) {
							switch (relation.getRelationType()) {
							case ONE_TO_MANY:
							case MANY_TO_MANY:
								if (!mo.has(relation.getDstOntology())) {
									mo.put(relation.getDstOntology(), new JSONArray());
								}
								((JSONArray) mo.get(relation.getDstOntology())).put(co);
								break;
							case ONE_TO_ONE:
							case MANY_TO_ONE:
								mo.put(relation.getDstOntology(), co);
								break;
							default:
								throw new IllegalArgumentException("Unexpected value: " + relation.getRelationType());
							}
						}
					});
				});
			}
		}
	}

	private String generateQueryForChild(String ontology, String fkColumn, List<Object> ids)
			throws JSQLParserException {
		// Conversor IDS a String
		if (expandReplacement.isHasColumnsToExpand() && !expandReplacement.getColumnsToExpand().contains(ontology)) {
			return null;
		}
		if (!ids.isEmpty()) {
			final List<String> idsToFormat = new ArrayList<>();
			if (ids.get(0) instanceof String) {
				ids.forEach(o -> idsToFormat.add("'" + o + "'"));
			} else {
				ids.forEach(o -> idsToFormat.add(String.valueOf(o)));
			}
			return SQLTableReplacer.replaceTableNameInSelect(
					String.format(QUERY_CHILD_ENTITY, ontology, fkColumn, String.join(",", idsToFormat)),
					BeanUtil.getBean(OntologyVirtualRepository.class), excludeParse);
		}
		return null;

	}

	private Set<OntologyRelation> getRelations(String ontology) {
		try {
			return ontologyDataService.getOntologyReferences(ontology);
		} catch (final Exception e) {
			return new HashSet<>();
		}
	}

}
