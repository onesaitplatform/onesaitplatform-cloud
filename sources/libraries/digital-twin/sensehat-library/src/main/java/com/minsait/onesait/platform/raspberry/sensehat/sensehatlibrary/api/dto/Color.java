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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto;

/**
 * Created by jcincera on 04/07/2017.
 */
public class Color {

	private String r;
	private String g;
	private String b;

	public static Color RED = Color.of(255, 0, 0);
	public static Color GREEN = Color.of(0, 255, 0);
	public static Color BLUE = Color.of(0, 0, 255);

	public Color(Integer r, Integer g, Integer b) {
		this.r = String.valueOf(r);
		this.g = String.valueOf(g);
		this.b = String.valueOf(b);
	}

	public static Color of(Integer r, Integer g, Integer b) {
		return new Color(r, g, b);
	}

	public String r() {
		return r;
	}

	public String g() {
		return g;
	}

	public String b() {
		return b;
	}
}
