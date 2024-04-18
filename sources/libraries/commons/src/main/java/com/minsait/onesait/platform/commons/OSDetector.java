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
package com.minsait.onesait.platform.commons;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OSDetector {

	private static final String OS = System.getProperty("os.name").toLowerCase();

	public static void main(String[] args) {
		log.debug(OS);
		if (isWindows()) {
			log.debug("This is Windows");
		} else if (isMac()) {
			log.debug("This is Mac");
		} else if (isUnix()) {
			log.debug("This is Unix or Linux");
		} else if (isSolaris()) {
			log.debug("This is Solaris");
		} else {
			log.debug("Your OS is not support!!");
		}
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") >= 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

}