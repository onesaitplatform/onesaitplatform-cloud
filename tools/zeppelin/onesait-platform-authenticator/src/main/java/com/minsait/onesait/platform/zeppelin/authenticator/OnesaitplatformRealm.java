package com.minsait.onesait.platform.zeppelin.authenticator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.minsait.onesait.platform.zeppelin.authenticator.Beans.NotebookInfoBean;
import com.minsait.onesait.platform.zeppelin.authenticator.Beans.UserInfoBean;
import com.minsait.onesait.platform.zeppelin.authenticator.Utils.Utils;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.zeppelin.notebook.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.hk2.api.ServiceLocatorFactory;

import com.google.gson.Gson;

public class OnesaitplatformRealm extends AuthorizingRealm {

	private String BASE_URL_OAUTH2 = "http://oauthservice:21000/oauth-server";
	private String BASE_URL_KEYCLOAK = "http://keycloak:8080/auth/realms";
	
	private String oauth2serverURL = "http://oauthservice:21000/oauth-server";
	private String controlpanelURL = "http://controlpanelservice:18000/controlpanel";

	
	private String CHECKTOKENPATH_URL_OAUTH2 = "/user";
	private String CHECKTOKENPATH_URL_KEYCLOAK = "/protocol/openid-connect/userinfo";	
	
	private String oauth2serverCheckTokenPath = "/user";
	private String controlpanelUserNotebooksPath = "/api/notebooks/listAllAndByProject/";

	
	private boolean avoidSSL = true;
	private String administratorRole = "ROLE_ADMINISTRATOR";

	private CloseableHttpClient httpClient;
	private final Gson gsonParser;
	
	private boolean keycloakEnabled;
	private String KEYCLOAK_ENV_KEY = "USE_KEYCLOAK";
	
	private String READ_ACCESSTYPE = "VIEW";
	private String EDIT_ACCESSTYPE = "EDIT";
	private String RUN_ACCESSTYPE = "RUN";
	
	private static final Logger LOG = LoggerFactory.getLogger(OnesaitplatformRealm.class);
	public static final String SERVICE_LOCATOR_NAME= "shared-locator";
	
	private AuthorizationService authorizationService;

	public OnesaitplatformRealm() {
		super();
		LOG.info("Init OnesaitplatformRealm v0.10.1");
		httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		gsonParser = new Gson();
		injectAuthorizationService();
		LOG.info("AuthorizationService inyected in osp realm");
		checkKeyCloakEnabled();
		setOauth2URLs();
		LOG.info("Default oauth2serverURL: " + oauth2serverURL);
		LOG.info("Default oauth2serverCheckTokenPath: " + oauth2serverCheckTokenPath);
		LOG.info("Default controlpanelURL: " + controlpanelURL);
		LOG.info("Default controlpanelUserNotebooksPath: " + controlpanelUserNotebooksPath);
	}
	
	private void injectAuthorizationService() {
		authorizationService = ServiceLocatorFactory.getInstance().find(SERVICE_LOCATOR_NAME).getService(AuthorizationService.class);
	}

	public void setOauth2serverURL(String oauth2serverURL) {
		this.oauth2serverURL = oauth2serverURL;
		LOG.info("Set Oauth2serverURL: " + oauth2serverURL);
	}

	public void setControlpanelURL(String controlpanelURL) {
		this.controlpanelURL = controlpanelURL;
		LOG.info("Set controlpanelURL: " + controlpanelURL);
	}

	public void setOauth2serverCheckTokenPath(String oauth2serverCheckTokenPath) {
		this.oauth2serverCheckTokenPath = oauth2serverCheckTokenPath;
		LOG.info("Set oauth2serverCheckTokenPath: " + oauth2serverCheckTokenPath);
	}
	
	public void setControlpanelUserNotebooksPath(String controlpanelUserNotebooksPath) {
		this.controlpanelUserNotebooksPath = controlpanelUserNotebooksPath;
		LOG.info("Set controlpanelUserNotebooksPath: " + controlpanelUserNotebooksPath);
	}

	public void setAvoidSSL(boolean avoidSSL) {
		this.avoidSSL = avoidSSL;
		LOG.info("Avoid SSL Changed: " + avoidSSL);
		if (!this.avoidSSL) {//need certificates ssl
			httpClient = HttpClients.createDefault();
		}
	}
	
	public void setAdministratorRole(String administratorRole) {
		this.administratorRole = administratorRole;
	}
	
	public void setKeycloakEnabled(boolean keycloakEnabled) {
		this.keycloakEnabled = keycloakEnabled;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		LOG.info("Execute doGetAuthorizationInfo");
		Set<String> role_set = new HashSet<String>(); 
		if(((UserInfoBean) principals.getPrimaryPrincipal()).getAuthorities().contains(administratorRole)) {
			role_set.add("admin");
		}
		else {
			role_set.add("analytics");
		}
		return new SimpleAuthorizationInfo(role_set);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authToken;
		if (token.getUsername() == null || token.getUsername().equals("")) {
			throw new AuthenticationException("Empty usernames are not allowed by this realm.");
		}
		try {
			UserInfoBean infoUser = authenticateUserOauth(token.getUsername(), String.valueOf(token.getPassword()));
			if(infoUser == null || infoUser.getPrincipal() == null || !infoUser.getPrincipal().equals(token.getUsername())){
				throw new AuthenticationException("Invalid token with user");
			}
			LOG.info("{} successfully login via onesaitPlatform", token.getUsername());
			
			loadNotebookAuthFromPlatform(infoUser, String.valueOf(token.getPassword()));
			
			return new SimpleAuthenticationInfo(infoUser, token.getPassword(), "onesaitplatformRealm");
		} catch (org.apache.http.auth.AuthenticationException|ParseException|IOException|URISyntaxException e) {
			throw new AuthenticationException("Problem comunicating with oauthserver: ", e);
		}
	}

	private UserInfoBean authenticateUserOauth(String username, String oauth2token)
			throws ParseException, IOException, org.apache.http.auth.AuthenticationException, URISyntaxException {
		URIBuilder builder = new URIBuilder(oauth2serverURL);
		if (keycloakEnabled) {
			builder.setPath(builder.getPath() + "/" + Utils.getVerticalFromToken(oauth2token) + oauth2serverCheckTokenPath);
			
		} else {
			builder.setPath(builder.getPath() + oauth2serverCheckTokenPath);
		}
		LOG.info("URL: " + builder.build());
		HttpPost httpPost = new HttpPost(builder.build());

		httpPost.addHeader("Authorization",oauth2token);

		CloseableHttpResponse response = httpClient.execute(httpPost);
		LOG.info("Status code login oauth server: {} ", response.getStatusLine().getStatusCode());

		if (response.getStatusLine().getStatusCode() / 100 != 2) {
			throw new AuthenticationException("Wrong code answer: " + response.getStatusLine().getStatusCode());
		}

		String jsonAnswer = EntityUtils.toString(response.getEntity());
		LOG.info("{} answer from oauth server", username);
		
		LOG.info("Response: {} ", jsonAnswer);

		return gsonParser.fromJson(jsonAnswer, UserInfoBean.class);
	}
	
	private void loadNotebookAuthFromPlatform(UserInfoBean infoUser, String oauth2token) {
		try {

			URIBuilder builder = new URIBuilder(controlpanelURL);
			builder.setPath(builder.getPath() + controlpanelUserNotebooksPath);
			LOG.info("URL: " + builder.build());
			HttpGet httpGet = new HttpGet(builder.build());

			httpGet.addHeader("Authorization", oauth2token);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			LOG.info("Status code notebook user list: {} ", response.getStatusLine().getStatusCode());

			if (response.getStatusLine().getStatusCode() / 100 != 2) {
				throw new AuthenticationException("Wrong code answer: " + response.getStatusLine().getStatusCode());
			}

			String jsonAnswer = EntityUtils.toString(response.getEntity());

			LOG.info("Response controlpanel: {} ", jsonAnswer);

			Type listType = new TypeToken<List<NotebookInfoBean>>() {}.getType();
			List<NotebookInfoBean> lnib = gsonParser.fromJson(jsonAnswer, listType);
			
			Set<String> defaultEntities = new HashSet<>();
			defaultEntities.add("admin");
			
			for(NotebookInfoBean nib : lnib){
				String idzep = nib.getIdzep();
				
				//Set default
				authorizationService.setReaders(idzep, defaultEntities, true);
				authorizationService.setWriters(idzep, defaultEntities, true);
				authorizationService.setRunners(idzep, defaultEntities, true);
				authorizationService.setOwners(idzep, defaultEntities, true);
				
				Set previousSet;
				if (READ_ACCESSTYPE.equals(nib.getAccessType())) {
					previousSet = authorizationService.getReaders(idzep);
					LOG.info("ReadOnly: {} with {} ",idzep, previousSet);
				} else if (RUN_ACCESSTYPE.equals(nib.getAccessType())) {
					previousSet = authorizationService.getReaders(idzep);
					LOG.info("ReadOnly: {} with {} ",idzep, previousSet);
					previousSet = authorizationService.getRunners(idzep);
					LOG.info("Runner: {} with {} ",idzep, previousSet);
				} else {
					previousSet = authorizationService.getWriters(idzep);
					LOG.info("WriteAccess: {} with {} ", idzep,previousSet);
				}
				if (previousSet == null) {
					LOG.error("Notebook: " + nib.getIdentification() + " with NoteId: " + idzep + " doesn't exist in zeppelin, please remove it from onesait platform");
				} else {
					previousSet.add(infoUser.getPrincipal());
					if (READ_ACCESSTYPE.equals(nib.getAccessType())) {
						authorizationService.setReaders(idzep, previousSet, true);
						LOG.info("Setting New ReadOnly: {} with {} ", idzep, previousSet);
					} else if (RUN_ACCESSTYPE.equals(nib.getAccessType())) {
						authorizationService.setReaders(idzep, previousSet, true);
						LOG.info("Setting New ReadOnly: {} with {} ", idzep, previousSet);
						authorizationService.setRunners(idzep, previousSet, true);
						LOG.info("Setting New Runner: {} with {} ", idzep, previousSet);
					} else if (EDIT_ACCESSTYPE.equals(nib.getAccessType())) {
						authorizationService.setWriters(idzep, previousSet, true);
						LOG.info("Setting New WriteAccess: {} with {} ", idzep, previousSet);
					}
				}
			}
	    } catch (Exception e) {
	      LOG.error("Fail to broadcastReloadedNoteList", e);
	    }
	}
	
	private void setOauth2URLs() {
		if (keycloakEnabled) {
			oauth2serverURL = BASE_URL_KEYCLOAK;
			oauth2serverCheckTokenPath = CHECKTOKENPATH_URL_KEYCLOAK;
			
		} else {
			oauth2serverURL = BASE_URL_OAUTH2;
			oauth2serverCheckTokenPath = CHECKTOKENPATH_URL_OAUTH2;
		}
	}
	
	private void checkKeyCloakEnabled() {
		Map <String, String> envvars = System.getenv();
		if (envvars.containsKey(KEYCLOAK_ENV_KEY)) {
			String keycloack = envvars.get(KEYCLOAK_ENV_KEY);
			keycloakEnabled = Boolean.parseBoolean(keycloack);
		} else {
			LOG.info(KEYCLOAK_ENV_KEY + " not found in env vars setting to disabled");
			keycloakEnabled = false;
		}
		LOG.info("Keycloack enabled is " + keycloakEnabled);
	}

}
