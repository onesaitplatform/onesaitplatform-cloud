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

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.versioning.VersionableVO;

public interface GadgetTemplateTypeRepository extends JpaRepository<GadgetTemplateType, String> {

	@Override
	List<GadgetTemplateType> findAll();

	GadgetTemplateType findByIdentification(String identification);

	default List<GadgetTemplateType> findByUser(User user){
		return this.findAll();
	}

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);

	@Query("SELECT new com.minsait.onesait.platform.config.versioning.VersionableVO(o.identification, o.id, 'GadgetTemplateType') FROM GadgetTemplateType AS o")
	public List<VersionableVO> findVersionableViews();
}
