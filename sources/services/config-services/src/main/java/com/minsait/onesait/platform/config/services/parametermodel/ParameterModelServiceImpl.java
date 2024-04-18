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
package com.minsait.onesait.platform.config.services.parametermodel;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.Model;
import com.minsait.onesait.platform.config.model.ParameterModel;
import com.minsait.onesait.platform.config.repository.ParameterModelRepository;
import com.minsait.onesait.platform.config.services.exceptions.ParameterModelServiceException;

@Service
public class ParameterModelServiceImpl implements ParameterModelService {

	@Autowired
	private ParameterModelRepository parameterModelRepository;

	private static final String ID_STR = "identification";

	@Override
	public void updateParameterModel(ParameterModel parameterModel) {
		parameterModelRepository.save(parameterModel);
	}

	@Override
	public ParameterModel getParameterModelById(String id) {
		return parameterModelRepository.findById(id);
	}

	@Override
	public ParameterModel getParameterModelByIdentificationAndModel(String identification, Model model) {
		return parameterModelRepository.findByIdentificationAndModel(identification, model);
	}

	@Override
	public List<ParameterModel> findAllParameterModelsByModel(Model model) {
		return parameterModelRepository.findAllByModel(model);
	}

	@Override
	public void deleteParameterModel(String id) {
		parameterModelRepository.delete(parameterModelRepository.findById(id));
	}

	@Override
	public void createParameterModel(HttpServletRequest httpServletRequest, Model model) {
		try {
			String[] parameters = httpServletRequest.getParameterValues("parameters");

			if (parameters != null && !parameters[0].equals("")) {
				for (String param : parameters) {
					JSONObject json = new JSONObject(param);

					ParameterModel.Type type = ParameterModel.Type.valueOf(json.getString("type"));

					ParameterModel paramModel = new ParameterModel();
					paramModel.setIdentification(json.getString(ID_STR));
					paramModel.setType(type);
					paramModel.setModel(model);
					if (type.equals(ParameterModel.Type.NUMBER)) {
						paramModel.setRangeFrom(Integer.parseInt(json.getString("from")));
						paramModel.setRangeTo(Integer.parseInt(json.getString("to")));
					} else if (type.equals(ParameterModel.Type.ENUMERATION)) {
						paramModel.setEnumerators(json.getString("enum"));
					}
					parameterModelRepository.save(paramModel);

				}
			}
		} catch (Exception e) {
			throw new ParameterModelServiceException("Problems creating the parameter model: " + e.getMessage());
		}
	}

	@Override
	public void updateParameterModel(HttpServletRequest request, Model model) {
		try {
			String[] parameters = request.getParameterValues("parameters");

			List<ParameterModel> parametersModel = parameterModelRepository.findAllByModel(model);

			if (!parametersModel.isEmpty()) {
				parameterModelRepository.delete(parametersModel);
			}
			if (parameters != null && !parameters[0].equals("")) {
				for (String param : parameters) {
					JSONObject json = new JSONObject(param);

					ParameterModel paramModel = parameterModelRepository
							.findByIdentificationAndModel(json.getString(ID_STR), model);

					if (paramModel == null) {
						paramModel = new ParameterModel();
					}

					ParameterModel.Type type = ParameterModel.Type.valueOf(json.getString("type").toUpperCase());

					paramModel.setIdentification(json.getString(ID_STR));
					paramModel.setType(type);
					paramModel.setModel(model);
					if (type.equals(ParameterModel.Type.NUMBER)) {
						paramModel.setRangeFrom(Integer.parseInt(json.getString("from")));
						paramModel.setRangeTo(Integer.parseInt(json.getString("to")));
					} else if (type.equals(ParameterModel.Type.ENUMERATION)) {
						paramModel.setEnumerators(json.getString("enum"));
					}

					parameterModelRepository.save(paramModel);

				}
			}
		} catch (Exception e) {
			throw new ParameterModelServiceException("Problems updating the parameter model: " + e.getMessage());
		}
	}

}
