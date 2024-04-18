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
package com.minsait.onesait.platform.security.plugin.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class PluginLoadCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		final String oauthEnabled = context.getEnvironment()
				.getProperty("onesaitplatform.authentication.oauth.enabled");
		final String ospKeycloak = context.getEnvironment()
				.getProperty("onesaitplatform.authentication.oauth.osp-keycloak");
		if (oauthEnabled != null && ospKeycloak != null) {
			return Boolean.valueOf(oauthEnabled) && Boolean.valueOf(ospKeycloak);
		} else if (ospKeycloak != null) {
			return Boolean.valueOf(ospKeycloak);
		}
		return false;
	}

}
