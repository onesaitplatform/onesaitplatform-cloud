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
package com.minsait.onesait.platform.router.transaction.operation;

import java.io.Serializable;
import java.util.List;

import com.minsait.onesait.platform.router.service.app.model.NotificationModel;

import lombok.Data;

@Data
public class TransactionalOperation implements Serializable, Comparable<TransactionalOperation> {

	public enum TransactionalOperationType {
		INSERT, UPDATE, DELETE
	}

	private String sessionkey;
	private long timestamp;
	private TransactionalOperationType type;
	private NotificationModel notificationModel;
	private List<String> affectedIds;
	private List<String> compensation;

	public static TransactionalOperation newInstance() {
		return new TransactionalOperation();
	}

	public TransactionalOperation() {

	}

	public TransactionalOperation(TransactionalOperation builder) {
		this.timestamp = builder.getTimestamp();
		this.type = builder.getType();
	}

	public TransactionalOperation sessionkey(String sessionkey) {
		this.sessionkey = sessionkey;
		return this;
	}

	public TransactionalOperation timestamp(long timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public TransactionalOperation type(TransactionalOperationType type) {
		this.type = type;
		return this;
	}

	public TransactionalOperation notificationModel(NotificationModel notificationModel) {
		this.notificationModel = notificationModel;
		return this;
	}

	@Override
	public int compareTo(TransactionalOperation tObj) {
		if (this.timestamp < tObj.timestamp) {
			return -1;
		} else if (this.timestamp > tObj.timestamp) {
			return 1;
		} else {
			return 0;
		}

	}

}
