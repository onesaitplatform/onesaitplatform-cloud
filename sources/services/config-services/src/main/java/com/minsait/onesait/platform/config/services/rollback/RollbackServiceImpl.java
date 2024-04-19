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
package com.minsait.onesait.platform.config.services.rollback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Rollback;
import com.minsait.onesait.platform.config.repository.RollbackRepository;

@Service
public class RollbackServiceImpl implements RollbackService {

	@Autowired
	private RollbackRepository rollbackRepository;

	@Override
	public void save(Rollback rollback) {
		rollbackRepository.save(rollback);
	}

	@Override
	public Rollback findByEntityId(String entityId) {
		return rollbackRepository.findByEntityId(entityId);
	}

}
