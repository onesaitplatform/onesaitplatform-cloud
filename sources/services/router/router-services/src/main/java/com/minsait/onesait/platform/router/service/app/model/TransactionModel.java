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
package com.minsait.onesait.platform.router.service.app.model;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TransactionModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum OperationType {
		START_TRANSACTION, COMMIT_TRANSACTION, ROLLBACK_TRANSACTION;
	}

	@Getter
	@Setter
	private OperationType type;

	@Getter
	@Setter
	private String clientSession;

	@Getter
	@Setter
	private String transactionId;

	@Getter
	@Setter
	private boolean lockTransaction = false;

	public TransactionModel() {

	}

	private TransactionModel(Builder builder) {
		type = builder.type;
		clientSession = builder.clientSession;
		transactionId = builder.transactionId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private OperationType type;
		private String clientSession;
		private String transactionId;

		public TransactionModel build() {
			return new TransactionModel(this);
		}

		public Builder type(OperationType type) {
			this.type = type;
			return this;
		}

		public Builder clientSession(String clientSession) {
			this.clientSession = clientSession;
			return this;
		}

		public Builder transactionId(String transactionId) {
			this.transactionId = transactionId;
			return this;
		}

	}

}
