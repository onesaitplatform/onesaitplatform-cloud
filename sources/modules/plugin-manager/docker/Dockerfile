FROM onesaitplatform/baseimage:latest-17

# Metadata
LABEL platform.image.name="plugins-manager"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata \
    bash

# logs folder
RUN mkdir -p /plugins && \
	mkdir /application

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

WORKDIR /application
RUN unzip /app.jar &&\
	rm /app.jar

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /plugins && \
    chown -R onesait:onesait /application && \
    chmod -R 777 /application && \
    chmod -R 777 /plugins && \
    chmod -R 777 /usr/local
 
# distribute tracing with opentelemetry agent
RUN  cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar

VOLUME ["/plugins"]

USER onesait

EXPOSE 18080

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="-Xms256M -Xmx1g -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar" \
    SERVER_NAME=localhost \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBUSER=root \
	CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=plugins-manager \
	OTEL_METRICS_EXPORTER=none

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
