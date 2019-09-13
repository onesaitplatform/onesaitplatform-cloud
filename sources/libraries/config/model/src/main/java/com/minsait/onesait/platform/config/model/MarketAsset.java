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
package com.minsait.onesait.platform.config.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "MARKET_ASSET")
public class MarketAsset extends OPResource {

	private static final long serialVersionUID = 1L;

	public enum MarketAssetState {
		PENDING, APPROVED, REJECTED;
	}

	public enum MarketAssetType {
		API, DOCUMENT, WEBPROJECT, APPLICATION, URLAPPLICATION;
	}

	public enum MarketAssetPaymentMode {
		FREE;
	}

	@Column(name = "IS_PUBLIC", nullable = false, columnDefinition = "BIT")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "STATE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private MarketAssetState state;

	@Column(name = "MARKETASSET_TYPE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private MarketAssetType marketAssetType;

	@Column(name = "PAYMENT_MODE", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private MarketAssetPaymentMode paymentMode;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "IMAGE", length = 100000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] image;

	@Column(name = "IMAGE_TYPE", length = 20)
	@Getter
	@Setter
	private String imageType;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "CONTENT", length = 100000000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] content;

	@Column(name = "CONTENT_ID", length = 200)
	@Getter
	@Setter
	private String contentId;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "JSON_DESC", length = 1000000)
	@Lob
	@Getter
	@Setter
	private String jsonDesc;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "REJECTION_REASON", length = 500)
	@Lob
	@Getter
	@Setter
	private String rejectionReason;

	@Column(name = "DELETED_AT", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	@Getter
	@Setter
	private Date deletedAt;

}
