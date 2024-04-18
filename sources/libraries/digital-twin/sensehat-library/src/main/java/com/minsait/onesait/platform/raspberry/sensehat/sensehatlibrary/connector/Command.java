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
package com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector;

/**
 * Created by jcincera on 21/06/2017.
 */
public enum Command {

	/**
	 * Imports
	 */
	IMPORT_SENSE_HAT("from sense_hat import SenseHat"),

	/**
	 * Core object
	 */
	SENSE_OBJECT("sensehat = SenseHat()"),

	/**
	 * Environmental sensor
	 */
	GET_HUMIDITY("print(sensehat.get_humidity())"), GET_TEMPERATURE(
			"print(sensehat.get_temperature())"), GET_TEMPERATURE_FROM_HUMIDITY(
					"print(sensehat.get_temperature_from_humidity())"), GET_TEMPERATURE_FROM_PRESSURE(
							"print(sensehat.get_temperature_from_pressure())"), GET_PRESSURE(
									"print(sensehat.get_pressure())"),

	/**
	 * IMU
	 */
	SET_IMU_CONFIG("sensehat.set_imu_config(%s, %s, %s)"),

	GET_ORIENTATION_RADIANS(
			"print('{pitch}@{roll}@{yaw}'.format(**sensehat.get_orientation_radians()))"), GET_ORIENTATION_DEGREES(
					"print('{pitch}@{roll}@{yaw}'.format(**sensehat.get_orientation_degrees()))"), GET_ORIENTATION(
							"print('{pitch}@{roll}@{yaw}'.format(**sensehat.get_orientation()))"),

	GET_COMPASS("print(sensehat.get_compass())"), GET_COMPASS_RAW(
			"print('{x}@{y}@{z}'.format(**sensehat.get_compass_raw()))"),

	GET_GYROSCOPE("print('{pitch}@{roll}@{yaw}'.format(**sensehat.get_gyroscope()))"), GET_GYROSCOPE_RAW(
			"print('{x}@{y}@{z}'.format(**sensehat.get_gyroscope_raw()))"),

	GET_ACCELEROMETER("print('{pitch}@{roll}@{yaw}'.format(**sensehat.get_accelerometer()))"), GET_ACCELEROMETER_RAW(
			"print('{x}@{y}@{z}'.format(**sensehat.get_accelerometer_raw()))"),

	/**
	 * LED Matrix
	 */
	SET_ROTATION("sensehat.set_rotation(%s)"), SET_PIXEL("sensehat.set_pixel(%s, %s, %s, %s, %s)"), SET_PIXELS(
			"sensehat.set_pixels([%s])"), CLEAR("sensehat.clear()"), CLEAR_WITH_COLOR(
					"sensehat.clear(%s, %s, %s)"), SHOW_MESSAGE(
							"sensehat.show_message('%s')"), SHOW_MESSAGE_PARAMETRIZED(
									"sensehat.show_message('%s', %s, [%s, %s, %s], [%s, %s, %s])"), SHOW_LETTER(
											"sensehat.show_letter('%s')"), SHOW_LETTER_PARAMETRIZED(
													"sensehat.show_letter('%s', [%s, %s, %s], [%s, %s, %s])"), LOW_LIGHT(
															"sensehat.low_light = %s"),

	/**
	 * Joystick
	 */
	GET_EVENTS("e = sensehat.stick.get_events();"
			+ "print(str('|'.join('{}@{}@{}'.format(i.action, i.direction, i.timestamp) for i in e)))"), WAIT_FOR_EVENT_EMPTY_BUFFER(
					"e = sensehat.stick.wait_for_event(%s)\n"
							+ "print('{}@{}@{}'.format(e.action, e.direction, e.timestamp))"),;

	private String rawCommand;

	Command(String command) {
		this.rawCommand = command;
	}

	public String getCommand() {
		return rawCommand;
	}
}
