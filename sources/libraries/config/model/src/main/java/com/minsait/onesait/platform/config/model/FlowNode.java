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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "FLOW_NODE")
public class FlowNode extends AuditableEntityWithUUID implements NotificationEntity {

	private static final long serialVersionUID = 1L;

	public enum Type {
		HTTP_NOTIFIER("onesaitplatform-notification-endpoint"), API_REST(
				"onesaitplatform api rest"), API_REST_OPERATION("onesaitplatform api rest operation");

		private String exposedName;

		Type(String exposedName) {
			this.exposedName = exposedName;
		}

		public String getName() {
			return exposedName;
		}
	}

	public enum MessageType {
		INSERT, DELETE, UPDATE;
	}

	@NotNull
	@Getter
	@Setter
	@Column(name = "IDENTIFICATION", length = 200, nullable = true)
	private String identification;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "FLOW_ID", referencedColumnName = "ID", nullable = false)
	@Getter
	@Setter
	private Flow flow;

	@Column(name = "TYPE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private Type flowNodeType;

	@NotNull
	@Getter
	@Setter
	@Column(name = "NODE_RED_NODE_ID", length = 50, unique = true, nullable = false)
	private String nodeRedNodeId;

	@NotNull
	@Getter
	@Setter
	@Column(name = "PARTIAL_URL", length = 50, nullable = false)
	private String partialUrl;

	@Getter
	@Setter
	@Column(name = "MESSAGE_TYPE", length = 50)
	@Enumerated(EnumType.STRING)
	private MessageType messageType;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private Ontology ontology;

	@Getter
	@Setter
	@Column(name = "RETRY_ON_FAILURE", nullable = true, columnDefinition = "BIT")
	private Boolean retryOnFailure;

	@Getter
	@Setter
	@Column(name = "ALLOW_DISCARD_AFTER_ELAPSED_TIME", nullable = true, columnDefinition = "BIT" )
	private Boolean discardAfterElapsedTime;

	@Getter
	@Setter
	@Column(name = "MAX_RETRY_ELAPSE_TIME", nullable = true)
	private Integer maxRetryElapsedTime;

	@Override
	public String getNotificationEntityId() {
		return getNodeRedNodeId();
	}

	@Override
	public String getNotificationUrl() {
		String domainId = getFlow().getFlowDomain().getIdentification();
		return domainId + getPartialUrl();
	}

	@Override
	public String getNotificationDomain() {
		return getFlow().getFlowDomain().getIdentification();
	}

	@Override
	public String getNotificationDomainUser() {
		return getFlow().getFlowDomain().getUser().getUserId();
	}
	
	@Override
	public Boolean isRetryOnFaialureEnabled(){
		return this.retryOnFailure;
	}
	
	@Override
	public Boolean isDiscardAfterElapsedTimeEnabled(){
		return this.discardAfterElapsedTime;
	}
	
	@Override
	public Integer getMaxRetryElapsedTime(){
		return this.maxRetryElapsedTime;
	}

}
