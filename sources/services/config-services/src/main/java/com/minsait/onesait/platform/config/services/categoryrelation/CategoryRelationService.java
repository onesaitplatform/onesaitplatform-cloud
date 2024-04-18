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
package com.minsait.onesait.platform.config.services.categoryrelation;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Category.Type;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Subcategory;

public interface CategoryRelationService {

	CategoryRelation getByTypeIdAndType(String typeId, Type type);

	CategoryRelation getByIdType(String typeId);

	void createCategoryRelation(String resourceId, Category category, Subcategory subcategory, Type type);
	
	void updateCategoryRelation(CategoryRelation categoryRelation, String resourceId, Category category,
			Subcategory subcategory);
	
	void deleteCategoryRelation(String resourceId);
	
}
