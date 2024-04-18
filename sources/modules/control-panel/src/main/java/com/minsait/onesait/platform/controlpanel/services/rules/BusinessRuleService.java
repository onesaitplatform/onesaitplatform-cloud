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
package com.minsait.onesait.platform.controlpanel.services.rules;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.controlpanel.controller.rules.DroolsRuleDTO;
import com.minsait.onesait.platform.controlpanel.controller.rules.RuleDTO;

public interface BusinessRuleService {

	void save(DroolsRuleDTO rule, String userId) throws GenericOPException, IOException;

	void save(RuleDTO rule, String userId) throws GenericOPException;

	void update(DroolsRuleDTO rule, String userId, String identification) throws GenericOPException, IOException;

	void update(RuleDTO rule, String userId, String identification) throws GenericOPException;

	void delete(String identification);

	String test(String identification, String input);

	void updateActive(String identification);

	void updateActive(String identification, boolean active);

	void updateDRL(String identification, String newDRL) throws GenericOPException;

	DroolsRuleDomain changeDomainState(String id);

	void changeDomainState(String userId, boolean active);

	void changeDomainStates(boolean active);

	DroolsRuleDomain createDomain(String user);

	void updateDecisionTable(String identification, MultipartFile decisionTable) throws GenericOPException, IOException;

	File createFolder(String path) throws GenericRuntimeOPException;

	File uploadFileToFolder(byte[] bytes, String path, String ruleName, String extension);

	boolean deleteDirectory(File directoryToBeDeleted);

}
