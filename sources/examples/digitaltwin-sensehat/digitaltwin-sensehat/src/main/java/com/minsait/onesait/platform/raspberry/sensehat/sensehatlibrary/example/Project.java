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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.example;

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.SenseHat;

/**
 * Created by jcincera on 03/07/2017.
 */
public class Project {

	public static void main(String[] args) {
		System.out.println(">>> Create project here <<<");

		SenseHat senseHat = new SenseHat();

		float humidity = senseHat.environmentalSensor.getHumidity();
		System.out.println("Current humidity: " + humidity);

		senseHat.ledMatrix.showMessage("my project");
		senseHat.ledMatrix.waitFor(5);
		senseHat.ledMatrix.clear();
	}
}
