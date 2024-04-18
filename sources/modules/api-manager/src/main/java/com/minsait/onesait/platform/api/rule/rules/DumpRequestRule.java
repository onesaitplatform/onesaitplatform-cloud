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
package com.minsait.onesait.platform.api.rule.rules;

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.util.RequestDumpUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Rule
@Slf4j
public class DumpRequestRule {

	@Autowired
	private RequestDumpUtil rUtil;

	@Priority
	public int getPriority() {
		return 1;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		HttpServletRequest request = facts.get(RuleManager.REQUEST);
		return (request != null);

	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		HttpServletRequest request = facts.get(RuleManager.REQUEST);

		StringBuilder sb = new StringBuilder();

		rUtil.dumpRequest(sb, request);
		rUtil.dumpRequestHeader(sb, request);
		rUtil.dumpRequestParameter(sb, request);
		rUtil.dumpRequestSessionAttribute(sb, request);

	}
}