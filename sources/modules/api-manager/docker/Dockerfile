FROM onesaitplatform/baseimage:latest

# Metadata
LABEL platform.image.name="apimanager"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata \
    bash

# logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir /application

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

WORKDIR /application
RUN unzip /app.jar &&\
	rm /app.jar

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /application && \
    chmod -R 777 /application && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local

VOLUME ["/tmp", "/var/log/platform-logs"]


EXPOSE 19100

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV SERVER_NAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	SCHEDULERDBBURL="jdbc:mysql://configdb:3306/onesaitplatform_scheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	CONFIGDBUSER=root \
	CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \    
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    OP_LOG_LEVEL=INFO \
    CONFIGDB_ACL_ENABLED=false \
    CONFIGDB_ACL_LIST=administrator,analytics,dataviewer,demo_developer,demo_user,developer,operations,partner,sysadmin,user \
    ENABLE_METRICS=true \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    USE_KEYCLOAK=false \
    SBA_USERNAME=operations \
	SBA_SECRET=ENC(2Xd2AOD2NrMaWs915/st8C4ILo3qlHXv)

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod 775 /usr/local/bin/docker-entrypoint.sh

USER onesait

ENTRYPOINT ["docker-entrypoint.sh"]
