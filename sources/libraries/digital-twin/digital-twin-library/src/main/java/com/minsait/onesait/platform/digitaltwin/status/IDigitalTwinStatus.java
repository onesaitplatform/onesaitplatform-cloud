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
package com.minsait.onesait.platform.digitaltwin.status;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.digitaltwin.exception.DigitaltwinException;
import com.minsait.onesait.platform.digitaltwin.property.controller.OperationType;

@Component
public interface IDigitalTwinStatus {

	Boolean validate(OperationType operationType, String property);

	Object getProperty(String property) throws DigitaltwinException;

	void setProperty(String property, Object value) throws DigitaltwinException;

	Map<String, Object> toMap();
}
