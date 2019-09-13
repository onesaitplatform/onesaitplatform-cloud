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
package com.minsait.onesait.platform.config.services.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Getter;

public class MigrationErrors {

	@Getter
	private List<MigrationError> errors = new ArrayList<>();

	private Set<MigrationError> errorsSet = new HashSet<>();

	public void addError(MigrationError error) {
		if (!errorsSet.contains(error)) {
			errors.add(error);
			errorsSet.add(error);
		}
	}

	public int numberOfErrors() {
		return errors.size();
	}

	public List<MigrationError> getErrors(Predicate<MigrationError> predicate) {
		return (errors.stream().filter(predicate).collect(Collectors.<MigrationError>toList()));
	}

	public void removeRequired(Instance neededInstanceToRemove) {

		Predicate<MigrationError> predicate = new Predicate<MigrationError>() {
			@Override
			public boolean test(MigrationError error) {
				if (error != null) {
					Instance neededInstance = error.getNeededInstance();
					if (neededInstance != null) {
						return !neededInstance.equals(neededInstanceToRemove);
					}
				}
				return true;
			}

		};
		List<MigrationError> filtered = errors.stream().filter(predicate).collect(Collectors.<MigrationError>toList());
		this.errors = filtered;
	}

	public void addErrors(MigrationErrors additionalErrors) {
		for (MigrationError error : additionalErrors.getErrors()) {
			this.addError(error);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Errors: [\n");
		for (MigrationError error : errors) {
			sb.append(error);
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}

	public void addAll(Collection<MigrationError> previousErrors) {
		for (MigrationError error : previousErrors) {
			this.addError(error);
		}
	}

}
