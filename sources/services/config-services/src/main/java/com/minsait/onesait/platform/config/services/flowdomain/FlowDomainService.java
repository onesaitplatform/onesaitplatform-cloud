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
package com.minsait.onesait.platform.config.services.flowdomain;

import java.util.List;

import com.minsait.onesait.platform.config.model.Flow;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.model.User;

public interface FlowDomainService {

	public List<FlowDomain> getFlowDomainByUser(User user);

	public FlowDomain getFlowDomainByIdentification(String identification);

	public void deleteFlowDomainFlows(String domainIdentification, User user);

	public void deleteFlowdomain(String domainIdentification);

	public FlowDomain createFlowDomain(String identification, User user,  String... domainAttributes);

	public boolean flowDomainExists(FlowDomain domain);

	public void updateDomain(FlowDomain domain);

	public boolean domainExists(String domainIdentification);

	public FlowDomain getFlowDomainById(String id);

	public boolean hasUserManageAccess(String id, String userId);

	public boolean hasUserViewAccess(String id, String userId);

	public List<Flow> getFlows(FlowDomain domain);
}
