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
package com.minsait.onesait.platform.controlpanel.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

@Service
@Deprecated
class Sofia2PullMetrics /* implements PublicMetrics */ {

	@Autowired
	@Lazy
	private SessionRegistry sessionRegistry;

	// @Override
	// public Collection<Metric<?>> metrics() {
	// final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
	// Metric<?> metric1 = new Metric<>("_sofia2.:controlpanel.userslogged",
	// allPrincipals.size(), new Date());
	// Metric<?> metric2 = new Metric<>("_sofia2:controlpanel.other", 1, new
	// Date());
	// // Metric<?> metric2 = new
	// //
	// Metric<String>("com.indracompany.sofia2.controlpanel.metric.userslogged.names",
	// // allPrincipals.get(0).toString(),new Date());
	// HashSet<Metric<?>> set = new HashSet<>();
	// set.add(metric1);
	// set.add(metric2);
	// return set;
	// }
}