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
package com.minsait.onesait.platform.config.services.subscription;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Subscription;
import com.minsait.onesait.platform.config.model.Subscriptor;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.SubscriptionRepository;
import com.minsait.onesait.platform.config.repository.SubscriptorRepository;
import com.minsait.onesait.platform.config.services.exceptions.SubscriptionServiceException;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

	private static final String USER_NOT_AUTHORIZED = "The user is not authorized";

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private SubscriptorRepository subscriptorRepository;

	@Override
	public void createSubscription(Subscription subscription) {
		subscriptionRepository.save(subscription);
	}

	@Override
	public Subscription findById(String id, User user) {
		final Subscription subscription = subscriptionRepository.findById(id).get();
		if (user.isAdmin() || subscription.getUser().equals(user)) {
			return subscription;
		} else {
			throw new SubscriptionServiceException(USER_NOT_AUTHORIZED);
		}
	}

	@Override
	public void deleteSubscription(Subscription subscription, User user) {
		if (user.isAdmin() || subscription.getUser().equals(user)) {
			final List<Subscriptor> subscriptors = subscriptorRepository.findBySubscription(subscription);
			if (!subscriptors.isEmpty()) {
				subscriptorRepository.deleteAll(subscriptors);
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

		if (user.isAdmin()) {
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
		final Integer count = subscriptionRepository.findByIdentification(identification).size();
		return count != 0;
	}

	@Override
	public List<Subscription> findByOntology(String ontologyName) {
		List<Subscription> subscriptions = subscriptionRepository.findByOntologyIdentification(ontologyName);
		return subscriptions;
	}

}
