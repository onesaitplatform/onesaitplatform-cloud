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

import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.IMUData;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.IMUDataRaw;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.connector.Command;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.utils.PythonUtils;

/**
 * Created by jcincera on 20/06/2017.
 */
public class IMU extends APIBase {

	IMU() {
	}

	/**
	 * Enables and disables the gyroscope, accelerometer and/or magnetometer
	 * contribution to the get orientation functions
	 *
	 * @param compassEnabled
	 *            enable compass
	 * @param gyroscopeEnabled
	 *            enable gyroscope
	 * @param accelerometerEnabled
	 *            enable accelerometer
	 */
	public void setIMUConfig(boolean compassEnabled, boolean gyroscopeEnabled, boolean accelerometerEnabled) {
		execute(Command.SET_IMU_CONFIG, PythonUtils.toBoolean(compassEnabled), PythonUtils.toBoolean(gyroscopeEnabled),
				PythonUtils.toBoolean(accelerometerEnabled)).checkEmpty();
	}

	/**
	 * Gets the current orientation in radians using the aircraft principal axes of
	 * pitch, roll and yaw
	 *
	 * @return Object with pitch, roll and yaw values. Values are Floats
	 *         representing the angle of the axis in radians
	 */
	public IMUData getOrientationRadians() {
		return execute(Command.GET_ORIENTATION_RADIANS).getIMUData();
	}

	/**
	 * Gets the current orientation in degrees using the aircraft principal axes of
	 * pitch, roll and yaw
	 *
	 * @return Object with pitch, roll and yaw values. Values are Floats
	 *         representing the angle of the axis in degrees
	 */
	public IMUData getOrientationDegrees() {
		return execute(Command.GET_ORIENTATION_DEGREES).getIMUData();
	}

	/**
	 * Calls get_orientation_degrees internally in Python core
	 *
	 * @return Object with pitch, roll and yaw representing the angle of the axis in
	 *         degrees
	 */
	public IMUData getOrientation() {
		return execute(Command.GET_ORIENTATION).getIMUData();
	}

	/**
	 * Calls set_imu_config internally in Python core to disable the gyroscope and
	 * accelerometer then gets the direction of North from the magnetometer in
	 * degrees
	 *
	 * @return The direction of North
	 */
	public float getCompass() {
		return execute(Command.GET_COMPASS).getFloat();
	}

	/**
	 * Gets the raw x, y and z axis magnetometer data
	 *
	 * @return Object representing the magnetic intensity of the axis in microteslas
	 *         (uT)
	 */
	public IMUDataRaw getCompassRaw() {
		return execute(Command.GET_COMPASS_RAW).getIMUDataRaw();
	}

	/**
	 * Calls set_imu_config internally in Python core to disable the magnetometer
	 * and accelerometer then gets the current orientation from the gyroscope only
	 *
	 * @return Object with pitch, roll and yaw representing the angle of the axis in
	 *         degrees
	 */
	public IMUData getGyroscope() {
		return execute(Command.GET_GYROSCOPE).getIMUData();
	}

	/**
	 * Gets the raw x, y and z axis gyroscope data
	 *
	 * @return Object representing the rotational intensity of the axis in radians
	 *         per second
	 */
	public IMUDataRaw getGyroscopeRaw() {
		return execute(Command.GET_GYROSCOPE_RAW).getIMUDataRaw();
	}

	/**
	 * Calls set_imu_config in Python core to disable the magnetometer and gyroscope
	 * then gets the current orientation from the accelerometer only
	 *
	 * @return Object representing the angle of the axis in degrees
	 */
	public IMUData getAccelerometer() {
		return execute(Command.GET_ACCELEROMETER).getIMUData();
	}

	/**
	 * Gets the raw x, y and z axis accelerometer data
	 *
	 * @return Object representing the acceleration intensity of the axis in Gs
	 */
	public IMUDataRaw getAccelerometerRaw() {
		return execute(Command.GET_ACCELEROMETER_RAW).getIMUDataRaw();
	}
}
