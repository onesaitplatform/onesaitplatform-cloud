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
package com.minsait.onesait.platform.config.services.category;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<Category> getCategoriesByIdentificationAndDescription(String identification, String description) {

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		return categoryRepository.findByIdentificationLikeAndDescriptionLike(identification,
				description);
	}

	@Override
	public List<String> getAllIdentifications() {
		List<Category> categories = categoryRepository.findAllByOrderByIdentificationAsc();

		final List<String> identifications = new ArrayList<>();
		for (final Category category : categories) {
			identifications.add(category.getIdentification());

		}
		return identifications;
	}

	@Override
	public void createCategory(Category category) {
		categoryRepository.save(category);
	}

	@Override
	public Category getCategoryToUpdate(String id) {

		return categoryRepository.findById(id);
	}

	@Override
	public void updateCategory(Category category) {
		categoryRepository.save(category);
	}

	@Override
	public Category getCategoryById(String id) {

		return categoryRepository.findById(id);
	}

	@Override
	public List<Category> findAllCategories() {

		return categoryRepository.findAll();
	}

	@Override
	public void deleteCategory(String id) {
		Category category = categoryRepository.findById(id);
		if (category != null) {
			categoryRepository.delete(category);
		}
	}

	@Override
	public Category getCategoryByIdentification(String identification) {
		return categoryRepository.findByIdentification(identification).get(0);
	}

}
