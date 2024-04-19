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
package com.minsait.onesait.platform.controlpanel.security.encryption;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.security.PasswordEncoderBean;
import com.minsait.onesait.platform.commons.security.PasswordEncoderSHA2;
import com.minsait.onesait.platform.multitenant.config.repository.MasterUserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PasswordEncryptionManager {

	private enum SHA2 {
		ADMIN("SHA256(z2kB6aNm2d95nEcUU6qKZi1AJTgPiZJrvRY2uZcMWxA=)"), USER(
				"SHA256(DCxLLN6X4qrlIoI0vvuyEApc5xZWZgXgTfYkqyuhUTQ=)"), DEVELOPER(
						"SHA256(yskOwp3Zjjuvf4UxUGcC7Ybq5w9S1iXJS2whDw4sE1A=)");

		String pass;

		SHA2(String pass) {
			this.pass = pass;
		}

		String getPass() {
			return pass;
		}
	}

	private enum SHA3 {
		ADMIN("SHA256(dT3iRABVFjpFqDrPupOWcyiYGqcC+7Yfic5ClXvjxuQ=)"), USER(
				"SHA256(/7dNGKpHyJ4uJefv1z/czAoG/1RD/u37t/QneQHsXJs=)"), DEVELOPER(
						"SHA256(jD77noamn+UFSmujXTE6/iZjaK79QiPxdMGVf8MmSts=)");

		String pass;

		SHA3(String pass) {
			this.pass = pass;
		}

		String getPass() {
			return pass;
		}
	}

	@Autowired
	private MasterUserRepository masterUserRepository;

	@PostConstruct
	public void migrate() {
		String algorithm = System.getenv(PasswordEncoderBean.ENV_VARIABLE);
		if (StringUtils.isEmpty(algorithm)) {
			algorithm = PasswordEncoderSHA2.SHA_2;
		}
		log.debug("Running password migrator for default passwords, current hashing algorithm is: {}", algorithm);
		int count = 0;
		if (PasswordEncoderSHA2.SHA_2.equals(algorithm)) {
			count += masterUserRepository.updateMasterUserPassword(SHA3.ADMIN.getPass(), SHA2.ADMIN.getPass());
			count += masterUserRepository.updateMasterUserPassword(SHA3.DEVELOPER.getPass(), SHA2.DEVELOPER.getPass());
			count += masterUserRepository.updateMasterUserPassword(SHA3.USER.getPass(), SHA2.USER.getPass());
		} else {
			count += masterUserRepository.updateMasterUserPassword(SHA2.ADMIN.getPass(), SHA3.ADMIN.getPass());
			count += masterUserRepository.updateMasterUserPassword(SHA2.DEVELOPER.getPass(), SHA3.DEVELOPER.getPass());
			count += masterUserRepository.updateMasterUserPassword(SHA2.USER.getPass(), SHA3.USER.getPass());
		}
		log.debug("Updated {} default passwords", count);
	}
}
