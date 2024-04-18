/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.spark.launcher.executor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InputStreamReaderRunnable implements Runnable {

	private String name = null;
	private BufferedReader reader = null;

	public InputStreamReaderRunnable(InputStream is, String name) {
		this.name = name;
		this.reader = new BufferedReader(new InputStreamReader(is));
		log.info("InputStreamReaderRunnable:  name=" + name);
	}

	public void run() {
		try {
			String line = reader.readLine();
			while (line != null) {
				log.info(line);
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("run() failed. for name=" + name, e);
		} finally {
			InputOutputUtil.close(reader);
		}
	}
}
