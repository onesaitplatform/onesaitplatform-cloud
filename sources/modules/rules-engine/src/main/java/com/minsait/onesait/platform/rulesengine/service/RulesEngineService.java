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
package com.minsait.onesait.platform.rulesengine.service;

import java.util.List;
import java.util.concurrent.Future;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

public interface RulesEngineService {

	String executeRules(String ontology, String jsonInput, String user) throws GenericOPException;

	List<Future<String>> executeRulesAsync(String ontology, String jsonInput, String vertical, String tenant) throws GenericOPException;

	String executeRestRule(String ruleIdentification, String jsonInput) throws GenericOPException;

	boolean canUserExecuteRule(String ruleIdentification, String userId) throws GenericOPException;

}
