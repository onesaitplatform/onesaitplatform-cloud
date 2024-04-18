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
package com.minsait.onesait.platform.config.services.gadgetfavorite;

import java.util.List;

import com.minsait.onesait.platform.config.model.GadgetFavorite;

public interface GadgetFavoriteService {
	void create(String identification, String idGadget, String idTemplate, String idDatasource, String type,
			String config, String metainf, String userId);

	public void update(String identification, String idGadget, String idTemplate, String idDatasource, String type,
			String config, String metainf, String userId);

	public void delete(String identification, String userId);
	
	public void deleteByUserId(String userlogged, String userId);

	public Boolean existWithIdentification(String identification);

	public List<GadgetFavorite> findAll(String userlogged);
	
	public List<GadgetFavorite> findAllGadgetFavorite(String userlogged , String userId);
	
	public List<String> getAllIdentifications(String userlogged, String userId);

	public GadgetFavorite findByIdentification(String identification, String userId);

	public GadgetFavorite findById(String id, String userId);

}