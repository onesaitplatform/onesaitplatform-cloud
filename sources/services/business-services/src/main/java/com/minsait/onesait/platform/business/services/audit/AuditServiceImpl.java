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
package com.minsait.onesait.platform.business.services.audit;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.config.services.utils.ServiceUtils;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.services.QueryToolService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

	@Autowired
	private QueryToolService queryToolService;

	@Autowired
	private IntegrationResourcesService resourcesServices;

	@Override
	public String getUserAuditData(String resultType, String modulesname, String operation, String offset, String user)
			throws Exception {

		final String where = getWhereForQuery(resultType, modulesname, operation);
		try {
			return getResultForQuery(user, where, offset);
		} catch (final Exception e) {
			log.error("Error getting audit of user {}", user);
			throw e;
		}
	}

	private String getResultForQuery(String user, String where, String offset)
			throws OntologyDataUnauthorizedException, GenericOPException {

		final String collection = ServiceUtils.getAuditCollectionName(user);

		String query = "select * from " + collection;

		if (!where.equalsIgnoreCase("")) {
			query += " WHERE " + where;
		}

		if (offset.equals("")) {
			offset = "50";
		}

		query += " order by timeStamp desc limit " + Integer.parseInt(offset);

		return queryToolService.querySQLAsJson(user, collection, query, 0);

	}

	private String getWhereForQuery(String resultOperation, String module, String operation) {
		String where = "";

		if (!resultOperation.equalsIgnoreCase("all")) {
			where += " resultOperation = '" + resultOperation + "'";
		}
		if (!module.equalsIgnoreCase("all")) {
			if (!where.equals("")) {
				where += " and";
			}
			where += " module = '" + module + "'";
		}
		if (!operation.equalsIgnoreCase("all")) {
			if (!where.equals("")) {
				where += " and";
			}
			where += " operationType = '" + operation + "'";
		}
		return where;
	}

	@Override
	public String getCustomQueryData(String query, String user)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException {
		String user_ontology = user.replace(".", "_");
		return queryToolService.querySQLAsJson(user, "Audit_" + user_ontology, query, 0);
	}

	@Override
	public Boolean verifyCipherData(String jsonData, String cipherData) {
		try {
			if (isSigned()) {
				final KeyStore keyStore = KeyStore.getInstance("PKCS12");
				keyStore.load(new FileInputStream(getKeyStorePath()), getKeystorePassword().toCharArray());
				final Certificate certificate = keyStore.getCertificate("auditKeys");
				final PublicKey publicKey = certificate.getPublicKey();

				final Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, publicKey);
				final byte[] signatureBytes = Base64.getDecoder().decode(cipherData);
				final byte[] decryptedMessageHash = cipher.doFinal(signatureBytes);

				if (isCipher()) {
					final byte[] msg = jsonData.getBytes();
					final MessageDigest md = MessageDigest.getInstance("SHA-256");
					final byte[] msgHash = md.digest(msg);

					return Arrays.equals(decryptedMessageHash, msgHash);
				}

			} else if (isCipher()) {
				final byte[] msg = jsonData.getBytes();
				final MessageDigest md = MessageDigest.getInstance("SHA-256");
				final byte[] msgHash = md.digest(msg);

				return Arrays.equals(cipherData.getBytes(), msgHash);
			} else {
				return true;
			}
			return false;
		} catch (final Exception e) {
			log.error("Error verifying signed data to audit.", e);
			return false;
		}
	}

	private boolean isCipher() {
		boolean b = false;
		try {
			b = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getHash();
		} catch (final RuntimeException e) {
			log.error("Could not find property hash-audit, returning false as default");
		}
		return b;
	}

	private boolean isSigned() {
		boolean b = false;
		try {
			b = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getSigned();
		} catch (final RuntimeException e) {
			log.error("Could not find property signed-audit, returning false as default");
		}
		return b;
	}

	private String getKeyStorePath() {
		String result = null;
		try {
			result = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getKeystorePath();
		} catch (final RuntimeException e) {
			log.error("Could not find property keystore-path, returning null as default");
		}
		return result;
	}

	private String getKeystorePassword() {
		String result = null;
		try {
			result = resourcesServices.getGlobalConfiguration().getEnv().getAudit().getKeystorePassword();
		} catch (final RuntimeException e) {
			log.error("Could not find property keystore-password, returning null as default");
		}
		return result;
	}

}
