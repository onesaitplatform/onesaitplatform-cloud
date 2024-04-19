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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.ODTypology;
import com.minsait.onesait.platform.config.model.User;

public interface ODTypologyRepository extends JpaRepository<ODTypology, String> {

	public List<ODTypology> findByIdentification(String identification);

	public ODTypology findTypologyById(String id);

	public void deleteByIdentificationAndUser(String identification, User user);

	public List<ODTypology> findAllByOrderByIdentificationAsc();

	public ODTypology findTypologyByIdentification(String identification);

	public List<ODTypology> findByUser(User user);

	public List<ODTypology> findByUserOrderByIdentificationAsc(User user);

	public List<ODTypology> findByUserOrderByIdentificationDesc(User user);

	public List<ODTypology> findByUserOrderByCreatedAtAsc(User user);

	public List<ODTypology> findByUserOrderByCreatedAtDesc(User user);

	public List<ODTypology> findByUserOrderByUpdatedAtAsc(User user);

	public List<ODTypology> findByUserOrderByUpdatedAtDesc(User user);

	public List<ODTypology> findByDescription(String description);

	public List<ODTypology> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	public List<ODTypology> findByIdentificationContaining(String identification);

	public List<ODTypology> findByDescriptionContaining(String description);

	public List<ODTypology> findByUserAndIdentificationContainingAndDescriptionContaining(User user,
			String identification, String description);

	public List<ODTypology> findByUserAndIdentificationContaining(User user, String identification);

	public List<ODTypology> findByUserAndDescriptionContaining(User user, String description);

	public List<ODTypology> findByIdentificationAndDescriptionAndUser(String identification, String description,
			User user);

	public List<ODTypology> findByIdentificationAndDescription(String identification, String description);

}
