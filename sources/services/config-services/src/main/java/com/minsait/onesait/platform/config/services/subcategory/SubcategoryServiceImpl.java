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
package com.minsait.onesait.platform.config.services.subcategory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;

@Service
public class SubcategoryServiceImpl implements SubcategoryService {

	@Autowired
	private SubcategoryRepository subcategoryRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<Subcategory> getCategoriesByIdentificationAndDescription(String identification, String description) {

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		return subcategoryRepository
				.findByIdentificationLikeAndDescriptionLike(identification, description);
	}

	@Override
	public List<String> getAllIdentifications() {
		List<Subcategory> subcategories = subcategoryRepository.findAllByOrderByIdentificationAsc();

		final List<String> identifications = new ArrayList<>();
		for (final Subcategory subcategory : subcategories) {
			identifications.add(subcategory.getIdentification());

		}
		return identifications;
	}

	@Override
	public void createSubcategory(Subcategory subcategory, String categoryId) {
		Category category = categoryRepository.findById(categoryId);
		subcategory.setCategory(category);
		subcategoryRepository.save(subcategory);
	}

	@Override
	public Subcategory getSubcategoryToUpdate(String id) {

		return subcategoryRepository.findById(id);
	}

	@Override
	public void updateSubcategory(Subcategory subcategory) {
		subcategoryRepository.save(subcategory);
	}

	@Override
	public Subcategory getSubcategoryById(String id) {

		return subcategoryRepository.findById(id);
	}

	@Override
	public List<Subcategory> findAllSubcategories() {

		return subcategoryRepository.findAll();
	}

	@Override
	public void deleteSubcategory(String id) {
		Subcategory category = subcategoryRepository.findById(id);
		if (category != null) {
			subcategoryRepository.delete(category);
		}
	}

	@Override
	public List<Subcategory> findSubcategoriesByCategory(Category category) {

		return subcategoryRepository.findByCategory(category);
	}

	@Override
	public List<String> findSubcategoriesNamesByCategory(Category category) {
		return subcategoryRepository.findIdentificationsByCategory(category);
	}

	@Override
	public Subcategory getSubcategoryByIdentificationAndCategory(String identification, Category category) {
		return subcategoryRepository.findByIdentificationAndCategory(identification, category);
	}

}
