FROM openjdk:8-jdk-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="module-template"	

ADD *-exec.jar app.jar

# logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target
	
# create onesait user/group
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
    
EXPOSE 19500

ENV JAVA_OPTS="$JAVA_OPTS -Xms2G -Xmx2G" \
    SERVER_NAME=localhost \
    KAFKAENABLED=false \
    KAFKAHOST=kafka \
    KAFKAPORT=9095 \
    REALTIMEDBSERVERS=realtimedb:27017 \
    REALTIMEDBAUTHDB=admin \    
	REALTIMEDBUSEAUTH=true \
	REALTIMEDBUSER=platformadmin \
	REALTIMEDBPASS=0pen-platf0rm-2018! \   
    REALTIMEDBWRITECONCERN=UNACKNOWLEDGED \	  
    CONFIGDBHOST=configdb \
    CONFIGDBPORT=3306 \
    SCHEDULERDBHOST=schedulerdb \
    SCHEDULERDBPORT=3306 \
    ELASTICDBHOST=elasticdb \
    ELASTICDBPORT=9200 \    
    QUASARHOST=quasar \
    QUASARPORT=10800

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar