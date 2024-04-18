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
package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import threads.DeviceConnectionThread;

@Slf4j
public class Application {

	public static final int MAX_DEVICES = 50;
	public static final String[] deviceIdentifications = { "Raspberry pi 3", "Samnsung Galaxy s5", "Samnsung Galaxy s4",
			"iPhone x", "Gateway 12", "Gateway Fortnite", "Firewall Fortinet", "Medical sensor 1", "Medical sensor 7",
			"Medical sensor 9", "IoT GW Central Building", "IoT GW South Building", "IoT GW North Building",
			"Raspberry pi 3 sensor", "Python driven GW", "Thunderboard 765", "Thunderboard 322", "Thunderboard 12",
			"Thunderboard AF56", "Thunderboard 46JL", "iPad", "Sensor IoT", "Arduino AEF", "Arduino GW 24",
			"Google chromecast 14", "Google chromecast 50", "Google chromecast 4", "Google chromecast 20", "TOOGOO L1",
			"TOOGOO L5", "TOOGOO 25", "TOOGOO TE2", "TOOGOO 65", "KOOKYE F4", "KOOKYE E4", "KOOKYE B4", "iPad FE4",
			"iPad BBEF", "iPad Building 2", "Router Central", "Gateway IoT BD", "Firebase sensor 1",
			"Firebase sensor 4", "Firebase sensor 6", "Firebase sensor 9", "LoRa GW 1", "LoRa GW 12", "LoRa GW 6",
			"LoRa GW AEF", "LoRa Dascher 22" };
	public static final double[][] coordinates = { { 40.226086, -6.245651 }, { 40.326086, -5.945651 },
			{ 40.026086, -5.545651 }, { 40.226086, -5.245651 }, { 40.326086, -5.045651 }, { 40.226086, -4.745651 },
			{ 40.026086, -4.045651 }, { 40.026086, -3.745651 }, { 40.226086, -3.045651 }, { 40.226086, -1.945651 },
			{ 40.326086, -0.945651 }, { 40.026086, -0.345651 }, { 36.675702, -3.045651 }, { 37.675702, -4.421921 },
			{ 37.975702, -3.421921 }, { 38.275702, -4.121921 }, { 38.675702, -4.321921 }, { 39.175702, -3.221921 },
			{ 39.675702, -3.421921 }, { 40.675702, -5.421921 }, { 41.175702, -5.121921 }, { 41.675702, -3.421921 },
			{ 42.275702, -5.421921 }, { 42.775702, -4.621921 }, { 43.070610, -4.441821 }, { 42.56307, -8.64743 },
			{ 42.46307, -8.04743 }, { 42.45307, -7.84743 }, { 42.42307, -7.44743 }, { 42.30307, -7.14743 },
			{ 42.23307, -6.64743 }, { 42.20307, -6.14743 }, { 42.19307, -5.64743 }, { 42.18307, -5.34743 },
			{ 42.16307, -4.84743 }, { 42.15307, -4.14743 }, { 42.12307, -4.64743 }, { 42.10307, -3.64743 },
			{ 42.09307, -3.14743 }, { 42.09307, -2.64743 }, { 42.07307, -1.64743 }, { 42.05307, 0.64743 },
			{ 39.185555, -1.306361 }, { 38.823569, -6.898402 }, { 37.280296, -6.00851 }, { 37.416542, -2.525844 },
			{ 41.038513, -3.562792 }, { 39.329795, -0.310839 }, { 38.848865, -0.255908 }, { 43.419972, -7.122363 },
			{ 42.903896, -1.636662 } };
	public static final String[] tags = { "iot", "gateway", "group 1", "group 2", "group 3", "central building",
			"south building", "smart", "sensor", "plugable", "iot", "gateway", "group 1", "group 2", "group 3",
			"central building", "south building", "smart", "sensor", "plugable", "iot", "gateway", "group 1", "group 2",
			"group 3", "central building", "south building", "smart", "sensor", "plugable", "iot", "gateway", "group 1",
			"group 2", "group 3", "central building", "south building", "smart", "sensor", "plugable", "iot", "gateway",
			"group 1", "group 2", "group 3", "central building", "south building", "smart", "sensor", "plugable",
			"group 3" };
	public static final String ERROR_MESSAGE = "Battery critical";
	public static final String OK_MESSAGE = "Device is UP, registering activity";
	public static final int[] ERROR_POSITIONS = { 1, 5, 9, 13, 17, 18, 23, 27, 34, 42, 49 };
	public static final int GLOBAL_TIMEOUT = 10;

	public static void main(String[] args) {

		final String url = "tcp://localhost:1883";
		int devices = MAX_DEVICES;
		if (args.length > 0) {
			final int paramN = Integer.valueOf(args[0]).intValue();
			if (paramN > 0 && paramN <= 50)
				devices = paramN;
		}
		final ExecutorService executor = Executors.newFixedThreadPool(devices);
		for (int i = 0; i < devices; i++) {
			final int position = i;
			final boolean isErrorLog = IntStream.of(ERROR_POSITIONS).anyMatch(n -> n == position);
			if (isErrorLog)
				executor.submit(new DeviceConnectionThread(url, coordinates[i][0], coordinates[i][1], ERROR_MESSAGE,
						deviceIdentifications[i], tags[i].concat(",").concat(tags[i + 1])));
			else
				executor.submit(new DeviceConnectionThread(url, coordinates[i][0], coordinates[i][1], OK_MESSAGE,
						deviceIdentifications[i], tags[i].concat(",").concat(tags[i + 1])));
			log.info("Starting device: " + deviceIdentifications[i]);
		}
		try {
			executor.awaitTermination(GLOBAL_TIMEOUT, TimeUnit.MINUTES);
		} catch (final InterruptedException e) {
			log.info("Simulator stopped due to timeout preset : " + GLOBAL_TIMEOUT + " " + TimeUnit.MINUTES.toString());
		}

	}
}
