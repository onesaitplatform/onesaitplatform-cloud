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
package com.minsait.onesait.platform.config.model;

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

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.vault.VaultException;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.RoleId;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.SecretId;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.LifecycleAwareSessionManager;
import org.springframework.vault.authentication.VaultLoginException;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.core.VaultTransitTemplate;
import org.springframework.vault.security.VaultBytesEncryptor;
import org.springframework.vault.security.VaultBytesKeyGenerator;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.Signature;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultSignRequest;
import org.springframework.vault.support.VaultTransitKeyCreationRequest;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.commons.exception.GenericOPException;

/**
 * A basic set of User/Role Vault operations. Tutorial: The methods to
 * store/retrieve data or encrypt/decrypt/sign/verify data or generate random
 * numbers, it is internally done creating rest calls to Vault within a unique
 * path. Those paths should have rights only for the current User/Role. For
 * example, the data encrypted in one keyPath can only be decrypted in the same
 * keyPath with the same AppRole. how-to: Example: URL vurl = new
 * URL("https://vault.organization.org:8200"); // r contains role_id, secret_id
 * and secret_id_accessor try (final HashiCorpVaultUser hc = new
 * HashiCorpVaultUser(vurl, r)) { System.out.println("random number:" +
 * hc.generateRandom(8192)); final String res1 = hc.encrypt("text to cipher",
 * "𝕿𝖍𝖊 𝖖𝖚𝖎𝖈𝖐 𝖇𝖗𝖔𝖜𝖓 𝖋𝖔𝖝 𝖏𝖚𝖒𝖕𝖘 𝖔𝖛𝖊𝖗 𝖙𝖍𝖊 𝖑𝖆𝖟𝖞
 * 𝖉𝖔𝖌"); } catch (final GenericOPException e) { // Do something }
 * 
 * explanation: role_id and secret_id are universally unique identifier (UUID)
 * of 16 bytes to login. Once logged, it generates automatically a series of
 * tokens as proof of identity to perform its tasks in the Vault. Notice that
 * using this class does not solve the egg and chicken problem of where to store
 * the secret to be able to access other secrets.
 *
 * reference: See the methods documentation
 */

public final class HashiCorpVaultUser implements AutoCloseable {

	// To be used as path prefixes
	private static final String pathstore = "/onesait/pathstore/";
	private static final String pathcipher = "/onesait/pathcipher/";
	// Allowed hashing methods. sha1 is not recommended, therefore is not allowed.
	private static final Collection<String> signAlgorithms = Arrays.asList("sha2-224", "sha2-256", "sha2-384",
			"sha2-512", "sha3-224", "sha3-256", "sha3-384", "sha3-512");

	@Autowired
	private final transient VaultTemplate voperations;
	private transient final String cipherpath; // will include pathcipher+/+role_id
	private transient final ClientHttpRequestFactory clientHttpRequestFactory;
	private transient final VaultKeyValueOperations vkvt;
	private transient final ThreadPoolTaskScheduler taskSchedule;

	/**
	 * Constructor
	 * 
	 * @param url the url to the Vault in the way of https://hostname:port
	 * @param ids the ids returned by HashiCorpVaultAdmin.createAppRole
	 * @throws GenericOPException    Any error
	 * @throws MalformedURLException the url is incorrect
	 */
	public HashiCorpVaultUser(final String url, final Map<String, String> ids)
			throws GenericOPException, MalformedURLException {
		this(new URL(url), ids);
	}

	/**
	 * This constructor is meant to be used with the data returned by
	 * HashiCorpVaultAdmin.createAppRole
	 * 
	 * @param url the url to the Vault in the way of https://hostname:port
	 * @param ids the ids returned by HashiCorpVaultAdmin.createAppRole must not be
	 *            null (or NullPointerException is thrown) and must contain values
	 *            for role_id and secret_id
	 * @throws GenericOPException Any error
	 */
	public HashiCorpVaultUser(final URL url, final Map<String, String> ids) throws GenericOPException {
		this(url, pathstore + ids.get("role_id"), pathcipher + ids.get("role_id"), ids.get("role_id"),
				ids.get("secret_id"));
	}

	/**
	 * This constructor is meant to be used with the data returned by
	 * HashiCorpVaultAdmin.createAppRole
	 * 
	 * @param url      the url to the Vault in the way of https://hostname:port
	 * @param roleId   the id of the AppRole
	 * @param secretId the password of the AppRole
	 * @throws GenericOPException Any error
	 */
	public HashiCorpVaultUser(final URL url, final String roleId, final String secretId) throws GenericOPException {
		this(url, pathstore + roleId, pathcipher + roleId, roleId, secretId);
	}

	/**
	 * This constructor can use any path to store and cipher (if the policy allows
	 * it) and
	 * 
	 * @param url        the url to the Vault
	 * @param storepath  the prefix path to store/retrieve data
	 * @param cipherpath the prefix path to do cipher operations
	 * @param sroleId    the universally unique identifier role_id
	 * @param ssecretId  the universally unique identifier secret_id
	 * @throws GenericOPException Any Error
	 */
	private HashiCorpVaultUser(final URL url, final String storepath, final String cipherpath, final String sroleId,
			final String ssecretId) throws GenericOPException {
		// Validate and transform url to vep
		if (url == null || !"https".equalsIgnoreCase(url.getProtocol())) {
			throw new GenericOPException(
					"It is really unsafe to use HashiCorp Vault without https. Only available with https.");
		}
		// FIXME: Validate storepath, cipherpath, sroleId and ssecretId

		// The url of the Vault to connect to. Only the https://hostname:port parts are
		// used.
		final VaultEndpoint vep = new VaultEndpoint();
		vep.setScheme(url.getProtocol());
		vep.setHost(url.getHost());
		vep.setPort(url.getPort());

		final AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
				.roleId(RoleId.provided(sroleId))
				// We do not use the pull mode (obtain the first secretId) from
				// auth/approle/role/(role-name)/secret-id
				// because it only requires the role name and have rights to use that path
				// Therefore, a never expiring secretId is used (with the chance to obtain new
				// ones)
				.secretId(SecretId.provided(ssecretId)).build();

		clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		final ClientAuthentication clientAuthentication = new AppRoleAuthentication(options,
				VaultClients.createRestTemplate(vep, clientHttpRequestFactory));
		// Used to recreate the new tokens as the old ones are about to expire
		taskSchedule = new ThreadPoolTaskScheduler();
		taskSchedule.initialize();
		RestTemplate restTemplate = new RestTemplate();

		final LifecycleAwareSessionManager sess = new LifecycleAwareSessionManager(clientAuthentication, taskSchedule,
				restTemplate);
		voperations = new VaultTemplate(vep, clientHttpRequestFactory, sess);
		vkvt = voperations.opsForKeyValue(storepath, VaultKeyValueOperations.KeyValueBackend.KV_1); // Without versions,
																									// only keep the
																									// last value.

		this.cipherpath = cipherpath;
	}

	/**
	 * Stop the Scheduler so the object can be released The ideal way to be used is
	 * in a try with resources.
	 */
	@Override
	public void close() {
		taskSchedule.shutdown();
	}

	/**
	 * Use a http proxy without authentication
	 * 
	 * @param host the hostname or ip of the proxy
	 * @param port the port of the proxy
	 */
	public void setProxy(final String host, final int port) {
		final Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
		((SimpleClientHttpRequestFactory) clientHttpRequestFactory).setProxy(proxy);
	}

	/*
	 * Vault does not like paths as parameters with / or = Base64 but replaces =
	 * with _ and / with .
	 */
	private String getCustomBase64(final String param) {
		return Base64.getEncoder().encodeToString(param.getBytes(StandardCharsets.UTF_8)).replace('=', '_').replace('/',
				'.');
	}

	/*
	 * Revert getCustomBase64 Base64 but replaces = with _ and / with .
	 */
	private String getFromCustomBase64(final String param) {
		return new String(
				Base64.getDecoder().decode(param.replace('_', '=').replace('.', '/').getBytes(StandardCharsets.UTF_8)),
				StandardCharsets.UTF_8);
	}

	/**
	 * Encrypts in HVault a byte[] using a generated key in HVault It uses the
	 * default key automatically generated by Vault for keyPath
	 * 
	 * @param data the byte[] to encrypt
	 * @return Base64 encoding of the encrypted data with the HVault encryptor
	 */
	public String encrypt(final byte[] data, final String keyPath) throws GenericOPException {
		if (null == data || data.length == 0) {
			throw new GenericOPException("Do you really want to encrypt en empty data?");
		}
		try {
			final VaultTransitOperations transit = new VaultTransitTemplate(voperations, cipherpath);
			final VaultBytesEncryptor encryptor = new VaultBytesEncryptor(transit, getCustomBase64(keyPath));
			return Base64.getEncoder().encodeToString(encryptor.encrypt(data));
		} catch (final IllegalArgumentException e) {
			throw new GenericOPException(e);
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("Unable to connect to vault", e);
		} catch (final VaultLoginException e) {
			throw new GenericOPException("Unable to login to Vault", e);
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to encrypt in Vault", e);
		}
	}

	/**
	 * Encrypts in HVault a UTF-8 string using a generated key in HVault It uses the
	 * default key automatically generated by Vault for keyPath
	 * 
	 * @param plaintext the text to encrypt
	 * @return Base64 encoding of the encrypted plaintext with the HVault encryptor
	 */
	public String encrypt(final String plaintext, final String keyPath) throws GenericOPException {
		return encrypt(plaintext.getBytes(StandardCharsets.UTF_8), keyPath);
	}

	/**
	 * Decrypts in HVault a base64 encrypted string using a generated key in HVault
	 * 
	 * @param b64encrypted the text to decrypt
	 * @param keyPath      The name of the key used to encrypt
	 * @return the byte[] decrypted only if it was encrypted with the same key it
	 *         can be converted to String using new String(value,
	 *         StandardCharsets.UTF_8).
	 * @throws GenericOPException in case of any error
	 */
	public byte[] decrypt(final String b64encrypted, final String keyPath) throws GenericOPException {
		if (!StringUtils.hasText(b64encrypted)) {
			throw new GenericOPException("Do you really want to decrypt en empty string?");
		}
		try {
			final VaultTransitOperations transit = new VaultTransitTemplate(voperations, cipherpath);
			final VaultBytesEncryptor encryptor = new VaultBytesEncryptor(transit, getCustomBase64(keyPath));
			return encryptor.decrypt(Base64.getDecoder().decode(b64encrypted));
		} catch (final IllegalArgumentException e) {
			throw new GenericOPException(e);
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("Unable to connect to vault", e);
		} catch (final VaultLoginException e) {
			throw new GenericOPException("Unable to login to Vault", e);
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to decrypt in Vault", e);
		}
	}

	// TODO: Listar todos los secretos que se hayan guardado (si es posible)

	// TODO: Listar todas las rutas donde se ha cifrado algo

	/**
	 * Sign the hash of data using algorithm with an ed25519 key
	 * 
	 * @param data      the data to sign. If you want to sign an String, you can use
	 *                  your_string.getBytes(StandardCharsets.UTF_8)
	 * @param keyPath   the key to use to sign
	 * @param algorithm one of sha2-224, sha2-256, sha2-384, sha2-512, sha3-224,
	 *                  sha3-256, sha3-384 or sha3-512
	 * @return the signature in vault format
	 * @throws GenericOPException Any Error
	 */
	public String sign(final byte[] data, final String keyPath, final String algorithm) throws GenericOPException {
		if (null == data || data.length == 0) {
			throw new GenericOPException("Do you really want to sign en empty string?");
		}
		if (!signAlgorithms.contains(algorithm)) {
			throw new GenericOPException(
					"Only sha2-224, sha2-256, sha2-384, sha2-512, sha3-224, sha3-256, sha3-384 or sha3-512");
		}
		try {
			final VaultTransitOperations transit = new VaultTransitTemplate(voperations, cipherpath);
			transit.createKey(getCustomBase64(keyPath), VaultTransitKeyCreationRequest.ofKeyType("ed25519"));
			return transit.sign(getCustomBase64(keyPath) + "/" + algorithm, VaultSignRequest.create(Plaintext.of(data)))
					.getSignature();
		} catch (final IllegalArgumentException e) {
			throw new GenericOPException(e);
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("Unable to connect to vault", e);
		} catch (final VaultLoginException e) {
			throw new GenericOPException("Unable to login to Vault", e);
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to sign in Vault", e);
		}
	}

	/**
	 * Verify that the signed is the correct sign with the key of keyPath with
	 * algorithm of plaintext
	 * 
	 * @param signed    the signature (in vault format)
	 * @param plaintext the plaintext that was signed
	 * @param keyPath   the path of the key
	 * @param algorithm one of sha2-224, sha2-256, sha2-384, sha2-512, sha3-224,
	 *                  sha3-256, sha3-384 or sha3-512
	 * @return true if it is correct
	 * @throws GenericOPException Any Error
	 */
	public boolean verify(final String signed, final byte[] data, final String keyPath, final String algorithm)
			throws GenericOPException {
		if (null == data || data.length == 0) {
			throw new GenericOPException("Do you really want to verify empty data?");
		}
		if (!signAlgorithms.contains(algorithm)) {
			throw new GenericOPException(
					"Only sha2-224, sha2-256, sha2-384, sha2-512, sha3-224, sha3-256, sha3-384 or sha3-512");
		}
		try {
			final VaultTransitOperations transit = new VaultTransitTemplate(voperations, cipherpath);
			return transit.verify(getCustomBase64(keyPath), Plaintext.of(data), Signature.of(signed));
		} catch (final IllegalArgumentException e) {
			throw new GenericOPException(e);
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("Unable to connect to vault", e);
		} catch (final VaultLoginException e) {
			throw new GenericOPException("Unable to login to Vault", e);
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to verify in Vault", e);
		}
	}

	/**
	 * @param len the number of random bytes, between 1 and 8192 (both included)
	 * @return a byte[] with 0<len<=8192 random bytes or 0 length byte[]
	 */
	@Nullable
	public byte[] generateRandom(final int len) {
		final byte[] res = {};
		return len > 0 && len <= 8192 ? new VaultBytesKeyGenerator(voperations, cipherpath, len).generateKey() : res;
	}

	/**
	 * Store base64(plainText) in Vault in the key base64(path) In the url:
	 * url/v1/clave/base64(path)
	 * 
	 * @param path      The key where to write the plainText
	 * @param plainText The value to write in the key
	 * @throws GenericOPException if there is something wrong
	 */
	public void write(@Nonnull final String path, final String plainText) throws GenericOPException {
		if (!StringUtils.hasText(path)) {
			throw new GenericOPException("Unable to store in null path");
		}
		try {
			// If path contains Character unicode 0000, it goes all wrong, but in base64, no
			// problemo.
			final String _path = getCustomBase64(path);
			// Vault only accepts json.
			// Let's make it accept any string, even with Unicode control characters,
			// encoding it to base64 before sending it to Vault.
			if (plainText != null) {
				vkvt.put(_path, "{\"type\": \"str\", \"value\": \"" + getCustomBase64(plainText) + "\"}");
			} else {
				vkvt.put(_path, "{\"type\": \"str\"}"); // store null
			}
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("The vault is inaccessible or not running", e);
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to store in the vault", e);
		}
	}

	/**
	 * read the base64.decoded(value) stored in the url/v1/clave/base64(path)
	 * 
	 * @param path The key where to read the value
	 * @return The value decoded
	 * @throws GenericOPException if there is something wrong
	 */
	@Nullable
	public String read(final String path) throws GenericOPException {
		VaultResponse res;
		if (!StringUtils.hasText(path)) {
			throw new GenericOPException("Unable to read from null path");
		}
		try {
			final String _path = getCustomBase64(path);
			res = vkvt.get(_path);
			if (res != null && res.getData() != null && "str".equals(res.getData().get("type"))
					&& res.getData().get("value") != null) {
				final String value = (String) res.getData().get("value");
				return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
			} else {
				throw new GenericOPException("Unable to read from vault");
			}
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to read from vault", e);
		}
	}

	/**
	 * Store base64(plainData) in Vault in the key base64(path) In the url:
	 * url/v1/clave/base64(path)
	 * 
	 * @param path      The key where to write the plainData
	 * @param plainData The data to store
	 * @throws GenericOPException if there is something wrong
	 */
	public void writeData(final String path, final byte[] plainData) throws GenericOPException {
		if (!StringUtils.hasText(path)) {
			throw new GenericOPException("Unable to store in null path");
		}
		try {
			final String _path = getCustomBase64(path);
			vkvt.put(_path,
					"{\"type\": \"byte[]\", \"value\": \"" + Base64.getEncoder().encodeToString(plainData) + "\"}");
		} catch (final ResourceAccessException e) {
			throw new GenericOPException("The vault is inaccessible or not running", e);
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to write in the vault", e);
		}
	}

	/**
	 * read the base64.decoded(value) stored in the url/v1/clave/base64(path)
	 * 
	 * @param path The key where to read the value
	 * @return the value decoded
	 * @throws GenericOPException if there is something wrong
	 */
	public byte[] readData(final String path) throws GenericOPException {
		VaultResponse res;
		if (!StringUtils.hasText(path)) {
			throw new GenericOPException("Unable to read from null path");
		}
		try {
			final String _path = getCustomBase64(path);
			res = vkvt.get(_path);
			if (res != null && res.getData() != null && "byte[]".equals(res.getData().get("type"))
					&& res.getData().get("value") != null) {
				final String value = (String) res.getData().get("value");
				return Base64.getDecoder().decode(value);
			} else {
				throw new GenericOPException("Unable to read from vault");
			}
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to read from vault", e);
		}
	}

	/**
	 * list all the keys (or paths) where a value is stored, but not the paths used
	 * to encrypt/decrypt/sign/verify.
	 * 
	 * @return List<String> of decoded keys (paths)
	 * @throws GenericOPException if there is something wrong
	 */
	public List<String> listKeys() throws GenericOPException {
		List<String> res1;
		try {
			final List<String> res = vkvt.list("");
			res1 = new ArrayList<>(res.size());
			for (final String s : res) {
				res1.add(getFromCustomBase64(s));
			}
			return res1;
		} catch (final VaultException e) {
			throw new GenericOPException("Unable to read from vault", e);
		}
	}

	/**
	 * As any toString, but it does not return sensitive information
	 */
	@Override
	public String toString() {
		return "An HashiCorpVaultUser object"; // No sensitive information returned
	}
}
