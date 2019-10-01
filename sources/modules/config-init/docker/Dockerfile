FROM openjdk:8-jre-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="configinit"	

VOLUME /tmp

ADD *-exec.jar app.jar
ADD examples /tmp/examples

# Timezone 
RUN apk add --no-cache tzdata

# web projects folder & logs folder
RUN mkdir -p /usr/local/webprojects && \
	mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

RUN apk add --no-cache mongodb-tools

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait /tmp/examples && \
    chown -R onesait:onesait ./target && \    
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target
	    
USER onesait

EXPOSE 21000

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap" \
    SERVER_NAME=localhost \
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
    LOADCONFIGDB=true \
    LOADMONGODB=true \
    LOADELASTICDB=true \ 
    REALTIMEDBNAME=onesaitplatform_rtdb \
    CONTROLPANEL_SERVICE=localhost

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar