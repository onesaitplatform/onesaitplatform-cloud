FROM onesaitplatform/baseimage:latest

# metadata
LABEL platform.image.name="rules-engine"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata \
    bash

# web projects folder & logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir /application

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

WORKDIR /application
RUN unzip /app.jar &&\
	rm /app.jar
	
# Install git
RUN apk update && apk upgrade && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /application && \
    chmod -R 777 /application && \
    chmod -R 777 /var/log

VOLUME ["/var/log/platform-logs"]

USER onesait

EXPOSE 20200

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV SERVER_NAME=localhost \
    JDBCPROTOCOL="jdbc:mysql:" \
		CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
		CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    DBADDPROPS="" \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    USE_KEYCLOAK=false \
    SBA_USERNAME=operations \
	SBA_SECRET=changeIt2019!

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]

