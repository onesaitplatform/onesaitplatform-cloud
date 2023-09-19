FROM onesaitplatform/baseimage:latest-17

# metadata
LABEL platform.image.name="audit-router"

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

RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/$ASPECTJ_VERSION/aspectjweaver-$ASPECTJ_VERSION.jar

# distribute tracing with opentelemetry agent
RUN  cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar

VOLUME ["/tmp", "/var/log/platform-logs"]

USER onesait

EXPOSE 20000

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -javaagent:/usr/local/aspectjweaver-$ASPECTJ_VERSION.jar -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar" \
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
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
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
    OPENSEARCH_ENABLED=false \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    SBA_USERNAME=operations \
	SBA_SECRET=changeIt2019! \
	TIMESCALE_ENABLED=false \
	TIMESCALE_URL=jdbc:postgresql://timeseriesdb:5432/?currentSchema=public \
	TIMESCALE_USER=ENC(0K1krO/jgwq+DCymHZDLYDFMfI8tJRot) \
	TIMESCALE_PASS=ENC(DdyKkq1kRMvaC1p4RlThA/MkXjE5QU4c) \
	MINIO_ROOT_USER=ENC(d6bDYa5aGMyXndGA0edIGWFtcw3UygXi) \
	MINIO_ROOT_PASSWORD=ENC(kV79KowPOv0J8QGeQ6iSQcFUqoZalNVj) \
	ENABLE_QUERY_METRICS=true \
	ENABLE_AUDIT=true \
	HTTP_LOG_LEVEL=INFO \
	OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=audit-router \
	OTEL_METRICS_EXPORTER=none

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
