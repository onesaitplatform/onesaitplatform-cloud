package com.minsait.onesait.platform.zeppelin.authenticator.Utils;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
	private static final String REGEX_REALM = ".*\\/realms\\/([a-z-_]+).*";
	private static final Pattern PATTERN_REALM = Pattern.compile(REGEX_REALM);
	
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	public static String getVerticalFromToken(String token) {
		try {
			final String[] jwtSegments = token.split("\\.");
			final String jwtBody = jwtSegments[1];
			final String parsedBody = new String(Base64.getDecoder().decode(jwtBody));
			final ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonBody = mapper.createObjectNode();
			jsonBody = mapper.readValue(parsedBody, JsonNode.class);
			final String issuer = jsonBody.get("iss").asText();
			Matcher matcher = PATTERN_REALM.matcher(issuer);
			String realm = null;
			if (matcher.find()) {
				realm = matcher.group(1);
			} else {
				LOG.info("No match found for keycloak realm");
			}
			if (realm != null) {
				return realm;
			} else {
				LOG.error("Token doesn't have vertical inside url, " + issuer + " , using default vertical onesaitplatform");
				return "onesaitplatform";
			}

		} catch (final Exception e) {
			LOG.error("Error while trying to get Keycloak vertical from token, using default vertical onesaitplatform", e);
			return "onesaitplatform";
		}
	}
	
}
