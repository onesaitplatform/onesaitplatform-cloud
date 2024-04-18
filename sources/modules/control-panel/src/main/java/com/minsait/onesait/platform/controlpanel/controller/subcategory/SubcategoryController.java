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
package com.minsait.onesait.platform.controlpanel.controller.subcategory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.services.exceptions.SubcategoryServiceException;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/subcategories")
@Slf4j
public class SubcategoryController {

	@Autowired
	private SubcategoryService subcategoryConfigService;

	@Autowired
	private AppWebUtils utils;

	private static final String SUBCAT_STR = "subcategory";
	private static final String SUBCAT_VAL_ERROR = "subcategory.validation.error";
	private static final String REDIRECT_CAT_SHOW = "redirect:/categories/show/";
	private static final String REDIRECT_CAT_LIST = "redirect:/categories/list";

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return subcategoryConfigService.getAllIdentifications();
	}

	@GetMapping(value = "/create/{idCategory}")
	public String create(Model model, @PathVariable("idCategory") String idCategory) {

		Subcategory subcategory = new Subcategory();
		model.addAttribute(SUBCAT_STR, subcategory);
		model.addAttribute("category", idCategory);
		return "subcategories/create";
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		Subcategory subcategory = subcategoryConfigService.getSubcategoryToUpdate(id);
		model.addAttribute(SUBCAT_STR, subcategory);
		return "subcategories/create";
	}

	@PostMapping(value = "/create/{idCategory}")
	public String createSubcategory(Model model, @Valid Subcategory subcategory, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request, @PathVariable("idCategory") String idCategory) {

		if (bindingResult.hasErrors()) {
			log.debug("Some subcategories properties missing");
			utils.addRedirectMessage(SUBCAT_VAL_ERROR, redirect);
			return REDIRECT_CAT_SHOW + idCategory;
		}

		try {
			subcategoryConfigService.createSubcategory(subcategory, idCategory);
			return REDIRECT_CAT_SHOW + idCategory;
		} catch (final Exception e) {
			log.error("Generic internal error creating subcategory: " + e.getMessage());
			utils.addRedirectMessage(SUBCAT_VAL_ERROR, redirect);
			return REDIRECT_CAT_SHOW + idCategory;

		}
	}

	@PutMapping(value = "/update/{id}")
	public String updateSubcategory(Model model, @PathVariable("id") String id, @Valid Subcategory subcategory,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {

		if (bindingResult.hasErrors()) {
			log.debug("Some subsubcategories properties missing");
			utils.addRedirectMessage(SUBCAT_VAL_ERROR, redirect);
			return "redirect:/subcategories/update/" + id;

		}

		try {
			Category category = subcategoryConfigService.getSubcategoryById(id).getCategory();
			subcategory.setCategory(category);
			subcategoryConfigService.updateSubcategory(subcategory);
			return "redirect:/subcategories/show/" + id;

		} catch (Exception e) {
			log.error("Cannot update subcategory {}", e.getMessage());
			utils.addRedirectMessage("subcategory.update.error", redirect);
			return REDIRECT_CAT_LIST;

		}
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final Subcategory subcategory = subcategoryConfigService.getSubcategoryById(id);
			if (subcategory != null) {

				model.addAttribute(SUBCAT_STR, subcategory);
				return "subcategories/show";

			} else {
				utils.addRedirectMessage("subcategory.notfound.error", redirect);
				return REDIRECT_CAT_LIST;
			}
		} catch (final SubcategoryServiceException e) {
			return REDIRECT_CAT_LIST;
		}
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final Subcategory subcategory = subcategoryConfigService.getSubcategoryById(id);
		if (subcategory != null) {
			try {
				subcategoryConfigService.deleteSubcategory(id);
			} catch (final Exception e) {
				utils.addRedirectMessageWithParam("subcategory.delete.error", e.getMessage(), redirect);
				log.error("Error deleting subcategory. ", e);
				return "redirect:/subcategories/show/" + id;
			}
			return REDIRECT_CAT_LIST;
		} else {
			return REDIRECT_CAT_LIST;
		}
	}

}
