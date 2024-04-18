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
package com.minsait.onesait.platform.config.services.market;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.MarketAsset;
import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetState;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserComment;
import com.minsait.onesait.platform.config.model.UserRatings;
import com.minsait.onesait.platform.config.repository.MarketAssetRepository;
import com.minsait.onesait.platform.config.repository.UserCommentRepository;
import com.minsait.onesait.platform.config.repository.UserRatingsRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MarketAssetServiceImpl implements MarketAssetService {

	@Autowired
	UserService userService;

	@Autowired
	private MarketAssetRepository marketAssetRepository;

	@Autowired
	private UserRatingsRepository userRatingRepository;

	@Autowired
	private UserCommentRepository userCommentRepository;

	@Override
	public List<MarketAsset> loadMarketAssetByFilter(String marketAssetId, String userId) {
		final User user = userService.getUser(userId);

		if (marketAssetId == null || marketAssetId.trim().equals("")) {
			marketAssetId = "";
		}

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			return marketAssetRepository.findByIdentificationLike(marketAssetId);
		} else {
			return (filterByUserOrAproved(marketAssetRepository.findByIdentificationLike(marketAssetId), user));
		}
	}

	@Override
	public MarketAsset getMarketAssetById(String id) {
		return marketAssetRepository.findById(id);
	}

	@Override
	public String createMarketAsset(MarketAsset marketAsset) {

		if (marketAsset.getUser().getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			marketAsset.setState(MarketAssetState.APPROVED);
		} else {
			marketAsset.setState(MarketAssetState.PENDING);
		}

		marketAssetRepository.save(marketAsset);

		return marketAsset.getId();
	}

	@Override
	public void updateMarketAsset(String id, MarketAsset marketAsset, String userId) throws GenericOPException {

		final MarketAsset marketAssetMemory = marketAssetRepository.findById(id);
		final User user = userService.getUser(userId);

		// If the user is not the owner nor Admin an exception is launch to redirect to
		// list view
		if (!marketAsset.getUser().getUserId().equals(userId)
				&& !user.getRole().toString().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			log.error("User is not allow to perform this operation");
			throw new GenericOPException("User is not allow to perform this operation");
		}

		marketAssetMemory.setIdentification(marketAsset.getIdentification());

		marketAssetMemory.setUser(marketAsset.getUser());

		marketAssetMemory.setPublic(marketAsset.isPublic());
		marketAssetMemory.setMarketAssetType(marketAsset.getMarketAssetType());
		marketAssetMemory.setPaymentMode(marketAsset.getPaymentMode());
		marketAssetMemory.setState(MarketAsset.MarketAssetState.PENDING);

		marketAssetMemory.setJsonDesc(marketAsset.getJsonDesc());

		if (marketAsset.getContentId() == null || "".equals(marketAsset.getContentId())) {
			marketAssetMemory.setContent(null);
			marketAssetMemory.setContentId(null);
		} else if (marketAsset.getContent() != null && marketAsset.getContent().length > 0) {
			marketAssetMemory.setContent(marketAsset.getContent());
			marketAssetMemory.setContentId(marketAsset.getContentId());
		}

		if (marketAsset.getImageType() == null || "".equals(marketAsset.getImageType())) {
			marketAssetMemory.setImage(null);
			marketAssetMemory.setImageType(null);
		} else if (marketAsset.getImage() != null && marketAsset.getImage().length > 0) {
			marketAssetMemory.setImage(marketAsset.getImage());
			marketAssetMemory.setImageType(marketAsset.getImageType());
		}

		marketAssetMemory.setCreatedAt(marketAsset.getCreatedAt());
		marketAssetMemory.setUpdatedAt(marketAsset.getUpdatedAt());

		marketAssetRepository.save(marketAssetMemory);
	}

	private List<MarketAsset> filterByUserOrAproved(List<MarketAsset> marketAssetList, User user) {
		final List<MarketAsset> marketAssetResult = new ArrayList<>();
		for (final MarketAsset marketAsset : marketAssetList) {
			if (marketAsset.getUser().getUserId().equals(user.getUserId())
					|| (marketAsset.getState().name().equals(MarketAsset.MarketAssetState.APPROVED.name())
							&& marketAsset.isPublic())) {
				marketAssetResult.add(marketAsset);
			}
		}
		return marketAssetResult;
	}

	@Override
	public byte[] getImgBytes(String id) {
		final MarketAsset market = marketAssetRepository.findById(id);
		return market.getImage();
	}

	@Override
	public byte[] getContent(String id) {
		final MarketAsset marketAsset = marketAssetRepository.findById(id);
		return marketAsset.getContent();
	}

	@Override
	public void downloadDocument(String id, HttpServletResponse response) {
		final MarketAsset marketAsset = marketAssetRepository.findById(id);

		final InputStream bis = new ByteArrayInputStream(marketAsset.getContent());
		final String name = marketAsset.getContentId();
		response.setContentType("application/octet-stream");

		ServletOutputStream out;
		try {
			response.setHeader("Content-Disposition", "filename=" + name);
			out = response.getOutputStream();
			IOUtils.copy(bis, out);
			response.flushBuffer();
		} catch (final IOException e) {
			log.error("Exception reached " + e.getMessage(), e);
		}
	}

	@Override
	public String updateState(String id, String state, String reason) {
		Map<String, String> obj;
		String rejectReason = "";
		try {
			obj = new ObjectMapper().readValue(reason, new TypeReference<Map<String, String>>() {
			});
			rejectReason = obj.get("rejectionReason");
		} catch (final IOException e) {
			log.error("Exception reached " + e.getMessage(), e);
		}

		final MarketAsset marketAsset = marketAssetRepository.findById(id);

		marketAsset.setRejectionReason(rejectReason);
		marketAsset.setState(MarketAssetState.valueOf(state));

		marketAssetRepository.save(marketAsset);

		return state;
	}

	@Override
	public void delete(String id, String userId) {
		final User user = userService.getUser(userId);
		final MarketAsset marketAssetToDelete = marketAssetRepository.findById(id);

		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())
				|| marketAssetToDelete.getUser().equals(user)) {
			marketAssetToDelete.setDeletedAt(new Date());
			marketAssetRepository.save(marketAssetToDelete);
		}
	}

	@Override
	public void rate(String marketAssetId, String rate, String userId) {

		final List<UserRatings> userRatings = userRatingRepository.findByMarketAssetAndUser(marketAssetId, userId);

		if (userRatings != null && !userRatings.isEmpty()) {
			userRatingRepository.delete(userRatings);
		}

		final User user = userService.getUser(userId);
		final MarketAsset marketAsset = marketAssetRepository.findById(marketAssetId);

		final UserRatings newUserRatings = new UserRatings();

		newUserRatings.setMarketAsset(marketAsset);
		newUserRatings.setUser(user);

		newUserRatings.setValue(Double.parseDouble(rate));

		userRatingRepository.save(newUserRatings);
	}

	@Override
	public void createComment(String marketAssetId, String userId, String title, String comment) {
		final User user = userService.getUser(userId);
		final MarketAsset marketAsset = marketAssetRepository.findById(marketAssetId);

		final UserComment userComment = new UserComment();
		userComment.setMarketAsset(marketAsset);
		userComment.setUser(user);
		userComment.setTitle(title);
		userComment.setComment(comment);

		userCommentRepository.save(userComment);
	}

	@Override
	public void deleteComment(String id) {
		userCommentRepository.delete(id);
	}
}
