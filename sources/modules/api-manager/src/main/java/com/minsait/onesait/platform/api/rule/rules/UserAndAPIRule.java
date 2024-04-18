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
package com.minsait.onesait.platform.api.rule.rules;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.security.UserPrincipal;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Component
@Rule
@Slf4j
public class UserAndAPIRule extends DefaultRuleBase {

	@Autowired
	private ApiManagerService apiManagerService;

	@Autowired
	private UserService userService;

	@Autowired
	private MultitenancyService masterUserService;

	@Autowired
	private ConfigDBDetailsService detailsService;

	@Autowired(required = false)
	private JWTService jwtService;

	@Priority
	public int getPriority() {
		return 2;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = facts.get(RuleManager.REQUEST);
		return request != null && canExecuteRule(facts);
	}

	@Action
	public void setFirstDerivedData(Facts facts) {

		final Map<String, Object> data = facts.get(RuleManager.FACTS);

		final String PATH_INFO = (String) data.get(Constants.PATH_INFO);
		final String TOKEN = (String) data.get(Constants.AUTHENTICATION_HEADER);
		final String JWT_TOKEN = (String) data.get(Constants.JWT_TOKEN);
		User user = null;
		try {
			final UserDetails details = detailsService.loadUserByUserToken(TOKEN);
			if (details != null) {
				SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(details,
						details.getPassword(), details.getAuthorities()));
			}
			user = userService.getUserByToken(TOKEN);
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}

		Api api = null;
		if (user == null && JWT_TOKEN.length() > 0 && jwtService != null) {
			final Authentication auth = jwtService.getAuthentication(JWT_TOKEN);
			if (auth != null) {
				if (auth.getPrincipal() instanceof UserPrincipal) {
					final UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
					MultitenancyContextHolder.setTenantName(principal.getTenant());
					MultitenancyContextHolder.setVerticalSchema(principal.getVerticalSchema());
				}
				detailsService.loadUserByUsername(auth.getName());
				SecurityContextHolder.getContext().setAuthentication(auth);
				user = userService.getUser(auth.getName());
			}
		}
		if (user == null) {

			stopAllNextRules(facts, "Token " + TOKEN + " not recognized for user ", DefaultRuleBase.ReasonType.SECURITY,
					HttpStatus.UNAUTHORIZED);

		}

		else {
			api = apiManagerService.getApi(PATH_INFO);
			if (api == null) {
				stopAllNextRules(facts, "API not found with Token :" + TOKEN + " and Path Info" + PATH_INFO,

						DefaultRuleBase.ReasonType.SECURITY, HttpStatus.NOT_FOUND);
			}
		}

		data.put(Constants.USER, user);
		data.put(Constants.API, api);
	}

}
