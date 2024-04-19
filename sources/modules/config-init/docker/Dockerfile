FROM openjdk:8-jdk-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="configinit"

VOLUME /tmp

ADD *-exec.jar app.jar
ADD examples /tmp/examples

# Timezone
#RUN apk add --no-cache tzdata

# web projects folder & logs folder
RUN mkdir -p /usr/local/webprojects && \
	mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN apk add --no-cache mongodb-tools

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /tmp/examples && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target

USER onesait

EXPOSE 21000

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
    SERVER_NAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \ 
    REALTIMEDBUSEAUTH=true \
    REALTIMEDBUSER=platformadmin \ 
    REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
    CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \
    LOADCONFIGDB=true \
    LOADMONGODB=false \
    LOADTESTDATA=false \
    LOADELASTICDB=false \
    LOADMAILCONFIG=false \
    LOADGRAYLOGDB=false \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    CONTROLPANEL_SERVICE=localhost \
    UPDATE_MODE_MULTITENANT=true \
    GRAYLOG_USER=admin \
    GRAYLOG_PASS=ENC(WOukGj28rhRwzEa3HDCnH2p4vX3Iur8V) \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    GRAYLOG_EXTERNAL_URI=http://log-centralizer:9000 \
    GRAYLOG_PLUGIN_AUTH_PATH_TOKEN=http://oauthservice:21000/oauth-server/oauth/token \
    GRAYLOG_PLUGIN_AUTH_PATH_USERINFO=http://oauthservice:21000/oauth-server/oidc/userinfo

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
