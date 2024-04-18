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
package com.minsait.onesait.platform.config.services.tag;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.dto.OPResourceVO;
import com.minsait.onesait.platform.config.dto.ResourceTagVO;
import com.minsait.onesait.platform.config.model.Tag;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.TagRepository;
import com.minsait.onesait.platform.config.services.exceptions.ApiManagerServiceException;
import com.minsait.onesait.platform.config.services.exceptions.TagServiceException;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TagServiceImpl implements TagService {

	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private OPResourceRepository opResourceRepository;

	@Override
	public List<Tag> getAllTags() {
		return tagRepository.findAll();
	}

	@Override
	public boolean tagExists(String name) {
		final String id = tagRepository.findIdByTagName(name);
		return id != null;
	}

	@Override
	public void createTags(List<TagCreate> tags) {
		tags.forEach(t -> {
			final boolean exists = tagExists(t.getName());
			final OPResource r = opResourceRepository.findById(t.getResourceId()).orElse(null);
			if (r == null) {
				log.warn("Resource with id {} does not exist", t.getResourceId());
			} else {
				final OPResourceVO vo = new OPResourceVO();
				vo.setId(t.getResourceId());
				vo.setName(r.getIdentification());
				vo.setType(r.getClass().getSimpleName());
				if (exists) {
					final Tag tag = tagRepository.findByName(t.getName()).iterator().next();
					tag.getResources().add(vo);
					tagRepository.save(tag);
				} else {
					final Tag tag = new Tag();
					tag.setName(t.getName());
					tag.getResources().add(vo);
					tagRepository.save(tag);
				}
			}
		});
	}

	@Override
	public List<String> getTagNames() {
		return tagRepository.findTagNames();
	}

	@Override
	public void deleteTag(String name) {
		tagRepository.deleteByName(name);
	}

	@Override
	public void deleteByResourceIds(List<String> ids) {
		tagRepository.deleteByResourceId(ids);

	}

	@Override
	public void createTag(String name) {
		final String[] ts = name.split(",");
		for (final String tn : ts) {
			final boolean exists = tagExists(tn);
			if (exists) {
				throw new TagServiceException("El tag ya existe");
			}else {
				final Tag t = new Tag();
				t.setName(tn);
				tagRepository.save(t);	
			}
		}
	}
	
	@Override
	public void deleteByResourceIdAndTagId(String resourceId, String tagId){
		tagRepository.deleteByResourceIdAndTagId(tagId, List.of(resourceId));
	}
	
	@Override
	public void deleteByTagId(String tagId){
		tagRepository.deleteByTagId(tagId);
	}
	
	@Override
	public List<ResourceTagVO> findResourceTagsByName(String name) {
		return tagRepository.findResourceTagsVO(name);		
	}
	
}
