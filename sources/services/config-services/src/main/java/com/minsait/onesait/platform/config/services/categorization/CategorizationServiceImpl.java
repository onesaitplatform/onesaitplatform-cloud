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
package com.minsait.onesait.platform.config.services.categorization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Categorization;
import com.minsait.onesait.platform.config.model.CategorizationUser;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategorizationRepository;
import com.minsait.onesait.platform.config.repository.CategorizationUserRepository;
import com.minsait.onesait.platform.config.services.categorization.user.CategorizationUserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategorizationServiceImpl implements CategorizationService {

	@Autowired
	private CategorizationRepository categorizationRepository;

	@Autowired
	private CategorizationUserService categorizationUserService;

	@Autowired
	private CategorizationUserRepository categorizationUserRepository;

	private static final String PARENT_ATTRIB = "parent";
	private static final String TEXT_ATTRIB = "text";
	private static final String URL_SEPARATOR = "/";

	@Override
	public void createCategorization(String name, String json, User user) {
		final Categorization categorization = new Categorization();
		final CategorizationUser categorizationUser = new CategorizationUser();

		try {
			categorization.setIdentification(name);
			categorization.setJson(json);
			categorization.setUser(user);

			categorizationRepository.save(categorization);

			categorizationUser.setCategorization(categorizationRepository.findByIdentification(name));
			categorizationUser.setUser(user);
			categorizationUser.setActive(false);
			categorizationUser.setAuthorizationTypeEnum(CategorizationUser.Type.OWNER);

			categorizationUserRepository.save(categorizationUser);
		} catch (final Exception e) {
			log.error("Error creating a new Categorization: " + e.getMessage());
		}
	}

	@Override
	public void updateCategorization(String id, String json) {
		categorizationRepository.findById(id).ifPresent(c -> updateCategorization(c, json));

	}

	@Override
	public void updateCategorization(Categorization categorizationMemory, String json) {

		try {
			categorizationMemory.setJson(json);
			categorizationRepository.save(categorizationMemory);
		} catch (final Exception e) {
			log.error("Error editing the theme: " + e.getMessage());
		}

	}

	@Override
	public void deteleConfiguration(String id) {
		categorizationRepository.deleteById(id);
	}

	@Override
	public void activateByCategoryAndUser(String categorizationIdentification, User user) {
		final Categorization categorization = getCategorizationByIdentification(categorizationIdentification);
		final CategorizationUser categorizationUser = categorizationUserService
				.findByCategorizationAndUser(categorization, user);

		setActive(categorizationUser.getId(), user);
	}

	@Override
	public void deactivateByCategoryAndUser(String categorizationIdentification, User user) {
		final Categorization categorization = getCategorizationByIdentification(categorizationIdentification);

		deactivate(categorization.getId(), user);
	}

	@Override
	public void setActive(String categorizationId, User user) {
		final List<CategorizationUser> actives = categorizationUserRepository.findByUserAndActive(user);
		for (final CategorizationUser categorization : actives) {
			categorization.setActive(false);
			categorizationUserRepository.save(categorization);
		}
		
		final Categorization categorization = getCategorizationById(categorizationId);
		CategorizationUser categorizationUser = categorizationUserRepository.findByUserAndCategorization(user, categorization);
		
		if (categorizationUser!=null) {
			categorizationUser.setActive(true);
			categorizationUserRepository.save(categorizationUser);
		} else {
			CategorizationUser categorizationUserNew = new CategorizationUser();
			categorizationUserNew.setUser(user);
			categorizationUserNew.setCategorization(categorization);
			categorizationUserNew.setActive(true);
			categorizationUserNew.setAuthorizationType("GUEST");
			categorizationUserRepository.save(categorizationUserNew);
		}

	}

	@Override
	public void deactivate(String categorizationId, User user) {
		try {
			final Categorization categorization = getCategorizationById(categorizationId);
			CategorizationUser categorizationUser = categorizationUserRepository.findByUserAndCategorization(user, categorization);
			
			if (categorizationUser!=null) {
				categorizationUser.setActive(false);
				categorizationUserRepository.save(categorizationUser);
			}

		} catch (final Exception e) {
			log.error("Error setting to inactive: " + e.getMessage());
		}
	}

	@Override
	public void addAuthorization(Categorization categorization, User user, String accessType) {
		final CategorizationUser categorizationUser = new CategorizationUser();
		CategorizationUser categorizationuserBD = categorizationUserRepository.findByUserAndCategorization(user, categorization);
		if (categorizationuserBD==null) {
			categorizationUser.setCategorization(categorization);
			categorizationUser.setUser(user);
			categorizationUser.setAuthorizationType(accessType);
			categorizationUser.setActive(false);
	
			categorizationUserRepository.save(categorizationUser);
		}
	}

	@Override
	public boolean hasUserPermission(User user, Categorization categorization) {
		return (categorizationUserRepository.findByUserAndCategorization(user, categorization) != null);
	}

	@Override
	public Categorization getCategorizationById(String id) {
		return categorizationRepository.findById(id).orElse(null);
	}

	@Override
	public Categorization getCategorizationByIdentification(String identification) {
		return (categorizationRepository.findByIdentification(identification));
	}

	@Override
	public List<Categorization> getAllCategorizations() {
		return categorizationRepository.findAll();
	}

	@Override
	public List<Categorization> getCategorizationsByUser(User user) {
		return categorizationRepository.findByUser(user);
	}

	@Override
	public List<Categorization> getActiveCategorizations(User user) {
		final List<CategorizationUser> actives = categorizationUserRepository.findByUserAndActive(user);
		final List<Categorization> categorizations = new ArrayList<>();
		for (final CategorizationUser categorizationUser : actives) {
			categorizations.add(categorizationUser.getCategorization());
		}
		return categorizations;
	}

	@Override
	public JSONArray getNodesCategory(String categorizationJson, String pathCategorization) throws IOException {

		final JSONArray categorizationTree = new JSONArray(categorizationJson);
		final JSONArray resultNodes = new JSONArray();

		final String[] categories = pathCategorization.split(URL_SEPARATOR);
		final String category = categories[categories.length - 1];

		for (int i = 0; i < categorizationTree.length(); ++i) {
			final JSONObject categorizationObj = categorizationTree.getJSONObject(i);
			final String categoryName = categorizationObj.getString(TEXT_ATTRIB);
			if (categoryName.equals(category) && isValidPath(categorizationJson, category, pathCategorization)) {
				addChildNodes(categorizationTree, categorizationObj.get("id").toString(), resultNodes);
			}
		}
		return resultNodes;
	}

	private void addChildNodes(JSONArray categorizationTree, String category, JSONArray resultNodes) {
		for (int i = 0; i < categorizationTree.length(); ++i) {
			final JSONObject categorizationObj = categorizationTree.getJSONObject(i);
			if (categorizationObj.getString(PARENT_ATTRIB).equals(category)) {
				final JSONObject outputNode = new JSONObject();
				outputNode.put("name", categorizationObj.getString(TEXT_ATTRIB));

				if (categorizationObj.getJSONObject("a_attr").has("elementType")) {
					outputNode.put("type", categorizationObj.getJSONObject("a_attr").getString("elementType"));
				}

				resultNodes.put(outputNode);
			}
		}
	}

	private boolean isValidPath(String categorizationJson, String category, String pathCategorization) {
		try {
			if (!pathCategorization.startsWith(URL_SEPARATOR)) {
				pathCategorization = URL_SEPARATOR + pathCategorization;
			}
			final JSONArray categoriesArray = getCategoryNode(categorizationJson, category);
			final String path = categoriesArray.getJSONObject(0).get("path") + URL_SEPARATOR + category;

			return path.equals(pathCategorization);
		} catch (final IOException e) {
			log.error("Error obtaining nodes " + e.getMessage());
			return false;
		}
	}

	@Override
	public JSONArray getCategoryNode(String categorizationJson, String nodeId) throws IOException {
		final JSONArray categorizationTree = new JSONArray(categorizationJson);

		final JSONArray categoriesArray = new JSONArray();

		for (int i = 0; i < categorizationTree.length(); ++i) {
			final JSONObject categorizationObj = categorizationTree.getJSONObject(i);
			final String categoryName = categorizationObj.getString(TEXT_ATTRIB);
			if (categoryName.equals(nodeId)) {
				final JSONObject outputNode = new JSONObject();
				outputNode.put("path", getCompletePath(categorizationTree, categorizationObj.getString(PARENT_ATTRIB)));

				categoriesArray.put(outputNode);
			}
		}

		return categoriesArray;
	}

	private String getCompletePath(JSONArray categorizationTree, String parentId) {
		if (parentId.equals("#")) {
			return "";
		} else {
			return getParentTree(categorizationTree, parentId);
		}
	}

	private String getParentTree(JSONArray categorizationTree, String parentId) {
		for (int j = 0; j < categorizationTree.length(); ++j) {
			final JSONObject categorizationObj = categorizationTree.getJSONObject(j);
			final String categoryIdName = categorizationObj.getString("id");
			if (categoryIdName.equals(parentId)) {
				return getParentTree(categorizationTree, categorizationObj.getString(PARENT_ATTRIB)) + URL_SEPARATOR
						+ categorizationObj.getString(TEXT_ATTRIB);
			}
		}
		return "";
	}

}
