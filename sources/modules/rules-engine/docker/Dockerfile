FROM maven:3.5.3-jdk-8-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="rules-engine"	

ADD *-exec.jar app.jar

# Timezone 
RUN apk add --no-cache tzdata

# web projects folder & logs folder
RUN mkdir -p /var/log/platform-logs && \
	mkdir ./target

# create sofia user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait 

# Install git
RUN apk update && apk upgrade && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \    
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log 
    


    
VOLUME ["/var/log/platform-logs"]

	
USER onesait

EXPOSE 20200

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G" \
    SERVER_NAME=localhost \
    JDBCPROTOCOL="jdbc:mysql:" \
    CONFIGDBSERVERS=configdb:3306 \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    DBADDPROPS=""
    
ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -Dloader.path=file:/usr/local/themes/ -Djava.awt.headless=true -jar /app.jar
