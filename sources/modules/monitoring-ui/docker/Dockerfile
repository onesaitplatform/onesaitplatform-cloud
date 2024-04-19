FROM onesaitplatform/baseimage:latest

# Metadata
LABEL platform.image.name="monitoringui"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata \
    bash

# logs folder
RUN mkdir -p /var/log/platform-logs && \
    mkdir -p /usr/local/bin && \
	mkdir /application

# Copy certs
COPY ocpserver.crt /usr/local/
COPY ocpserver.key /usr/local/

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /bin/sh onesait


WORKDIR /application
RUN unzip /app.jar &&\
	rm /app.jar


RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /application && \
    chmod -R 777 /application && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local && \
    chmod -R 777 /etc/ssl/certs/java

VOLUME ["/tmp", "/var/log/platform-logs"]

EXPOSE 18100

ENV SERVER_NAME=localhost \
    CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
    CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    CLIENTID="onesaitplatform" \
    CLIENTSECRET="onesaitplatform" \
    PATH="/usr/local/bin:${PATH}" \
    ENVIRONMENTNAME="" \
    CONFIGDB_ACL_ENABLED=false \
    CONFIGDB_ACL_LIST=administrator,analytics,dataviewer,demo_developer,demo_user,developer,operations,partner,sysadmin,user \
    OAUTH_USE_DEFAULT_URI=false \
    ENCRYPTION_KEY=ENC(VYVseIUh5xiRd8ws0prbEAg6bGq7vmbfi3gkM65HECy+YPbjz4f49w==) \
    ENCRYPTION_ITERATIONS=5 \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    USE_KEYCLOAK=false 


COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod 775 /usr/local/bin/docker-entrypoint.sh

USER onesait

ENTRYPOINT ["docker-entrypoint.sh"]
