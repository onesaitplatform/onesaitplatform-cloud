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
package com.minsait.onesait.platform.controlpanel.helper.market;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserComment;
import com.minsait.onesait.platform.config.model.UserRatings;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.repository.ApiRepository;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.UserCommentRepository;
import com.minsait.onesait.platform.config.repository.UserRatingsRepository;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.exception.BinaryFileException;
import com.minsait.onesait.platform.controlpanel.multipart.MarketAssetMultipart;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MarketAssetHelper {

	@Autowired
	ApiRepository apiRepository;

	@Autowired
	WebProjectRepository webProjectRepository;

	@Autowired
	private MarketAssetRepository marketAssetRepository;

	@Autowired
	UserRatingsRepository userRatingsRepository;

	@Autowired
	UserCommentRepository userCommentRepository;

	@Autowired
	IntegrationResourcesService resourcesService;

	@Autowired
	UserService userService;

	@Autowired
	AppWebUtils utils;

	@Value("${apimanager.services.api:/api-manager/services}")
	private String apiServices;

	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18000/web/}")
	private static final String ROOT_WWW = "";

	private static final String MARKET_ASSET_TYPES_STR = "marketassettypes";
	private static final String MARKET_ASSET_MODES_STR = "marketassetmodes";
	private static final String TECHNOLOGIES_STR = "technologies";
	private static final String MARKET_ASSET_STR = "marketasset";
	private static final String DESCRIPTION_STR = "description";
	private static final String USER_NOT_ALLOW_STR = "User is not allow to perform this operation";

	public void populateMarketAssetListForm(Model model) {
		model.addAttribute(MARKET_ASSET_TYPES_STR, MarketAsset.MarketAssetType.values());
		model.addAttribute("marketassetstates", MarketAsset.MarketAssetState.values());
		model.addAttribute(MARKET_ASSET_MODES_STR, MarketAsset.MarketAssetPaymentMode.values());
		model.addAttribute(TECHNOLOGIES_STR, getTechnologies());
	}

	public void populateMarketAssetCreateForm(Model model) {
		model.addAttribute(MARKET_ASSET_STR, new MarketAsset());
		model.addAttribute(MARKET_ASSET_TYPES_STR, MarketAsset.MarketAssetType.values());
		model.addAttribute(MARKET_ASSET_MODES_STR, MarketAsset.MarketAssetPaymentMode.values());
	}

	public void populateMarketAssetUpdateForm(Model model, String id) throws GenericOPException {
		final User user = userService.getUser(utils.getUserId());

		final MarketAsset marketAsset = marketAssetRepository.findById(id);

		// If the user is not the owner nor Admin an exception is launch to redirect to
		// list view
		if (!marketAsset.getUser().equals(user)
				&& !user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			log.error(USER_NOT_ALLOW_STR);
			throw new GenericOPException(USER_NOT_ALLOW_STR);
		}

		model.addAttribute(MARKET_ASSET_STR, marketAsset);
		model.addAttribute(MARKET_ASSET_TYPES_STR, MarketAsset.MarketAssetType.values());
		model.addAttribute(MARKET_ASSET_MODES_STR, MarketAsset.MarketAssetPaymentMode.values());

		populateMarketAssetFragment(model, marketAsset.getMarketAssetType().toString());
	}

	public void populateMarketAssetShowForm(Model model, String id) throws GenericOPException {
		// Asset to show
		final MarketAsset marketAsset = marketAssetRepository.findById(id);

		final String userId = utils.getUserId();

		// If the user is not the owner nor Admin and is not public an exception is
		// launch to redirect to
		// list view
		if (!marketAsset.isPublic() && !marketAsset.getUser().getUserId().equals(userId) && !utils.isAdministrator()) {
			log.error(USER_NOT_ALLOW_STR);
			throw new GenericOPException(USER_NOT_ALLOW_STR);
		}

		model.addAttribute(MARKET_ASSET_STR, marketAsset);
		model.addAttribute("json_desc", marketAsset.getJsonDesc());

		// User Asset Market Rating
		final List<UserRatings> userRatings = userRatingsRepository.findByMarketAssetAndUser(id, userId);
		if (userRatings != null && !userRatings.isEmpty()) {
			model.addAttribute("userRating", userRatings.get(0).getValue());
		}

		// Asset Market Rating
		Double ratingMarketAsset = 0.0;
		final List<UserRatings> usersRatingsMarketAssets = userRatingsRepository.findByMarketAsset(id);

		for (final UserRatings usersRatingsMarketAsset : usersRatingsMarketAssets) {
			ratingMarketAsset = ratingMarketAsset + usersRatingsMarketAsset.getValue();
		}

		if (!usersRatingsMarketAssets.isEmpty()) {
			ratingMarketAsset = ratingMarketAsset / usersRatingsMarketAssets.size();
		} else {
			ratingMarketAsset = 5.0;
		}
		model.addAttribute("marketassetRating", ratingMarketAsset.intValue());

		// Asset Comments
		final List<UserComment> usersComments = userCommentRepository.findByMarketAsset(id);
		model.addAttribute("commentsList", usersComments);
		model.addAttribute("commentsNumber", usersComments.size());

		// Technologies
		model.addAttribute(TECHNOLOGIES_STR, getTechnologies());

		// Five Assets
		final List<MarketAsset> assets = marketAssetRepository.findByUser(utils.getUserId());
		final List<MarketAssetDTO> fiveAssets = toMarketAssetBean(assets.subList(0, Math.min(assets.size(), 5)));

		// Technologies
		model.addAttribute("fiveAssets", fiveAssets);
	}

	public void populateMarketAssetFragment(Model model, String type) {

		final User user = userService.getUser(utils.getUserId());

		if (type.equals(MarketAsset.MarketAssetType.API.toString())) {

			List<Api> apiList = null;

			if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				apiList = apiRepository.findAll();
			} else {
				apiList = apiRepository.findByUser(user);
			}

			final List<Api> apis = getApisIds(apiList);

			model.addAttribute("apis", apis);

		} else if (type.equals(MarketAsset.MarketAssetType.WEBPROJECT.toString())) {

			List<WebProject> webProjectList = null;
			List<WebProject> webProjectListFiltered = null;

			webProjectList = webProjectRepository.findAll();

			if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
				webProjectListFiltered = webProjectList;
			} else {
				webProjectListFiltered = new ArrayList<>();
				for (final WebProject webProject : webProjectList) {
					if (user.getUserId().equals(webProject.getUser().getUserId())) {
						webProjectListFiltered.add(webProject);
					}
				}
			}
			model.addAttribute("webProjects", webProjectListFiltered);
		} else if (type.equals(MarketAsset.MarketAssetType.DOCUMENT.toString())) {
			// NO INFO DATA TO FILL AT THE MOMENT
		} else if (type.equals(MarketAsset.MarketAssetType.APPLICATION.toString())) {
			// NO INFO DATA TO FILL AT THE MOMENT
		} else if (type.equals(MarketAsset.MarketAssetType.URLAPPLICATION.toString())) {
			// NO INFO DATA TO FILL AT THE MOMENT
		}

	}

	private List<Api> getApisIds(List<Api> apiList) {
		final List<Api> apis = new ArrayList<>();
		for (final Api api : apiList) {
			if (api.getState().equals(Api.ApiStates.DEVELOPMENT) || api.getState().equals(Api.ApiStates.PUBLISHED)) {
				apis.add(api);
			}
		}
		return apis;
	}

	public void populateApiVersions(Model model, String identification) {
		final User user = userService.getUser(utils.getUserId());

		List<Api> apiList = null;

		if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString())) {
			apiList = apiRepository.findByIdentification(identification);
		} else {
			apiList = apiRepository.findByIdentificationAndUser(identification, user);
		}

		final List<Api> apis = getApisIds(apiList);

		model.addAttribute("apis", apis);
	}

	public String getApiDescription(String apiData) {
		Api api = null;
		final JSONObject returnObject = new JSONObject();
		try {
			final Map<String, String> obj = new ObjectMapper().readValue(apiData,
					new TypeReference<Map<String, String>>() {
					});

			final String identification = obj.get("identification");
			final String version = obj.get("version");

			api = apiRepository.findByIdentificationAndNumversion(identification, Integer.parseInt(version));

			final String srcSwagger = resourcesService.getUrl(Module.APIMANAGER, ServiceUrl.BASE)
					+ "swagger-ui.html?url=" + apiServices + "/management/swagger/v" + api.getNumversion() + "/"
					+ api.getIdentification() + "/swagger.json";

			returnObject.put(DESCRIPTION_STR, api.getDescription());
			returnObject.put("srcSwagger", srcSwagger);

		} catch (final Exception e) {
			log.warn(e.getClass().getName() + ":" + e.getMessage());
			log.error("getApiDescription:", e);
		}
		return (returnObject.toString());
	}

	public String validateId(String marketAssetId) {
		try {
			final Map<String, String> obj = new ObjectMapper().readValue(marketAssetId,
					new TypeReference<Map<String, String>>() {
					});

			final String identification = obj.get("identification");

			final MarketAsset marketAsset = marketAssetRepository.findByIdentification(identification);

			if (marketAsset != null) {
				return "ERROR";
			}
		} catch (final IOException e) {
			log.error(e.getMessage());
		}

		return null;
	}

	public MarketAsset marketAssetMultipartMap(MarketAssetMultipart marketAssetMultipart) {

		final MarketAsset marketAsset = new MarketAsset();

		marketAsset.setIdentification(marketAssetMultipart.getIdentification());

		marketAsset.setUser(userService.getUser(utils.getUserId()));

		marketAsset.setPublic(marketAssetMultipart.isPublic());
		marketAsset.setState(marketAssetMultipart.getState());
		marketAsset.setMarketAssetType(marketAssetMultipart.getMarketAssetType());
		marketAsset.setPaymentMode(marketAssetMultipart.getPaymentMode());

		marketAsset.setJsonDesc(marketAssetMultipart.getJsonDesc());

		try {
			if (marketAssetMultipart.getContentId() != null && !"".equals(marketAssetMultipart.getContentId())) {
				if (marketAssetMultipart.getContent().getSize() > utils.getMaxFileSizeAllowed())
					throw new BinaryFileException("File size is too big");
				marketAsset.setContent(marketAssetMultipart.getContent().getBytes());
				marketAsset.setContentId(marketAssetMultipart.getContentId());
			}
			if (marketAssetMultipart.getImage() != null) {
				if (marketAssetMultipart.getImage().getSize() > 1024 * 60)
					throw new BinaryFileException("Image size is too big");
				marketAsset.setImage(marketAssetMultipart.getImage().getBytes());
				marketAsset.setImageType(marketAssetMultipart.getImageType());
			}
		} catch (final IOException e1) {
			log.error("marketAssetMultipartMap", e1);
		}

		marketAsset.setCreatedAt(marketAssetMultipart.getCreatedAt());
		marketAsset.setUpdatedAt(marketAssetMultipart.getUpdatedAt());

		return marketAsset;
	}

	public List<MarketAssetDTO> toMarketAssetBean(List<MarketAsset> marketAssetList) {
		final List<MarketAssetDTO> marketAssetDTOList = new ArrayList<>();

		for (final MarketAsset marketAsset : marketAssetList) {

			final MarketAssetDTO marketAssetDTO = new MarketAssetDTO();

			marketAssetDTO.setId(marketAsset.getId());
			marketAssetDTO.setIdentification(marketAsset.getIdentification());

			marketAssetDTO.setUser(marketAsset.getUser());

			marketAssetDTO.setPublic(marketAsset.isPublic());
			marketAssetDTO.setState(marketAsset.getState());
			marketAssetDTO.setRejectionReason(marketAsset.getRejectionReason());
			marketAssetDTO.setMarketAssetType(marketAsset.getMarketAssetType());
			marketAssetDTO.setPaymentMode(marketAsset.getPaymentMode());
			marketAssetDTO.setJsonDesc(marketAsset.getJsonDesc());

			if (marketAsset.getImage() != null) {
				marketAssetDTO.setImage(marketAsset.getImage());
			}

			if (marketAsset.getJsonDesc() != null && !"".equals(marketAsset.getJsonDesc())) {

				Map<String, String> obj;
				try {
					obj = new ObjectMapper().readValue(marketAsset.getJsonDesc(),
							new TypeReference<Map<String, String>>() {
							});

					marketAssetDTO.setTitle(obj.get("title"));
					marketAssetDTO.setDescription(obj.get(DESCRIPTION_STR));
					marketAssetDTO.setTechnologies(obj.get(TECHNOLOGIES_STR));
				} catch (final IOException e) {
					log.error("toMarketAssetBean", e);
				}
			}

			marketAssetDTO.setCreatedAt(marketAsset.getCreatedAt());
			marketAssetDTO.setUpdatedAt(marketAsset.getUpdatedAt());

			marketAssetDTOList.add(marketAssetDTO);
		}
		return marketAssetDTOList;
	}

	private Collection<String> getTechnologies() {
		final List<String> jsonDescArray = marketAssetRepository.findJsonDescs();
		final HashMap<String, String> technologiesMap = new HashMap<>();

		Map<String, String> obj;
		for (final String jsonDesc : jsonDescArray) {
			try {
				obj = new ObjectMapper().readValue(jsonDesc, new TypeReference<Map<String, String>>() {
				});

				final String technologies = obj.get(TECHNOLOGIES_STR).trim();
				final List<String> technologiesList = new ArrayList<>(Arrays.asList(technologies.split(",")));

				for (final String technology : technologiesList) {
					technologiesMap.put(technology.toUpperCase(), technology.toUpperCase());
				}
			} catch (final IOException e) {
				log.error("getTechnologies", e);
			}
		}
		return (technologiesMap.values());
	}

	public String getUrlWebProjectData(String webProjectData) {

		Map<String, String> obj;
		String id = "";

		try {
			obj = new ObjectMapper().readValue(webProjectData, new TypeReference<Map<String, String>>() {
			});

			id = obj.get("webprojectId");
		} catch (final IOException e) {
			log.error(e.getMessage());
		}

		final WebProject webProject = webProjectRepository.findById(id);

		final String webProjectUrl = ROOT_WWW + webProject.getIdentification() + "/" + webProject.getMainFile();

		final JSONObject returnObject = new JSONObject();

		try {
			returnObject.put(DESCRIPTION_STR, webProject.getDescription());
			returnObject.put("webProjectUrl", webProjectUrl);
		} catch (final JSONException e) {
			log.error(e.getMessage());
		}
		return (returnObject.toString());
	}

}
