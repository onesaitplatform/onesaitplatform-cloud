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
package com.minsait.onesait.platform.config.services.menu;

import java.util.Date;

import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.ConsoleMenuRepository;

@Service

public class MenuServiceImpl implements MenuService {

	@Autowired
	private ConsoleMenuRepository consoleMenuRepository;

	@Override
	public String loadMenuByRole(User user) {
		if (user != null)
			return consoleMenuRepository.findByRoleType(user.getRole()).getJson();
		else
			return null;
	}

	@Override
	public void updateMenu(String menuId, String menuJson) {
		try {
			consoleMenuRepository.findById(menuId).ifPresent(menu -> {
				menu.setJson(menuJson);
				menu.setUpdatedAt(new Date());

				consoleMenuRepository.save(menu);
			});

		} catch (final RuntimeException e) {
			Log.error("Error updating menu: ", e.getMessage());
		}
	}

}
