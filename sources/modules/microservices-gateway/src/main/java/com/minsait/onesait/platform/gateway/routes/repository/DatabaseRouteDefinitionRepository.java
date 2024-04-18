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
package com.minsait.onesait.platform.gateway.routes.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.minsait.onesait.platform.gateway.model.GatewayRoute;
import com.minsait.onesait.platform.gateway.model.GatewayRouteRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DatabaseRouteDefinitionRepository implements RouteDefinitionRepository {

	@Autowired
	private GatewayRouteRepository gatewayRouteRepository;

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		final List<Mono<RouteDefinition>> routes = gatewayRouteRepository.findAll().stream()
				.map(GatewayRoute::convertValue).collect(Collectors.toList());
		return Flux.concat(routes);
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return route.flatMap(r -> {
			if (ObjectUtils.isEmpty(r.getId())) {
				return Mono.error(new IllegalArgumentException("id may not be empty"));
			}
			gatewayRouteRepository.save(GatewayRoute.convertValue(r).block());
			return Mono.empty();
		});
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		return routeId.flatMap(id -> {
			if (gatewayRouteRepository.findById(id).isPresent()) {
				gatewayRouteRepository.deleteById(id);
				return Mono.empty();
			}
			return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
		});
	}

}
