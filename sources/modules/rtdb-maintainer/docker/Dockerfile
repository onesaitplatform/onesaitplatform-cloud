FROM onesaitplatform/baseimage:latest

# Metadata
LABEL platform.image.name="rtdbmaintainer"	

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV SERVER_NAME=localhost \    
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=ENC(GGpZ1sLYnXwK+vz2QLkI/VK7geKcdM4pZaTL6hv6QTk=) \ 
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \
    CONFIGDBURL="jdbc:mysql://configdb:3306/onesaitplatform_config?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true" \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
    SCHEDULERDBSERVERS=schedulerdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \ 
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \
    AUDITGLOBALNOTIFY=false \              
    QUASARHOST=quasar \
    QUASARPORT=10800 \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181 \
    REALTIMEDBNAME=onesaitplatform_rtdb

# Timezone 
RUN apk add --no-cache tzdata

RUN mkdir -p /var/log/platform-logs && \
    mkdir -p /tmp/export && \
    mkdir -p /tmp/rtdbhdb && \
    mkdir -p /usr/local/files && \
	mkdir ./target	

ADD *-exec.jar app.jar

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

# Install mongodb tools and node-npm
RUN apk add --no-cache mongodb-tools && \
	apk add --update nodejs nodejs-npm

# Install elasticdump globally
RUN npm install elasticdump -g	

RUN chown -R onesait:onesait /var/log/platform-logs && \
	chown -R onesait:onesait /tmp/export && \
	chown -R onesait:onesait /usr/local/files && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /tmp/export && \
    chmod -R 777 /usr/local/files
    
VOLUME ["/tmp", "/var/log/platform-logs", "/usr/local/files"]
    
USER onesait

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Dspring.profiles.active=docker -jar /app.jar