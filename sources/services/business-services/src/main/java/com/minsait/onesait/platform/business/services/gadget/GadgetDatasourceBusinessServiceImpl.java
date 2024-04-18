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
package com.minsait.onesait.platform.business.services.gadget;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.gadget.GadgetDatasourceFieldDTO.FIELD_TYPE;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.GadgetDatasource;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.services.gadget.GadgetDatasourceService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;

@Slf4j
@Service
public class GadgetDatasourceBusinessServiceImpl implements GadgetDatasourceBusinessService {

	@Autowired
	private GadgetDatasourceService gadgetDatasourceService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private QueryToolService queryToolService;

	private static final String SIMPLE_MODE = "simpleMode";

	@Override
	public String getSampleGadgetDatasourceById(String datasourceId, String user)
			throws GadgetDatasourceBusinessServiceException, DBPersistenceException, OntologyDataUnauthorizedException,
			GenericOPException, JSQLParserException {
		return getSampleGadgetDatasourceById(datasourceId, user, 1, false);
	}

	@Override
	public String getSampleGadgetDatasourceById(String datasourceId, String user, int limit, boolean forFilter)
			throws GadgetDatasourceBusinessServiceException, DBPersistenceException, OntologyDataUnauthorizedException,
			GenericOPException, JSQLParserException {
		if (gadgetDatasourceService.getGadgetDatasourceById(datasourceId) == null) {
			throw new GadgetDatasourceBusinessServiceException(
					GadgetDatasourceBusinessServiceException.ErrorType.NOT_FOUND, "Not exists");
		} else if (gadgetDatasourceService.hasUserViewPermission(datasourceId, user)) {
			GadgetDatasource gd = this.gadgetDatasourceService.getGadgetDatasourceById(datasourceId);
			String query = gd.getQuery();
			Ontology ontologyEnt = gd.getOntology();
			String ontology;
			if (ontologyEnt != null) {
				ontology = ontologyEnt.getIdentification();
			} else {
				ontology = gadgetDatasourceService.getOntologyFromDatasource(query);
			}
			Ontology o = ontologyService.getOntologyByIdentification(ontology, user);
			String sampleQuery;
			if (o.getRtdbDatasource() != RtdbDatasource.NEBULA_GRAPH) {
				boolean isSimpleMode = isDatasourceSimpleMode(gd);
				if (forFilter && (isSimpleMode || isSimpleQuery(gd))) {
					sampleQuery = this.gadgetDatasourceService.getSampleQueryForFilterGadgetDatasourceById(datasourceId,
							ontology, user, limit);
				} else {
					sampleQuery = this.gadgetDatasourceService.getSampleQueryGadgetDatasourceById(datasourceId,
							ontology, user, limit);
				}
			} else {
				sampleQuery = query;
			}
			if (!o.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				return queryToolService.querySQLAsJson(user, ontology, sampleQuery, 0);
			} else {
				return queryToolService.queryNativeAsJson(user, ontology, query);
			}
		} else {
			throw new GadgetDatasourceBusinessServiceException(
					GadgetDatasourceBusinessServiceException.ErrorType.UNAUTHORIZED, "Not authorized");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<GadgetDatasourceFieldDTO> getFieldsGadgetDatasourceById(String datasourceId, String user,
			boolean forFilter)
			throws GadgetDatasourceBusinessServiceException, DBPersistenceException, OntologyDataUnauthorizedException,
			GenericOPException, JsonParseException, JsonMappingException, IOException, JSQLParserException {
		List<GadgetDatasourceFieldDTO> result = new LinkedList<>();
		String resultstr = getSampleGadgetDatasourceById(datasourceId, user, 1, forFilter);
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object>[] jsonMap = objectMapper.readValue(resultstr, Map[].class);
		if (jsonMap.length == 0) {
			throw new GadgetDatasourceBusinessServiceException(
					GadgetDatasourceBusinessServiceException.ErrorType.NOT_DATA, "Not data");
		} else {
			findKeys("", jsonMap[0], result);
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	private void findKeys(String root, Map<String, Object> treeMap, List<GadgetDatasourceFieldDTO> keys) {
		treeMap.forEach((key, value) -> {
			if (value instanceof LinkedHashMap) {
				Map<String, Object> map = (LinkedHashMap<String, Object>) value;
				findKeys(root + key + ".", map, keys);
			} else {
				GadgetDatasourceFieldDTO gadgetDatasourceFieldDTO = new GadgetDatasourceFieldDTO();
				gadgetDatasourceFieldDTO.setName(root + key);
				FIELD_TYPE fType;
				if (value instanceof String) {
					fType = FIELD_TYPE.STRING;
				} else if (value instanceof Integer) {
					fType = FIELD_TYPE.INTEGER;
				} else if (value instanceof Long) {
					fType = FIELD_TYPE.LONG;
				} else if (value instanceof Double) {
					fType = FIELD_TYPE.NUMBER;
				} else if (value instanceof List<?>) {
					fType = FIELD_TYPE.ARRAY;
				} else {
					fType = FIELD_TYPE.UNKNOWN;
				}
				gadgetDatasourceFieldDTO.setType(fType);
				keys.add(gadgetDatasourceFieldDTO);
			}
		});

	}

	private boolean isDatasourceSimpleMode(GadgetDatasource gd) {
		String config = gd.getConfig();
		if (config != null && config.trim().length() > 0) {
			try {
				JSONObject configJson = new JSONObject(config);
				boolean isSimpleMode = configJson.getBoolean(SIMPLE_MODE);
				return isSimpleMode;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	private boolean isSimpleQuery(GadgetDatasource gd) {
		String trimQuery = gd.getQuery().replaceAll("\\t|\\r|\\r\\n\\t|\\n|\\r\\t", " ");
		trimQuery = trimQuery.trim().replaceAll(" +", " ");
		trimQuery = trimQuery.replaceAll(",", ", ");
		String query = trimQuery.toLowerCase();
		return query.indexOf("inner join") == -1 && query.indexOf("select", 1) == -1
				&& query.indexOf("outer join") == -1 && query.indexOf("full join") == -1 && query.indexOf("join") == -1;
	}

}
