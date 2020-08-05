FROM onesaitplatform/baseimage:latest

# metadata
LABEL platform.image.name="rules-engine"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata

# web projects folder & logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

# Install git
RUN apk update && apk upgrade && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
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
    DBADDPROPS=""

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -Djava.awt.headless=true -jar /app.jar
