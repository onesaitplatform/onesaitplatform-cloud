/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.config.versioning;

public class VersioningException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public VersioningException(String message, Throwable t) {
		super(message, t);
	}

	public VersioningException(String message) {
		super(message);
	}

	public VersioningException(Throwable t) {
		super(t);
	}

	public static String processErrorMessageToFront(Throwable e) {
		final StringBuilder builder = new StringBuilder(e.getMessage());
		Throwable thrower = e;
		boolean doContinue = true;
		while(doContinue) {
			if(thrower.getCause() == null) {
				doContinue = false;
			}else {
				builder.append("\n");
				builder.append(thrower.getCause().getLocalizedMessage());
				thrower = thrower.getCause();
			}
		}
		return builder.toString();
	}

}
