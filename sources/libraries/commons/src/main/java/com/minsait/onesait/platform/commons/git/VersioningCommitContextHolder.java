/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.commons.git;

public class VersioningCommitContextHolder {

	private VersioningCommitContextHolder() {
		// NO-OP
	}

	private static final ThreadLocal<String> MESSAGE = new ThreadLocal<>();

	private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> PROCESS_POST_UPDATE = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> PROCESS_POST_CREATE = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> PROCESS_POST_DELETE = new ThreadLocal<>();

	public static void setCommitMessage(String message) {
		MESSAGE.set(message);
	}

	public static String getCommitMessage() {
		return MESSAGE.get();
	}

	public static void setUserId(String userId) {
		USER_ID.set(userId);
	}

	public static String getUserId() {
		return USER_ID.get();
	}

	public static void setProcessPostCreate(Boolean bool) {
		PROCESS_POST_CREATE.set(bool);
	}

	public static Boolean isProcessPostCreate() {
		final Boolean bool = PROCESS_POST_CREATE.get();
		if (bool == null) {
			return true;
		} else {
			return bool;
		}
	}

	public static void setProcessPostUpdate(Boolean bool) {
		PROCESS_POST_UPDATE.set(bool);
	}

	public static Boolean isProcessPostUpdate() {
		final Boolean bool = PROCESS_POST_UPDATE.get();
		if (bool == null) {
			return true;
		} else {
			return bool;
		}
	}

	public static void setProcessPostDelete(Boolean bool) {
		PROCESS_POST_DELETE.set(bool);
	}

	public static Boolean isProcessPostDelete() {
		final Boolean bool = PROCESS_POST_DELETE.get();
		if (bool == null) {
			return true;
		} else {
			return bool;
		}
	}

	public static void setProcessPostAllEvents(Boolean bool) {
		PROCESS_POST_CREATE.set(bool);
		PROCESS_POST_UPDATE.set(bool);
		PROCESS_POST_DELETE.set(bool);
	}

}
