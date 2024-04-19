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
package com.minsait.onesait.platform.config.model;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.vault.VaultException;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.RoleId;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.SecretId;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.LifecycleAwareSessionManager;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.client.VaultHttpHeaders;
import org.springframework.vault.client.VaultResponses;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Policy;
import org.springframework.vault.support.Policy.Rule;
import org.springframework.vault.support.VaultHealth;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponseSupport;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.exception.GenericOPException;

/**
 * A basic set of administrative Vault operations
 * Tutorial:
 *  - Creates AppRoles, routes and policies associated to it.
 *  - Creates AppRoles than cat create other AppRoles.
 *  This set of classes tries to hide the implementation details
 *  of the Vault, like the policies to allow only the usage of routes by
 *  the AppRoles specified. Each route has an engine associated to
 *  store/retrieve data and to encrypt/decrypt/sign/verify data.
 *  - Is also able to list the AppRoles available.
 * how-to:
 *  - Create an AppRole for a new Tenant
 *  You should use an AppRole with enough rights to
 *  create new AppRoles, paths after /onesait/ and assign policies
 *  to the new created AppRole.
 *  This AppRole must have been created when Vault is deployed.
 *
 *  Example:
    URL vurl = new URL("https://vault.organization.org:8200");
  	try (HashiCorpVaultAdmin vc = new HashiCorpVaultAdmin(vurl, role_id, role_secret)) {
	if (vc.isInitialized() && !vc.isSealed() && !vc.isStandby()) {
		Map<String, String> r = vc.createAppRole(name_of_new_role);
		if (r != null) {
			// And now using the new role
			try (final HashiCorpVaultUser hc = new HashiCorpVaultUser(vurl, r)) {
				System.out.println("random number:" + hc.generateRandom(8192));
				...
	        }
	        catch (final GenericOPException e)
	        {
	        	// Do something
	        }
        }
	  }
	} catch (final GenericException e)
	{
		// Do something
	}

 * explanation:
 *   An AppRole is one type of authentication that needs two
 *   universally unique identifier (UUID) of 16 bytes to login and
 *   then generates internally a series of tokens as proof of identity
 *   to perform its tasks.
 * reference:
 *   See the methods documentation
 */
final public class HashiCorpVaultAdmin  implements AutoCloseable {

	// To be used as suffixes of policy names
	private static final String policystore = ".policystore";
	private static final String policycipher = ".policycipher";
	private static final String policyadmin = ".policyadmin";
	// To be used as path prefixes
	private static final String pathstore = "/onesait/pathstore/";
	private static final String pathcipher ="/onesait/pathcipher/";

	private final VaultTemplate vaultTemplate;
	private final VaultSysOperations sysv;
	private final VaultHealth vaultHealthResponse;
	private final ClientHttpRequestFactory clientHttpRequestFactory;
	private final URL connectionUrl;  // To be used in the list methods
	final LifecycleAwareSessionManager sess; // To get the current token for the list methods
	private transient final ThreadPoolTaskScheduler taskSchedule;

	/**
	 * Constructor
	 * It is recommended to be used as a resource inside a try, in this way
	 * it is always closed. If it is not closed, the program will never stop.
	 * @param url   the url to the Vault in the way of https://hostname:port
	 * @param  roleId      the roleId of the AppRole that has rights over /onesait/*
	 * @param  roleSecret  the role secret or password
	 * @throws GenericOPException  Any Error
	 * @throws MalformedURLException if the url is incorrect
	 */
	public HashiCorpVaultAdmin(final String url, final String roleId, final String roleSecret) throws GenericOPException, MalformedURLException {
		this(new URL(url), roleId, roleSecret);
	}

	/**
	 * Constructor
	 * It is recommended to be used as a resource inside a try, in this way
	 * it is always closed. If it is not closed, the program will never stop.
	 * @param url   the url to the Vault in the way of https://hostname:port
	 * @param ids  the ids returned by HashiCorpVaultAdmin.createAppRoleAppRole
	 *             must contain values for role_id and secret_id
	 *             of the AppRole with rights over /onesait/*
	 * @throws GenericOPException  Any Error
	 * @throws MalformedURLException if the url is incorrect
	 */
	public HashiCorpVaultAdmin(final String url, final Map<String, String> ids) throws GenericOPException, MalformedURLException {
		this(new URL(url), ids.get("role_id"), ids.get("secret_id"));
	}

	/**
	 * Constructor
	 * It is recommended to be used as a resource inside a try, in this way
	 * it is always closed. If it is not closed, the program will never stop.
	 * @param url   the url to the Vault in the way of https://hostname:port
	 * @param ids  the ids returned by HashiCorpVaultAdmin.createAppRoleAppRole
	 *             must contain values for role_id and secret_id
	 *             of the AppRole with rights over /onesait/*
	 * @throws GenericOPException  Any Error
	 * @throws MalformedURLException if the url is incorrect
	 */
	public HashiCorpVaultAdmin(final URL url, final Map<String, String> ids) throws GenericOPException, MalformedURLException {
		this(url, ids.get("role_id"), ids.get("secret_id"));
	}

	/**
	 * Constructor
	 * It is recommended to be used as a resource inside a try, in this way
	 * it is always closed. If it is not closed, the program will never stop.
	 * @param url   the url to the Vault in the way of https://hostname:port
	 * @param  roleId      the roleId of the AppRole that has rights over /onesait/*
	 * @param  roleSecret  the role secret or password
	 * @throws GenericOPException Any Error
	 */
	public HashiCorpVaultAdmin(final URL url, final String roleId, final String roleSecret) throws GenericOPException {
		// Validate and transform URL to VaultEndpoint
		if (url == null || !"https".equalsIgnoreCase(url.getProtocol())) {
			throw new GenericOPException(
					"It is really unsafe to use HashiCorp Vault without https. Only available with https.");
		}
		// The url of the Vault to connect to. Only the https://hostname:port parts are used.
		final VaultEndpoint vep = new VaultEndpoint();
		vep.setScheme(url.getProtocol());
		vep.setHost(url.getHost());
		vep.setPort(url.getPort());
		try {
			connectionUrl = new URL(vep.getScheme()+"://"+vep.getHost()+":"+String.valueOf(vep.getPort()));
		} catch (final MalformedURLException e) {
			throw new GenericOPException("malformed url", e);
		}
		// this.token = token;

		clientHttpRequestFactory = new SimpleClientHttpRequestFactory();

		final AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
				.roleId(RoleId.provided(roleId))
				// We do not use the pull mode (obtain the first secretId) from auth/approle/role/(role-name)/secret-id
				// because it only requires the role name and have rights to use that path
				// Therefore, a never expiring secretId is used (with the chance to obtain new ones)
				.secretId(SecretId.provided(roleSecret))
				.build();

		final ClientAuthentication clientAuthentication = new AppRoleAuthentication(options, VaultClients.createRestTemplate(vep, clientHttpRequestFactory));
		// Used to recreate the new tokens as the old ones are about to expire
		taskSchedule = new ThreadPoolTaskScheduler();
		taskSchedule.initialize();
		sess = new LifecycleAwareSessionManager(clientAuthentication, taskSchedule, new RestTemplate());

		vaultTemplate = new VaultTemplate(vep, clientHttpRequestFactory, sess);

		// The operations that use sysv do not need authentication
		sysv = vaultTemplate.opsForSys();
		try {
			vaultHealthResponse = sysv.health();
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("Unable to connect to vault", e);
		}
	}

	/**
	 * To use a https proxy to connect to the Vault
	 * @param host  the host of the proxy
	 * @param port  the port of the proxy
	 */
	public void setProxy(final String host, final int port) {
		final Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
		((SimpleClientHttpRequestFactory) clientHttpRequestFactory).setProxy(proxy);
	}

	/**
	 * Stop the Scheduler so the object can be released
	 * The ideal way to be used is in a try with resources.
	 */
	@Override
	public void close() {
		taskSchedule.shutdown();
	}

	/**
	 * Doesn't require authentication
	 *
	 * @return true if the vault is initialized, i.e. it can be used
	 */
	public boolean isInitialized() {
		return vaultHealthResponse.isInitialized();
	}

	/**
	 * Doesn't require authentication
	 *
	 * @return true if the vault is sealed, i.e., it can't be used until is unsealed
	 */
	public boolean isSealed() {
		return vaultHealthResponse.isSealed();
	}

	/**
	 * Doesn't require authentication
	 *
	 * @return true if the vault is in standby mode, i.e., it can't be used
	 */
	public boolean isStandby() {
		return vaultHealthResponse.isStandby();
	}

	/**
	 * Doesn't require authentication
	 *
	 * @return the version of the vault program
	 */
	public String version() {
		return vaultHealthResponse.getVersion();
	}

	// borrowed from spring vault to create new requests
	private static <T> T requireResponse(@Nullable final T response) throws GenericOPException {
		if (response != null) {
			return response;
		}
		throw new GenericOPException("Vault response must not be null");
	}

	// borrowed from spring vault to create new requests
	private static <T> HttpEntity<T> emptyNamespace(@Nullable final T body) {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(VaultHttpHeaders.VAULT_NAMESPACE, "");
		return new HttpEntity<>(body, headers);
	}

	// class that stores the values of the fields of the body (in json) of the
	// request
	// required by spring to make the rest request
	// This request has no return data, no Response
	private static class VaultCreateRoleRequest {
		@JsonProperty("role_name")
		private final String roleName;

		// policies name list
		@JsonProperty("policies")
		private final List<String> policies;

		// token time to live
		@JsonProperty("token_ttl")
		private final String token_ttl;

		// constructor
		VaultCreateRoleRequest(final String roleName, final List<String> policies, final String token_ttl) {
			this.roleName = roleName;
			this.policies = policies;
			this.token_ttl = token_ttl;
		}
	}

	// class that stores the values of the fields of the request
	private static class VaultGetRoleIdRequest {
		@JsonProperty("role_name")
		private final String roleName;

		VaultGetRoleIdRequest(final String roleName) {
			this.roleName = roleName;
		}
	}

	// Class to retrieve the response from the rest call
	// jackson requires the class not to be inside a method,
	// and if it is an inner class, it should be static
	private static class VaultGetRoleIdDataResponse {

		@JsonProperty("role_id")
		private final String roleId;

		public VaultGetRoleIdDataResponse(final String roleId) {
			this.roleId = roleId;
		}

		public String geRoleId() {
			return roleId;
		}
	}

	// This class stores the global answer, being VaultGetRoleIdDataResponse
	// where the inner data is stored
	private static class VaultGetRoleIdResponse extends VaultResponseSupport<Map<String, VaultGetRoleIdDataResponse>> {
	}

	// Class to retrieve the response from the rest call
	// jackson requires the class not to be inside a method,
	// and if it is an inner class, it should be static and have
	// a constructor for every type.
	// {"secret_id":"uuid","secret_id_accessor":"uuid","secret_id_ttl":0}
	// It does strange things to fill the class, like creating 3 instances
	// calling the constructor of the types (2 string 1 int)
	private static class VaultGetSecretIdDataResponse {
		private String dataString;
		private int dataInt;  // stores the secret_id_ttl

		public VaultGetSecretIdDataResponse(final String dataString) {
			this.dataString = dataString;
		}

		public VaultGetSecretIdDataResponse(final int dataint) {
			dataInt = dataint;
		}

		public String getSecretId() {
			return dataString;
		}

		public String getSecretIdAccessor() {
			return dataString;
		}

		public int getSecretIdttl() {
			return dataInt;
		}
	}

	// This class stores the answer, being VaultGetRoleIdDataResponse where the data is stored
	// {"request_id":"uuid","lease_id":"","renewable":false,"lease_duration":0,"data":{VaultGetSecretIdDataResponse},"wrap_info":null,"auth":null}
	private static class VaultGetSecretIdResponse
	extends VaultResponseSupport<Map<String, VaultGetSecretIdDataResponse>> {
	}


	/*
	 * Creates the policies that enable to create other AppRoles
	 * but does not allows to save/retrieve/sign/encrypt/decrypt values
	 * @param role    to create the name of the policies
	 * @param roleId  to create the paths to apply the capabilities
	 */
	private void createPoliciesAppRole(final String role, final String roleId) {
		// FIXME: Vault 1.11.0 says 'rules' is deprecated, please use 'policy' instead (spring sends 'rules')
		// TODO: Probably the capabilities can be reduced.

		// FIXME: ¿El + o ^ o el nombre del approle actual? ¿se obtiene en algún sitio?
		final Policy pol = Policy.of(Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path("auth/approle/role/+/role-id").build()
				, Rule.builder().capabilities("CREATE"        , "LIST", "UPDATE", "DELETE").path("auth/approle/role/*").build()
				, Rule.builder().capabilities("CREATE"                , "UPDATE"          ).path("auth/approle/role/+/secret-id").build()
				, Rule.builder().capabilities("CREATE", "READ"        , "UPDATE", "DELETE").path("sys/policy/*").build()
				, Rule.builder().capabilities("CREATE", "READ"        , "UPDATE", "DELETE").path("sys/auth/approle").build()
				, Rule.builder().capabilities("CREATE", "READ"        , "UPDATE", "DELETE").path("sys/auth/approle/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path("auth/approle/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path("sys/mounts/onesait/*").build()
				);
		sysv.createOrUpdatePolicy(role, pol);
	}

	/*
	 * Creates the policies to apply to the paths assigned to the role with roleId
	 * @param role    to create the name of the policies
	 * @param roleId  to create the paths to apply the capabilities
	 */
	private void createPolicies(final String role, final String roleId) {
		// FIXME: Vault 1.11.0 says 'rules' is deprecated, please use 'policy' instead (spring sends 'rules')
		// TODO: Probably the capabilities can be reduced.
		Policy pol = Policy.of(Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathstore + roleId + "/*").build());
		sysv.createOrUpdatePolicy(role+policystore, pol);
		pol = Policy.of(Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId).build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId + "/random/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId + "/encrypt/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId + "/decrypt/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId + "/keys/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId + "/sign/*").build()
				, Rule.builder().capabilities("CREATE", "READ", "LIST", "UPDATE", "DELETE").path(pathcipher + roleId + "/verify/*").build()
				);
		sysv.createOrUpdatePolicy(role+policycipher, pol);
	}

	/*
	 * Delete the policies for the role
	 * @param role
	 */
	private void deletePolicies(final String role) {
		sysv.deletePolicy(role);
		sysv.deletePolicy(role+policycipher);
		sysv.deletePolicy(role+policystore);
	}

	/*
	 * Create exclusive routes for the role
	 * @param roleId the id of the role
	 */
	private void createRoutes(final String roleId) throws GenericOPException {
		try {
			// the transit engine allows cipher operations
			final VaultMount mountToCipher = VaultMount.create("transit");
			// the key/value engine allows store/retrive operations
			final VaultMount mountToStore = VaultMount.create("kv");
			sysv.mount(pathstore + roleId, mountToStore);
			sysv.mount(pathcipher + roleId, mountToCipher);
		}
		catch (final VaultException e) {
			throw new GenericOPException("Path already in use", e);
		}
	}

	/*
	 * Delete the exclusive routes for the role
	 * @param roleId the id of the role
	 */
	private void deleteRoutes(final String roleId) {
		sysv.unmount(pathstore + roleId);
		sysv.unmount(pathcipher + roleId);
	}

	/**
	 * Create an AppRole in Vault using this API
	 * https://www.vaultproject.io/api-docs/auth/approle
	 *
	 * @param role     the name of the role
	 * @return a Map with the following keys:
	 *  - role_id   must be used to identify the role
	 *  - secret_id must be used to identify the role (like a password)
	 *  or null if the role already exists
	 * @throws GenericOPException  Any Error
	 */
	public @Nullable Map<String, String> createAppRoleUser(final String role)
			throws GenericOPException {
		return createAppRole(role, false);
	}


	/**
	 * Create an AppRole that can create other AppRoles in Vault using this API
	 * https://www.vaultproject.io/api-docs/auth/approle
	 *
	 * @param role     the name of the role
	 * @return a Map with the following keys:
	 *  - role_id   must be used to identify the role
	 *  - secret_id must be used to identify the role (like a password)
	 *  or null if the role already exists
	 * @throws GenericOPException  Any Error
	 */
	public @Nullable Map<String, String> createAppRoleAppRole(final String role)
			throws GenericOPException {
		return createAppRole(role, true);
	}

	/*
	 * Create an AppRole in Vault using this API
	 * https://www.vaultproject.io/api-docs/auth/approle
	 *
	 * FIXME: Decir las rutas donde creará los secretos
	 * (para poder ser utilizado directamente en spring-boot)
	 *
	 * @param role     the name of the role
	 * @return a Map with the following keys:
	 *  - role_id   must be used to identify the role
	 *  - secret_id must be used to identify the role (like a password)
	 *  FIXME: ¿Como parte de la respuesta?
	 *  or null if the role already exists
	 * @throws GenericOPException  Any Error
	 */
	private @Nullable Map<String, String> createAppRole(final String role, boolean canCreateAppRoles)
			throws GenericOPException {
		if (StringUtils.hasText(role)) {
			// Using spring low level methods to call the api, authenticated with the .doWithSession
			final VaultGetRoleIdRequest vaultGetRoleIdRequest = new VaultGetRoleIdRequest(role);
			try {
				// If the role already exists, the role_id can be read, otherwise probably throws an exception
				final VaultGetRoleIdResponse roleIdData = requireResponse(vaultTemplate.doWithSession(restOperations -> {
					try {
						final ResponseEntity<VaultGetRoleIdResponse> exchange = restOperations.exchange(
								"/auth/approle/role/" + role + "/role-id", HttpMethod.GET,
								emptyNamespace(vaultGetRoleIdRequest), VaultGetRoleIdResponse.class);

						Assert.state(exchange.getBody() != null, "createAppRole response must not be null");

						return exchange.getBody();
					} catch (final HttpStatusCodeException e) {
						throw VaultResponses.buildException(e);
					}
				}));
				// If the role exists, return null
				if (roleIdData.getData() != null && roleIdData.getData().get("role_id") != null &&
						StringUtils.hasText(roleIdData.getData().get("role_id").geRoleId())) {
					return null;
				}
			} catch (final VaultException e)
			{
				// If the vault answer is an Exception, here we do nothing because
				// If the user doesn't exist, it is expected
				// if the vault is sealed (for example), the next attempts will throw the exception
			}

			// The names of the policies to apply to the paths of the role
			// The policies do not need to exist!
			final List<String> policies = new ArrayList<> (2);
			if (canCreateAppRoles) {
				policies.add(role);
			} else {
				policies.add(role+policystore);
				policies.add(role+policycipher);
			}
			// First create the role
			vaultTemplate.doWithSession(restOperations -> {
				try {
					// the token will be renewed every 15m, but not the secret_id
					final VaultCreateRoleRequest vcrRequest = new VaultCreateRoleRequest(role, policies, "15m");
					// Does not return http body or throws an exception in case of error
					final ResponseEntity<Void> exchange = restOperations.exchange("/auth/approle/role/" + role,
							HttpMethod.POST, emptyNamespace(vcrRequest), Void.class);
					return null;
				} catch (final HttpStatusCodeException e) {
					throw VaultResponses.buildException(e);
				}
			});
			// Then read (again) the role id
			final VaultGetRoleIdResponse roleIdData = requireResponse(vaultTemplate.doWithSession(restOperations -> {
				try {
					final ResponseEntity<VaultGetRoleIdResponse> exchange = restOperations.exchange(
							"/auth/approle/role/" + role + "/role-id", HttpMethod.GET,
							emptyNamespace(vaultGetRoleIdRequest), VaultGetRoleIdResponse.class);

					Assert.state(exchange.getBody() != null, "createAppRole response must not be null");

					return exchange.getBody();
				} catch (final HttpStatusCodeException e) {
					throw VaultResponses.buildException(e);
				}
			}));
			final String roleId = roleIdData.getData().get("role_id").geRoleId();
			// Now get a valid secret_id (roleId is not needed!)
			final String secretId = getSecretId(role);
			// prepare the return
			final Map<String, String> result = new HashMap<>(2);
			System.out.println("role_id:" + roleId + ",secret_id" + secretId);
			result.put("role_id", roleId);
			result.put("secret_id", secretId);
			// the final touch to the AppRole
			if (canCreateAppRoles) {
				createPoliciesAppRole(role, roleId);
			} else {
				createRoutes(roleId);
				createPolicies(role, roleId);
			}
			return result;
		}
		throw new GenericOPException("Unable to create null role or without policies");
	}

	/**
	 * To get a new secret_id for an AppRole
	 * @param role  the role name
	 * @return  the new secret_id
	 * @throws GenericOPException in case of error
	 */
	public String getSecretId(final String role) throws GenericOPException {
		if (StringUtils.hasText(role)) {
			// Now get a valid secret_id and secret_id_accessor (roleId is not needed!)
			final VaultGetSecretIdResponse secretIdData1 = requireResponse(
					vaultTemplate.doWithSession(restOperations -> {
						try {
							final ResponseEntity<VaultGetSecretIdResponse> exchange = restOperations.exchange(
									"/auth/approle/role/" + role + "/secret-id", HttpMethod.POST, emptyNamespace(null),
									VaultGetSecretIdResponse.class);

							Assert.state(exchange.getBody() != null, "createAppRole response must not be null");

							return exchange.getBody();
						} catch (final HttpStatusCodeException e) {
							throw VaultResponses.buildException(e);
						}
					}));
			if (null != secretIdData1.getData() && null != secretIdData1.getData().get("secret_id")) {
				return secretIdData1.getData().get("secret_id").getSecretId();
			}
			else {
				throw new GenericOPException("Unable to get new secret id");
			}
		}
		else {
			throw new GenericOPException("Unable to get new secret id");
		}
	}

	/**
	 * delete the AppRole, its paths and policies
	 * @param role  the role name
	 * @param roleId the id of the role
	 * @throws GenericOPException in case of error
	 */
	public void deleteAppRole(final String role, final String roleId)
			throws GenericOPException {
		// Using spring low level methods to call the api, authenticated with the
		// .doWithSession
		if (StringUtils.hasText(role)) {
			try {
				deleteRoutes(roleId);
				deletePolicies(role);

				vaultTemplate.doWithSession(restOperations -> {
					try {
						// Does not return http body or throws an exception in case of error
						final ResponseEntity<Void> exchange = restOperations.exchange("/auth/approle/role/" + role,
								HttpMethod.DELETE, emptyNamespace(null), Void.class);
						return null;
					} catch (final HttpStatusCodeException e) {
						throw VaultResponses.buildException(e);
					}
				});

			} catch (final VaultException e) {
				throw new GenericOPException(e);
			}
		}
	}

	/* Class needed to implement the method LIST to be able to
	 * get the list of Roles created
	 *
	 * @param role  the role name
	 * @throws GenericOPException if case of error
	 */
	public static class HttpList extends HttpEntityEnclosingRequestBase {

		public final static String METHOD_NAME = "LIST";

		public HttpList() {
			super();
		}

		public HttpList(final URI uri) {
			super();
			setURI(uri);
		}

		/*
		 * @throws IllegalArgumentException if the uri is invalid.
		 */
		public HttpList(final String uri) {
			super();
			setURI(URI.create(uri));
		}

		@Override
		public String getMethod() {
			return METHOD_NAME;
		}
	}

	/**
	 *
	 * @return the list of AppRoles the token used in the constructor can see
	 * @throws GenericOPException  in case of error
	 */
	public List<String> listAppRoles()
			throws GenericOPException {
		List<String> res = null;
		// Using Apache http client to call the api, authenticated with the token
		try {
			final CloseableHttpClient client = HttpClients.createDefault();
			final URL c = new URL(connectionUrl.toExternalForm() + "/v1/auth/approle/role/");

			final HttpList list = new HttpList(c.toURI());
			list.addHeader("Content-Type", "application/json");
			list.addHeader("X-Vault-Token", sess.getSessionToken().getToken());
			final CloseableHttpResponse response1 = client.execute(list);
			if (response1.getStatusLine().getStatusCode() != 200) {
				throw new GenericOPException("Unable to get the list of roles");
			}
			try {
				final org.apache.http.HttpEntity entity1 = response1.getEntity();
				// do something useful with the response body
				final ObjectMapper mapper = new ObjectMapper();
				final com.fasterxml.jackson.databind.JsonNode neoJsonNode = mapper.readTree(entity1.getContent());
				res = new ArrayList<> ();
				res = mapper.readValue(neoJsonNode.get("data").get("keys").traverse(), res.getClass());
				// and ensure it is fully consumed
				EntityUtils.consume(entity1);
			} finally {
				response1.close();
			}
		} catch (final MalformedURLException | URISyntaxException  e) {
			throw new GenericOPException("Malformed url", e);
		} catch (final IOException e) {
			throw new GenericOPException("Unable to connect", e);
		}
		return res;
	}

	/**
	 * As any toString, but it does not return sensitive information
	 */
	@Override
	public String toString() {
		return "An HashiCorpVaultAdmin object"; // No sensitive information returned
	}

}

