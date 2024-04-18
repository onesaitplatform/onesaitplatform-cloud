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
package com.minsait.onesait.platform.persistence.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ResponseUtil {
	private ResponseUtil() {
	}

	public static void closeResponse(CloseableHttpResponse response) {
		if (response == null) {
			return;
		}

		try {
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				entity.getContent().close();
			}
		} catch (UnsupportedOperationException | IOException e) {
		    log.error("" + e);
		} finally {
			try {
				response.close();
			} catch (IOException e) {
			    log.error("" + e);
			}
		}
	}
}