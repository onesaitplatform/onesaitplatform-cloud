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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.KsqlRelation;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.KsqlResource.KsqlResourceType;
import com.minsait.onesait.platform.config.model.User;

public interface KsqlRelationRepository extends JpaRepository<KsqlRelation, String> {

	public List<KsqlRelation> findByKsqlFlow(KsqlFlow ksqlFlow);

	public List<KsqlRelation> findByKsqlResource(KsqlResource ksqlResource);

	public KsqlRelation findByKsqlFlowAndKsqlResource(KsqlFlow ksqlFlow, KsqlResource ksqlResource);

	public List<KsqlRelation> findByKsqlResourceKafkaTopicAndKsqlFlowNot(String kafkaTopic, KsqlFlow ksqlFlow);

	@Modifying
	@Transactional
	void deleteByKsqlFlowIdentificationAndKsqlResourceIdentification(String ksqlFlowIdentification,
			String ksqlResourceIdentification);

	public List<KsqlRelation> findByKsqlFlowAndKsqlResourceKafkaTopic(KsqlFlow ksqlFlow, String kafkaTopic);

	public List<KsqlRelation> findByKsqlFlowAndKsqlResourceIdentification(KsqlFlow ksqlFlow, String identification);

	public List<KsqlRelation> findByKsqlResourceIdentification(String identification);

	@Query("SELECT R.ksqlResource.identification FROM KsqlRelation as R")
	public List<String> findAllIdentifications();

	public List<KsqlRelation> findByKsqlFlowIdAndKsqlResourceIdentificationContainingAndKsqlResourceDescriptionContaining(
			String id, String identification, String description);

	public List<KsqlRelation> findByKsqlFlowUserAndKsqlFlowIdAndKsqlResourceIdentificationContainingAndKsqlResourceDescriptionContaining(
			User sessionUser, String id, String identification, String description);

	public List<KsqlRelation> findByKsqlFlowUserAndKsqlFlowId(User sessionUser, String id);

	// Selecting ORIGIN, PROCESS or DESTINY
	public List<KsqlRelation> findByKsqlFlowAndKsqlResourceResourceTypeOrderByCreatedAtDesc(KsqlFlow ksqlFlow,
			FlowResourceType resourceType);

	// Selecting STREAMS, TABLES or INSERTS
	public List<KsqlRelation> findByKsqlFlowAndKsqlResourceKsqlType(KsqlFlow ksqlFlow, KsqlResourceType ksqlType);

	// Selecting ORIGIN, PROCESS or DESTINY
	public List<KsqlRelation> findByKsqlFlowAndKsqlResourceResourceTypeAndKsqlResourceKsqlTypeNotOrderByCreatedAtDesc(
			KsqlFlow ksqlFlow, FlowResourceType resourceType, KsqlResourceType ksqlType);
}
