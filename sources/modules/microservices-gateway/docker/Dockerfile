FROM onesaitplatform/baseimage:latest-17

# Metadata
LABEL platform.image.name="microservices-gateway"


COPY *.jar app.jar

# logs folder
RUN mkdir -p /var/log && \
	mkdir ./target


# create user
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN  chown -R onesait:onesait /var/log && \
	 chmod -R 777 /var/log && \
	 chown onesait:onesait app.jar && \
	 chown -R onesait:onesait ./target && \
	 chown -R onesait:onesait /usr/local/bin && \
     chmod -R 777 ./target

# distribute tracing with opentelemetry agent
RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar

     

VOLUME ["/var/log/platform-logs"]



COPY docker-entrypoint.sh /usr/local/bin/

RUN chown -R onesait:onesait /usr/local/bin && \
	chmod -R 755 /usr/local/bin

USER onesait

EXPOSE 26000


ENV JAVA_OPTS="$JAVA_OPTS -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar" \
    SERVER_NAME=localhost \
    GATEWAY_CLIENT=operations \
    GATEWAY_SECRET=operations \
    PATH="/usr/local/bin:${PATH}" \
    SSL_ENABLED=false \
    PORT=26000 \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
	CLIENT_ID=onesaitplatform \
	CLIENT_SECRET=onesaitplatform \
	INTROSPECTION_URI=https://development.onesaitplatform.com/auth/realms/onesaitplatform/protocol/openid-connect/token/introspect \
	INSECURE_URLS= \
	OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=microservices-gateway \
	OTEL_METRICS_EXPORTER=none

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
