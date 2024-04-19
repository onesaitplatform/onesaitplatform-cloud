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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.MarketAsset;

public interface MarketAssetRepository extends JpaRepository<MarketAsset, String> {

	@Override

	void flush();

	@Override

	<S extends MarketAsset> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override

	MarketAsset save(MarketAsset entity);

	@Override

	void delete(MarketAsset id);

	@Override

	List<MarketAsset> findAll();

	@Override
	@CacheEvict(cacheNames = "OntologyRepository")
	void deleteAll();

	@Query("SELECT o FROM MarketAsset AS o WHERE (o.id = :id AND o.deletedAt = null)")
	MarketAsset findByIdCustom(@Param("id") String id);

	@Query("SELECT o FROM MarketAsset AS o WHERE (o.identification = :marketAssetId) order by o.identification")
	MarketAsset findByIdentification(@Param("marketAssetId") String marketAssetId);

	@Query("SELECT o FROM MarketAsset AS o WHERE (o.identification LIKE %:marketAssetId% AND o.deletedAt = null) order by o.identification")
	List<MarketAsset> findByIdentificationLike(@Param("marketAssetId") String marketAssetId);

	@Query("SELECT o FROM MarketAsset AS o WHERE (((o.user.userId = :userId OR o.isPublic = true) AND o.state = 'APPROVED')) AND (o.deletedAt = null) order by o.createdAt desc")
	List<MarketAsset> findByUser(@Param("userId") String userId);

	@Query("SELECT o.jsonDesc FROM MarketAsset AS o WHERE (o.deletedAt = null)")
	List<String> findJsonDescs();

}
