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
package com.minsait.onesait.platform.encryptor.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig implements ApplicationContextAware {

	public static final String JASYPT_BEAN = "jasyptStringEncryptor";

	@Value("${onesaitplatform.password.encryptor.passphrase:0n3sa1tP1atf0rm}")
	private String password;

	@Value("${onesaitplatform.password.encryptor.algorithm:PBEWithMD5AndDES}")
	private String algorithm;

	@Value("${onesaitplatform.password.encryptor.providerName:SunJCE}")
	private String providerName;

	@Value("${onesaitplatform.password.encryptor.saltGeneratorClassName:org.jasypt.salt.RandomSaltGenerator}")
	private String saltGeneratorClassName;

	@Value("${onesaitplatform.password.encryptor.ivGeneratorClassName:org.jasypt.salt.NoOpIVGenerator}")
	private String ivGeneratorClassName;

	@Value("${onesaitplatform.password.encryptor.outputType:base64}")
	private String outputType;

	@Value("${onesaitplatform.password.encryptor.iterations:1000}")
	private String iterations;

	@Value("${onesaitplatform.password.encryptor.poolSize:1}")
	private String poolSize;

	/**
	 * Bean with name jasyptStringEncryptor override default jasypt encryptor But
	 * one can also override this by defining property:
	 *
	 * jasypt.encryptor.bean=myBeanName
	 *
	 * @return
	 */
	@Bean(JASYPT_BEAN)
	public StringEncryptor stringEncryptor() {
		final PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		final SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(password);
		config.setAlgorithm(algorithm);
		config.setKeyObtentionIterations(iterations);
		config.setPoolSize(poolSize);
		config.setProviderName(providerName);
		config.setSaltGeneratorClassName(saltGeneratorClassName);
		config.setStringOutputType(outputType);
		encryptor.setConfig(config);
		return encryptor;
	}

	private static ApplicationContext context;

	public static StringEncryptor getEncryptor() {
		return (StringEncryptor) context.getBean("jasyptStringEncryptor");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		setContext(applicationContext);

	}

	private synchronized void setContext(ApplicationContext applicationContext) {
		context = applicationContext;
	}

}
