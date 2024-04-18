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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.joystick;

/**
 * Created by jcincera on 17/07/2017.
 */
public enum Direction {

	UP("up"), DOWN("down"), LEFT("left"), RIGHT("right"), MIDDLE("middle");

	private String value;

	Direction(String value) {
		this.value = value;
	}

	public static Direction from(String value) {
		for (Direction d : values()) {
			if (d.value.equals(value)) {
				return d;
			}
		}
		throw new IllegalArgumentException("Unsupported direction: " + value);
	}
}
