FROM onesaitplatform/baseimage:latest

# metadata
LABEL platform.image.name="semantic-inf-broker"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata

# logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local

ENV ASPECTJ_VERSION 1.8.9

RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/$ASPECTJ_VERSION/aspectjweaver-$ASPECTJ_VERSION.jar

VOLUME ["/tmp", "/var/log/platform-logs"]


EXPOSE 20000

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -javaagent:/usr/local/aspectjweaver-$ASPECTJ_VERSION.jar" \
    SERVER_NAME=localhost \
    KAFKAENABLED=false \
    KAFKABROKERS=none \    
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
    REALTIMEDBUSEAUTH=true \
    REALTIMEDBUSER=platformadmin \
    REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
		CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
		CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    ENABLEHADOOP=false \
    HDFSURL=hdfs://127.0.0.1:8020 \
    HDFSPATH=/user/hdfs \
    HIVEURL=jdbc:hive2://localhost:10000/default \
    HIVEUSERNAME=cloudera-scm \
    HIVEPASSWORD=cloudera-scm \
    HIVEDRIVERCLASS=org.apache.hive.jdbc.HiveDriver \
    IMPALAURL=jdbc:hive2://localhost:21050/default;auth=noSasl \
    KUDUNUMREPLICAS=1 \
    KUDUURL=localhost:7051 \
    INCLUDEKUDUTABLENAME=false \
    MAXCONN=100 \
    MAXCONNROUTE=100 \
    TIMESERIES_TIMEZONE=UTC \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    OP_LOG_LEVEL=INFO \
    NOTIFICATION_POOL_NODERED=50 \
    NOTIFICATION_POOL_KAFKA=50 \
    NOTIFICATION_POOL_RULESENGINE=50 \
    ELASTICSEARCH_ENABLED=false \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    SBA_USERNAME=operations \
    SBA_SECRET=changeIt2019! \
    TIMESERIES_INSERT_LOCK_CHECK_MILLIS=50 \
    TIMESERIES_INSERT_LOCK_TIMEOUT_MILLIS=500

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod 775 /usr/local/bin/docker-entrypoint.sh

USER onesait

ENTRYPOINT ["docker-entrypoint.sh"]
