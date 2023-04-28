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
package com.minsait.onesait.platform.gateway.model;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Entity
@Table(name = "GATEWAY_ROUTE")
@Data
@Slf4j
public class GatewayRoute {

	@Id
	private String id;
	private String predicates;
	private String filters;
	private String uri;
	@Column(name="ROUTE_ORDER")
	private Integer order = 0;
	private String metadata;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static Mono<RouteDefinition> convertValue(GatewayRoute route) {
		final RouteDefinition rd = new RouteDefinition();

		try {
			rd.setId(route.getId());
			rd.setUri(new URI(route.getUri()));
			rd.setPredicates(MAPPER.readValue(route.getPredicates(), new TypeReference<List<PredicateDefinition>>() {
			}));
			rd.setFilters(MAPPER.readValue(route.getFilters(), new TypeReference<List<FilterDefinition>>() {
			}));
			rd.setMetadata(MAPPER.readValue(route.getMetadata(), new TypeReference<Map<String, Object>>() {
			}));
			rd.setOrder(route.getOrder());
		} catch (final Exception e) {
			log.error("Could not convert database value to Route Definition. id {}", route.getId(), e);
			return Mono.empty();
		}
		return Mono.just(rd);
	}

	public static Mono<GatewayRoute> convertValue(RouteDefinition rd) {
		final GatewayRoute gr = new GatewayRoute();
		try {
			gr.setId(rd.getId());
			gr.setOrder(rd.getOrder());
			gr.setUri(rd.getUri().toString());
			gr.setPredicates(MAPPER.writeValueAsString(rd.getPredicates()));
			gr.setFilters(MAPPER.writeValueAsString(rd.getFilters()));
			gr.setMetadata(MAPPER.writeValueAsString(rd.getMetadata()));
		} catch (final Exception e) {
			log.error("Could not convert Route Definition to database value. id {}", rd.getId(), e);
			return Mono.empty();
		}
		return Mono.just(gr);
	}
}