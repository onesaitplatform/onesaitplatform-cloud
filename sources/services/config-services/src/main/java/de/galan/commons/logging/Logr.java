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
package de.galan.commons.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.util.ReflectionUtil;


/**
 * This library is unmaintained and has problems with newer versions of log4j. This modifications prevents errors when using new versions
 *
 * @author galan
 */
public class Logr {

	// Using ReflectionUtil directly with "2" like the log4j2 LogManager.getLogger()
	private static final int THREAD_TYPE_DEEP = 2;


	/**
	 * To be used to get the Log4j2 Logger in a class, eg. <code>private final static Logger LOG = Logr.get();</code>.
	 * To avoid the Logger declaration completely, use the class <code>Say</code>.
	 */
	public static Logger get() {
		return LogManager.getLogger();
		//return LogManager.getLogger(ReflectionUtil.getCallerClass(THREAD_TYPE_DEEP), PayloadMessageFactory.INSTANCE);
	}

}