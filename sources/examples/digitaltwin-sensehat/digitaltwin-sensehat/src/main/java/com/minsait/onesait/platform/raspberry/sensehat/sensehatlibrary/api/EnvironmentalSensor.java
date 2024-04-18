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

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.Command;

/**
 * Created by jcincera on 20/06/2017.
 */
public class EnvironmentalSensor extends APIBase {

	EnvironmentalSensor() {
	}

	/**
	 * Gets the percentage of relative humidity from the humidity sensor
	 *
	 * @return the percentage of relative humidity
	 */
	public float getHumidity() {
		return execute(Command.GET_HUMIDITY).getFloat();
	}

	/**
	 * Calls get_temperature_from_humidity internally in Python core
	 *
	 * @return the current temperature in degrees Celsius
	 */
	public float getTemperature() {
		return execute(Command.GET_TEMPERATURE).getFloat();
	}

	/**
	 * Gets the current temperature in degrees Celsius from the humidity sensor
	 *
	 * @return the current temperature in degrees Celsius
	 */
	public float getTemperatureFromHumidity() {
		return execute(Command.GET_TEMPERATURE_FROM_HUMIDITY).getFloat();
	}

	/**
	 * Gets the current temperature in degrees Celsius from the pressure sensor
	 *
	 * @return the current temperature in degrees Celsius
	 */
	public float getTemperatureFromPressure() {
		return execute(Command.GET_TEMPERATURE_FROM_PRESSURE).getFloat();
	}

	/**
	 * Gets the current pressure in Millibars from the pressure sensor
	 *
	 * @return the current pressure in Millibars
	 */
	public float getPressure() {
		return execute(Command.GET_PRESSURE).getFloat();
	}
}
