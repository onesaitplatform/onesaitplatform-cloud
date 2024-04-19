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
package com.minsait.onesait.platform.config.services.ksql.flow;

import java.util.List;

import com.minsait.onesait.platform.commons.ksql.KsqlExecutionException;
import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.User;

public interface KsqlFlowService {

	public void createKsqlFlow(KsqlFlow ksqlFlow);

	public void deleteKsqlFlow(String id) throws KsqlExecutionException;

	public List<KsqlFlow> getKsqlFlowsWithDescriptionAndIdentification(User ksqlFlowOwneruser, String identification,
			String description);

	public List<String> getAllIdentifications();

	public boolean identificationIsAvailable(User sessionUser, String identification);

	public KsqlFlow getKsqlFlowWithId(String id);

	public void updateKsqlFlow(String id, KsqlFlow ksqlFlow, String userId);

}
