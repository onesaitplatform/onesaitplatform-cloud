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
package com.minsait.onesait.platform.config.services.subscription;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.OntologyRepository;
import com.minsait.onesait.platform.config.repository.SubscriptionRepository;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;
import com.minsait.onesait.platform.config.services.exceptions.SubscriptionServiceException;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

	private static final String USER_NOT_AUTHORIZED = "The user is not authorized";

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private OntologyRepository ontologyRepository;

	@Autowired
	private SubscriptorRepository subscriptorRepository;

	@Override
	public void createSubscription(Subscription subscription) {
		subscriptionRepository.save(subscription);
	}

	@Override
	public Subscription findById(String id, User user) {
		Subscription subscription = subscriptionRepository.findById(id);
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| subscription.getUser().equals(user)) {
			return subscription;
		} else {
			throw new SubscriptionServiceException(USER_NOT_AUTHORIZED);
		}
	}

	@Override
	public void deleteSubscription(Subscription subscription, User user) {
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())
				|| subscription.getUser().equals(user)) {
			List<Subscriptor> subscriptors = subscriptorRepository.findBySubscription(subscription);
			if (!subscriptors.isEmpty()) {
				subscriptorRepository.delete(subscriptors);
			}
			subscriptionRepository.delete(subscription);
		} else {
			throw new SubscriptionServiceException(USER_NOT_AUTHORIZED);
		}
	}

	@Override
	public List<Subscription> getWebProjectsWithDescriptionAndIdentification(User user, String identification,
			String description) {
		List<Subscription> subscriptions;

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			subscriptions = subscriptionRepository
					.findByIdentificationContainingAndDescriptionContainingOrderByIdentificationAsc(identification,
							description);
		} else {
			subscriptions = subscriptionRepository
					.findByUserAndIdentificationContainingAndDescriptionContainingOrderByIdentificationAsc(user,
							identification, description);
		}

		return subscriptions;
	}

	@Override
	public boolean isIdValid(String identification) {

		final String regExp = "^[^\\d].*";
		return (identification.matches(regExp));
	}

	@Override
	public boolean existSubscriptionWithIdentification(String identification) {
		Integer count = subscriptionRepository.findByIdentification(identification).size();
		if (count != 0) {
			return true;
		}
		return false;
	}

	@Override
	public List<Subscription> findByOntology(String ontologyName) {
		Ontology ontology = ontologyRepository.findByIdentification(ontologyName);
		if (ontology == null) {
			return new ArrayList<>();
		}
		return subscriptionRepository.findByOntology(ontology);
	}

}
