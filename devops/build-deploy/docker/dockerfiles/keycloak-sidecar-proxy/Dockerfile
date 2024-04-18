FROM openjdk:8u151-jre

ENV KEYCLOAK_VERSION 4.6.0.Final
ENV ALPN_BOOT_VERSION 8.1.11.v20170118

RUN curl -L https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/keycloak-proxy-${KEYCLOAK_VERSION}.zip -O \
  && unzip keycloak-proxy-${KEYCLOAK_VERSION}.zip \
  && mv keycloak-proxy-${KEYCLOAK_VERSION} /opt/keycloak-proxy \
  && rm keycloak-proxy-${KEYCLOAK_VERSION}.zip \
  && curl https://repo1.maven.org/maven2/org/mortbay/jetty/alpn/alpn-boot/${ALPN_BOOT_VERSION}/alpn-boot-${ALPN_BOOT_VERSION}.jar -o /opt/keycloak-proxy/lib/alpn-boot.jar

# Default configuration: can be overridden at the docker command line
ENV HTTP_PORT=8080 \
    HTTPS_PORT=8443 \
    KEYSTORE=/opt/keycloak-proxy/conf/key.jks \
    KEYSTORE_PASSWORD=secret \
    KEYSTORE_PASSWORD_FILE=/tmp/keystore_password \
    KEY_PASSWORD=secret \
    KEY_PASSWORD_FILE=/tmp/key_password \
    TRUSTSTORE=/opt/keycloak-proxy/conf/cacerts.jks \
    TRUSTSTORE_PASSWORD=secret \
    TRUSTSTORE_PASSWORD_FILE=/tmp/truststore_password \
    ALLOW_ANY_HOSTNAME=true \
    AUTH_SERVER_URL=https://keycloak:8443/auth \
    SSL_REQUIRED=external \
    REALM_NAME=dcm4che \
    CLIENT_ID=kibana \
    PUBLIC_CLIENT=true \
    CREDENTIAL_SECRET= \
    BEARER_ONLY=false \
    TARGET_URL=http://kibana:5601 \
    BASE_PATH=/ \
    ROLE_ALLOWED=auditlog

COPY conf /opt/keycloak-proxy/conf
COPY docker-entrypoint.sh /

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["keycloak-proxy"]
