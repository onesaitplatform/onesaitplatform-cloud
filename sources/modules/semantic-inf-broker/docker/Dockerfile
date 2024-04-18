FROM maven:3.5.3-jdk-8-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="router"	

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
    
VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 20000

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
    SERVER_NAME=localhost \
    KAFKAENABLED=false \
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
    CONFIGDBSERVERS=configdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \ 
    AUDITGLOBALNOTIFY=false \
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
    OP_LOG_LEVEL=INFO

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar