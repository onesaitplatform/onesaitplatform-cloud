/**
 * Copyright Indra Sistemas, S.A.
 * 2013-2018 SPAIN
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
package com.indracompany.sofia2.config.service.digitaltwin.type;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.indracompany.sofia2.config.model.DigitalTwinType;

public interface DigitalTwinTypeService {
	
	void createDigitalTwinType(DigitalTwinType digitalTwinType, HttpServletRequest httpServletRequest);
	List<DigitalTwinType> getAll();
	List<String> getAllIdentifications();
	DigitalTwinType getDigitalTwinTypeById(String id);
	List<PropertyDigitalTwinTypeDTO> getPropertiesByDigitalId(String id);
	List<ActionsDigitalTwinTypeDTO> getActionsByDigitalId(String TypeId);
	List<EventsDigitalTwinTypeDTO> getEventsByDigitalId(String TypeId);
	String getLogicByDigitalId(String TypeId);
	void getDigitalTwinToUpdate(Model model, String id);
	void updateDigitalTwinType(DigitalTwinType digitalTwinType, HttpServletRequest httpServletRequest);
	public void deleteDigitalTwinType(DigitalTwinType digitalTwinType);
}
