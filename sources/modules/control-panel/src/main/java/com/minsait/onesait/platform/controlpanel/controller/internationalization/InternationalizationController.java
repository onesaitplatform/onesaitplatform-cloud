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
package com.minsait.onesait.platform.controlpanel.controller.internationalization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Internationalization;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.InternationalizationRepository;
import com.minsait.onesait.platform.config.services.exceptions.InternationalizationServiceException;
import com.minsait.onesait.platform.config.services.internationalization.InternationalizationService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/internationalizations")
@Controller
@Slf4j
public class InternationalizationController {

    @Autowired
    private InternationalizationService internationalizationService;
    @Autowired
    private AppWebUtils utils;
    @Autowired
    private InternationalizationRepository internationalizationRepository;
    @Autowired
    private UserService userService;
    

    private static final String INTERNATIONALIZATION_STR = "internationalization";
    private static final String JSON18N_CREATE = "internationalizations/create";
    private static final String REDIRECT_JSON18N_CREATE = "redirect:/internationalizations/create";
    private static final String INTERNATIONALIZATION_VALIDATION_ERROR = "internationalization.validation.error";
    private static final String CREDENTIALS_STR = "credentials";
    private static final String EDITION = "edition";
    private static final String REDIRECT_INTERNATIONALIZATIONS_VIEW = "internationalizations/show";
    private static final String BLOCK_PRIOR_LOGIN = "block_prior_login";
    private static final String USERS = "users";
    private static final String REDIRECT_ERROR_403 = "error/403";
    
    
    @RequestMapping(value = "/list",
        produces = "text/html")
    public String list(Model uiModel, HttpServletRequest request,
        @RequestParam(required = false,
            name = "identification") String identification,
        @RequestParam(required = false,
            name = "description") String description) {

        // Scaping "" string values for parameters
        if (identification != null && identification.equals("")) {
            identification = null;
        }
        if (description != null && description.equals("")) {
            description = null;
        }
        final List<Internationalization> internationalization; 
        internationalization = internationalizationService.findInternationalizationWithIdentificationAndDescription(identification, description, utils.getUserId());

        uiModel.addAttribute("internationalizations", internationalization);
        return "internationalizations/list";

    }

    @GetMapping(value = "/create")
    public String create(Model model) {
        model.addAttribute(INTERNATIONALIZATION_STR, new Internationalization());

        model.addAttribute(USERS, getUserListDTO());
        model.addAttribute("schema", internationalizationRepository.findAll());
        return JSON18N_CREATE;
    }
    
    @PostMapping(value = { "/create" })
    public String createInternationalization(Model model, @Valid Internationalization internationalization, BindingResult bindingResult,
            HttpServletRequest request, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            utils.addRedirectMessage(INTERNATIONALIZATION_VALIDATION_ERROR, redirect);
            return REDIRECT_JSON18N_CREATE;
        }

        try {
            final String internationalizationId = internationalizationService.createNewInternationalization(internationalization, utils.getUserId(), false);
            return "redirect:/internationalizations/editfull/" + internationalizationId;

        } catch (final InternationalizationServiceException e) {
            utils.addRedirectException(e, redirect);
            return REDIRECT_JSON18N_CREATE;
        }
    }
    
    @GetMapping(value = "/update/{id}", produces = "text/html")
    public String update(Model model, @PathVariable("id") String id) {
        model.addAttribute(INTERNATIONALIZATION_STR, internationalizationService.getInternationalizationEditById(id, utils.getUserId()));
        return JSON18N_CREATE;
    }
    
    @PostMapping(value = { "/internationalizationconf/{id}" })
    public String saveUpdateInternationalization(@PathVariable("id") String id, Internationalization internationalization,
            BindingResult bindingResult, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            utils.addRedirectMessage(INTERNATIONALIZATION_VALIDATION_ERROR, redirect);
            return REDIRECT_JSON18N_CREATE;
        }
        try {
            if (internationalizationService.hasUserEditPermission(id, utils.getUserId())) {
                internationalizationService.updatePublicInternationalization(internationalization, utils.getUserId());

            } else {
                throw new InternationalizationServiceException(
                        "Cannot update Internationalization that does not exist or don't have permission");
            }
            return "redirect:/internationalizations/list/";

        } catch (final InternationalizationServiceException e) {
            utils.addRedirectException(e, redirect);
            return "redirect:/internationalizations/internationalizationconf/" + internationalization.getId();
        }
    }

    @GetMapping(value = "/internationalizationconf/{id}", produces = "text/html")
    public String updateInternationalization(Model model, @PathVariable("id") String id) {
        try {
        	final Internationalization internationalization = internationalizationService.getInternationalizationEditById(id, utils.getUserId());
        
	        if (internationalization != null) {
	
	            final Internationalization i18n = new Internationalization();
	
	            i18n.setId(id);
	            i18n.setIdentification(internationalization.getIdentification());
	            i18n.setDescription(internationalization.getDescription());
	            i18n.setJsoni18n(internationalization.getJsoni18n());
	            i18n.setPublic(internationalization.isPublic());
	            i18n.setLanguage(internationalization.getLanguage());
	
	            model.addAttribute(INTERNATIONALIZATION_STR, i18n);
	
	            return JSON18N_CREATE;
	        } else {
	            return "redirect:/internationalizations/list";
	        }
        } catch (InternationalizationServiceException e) {
            return "redirect:/internationalizations/list";
        }
    }
    
    @GetMapping(value = "/editor/{id}", produces = "text/html")
    public String editorInternationalization(Model model, @PathVariable("id") String id) {
        model.addAttribute(INTERNATIONALIZATION_STR, internationalizationService.getInternationalizationById(id, utils.getUserId()));
        return "internationalizations/editor";

    }
    
    @GetMapping(value = "/show/{id}", produces = "text/html")
    public String viewerInternationalization(Model model, @PathVariable("id") String id, HttpServletRequest request) {
        if (internationalizationService.hasUserViewPermission(id, utils.getUserId())) {
            final Internationalization internationalization = internationalizationService.getInternationalizationById(id, utils.getUserId());
            model.addAttribute(INTERNATIONALIZATION_STR, internationalization);
            model.addAttribute(CREDENTIALS_STR, internationalizationService.getCredentialsString(utils.getUserId()));
            model.addAttribute(EDITION, false);
            request.getSession().removeAttribute(BLOCK_PRIOR_LOGIN);
            return REDIRECT_INTERNATIONALIZATIONS_VIEW;
        } else {
            request.getSession().setAttribute(BLOCK_PRIOR_LOGIN, request.getRequestURI());
            return "redirect:/login";
        }
    }
    
    @PutMapping(value = "/save/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String updateInternationalization(@PathVariable("id") String id,
            @RequestParam("data") Internationalization internationalization) {
        internationalizationService.saveInternationalization(id, internationalization, utils.getUserId());
        return "ok";
    }
    
    @PutMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String deleteInternationalization(@PathVariable("id") String id) {

        try {
            internationalizationService.deleteInternationalization(id, utils.getUserId());
        } catch (final RuntimeException e) {
            return "{\"ok\":false, \"error\":\"" + e.getMessage() + "\"}";
        }
        return "{\"ok\":true}";
    }
    
    @DeleteMapping("/{id}")
    public String delete(Model model, @PathVariable("id") String id, RedirectAttributes ra) {
        try {
        	String userId = utils.getUserId();
	        if (!internationalizationService.hasUserPermission(
	        		internationalizationService.getInternationalizationById(id, userId).getIdentification(), userId)) {
	        	return REDIRECT_ERROR_403;
	        }
	        internationalizationService.deleteInternationalization(id, userId);
        } catch (final RuntimeException e) {
            utils.addRedirectException(e, ra);
        }
        return "redirect:/internationalizations/list/";
    }
    
    @GetMapping(value = "/editfull/{id}", produces = "text/html")
    public String editFullDashboard(Model model, @PathVariable("id") String id) {
        if (internationalizationService.hasUserEditPermission(id, utils.getUserId())) {
            final Internationalization internationalization = internationalizationService.getInternationalizationById(id, utils.getUserId());
            model.addAttribute(INTERNATIONALIZATION_STR, internationalization);
            model.addAttribute(CREDENTIALS_STR, internationalizationService.getCredentialsString(utils.getUserId()));
            model.addAttribute(EDITION, true);
            return REDIRECT_INTERNATIONALIZATIONS_VIEW;
        } else {
            return REDIRECT_ERROR_403;
        }
    }
    
    private ArrayList<UserDTO> getUserListDTO() {
        final List<User> users = userService.getAllActiveUsers();
        final ArrayList<UserDTO> userList = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            for (final Iterator<User> iterator = users.iterator(); iterator.hasNext();) {
                final User user = iterator.next();
                final UserDTO uDTO = new UserDTO();
                uDTO.setUserId(user.getUserId());
                uDTO.setFullName(user.getFullName());
                userList.add(uDTO);
            }
        }
        return userList;
    }
}
