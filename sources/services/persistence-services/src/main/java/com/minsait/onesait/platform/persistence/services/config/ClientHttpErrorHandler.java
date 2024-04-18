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
package com.minsait.onesait.platform.persistence.services.config;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import com.google.common.io.ByteStreams;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

public class ClientHttpErrorHandler implements ResponseErrorHandler {

	private final ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {

		try {

			if (response.getBody() != null) {
				final String json = new String(ByteStreams.toByteArray(response.getBody()), Charset.forName("UTF-8"));
				throw new DBPersistenceException(json);
			} else
				errorHandler.handleError(response);

		} finally {
			response.close();
		}
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return errorHandler.hasError(response);
	}

}
