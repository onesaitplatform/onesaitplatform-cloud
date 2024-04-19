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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.minsait.onesait.platform.config.model.KsqlFlow;
import com.minsait.onesait.platform.config.model.User;

public interface KsqlFlowRepository extends JpaRepository<KsqlFlow, String> {

	List<KsqlFlow> findByUser(User user);

	List<KsqlFlow> findByIdentification(String identification);

	List<KsqlFlow> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	List<KsqlFlow> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	@Modifying
	@Transactional
	void deleteByIdentificationAndUserUserId(String identification, String userId);

	List<KsqlFlow> findAllByOrderByIdentificationAsc();

	KsqlFlow findByUserAndIdentification(User sessionUser, String identification);

	@Override
	@Transactional
	void deleteById(String id);
}
