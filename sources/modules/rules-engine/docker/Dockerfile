FROM onesaitplatform/baseimage:latest-17

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
    chmod -R 777 /var/log && \
    chown -R onesait:onesait /usr/local && \
    chmod -R 777 /usr/local

# distribute tracing with opentelemetry agent
RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar

VOLUME ["/var/log/platform-logs"]

USER onesait

EXPOSE 20200

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper
ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar --add-opens java.base/java.lang=ALL-UNNAMED"
ENV SERVER_NAME=localhost \
    JDBCPROTOCOL="jdbc:mysql:" \
		CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
		CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    DBADDPROPS="" \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    USE_KEYCLOAK=false \
    SBA_USERNAME=operations \
	SBA_SECRET=changeIt2019! \
	MAX_HTTP_HEADER_SIZE=20000 \
	OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=rules-engine \
	OTEL_METRICS_EXPORTER=none

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]

