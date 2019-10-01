FROM openjdk:8-jre-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="oauthserver"	

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
	SERVER_NAME=localhost \    
    REALTIMEDBSERVERS=realtimedb:27017 \  
    CONFIGDBSERVERS=configdb:3306 \
    SCHEDULERDBSERVERS=schedulerdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \        
    AUDITGLOBALNOTIFY=false \      
    QUASARHOST=quasar \
    QUASARPORT=10800 \ 
    REALTIMEDBNAME=onesaitplatform_rtdb 

# Timezone 
RUN apk add --no-cache tzdata

RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target	

ADD *-exec.jar app.jar

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

RUN chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log
    
VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 21000

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar