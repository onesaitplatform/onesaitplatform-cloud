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
package com.minsait.onesait.platform.config.services.gadgettemplate;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.GadgetTemplate;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateRepository;
import com.minsait.onesait.platform.config.repository.GadgetTemplateTypeRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.category.CategoryService;
import com.minsait.onesait.platform.config.services.categoryrelation.CategoryRelationService;
import com.minsait.onesait.platform.config.services.exceptions.CategoryServiceException;
import com.minsait.onesait.platform.config.services.exceptions.GadgetTemplateServiceException;
import com.minsait.onesait.platform.config.services.exceptions.SubcategoryServiceException;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateDTO;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateExportDto;
import com.minsait.onesait.platform.config.services.gadgettemplate.dto.GadgetTemplateImportResponsetDto;
import com.minsait.onesait.platform.config.services.subcategory.SubcategoryService;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GadgetTemplateServiceImpl implements GadgetTemplateService {

	@Autowired
	private GadgetTemplateRepository gadgetTemplateRepository;

	@Autowired
	private GadgetTemplateTypeRepository gadgetTemplateTypeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryRelationService categoryRelationService;
	
	@Autowired
	private CategoryRelationRepository categoryRelationRepository;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private SubcategoryService subcategoryService;

	public static final String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

	@Override
	public List<GadgetTemplate> findAllGadgetTemplates() {
		return gadgetTemplateRepository.findAll();
	}

	@Override
	public List<GadgetTemplate> findGadgetTemplateWithIdentificationAndDescription(String identification,
			String description, String userId) {
		List<GadgetTemplate> gadgetTemplates;
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			if (identification == null) {
				gadgetTemplates = gadgetTemplateRepository.findAll();
			} else {
				gadgetTemplates = gadgetTemplateRepository.findByIdentificationContaining(identification);
			}
		} else {
			if (identification == null) {
				gadgetTemplates = gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrue(user.getUserId());
			} else {
				gadgetTemplates = gadgetTemplateRepository
						.findGadgetTemplateByUserAndIsPublicTrueAndIdentificationLike(user.getUserId(), identification);
			}
		}

		return gadgetTemplates;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<GadgetTemplate> gadgetTemplates = gadgetTemplateRepository.findAllByOrderByIdentificationAsc();
		final List<String> names = new ArrayList<>();
		for (final GadgetTemplate gadgetTemplate : gadgetTemplates) {
			names.add(gadgetTemplate.getIdentification());
		}
		return names;
	}

	@Override
	public GadgetTemplate getGadgetTemplateById(String id) {
		return gadgetTemplateRepository.findById(id).orElse(null);
	}

	@Override
	public GadgetTemplate getGadgetTemplateByIdentification(String identification) {
		return gadgetTemplateRepository.findByIdentification(identification);
	}

	@Override
	public boolean hasUserPermission(String id, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (userService.isUserAdministrator(user)) {
			Log.info("user has permission");
			return true;
		} else {
			Log.info("user has not permission");
			return gadgetTemplateRepository.findById(id).orElse(null).getUser().getUserId().equals(userId);
		}
	}

	@Override
	public void updateGadgetTemplate(GadgetTemplateDTO dto) {
		GadgetTemplate gadgetTemplate = getUpdateGadgetTemplate(dto);
		gadgetTemplateRepository.save(gadgetTemplate);
		updateCategoryRelation(dto, gadgetTemplate.getId());
	}

	@Transactional
	@Override
	public void createGadgetTemplate(GadgetTemplateDTO dto) {
		GadgetTemplate gadgetTemplate = getNewGadgetTemplate(dto, dto.getUser().getUserId());
		try {
			gadgetTemplateRepository.save(gadgetTemplate);
		} catch (final Exception e) {
			throw new GadgetTemplateServiceException("Can not save gadgetTemplate");
		}
		createCategoryRelation(dto, gadgetTemplate.getId());
	}

	@Transactional
	@Override
	public void deleteGadgetTemplate(String id, String userId) {
		if (hasUserPermission(id, userId)) {
			final GadgetTemplate gadgetTemplate = gadgetTemplateRepository.findById(id).orElse(null);
			if (gadgetTemplate != null) {
				categoryRelationService.deleteCategoryRelation(gadgetTemplate.getId());
				gadgetTemplateRepository.delete(gadgetTemplate);
			} else
				throw new GadgetTemplateServiceException("Can not delete gadgetTemplate that does not exist");
		}

	}

	@Override
	public List<GadgetTemplate> getUserGadgetTemplate(String userId) {
		return gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrue(userId);
	}

	@Override
	public List<GadgetTemplate> getUserGadgetTemplate(String userId, String type) {
		final User user = userRepository.findByUserId(userId);

		if (user.getRole().getId().equals(ADMINISTRATOR)) {
			return this.gadgetTemplateRepository.findByType(type);
		} else {
			return this.gadgetTemplateRepository.findGadgetTemplateByUserAndIsPublicTrueAndType(userId, type);
		}
	}

	@Override
	public GadgetTemplate getGadgetTemplateByIdentification(String identification, String userId) {
		final User user = userRepository.findByUserId(userId);
		GadgetTemplate gadgetTemplate;
		if (userService.isUserAdministrator(user)) {
			gadgetTemplate = gadgetTemplateRepository.findByIdentification(identification);
		} else {
			gadgetTemplate = gadgetTemplateRepository
					.findGadgetTemplateByUserAndIsPublicTrueAndIdentification(user.getUserId(), identification);
		}
		return gadgetTemplate;
	}

	@Override
	public List<GadgetTemplateType> getTemplateTypes() {
		return gadgetTemplateTypeRepository.findAll();
	}

	@Override
	public GadgetTemplateType getTemplateTypeById(String id) {
		return gadgetTemplateTypeRepository.findById(id).orElse(null);
	}

	@Override
	public GadgetTemplateExportDto exportGradgetTemplate(String identification, String userId) {
		final GadgetTemplate gadgetTemplate = getGadgetTemplateByIdentification(identification);
		final GadgetTemplateDTO dto = getGadgetTemplateDTOByIdentification(identification);
		if (gadgetTemplate == null || !hasUserPermission(gadgetTemplate, userId)) {
			throw new GadgetTemplateServiceException(
					"Gadget Template " + identification + " NOT FOUND or unauthorized");
		}

		final GadgetTemplateExportDto gadgetTemplateDTO = GadgetTemplateExportDto.builder().id(dto.getId())
				.identification(dto.getIdentification()).user(dto.getUser().getUserId())
				.headerlibs(dto.getHeaderlibs()).createdAt(gadgetTemplate.getCreatedAt())
				.description(dto.getDescription()).modifiedAt(gadgetTemplate.getUpdatedAt())
				.type(dto.getType()).isPublic(dto.isPublic())
				.template(dto.getTemplate()).templateJS(dto.getTemplateJS())
				.category(dto.getCategory()).subcategory(dto.getSubcategory()).config(dto.getConfig()).build();

		return gadgetTemplateDTO;
	}

	private boolean hasUserPermission(GadgetTemplate gadgetTemplate, String userId) {
		final User user = userRepository.findByUserId(userId);
		if (user.isAdmin() || user.equals(gadgetTemplate.getUser())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<GadgetTemplateExportDto> exportGradgetTemplateByUser(String userId) {
		final List<GadgetTemplate> gadgetTemplates = getUserGadgetTemplate(userId);
		List<GadgetTemplateExportDto> dtos = new ArrayList<>();

		for (GadgetTemplate gadgetTemplate : gadgetTemplates) {
			dtos.add(GadgetTemplateExportDto.builder().id(gadgetTemplate.getId())
					.identification(gadgetTemplate.getIdentification()).user(gadgetTemplate.getUser().getUserId())
					.headerlibs(gadgetTemplate.getHeaderlibs()).createdAt(gadgetTemplate.getCreatedAt())
					.description(gadgetTemplate.getDescription()).modifiedAt(gadgetTemplate.getUpdatedAt())
					.type(gadgetTemplate.getType()).isPublic(gadgetTemplate.isPublic())
					.template(gadgetTemplate.getTemplate()).templateJS(gadgetTemplate.getTemplateJS()).build());
		}
		return dtos;
	}

	@Override
	public List<GadgetTemplateImportResponsetDto> importGradgetTemplateByUser(String userId,
			List<GadgetTemplateExportDto> gadgetTemplates, Boolean override) {
		List<GadgetTemplateImportResponsetDto> responses = new ArrayList<>();
		for (GadgetTemplateExportDto gadgetTemplateDTO : gadgetTemplates) {
			GadgetTemplate gadgetTemplateDb = gadgetTemplateRepository
					.findByIdentification(gadgetTemplateDTO.getIdentification());
			GadgetTemplateDTO dto = getGadgetTemplateDTOByIdentification(gadgetTemplateDTO.getIdentification());
			if (dto != null && !override) {
				Log.warn("GadgetTemplate {} not override.", gadgetTemplateDTO.getIdentification());
			} else {

				if (dto == null) {
					GadgetTemplateDTO gadgetTemplate = toGadgetTemplate(gadgetTemplateDTO);
					if (hasUserPermission(gadgetTemplate.getId(), userId)) {

						createGadgetTemplate(dto);
						responses.add(GadgetTemplateImportResponsetDto.builder()
								.identification(gadgetTemplate.getIdentification()).id(gadgetTemplate.getId()).build());
					}
				} else if (dto != null && override) {
					if (hasUserPermission(gadgetTemplateDb, userId)) {
						copyProperties(dto, gadgetTemplateDTO);
						updateGadgetTemplate(dto);
						responses.add(GadgetTemplateImportResponsetDto.builder()
								.identification(dto.getIdentification()).id(dto.getId())
								.build());
					}
				}
			}
		}

		return responses;
	}

	private GadgetTemplateDTO toGadgetTemplate(GadgetTemplateExportDto dto) {

		GadgetTemplateDTO template = new GadgetTemplateDTO();
		template.setIdentification(dto.getIdentification());
		template.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
		template.setType(dto.getType() == null ? "angularJS" : dto.getType());
		template.setPublic(dto.isPublic());
		template.setTemplate(dto.getTemplate());
		template.setTemplateJS(dto.getTemplateJS());
		template.setUser(userRepository.findByUserId(dto.getUser()));
		template.setCategory(dto.getCategory());
		template.setSubcategory(dto.getSubcategory());
		return template;

	}

	private void copyProperties(GadgetTemplateDTO template, GadgetTemplateExportDto dto) {
		if (dto.getDescription() == null) {
			template.setDescription("");
		} else {
			template.setDescription(dto.getDescription());
		}
		template.setPublic(dto.isPublic());
		template.setTemplate(dto.getTemplate());
		template.setTemplateJS(dto.getTemplateJS());
		template.setCategory(dto.getCategory());
		template.setSubcategory(dto.getSubcategory());
	}

	private void createCategoryRelation(GadgetTemplateDTO dto, String id) {
		if (!StringUtils.isEmpty(dto.getCategory()) && id != null) {
			final Category category = categoryService.getCategoryByIdentification(dto.getCategory());
			if (category == null) {
				throw new CategoryServiceException("Category does not exist");
			}
			if (!categoryService.isValidCategoryType(Category.Type.GADGET, category.getType())) {
				throw new CategoryServiceException("Type of category is not for this element");
			}
			Subcategory subcategory = new Subcategory();
			if (!StringUtils.isEmpty(dto.getSubcategory())) {
				subcategory = subcategoryService.getSubcategoryByIdentificationAndCategory(dto.getSubcategory(), category);
				if (subcategory == null) {
					throw new SubcategoryServiceException("Subcategory does not exist");
				}
			}
			categoryRelationService.createCategoryRelation(id, category, subcategory, Category.Type.GADGET);			
		}
	}
	
	private GadgetTemplate getNewGadgetTemplate(GadgetTemplateDTO dto, String userId) {
		GadgetTemplate gt = new GadgetTemplate();
		gt.setUser(userService.getUser(userId));
		gt.setIdentification(dto.getIdentification());
		gt.setDescription(dto.getDescription());
		gt.setPublic(dto.isPublic());
		gt.setType(dto.getType());
		gt.setTemplate(dto.getTemplate());
		gt.setTemplateJS(dto.getTemplateJS());
		gt.setHeaderlibs(dto.getHeaderlibs());
		gt.setConfig(dto.getConfig());
		return gt;
	}
	
	private void updateCategoryRelation(GadgetTemplateDTO dto, String id) {
		final CategoryRelation categoryRelation = categoryRelationService.getByIdType(id);
		if (!StringUtils.isEmpty(dto.getCategory())) {

			final Category category = categoryService.getCategoryByIdentification(dto.getCategory());
			if (category == null) {
				throw new CategoryServiceException("Category does not exist");
			}
			if (!categoryService.isValidCategoryType(Category.Type.GADGET, category.getType())) {
				throw new CategoryServiceException("Type of category is not for this element");
			}
			Subcategory subcategory = new Subcategory();
			if (!StringUtils.isEmpty(dto.getSubcategory())) {
				subcategory = subcategoryService.getSubcategoryByIdentificationAndCategory(dto.getSubcategory(), category);
				if (subcategory == null) {
					throw new SubcategoryServiceException("Subcategory does not exist");
				}
			}
			if (categoryRelation == null) {
				categoryRelationService.createCategoryRelation(id, category, subcategory, Category.Type.GADGET);
			} else {
				categoryRelationService.updateCategoryRelation(categoryRelation, id, category, subcategory);
			}
		} else if (categoryRelation != null) {
			categoryRelationService.deleteCategoryRelation(id);
		}
	}
	
	private GadgetTemplate getUpdateGadgetTemplate(GadgetTemplateDTO dto) {
		GadgetTemplate gt = this.getGadgetTemplateById(dto.getId());
		gt.setDescription(dto.getDescription());
		gt.setPublic(dto.isPublic());
		gt.setType(dto.getType());
		gt.setTemplate(dto.getTemplate());
		gt.setTemplateJS(dto.getTemplateJS());
		gt.setHeaderlibs(dto.getHeaderlibs());
		gt.setConfig(dto.getConfig());
		return gt;
	}
	
	@Override
	public GadgetTemplateDTO getGadgetTemplateDTOById(String id) {
		final GadgetTemplate gadgetTemplate = this.getGadgetTemplateById(id);
		return getGadgetTemplateDTO(gadgetTemplate);
	}
	
	@Override
	public GadgetTemplateDTO getGadgetTemplateDTOByIdentification(String identification) {
		final GadgetTemplate gadgetTemplate = this.getGadgetTemplateByIdentification(identification);
		return getGadgetTemplateDTO(gadgetTemplate);
	}

	private GadgetTemplateDTO getGadgetTemplateDTO(GadgetTemplate gt) {
		GadgetTemplateDTO gadgetTemplate = new GadgetTemplateDTO();
		gadgetTemplate.setId(gt.getId());
		gadgetTemplate.setIdentification(gt.getIdentification());
		gadgetTemplate.setDescription(gt.getDescription());
		gadgetTemplate.setType(gt.getType());
		gadgetTemplate.setPublic(gt.isPublic());
		gadgetTemplate.setTemplate(gt.getTemplate());
		gadgetTemplate.setTemplateJS(gt.getTemplateJS());
		gadgetTemplate.setHeaderlibs(gt.getHeaderlibs());
		gadgetTemplate.setUser(gt.getUser());
		gadgetTemplate.setConfig(gt.getConfig());
		
		final CategoryRelation cr = categoryRelationService.getByIdType(gt.getId());
		if (cr != null) {
			final Category c = categoryService.getCategoryById(cr.getCategory());
			if (c != null)
				gadgetTemplate.setCategory(c.getIdentification());
			final Subcategory s = subcategoryService.getSubcategoryById(cr.getSubcategory());
			if (s != null)
				gadgetTemplate.setSubcategory(s.getIdentification());
		}
		return gadgetTemplate;
	}

    @Override
    public String cloneGadgetTemplate(GadgetTemplate gadgetTemplate, String identification, User user) {
        final GadgetTemplate cloneGadgetTemplate = new GadgetTemplate();
        
        try {
            cloneGadgetTemplate.setIdentification(identification);
            cloneGadgetTemplate.setUser(user);
            cloneGadgetTemplate.setConfig(gadgetTemplate.getConfig());
            cloneGadgetTemplate.setDescription(gadgetTemplate.getDescription());
            cloneGadgetTemplate.setPublic(gadgetTemplate.isPublic());
            cloneGadgetTemplate.setType(gadgetTemplate.getType());
            cloneGadgetTemplate.setHeaderlibs(gadgetTemplate.getHeaderlibs());
            cloneGadgetTemplate.setTemplate(gadgetTemplate.getTemplate());
            cloneGadgetTemplate.setTemplateJS(gadgetTemplate.getTemplateJS());
    
            gadgetTemplateRepository.save(cloneGadgetTemplate);

            CategoryRelation cr = categoryRelationRepository.findByTypeId(gadgetTemplate.getId());
            if(cr != null) {
                CategoryRelation cloneCR = new CategoryRelation();
                cloneCR.setTypeId(cloneGadgetTemplate.getId());
                cloneCR.setCategory(cr.getCategory());
                cloneCR.setSubcategory(cr.getSubcategory());
                cloneCR.setType(cr.getType());
                categoryRelationRepository.save(cloneCR);
            }
            return cloneGadgetTemplate.getId();
        } catch (final Exception e) {
    
            log.error(e.getMessage());
            return null;
        }
    }
	
}
