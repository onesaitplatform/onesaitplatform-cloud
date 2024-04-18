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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.security.PasswordEncoder;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.RoleRepository;
import com.minsait.onesait.platform.config.services.user.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class X509CertService {

	private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
	@Autowired
	private UserService userService;
	@Autowired
	private RoleRepository roleRepository;

	@Value("${onesaitplatform.authentication.default_password:changeIt9900!}")
	private String defaultPassword;
	@Value("${onesaitplatform.authentication.X509.admin_user_id:51503283G}")
	private String admin;
	@Value("${onesaitplatform.authentication.X509.id_att:SERIALNUMBER}")
	private String idAtt;
	@Value("${onesaitplatform.authentication.X509.id_att_regex:(.*?)(?:,|$)}")
	private String idAttRegex;
	@Value("${onesaitplatform.authentication.X509.cn_regex:CN=\"(.*?)(?:\"|$)}")
	private String cnRegex;
	@Value("${onesaitplatform.authentication.X509.id_hash:false}")
	private boolean shoudlHashIdAttribute;

	private Pattern subjectDnPattern;

	private Pattern subjectSerialIDPattern;
	private static final String CN = "CN";

	@PostConstruct
	public void setRegexPatterns() {
		subjectDnPattern = Pattern.compile(cnRegex, Pattern.CASE_INSENSITIVE);
		subjectSerialIDPattern = Pattern.compile(idAtt + "=" + idAttRegex, Pattern.CASE_INSENSITIVE);
	}

	public Optional<String> extractUserNameFromCert(String pem)
			throws CertificateException, UnsupportedEncodingException, GenericOPException {
		return extractUserNameFromCert(parseCertificate(pem));
	}

	public Optional<String> extractUserNameFromCert(X509Certificate cert) throws GenericOPException {
		final Map<String, String> info = extractInfoFromSN(cert.getSubjectDN().getName());
		if (info.isEmpty())
			return Optional.empty();

		final User user = userService.getUser(info.get(idAtt));
		if (user == null)

			return Optional.of(createUserFromCertInfo(info).getUserId());
		else
			return Optional.of(user.getUserId());

	}

	public User createUserFromCertInfo(Map<String, String> info) {
		final User user = new User();
		user.setUserId(info.get(idAtt));
		user.setFullName(info.get(CN));
		user.setEmail(info.get(idAtt) + "@e-cert.com");
		user.setActive(true);
		user.setPassword(defaultPassword + UUID.randomUUID().toString().substring(1, 5));
		if (user.getUserId().equals(admin))
			user.setRole(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.name()).orElse(null));
		else
			user.setRole(roleRepository.findById(Role.Type.ROLE_USER.name()).orElse(null));
		userService.createUser(user);
		return user;

	}

	public User createUserFromCert(X509Certificate cert) throws GenericOPException {
		return createUserFromCertInfo(extractInfoFromSN(cert.getSubjectDN().getName()));
	}

	private X509Certificate parseCertificate(String pem) throws CertificateException, UnsupportedEncodingException {
		final String cert = URLDecoder.decode(pem, StandardCharsets.UTF_8.name());

		final byte[] decoded = Base64.getDecoder()
				.decode(cert.replaceAll(BEGIN_CERTIFICATE, "").replaceAll(END_CERTIFICATE, "").replaceAll("\n", ""));

		return (X509Certificate) CertificateFactory.getInstance("X.509")
				.generateCertificate(new ByteArrayInputStream(decoded));
	}

	private Map<String, String> extractInfoFromSN(String subjectDN) throws GenericOPException {
		final Map<String, String> info = new HashMap<>();
		Matcher matcher = subjectDnPattern.matcher(subjectDN);
		if (matcher.find()) {
			info.put(CN, matcher.group(1));
		}

		matcher = subjectSerialIDPattern.matcher(subjectDN);

		if (matcher.find()) {
			final String id = shoudlHashIdAttribute ? PasswordEncoder.getInstance().encodeSHA256(matcher.group(1))
					: matcher.group(1);
			info.put(idAtt, id);

		}
		return info;
	}
}
