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
package com.minsait.onesait.platform.config.services.migration;

import lombok.Getter;

public class MigrationError {

	public enum ErrorType {
		INFO, WARN, ERROR
	}

	@Getter
	private final ErrorType type;
	@Getter
	private final String msg;
	@Getter
	private final Instance neededInstance;
	@Getter
	private final Instance processedInstance;

	public final Instance noInstance = new Instance(null, null, null, null);

	public MigrationError(Instance instanceThatNeedsIt, Instance neededInstance, ErrorType type, String msg) {
		this.type = type;
		this.msg = msg;
		this.neededInstance = neededInstance;
		this.processedInstance = instanceThatNeedsIt;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getType());
		sb.append(" - ");
		sb.append(getMsg());
		sb.append(" - Processed instance: ");
		sb.append(getProcessedInstance());
		sb.append(" - Required instance: ");
		sb.append(getNeededInstance());
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MigrationError))
			return false;
		MigrationError that = (MigrationError) o;
		return getNeededInstance() != null && getNeededInstance().equals(that.getNeededInstance())
				&& getProcessedInstance() != null && getProcessedInstance().equals(that.getProcessedInstance());
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(getNeededInstance(), getProcessedInstance());
	}
}
