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
package com.minsait.onesait.platform.solver;

import java.util.List;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.dto.socket.querystt.FilterStt;
import com.minsait.onesait.platform.dto.socket.querystt.OrderByStt;
import com.minsait.onesait.platform.dto.socket.querystt.ParamStt;
import com.minsait.onesait.platform.dto.socket.querystt.ProjectStt;
import com.minsait.onesait.platform.exception.DashboardEngineException;

public interface SolverInterface {

	public String buildQueryAndSolve(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param, boolean debug,
			String executeAs, String ontology)
			throws DashboardEngineException, OntologyDataUnauthorizedException, GenericOPException;

	public String buildQuery(String query, int maxreg, List<FilterStt> where, List<ProjectStt> project,
			List<String> group, List<OrderByStt> sort, long offset, long limit, List<ParamStt> param, boolean debug,
			String executeAs, String ontology);
}
