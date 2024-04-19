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
package com.minsait.onesait.platform.config.services.token;

import java.util.List;

import com.minsait.onesait.platform.config.dto.ClientPlatformTokenDTO;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstanceSimulation;
import com.minsait.onesait.platform.config.model.Token;

public interface TokenService {

	public Token generateTokenForClient(ClientPlatform clientPlatform);

	public Token getToken(ClientPlatform clientPlatform);

	public Token getTokenByToken(String token);

	public Token getTokenByID(String id);

	public void deactivateToken(Token token, boolean active);

	List<Token> getTokens(ClientPlatform clientPlatform);
	
	public ClientPlatformTokenDTO getClientPlatformIdByTokenName(String tokenName);

	List<ClientPlatformInstanceSimulation> getSimulations(Token token);

}
