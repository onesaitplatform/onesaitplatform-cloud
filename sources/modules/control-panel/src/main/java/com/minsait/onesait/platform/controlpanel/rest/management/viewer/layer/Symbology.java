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
package com.minsait.onesait.platform.controlpanel.rest.management.viewer.layer;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Symbology {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String pixelSize;

	@Getter
	@Setter
	private ColorRGB innerColorRGB;

	@Getter
	@Setter
	private String innerColorHEX;

	@Getter
	@Setter
	private String innerColorAlpha;

	@Getter
	@Setter
	private ColorRGB outlineColorRGB;

	@Getter
	@Setter
	private String outlineColorHEX;

	@Getter
	@Setter
	private String outerColorAlpha;

	@Getter
	@Setter
	private String outlineWidth;

	@Getter
	@Setter
	private List<Filter> filters;

}
