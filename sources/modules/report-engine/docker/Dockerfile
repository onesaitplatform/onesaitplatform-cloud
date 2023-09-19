FROM onesaitplatform/baseimage:latest-17

# metadata
LABEL platform.image.name="onesaitplatform-report-engine"

ADD *-exec.jar app.jar

# Timezone
RUN apk add --no-cache tzdata \
    bash 
    
#Fonts
RUN apk --no-cache add msttcorefonts-installer fontconfig && \
    update-ms-fonts && \
    fc-cache -f

# web projects folder & logs folder
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
    chmod -R 777 /usr/local && \
    chmod -R 777 /etc/ssl/certs/java

# distribute tracing with opentelemetry agent
RUN cd /usr/local \
    && wget https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.27.0/opentelemetry-javaagent-1.27.0.jar



RUN apk add --no-cache \
        wget \
        libc6-compat \
        libstdc++ \
        nss \
        openjdk17-jre \
        fontconfig \
        ttf-dejavu

VOLUME ["/var/log/platform-logs"]

USER onesait

EXPOSE 18400

ENV SERVER_NAME=localhost \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
	CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
	CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
	SCHEDULERDBBURL="jdbc:mysql://configdb:3306/onesaitplatform_scheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \    
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    OAUTH_SERVER_URL=http://localhost:18000/controlpanel \
    OAUTH_SERVER=http://localhost:21000/oauth-server \
    OAUTH_CLIENT_SECRET=onesaitplatform \
    OAUTH_CLIENT_ID=onesaitplatform \
    LOG_LEVEL=INFO \
    DS_TIME_BETWEEN_EVICTION_RUNS_MILLIS=10000 \
    DS_MIN_EVICTABLE_IDLE_TIME_MILLIS=180000 \
    DS_MAX_WAIT_MILLIS=10000 \
    DS_MAX_WAIT=10000 \
    DS_INITIAL_SIZE=10 \
    DS_MAX_ACTIVE=30 \
    DS_MAX_IDLE=5 \
    DS_MIN_IDLE=5 \
    DS_REMOVE_ABANDONED=true \
    DS_REMOVE_ABANDONED_TIMEOUT=60 \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181  \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
    USE_KEYCLOAK=false \
    REPORT_LOCALE_LANG=es \
    REPORT_LOCALE_COUNTRY=ES \
    SBA_USERNAME=operations \
	SBA_SECRET=changeIt2019! \
	MAX_HTTP_HEADER_SIZE=20000 \
	HTTP_LOG_LEVEL=INFO \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk/jre \
    OTEL_JAVAAGENT_ENABLED=false \
	OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger-service:4317 \
	OTEL_SERVICE_NAME=report-engine \
	OTEL_METRICS_EXPORTER=none

ENV JAVA_OPTS="$JAVA_OPTS -Duser.language=$REPORT_LOCALE_LANG -Duser.country=$REPORT_LOCALE_COUNTRY -Djava.awt.headless=true -javaagent:/usr/local/opentelemetry-javaagent-1.27.0.jar"


COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
