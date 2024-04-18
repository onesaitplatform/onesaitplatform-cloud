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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.ClientConnection;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.User;

public interface ClientConnectionRepository extends JpaRepository<ClientConnection, String> {

	List<ClientConnection> findByClientPlatform(ClientPlatform clientPlatform);

	List<ClientConnection> findByClientPlatformAndStaticIpTrue(ClientPlatform clientPlatform);

	List<ClientConnection> findByClientPlatformAndStaticIpFalse(ClientPlatform clientPlatform);

	List<ClientConnection> findByClientPlatformAndIdentification(ClientPlatform clientPlatform, String indentification);

	@Query("SELECT o FROM ClientConnection o WHERE o.clientPlatform.user= :#{#user}")
	List<ClientConnection> findByUser(@Param("user") User user);

	List<ClientConnection> findByIdentification(String identification);

	long countByIdentification(String identification);

	long countByClientPlatform(ClientPlatform clientPlatform);

}
