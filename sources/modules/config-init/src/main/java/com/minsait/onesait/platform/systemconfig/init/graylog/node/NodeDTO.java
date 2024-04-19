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
package com.minsait.onesait.platform.systemconfig.init.graylog.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class NodeDTO {

	@Getter
	@Setter
	private String cluster_id;
	@Getter
	@Setter
	private String node_id;
	@Getter
	@Setter
	private String type;
	@Getter
	@Setter
	private String transport_address;
	@Getter
	@Setter
	private String last_seen;
	@Getter
	@Setter
	private String short_node_id;
	@Getter
	@Setter
	private String hostname;
	@Getter
	@Setter
	private Boolean is_master;
}
