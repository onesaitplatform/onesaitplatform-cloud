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
package com.minsait.onesait.platform.controlpanel.controller.categories;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.exceptions.CategoryServiceException;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/categories")
@Slf4j
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
public class CategoryController {

	@Autowired
	private CategoryService categoryConfigService;

	@Autowired
	private SubcategoryService subcategoryConfigService;

	@Autowired
	private AppWebUtils utils;

	private static final String CATEGORY_STR = "category";
	private static final String CAT_VAL_ERROR = "category.validation.error";
	private static final String REDIRECT_CAT_CREATE = "redirect:/categories/create";
	private static final String REDIRECT_CAT_LIST = "redirect:/categories/list";

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		final List<Category> categories = categoryConfigService.findAllCategories();
		model.addAttribute("categories", categories);
		return "categories/list";
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return categoryConfigService.getAllIdentifications();
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute(CATEGORY_STR, new Category());
		return "categories/create";
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		Category category = categoryConfigService.getCategoryToUpdate(id);
		model.addAttribute(CATEGORY_STR, category);
		return "categories/create";
	}

	@PostMapping(value = "/create")
	public String createCategory(Model model, @Valid Category category, BindingResult bindingResult,
			RedirectAttributes redirect, HttpServletRequest request) {

		if (bindingResult.hasErrors()) {
			log.debug("Some categories properties missing");
			utils.addRedirectMessage(CAT_VAL_ERROR, redirect);
			return REDIRECT_CAT_CREATE;
		}

		try {
			categoryConfigService.createCategory(category);
			return REDIRECT_CAT_LIST;
		} catch (final Exception e) {
			log.error("Generic internal error creating category: " + e.getMessage());
			utils.addRedirectMessage(CAT_VAL_ERROR, redirect);
			return REDIRECT_CAT_CREATE;

		}
	}

	@PutMapping(value = "/update/{id}")
	public String updateCategory(Model model, @PathVariable("id") String id, @Valid Category category,
			BindingResult bindingResult, RedirectAttributes redirect, HttpServletRequest request) {

		if (bindingResult.hasErrors()) {
			log.debug("Some categories properties missing");
			utils.addRedirectMessage(CAT_VAL_ERROR, redirect);
			return "redirect:/categories/update/" + id;

		}

		try {
			categoryConfigService.updateCategory(category);
			return "redirect:/categories/show/" + id;

		} catch (Exception e) {
			log.error("Cannot update category {}", e.getMessage());
			utils.addRedirectMessage("category.update.error", redirect);
			return REDIRECT_CAT_CREATE;

		}
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final Category category = categoryConfigService.getCategoryById(id);
			if (category != null) {

				final List<Subcategory> subcategories = subcategoryConfigService.findSubcategoriesByCategory(category);
				model.addAttribute("subcategories", subcategories);

				model.addAttribute(CATEGORY_STR, category);
				return "categories/show";

			} else {
				utils.addRedirectMessage("category.notfound.error", redirect);
				return REDIRECT_CAT_LIST;
			}
		} catch (final CategoryServiceException e) {
			return REDIRECT_CAT_LIST;
		}
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		final Category category = categoryConfigService.getCategoryById(id);
		if (category != null) {
			try {
				categoryConfigService.deleteCategory(id);
			} catch (final Exception e) {
				utils.addRedirectMessageWithParam("category.delete.error", e.getMessage(), redirect);
				log.error("Error deleting category. ", e);
				return "redirect:/categories/show/" + id;
			}
			return REDIRECT_CAT_LIST;
		} else {
			return REDIRECT_CAT_LIST;
		}
	}

	@GetMapping("/addSubcategory/{id}")
	public String addSubcategory(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			final Category category = categoryConfigService.getCategoryById(id);
			if (category != null) {

				model.addAttribute(CATEGORY_STR, category);
				return "redirect:/subcategories/create/" + id;

			} else {
				utils.addRedirectMessage("category.notfound.error", redirect);
				return REDIRECT_CAT_LIST;
			}
		} catch (final CategoryServiceException e) {
			return REDIRECT_CAT_LIST;
		}
	}

}
