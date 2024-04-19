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
package com.minsait.onesait.platform.rtdbmaintainer.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplate;
import com.minsait.onesait.platform.rtdbmaintainer.audit.aop.RtdbMaintainerAuditable;
import com.minsait.onesait.platform.rtdbmaintainer.excepton.RtdbMaintainerException;
import com.minsait.onesait.platform.rtdbmaintainer.service.RtdbMaintenanceService;

@Service
public class RtdbMaintenanceServiceImpl implements RtdbMaintenanceService {
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private MongoDbTemplate mongoTemplate;
	@Value("${onesaitplatform.database.mongodb.database:onesaitplatform_rtdb}")
	private String rtdb;

	private static final String INRPOG = "inprog";
	private static final String OP_ID = "opid";
	private static final String OK = "ok";
	private static final String TMP_GEN_PREFIX = "tmp.gen_";

	@Override
	public List<Long> getCurrentOpsGT(int time) {
		final ArrayList<Long> ids = new ArrayList<>();
		final JsonNode ops = mapper.valueToTree(mongoTemplate.getCurrentOps(time));
		ops.get(INRPOG).forEach(n -> ids.add(n.get(OP_ID).asLong()));
		return ids;
	}

	@Override
	@RtdbMaintainerAuditable
	public void killOp(long opId) {
		final JsonNode result = mapper.valueToTree(mongoTemplate.killOp(opId));
		if (result.get(OK).asInt() != 1)
			throw new RtdbMaintainerException("Could not kill operation " + opId);
	}

	@Override
	public List<String> getTmpGenCollections() {
		return mongoTemplate.getCollectionNames(rtdb).stream().filter(s -> s.toLowerCase().startsWith(TMP_GEN_PREFIX))
				.collect(Collectors.toList());
	}

	@Override
	@RtdbMaintainerAuditable
	public void deleteTmpGenCollection(String collection) {
		mongoTemplate.dropCollection(rtdb, collection);
	}

}
