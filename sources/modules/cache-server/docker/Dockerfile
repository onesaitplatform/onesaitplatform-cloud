FROM openjdk:8-jre-alpine

# metadata
LABEL module.maintainer="onesaitplatform@indra.es" \
	  module.name="cache-server"	

ADD *-exec.jar app.jar

RUN apk add --no-cache tzdata \
 && mkdir -p /var/log/platform-logs \
 && mkdir ./target \
 && addgroup -S onesait -g 433 && adduser -u 431 -S -g onesait -h /usr/local -s /sbin/nologin onesait \
 && chown -R onesait:onesait /usr/local \
 && chown -R onesait:onesait /var/log/platform-logs \
 && chown -R onesait:onesait ./target \
 && chown onesait:onesait app.jar \
 && chmod -R 777 ./target \
 && chmod -R 777 /var/log \
 && chmod -R 777 /usr/local

VOLUME ["/tmp", "/var/log/platform-logs"]
    
USER onesait

EXPOSE 20001 5701

#HZ_SERVICE_DISCOVERY_STRATEGY can take values: service or zookeeper

ENV JAVA_OPTS ${JAVA_OPTS} \
    -server \
    -Xms1G -Xmx3G \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
    -XX:+UseStringDeduplication -XX:TieredStopAtLevel=1 -noverify

ENV SERVER_NAME=localhost \
    HZ_SERVICE_DISCOVERY_STRATEGY=service \
    HZ_ZOOKEEPER_URL=zookeeper:2181

ENTRYPOINT java $JAVA_OPTS -Dspring.application.json=$ONESAIT_PROPERTIES -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /app.jar
