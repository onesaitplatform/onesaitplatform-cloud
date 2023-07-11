package com.minsait.onesait.platform.zeppelin.authenticator.Utils;

import javax.inject.Inject;

import org.apache.zeppelin.notebook.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationServiceManager {
	
	private final AuthorizationService authorizationService;
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthorizationServiceManager.class);
	
	@Inject
	public AuthorizationServiceManager(AuthorizationService authorizationService) {
		this.authorizationService = authorizationService;
		LOG.info("---------------------------------------------- Try intern ---------------------------------------------- ");
		LOG.info(this.authorizationService.toString());
	}

	public AuthorizationService getAuthorizationService() {
		return authorizationService;
	}

}
