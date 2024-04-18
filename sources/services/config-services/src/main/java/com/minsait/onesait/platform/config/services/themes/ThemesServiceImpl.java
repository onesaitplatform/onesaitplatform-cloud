/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.config.services.themes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Themes;
import com.minsait.onesait.platform.config.repository.ThemesRepository;
import com.minsait.onesait.platform.config.services.themes.dto.ThemesDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ThemesServiceImpl implements ThemesService {

	@Autowired
	private ThemesRepository themesRepository;

	@Override
	public void createTheme(ThemesDTO themeDTO) {

		final Themes theme = new Themes();

		try {
			if (themesRepository.findIdByIdentification(themeDTO.getIdentification()) != null) {
				log.error("There is a Theme with the same Identification");
				throw new GenericOPException("There is a Theme with the same Identification");
			}

			theme.setIdentification(themeDTO.getIdentification());
			theme.setJson(themeDTO.getJson().toString());

			themesRepository.save(theme);
		} catch (final GenericOPException e) {
			log.error("Error creating a new theme: {}", e.getMessage());
		}
	}

	@Override
	public void updateTheme(ThemesDTO themeDTO) {
		try {
			themesRepository.findById(themeDTO.getId()).ifPresent(theme -> {
				theme.setIdentification(themeDTO.getIdentification());
				theme.setJson(themeDTO.getJson().toString());

				themesRepository.save(theme);
			});

		} catch (final Exception e) {
			log.error("Error updating a theme: {}", e.getMessage());
		}
	}

	@Override
	public void setActive(String id) {
		try {
			final List<Themes> activeThemes = themesRepository.findActive();

			for (final Themes theme : activeThemes) {
				theme.setActive(false);
				themesRepository.save(theme);
			}

			themesRepository.findById(id).ifPresent(themeToActive -> {
				themeToActive.setActive(true);
				themesRepository.save(themeToActive);
			});

		} catch (final Exception e) {
			log.error("Error setting to active: {}", e.getMessage());
		}
	}

	@Override
	public ThemesDTO getTheme(String id) {
		try {
			Optional<Themes> theme = themesRepository.findById(id);
			if (theme.isPresent()) {
				return castThemetoDTO(theme.get());
			}
		} catch (final Exception e) {
			log.error("Error getting theme: {}", e.getMessage());
		}
		return null;
	}

	@Override
	public ThemesDTO getThemeByIdentification(String id) {
		try {
			Themes theme = themesRepository.findByIdentificationOrId(id, id);
			if (theme != null) {
				return castThemetoDTO(theme);
			}
		} catch (final Exception e) {
			log.error("Error getting theme: {}", e.getMessage());
		}
		return null;
	}

	@Override
	public void deactivate(String id) {
		try {
			themesRepository.findById(id).ifPresent(theme -> {
				theme.setActive(false);
				themesRepository.save(theme);
			});

		} catch (final Exception e) {
			log.error("Error setting to inactive: {}", e.getMessage());
		}
	}

	@Override
	public void setDefault() {
		try {
			final List<Themes> activeThemes = themesRepository.findActive();

			for (final Themes theme : activeThemes) {
				theme.setActive(false);
				themesRepository.save(theme);
			}
		} catch (final Exception e) {
			log.error("Error setting default theme: {}", e.getMessage());
		}
	}

	private ThemesDTO castThemetoDTO(Themes theme) {
		ThemesDTO themeDTO = new ThemesDTO();
		themeDTO.setIdentification(theme.getIdentification());
		themeDTO.setJson(new JSONObject(theme.getJson()));
		return themeDTO;
	}
	
	@Override
	public List<String> listThemes() {
		return themesRepository.findAll().stream().map(Themes::getIdentification).collect(Collectors.toList());
	}

}
