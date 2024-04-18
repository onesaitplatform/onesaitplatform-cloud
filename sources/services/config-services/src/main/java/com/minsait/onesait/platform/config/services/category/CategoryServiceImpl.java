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
package com.minsait.onesait.platform.config.services.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Category.Type;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.services.exceptions.CategoryServiceException;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryRelationRepository categoryRelationRepository;
	
	@Override
	public List<Category> getCategoriesByIdentificationAndDescription(String identification, String description) {

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		return categoryRepository.findByIdentificationLikeAndDescriptionLike(description,
				identification);
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
		final Category c = getCategoryByIdentification(category.getIdentification());
		if (c != null) {
			throw new CategoryServiceException("Category identification already exists");
		}	
		categoryRepository.save(category);
	}

	@Override
	public Category getCategoryToUpdate(String id) {
		return categoryRepository.findById(id);
	}

	@Override
	public void updateCategory(Category category) {
		final Category cat = getCategoryById(category.getId());
		category.setType(cat.getType());
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

	@Transactional
	@Override
	public void deleteCategory(String id) {
		Category category = categoryRepository.findById(id);
		if (category != null) {
			List<CategoryRelation> categoryRelations = categoryRelationRepository.findByCategory(id);
			categoryRelations.forEach(cr -> categoryRelationRepository.delete(cr));
			categoryRepository.delete(category);
		}
	}

	@Override
	public Category getCategoryByIdentification(String identification) {
		return categoryRepository.findByIdentification(identification);
	}
	
	@Override
	public List<Category> getCategoryByIdentificationLike(String identification) {
		return categoryRepository.findByIdentificationLike(identification);
	}
	
	@Override
	public List<Category> getCategoryByDescriptionLike(String description) {
		return categoryRepository.findByDescriptionLike(description);
	}
	
	@Override
	public List<Type> getCategoryTypeList() {
		return Arrays.asList(Category.Type.values());
	}

	@Override
	public List<Category> getCategoriesByTypeAndGeneralType(Type type) {
		final List<Type> types = Arrays.asList(type, Category.Type.GENERAL);
		return categoryRepository.findByTypeIn(types);
	}
	
	@Override
	public boolean isValidCategoryType(Type categoryType, Type elementType) {
		return (Arrays.asList(Category.Type.GENERAL, categoryType).indexOf(elementType) >= 0);
	}

	@Override
	public List<Category> getCategoriesByType(Type type) {
		return categoryRepository.findByType(type);
	}
}
