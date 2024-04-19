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
package com.minsait.onesait.platform.config.dto.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

@Component
public class ParameterMapConverter {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	public Map<String, Object> convert(List<ReportParameter> reportParameters) throws GenericOPException {

		final Map<String, Object> params = new HashMap<>();
		for (final ReportParameter reportParameter : reportParameters) {

			switch (reportParameter.getType()) {
			case INTEGER:
				params.put(reportParameter.getName(), Integer.parseInt(reportParameter.getValue()));
				break;
			case DOUBLE:
				params.put(reportParameter.getName(), Double.parseDouble(reportParameter.getValue()));
				break;
			case BOOLEAN:
				params.put(reportParameter.getName(), Boolean.valueOf(reportParameter.getValue()));
				break;
			case COLLECTION:
			case LIST:
				params.put(reportParameter.getName(), Arrays.asList(reportParameter.getValue().split(",")));
				break;
			case DATE:
				try {
					params.put(reportParameter.getName(), dateFormat.parse(reportParameter.getValue()));
				} catch (final ParseException e) {
					throw new GenericOPException(e);
				}
				break;

			default:
				params.put(reportParameter.getName(), reportParameter.getValue());
				break;
			}
		}

		return params;
	}

}
