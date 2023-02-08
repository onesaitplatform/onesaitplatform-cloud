# Step1 - nodejs binaries
FROM node:16-alpine AS nodebase

# Step2 - openjdk base image
FROM onesaitplatform/baseimage:latest

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="flowengine"

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV ASPECTJ_VERSION 1.9.7

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx2G -javaagent:/usr/local/aspectjweaver-$ASPECTJ_VERSION.jar" \
	SERVERNAME=localhost \
	PROTOCOL=https \
    REALTIMEDBSERVERS=realtimedb:27017 \
		CONFIGDBMASTERURL="jdbc:mysql://configdb:3306/onesaitplatform_master_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
		CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true&nullDatabaseMeansCurrent=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    CONFIGDB_MAX_ACTIVE=2 \
    CONFIGDB_MAX_IDLE=2 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \
    AUDITGLOBALNOTIFY=true \
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    REALTIMEDBNAME=onesaitplatform_rtdb \
    ENABLE_NODERED_LOG=true \
    NODERED_LOG_PATH=/var/log/platform-logs \
    NODERED_LOG_RETAIN_DAYS=5 \
    HTTPREQUESTTIMEOUT=20000 \
    SOCKETTIMEOUT=22000 \
    GRAYLOG_ENABLED=false \
    GRAYLOG_HOST=log-centralizer \
    GRAYLOG_PORT=12201 \
	SBA_USERNAME=operations \
	SBA_SECRET=changeIt2019! \
	TIMESCALE_ENABLED=false \
	TIMESCALE_URL=jdbc:postgresql://timeseriesdb:5432/?currentSchema=public \
	TIMESCALE_USER=ENC(0K1krO/jgwq+DCymHZDLYDFMfI8tJRot) \
    TIMESCALE_PASS=ENC(DdyKkq1kRMvaC1p4RlThA/MkXjE5QU4c) \
    PROMETHEUS_ENABLED=false \
    FLOW_SECRET=n0-s3cr3T \
    HTTP_LOG_LEVEL=TRACE

COPY --from=nodebase /usr/local/bin/node /bin
COPY --from=nodebase /usr/lib /usr/lib
COPY --from=nodebase /usr/local/share /usr/local/share
COPY --from=nodebase /usr/local/lib /usr/local/lib
COPY --from=nodebase /usr/local/include /usr/local/include
COPY --from=nodebase /usr/local/bin /usr/local/bin
# Download tzdata for Timezone support and aspectj library
RUN apk add --no-cache \
    tzdata \
    #npm 
    python \
    build-base \
    libexecinfo-dev \
    libc6-compat \
    git \
    openssh 

RUN mkdir -p /opt/nodeRed && \
	  mkdir -p /tmp/logs/flowEngine && \
    mkdir -p /var/log/platform-logs && \
	  mkdir ./target && \
    cd /usr/local && \
    wget https://repo1.maven.org/maven2/org/aspectj/aspectjweaver/$ASPECTJ_VERSION/aspectjweaver-$ASPECTJ_VERSION.jar

ADD *-exec.jar app.jar
ADD Flow-Engine-Manager /opt/nodeRed/Flow-Engine-Manager

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown -R onesait:onesait /opt/nodeRed && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local && \
    chmod -R 777 /opt/nodeRed
WORKDIR /opt/nodeRed/Flow-Engine-Manager
RUN npm i appmetrics-dash  && \ 
    npm i prom-client && \ 
    npm rebuild

VOLUME ["/tmp", "/var/log/platform-logs"]

USER onesait

EXPOSE 10000 20100 5050

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]
