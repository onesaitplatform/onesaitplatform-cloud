FROM onesaitplatform/baseimage:latest-17

# Metadata
LABEL platform.image.name="device-simulator"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata

# logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local

# distribute tracing with opentelemetry agent
RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar


VOLUME ["/tmp", "/var/log/platform-logs"]

USER onesait

EXPOSE 19200

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper
ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar --add-opens java.base/java.lang=ALL-UNNAMED"
ENV SERVER_NAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	SCHEDULERDBBURL="jdbc:mysql://configdb:3306/onesaitplatform_scheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
    CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    SCHEDULERDBBUSER=root \
    SCHEDULERDBBPASS=changeIt! \
    SCHEDULERDBBDRIVER=com.mysql.cj.jdbc.Driver \
    SCHEDULERDBBDIALECT=org.hibernate.dialect.MySQL5Dialect \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    SBA_USERNAME=operations \
	QUARTZ_DRIVER=org.quartz.impl.jdbcjobstore.StdJDBCDelegate \
    SBA_SECRET=changeIt2019! \
    OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=device-simulator \
	OTEL_METRICS_EXPORTER=none

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dspring.profiles.active=docker -jar /app.jar
