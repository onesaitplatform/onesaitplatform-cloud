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
package com.minsait.onesait.platform.config.services.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.dto.OPResourceVO;
import com.minsait.onesait.platform.config.model.Tag;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.TagRepository;
import com.minsait.onesait.platform.config.services.exceptions.TagServiceException;

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
	public List<Tag> createTags(List<TagCreate> tags) throws TagServiceException {
		List<Tag> res = new ArrayList<Tag>();
		for(TagCreate t : tags) {
			final boolean exists = tagExists(t.getName());
			final OPResource r = opResourceRepository.findById(t.getResourceId()).orElse(null);
			if (r == null) {
				throw new TagServiceException("There is no resource with this id: " + t.getResourceId());
			} else {
				final OPResourceVO vo = new OPResourceVO();
				vo.setId(t.getResourceId());
				vo.setName(r.getIdentification());
				vo.setType(r.getClass().getSimpleName());
				if (exists) {
					final Tag tag = tagRepository.findByName(t.getName()).iterator().next();
					tag.getResources().add(vo);
					res.add(tagRepository.save(tag));
					} else {
						final Tag tag = new Tag();
						tag.setName(t.getName());
						tag.getResources().add(vo);
						res.add(tagRepository.save(tag));
					}
				}
			}
		return res;
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
	public void deleteByResourceIdsAndTag(String name, List asList) {
		List<Tag> tags = tagRepository.findByName(name);
		if (tags.size()>0){
			tagRepository.deleteByResourcesIdsAndTagId(tags.get(0).getId(), asList);
		}
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
	public void deleteResourceByResourceIdAndTagId(String resourceId, String tagId){
		tagRepository.deleteResourceByResourceIdAndTagId(tagId, List.of(resourceId));
	}
	
	@Override
	public void deleteResourcesByTagId(String tagId){
		tagRepository.deleteResourcesByTagId(tagId);
	}
	
	@Override
	public List<Tag> findResourceTagsByNameLike(String name) {
		List<Tag> taglist = tagRepository.findTagsLike(name);
		return 	taglist;
	}
	
	@Override
	public Tag findResourceTagsByName(String name) {
		Tag tag = tagRepository.findTag(name);
		return 	tag;
	}
	
}
