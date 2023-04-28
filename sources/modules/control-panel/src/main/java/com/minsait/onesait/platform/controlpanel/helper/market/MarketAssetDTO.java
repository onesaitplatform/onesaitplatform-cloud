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
package com.minsait.onesait.platform.controlpanel.helper.market;

import java.util.Date;

import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetPaymentMode;
import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetState;
import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetType;
import com.minsait.onesait.platform.config.model.User;

import lombok.Getter;
import lombok.Setter;

public class MarketAssetDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private User user;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private MarketAssetState state;

	@Getter
	@Setter
	private MarketAssetType marketAssetType;

	@Getter
	@Setter
	private MarketAssetPaymentMode paymentMode;

	@Getter
	@Setter
	private String title;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String technologies;

	@Getter
	@Setter
	private String jsonDesc;

	@Getter
	@Setter
	private Date createdAt;

	@Getter
	@Setter
	private Date updatedAt;

	@Getter
	@Setter
	private String rejectionReason;

	@Getter
	@Setter
	byte[] image;
}
