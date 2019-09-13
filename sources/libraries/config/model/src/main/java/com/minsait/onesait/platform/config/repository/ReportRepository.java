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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.User;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

	List<Report> findByActiveTrue();

	List<Report> findByUserAndActiveTrueOrIsPublicTrueAndActiveTrue(User user);

	Report findByIdentificationOrId(String identification, String id);

}
