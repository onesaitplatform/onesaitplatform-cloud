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
package com.minsait.onesait.platform.config.services.ksql.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.kafka.KafkaService;
import com.minsait.onesait.platform.commons.ksql.KsqlExecutionException;
import com.minsait.onesait.platform.commons.ksql.KsqlService;
import com.minsait.onesait.platform.config.model.KsqlResource;
import com.minsait.onesait.platform.config.model.KsqlResource.FlowResourceType;
import com.minsait.onesait.platform.config.model.KsqlResource.KsqlResourceType;
import com.minsait.onesait.platform.config.repository.KsqlResourceRepository;
import com.minsait.onesait.platform.config.services.exceptions.KsqlResourceServiceException;
import com.minsait.onesait.platform.config.services.ksql.resource.pojo.KsqlResourceForUpdate;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.repository.VerticalRepository;
import com.minsait.onesait.platform.router.service.app.service.KafkaTopicKsqlNotificationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KsqlResourceServiceImpl implements KsqlResourceService, KafkaTopicKsqlNotificationService {

	@Autowired
	private KsqlResourceRepository ksqlResourceRepository;
	@Autowired(required = false)
	private KafkaService kafkaService;
	@Autowired(required = false)
	private KsqlService ksqlService;
	@Autowired
	private VerticalRepository verticalRepository;

	private final Pattern creation = Pattern.compile(
			"(?i)\\s*CREATE\\s+(TABLE|STREAM)\\s+(\\w+)\\s+(\\(((?!\\)\\s+WITH\\s+\\().)+\\))\\s*(WITH\\s+\\((\\w+\\s*\\=\\s*'[\\w]+')?(\\s*,\\s*(\\w+\\s*\\=\\s*'[\\w]+'))*\\))?.*;");
	private final Pattern creationAs = Pattern.compile(
			"(?i)\\s*CREATE\\s+(TABLE|STREAM)\\s+(\\w+)\\s+(\\s*WITH\\s*\\((\\w+\\s*\\=\\s*'[\\w]+')?(\\s*,\\s*(\\w+\\s*\\=\\s*'[\\w]+'))*\\))?\\s*AS\\s+.*FROM\\s+((\\w+)(\\s+(LEFT\\s+JOIN\\s+)(\\w+))?).*;");
	Pattern insertInto = Pattern.compile(
			"(?i)\\s*(INSERT)\\s+INTO\\s+(\\w+)\\s+SELECT\\s+((?!\\s+FROM\\s+).)*\\s+FROM\\s+((\\w+)(\\s+(LEFT\\s+JOIN\\s+)(\\w+))?).*;");

	@Override
	public KsqlResource getKsqlResourceByIdentification(String identification) {
		return ksqlResourceRepository.findByIdentification(identification);
	}

	@Override
	public List<KsqlResource> getKsqlResourceByKafkaTopic(String kafkaTopic) {
		return ksqlResourceRepository.findByKafkaTopic(kafkaTopic);
	}

	@Override
	public void validateKsqlResource(KsqlResource newKsqlResource) {
		String id = newKsqlResource.getId();
		// ¿Is the ResourceName free?
		if (id == null) {
			id = "";
		}
		if (ksqlResourceRepository.findByIdentificationAndIdNot(newKsqlResource.getIdentification(), id) != null) {
			log.error("The KsqlResource Identification is already being used. Identification = {}.",
					newKsqlResource.getIdentification());
			throw new KsqlResourceServiceException(
					"The KsqlResource Identification is already being used. Identification = "
							+ newKsqlResource.getIdentification());
		}

	}

	@Override
	@Transactional
	public void createKsqlResource(KsqlResource ksqlResource) throws KsqlExecutionException {

		validateKsqlResource(ksqlResource);
		// Check if topic exists. If not, then create it
		if (ksqlResource.getResourceType() == FlowResourceType.ORIGIN) {

			kafkaService.createTopicForKsqlInput(ksqlResource.getOntology().getIdentification(),
					verticalRepository.findBySchema(MultitenancyContextHolder.getVerticalSchema()).getName(),
					MultitenancyContextHolder.getTenantName());
		} else if (ksqlResource.getResourceType() == FlowResourceType.DESTINY) {
			kafkaService.createTopic(ksqlResource.getKafkaTopic());
		}

		// Send info to KSQL Client to activate the resource
		ksqlService.executeKsqlRequest(ksqlResource.getStatementText());

		ksqlResourceRepository.save(ksqlResource);
	}

	@Override
	@Transactional
	public void updateKsqlResource(KsqlResourceForUpdate ksqlresourceForUpdate) throws KsqlExecutionException {
		// Check if exits by id/identification
		final KsqlResource ksqlResource = ksqlresourceForUpdate.getChangedKsqlReousrce();
		final KsqlResource preModifKsqlResource = ksqlResourceRepository
				.findByIdentification(ksqlResource.getIdentification());
		if (preModifKsqlResource == null) {
			log.error("KsqlResource does not exist. Identification = {}", ksqlResource.getIdentification());
			throw new KsqlResourceServiceException(
					"KsqlResource does not exist. Identification = " + ksqlResource.getIdentification());
		}
		if (ksqlresourceForUpdate.isStatemenChanged()) {
			// Validate ksqlResource
			parseStatementTextAndGetDependencies(ksqlResource);
			if (!ksqlresourceForUpdate.getCurrentIdentification().equals(ksqlResource.getIdentification())) {
				validateKsqlResource(ksqlResource);
			}
			try {
				// Delete from KSQL Server
				ksqlService.deleteQueriesFromSink(ksqlresourceForUpdate.getCurrentIdentification());
				ksqlService.executeKsqlRequest("drop " + ksqlresourceForUpdate.getCurrentKsqlResourceType() + " "
						+ ksqlresourceForUpdate.getCurrentIdentification() + ";");

				// Create in KSQL server
				ksqlService.executeKsqlRequest(ksqlResource.getStatementText());

			} catch (final KsqlExecutionException e) {
				// Rollback
				ksqlService.executeKsqlRequest(ksqlresourceForUpdate.getCurrentStatement());

				log.error("Error creating KSQL {}={} in Server.", ksqlResource.getKsqlType(),
						ksqlResource.getIdentification());
				throw new KsqlExecutionException("KsqlResource cannot be created in KSQL server. Identification = "
						+ ksqlResource.getIdentification());
			}

		}
		// Update in CDB
		ksqlResourceRepository.save(ksqlResource);
	}

	@Override
	@Transactional
	public void deleteKsqlResource(KsqlResource ksqlResource) throws KsqlExecutionException {
		// Check if exits by id/identification
		if (existsByIdentification(ksqlResource.getIdentification())) {
			// Check if there are QUERIES using the Resource -> Delete them
			if (ksqlResource.getResourceType() != FlowResourceType.DESTINY) {
				// No implicit queries will be generated by DESTINY resources.
				// If we activate this for DESTINY we risk deleting INSERT
				// resources from KSQL but not from CDB.
				ksqlService.deleteQueriesFromSink(ksqlResource.getIdentification().toUpperCase());
			}
			// Delete from KSQL Server
			if (ksqlResource.getKsqlType() != KsqlResourceType.INSERT) {
				ksqlService.executeKsqlRequest(
						"drop " + ksqlResource.getKsqlType() + " " + ksqlResource.getIdentification() + ";");
			} else {
				ksqlService.deleteQueriesByStatement(ksqlResource.getStatementText());
			}
			// Delete Resource if no more Relationis are
			ksqlResourceRepository.delete(ksqlResource);

		} else {
			log.info("Error while deleting resource. Resource {} does not exist.", ksqlResource.getIdentification());
			throw new KsqlResourceServiceException("Resource " + ksqlResource.getIdentification() + " does not exist.");
		}

	}

	private boolean existsByIdentification(String identification) {
		return ksqlResourceRepository.findByIdentification(identification) != null;
	}

	@Override
	public KsqlResource getKsqlResourceById(String id) {
		return ksqlResourceRepository.findById(id).orElse(null);
	}

	@Override
	public List<String> parseStatementTextAndGetDependencies(KsqlResource ksqlResource) {

		Matcher matcher = creation.matcher(ksqlResource.getStatementText());

		final List<String> dependencies;
		ksqlResource.setCreatedAs(false);
		boolean hasWithClause = true;
		int withGroup = 5;
		int fromDepencencyIndex = 8;
		int leftJoinDependencyIndex = 11;
		String prefix = "";

		if (!matcher.matches()) {
			matcher = creationAs.matcher(ksqlResource.getStatementText());
			ksqlResource.setCreatedAs(true);
			withGroup = 3;
		}
		if (!matcher.matches()) {
			matcher = insertInto.matcher(ksqlResource.getStatementText());
			ksqlResource.setCreatedAs(true);
			hasWithClause = false;
			fromDepencencyIndex = 5;
			leftJoinDependencyIndex = 8;
			final DateTime now = new DateTime();
			prefix = "INSERT_" + now.getMillis() + "_";
		}

		if (matcher.matches()) {
			ksqlResource.setKsqlType(KsqlResourceType.valueOf(matcher.group(1).toUpperCase()));
			ksqlResource.setIdentification(prefix + matcher.group(2));
			final String withClause = matcher.group(withGroup);
			dependencies = configureMatchingKsqlResource(ksqlResource, matcher, hasWithClause, withClause,
					fromDepencencyIndex, leftJoinDependencyIndex);
		} else {
			// Statement does not correspond to any type
			throw new KsqlResourceServiceException("Syntax ERROR. Invalid KSQL Statement.");
		}
		return dependencies;
	}

	private List<String> configureMatchingKsqlResource(KsqlResource ksqlResource, Matcher matcher,
			boolean hasWithClause, String withClause, int fromDepencencyIndex, int leftJoinDependencyIndex) {
		final List<String> dependencies = new ArrayList<>();

		if (hasWithClause && withClause != null) {
			final String[] listOfPorperties = withClause.trim().replaceAll("(?i)^WITH\\s*\\(", "")
					.replaceAll("\\s*\\).*", "").split(",");
			for (final String property : listOfPorperties) {
				final String[] prpNameValue = property.split("=");
				if (prpNameValue[0].equalsIgnoreCase("KAFKA_TOPIC")) {
					ksqlResource.setKafkaTopic(prpNameValue[1].replaceAll("'", ""));
				}
			}
		} else {
			ksqlResource.setKafkaTopic(ksqlResource.getIdentification().toUpperCase());
		}
		// Dependencies
		if (ksqlResource.isCreatedAs()) {
			dependencies.add(matcher.group(fromDepencencyIndex));
			if (matcher.group(leftJoinDependencyIndex) != null) {
				dependencies.add(matcher.group(leftJoinDependencyIndex));
			}
		}
		return dependencies;
	}

	@Override
	public List<String> getKafkaTopicKsqlNotification(String ontology) {
		return ksqlResourceRepository.findByOntologyIdentificationAndResourceType(ontology, FlowResourceType.ORIGIN);
	}

}
