FROM openjdk:8-jre-alpine

# Metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="monitoringui"

ADD *-exec.jar app.jar

# Timezone 
RUN apk add --no-cache tzdata

# logs folder
RUN mkdir -p /var/log/platform-logs && \
    mkdir -p /usr/local/bin && \
	mkdir ./target
	
# Copy certs
COPY ocpserver.crt /usr/local/
COPY ocpserver.key /usr/local/

# create onesait user/group
RUN addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /bin/sh onesait 

RUN chown -R onesait:onesait /usr/local && \
    chown -R onesait:onesait /var/log/platform-logs && \
    chown -R onesait:onesait ./target && \    
    chown onesait:onesait app.jar && \
    chmod -R 777 ./target && \
    chmod -R 777 /var/log && \
    chmod -R 777 /usr/local && \
    chmod -R 777 /etc/ssl/certs/java

VOLUME ["/tmp", "/var/log/platform-logs"]

EXPOSE 18100

ENV JAVA_OPTS="$JAVA_OPTS -Xms1G -Xmx3G -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap" \
    SERVER_NAME=localhost \
    CONFIGDBSERVERS=configdb:3306 \
    JDBCPROTOCOL="jdbc:mysql:" \
    DBADDPROPS="" \
    CLIENTID="onesaitplatform" \
    CLIENTSECRET="onesaitplatform" \
    PATH="/usr/local/bin:${PATH}" \
    ENVIRONMENTNAME=""
    
USER onesait

COPY docker-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["docker-entrypoint.sh"]