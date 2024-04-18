/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.ksql.relation;

import java.util.List;

import com.minsait.onesait.platform.commons.ksql.KsqlExecutionException;
import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.User;

public interface KsqlRelationService {

	public void createKsqlRelation(KsqlFlow ksqlFlow, KsqlResource ksqlResource) throws KsqlExecutionException;

	public void deleteKsqlRelation(KsqlRelation relation) throws KsqlExecutionException;

	public void updateKsqlRelation(KsqlFlow ksqlFlow, KsqlResource ksqlResource, String statement, String description)
			throws KsqlExecutionException;

	public List<String> getAllIdentifications();

	public List<KsqlRelation> getKsqlRelationsWithFlowIdDescriptionAndIdentification(User sessionUser, String id,
			String identification, String description);

	public List<KsqlRelation> getKsqlRelationsWithFlowId(User user, String flowId);

	public KsqlRelation getKsqlRelationWithId(String id);

}
