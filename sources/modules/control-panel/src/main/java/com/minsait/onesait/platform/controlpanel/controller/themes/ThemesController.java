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
package com.minsait.onesait.platform.controlpanel.controller.themes;


import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.minsait.onesait.platform.config.model.Themes;
import com.minsait.onesait.platform.config.model.Themes.editItems;
import com.minsait.onesait.platform.config.repository.ThemesRepository;
import com.minsait.onesait.platform.config.services.themes.ThemesServiceImpl;
import com.minsait.onesait.platform.config.services.themes.dto.ThemesDTO;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/themes")
@Slf4j
public class ThemesController {
	
	@Autowired
	private ThemesServiceImpl themesService;

	@Autowired
	private ThemesRepository themesRepository;
	
	private static final String LOGIN_TITLE = editItems.LOGIN_TITLE.toString();
	private static final String LOGIN_IMG = editItems.LOGIN_IMAGE.toString();
	private static final String HEADER_IMG = editItems.HEADER_IMAGE.toString();
	private static final String LOGIN_BACKGROUND_COLOR = editItems.LOGIN_BACKGROUND_COLOR.toString();
	private static final String LOGIN_TITLE_ES = editItems.LOGIN_TITLE_ES.toString();
	private static final String FOOTER_TEXT = editItems.FOOTER_TEXT.toString();
	private static final String FOOTER_TEXT_ES = editItems.FOOTER_TEXT_ES.toString();
	private static final String THEME = "theme";
	private static final String OK = "{\"status\" : \"ok\"}";
	private static final String FAIL = "{\"status\" : \"fail\"}";
	private static final String DELETINGERROR = "Error delating the support request: ";
	
	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String create(Model model) {
		model.addAttribute(THEME, new ThemesDTO());
		return "themes/create";
	}
	
	@GetMapping(value = "/show/{id}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String show(Model model, @PathVariable("id") String id) {
		model.addAttribute("type", "show");
		model.addAttribute(THEME, populateThemeDTO(themesRepository.findById(id)));
		
		return "themes/show";
	}
	
	@GetMapping(value = "/edit/{id}", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String edit(Model model, @PathVariable("id") String id) {
		model.addAttribute("type", "edit");
		model.addAttribute(THEME, populateThemeDTO(themesRepository.findById(id)));
		return "themes/show";
	}
	
	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String show(Model model) {
		model.addAttribute("themes", themesRepository.findAll());
		return "themes/list";
	}

	
	@PostMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String createTheme (@Valid ThemesDTO theme){
		try {
			populateJsonTheme(theme);			
			themesService.createTheme(theme);
		} catch (Exception e) {
			log.error("Could not create the Theme");
			return "redirect:/themes/create";
		}
		
		return "redirect:/themes/list";	
				
	}
	
	@PostMapping(value = "/edit/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public String editTheme (@Valid ThemesDTO theme,  @PathVariable("id") String id){
		try {
			theme.setId(id);
			populateJsonTheme(theme);			
			themesService.updateTheme(theme);
		} catch (Exception e) {
			log.error("Could not create the Theme");
			return "redirect:/themes/create";
		}
		
		return "redirect:/themes/list";	
				
	}

	@Transactional
	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> delete(@RequestParam String id) {
		try {
			themesRepository.delete(id);
			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error(DELETINGERROR + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/activate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> active(@RequestParam String id) {
		try {
			themesService.setActive(id);

			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error(DELETINGERROR + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/deactivate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> inactive(@RequestParam String id) {
		try {
			themesService.deactivate(id);

			return new ResponseEntity<>(OK, HttpStatus.OK);
		} catch (Exception e) {
			log.error(DELETINGERROR + e);
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/byDefault", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<String> byDefault() {
		themesService.setDefault();
		final List<Themes> activeThemes = themesRepository.findActive();
		if (activeThemes.isEmpty()) {
			return new ResponseEntity<>(OK, HttpStatus.OK);
		} else {
			log.error("Error settig default theme.");
			return new ResponseEntity<>(FAIL, HttpStatus.BAD_REQUEST);
		}
	}
	
	@Transactional
	@RequestMapping(value = "/getThemeJson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> getThemeJson(){
		final List<Themes> activeThemes = themesRepository.findActive();
		if (activeThemes.size() == 1) {
			return new ResponseEntity<>(activeThemes.get(0).getJson(), HttpStatus.OK);
		}
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}

	
	private void populateJsonTheme(ThemesDTO theme) {
		final JSONObject json = new JSONObject();
		theme.setJson(json);		
		
		try {
			theme.getJson().put(LOGIN_TITLE, theme.getLoginTitle());
			theme.getJson().put(LOGIN_TITLE_ES, theme.getLoginTitleEs());
			theme.getJson().put(LOGIN_BACKGROUND_COLOR, theme.getBackgroundColor());
			theme.getJson().put(FOOTER_TEXT, theme.getFooterText());
			theme.getJson().put(FOOTER_TEXT_ES, theme.getFooterTextEs());
			
			if (theme.getImage64() == null || theme.getImage64().length() == 0) {
				final byte[] imageByte = theme.getImage().getBytes();
				final String image64 = Base64.getEncoder().encodeToString(imageByte);
				theme.getJson().put(LOGIN_IMG, image64);
			} else {
				theme.getJson().put(LOGIN_IMG, theme.getImage64());
			}
			if (theme.getHeaderImage64() == null || theme.getHeaderImage64().length() == 0) {
				final byte[] imageByte = theme.getHeaderImage().getBytes();
				final String image64 = Base64.getEncoder().encodeToString(imageByte);
				theme.getJson().put(HEADER_IMG, image64);
			} else {
				theme.getJson().put(HEADER_IMG, theme.getHeaderImage64());
			}
		} catch (Exception e) {
			log.error("Error creating Json Object: "+e.getMessage());
		}
	}
	
	private ThemesDTO populateThemeDTO (Themes theme) {
		
		final ThemesDTO themeDTO = new ThemesDTO();
		
		try {
			themeDTO.setIdentification(theme.getIdentification());
			
			final JSONObject json = new JSONObject(theme.getJson());
			final Iterator<String> jsonKeys = json.keys();
			
			while (jsonKeys.hasNext()) {
				final Themes.editItems loginTitle = editItems.valueOf(jsonKeys.next());				
				switch (loginTitle){
					case LOGIN_TITLE:
						themeDTO.setLoginTitle(json.get(LOGIN_TITLE).toString());
						break;
					case LOGIN_TITLE_ES:
						themeDTO.setLoginTitleEs(json.get(LOGIN_TITLE_ES).toString());
						break;	
					case LOGIN_IMAGE:
						themeDTO.setImage64(json.get(LOGIN_IMG).toString());
						break;
					case HEADER_IMAGE:
						themeDTO.setHeaderImage64(json.get(HEADER_IMG).toString());
						break;
					case LOGIN_BACKGROUND_COLOR:
						themeDTO.setBackgroundColor(json.get(LOGIN_BACKGROUND_COLOR).toString());
						break;
					case FOOTER_TEXT:
						themeDTO.setFooterText(json.get(FOOTER_TEXT).toString());
						break;
					case FOOTER_TEXT_ES:
						themeDTO.setFooterTextEs(json.get(FOOTER_TEXT_ES).toString());
						break;
					default: break;
				}
					
			}
			
			themeDTO.setJson(json);			
		} catch (Exception e) {
			log.error("Error parsing Json Object: "+e.getMessage());
		}
		return themeDTO;
	}	

}
