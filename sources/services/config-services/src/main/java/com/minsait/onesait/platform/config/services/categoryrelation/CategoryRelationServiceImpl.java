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
package com.minsait.onesait.platform.config.services.categoryrelation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.Category.Type;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;

@Service
public class CategoryRelationServiceImpl implements CategoryRelationService {

	@Autowired
	private CategoryRelationRepository categoryRelationRepository;

	@Override
	public CategoryRelation getByTypeIdAndType(String typeId, Type type) {
		return categoryRelationRepository.findByTypeIdAndType(typeId, type);
	}
	
	@Override
	public CategoryRelation getByIdType(String typeId) {
	    return categoryRelationRepository.findByTypeId(typeId);
	}

	@Override
	public void createCategoryRelation(String resourceId, Category category, Subcategory subcategory, Type type) {
		final CategoryRelation categoryRelation = new CategoryRelation();
		categoryRelation.setCategory(category.getId());
		categoryRelation.setSubcategory(subcategory.getId());
		categoryRelation.setType(type);
		categoryRelation.setTypeId(resourceId);

		categoryRelationRepository.save(categoryRelation);
	}

	@Override
	public void updateCategoryRelation(CategoryRelation categoryRelation, String resourceId, Category category, Subcategory subcategory) {
		categoryRelation.setCategory(category.getId());
		categoryRelation.setSubcategory(subcategory.getId());
		categoryRelation.setTypeId(resourceId);

		categoryRelationRepository.save(categoryRelation);
	}
	
	@Override
	public void deleteCategoryRelation(String resourceId) {
		final CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(resourceId);
		if (categoryRelation != null) {
			categoryRelationRepository.delete(categoryRelation);
		}
	}
	
}
