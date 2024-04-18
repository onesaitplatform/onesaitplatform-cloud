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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api;

/**
 * Created by jcincera on 04/07/2017.
 */
public class SenseHat {
	public final EnvironmentalSensor environmentalSensor;
	public final IMU IMU;
	public final LEDMatrix ledMatrix;
	public final Joystick joystick;

	public SenseHat() {
		this.environmentalSensor = new EnvironmentalSensor();
		this.IMU = new IMU();
		this.ledMatrix = new LEDMatrix();
		this.joystick = new Joystick();
	}
}
