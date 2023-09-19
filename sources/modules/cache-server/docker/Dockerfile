FROM onesaitplatform/baseimage:latest-17

# metadata
LABEL platform.image.name="cache-server"	

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
    
# distribute tracing with opentelemetry agent    
RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar

VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 20001 5701

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper
ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar  --add-opens java.base/java.lang=ALL-UNNAMED"
ENV SERVER_NAME=localhost \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=cache-server \
	OTEL_METRICS_EXPORTER=none

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dspring.profiles.active=docker -jar /app.jar
