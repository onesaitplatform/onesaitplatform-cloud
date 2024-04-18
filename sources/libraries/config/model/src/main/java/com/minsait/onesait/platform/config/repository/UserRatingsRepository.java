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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.UserRatings;

public interface UserRatingsRepository extends JpaRepository<UserRatings, String> {

	@Query("SELECT o FROM UserRatings AS o WHERE (o.marketAsset.id = :marketAssetId AND o.user.userId = :userId)")
	List<UserRatings> findByMarketAssetAndUser(@Param("marketAssetId") String marketAssetId,
			@Param("userId") String userId);

	@Query("SELECT o FROM UserRatings AS o WHERE (o.marketAsset.id = :marketAssetId)")
	List<UserRatings> findByMarketAsset(@Param("marketAssetId") String marketAssetId);

}
